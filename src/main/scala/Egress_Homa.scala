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
 * All IO for the Homa Egress module.
 */
class HomaEgressIO extends Bundle {
  val net_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val meta_in = Flipped(Valid(new EgressMetaIn))
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val txMsgPrioReg_req = Valid(new TxMsgPrioEgressReq)
  val txMsgPrioReg_resp = Flipped(Valid(new TxMsgPrioEgressResp))
  val nic_mac_addr = Input(UInt(ETH_MAC_BITS.W))
  val switch_mac_addr = Input(UInt(ETH_MAC_BITS.W))
  val nic_ip_addr = Input(UInt(32.W))
  val rtt_pkts = Input(UInt(CREDIT_BITS.W))

  override def cloneType = new HomaEgressIO().asInstanceOf[this.type]
}

class HomaEgressMeta extends Bundle {
  val meta_in = new EgressMetaIn
  val is_data = Bool()
  val data_prio = UInt(HOMA_PRIO_BITS.W)
}

@chiselName
class HomaEgress(implicit p: Parameters) extends Module {
  val io = IO(new HomaEgressIO)

  // queues to store pkts and metadata that are arriving
  val metaQueue_in = Wire(Decoupled(new HomaEgressMeta))
  val metaQueue_out = Wire(Flipped(Decoupled(new HomaEgressMeta)))
  val pktQueue_out = Wire(Flipped(Decoupled(new StreamChannel(NET_DP_BITS))))
  metaQueue_in.valid := false.B // default
  metaQueue_out.ready := false.B // default
  pktQueue_out.ready := false.B // default

  metaQueue_out <> Queue(metaQueue_in, DEPARSER_META_QUEUE_FLITS)
  pktQueue_out <> Queue(io.net_in, DEPARSER_PKT_QUEUE_FLITS) 

  // defaults
  io.txMsgPrioReg_req.valid := false.B

  val pkt_start = Wire(Bool())
  pkt_start := false.B

  // state machine to enqueue into metaQueue
  val sWordOne :: sFinishPkt :: Nil = Enum(2)
  val enqState = RegInit(sWordOne)

  switch(enqState) {
    is (sWordOne) {
      when (io.net_in.valid && io.net_in.ready) {
        // write the metadata to the queue
        pkt_start := true.B
        when (!io.net_in.bits.last) {
          enqState := sFinishPkt
        }
      }
    }
    is (sFinishPkt) {
      when (io.net_in.valid && io.net_in.ready && io.net_in.bits.last) {
        enqState := sWordOne
      }
    }
  }


  // A little pipeline to compute the priority of outgoing DATA pkts
  val pipe_stage1 = Reg(Valid(new HomaEgressMeta))
  val pipe_stage2 = Reg(Valid(new HomaEgressMeta))

  pipe_stage1.valid := pkt_start
  pipe_stage1.bits.meta_in := io.meta_in.bits

  when (pkt_start && !reset.toBool) {
    assert(io.meta_in.valid, "metadata is invalid on first word of pkt in egress pipeline!")
    val is_data = Wire(Bool())
    is_data := (io.meta_in.bits.flags & DATA_MASK) > 0.U
    pipe_stage1.bits.is_data := is_data
    when (is_data) {
      // trigger request to txMsgPrioReg
      io.txMsgPrioReg_req.valid := true.B
      io.txMsgPrioReg_req.bits.index := io.meta_in.bits.tx_msg_id
      io.txMsgPrioReg_req.bits.update := io.meta_in.bits.is_new_msg
      val msg_len_pkts = Wire(UInt())
      msg_len_pkts := MsgBufHelpers.compute_num_pkts(io.meta_in.bits.msg_len)
      val new_prio = Wire(UInt(HOMA_PRIO_BITS.W))
      new_prio := Mux(msg_len_pkts <= io.rtt_pkts, 0.U, (HOMA_NUM_UNSCHEDULED_PRIOS-1).U)
      io.txMsgPrioReg_req.bits.prio := new_prio // only used if is_new_msg
    }
  }

  pipe_stage2 := pipe_stage1

