# ActiveCore

### Project description

ActiveCore is a framework that demonstrates original hardware designing concept based on "Micro-Language IP" (MLIP) cores.

MLIP core is a hardware generator that provides custom synthesizable execution kernel associated with certain microarchitectural template. Selective functions of the microarchitecture are exposed for design-time behavioral-style programming, with scheduling, communication and synchronization "services" of the microarchitecture manageable using MLIP-specific API. I.e., each MLIP core offers custom computational model that reflects computational process organization inside a hardware microarchitecture.

MLIP core approach serves as intermediate solution for codification of custom microarchitectures between configurable IP cores with fixed functionality and general-purpose HW design tools:

Fixed-function IP core <------ MLIP core ------> General-purpose HW design tool

The aim of the project is to provide methodology and reusable components for explicit allocation of intermediate *“microarchitectural middleware”* design level for complex hardware. This design level (inspired by OS, VM, and various middleware in software stacks) incorporates expert knowledge about cross-cutting internal mechanisms of custom microarchitectures, addressing:
* rapid prototyping of various-purpose IP blocks with common microarchitectural mechanisms;
* diversification of responsibility and competence of IP engineers: implementation of key microarchitectural mechanisms can be charged to the core of leading engineers, while developers of final products can focus solely on application-specific functionality;
* enabling IP design centers to package complex hardware templates with selectively programmable microarchitectural mechanisms as standalone deliverables.

### Project structure

Current version of project is implemented as a collection of Kotlin libraries that are individually built using IntelliJ IDEA in the following order:

* **hwast** - generic AST constructor for behavioral HW specifications (/hwast)

* MLIP cores based on hwast (/mlip):

	* **rtl** - generator of behavioral RTL (in SystemVerilog HDL)

	* **cyclix** (**cycli**c e**x**ecution) - generator of statically scheduled cyclic processing hardware targeting RTL and HLS flows. Translates either to synchronous RTL for rtl MLIP or to C++ sources for Vivado HLS

	* **pipex** (**pipe**lined e**x**ecution) - generator of dynamically scheduled scalar in-order pipelined structures. Supports inter-stage communication and pipelined I/O synchronization features. Translates to cyclix MLIP

* core generators based on MLIP cores (/designs/coregen):

	* **aquaris** - RISC-V CPU generator with varying-length pipelines (RV32I, 1-6 pipeline stages), based on pipex MLIP core

	* **ariele** - full xbar generator, based on pipex MLIP core

Other reusable cores:

* **UDM** - bus transactor controlled via UART interface. Supports bursts and bus timeouts. Drivers for Python 3 included. Reference lab work manual included. Location: /designs/rtl/udm

* **sigma_tile** - basic CPU tile consisting of a single aquaris RISC-V core, tightly coupled scratchpad RAM with single-cycle delay, interrupt controller, timer, Host InterFace (HIF), and eXpansion InterFace (XIF). HIF and XIF protocols are equal to UDM bus protocol. Location: /designs/rtl/sigma_tile

Demo FPGA-based SoCs:

* **Sigma** - basic MCU consisting of a single sigma_tile module, UDM, and GPIO controller. Reference lab work manual included. Location: /designs/rtl/sigma

* **Magma** - MPSoC consisting of multiple sigma_tile modules connected by ariele xbar. Location: /designs/rtl/magma

### Publications

* A. Antonov, “Methods and Tools for Computer-Aided Synthesis of Processors Based on Microarchitectural Programmable Hardware Generators,” Ph.D dissertation, ITMO University, Saint-Petersburg, 28.12.2018. URL: http://fppo.ifmo.ru/dissertation/?number=63419

* A. Antonov, P. Kustarev, “Strategies of Computational Process Synthesis – a System-Level Model of HW/SW (Micro)Architectural Mechanisms,” in 2020 9th Mediterranean Conference on Embedded Computing (MECO), 2020. URL: https://ieeexplore.ieee.org/document/9134071

* A. Antonov, P. Kustarev, S. Bikovsky, “MLIP Cores: Designing Hardware Generators with Programmable Microarchitectural Mechanisms,” in 2020 IEEE International Symposium on Circuits and Systems (ISCAS), 2020 (accepted for publication)

* A. Antonov, P. Kustarev, S. Bikovsky, "Improving Microarchitecture Design and Hardware Generation using Micro-Language IP Cores", in Proc. IEEE Nordic Circuits and Systems Conference (NORCAS) / NORCHIP and International Symposium of System-on-Chip (SoC) - 2017, pp. 1-6. URL: https://ieeexplore.ieee.org/document/8124952

* A. Antonov, P. Kustarev, S. Bykovskii, "Methods of Computational Process Scheduling for Synthesis of Hardware Microarchitecture", in Proc. 19th International Multidisciplinary Scientific GeoConference, SGEM 2019 - 2019, Vol. 19, No. 2.1, pp. 445-452

* A. Antonov, P. Kustarev, "DSL-based approach to hardware pipelines design", in Proc. 17th International Multidisciplinary Scientific GeoConference, SGEM 2017 - 2017, Vol. 17, No. 21, pp. 287-294

* A. Аntonov, "Design of Computer Microarchitecture Basing on Problem-Oriented Languages", Journal of Instrument Engineering, 2017, Vol. 60, No. 10, pp. 980—985. URL: http://pribor.ifmo.ru/en/article/17220/proektirovanie_mikroarhitektury_vychisliteley_na_baze_problemno-orientirovannyh_yazykov.htm
