package lnic

import Chisel._

import chisel3.{VecInit, chiselTypeOf}
import chisel3.experimental._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.rocket.{StreamChannel, StreamIO}
import freechips.rocketchip.rocket.NetworkHelpers._
import LNICConsts._

/**
 * LNIC Arbiter classes
 * Used to schedule between control pkts (PktGen) and data pkts (Packetize) on the TX path.
 */
class ArbiterIO extends Bundle {
  // Generated control pkts
  val ctrl_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val ctrl_meta_in = Flipped(Valid(new EgressMetaIn))
  // Packetized data pkts
  val data_in = Flipped(Decoupled(new StreamChannel(NET_DP_BITS)))
  val data_meta_in = Flipped(Valid (new EgressMetaIn))
  // Serialized pkts
  val net_out = Decoupled(new StreamChannel(NET_DP_BITS))
  val meta_out = Valid(new EgressMetaIn)
}

@chiselName
class LNICArbiter(implicit p: Parameters) extends Module {
  val io = IO(new ArbiterIO)

  val pktQueue_in = Wire(Decoupled(new StreamChannel(NET_DP_BITS)))
  val metaQueue_in = Wire(Decoupled(new EgressMetaIn))
  val metaQueue_out = Wire(Flipped(Decoupled(new EgressMetaIn)))

  // Set up output queues
  // TODO(sibanez): use params or consts here?
  io.net_out <> Queue(pktQueue_in, ARBITER_PKT_BUF_FILTS)
  metaQueue_out <> Queue(metaQueue_in, ARBITER_META_BUF_FILTS)

  when (reset.toBool) {
    io.net_out.valid := false.B
  }

  /* state machine to arbitrate between ctrl_in and data_in */

  val sInSelect :: sInWaitEnd :: Nil = Enum(2)
  val inState = RegInit(sInSelect)

  val ctrl :: data :: Nil = Enum(2)
  val reg_selected = RegInit(ctrl)

  // defaults
  pktQueue_in.valid := false.B
  pktQueue_in.bits := io.ctrl_in.bits
  metaQueue_in.valid := false.B
  metaQueue_in.bits := io.ctrl_meta_in.bits

  io.ctrl_in.ready := false.B
  io.data_in.ready := false.B

  def selectCtrl() = {
    reg_selected := ctrl
    pktQueue_in <> io.ctrl_in
    metaQueue_in.valid := pktQueue_in.valid && pktQueue_in.ready
    metaQueue_in.bits := io.ctrl_meta_in.bits
    when (pktQueue_in.valid && pktQueue_in.ready) {
      assert(metaQueue_in.ready, "Arbiter: metaQueue is full during insertion of ctrl pkt!")
      assert(io.ctrl_meta_in.valid, "Arbiter: io.ctrl_meta_in is not valid on first word of pkt!")
    }
    when (pktQueue_in.valid && pktQueue_in.ready && !pktQueue_in.bits.last) {
      inState := sInWaitEnd
    }
  }

  def selectData() = {
    reg_selected := data
    pktQueue_in <> io.data_in
    metaQueue_in.valid := pktQueue_in.valid && pktQueue_in.ready
    metaQueue_in.bits := io.data_meta_in.bits
    when (pktQueue_in.valid && pktQueue_in.ready) {
      assert(metaQueue_in.ready, "Arbiter: metaQueue is full during insertion of data pkt!")
      assert(io.data_meta_in.valid, "Arbiter: io.data_meta_in is not valid on first word of pkt!")
    }
    when (pktQueue_in.valid && pktQueue_in.ready && !pktQueue_in.bits.last) {
      inState := sInWaitEnd
    }
  }

  switch (inState) {
    is (sInSelect) {
      // select which input to read from
      // NOTE: ctrl pkts are strictly prioritized over data pkts
      when (io.ctrl_in.valid) {
        selectCtrl()
      } .elsewhen (io.data_in.valid) {
        selectData()
      }
    }
    is (sInWaitEnd) {
      when (reg_selected === ctrl) {
        // Ctrl pkt selected
        pktQueue_in <> io.ctrl_in
      } .otherwise {
        // Data pkt selected
        pktQueue_in <> io.data_in
      }
      // wait until end of selected pkt then transition back to sSelect
      when (pktQueue_in.valid && pktQueue_in.ready && pktQueue_in.bits.last) {
        inState := sInSelect
      }
    }
  }

  // state machine to drive metaQueue_out.ready
  val sOutWordOne :: sOutWaitEnd :: Nil = Enum(2)
  val outState = RegInit(sOutWordOne)

  // only read metaQueue when first word is transferred to Egress pipeline
  metaQueue_out.ready := (outState === sOutWordOne) && (io.net_out.valid && io.net_out.ready)
  io.meta_out.valid := (outState === sOutWordOne) && (io.net_out.valid && metaQueue_out.valid)
  io.meta_out.bits := metaQueue_out.bits

  switch (outState) {
    is (sOutWordOne) {
      when (io.net_out.valid && io.net_out.ready && !io.net_out.bits.last) {
        outState := sOutWaitEnd
      }
    }
    is (sOutWaitEnd) {
      when (io.net_out.valid && io.net_out.ready && io.net_out.bits.last) {
        outState := sOutWordOne
      }
    }
  }
}

