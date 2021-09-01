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

class CPU_CFG() : Reordex_CFG(32, 32, true,48, 16, REORDEX_MODE.RISC)
{
    var exu_opcode      = AddSrcUImm("exu_opcode", 6)
    var alu_unsigned    = AddSrcUImm("alu_unsigned", 1)

    var src0 = AddSrc()
    var src1 = AddSrc()

    var alu_CF          = AddDstUImm("alu_CF", 1)
    var alu_SF          = AddDstUImm("alu_SF", 1)
    var alu_ZF          = AddDstUImm("alu_ZF", 1)
    var alu_OF          = AddDstUImm("alu_OF", 1)

    var rd0 = AddRd()
}
val CPU_CFG_inst = CPU_CFG()

class RISCV_Decoder() : reordex.RISCDecoder(CPU_CFG_inst) {

    //// base opcodes ///////////

    val opcode_LOAD			= 0x03
    val opcode_LOAD_FP		= 0x07
    val opcode_MISC_MEM		= 0x0f
    val opcode_OP_IMM		= 0x13
    val opcode_AUIPC		= 0x17
    val opcode_OP_IMM_32	= 0x1b
    val opcode_STORE		= 0x23
    val opcode_STORE_FP		= 0x27
    val opcode_AMO			= 0x2f
    val opcode_OP			= 0x33
    val opcode_LUI			= 0x37
    val opcode_OP_32		= 0x3b
    val opcode_MADD			= 0x43
    val opcode_MSUB			= 0x47
    val opcode_NMSUB		= 0x4b
    val opcode_NMADD		= 0x4f
    val opcode_OP_FP		= 0x53
    val opcode_BRANCH		= 0x63
    val opcode_JALR			= 0x67
    val opcode_JAL			= 0x6f
    val opcode_SYSTEM		= 0x73

    val instrcode_MRET        = 0x30200073

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
    val aluop_SLT		= 9

    ///////////////////

    var immediate_I     = ugenvar("immediate_I", 31, 0, "0")
    var immediate_S     = ugenvar("immediate_S", 31, 0, "0")
    var immediate_B     = ugenvar("immediate_B", 31, 0, "0")
    var immediate_U     = ugenvar("immediate_U", 31, 0, "0")
    var immediate_J     = ugenvar("immediate_J", 31, 0, "0")

