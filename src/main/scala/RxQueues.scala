package lnic

import Chisel._

import chisel3.{VecInit, SyncReadMem}
import chisel3.experimental._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.subsystem.RocketTilesKey
import freechips.rocketchip.rocket._
import freechips.rocketchip.rocket.NetworkHelpers._
import freechips.rocketchip.rocket.LNICRocketConsts._
import LNICConsts._

/**
 * Global Rx Queues
 *
 * This module stores global per-context (or per-application) queues.
 * Tasks:
 *   - Receive msgs from the assemly module, each indicates the destination context ID
 *   - Store the msg in the appropriate RX queue
 *   - Parameter: # of msgs outstanding at each core for each context ID (i.e. app)
 *   - Keep track of the # of outstanding msgs at each core for each context ID
 *   - When msg arrives:
 *     - Check to see if it can be immediately forwarded to one of the cores
 *   - When core indicates that it has finished processing a msg (also indicates context ID):
 *     - Check if there is another msg for this context ID that can be sent to the core
 */

class GlobalRxQueuesIO(implicit p: Parameters) extends Bundle {
  val net_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val meta_in = Flipped(Valid(new AssembleMetaOut))

  val num_cores = p(RocketTilesKey).size
  val net_out = Vec(num_cores, Decoupled(new StreamChannel(XLEN)))
  val meta_out = Vec(num_cores, Valid(new LNICRxMsgMeta))

  // core tells NIC to insert (i.e. register) provided port # (if not already inserted)
  val add_context = Vec(num_cores, Flipped(Valid(UInt(LNIC_CONTEXT_BITS.W))))
  // core tells NIC that it has finished processing
  val get_next_msg = Vec(num_cores, Flipped(Valid(UInt(LNIC_CONTEXT_BITS.W))))
}

class RxFIFOWord(val wordBits: Int, val ptrBits: Int) extends Bundle {
  val word = new StreamChannel(wordBits)
  val next = UInt(width = ptrBits)
  val app_hdr = new RxAppHdr

  override def cloneType = new RxFIFOWord(wordBits, ptrBits).asInstanceOf[this.type]
}

@chiselName
class GlobalRxQueues(implicit p: Parameters) extends Module {
  val io = IO(new GlobalRxQueuesIO)

  val num_contexts = p(LNICRocketKey).get.maxNumContexts
  val num_cores = p(RocketTilesKey).size

  val num_entries = (MAX_MSG_SIZE_BYTES/NET_DP_BYTES)*num_contexts*MAX_RX_MAX_MSGS_PER_CONTEXT
  val ptr_bits = log2Ceil(num_entries)

  // tables to index head and tail of FIFO queue for each context
  // NOTE: HeadTableEntry, TailTableEntry, and FIFOWord are defined in rocket.LNICQueues
  val head_table = RegInit(VecInit(Seq.fill(num_contexts)((new HeadTableEntry(ptr_bits)).fromBits(0.U))))
  val tail_table = RegInit(VecInit(Seq.fill(num_contexts)((new TailTableEntry(ptr_bits)).fromBits(0.U))))
  // NOTE: store msg as 512-bit words
  val ram = SyncReadMem(num_entries, new RxFIFOWord(NET_DP_BITS, ptr_bits))
  // create read / write ports
  val tail_ptr = Wire(UInt(ptr_bits.W))
  val ram_wr_port = ram(tail_ptr)
  val head_ptr = Wire(UInt(ptr_bits.W))
  val ram_rd_port = ram(head_ptr)

  // create free list for msg words
  val entries = for (i <- 0 until num_entries) yield i.U(log2Up(num_entries).W)
  val freelist = Module(new FreeList(entries))

  // divide the buffer space equally amongst contexts
  // NOTE: this may change in the future, but it's easy for now
  val max_qsize = num_entries/num_contexts

  // table mapping {context ID => core bitmap}
  val core_table = RegInit(VecInit(Seq.fill(num_contexts)(0.U(num_cores.W))))
  // table mapping {(context ID, core ID) => outstanding msg count}
  val msg_count_table = Reg(Vec(num_contexts, Vec(num_cores, UInt((log2Up(MAX_OUTSTANDING_MSGS) + 1).W))))
  when (reset.toBool) {
    for (i <- 0 until num_contexts) {
      for (j <- 0 until num_cores) {
        msg_count_table(i)(j) := 0.U
      }
    }
  }
  // table mapping {context ID => word count in the queue}
  val word_count_table = RegInit(VecInit(Seq.fill(num_contexts)(0.U((log2Up(max_qsize + 1)).W))))

