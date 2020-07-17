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
 * LNIC Packetization Module
 *
 * Tasks:
 *   - Reverse byte order of words coming from CPU
 *   - Consume message words from the CPU and transform into pkts
 *   - Store msgs and transmit pkts, also support retransmitting pkts when told to do so
 */

class PacketizeIO(implicit p: Parameters) extends Bundle {
  val num_cores = p(RocketTilesKey).size

  val net_in = Vec(num_cores, Flipped(Decoupled(new LNICTxMsgWord)))
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val meta_out = Valid(new PISAEgressMetaIn)
  // Events
  val delivered = Flipped(Valid(new DeliveredEvent))
  val creditToBtx = Flipped(Valid(new CreditToBtxEvent))
  val schedule = Valid(new ScheduleEvent)
  val reschedule = Valid(new ScheduleEvent)
  val cancel = Valid(new CancelEvent)
  val timeout = Flipped(Valid(new TimeoutEvent))
  val timeout_cycles = Input(UInt(TIMER_BITS.W))
  val rtt_pkts = Input(UInt(CREDIT_BITS.W))}

class TxMsgDescriptor extends Bundle {
  val tx_msg_id   = UInt(MSG_ID_BITS.W)
  val buf_ptr     = UInt(BUF_PTR_BITS.W)
  val size_class  = UInt(SIZE_CLASS_BITS.W)
  val tx_app_hdr  = new TxAppHdr()
  val src_context = UInt(LNIC_CONTEXT_BITS.W)
}

/* Descriptor that is used to schedule TX pkts */
class TxPktDescriptor extends Bundle {
  val msg_desc    = new TxMsgDescriptor  
  val tx_pkts     = UInt(MAX_SEGS_PER_MSG.W)
}

/* State maintained per-context on enqueue */
class ContextEnqState extends Bundle {
  val msg_desc = new TxMsgDescriptor
  val pkt_offset = UInt(PKT_OFFSET_BITS.W)
  val rem_bytes  = UInt(MSG_LEN_BITS.W)
  val pkt_bytes  = UInt(16.W)
  val word_count = UInt(16.W)
}

@chiselName
class LNICPacketize(implicit p: Parameters) extends Module {
  val num_contexts = p(LNICRocketKey).get.maxNumContexts
  val num_cores = p(RocketTilesKey).size

  val io = IO(new PacketizeIO)

  /* Memories (i.e. tables) and Queues */
  // freelist to keep track of available tx_msg_ids
  val tx_msg_ids = for (id <- 0 until NUM_MSG_BUFFERS) yield id.U(log2Up(NUM_MSG_BUFFERS).W)
  val tx_msg_id_freelist = Module(new FreeList(tx_msg_ids))
  // RAM used to store msgs while they are being reassembled and delivered to the CPU.
  //   Msgs are stored in words that are the same size as the datapath width.
  val msg_buffer_ram = SyncReadMem(NUM_MSG_BUFFER_WORDS, Vec(NET_DP_BITS/XLEN, UInt(XLEN.W)))

  // Vector of Regs containing the buffer size of each size class
  val size_class_buf_sizes = RegInit(VecInit(MSG_BUFFER_COUNT.map({ case (size: Int, count: Int) => size.U(MSG_LEN_BITS.W) }).toSeq))
  // Vector of freelists to keep track of available buffers to store msgs.
  //   There is one free list for each size class.
  val size_class_freelists_io = MsgBufHelpers.make_size_class_freelists() 

  /* State required for transport support */
  // table mapping {tx_msg_id => delivered_bitmap}
  val delivered_table = SyncReadMem(NUM_MSG_BUFFERS, UInt(MAX_SEGS_PER_MSG.W))
  // table mapping {tx_msg_id => credit}
  val credit_table = Module(new TrueDualPortRAM(CREDIT_BITS, NUM_MSG_BUFFERS))
  credit_table.io.clock := clock
  credit_table.io.reset := reset
  credit_table.io.portA.we := false.B
  credit_table.io.portB.we := false.B
  // table mapping {tx_msg_id => toBtx_bitmap}
  val toBtx_table = Module(new TrueDualPortRAM(MAX_SEGS_PER_MSG, NUM_MSG_BUFFERS))
  toBtx_table.io.clock := clock
  toBtx_table.io.reset := reset
  toBtx_table.io.portA.we := false.B
  toBtx_table.io.portB.we := false.B
  // table mapping {tx_msg_id => max pkt offset of the msg}
  val max_tx_pkt_offset_table = SyncReadMem(NUM_MSG_BUFFERS, UInt(PKT_OFFSET_BITS.W))

