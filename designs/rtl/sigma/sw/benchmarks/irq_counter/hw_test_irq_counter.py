# -*- coding:utf-8 -*-
from __future__ import division

import sys
sys.path.append('../../../../../rtl/udm/sw')

import time

import udm
from udm import *

def hw_test_irq_counter(udm, irq_counter_filename):
    print("#### IRQ COUNTER TEST STARTED ####");
    
    print("Loading test program...")
    udm.loadbin(irq_counter_filename)
    print("Test program written!")
    print("#### PRESS IRQ BUTTON TO TEST! ####")
    print("")
    return
