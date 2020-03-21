# -*- coding:utf-8 -*-

#
# sigma_tile.py
#
#  Created on: 27.12.2017
#      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
#     License: See LICENSE file for details
#

from __future__ import division

import sys

sys.path.append('../../udm/sw')
import udm
from udm import *


class sigma_tile:

    sigma_addr = 0x0
    
    def __init__(self, udm, sigma_addr):
        self.udm = udm
        self.sigma_addr = sigma_addr
        IDCODE = self.udm.rd32((self.sigma_addr + 0x00100000))
        print("sigma_tile@0x{:08x}".format(self.sigma_addr) , ": IDCODE: ", hex(IDCODE))
        print()
        if (IDCODE != 0xdeadbeef):
            self.udm.discon()
            raise Exception("sigma tile not found at address " + hex(sigma_addr))
    
    def __del__(self):
        self.udm.discon()
    
    def cpu_rst(self):
        self.udm.wr32((self.sigma_addr + 0x00100004), 0x01)
    
    def cpu_nrst(self):
        self.udm.wr32((self.sigma_addr + 0x00100004), 0x00)
    
    def loadbin(self, filename):
        self.cpu_rst()
        self.udm.wrbin32_le(self.sigma_addr, filename)
        self.cpu_nrst()
    
    def loadelf(self, filename):
        self.cpu_rst()
        self.udm.wrelf32(self.sigma_addr, filename)
        self.cpu_nrst()
    
    def msi(self, irq_num):
        self.udm.wr32((self.sigma_addr + 0x0010000C), irq_num)
