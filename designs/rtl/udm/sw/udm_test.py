# -*- coding:utf-8 -*-
from __future__ import division

import random as rng

import udm
from udm import *

udm.cc('COM10', 921600)
print("")

CSR_LED_ADDR    = 0x00000000
CSR_SW_ADDR     = 0x00000004
TESTMEM_ADDR    = 0x80000000

def memtest(size):
    print("\n---- memtest started, size:", size, " ----");
    test_succ = True
    
    # generating test data
    wrdata = []
    for i in range(size):
        wrdata.append(rng.randint(0, ((1024*1024*1024*4)-1)))
    
    # writing test data
    udm.wrarr(TESTMEM_ADDR, wrdata)
        
    #reading test data
    rddata = udm.rdarr32(TESTMEM_ADDR, size)
    
    # checking test data
    for i in range(size):
        if (rddata[i] != wrdata[i]):
            print("memtest failed on address ", hex(TESTMEM_ADDR + (i << 2)), "expected data: ", hex(wrdata[i]), " data read: ", hex(rddata[i]))
            test_succ = False
    
    if (test_succ):
        print("---- memtest PASSED ----\n")
    else:
        print("---- memtest FAILED ----\n")


udm.wr(CSR_LED_ADDR, 0xaa55)
print("Data read: ", hex(udm.rd(CSR_SW_ADDR)))
memtest(1024)

udm.discon()
