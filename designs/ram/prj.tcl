## See LICENSE file for license details

set LIP_PATH /home/alexander/work/activecore/prj/lip
load [file join $LIP_PATH activecore/cpp/Release/libactivecore.so]
source [file join $LIP_PATH activecore activecore.tcl]

#ActiveCore::debug_set
ActiveCore_Reset

source ram.tcl

set module_name [genmodule_RAM 32 32 0 2 1 false]
set filename $module_name.v

rtl::monitor debug.txt

ActiveCore::export verilog $filename
