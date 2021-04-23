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
class SDNetNDPIngress extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val net = new NDPIngressIO
  })

//  // additional resources for using Firesim
//  addResource("/vsrc/sdnet_ndp_ingress_pkg.sv")
//  addResource("/vsrc/sdnet_ndp_ingress.edn")
//  addResource("/vsrc/sdnet_ndp_ingress_stub.v")

  addResource("/vsrc/SDNetNDPIngress.sv")
}

/* Egress Pipeline Blackbox */
class SDNetNDPEgress extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val net = new NDPEgressIO
  })

//  // additional resources for using Firesim
//  addResource("/vsrc/sdnet_ndp_egress_pkg.sv")
//  addResource("/vsrc/sdnet_ndp_egress.edn")
//  addResource("/vsrc/sdnet_ndp_egress_stub.v")

  addResource("/vsrc/SDNetNDPEgress.sv")
}

