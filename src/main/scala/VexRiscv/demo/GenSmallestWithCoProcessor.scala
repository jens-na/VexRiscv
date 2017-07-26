package VexRiscv.demo

import VexRiscv.Plugin._
import VexRiscv.{Plugin, VexRiscv, VexRiscvConfig}
import _root_.VexRiscv.coprocessor.{CustomInstrPlugin, VexRiscvCoProcessor, VexRiscvCoProcessorConfig}
import spinal.core._

object GenSmallestWithCoProcessor extends App{

  // The custom instructions as described in RISC-V-ISA
  object CustomOpcodes {
    val custom0 = M"0001011"
    val custom1 = M"0101011"
    val custom2 = M"1011011"
    val custom3 = M"1111011"
  }

  // The Co-Cpu config
  val coProcessor = new CustomInstrPlugin(new VexRiscvCoProcessor(
    config = VexRiscvCoProcessorConfig(
      opcode = CustomOpcodes.custom0
    )
  ))

  // The core Cpu config
  def cpu() = new VexRiscv(
    config = VexRiscvConfig(
      plugins = List(

        coProcessor,

        new PcManagerSimplePlugin(
          resetVector = 0x00000000l,
          fastPcCalculation = true
        ),
        new IBusSimplePlugin(
          interfaceKeepData = false,
          catchAccessFault = false
        ),
        new DBusSimplePlugin(
          catchAddressMisaligned = false,
          catchAccessFault = false
        ),
        new CsrPlugin(CsrPluginConfig.smallest),
        new DecoderSimplePlugin(
          catchIllegalInstruction = false
        ),
        new RegFilePlugin(
          regFileReadyKind = Plugin.SYNC,
          zeroBoot = true
        ),
        new IntAluPlugin,
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = false
        ),
        new LightShifterPlugin,
        new HazardSimplePlugin(
          bypassExecute           = false,
          bypassMemory            = false,
          bypassWriteBack         = false,
          bypassWriteBackBuffer   = false,
          pessimisticUseSrc       = false,
          pessimisticWriteRegFile = false,
          pessimisticAddressMatch = false
        ),
        new BranchPlugin(
          earlyBranch = false,
          catchAddressMisaligned = false,
          prediction = NONE
        ),
        new YamlPlugin("cpu0.yaml")
      )
    )
  )

  SpinalVerilog(cpu())
}
