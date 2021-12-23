/*
 * fpu.kt
 *
 *  Created on: 20.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package citadel

import hwast.*
import cyclix.*
import reordex.*

class FPU_CFG() : Reordex_CFG(32, 32, 1, REG_MGMT_RENAMING(64), 64, REORDEX_MODE.COPROCESSOR)
{
    var opcode = AddSrcUImm("opcode", 32)

    var src0 = AddSrc()
    var src1 = AddSrc()
    var src2 = AddSrc()

    var rd0 = AddRd()
}
val CFG = FPU_CFG()

class EXU_FP_ADD_SUB() : reordex.Exu("FP_ADD_SUB", CFG) {

    init {
        begif(eq2(CFG.opcode, 0))
        run {
            CFG.rd0.assign(add(CFG.src0, CFG.src1))
        }; endif()
        begelse()
        run {
            CFG.rd0.assign(sub(CFG.src0, CFG.src1))
        }; endif()
    }
}

class EXU_FP_MUL() : reordex.Exu("FP_MUL", CFG) {

    init {
        CFG.rd0.assign(mul(CFG.src0, CFG.src1))
    }
}

class EXU_FP_DIV() : reordex.Exu("FP_DIV", CFG) {

    init {
        CFG.rd0.assign(div(CFG.src0, CFG.src1))
    }
}

class EXU_FP_FMA() : reordex.Exu("FP_FMA", CFG) {

    init {
        CFG.rd0.assign(add(mul(CFG.src0, CFG.src1), CFG.src2))
    }
}

class fpu(name : String) : reordex.MultiExuCoproc(name, CFG, 4) {

    init {
        add_exu(EXU_FP_ADD_SUB(), 2, 4, STREAM_PREF_IMPL.HLS)
        add_exu(EXU_FP_MUL(), 1, 3, STREAM_PREF_IMPL.HLS)
        add_exu(EXU_FP_DIV(), 1, 3, STREAM_PREF_IMPL.HLS)
        add_exu(EXU_FP_FMA(), 1, 3, STREAM_PREF_IMPL.HLS)
    }
}