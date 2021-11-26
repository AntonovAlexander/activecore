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

def test_mul_sw(sigma, a, b):
    sigma.tile.udm.wr32(0x6000, a)
    sigma.tile.udm.wr32(0x6004, b)
    corr_result = a * b
    time.sleep(0.1)
    led_val = sigma.udm.rd32(0x80000000)
    if (led_val == corr_result):
        print("CORRECT: ", a, " * ", b, " = ", corr_result)
        return 1
    else:
        print("INCORRECT: ", a, " * ", b, " = ", corr_result, ", received: ", led_val)
        return 0

def hw_test_mul_sw(sigma, mul_sw_filename):
    print("#### MUL_SW TEST STARTED ####")
          
    print("Loading test program...")
    sigma.tile.loadelf(mul_sw_filename)
    print("Test program written!")
    
    test_succ_flag = 1
    test_succ_flag &= test_mul_sw(sigma, 6, 7)
    test_succ_flag &= test_mul_sw(sigma, 2, 10)
    test_succ_flag &= test_mul_sw(sigma, 256, 256)
    
    if (test_succ_flag):
        print("#### MUL_SW TEST PASSED! ####")
    else:
        print("#### MUL_SW TEST FAILED! ####")
    
    print("")
    return test_succ_flag
