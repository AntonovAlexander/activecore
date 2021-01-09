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

class ex_unit(name_in : String) : reordex.MultiExu(name_in, MultiExu_CFG_RF(32,32, false, 0), 16) {

    /*
    var alu_req         = ulocal("alu_req", 0, 0, "0")
    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_op1_wide    = ulocal("alu_op1_wide", 32, 0, "0")
    var alu_op2_wide    = ulocal("alu_op2_wide", 32, 0, "0")
    var alu_opcode      = ulocal("alu_opcode", 3, 0, "0")
    var alu_unsigned    = ulocal("alu_unsigned", 0, 0, "0")

    var alu_result_wide = ulocal("alu_result_wide", 32, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")
    var alu_CF          = ulocal("alu_CF", 0, 0, "0")
    var alu_SF          = ulocal("alu_SF", 0, 0, "0")
    var alu_ZF          = ulocal("alu_ZF", 0, 0, "0")
    var alu_OF          = ulocal("alu_OF", 0, 0, "0")
    var alu_overflow    = ulocal("alu_overflow", 0, 0, "0")
    */

    var EXU_INTEGER = add_exu("INTEGER", 2, 4)
    var EXU_MUL = add_exu("MUL", 1, 2)
    var EXU_SHIFT = add_exu("SHIFT", 1, 1)

    init {
        EXU_INTEGER.begin()
        run {
            add_gen(EXU_INTEGER.rd_wdata, EXU_INTEGER.rs0_rdata, EXU_INTEGER.rs1_rdata)
        }; endexu()

        EXU_MUL.begin()
        run {
            mul_gen(EXU_INTEGER.rd_wdata, EXU_INTEGER.rs0_rdata, EXU_INTEGER.rs1_rdata)
        }; endexu()

        EXU_SHIFT.begin()
        run {
            sra_gen(EXU_INTEGER.rd_wdata, EXU_INTEGER.rs0_rdata, EXU_INTEGER.rs1_rdata)
        }; endexu()
    }

}