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
 * All IO for the Homa Ingress module.
 */
class HomaIngressIO extends Bundle {
  val net_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val meta_out = Valid(new IngressMetaOut)
  val get_rx_msg_info = new GetRxMsgInfoIO
  val delivered = Valid(new DeliveredEvent)
  val creditToBtx = Valid(new CreditToBtxEvent)
  val ackPkt = Valid(new EgressMetaIn)
  val grantPkt = Valid(new EgressMetaIn)
  val pendingMsgReg = new PendingMsgRegIO
  val grantScheduler = new GrantSchedulerIO
  val txMsgPrioReg_req = Valid(new TxMsgPrioIngressReq)
  val rtt_pkts = Input(UInt(CREDIT_BITS.W))

  override def cloneType = new HomaIngressIO().asInstanceOf[this.type]
}

class HomaHeaders extends Bundle {
  // Ethernet Header
  val eth_dst = UInt(48.W)
  val eth_src = UInt(48.W)
  val eth_type = UInt(16.W)
  // IP Header
  val ip_version = UInt(4.W)
  val ip_ihl = UInt(4.W)
  val ip_tos = UInt(8.W)
  val ip_len = UInt(16.W)
  val ip_id = UInt(16.W)
  val ip_flags = UInt(3.W)
  val ip_offset = UInt(13.W)
  val ip_ttl = UInt(8.W)
  val ip_proto = UInt(8.W)
  val ip_chksum = UInt(16.W)
  val ip_src = UInt(32.W)
  val ip_dst = UInt(32.W)
  // LNIC Homa Header
  val homa_flags = UInt(8.W)
  val homa_src = UInt(LNIC_CONTEXT_BITS.W)
  val homa_dst = UInt(LNIC_CONTEXT_BITS.W)
  val homa_msg_len = UInt(MSG_LEN_BITS.W)
  val homa_pkt_offset = UInt(PKT_OFFSET_BITS.W)
  val homa_grant_offset = UInt(CREDIT_BITS.W)
  val homa_grant_prio = UInt(HOMA_PRIO_BITS.W)
  val homa_tx_msg_id = UInt(MSG_ID_BITS.W)
  val homa_buf_ptr = UInt(BUF_PTR_BITS.W)
  val homa_buf_size_class = UInt(SIZE_CLASS_BITS.W)
  val homa_padding = UInt(112.W) // padding to make header len = 64B for easy parsing / deparsing
}

// Metadata that is only used within the M/A pipeline
class HomaPipeMeta extends Bundle {
  val drop = Bool()
  val is_data = Bool()
  val buf_ptr = UInt(BUF_PTR_BITS.W)
  val buf_size_class = UInt(SIZE_CLASS_BITS.W)
  val grant_offset = UInt(CREDIT_BITS.W)
  val grant_prio = UInt(HOMA_PRIO_BITS.W)
  val genGRANT = Bool()
  val cur_msg_expect_resp = Bool()
  val grant_sched_expect_resp = Bool()
  val msg_len_pkts = UInt(log2Up(MAX_SEGS_PER_MSG).W)
}

class HomaPipelineRegs extends Bundle {
  val ingress_meta = new IngressMetaOut
  val pipe_meta = new HomaPipeMeta
}


@chiselName
class HomaIngress(implicit p: Parameters) extends Module {
  val io = IO(new HomaIngressIO)

  val parserPktQueue_out = Wire(Flipped(Decoupled(new StreamChannel(NET_DP_BITS))))
  val headers = Wire(new HomaHeaders)
  val headers_reg = Reg(Valid(new HomaHeaders))
  headers_reg.valid := false.B // default

  val maPktQueue_in = Wire(Decoupled(new StreamChannel(NET_DP_BITS)))
  val maPktQueue_out = Wire(Flipped(Decoupled(new StreamChannel(NET_DP_BITS))))
  maPktQueue_in.valid := false.B // default
  maPktQueue_out.ready := false.B // default

  // need metadata queue to synchronize metadata with pkt payload
  val maMetaQueue_in = Wire(Decoupled(new HomaPipelineRegs))
  val maMetaQueue_out = Wire(Flipped(Decoupled(new HomaPipelineRegs)))
  maMetaQueue_in.valid := false.B // default
  maMetaQueue_out.ready := false.B // default

  // default IO
  io.net_out.valid := false.B
  io.meta_out.valid := false.B
  io.get_rx_msg_info.req.valid := false.B
  io.delivered.valid := false.B
  io.creditToBtx.valid := false.B
  io.ackPkt.valid := false.B
  io.grantPkt.valid := false.B

  io.pendingMsgReg.cur_msg_req.valid := false.B
  io.pendingMsgReg.grant_msg_req.valid := false.B
  io.grantScheduler.req.valid := false.B
  io.txMsgPrioReg_req.valid := false.B