  // this is when txMsgPrioReg should return its response
  when (pipe_stage2.valid && !reset.toBool) {
    // write final metadata to the metadata queue
    metaQueue_in.valid := true.B
    metaQueue_in.bits := pipe_stage2.bits // default
    assert(metaQueue_in.ready, "metaQueue is full in egress pipeline!")
    when (pipe_stage2.bits.is_data) {
      assert(io.txMsgPrioReg_resp.valid, "txMsgPrioReg did not return response in egress pipeline!")
      metaQueue_in.bits.data_prio := Mux(pipe_stage2.bits.meta_in.is_rtx && (io.txMsgPrioReg_resp.bits.prio < HOMA_NUM_UNSCHEDULED_PRIOS.U),
          HOMA_NUM_UNSCHEDULED_PRIOS.U, // don't send retransmissions at an unscheduled prio level
          io.txMsgPrioReg_resp.bits.prio)
    }
  }


  // state machine to drive io.net_out
  val deqState = RegInit(sWordOne)

  io.net_out.valid := false.B // default
  io.net_out.bits := pktQueue_out.bits

  switch(deqState) {
    is (sWordOne) {
      when (pktQueue_out.valid && metaQueue_out.valid) {
        // fill out headers
        val headers = Wire(new HomaHeaders)
        // Ethernet Header
        headers.eth_dst  := io.switch_mac_addr
        headers.eth_src  := io.nic_mac_addr
        headers.eth_type := IPV4_TYPE

        // IP Header
        headers.ip_version := 4.U
        headers.ip_ihl     := 5.U
        // tos field carries pkt priority, ctrl pkts are forwarded with priority=0
        headers.ip_tos     := Mux(metaQueue_out.bits.is_data, metaQueue_out.bits.data_prio, 0.U)
        val ip_len = Wire(UInt(16.W))
        when (!metaQueue_out.bits.is_data) {
          ip_len   := IP_HDR_BYTES.U + LNIC_CTRL_PKT_BYTES.U
        } .otherwise {
          val msg_len = metaQueue_out.bits.meta_in.msg_len
          val num_pkts = MsgBufHelpers.compute_num_pkts(msg_len)
          val is_last_pkt = (metaQueue_out.bits.meta_in.pkt_offset === (num_pkts - 1.U))
          require(isPow2(MAX_SEG_LEN_BYTES), "MAX_SEG_LEN_BYTES must be a power of 2!")
          val last_bytes = Wire(UInt(16.W))
          // check if msg_len is divisible by MAX_SEG_LEN_BYTES
          val msg_len_mod_mtu = msg_len(log2Up(MAX_SEG_LEN_BYTES)-1, 0)
          last_bytes := Mux(msg_len_mod_mtu === 0.U,
                            MAX_SEG_LEN_BYTES.U(16.W),
                            msg_len_mod_mtu)
          when (is_last_pkt) {
            // need to pad last_bytes to 16 bits wide
            ip_len := last_bytes + IP_HDR_BYTES.U + LNIC_HDR_BYTES.U
          } .otherwise {
            ip_len := MAX_SEG_LEN_BYTES.U + IP_HDR_BYTES.U + LNIC_HDR_BYTES.U
          }
        }
        headers.ip_len     := ip_len
        headers.ip_id      := 1.U
        headers.ip_flags   := 0.U
        headers.ip_offset  := 0.U
        headers.ip_ttl     := 64.U
        headers.ip_proto   := HOMA_PROTO
        headers.ip_chksum  := 0.U // TODO(sibanez): implement this ..
        headers.ip_src     := io.nic_ip_addr
        headers.ip_dst     := metaQueue_out.bits.meta_in.dst_ip

        // Homa Header
        headers.homa_flags          := metaQueue_out.bits.meta_in.flags
        headers.homa_src            := metaQueue_out.bits.meta_in.src_context
        headers.homa_dst            := metaQueue_out.bits.meta_in.dst_context
        headers.homa_msg_len        := metaQueue_out.bits.meta_in.msg_len
        headers.homa_pkt_offset     := metaQueue_out.bits.meta_in.pkt_offset
        headers.homa_grant_offset   := metaQueue_out.bits.meta_in.credit
        headers.homa_grant_prio     := metaQueue_out.bits.meta_in.rank
        headers.homa_tx_msg_id      := metaQueue_out.bits.meta_in.tx_msg_id
        headers.homa_buf_ptr        := metaQueue_out.bits.meta_in.buf_ptr
        headers.homa_buf_size_class := metaQueue_out.bits.meta_in.buf_size_class
        headers.homa_padding        := 0.U

        io.net_out.valid := true.B
        io.net_out.bits.data := reverse_bytes(headers.asUInt, NET_DP_BYTES)
        io.net_out.bits.keep := NET_DP_FULL_KEEP
        io.net_out.bits.last := false.B
        when (io.net_out.ready) {
          metaQueue_out.ready := true.B // read from metaQueue
          deqState := sFinishPkt
        }
      }
    }
    is (sFinishPkt) {
      io.net_out <> pktQueue_out
      when (io.net_out.valid && io.net_out.ready && io.net_out.bits.last) {
        deqState := sWordOne
      }
    }
  }

}


