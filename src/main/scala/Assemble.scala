package lnic

import Chisel._

import chisel3.{VecInit, SyncReadMem}
import chisel3.experimental._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.rocket._
import freechips.rocketchip.rocket.NetworkHelpers._
import freechips.rocketchip.rocket.LNICRocketConsts._
import LNICConsts._

/**
 * LNIC Assemble classes.
 *
 * Tasks:
 *   - Reassemble potentially multi-pkt msgs then deliver to CPU
 *   - Reverse byte order of data going to CPU
 *   - Allocate rx_msg_ids and buffers to msgs
 */
class AssembleIO extends Bundle {
  val net_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val meta_in = Flipped(Valid(new PISAIngressMetaOut))
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val meta_out = Valid(new AssembleMetaOut)
  val get_rx_msg_info = Flipped(new GetRxMsgInfoIO)

  override def cloneType = new AssembleIO().asInstanceOf[this.type]
}

class AssembleMetaOut extends Bundle {
  val app_hdr = new RxAppHdr
  val dst_context = UInt(LNIC_CONTEXT_BITS.W)
}

class BufInfoTableEntry extends Bundle {
  val buf_ptr = UInt(BUF_PTR_BITS.W)
  val buf_size = UInt(MSG_LEN_BITS.W)
  // index of the corresponding size_class_freelist
  val size_class = UInt(SIZE_CLASS_BITS.W)
}

class RxMsgIdTableEntry extends Bundle {
  val valid = Bool()
  val rx_msg_id = UInt(MSG_ID_BITS.W)
}

// The first word delivered to the application at the start of
// every received msg
class RxAppHdr extends Bundle {
  val src_ip = UInt(32.W)
  val src_context = UInt(LNIC_CONTEXT_BITS.W)
  val msg_len = UInt(MSG_LEN_BITS.W)
}

// The RxMsgDescriptor is the element that is actually scheduled.
// It is inserted into the scheduler when the msg is fully reassembled.
// When the scheduler selects a descriptor the dequeue logic uses
// the info to deliver the indicated msg to the CPU.
class RxMsgDescriptor extends Bundle {
  val rx_msg_id = UInt(MSG_ID_BITS.W)
  val tx_msg_id = UInt(MSG_ID_BITS.W)
  val size_class = UInt(SIZE_CLASS_BITS.W)
  val buf_ptr = UInt(BUF_PTR_BITS.W)
  val dst_context = UInt(LNIC_CONTEXT_BITS.W)
  val rx_app_hdr = new RxAppHdr()
}

@chiselName
class LNICAssemble(implicit p: Parameters) extends Module {
  val io = IO(new AssembleIO)

  /* Memories (i.e. tables) and Queues */
  // freelist to keep track of available rx_msg_ids
  val rx_msg_ids = for (id <- 0 until NUM_MSG_BUFFERS) yield id.U(log2Up(NUM_MSG_BUFFERS).W)
  val rx_msg_id_freelist = Module(new FreeList(rx_msg_ids))
  // table mapping unique msg identifier to rx_msg_id
  // TODO(sibanez): this should eventually turn into a D-left exact-match table
  val rx_msg_id_table = Module(new TrueDualPortRAM((new RxMsgIdTableEntry).getWidth, NUM_MSG_BUFFERS))
  rx_msg_id_table.io.clock := clock
  rx_msg_id_table.io.reset := reset
  rx_msg_id_table.io.portA.we := false.B
  rx_msg_id_table.io.portB.we := false.B

  // RAM used to store msgs while they are being reassembled and delivered to the CPU.
  //   Msgs are stored in words that are the same size as the datapath width.
  val msg_buffer_ram = SyncReadMem(NUM_MSG_BUFFER_WORDS, UInt(NET_DP_BITS.W))
  // table mapping {rx_msg_id => received_bitmap}
  val received_table = Module(new TrueDualPortRAM(MAX_SEGS_PER_MSG, NUM_MSG_BUFFERS))
  received_table.io.clock := clock
  received_table.io.reset := reset
  received_table.io.portA.we := false.B
  received_table.io.portB.we := false.B
  // table mapping {rx_msg_id => buffer info}
  val buf_info_table = SyncReadMem(NUM_MSG_BUFFERS, new BufInfoTableEntry())