  // Queues to schedule delivery of TX pkts
  // TODO(sibanez): these should become a PIFO ideally
  // Queue used for pkts scheduled by Enqueue state machine
  val init_scheduled_pkts_enq = Wire(Decoupled(new TxPktDescriptor))
  val init_scheduled_pkts_deq = Wire(Flipped(Decoupled(new TxPktDescriptor)))
  init_scheduled_pkts_deq <> Queue(init_scheduled_pkts_enq, SCHEDULED_PKTS_Q_DEPTH)
  // Queue used for pkts scheduled by creditToBtx state machine
  val credit_scheduled_pkts_enq = Wire(Decoupled(new TxPktDescriptor))
  val credit_scheduled_pkts_deq = Wire(Flipped(Decoupled(new TxPktDescriptor)))
  credit_scheduled_pkts_deq <> Queue(credit_scheduled_pkts_enq, SCHEDULED_PKTS_Q_DEPTH)
  // Queue used for pkts schedule by timeout state machine
  val timeout_scheduled_pkts_enq = Wire(Decoupled(new TxPktDescriptor))
  val timeout_scheduled_pkts_deq = Wire(Flipped(Decoupled(new TxPktDescriptor)))
  timeout_scheduled_pkts_deq <> Queue(timeout_scheduled_pkts_enq, SCHEDULED_PKTS_Q_DEPTH)

  // defaults
  tx_msg_id_freelist.io.enq.valid := false.B
  tx_msg_id_freelist.io.deq.ready := false.B
  size_class_freelists_io.foreach( io => {
    io.enq.valid := false.B
    io.deq.ready := false.B
  })
  credit_scheduled_pkts_enq.valid := false.B

  init_scheduled_pkts_deq.ready := false.B
  credit_scheduled_pkts_deq.ready := false.B
  timeout_scheduled_pkts_deq.ready := false.B

  /**
   * Msg Enqueue State Machine.
   * Tasks:
   *   - Wait for MsgWord from the TxQueue
   *   - When a MsgWord arrives, record msg_len and keep track of remaining_bytes
   *   - On the first word of a msg, allocate a buffer and tx_msg_id
   *   - For subsequent words of the msg, write into the appropriate buffer location
   *   - When either (1) an MTU has been accumulated, or (2) the full msg has arrived,
   *     schedule a tx pkt descriptor for transmission
   *
   * NOTE: All state variables are per-context and per-core
   *
   * Description of states:
   * sWaitAppHdr:
   *   - Wait for TxAppHdr to arrive (the first MsgWord of a msg)
   *   - If there is no buffer available to store this msg => assert backpressure
   *   - Otherwise, allocate a buffer and tx_msg_id and store in per-context state
   *
   * sWriteMsg:
   *   - Wait for a MsgWord to arrive
   *   - Write the MsgWord into the correct buffer location (using a masked write)
   *   - When either (1) an MTU has been accumulated, or (2) the full msg has arrived,
   *     schedule a tx pkt descriptor for transmission
   */
  val sWaitAppHdr :: sWriteMsg :: Nil = Enum(2)
  // need one state per-context and per-core
  val enqStates = Reg(Vec(num_contexts, Vec(num_cores, UInt())))
  when (reset.toBool) {
    for (i <- 0 until num_contexts) {
      for (j <- 0 until num_cores) {
        enqStates(i)(j) := sWaitAppHdr
      }
    }
  }

  // Select core to receive msg word from
  val net_in_valids = VecInit(io.net_in.map(_.valid))
  val selected_core_reg = RegInit(0.U)
  val selected_core = Wire(UInt())
  when (net_in_valids.asUInt > 0.U) {
    val core = PriorityEncoder(net_in_valids.asUInt)
    selected_core_reg := core
    selected_core := core
  } .otherwise {
    selected_core := selected_core_reg
  }

  // NOTE: register the ID of the last context that enqueued a word. We do this cuz
  //   we want to continuously read the credit and toBtx state.
  val enq_context_reg = Reg(UInt(LNIC_CONTEXT_BITS.W))
  when (io.net_in(selected_core).valid) {
    enq_context_reg := io.net_in(selected_core).bits.src_context
  }

  // defaults
  val enq_context = Wire(UInt(LNIC_CONTEXT_BITS.W))
  enq_context := Mux(io.net_in(selected_core).valid,
                     io.net_in(selected_core).bits.src_context,
                     enq_context_reg)
  init_scheduled_pkts_enq.valid := false.B
  io.schedule.valid := false.B
  // default - do not read from any cores
  io.net_in.foreach { net_in =>
    net_in.ready := false.B
  }