  /**********/
  /* Parser */
  /**********/

  // queue to store all arriving pkts
  parserPktQueue_out <> Queue(io.net_in, PARSER_PKT_QUEUE_FLITS)
  parserPktQueue_out.ready := true.B // no backpressure on this queue

  // Parse headers from each pkt. Pass headers and pkt payload to match-action processing.
  // NOTE: assumes min pkt size = 65B (more than one word so that every pkt has headers and payload).
  //   This is just a simplying assumption to make writing this logic easier - not needed when using SDNet.
  val sWordOne :: sFinishPkt :: sDropPkt :: Nil = Enum(3)
  val parseState = RegInit(sWordOne)

  switch(parseState) {
    is (sWordOne) {
      when (parserPktQueue_out.valid && !reset.toBool) {
        headers := (new HomaHeaders).fromBits(reverse_bytes(parserPktQueue_out.bits.data, NET_DP_BYTES))
        when (headers.eth_type === IPV4_TYPE && headers.ip_proto === HOMA_PROTO && !parserPktQueue_out.bits.last) {
          // this is a Homa pkt
          // start M/A processing
          headers_reg.valid := true.B
          headers_reg.bits := headers
          // forward pkt payload along
          parseState := sFinishPkt
        } .elsewhen (!parserPktQueue_out.bits.last) {
          // this is not a Homa pkt (and it has additional data)
          parseState := sDropPkt
        }
        // NOTE: implicitly drop any pkts with no payload
      }
    }
    is (sFinishPkt) {
      // write pkt payload into maPktQueue
      maPktQueue_in <> parserPktQueue_out
      assert(maPktQueue_in.ready, "M/A Pkt Queue cannot store pkt payload, it must be too small!")
      when (parserPktQueue_out.valid && parserPktQueue_out.bits.last) {
        parseState := sWordOne
      }
    }
    is (sDropPkt) {
      when (parserPktQueue_out.valid && parserPktQueue_out.bits.last) {
        parseState := sWordOne
      }
    }
  }

  /***************************/
  /* M/A Processing Pipeline */
  /***************************/

  // Pipeline stages to invoke get_rx_msg_info extern
  val rx_info_stage1 = Reg(Valid(new HomaPipelineRegs))
  val rx_info_stage2 = Reg(Valid(new HomaPipelineRegs))

  // Pipeline stages to access state for current message
  val cur_msg_stage1 = Reg(Valid(new HomaPipelineRegs))
  val cur_msg_stage2 = Reg(Valid(new HomaPipelineRegs))

  // Pipeline stages to invoke scheduling extern to decide which msg to grant (if any)
  val grant_sched_stage1 = Reg(Valid(new HomaPipelineRegs))
  val grant_sched_stage2 = Reg(Valid(new HomaPipelineRegs))

  // Pipeline stages to access / update state for msg being granted (if any)
  val grant_msg_stage1 = Reg(Valid(new HomaPipelineRegs))
  val grant_msg_stage2 = Reg(Valid(new HomaPipelineRegs))

  maPktQueue_out <> Queue(maPktQueue_in, MA_PKT_QUEUE_FLITS)
  maMetaQueue_out <> Queue(maMetaQueue_in, MA_META_QUEUE_FLITS)

  rx_info_stage1.valid := headers_reg.valid

