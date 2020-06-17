package lnic

import Chisel._

import chisel3.{VecInit, SyncReadMem}
import chisel3.experimental._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.rocket._
import freechips.rocketchip.rocket.NetworkHelpers._
import freechips.rocketchip.rocket.LNICRocketConsts._
import LNICConsts._

class PISAIngressMetaOut extends Bundle {
  // metadata for pkts going to CPU
  val src_ip       = UInt(32.W)
  val src_context  = UInt(LNIC_CONTEXT_BITS.W)
  val msg_len      = UInt(MSG_LEN_BITS.W)
  val pkt_offset   = UInt(PKT_OFFSET_BITS.W)
  val dst_context  = UInt(LNIC_CONTEXT_BITS.W)
  val rx_msg_id    = UInt(MSG_ID_BITS.W)
  val tx_msg_id    = UInt(MSG_ID_BITS.W)
}

/**
 * All IO for the LNIC PISA Ingress module.
 */
class LNICPISAIngressIO extends Bundle {
  val net_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val meta_out = Valid(new PISAIngressMetaOut)
  val get_rx_msg_info = new GetRxMsgInfoIO
  val delivered = Valid(new DeliveredEvent)
  val creditToBtx = Valid(new CreditToBtxEvent)
  val ctrlPkt = Valid(new PISAEgressMetaIn)
  val creditReg = new IfElseRawIO

  override def cloneType = new LNICPISAIngressIO().asInstanceOf[this.type]
}

/* IO for Ingress extern call */
class GetRxMsgInfoIO extends Bundle {
  val req = Valid(new GetRxMsgInfoReq)
  val resp = Flipped(Valid(new GetRxMsgInfoResp))
}

class GetRxMsgInfoReq extends Bundle {
  val src_ip = UInt(32.W)
  val src_context = UInt(LNIC_CONTEXT_BITS.W)
  val tx_msg_id = UInt(MSG_ID_BITS.W)
  val msg_len = UInt(MSG_LEN_BITS.W)
}

class GetRxMsgInfoResp extends Bundle {
  val fail = Bool()
  val rx_msg_id = UInt(MSG_ID_BITS.W)
  // TODO(sibanez): add additional fields for transport processing
  val is_new_msg = Bool()
}

class DeliveredEvent extends Bundle {
  val tx_msg_id = UInt(MSG_ID_BITS.W)
  val pkt_offset = UInt(PKT_OFFSET_BITS.W)
  val msg_len = UInt(MSG_LEN_BITS.W)
  val buf_ptr = UInt(BUF_PTR_BITS.W)
  val buf_size_class = UInt(SIZE_CLASS_BITS.W)
}

class CreditToBtxEvent extends Bundle {
  val tx_msg_id = UInt(MSG_ID_BITS.W)
  val rtx = Bool()
  val rtx_pkt_offset = UInt(PKT_OFFSET_BITS.W)
  val update_credit = Bool()
  val new_credit = UInt(CREDIT_BITS.W)
  // Additional fields for generating pkts
  // NOTE: these could be stored in tables indexed by tx_msg_id, but this would require extra state ...
  val buf_ptr = UInt(BUF_PTR_BITS.W)
  val buf_size_class = UInt(SIZE_CLASS_BITS.W)
  val dst_ip = UInt(32.W)
  val dst_context = UInt(LNIC_CONTEXT_BITS.W)
  val msg_len = UInt(MSG_LEN_BITS.W)
  val src_context = UInt(LNIC_CONTEXT_BITS.W)
}

class Headers extends Bundle {
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
  // LNIC Header
  val lnic_flags = UInt(8.W)
  val lnic_src = UInt(LNIC_CONTEXT_BITS.W)
  val lnic_dst = UInt(LNIC_CONTEXT_BITS.W)
  val lnic_msg_len = UInt(MSG_LEN_BITS.W)
  val lnic_pkt_offset = UInt(PKT_OFFSET_BITS.W)
  val lnic_pull_offset = UInt(CREDIT_BITS.W)
  val lnic_tx_msg_id = UInt(MSG_ID_BITS.W)
  val lnic_buf_ptr = UInt(BUF_PTR_BITS.W)
  val lnic_buf_size_class = UInt(SIZE_CLASS_BITS.W)
  val lnic_padding = UInt(120.W) // padding to make header len = 64B for easy parsing / deparsing
}

