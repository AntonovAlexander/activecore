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

def hw_test_dhrystone(sigma, dhrystone_filename):
    print("#### DHRYSTONE TEST STARTED ####")
          
    print("Clearing buffer")
    sigma.reset_buf()
    
    print("Loading test program...")
    sigma.tile.loadelf(dhrystone_filename)
    print("Test program written!")
    time.sleep(0.1)
    
    rdarr = sigma.tile.udm.rdarr32(0x6000, 2)
    Microseconds = rdarr[0]
    Dhrystones_Per_Second = rdarr[1]
    DMIPS = Dhrystones_Per_Second / 1757
    print("Microseconds: ", Microseconds)
    print("Dhrystones_Per_Second: ", Dhrystones_Per_Second)
    print("DMIPS: ", DMIPS)
    
    if ((Microseconds != 0) & (Dhrystones_Per_Second != 0)):
        test_succ_flag = 1
        print("#### DHRYSTONE TEST PASSED! ####")

    else:
        test_succ_flag = 0
        print("#### DHRYSTONE TEST FAILED! ####")
    
    print("")
    return test_succ_flag
