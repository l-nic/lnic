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
 * All IO for the NDP Ingress module.
 */
class NDPIngressIO extends Bundle {
  val net_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val meta_out = Valid(new IngressMetaOut)
  val get_rx_msg_info = new GetRxMsgInfoIO
  val delivered = Valid(new DeliveredEvent)
  val creditToBtx = Valid(new CreditToBtxEvent)
  val ctrlPkt = Valid(new EgressMetaIn)
  val creditReg = new IfElseRawIO
  val nic_mac_addr = Input(UInt(ETH_MAC_BITS.W))
  val switch_mac_addr = Input(UInt(ETH_MAC_BITS.W))
  val nic_ip_addr = Input(UInt(32.W))
  val rtt_pkts = Input(UInt(CREDIT_BITS.W))

  override def cloneType = new NDPIngressIO().asInstanceOf[this.type]
}

class NDPHeaders extends Bundle {
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
  // NDP Header
  val ndp_flags = UInt(8.W)
  val ndp_src = UInt(LNIC_CONTEXT_BITS.W)
  val ndp_dst = UInt(LNIC_CONTEXT_BITS.W)
  val ndp_msg_len = UInt(MSG_LEN_BITS.W)
  val ndp_pkt_offset = UInt(PKT_OFFSET_BITS.W)
  val ndp_pull_offset = UInt(CREDIT_BITS.W)
  val ndp_tx_msg_id = UInt(MSG_ID_BITS.W)
  val ndp_buf_ptr = UInt(BUF_PTR_BITS.W)
  val ndp_buf_size_class = UInt(SIZE_CLASS_BITS.W)
  val ndp_padding = UInt(120.W) // padding to make header len = 64B for easy parsing / deparsing
}

// Metadata that is only used within the M/A pipeline
class NDPPipeMeta extends Bundle {
  val drop = Bool()
  val is_data = Bool()
  val flags = UInt(8.W)
  val buf_ptr = UInt(BUF_PTR_BITS.W)
  val buf_size_class = UInt(SIZE_CLASS_BITS.W)
  val pull_offset = UInt(CREDIT_BITS.W)
  val genACK = Bool()
  val genNACK = Bool()
  val genPULL = Bool()
  val expect_resp = Bool()
}

class NDPPipelineRegs extends Bundle {
  val ingress_meta = new IngressMetaOut
  val pipe_meta = new NDPPipeMeta
}


@chiselName
class NDPIngress(implicit p: Parameters) extends Module {
  val io = IO(new NDPIngressIO)

  val parserPktQueue_out = Wire(Flipped(Decoupled(new StreamChannel(NET_DP_BITS))))
  val headers = Wire(new NDPHeaders)
  val headers_reg = Reg(Valid(new NDPHeaders))
  headers_reg.valid := false.B // default

  val maPktQueue_in = Wire(Decoupled(new StreamChannel(NET_DP_BITS)))
  val maPktQueue_out = Wire(Flipped(Decoupled(new StreamChannel(NET_DP_BITS))))
  maPktQueue_in.valid := false.B // default
  maPktQueue_out.ready := false.B // default

  // need metadata queue to synchronize metadata with pkt payload
  val maMetaQueue_in = Wire(Decoupled(new NDPPipelineRegs))
  val maMetaQueue_out = Wire(Flipped(Decoupled(new NDPPipelineRegs)))
  maMetaQueue_in.valid := false.B // default
  maMetaQueue_out.ready := false.B // default

