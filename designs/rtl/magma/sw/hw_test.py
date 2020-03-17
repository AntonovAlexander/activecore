# -*- coding:utf-8 -*-
from __future__ import division

import sys

sys.path.append('../../rtl/udm/sw')
import udm
from udm import *

import magma
from magma import *


udm = udm('COM10', 921600)
print("")

magma = magma(udm)
magma.setleds(0, 3)
magma.setleds(1, 5)
magma.setleds(2, 7)
magma.setleds(3, 9)
print("SW: ", hex(magma.udm.rd32(magma.gpio_addr + 0x10)))
