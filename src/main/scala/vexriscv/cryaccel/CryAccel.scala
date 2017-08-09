package vexriscv.cryaccel

import spinal.core._
import spinal.lib._

case class CryAccelCmd() extends Bundle {
  val funct = Bits(7 bits)
  val rs2 = Bits(5 bits)
  val rs1 = Bits(5 bits)
  val tags = Bits(3 bits)
  val rd = Bits(5 bits)
  val opcode = Bits(7 bits)
}

case class CryAccelResp() extends Bundle {
  val rd = Bits(5 bits)
  val data = Bits(32 bits)
}

class CryAccel extends Component {
  val io = new Bundle {
    val flush = in Bool
    val cmd = slave Stream(CryAccelCmd())
    val resp = master Stream(CryAccelResp())
  }

  val flushResp = RegInit(False)
  val counter = Counter(15)
  val done = RegInit(True)

  io.cmd.ready := True
  io.resp.valid := flushResp

  when(done) {
    when(!flushResp || io.resp.ready) {
      counter.clear()
      done := !io.cmd.valid
    }
  }.otherwise {
    counter.increment()
    when(counter.willOverflowIfInc) {
      done := True
      flushResp := True
      io.resp.data := counter.value.asBits
    }
  }

  when(io.flush){
    done := True
    flushResp := False
  }
}