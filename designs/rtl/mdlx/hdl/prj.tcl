## See LICENSE file for license details

set MLIP_PATH /home/alexander/work/activecore/prj/mlip
load [file join $MLIP_PATH activecore/cpp/Release/libactivecore.so]
source [file join $MLIP_PATH activecore activecore.tcl]

set MNUM 	9
set SNUM 	9
set START_ADDR 0x80000000
set STRIDE [expr 1024 * 1024]

#ActiveCore::debug_set
ActiveCore_Reset

source ../../../activecore/xbar/xbar_nobuf/xbar_nobuf.tcl

xbar_nobuf::reset

xbar_nobuf::set_mnum $MNUM
xbar_nobuf::set_snum $SNUM

for {set idx 0} {$idx < $SNUM} {incr idx} {
	xbar_nobuf::add_slave $START_ADDR $STRIDE
	set START_ADDR [expr $START_ADDR + $STRIDE]
}

xbar_nobuf::generate

set filename xbar_nobuf.v

rtl::monitor debug_rtl.txt
pipe::monitor debug_pipe.txt

ActiveCore::export verilog $filename
