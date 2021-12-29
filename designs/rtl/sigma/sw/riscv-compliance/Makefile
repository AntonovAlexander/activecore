#=======================================================================
# UCB VLSI FLOW: Makefile for riscv-isa_tests
#-----------------------------------------------------------------------
# Yunsup Lee (yunsup@cs.berkeley.edu)
#

XLEN ?= 64

default: all

src_dir = .

#--------------------------------------------------------------------
# Sources
#--------------------------------------------------------------------

rv32i_tests = \
	I-ADD-01 \
	I-ADDI-01 \
	I-AND-01 \
	I-ANDI-01 \
	I-AUIPC-01 \
	I-BEQ-01 \
	I-BGE-01 \
	I-BGEU-01 \
	I-BLT-01 \
	I-BLTU-01 \
	I-BNE-01 \
	I-JAL-01 \
	I-JALR-01 \
	I-LB-01 \
	I-LBU-01 \
	I-LH-01 \
	I-LHU-01 \
	I-LUI-01 \
	I-LW-01 \
	I-OR-01 \
	I-ORI-01 \
	I-SB-01 \
	I-SH-01 \
	I-SLL-01 \
	I-SLLI-01 \
	I-SLT-01 \
	I-SLTI-01 \
	I-SLTIU-01 \
	I-SLTU-01 \
	I-SRA-01 \
	I-SRAI-01 \
	I-SRL-01 \
	I-SRLI-01 \
	I-SUB-01 \
	I-SW-01 \
	I-XOR-01 \
	I-XORI-01 \
	I-DELAY_SLOTS-01 \
	I-EBREAK-01 \
	I-ECALL-01 \
	I-ENDIANESS-01 \
	I-IO-01 \
	I-MISALIGN_JMP-01 \
	I-MISALIGN_LDST-01 \
	I-NOP-01 \
	I-RF_size-01 \
	I-RF_width-01 \
	I-RF_x0-01

rv32m_tests = \
	mul-01 \
	mulh-01 \
	mulhsu-01 \
	mulhu-01 \
	div-01 \
	divu-01 \
	rem-01 \
	remu-01

isa_tests = $(rv32i_tests) $(rv32m_tests)

#--------------------------------------------------------------------
# Build rules
#--------------------------------------------------------------------

RISCV_PREFIX ?= riscv$(XLEN)-unknown-elf-
RISCV_GCC ?= $(RISCV_PREFIX)gcc
RISCV_GCC_OPTS ?= -mcmodel=medany -static -std=gnu99 -O2 -march=rv32im -mabi=ilp32
RISCV_LINK ?= $(RISCV_GCC) -T $(src_dir)/../common/sigma.ld $(incs)
RISCV_LINK_OPTS ?= -static -nostdlib -nostartfiles -lgcc -T $(src_dir)/../common/sigma.ld
RISCV_OBJDUMP ?= $(RISCV_PREFIX)objdump --disassemble-all --disassemble-zeroes -h

incs  += -I$(src_dir)/../env -I$(src_dir)/../common -I$(src_dir)/riscv-test-env -I$(src_dir)/riscv-target
objs  :=

define rv32i_compile_template
$(1).riscv: $(wildcard $(src_dir)/$(1)/*) $(wildcard $(src_dir)/../common/*)
	$$(RISCV_GCC) $$(incs) $$(RISCV_GCC_OPTS) $$(RISCV_LINK_OPTS) -o $$@ $(wildcard $(src_dir)/riscv-test-suite/rv32i/src/$(1).S)
	hexdump -ve '/4 "%08x" "\n"' $(1).riscv > $(1).riscv.hex
endef

define rv32m_compile_template
$(1).riscv: $(wildcard $(src_dir)/$(1)/*) $(wildcard $(src_dir)/../common/*)
	$$(RISCV_GCC) $$(incs) $$(RISCV_GCC_OPTS) $$(RISCV_LINK_OPTS) -o $$@ $(wildcard $(src_dir)/riscv-test-suite/rv32m/src/$(1).S)
	hexdump -ve '/4 "%08x" "\n"' $(1).riscv > $(1).riscv.hex
endef

$(foreach isa_test,$(rv32i_tests),$(eval $(call rv32i_compile_template,$(isa_test))))
$(foreach isa_test,$(rv32m_tests),$(eval $(call rv32m_compile_template,$(isa_test))))

#------------------------------------------------------------
# Build and run benchmarks on riscv simulator

isa_tests_riscv_bin  = $(addsuffix .riscv,  $(isa_tests))
isa_tests_riscv_dump = $(addsuffix .riscv.dump, $(isa_tests))
isa_tests_riscv_out  = $(addsuffix .riscv.out,  $(isa_tests))
isa_tests_riscv_hex  = $(addsuffix .riscv.hex,  $(isa_tests))

$(isa_tests_riscv_dump): %.riscv.dump: %.riscv
	$(RISCV_OBJDUMP) $< > $@

$(isa_tests_riscv_out): %.riscv.out: %.riscv
	$(RISCV_SIM) $< > $@

riscv: $(isa_tests_riscv_dump)
run: $(isa_tests_riscv_out)

junk += $(isa_tests_riscv_bin) $(isa_tests_riscv_dump) $(isa_tests_riscv_hex) $(isa_tests_riscv_out)

#------------------------------------------------------------
# Default

all: riscv

#------------------------------------------------------------
# Clean up

clean:
	rm -rf $(objs) $(junk)
