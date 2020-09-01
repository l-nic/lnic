package lnic

import Chisel._

import chisel3.util.{EnqIO, DeqIO, HasBlackBoxResource}
import chisel3.experimental._
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket._
import freechips.rocketchip.rocket.LNICRocketConsts._

import scala.collection.mutable.LinkedHashMap

object LNICConsts {
  // width of MAC interface
  val NET_IF_BITS = 64
  val NET_IF_BYTES = NET_IF_BITS/8

  // width of datapath
  val NET_DP_BITS = 512
  val NET_DP_BYTES = NET_DP_BITS/8

  def NET_IF_FULL_KEEP = ~0.U(NET_IF_BYTES.W)
  def NET_DP_FULL_KEEP = ~0.U(NET_DP_BYTES.W)
  def NET_CPU_FULL_KEEP = ~0.U(XBYTES.W)

  val IPV4_TYPE = "h0800".U(16.W)
  val LNIC_PROTO = "h99".U(8.W)
  val DATA_MASK = "b00000001".U(8.W)
  val ACK_MASK  = "b00000010".U(8.W)
  val NACK_MASK = "b00000100".U(8.W)
  val PULL_MASK = "b00001000".U(8.W)
  val CHOP_MASK = "b00010000".U(8.W)

  val IP_HDR_BYTES = 20
  val LNIC_HDR_BYTES = 30
  val LNIC_CTRL_PKT_BYTES = 94

  val ETH_MAC_BITS = 48
  val MSG_ID_BITS = 16
  val BUF_PTR_BITS = 16
  val SIZE_CLASS_BITS = 8
  val PKT_OFFSET_BITS = 8
  val CREDIT_BITS = 16
  val TIMER_BITS = 64

  // TODO(sibanez): maybe these should be parameters as well?
  val MAX_SEG_LEN_BYTES = 1024
  require(MAX_MSG_SIZE_BYTES % MAX_SEG_LEN_BYTES == 0, "MAX_MSG_SIZE_BYTES must be evenly divisible by MAX_SEG_LEN_BYTES!")
  val MAX_SEGS_PER_MSG = MAX_MSG_SIZE_BYTES/MAX_SEG_LEN_BYTES
  // Compute how long to wait b/w sending PULL pkts
  val LINK_RATE_GBPS = 200
  val CYCLE_RATE_GHZ = 3
  val MTU_BYTES = MAX_SEG_LEN_BYTES + LNIC_HDR_BYTES + IP_HDR_BYTES + 14
  val MTU_CYCLES = (CYCLE_RATE_GHZ*(MAX_SEG_LEN_BYTES*8)/LINK_RATE_GBPS).toInt

  // Message buffers for both packetization and reassembly
  // LinkedHashMap[Int, Int] : {buffer_size (bytes) => num_buffers}
  val MSG_BUFFER_COUNT = LinkedHashMap(1024 -> 64,
                                       MAX_MSG_SIZE_BYTES -> 64)
  val NUM_MSG_BUFFER_WORDS = MSG_BUFFER_COUNT.map({ case (size: Int, count: Int) => (size/NET_DP_BYTES)*count }).reduce(_ + _)
  val NUM_MSG_BUFFERS = MSG_BUFFER_COUNT.map({ case (size: Int, count: Int) => count }).reduce(_ + _)
  // TODO(sibanez): how best to size these queues?
  // This queue only builds up if pkts are being scheduled faster than
  // they are being transmitted.
  val SCHEDULED_PKTS_Q_DEPTH = 256
  val PACED_PKTS_Q_DEPTH = 256

  // Default L-NIC parameter values
  // Will now be provided via the LNICBridge
  // val TIMEOUT_CYCLES = 30000 // ~10us @ 3GHz
  // val RTT_PKTS = 5

  // this is used to decide how many bits of the src IP to look at when allocating rx msg IDs
  val MAX_NUM_HOSTS = 128

  // The maximum number of max size msgs that are provisioned to each context in the global RX queues
  val MAX_RX_MAX_MSGS_PER_CONTEXT = 2

  // TODO(sibanez): how best to size these?
  val ARBITER_PKT_BUF_FILTS = MTU_BYTES/NET_DP_BYTES * 2
  val ARBITER_META_BUF_FILTS = MTU_BYTES/NET_DP_BYTES * 2

