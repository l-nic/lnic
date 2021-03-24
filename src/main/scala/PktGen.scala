package lnic

import Chisel._

import chisel3.experimental._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.rocket.{StreamChannel, StreamIO}
import LNICConsts._

/* LNIC PktGen:
 * Receive CtrlPktEvents and generate control pkts to be processed by the egress pipe
 */
class LNICPktGenIO extends Bundle {
  val ctrlPkt = Flipped(Valid(new PISAEgressMetaIn))
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val meta_out = Valid(new PISAEgressMetaIn)
}

@chiselName
class LNICPktGen(implicit p: Parameters) extends Module {
  val io = IO(new LNICPktGenIO)

  // Queue to store metadata for scheduled pkts
  val scheduled_meta_enq = Wire(Decoupled(new PISAEgressMetaIn))
  val scheduled_meta_deq = Wire(Flipped(Decoupled(new PISAEgressMetaIn)))
  scheduled_meta_deq <> Queue(scheduled_meta_enq, SCHEDULED_PKTS_Q_DEPTH)

  // Queue to store metadata for paced PULL pkts 
  val paced_meta_enq = Wire(Decoupled(new PISAEgressMetaIn))
  val paced_meta_deq = Wire(Flipped(Decoupled(new PISAEgressMetaIn)))
  paced_meta_deq <> Queue(paced_meta_enq, PACED_PKTS_Q_DEPTH)

  // defaults
  io.net_out.valid := scheduled_meta_deq.valid
  io.net_out.bits.data := 0.U
  io.net_out.bits.keep := 1.U // one dummy byte
  io.net_out.bits.last := true.B
  scheduled_meta_deq.ready := io.net_out.ready

  io.meta_out.valid := scheduled_meta_deq.valid
  io.meta_out.bits := scheduled_meta_deq.bits

  scheduled_meta_enq.valid := false.B
  paced_meta_enq.valid := false.B
  paced_meta_deq.ready := false.B

  val sRefill :: sAvailable :: Nil = Enum(2)
  val stateTokens = RegInit(sRefill)

  /* Logic to process ctrlPkt events */

  // pipeline reg
  val ctrlPkt_reg = RegNext(io.ctrlPkt)

  when (ctrlPkt_reg.valid && !reset.toBool) {
    assert(ctrlPkt_reg.bits.genACK || ctrlPkt_reg.bits.genNACK, "Violated assumption that ACK or NACK is always generated!")

    when (ctrlPkt_reg.bits.genPULL) {
      // must check for token availability when generating a PULL
      when (stateTokens === sAvailable) {
        // combine ctrl pkts and schedule immediately
        schedule_pkt(ctrlPkt_reg.bits)
        // refill token
        stateTokens := sRefill
        // TODO(sibanez): this may cause re-ordering of PULL pkts if the paced queue
        //   is not empty when this event fires. Is that a problem?
      } .otherwise {
        // separate out the ACK/NACK from PULL
        val ack_nack_pkt = Wire(new PISAEgressMetaIn)
        ack_nack_pkt := ctrlPkt_reg.bits
        ack_nack_pkt.genPULL := false.B
        val pull_pkt = Wire(new PISAEgressMetaIn)
        pull_pkt := ctrlPkt_reg.bits
        pull_pkt.genACK := false.B
        pull_pkt.genNACK := false.B
        // schedule the ACK/NACK immediately and pace the PULL
        schedule_pkt(ack_nack_pkt)
        pace_pkt(pull_pkt)
      }
    } .otherwise {
      // schedule the ACK/NACK immediately
      schedule_pkt(ctrlPkt_reg.bits)
    }

  } .otherwise {
    /* Pacing logic */

    // logic to move from paced queue to scheduled queue
    when (stateTokens === sAvailable && paced_meta_deq.valid && scheduled_meta_enq.ready) {
      paced_meta_deq.ready := true.B
      schedule_pkt(paced_meta_deq.bits)
      // refill token
      stateTokens := sRefill
    }

  }

  val timer_reg = RegInit(0.U(64.W))

  /* state machine to refill tokens */
  switch (stateTokens) {
    is (sRefill) {
      when (timer_reg === (MTU_CYCLES - 1).U) {
        timer_reg := 0.U
        stateTokens := sAvailable
      } .otherwise {
        timer_reg := timer_reg + 1.U
      }
    }
    is (sAvailable) {
      // NOTE: transition out of this state is implemented above (i.e. when a token is consumed)
    }
  }

  def schedule_pkt(meta: PISAEgressMetaIn) = {
      assert (scheduled_meta_enq.ready, "scheduled_meta queue is full during enqueue!")
      scheduled_meta_enq.valid := true.B
      scheduled_meta_enq.bits := meta
  }

  def pace_pkt(meta: PISAEgressMetaIn) = {
      assert (paced_meta_enq.ready, "paced_meta queue is full during enqueue!")
      paced_meta_enq.valid := true.B
      paced_meta_enq.bits := meta
  }

}

