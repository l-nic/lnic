package lnic

import Chisel._

import chisel3.experimental._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.rocket.{StreamChannel, StreamIO}
import LNICConsts._

/* Homa PktGen:
 * Receive ackPkt and grantPkt events and generate corresponding control pkts.
 */
class HomaPktGenIO extends Bundle {
  val ackPkt = Flipped(Valid(new EgressMetaIn))
  val nackPkt = Flipped(Valid(new EgressMetaIn))
  val grantPkt = Flipped(Valid(new EgressMetaIn)) 
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val meta_out = Valid(new EgressMetaIn)
}

@chiselName
class HomaPktGen(implicit p: Parameters) extends Module {
  val io = IO(new HomaPktGenIO)

  // Queue to store metadata for ACK pkts
  val ack_meta_enq = Wire(Decoupled(new EgressMetaIn))
  val ack_meta_deq = Wire(Flipped(Decoupled(new EgressMetaIn)))
  ack_meta_deq <> Queue(ack_meta_enq, SCHEDULED_PKTS_Q_DEPTH)
  // defaults
  ack_meta_deq.ready := false.B
  ack_meta_enq.valid := false.B
  ack_meta_enq.bits := io.ackPkt.bits

  // Process ackPkt events
  when (io.ackPkt.valid) {
    ack_meta_enq.valid := true.B
    assert(ack_meta_enq.ready, "ack_meta queue is full during enqueue!")
  }

  // Queue to store metadata for NACK pkts
  val nack_meta_enq = Wire(Decoupled(new EgressMetaIn))
  val nack_meta_deq = Wire(Flipped(Decoupled(new EgressMetaIn)))
  nack_meta_deq <> Queue(nack_meta_enq, SCHEDULED_PKTS_Q_DEPTH)
  // defaults
  nack_meta_deq.ready := false.B
  nack_meta_enq.valid := false.B
  nack_meta_enq.bits := io.nackPkt.bits

  // Process ackPkt events
  when (io.nackPkt.valid) {
    nack_meta_enq.valid := true.B
    assert(nack_meta_enq.ready, "nack_meta queue is full during enqueue!")
  }

  // Queue to store metadata for GRANT pkts 
  val grant_meta_enq = Wire(Decoupled(new EgressMetaIn))
  val grant_meta_deq = Wire(Flipped(Decoupled(new EgressMetaIn)))
  grant_meta_deq <> Queue(grant_meta_enq, SCHEDULED_PKTS_Q_DEPTH)
  // defaults
  grant_meta_deq.ready := false.B
  grant_meta_enq.valid := false.B
  grant_meta_enq.bits := io.grantPkt.bits

  // Process grantPkt events
  when (io.grantPkt.valid) {
    grant_meta_enq.valid := true.B
    assert(grant_meta_enq.ready, "grant_meta queue is full during enqueue!")
  }

  // drive outputs
  io.net_out.bits.data := 0.U
  io.net_out.bits.keep := 1.U // one dummy byte
  io.net_out.bits.last := true.B

  val valid_data = Wire(Bool())
  valid_data := ack_meta_deq.valid || nack_meta_deq.valid || grant_meta_deq.valid

  io.net_out.valid  := valid_data
  io.meta_out.valid := valid_data

  // We'll strictly prioritize transmission of ACKs then NACKs then GRANTs
  when (ack_meta_deq.valid) {
    io.meta_out.bits := ack_meta_deq.bits
    ack_meta_deq.ready := io.net_out.ready
  } .elsewhen (nack_meta_deq.valid) {
    io.meta_out.bits := nack_meta_deq.bits
    nack_meta_deq.ready := io.net_out.ready
  } .otherwise {
    io.meta_out.bits := grant_meta_deq.bits
    grant_meta_deq.ready := io.net_out.ready
  }

}