  val new_head_entry = Wire(new HeadTableEntry(ptr_bits))
  val cur_tail_entry = Wire(new TailTableEntry(ptr_bits))
  val new_tail_entry = Wire(new TailTableEntry(ptr_bits))

  val do_enq = Wire(Bool())
  val do_deq = Wire(Bool())
  do_enq := false.B // default
  do_deq := false.B // default

  val enq_context = Wire(UInt(LNIC_CONTEXT_BITS.W))
  enq_context := io.meta_in.bits.dst_context // default
  val deq_context = Wire(UInt(LNIC_CONTEXT_BITS.W))

  // queues to store get_next_msg requests from cores
  val get_next_msg_cmd_enq = Wire(Vec(num_cores, Decoupled(UInt(LNIC_CONTEXT_BITS.W))))
  val get_next_msg_cmd_deq = Wire(Vec(num_cores, Flipped(Decoupled(UInt(LNIC_CONTEXT_BITS.W)))))
  for (i <- 0 until num_cores) yield {
    get_next_msg_cmd_enq(i).valid := io.get_next_msg(i).valid
    get_next_msg_cmd_enq(i).bits := io.get_next_msg(i).bits
    when (get_next_msg_cmd_enq(i).valid) {
      assert(get_next_msg_cmd_enq(i).ready, "GlobalRxQueues: get_next_msg_cmd queue is full on enqueue!")
    }
    get_next_msg_cmd_deq(i) <> Queue(get_next_msg_cmd_enq(i), MAX_OUTSTANDING_MSGS*num_contexts*2)
    get_next_msg_cmd_deq(i).ready := false.B // default
  }

  // queue to store enqueue notifcation cmds
  val enq_cmd_enq = Wire(Decoupled(UInt(LNIC_CONTEXT_BITS.W)))
  val enq_cmd_deq = Wire(Flipped(Decoupled(UInt(LNIC_CONTEXT_BITS.W))))
  // This queue should ideally be large enough to store one cmd for each possible msg in the buffer.
  // If a enq cmd is dropped then the corresponding msg could sit in the buffer until another msg for
  // that context arrives.
  // TODO(sibanez): NUM_MSG_BUFFERS is not really the best way to size this queue, but maybe it'll be ok for now?
  enq_cmd_deq <> Queue(enq_cmd_enq, NUM_MSG_BUFFERS)
  enq_cmd_enq.valid := false.B // default
  enq_cmd_deq.ready := false.B // default

  // defaults
  // do not enq or deq from the free list
  freelist.io.enq.valid := false.B
  freelist.io.enq.bits := 0.U
  freelist.io.deq.ready := false.B

  io.net_in.ready := false.B

  // register add_context cmds from cores so they can be serialized
  // NOTE: this approach is fine as long as the cores do not send a bunch
  //   of back-to-back add_context commands. In that case, some of the
  //   cmds may be dropped.
  //   Maybe update this to use queues rather than just regs?
  val reg_add_context = RegInit(VecInit(Seq.fill(num_cores)((Valid(UInt(LNIC_CONTEXT_BITS.W))).fromBits(0.U))))
  for (i <- 0 until num_cores) {
    when (io.add_context(i).valid) {
      reg_add_context(i) := io.add_context(i)
    }
  }

  val add_context_valids = Wire(Vec(num_cores, Bool()))
  for (i <- 0 until num_cores) { add_context_valids(i) := reg_add_context(i).valid }

  // check if we need to add a context
  val do_add_context = Wire(Bool())
  do_add_context := add_context_valids.asUInt.orR

  // select the core ID to process the add_context cmd
  val core = Wire(UInt())
  core := PriorityEncoder(add_context_valids.asUInt)

