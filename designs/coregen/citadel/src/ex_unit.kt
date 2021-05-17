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

class FPU_CFG() : Reordex_CFG(32, 32, true,64)
{
    var rs0 = AddRs()
    var rs1 = AddRs()
    var rs2 = AddRs()
}
val FPU_CFG_inst = FPU_CFG()

class EXU_FP_ADD_SUB() : reordex.Exu("FP_ADD_SUB", FPU_CFG_inst) {

    init {
        begif(eq2(opcode, 0))
        run {
            result.assign(rs[0] + rs[1])
        }; endif()
        begelse()
        run {
            result.assign(rs[0] - rs[1])
        }; endif()
    }
}

class EXU_FP_MUL() : reordex.Exu("FP_MUL", FPU_CFG_inst) {

    init {
        result.assign(rs[0] * rs[1])
    }
}

class EXU_FP_DIV() : reordex.Exu("FP_DIV", FPU_CFG_inst) {

    init {
        result.assign(rs[0] / rs[1])
    }
}

class EXU_FP_FMA() : reordex.Exu("FP_FMA", FPU_CFG_inst) {

    init {
        result.assign((rs[0] * rs[1]) + rs[2])
    }
}

class fpu(name : String) : reordex.MultiExu(name, FPU_CFG_inst, 4) {

    init {
        add_exu(EXU_FP_ADD_SUB(), 2, 4)
        add_exu(EXU_FP_MUL(), 1, 3)
        add_exu(EXU_FP_DIV(), 1, 3)
        add_exu(EXU_FP_FMA(), 1, 3)
    }
}