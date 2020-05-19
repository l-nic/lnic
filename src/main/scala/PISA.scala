
package freechips.rocketchip.tile

import Chisel._

import chisel3.experimental._
import chisel3.SyncReadMem
import chisel3.util.{HasBlackBoxResource}
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket._
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util._
import NetworkHelpers._
import LNICConsts._

class PISAIngressMetaOut extends Bundle {
  // metadata for pkts going to CPU
  val src_ip       = UInt(32.W)
  val src_context  = UInt(LNIC_CONTEXT_BITS.W)
  val msg_len      = UInt(MSG_LEN_BITS.W)
  val pkt_offset   = UInt(PKT_OFFSET_BITS.W)
  val dst_context  = UInt(LNIC_CONTEXT_BITS.W)
  val rx_msg_id    = UInt(LNIC_MSG_ID_BITS.W)
  val tx_msg_id    = UInt(LNIC_MSG_ID_BITS.W)
}

class PISAEgressMetaIn extends Bundle {
  // metadata for pkts coming from CPU
  val dst_ip         = UInt(32.W)
  val dst_context    = UInt(LNIC_CONTEXT_BITS.W)
  val msg_len        = UInt(MSG_LEN_BITS.W)
  val pkt_offset     = UInt(PKT_OFFSET_BITS.W)
  val src_context    = UInt(LNIC_CONTEXT_BITS.W)
  val tx_msg_id      = UInt(LNIC_MSG_ID_BITS.W)
  val buf_ptr        = UInt(BUF_PTR_BITS.W)
  val buf_size_class = UInt(SIZE_CLASS_BITS.W)
  val pull_offset    = UInt(CREDIT_BITS.W)
  val genACK = Bool()
  val genNACK = Bool()
  val genPULL = Bool()
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

/**
 * All IO for the LNIC PISA Egress module.
 */
class LNICPISAEgressIO extends Bundle {
  val net_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val meta_in = Flipped(Valid(new PISAEgressMetaIn))
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))

  override def cloneType = new LNICPISAEgressIO().asInstanceOf[this.type]
}

/* IO for Ingress extern call */
class GetRxMsgInfoIO extends Bundle {
  val req = Valid(new GetRxMsgInfoReq)
  val resp = Flipped(Valid(new GetRxMsgInfoResp))
}

class GetRxMsgInfoReq extends Bundle {
  val src_ip = UInt(32.W)
  val src_context = UInt(LNIC_CONTEXT_BITS.W)
  val tx_msg_id = UInt(LNIC_MSG_ID_BITS.W)
  val msg_len = UInt(MSG_LEN_BITS.W)
}

class GetRxMsgInfoResp extends Bundle {
  val fail = Bool()
  val rx_msg_id = UInt(LNIC_MSG_ID_BITS.W)
  // TODO(sibanez): add additional fields for transport processing
  val is_new_msg = Bool()
}

class DeliveredEvent extends Bundle {
  val tx_msg_id = UInt(LNIC_MSG_ID_BITS.W)
  val pkt_offset = UInt(PKT_OFFSET_BITS.W)
  val msg_len = UInt(MSG_LEN_BITS.W)
  val buf_ptr = UInt(BUF_PTR_BITS.W)
  val buf_size_class = UInt(SIZE_CLASS_BITS.W)
}

class CreditToBtxEvent extends Bundle {
  val tx_msg_id = UInt(LNIC_MSG_ID_BITS.W)
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

/* Ingress Pipeline Blackbox */
class SDNetIngressWrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val net = new LNICPISAIngressIO
  })

  addResource("/vsrc/SDNetIngressWrapper.sv")
}

/* Egress Pipeline Blackbox */
class SDNetEgressWrapper extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val net = new LNICPISAEgressIO
  })

  addResource("/vsrc/SDNetEgressWrapper.sv")
}

/* IfElseRawReg Extern */
class IfElseRawIO extends Bundle {
  val req = Valid(new IfElseRawReq)
  val resp = Flipped(Valid(new IfElseRawResp))
}

class IfElseRawReq extends Bundle {
  val index = UInt(LNIC_MSG_ID_BITS.W)
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

  val ram_ptr = Wire(UInt(LNIC_MSG_ID_BITS.W))
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