  val tx_app_hdr = Wire(new TxAppHdr)
  tx_app_hdr := (new TxAppHdr).fromBits(io.net_in(selected_core).bits.data)

  // bitmap of valid signals for all size classes
  val free_classes = VecInit(size_class_freelists_io.map(_.deq.valid))
  // bitmap of size_classes that are large enough to store the whole msg
  val candidate_classes = VecInit(size_class_buf_sizes.map(_ >= tx_app_hdr.msg_len))
  // bitmap indicates classes with available buffers that are large enough
  val available_classes = free_classes.asUInt & candidate_classes.asUInt

  // Per-context, per-core enq state
  val context_enq_state = Reg(Vec(num_contexts, Vec(num_cores, new ContextEnqState)))
  when (reset.toBool) {
    for (i <- 0 until num_contexts) {
      for (j <- 0 until num_cores) {
        context_enq_state(i)(j) := (new ContextEnqState).fromBits(0.U)
      }
    }
  }

  // msg buffer write port
  val enq_buf_ptr = Wire(UInt(BUF_PTR_BITS.W))
  val enq_msg_buf_ram_port = msg_buffer_ram(enq_buf_ptr)

  val tx_msg_id = Wire(UInt(MSG_ID_BITS.W))
  val tx_msg_id_reg = Reg(next = tx_msg_id)
  tx_msg_id := tx_msg_id_freelist.io.deq.bits // default

  credit_table.io.portA.addr := tx_msg_id
  toBtx_table.io.portA.addr := tx_msg_id

  switch (enqStates(enq_context)(selected_core)) {
    is (sWaitAppHdr) {
      // assert back pressure to the CPU if there are no available buffers for this msg
      io.net_in(selected_core).ready := (available_classes > 0.U)
      when (io.net_in(selected_core).valid && io.net_in(selected_core).ready) {
        assert (tx_msg_id_freelist.io.deq.valid, "There is an available buffer but not an available tx_msg_id?")
        // read tx_msg_id_freelist
        tx_msg_id_freelist.io.deq.ready := true.B
        // read from target size class freelist
        val target_size_class = PriorityEncoder(available_classes)
        val target_freelist = size_class_freelists_io(target_size_class)
        target_freelist.deq.ready := true.B
        val buf_ptr = target_freelist.deq.bits
        // build msg descriptor
        val msg_desc = Wire(new TxMsgDescriptor)
        msg_desc.tx_msg_id := tx_msg_id
        msg_desc.buf_ptr := buf_ptr
        msg_desc.size_class := target_size_class
        msg_desc.tx_app_hdr := tx_app_hdr
        msg_desc.src_context := enq_context
        // record per-context enqueue state
        val ctx_state = context_enq_state(enq_context)(selected_core)
        ctx_state.msg_desc := msg_desc
        ctx_state.pkt_offset := 0.U
        ctx_state.rem_bytes := tx_app_hdr.msg_len
        ctx_state.pkt_bytes := 0.U
        ctx_state.word_count := 0.U // counts the number of words written by CPU for this msg (not including app hdr)
        val num_pkts = MsgBufHelpers.compute_num_pkts(tx_app_hdr.msg_len)
        // initialize state that is indexed by tx_msg_id (for transport support)
        delivered_table(tx_msg_id) := 0.U 
        credit_table.io.portA.we := true.B
        credit_table.io.portA.din := io.rtt_pkts
        toBtx_table.io.portA.we := true.B
        toBtx_table.io.portA.din := 0.U // no pkts have been written yet
        // NOTE: we could also initialize max_tx_pkt_offset_table here but that would require
        //   an extra port so we will do that initialization in the dequeue state machine.
        // schedule timer
        io.schedule.valid := true.B
        io.schedule.bits.msg_id := tx_msg_id
        io.schedule.bits.delay := io.timeout_cycles
        io.schedule.bits.metadata.rtx_offset := 0.U
        io.schedule.bits.metadata.msg_desc := msg_desc
        // state transition
        enqStates(enq_context)(selected_core) := sWriteMsg
      }
    }
    is (sWriteMsg) {
      val ctx_state = context_enq_state(enq_context)(selected_core)
      tx_msg_id := ctx_state.msg_desc.tx_msg_id
      // wait for tx_msg_id to converge (so that we can read credit and toBtx state)
      io.net_in(selected_core).ready := (tx_msg_id === tx_msg_id_reg)
      // wait for a MsgWord to arrive 
      when (io.net_in(selected_core).valid && io.net_in(selected_core).ready) {
        // compute where to write MsgWord in the buffer
        val word_offset_bits = log2Up(NET_DP_BITS/XLEN)
        val word_offset = ctx_state.word_count(word_offset_bits-1, 0)
        val word_ptr = ctx_state.word_count(15, word_offset_bits)
        enq_buf_ptr := ctx_state.msg_desc.buf_ptr + word_ptr
        // Perform a sub-word write
        enq_msg_buf_ram_port(word_offset) := reverse_bytes(io.net_in(selected_core).bits.data, XBYTES)
        // build tx pkt descriptor just in case we need to schedule a pkt
        val tx_pkt_desc = Wire(new TxPktDescriptor)
        tx_pkt_desc.msg_desc := ctx_state.msg_desc
        tx_pkt_desc.tx_pkts := 1.U << ctx_state.pkt_offset

        val is_last_word = ctx_state.rem_bytes <= XBYTES.U
        val is_full_pkt = ctx_state.pkt_bytes + XBYTES.U === MAX_SEG_LEN_BYTES.U

        // get credit state read result
        val enq_credit = Wire(UInt(CREDIT_BITS.W))
        enq_credit := credit_table.io.portA.dout
        // get toBtx state read result
        val enq_toBtx = Wire(UInt(MAX_SEGS_PER_MSG.W))
        enq_toBtx := toBtx_table.io.portA.dout
        // NOTES:
        //   - We want to read the current value of toBtx here because if a pkt was dropped, we want to
        //     make sure that bit stays set.
        //   - We want to read the current value of credit here because if the credit increases quickly
        //     (i.e. PULL arrives quickly or pkts are written slowly) then more pkts can be sent immediately.

        when (is_last_word || is_full_pkt) {
          // only immediately transmit up to credit pkts, not all pkts
          when (ctx_state.pkt_offset < enq_credit) {
            // schedule pkt for tx
            // TODO(sibanez): this isn't really a bug, this can legit happen, how best to deal with it?
            assert (init_scheduled_pkts_enq.ready, "scheduled_pkts queue is full during enqueue!")
            init_scheduled_pkts_enq.valid := true.B
            init_scheduled_pkts_enq.bits := tx_pkt_desc
          } .otherwise {
            // mark pkt as in need of transmission via credit events
            toBtx_table.io.portA.we := true.B
            toBtx_table.io.portA.din := enq_toBtx | tx_pkt_desc.tx_pkts
          }
        }

        when (is_full_pkt) {
          // reset/increment pkt counter state
          ctx_state.pkt_offset := ctx_state.pkt_offset + 1.U
          ctx_state.pkt_bytes := 0.U         
        } .otherwise {
          ctx_state.pkt_bytes := ctx_state.pkt_bytes + XBYTES.U
        }

        when (is_last_word) {
          // state transition
          enqStates(enq_context)(selected_core) := sWaitAppHdr
        }

        // update context state variables
        ctx_state.rem_bytes := ctx_state.rem_bytes - XBYTES.U
        ctx_state.word_count := ctx_state.word_count + 1.U
      }
    }
  }

