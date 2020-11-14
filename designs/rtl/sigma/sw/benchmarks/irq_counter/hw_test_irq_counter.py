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

def hw_test_irq_counter(sigma, irq_counter_filename):
    print("#### IRQ COUNTER TEST STARTED ####");
    
    print("Loading test program...")
    sigma.tile.loadelf(irq_counter_filename)
    print("Test program written!")
    print("#### PRESS IRQ BUTTON TO INCREMENT LED COUNTER! ####")
    sigma.tile.sgi(0x3)
    sigma.tile.sgi(0x3)
    sigma.tile.sgi(0x3)
    sigma.tile.sgi(0x3)
    led_val = sigma.udm.rd32(0x80000000)
    print("LEDs: ", led_val)
    
    test_succ_flag = 0
    if (led_val == 5):
        test_succ_flag = 1
    
    if (test_succ_flag):
        print("#### IRQ TEST PASSED! ####")
    else:
        print("#### IRQ TEST FAILED! ####")
    
    print("")
    return test_succ_flag