  // wait for Homa pkt headers to be parsed
  when (headers_reg.valid && !reset.toBool) {
    // fill out as much metadata as we can right now
    rx_info_stage1.bits.ingress_meta.src_ip      := headers_reg.bits.ip_src
    rx_info_stage1.bits.ingress_meta.src_context := headers_reg.bits.homa_src
    rx_info_stage1.bits.ingress_meta.msg_len     := headers_reg.bits.homa_msg_len
    rx_info_stage1.bits.ingress_meta.pkt_offset  := headers_reg.bits.homa_pkt_offset
    rx_info_stage1.bits.ingress_meta.dst_context := headers_reg.bits.homa_dst
    rx_info_stage1.bits.ingress_meta.rx_msg_id   := 0.U // default
    rx_info_stage1.bits.ingress_meta.tx_msg_id   := headers_reg.bits.homa_tx_msg_id
    rx_info_stage1.bits.ingress_meta.is_last_pkt := false.B // default
    rx_info_stage1.bits.pipe_meta.drop           := false.B // default
    rx_info_stage1.bits.pipe_meta.is_data        := false.B // default
    rx_info_stage1.bits.pipe_meta.buf_ptr        := headers_reg.bits.homa_buf_ptr
    rx_info_stage1.bits.pipe_meta.buf_size_class := headers_reg.bits.homa_buf_size_class
    rx_info_stage1.bits.pipe_meta.grant_offset   := 0.U // default
    rx_info_stage1.bits.pipe_meta.grant_prio     := 0.U // default
    rx_info_stage1.bits.pipe_meta.genGRANT       := false.B // default
    rx_info_stage1.bits.pipe_meta.cur_msg_expect_resp     := false.B // default
    rx_info_stage1.bits.pipe_meta.grant_sched_expect_resp := false.B // default
    rx_info_stage1.bits.pipe_meta.msg_len_pkts   := MsgBufHelpers.compute_num_pkts(headers_reg.bits.homa_msg_len)
    when ((headers_reg.bits.homa_flags & DATA_MASK) > 0.U) {
      // this is a DATA pkt or a CHOP'ed DATA pkt
      val is_chopped = Wire(Bool())
      is_chopped := (headers_reg.bits.homa_flags & CHOP_MASK) > 0.U

      // do not pass chopped pkts to the assembly module
      rx_info_stage1.bits.pipe_meta.drop := is_chopped

      rx_info_stage1.bits.pipe_meta.is_data := !is_chopped
      // invoke get_rx_msg_info extern
      // NOTE: CHOP'ed DATA pkts do not need to invoke this.
      io.get_rx_msg_info.req.valid := !is_chopped
      io.get_rx_msg_info.req.bits.mark_received  := !is_chopped
      io.get_rx_msg_info.req.bits.src_ip         := headers_reg.bits.ip_src
      io.get_rx_msg_info.req.bits.src_context    := headers_reg.bits.homa_src
      io.get_rx_msg_info.req.bits.tx_msg_id      := headers_reg.bits.homa_tx_msg_id
      io.get_rx_msg_info.req.bits.msg_len        := headers_reg.bits.homa_msg_len
      io.get_rx_msg_info.req.bits.pkt_offset     := headers_reg.bits.homa_pkt_offset

      // generate either an ACK or NACK
      io.ackPkt.valid := true.B
      io.ackPkt.bits.dst_ip         := headers_reg.bits.ip_src
      io.ackPkt.bits.dst_context    := headers_reg.bits.homa_src
      io.ackPkt.bits.msg_len        := headers_reg.bits.homa_msg_len
      io.ackPkt.bits.pkt_offset     := headers_reg.bits.homa_pkt_offset
      io.ackPkt.bits.src_context    := headers_reg.bits.homa_dst
      io.ackPkt.bits.tx_msg_id      := headers_reg.bits.homa_tx_msg_id
      io.ackPkt.bits.buf_ptr        := headers_reg.bits.homa_buf_ptr
      io.ackPkt.bits.buf_size_class := headers_reg.bits.homa_buf_size_class
      io.ackPkt.bits.grant_offset   := 0.U // unused for ACKs and NACKs
      io.ackPkt.bits.grant_prio     := 0.U // unused for ACKs and NACKs
      io.ackPkt.bits.flags          := Mux(is_chopped, NACK_MASK, ACK_MASK)
      io.ackPkt.bits.is_new_msg     := false.B
      io.ackPkt.bits.is_rtx         := false.B

    } .otherwise {
      // this is an ACK, NACK, or GRANT pkt
      rx_info_stage1.bits.pipe_meta.is_data := false.B
      // do not pass control pkts to assembly module
      rx_info_stage1.bits.pipe_meta.drop := true.B

      val is_ack = Wire(Bool())
      is_ack := (headers_reg.bits.homa_flags & ACK_MASK) > 0.U
      val is_nack = Wire(Bool())
      is_nack := (headers_reg.bits.homa_flags & NACK_MASK) > 0.U
      val is_grant = Wire(Bool())
      is_grant := (headers_reg.bits.homa_flags & GRANT_MASK) > 0.U

      when (is_grant) {
        // Update tx_msg_id's priority.
        // NOTE: this is state that is shared with the egress pipeline.
        io.txMsgPrioReg_req.valid := true.B
        io.txMsgPrioReg_req.bits.index := headers_reg.bits.homa_tx_msg_id
        io.txMsgPrioReg_req.bits.prio  := headers_reg.bits.homa_grant_prio
      }

      when (is_ack) {
        // fire delivered event
        io.delivered.valid := true.B
        io.delivered.bits.tx_msg_id      := headers_reg.bits.homa_tx_msg_id
        io.delivered.bits.pkt_offset     := headers_reg.bits.homa_pkt_offset
        io.delivered.bits.msg_len        := headers_reg.bits.homa_msg_len
        io.delivered.bits.buf_ptr        := headers_reg.bits.homa_buf_ptr
        io.delivered.bits.buf_size_class := headers_reg.bits.homa_buf_size_class
      } .elsewhen (is_nack || is_grant) {
        // fire creditToBtx event to either increase msg credit or invoke retransmission
        io.creditToBtx.valid := true.B
        io.creditToBtx.bits.tx_msg_id      := headers_reg.bits.homa_tx_msg_id
        io.creditToBtx.bits.rtx            := is_nack
        io.creditToBtx.bits.rtx_pkt_offset := headers_reg.bits.homa_pkt_offset
        io.creditToBtx.bits.update_credit  := is_grant
        io.creditToBtx.bits.new_credit     := headers_reg.bits.homa_grant_offset
        io.creditToBtx.bits.buf_ptr        := headers_reg.bits.homa_buf_ptr
        io.creditToBtx.bits.buf_size_class := headers_reg.bits.homa_buf_size_class
        io.creditToBtx.bits.dst_ip         := headers_reg.bits.ip_src
        io.creditToBtx.bits.dst_context    := headers_reg.bits.homa_src
        io.creditToBtx.bits.msg_len        := headers_reg.bits.homa_msg_len
        io.creditToBtx.bits.src_context    := headers_reg.bits.homa_dst
      }
    }
  }