  /**
   * Pkt Dequeue State Machine:
   * Tasks:
   *   - Wait for a pkt to be scheduled
   *   - Record the tx pkt descriptor
   *   - Transmit all pkts indicated by the descriptor
   *   - Repeat
   *
   * Description of states:
   * sWaitTxPkts:
   *   - Wait for a TxPktDescriptor to be scheduled
   *   - Start reading the first word of the pkt from the msg buffer
   *   - Record an updated descriptor indicating the next pkt to transmit after the first one
   *   - Record the number of bytes remaining for the current pkt
   *   - Transition to the sSendTxPkts state
   *
   * sSendTxPkts:
   *   - Transmit all bytes of the current pkt
   *   - Update descriptor when done sending a pkt
   *   - Transition back to sWaitTxPkts when done sending all pkts
   */
  val sWaitTxPkts :: sSendTxPkts :: Nil = Enum(2)
  val deqState = RegInit(sWaitTxPkts)

  // msg buffer read port
  val deq_buf_ptr = Wire(UInt(BUF_PTR_BITS.W))
  val deq_msg_buf_ram_port = msg_buffer_ram(deq_buf_ptr)
  val deq_buf_ptr_reg = RegInit(0.U(BUF_PTR_BITS.W))

  // register to store descriptor of pkt(s) currently being transmitted
  val active_tx_desc_reg = Reg(new TxPktDescriptor)
  // register to track the number of bytes remaining to send for the current pkt
  val deq_pkt_rem_bytes_reg = RegInit(0.U(16.W))
  // register to track current pkt offset
  val deq_pkt_offset_reg = RegInit(0.U(PKT_OFFSET_BITS.W))
  // register to track first word of each pkt (used to drive meta_out.valid)
  val is_first_word_reg = RegInit(false.B)
  // register to track if the descriptor came from a CPU write and
  // hence if the max_tx_pkt_offset state should be initialized
  val init_max_pkt_offset_reg = RegInit(false.B)

