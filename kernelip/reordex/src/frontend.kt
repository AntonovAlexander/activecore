/*
 * frontend.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

class instr_req_stage(val name : String, val cyclix_gen : cyclix.Generic, val instr_fetch : instr_fetch_buffer) : hw_imm(0) {

    var pc = cyclix_gen.uglobal("pc", 31, 0, hw_imm(32, IMM_BASE_TYPE.HEX, "200"))

    var busreq_mem_struct = hw_struct(name + "_busreq_mem_struct")
    val instr_name_prefix = "genmcopipe_instr_mem_"

    var wr_struct = hw_struct("genpmodule_" + name + "_" + instr_name_prefix + "genstruct_fifo_wdata")
    var instr_req_fifo = cyclix_gen.fifo_out((instr_name_prefix + "req"), wr_struct)

    fun Process(instr_fetch : instr_fetch_buffer) {

        var new_fetch_buf = instr_fetch.GetPushTrx()
        var instr_data_wdata = cyclix_gen.local("instr_data_wdata", instr_req_fifo.vartype, "0")

        busreq_mem_struct.addu("addr",     31, 0, "0")
        busreq_mem_struct.addu("be",       3,  0, "0")
        busreq_mem_struct.addu("wdata",    31, 0, "0")

        wr_struct.addu("we", 0, 0, "0")
        wr_struct.add("wdata", hw_type(busreq_mem_struct), "0")


        cyclix_gen.assign(new_fetch_buf.GetFracRef("curinstr_addr"), pc)

        cyclix_gen.begif(instr_fetch.ctrl_rdy)
        run {
            cyclix_gen.assign(instr_data_wdata.GetFracRef("we"), 0)
            cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("addr"), pc)
            cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("be"), 15)
            cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("wdata"), 0)
            cyclix_gen.fifo_wr_unblk(instr_req_fifo, instr_data_wdata)

            cyclix_gen.add_gen(pc, pc, 4)

            cyclix_gen.assign(new_fetch_buf.GetFracRef("enb"), 1)
            cyclix_gen.assign(new_fetch_buf.GetFracRef("nextinstr_addr"), pc)

            cyclix_gen.assign(instr_fetch.push, 1)
            instr_fetch.push_trx(new_fetch_buf)
        }; cyclix_gen.endif()
    }
}

class instr_fetch_buffer(name: String,
                         cyclix_gen : cyclix.Generic,
                         name_prefix : String,
                         TRX_BUF_SIZE : Int,
                         MultiExu_CFG : Reordex_CFG,
                         val global_structures: __global_structures) : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG) {

    val curinstr_addr  = AdduStageVar("curinstr_addr", 31, 0, "0")
    val nextinstr_addr = AdduStageVar("nextinstr_addr", 31, 0, "0")

    val instr_name_prefix = "genmcopipe_instr_mem_"
    var instr_resp_fifo = cyclix_gen.ufifo_in((instr_name_prefix + "resp"), 31, 0)

    var instr_code      = AddLocal("instr_code", instr_resp_fifo.vartype, "0")

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

    // op1 sources
    val OP0_SRC_RS1     = 0
    val OP0_SRC_IMM     = 1
    val OP0_SRC_PC 	    = 2
    // op2 sources
    val OP1_SRC_RS2     = 0
    val OP1_SRC_IMM     = 1
    val OP1_SRC_CSR     = 2

    // rd sources
    val RD_LUI		    = 0
    val RD_ALU		    = 1
    val RD_CF_COND	    = 2
    val RD_OF_COND	    = 3
    val RD_PC_INC	    = 4
    val RD_MEM		    = 5
    val RD_CSR		    = 6

    // jmp sources
    val JMP_SRC_IMM     = 0
    val JMP_SRC_ALU     = 1

    ///////////////////////

    // opcode signals
    var opcode          = AdduLocal("opcode", 6, 0, aluop_ADD.toString())

    // control transfer signals
    var jump_req        = AdduLocal("jump_req", 0, 0, "0")
    var jump_req_cond   = AdduLocal("jump_req_cond", 0, 0, "0")
    var jump_src        = AdduLocal("jump_src", 0, 0, JMP_SRC_IMM.toString())
    var jump_vector     = AdduLocal("jump_vector", 31, 0, "0")

    // regfile control signals
    var rs0_req         = AdduLocal("rs0_req", 0, 0, "0")
    var rs0_addr        = AdduLocal("rs0_addr", 4, 0, "0")
    var rs0_rdata       = AdduLocal("rs0_rdata", 31, 0, "0")

    var rs1_req         = AdduLocal("rs1_req", 0, 0, "0")
    var rs1_addr        = AdduLocal("rs1_addr", 4, 0, "0")
    var rs1_rdata       = AdduLocal("rs1_rdata", 31, 0, "0")

    //var rs2_req         = AdduLocal("rs2_req", 0, 0, "0")
    //var rs2_addr        = AdduLocal("rs2_addr", 4, 0, "0")
    //var rs2_rdata       = AdduLocal("rs2_rdata", 31, 0, "0")

    var csr_rdata       = AdduLocal("csr_rdata", 31, 0, "0")

    var rd_req          = AdduLocal("rd_req", 0, 0, "0")
    var rd_source       = AdduLocal("rd_source", 2, 0, RD_ALU.toString())
    var rd_addr         = AdduLocal("rd_addr", 4, 0, "0")
    var rd_wdata        = AdduLocal("rd_wdata", 31, 0, "0")
    var rd_rdy          = AdduLocal("rd_rdy", 0, 0, "0")

    var immediate_I     = AdduLocal("immediate_I", 31, 0, "0")
    var immediate_S     = AdduLocal("immediate_S", 31, 0, "0")
    var immediate_B     = AdduLocal("immediate_B", 31, 0, "0")
    var immediate_U     = AdduLocal("immediate_U", 31, 0, "0")
    var immediate_J     = AdduLocal("immediate_J", 31, 0, "0")

    var immediate       = AdduLocal("immediate", 31, 0, "0")

    var curinstraddr_imm    = AdduLocal("curinstraddr_imm", 31, 0, "0")

    var funct3          = AdduLocal("funct3", 2, 0, "0")
    var funct7          = AdduLocal("funct7", 6, 0, "0")
    var shamt           = AdduLocal("shamt", 4, 0, "0")

    var fencereq        = AdduLocal("fencereq", 0, 0, "0")
    var pred            = AdduLocal("pred", 3, 0, "0")
    var succ            = AdduLocal("succ", 3, 0, "0")

    var ecallreq        = AdduLocal("ecallreq", 0, 0, "0")
    var ebreakreq       = AdduLocal("ebreakreq", 0, 0, "0")

    var csrreq          = AdduLocal("csrreq", 0, 0, "0")
    var csrnum          = AdduLocal("csrnum", 11, 0, "0")
    var zimm            = AdduLocal("zimm", 4, 0, "0")

    var op0_source      = AdduLocal("op0_source", 1, 0, OP0_SRC_RS1.toString())
    var op1_source      = AdduLocal("op1_source", 1, 0, OP1_SRC_RS2.toString())

    // ALU control
    var alu_req         = AdduLocal("alu_req", 0, 0, "0")
    var alu_op1         = AdduLocal("alu_op1", 31, 0, "0")
    var alu_op2         = AdduLocal("alu_op2", 31, 0, "0")
    var alu_op1_wide    = AdduLocal("alu_op1_wide", 32, 0, "0")
    var alu_op2_wide    = AdduLocal("alu_op2_wide", 32, 0, "0")
    var alu_opcode      = AdduLocal("alu_opcode", 3, 0, "0")
    var alu_unsigned    = AdduLocal("alu_unsigned", 0, 0, "0")

    var alu_result_wide = AdduLocal("alu_result_wide", 32, 0, "0")
    var alu_result      = AdduLocal("alu_result", 31, 0, "0")
    var alu_CF          = AdduLocal("alu_CF", 0, 0, "0")
    var alu_SF          = AdduLocal("alu_SF", 0, 0, "0")
    var alu_ZF          = AdduLocal("alu_ZF", 0, 0, "0")
    var alu_OF          = AdduLocal("alu_OF", 0, 0, "0")
    var alu_overflow    = AdduLocal("alu_overflow", 0, 0, "0")

    // data memory control
    var mem_req         = AdduLocal("mem_req", 0, 0, "0")
    var mem_cmd         = AdduLocal("mem_cmd", 0, 0, "0")
    var mem_addr        = AdduLocal("mem_addr", 31, 0, "0")
    var mem_be          = AdduLocal("mem_be", 3, 0, "0")
    var mem_wdata       = AdduLocal("mem_wdata", 31, 0, "0")
    var mem_rdata       = AdduLocal("mem_rdata", 31, 0, "0")
    var mem_rshift      = AdduLocal("mem_rshift", 0, 0, "0")
    var load_signext    = AdduLocal("load_signext", 0, 0, "0")

    var mret_req        = AdduLocal("mret_req", 0, 0, "0")

    var MRETADDR        = cyclix_gen.uglobal("MRETADDR", 31, 0, "0")

    //////////
    var rs0_rdy         = AdduLocal("rs0_rdy", 0, 0, "0")
    var rs0_tag         = AdduLocal("rs0_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")

    var rs1_rdy         = AdduLocal("rs1_rdy", 0, 0, "0")
    var rs1_tag         = AdduLocal("rs1_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")

    var rd_tag          = AdduLocal("rd0_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")

    fun Process(renamed_uop_buf : rename_buffer) {

        var new_renamed_uop = renamed_uop_buf.GetPushTrx()

        // instruction fetch/decode

        preinit_ctrls()
        init_locals()

        cyclix_gen.begif(ctrl_active)
        run {

            cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(instr_resp_fifo, instr_code))
            run {

                //// instruction decoding ////

                opcode.assign(instr_code[6, 0])
                alu_unsigned.assign(0)

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

                immediate_I.assign(cyclix_gen.signext(instr_code[31, 20], 32))

                var immediate_S_src = ArrayList<hw_param>()
                immediate_S_src.add(instr_code[31, 25])
                immediate_S_src.add(instr_code[11, 7])
                immediate_S.assign(cyclix_gen.signext(cyclix_gen.cnct(immediate_S_src), 32))

                var immediate_B_src = ArrayList<hw_param>()
                immediate_B_src.add(instr_code[31])
                immediate_B_src.add(instr_code[7])
                immediate_B_src.add(instr_code[30, 25])
                immediate_B_src.add(instr_code[11, 8])
                immediate_B_src.add(hw_imm(1, "0"))
                immediate_B.assign(cyclix_gen.signext(cyclix_gen.cnct(immediate_B_src), 32))

                var immediate_U_src = ArrayList<hw_param>()
                immediate_U_src.add(instr_code[31, 12])
                immediate_U_src.add(hw_imm(12, "0"))
                immediate_U.assign(cyclix_gen.cnct(immediate_U_src))

                var immediate_J_src = ArrayList<hw_param>()
                immediate_J_src.add(instr_code[31])
                immediate_J_src.add(instr_code[19, 12])
                immediate_J_src.add(instr_code[20])
                immediate_J_src.add(instr_code[30, 21])
                immediate_J_src.add(hw_imm(1, "0"))
                immediate_J.assign(cyclix_gen.signext(cyclix_gen.cnct(immediate_J_src), 32))

                cyclix_gen.begcase(opcode)
                run {
                    cyclix_gen.begbranch(opcode_LUI)
                    run {
                        op0_source.assign(OP0_SRC_IMM)
                        rd_req.assign(1)
                        rd_source.assign(RD_LUI)
                        immediate.assign(immediate_U)
                    }
                    cyclix_gen.endbranch()

                    cyclix_gen.begbranch(opcode_AUIPC)
                    run {
                        op0_source.assign(OP0_SRC_PC)
                        op1_source.assign(OP1_SRC_IMM)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_ADD)
                        rd_req.assign(1)
                        rd_source.assign(RD_ALU)
                        immediate.assign(immediate_U)
                    }; cyclix_gen.endbranch()

                    cyclix_gen.begbranch(opcode_JAL)
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
                    }; cyclix_gen.endbranch()

                    cyclix_gen.begbranch(opcode_JALR)
                    run {
                        rs0_req.assign(1)
                        op0_source.assign(OP0_SRC_RS1)
                        op1_source.assign(OP1_SRC_IMM)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_ADD)
                        rd_req.assign(1)
                        rd_source.assign(RD_PC_INC)
                        jump_req.assign(1)
                        jump_src.assign(JMP_SRC_ALU)
                        immediate.assign(immediate_I)
                    }; cyclix_gen.endbranch()

                    cyclix_gen.begbranch(opcode_BRANCH)
                    run {
                        rs0_req.assign(1)
                        rs1_req.assign(1)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_SUB)
                        jump_req_cond.assign(1)
                        jump_src.assign(JMP_SRC_ALU)
                        immediate.assign(immediate_B)

                        cyclix_gen.begif(cyclix_gen.bor(cyclix_gen.eq2(funct3, 0x6), cyclix_gen.eq2(funct3, 0x7)))
                        run {
                            alu_unsigned.assign(1)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endbranch()

                    cyclix_gen.begbranch(opcode_LOAD)
                    run {
                        rs0_req.assign(1)
                        op0_source.assign(OP0_SRC_RS1)
                        op1_source.assign(OP1_SRC_IMM)
                        rd_req.assign(1)
                        rd_source.assign(RD_MEM)
                        alu_req.assign(1)
                        mem_req.assign(1)
                        mem_cmd.assign(0)
                        immediate.assign(immediate_I)
                    }; cyclix_gen.endbranch()

                    cyclix_gen.begbranch(opcode_STORE)
                    run {
                        rs0_req.assign(1)
                        rs1_req.assign(1)
                        op0_source.assign(OP0_SRC_RS1)
                        op1_source.assign(OP1_SRC_IMM)
                        alu_req.assign(1)
                        mem_req.assign(1)
                        mem_cmd.assign(1)
                        immediate.assign(immediate_S)
                    }; cyclix_gen.endbranch()

                    cyclix_gen.begbranch(opcode_OP_IMM)
                    run {
                        rs0_req.assign(1)
                        op0_source.assign(OP0_SRC_RS1)
                        op1_source.assign(OP1_SRC_IMM)
                        rd_req.assign(1)
                        immediate.assign(immediate_I)
                        alu_req.assign(1)

                        cyclix_gen.begcase(funct3)
                        run {
                            // ADDI
                            cyclix_gen.begbranch(0x0)
                            run {
                                alu_opcode.assign(aluop_ADD)
                                rd_source.assign(RD_ALU)
                            }; cyclix_gen.endbranch()

                            // SLLI
                            cyclix_gen.begbranch(0x1)
                            run {
                                alu_opcode.assign(aluop_SLL)
                                rd_source.assign(RD_ALU)
                                immediate.assign(cyclix_gen.zeroext(instr_code[24, 20], 32))
                            }; cyclix_gen.endbranch()

                            // SLTI
                            cyclix_gen.begbranch(0x2)
                            run {
                                alu_opcode.assign(aluop_SUB)
                                rd_source.assign(RD_CF_COND)
                            }; cyclix_gen.endbranch()

                            // SLTIU
                            cyclix_gen.begbranch(0x3)
                            run {
                                alu_opcode.assign(aluop_SUB)
                                alu_unsigned.assign(1)
                                rd_source.assign(RD_CF_COND)
                            }; cyclix_gen.endbranch()

                            // XORI
                            cyclix_gen.begbranch(0x4)
                            run {
                                alu_opcode.assign(aluop_XOR)
                                rd_source.assign(RD_ALU)
                            }; cyclix_gen.endbranch()

                            // SRLI, SRAI
                            cyclix_gen.begbranch(0x5)
                            run {
                                // SRAI
                                cyclix_gen.begif(instr_code[30])
                                run {
                                    alu_opcode.assign(aluop_SRA)
                                }; cyclix_gen.endif()

                                // SRLI
                                cyclix_gen.begelse()
                                run {
                                    alu_opcode.assign(aluop_SRL)
                                }; cyclix_gen.endif()

                                rd_source.assign(RD_ALU)
                                immediate.assign(cyclix_gen.zeroext(instr_code[24, 20], 32))
                            }; cyclix_gen.endbranch()

                            // ORI
                            cyclix_gen.begbranch(0x6)
                            run {
                                alu_opcode.assign(aluop_OR)
                                rd_source.assign(RD_ALU)
                            }; cyclix_gen.endbranch()

                            // ANDI
                            cyclix_gen.begbranch(0x7)
                            run {
                                alu_opcode.assign(aluop_AND)
                                rd_source.assign(RD_ALU)
                            }; cyclix_gen.endbranch()

                        }; cyclix_gen.endcase()
                    }; cyclix_gen.endbranch()

                    cyclix_gen.begbranch(opcode_OP)
                    run {
                        rs0_req.assign(1)
                        rs1_req.assign(1)
                        op0_source.assign(OP0_SRC_RS1)
                        op1_source.assign(OP1_SRC_RS2)
                        rd_req.assign(1)
                        rd_source.assign(RD_ALU)
                        alu_req.assign(1)

                        cyclix_gen.begcase(funct3)
                        run {
                            // ADD/SUB
                            cyclix_gen.begbranch(0x0)
                            run {
                                // SUB
                                cyclix_gen.begif(instr_code[30])
                                run {
                                    alu_opcode.assign(aluop_SUB)
                                }; cyclix_gen.endif()

                                // ADD
                                cyclix_gen.begelse()
                                run {
                                    alu_opcode.assign(aluop_ADD)
                                }; cyclix_gen.endif()
                                rd_source.assign(RD_ALU)
                            }; cyclix_gen.endbranch()

                            // SLL
                            cyclix_gen.begbranch(0x1)
                            run {
                                alu_opcode.assign(aluop_SLL)
                                rd_source.assign(RD_ALU)
                            }; cyclix_gen.endbranch()

                            // SLT
                            cyclix_gen.begbranch(0x2)
                            run {
                                alu_opcode.assign(aluop_SUB)
                                rd_source.assign(RD_CF_COND)
                            }; cyclix_gen.endbranch()

                            // SLTU
                            cyclix_gen.begbranch(0x3)
                            run {
                                alu_opcode.assign(aluop_SUB)
                                alu_unsigned.assign(1)
                                rd_source.assign(RD_CF_COND)
                            }; cyclix_gen.endbranch()

                            // XORI
                            cyclix_gen.begbranch(0x4)
                            run {
                                alu_opcode.assign(aluop_XOR)
                                rd_source.assign(RD_ALU)
                            }; cyclix_gen.endbranch()

                            // SRL/SRA
                            cyclix_gen.begbranch(0x5)
                            run {
                                // SRA
                                cyclix_gen.begif(instr_code[30])
                                run {
                                    alu_opcode.assign(aluop_SRA)
                                }; cyclix_gen.endif()
                                // SRL
                                cyclix_gen.begelse()
                                run {
                                    alu_opcode.assign(aluop_SRL)
                                }; cyclix_gen.endif()
                                rd_source.assign(RD_ALU)
                            }; cyclix_gen.endbranch()

                            // OR
                            cyclix_gen.begbranch(0x6)
                            run {
                                alu_opcode.assign(aluop_OR)
                                rd_source.assign(RD_ALU)
                            }; cyclix_gen.endbranch()

                            // AND
                            cyclix_gen.begbranch(0x7)
                            run {
                                alu_opcode.assign(aluop_AND)
                                rd_source.assign(RD_ALU)
                            }; cyclix_gen.endbranch()

                        }; cyclix_gen.endcase()
                    }; cyclix_gen.endbranch()

                    cyclix_gen.begbranch(opcode_MISC_MEM)
                    run {
                        fencereq.assign(1)
                    }; cyclix_gen.endbranch()

                    cyclix_gen.begbranch(opcode_SYSTEM)
                    run {
                        cyclix_gen.begcase(funct3)
                        run {
                            // EBREAK/ECALL
                            cyclix_gen.begbranch(0x0)
                            run {
                                // EBREAK
                                cyclix_gen.begif(instr_code[20])
                                run {
                                    ebreakreq.assign(1)
                                }; cyclix_gen.endif()
                                // ECALL
                                cyclix_gen.begelse()
                                run {
                                    ecallreq.assign(1)
                                }; cyclix_gen.endif()
                            }; cyclix_gen.endbranch()

                            //CSRRW
                            cyclix_gen.begbranch(0x1)
                            run {
                                csrreq.assign(1)
                                rs0_req.assign(1)
                                rd_req.assign(1)
                                rd_source.assign(RD_CSR)
                                op0_source.assign(OP0_SRC_RS1)
                                op1_source.assign(OP1_SRC_CSR)
                            }; cyclix_gen.endbranch()

                            // CSRRS
                            cyclix_gen.begbranch(0x2)
                            run {
                                csrreq.assign(1)
                                rs0_req.assign(1)
                                rd_req.assign(1)
                                rd_source.assign(RD_CSR)
                                alu_req.assign(1)
                                alu_opcode.assign(aluop_OR)
                                op0_source.assign(OP0_SRC_RS1)
                                op1_source.assign(OP1_SRC_CSR)
                            }; cyclix_gen.endbranch()

                            // CSRRC
                            cyclix_gen.begbranch(0x3)
                            run {
                                csrreq.assign(1)
                                rs0_req.assign(1)
                                rd_req.assign(1)
                                rd_source.assign(RD_CSR)
                                alu_req.assign(1)
                                alu_opcode.assign(aluop_CLRB)
                                op0_source.assign(OP0_SRC_RS1)
                                op1_source.assign(OP1_SRC_CSR)
                            }; cyclix_gen.endbranch()

                            // CSRRWI
                            cyclix_gen.begbranch(0x5)
                            run {
                                csrreq.assign(1)
                                rd_req.assign(1)
                                op0_source.assign(OP0_SRC_IMM)
                                op1_source.assign(OP1_SRC_CSR)
                                immediate.assign(cyclix_gen.zeroext(zimm, 32))
                            }; cyclix_gen.endbranch()

                            // CSRRSI
                            cyclix_gen.begbranch(0x6)
                            run {
                                csrreq.assign(1)
                                rd_req.assign(1)
                                rd_source.assign(RD_CSR)
                                alu_req.assign(1)
                                alu_opcode.assign(aluop_CLRB)
                                op0_source.assign(OP0_SRC_IMM)
                                op1_source.assign(OP1_SRC_CSR)
                                immediate.assign(cyclix_gen.zeroext(zimm, 32))
                            }; cyclix_gen.endbranch()

                            // CSRRSI
                            cyclix_gen.begbranch(0x7)
                            run {
                                csrreq.assign(1)
                                rd_req.assign(1)
                                rd_source.assign(RD_CSR)
                                alu_req.assign(1)
                                alu_opcode.assign(aluop_CLRB)
                                op0_source.assign(OP0_SRC_IMM)
                                op1_source.assign(OP1_SRC_CSR)
                                immediate.assign(cyclix_gen.zeroext(zimm, 32))
                            }; cyclix_gen.endbranch()
                        }; cyclix_gen.endcase()
                    }; cyclix_gen.endbranch()

                }; cyclix_gen.endcase()

                curinstraddr_imm.assign(curinstr_addr + immediate)

                cyclix_gen.begif(mem_req)
                run {
                    cyclix_gen.begcase(funct3)
                    run {
                        cyclix_gen.begbranch(0x0)
                        run {
                            mem_be.assign(0x1)
                            load_signext.assign(1)
                        }; cyclix_gen.endbranch()

                        cyclix_gen.begbranch(0x1)
                        run {
                            mem_be.assign(0x3)
                            load_signext.assign(1)
                        }; cyclix_gen.endbranch()

                        cyclix_gen.begbranch(0x2)
                        run {
                            mem_be.assign(0xf)
                        }; cyclix_gen.endbranch()

                        cyclix_gen.begbranch(0x4)
                        run {
                            mem_be.assign(0x1)
                        }; cyclix_gen.endbranch()

                        cyclix_gen.begbranch(0x5)
                        run {
                            mem_be.assign(0x3)
                        }; cyclix_gen.endbranch()
                    }; cyclix_gen.endcase()
                }; cyclix_gen.endif()

                cyclix_gen.begif(cyclix_gen.eq2(instr_code, instrcode_MRET))
                run {
                    mret_req.assign(1)
                    jump_req.assign(1)
                    jump_req_cond.assign(0)
                    jump_src.assign(JMP_SRC_IMM)
                    immediate.assign(MRETADDR)
                }; cyclix_gen.endif()

                cyclix_gen.begif(cyclix_gen.eq2(rd_addr, 0))
                run {
                    rd_req.assign(0)
                }; cyclix_gen.endif()

                ////////////////////////

                var nru_rd_tag_prev     = new_renamed_uop.GetFracRef("rd_tag_prev")
                var nru_rd_tag_prev_clr = new_renamed_uop.GetFracRef("rd_tag_prev_clr")

                rs0_rdy.assign(1)
                cyclix_gen.begif(rs0_req)
                run {
                    rs0_tag.assign(global_structures.RenameReg(rs0_addr))
                    global_structures.FetchRs(rs0_rdata, rs0_tag)
                    rs0_rdy.assign(global_structures.FetchRsRdy(rs0_tag))
                }; cyclix_gen.endif()

                //// TODO: cleanup
                cyclix_gen.begif(cyclix_gen.eq2(op0_source, OP0_SRC_IMM))
                run {
                    rs0_rdata.assign(immediate)
                }; cyclix_gen.endif()

                cyclix_gen.begif(cyclix_gen.eq2(op0_source, OP0_SRC_PC))
                run {
                    rs0_rdata.assign(curinstr_addr)
                }; cyclix_gen.endif()
                ////

                rs1_rdy.assign(1)
                cyclix_gen.begif(rs1_req)
                run {
                    rs1_tag.assign(global_structures.RenameReg(rs1_addr))
                    global_structures.FetchRs(rs1_rdata, global_structures.RenameReg(rs1_addr))
                    rs1_rdy.assign(global_structures.FetchRsRdy(rs1_tag))
                }; cyclix_gen.endif()

                //// TODO: cleanup
                mem_wdata.assign(rs1_rdata)
                cyclix_gen.begif(cyclix_gen.eq2(op1_source, OP1_SRC_IMM))
                run {
                    rs1_rdata.assign(immediate)
                    rs1_rdy.assign(1)
                }; cyclix_gen.endif()
                ////

                // TODO: CSR

                cyclix_gen.assign(new_renamed_uop.GetFracRef("rs2_rdy"), 1)

                opcode.assign(alu_opcode)

                cyclix_gen.begif(rd_req)
                run {
                    cyclix_gen.assign(nru_rd_tag_prev, global_structures.RenameReg(rd_addr))
                    cyclix_gen.assign(nru_rd_tag_prev_clr, cyclix_gen.indexed(global_structures.PRF_mapped, nru_rd_tag_prev))

                    var alloc_rd_tag = global_structures.GetFreePRF()
                    cyclix_gen.assign(rd_tag, alloc_rd_tag.position)
                    global_structures.ReserveRd(rd_addr, rd_tag)      // TODO: "free not found" processing
                }; cyclix_gen.endif()

                cyclix_gen.assign_subStructs(new_renamed_uop, TRX_LOCAL)
                cyclix_gen.assign(new_renamed_uop.GetFracRef("exu_opcode"), alu_opcode)
                cyclix_gen.assign(new_renamed_uop.GetFracRef("rdy"), !alu_req)

                cyclix_gen.begif(renamed_uop_buf.ctrl_rdy)
                run {
                    cyclix_gen.assign(renamed_uop_buf.push, 1)
                    renamed_uop_buf.push_trx(new_renamed_uop)

                    cyclix_gen.assign(pop, 1)
                    pop_trx()
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()

        }; cyclix_gen.endif()
    }
}

class coproc_frontend(val name : String, val cyclix_gen : cyclix.Generic, val MultiExu_CFG : Reordex_CFG, val global_structures : __global_structures) {

    var cmd_req_struct = hw_struct(name + "_cmd_req_struct")

    var cmd_req = cyclix_gen.fifo_in("cmd_req",  hw_type(cmd_req_struct))

    var cmd_req_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_req_data"), cmd_req_struct)
    var cmd_resp_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_resp_data"), hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(MultiExu_CFG.RF_width-1, 0)), "0")

    fun Send_toRenameBuf(renamed_uop_buf : rename_buffer) {

        cmd_req_struct.addu("exec",     0, 0, "0")
        cmd_req_struct.addu("rf_we",       0,  0, "0")
        cmd_req_struct.addu("rf_addr",    MultiExu_CFG.ARF_addr_width-1, 0, "0")
        cmd_req_struct.addu("rf_wdata",    MultiExu_CFG.RF_width-1, 0, "0")
        cmd_req_struct.addu("fu_id",    GetWidthToContain(global_structures.ExecUnits.size)-1, 0, "0")
        for (imm_idx in 0 until MultiExu_CFG.src_imms.size) {
            cmd_req_struct.add("fu_imm_" + MultiExu_CFG.src_imms[imm_idx].name, MultiExu_CFG.src_imms[imm_idx].vartype, MultiExu_CFG.src_imms[imm_idx].defimm)
        }
        for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
            cmd_req_struct.addu("fu_rs" + RF_rs_idx, MultiExu_CFG.ARF_addr_width-1, 0, "0")
        }
        cmd_req_struct.addu("fu_rd",    MultiExu_CFG.ARF_addr_width-1, 0, "0")

        var new_renamed_uop     = renamed_uop_buf.GetPushTrx()
        var nru_enb             = new_renamed_uop.GetFracRef("enb")
        var nru_rdy             = new_renamed_uop.GetFracRef("rdy")
        var nru_fu_req          = new_renamed_uop.GetFracRef("fu_req")
        var nru_fu_id           = new_renamed_uop.GetFracRef("fu_id")
        var nru_wb_ext          = new_renamed_uop.GetFracRef("wb_ext")
        var nru_rd_tag          = new_renamed_uop.GetFracRef("rd0_tag")
        var nru_rd_tag_prev     = new_renamed_uop.GetFracRef("rd_tag_prev")
        var nru_rd_tag_prev_clr = new_renamed_uop.GetFracRef("rd_tag_prev_clr")
        var nru_rs_use_mask     = cyclix_gen.ulocal("genrs_use_mask", MultiExu_CFG.rss.size-1, 0, "0")

        cyclix_gen.begif(renamed_uop_buf.ctrl_rdy)
        run {
            cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(cmd_req, cmd_req_data))
            run {

                // decoding input
                cyclix_gen.assign(nru_enb, 1)

                cyclix_gen.assign(nru_fu_id,      cmd_req_data.GetFracRef("fu_id"))

                // getting imms from req
                for (imm_idx in 0 until MultiExu_CFG.src_imms.size) {
                    cyclix_gen.assign(
                        new_renamed_uop.GetFracRef(MultiExu_CFG.src_imms[imm_idx].name),
                        cmd_req_data.GetFracRef("fu_imm_" + MultiExu_CFG.src_imms[imm_idx].name))
                }

                // LOAD/STORE commutation
                cyclix_gen.begif(!cmd_req_data.GetFracRef("exec"))
                run {
                    cyclix_gen.assign(nru_fu_id, global_structures.ExecUnits.size)

                    // LOAD
                    cyclix_gen.begif(cmd_req_data.GetFracRef("rf_we"))
                    run {
                        cyclix_gen.assign(cmd_req_data.GetFracRef("fu_rd"), cmd_req_data.GetFracRef("rf_addr"))
                    }; cyclix_gen.endif()

                    // STORE
                    cyclix_gen.begelse()
                    run {
                        cyclix_gen.assign(cmd_req_data.GetFracRef("fu_rs0"), cmd_req_data.GetFracRef("rf_addr"))
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()

                var rss_tags = ArrayList<hw_var>()
                for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
                    rss_tags.add(global_structures.RenameReg(cmd_req_data.GetFracRef("fu_rs" + RF_rs_idx)))
                }
                var rd_tag = global_structures.RenameReg(cmd_req_data.GetFracRef("fu_rd"))

                for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
                    cyclix_gen.assign(new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_tag"), rss_tags[RF_rs_idx])
                    global_structures.FetchRs(new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdata"), new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_tag"))
                }

                var alloc_rd_tag = global_structures.GetFreePRF()

                cyclix_gen.begif(cmd_req_data.GetFracRef("exec"))
                run {

                    cyclix_gen.assign(nru_fu_req, 1)

                    for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {

                        // processing RS use mask
                        var nru_rs_use = nru_rs_use_mask.GetFracRef(RF_rs_idx)
                        var exu_descr_idx = 0
                        for (exu_descr in global_structures.exu_descrs) {
                            cyclix_gen.begif(cyclix_gen.eq2(nru_fu_id, exu_descr_idx))
                            run {
                                cyclix_gen.assign(nru_rs_use, hw_imm(exu_descr.value.rs_use_flags[RF_rs_idx]))
                            }; cyclix_gen.endif()
                            exu_descr_idx++
                        }

                        // fetching rdy flags from PRF_rdy and masking with rsX_req
                        cyclix_gen.assign(
                            new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdy"),
                            cyclix_gen.bor(global_structures.FetchRsRdy(rss_tags[RF_rs_idx]), !nru_rs_use))
                    }

                    cyclix_gen.assign(nru_rd_tag, alloc_rd_tag.position)            // TODO: check for availability flag
                    cyclix_gen.assign(nru_rd_tag_prev, rd_tag)
                    cyclix_gen.assign(nru_rd_tag_prev_clr, cyclix_gen.indexed(global_structures.PRF_mapped, rd_tag))

                    global_structures.ReserveRd(cmd_req_data.GetFracRef("fu_rd"), alloc_rd_tag.position)

                    cyclix_gen.assign(nru_rdy, 0)
                    cyclix_gen.assign(nru_wb_ext, 0)

                    cyclix_gen.assign(renamed_uop_buf.push, 1)
                }; cyclix_gen.endif()

                cyclix_gen.begelse()
                run {

                    // LOAD
                    cyclix_gen.begif(cmd_req_data.GetFracRef("rf_we"))
                    run {

                        cyclix_gen.assign(nru_rdy, 1)
                        cyclix_gen.assign(nru_rd_tag, alloc_rd_tag.position)        // TODO: check for availability flag
                        cyclix_gen.assign(nru_rd_tag_prev, rd_tag)
                        cyclix_gen.assign(nru_rd_tag_prev_clr, global_structures.PRF_mapped.GetFracRef(rd_tag))

                        global_structures.ReserveWriteRd(cmd_req_data.GetFracRef("fu_rd"), alloc_rd_tag.position, cmd_req_data.GetFracRef("rf_wdata"))

                    }; cyclix_gen.endif()

                    // STORE
                    cyclix_gen.begelse()
                    run {
                        cyclix_gen.assign(new_renamed_uop.GetFracRef("rs0_rdy"), global_structures.PRF_rdy.GetFracRef(rss_tags[0]))

                        for (RF_rs_idx in 1 until MultiExu_CFG.rss.size) {
                            cyclix_gen.assign(new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdy"), 1)
                        }

                        cyclix_gen.assign(nru_rdy, new_renamed_uop.GetFracRef("rs0_rdy"))
                        cyclix_gen.assign(nru_wb_ext, 1)

                        cyclix_gen.assign(renamed_uop_buf.push, 1)

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()

                // placing new uop in rename_buf
                cyclix_gen.begif(renamed_uop_buf.push)
                run {
                    renamed_uop_buf.push_trx(new_renamed_uop)
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()
        }; cyclix_gen.endif()
    }
}