/* TxMsgPrioReg Extern */
class TxMsgPrioRegIO extends Bundle {
  val ingress_req = Valid(new TxMsgPrioIngressReq)
  val egress_req = Valid(new TxMsgPrioEgressReq)
  val egress_resp = Flipped(Valid(new TxMsgPrioEgressResp))
}

class TxMsgPrioIngressReq extends Bundle {
  val index = UInt(MSG_ID_BITS.W)
  val prio = UInt(HOMA_PRIO_BITS.W)
}

class TxMsgPrioEgressReq extends Bundle {
  val index = UInt(MSG_ID_BITS.W)
  val update = Bool()
  val prio = UInt(HOMA_PRIO_BITS.W)
}

class TxMsgPrioEgressResp extends Bundle {
  val prio = UInt(HOMA_PRIO_BITS.W)
}

@chiselName
class TxMsgPrioReg(implicit p: Parameters) extends Module {
  val io = IO(Flipped(new TxMsgPrioRegIO))

  // ram is index by tx_msg_id and just holds the msg's current priority
  val ram = Module(new TrueDualPortRAM(HOMA_PRIO_BITS, NUM_MSG_BUFFERS))
  ram.io.clock := clock
  ram.io.reset := reset

  /* state machine to process Egress Requests
   *   - Used by egress pipeline to either initialize msg priority state
   *     or just read the current msg priority.
   */

  val sRead :: sWrite :: Nil = Enum(2)
  val egress_fsm = RegInit(sRead)

  // pipeline reg
  val egress_req_reg_0 = RegNext(io.egress_req)
  val egress_req_reg_1 = RegNext(egress_req_reg_0)

  // Need 2 cycles for RMW operation
  assert(!(egress_req_reg_0.valid && egress_req_reg_1.valid), "Violated assumption that requests will not arrive on back-to-back cycles!")

  // defaults
  io.egress_resp.valid := false.B

  ram.io.portA.we := false.B // default
  ram.io.portA.addr := egress_req_reg_0.bits.index

  switch(egress_fsm) {
    is (sRead) {
      when (egress_req_reg_0.valid) {
        when (egress_req_reg_0.bits.update) {
          // initialize msg priority
          ram.io.portA.din := egress_req_reg_0.bits.prio
          ram.io.portA.we  := true.B
        }
        egress_fsm := sWrite
      }
    }
    is (sWrite) {
      // write response
      io.egress_resp.valid := !reset.toBool
      io.egress_resp.bits.prio := Mux(egress_req_reg_1.bits.update,
          egress_req_reg_1.bits.prio, // we just initialized the msg priority
          ram.io.portA.dout) // get the current msg priority from the ram

      // state transition
      egress_fsm := sRead
    }
  }

  /* Logic to process Ingress Requests 
   *   - Used by ingress pipeline to update msg priority state.
   *     Invoked when a GRANT pkt arrives.
   */

  ram.io.portB.we   := io.ingress_req.valid && !reset.toBool
  ram.io.portB.addr := io.ingress_req.bits.index
  ram.io.portB.din  := io.ingress_req.bits.prio

}

