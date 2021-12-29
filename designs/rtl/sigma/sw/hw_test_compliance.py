# -*- coding:utf-8 -*-
from __future__ import division

import sys

sys.path.append('../../udm/sw')
import udm
from udm import *

import sigma
from sigma import *


udm = udm('COM1', 921600)
print("")

sigma = sigma(udm)
sigma.run_compliance_tests(["RV32I", "RV32M"])

udm.disconnect()
