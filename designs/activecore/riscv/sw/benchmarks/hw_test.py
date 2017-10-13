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

test_succ_counter = 0
test_fail_counter = 0

udm.cc('COM5', 921600)
print("")

if (hw_test_median('median.riscv') == 1):
    test_succ_counter = test_succ_counter + 1
else:
    test_fail_counter = test_fail_counter + 1

if (hw_test_qsort('qsort.riscv') == 1):
    test_succ_counter = test_succ_counter + 1
else:
    test_fail_counter = test_fail_counter + 1

if (hw_test_rsort('rsort.riscv') == 1):
    test_succ_counter = test_succ_counter + 1
else:
    test_fail_counter = test_fail_counter + 1

print("Total tests passed: ", test_succ_counter, ", total test failed: ", test_fail_counter)

udm.discon()