  val deq_tx_msg_id = Wire(UInt(MSG_ID_BITS.W))
  deq_tx_msg_id := active_tx_desc_reg.msg_desc.tx_msg_id

  val update_max_tx_pkt_offset_port = max_tx_pkt_offset_table(deq_tx_msg_id)

  // defaults
  io.net_out.valid     := false.B
  io.net_out.bits.data := deq_msg_buf_ram_port.asUInt
  io.net_out.bits.keep := NET_DP_FULL_KEEP
  io.net_out.bits.last := false.B

  io.meta_out.valid               := is_first_word_reg
  io.meta_out.bits.dst_ip         := active_tx_desc_reg.msg_desc.tx_app_hdr.dst_ip
  io.meta_out.bits.dst_context    := active_tx_desc_reg.msg_desc.tx_app_hdr.dst_context
  io.meta_out.bits.msg_len        := active_tx_desc_reg.msg_desc.tx_app_hdr.msg_len
  io.meta_out.bits.pkt_offset     := deq_pkt_offset_reg
  io.meta_out.bits.src_context    := active_tx_desc_reg.msg_desc.src_context
  io.meta_out.bits.tx_msg_id      := active_tx_desc_reg.msg_desc.tx_msg_id
  io.meta_out.bits.buf_ptr        := active_tx_desc_reg.msg_desc.buf_ptr
  io.meta_out.bits.buf_size_class := active_tx_desc_reg.msg_desc.size_class
  io.meta_out.bits.pull_offset    := 0.U
  io.meta_out.bits.genACK         := false.B
  io.meta_out.bits.genNACK        := false.B
  io.meta_out.bits.genPULL        := false.B

  switch (deqState) {
    is (sWaitTxPkts) {
      // wait for a TxPktDescriptor to be scheduled
      // schedule between the various scheduled_pkts queues
      // NOTE: first RTT pkts of new msgs are strictly prioritized over pkts made
      //   available from credit update events which are strictly prioritized over
      //   pkts scheduled from timeout events.
      when (init_scheduled_pkts_deq.valid) {
        // read descriptor
        init_scheduled_pkts_deq.ready := true.B
        tx_next_pkt(init_scheduled_pkts_deq.bits)
        // state transition
        deqState := sSendTxPkts
        // This is a descriptor for one of the first pkts of the msg -- initialize the max_tx_pkt_offset state
        init_max_pkt_offset_reg := true.B
      } .elsewhen (credit_scheduled_pkts_deq.valid) {
        // read descriptor
        credit_scheduled_pkts_deq.ready := true.B
        tx_next_pkt(credit_scheduled_pkts_deq.bits)
        // state transition
        deqState := sSendTxPkts
        init_max_pkt_offset_reg := false.B
      } .elsewhen (timeout_scheduled_pkts_deq.valid) {
        timeout_scheduled_pkts_deq.ready := true.B
        tx_next_pkt(timeout_scheduled_pkts_deq.bits)
        // state transition
        deqState := sSendTxPkts
        init_max_pkt_offset_reg := false.B
      }
    }
    is (sSendTxPkts) {
      io.net_out.valid := !(reset.toBool) // do not assert valid on reset
      val is_last_word = deq_pkt_rem_bytes_reg <= NET_DP_BYTES.U
      io.net_out.bits.keep := Mux(is_last_word,
                                  (1.U << deq_pkt_rem_bytes_reg) - 1.U,
                                  NET_DP_FULL_KEEP)
      io.net_out.bits.last := is_last_word

      // default - keep reading the same word
      deq_buf_ptr := deq_buf_ptr_reg
  
      // wait for no backpressure
      when (io.net_out.ready) {
        when (is_last_word) {
          when (active_tx_desc_reg.tx_pkts === 0.U) {
            // no more pkts to transmit
            deqState := sWaitTxPkts
          } .otherwise {
            // there are more pkts to transmit
            tx_next_pkt(active_tx_desc_reg)
          }
        } .otherwise {
          // start reading the next word
          deq_buf_ptr := deq_buf_ptr_reg + 1.U
          deq_buf_ptr_reg := deq_buf_ptr
          // update deq_pkt_rem_bytes_reg
          deq_pkt_rem_bytes_reg := deq_pkt_rem_bytes_reg - NET_DP_BYTES.U
          // no longer the first word
          is_first_word_reg := false.B
        }
      }
  
      // update max_tx_pkt_offset state
      val cur_max_pkt_offset = Wire(UInt(PKT_OFFSET_BITS.W))
      cur_max_pkt_offset := update_max_tx_pkt_offset_port
      when (init_max_pkt_offset_reg || deq_pkt_offset_reg > cur_max_pkt_offset) {
        update_max_tx_pkt_offset_port := deq_pkt_offset_reg
      }

    }
  }