  // Vector of Regs containing the buffer size of each size class
  val size_class_buf_sizes = RegInit(VecInit(MSG_BUFFER_COUNT.map({ case (size: Int, count: Int) => size.U(MSG_LEN_BITS.W) }).toSeq))
  // Vector of freelists to keep track of available buffers to store msgs.
  //   There is one free list for each size class.
  val size_class_freelists_io = MsgBufHelpers.make_size_class_freelists()

  // FIFO queue to schedule delivery of fully assembled msgs to the CPU
  // TODO(sibanez): this should become a PIFO ideally, or at least per-context queues each with an associated priority
  val scheduled_msgs_enq = Wire(Decoupled(new RxMsgDescriptor))
  val scheduled_msgs_deq = Wire(Flipped(Decoupled(new RxMsgDescriptor)))
  scheduled_msgs_deq <> Queue(scheduled_msgs_enq, NUM_MSG_BUFFERS)

  // defaults
  rx_msg_id_freelist.io.enq.valid := false.B
  rx_msg_id_freelist.io.deq.ready := false.B
  size_class_freelists_io.foreach( io => {
    io.enq.valid := false.B
    io.deq.ready := false.B
  })

  /* GetRxMsgInfo State Machine:
   *   - Process get_rx_msg_info() extern function calls
   *   - Returns rx_msg_id (or all 1's if failure)
   *   - Initialize rx msg state
   *
   * Description states:
   * sLookupMsg:
   *   - Lookup the unique msg identifier in the rx_msg_id_table: key = {src_ip, src_context, tx_msg_id}
   *   - Perform computations to see if there is a buffer and rx_msg_id available (in case this is a new msg)
   * sWriteback:
   *   - Return result of extern function call: {failure, rx_msg_id}
   *   - Write rx_msg_id_table[key] = rx_msg_id
   *   - Write buf_info_table[rx_msg_id] = buf_info
   *   - Write received_table[rx_msg_id] = 0
   *   - Write app_hdr_table[rx_msg_id] = {src_ip, src_context, msg_len}
   * TODO(sibanez): Need to read received_table[rx_msg_id] in sWriteback and add a
   *   pipeline stage to process the received bitmap and return the result.
   */
  val sLookupMsg :: sWriteback :: Nil = Enum(2)
  val stateRxMsgInfo = RegInit(sLookupMsg)

  // register the extern function call request parameters
  // NOTE: this assumes requests will not arrive on back-to-back cycles - TODO: may want to check this assumption
  val get_rx_msg_info_req_reg = RegNext(io.get_rx_msg_info.req)

  // TODO(sibanez): update msg_key to include src_ip and src_context,
  //   which requires rx_msg_id_table to become a D-left lookup table.
  val msg_key = Wire(UInt())
  val msg_key_reg = RegNext(msg_key)
  val cur_rx_msg_id_table_entry = Wire(new RxMsgIdTableEntry())

  // defaults
  msg_key := get_rx_msg_info_req_reg.bits.tx_msg_id
  rx_msg_id_table.io.portA.addr := msg_key
  io.get_rx_msg_info.resp.valid := false.B

  // Initialize rx_msg_id_table so that all entries are invalid
  val init_done_reg = RegInit(false.B)
  MemHelpers.memory_init(rx_msg_id_table.io.portA, NUM_MSG_BUFFERS, 0.U, init_done_reg)

