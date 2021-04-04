# -*- coding:utf-8 -*-
from __future__ import division

import udm
from udm import *

udm = udm('COM1', 921600)
print("")

CSR_LED_ADDR    = 0x00000000
CSR_SW_ADDR     = 0x00000004
TESTMEM_ADDR    = 0x80000000


udm.wr32(CSR_LED_ADDR, 0xaa55)
print("SW read: ", hex(udm.rd32(CSR_SW_ADDR)))
udm.memtest32(TESTMEM_ADDR, 1024)

udm.disconnect()
