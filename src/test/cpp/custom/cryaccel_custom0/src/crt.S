.globl _start
_start:

#define r_type_insn(_f7, _rs2, _rs1, _f3, _rd, _opc) \
.word (((_f7) << 25) | ((_rs2) << 20) | ((_rs1) << 15) | ((_f3) << 12) | ((_rd) << 7) | ((_opc) << 0))

#define aes_load_key(_rd, _rs1 ) \
r_type_insn(0b0000001, 0b000, _rs1, 0b000, _rd, 0b0001011)

#define aes_load_data(_rd, _rs1 ) \
r_type_insn(0b0000010, 0b000, _rs1, 0b001, _rd, 0b0001011)

#define aes_enc(_rd, _rs1, _rs2 ) \
r_type_insn(0b0000011, _rs2, _rs1, 0b000, _rd, 0b0001011)

// Load key and data to accelerator
# key = 0x2b7e1516 0x28aed2a6 0xabf71588 0x09cf4f3c
# data = 0x6bc1bee2 0x2e409f96 0xe93d7e11 0x7393172a

// Load key and data to 128 bit registers
    li x1, 0x2b7e1516
    aes_load_key(3, 1)

    li x1, 0x28aed2a6
    aes_load_key(2, 1)

    li x1, 0xabf71588
    aes_load_key(1, 1)


    li x1, 0x09cf4f3c
    aes_load_key(0, 1)

    li x1,  0x6bc1bee2
    aes_load_data(3, 1)

    li x1,  0x2e409f96     
    aes_load_data(2, 1)

    li x1,  0xe93d7e11 
    aes_load_data(1, 1)

    li x1,  0x7393172a
    aes_load_data(0, 1)

    aes_enc(2,1,3)


//Test 1
//    li x28, 1
//    custom0(1, 0, 0)
//    bne x1, x0, fail

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