  def tx_next_pkt(descriptor: TxPktDescriptor) = {
    // find the next pkt to transmit
    val pkt_offset = PriorityEncoder(descriptor.tx_pkts)
    deq_pkt_offset_reg := pkt_offset
    // find the word offset from the buf_ptr: pkt_offset*words_per_mtu
    require(isPow2(MAX_SEG_LEN_BYTES), "MAX_SEG_LEN_BYTES must be a power of 2!")
    require(MAX_SEG_LEN_BYTES >= 64, "MAX_SEG_LEN_BYTES must be at least 64!")
    val word_offset = pkt_offset << (log2Up(MAX_SEG_LEN_BYTES/NET_DP_BYTES)).U
    // start reading the first word of the pkt
    deq_buf_ptr := descriptor.msg_desc.buf_ptr + word_offset
    deq_buf_ptr_reg := deq_buf_ptr
    // start reading max_tx_pkt_offset state
    deq_tx_msg_id := descriptor.msg_desc.tx_msg_id
    // register to track first word of each pkt
    is_first_word_reg := true.B
    // record the updated descriptor
    val new_descriptor = Wire(new TxPktDescriptor)
    new_descriptor := descriptor
    new_descriptor.tx_pkts := descriptor.tx_pkts ^ (1.U << pkt_offset)
    active_tx_desc_reg := new_descriptor
    // record bytes remaining for the current pkt
    val msg_len = descriptor.msg_desc.tx_app_hdr.msg_len
    val num_pkts = MsgBufHelpers.compute_num_pkts(msg_len)
    when (pkt_offset === num_pkts - 1.U) {
        // this is the last pkt of the msg
        // compute the number of bytes in the last pkt of the msg
        val msg_len_mod_mtu = msg_len(log2Up(MAX_SEG_LEN_BYTES)-1, 0) 
        val final_pkt_bytes = Mux(msg_len_mod_mtu === 0.U,
                                  MAX_SEG_LEN_BYTES.U,
                                  msg_len_mod_mtu)
        deq_pkt_rem_bytes_reg := final_pkt_bytes
    } .otherwise {
        // this is not the last pkt of the msg
        deq_pkt_rem_bytes_reg := MAX_SEG_LEN_BYTES.U
    }
  }

  /* Delivered State Machine:
   * Tasks:
   *   - Process delivered events from the ingress pipeline
   *   - Mark pkt of a msg as delivered (i.e. ACKed)
   *   - Once all pkts have been delivered, free the tx_msg_id and msg buffer
   */
  val sReadDelivered :: sWriteDelivered :: Nil = Enum(2)
  val deliveredState = RegInit(sReadDelivered)

  // pipeline regs to store delivered event
  val delivered_reg_0 = RegNext(io.delivered)
  val delivered_reg_1 = RegNext(delivered_reg_0)
  // read/write port to update delivered state
  val delivered_ptr = Wire(UInt(MSG_ID_BITS.W))
  delivered_ptr := delivered_reg_0.bits.tx_msg_id
  val update_delivered_table_port = delivered_table(delivered_ptr)

  // TODO(sibanez): this state machine assumes delivered events will not fire on back-to-back
  //   cycles. Need 2 cycles to perform RMW of delivered state.
  assert(!(delivered_reg_0.valid && delivered_reg_1.valid), "Delivered events fired on back-to-back cycles! This is currently unsupported!")

  // defaults
  io.cancel.valid := false.B

