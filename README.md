ActiveCore is a temporary working name for MLIP-enabled framework prototype and may change in the future.

At the moment, GPLC component is distributed as open-source, while rtl and pipe MLIPs are distributed as static compiled libraries to demonstrate MLIP protection capabilities.

MLIP components are located at /mlip.
Test designs are located at /designs. MLIP-based components are located at activecore, RTL sources are located at rtl.

MLIP-based codes for DLX CPU core are located at activecore/dlx.
There are two versions of the CPU core: dlx_mem1cycle and dlx_memsplit.

dlx_mem1cycle is a simplier core that uses strictly 1-cycle memory for instructions and data.

dlx_memsplit uses split-transfer bus and the new "mcopipe" interaction/synchronization primitive.

Both cores function in hardware (however, exaustive testing is in progress).

At the moment, I use Eclipse IDE to build the C++ shared library. Shell-based build flow will be provided later.

Test projects for DLX processor is located at rtl/dlx_udm and rtl/dlx_udm_memsplit. The projects need preliminary build of DLX core using the framework.
