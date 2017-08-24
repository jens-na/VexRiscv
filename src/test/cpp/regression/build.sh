#!/bin/bash
set -e

cd ../custom/cryaccel_custom0/
make
cd ../../regression/ 
make clean run IBUS=SIMPLE DBUS=SIMPLE CSR=no MMU=no DEBUG_PLUGIN=no MUL=no DIV=no DHRYSTONE=no REDO=2 CUSTOM_CRYACCEL=yes CUSTOM_SIMD_ADD=yes TRACE=yes
