/*
 * exu_alu_integer.kt
 *
 *  Created on: 01.04.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package agenda

internal class EXU_BRANCH : reordex.Exu("BRANCH", CFG) {

    var op0_wide    = ulocal("op0_wide", 32, 0, "0")
    var op1_wide    = ulocal("op1_wide", 32, 0, "0")
    var result_wide = ulocal("result_wide", 32, 0, "0")
    var CF          = ulocal("CF", 31, 0, "0")
    var SF          = ulocal("SF", 31, 0, "0")
    var ZF          = ulocal("ZF", 31, 0, "0")
    var OF          = ulocal("OF", 31, 0, "0")

    init {

        CFG.rd.assign(nextinstr_addr)

        begif(CFG.exu_unsigned)
        run {
            op0_wide.assign(zeroext(CFG.src0, 33))
            op1_wide.assign(zeroext(CFG.src1, 33))
        }; endif()
        begelse()
        run {
            op0_wide.assign(signext(CFG.src0, 33))
            op1_wide.assign(signext(CFG.src1, 33))
        }; endif()

        result_wide.assign(op0_wide)

        // computing result
        begcase(CFG.exu_opcode)
        run {
            begbranch(aluop_ADD)
            run {
                result_wide.assign(op0_wide + op1_wide)
            }; endbranch()

            begbranch(aluop_SUB)
            run {
                result_wide.assign(op0_wide - op1_wide)
            }; endbranch()
        }; endcase()

        // formation of result and flags
        CF.assign(result_wide[32])
        SF.assign(result_wide[31])
        ZF.assign(bnot(ror(result_wide.GetFracRef(31, 0))))
        OF.assign(bor(band(!CFG.src0[31], !CFG.src1[31], result_wide[31]), band(CFG.src0[31], CFG.src1[31], !result_wide[31])))

        begif(CFG.brctrl_cond)
        run {

            begcase(CFG.brctrl_mask)
            run {
                // BEQ
                begbranch(0x0)
                run {
                    begif(ZF)
                    run {
                        nextinstr_addr.assign(CFG.curinstraddr_imm)
                    }; endif()
                }; endbranch()

                // BNE
                begbranch(0x1)
                run {
                    begif(!ZF)
                    run {
                        nextinstr_addr.assign(CFG.curinstraddr_imm)
                    }; endif()
                }; endbranch()

                // BLT
                begbranch(0x4)
                run {
                    begif(CF)
                    run {
                        nextinstr_addr.assign(CFG.curinstraddr_imm)
                    }; endif()
                }; endbranch()

                // BGE
                begbranch(0x5)
                run {
                    begif(!CF)
                    run {
                        nextinstr_addr.assign(CFG.curinstraddr_imm)
                    }; endif()
                }; endbranch()

                // BLTU
                begbranch(0x6)
                run {
                    begif(CF)
                    run {
                        nextinstr_addr.assign(CFG.curinstraddr_imm)
                    }; endif()
                }; endbranch()

                // BGEU
                begbranch(0x7)
                run {
                    begif(!CF)
                    run {
                        nextinstr_addr.assign(CFG.curinstraddr_imm)
                    }; endif()
                }; endbranch()

            }; endcase()

        }; endif()

        begelse()
        run {
            begcase(CFG.brctrl_src)
            run {
                begbranch(JMP_SRC_IMM)
                run {
                    nextinstr_addr.assign(CFG.src0)
                }; endbranch()

                begbranch(JMP_SRC_ALU)
                run {
                    nextinstr_addr.assign(result_wide)
                }; endbranch()

            }; endcase()
        }; endif()
    }
}