# -*- coding:utf-8 -*-

#
# sigma.py
#
#  Created on: 24.09.2017
#      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
#     License: See LICENSE file for details
#

from __future__ import division

import sys

import time

sys.path.append('../../udm/sw')
import udm
from udm import *

sys.path.append('../../sigma_tile/sw')
import sigma_tile
from sigma_tile import *

sys.path.append('riscv-compliance')
import hw_test_riscv_compliance
from hw_test_riscv_compliance import *

sys.path.append('benchmarks/mul_sw')
import hw_test_mul_sw
from hw_test_mul_sw import *

sys.path.append('benchmarks/median')
import hw_test_median
from hw_test_median import *

sys.path.append('benchmarks/qsort')
import hw_test_qsort
from hw_test_qsort import *

sys.path.append('benchmarks/rsort')
import hw_test_rsort
from hw_test_rsort import *

sys.path.append('benchmarks/crc32')
import hw_test_crc32
from hw_test_crc32 import *

sys.path.append('benchmarks/md5')
import hw_test_md5
from hw_test_md5 import *

sys.path.append('benchmarks/bootloader')
import hw_test_bootloader
from hw_test_bootloader import *

sys.path.append('benchmarks/irq_counter')
import hw_test_irq_counter
from hw_test_irq_counter import *

sys.path.append('benchmarks/dhrystone')
import hw_test_dhrystone
from hw_test_dhrystone import *


class sigma:

    __sigma_addr = 0x0
    __buf_addr = 0x6000
    __buf_size = 8192
    
    def __init__(self, udm):
        self.udm = udm
        self.tile = sigma_tile(self.udm, self.__sigma_addr)
    
    def __del__(self):
        self.tile.udm.discon()
    
    def reset_buf(self):
        """Description:
            Reset memory region allocated for I/O

        """
        self.udm.clr(self.__buf_addr, self.__buf_size)
    
    def hw_test_generic(self, sigma, test_name, firmware_filename, sleep_secs, verify_data):
        print("#### " + test_name + " TEST STARTED ####");
        
        print("Clearing buffer")
        sigma.reset_buf()
        
        print("Loading test program...")
        sigma.tile.loadelf(firmware_filename)
        print("Test program written!")
    
        time.sleep(sleep_secs)
        
        print("Reading data buffer...")
        rdarr = sigma.tile.udm.rdarr32(0x6000, len(verify_data))
        print("Data buffer read!")
    
        test_succ_flag = 1
        for i in range(len(verify_data)):
            if (verify_data[i] != rdarr[i]):
                test_succ_flag = 0
                print("Test failed on data ", i, "! Expected: ", hex(verify_data[i]), ", received: ", hex(rdarr[i]))
        
        if (test_succ_flag):
            print("#### " + test_name + " TEST PASSED! ####");
        else:
            print("#### " + test_name + " TEST FAILED! ####")
        
        print("")
        return test_succ_flag
    
    def run_compliance_tests(self):
        hw_test_riscv_compliance(self)
    
    def run_app_tests(self):
        """Description:
            Run automated hardware tests

        """
        test_succ_counter = 0
        test_fail_counter = 0
        
        if (hw_test_dhrystone(self, 'benchmarks/dhrystone.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_mul_sw(self, 'benchmarks/mul_sw.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_median(self, 'benchmarks/median.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_qsort(self, 'benchmarks/qsort.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_rsort(self, 'benchmarks/rsort.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_crc32(self, 'benchmarks/crc32.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_md5(self, 'benchmarks/md5.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_bootloader(self, 'benchmarks/bootloader.riscv', 'benchmarks/bootloader_testapp.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_irq_counter(self, 'benchmarks/irq_counter.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        print("Total tests PASSED: ", test_succ_counter, ", FAILED: ", test_fail_counter)
        print("")