  rx_info_stage2 := rx_info_stage1

  cur_msg_stage1 := rx_info_stage2 // default

  // rx_info_stage2 (this is when get_rx_msg_info extern call should return)
  when (rx_info_stage2.valid && !reset.toBool) {
    when (rx_info_stage2.bits.pipe_meta.is_data) {
      assert(io.get_rx_msg_info.resp.valid, "get_rx_msg_info extern call failed to return result after 2 cycles!")

      // drop any packets for which we've failed to allocate resources or if its a duplicate packet
      cur_msg_stage1.bits.pipe_meta.drop := (io.get_rx_msg_info.resp.bits.fail || !io.get_rx_msg_info.resp.bits.is_new_pkt)

      val rx_msg_id = Wire(UInt())
      rx_msg_id := io.get_rx_msg_info.resp.bits.rx_msg_id
      cur_msg_stage1.bits.ingress_meta.rx_msg_id := rx_msg_id

      // get_rx_msg_info extern indicates if this is the last pkt of the msg.
      // Need to pass this flag to the Assembly module so it can schedule the msg for delivery to the CPU.
      cur_msg_stage1.bits.ingress_meta.is_last_pkt := io.get_rx_msg_info.resp.bits.is_last_pkt

      when (!io.get_rx_msg_info.resp.bits.fail) {
        // access state for current msg in pendingMsgRed 
        cur_msg_stage1.bits.pipe_meta.cur_msg_expect_resp := true.B
        io.pendingMsgReg.cur_msg_req.valid := true.B
        io.pendingMsgReg.cur_msg_req.bits.index          := rx_msg_id
        io.pendingMsgReg.cur_msg_req.bits.msg_info.src_ip         := rx_info_stage2.bits.ingress_meta.src_ip
        io.pendingMsgReg.cur_msg_req.bits.msg_info.src_context    := rx_info_stage2.bits.ingress_meta.src_context
        io.pendingMsgReg.cur_msg_req.bits.msg_info.dst_context    := rx_info_stage2.bits.ingress_meta.dst_context
        io.pendingMsgReg.cur_msg_req.bits.msg_info.tx_msg_id      := rx_info_stage2.bits.ingress_meta.tx_msg_id
        io.pendingMsgReg.cur_msg_req.bits.msg_info.msg_len        := rx_info_stage2.bits.ingress_meta.msg_len
        io.pendingMsgReg.cur_msg_req.bits.msg_info.buf_ptr        := rx_info_stage2.bits.pipe_meta.buf_ptr
        io.pendingMsgReg.cur_msg_req.bits.msg_info.buf_size_class := rx_info_stage2.bits.pipe_meta.buf_size_class
        io.pendingMsgReg.cur_msg_req.bits.msg_info.ackNo          := io.get_rx_msg_info.resp.bits.ackNo
        io.pendingMsgReg.cur_msg_req.bits.grant_info.grantedIdx     := io.rtt_pkts
        io.pendingMsgReg.cur_msg_req.bits.grant_info.grantableIdx   := io.rtt_pkts + 1.U
        io.pendingMsgReg.cur_msg_req.bits.grant_info.remaining_size := rx_info_stage2.bits.pipe_meta.msg_len_pkts - 1.U
        // Initialize msg state if this is the 1st pkt of a new msg.
        // Otherwise, update the msg state:
        //   - Increment grantableIdx, decrement remainingSize, update ackNo
        io.pendingMsgReg.cur_msg_req.bits.is_new_msg   := io.get_rx_msg_info.resp.bits.is_new_msg
      }

    }
  }

  cur_msg_stage2 := cur_msg_stage1

  grant_sched_stage1 := cur_msg_stage2 // default