  val PARSER_PKT_QUEUE_FLITS = MTU_BYTES/NET_DP_BYTES * 2
  // NOTE: should size MA_PKT_QUEUE_FLITS based on depth of M/A pipeline, but this should be enough
  val MA_PKT_QUEUE_FLITS = MTU_BYTES/NET_DP_BYTES * 2
  // NOTE: MA_META_QUEUE is just used to synchronize metadata and payload and can be pretty small
  val MA_META_QUEUE_FLITS = MTU_BYTES/NET_DP_BYTES * 2
  val DEPARSER_META_QUEUE_FLITS = MTU_BYTES/NET_DP_BYTES * 2
  // NOTE: the DEPARSER_PKT_QUEUE can actually fill up and exert backpressure because it is adding headers to the pkts
  val DEPARSER_PKT_QUEUE_FLITS = MTU_BYTES/NET_DP_BYTES * 2

  // Consts for register externs
  val REG_READ  = 0.U(8.W)
  val REG_WRITE = 1.U(8.W)
  val REG_ADD   = 2.U(8.W)

  // NOTE: these are only used for the Simulation Timestamp/Latency measurement module
  val TEST_CONTEXT_ID = 0x1234.U(LNIC_CONTEXT_BITS.W)
}

case class LNICParams(
  max_rx_max_msgs_per_context:  Int = LNICConsts.MAX_RX_MAX_MSGS_PER_CONTEXT,
  max_num_hosts: Int = LNICConsts.MAX_NUM_HOSTS
)

case object LNICKey extends Field[Option[LNICParams]](None)

class NICIO extends StreamIO(LNICConsts.NET_IF_BITS) {
  val nic_mac_addr = Input(UInt(LNICConsts.ETH_MAC_BITS.W))
  val switch_mac_addr = Input(UInt(LNICConsts.ETH_MAC_BITS.W))
  val nic_ip_addr = Input(UInt(32.W))
  val timeout_cycles = Input(UInt(LNICConsts.TIMER_BITS.W))
  val rtt_pkts = Input(UInt(LNICConsts.CREDIT_BITS.W))

  override def cloneType = (new NICIO).asInstanceOf[this.type]
}

class NICIOvonly extends Bundle {
  val in = Flipped(Valid(new StreamChannel(LNICConsts.NET_IF_BITS)))
  val out = Valid(new StreamChannel(LNICConsts.NET_IF_BITS))
  val nic_mac_addr = Input(UInt(LNICConsts.ETH_MAC_BITS.W))
  val switch_mac_addr = Input(UInt(LNICConsts.ETH_MAC_BITS.W))
  val nic_ip_addr = Input(UInt(32.W))
  val timeout_cycles = Input(UInt(LNICConsts.TIMER_BITS.W))
  val rtt_pkts = Input(UInt(LNICConsts.CREDIT_BITS.W))

  override def cloneType = (new NICIOvonly).asInstanceOf[this.type]
}

object NICIOvonly {
  def apply(nicio: NICIO): NICIOvonly = {
    val vonly = Wire(new NICIOvonly)
    vonly.out.valid := nicio.out.valid
    vonly.out.bits  := nicio.out.bits
    nicio.out.ready := true.B
    nicio.in.valid  := vonly.in.valid
    nicio.in.bits   := vonly.in.bits
    assert(!vonly.in.valid || nicio.in.ready, "NIC input not ready for valid")
    nicio.nic_mac_addr := vonly.nic_mac_addr
    nicio.switch_mac_addr := vonly.switch_mac_addr
    nicio.nic_ip_addr := vonly.nic_ip_addr
    nicio.timeout_cycles := vonly.timeout_cycles
    nicio.rtt_pkts := vonly.rtt_pkts
    vonly
  }
}

object NICIO {
  def apply(vonly: NICIOvonly): NICIO = {
    val nicio = Wire(new NICIO)
    assert(!vonly.out.valid || nicio.out.ready)
    nicio.out.valid := vonly.out.valid
    nicio.out.bits  := vonly.out.bits
    vonly.in.valid  := nicio.in.valid
    vonly.in.bits   := nicio.in.bits
    nicio.in.ready  := true.B
    vonly.nic_mac_addr   := nicio.nic_mac_addr
    vonly.switch_mac_addr := nicio.switch_mac_addr
    vonly.nic_ip_addr := nicio.nic_ip_addr
    vonly.timeout_cycles := nicio.timeout_cycles
    vonly.rtt_pkts := nicio.rtt_pkts
    nicio
  }

}

/**
 * This is intended to be L-NIC's IO to the core.
 */
class LNICCoreIO extends Bundle {
  // Msg words from TxQueue
  val net_in = Flipped(Decoupled(new LNICTxMsgWord))
  // Msgs going to RxQueues
  val net_out = Decoupled(new StreamChannel(XLEN))
  val meta_out = Valid(new LNICRxMsgMeta)
  // Core out-of-band coordination with NIC for load balancing
  val add_context = Flipped(Valid(UInt(LNIC_CONTEXT_BITS.W)))
  val get_next_msg = Flipped(Valid(UInt(LNIC_CONTEXT_BITS.W)))
  val reset_done = Output(Bool())
}

