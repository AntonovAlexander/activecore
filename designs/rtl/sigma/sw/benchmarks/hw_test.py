# -*- coding:utf-8 -*-
from __future__ import division

import sys

sys.path.append('../../../../rtl/udm/sw')
import udm
from udm import *

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

buf_addr = 0x6000
buf_size = 8192

test_succ_counter = 0
test_fail_counter = 0

udm = udm('COM10', 921600)
print("")

def reset_buf():
    udm.clr(buf_addr, buf_size)

reset_buf()
if (hw_test_median(udm, 'median.riscv') == 1):
    test_succ_counter = test_succ_counter + 1
else:
    test_fail_counter = test_fail_counter + 1

reset_buf()
if (hw_test_qsort(udm, 'qsort.riscv') == 1):
    test_succ_counter = test_succ_counter + 1
else:
    test_fail_counter = test_fail_counter + 1

reset_buf()
if (hw_test_rsort(udm, 'rsort.riscv') == 1):
    test_succ_counter = test_succ_counter + 1
else:
    test_fail_counter = test_fail_counter + 1

print("Total tests PASSED: ", test_succ_counter, ", total test FAILED: ", test_fail_counter)
print("")

reset_buf()
hw_test_irq_counter(udm, 'irq_counter.riscv')

udm.discon()