  // The current msg state is now available
  when (cur_msg_stage2.valid && !reset.toBool) {
    when (cur_msg_stage2.bits.pipe_meta.cur_msg_expect_resp) {
      assert(io.pendingMsgReg.cur_msg_resp.valid, "pendingMsgReg.cur_msg extern call failed to return result after 2 cycles!")
      val msg_len_pkts = cur_msg_stage2.bits.pipe_meta.msg_len_pkts
      val is_fully_granted = Wire(Bool())
      val pending_msg_info = io.pendingMsgReg.cur_msg_resp.bits
      is_fully_granted := (pending_msg_info.grantedIdx >= msg_len_pkts)

      // Invoke scheduling extern to decide which msg to grant (if any)
      grant_sched_stage1.bits.pipe_meta.grant_sched_expect_resp := true.B
      io.grantScheduler.req.valid := true.B
      io.grantScheduler.req.bits.rx_msg_id    := cur_msg_stage2.bits.ingress_meta.rx_msg_id
      io.grantScheduler.req.bits.rank         := pending_msg_info.remaining_size
      io.grantScheduler.req.bits.removeObj    := is_fully_granted
      io.grantScheduler.req.bits.grantableIdx := pending_msg_info.grantableIdx
      io.grantScheduler.req.bits.grantedIdx   := pending_msg_info.grantedIdx
    }
  }

  grant_sched_stage2 := grant_sched_stage1

  grant_msg_stage1 := grant_sched_stage2 // default

  // Analyze results of grant scheduler extern and generate GRANT if needed
  when (grant_sched_stage2.valid && !reset.toBool) {
    when (grant_sched_stage2.bits.pipe_meta.grant_sched_expect_resp) {
      assert(io.grantScheduler.resp.valid, "grantScheduler extern call failed to return result after 2 cycles!")
      val sched_resp = io.grantScheduler.resp.bits
      when(sched_resp.success && sched_resp.prio_level < HOMA_OVERCOMMITMENT_LEVEL.U) {
        // generate GRANT
        grant_msg_stage1.bits.pipe_meta.genGRANT := true.B
        grant_msg_stage1.bits.pipe_meta.grant_prio := sched_resp.prio_level + HOMA_NUM_UNSCHEDULED_PRIOS.U
        grant_msg_stage1.bits.pipe_meta.grant_offset := sched_resp.grant_offset
        // update granted msg state
        // NOTE: the scheduler should return the rx_msg_id of the msg to be granted
        //   as well as the grant_offset for that msg.
        io.pendingMsgReg.grant_msg_req.valid := true.B
        io.pendingMsgReg.grant_msg_req.bits.index := sched_resp.grant_msg_id
        io.pendingMsgReg.grant_msg_req.bits.grantedIdx := sched_resp.grant_offset
      }
    }
  }

  grant_msg_stage2 := grant_msg_stage1

  // info for granted msg is now available
  // fire grantPktEvent
  when (grant_msg_stage2.valid && !reset.toBool) {
    when (grant_msg_stage2.bits.pipe_meta.genGRANT) {
      assert(io.pendingMsgReg.grant_msg_resp.valid, "pendingMsgReg.grant_msg extern call failed to return result after 2 cycles!")
      val grant_msg_info = io.pendingMsgReg.grant_msg_resp.bits
      // copy grant_msg_info into fields for GRANT pkt
      io.grantPkt.valid := true.B
      io.grantPkt.bits.dst_ip         := grant_msg_info.src_ip
      io.grantPkt.bits.dst_context    := grant_msg_info.src_context
      io.grantPkt.bits.msg_len        := grant_msg_info.msg_len
      io.grantPkt.bits.pkt_offset     := grant_msg_info.ackNo
      io.grantPkt.bits.src_context    := grant_msg_info.dst_context
      io.grantPkt.bits.tx_msg_id      := grant_msg_info.tx_msg_id
      io.grantPkt.bits.buf_ptr        := grant_msg_info.buf_ptr
      io.grantPkt.bits.buf_size_class := grant_msg_info.buf_size_class
      io.grantPkt.bits.grant_offset   := grant_msg_stage2.bits.pipe_meta.grant_offset
      io.grantPkt.bits.grant_prio     := grant_msg_stage2.bits.pipe_meta.grant_prio
      io.grantPkt.bits.flags          := GRANT_MASK
      io.grantPkt.bits.is_new_msg     := false.B
      io.grantPkt.bits.is_rtx         := false.B
    }
  }

  // write to maMetaQueue 
  when (grant_msg_stage2.valid && !reset.toBool) {
    maMetaQueue_in.valid := true.B
    maMetaQueue_in.bits := grant_msg_stage2.bits
    assert(maMetaQueue_in.ready, "maMetaQueue is full in Ingress pipeline!")
  }

