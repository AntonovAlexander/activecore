# -*- coding:utf-8 -*-
from __future__ import division

import udm
from udm import *

udm.cc('COM10', 921600)
print("")

CSR_LED_ADDR    = 0x00000000
CSR_SW_ADDR     = 0x00000004
TESTMEM_ADDR    = 0x80000000

def memtest(size):
    print("\n---- memtest started ----");
    test_succ = True
    for i in range(size):
        udm.wr(TESTMEM_ADDR + (i << 2), i)
    for i in range(size):
        data_read = udm.rd(TESTMEM_ADDR + (i << 2))
        if (data_read != i):
            print("memtest failed on address ", hex(TESTMEM_ADDR + (i << 2)), "expected data: ", hex(i), " data read: ", hex(data_read))
            test_succ = False
    if (test_succ):
        print("---- memtest PASSED ----\n")
    else:
        print("---- memtest FAILED ----\n")

udm.wr(CSR_LED_ADDR, 0xaa55)
print("Data read: ", hex(udm.rd(CSR_SW_ADDR)))
memtest(16)

udm.discon()
