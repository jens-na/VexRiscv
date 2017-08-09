package vexriscv.demo

import vexriscv.plugin._
import vexriscv.{plugin, VexRiscv, VexRiscvConfig}
import vexriscv.cryaccel._
import spinal.core._

object GenSmallestWithAccelerator extends App{

  // The custom instructions as described in the RISC-V-ISA
  object CustomOpcodes {
    val Custom0 = "0001011"
    val Custom1 = "0101011"
    val Custom2 = "1011011"
    val Custom3 = "1111011"
  }

  // The accelerator
  def accelerator() = new CryAccel()
  def customInstr() = new CryAccelCustomInstrPlugin(accelerator(), CustomOpcodes.Custom0)

  // The core Cpu config
  def cpu() = new VexRiscv(
    config = VexRiscvConfig(
      plugins = List(

        customInstr(),

        new PcManagerSimplePlugin(
          resetVector = 0x00000000l,
          relaxedPcCalculation = false
        ),
        new IBusSimplePlugin(
          interfaceKeepData = false,
          catchAccessFault = false
        ),
        new DBusSimplePlugin(
          catchAddressMisaligned = false,
          catchAccessFault = false
        ),
        new DecoderSimplePlugin(
          catchIllegalInstruction = false
        ),
        new RegFilePlugin(
          regFileReadyKind = plugin.SYNC,
          zeroBoot = true
        ),
        new IntAluPlugin,
        new SrcPlugin(
          separatedAddSub = false,
          executeInsertion = false
        ),
        new FullBarrielShifterPlugin,
        new HazardSimplePlugin(
          bypassExecute           = true,
          bypassMemory            = true,
          bypassWriteBack         = true,
          bypassWriteBackBuffer   = true,
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
