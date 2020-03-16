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
    print("#### PRESS IRQ BUTTON TO TEST! ####")
    print("")
    return
