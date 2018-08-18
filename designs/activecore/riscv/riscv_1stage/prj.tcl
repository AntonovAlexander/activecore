## See LICENSE file for license details

set MLIP_PATH /home/alexander/work/activecore/mlip
load [file join $MLIP_PATH activecore/cpp/Release/libactivecore.so]
source [file join $MLIP_PATH activecore activecore.tcl]

#ActiveCore::debug_set
ActiveCore_Reset

source ../riscv_pipe.tcl
riscv_pipe::generate 1

pipe::translate
pipe::export rtl sverilog sverilog
pipe::export hls vivado_cpp vivado_cpp