  when (do_add_context) {
    // mark this cmd as processed
    reg_add_context(core).valid := false.B
    val new_context = reg_add_context(core).bits
    // allocate a queue for the indicated context ID if one is not already allocated
    when (!head_table(new_context).valid) {
      // this context has not already been added
      // update head_table, try to read from free list
      freelist.io.deq.ready := true.B
      assert (freelist.io.deq.valid, "GlobalRxQueues: freelist is empty during context insertion!")
      head_table(new_context).valid := freelist.io.deq.valid
      head_table(new_context).head := freelist.io.deq.bits
      // update tail_table
      tail_table(new_context).valid := freelist.io.deq.valid
      tail_table(new_context).tail := freelist.io.deq.bits
      // msg / word count
      for (i <- 0 until num_cores) {
        msg_count_table(new_context)(i) := 0.U
      }
      word_count_table(new_context) := 0.U
      core_table(new_context) := UIntToOH(core)
    } .otherwise {
      // this context has already been added
      core_table(new_context) := core_table(new_context) | UIntToOH(core)
    }
  } .otherwise {
    /*****************/
    /* Enqueue Logic */
    /*****************/
    val reg_enq_context = Reg(UInt())

    val sStart :: sEnqueue :: Nil = Enum(2)
    val enqState = RegInit(sStart)

    // make sure there is sufficient room for enqueue
    io.net_in.ready := (enq_context < num_contexts.U && word_count_table(enq_context) < max_qsize.U)

    switch (enqState) {
      is (sStart) {
        when (io.net_in.valid && io.net_in.ready) {
          assert(io.meta_in.valid, "GlobalRxQueues: net_in is valid, but not meta_in on the first word of msg!")
          reg_enq_context := io.meta_in.bits.dst_context
          // enqueue the msg into the buffer
          perform_enq(enq_context)
          // enqueue the enq notification cmd
          enq_cmd_enq.valid := true.B
          enq_cmd_enq.bits := enq_context
          assert(enq_cmd_enq.ready, "GlobalRxQueues: enq_cmd queue is full during enqueue!")
          when (!io.net_in.bits.last) {
            enqState := sEnqueue
          }
        }
      }
      is (sEnqueue) {
        enq_context := reg_enq_context
        when (io.net_in.valid && io.net_in.ready) {
          perform_enq(reg_enq_context)
          when (io.net_in.bits.last) {
            enqState := sStart
          }
        }
      }
    }

    /*****************/
    /* Dequeue Logic */
    /*****************/
    // Logic to compute which core to send msg to when processing end cmd.
    // The target core must be running the context and have fewer than MAX_OUTSTANDING_MSGS msgs outstanding
    val enq_cmd_context = Wire(UInt())
    enq_cmd_context := enq_cmd_deq.bits
    val core_bitmap = core_table(enq_cmd_context)
    val candidate_cores = VecInit(msg_count_table(enq_cmd_context).map( _ < MAX_OUTSTANDING_MSGS.U))
    val available_cores = core_bitmap & candidate_cores.asUInt
    val target_core = Wire(UInt())
    target_core := PriorityEncoder(available_cores)
    val reg_target_core = Reg(UInt())
    val reg_deq_context = Reg(UInt(LNIC_CONTEXT_BITS.W))
    deq_context := reg_deq_context

    val get_next_msg_valids = VecInit(get_next_msg_cmd_deq.map(_.valid))
    val new_msg_core = Wire(UInt())
    new_msg_core := PriorityEncoder(get_next_msg_valids.asUInt)
    val new_msg_context = Wire(UInt())
    new_msg_context := get_next_msg_cmd_deq(new_msg_core).bits

    // 512-bit and 64-bit words from the msg buffer
    val buf_net_out_wide = Wire(Vec(num_cores, Decoupled(new StreamChannel(NET_DP_BITS))))
    val buf_net_out_narrow = Wire(Vec(num_cores, Decoupled(new StreamChannel(XLEN))))

    for (i <- 0 until num_cores) {  
      // 512-bit => 64-bit
      StreamWidthAdapter(buf_net_out_narrow(i), // output
                         buf_net_out_wide(i))   // input
      // defaults
      io.net_out(i) <> buf_net_out_narrow(i)
      io.net_out(i).bits.data := reverse_bytes(buf_net_out_narrow(i).bits.data, XBYTES)
  
      buf_net_out_wide(i).valid := false.B
      buf_net_out_wide(i).bits.keep := NET_DP_FULL_KEEP
      buf_net_out_wide(i).bits.last := false.B

      io.meta_out(i).valid := false.B
    }

    val sIdle :: sDeqStart :: sDeqFinish :: Nil = Enum(3)
    val deqState = RegInit(sIdle)

    switch (deqState) {
      is (sIdle) {
        // Select core to dispatch a msg to
        // Prioritize processing enq notification cmds so that the queue doesn't overflow
        when (enq_cmd_deq.valid) {
          enq_cmd_deq.ready := true.B
          when (available_cores > 0.U) {
            // we found a core to forward a msg to
            reg_target_core := target_core
            reg_deq_context := enq_cmd_context
            // start reading the msg buffer ram (result available on next cycle)
            head_ptr := head_table(enq_cmd_context).head
            assert(head_table(enq_cmd_context).valid, "GlobalRxQueues: Attempting to perform dequeue for invalid context!")
            // increment outstanding msg count
            msg_count_table(enq_cmd_context)(target_core) := msg_count_table(enq_cmd_context)(target_core) + 1.U
            deqState := sDeqStart
          }
        } .elsewhen (get_next_msg_valids.asUInt.orR) {
          // At least one core has indicated that it has finished processing a msg
          get_next_msg_cmd_deq(new_msg_core).ready := true.B
          // Check if we can send another msg to the core, if not then decrement outstanding msg count
          when (word_count_table(new_msg_context) > 0.U) {
            reg_target_core := new_msg_core
            reg_deq_context := new_msg_context
            head_ptr := head_table(new_msg_context).head
            assert(head_table(new_msg_context).valid, "GlobalRxQueues: Attempting to perform dequeue for invalid context!")
            deqState := sDeqStart
          } .otherwise {
            msg_count_table(new_msg_context)(new_msg_core) := msg_count_table(new_msg_context)(new_msg_core) - 1.U
          }
        }
      }
      is (sDeqStart) {
        // write app hdr
        head_ptr := head_table(reg_deq_context).head
        assert(head_table(reg_deq_context).valid, "GlobalRxQueues: Attempting to perform dequeue for invalid context!")
        val deq_fifo_word  = Wire(new RxFIFOWord(NET_DP_BITS, ptr_bits))
        deq_fifo_word := ram_rd_port

        val net_out = io.net_out(reg_target_core)
        val meta_out = io.meta_out(reg_target_core)
        net_out.valid := true.B
        net_out.bits.data := deq_fifo_word.app_hdr.asUInt
        net_out.bits.keep := NET_CPU_FULL_KEEP
        net_out.bits.last := false.B
        when (net_out.ready) {
          deqState := sDeqFinish
        }
      }
      is (sDeqFinish) {
        // write msg body
        val data_valid = word_count_table(reg_deq_context) > 0.U
        val meta_out = io.meta_out(reg_target_core)
        meta_out.valid := data_valid
        meta_out.bits.dst_context := reg_deq_context
        val net_out = buf_net_out_wide(reg_target_core)
        net_out.valid := data_valid

        // lookup the current head of the FIFO for this context
        head_ptr := head_table(reg_deq_context).head
        assert (head_table(reg_deq_context).valid, "GlobalRxQueues: Attempting to perform dequeue for an invalid contextID!")
    
        // read the head word from the RAM
        val deq_fifo_word = Wire(new RxFIFOWord(NET_DP_BITS, ptr_bits))
        deq_fifo_word := ram_rd_port
        net_out.bits := deq_fifo_word.word

        when (net_out.valid && net_out.ready) {
          perform_deq(reg_deq_context, deq_fifo_word.next)
          // start reading the next word
          head_ptr := deq_fifo_word.next
          when (net_out.bits.last) {
            deqState := sIdle
          }
        }
      }
    }

  }

