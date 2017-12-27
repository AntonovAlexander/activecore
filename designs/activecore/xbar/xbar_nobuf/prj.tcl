## See LICENSE file for license details

set MLIP_PATH /home/alexander/work/activecore/prj/mlip
load [file join $MLIP_PATH activecore/cpp/Release/libactivecore.so]
source [file join $MLIP_PATH activecore activecore.tcl]

set MNUM 	4
set SNUM 	4
set START_ADDR 0x00000000
set STRIDE 0x40000000

#ActiveCore::debug_set
ActiveCore_Reset

source pavana_xbar.tcl

pavana_xbar::reset

pavana_xbar::set_mnum $MNUM
pavana_xbar::set_snum $SNUM

for {set idx 0} {$idx < $SNUM} {incr idx} {
	pavana_xbar::add_slave $START_ADDR $STRIDE
	set START_ADDR [expr $START_ADDR + $STRIDE]
}

pavana_xbar::generate

set filename pavana_xbar.v

rtl::monitor debug_rtl.txt
pipe::monitor debug_pipe.txt

ActiveCore::export verilog $filename
