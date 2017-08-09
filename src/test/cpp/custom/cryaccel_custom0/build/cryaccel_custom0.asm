
build/cryaccel_custom0.elf:     file format elf32-littleriscv


Disassembly of section .crt_section:

00000000 <_start>:
   0:	00100e13          	li	t3,1
   4:	0600008b          	0x600008b

00000008 <fail>:
   8:	f0100137          	lui	sp,0xf0100
   c:	f2410113          	addi	sp,sp,-220 # f00fff24 <pass+0xf00fff10>
  10:	01c12023          	sw	t3,0(sp)

00000014 <pass>:
  14:	f0100137          	lui	sp,0xf0100
  18:	f2010113          	addi	sp,sp,-224 # f00fff20 <pass+0xf00fff0c>
  1c:	00012023          	sw	zero,0(sp)
  20:	00000013          	nop
  24:	00000013          	nop
  28:	00000013          	nop
  2c:	00000013          	nop
  30:	00000013          	nop
  34:	00000013          	nop