  switch (deliveredState) {
    is (sReadDelivered) {
      // start reading delivered state
      // state transition
      when (delivered_reg_0.valid) {
        deliveredState := sWriteDelivered
      }
    }
    is (sWriteDelivered) {
      // get read result
      val delivered_bitmap = Wire(UInt(MAX_SEGS_PER_MSG.W))
      delivered_bitmap := update_delivered_table_port

      // compute updated bitmap
      val new_delivered_bitmap = Wire(UInt(MAX_SEGS_PER_MSG.W))
      new_delivered_bitmap := delivered_bitmap | (1.U << delivered_reg_1.bits.pkt_offset)
      delivered_ptr := delivered_reg_1.bits.tx_msg_id
      update_delivered_table_port := new_delivered_bitmap

      // check if all pkts have been delivered
      val num_pkts = MsgBufHelpers.compute_num_pkts(delivered_reg_1.bits.msg_len)
      val all_pkts = Wire(UInt(MAX_SEGS_PER_MSG.W))
      all_pkts := (1.U << num_pkts) - 1.U
      when (new_delivered_bitmap === all_pkts) {
        // free tx_msg_id
        assert(tx_msg_id_freelist.io.enq.ready, "tx_msg_id_freelist is full when trying to free a tx_msg_id!")
        tx_msg_id_freelist.io.enq.valid := true.B
        tx_msg_id_freelist.io.enq.bits := delivered_reg_1.bits.tx_msg_id
        // free msg buffer
        val buffer_freelist = size_class_freelists_io(delivered_reg_1.bits.buf_size_class)
        assert(buffer_freelist.enq.ready, "buffer freelist is full when trying to free a msg buffer!")
        buffer_freelist.enq.valid := true.B
        buffer_freelist.enq.bits := delivered_reg_1.bits.buf_ptr
        // fire cancel timer event
        io.cancel.valid := true.B
        io.cancel.bits.msg_id := delivered_reg_1.bits.tx_msg_id
      }

      // state transition
      deliveredState := sReadDelivered
    }
  }

  /* creditToBtx State Machine:
   * Tasks:
   *   - Process creditToBtx events from the ingress pipeline
   */
  val sReadState :: sWriteState :: Nil = Enum(2)
  val creditToBtxState = RegInit(sReadState)

  // pipeline regs to store creditToBtx event
  val creditToBtx_reg_0 = RegNext(io.creditToBtx)
  val creditToBtx_reg_1 = RegNext(creditToBtx_reg_0)
  // read/write port to update creditToBtx state
  val credit_ptr = Wire(UInt(CREDIT_BITS.W))
  val toBtx_ptr = Wire(UInt(MAX_SEGS_PER_MSG.W))
  credit_ptr := creditToBtx_reg_0.bits.tx_msg_id
  toBtx_ptr := creditToBtx_reg_0.bits.tx_msg_id
  credit_table.io.portB.addr := credit_ptr
  toBtx_table.io.portB.addr := toBtx_ptr

  // TODO(sibanez): this state machine assumes events will not fire on back-to-back
  //   cycles. Need 2 cycles to perform RMW of state variables.
  assert(!(creditToBtx_reg_0.valid && creditToBtx_reg_1.valid), "Delivered events fired on back-to-back cycles! This is currently unsupported!")

  switch (creditToBtxState) {
    is (sReadState) {
      // start reading credit and toBtx tables
      // state transition
      when (creditToBtx_reg_0.valid) {
        creditToBtxState := sWriteState
      }
    }
    is (sWriteState) {
      // get read results
      val credit = Wire(UInt(CREDIT_BITS.W))
      val toBtx = Wire(UInt(MAX_SEGS_PER_MSG.W))
      credit := credit_table.io.portB.dout
      toBtx := toBtx_table.io.portB.dout

      // update toBtx with rtx pkt
      val rtx_toBtx = Wire(UInt(MAX_SEGS_PER_MSG.W))
      rtx_toBtx := Mux(creditToBtx_reg_1.bits.rtx,
                       toBtx | (1.U << creditToBtx_reg_1.bits.rtx_pkt_offset),
                       toBtx)

      // compute updated credit and toBtx state
      val new_credit = Wire(UInt(CREDIT_BITS.W))
      val new_toBtx = Wire(UInt(MAX_SEGS_PER_MSG.W))
      when (creditToBtx_reg_1.bits.update_credit) {
        new_credit := creditToBtx_reg_1.bits.new_credit
        // compute pkts to transmit
        val credit_tx_pkts = Wire(UInt(MAX_SEGS_PER_MSG.W))
        credit_tx_pkts := rtx_toBtx & ((1.U << new_credit) - 1.U)
        // clear bits of pkts to be transmitted
        new_toBtx := rtx_toBtx & ~credit_tx_pkts
        when (credit_tx_pkts =/= 0.U) {
          // there are pkts to transmit
          val tx_pkt_desc = Wire(new TxPktDescriptor)
          tx_pkt_desc.msg_desc.tx_msg_id              := creditToBtx_reg_1.bits.tx_msg_id
          tx_pkt_desc.msg_desc.buf_ptr                := creditToBtx_reg_1.bits.buf_ptr
          tx_pkt_desc.msg_desc.size_class             := creditToBtx_reg_1.bits.buf_size_class
          tx_pkt_desc.msg_desc.tx_app_hdr.dst_ip      := creditToBtx_reg_1.bits.dst_ip
          tx_pkt_desc.msg_desc.tx_app_hdr.dst_context := creditToBtx_reg_1.bits.dst_context
          tx_pkt_desc.msg_desc.tx_app_hdr.msg_len     := creditToBtx_reg_1.bits.msg_len
          tx_pkt_desc.msg_desc.src_context            := creditToBtx_reg_1.bits.src_context
          tx_pkt_desc.tx_pkts                := credit_tx_pkts
          // TODO(sibanez): this is not really a bug, it can legit happen, how to handle?
          assert(credit_scheduled_pkts_enq.ready, "scheduled_pkts queue is full while scheduling a packet!")
          credit_scheduled_pkts_enq.valid := true.B
          credit_scheduled_pkts_enq.bits := tx_pkt_desc
        }
      } .otherwise {
        new_credit := credit
        new_toBtx := rtx_toBtx
      }

      // update state
      credit_ptr := creditToBtx_reg_1.bits.tx_msg_id
      toBtx_ptr  := creditToBtx_reg_1.bits.tx_msg_id
      credit_table.io.portB.we := true.B
      credit_table.io.portB.din := new_credit
      toBtx_table.io.portB.we := true.B
      toBtx_table.io.portB.din := new_toBtx

      // state transition
      creditToBtxState := sReadState
    }
  }

