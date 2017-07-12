## See LICENSE file for license details

set LIP_PATH /home/alexander/work/activecore/prj/lip
load [file join $LIP_PATH activecore/cpp/Release/libactivecore.so]
source [file join $LIP_PATH activecore activecore.tcl]

#ActiveCore::debug_set
ActiveCore_Reset

source dlx.tcl

set filename dlx.v

rtl::monitor debug_rtl.txt
pipe::monitor debug_pipe.txt

ActiveCore::export verilog $filename
