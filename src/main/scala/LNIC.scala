package lnic

import Chisel._

import chisel3.util.{EnqIO, DeqIO, HasBlackBoxResource}
import chisel3.experimental._
import freechips.rocketchip.config._
import freechips.rocketchip.subsystem._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket._
import freechips.rocketchip.rocket.LNICRocketConsts._
import icenet.{NICIO, NICIOvonly}

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

  val SWITCH_MAC_ADDR = "h085566778808".U
  val NIC_MAC_ADDR = "h081122334408".U
  val NIC_IP_ADDR = "h0A000001".U // 10.0.0.1

  val IP_TYPE = "h0800".U
  val LNIC_PROTO = "h99".U
  val PARSER_PKT_QUEUE_FLITS = ???
  val MA_PKT_QUEUE_FLITS = ???
  val DATA_MASK = "b00000001".U(8.W)
  val ACK_MASK  = "b00000010".U(8.W)
  val NACK_MASK = "b00000100".U(8.W)
  val PULL_MASK = "b00001000".U(8.W)
  val CHOP_MASK = "b00010000".U(8.W)

  val MSG_ID_BITS = 16
  val BUF_PTR_BITS = 16
  val SIZE_CLASS_BITS = 8
  val PKT_OFFSET_BITS = 8
  // TODO(sibanez): what is the expected range of the credit state?
  val CREDIT_BITS = 16
  val TIMER_BITS = 64

  // Default L-NIC parameter values
  val TIMEOUT_CYCLES = 30000 // ~100us @ 300MHz
  val RTT_PKTS = 5

  // TODO(sibanez): maybe these should be parameters as well?
  val MAX_SEGS_PER_MSG = 16
  val MAX_SEG_LEN_BYTES = 512
  // Compute how long to wait b/w sending PULL pkts
  val LINK_RATE_GBPS = 100
  val MTU_CYCLES = ((MAX_SEG_LEN_BYTES*8)/LINK_RATE_GBPS).toInt

  // Message buffers for both packetization and reassembly
  // LinkedHashMap[Int, Int] : {buffer_size (bytes) => num_buffers}
  val MSG_BUFFER_COUNT = LinkedHashMap(64  -> 64,
                                       128 -> 32,
                                       1024 -> 16,
                                       MAX_MSG_SIZE_BYTES -> 8)
  val NUM_MSG_BUFFER_WORDS = MSG_BUFFER_COUNT.map({ case (size: Int, count: Int) => (size/NET_DP_BYTES)*count }).reduce(_ + _)
  val NUM_MSG_BUFFERS = MSG_BUFFER_COUNT.map({ case (size: Int, count: Int) => count }).reduce(_ + _)
  // TODO(sibanez): how best to size these queues?
  // This queue only builds up if pkts are being scheduled faster than
  // they are being transmitted.
  val SCHEDULED_PKTS_Q_DEPTH = 256
  val PACED_PKTS_Q_DEPTH = 256

  // TODO(sibanez): how best to size these?
  val ARBITER_PKT_BUF_FILTS = MAX_SEG_LEN_BYTES/NET_DP_BYTES * 2
  val ARBITER_META_BUF_FILTS = MAX_SEG_LEN_BYTES/NET_DP_BYTES * 2

  // NOTE: these are only used for the Simulation Timestamp/Latency measurement module
  val IP_TYPE = 0x800.U(16.W)
  val LNIC_PROTO = 0x99.U(8.W)
  val TEST_CONTEXT_ID = 0x1234.U(LNIC_CONTEXT_BITS.W)
}

case class LNICParams(
  timeoutCycles:  Int = LNICConsts.TIMEOUT_CYCLES,
  rttPkts:        Int = LNICConsts.RTT_PKTS
)

case object LNICKey extends Field[Option[LNICParams]](None)

/**
 * This is intended to be L-NIC's IO to the core.
 */
class LNICCoreIO extends Bundle {
  // Msg words from TxQueue
  val net_in = Flipped(Decoupled(new LNICTxMsgWord))
  // Msgs going to RxQueues
  val net_out = Decoupled(new StreamChannel(XLEN))
  val meta_out = Valid(new LNICRxMsgMeta)
}

/**
 * All IO for the LNIC module.
 */
class LNICIO(implicit p: Parameters) extends Bundle {
  val core = new LNICCoreIO()
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

  // NIC datapath
  val pisa_ingress = Module(new SDNetIngressWrapper)
  val pisa_egress = Module(new SDNetEgressWrapper)
  val assemble = Module(new LNICAssemble)
  val packetize = Module(new LNICPacketize)

  val msg_timers = Module(new LNICTimers)
  val credit_reg = Module(new IfElseRaw)
  val pkt_gen = Module(new LNICPktGen)
  val arbiter = Module(new LNICArbiter)

  pisa_ingress.io.clock := clock
  pisa_ingress.io.reset := reset
  pisa_egress.io.clock := clock
  pisa_egress.io.reset := reset

  ////////////////////////////////
  /* Event / Extern Connections */
  ////////////////////////////////
  credit_reg.io <> pisa_ingress.io.net.creditReg
  assemble.io.get_rx_msg_info <> pisa_ingress.io.net.get_rx_msg_info
  packetize.io.delivered := pisa_ingress.io.net.delivered
  packetize.io.creditToBtx := pisa_ingress.io.net.creditToBtx
  pkt_gen.io.ctrlPkt := pisa_ingress.io.net.ctrlPkt
  msg_timers.io.schedule := packetize.io.schedule
  msg_timers.io.reschedule := packetize.io.reschedule
  msg_timers.io.cancel := packetize.io.cancel
  packetize.io.timeout := msg_timers.io.timeout

  //////////////////////////
  /* Datapath Connections */
  //////////////////////////
  // Queue to accomodate a little backpressure from StreamWidener
  val net_in_queue_deq = Wire(Decoupled(new StreamChannel(LNICConsts.NET_IF_BITS)))
  net_in_queue_deq <> Queue(io.net.in, LNICConsts.MAX_SEG_LEN_BYTES/LNICConsts.NET_IF_BYTES * 2)

  // 64-bit => 512-bit
  StreamWidthAdapter(pisa_ingress.io.net.net_in,
                     net_in_queue_deq)

  assemble.io.net_in <> pisa_ingress.io.net.net_out
  assemble.io.meta_in := pisa_ingress.io.net.meta_out

  io.core.net_out <> assemble.io.net_out
  io.core.meta_out := assemble.io.meta_out 

  packetize.io.net_in <> io.core.net_in

  arbiter.io.data_in <> packetize.io.net_out
  arbiter.io.data_meta_in := packetize.io.meta_out

  arbiter.io.ctrl_in <> pkt_gen.io.net_out
  arbiter.io.ctrl_meta_in := pkt_gen.io.meta_out

  pisa_egress.io.net.net_in <> arbiter.io.net_out
  pisa_egress.io.net.meta_in := arbiter.io.meta_out

  // 512-bit => 64-bit
  StreamWidthAdapter(io.net.out,
                     pisa_egress.io.net.net_out)

}

