#=======================================================================
# UCB VLSI FLOW: Makefile for riscv-bmarks
#-----------------------------------------------------------------------
# Yunsup Lee (yunsup@cs.berkeley.edu)
#

XLEN ?= 64

default: all

src_dir = .

#--------------------------------------------------------------------
# Sources
#--------------------------------------------------------------------

bmarks = \
	heartbeat_variable \
	md5 \
	timer_test \
	mul_sw \
	irq_counter \
	median \
	qsort \
	rsort \
	crc32 \
	bootloader \
	dhrystone \
	#towers \
	#vvadd \
	#multiply \
	#mm \
	#spmv \
	#mt-vvadd \
	#mt-matmul \
	#pmp \

#--------------------------------------------------------------------
# Build rules
#--------------------------------------------------------------------

RISCV_PREFIX ?= riscv$(XLEN)-unknown-elf-
RISCV_GCC ?= $(RISCV_PREFIX)gcc
RISCV_GCC_OPTS ?= -mcmodel=medany -static -std=gnu99 -O2 -march=rv32im -mabi=ilp32
RISCV_LINK ?= $(RISCV_GCC) -T $(src_dir)/../common/sigma.ld $(incs)
RISCV_LINK_OPTS ?= -static -nostartfiles -lgcc -T $(src_dir)/../common/sigma.ld
RISCV_LINK_OPTS_TESTAPP ?= -static -nostartfiles -lgcc -T $(src_dir)/bootloader_testapp/bootloader_testapp.ld
RISCV_OBJDUMP ?= $(RISCV_PREFIX)objdump --disassemble-all --disassemble-zeroes -h

incs  += -I$(src_dir)/../env -I$(src_dir)/../common $(addprefix -I$(src_dir)/, $(bmarks))
objs  :=

define compile_template
$(1).riscv: $(wildcard $(src_dir)/$(1)/*) $(wildcard $(src_dir)/../common/*)
	$$(RISCV_GCC) $$(incs) $$(RISCV_GCC_OPTS) $$(RISCV_LINK_OPTS) -o $$@ $(wildcard $(src_dir)/$(1)/*.c) $(wildcard $(src_dir)/$(1)/*.S) $(wildcard $(src_dir)/../common/*.c) $(wildcard $(src_dir)/../common/*.S)
	hexdump -ve '/4 "%08x" "\n"' $(1).riscv > $(1).riscv.hex
endef

define compile_template_testapp
$(1).riscv: $(wildcard $(src_dir)/$(1)/*) $(wildcard $(src_dir)/../common/*)
	$$(RISCV_GCC) $$(incs) $$(RISCV_GCC_OPTS) $$(RISCV_LINK_OPTS_TESTAPP) -o $$@ $(wildcard $(src_dir)/$(1)/*.c) $(wildcard $(src_dir)/$(1)/*.S) $(wildcard $(src_dir)/../common/*.c) $(wildcard $(src_dir)/../common/*.S)
	hexdump -ve '/4 "%08x" "\n"' $(1).riscv > $(1).riscv.hex
endef

$(foreach bmark,$(bmarks),$(eval $(call compile_template,$(bmark))))
$(eval $(call compile_template_testapp,bootloader_testapp))

#------------------------------------------------------------
# Build and run benchmarks on riscv simulator

bmarks_riscv_bin  = $(addsuffix .riscv,      $(bmarks) bootloader_testapp)
bmarks_riscv_dump = $(addsuffix .riscv.dump, $(bmarks) bootloader_testapp)
bmarks_riscv_out  = $(addsuffix .riscv.out,  $(bmarks) bootloader_testapp)
bmarks_riscv_hex  = $(addsuffix .riscv.hex,  $(bmarks) bootloader_testapp)

$(bmarks_riscv_dump): %.riscv.dump: %.riscv
	$(RISCV_OBJDUMP) $< > $@

$(bmarks_riscv_out): %.riscv.out: %.riscv
	$(RISCV_SIM) $< > $@

riscv: $(bmarks_riscv_dump)
run: $(bmarks_riscv_out)

junk += $(bmarks_riscv_bin) $(bmarks_riscv_dump) $(bmarks_riscv_hex) $(bmarks_riscv_out)

#------------------------------------------------------------
# Default

all: riscv

#------------------------------------------------------------
# Clean up

clean:
	rm -rf $(objs) $(junk)
