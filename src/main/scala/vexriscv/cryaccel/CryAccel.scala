package vexriscv.cryaccel

import spinal.core._
import spinal.lib._
import spinalcrypto.symmetric._
import spinalcrypto.symmetric.aes._

case class CryAccelCmd() extends Bundle {
  val instr = new Bundle {
    val funct = Bits(7 bits)
    val rs2 = Bits(5 bits)
    val rs1 = Bits(5 bits)
    val tags = Bits(3 bits)
    val rd = Bits(5 bits)
    val opcode = Bits(7 bits)
  }
  val rs1 = Bits(32 bits)
  val rs2 = Bits(32 bits)
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
  val flushResp = False

  // VexRiscv has no 128 bit xmmN register for floating points
  // This way we can load 4x32 bits (128 bits) into a
  // RAM which is only accessible by the accelerator
  // to store and load data.
  val ramKey = Mem(Bits(32 bits), 4)
  val ramData = Mem(Bits(32 bits), 4)
  val ramResult = Mem(Bits(32 bits), 4)

  val funct = io.cmd.instr.funct
  val aesLoadKey = (funct === B"0000001")
  val aesLoadData = (funct === B"0000010")
  val aesEncrypt = (funct === B"0000011")

  val key = Bits(128 bits)
  val data = Bits(128 bits)

  val aes = new AESCore(128, 128)
  val crypto = slave(new SymmetricCryptoBlockIO(aes.gIO))
  aes.io.crypto <> crypto

  aes.io.crypto.cmd.arbitrationFrom(io.cmd)
  aes.io.crypto.cmd.enc := aesEncrypt
  aes.io.crypto.cmd.block := data
  aes.io.crypto.cmd.key := key

  when(aesLoadKey) {
    val internalAddr = io.cmd.instr.rd.asUInt.resize(2)
    ramKey.write(internalAddr, io.cmd.rs1)
    flushResp := True
  }

  when(aesLoadData) {
    val internalAddr = io.cmd.instr.rd.asUInt.resize(2)
    ramData.write(internalAddr, io.cmd.rs1)
    flushResp := True
  }

  when(aesEncrypt) {

    key := ((127 downto 96) -> ramKey.readAsync(3),
            (95 downto 64) -> ramKey.readAsync(2),
            (63 downto 32) -> ramKey.readAsync(1),
            (31 downto 0) -> ramKey.readAsync(0))

    data := ((127 downto 96) -> ramData.readAsync(3),
            (95 downto 64) -> ramData.readAsync(2),
            (63 downto 32) -> ramData.readAsync(1),
            (31 downto 0) -> ramData.readAsync(0))

  }.otherwise {
    key := (default -> false)
    data := (default -> false)
  }


  val rspValid  = RegNext(flushResp)
  io.resp.data := key(31 downto 0)
  io.cmd.ready := rspValid
  io.resp.valid := rspValid
}