    init {
        //// instruction decoding ////
        opcode.assign(instr_code[6, 0])
        assign(CPU_CFG_inst.alu_unsigned, 0)

        rs0_addr.assign(instr_code[19, 15])
        rs1_addr.assign(instr_code[24, 20])
        rd_addr.assign(instr_code[11, 7])

        funct3.assign(instr_code[14, 12])
        funct7.assign(instr_code[31, 25])
        shamt.assign(instr_code[24, 20])
        pred.assign(instr_code[27, 24])
        succ.assign(instr_code[23, 20])
        csrnum.assign(instr_code[31, 20])
        zimm.assign(instr_code[19, 15])

        immediate_I.assign(signext(instr_code[31, 20], 32))

        var immediate_S_src = ArrayList<hw_param>()
        immediate_S_src.add(instr_code[31, 25])
        immediate_S_src.add(instr_code[11, 7])
        immediate_S.assign(signext(cnct(immediate_S_src), 32))

        var immediate_B_src = ArrayList<hw_param>()
        immediate_B_src.add(instr_code[31])
        immediate_B_src.add(instr_code[7])
        immediate_B_src.add(instr_code[30, 25])
        immediate_B_src.add(instr_code[11, 8])
        immediate_B_src.add(hw_imm(1, "0"))
        immediate_B.assign(signext(cnct(immediate_B_src), 32))

        var immediate_U_src = ArrayList<hw_param>()
        immediate_U_src.add(instr_code[31, 12])
        immediate_U_src.add(hw_imm(12, "0"))
        immediate_U.assign(cnct(immediate_U_src))

        var immediate_J_src = ArrayList<hw_param>()
        immediate_J_src.add(instr_code[31])
        immediate_J_src.add(instr_code[19, 12])
        immediate_J_src.add(instr_code[20])
        immediate_J_src.add(instr_code[30, 21])
        immediate_J_src.add(hw_imm(1, "0"))
        immediate_J.assign(signext(cnct(immediate_J_src), 32))

        begcase(opcode)
        run {
            begbranch(opcode_LUI)
            run {
                op0_source.assign(OP0_SRC_IMM)
                rd_req.assign(1)
                rd_source.assign(RD_LUI)
                immediate.assign(immediate_U)
            }
            endbranch()

            begbranch(opcode_AUIPC)
            run {
                op0_source.assign(OP0_SRC_PC)
                op1_source.assign(OP1_SRC_IMM)
                alu_req.assign(1)
                alu_opcode.assign(aluop_ADD)
                rd_req.assign(1)
                rd_source.assign(RD_ALU)
                immediate.assign(immediate_U)
            }; endbranch()

            begbranch(opcode_JAL)
            run {
                op0_source.assign(OP0_SRC_PC)
                op1_source.assign(OP1_SRC_IMM)
                alu_req.assign(1)
                alu_opcode.assign(aluop_ADD)
                rd_req.assign(1)
                rd_source.assign(RD_PC_INC)
                jump_req.assign(1)
                jump_src.assign(JMP_SRC_ALU)
                immediate.assign(immediate_J)
            }; endbranch()

            begbranch(opcode_JALR)
            run {
                rs0_req.assign(1)
                op0_source.assign(OP0_SRC_RS)
                op1_source.assign(OP1_SRC_IMM)
                alu_req.assign(1)
                alu_opcode.assign(aluop_ADD)
                rd_req.assign(1)
                rd_source.assign(RD_PC_INC)
                jump_req.assign(1)
                jump_src.assign(JMP_SRC_ALU)
                immediate.assign(immediate_I)
            }; endbranch()

            begbranch(opcode_BRANCH)
            run {
                rs0_req.assign(1)
                rs1_req.assign(1)
                alu_req.assign(1)
                alu_opcode.assign(aluop_SUB)
                jump_req_cond.assign(1)
                jump_src.assign(JMP_SRC_ALU)
                immediate.assign(immediate_B)

                begif(bor(eq2(funct3, 0x6), eq2(funct3, 0x7)))
                run {
                    assign(CPU_CFG_inst.alu_unsigned, 1)
                }; endif()
            }; endbranch()

            begbranch(opcode_LOAD)
            run {
                rs0_req.assign(1)
                op0_source.assign(OP0_SRC_RS)
                op1_source.assign(OP1_SRC_IMM)
                rd_req.assign(1)
                rd_source.assign(RD_MEM)
                alu_req.assign(1)
                mem_req.assign(1)
                mem_cmd.assign(0)
                immediate.assign(immediate_I)
            }; endbranch()

            begbranch(opcode_STORE)
            run {
                rs0_req.assign(1)
                rs1_req.assign(1)
                op0_source.assign(OP0_SRC_RS)
                op1_source.assign(OP1_SRC_IMM)
                alu_req.assign(1)
                mem_req.assign(1)
                mem_cmd.assign(1)
                immediate.assign(immediate_S)
            }; endbranch()

            begbranch(opcode_OP_IMM)
            run {
                rs0_req.assign(1)
                op0_source.assign(OP0_SRC_RS)
                op1_source.assign(OP1_SRC_IMM)
                rd_req.assign(1)
                immediate.assign(immediate_I)
                alu_req.assign(1)

                begcase(funct3)
                run {
                    // ADDI
                    begbranch(0x0)
                    run {
                        alu_opcode.assign(aluop_ADD)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // SLLI
                    begbranch(0x1)
                    run {
                        alu_opcode.assign(aluop_SLL)
                        rd_source.assign(RD_ALU)
                        immediate.assign(zeroext(instr_code[24, 20], 32))
                    }; endbranch()

                    // SLTI
                    begbranch(0x2)
                    run {
                        alu_opcode.assign(aluop_SLT)
                        rd_source.assign(RD_CF_COND)
                    }; endbranch()

                    // SLTIU
                    begbranch(0x3)
                    run {
                        alu_opcode.assign(aluop_SLT)
                        assign(CPU_CFG_inst.alu_unsigned, 1)
                        rd_source.assign(RD_CF_COND)
                    }; endbranch()

                    // XORI
                    begbranch(0x4)
                    run {
                        alu_opcode.assign(aluop_XOR)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // SRLI, SRAI
                    begbranch(0x5)
                    run {
                        // SRAI
                        begif(instr_code[30])
                        run {
                            alu_opcode.assign(aluop_SRA)
                        }; endif()

                        // SRLI
                        begelse()
                        run {
                            alu_opcode.assign(aluop_SRL)
                        }; endif()

                        rd_source.assign(RD_ALU)
                        immediate.assign(zeroext(instr_code[24, 20], 32))
                    }; endbranch()

                    // ORI
                    begbranch(0x6)
                    run {
                        alu_opcode.assign(aluop_OR)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // ANDI
                    begbranch(0x7)
                    run {
                        alu_opcode.assign(aluop_AND)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                }; endcase()
            }; endbranch()

            begbranch(opcode_OP)
            run {
                rs0_req.assign(1)
                rs1_req.assign(1)
                op0_source.assign(OP0_SRC_RS)
                op1_source.assign(OP1_SRC_RS)
                rd_req.assign(1)
                rd_source.assign(RD_ALU)
                alu_req.assign(1)

                begcase(funct3)
                run {
                    // ADD/SUB
                    begbranch(0x0)
                    run {
                        // SUB
                        begif(instr_code[30])
                        run {
                            alu_opcode.assign(aluop_SUB)
                        }; endif()

                        // ADD
                        begelse()
                        run {
                            alu_opcode.assign(aluop_ADD)
                        }; endif()
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // SLL
                    begbranch(0x1)
                    run {
                        alu_opcode.assign(aluop_SLL)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // SLT
                    begbranch(0x2)
                    run {
                        alu_opcode.assign(aluop_SLT)
                        rd_source.assign(RD_CF_COND)
                    }; endbranch()

                    // SLTU
                    begbranch(0x3)
                    run {
                        alu_opcode.assign(aluop_SLT)
                        assign(CPU_CFG_inst.alu_unsigned, 1)
                        rd_source.assign(RD_CF_COND)
                    }; endbranch()

                    // XORI
                    begbranch(0x4)
                    run {
                        alu_opcode.assign(aluop_XOR)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // SRL/SRA
                    begbranch(0x5)
                    run {
                        // SRA
                        begif(instr_code[30])
                        run {
                            alu_opcode.assign(aluop_SRA)
                        }; endif()
                        // SRL
                        begelse()
                        run {
                            alu_opcode.assign(aluop_SRL)
                        }; endif()
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // OR
                    begbranch(0x6)
                    run {
                        alu_opcode.assign(aluop_OR)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                    // AND
                    begbranch(0x7)
                    run {
                        alu_opcode.assign(aluop_AND)
                        rd_source.assign(RD_ALU)
                    }; endbranch()

                }; endcase()
            }; endbranch()

            begbranch(opcode_MISC_MEM)
            run {
                fencereq.assign(1)
            }; endbranch()

            begbranch(opcode_SYSTEM)
            run {
                begcase(funct3)
                run {
                    // EBREAK/ECALL
                    begbranch(0x0)
                    run {
                        // EBREAK
                        begif(instr_code[20])
                        run {
                            ebreakreq.assign(1)
                        }; endif()
                        // ECALL
                        begelse()
                        run {
                            ecallreq.assign(1)
                        }; endif()
                    }; endbranch()

                    //CSRRW
                    begbranch(0x1)
                    run {
                        csrreq.assign(1)
                        rs0_req.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        op0_source.assign(OP0_SRC_RS)
                        op1_source.assign(OP1_SRC_CSR)
                    }; endbranch()

                    // CSRRS
                    begbranch(0x2)
                    run {
                        csrreq.assign(1)
                        rs0_req.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_OR)
                        op0_source.assign(OP0_SRC_RS)
                        op1_source.assign(OP1_SRC_CSR)
                    }; endbranch()

                    // CSRRC
                    begbranch(0x3)
                    run {
                        csrreq.assign(1)
                        rs0_req.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_CLRB)
                        op0_source.assign(OP0_SRC_RS)
                        op1_source.assign(OP1_SRC_CSR)
                    }; endbranch()

                    // CSRRWI
                    begbranch(0x5)
                    run {
                        csrreq.assign(1)
                        rd_req.assign(1)
                        op0_source.assign(OP0_SRC_IMM)
                        op1_source.assign(OP1_SRC_CSR)
                        immediate.assign(zeroext(zimm, 32))
                    }; endbranch()

                    // CSRRSI
                    begbranch(0x6)
                    run {
                        csrreq.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_CLRB)
                        op0_source.assign(OP0_SRC_IMM)
                        op1_source.assign(OP1_SRC_CSR)
                        immediate.assign(zeroext(zimm, 32))
                    }; endbranch()

                    // CSRRSI
                    begbranch(0x7)
                    run {
                        csrreq.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_CLRB)
                        op0_source.assign(OP0_SRC_IMM)
                        op1_source.assign(OP1_SRC_CSR)
                        immediate.assign(zeroext(zimm, 32))
                    }; endbranch()
                }; endcase()
            }; endbranch()

        }; endcase()

        curinstraddr_imm.assign(curinstr_addr + immediate)

        begif(mem_req)
        run {
            begcase(funct3)
            run {
                begbranch(0x0)
                run {
                    mem_be.assign(0x1)
                    load_signext.assign(1)
                }; endbranch()

                begbranch(0x1)
                run {
                    mem_be.assign(0x3)
                    load_signext.assign(1)
                }; endbranch()

                begbranch(0x2)
                run {
                    mem_be.assign(0xf)
                }; endbranch()

                begbranch(0x4)
                run {
                    mem_be.assign(0x1)
                }; endbranch()

                begbranch(0x5)
                run {
                    mem_be.assign(0x3)
                }; endbranch()
            }; endcase()
        }; endif()

        begif(eq2(instr_code, instrcode_MRET))
        run {
            mret_req.assign(1)
            jump_req.assign(1)
            jump_req_cond.assign(0)
            jump_src.assign(JMP_SRC_IMM)
            immediate.assign(MRETADDR)
        }; endif()

        begif(eq2(rs0_addr, 0))
        run {
            rs0_req.assign(0)
        }; endif()

        begif(eq2(rs1_addr, 0))
        run {
            rs1_req.assign(0)
        }; endif()

        begif(eq2(rd_addr, 0))
        run {
            rd_req.assign(0)
        }; endif()

        ////

        begif(csrreq)
        run {
            csr_rdata.assign(CSR_MCAUSE)
        }; endif()

        begcase(op0_source)
        run {

            begbranch(OP0_SRC_RS)
            run {
                begif(eq2(rs0_addr, 0))
                run {
                    SrcSetImm(CPU_CFG_inst.src0, hw_imm(0))
                }; endif()
                begelse()
                run {
                    SrcReadReg(CPU_CFG_inst.src0, rs0_addr)
                }; endif()
            }; endbranch()

            begbranch(OP0_SRC_IMM)
            run {
                SrcSetImm(CPU_CFG_inst.src0, immediate)
            }; endbranch()

            begbranch(OP0_SRC_PC)
            run {
                SrcSetImm(CPU_CFG_inst.src0, curinstr_addr)
            }; endbranch()

        }; endcase()

        // TODO: cleanup

        begif(rs1_req)
        run {
            begif(eq2(rs1_addr, 0))
            run {
                SrcSetImm(CPU_CFG_inst.src1, hw_imm(0))
            }; endif()
            begelse()
            run {
                SrcReadReg(CPU_CFG_inst.src1, rs1_addr)
            }; endif()
        }; endif()

        begif (!mem_req)
        run {

            begcase(op1_source)
            run {

                begbranch(OP1_SRC_IMM)
                run {
                    SrcSetImm(CPU_CFG_inst.src1, immediate)
                }; endbranch()

                begbranch(OP1_SRC_CSR)
                run {
                    SrcSetImm(CPU_CFG_inst.src1, csr_rdata)
                }; endbranch()

            }; endcase()

        }; endif()
    }

}

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
    val aluop_SLT		= 9

    var alu_op0         = ulocal("alu_op0", 31, 0, "0")
    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op0_wide    = ulocal("alu_op0_wide", 32, 0, "0")
    var alu_op1_wide    = ulocal("alu_op1_wide", 32, 0, "0")
    var alu_opcode      = ulocal("alu_opcode", 3, 0, "0")

    var alu_result_wide = ulocal("alu_result_wide", 32, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")
    var alu_CF          = ulocal("alu_CF", 0, 0, "0")
    var alu_SF          = ulocal("alu_SF", 0, 0, "0")
    var alu_ZF          = ulocal("alu_ZF", 0, 0, "0")
    var alu_OF          = ulocal("alu_OF", 0, 0, "0")
    var alu_overflow    = ulocal("alu_overflow", 0, 0, "0")

    init {
        alu_op0.assign(CPU_CFG_inst.src0)
        alu_op1.assign(CPU_CFG_inst.src1)
        alu_opcode.assign(CPU_CFG_inst.exu_opcode)

        begif(CPU_CFG_inst.alu_unsigned)
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

            begbranch(aluop_SLT)
            run {
                alu_result_wide.assign(alu_op0_wide - alu_op1_wide)
            }; endbranch()
        }; endcase()

        // formation of result and flags
        alu_result.assign(alu_result_wide[31, 0])
        alu_CF.assign(alu_result_wide[32])
        alu_SF.assign(alu_result_wide[31])
        alu_ZF.assign(bnot(ror(alu_result)))
        alu_OF.assign(bor(band(!alu_op0[31], band(!alu_op1[31], alu_result[31])), band(alu_op0[31], band(alu_op1[31], !alu_result[31]))))

        begif(eq2(alu_opcode, aluop_SLT))
        run {
            alu_result.assign(alu_CF)
        }; endif()

        begif(CPU_CFG_inst.alu_unsigned)
        run {
            alu_overflow.assign(alu_CF)
        }; endif()
        begelse()
        run {
            alu_overflow.assign(alu_OF)
        }; endif()

        rd0.assign(alu_result)
        assign(CPU_CFG_inst.alu_CF, alu_CF)
        assign(CPU_CFG_inst.alu_SF, alu_SF)
        assign(CPU_CFG_inst.alu_ZF, alu_ZF)
        assign(CPU_CFG_inst.alu_OF, alu_OF)
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
        alu_op0.assign(CPU_CFG_inst.src0)
        alu_op1.assign(CPU_CFG_inst.src1)
        alu_opcode.assign(CPU_CFG_inst.exu_opcode)

        begif(CPU_CFG_inst.alu_unsigned)
        run {
            alu_op0_wide.assign(zeroext(alu_op0, 33))
            alu_op1_wide.assign(zeroext(alu_op1, 33))
        }; endif()
        begelse()
        run {
            alu_op0_wide.assign(signext(alu_op0, 33))
            alu_op1_wide.assign(signext(alu_op1, 33))
        }; endif()

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

        rd0.assign(alu_result)
    }
}

class EXU_FP_ADD_SUB() : reordex.Exu("FP_ADD_SUB", CPU_CFG_inst) {

    init {
        begif(eq2(CPU_CFG_inst.exu_opcode, 0))
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

class cpu(name : String) : reordex.MultiExuRISC(name, CPU_CFG_inst, 4, RISCV_Decoder()) {

    init {
        add_exu(EXU_ALU_INTEGER(), 2, 4, STREAM_PREF_IMPL.RTL)
        add_exu(EXU_MUL_DIV(), 1, 3, STREAM_PREF_IMPL.RTL)

        //add_exu(EXU_FP_ADD_SUB(), 2, 4, STREAM_PREF_IMPL.RTL)
        //add_exu(EXU_FP_MUL(), 1, 3, STREAM_PREF_IMPL.RTL)
        //add_exu(EXU_FP_DIV(), 1, 3, STREAM_PREF_IMPL.RTL)
        //add_exu(EXU_FP_FMA(), 1, 3, STREAM_PREF_IMPL.RTL)
    }
}