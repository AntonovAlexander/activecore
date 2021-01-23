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

val Exu_cfg_rf = Exu_CFG_RF(32)

class EXU_INTEGER(stage_num: Int) : reordex.Exu("INTEGER", Exu_cfg_rf, stage_num) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        alu_op1.assign(subStruct(req_data, "rs0_rdata"))
        alu_op2.assign(subStruct(req_data, "rs1_rdata"))
        add_gen(alu_result, alu_op1, alu_op1)
        add_gen(alu_result, alu_op1, alu_op2)
        resp_data.assign(hw_fracs(hw_frac_SubStruct("wdata")), alu_result)
    }
}

class EXU_MUL(stage_num: Int) : reordex.Exu("MUL", Exu_cfg_rf, stage_num) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        alu_op1.assign(subStruct(req_data, "rs0_rdata"))
        alu_op2.assign(subStruct(req_data, "rs1_rdata"))
        mul_gen(alu_result, alu_op1, alu_op2)
        resp_data.assign(hw_fracs(hw_frac_SubStruct("wdata")), alu_result)
    }
}

class EXU_SHIFT(stage_num: Int) : reordex.Exu("SHIFT", Exu_cfg_rf, stage_num) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        alu_op1.assign(subStruct(req_data, "rs0_rdata"))
        alu_op2.assign(subStruct(req_data, "rs1_rdata"))
        sra_gen(alu_result, alu_op1, alu_op2)
        resp_data.assign(hw_fracs(hw_frac_SubStruct("wdata")), alu_result)
    }
}

class test_multiexu(name_in : String) : reordex.MultiExu(name_in, Exu_cfg_rf, MultiExu_CFG_RF(32, 32, false, 64), 8) {

    init {
        add_exu(EXU_INTEGER(2), 3)
        add_exu(EXU_MUL(4), 1)
        add_exu(EXU_SHIFT(1), 2)
    }
}