  // default IO
  io.net_out.valid := false.B
  io.meta_out.valid := false.B
  io.get_rx_msg_info.req.valid := false.B
  io.delivered.valid := false.B
  io.creditToBtx.valid := false.B
  io.ctrlPkt.valid := false.B
  io.creditReg.req.valid := false.B

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
        headers := (new NDPHeaders).fromBits(reverse_bytes(parserPktQueue_out.bits.data, NET_DP_BYTES))
        when (headers.eth_type === IPV4_TYPE && headers.ip_proto === NDP_PROTO && !parserPktQueue_out.bits.last) {
          // this is an LNIC pkt
          // start M/A processing
          headers_reg.valid := true.B
          headers_reg.bits := headers
          // forward pkt payload along
          parseState := sFinishPkt
        } .elsewhen (!parserPktQueue_out.bits.last) {
          // this is not an LNIC pkt (and it has additional data)
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

  val rx_info_stage1 = Reg(Valid(new NDPPipelineRegs))
  val rx_info_stage2 = Reg(Valid(new NDPPipelineRegs))
  val credit_stage1 = Reg(Valid(new NDPPipelineRegs))
  val credit_stage2 = Reg(Valid(new NDPPipelineRegs))

  maPktQueue_out <> Queue(maPktQueue_in, MA_PKT_QUEUE_FLITS)
  maMetaQueue_out <> Queue(maMetaQueue_in, MA_META_QUEUE_FLITS)

  rx_info_stage1.valid := headers_reg.valid

  // wait for LNIC pkt headers to be parsed
  when (headers_reg.valid && !reset.toBool) {
    // fill out as much metadata as we can right now
    rx_info_stage1.bits.ingress_meta.src_ip      := headers_reg.bits.ip_src
    rx_info_stage1.bits.ingress_meta.src_context := headers_reg.bits.ndp_src
    rx_info_stage1.bits.ingress_meta.msg_len     := headers_reg.bits.ndp_msg_len
    rx_info_stage1.bits.ingress_meta.pkt_offset  := headers_reg.bits.ndp_pkt_offset
    rx_info_stage1.bits.ingress_meta.dst_context := headers_reg.bits.ndp_dst
    rx_info_stage1.bits.ingress_meta.rx_msg_id   := 0.U // default
    rx_info_stage1.bits.ingress_meta.tx_msg_id   := headers_reg.bits.ndp_tx_msg_id
    rx_info_stage1.bits.ingress_meta.is_last_pkt := false.B // default
    rx_info_stage1.bits.pipe_meta.drop           := false.B // default
    rx_info_stage1.bits.pipe_meta.flags          := headers_reg.bits.ndp_flags
    rx_info_stage1.bits.pipe_meta.buf_ptr        := headers_reg.bits.ndp_buf_ptr
    rx_info_stage1.bits.pipe_meta.buf_size_class := headers_reg.bits.ndp_buf_size_class
    rx_info_stage1.bits.pipe_meta.pull_offset    := headers_reg.bits.ndp_pull_offset
    rx_info_stage1.bits.pipe_meta.genACK         := false.B // default
    rx_info_stage1.bits.pipe_meta.genNACK        := false.B // default
    rx_info_stage1.bits.pipe_meta.genPULL        := false.B // default
    rx_info_stage1.bits.pipe_meta.expect_resp    := false.B // default
    when ((headers_reg.bits.ndp_flags & DATA_MASK) > 0.U) {
      // this is a DATA pkt
      rx_info_stage1.bits.pipe_meta.is_data := true.B

      val is_chopped = Wire(Bool())
      is_chopped := (headers_reg.bits.ndp_flags & CHOP_MASK) > 0.U
      // do not pass CHOP pkts to the assembly module
      rx_info_stage1.bits.pipe_meta.drop := is_chopped

      // NOTE: Both DATA and CHOP'ed DATA pkts need to invoke get_rx_msg_info
      // because they both need to access the creditReg to compute the PULL offset.

      // invoke get_rx_msg_info extern
      io.get_rx_msg_info.req.valid := true.B
      io.get_rx_msg_info.req.bits.mark_received := !is_chopped // CHOP pkts don't have any data
      io.get_rx_msg_info.req.bits.src_ip      := headers_reg.bits.ip_src
      io.get_rx_msg_info.req.bits.src_context := headers_reg.bits.ndp_src
      io.get_rx_msg_info.req.bits.tx_msg_id   := headers_reg.bits.ndp_tx_msg_id
      io.get_rx_msg_info.req.bits.msg_len     := headers_reg.bits.ndp_msg_len
      io.get_rx_msg_info.req.bits.pkt_offset  := headers_reg.bits.ndp_pkt_offset
    } .otherwise {
      // this is an ACK/NACK/PULL pkt
      rx_info_stage1.bits.pipe_meta.is_data := false.B
      // do not pass control pkts to assembly module
      rx_info_stage1.bits.pipe_meta.drop := true.B
    }
  }

  rx_info_stage2 := rx_info_stage1

  credit_stage1 := rx_info_stage2 // default

  // rx_info_stage2 (this is when get_rx_msg_info extern call should return)
  when (rx_info_stage2.valid && !reset.toBool) {
    when (rx_info_stage2.bits.pipe_meta.is_data) {
      assert(io.get_rx_msg_info.resp.valid, "get_rx_msg_info extern call failed to return result after 2 cycles!")

      // defaults
      credit_stage1.bits.pipe_meta.genACK  := false.B
      credit_stage1.bits.pipe_meta.genNACK := false.B
      credit_stage1.bits.pipe_meta.genPULL := true.B

      // rx_msg_id must be passed to Assembly module (for DATA pkts)
      credit_stage1.bits.ingress_meta.rx_msg_id := io.get_rx_msg_info.resp.bits.rx_msg_id

      // get_rx_msg_info extern indicates if this is the last pkt of the msg.
      // Need to pass this flag to the Assembly module so it can schedule the msg for
      // delivery to the CPU.
      credit_stage1.bits.ingress_meta.is_last_pkt := io.get_rx_msg_info.resp.bits.is_last_pkt

      val pull_offset_diff = Wire(UInt(CREDIT_BITS.W))
      pull_offset_diff := 0.U
      when ((rx_info_stage2.bits.pipe_meta.flags & CHOP_MASK) > 0.U) {
        // this is a chopped data pkt
        credit_stage1.bits.pipe_meta.genNACK := true.B
      } .otherwise {
        // this is a normal data pkt
        credit_stage1.bits.pipe_meta.genACK := true.B
        pull_offset_diff := 1.U
      }

      when (io.get_rx_msg_info.resp.bits.fail) {
        credit_stage1.bits.pipe_meta.drop := true.B
      } .otherwise {
        // compute PULL offset
        io.creditReg.req.valid := true.B
        io.creditReg.req.bits.index     := io.get_rx_msg_info.resp.bits.rx_msg_id
        io.creditReg.req.bits.data_1    := pull_offset_diff
        io.creditReg.req.bits.opCode_1  := REG_ADD
        io.creditReg.req.bits.data_0    := io.rtt_pkts + pull_offset_diff
        io.creditReg.req.bits.opCode_0  := REG_WRITE
        io.creditReg.req.bits.predicate := io.get_rx_msg_info.resp.bits.is_new_msg
        credit_stage1.bits.pipe_meta.expect_resp := true.B
      }
    }
  }

  credit_stage2 := credit_stage1

  when (credit_stage2.valid && !reset.toBool) {
    when (credit_stage2.bits.pipe_meta.expect_resp) {
      assert(io.creditReg.resp.valid, "creditReg extern call failed to return result after 2 cycles!")
      val pull_offset = io.creditReg.resp.bits.new_val

      val flags = Wire(UInt(8.W))
      val ack_flag  = Mux(credit_stage2.bits.pipe_meta.genACK, ACK_MASK, 0.U) 
      val nack_flag = Mux(credit_stage2.bits.pipe_meta.genNACK, NACK_MASK, 0.U)
      val pull_flag = Mux(credit_stage2.bits.pipe_meta.genPULL, PULL_MASK, 0.U)
      flags := ack_flag | nack_flag | pull_flag

      // fire control pkt event
      // TODO: this only fires when the pkt is successfully allocated a buffer & rx_msg_id.
      //   This means that when a pkt fails to be allocated a buffer & rx_msg_id, it will be
      //   silently dropped. Maybe it should be NACKed instead?
      io.ctrlPkt.valid := true.B
      io.ctrlPkt.bits.dst_ip         := credit_stage2.bits.ingress_meta.src_ip
      io.ctrlPkt.bits.dst_context    := credit_stage2.bits.ingress_meta.src_context
      io.ctrlPkt.bits.msg_len        := credit_stage2.bits.ingress_meta.msg_len
      io.ctrlPkt.bits.pkt_offset     := credit_stage2.bits.ingress_meta.pkt_offset
      io.ctrlPkt.bits.src_context    := credit_stage2.bits.ingress_meta.dst_context
      io.ctrlPkt.bits.tx_msg_id      := credit_stage2.bits.ingress_meta.tx_msg_id
      io.ctrlPkt.bits.buf_ptr        := credit_stage2.bits.pipe_meta.buf_ptr
      io.ctrlPkt.bits.buf_size_class := credit_stage2.bits.pipe_meta.buf_size_class
      io.ctrlPkt.bits.credit         := pull_offset
      io.ctrlPkt.bits.rank           := 0.U // unused for NDP
      io.ctrlPkt.bits.flags          := flags
      io.ctrlPkt.bits.is_new_msg     := false.B
      io.ctrlPkt.bits.is_rtx         := false.B

    } .otherwise {
      when ((credit_stage2.bits.pipe_meta.flags & ACK_MASK) > 0.U) {
        // fire delivered event
        io.delivered.valid := true.B
        io.delivered.bits.tx_msg_id      := credit_stage2.bits.ingress_meta.tx_msg_id
        io.delivered.bits.delivered_pkts := (1.U << credit_stage2.bits.ingress_meta.pkt_offset)
        io.delivered.bits.msg_len        := credit_stage2.bits.ingress_meta.msg_len
        io.delivered.bits.buf_ptr        := credit_stage2.bits.pipe_meta.buf_ptr
        io.delivered.bits.buf_size_class := credit_stage2.bits.pipe_meta.buf_size_class
      }

      when ( ((credit_stage2.bits.pipe_meta.flags & NACK_MASK) > 0.U) || ((credit_stage2.bits.pipe_meta.flags & PULL_MASK) > 0.U) ) {
        val rtx = ((credit_stage2.bits.pipe_meta.flags & NACK_MASK) > 0.U)
        val update_credit = ((credit_stage2.bits.pipe_meta.flags & PULL_MASK) > 0.U)

        // fire creditToBtx event
        io.creditToBtx.valid := true.B

        io.creditToBtx.bits.tx_msg_id      := credit_stage2.bits.ingress_meta.tx_msg_id
        io.creditToBtx.bits.rtx            := rtx
        io.creditToBtx.bits.rtx_pkt_offset := credit_stage2.bits.ingress_meta.pkt_offset
        io.creditToBtx.bits.update_credit  := update_credit
        io.creditToBtx.bits.new_credit     := credit_stage2.bits.pipe_meta.pull_offset
        io.creditToBtx.bits.buf_ptr        := credit_stage2.bits.pipe_meta.buf_ptr
        io.creditToBtx.bits.buf_size_class := credit_stage2.bits.pipe_meta.buf_size_class
        io.creditToBtx.bits.dst_ip         := credit_stage2.bits.ingress_meta.src_ip
        io.creditToBtx.bits.dst_context    := credit_stage2.bits.ingress_meta.src_context
        io.creditToBtx.bits.msg_len        := credit_stage2.bits.ingress_meta.msg_len
        io.creditToBtx.bits.src_context    := credit_stage2.bits.ingress_meta.dst_context

      }
    }
  }

  // write to maMetaQueue 
  when (credit_stage2.valid && !reset.toBool) {
    maMetaQueue_in.valid := true.B
    maMetaQueue_in.bits := credit_stage2.bits
    assert(maMetaQueue_in.ready, "maMetaQueue is full in Ingress pipeline!")
  }

  // state machine to drive net_out and meta_out
  val pktOutState = RegInit(sWordOne)

  switch (pktOutState) {
    is (sWordOne) {
      // wait for both metadata and payload
      when (maPktQueue_out.valid && maMetaQueue_out.valid) {
        when (maMetaQueue_out.bits.pipe_meta.drop) {
          // drop the pkt payload an metadata
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

/* IfElseRawReg Extern */
class IfElseRawIO extends Bundle {
  val req = Valid(new IfElseRawReq)
  val resp = Flipped(Valid(new IfElseRawResp))
}

class IfElseRawReq extends Bundle {
  val index = UInt(MSG_ID_BITS.W)
  val data_1 = UInt(CREDIT_BITS.W)
  val opCode_1 = UInt(8.W)
  val data_0 = UInt(CREDIT_BITS.W)
  val opCode_0 = UInt(8.W)
  val predicate = Bool()
}

class IfElseRawResp extends Bundle {
  val new_val = UInt(CREDIT_BITS.W)
}

@chiselName
class IfElseRaw(implicit p: Parameters) extends Module {
  val io = IO(Flipped(new IfElseRawIO))

  val ram = SyncReadMem(NUM_MSG_BUFFERS, UInt(CREDIT_BITS.W))

  /* state machine to perform RMW ops on ram */

  val REG_READ  = 0.U
  val REG_WRITE = 1.U
  val REG_ADD   = 2.U

  val sRead :: sWrite :: Nil = Enum(2)
  val state = RegInit(sRead)

  // pipeline reg
  val req_reg_0 = RegNext(io.req)
  val req_reg_1 = RegNext(req_reg_0)

  // Need 2 cycles for RMW operation
  assert(!(req_reg_0.valid && req_reg_1.valid), "Violated assumption that requests will not arrive on back-to-back cycles!")

  val ram_ptr = Wire(UInt(MSG_ID_BITS.W))
  ram_ptr := req_reg_0.bits.index
  val ram_port = ram(ram_ptr)

  // defaults
  io.resp.valid := false.B

  switch(state) {
    is (sRead) {
      when (req_reg_0.valid) {
        // start reading ram
        state := sWrite
      }
    }
    is (sWrite) {
      // get read result
      val cur_val = Wire(UInt(CREDIT_BITS.W))
      cur_val := ram_port

      // compute new val
      val new_val = Wire(UInt(CREDIT_BITS.W))
      when (req_reg_1.bits.predicate) {
        // do opCode_0
        doRMW(cur_val, req_reg_1.bits.opCode_0, req_reg_1.bits.data_0, new_val)
      } .otherwise {
        // do opCode_1
        doRMW(cur_val, req_reg_1.bits.opCode_1, req_reg_1.bits.data_1, new_val)
      }

      // update ram
      ram_ptr := req_reg_1.bits.index
      ram_port := new_val

      // write response
      io.resp.valid := !reset.toBool
      io.resp.bits.new_val := new_val

      // state transition
      state := sRead
    }
  }

  def doRMW(cur_val: UInt, opCode: UInt, data: UInt, new_val: UInt) = {
    switch (opCode) {
      is (REG_READ) {
        new_val := cur_val
      }
      is (REG_WRITE) {
        new_val := data
      }
      is (REG_ADD) {
        new_val := cur_val + data
      }
    }
  }

}

