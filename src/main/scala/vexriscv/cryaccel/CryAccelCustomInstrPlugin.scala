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

    //Add a new scope on the execute stage (used to give a name to signals)
    execute plug new Area {
      import execute._

      //Define some signals used internally to the plugin
      //val rs1 = execute.input(RS1).asUInt //32 bits UInt value of the regfile[RS1]
      //val rs2 = execute.input(RS2).asUInt
      //val rd = UInt(32 bits)

      //Do some computation
      /*rd(7 downto 0) := rs1(7 downto 0) + rs2(7 downto 0)
      rd(16 downto 8) := rs1(16 downto 8) + rs2(16 downto 8)
      rd(23 downto 16) := rs1(23 downto 16) + rs2(23 downto 16)
      rd(31 downto 24) := rs1(31 downto 24) + rs2(31 downto 24)*/

      accel.io.cmd.valid := False

      //When the instruction is a SIMD_ADD one, then write the result into the register file data path.
      when(arbitration.isValid && input(IS_CUSTOM_INSTR)) {
        //execute.output(REGFILE_WRITE_DATA) := rd.asBits
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
