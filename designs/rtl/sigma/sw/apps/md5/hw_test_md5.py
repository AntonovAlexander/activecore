# -*- coding:utf-8 -*-
from __future__ import division

import sys
sys.path.append('../../../../../rtl/udm/sw')

import time

import udm
from udm import *

sys.path.append('..')
import sigma
from sigma import *

def hw_test_md5(sigma, firmware_filename):
    
    
    verify_data = [
        0xed076287, 0x532e8636, 0x5e841e92, 0xbfc50d8c
    ]
    
    return sigma.hw_test_generic(sigma, "MD5", firmware_filename, 0.1, verify_data)
