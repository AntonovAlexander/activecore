## See LICENSE file for license details

set MLIP_PATH /home/alexander/work/activecore/mlip
load [file join $MLIP_PATH activecore/cpp/Release/libactivecore.so]
source [file join $MLIP_PATH activecore activecore.tcl]

set MNUM 		9
set TILE_NUM 	8
set SNUM 		[expr $TILE_NUM + 1]

set TILE_ADDR 0x80000000
set TILE_SIZE [expr 1024 * 1024]

set PERIPH_ADDR 0xC0000000
set PERIPH_SIZE [expr 1024 * 1024]

#ActiveCore::debug_set
ActiveCore_Reset

source ../../../activecore/xbar/xbar_buffered/xbar_buffered.tcl

xbar_buffered::reset

xbar_buffered::set_mnum $MNUM
xbar_buffered::set_snum $SNUM

for {set idx 0} {$idx < $TILE_NUM} {incr idx} {
	xbar_buffered::add_slave $TILE_ADDR $TILE_SIZE
	set TILE_ADDR [expr $TILE_ADDR + $TILE_SIZE]
}
xbar_buffered::add_slave $PERIPH_ADDR $PERIPH_SIZE

xbar_buffered::generate

set filename xbar_buffered.v

rtl::monitor debug_rtl.txt
pipe::monitor debug_pipe.txt

ActiveCore::export verilog $filename
