# -*- coding:utf-8 -*-
from __future__ import division

import sys

sys.path.append('../../../../rtl/udm/sw')
import udm
from udm import *

buf_addr = 0x6000
buf_size = 8192

def reset_buf(udm):
    udm.clr(buf_addr, buf_size)

def loadbin(udm, filename):
    udm.rst()
    udm.wrbin32_le(0x0, filename)
    udm.nrst()

def loadelf(udm, filename):
    udm.rst()
    udm.wrelf32(0x0, filename)
    udm.nrst()
