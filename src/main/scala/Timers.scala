package lnic

import Chisel._

import chisel3.experimental._
import LNICConsts._

class TimerMeta extends Bundle {
  // max pkt offset transmitted at the last timeout event
  val rtx_offset = UInt(PKT_OFFSET_BITS.W)
  val msg_desc = new TxMsgDescriptor
}

class ScheduleEvent extends Bundle {
  val msg_id = UInt(MSG_ID_BITS.W)
  val delay = UInt(TIMER_BITS.W)
  val metadata = new TimerMeta
}

class CancelEvent extends Bundle {
  val msg_id = UInt(MSG_ID_BITS.W)
}

class TimeoutEvent extends Bundle {
  val msg_id = UInt(MSG_ID_BITS.W)
  val metadata = new TimerMeta
}

class TimerEntry extends Bundle {
  val valid = Bool()
  val timeout_val = UInt(TIMER_BITS.W)
  val metadata = new TimerMeta
}

/* LNIC Timers:
 * Maintain N timers that support schedule, reschedule, cancel, and timeout events
 */
class LNICTimersIO extends Bundle {
  val schedule = Flipped(Valid(new ScheduleEvent))
  val reschedule = Flipped(Valid(new ScheduleEvent))
  val cancel = Flipped(Valid(new CancelEvent))
  val timeout = Valid(new TimeoutEvent)
}

@chiselName
class LNICTimers(implicit p: Parameters) extends Module {
  val io = IO(new LNICTimersIO)

  // Timer state
  val timer_mem = Module(new TrueDualPortRAM((new TimerEntry).getWidth, NUM_MSG_BUFFERS))
  timer_mem.io.clock := clock
  timer_mem.io.reset := reset
  timer_mem.io.portA.we := false.B
  timer_mem.io.portB.we := false.B

  // initialize timer_mem so all entries are invalid
  val init_done_reg = RegInit(false.B)
  MemHelpers.memory_init(timer_mem.io.portA, NUM_MSG_BUFFERS, 0.U, init_done_reg)

  // Cycle counter to track time
  val now = RegInit(0.U(TIMER_BITS.W))
  now := now + 1.U

  /* Logic to process Schedule/Reschedule/Timeout Events */
  // NOTE: uses timer_mem.io.portA

  // Pipeline regs
  val schedule_reg = RegNext(io.schedule)
  val reschedule_reg = RegNext(io.reschedule)

  val new_timer_entry = Wire(new TimerEntry)

  // state machine used to search for timeouts
  val cur_timer_id = RegInit(0.U(MSG_ID_BITS.W))
  val sRead :: sCheck :: Nil = Enum(2)
  val stateTimeout = RegInit(sRead)

  // defaults
  io.timeout.valid := false.B

  when (!reset.toBool) {
    when (schedule_reg.valid) {
      stateTimeout := sRead // reset timeout state machine
      // process schedule event
      new_timer_entry.valid := true.B
      new_timer_entry.timeout_val := now + schedule_reg.bits.delay
      new_timer_entry.metadata := schedule_reg.bits.metadata
      timer_mem.io.portA.addr := schedule_reg.bits.msg_id
      timer_mem.io.portA.we   := true.B
      timer_mem.io.portA.din  := new_timer_entry.asUInt
    } .elsewhen (reschedule_reg.valid) {
      stateTimeout := sRead // reset timeout state machine
      // process reschedule event
      new_timer_entry.valid := true.B
      new_timer_entry.timeout_val := now + reschedule_reg.bits.delay
      new_timer_entry.metadata := reschedule_reg.bits.metadata
      timer_mem.io.portA.addr := reschedule_reg.bits.msg_id
      timer_mem.io.portA.we   := true.B
      timer_mem.io.portA.din  := new_timer_entry.asUInt
    } .elsewhen (init_done_reg) {
      // state machine to search for timeouts

      timer_mem.io.portA.addr := cur_timer_id

      switch (stateTimeout) {
        is (sRead) {
          // start reading
          // state transition
          stateTimeout := sCheck
        }
        is (sCheck) {
          // get read result
          val cur_timer_entry = Wire(new TimerEntry)
          cur_timer_entry := (new TimerEntry).fromBits(timer_mem.io.portA.dout)
          when (cur_timer_entry.valid && now >= cur_timer_entry.timeout_val) {
            // fire timeout event
            io.timeout.valid := true.B
            io.timeout.bits.msg_id := cur_timer_id
            io.timeout.bits.metadata := cur_timer_entry.metadata
            // do not fire the timer again
            val update_timer_entry = Wire(new TimerEntry)
            update_timer_entry := cur_timer_entry
            update_timer_entry.valid := false.B
            timer_mem.io.portA.we := true.B
            timer_mem.io.portA.din := update_timer_entry.asUInt
          }
          // move to next timer entry
          cur_timer_id := Mux(cur_timer_id === (NUM_MSG_BUFFERS-1).U,
                              0.U,
                              cur_timer_id + 1.U)
          // state transition
          stateTimeout := sRead
        }
      }
    }
  }

  /* Logic to process CancelEvents */
  // NOTE: uses timer_mem.io.portB

  // Pipeline reg
  val cancel_reg = RegNext(io.cancel)

  when (cancel_reg.valid && !reset.toBool) {
    val cancel_entry = Wire(new TimerEntry)
    cancel_entry.valid := false.B
    cancel_entry.timeout_val := 0.U
    cancel_entry.metadata := (new TimerMeta).fromBits(0.U)
    timer_mem.io.portB.addr := cancel_reg.bits.msg_id
    timer_mem.io.portB.we := true.B
    timer_mem.io.portB.din := cancel_entry.asUInt
  }

}

