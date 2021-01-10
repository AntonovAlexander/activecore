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

class EXU_INTEGER() : reordex.Exu("INTEGER", 2) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        add_gen(alu_result, alu_op1, alu_op2)
    }
}

class EXU_MUL() : reordex.Exu("MUL", 4) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        mul_gen(alu_result, alu_op1, alu_op2)
    }
}

class EXU_SHIFT() : reordex.Exu("SHIFT", 1) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        sra_gen(alu_result, alu_op1, alu_op2)
    }
}

class test_multiexu(name_in : String) : reordex.MultiExu(name_in, MultiExu_CFG_RF(32,32, false, 0), 16) {

    init {
        add_exu(EXU_INTEGER(), 2)
        add_exu(EXU_MUL(), 1)
        add_exu(EXU_SHIFT(), 1)
    }
}