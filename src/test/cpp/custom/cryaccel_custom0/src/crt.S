.globl _start
_start:

#define r_type_insn(_f7, _rs2, _rs1, _f3, _rd, _opc) \
.word (((_f7) << 25) | ((_rs2) << 20) | ((_rs1) << 15) | ((_f3) << 12) | ((_rd) << 7) | ((_opc) << 0))

#define aes_store_key(_rdi, _rs1 ) \
r_type_insn(0b0000001, 0b000, _rs1, 0b000, _rdi, 0b0001011)

#define aes_store_data(_rdi, _rs1 ) \
r_type_insn(0b0000010, 0b000, _rs1, 0b001, _rdi, 0b0001011)

#define aes_load_res(_rd, _rs1 ) \
r_type_insn(0b0000011, 0b000, _rs1, 0b000, _rd, 0b0001011)

#define aes_enc \
r_type_insn(0b0000100, 0b00000, 0b00000, 0b000, 0b00000, 0b0001011)

start:
    // Store key and data to internal 128 bit registers
    # key = 0x2b7e1516 0x28aed2a6 0xabf71588 0x09cf4f3c
    # data = 0x6bc1bee2 0x2e409f96 0xe93d7e11 0x7393172a
    li x1, 0x2b7e1516
    aes_store_key(0x3, 1)

    li x1, 0x28aed2a6
    aes_store_key(0x2, 1)

    li x1, 0xabf71588
    aes_store_key(0x1, 1)

    li x1, 0x09cf4f3c
    aes_store_key(0x0, 1)

    li x1,  0x6bc1bee2
    aes_store_data(0x3, 1)

    li x1,  0x2e409f96     
    aes_store_data(0x2, 1)

    li x1,  0xe93d7e11 
    aes_store_data(0x1, 1)

    li x1,  0x7393172a
    aes_store_data(0x0, 1)

    # Start encoding process
    aes_enc

    # Load result into general purpose register
    # result: 0xab74d1cf 0xeddddc1a 0x78bd6163 0xa5f41edb
    # result: 0x3ad77bb4 0x0d7a3660 0xa89ecaf3 0x2466ef97
    aes_load_res(4, 0x3)
    aes_load_res(3, 0x2)
    aes_load_res(2, 0x1)
    aes_load_res(1, 0x0)

    li x5, 0x3ad77bb4
    bne x4, x5, fail

    li x5, 0x0d7a3660     
    bne x3, x5, fail

    
    li x5, 0xa89ecaf3
    bne x2, x5, fail

    li x5, 0x2466ef97
    bne x1, x5, fail

    j pass

fail: //x28 => error code
    li x2, 0xF00FFF24
    sw x28, 0(x2)

pass:
    li x2, 0xF00FFF20
    sw x0, 0(x2)

    nop
    nop
    nop
    nop
    nop
    nop