  // True if both an rx_msg_id and buffer are available for this msg
  val allocation_success_reg = RegInit(false.B)
  // The size_class from which to allocate a buffer, only valid if allocation_success_reg === true.B
  val size_class_reg = RegInit(0.U)
  // bitmap of valid signals for all size classes
  val free_classes = VecInit(size_class_freelists_io.map(_.deq.valid))
  // bitmap of size_classes that are large enough to store the whole msg
  val candidate_classes = VecInit(size_class_buf_sizes.map(_ >= get_rx_msg_info_req_reg.bits.msg_len))
  // bitmap indicates classes with available buffers that are large enough
  val available_classes = free_classes.asUInt & candidate_classes.asUInt

  switch (stateRxMsgInfo) {
    is (sLookupMsg) {
      // wait for a request to arrive
      when (get_rx_msg_info_req_reg.valid) {
        stateRxMsgInfo := sWriteback
        // read rx_msg_id_table[msg_key] => msg_key is updated on this cycle, result is available on the next one
        // check if there is an available buffer for this msg
        allocation_success_reg := available_classes > 0.U
        // find the smallest available buffer that can hold the msg
        size_class_reg := PriorityEncoder(available_classes)
      }
    }
    is (sWriteback) {
      stateRxMsgInfo := sLookupMsg
      // return extern call response
      io.get_rx_msg_info.resp.valid := !(reset.toBool)
      // Get result of reading the rx_msg_id_table
      cur_rx_msg_id_table_entry := (new RxMsgIdTableEntry).fromBits(rx_msg_id_table.io.portA.dout)
      when (cur_rx_msg_id_table_entry.valid) {
        // This msg has already been allocated an rx_msg_id
        io.get_rx_msg_info.resp.bits.fail := false.B
        io.get_rx_msg_info.resp.bits.rx_msg_id := cur_rx_msg_id_table_entry.rx_msg_id
        io.get_rx_msg_info.resp.bits.is_new_msg := false.B
      } .elsewhen (allocation_success_reg) {
        // This is a new msg and we can allocate a buffer and rx_msg_id
        io.get_rx_msg_info.resp.bits.fail := false.B
        val rx_msg_id = rx_msg_id_freelist.io.deq.bits
        io.get_rx_msg_info.resp.bits.rx_msg_id := rx_msg_id
        io.get_rx_msg_info.resp.bits.is_new_msg := true.B
        // read from rx_msg_id freelist
        assert(rx_msg_id_freelist.io.deq.valid, "There is an available buffer but not an available rx_msg_id?")
        rx_msg_id_freelist.io.deq.ready := true.B
        // update rx_msg_id_table
        val new_rx_msg_id_table_entry = Wire(new RxMsgIdTableEntry())
        new_rx_msg_id_table_entry.valid := true.B
        new_rx_msg_id_table_entry.rx_msg_id := rx_msg_id
        rx_msg_id_table.io.portA.addr := msg_key_reg
        rx_msg_id_table.io.portA.we := true.B
        rx_msg_id_table.io.portA.din := new_rx_msg_id_table_entry.asUInt
        // update buf_info_table
        val target_freelist = size_class_freelists_io(size_class_reg)
        target_freelist.deq.ready := true.B // read from freelist
        val new_buf_info_table_entry = Wire(new BufInfoTableEntry())
        new_buf_info_table_entry.buf_ptr := target_freelist.deq.bits
        new_buf_info_table_entry.buf_size := size_class_buf_sizes(size_class_reg)
        new_buf_info_table_entry.size_class := size_class_reg
        buf_info_table(rx_msg_id) := new_buf_info_table_entry
        // update received_table
        received_table.io.portA.addr := rx_msg_id
        received_table.io.portA.we := true.B
        received_table.io.portA.din := 0.U
      } .otherwise {
        // This is a new msg and we cannot allocate a buffer and rx_msg_id
        io.get_rx_msg_info.resp.bits.fail := true.B
        io.get_rx_msg_info.resp.bits.rx_msg_id := 0.U
        io.get_rx_msg_info.resp.bits.is_new_msg := true.B
      }
    }
  }

