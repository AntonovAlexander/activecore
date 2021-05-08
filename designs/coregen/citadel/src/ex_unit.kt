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

val Exu_cfg_rf = Exu_CFG_RF(32, 3)

class EXU_FP_ADD_SUB() : reordex.Exu("FP_ADD_SUB", Exu_cfg_rf) {

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

class EXU_FP_MUL() : reordex.Exu("FP_MUL", Exu_cfg_rf) {

    init {
        result.assign(rs[0] * rs[1])
    }
}

class EXU_FP_DIV() : reordex.Exu("FP_DIV", Exu_cfg_rf) {

    init {
        result.assign(rs[0] / rs[1])
    }
}

class EXU_FP_FMA() : reordex.Exu("FP_FMA", Exu_cfg_rf) {

    init {
        result.assign((rs[0] * rs[1]) + rs[2])
    }
}

class fpu(name : String) : reordex.MultiExu(name, Exu_cfg_rf, MultiExu_CFG_RF(32, true, 64), 4) {

    init {
        add_exu(EXU_FP_ADD_SUB(), 2, 4)
        add_exu(EXU_FP_MUL(), 1, 3)
        add_exu(EXU_FP_DIV(), 1, 3)
        add_exu(EXU_FP_FMA(), 1, 3)
    }
}