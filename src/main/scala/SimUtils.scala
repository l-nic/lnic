package lnic

import Chisel._

import chisel3.util.{HasBlackBoxResource}
import chisel3.experimental._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.rocket.NetworkHelpers._
import NICIO._
import NICIOvonly._

/* Test Modules */

class SimNetwork extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val net = Flipped(new NICIOvonly)
  })

  addResource("/vsrc/LNIC_SimNetwork.v")
  addResource("/csrc/LNIC_SimNetwork.cc")
  addResource("/csrc/LNIC_device.h")
  addResource("/csrc/LNIC_device.cc")
  addResource("/csrc/LNIC_packet.h")
}

/**
 * Parse Eth/IP/LNIC headers.
 * Check if LNIC src/dst port indicates test_app then insert timestamp/latency measurement
 * If yes:
 *   For pkts going to core, record timestamp of the first word in the last 8 bytes of msg.
 *   For pkts coming from core, record timestamp - now in the last 8 bytes of msg.
 * TODO(sibanez): current implementation assumes 8-byte aligned msgs.
 */
class LatencyModule extends Module {
  val io = IO(new Bundle {
    val net = new NICIO 
    val nic = new NICIO 
  })

  val nic_ts = Module(new Timestamp(to_nic = true))
  nic_ts.io.net.in <> io.net.in
  io.nic.out <> nic_ts.io.net.out

  val net_ts = Module(new Timestamp(to_nic = false))
  net_ts.io.net.in <> io.nic.in
  io.net.out <> net_ts.io.net.out
}

class Timestamp(to_nic: Boolean = true) extends Module {
  val io = IO(new Bundle {
   val net = new NICIO 
  })

  // state machine to parse headers
  val sWordOne :: sWordTwo :: sWordThree :: sWordFour :: sWordFive :: sWordSix :: sWaitEnd :: Nil = Enum(7)
  val state = RegInit(sWordOne)

  val reg_now = RegInit(0.U(64.W))
  reg_now := reg_now + 1.U

  val reg_ts_start = RegInit(0.U)
  val reg_eth_type = RegInit(0.U)
  val reg_ip_proto = RegInit(0.U)
  val reg_lnic_src = RegInit(0.U)
  val reg_lnic_dst = RegInit(0.U)

  // default - connect input to output
  io.net.out <> io.net.in

  switch (state) {
    is (sWordOne) {
      reg_ts_start := reg_now
      transition(sWordTwo)
    }
    is (sWordTwo) {
      reg_eth_type := reverse_bytes(io.net.in.bits.data(47, 32), 2)
      transition(sWordThree)
    }
    is (sWordThree) {
      reg_ip_proto := io.net.in.bits.data(63, 56)
      transition(sWordFour)
    }
    is (sWordFour) {
      transition(sWordFive)
    }
    is (sWordFive) {
      reg_lnic_src := reverse_bytes(io.net.in.bits.data(31, 16), 2)
      reg_lnic_dst := reverse_bytes(io.net.in.bits.data(47, 32), 2)
      transition(sWordSix)
    }
    is (sWordSix) {
      transition(sWaitEnd)
    }
    is (sWaitEnd) {
      when (io.net.in.valid && io.net.in.ready && io.net.in.bits.last) {
        state := sWordOne
        // overwrite last bytes with timestamp / latency
        when (reg_eth_type === LNICConsts.IPV4_TYPE && reg_ip_proto === LNICConsts.LNIC_PROTO) {
          when (to_nic.B && (reg_lnic_src === LNICConsts.TEST_CONTEXT_ID)) {
            io.net.out.bits.data := reverse_bytes(reg_ts_start, 8)
          } .elsewhen (!to_nic.B && (reg_lnic_dst === LNICConsts.TEST_CONTEXT_ID)) {
            val pkt_ts = reverse_bytes(io.net.in.bits.data, 8)
            io.net.out.bits.data := reverse_bytes(reg_now - pkt_ts, 8)
          }
        }
      }
    }
  }

  def transition(next_state: UInt) = {
    when (io.net.in.valid && io.net.in.ready) {
      when (!io.net.in.bits.last) {
        state := next_state
      } .otherwise {
        state := sWordOne
      }
    }
  }

}

