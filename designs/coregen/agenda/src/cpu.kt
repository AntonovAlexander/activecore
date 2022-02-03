/*
 * cpu.kt
 *
 *  Created on: 01.04.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package agenda

import hwast.*
import cyclix.*
import reordex.*

internal class CPU_CFG : Reordex_CFG(32, 32, 2, REG_MGMT_RENAMING(48), 16, REORDEX_MODE.RISC)
//internal class CPU_CFG : Reordex_CFG(32, 32, 2, REG_MGMT_SCOREBOARDING(), 16, REORDEX_MODE.RISC)
{
    var exu_opcode      = AddSrcUImm("exu_opcode", 6)
    var exu_unsigned    = AddSrcUImm("exu_unsigned", 1)
    var exu_rd_source   = AddSrcUImm("exu_rd_source", 3)

    var curinstraddr_imm    = AddSrcUImm("curinstraddr_imm", 32)

    var brctrl_src      = AddSrcUImm("brctrl_src", 1)
    var brctrl_cond     = AddSrcUImm("brctrl_cond", 1)
    var brctrl_mask     = AddSrcUImm("brctrl_mask", 3)

    var src0 = AddSrc()
    var src1 = AddSrc()

    var rd = AddRd()

    val M_Ext = true
    val Custom0_Ext = true
}

internal val CFG = CPU_CFG()

class cpu(name : String) : reordex.MultiExuRISC(name, CFG, RISCV_Decoder(), 4, 4, 4, 8) {

    init {
        add_exu(EXU_ALU_INTEGER(), 2, 4, STREAM_PREF_IMPL.RTL)
        add_exu(EXU_BRANCH(), 1, 8, STREAM_PREF_IMPL.RTL)
        if (CFG.M_Ext) add_exu(EXU_MUL_DIV(), 1, 3, STREAM_PREF_IMPL.RTL)
        if (CFG.Custom0_Ext) add_exu(EXU_CUSTOM_0(), 1, 3, STREAM_PREF_IMPL.RTL)
    }
}