// Metadata that is only used within the M/A pipeline
class PipeMeta extends Bundle {
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

class PipelineRegs extends Bundle {
  val ingress_meta = new PISAIngressMetaOut
  val pipe_meta = new PipeMeta
}


@chiselName
class Ingress(implicit p: Parameters) extends Module {
  val io = IO(new LNICPISAIngressIO)

  val parserPktQueue_out = Wire(Flipped(Decoupled(new StreamChannel(NET_DP_BITS))))
  val headers = Wire(new Headers)
  val headers_reg = Reg(Valid(new Headers))
  headers_reg.valid := false.B // default

  val maPktQueue_in = Wire(Decoupled(new StreamChannel(NET_DP_BITS)))
  val maPktQueue_out = Wire(Flipped(Decoupled(new StreamChannel(NET_DP_BITS))))
  maPktQueue_in.valid := false.B // default
  maPktQueue_out.ready := false.B // default

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
        headers := (new Headers).fromBits(reverse_bytes(parserPktQueue_out.bits.data, NET_DP_BYTES))
        when (headers.eth_type === IPV4_TYPE && headers.ip_proto === LNIC_PROTO && !parserPktQueue_out.bits.last) {
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
      when (parserPktQueue_out.bits.last) {
        parseState := sWordOne
      }
    }
    is (sDropPkt) {
      when (parserPktQueue_out.bits.last) {
        parseState := sWordOne
      }
    }
  }

  /***************************/
  /* M/A Processing Pipeline */
  /***************************/

  val rx_info_stage1 = Reg(Valid(new PipelineRegs))
  val rx_info_stage2 = Reg(Valid(new PipelineRegs))
  val credit_stage1 = Reg(Valid(new PipelineRegs))
  val credit_stage2 = Reg(Valid(new PipelineRegs))

  maPktQueue_out <> Queue(maPktQueue_in, MA_PKT_QUEUE_FLITS)

  rx_info_stage1.valid := headers_reg.valid

