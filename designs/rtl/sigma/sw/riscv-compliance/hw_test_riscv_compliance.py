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

def hw_test_riscv_compliance_template(sigma, instr_name, ref_directory):
    
    f = open(ref_directory + instr_name + "-01.reference_output", "r")

    verify_data = []
    
    while True:
    	verify_dataword = f.readline()
    	if (verify_dataword):
    		verify_data.append(int(verify_dataword, 16))
    	else:
    		break
    
    return sigma.hw_test_generic(sigma, instr_name, "riscv-compliance/" + instr_name + "-01.riscv", 0.1, verify_data)

def hw_test_riscv_compliance(sigma, testsuites_todo):
    
    print("#################################################################################")
    print("############################ RISC-V Compliance Test #############################")
    print("#### Imperas Software Ltd., 2019 <https://github.com/riscv/riscv-compliance> ####")
    print("#################################################################################")
    print("")
    
    test_succ_counter = 0
    test_fail_counter = 0
    
    TESTS_RV32I = [  "I-ADD",
                     "I-ADDI",
                     "I-AND",
                     "I-ANDI",
                     "I-AUIPC",
                     "I-BEQ",
                     "I-BGE",
                     "I-BGEU",
                     "I-BLT",
                     "I-BLTU",
                     "I-BNE",
                     "I-JAL",
                     "I-JALR",
                     "I-LB",
                     "I-LBU",
                     "I-LH",
                     "I-LHU",
                     "I-LUI",
                     "I-LW",
                     "I-OR",
                     "I-ORI",
                     "I-SB",
                     "I-SH",
                     "I-SLL",
                     "I-SLLI",
                     "I-SLT",
                     "I-SLTI",
                     "I-SLTIU",
                     "I-SLTU",
                     "I-SRA",
                     "I-SRAI",
                     "I-SRL",
                     "I-SRLI",
                     "I-SUB",
                     "I-SW",
                     "I-XOR",
                     "I-XORI",
                     "I-DELAY_SLOTS",
                     #"I-EBREAK",
                     #"I-ECALL",
                     "I-ENDIANESS",
                     "I-IO",
                     #"I-MISALIGN_JMP",
                     #"I-MISALIGN_LDST",
                     "I-NOP",
                     "I-RF_size",
                     "I-RF_width",
                     "I-RF_x0"]
    
    TESTS_RV32M = [  "mul",
                     "mulh",
                     "mulhsu",
                     "mulhu",
                     "div",
                     "divu",
                     "rem",
                     "remu"]
    
    TESTS_SUCC = []
    TESTS_FAIL = []
    
    for testsuite_todo in testsuites_todo:
        if (testsuite_todo == "RV32I"):
            for TEST in TESTS_RV32I:
                if (hw_test_riscv_compliance_template(sigma, TEST, "riscv-compliance/riscv-test-suite/rv32i/references/") == 1):
                    TESTS_SUCC.append(TEST)
                    test_succ_counter = test_succ_counter + 1
                else:
                    TESTS_FAIL.append(TEST)
                    test_fail_counter = test_fail_counter + 1
        elif (testsuite_todo == "RV32M"):
            for TEST in TESTS_RV32M:    
                if (hw_test_riscv_compliance_template(sigma, TEST, "riscv-compliance/riscv-test-suite/rv32m/references/") == 1):
                    TESTS_SUCC.append(TEST)
                    test_succ_counter = test_succ_counter + 1
                else:
                    TESTS_FAIL.append(TEST)
                    test_fail_counter = test_fail_counter + 1
        else:
            raise Exception("Test not recognized!")
    
    
    
    print("Total tests PASSED: ", test_succ_counter, ", FAILED: ", test_fail_counter)
    
    TESTS_FAIL_STR = ""
    for TEST in TESTS_FAIL:
        TESTS_FAIL_STR = TESTS_FAIL_STR + " " + TEST
    if (len(TESTS_FAIL) > 0):
        print("Failed tests:" + TESTS_FAIL_STR)
    
    print("")
    print("#################################################################################")
    print("")