  // state machine to drive net_out and meta_out
  val pktOutState = RegInit(sWordOne)

  switch (pktOutState) {
    is (sWordOne) {
      // wait for both metadata and payload
      when (maPktQueue_out.valid && maMetaQueue_out.valid) {
        when (maMetaQueue_out.bits.pipe_meta.drop) {
          // drop the pkt payload and metadata
          maPktQueue_out.ready := true.B
          maMetaQueue_out.ready := true.B
          when (!maPktQueue_out.bits.last) {
            pktOutState := sDropPkt
          }
        } .otherwise {
          // transfer first word (and metadata)
          io.net_out <> maPktQueue_out
          io.meta_out.valid := true.B
          io.meta_out.bits := maMetaQueue_out.bits.ingress_meta
          maMetaQueue_out.ready := io.net_out.ready // only read metaQueue when first word is transferred
          when (io.net_out.ready && !maPktQueue_out.bits.last) {
            pktOutState := sFinishPkt
          }
        }
      }
    }
    is (sFinishPkt) {
      io.net_out <> maPktQueue_out
      when (maPktQueue_out.valid && maPktQueue_out.ready && maPktQueue_out.bits.last) {
        pktOutState := sWordOne
      }
    }
    is (sDropPkt) {
      maPktQueue_out.ready := true.B
      when (maPktQueue_out.valid && maPktQueue_out.ready && maPktQueue_out.bits.last) {
        pktOutState := sWordOne
      }
    }
  }

}

/* Grant Scheduling Extern */
class GrantSchedulerIO extends Bundle {
  val req = Valid(new GrantSchedulerReq)
  val resp = Flipped(Valid(new GrantSchedulerResp))
}

class GrantSchedulerReq extends Bundle {
  val rx_msg_id = UInt(MSG_ID_BITS.W)
  val rank = UInt(PKT_OFFSET_BITS.W)
  val removeObj = Bool()
  // TODO(sibanez): check the bit width of these fields. What is their expected range?
  val grantableIdx = UInt(CREDIT_BITS.W)
  val grantedIdx = UInt(CREDIT_BITS.W)
}

class GrantSchedulerResp extends Bundle {
  val success = Bool() // a msg to grant was identified
  val prio_level = UInt(HOMA_PRIO_BITS.W)
  val grant_offset = UInt(CREDIT_BITS.W)
  val grant_msg_id = UInt(MSG_ID_BITS.W)
}

class GrantMsgState extends Bundle {
  val valid = Bool()
  val rx_msg_id = UInt(MSG_ID_BITS.W)
  val rank = UInt(PKT_OFFSET_BITS.W)
  val grantableIdx = UInt(CREDIT_BITS.W)
  val grantedIdx = UInt(CREDIT_BITS.W)
}

@chiselName
class GrantScheduler(implicit p: Parameters) extends Module {
  val io = IO(Flipped(new GrantSchedulerIO))

  /* Expected functionality:
   *   - Insert / remove msg state as required.
   *   - Identify the highest priority (lowest rank) msg for which
   *     grantableIdx > grantedIdx.
   *   - Also identify how many messages have higher priority (lower rank)
   *   - If the number of msgs that have higher priority is less than the
   *     HOMA_OVERCOMMITMENT_LEVEL then the msg should be granted.
   *   - Update grantedIdx to grantableIdx when granting a msg.
   *   - Return the grant_offset, msg_id, and num msgs with higher priority
   */

  val msg_state = RegInit(VecInit(Seq.fill(NUM_MSG_BUFFERS)((new GrantMsgState).fromBits(0.U))))

  // Cycle 1: When receiving a request, update rx_msg_id's rank, grantableIdx,
  //   and grantedIDx. Mark the msg's valid flag as !removeObj.
  val req_valid_reg_0 = RegNext(io.req.valid)

  when (io.req.valid && !reset.toBool) {
    msg_state(io.req.bits.rx_msg_id).valid := !io.req.bits.removeObj
    msg_state(io.req.bits.rx_msg_id).rank := io.req.bits.rank
    msg_state(io.req.bits.rx_msg_id).grantableIdx := io.req.bits.grantableIdx
    msg_state(io.req.bits.rx_msg_id).grantedIdx := io.req.bits.grantedIdx
  }

  // Cycle 2: Find the highest priority grantable msg and count the number of
  //   higher priority valid messages (prio_level).
  //   If prio_level < HOMA_OVERCOMMITMENT_LEVEL, grant the msg (update grantedIdx).

  // Identify all valid, grantable msgs
  val grantable_msgs = VecInit(msg_state.map( msg => msg.valid && (msg.grantableIdx > msg.grantedIdx) ))

