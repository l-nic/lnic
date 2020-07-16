package lnic

import Chisel._

import chisel3.util.{HasBlackBoxResource}
import chisel3.experimental._
import freechips.rocketchip.rocket._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.rocket.NetworkHelpers._
import NICIO._
import NICIOvonly._
import LNICConsts._


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
 *   For DATA pkts going to core, record timestamp of the first word in the last 4 bytes of pkt.
 *   For DATA pkts coming from core, record timestamp in second to last 4B of pkt, and record latency
 *     in last 4B of pkt (i.e. now - pkt_timestamp) 
 * NOTE: current implementation assumes 8-byte aligned pkts.
 */
@chiselName
class LatencyModule extends Module {
  val io = IO(new Bundle {
    val net = new StreamIOvonly(NET_IF_BITS) 
    val nic = new StreamIOvonly(NET_IF_BITS)
  })

  val nic_ts = Module(new Timestamp(to_nic = true))
  nic_ts.io.net.in <> io.net.in
  io.nic.out <> nic_ts.io.net.out

  val net_ts = Module(new Timestamp(to_nic = false))
  net_ts.io.net.in <> io.nic.in
  io.net.out <> net_ts.io.net.out
}

@chiselName
class Timestamp(to_nic: Boolean = true) extends Module {
  val io = IO(new Bundle {
   val net = new StreamIOvonly(NET_IF_BITS)
  })

  val net_word = Reg(Valid(new StreamChannel(NET_IF_BITS)))

  net_word.valid := io.net.in.valid
  io.net.out.valid := net_word.valid // default
  net_word.bits := io.net.in.bits
  io.net.out.bits := net_word.bits

  // state machine to parse headers
  val sWordOne :: sWordTwo :: sWordThree :: sWordFour :: sWordFive :: sWordSix :: sWaitEnd :: Nil = Enum(7)
  val state = RegInit(sWordOne)

  val reg_now = RegInit(0.U(32.W))
  reg_now := reg_now + 1.U

  val reg_ts_start = RegInit(0.U)
  val reg_eth_type = RegInit(0.U)
  val reg_ip_proto = RegInit(0.U)
  val reg_lnic_flags = RegInit(0.U)
  val reg_lnic_src = RegInit(0.U)
  val reg_lnic_dst = RegInit(0.U)

  switch (state) {
    is (sWordOne) {
      reg_ts_start := reg_now
      transition(sWordTwo)
    }
    is (sWordTwo) {
      reg_eth_type := reverse_bytes(net_word.bits.data(47, 32), 2)
      transition(sWordThree)
    }
    is (sWordThree) {
      reg_ip_proto := net_word.bits.data(63, 56)
      transition(sWordFour)
    }
    is (sWordFour) {
      transition(sWordFive)
    }
    is (sWordFive) {
      reg_lnic_flags := net_word.bits.data(23, 16)
      reg_lnic_src := reverse_bytes(net_word.bits.data(39, 24), 2)
      reg_lnic_dst := reverse_bytes(net_word.bits.data(55, 40), 2)
      transition(sWordSix)
    }
    is (sWordSix) {
      transition(sWaitEnd)
    }
    is (sWaitEnd) {
      when (net_word.valid && net_word.bits.last) {
        state := sWordOne
        // overwrite last bytes with timestamp / latency
        val is_lnic_data = Wire(Bool())
        is_lnic_data := reg_eth_type === LNICConsts.IPV4_TYPE && reg_ip_proto === LNICConsts.LNIC_PROTO && reg_lnic_flags(0).asBool
        when (is_lnic_data) {
          val insert_timestamp = Wire(Bool())
          insert_timestamp:= to_nic.B && (reg_lnic_src === LNICConsts.TEST_CONTEXT_ID) 
          val insert_latency = Wire(Bool())
          insert_latency := !to_nic.B && (reg_lnic_dst === LNICConsts.TEST_CONTEXT_ID)
          val new_data = Wire(UInt())
          when (insert_timestamp) {
            new_data := Cat(reverse_bytes(reg_ts_start, 4), net_word.bits.data(31, 0))
            io.net.out.bits.data := new_data
            io.net.out.bits.keep := net_word.bits.keep
            io.net.out.bits.last := net_word.bits.last
          } .elsewhen (insert_latency) {
            val pkt_ts = reverse_bytes(net_word.bits.data(63, 32), 4)
            new_data := Cat(reverse_bytes(reg_now - pkt_ts, 4), reverse_bytes(reg_now, 4))
            // last 4B is latency, first 4B is timestamp
            io.net.out.bits.data := new_data
            io.net.out.bits.keep := net_word.bits.keep
            io.net.out.bits.last := net_word.bits.last
          }
        }
      }
    }
  }

  def transition(next_state: UInt) = {
    when (net_word.valid) {
      when (!net_word.bits.last) {
        state := next_state
      } .otherwise {
        state := sWordOne
      }
    }
  }

}

