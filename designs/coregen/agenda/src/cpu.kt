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
    var alu_unsigned    = AddSrcUImm("alu_unsigned", 1)

    var src0 = AddSrc()
    var src1 = AddSrc()

    var rd = AddRd()
}
internal val CFG = CPU_CFG()


class cpu(name : String) : reordex.MultiExuRISC(name, CFG, 4, RISCV_Decoder()) {

    init {
        add_exu(EXU_ALU_INTEGER(), 2, 4, STREAM_PREF_IMPL.RTL)
        add_exu(EXU_MUL_DIV(), 1, 3, STREAM_PREF_IMPL.RTL)
    }
}