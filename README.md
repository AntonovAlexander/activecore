ActiveCore is a temporary working name for "Micro-Language IP"(MLIP)-enabled framework prototype and may change in the future.

Full description of the project is available in my PhD thesis "Methods and Tools for Computer-Aided Synthesis of Processors Based on Microarchitectural Programmable Hardware Generators"

Thesis web page: http://fppo.ifmo.ru/dissertation/?number=63419

Current version of project is implemented as Kotlin libraries and built using IntelliJ IDEA in the following order:

* hwast - AST constructor for behavioural HW specifications

* rtl - RTL generator

* cyclix - cyclic processors generator for RTL and HLS flows

* pipex - pipelined structures generator

* core generators

The following demo designs for FPGA are available:

* pss_memsplit - minimalistic uC with one RISC-V core

* mpss - SoC with multiple RISC-V cores and xbar

Pipex MLIP functionality is demonstrated via 6 RISC-V (RV32I) CPU designs with variable-length pipelines (riscv_1stage-riscv_6stage). RISC-V CPU core generator is located at /designs/activecore/riscv_pipex. Preliminary build of core and software is required. Demo projects use UART-controllable master for reset and initialization (tests for pss_memsplit are launched by /designs/rtl/pss_memsplit/sw/benchmarks/hw_test.py).

For questions, please contact antonov.alex.alex@gmail.com
