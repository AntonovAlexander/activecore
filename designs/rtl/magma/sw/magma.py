# -*- coding:utf-8 -*-
from __future__ import division

import sys

sys.path.append('../../udm/sw')
import udm
from udm import *

sys.path.append('../../sigma_tile/sw')
import sigma_tile
from sigma_tile import *


class magma:
    
    tile0_addr = 0x00000000
    tile1_addr = 0x10000000
    tile2_addr = 0x20000000
    tile3_addr = 0x30000000
    gpio_addr  = 0x40000000
    
    def __init__(self, udm):
        self.udm = udm
        self.tile0 = sigma_tile(self.udm, self.tile0_addr)
        self.tile1 = sigma_tile(self.udm, self.tile1_addr)
        self.tile2 = sigma_tile(self.udm, self.tile2_addr)
        self.tile3 = sigma_tile(self.udm, self.tile3_addr)
    
    def __del__(self):
        self.tile.udm.discon()
    
    def setleds(self, led_num, data):
        self.udm.wr32((self.gpio_addr + (led_num << 2)), data)
