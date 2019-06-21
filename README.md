# ActiveCore

### Project description

ActiveCore is a framework that demonstrates hardware designing concept based on "Micro-Language IP" (MLIP) cores.

MLIP core is a hardware generator that exposes selective functions of behavior and/or microarchitecture for design-time programming and generates hardware implemenations according to certain microarchitectural template.

Full description of the concept and preliminary version of the project (based on C++/Tcl) is given in my PhD thesis:

* A. Antonov, “Methods and Tools for Computer-Aided Synthesis of Processors Based on Microarchitectural Programmable Hardware Generators,” Ph.D dissertation, ITMO University, Saint-Petersburg, 28.12.2018.

Thesis web page: http://fppo.ifmo.ru/dissertation/?number=63419

### Project structure

Current version of project is implemented as several Kotlin libraries that are separately built using IntelliJ IDEA in the following order:

* hwast - AST constructor for behavioral HW specifications (/hwast)

* rtl - MLIP core for behavioral RTL generation (/mlip/rtl)

* cyclix - MLIP core for generation of cycle-oriented processing hardware targeting RTL and HLS flows (/mlip/cyclix)

* pipex - MLIP core for pipelined structures generation (/mlip/pipex)

* core generators

	* RISC-V CPU generator (/designs/activecore/riscv_pipex)

	* full xbar generator (/designs/activecore/xbar_pipex)

Pipex MLIP functionality is demonstrated via 6 RISC-V (RV32I) CPU designs with varying-length pipelines (riscv_1stage-riscv_6stage) and xbar generator based on it. The following demo designs for FPGA are available:

* pss_memsplit - minimalistic uC with one RISC-V core (/designs/rtl/pss_memsplit). Tests are run by /designs/rtl/pss_memsplit/sw/benchmarks/hw_test.py.

* mpss - SoC with multiple RISC-V cores connected by full xbar (/designs/rtl/mpss)

Preliminary build of the cores and software is required. Demo projects use UART-controllable bus master for reset and initialization.

For questions, please contact antonov.alex.alex@gmail.com
