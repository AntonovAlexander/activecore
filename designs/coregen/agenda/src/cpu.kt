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

class CPU_CFG() : Reordex_CFG(32, 32, true,64, 16, REORDEX_MODE.RISC)
{
    var opcode = AddSrcUImm("opcode", 6)

    var rs0 = AddRs()
    var rs1 = AddRs()
    var rs2 = AddRs()

    var rd0 = AddRd()
}
val CPU_CFG_inst = CPU_CFG()

class EXU_ALU_INTEGER() : reordex.Exu("INTEGER", CPU_CFG_inst) {

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

    var alu_op0         = ulocal("alu_op0", 31, 0, "0")
    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op0_wide    = ulocal("alu_op0_wide", 32, 0, "0")
    var alu_op1_wide    = ulocal("alu_op1_wide", 32, 0, "0")
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
        alu_op0.assign(subStruct(req_data, "rs0_rdata"))
        alu_op1.assign(subStruct(req_data, "rs1_rdata"))
        alu_opcode.assign(subStruct(req_data, "opcode"))

        begif(alu_unsigned)
        run {
            alu_op0_wide.assign(zeroext(alu_op0, 33))
            alu_op1_wide.assign(zeroext(alu_op1, 33))
        }; endif()
        begelse()
        run {
            alu_op0_wide.assign(signext(alu_op0, 33))
            alu_op1_wide.assign(signext(alu_op1, 33))
        }; endif()

        alu_result_wide.assign(alu_op0_wide)

        // computing result
        begcase(alu_opcode)
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
        alu_CF.assign(alu_result_wide[32])
        alu_SF.assign(alu_result_wide[31])
        alu_ZF.assign(bnot(ror(alu_result)))
        alu_OF.assign(bor(band(!alu_op0[31], band(!alu_op1[31], alu_result[31])), band(alu_op0[31], band(alu_op1[31], !alu_result[31]))))

        begif(alu_unsigned)
        run {
            alu_overflow.assign(alu_CF)
        }; endif()
        begelse()
        run {
            alu_overflow.assign(alu_OF)
        }; endif()

        rd0.assign(alu_result)
    }
}

class EXU_MUL_DIV() : reordex.Exu("MUL_DIV", CPU_CFG_inst) {

    // ALU opcodes
    val aluop_MUL		= 0
    val aluop_DIV		= 1

    var alu_op0         = ulocal("alu_op0", 31, 0, "0")
    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op0_wide    = ulocal("alu_op0_wide", 32, 0, "0")
    var alu_op1_wide    = ulocal("alu_op1_wide", 32, 0, "0")
    var alu_opcode      = ulocal("alu_opcode", 3, 0, "0")
    var alu_result_wide = ulocal("alu_result_wide", 32, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")

    init {
        alu_op0.assign(subStruct(req_data, "rs0_rdata"))
        alu_op1.assign(subStruct(req_data, "rs1_rdata"))
        alu_opcode.assign(subStruct(req_data, "opcode"))

        begcase(alu_opcode)
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
    }
}

class EXU_LSU() : reordex.Exu("LSU", CPU_CFG_inst) {

    // ALU opcodes
    val op_LD		= 0
    val op_ST		= 1

    var mem_addr        = ulocal("mem_addr", 31, 0, "0")
    var mem_wdata       = ulocal("mem_wdata", 31, 0, "0")
    var mem_rdata       = ulocal("mem_rdata", 3, 0, "0")
    var mem_opcode      = ulocal("mem_opcode", 3, 0, "0")

    init {
        mem_addr.assign(subStruct(req_data, "rs0_rdata"))
        mem_wdata.assign(subStruct(req_data, "rs1_rdata"))
        mem_opcode.assign(subStruct(req_data, "opcode"))

        begcase(CPU_CFG_inst.opcode)
        run {
            begbranch(op_LD)
            run {
                exec_load(mem_rdata, mem_addr)
            }; endbranch()

            begbranch(op_ST)
            run {
                exec_store(mem_addr, mem_wdata)
            }; endbranch()
        }; endcase()
    }
}

class EXU_FP_ADD_SUB() : reordex.Exu("FP_ADD_SUB", CPU_CFG_inst) {

    init {
        begif(eq2(CPU_CFG_inst.opcode, 0))
        run {
            rd0.assign(rss[0] + rss[1])
        }; endif()
        begelse()
        run {
            rd0.assign(rss[0] - rss[1])
        }; endif()
    }
}

class EXU_FP_MUL() : reordex.Exu("FP_MUL", CPU_CFG_inst) {

    init {
        rd0.assign(rss[0] * rss[1])
    }
}

class EXU_FP_DIV() : reordex.Exu("FP_DIV", CPU_CFG_inst) {

    init {
        rd0.assign(rss[0] / rss[1])
    }
}

class EXU_FP_FMA() : reordex.Exu("FP_FMA", CPU_CFG_inst) {

    init {
        rd0.assign((rss[0] * rss[1]) + rss[2])
    }
}

class cpu(name : String) : reordex.MultiExu(name, CPU_CFG_inst, 4) {

    init {
        add_exu(EXU_ALU_INTEGER(), 2, 4, STREAM_PREF_IMPL.RTL)
        add_exu(EXU_MUL_DIV(), 1, 3, STREAM_PREF_IMPL.RTL)

        //add_exu(EXU_FP_ADD_SUB(), 2, 4, STREAM_PREF_IMPL.RTL)
        //add_exu(EXU_FP_MUL(), 1, 3, STREAM_PREF_IMPL.RTL)
        //add_exu(EXU_FP_DIV(), 1, 3, STREAM_PREF_IMPL.RTL)
        //add_exu(EXU_FP_FMA(), 1, 3, STREAM_PREF_IMPL.RTL)
    }
}