  // TODO(sibanez): this state machine exerts backpressure on the first cycle of every pkt.
  //   It does this because it needs 2 cycles to perform RMW of received_table[rx_msg_id].
  //   There is a way to avoid backpressuring all but single-cycle pkts, but is it worth implementing?
  //   This will still process pkts at line rate if the clock is 400MHz.
  /* Enqueue State Machine:
   *   - Enqueue incomming pkts into the appropriate msg buffer
   *   - Mark the corresponding pkt as having been received
   *
   * Description states:
   * sEnqStart state:
   *   - Lookup buf_info_table[rx_msg_id]
   *   - Lookup received_table[rx_msg_id]
   *   - Exert backpressure
   *
   * sEnqWordOne state:
   *   - Write received_table[rx_msg_id] |= (1 << pkt_offset)
   *   - Write msg_buffer_ram[buf_ptr + pkt_offset*max_words_per_pkt] = first word of pkt
   *   - Check if the full msg has been received
   *   - Schedule msg for delivery to CPU if needed
   *
   * sEnqFinishPkt state:
   *   - Write the rest of the words of the pkt into the msg buffer
   *   - Schedule msg for delivery to CPU if needed
   */
  val sEnqStart :: sEnqWordOne :: sEnqFinishPkt :: Nil = Enum(3)
  val stateEnq = RegInit(sEnqStart)

  val meta_in_bits_reg = Reg(new PISAIngressMetaOut())

  val max_words_per_pkt = MAX_SEG_LEN_BYTES/NET_DP_BYTES
  require(isPow2(max_words_per_pkt))

  val enq_rx_msg_id = Wire(UInt(MSG_ID_BITS.W))
  // buf_info_table read port
  val enq_buf_info_table_port = buf_info_table(enq_rx_msg_id)
  val buf_info = Wire(new BufInfoTableEntry())
  val buf_info_reg = Reg(new BufInfoTableEntry())
  // received_table read result
  val enq_received = Wire(UInt(MAX_SEGS_PER_MSG.W))
  // msg_buffer_ram write port
  val pkt_word_ptr = Wire(UInt(BUF_PTR_BITS.W))
  val enq_msg_buffer_ram_port = msg_buffer_ram(pkt_word_ptr)

  val msg_complete_reg = Reg(Bool())
  val pkt_word_count = RegInit(0.U(log2Up(max_words_per_pkt).W))

  // defaults
  io.net_in.ready := true.B
  enq_rx_msg_id := io.meta_in.bits.rx_msg_id
  received_table.io.portB.addr := enq_rx_msg_id
  scheduled_msgs_enq.valid := false.B

