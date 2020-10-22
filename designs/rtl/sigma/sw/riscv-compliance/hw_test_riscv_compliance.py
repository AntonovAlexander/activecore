# -*- coding:utf-8 -*-
from __future__ import division

import sys
sys.path.append('../../../../../rtl/udm/sw')

import time

import udm
from udm import *

sys.path.append('..')
import sigma
from sigma import *

def hw_test_riscv_compliance_ADD(sigma, firmware_filename):
    
    verify_data = [
        0x00000000,
        0xfffff802,
        0xffffffff,
        0xfffff5cb,
        0x80000000,
        0x00001a34,
        0x07654320,
        0x80000000,
        0x80000000,
        0x07654320,
        0x00001a34,
        0x80000000,
        0xfffff5cb,
        0xfffffffe,
        0xfffff802,
        0x00000000,
        0xffffffff,
        0xfffff802,
        0xffffffff,
        0xfffff5cb,
        0x80000000,
        0x00001a34,
        0x07654320,
        0x80000000,
        0x80000000,
        0x07654320,
        0x00001a34,
        0x80000000,
        0xfffff5cb,
        0xfffffffe,
        0xfffff802,
        0x00000000,
        0xffffffff,
        0xffffffff,
        0xffffffff,
        0x00000000
	]
    
    return sigma.hw_test_generic(sigma, "ADD", firmware_filename, 1, verify_data)

def hw_test_riscv_compliance_ADDI(sigma, firmware_filename):
    
    verify_data = [
        0x00000000,
        0xfffff802,
        0xffffffff,
        0xffffffff,
        0xfffff800,
        0x00000000,
        0x07653b21,
        0x80000000,
        0xfffff801,
        0xfffff7ff,
        0x00000a34,
        0x80000000,
        0xfffff5cb,
        0xfffffffe,
        0xfffff802,
        0x00000000,
        0xffffffff,
        0xfffff802,
        0xffffffff,
        0xffffffff,
        0xfffff800,
        0x00000000,
        0x07653b21,
        0x80000000,
        0xfffff801,
        0xfffff7ff,
        0x00000a34,
        0x80000000,
        0xfffff5cb,
        0xfffffffe,
        0xfffff802,
        0x00000000,
        0xffffffff,
        0xffffffff,
        0xffffffff,
        0x00000000
	]
    
    return sigma.hw_test_generic(sigma, "ADDI", firmware_filename, 1, verify_data)

def hw_test_riscv_compliance_JALR(sigma, firmware_filename):
    
    verify_data = [
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x0000cccc,
        0x00000000,
        0xffffffff,
        0xffffffff,
        0xffffffff,
        0x00000000
	]
    
    return sigma.hw_test_generic(sigma, "JALR", firmware_filename, 1, verify_data)

def hw_test_riscv_compliance(sigma):
    
    print("#################################################################################")
    print("############################ RISC-V Compliance Test #############################")
    print("#### Imperas Software Ltd., 2019 <https://github.com/riscv/riscv-compliance> ####")
    print("#################################################################################")
    print("")
    
    test_succ_counter = 0
    test_fail_counter = 0
    
    if (hw_test_riscv_compliance_ADD(sigma, "riscv-compliance/I-ADD-01.riscv") == 1):
        test_succ_counter = test_succ_counter + 1
    else:
        test_fail_counter = test_fail_counter + 1
    
    if (hw_test_riscv_compliance_ADDI(sigma, "riscv-compliance/I-ADDI-01.riscv") == 1):
        test_succ_counter = test_succ_counter + 1
    else:
        test_fail_counter = test_fail_counter + 1
    
    if (hw_test_riscv_compliance_JALR(sigma, "riscv-compliance/I-JALR-01.riscv") == 1):
        test_succ_counter = test_succ_counter + 1
    else:
        test_fail_counter = test_fail_counter + 1
    
    print("Total tests PASSED: ", test_succ_counter, ", FAILED: ", test_fail_counter)
    print("")
    print("#################################################################################")
    print("")
