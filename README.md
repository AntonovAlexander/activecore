ActiveCore is a temporary working name for MLIP-enabled framework prototype and may change in the future.

At the moment, GPLC component is distributed as open-source, while rtl and pipe MLIPs are distributed as static compiled libraries.

MLIP components are located at /mlip.
Test designs are located at /designs. MLIP-based components are located at activecore, RTL sources are located at rtl.

Pipe MLIP functionality is demonstrated via 6 RISC-V (RV32I) CPU designs with variable-length pipelines (riscv_1stage-riscv_6stage).
MLIP-based codes for RISC-V CPU cores are located at /designs/activecore/riscv.
All the cores function in hardware (however, exaustive testing is in progress).

Previously, mainline working projects were based on CPUs with DLX architecture (codes available, but not supported now).

At the moment, I use Eclipse IDE to build the C++ shared library. Shell-based build flow will be provided later.

Test projects for CPUs are located at /designs/rtl/riscv_xstage_udm_memsplit. The projects need preliminary build of CPU cores using the framework. The projects use UART-controllable master for reset and initialization (tests are launched with /designs/activecore/riscv/sw/benchmarks/hw_test.py).

For questions, please contact antonov.alex.alex@gmail.com