  switch (stateEnq) {
    is (sEnqStart) {
        // wait for the first word to arrive
        when (io.net_in.valid) {
            stateEnq := sEnqWordOne
            // backpressure one cycle
            io.net_in.ready := false.B
            // register pkt metadata
            meta_in_bits_reg := io.meta_in.bits
        }
    }
    is (sEnqWordOne) {
        // NOTE: this assumes that io.net_in and io.meta_in are the same as they were in the sEnqStart state.
        // write pkt word into msg buffer
        buf_info := enq_buf_info_table_port
        buf_info_reg := buf_info
        val pkt_ptr = compute_pkt_ptr(buf_info.buf_ptr, io.meta_in.bits.pkt_offset)
        pkt_word_ptr := pkt_ptr
        enq_msg_buffer_ram_port := io.net_in.bits.data
        pkt_word_count := 1.U
        // mark pkt as received
        enq_received := received_table.io.portB.dout
        val new_enq_received = Wire(UInt(MAX_SEGS_PER_MSG.W))
        new_enq_received := enq_received | (1.U << io.meta_in.bits.pkt_offset)
        received_table.io.portB.we := !reset.toBool
        received_table.io.portB.din := new_enq_received
        // check if the whole msg has been received
        val num_pkts = MsgBufHelpers.compute_num_pkts(io.meta_in.bits.msg_len)
        val all_pkts = Wire(UInt(MAX_SEGS_PER_MSG.W))
        all_pkts := (1.U << num_pkts) - 1.U
        val msg_complete = (new_enq_received === all_pkts)
        msg_complete_reg := msg_complete
        // state transition
        when (io.net_in.bits.last) {
            stateEnq := sEnqStart
            when (msg_complete) {
                // schedule msg for delivery to the CPU
                schedule_msg(meta_in_bits_reg.dst_context,
                             meta_in_bits_reg.rx_msg_id,
                             meta_in_bits_reg.tx_msg_id,
                             buf_info.buf_ptr,
                             buf_info.size_class,
                             meta_in_bits_reg.src_ip,
                             meta_in_bits_reg.src_context,
                             meta_in_bits_reg.msg_len)
            }
        } .otherwise {
            stateEnq := sEnqFinishPkt
        }
    }
    is (sEnqFinishPkt) {
        // write pkt word into msg buffer
        when (io.net_in.valid) {
            val pkt_ptr = compute_pkt_ptr(buf_info_reg.buf_ptr, meta_in_bits_reg.pkt_offset)
            pkt_word_ptr := pkt_ptr + pkt_word_count
            // Make sure we are not writing beyond the buffer
            when (pkt_word_count < (buf_info_reg.buf_size >> log2Up(NET_DP_BYTES))) {
                enq_msg_buffer_ram_port := io.net_in.bits.data
                pkt_word_count := pkt_word_count + 1.U
            }
            when (io.net_in.bits.last) {
                // state transition
                stateEnq := sEnqStart
                when (msg_complete_reg) {
                    // schedule msg for delivery to the CPU
                    schedule_msg(meta_in_bits_reg.dst_context,
                                 meta_in_bits_reg.rx_msg_id,
                                 meta_in_bits_reg.tx_msg_id,
                                 buf_info_reg.buf_ptr,
                                 buf_info_reg.size_class,
                                 meta_in_bits_reg.src_ip,
                                 meta_in_bits_reg.src_context,
                                 meta_in_bits_reg.msg_len)
                }
            }
        }
    }
  }

  def compute_pkt_ptr(buf_ptr: UInt, pkt_offset: UInt) = {
    val pkt_ptr = buf_ptr + (pkt_offset<<log2Up(max_words_per_pkt).U)
    pkt_ptr
  }

  def schedule_msg(dst_context: UInt, rx_msg_id: UInt, tx_msg_id: UInt, buf_ptr: UInt, size_class: UInt, src_ip: UInt, src_context: UInt, msg_len: UInt) = {
    // TODO(sibanez): this should be inserting into a PIFO or per-context queues rather than
    //   a single fifo queue. This is just a temporary simplification.
    assert (scheduled_msgs_enq.ready, "scheduled_msgs FIFO is full when trying to schedule a msg")
    scheduled_msgs_enq.valid := true.B
    scheduled_msgs_enq.bits.rx_msg_id := rx_msg_id
    scheduled_msgs_enq.bits.tx_msg_id := tx_msg_id
    scheduled_msgs_enq.bits.size_class := size_class
    scheduled_msgs_enq.bits.buf_ptr := buf_ptr
    scheduled_msgs_enq.bits.dst_context := dst_context
    scheduled_msgs_enq.bits.rx_app_hdr.src_ip := src_ip
    scheduled_msgs_enq.bits.rx_app_hdr.src_context := src_context
    scheduled_msgs_enq.bits.rx_app_hdr.msg_len := msg_len
  }