  // wait for LNIC pkt headers to be parsed
  when (headers_reg.valid && !reset.toBool) {
    // fill out as much metadata as we can right now
    rx_info_stage1.bits.ingress_meta.src_ip      := headers_reg.bits.ip_src
    rx_info_stage1.bits.ingress_meta.src_context := headers_reg.bits.lnic_src
    rx_info_stage1.bits.ingress_meta.msg_len     := headers_reg.bits.lnic_msg_len
    rx_info_stage1.bits.ingress_meta.pkt_offset  := headers_reg.bits.lnic_pkt_offset
    rx_info_stage1.bits.ingress_meta.dst_context := headers_reg.bits.lnic_dst
    rx_info_stage1.bits.ingress_meta.rx_msg_id   := 0.U // default
    rx_info_stage1.bits.ingress_meta.tx_msg_id   := headers_reg.bits.lnic_tx_msg_id
    rx_info_stage1.bits.pipe_meta.drop           := false.B // default
    rx_info_stage1.bits.pipe_meta.flags          := headers_reg.bits.lnic_flags
    rx_info_stage1.bits.pipe_meta.buf_ptr        := headers_reg.bits.lnic_buf_ptr
    rx_info_stage1.bits.pipe_meta.buf_size_class := headers_reg.bits.lnic_buf_size_class
    rx_info_stage1.bits.pipe_meta.pull_offset    := headers_reg.bits.lnic_pull_offset
    rx_info_stage1.bits.pipe_meta.genACK         := false.B // default
    rx_info_stage1.bits.pipe_meta.genNACK        := false.B // default
    rx_info_stage1.bits.pipe_meta.genPULL        := false.B // default
    rx_info_stage1.bits.pipe_meta.expect_resp    := false.B // default
    when ((headers_reg.bits.lnic_flags & DATA_MASK) > 0.U) {
      // this is a DATA pkt
      rx_info_stage1.bits.pipe_meta.is_data := true.B
      // invoke get_rx_msg_info extern
      io.get_rx_msg_info.req.valid := true.B
      io.get_rx_msg_info.req.bits.src_ip      := headers_reg.bits.ip_src
      io.get_rx_msg_info.req.bits.src_context := headers_reg.bits.lnic_src
      io.get_rx_msg_info.req.bits.tx_msg_id   := headers_reg.bits.lnic_tx_msg_id
      io.get_rx_msg_info.req.bits.msg_len     := headers_reg.bits.lnic_msg_len

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

      val pull_offset_diff = Wire(UInt(CREDIT_BITS.W))
      pull_offset_diff := 0.U
      when ((rx_info_stage2.bits.pipe_meta.flags & CHOP_MASK) > 0.U) {
        // this is a chopped data pkt
        credit_stage1.bits.pipe_meta.genNACK := true.B
        credit_stage1.bits.pipe_meta.drop := true.B
      } .otherwise {
        // this is a normal data pkt
        credit_stage1.bits.pipe_meta.genACK := true.B
        pull_offset_diff := 1.U
        // fill out metadata for pkt going to CPU
        credit_stage1.bits.ingress_meta.rx_msg_id := io.get_rx_msg_info.resp.bits.rx_msg_id
      }

      when (io.get_rx_msg_info.resp.bits.fail) {
        credit_stage1.bits.pipe_meta.drop := true.B
      } .otherwise {
        // compute PULL offset
        io.creditReg.req.valid := true.B
        io.creditReg.req.bits.index     := io.get_rx_msg_info.resp.bits.rx_msg_id
        io.creditReg.req.bits.data_1    := pull_offset_diff
        io.creditReg.req.bits.opCode_1  := REG_ADD
        io.creditReg.req.bits.data_0    := RTT_PKTS.U + pull_offset_diff
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

      // fire control pkt event
      io.ctrlPkt.valid := true.B
      io.ctrlPkt.bits.dst_ip         := credit_stage2.bits.ingress_meta.src_ip
      io.ctrlPkt.bits.dst_context    := credit_stage2.bits.ingress_meta.src_context
      io.ctrlPkt.bits.msg_len        := credit_stage2.bits.ingress_meta.msg_len
      io.ctrlPkt.bits.pkt_offset     := credit_stage2.bits.ingress_meta.pkt_offset
      io.ctrlPkt.bits.src_context    := credit_stage2.bits.ingress_meta.dst_context
      io.ctrlPkt.bits.tx_msg_id      := credit_stage2.bits.ingress_meta.tx_msg_id
      io.ctrlPkt.bits.buf_ptr        := credit_stage2.bits.pipe_meta.buf_ptr
      io.ctrlPkt.bits.buf_size_class := credit_stage2.bits.pipe_meta.buf_size_class
      io.ctrlPkt.bits.pull_offset    := pull_offset
      io.ctrlPkt.bits.genACK         := credit_stage2.bits.pipe_meta.genACK
      io.ctrlPkt.bits.genNACK        := credit_stage2.bits.pipe_meta.genNACK
      io.ctrlPkt.bits.genPULL        := credit_stage2.bits.pipe_meta.genPULL

    } .otherwise {
      when ((credit_stage2.bits.pipe_meta.flags & ACK_MASK) > 0.U) {
        // fire delivered event
        io.delivered.valid := true.B
        io.delivered.bits.tx_msg_id      := credit_stage2.bits.ingress_meta.tx_msg_id
        io.delivered.bits.pkt_offset     := credit_stage2.bits.ingress_meta.pkt_offset
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

  // logic to drive net_out and meta_out IO
  val pktOutState = RegInit(sWordOne)
  when (credit_stage2.valid && !reset.toBool) {
    assert(pktOutState === sWordOne, "Pkt transfer state machine is not ready after M/A processing!")
    assert(maPktQueue_out.valid, "Pkt payload not available after M/A processing!")
    when (credit_stage2.bits.pipe_meta.drop) {
      // drop the pkt payload
      maPktQueue_out.ready := true.B
      when (!maPktQueue_out.bits.last) {
        pktOutState := sDropPkt
      }
    } .otherwise {
      // transfer first word (and metadata)
      assert(io.net_out.ready, "net_out.ready is false after M/A processing!")
      io.net_out <> maPktQueue_out
      io.meta_out.valid := true.B
      io.meta_out.bits := credit_stage2.bits.ingress_meta
      when (!maPktQueue_out.bits.last) {
        pktOutState := sFinishPkt
      }
    }
  }

  switch (pktOutState) {
    is (sWordOne) {
      // state logic is specified above
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

