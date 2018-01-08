# -*- coding:utf-8 -*-
from __future__ import division

import sys

sys.path.append('../../udm/sw')
import udm
from udm import *

def reset_buf(offset):    
    udm.clr(offset, buf_size)

TILE_OFFSET = 0x80000000
TILE_SIZE = 1024 * 1024
BUF_SIZE = 32 * 1024

udm.cc('COM4', 921600)
print("")

udm.rst()

for i in range(4):
    mem_offset = TILE_OFFSET + (i * TILE_SIZE)
    reset_buf(mem_offset)

for i in range(0, 4):
    mem_offset = TILE_OFFSET + (i * TILE_SIZE)
    udm.wrfile_le(mem_offset, "io_heartbeat_variable.bin")

udm.nrst()

udm.discon()
