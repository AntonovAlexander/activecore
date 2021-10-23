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

def hw_test_crc32(sigma, crc32_sw_filename):
    print("#### CRC32_SW TEST STARTED ####")
    crc = sigma.udm.rd32(0x80000000)
    print('ishod %X'%crc)
    print("Loading test program...")
    sigma.tile.loadelf(crc32_sw_filename)
    print("Test program written!")
    #time.sleep(1)
    crc = sigma.udm.rd32(0x80000000)
    i = 0;
    while i < 9:
        prev_crc = sigma.udm.rd32(0x6000 + i * 4)
        inp_data = sigma.udm.rd32(0x6000 + 9 * 4 + i * 4)
        crc = sigma.udm.rd32(0x6000 + 18 * 4 + i * 4)
        print('%(iter)d          %(prev)X    %(in)d    %(in)X    %(last)X' % {"iter":i + 1,"prev": prev_crc, "in" : inp_data, "last":crc})
        i += 1

    crc = sigma.udm.rd32(0x80000000)
    print('res %x'%crc)
    if (crc == 0xcbf43926):
        test_succ_flag = 1
        print("#### CRC32_SW TEST PASSED! ####")

    else:
        test_succ_flag = 0
        print("#### CRC32_SW TEST FAILED! ####")
    
    print("")
    return test_succ_flag
