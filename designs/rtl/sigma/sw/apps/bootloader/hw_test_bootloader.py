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

def hw_test_bootloader(sigma, firmware_filename, app_filename):
    
    print("#### ELF BOOTLOADER TEST STARTED ####")
    
    print("Clearing buffer")
    sigma.reset_buf()
    
    sigma.tile.sw_rst()
    print("Loading bootloader...")
    sigma.udm.wrelf32(0x0, firmware_filename)
    print("Loading test ELF image...")
    sigma.udm.wrbin32_le(0x6000, app_filename)
    print("Test written!")
    sigma.tile.sw_nrst()
    
    time.sleep(0.1)
    
    test_succ_flag = 0
    if (sigma.udm.rd32(0x80000000) == 0xaabb55aa):
        test_succ_flag = 1
    
    if (test_succ_flag):
        print("#### ELF BOOTLOADER TEST PASSED! ####");
    else:
        print("#### ELF BOOTLOADER TEST FAILED! ####")
    
    print("")
    return test_succ_flag
