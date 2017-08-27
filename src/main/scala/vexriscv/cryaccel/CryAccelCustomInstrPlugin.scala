package vexriscv.cryaccel

import spinal.core._
import vexriscv.plugin.{Masked, Plugin}
import vexriscv.{VexRiscv, Stageable, DecoderService}
import spinalcrypto.symmetric.aes._


class CryAccelCustomInstrPlugin(accelerator : CryAccel, opcode : String) extends Plugin[VexRiscv] {

  object IS_CUSTOM_INSTR extends Stageable(Bool)

  override def setup(pipeline: VexRiscv): Unit = {
    import pipeline.config._

    val dec = pipeline.service(classOf[DecoderService])
    dec.addDefault(IS_CUSTOM_INSTR, False)

    val pattern = MaskedLiteral.apply("---0000----------000-----" + opcode)

    dec.add(
      key = pattern,
      List(
        IS_CUSTOM_INSTR -> True,
        REGFILE_WRITE_VALID -> True,
        BYPASSABLE_EXECUTE_STAGE -> False,
        BYPASSABLE_MEMORY_STAGE -> False,
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

        val fire = arbitration.isValid && input(IS_CUSTOM_INSTR)

        accel.io.cmd.instr.opcode := fire ? input(INSTRUCTION)(6 downto 0) | 0
        accel.io.cmd.instr.rd := fire ? input(INSTRUCTION)(11 downto 7) | 0
        accel.io.cmd.instr.tags := fire ? input(INSTRUCTION)(14 downto 12) | 0
        accel.io.cmd.instr.rs1 := fire ? input(INSTRUCTION)(19 downto 15) | 0
        accel.io.cmd.instr.rs2 := fire ? input(INSTRUCTION)(24 downto 20) | 0
        accel.io.cmd.instr.funct := fire ? input(INSTRUCTION)(31 downto 25) | 0

        accel.io.cmd.rs1 := fire ? input(RS1) | 0
        accel.io.cmd.rs2 := fire ? input(RS2) | 0

      when(fire) {
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

        input(REGFILE_WRITE_DATA) := accel.io.resp.payload.data
      }
    }
  }
}
