# ActiveCore
*Making complex RTL designing rapid, fun and full of insights*

### Project description

ActiveCore is a framework that demonstrates original hardware designing concept based on **"Micro-Language IP"/"Microarchitecture-Level IP" (MLIP) cores**.

MLIP core is a hardware generator that provides custom synthesizable execution kernel constructed in accordance to certain microarchitectural template. Selective functions of the microarchitecture are exposed for design-time behavioral-style programming, with scheduling, communication and synchronization "services" of the microarchitecture manageable using MLIP-specific API. I.e., each MLIP core offers custom computational model that reflects computational process organization inside a hardware microarchitecture.

MLIP core approach serves as intermediate solution for codification of custom microarchitectures between configurable IP cores with fixed functionality and general-purpose HW design tools:

Fixed-function IP core <------ MLIP core ------> General-purpose HW design tool

The ultimate goal of the project is to provide top-down methodology and reusable components for explicit allocation of intermediate ***“microarchitectural middleware”*** design level for complex hardware. This design level (inspired by OS, VM, and various middleware in software stacks) incorporates expert knowledge about cross-cutting internal mechanisms of custom microarchitectures, addressing:
* rapid prototyping of various-purpose IP blocks with common microarchitectural mechanisms;
* diversification of responsibility and competence of IP engineers: implementation of key microarchitectural mechanisms can be charged to the core of leading engineers, while developers of final products can focus solely on application-specific functionality;
* facilitating IP design centers to package, reuse and deliver complex hardware templates with programmable microarchitectural mechanisms as basis for semi-custom IP cores.

### Project structure

Current version of project is implemented as a collection of Kotlin libraries that are individually built using IntelliJ IDEA in the following order:

* **hwast** - generic AST constructor for behavioral HW specifications (/hwast)

* Demo MLIP cores based on hwast (/mlip):

	* **rtl** - generator of behavioral RTL. Exports to SystemVerilog HDL

	* **cyclix** (**cycli**c e**x**ecution) - generator of hardware performing cyclic statically scheduled computations. Translates either to synchronous RTL for rtl MLIP or to C++ sources for Xilinx HLS

	* **pipex** (**pipe**lined e**x**ecution) - generator of hardware with dynamically scheduled scalar in-order pipelined microarchitecture. Supports inter-stage communication and pipelined I/O synchronization features. Translates to cyclix MLIP

	* **reordex** (**reorde**red e**x**ecution) - generator of co-processors with superscalar out-of-order (OoO) microarchitecture and register renaming. Translates to cyclix MLIP

* Demo core generators based on MLIP cores (/designs/coregen):

	* **aquaris** - RISC-V CPU generator with varying-length pipelines (RV32I, 1-6 pipeline stages), based on pipex MLIP core

	* **ariele** - full xbar generator, based on pipex MLIP core

	* **citadel** - OoO FPU coprocessor, based on reordex MLIP core

* Auxiliary reusable cores:

	* **UDM** - bus transactor controlled via UART interface. Supports bursts and bus timeouts. Communication library for Python 3 included. Reference lab work manual included. Location: /designs/rtl/udm

	* **sigma_tile** - basic CPU tile consisting of a single aquaris RISC-V core, tightly coupled scratchpad RAM with single-cycle delay, interrupt controller, timer, Host InterFace (HIF), and eXpansion InterFace (XIF). HIF and XIF protocols are equivalent to UDM bus protocol. Location: /designs/rtl/sigma_tile

* Demo FPGA-based SoCs:

	* **Sigma** - basic MCU consisting of a single sigma_tile module, UDM, and GPIO controller. Reference lab work manual included. Location: /designs/rtl/sigma

	* **Magma** - NUMA MPSoC consisting of multiple sigma_tile modules connected by ariele xbar. Location: /designs/rtl/magma

### Publications

* A. Antonov, “Methods and Tools for Computer-Aided Synthesis of Processors Based on Microarchitectural Programmable Hardware Generators,” Ph.D dissertation, ITMO University, Saint-Petersburg, 28.12.2018. URL: http://fppo.ifmo.ru/dissertation/?number=63419

* A. Antonov, P. Kustarev, “Strategies of Computational Process Synthesis – a System-Level Model of HW/SW (Micro)Architectural Mechanisms,” in 2020 9th Mediterranean Conference on Embedded Computing (MECO), 2020. URL: https://ieeexplore.ieee.org/document/9134071 Preprint downloadable from: http://programme.meconet.me/documents/PAPERS/MECO_2020_paper_26.pdf

* A. Antonov, P. Kustarev, S. Bikovsky, “MLIP Cores: Designing Hardware Generators with Programmable Microarchitectural Mechanisms,” in 2020 IEEE International Symposium on Circuits and Systems (ISCAS), 2020. URL: https://ieeexplore.ieee.org/document/9180593

* A. Antonov, P. Kustarev, S. Bikovsky, "Improving Microarchitecture Design and Hardware Generation using Micro-Language IP Cores", in Proc. IEEE Nordic Circuits and Systems Conference (NORCAS) / NORCHIP and International Symposium of System-on-Chip (SoC) - 2017, pp. 1-6. URL: https://ieeexplore.ieee.org/document/8124952

* A. Antonov, P. Kustarev, S. Bykovskii, "Methods of Computational Process Scheduling for Synthesis of Hardware Microarchitecture", in Proc. 19th International Multidisciplinary Scientific GeoConference, SGEM 2019 - 2019, Vol. 19, No. 2.1, pp. 445-452

* A. Antonov, P. Kustarev, "DSL-based approach to hardware pipelines design", in Proc. 17th International Multidisciplinary Scientific GeoConference, SGEM 2017 - 2017, Vol. 17, No. 21, pp. 287-294

* A. Аntonov, "Design of Computer Microarchitecture Basing on Problem-Oriented Languages", Journal of Instrument Engineering, 2017, Vol. 60, No. 10, pp. 980—985. URL: http://pribor.ifmo.ru/en/article/17220/proektirovanie_mikroarhitektury_vychisliteley_na_baze_problemno-orientirovannyh_yazykov.htm
