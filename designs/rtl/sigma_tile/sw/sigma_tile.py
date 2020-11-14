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

    __sigma_addr = 0x0
    
    def __init__(self, udm, sigma_addr):
        self.udm = udm
        self.__sigma_addr = sigma_addr
        IDCODE = self.udm.rd32((self.__sigma_addr + 0x00100000))
        print("sigma_tile@0x{:08x}".format(self.__sigma_addr) , ": IDCODE: ", hex(IDCODE))
        print()
        if (IDCODE != 0xdeadbeef):
            self.udm.discon()
            raise Exception("sigma tile not found at address " + hex(__sigma_addr))
    
    def __del__(self):
        self.udm.discon()
    
    def sw_rst(self):
        """Description:
            Assert software reset

        """
        self.udm.wr32((self.__sigma_addr + 0x00100004), 0x01)
    
    def sw_nrst(self):
        """Description:
            Deassert software reset

        """
        self.udm.wr32((self.__sigma_addr + 0x00100004), 0x00)
    
    def sw_rst_autoclr(self):
        """Description:
            Assert auto-clearable software reset

        """
        self.udm.wr32((self.__sigma_addr + 0x00100004), 0x03)
    
    def loadbin(self, filename):
        """Description:
            Write data from binary file to local CPU RAM

        Parameters:
            filename (str): Binary file name

        """
        self.sw_rst()
        self.udm.wrbin32_le(self.__sigma_addr, filename)
        self.sw_nrst()
    
    def loadelf(self, filename):
        """Description:
            Write elf file to local CPU RAM

        Parameters:
            filename (str): Elf file name

        """
        self.sw_rst()
        self.udm.wrelf32(self.__sigma_addr, filename)
        self.sw_nrst()
    
    def sgi(self, irq_num):
        """Description:
            Fire software generated interrupt

        Parameters:
            irq_num (int): Interrupt number

        """
        self.udm.wr32((self.__sigma_addr + 0x00100014), irq_num)
