package lnic

import Chisel._

import chisel3.{VecInit, SyncReadMem}
import chisel3.experimental._
import freechips.rocketchip.config.Parameters
import chisel3.util.HasBlackBoxResource
import freechips.rocketchip.rocket._
import freechips.rocketchip.rocket.NetworkHelpers._
import freechips.rocketchip.rocket.LNICRocketConsts._
import LNICConsts._

/* Ingress Pipeline Blackbox */
class SDNetHomaIngress extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val net = new HomaIngressIO
  })

//  // additional resources for using Firesim
//  addResource("/vsrc/sdnet_homa_ingress_pkg.sv")
//  addResource("/vsrc/sdnet_homa_ingress.edn")
//  addResource("/vsrc/sdnet_homa_ingress_stub.v")

  addResource("/vsrc/SDNetHomaIngress.sv")
}

/* Egress Pipeline Blackbox */
class SDNetHomaEgress extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val net = new HomaEgressIO
  })

//  // additional resources for using Firesim
//  addResource("/vsrc/sdnet_homa_egress_pkg.sv")
//  addResource("/vsrc/sdnet_homa_egress.edn")
//  addResource("/vsrc/sdnet_homa_egress_stub.v")

  addResource("/vsrc/SDNetHomaEgress.sv")
}