  /* Delivery State Machine:
   *   - Schedule and perform delivery of fully reassembled msgs to the CPU.
   *   - The order in which msgs are delivered to the CPU should be determined by the priority of the dst_context.
   *   - Free rx_msg_id and msg buffer after the msg is delivered to the CPU
   *   - TODO(sibanez): implement msg delivery scheduling to improve performance (rather than FIFO).
   *
   * Description of states:
   * sScheduleMsg:
   *   - Wait for a msg descriptor to arrive in the scheduled_msgs fifo
   *   - Read the first word of the msg from the msg buffer
   *
   * sDeliverMsg:
   *   - Deliver the selected msg to the CPU
   *   - Reverse byte order of all words
   *   - Free the rx_msg_id and the msg buffer
   */
  val sScheduleMsg :: sDeliverMsg :: Nil = Enum(2)
  val stateDeq = RegInit(sScheduleMsg)

  // message buffer read port
  val deq_buf_word_ptr = Wire(UInt())
  deq_buf_word_ptr := scheduled_msgs_deq.bits.buf_ptr // default
  val deq_msg_buf_ram_port = msg_buffer_ram(deq_buf_word_ptr)

  val msg_desc_reg = RegInit((new RxMsgDescriptor).fromBits(0.U))
  val msg_word_count = RegInit(0.U(MSG_LEN_BITS.W))
  val rem_bytes_reg = RegInit(0.U(MSG_LEN_BITS.W))

  // defaults
  io.net_out.valid := false.B
  io.net_out.bits.keep := NET_DP_FULL_KEEP
  io.net_out.bits.last := false.B
  scheduled_msgs_deq.ready := false.B

  // NOTE: this field is used by GlobalRxQueues to drive io.net_out.ready
  io.meta_out.valid := true.B
  io.meta_out.bits.app_hdr := msg_desc_reg.rx_app_hdr
  io.meta_out.bits.dst_context := msg_desc_reg.dst_context

  switch (stateDeq) {
    is (sScheduleMsg) {
      // Wait for a msg descriptor to be scheduled
      when (scheduled_msgs_deq.valid) {
        // read the head scheduled msg
        scheduled_msgs_deq.ready := true.B
        // init regs
        msg_desc_reg := scheduled_msgs_deq.bits
        msg_word_count := 0.U
        rem_bytes_reg := scheduled_msgs_deq.bits.rx_app_hdr.msg_len
        // state transition
        stateDeq := sDeliverMsg
      }
    }
    is (sDeliverMsg) {
      io.net_out.valid := true.B
      // read current buffer word
      io.net_out.bits.data := deq_msg_buf_ram_port
      val is_last_word = rem_bytes_reg <= NET_DP_BYTES.U
      io.net_out.bits.keep := Mux(is_last_word,
                                  (1.U << rem_bytes_reg) - 1.U,
                                  NET_DP_FULL_KEEP)
      io.net_out.bits.last := is_last_word
      when (io.net_out.ready) {
        // move to the next word
        deq_buf_word_ptr := msg_desc_reg.buf_ptr + msg_word_count + 1.U
        msg_word_count := msg_word_count + 1.U
        rem_bytes_reg := rem_bytes_reg - NET_DP_BYTES.U
        when (is_last_word) {
          // state transition
          stateDeq := sScheduleMsg
          // free the rx_msg_id and the msg buffer
          rx_msg_id_freelist.io.enq.valid := true.B
          rx_msg_id_freelist.io.enq.bits := msg_desc_reg.rx_msg_id
          val target_buf_freelist_io = size_class_freelists_io(msg_desc_reg.size_class)
          target_buf_freelist_io.enq.valid := true.B
          target_buf_freelist_io.enq.bits := msg_desc_reg.buf_ptr
          // mark the corresponding entry in rx_msg_id_table as invalid
          // TODO(sibanez): this will eventually turn into a D-left table ...
          rx_msg_id_table.io.portB.addr := msg_desc_reg.tx_msg_id
          rx_msg_id_table.io.portB.we   := true.B
          rx_msg_id_table.io.portB.din  := 0.U
        }
      } .otherwise {
        // stay at the same word
        deq_buf_word_ptr := msg_desc_reg.buf_ptr + msg_word_count
      }
    }
  }

}


