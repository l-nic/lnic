package lnic

import Chisel._

import chisel3.{VecInit, SyncReadMem}
import chisel3.experimental._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.rocket._
import freechips.rocketchip.rocket.NetworkHelpers._
import freechips.rocketchip.rocket.LNICRocketConsts._
import LNICConsts._

class PISAEgressMetaIn extends Bundle {
  // metadata for pkts coming from CPU
  val dst_ip         = UInt(32.W)
  val dst_context    = UInt(LNIC_CONTEXT_BITS.W)
  val msg_len        = UInt(MSG_LEN_BITS.W)
  val pkt_offset     = UInt(PKT_OFFSET_BITS.W)
  val src_context    = UInt(LNIC_CONTEXT_BITS.W)
  val tx_msg_id      = UInt(MSG_ID_BITS.W)
  val buf_ptr        = UInt(BUF_PTR_BITS.W)
  val buf_size_class = UInt(SIZE_CLASS_BITS.W)
  val pull_offset    = UInt(CREDIT_BITS.W)
  val genACK         = Bool()
  val genNACK        = Bool()
  val genPULL        = Bool()
}

/**
 * All IO for the LNIC PISA Egress module.
 */
class LNICPISAEgressIO extends Bundle {
  val net_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val meta_in = Flipped(Valid(new PISAEgressMetaIn))
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val nic_mac_addr = Flipped(UInt(ETH_MAC_BITS.W))
  val switch_mac_addr = Flipped(UInt(ETH_MAC_BITS.W))
  val nic_ip_addr = Flipped(UInt(32.W))

  override def cloneType = new LNICPISAEgressIO().asInstanceOf[this.type]
}

@chiselName
class Egress(implicit p: Parameters) extends Module {
  val io = IO(new LNICPISAEgressIO)

  // queues to store pkts and metadata that are arriving
  val metaQueue_in = Wire(Decoupled(new PISAEgressMetaIn))
  val metaQueue_out = Wire(Flipped(Decoupled(new PISAEgressMetaIn)))
  val pktQueue_out = Wire(Flipped(Decoupled(new StreamChannel(NET_DP_BITS))))
  metaQueue_in.valid := false.B // default
  metaQueue_in.bits := io.meta_in.bits
  metaQueue_out.ready := false.B // default
  pktQueue_out.ready := false.B // default

  metaQueue_out <> Queue(metaQueue_in, DEPARSER_META_QUEUE_FLITS)
  pktQueue_out <> Queue(io.net_in, DEPARSER_PKT_QUEUE_FLITS) 

  // state machine to enqueue into metaQueue
  val sWordOne :: sFinishPkt :: Nil = Enum(2)
  val enqState = RegInit(sWordOne)

  switch(enqState) {
    is (sWordOne) {
      when (io.net_in.valid && io.net_in.ready) {
        // write the metadata to the queue
        metaQueue_in.valid := true.B
        assert(metaQueue_in.ready, "metaQueue is full in egress deparser!")
        assert(io.meta_in.valid, "metadata is invalid on first word of pkt in egress deparser!")
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

  // state machine to drive io.net_out
  val deqState = RegInit(sWordOne)

  io.net_out.valid := false.B // default
  io.net_out.bits := pktQueue_out.bits

  switch(deqState) {
    is (sWordOne) {
      when (pktQueue_out.valid && metaQueue_out.valid) {
        val is_ctrl_pkt = metaQueue_out.bits.genACK || metaQueue_out.bits.genNACK || metaQueue_out.bits.genPULL

        // fill out headers
        val headers = Wire(new Headers)
        // Ethernet Header
        headers.eth_dst  := io.switch_mac_addr
        headers.eth_src  := io.nic_mac_addr
        headers.eth_type := IPV4_TYPE

        // IP Header
        headers.ip_version := 4.U
        headers.ip_ihl     := 5.U
        headers.ip_tos     := 0.U
        when (is_ctrl_pkt) {
          headers.ip_len   := IP_HDR_BYTES + LNIC_CTRL_PKT_BYTES
        } .otherwise {
          // NOTE: this len is not correct when the msg spans multiple pkts
          headers.ip_len   := metaQueue_out.bits.msg_len + IP_HDR_BYTES + LNIC_HDR_BYTES
        }
        headers.ip_id      := 1.U
        headers.ip_flags   := 0.U
        headers.ip_offset  := 0.U
        headers.ip_ttl     := 64.U
        headers.ip_proto   := LNIC_PROTO
        headers.ip_chksum  := 0.U // TODO(sibanez): implement this ..
        headers.ip_src     := io.nic_ip_addr
        headers.ip_dst     := metaQueue_out.bits.dst_ip

        // LNIC Header
        val ack_flag = Mux(metaQueue_out.bits.genACK, ACK_MASK, 0.U)
        val nack_flag = Mux(metaQueue_out.bits.genNACK, NACK_MASK, 0.U)
        val pull_flag = Mux(metaQueue_out.bits.genPULL, PULL_MASK, 0.U)
        val data_flag = Mux(!is_ctrl_pkt, DATA_MASK, 0.U)

        headers.lnic_flags          := ack_flag | nack_flag | pull_flag | data_flag
        headers.lnic_src            := metaQueue_out.bits.src_context
        headers.lnic_dst            := metaQueue_out.bits.dst_context
        headers.lnic_msg_len        := metaQueue_out.bits.msg_len
        headers.lnic_pkt_offset     := metaQueue_out.bits.pkt_offset
        headers.lnic_pull_offset    := metaQueue_out.bits.pull_offset
        headers.lnic_tx_msg_id      := metaQueue_out.bits.tx_msg_id
        headers.lnic_buf_ptr        := metaQueue_out.bits.buf_ptr
        headers.lnic_buf_size_class := metaQueue_out.bits.buf_size_class
        headers.lnic_padding        := 0.U

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

