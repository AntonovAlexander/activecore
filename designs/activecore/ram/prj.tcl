## See LICENSE file for license details

set MLIP_PATH /home/alexander/work/activecore/prj/mlip
load [file join $MLIP_PATH activecore/cpp/Release/libactivecore.so]
source [file join $MLIP_PATH activecore activecore.tcl]

#ActiveCore::debug_set
ActiveCore_Reset

source ram.tcl

set module_name [genmodule_RAM 32 32 0 2 1 false]
set filename $module_name.v

rtl::monitor debug.txt

ActiveCore::export verilog $filename
