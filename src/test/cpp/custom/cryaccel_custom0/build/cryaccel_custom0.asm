
build/cryaccel_custom0.elf:     file format elf32-littleriscv


Disassembly of section .crt_section:

00000000 <_start>:
   0:	2b7e10b7          	lui	ra,0x2b7e1
   4:	51608093          	addi	ra,ra,1302 # 2b7e1516 <pass+0x2b7e14a6>
   8:	0200818b          	0x200818b
   c:	28aed0b7          	lui	ra,0x28aed
  10:	2a608093          	addi	ra,ra,678 # 28aed2a6 <pass+0x28aed236>
  14:	0200810b          	0x200810b
  18:	abf710b7          	lui	ra,0xabf71
  1c:	58808093          	addi	ra,ra,1416 # abf71588 <pass+0xabf71518>
  20:	0200808b          	0x200808b
  24:	09cf50b7          	lui	ra,0x9cf5
  28:	f3c08093          	addi	ra,ra,-196 # 9cf4f3c <pass+0x9cf4ecc>
  2c:	0200800b          	0x200800b
  30:	6bc1c0b7          	lui	ra,0x6bc1c
  34:	ee208093          	addi	ra,ra,-286 # 6bc1bee2 <pass+0x6bc1be72>
  38:	0400918b          	0x400918b
  3c:	2e40a0b7          	lui	ra,0x2e40a
  40:	f9608093          	addi	ra,ra,-106 # 2e409f96 <pass+0x2e409f26>
  44:	0400910b          	0x400910b
  48:	e93d80b7          	lui	ra,0xe93d8
  4c:	e1108093          	addi	ra,ra,-495 # e93d7e11 <pass+0xe93d7da1>
  50:	0400908b          	0x400908b
  54:	739310b7          	lui	ra,0x73931
  58:	72a08093          	addi	ra,ra,1834 # 7393172a <pass+0x739316ba>
  5c:	0400900b          	0x400900b
  60:	0630810b          	0x630810b

00000064 <fail>:
  64:	f0100137          	lui	sp,0xf0100
  68:	f2410113          	addi	sp,sp,-220 # f00fff24 <pass+0xf00ffeb4>
  6c:	01c12023          	sw	t3,0(sp)

00000070 <pass>:
  70:	f0100137          	lui	sp,0xf0100
  74:	f2010113          	addi	sp,sp,-224 # f00fff20 <pass+0xf00ffeb0>
  78:	00012023          	sw	zero,0(sp)
  7c:	00000013          	nop
  80:	00000013          	nop
  84:	00000013          	nop
  88:	00000013          	nop
  8c:	00000013          	nop
  90:	00000013          	nop
