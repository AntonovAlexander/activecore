# -*- coding:utf-8 -*-
from __future__ import division

import udm
from udm import *

udm.cc('COM6', 921600)
print("")

udm.wr(0x00000000, 0xaa55)
print("Data read: ", hex(udm.rd(0x00000004)))

udm.discon()
