#!/bin/bash

cd riscv_1stage
tclsh prj.tcl | tee debug.txt

cd ../riscv_2stage
tclsh prj.tcl | tee debug.txt

cd ../riscv_3stage
tclsh prj.tcl | tee debug.txt

cd ../riscv_4stage
tclsh prj.tcl | tee debug.txt

cd ../riscv_5stage
tclsh prj.tcl | tee debug.txt

cd ../riscv_6stage
tclsh prj.tcl | tee debug.txt
