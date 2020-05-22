package lnic

import chisel3._
import freechips.rocketchip.config.Config
import LNICConsts._

// TODO(sibanez): this should eventually be updated to actually pass params.
//   Currently just using the defaults.
class WithLNIC extends Config((site, here, up) => {
  case LNICKey => Some(LNICParams())
})