  // Logic to count the size of each FIFO, updated on enq/deq operations
  for (i <- 0 until num_contexts) {
    val enq_inc = Mux(do_enq && (enq_context === i.U), 1.U, 0.U)
    val deq_dec = Mux(do_deq && (deq_context === i.U), 1.U, 0.U)
    word_count_table(i) := word_count_table(i) + enq_inc - deq_dec
  }

  // check to make sure msg_count never exceeds MAX_OUTSTANDING_MSGS
  for (i <- 0 until num_contexts) {
    for (j <- 0 until num_cores) {
      assert(msg_count_table(i)(j) <= MAX_OUTSTANDING_MSGS.U, "GlobalRxQueues: msg_count exceeds MAX_OUTSTANDING_MSGS!")
    }
  }

  def perform_enq(context: UInt) = {
    do_enq := true.B
    // lookup current tail_ptr for this context
    cur_tail_entry := tail_table(context)
    tail_ptr := cur_tail_entry.tail
    assert (cur_tail_entry.valid, "Attempting to perform an enqueue for an invalid contextID")

    // read free list
    val tail_ptr_next = freelist.io.deq.bits
    freelist.io.deq.ready := true.B
    assert (freelist.io.deq.valid, "Free list is empty during msg enqueue!")

    // write new word to RAM
    val enq_fifo_word = Wire(new RxFIFOWord(NET_DP_BITS, ptr_bits))
    enq_fifo_word.word := io.net_in.bits
    enq_fifo_word.app_hdr := io.meta_in.bits.app_hdr
    enq_fifo_word.next := tail_ptr_next
    // write the new word to the ram
    ram_wr_port := enq_fifo_word

    // update tail ptr
    new_tail_entry := cur_tail_entry // default
    new_tail_entry.tail := tail_ptr_next
    tail_table(context) := new_tail_entry
  }

  def perform_deq(context: UInt, new_head: UInt) = {
    do_deq := true.B

    // add current head to free list
    freelist.io.enq.valid := true.B
    freelist.io.enq.bits := head_table(context).head
    assert (freelist.io.enq.ready, "GlobalRxQueues: Free list is full during dequeue!")

    // update head table
    head_table(context).head := new_head
  }
  
}

