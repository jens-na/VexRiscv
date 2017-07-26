package VexRiscv.coprocessor

import VexRiscv._
import _root_.VexRiscv.Plugin.{Masked, Plugin}
import spinal.core._

class CustomInstrPlugin(val coProcessor: VexRiscvCoProcessor) extends Plugin[VexRiscv] {

  object CUSTOM_INSTR extends Stageable(Bool)

  override def setup(pipeline: VexRiscv): Unit = {
    import pipeline.config._

    val dec = pipeline.service(classOf[DecoderService])
    dec.addDefault(CUSTOM_INSTR, False)

    val customOpcode = coProcessor.config.opcode
    val pattern = MaskedLiteral.apply("---0000----------000-----" + customOpcode)

    dec.add(
      key = pattern,
      List(
        CUSTOM_INSTR -> True,
        REGFILE_WRITE_VALID -> True,
        BYPASSABLE_EXECUTE_STAGE -> True,
        BYPASSABLE_MEMORY_STAGE -> True,
        RS1_USE -> True,
        RS2_USE -> True
      )
    )
  }

  override def build(pipeline: VexRiscv): Unit = {
    import pipeline._
    import pipeline.config._

    val rs1 = execute.input(RS1).asUInt
    val rs2 = execute.input(RS2).asUInt
    val rd = UInt(32 bits)

    val instr = decode.input(INSTRUCTION).asBits

    // Call Co-processor

    when(execute.input(CUSTOM_INSTR)){
      execute.output(REGFILE_WRITE_DATA) := rd.asBits
    }
  }
}
