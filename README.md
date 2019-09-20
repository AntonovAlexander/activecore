# ActiveCore

### Project description

ActiveCore is a framework that demonstrates original hardware designing concept based on "Micro-Language IP" (MLIP) cores.

MLIP core is a hardware generator that exposes selective functions of behavior and/or microarchitecture for design-time programming and generates hardware implementations according to certain microarchitectural template. MLIP core approach serves as an intermediate solution for codification of custom microarchitectures between configurable IP cores with fixed functionality and general-purpose HW design tools:

Fixed-function IP core <------ MLIP core ------> General-purpose design tool

### Project structure

Current version of project is implemented as several Kotlin libraries that are separately built using IntelliJ IDEA in the following order:

* **hwast** - AST constructor for behavioral HW specifications (/hwast)

* MLIP cores based on hwast (/mlip):

	* **rtl** - generator of behavioral RTL

	* **cyclix** (**cycli**c e**x**ecution) - generator of statically scheduled cycle-oriented processing hardware targeting RTL and HLS flows

	* **pipex** (**pipe**lined e**x**ecution) - generator of dynamically scheduled pipelined structures

* core generators based on pipex MLIP core (/designs/coregen):

	* **aquaris** - RISC-V CPU generator with varying-length pipelines (1-6 stages)

	* **ariele** - full xbar generator

The following demo designs for FPGA are available:

* **pss_memsplit** - minimalistic uC with one RISC-V core (/designs/rtl/pss_memsplit). Tests are run by /designs/rtl/pss_memsplit/sw/benchmarks/hw_test.py.

* **mpss** - MPSoC with multiple RISC-V cores connected by full xbar (/designs/rtl/mpss)

Preliminary build of the cores and software is required. Demo projects use UART-controllable bus master for reset and initialization.

### Published works

* A. Antonov, “Methods and Tools for Computer-Aided Synthesis of Processors Based on Microarchitectural Programmable Hardware Generators,” Ph.D dissertation, ITMO University, Saint-Petersburg, 28.12.2018. Thesis web page: http://fppo.ifmo.ru/dissertation/?number=63419

* A. Antonov, P. Kustarev, S. Bykovskii, "Methods of Computational Process Scheduling for Synthesis of Hardware Microarchitecture", in Proc. 19th International Multidisciplinary Scientific GeoConference, SGEM 2019

* A. Antonov, P. Kustarev, S. Bikovsky, "Improving Microarchitecture Design and Hardware Generation using Micro-Language IP Cores", in Proc. IEEE Nordic Circuits and Systems Conference (NORCAS) / NORCHIP and International Symposium of System-on-Chip (SoC) - 2017, pp. 1-6

* A. Antonov, P. Kustarev, "DSL-based approach to hardware pipelines design", in Proc. 17th International Multidisciplinary Scientific GeoConference, SGEM 2017 - 2017, Vol. 17, No. 21, pp. 287-294

For questions, please contact antonov.alex.alex@gmail.com