/**
 * All IO for the LNIC module.
 */
class LNICIO(implicit p: Parameters) extends Bundle {
  val num_tiles = p(RocketTilesKey).size

  val core = Vec(num_tiles, new LNICCoreIO)
  val net = new NICIO()
}

/**
 * Diplomatic LNIC module.
 */
class LNIC(implicit p: Parameters) extends LazyModule {
  lazy val module = new LNICModuleImp(this)
}

/**
 * LNIC module implementation.
 */
class LNICModuleImp(outer: LNIC)(implicit p: Parameters) extends LazyModuleImp(outer) {
  val io = IO(new LNICIO)

  val num_cores = p(RocketTilesKey).size

  // NIC datapath
  val pisa_ingress = Module(new Ingress)
  val pisa_egress = Module(new Egress)
  val assemble = Module(new LNICAssemble)
  val packetize = Module(new LNICPacketize)
  val rx_queues = Module(new GlobalRxQueues)

  val msg_timers = Module(new LNICTimers)
  val credit_reg = Module(new IfElseRaw)
  val pkt_gen = Module(new LNICPktGen)
  val arbiter = Module(new LNICArbiter)

  val lnic_reset_done = Wire(Bool())
  val lnic_reset_done_reg = RegNext(lnic_reset_done)
  lnic_reset_done := assemble.io.reset_done

  when (lnic_reset_done && !lnic_reset_done_reg) {
    printf("L-NIC reset done!\n")
    printf("RTT packets is %d\n", io.net.rtt_pkts)
    printf("Timeout cycles is %d\n", io.net.timeout_cycles)
  }

  ////////////////////////////////
  /* Event / Extern Connections */
  ////////////////////////////////
  credit_reg.io <> pisa_ingress.io.creditReg
  assemble.io.get_rx_msg_info <> pisa_ingress.io.get_rx_msg_info
  packetize.io.delivered := pisa_ingress.io.delivered
  packetize.io.creditToBtx := pisa_ingress.io.creditToBtx
  pkt_gen.io.ctrlPkt := pisa_ingress.io.ctrlPkt
  pisa_ingress.io.rtt_pkts := io.net.rtt_pkts
  msg_timers.io.schedule := packetize.io.schedule
  msg_timers.io.reschedule := packetize.io.reschedule
  msg_timers.io.cancel := packetize.io.cancel
  packetize.io.timeout := msg_timers.io.timeout
  packetize.io.timeout_cycles := io.net.timeout_cycles
  packetize.io.rtt_pkts := io.net.rtt_pkts

  //////////////////////////
  /* Datapath Connections */
  //////////////////////////
  // Queue to accomodate a little backpressure from StreamWidener
  val net_in_queue_deq = Wire(Decoupled(new StreamChannel(LNICConsts.NET_IF_BITS)))
  net_in_queue_deq <> Queue(io.net.in, LNICConsts.MTU_BYTES/LNICConsts.NET_IF_BYTES * 2)

  // 64-bit => 512-bit
  StreamWidthAdapter(pisa_ingress.io.net_in,
                     net_in_queue_deq)

  assemble.io.net_in <> pisa_ingress.io.net_out
  assemble.io.meta_in := pisa_ingress.io.meta_out

  rx_queues.io.net_in <> assemble.io.net_out
  rx_queues.io.meta_in := assemble.io.meta_out 

  for (i <- 0 until num_cores) {
    io.core(i).reset_done := lnic_reset_done

    io.core(i).net_out <> rx_queues.io.net_out(i)
    io.core(i).meta_out := rx_queues.io.meta_out(i)
    rx_queues.io.add_context(i) := io.core(i).add_context
    rx_queues.io.get_next_msg(i) := io.core(i).get_next_msg

    packetize.io.net_in(i) <> io.core(i).net_in
  }

  arbiter.io.data_in <> packetize.io.net_out
  arbiter.io.data_meta_in := packetize.io.meta_out

  arbiter.io.ctrl_in <> pkt_gen.io.net_out
  arbiter.io.ctrl_meta_in := pkt_gen.io.meta_out

  pisa_egress.io.net_in <> arbiter.io.net_out
  pisa_egress.io.meta_in := arbiter.io.meta_out
  pisa_egress.io.nic_mac_addr := io.net.nic_mac_addr
  pisa_egress.io.switch_mac_addr := io.net.switch_mac_addr
  pisa_egress.io.nic_ip_addr := io.net.nic_ip_addr

  // 512-bit => 64-bit
  StreamWidthAdapter(io.net.out,
                     pisa_egress.io.net_out)

}