  /* Timeout event processing state machine
   * Tasks:
   *   - Read delivered bitmap to figure out which pkts to retransmit
   *   - Schedule those pkts for retransmission (if any)
   *   - Fire a reschedule event using the max tx pkt offset
   */
  val sReadTimeoutState :: sSchedRtx :: Nil = Enum(2)
  val timeoutState = RegInit(sReadTimeoutState)

  // pipeline regs
  val timeout_reg_0 = RegNext(io.timeout)
  val timeout_reg_1 = RegNext(timeout_reg_0)

  assert(!(timeout_reg_0.valid && timeout_reg_1.valid), "Back-to-back timeout events are unsupported!")

  // TODO(sibanez): we are just adding another port here for now, but this would be the 3rd port
  //   for this table. This should really access a separate table that is sync'd with the delivered_table
  //   in the background.
  // delivered_table read port
  val timeout_msg_id = Wire(UInt(MSG_ID_BITS.W))
  timeout_msg_id := timeout_reg_0.bits.msg_id
  val timeout_delivered_table_port = delivered_table(timeout_msg_id) 

  // max_tx_pkt_offset read port
  val max_tx_pkt_offset_port = max_tx_pkt_offset_table(timeout_msg_id)

  // defaults
  io.reschedule.valid := false.B
  timeout_scheduled_pkts_enq.valid := false.B 

  switch (timeoutState) {
    is (sReadTimeoutState) {
      // wait for a timeout event
      when (timeout_reg_0.valid) {
        // start reading delivered_table and max_tx_pkt_offset_table
        // state transition
        timeoutState := sSchedRtx
      }
    }
    is (sSchedRtx) {
      // get read results
      val delivered_bitmap = Wire(UInt(MAX_SEGS_PER_MSG.W))
      delivered_bitmap := timeout_delivered_table_port
      val max_tx_pkt_offset = Wire(UInt(PKT_OFFSET_BITS.W))
      max_tx_pkt_offset := max_tx_pkt_offset_port

      // find any pkts to retransmit
      val rtx_pkts_mask = Wire(UInt(MAX_SEGS_PER_MSG.W))
      rtx_pkts_mask := (1.U << (timeout_reg_1.bits.metadata.rtx_offset + 1.U)) - 1.U
      val rtx_pkts = ~delivered_bitmap & rtx_pkts_mask
      when (rtx_pkts > 0.U && !reset.toBool) {
        // there are pkts to retransmit
        val tx_pkt_desc = Wire(new TxPktDescriptor)
        tx_pkt_desc.msg_desc := timeout_reg_1.bits.metadata.msg_desc
        tx_pkt_desc.tx_pkts := rtx_pkts
        assert(timeout_scheduled_pkts_enq.ready, "schedule_pkts queue is full when processing timeout event!")
        timeout_scheduled_pkts_enq.valid := true.B
        timeout_scheduled_pkts_enq.bits := tx_pkt_desc
      }

      // fire reschedule event
      io.reschedule.valid := !reset.toBool
      io.reschedule.bits.msg_id := timeout_reg_1.bits.msg_id
      io.reschedule.bits.delay := io.timeout_cycles
      io.reschedule.bits.metadata.rtx_offset := max_tx_pkt_offset
      io.reschedule.bits.metadata.msg_desc := timeout_reg_1.bits.metadata.msg_desc

      // state transition
      timeoutState := sReadTimeoutState
    }
  }

}

