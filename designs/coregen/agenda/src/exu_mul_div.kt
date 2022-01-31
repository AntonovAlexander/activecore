/*
 * exu_mul_div.kt (placeholder for mul/div, replaced with custom RTL in implementation)
 *
 *  Created on: 01.04.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package agenda

internal class EXU_MUL_DIV : reordex.Exu("MUL_DIV", CFG) {

    // ALU opcodes
    val aluop_MUL		= 0
    val aluop_DIV		= 1

    var alu_op0_wide    = ulocal("alu_op0_wide", 32, 0, "0")
    var alu_op1_wide    = ulocal("alu_op1_wide", 32, 0, "0")
    var alu_result_wide = ulocal("alu_result_wide", 32, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {

        begif(CFG.exu_unsigned)
        run {
            alu_op0_wide.assign(zeroext(CFG.src0, 33))
            alu_op1_wide.assign(zeroext(CFG.src1, 33))
        }; endif()
        begelse()
        run {
            alu_op0_wide.assign(signext(CFG.src0, 33))
            alu_op1_wide.assign(signext(CFG.src1, 33))
        }; endif()

        begcase(CFG.exu_opcode)
        run {
            begbranch(aluop_MUL)
            run {
                alu_result_wide.assign(alu_op0_wide * alu_op1_wide)
            }; endbranch()

            begbranch(aluop_DIV)
            run {
                alu_result_wide.assign(alu_op0_wide / alu_op1_wide)
            }; endbranch()
        }; endcase()

        alu_result.assign(alu_result_wide[31, 0])

        CFG.rd.assign(alu_result)
    }
}