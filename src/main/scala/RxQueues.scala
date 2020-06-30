package lnic

import Chisel._

import chisel3.{VecInit, SyncReadMem}
import chisel3.experimental._
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.subsystem.RocketTilesKey
import freechips.rocketchip.rocket._
import freechips.rocketchip.rocket.NetworkHelpers._
import freechips.rocketchip.rocket.LNICRocketConsts._
import LNICConsts._

/**
 * Global Rx Queues
 *
 * This module stores global per-port (or per-application) queues.
 * Tasks:
 *   - Receive msgs from the assemly module, each indicates the destination port #
 *   - Store the msg in the appropriate RX queue
 *   - Parameter: # of msgs outstanding at each core for each port # (i.e. app)
 *   - Keep track of the # of outstanding msgs at each core for each port #
 *   - When msg arrives:
 *     - Check to see if it can be immediately forwarded to one of the cores
 *   - When core indicates that it has finished processing a msg (also indicates port #):
 *     - Check if there is another msg for this port # that can be sent to the core
 */

class RxQueuesIO(implicit p: Parameters) extends Bundle {
  val net_in = Flipped(Decoupled(new StreamChannel(XLEN)))
  val meta_in = Flipped(Valid(new LNICRxMsgMeta))

  val num_tiles = p(RocketTilesKey).size
  val net_out = Vec(num_tiles, Decoupled(new StreamChannel(XLEN)))
  val meta_out = Vec(num_tiles, Valid(new LNICRxMsgMeta))

  // core tells NIC to insert (i.e. register) provided port # (if not already inserted)
  val add_context = Vec(num_tiles, Flipped(Valid(UInt(LNIC_CONTEXT_BITS.W))))
  // core tells NIC that it has finished processing
  val get_next_msg = Vec(num_tiles, Flipped(Valid(UInt(LNIC_CONTEXT_BITS.W))))
}

@chiselName
class RxQueues(implicit p: Parameters) extends Module {
  val io = IO(new RxQueuesIO)

  assert(p(RocketTilesKey).size == 1, "L-NIC only supports single core!")
  io.net_out(0) <> io.net_in
  io.meta_out(0) := io.meta_in
  
}

