/*
 * ex_unit.kt
 *
 *  Created on: 20.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package citadel

import hwast.*
import reordex.*

class EXU_INTEGER(stage_num: Int) : reordex.Exu("INTEGER", Exu_CFG_RF(32), stage_num) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        add_gen(alu_result, alu_op1, alu_op2)
        add_gen(alu_result, alu_op1, alu_op2)
        //resp_data.assign(hw_fracs(hw_frac_SubStruct("rd_wdata")), alu_result)
    }
}

class EXU_MUL(stage_num: Int) : reordex.Exu("MUL", Exu_CFG_RF(32), stage_num) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        mul_gen(alu_result, alu_op1, alu_op2)
        //resp_data.assign(hw_fracs(hw_frac_SubStruct("rd_wdata")), alu_result)
    }
}

class EXU_SHIFT(stage_num: Int) : reordex.Exu("SHIFT", Exu_CFG_RF(32), stage_num) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        sra_gen(alu_result, alu_op1, alu_op2)
        //resp_data.assign(hw_fracs(hw_frac_SubStruct("rd_wdata")), alu_result)
    }
}

class test_multiexu(name_in : String) : reordex.MultiExu(name_in, MultiExu_CFG_RF(32, 32, false, 0), 16) {

    init {
        add_exu(EXU_INTEGER(2), 3)
        add_exu(EXU_MUL(4), 1)
        add_exu(EXU_SHIFT(1), 2)
    }
}