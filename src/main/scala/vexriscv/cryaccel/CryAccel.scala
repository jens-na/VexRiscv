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

  // VexRiscv has no 128 bit xmmN register for floating points
  // This way we can load 4x32 bits (128 bits) into a
  // RAM which is only accessible by the accelerator
  // to store and load data.
  val ramKey = Mem(Bits(32 bits), 4)
  val ramData = Mem(Bits(32 bits), 4)
  val ramResult = Mem(Bits(32 bits), 4)

  // The functions of this accelerator
  val funct = io.cmd.instr.funct
  val funcAesStoreKey = (funct === B"0000001")
  val funcAesStoreData = (funct === B"0000010")
  val funcAesLoadResult = (funct === B"0000011")
  val funcAesEncrypt = (funct === B"0000100")

  val accelStoreKey = RegInit(False)
  val accelStoreData = RegInit(False)
  val accelEncrypt = RegInit(False)
  val accelLoadResult = RegInit(False)

  val flushResp = RegInit(False)
  val flushAesResp = RegInit(False)
  val done = RegInit(True)

  val key = Reg(Bits(128 bits))
  val data = Reg(Bits(128 bits))
  val result = Reg(Bits(32 bits))

  val internalAddr = Reg(UInt(2 bits))
  val internalWriteData = Reg(Bits(32 bits))

  val aes = new AESCore(128, 128)
  val crypto = slave(new SymmetricCryptoBlockIO(aes.gIO))

  aes.io.crypto <> crypto
  aes.io.crypto.cmd.valid := accelEncrypt
  aes.io.crypto.cmd.enc := accelEncrypt
  aes.io.crypto.cmd.block := data
  aes.io.crypto.cmd.key := key
  aes.io.crypto.cmd.valid := accelEncrypt

  flushAesResp := aes.io.crypto.rsp.valid
  io.cmd.ready := False
  io.resp.valid := flushResp

  when(io.resp.ready){
    flushResp := False
  }

  when(done) {

    // New accelerator command
    when(!flushResp || io.resp.ready) {

      when(funcAesStoreKey || funcAesStoreData) {
        internalAddr := io.cmd.instr.rd.asUInt.resize(2)
        internalWriteData := io.cmd.rs1

      }

      when(funcAesStoreKey) {
        accelStoreKey := True
      }

      when(funcAesStoreData) {
        accelStoreData := True
      }

      when(funcAesEncrypt) {
        key := ((127 downto 96) -> ramKey.readAsync(3),
          (95 downto 64) -> ramKey.readAsync(2),
          (63 downto 32) -> ramKey.readAsync(1),
          (31 downto 0) -> ramKey.readAsync(0))

        data := ((127 downto 96) -> ramData.readAsync(3),
          (95 downto 64) -> ramData.readAsync(2),
          (63 downto 32) -> ramData.readAsync(1),
          (31 downto 0) -> ramData.readAsync(0))

        accelEncrypt := True
      }

      when(funcAesLoadResult) {
        accelLoadResult := True
        internalAddr := io.cmd.instr.rs1.asUInt.resize(2)
      }

      done := !io.cmd.valid
    }
  }.otherwise {

    // Write key to internal RAM
    when(accelStoreKey) {
      ramKey.write(internalAddr, internalWriteData)
      accelStoreKey := False
      flushResp := True
      done := True
    }

    // Write data to internal RAM
    when(accelStoreData) {
      ramData.write(internalAddr, internalWriteData)
      accelStoreData := False
      flushResp := True
      done := True
    }

    // Wait for AES response
    when(flushAesResp) {
      ramResult.write(3, aes.io.crypto.rsp.payload.block(127 downto 96))
      ramResult.write(2, aes.io.crypto.rsp.payload.block(95 downto 64))
      ramResult.write(1, aes.io.crypto.rsp.payload.block(63 downto 32))
      ramResult.write(0, aes.io.crypto.rsp.payload.block(31 downto 0))

      flushResp := True
      done := True
      accelEncrypt := False
    }

    // Load result into register file
    when(accelLoadResult) {
      result := ramResult.readAsync(internalAddr)
      accelLoadResult := False
      flushResp := True
      done := True
    }
  }

  io.resp.data := result
}