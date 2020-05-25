package lnic

import Chisel._
import chisel3.{VecInit, chiselTypeOf}
import chisel3.util.HasBlackBoxResource
import chisel3.experimental.IntParam
import freechips.rocketchip.rocket._
import LNICConsts._

object MsgBufHelpers {
  def compute_num_pkts(msg_len: UInt) = {
    require(isPow2(MAX_SEG_LEN_BYTES))
    // check if msg_len is divisible by MAX_SEG_LEN_BYTES
    val num_pkts = Mux(msg_len(log2Up(MAX_SEG_LEN_BYTES)-1, 0) === 0.U,
                       msg_len >> log2Up(MAX_SEG_LEN_BYTES).U,
                       (msg_len >> log2Up(MAX_SEG_LEN_BYTES).U) + 1.U)
    num_pkts
  }
  
  def make_size_class_freelists() = {
    // Vector of freelists to keep track of available buffers to store msgs.
    //   There is one free list for each size class.
    var ptr = 0
    val size_class_freelists = for ((size, count) <- MSG_BUFFER_COUNT) yield {
      require(size % NET_DP_BYTES == 0, "Size of each buffer must be evenly divisible by word size.")
      // compute the buffer pointers to insert into each free list
      val buf_ptrs = for (i <- 0 until count) yield {
          val p = ptr
          ptr = p + size/NET_DP_BYTES
          p.U(BUF_PTR_BITS.W)
      }
      val free_list = Module(new FreeList(buf_ptrs))
      free_list
    }
    VecInit(size_class_freelists.map(_.io).toSeq)
  }
}

class StreamNarrower[T <: Data](inW: Int, outW: Int, metaType: T) extends Module {
  require(inW > outW)
  require(inW % outW == 0)

  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new StreamChannel(inW)))
    val meta_in = Flipped(Valid(metaType.cloneType))
    val out = Decoupled(new StreamChannel(outW))
    val meta_out = Valid(metaType.cloneType)
  })

  val outBytes = outW / 8
  val outBeats = inW / outW

  val bits = Reg(new StreamChannel(inW))
  val meta_valid = Reg(Bool())
  val meta = Reg(metaType.cloneType)
  val count = Reg(UInt(log2Ceil(outBeats).W))

  val s_recv :: s_send_first :: s_send_finish :: Nil = Enum(3)
  val state = RegInit(s_recv)

  val nextData = bits.data >> outW.U
  val nextKeep = bits.keep >> outBytes.U

  io.in.ready := state === s_recv
  io.out.valid := (state === s_send_first) || (state === s_send_finish)
  io.out.bits.data := bits.data(outW - 1, 0)
  io.out.bits.keep := bits.keep(outBytes - 1, 0)
  io.out.bits.last := bits.last && !nextKeep.orR
  io.meta_out.bits := meta
  io.meta_out.valid := meta_valid && (state === s_send_first)

  when (io.in.fire()) {
    count := (outBeats - 1).U
    bits := io.in.bits
    meta_valid := io.meta_in.valid
    meta := io.meta_in.bits
    state := s_send_first
  }

  when (io.out.fire()) {
    count := count - 1.U
    bits.data := nextData
    bits.keep := nextKeep
    state := s_send_finish
    when (io.out.bits.last || count === 0.U) {
      state := s_recv
    }
  }
}

