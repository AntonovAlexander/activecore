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

class EXU_INTEGER() : reordex.Exu("INTEGER", Exu_cfg_rf) {

    // ALU opcodes
    val aluop_ADD		= 0
    val aluop_SUB		= 1
    val aluop_AND		= 2
    val aluop_OR		= 3
    val aluop_SLL		= 4
    val aluop_SRL		= 5
    val aluop_SRA		= 6
    val aluop_XOR		= 7
    val aluop_CLRB		= 8

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

    init {
        alu_op1.assign(subStruct(req_data, "rs0_rdata"))
        alu_op2.assign(subStruct(req_data, "rs1_rdata"))
        alu_opcode.assign(subStruct(req_data, "opcode"))

        begif(alu_unsigned)
        run {
            alu_op1_wide.assign(zeroext(alu_op1, 33))
            alu_op2_wide.assign(zeroext(alu_op2, 33))
        }; endif()
        begelse()
        run {
            alu_op1_wide.assign(signext(alu_op1, 33))
            alu_op2_wide.assign(signext(alu_op2, 33))
        }; endif()

        alu_result_wide.assign(alu_op1_wide)

        // computing result
        begcase(alu_opcode)
        run {
            begbranch(aluop_ADD)
            run {
                alu_result_wide.assign(alu_op1_wide + alu_op2_wide)
            }; endbranch()

            begbranch(aluop_SUB)
            run {
                alu_result_wide.assign(alu_op1_wide - alu_op2_wide)
            }; endbranch()

            begbranch(aluop_AND)
            run {
                alu_result_wide.assign(band(alu_op1_wide, alu_op2_wide))
            }; endbranch()

            begbranch(aluop_OR)
            run {
                alu_result_wide.assign(bor(alu_op1_wide, alu_op2_wide))
            }; endbranch()

            begbranch(aluop_SLL)
            run {
                alu_result_wide.assign(sll(alu_op1_wide, alu_op2_wide))
            }; endbranch()

            begbranch(aluop_SRL)
            run {
                alu_result_wide.assign(srl(zeroext(alu_op1_wide[31, 0], 64), alu_op2_wide[4, 0]))
            }; endbranch()

            begbranch(aluop_SRA)
            run {
                alu_result_wide.assign(sra(signext(alu_op1_wide[31, 0], 64), alu_op2_wide[4, 0]))
            }; endbranch()

            begbranch(aluop_XOR)
            run {
                alu_result_wide.assign(bxor(alu_op1_wide, alu_op2_wide))
            }; endbranch()

            begbranch(aluop_CLRB)
            run {
                alu_result_wide.assign(band(alu_op1_wide, !alu_op2_wide))
            }; endbranch()
        }; endcase()

        // formation of result and flags
        alu_result.assign(alu_result_wide[31, 0])
        alu_CF.assign(alu_result_wide[32])
        alu_SF.assign(alu_result_wide[31])
        alu_ZF.assign(bnot(ror(alu_result)))
        alu_OF.assign(bor(band(!alu_op1[31], band(!alu_op2[31], alu_result[31])), band(alu_op1[31], band(alu_op2[31], !alu_result[31]))))

        begif(alu_unsigned)
        run {
            alu_overflow.assign(alu_CF)
        }; endif()
        begelse()
        run {
            alu_overflow.assign(alu_OF)
        }; endif()
    }
}

class EXU_ADD() : reordex.Exu("ADD", Exu_cfg_rf) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        alu_op1.assign(subStruct(req_data, "rs0_rdata"))
        alu_op2.assign(subStruct(req_data, "rs1_rdata"))
        add_gen(alu_result, alu_op1, alu_op2)
        resp_data.assign(hw_fracs(hw_frac_SubStruct("wdata")), alu_result)
    }
}

class EXU_MUL() : reordex.Exu("MUL", Exu_cfg_rf) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        alu_op1.assign(subStruct(req_data, "rs0_rdata"))
        alu_op2.assign(subStruct(req_data, "rs1_rdata"))
        mul_gen(alu_result, alu_op1, alu_op2)
        resp_data.assign(hw_fracs(hw_frac_SubStruct("wdata")), alu_result)
    }
}

class EXU_XOR() : reordex.Exu("XOR", Exu_cfg_rf) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        alu_op1.assign(subStruct(req_data, "rs0_rdata"))
        alu_op2.assign(subStruct(req_data, "rs1_rdata"))
        bxor_gen(alu_result, alu_op1, alu_op2)
        resp_data.assign(hw_fracs(hw_frac_SubStruct("wdata")), alu_result)
    }
}

class EXU_SHIFT() : reordex.Exu("SHIFT", Exu_cfg_rf) {

    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        alu_op1.assign(subStruct(req_data, "rs0_rdata"))
        alu_op2.assign(subStruct(req_data, "rs1_rdata"))
        sra_gen(alu_result, alu_op1, alu_op2)
        resp_data.assign(hw_fracs(hw_frac_SubStruct("wdata")), alu_result)
    }
}

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

class test_multiexu(name : String) : reordex.MultiExu(name, Exu_cfg_rf, MultiExu_CFG_RF(32, true, 64), 4) {

    init {
        //add_exu(EXU_ADD(2), 2)
        //add_exu(EXU_XOR(4), 1)
        //add_exu(EXU_SHIFT(1), 1)

        add_exu(EXU_FP_ADD_SUB(), 2, 4)
        add_exu(EXU_FP_MUL(), 1, 3)
        add_exu(EXU_FP_DIV(), 1, 3)
        add_exu(EXU_FP_FMA(), 1, 3)
    }
}