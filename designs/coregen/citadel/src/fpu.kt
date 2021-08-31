/*
 * fpu.kt
 *
 *  Created on: 20.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package citadel

import cyclix.STREAM_PREF_IMPL
import hwast.*
import reordex.*

class FPU_CFG() : Reordex_CFG(32, 32, true,64, 64, REORDEX_MODE.COPROCESSOR)
{
    var opcode = AddSrcUImm("opcode", 32)

    var src0 = AddSrc()
    var src1 = AddSrc()
    var src2 = AddSrc()

    var rd0 = AddRd()
}
val FPU_CFG_inst = FPU_CFG()

class EXU_FP_ADD_SUB() : reordex.Exu("FP_ADD_SUB", FPU_CFG_inst) {

    init {
        begif(eq2(FPU_CFG_inst.opcode, 0))
        run {
            rd0.assign(add(FPU_CFG_inst.src0, FPU_CFG_inst.src1))
        }; endif()
        begelse()
        run {
            rd0.assign(sub(FPU_CFG_inst.src0, FPU_CFG_inst.src1))
        }; endif()
    }
}

class EXU_FP_MUL() : reordex.Exu("FP_MUL", FPU_CFG_inst) {

    init {
        rd0.assign(mul(FPU_CFG_inst.src0, FPU_CFG_inst.src1))
    }
}

class EXU_FP_DIV() : reordex.Exu("FP_DIV", FPU_CFG_inst) {

    init {
        rd0.assign(div(FPU_CFG_inst.src0, FPU_CFG_inst.src1))
    }
}

class EXU_FP_FMA() : reordex.Exu("FP_FMA", FPU_CFG_inst) {

    init {
        rd0.assign(add(mul(FPU_CFG_inst.src0, FPU_CFG_inst.src1), FPU_CFG_inst.src2))
    }
}

class fpu(name : String) : reordex.MultiExuCoproc(name, FPU_CFG_inst, 4) {

    init {
        add_exu(EXU_FP_ADD_SUB(), 2, 4, STREAM_PREF_IMPL.HLS)
        add_exu(EXU_FP_MUL(), 1, 3, STREAM_PREF_IMPL.HLS)
        add_exu(EXU_FP_DIV(), 1, 3, STREAM_PREF_IMPL.HLS)
        add_exu(EXU_FP_FMA(), 1, 3, STREAM_PREF_IMPL.HLS)
    }
}