  // Compute the highest priority grantable msg
  val candidate = msg_state.reduce( (msg1, msg2) => {
    val result_msg = Wire(new GrantMsgState)

    val msg1_is_grantable = grantable_msgs(msg1.rx_msg_id)
    val msg2_is_grantable = grantable_msgs(msg2.rx_msg_id)

    when (msg1_is_grantable && msg2_is_grantable) {
      when (msg1.rank <= msg2.rank) {
        result_msg := msg1
      } .otherwise {
        result_msg := msg2
      }
    } .elsewhen (msg2_is_grantable) {
      result_msg := msg2
    } .otherwise {
      // only msg1 is grantable OR neither msg is grantable
      result_msg := msg1
    }    
    result_msg
  })
  val grant_candidate = Wire(new GrantMsgState)
  grant_candidate := candidate

  // Identify all valid msgs that are higher priority than the candidate.
  // Use rx_msg_id to break ties.
  val higher_prio_msgs = VecInit(msg_state.map( msg => msg.valid && ( (msg.rank < grant_candidate.rank) || 
      (msg.rank === grant_candidate.rank && msg.rx_msg_id < grant_candidate.rx_msg_id) ) ))

  // Count the number of higher priority msgs
  val prio_level = Wire(UInt())
  prio_level := PopCount(higher_prio_msgs.asUInt)

  // output registers
  val result_reg = RegInit( (new GrantSchedulerResp).fromBits(0.U))
  val req_valid_reg_1 = RegNext(req_valid_reg_0)

  when (req_valid_reg_0 && !reset.toBool) {
    when ( (prio_level < HOMA_OVERCOMMITMENT_LEVEL.U) && grant_candidate.valid && (grant_candidate.grantableIdx > grant_candidate.grantedIdx)) {
      // The msg should be granted.
      // Fill out the result registers and update the msg state
      result_reg.success := true.B
      msg_state(grant_candidate.rx_msg_id).grantedIdx := grant_candidate.grantableIdx
    } .otherwise {
      // The msg should NOT be granted.
      result_reg.success := false.B
    }
    result_reg.prio_level := prio_level
    result_reg.grant_offset := grant_candidate.grantableIdx
    result_reg.grant_msg_id := grant_candidate.rx_msg_id
  }

  // Drive the response IO
  io.resp.valid := req_valid_reg_1
  io.resp.bits := result_reg
}


/* PendingMsgReg Extern */
class PendingMsgRegIO extends Bundle {
  val cur_msg_req = Valid(new CurMsgReq)
  val cur_msg_resp = Flipped(Valid(new GrantInfo))
  val grant_msg_req = Valid(new GrantMsgReq)
  val grant_msg_resp = Flipped(Valid(new PendingMsgInfo))
}

class PendingMsgInfo extends Bundle {
  val src_ip = UInt(32.W)
  val src_context = UInt(LNIC_CONTEXT_BITS.W)
  val dst_context = UInt(LNIC_CONTEXT_BITS.W)
  val tx_msg_id = UInt(MSG_ID_BITS.W)
  val msg_len = UInt(MSG_LEN_BITS.W)
  val buf_ptr = UInt(BUF_PTR_BITS.W)
  val buf_size_class = UInt(SIZE_CLASS_BITS.W)
  val ackNo = UInt((PKT_OFFSET_BITS + 1).W) // ackNo needs to reach max_num_pkts + 1
}

class GrantInfo extends Bundle {
  val grantedIdx = UInt(CREDIT_BITS.W)
  val grantableIdx = UInt(CREDIT_BITS.W)
  val remaining_size = UInt(PKT_OFFSET_BITS.W)
}

class CurMsgReq extends Bundle {
  val index = UInt(MSG_ID_BITS.W)
  val msg_info = new PendingMsgInfo
  val grant_info = new GrantInfo
  val is_new_msg = Bool()
}

class GrantMsgReq extends Bundle {
  val index = UInt(MSG_ID_BITS.W)
  val grantedIdx = UInt(CREDIT_BITS.W)
}

class PendingMsgState extends Bundle {
  val msg_info = new PendingMsgInfo
  val grant_info = new GrantInfo
}

@chiselName
class PendingMsgReg(implicit p: Parameters) extends Module {
  val io = IO(Flipped(new PendingMsgRegIO))

  val ram = Module(new TrueDualPortRAM((new PendingMsgState).getWidth, NUM_MSG_BUFFERS))
  ram.io.clock := clock
  ram.io.reset := reset
  ram.io.portA.we := false.B
  ram.io.portB.we := false.B

  /* state machine to process Current Message Requests
   *   - Used to access / update state for the current message (i.e. the msg
   *     corresponding to the DATA pkt that just arrived).
   *   - If is_new_msg is set, then initialize the message state using the provided data
   *   - Otherwise, increment grantableIdx, decrement remaining_size, and update ackNo
   */

