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

sys.path.append('../../../udm/sw')
import udm
from udm import *

sys.path.append('../../../sigma_tile/sw')
import sigma_tile
from sigma_tile import *

sys.path.append('median')
import hw_test_median
from hw_test_median import *

sys.path.append('qsort')
import hw_test_qsort
from hw_test_qsort import *

sys.path.append('rsort')
import hw_test_rsort
from hw_test_rsort import *

sys.path.append('irq_counter')
import hw_test_irq_counter
from hw_test_irq_counter import *


class sigma:

    sigma_addr = 0x0
    buf_addr = 0x6000
    buf_size = 8192
    
    def __init__(self, udm):
        self.udm = udm
        self.tile = sigma_tile(self.udm, self.sigma_addr)
    
    def __del__(self):
        self.tile.udm.discon()
    
    def reset_buf(self):
        self.udm.clr(self.buf_addr, self.buf_size)
    
    def runtests(self):
        test_succ_counter = 0
        test_fail_counter = 0
        
        if (hw_test_median(self, 'median.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_qsort(self, 'qsort.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_rsort(self, 'rsort.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        if (hw_test_irq_counter(self, 'irq_counter.riscv') == 1):
            test_succ_counter = test_succ_counter + 1
        else:
            test_fail_counter = test_fail_counter + 1
        
        print("Total tests PASSED: ", test_succ_counter, ", FAILED: ", test_fail_counter)
        print("")
