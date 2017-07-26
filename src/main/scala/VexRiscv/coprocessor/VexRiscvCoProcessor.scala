package VexRiscv.coprocessor

import spinal.core._
import _root_.VexRiscv.{Pipeline, Stage, Stageable, VexRiscv}


case class VexRiscvCoProcessorConfig(val opcode : MaskedLiteral) {
  object INSTRUCTION extends Stageable(Bits(32 bits))
}

class VexRiscvCoProcessor(val config: VexRiscvCoProcessorConfig) extends Component with Pipeline {
  type  T = VexRiscvCoProcessor
  import config._

  stages ++= List.fill(2)(new Stage())
  val decode :: execute :: Nil = stages.toList

  decode.input(config.INSTRUCTION).addAttribute(Verilator.public)
}
