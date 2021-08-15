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
 * All IO for the LNIC PISA Egress module.
 */
class NDPEgressIO extends Bundle {
  val net_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val meta_in = Flipped(Valid(new EgressMetaIn))
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val nic_mac_addr = Input(UInt(ETH_MAC_BITS.W))
  val switch_mac_addr = Input(UInt(ETH_MAC_BITS.W))
  val nic_ip_addr = Input(UInt(32.W))
  val rtt_pkts = Input(UInt(CREDIT_BITS.W))

  override def cloneType = new NDPEgressIO().asInstanceOf[this.type]
}

@chiselName
class NDPEgress(implicit p: Parameters) extends Module {
  val io = IO(new NDPEgressIO)

  // queues to store pkts and metadata that are arriving
  val metaQueue_in = Wire(Decoupled(new EgressMetaIn))
  val metaQueue_out = Wire(Flipped(Decoupled(new EgressMetaIn)))
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
        val is_data_pkt = ((metaQueue_out.bits.flags & DATA_MASK) > 0.U)

        // fill out headers
        val headers = Wire(new NDPHeaders)
        // Ethernet Header
        headers.eth_dst  := io.switch_mac_addr
        headers.eth_src  := io.nic_mac_addr
        headers.eth_type := IPV4_TYPE

        // IP Header
        headers.ip_version := 4.U
        headers.ip_ihl     := 5.U
        headers.ip_tos     := 0.U
        val ip_len = Wire(UInt(16.W))
        when (!is_data_pkt) {
          // this is a control pkt
          ip_len   := IP_HDR_BYTES.U(16.W) + LNIC_CTRL_PKT_BYTES.U(16.W)
        } .otherwise {
          // this is a data pkt
          val msg_len = metaQueue_out.bits.msg_len
          val num_pkts = MsgBufHelpers.compute_num_pkts(msg_len)
          val is_last_pkt = (metaQueue_out.bits.pkt_offset === (num_pkts - 1.U))
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
        headers.ip_proto   := NDP_PROTO
        headers.ip_chksum  := 0.U // TODO(sibanez): implement this ..
        headers.ip_src     := io.nic_ip_addr
        headers.ip_dst     := metaQueue_out.bits.dst_ip

        // NDP Header
        headers.ndp_flags          := metaQueue_out.bits.flags
        headers.ndp_src            := metaQueue_out.bits.src_context
        headers.ndp_dst            := metaQueue_out.bits.dst_context
        headers.ndp_msg_len        := metaQueue_out.bits.msg_len
        headers.ndp_pkt_offset     := metaQueue_out.bits.pkt_offset
        headers.ndp_pull_offset    := metaQueue_out.bits.credit
        headers.ndp_tx_msg_id      := metaQueue_out.bits.tx_msg_id
        headers.ndp_buf_ptr        := metaQueue_out.bits.buf_ptr
        headers.ndp_buf_size_class := metaQueue_out.bits.buf_size_class
        headers.ndp_padding        := 0.U

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

