/*
 * exu_alu_integer.kt
 *
 *  Created on: 01.04.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package agenda

internal class EXU_ALU_INTEGER : reordex.Exu("INTEGER", CFG) {

    var alu_op0_wide    = ulocal("alu_op0_wide", 32, 0, "0")
    var alu_op1_wide    = ulocal("alu_op1_wide", 32, 0, "0")

    var alu_result_wide = ulocal("alu_result_wide", 32, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")
    var CF              = ulocal("CF", 31, 0, "0")
    var SF              = ulocal("SF", 31, 0, "0")
    var ZF              = ulocal("ZF", 31, 0, "0")
    var OF              = ulocal("OF", 31, 0, "0")
    var alu_overflow    = ulocal("alu_overflow", 0, 0, "0")

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

        alu_result_wide.assign(alu_op0_wide)

        // computing result
        begcase(CFG.exu_opcode)
        run {
            begbranch(aluop_ADD)
            run {
                alu_result_wide.assign(alu_op0_wide + alu_op1_wide)
            }; endbranch()

            begbranch(aluop_SUB)
            run {
                alu_result_wide.assign(alu_op0_wide - alu_op1_wide)
            }; endbranch()

            begbranch(aluop_AND)
            run {
                alu_result_wide.assign(band(alu_op0_wide, alu_op1_wide))
            }; endbranch()

            begbranch(aluop_OR)
            run {
                alu_result_wide.assign(bor(alu_op0_wide, alu_op1_wide))
            }; endbranch()

            begbranch(aluop_SLL)
            run {
                alu_result_wide.assign(sll(alu_op0_wide, alu_op1_wide))
            }; endbranch()

            begbranch(aluop_SRL)
            run {
                alu_result_wide.assign(srl(zeroext(alu_op0_wide[31, 0], 64), alu_op1_wide[4, 0]))
            }; endbranch()

            begbranch(aluop_SRA)
            run {
                alu_result_wide.assign(sra(signext(alu_op0_wide[31, 0], 64), alu_op1_wide[4, 0]))
            }; endbranch()

            begbranch(aluop_XOR)
            run {
                alu_result_wide.assign(bxor(alu_op0_wide, alu_op1_wide))
            }; endbranch()

            begbranch(aluop_CLRB)
            run {
                alu_result_wide.assign(band(alu_op0_wide, !alu_op1_wide))
            }; endbranch()
        }; endcase()

        // formation of result and flags
        alu_result.assign(alu_result_wide[31, 0])
        CF.assign(alu_result_wide[32])
        SF.assign(alu_result_wide[31])
        ZF.assign(bnot(ror(alu_result)))
        OF.assign(bor(band(!CFG.src0[31], !CFG.src1[31], alu_result[31]), band(CFG.src0[31], CFG.src1[31], !alu_result[31])))

        begif(CFG.exu_unsigned)
        run {
            alu_overflow.assign(CF)
        }; endif()
        begelse()
        run {
            alu_overflow.assign(OF)
        }; endif()

        // rd wdata processing
        begcase(CFG.exu_rd_source)
        run {
            begbranch(RD_LUI)
            run {
                alu_result.assign(CFG.src0)
            }; endbranch()

            begbranch(RD_ALU)
            run {
                alu_result.assign(alu_result)
            }; endbranch()

            begbranch(RD_CF_COND)
            run {
                alu_result.assign(CF)
            }; endbranch()

            begbranch(RD_OF_COND)
            run {
                alu_result.assign(OF)
            }; endbranch()

            begbranch(RD_PC_INC)
            run {
                alu_result.assign(nextinstr_addr)
            }; endbranch()

            /*
            begbranch(RD_CSR)
            run {
                rds[0].wdata.assign(csr_rdata)
                rds[0].rdy.assign(1)
            }; endbranch()
             */

        }; endcase()

        CFG.rd.assign(alu_result)
    }
}