  val sRead :: sWrite :: Nil = Enum(2)
  val cur_msg_fsm = RegInit(sRead)

  // pipeline reg
  val cur_msg_req_reg_0 = RegNext(io.cur_msg_req)
  val cur_msg_req_reg_1 = RegNext(cur_msg_req_reg_0)

  // Need 2 cycles for RMW operation
  assert(!(cur_msg_req_reg_0.valid && cur_msg_req_reg_1.valid), "Violated assumption that requests will not arrive on back-to-back cycles!")

  // defaults
  io.cur_msg_resp.valid := false.B

  ram.io.portA.addr := cur_msg_req_reg_0.bits.index // default

  switch(cur_msg_fsm) {
    is (sRead) {
      when (cur_msg_req_reg_0.valid) {
        when (cur_msg_req_reg_0.bits.is_new_msg) {
          // initialize all the pending msg state
          val init_msg_state = Wire(new PendingMsgState)
          init_msg_state.msg_info := cur_msg_req_reg_0.bits.msg_info
          init_msg_state.grant_info := cur_msg_req_reg_0.bits.grant_info
          ram.io.portA.din := init_msg_state.asUInt
          ram.io.portA.we  := true.B
        }
        cur_msg_fsm := sWrite
      }
    }
    is (sWrite) {
      // get read result
      val cur_msg_state = Wire(new PendingMsgState)
      cur_msg_state := (new PendingMsgState).fromBits(ram.io.portA.dout)

      val cur_msg_resp = Wire(new GrantInfo)
      // default
      cur_msg_resp := cur_msg_req_reg_1.bits.grant_info

      when (!cur_msg_req_reg_1.bits.is_new_msg) {
        // increment grantableIdx, decrement remaining_size, and update ackNo
        val new_msg_state = Wire(new PendingMsgState)
        new_msg_state.msg_info       := cur_msg_state.msg_info
        new_msg_state.msg_info.ackNo := cur_msg_req_reg_1.bits.msg_info.ackNo
        new_msg_state.grant_info.grantedIdx     := cur_msg_state.grant_info.grantedIdx
        new_msg_state.grant_info.grantableIdx   := cur_msg_state.grant_info.grantableIdx + 1.U
        new_msg_state.grant_info.remaining_size := cur_msg_state.grant_info.remaining_size - 1.U

        // Update state
        ram.io.portA.addr := cur_msg_req_reg_1.bits.index
        ram.io.portA.din  := new_msg_state.asUInt
        ram.io.portA.we   := true.B

        // Drive outputs
        cur_msg_resp := new_msg_state.grant_info
      }

      // write response
      io.cur_msg_resp.valid := !reset.toBool
      io.cur_msg_resp.bits := cur_msg_resp

      // state transition
      cur_msg_fsm := sRead
    }
  }

  /* state machine to process Grant Message Requests 
   *   - Used to update the grantedIdx of the msg being granted.
   *   - Returns all of the info about the granted msg that is needed to generate
   *     a GRANT pkt.
   */

  val grant_msg_fsm = RegInit(sRead)

  // pipeline reg
  val grant_msg_req_reg_0 = RegNext(io.grant_msg_req)
  val grant_msg_req_reg_1 = RegNext(grant_msg_req_reg_0)

  // Need 2 cycles for RMW operation
  assert(!(grant_msg_req_reg_0.valid && grant_msg_req_reg_1.valid), "Violated assumption that requests will not arrive on back-to-back cycles!")

  // defaults
  io.grant_msg_resp.valid := false.B

  ram.io.portB.addr := grant_msg_req_reg_0.bits.index // default

  switch(grant_msg_fsm) {
    is (sRead) {
      when (grant_msg_req_reg_0.valid) {
        grant_msg_fsm := sWrite
      }
    }
    is (sWrite) {
      // get read result
      val grant_msg_state = Wire(new PendingMsgState)
      grant_msg_state := (new PendingMsgState).fromBits(ram.io.portB.dout)

      // update msg state
      val new_grant_msg_state = Wire(new PendingMsgState)
      new_grant_msg_state := grant_msg_state // default
      new_grant_msg_state.grant_info.grantedIdx := grant_msg_req_reg_1.bits.grantedIdx

      ram.io.portB.addr := grant_msg_req_reg_1.bits.index
      ram.io.portB.din  := new_grant_msg_state.asUInt
      ram.io.portB.we   := true.B

      // write response
      io.grant_msg_resp.valid := !reset.toBool
      io.grant_msg_resp.bits := new_grant_msg_state.msg_info

      // state transition
      grant_msg_fsm := sRead
    }
  }

}
