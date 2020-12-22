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

def hw_test_riscv_compliance_template(sigma, instr_name):
    
    f = open("riscv-compliance/references/I-" + instr_name + "-01.reference_output", "r")

    verify_data = []
    
    while True:
    	verify_dataword = f.readline()
    	if (verify_dataword):
    		verify_data.append(int(verify_dataword, 16))
    	else:
    		break
    
    return sigma.hw_test_generic(sigma, instr_name, "riscv-compliance/I-" + instr_name + "-01.riscv", 1, verify_data)


def hw_test_riscv_compliance(sigma):
    
    print("#################################################################################")
    print("############################ RISC-V Compliance Test #############################")
    print("#### Imperas Software Ltd., 2019 <https://github.com/riscv/riscv-compliance> ####")
    print("#################################################################################")
    print("")
    
    test_succ_counter = 0
    test_fail_counter = 0
    
    TESTS = ["ADD",
             "ADDI",
             "AND",
             "ANDI",
             "AUIPC",
             "BEQ",
             "BGE",
             "BGEU",
             "BLT",
             "BLTU",
             "BNE",
             "JAL",
             "JALR",
             "LB",
             "LBU",
             "LH",
             "LHU",
             "LUI",
             "LW",
             "OR",
             "ORI",
             "SB",
             "SH",
             "SLL",
             "SLLI",
             "SLT",
             "SLTI",
             "SLTIU",
             "SLTU",
             "SRA",
             "SRAI",
             "SRL",
             "SRLI",
             "SUB",
             "SW",
             "XOR",
             "XORI"]
    
    TESTS_SUCC = []
    TESTS_FAIL = []
    
    for TEST in TESTS:
        if (hw_test_riscv_compliance_template(sigma, TEST) == 1):
            TESTS_SUCC.append(TEST)
            test_succ_counter = test_succ_counter + 1
        else:
            TESTS_FAIL.append(TEST)
            test_fail_counter = test_fail_counter + 1
    
    print("Total tests PASSED: ", test_succ_counter, ", FAILED: ", test_fail_counter)
    
    TESTS_FAIL_STR = ""
    for TEST in TESTS_FAIL:
        TESTS_FAIL_STR = TESTS_FAIL_STR + " " + TEST
    if (len(TESTS_FAIL) > 0):
        print("Failed tests:" + TESTS_FAIL_STR)
    
    print("")
    print("#################################################################################")
    print("")
