# -*- coding:utf-8 -*-
from __future__ import division

import sys

sys.path.append('../../../udm/sw')
import udm
from udm import *

import sigma
from sigma import *


sys.path.append('../riscv-compliance')

udm = udm('COM12', 921600)
print("")

def hw_test_riscv_compliance_ADD(sigma, test_filename):
    print("#### ADD TEST STARTED ####");
    
    DATA_SIZE = 36
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
    
    print("Clearing buffer")
    sigma.reset_buf()
    
    print("Loading test program...")
    sigma.tile.loadelf(test_filename)
    print("Test program written!")

    time.sleep(1)

    print("Reading data buffer...")
    rdarr = sigma.tile.udm.rdarr32(0x6000, DATA_SIZE)
    print("Data buffer read!")

    test_succ_flag = 1
    for i in range(DATA_SIZE):
        if (verify_data[i] != rdarr[i]):
            test_succ_flag = 0
            print("Test failed on data ", i, "! Expected: ", hex(verify_data[i]), ", received: ", hex(rdarr[i]))
    
    if (test_succ_flag):
        print("#### ADD TEST PASSED! ####");
    else:
        print("#### ADD TEST FAILED! ####")
    
    print("")    
    return test_succ_flag

sigma = sigma(udm)
#sigma.runtests()

hw_test_riscv_compliance_ADD(sigma, "../riscv-compliance/I-ADD-01.riscv")
