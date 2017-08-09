package vexriscv.cryaccel

import spinal.core._
import vexriscv.plugin.{Masked, Plugin}
import vexriscv.{VexRiscv, Stageable, DecoderService}


class CryAccelCustomInstrPlugin(accelerator1 : CryAccel, opcode : String) extends Plugin[VexRiscv] {

  object IS_CUSTOM_INSTR extends Stageable(Bool)

  override def setup(pipeline: VexRiscv): Unit = {
    import pipeline.config._

    val dec = pipeline.service(classOf[DecoderService])
    dec.addDefault(IS_CUSTOM_INSTR, False)

    val pattern = MaskedLiteral.apply("---0000----------000-----" + opcode)
    //val pattern = M"0000011----------000-----0110011"

    dec.add(
      key = pattern,
      List(
        IS_CUSTOM_INSTR -> True,
        REGFILE_WRITE_VALID -> True,
        BYPASSABLE_EXECUTE_STAGE -> False,
        BYPASSABLE_MEMORY_STAGE -> True,
        RS1_USE -> True,
        RS2_USE -> True
      )
    )
  }

  override def build(pipeline: VexRiscv): Unit = {
    import pipeline._
    import pipeline.config._

    val accel = new CryAccel()

    execute plug new Area {
      import execute._

      accel.io.cmd.valid := False

      when(arbitration.isValid && input(IS_CUSTOM_INSTR)) {
        accel.io.cmd.valid := !arbitration.isStuckByOthers && !arbitration.removeIt
        arbitration.haltItself := memory.arbitration.isValid && memory.input(IS_CUSTOM_INSTR)
      }
    }

    memory plug new Area {
      import memory._

      accel.io.flush := memory.arbitration.removeIt
      accel.io.resp.ready := !arbitration.isStuckByOthers

      when(arbitration.isValid && input(IS_CUSTOM_INSTR)) {
        arbitration.haltItself := !accel.io.resp.valid

        input(REGFILE_WRITE_DATA) := Mux(input(INSTRUCTION)(13),
          input(INSTRUCTION), input(INSTRUCTION)).asBits
      }
    }
  }
}