class StreamWidener[T <: Data](inW: Int, outW: Int, metaType: T) extends Module {
  require(outW > inW)
  require(outW % inW == 0)

  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new StreamChannel(inW)))
    val meta_in = Flipped(Valid(metaType.cloneType))
    val out = Decoupled(new StreamChannel(outW))
    val meta_out = Valid(metaType.cloneType)
  })

  val inBytes = inW / 8
  val inBeats = outW / inW

  val data = Reg(Vec(inBeats, UInt(inW.W)))
  val keep = RegInit(Vec(Seq.fill(inBeats)(0.U(inBytes.W))))
  val last = Reg(Bool())
  val meta = Reg(metaType.cloneType)
  val meta_valid = Reg(Bool())

  val idx = RegInit(0.U(log2Ceil(inBeats).W))

  val s_recv_first :: s_recv_finish :: s_send :: Nil = Enum(3)
  val state = RegInit(s_recv_first)

  io.in.ready := (state === s_recv_first) || (state === s_recv_finish)
  io.out.valid := (state === s_send) && !(reset.toBool)
  io.out.bits.data := data.asUInt
  io.out.bits.keep := keep.asUInt
  io.out.bits.last := last
  io.meta_out.bits := meta
  io.meta_out.valid := meta_valid

  when (io.in.fire()) {
    idx := idx + 1.U
    data(idx) := io.in.bits.data
    keep(idx) := io.in.bits.keep
    state := s_recv_finish
    when (state === s_recv_first) {
      meta := io.meta_in.bits
      meta_valid := io.meta_in.valid
    }
    when (io.in.bits.last || idx === (inBeats - 1).U) {
      last := io.in.bits.last
      state := s_send
    }
  }

  when (io.out.fire()) {
    idx := 0.U
    keep.foreach(_ := 0.U)
    state := s_recv_first
  }
}

object StreamWidthAdapter {
  def apply[T <: Data](out: DecoupledIO[StreamChannel], meta_out: T, in: DecoupledIO[StreamChannel], meta_in: T) {
    if (out.bits.w > in.bits.w) {
      val widener = Module(new StreamWidener(in.bits.w, out.bits.w, chiselTypeOf(meta_out)))
      widener.io.in <> in
      widener.io.meta_in := meta_in
      out <> widener.io.out
      meta_out := widener.io.meta_out
    } else if (out.bits.w < in.bits.w) {
      val narrower = Module(new StreamNarrower(in.bits.w, out.bits.w, chiselTypeOf(meta_out)))
      narrower.io.in <> in
      narrower.io.meta_in := meta_in
      out <> narrower.io.out
      meta_out := narrower.io.meta_out
    } else {
      out <> in
      meta_out := meta_in
    }
  }

  def apply(out: DecoupledIO[StreamChannel], in: DecoupledIO[StreamChannel]) {
    if (out.bits.w > in.bits.w) {
      val widener = Module(new StreamWidener(in.bits.w, out.bits.w, Bool()))
      widener.io.in <> in
      out <> widener.io.out
    } else if (out.bits.w < in.bits.w) {
      val narrower = Module(new StreamNarrower(in.bits.w, out.bits.w, Bool()))
      narrower.io.in <> in
      out <> narrower.io.out
    } else {
      out <> in
    }
  }

}

class RWPortIO(val addr_width: Int, val data_width: Int) extends Bundle {
  val addr = Input(UInt(addr_width.W))
  val dout = Output(UInt(data_width.W))
  val we   = Input(Bool())
  val din  = Input(UInt(data_width.W))
}

/* BlackBox True Dual Port RAM */
class TrueDualPortRAM(val data_width: Int, val num_entries: Int)
    extends BlackBox(Map("DATA_WIDTH" -> IntParam(data_width),
                         "NUM_ENTRIES" -> IntParam(num_entries)))
    with HasBlackBoxResource {

  val io = IO(new Bundle {
    val clock = Input(Clock())
    val reset = Input(Bool())
    val portA = new RWPortIO(log2Up(num_entries), data_width) 
    val portB = new RWPortIO(log2Up(num_entries), data_width) 
  })

  addResource("/vsrc/TrueDualPortRAM.v")
}

object MemHelpers {
  // Initialize all memory entries with the provided value
  def memory_init(mem_port: RWPortIO, num_entries: Int, reset_val: UInt, init_done: Bool) = {

    val sReset :: sIdle :: Nil = Enum(2)
    val state = RegInit(sReset)

    val index = RegInit(0.U(log2Up(num_entries).W))

    switch(state) {
      is (sReset) {
        init_done := false.B
        mem_port.addr := index
        mem_port.we := true.B
        mem_port.din := reset_val
        index := index + 1.U
        when (index === (num_entries - 1).U) {
          state := sIdle
        }
      }
      is (sIdle) {
        init_done := true.B
      }
    }

  }
}

