/*
 * cpu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package aquaris

import hwast.*

class cpu(name_in : String, num_stages_in : Int, START_ADDR_in : Int) : pipex.pipeline(name_in) {

    // @Suppress("UNUSED_PARAMETER")

    val num_stages = num_stages_in
    val START_ADDR = START_ADDR_in

    //// base opcodes ////
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

    // ALU opcodes
    val aluop_ADD		= 0
    val aluop_SUB		= 1
    val aluop_AND		= 2
    val aluop_OR		= 3
    val aluop_SRA		= 4
    val aluop_SLL		= 5
    val aluop_SRL		= 6
    val aluop_XOR		= 7
    val aluop_CLRB		= 8

    // op1 sources
    val OP1_SRC_RS1     = 0
    val OP1_SRC_IMM     = 1
    val OP1_SRC_PC 	    = 2
    // op2 sources
    val OP2_SRC_RS2     = 0
    val OP2_SRC_IMM     = 1
    val OP2_SRC_CSR     = 2

    // rd sources
    val RD_LUI		    = 2
    val RD_ALU		    = 0
    val RD_CF_COND	    = 4
    val RD_OF_COND	    = 3
    val RD_PC_INC	    = 1
    val RD_MEM		    = 5
    val RD_CSR		    = 6

    // jmp sources
    val JMP_SRC_OP1     = 0
    val JMP_SRC_ALU     = 1


    //// signals ////
    var busreq_mem_struct = add_if_struct(name + "_busreq_mem_struct")

    var reset_active    = ulocal("reset_active", 0, 0, "0")
    var curinstr_addr   = ulocal("curinstr_addr", 31, 0, "0")
    var nextinstr_addr  = ulocal("nextinstr_addr", 31, 0, "0")
    var instr_code      = ulocal("instr_code", 31, 0, "0")

    // opcode signals
    var opcode      = ulocal("opcode", 6, 0, aluop_ADD.toString())

    // control transfer signals
    var jump_req        = ulocal("jump_req", 0, 0, "0")
    var jump_req_cond   = ulocal("jump_req_cond", 0, 0, "0")
    var jump_src        = ulocal("jump_src", 0, 0, JMP_SRC_OP1.toString())
    var jump_vector     = ulocal("jump_vector", 31, 0, "0")

    // regfile control signals
    var rs1_req         = ulocal("rs1_req", 0, 0, "0")
    var rs1_addr        = ulocal("rs1_addr", 4, 0, "0")
    var rs1_rdata       = ulocal_sticky("rs1_rdata", 31, 0, "0")

    var rs2_req         = ulocal("rs2_req", 0, 0, "0")
    var rs2_addr        = ulocal("rs2_addr", 4, 0, "0")
    var rs2_rdata       = ulocal_sticky("rs2_rdata", 31, 0, "0")

    var rd_req          = ulocal("rd_req", 0, 0, "0")
    var rd_source       = ulocal("rd_source", 2, 0, RD_ALU.toString())
    var rd_addr         = ulocal("rd_addr", 4, 0, "0")
    var rd_wdata        = ulocal("rd_wdata", 31, 0, "0")
    var rd_rdy          = ulocal("rd_rdy", 0, 0, "0")

    var immediate_I     = ulocal("immediate_I", 31, 0, "0")
    var immediate_S     = ulocal("immediate_S", 31, 0, "0")
    var immediate_B     = ulocal("immediate_B", 31, 0, "0")
    var immediate_U     = ulocal("immediate_U", 31, 0, "0")
    var immediate_J     = ulocal("immediate_J", 31, 0, "0")

    var immediate       = ulocal("immediate", 31, 0, "0")

    var curinstraddr_imm    = ulocal("curinstraddr_imm", 31, 0, "0")

    var funct3          = ulocal("funct3", 2, 0, "0")
    var funct7          = ulocal("funct7", 6, 0, "0")
    var shamt           = ulocal("shamt", 4, 0, "0")

    var fencereq        = ulocal("fencereq", 0, 0, "0")
    var pred            = ulocal("pred", 3, 0, "0")
    var succ            = ulocal("succ", 3, 0, "0")

    var ecallreq        = ulocal("ecallreq", 0, 0, "0")
    var ebreakreq       = ulocal("ebreakreq", 0, 0, "0")

    var csrreq          = ulocal("csrreq", 0, 0, "0")
    var csrnum          = ulocal("csrnum", 11, 0, "0")
    var zimm            = ulocal("zimm", 4, 0, "0")

    var op1_source      = ulocal("op1_source", 1, 0, OP1_SRC_RS1.toString())
    var op2_source      = ulocal("op2_source", 1, 0, OP2_SRC_RS2.toString())

    // ALU control
    var alu_req         = ulocal("alu_req", 0, 0, "0")
    var alu_op1         = ulocal("alu_op1", 31, 0, "0")
    var alu_op2         = ulocal("alu_op2", 31, 0, "0")
    var alu_opcode      = ulocal("alu_opcode", 3, 0, "0")
    var alu_unsigned    = ulocal("alu_unsigned", 0, 0, "0")

    var alu_op1_wide    = ulocal("alu_op1_wide", 32, 0, "0")
    var alu_op2_wide    = ulocal("alu_op2_wide", 32, 0, "0")
    var alu_result_wide = ulocal("alu_result_wide", 32, 0, "0")
    var alu_result      = ulocal("alu_result", 31, 0, "0")
    var alu_CF          = ulocal("alu_CF", 0, 0, "0")
    var alu_SF          = ulocal("alu_SF", 0, 0, "0")
    var alu_ZF          = ulocal("alu_ZF", 0, 0, "0")
    var alu_OF          = ulocal("alu_OF", 0, 0, "0")
    var alu_overflow    = ulocal("alu_overflow", 0, 0, "0")

    // data memory control
    var mem_req         = ulocal("mem_req", 0, 0, "0")
    var mem_cmd         = ulocal("mem_cmd", 0, 0, "0")
    var mem_addr        = ulocal("mem_addr", 31, 0, "0")
    var mem_be          = ulocal("mem_be", 3, 0, "0")
    var mem_wdata       = ulocal("mem_wdata", 31, 0, "0")
    var mem_rdata       = ulocal("mem_rdata", 31, 0, "0")
    var mem_rshift      = ulocal("mem_rshift", 0, 0, "0")

    var pc              = uglobal("pc", 31, 0, START_ADDR.toString())
    var rf_dim = hw_dim_static()
    var regfile         = uglobal("regfile", rf_dim, "0")
    var jump_req_cmd    = uglobal("jump_req_cmd", 0, 0, "0")
    var jump_vector_cmd = uglobal("jump_vector_cmd", 31, 0, "0")

    // TODO: CSRs

    //// interfaces ////
    var instr_mem = mcopipe_if("instr_mem",
        hw_type(busreq_mem_struct),
        hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(31, 0)))
    var instr_handle = mcopipe_handle(instr_mem)
    var instr_busreq = local("instr_busreq", busreq_mem_struct)
    var instr_req_done = ulocal_sticky("instr_req_done", 0, 0, "0")

    var data_mem = mcopipe_if("data_mem",
        hw_type(busreq_mem_struct),
        hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(31, 0)))
    var data_handle = mcopipe_handle(data_mem)
    var data_busreq = local("data_busreq", busreq_mem_struct)
    var data_req_done = ulocal_sticky("data_req_done", 0, 0, "0")


    //// RISC-V pipeline macro-operations ////

    fun process_pc() {
        curinstr_addr.assign(pc)

        begif(jump_req_cmd)
        run {
            curinstr_addr.assign(jump_vector_cmd)
        }; endif()
        jump_req_cmd.assign_succ(0)

        nextinstr_addr.assign(curinstr_addr + 4)

        pc.assign_succ(nextinstr_addr)
    }

    fun process_req_instrmem() {
        instr_busreq.assign(hw_fractions("addr"), curinstr_addr)
        instr_busreq.assign(hw_fractions("be"), 0xf)
        instr_busreq.assign(hw_fractions("wdata"), 0)

        begif(!instr_req_done)
        run {
            instr_req_done.assign(instr_mem.rdreq(instr_handle, instr_busreq))
        }; endif()
        begif(!instr_req_done)
        run {
            pstall()
        }; endif()
    }

    fun process_resp_instrmem() {
        begif(!instr_handle.resp(instr_code))
        run {
            pstall()
        }; endif()
    }

    fun process_decode() {
        opcode.assign(instr_code[6, 0])
        alu_unsigned.assign(0)

        rs1_addr.assign(instr_code[19, 15])
        rs2_addr.assign(instr_code[24, 20])
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

        begif(eq2(opcode, opcode_LUI))
        run {
            op1_source.assign(OP1_SRC_IMM)
            rd_req.assign(1)
            rd_source.assign(RD_LUI)
            immediate.assign(immediate_U)
        }; endif()

        begif(eq2(opcode, opcode_AUIPC))
        run {
            op1_source.assign(OP1_SRC_PC)
            op2_source.assign(OP2_SRC_IMM)
            alu_req.assign(1)
            alu_opcode.assign(aluop_ADD)
            rd_req.assign(1)
            rd_source.assign(RD_ALU)
            immediate.assign(immediate_U)
        }; endif()

        begif(eq2(opcode, opcode_JAL))
        run {
            op1_source.assign(OP1_SRC_PC)
            op2_source.assign(OP2_SRC_IMM)
            alu_req.assign(1)
            alu_opcode.assign(aluop_ADD)
            rd_req.assign(1)
            rd_source.assign(RD_PC_INC)
            jump_req.assign(1)
            jump_src.assign(JMP_SRC_ALU)
            immediate.assign(immediate_J)
        }; endif()

        begif(eq2(opcode, opcode_JALR))
        run {
            rs1_req.assign(1)
            op1_source.assign(OP1_SRC_RS1)
            op2_source.assign(OP2_SRC_IMM)
            alu_req.assign(1)
            alu_opcode.assign(aluop_ADD)
            rd_req.assign(1)
            rd_source.assign(RD_PC_INC)
            jump_req.assign(1)
            jump_src.assign(JMP_SRC_ALU)
            immediate.assign(immediate_I)
        }; endif()

        begif(eq2(opcode, opcode_BRANCH))
        run {
            rs1_req.assign(1)
            rs2_req.assign(1)
            alu_req.assign(1)
            alu_opcode.assign(aluop_SUB)
            jump_req_cond.assign(1)
            jump_src.assign(JMP_SRC_ALU)
            immediate.assign(immediate_B)

            begif(bor(eq2(funct3, 0x6), eq2(funct3, 0x7)))
            run {
                alu_unsigned.assign(1)
            }; endif()
        }; endif()

        begif(eq2(opcode, opcode_LOAD))
        run {
            rs1_req.assign(1)
            op1_source.assign(OP1_SRC_RS1)
            op2_source.assign(OP2_SRC_IMM)
            rd_req.assign(1)
            rd_source.assign(RD_MEM)
            alu_req.assign(1)
            mem_req.assign(1)
            mem_cmd.assign(0)

            begif(bor(eq2(funct3, 0x0), eq2(funct3, 0x4)))
            run {
                mem_be.assign(0x1)
            }; endif()
            begif(bor(eq2(funct3, 0x1), eq2(funct3, 0x5)))
            run {
                mem_be.assign(0x3)
            }; endif()
            begif(eq2(funct3, 0x2))
            run {
                mem_be.assign(0xf)
            }; endif()

            immediate.assign(immediate_I)
        }
        endif()

        begif(eq2(opcode, opcode_STORE))
        run {
            rs1_req.assign(1)
            rs2_req.assign(1)
            op1_source.assign(OP1_SRC_RS1)
            op2_source.assign(OP2_SRC_IMM)
            alu_req.assign(1)
            mem_req.assign(1)
            mem_cmd.assign(1)

            begif(eq2(funct3, 0x0))
            run {
                mem_be.assign(0x1)
            }; endif()

            begif(eq2(funct3, 0x1))
            run {
                mem_be.assign(0x3)
            }; endif()

            begif(eq2(funct3, 0x2))
            run {
                mem_be.assign(0xf)
            }; endif()

            immediate.assign(immediate_S)
        }; endif()

        begif(eq2(opcode, opcode_OP_IMM))
        run {
            rs1_req.assign(1)
            op1_source.assign(OP1_SRC_RS1)
            op2_source.assign(OP2_SRC_IMM)
            rd_req.assign(1)
            immediate.assign(immediate_I)
            alu_req.assign(1)

            // ADDI
            begif(eq2(funct3, 0x0))
            run {
                alu_opcode.assign(aluop_ADD)
                rd_source.assign(RD_ALU)
            }; endif()

            // SLTI
            begif(eq2(funct3, 0x2))
            run {
                alu_opcode.assign(aluop_SUB)
                rd_source.assign(RD_OF_COND)
            }; endif()

            // SLTIU
            begif(eq2(funct3, 0x3))
            run {
                alu_opcode.assign(aluop_SUB)
                alu_unsigned.assign(1)
                rd_source.assign(RD_CF_COND)
            }; endif()

            // XORI
            begif(eq2(funct3, 0x4))
            run {
                alu_opcode.assign(aluop_XOR)
                rd_source.assign(RD_ALU)
            }; endif()

            // ORI
            begif(eq2(funct3, 0x6))
            run {
                alu_opcode.assign(aluop_OR)
                rd_source.assign(RD_ALU)
            }; endif()

            // ANDI
            begif(eq2(funct3, 0x7))
            run {
                alu_opcode.assign(aluop_AND)
                rd_source.assign(RD_ALU)
            }; endif()

            // SLLI
            begif(eq2(funct3, 0x1))
            run {
                alu_opcode.assign(aluop_SLL)
                rd_source.assign(RD_ALU)
                immediate.assign(zeroext(instr_code[24, 20], 32))
            }; endif()

            // SRLI, SRAI
            begif(eq2(funct3, 0x5))
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
            }; endif()
        }; endif()

        begif(eq2(opcode, opcode_OP))
        run {
            rs1_req.assign(1)
            op1_source.assign(OP1_SRC_RS1)
            op2_source.assign(OP2_SRC_RS2)
            rd_req.assign(1)
            rd_source.assign(RD_ALU)
            alu_req.assign(1)

            // ADD/SUB
            begif(eq2(funct3, 0x0))
            run {
                // SUB
                begif(instr_code[30])
                run {
                    alu_opcode.assign(aluop_SUB)
                }; endif()
                begelse()
                run {
                    alu_opcode.assign(aluop_ADD)
                }; endif()

                rd_source.assign(RD_ALU)
            }; endif()

            // SLL
            begif(eq2(funct3, 0x1))
            run {
                alu_opcode.assign(aluop_SLL)
                rd_source.assign(RD_OF_COND)
            }; endif()

            // SLT
            begif(eq2(funct3, 0x2))
            run {
                alu_opcode.assign(aluop_SUB)
                rd_source.assign(RD_OF_COND)
            }; endif()

            // SLTU
            begif(eq2(funct3, 0x3))
            run {
                alu_opcode.assign(aluop_SUB)
                alu_unsigned.assign(1)
                rd_source.assign(RD_CF_COND)
            }; endif()

            // XORI
            begif(eq2(funct3, 0x4))
            run {
                alu_opcode.assign(aluop_XOR)
                rd_source.assign(RD_ALU)
            }; endif()

            // SRL/SRA
            begif(eq2(funct3, 0x5))
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
            }; endif()

            // OR
            begif(eq2(funct3, 0x6))
            run {
                alu_opcode.assign(aluop_OR)
                rd_source.assign(RD_ALU)
            }; endif()

            // AND
            begif(eq2(funct3, 0x7))
            run {
                alu_opcode.assign(aluop_AND)
                rd_source.assign(RD_ALU)
            }; endif()
        }; endif()

        begif(eq2(opcode, opcode_MISC_MEM))
        run {
            fencereq.assign(1)
        }; endif()

        begif(eq2(opcode, opcode_SYSTEM))
        run {

            // EBREAK/ECALL
            begif(eq2(funct3, 0))
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
            }; endif()

            // CSRRW
            begif(eq2(funct3, 0x1))
            run {
                begif(neq2(rs1_addr, 0))
                run {
                    csrreq.assign(1)
                    rs1_req.assign(1)
                    rd_req.assign(1)
                    rd_source.assign(RD_CSR)
                    op1_source.assign(OP1_SRC_RS1)
                    op2_source.assign(OP2_SRC_CSR)
                }; endif()
            }; endif()

            // CSRRS
            begif(eq2(funct3, 0x2))
            run {
                begif(neq2(rs1_addr, 0))
                run {
                    csrreq.assign(1)
                    rs1_req.assign(1)
                    rd_req.assign(1)
                    rd_source.assign(RD_CSR)
                    alu_req.assign(1)
                    alu_opcode.assign(aluop_OR)
                    op1_source.assign(OP1_SRC_RS1)
                    op2_source.assign(OP2_SRC_CSR)
                }; endif()
            }; endif()

            // CSRRC
            begif(eq2(funct3, 0x3))
            run {
                begif(neq2(rs1_addr, 0))
                run {
                    csrreq.assign(1)
                    rs1_req.assign(1)
                    rd_req.assign(1)
                    rd_source.assign(RD_CSR)
                    alu_req.assign(1)
                    alu_opcode.assign(aluop_CLRB)
                    op1_source.assign(OP1_SRC_RS1)
                    op2_source.assign(OP2_SRC_CSR)
                }; endif()
            }; endif()

            // CSRRWI
            begif(eq2(funct3, 0x5))
            run {
                csrreq.assign(1)
                rd_req.assign(1)
                op1_source.assign(OP1_SRC_IMM)
                op2_source.assign(OP2_SRC_CSR)
                immediate.assign(zeroext(zimm, 32))
            }; endif()

            // CSRRSI
            begif(eq2(funct3, 0x6))
            run {
                csrreq.assign(1)
                rd_req.assign(1)
                rd_source.assign(RD_CSR)
                alu_req.assign(1)
                alu_opcode.assign(aluop_CLRB)
                op1_source.assign(OP1_SRC_IMM)
                op2_source.assign(OP2_SRC_CSR)
                immediate.assign(zeroext(zimm, 32))
            }; endif()

            // CSRRSI
            begif(eq2(funct3, 0x7))
            run {
                csrreq.assign(1)
                rd_req.assign(1)
                rd_source.assign(RD_CSR)
                alu_req.assign(1)
                alu_opcode.assign(aluop_CLRB)
                op1_source.assign(OP1_SRC_IMM)
                op2_source.assign(OP2_SRC_CSR)
                immediate.assign(zeroext(zimm, 32))
            }; endif()

        }; endif()

        begif(eq2(rd_addr, 0))
        run {
            rd_req.assign(0)
        }; endif()
    }

    // data fetching - reading regfile ##
    fun process_regfetch () {

        // unoptimized
        // rs1_rdata.assign(regfile[rs1_addr])
        // rs2_rdata.assign(regfile[rs2_addr])

        // optimized for synthesis
        rs1_rdata.assign(regfile.readprev()[rs1_addr])
        rs2_rdata.assign(regfile.readprev()[rs2_addr])

        begif(eq2(rs1_addr, 0))
        run {
            rs1_rdata.assign(0)
        }; endif()

        begif(eq2(rs2_addr, 0))
        run {
            rs2_rdata.assign(0)
        }; endif()
    }

    // unblocking forwarding
    fun forward_unblk (fw_stage : pipex.hw_stage) {

        begif(band(fw_stage.isworking(), fw_stage.readremote(rd_req)))
        run {

            begif(eq2(fw_stage.readremote(rd_addr), rs1_addr))
            run {
                begif(fw_stage.readremote(rd_rdy))
                run {
                    rs1_rdata.assign(fw_stage.readremote(rd_wdata))
                }; endif()
            }; endif()

            begif(eq2(fw_stage.readremote(rd_addr), rs2_addr))
            run {
                begif(fw_stage.readremote(rd_rdy))
                run {
                    rs2_rdata.assign(fw_stage.readremote(rd_wdata))
                }; endif()
            }; endif()

        }; endif()
    }

    // blocking forwarding with accumulation
    fun forward_accum_blk (fw_stage : pipex.hw_stage) {

        begif(band(fw_stage.isworking(), fw_stage.readremote(rd_req)))
        run {

            begif(eq2(fw_stage.readremote(rd_addr), rs1_addr))
            run {
                begif(fw_stage.readremote(rd_rdy))
                run {
                    rs1_rdata.assign(fw_stage.readremote(rd_wdata))
                }; endif()
                begelse()
                run {
                    pstall()
                }; endif()
            }; endif()

            begif(eq2(fw_stage.readremote(rd_addr), rs2_addr))
            run {
                begif(fw_stage.readremote(rd_rdy))
                run {
                    rs2_rdata.assign(fw_stage.readremote(rd_wdata))
                }; endif()
                begelse()
                run {
                    pstall()
                }; endif()
            }; endif()

        }; endif()
    }

    fun forward_blk (fw_stage : pipex.hw_stage) {

        begif(band(fw_stage.isworking(), fw_stage.readremote(rd_req)))
        run {

            begif(eq2(fw_stage.readremote(rd_addr), rs1_addr))
            run {
                begif(fw_stage.readremote(rd_rdy))
                run {
                    rs1_rdata.assign(fw_stage.readremote(rd_wdata))
                }; endif()
                begelse()
                run {
                    pstall()
                }; endif()
            }; endif()

            begif(eq2(fw_stage.readremote(rd_addr), rs2_addr))
            run {
                begif(fw_stage.readremote(rd_rdy))
                run {
                rs2_rdata.assign(fw_stage.readremote(rd_wdata))
            }; endif()
                begelse()
                run {
                    pstall()
                }; endif()
            }; endif()

        }; endif()

    }

    // ALU processing ##
    fun process_alu () {

        // acquiring data
        begif(eq2(op1_source, OP1_SRC_RS1))
        run {
            alu_op1.assign(rs1_rdata)
        }; endif()

        begif(eq2(op1_source, OP1_SRC_IMM))
        run {
            alu_op1.assign(immediate)
        }; endif()

        begif(eq2(op1_source, OP1_SRC_PC))
        run {
            alu_op1.assign(curinstr_addr)
        }; endif()

        begif(eq2(op2_source, OP2_SRC_RS2))
        run {
            alu_op2.assign(rs2_rdata)
        }; endif()

        begif(eq2(op2_source, OP2_SRC_IMM))
        run {
            alu_op2.assign(immediate)
        }; endif()

        // begif(eq2(op2_source, OP2_SRC_CSR))
        // run {
        // TODO: reading CSRs
        // }; endif()

        // acquiring wide operandes
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

        begif(alu_req)
        run {

            // computing result
            begif(eq2(alu_opcode, aluop_ADD))
            run {
                alu_result_wide.assign(alu_op1_wide + alu_op2_wide)
            }; endif()

            begif(eq2(alu_opcode, aluop_SUB))
            run {
                alu_result_wide.assign(alu_op1_wide - alu_op2_wide)
            }; endif()

            begif(eq2(alu_opcode, aluop_AND))
            run {
                alu_result_wide.assign(band(alu_op1_wide, alu_op2_wide))
            }; endif()

            begif(eq2(alu_opcode, aluop_OR))
            run {
                alu_result_wide.assign(bor(alu_op1_wide, alu_op2_wide))
            }; endif()

            begif(eq2(alu_opcode, aluop_XOR))
            run {
                alu_result_wide.assign(bxor(alu_op1_wide, alu_op2_wide))
            }; endif()

            begif(eq2(alu_opcode, aluop_SRL))
            run {
                alu_result_wide.assign(shr(alu_op1_wide, alu_op2_wide))
            }; endif()

            begif(eq2(alu_opcode, aluop_SRA))
            run {
                alu_result_wide.assign(sra(alu_op1_wide, alu_op2_wide))
            }; endif()

            begif(eq2(alu_opcode, aluop_SLL))
            run {
                alu_result_wide.assign(shl(alu_op1_wide, alu_op2_wide))
            }; endif()

            begif(eq2(alu_opcode, aluop_CLRB))
            run {
                alu_result_wide.assign(band(alu_op1, !alu_op2))
            }; endif()

            // formation of result and flags
            alu_result.assign(alu_result_wide[31, 0])
            alu_CF.assign(alu_result_wide[32])
            alu_SF.assign(alu_result_wide[31])
            alu_ZF.assign(eq2(alu_result_wide, 0))
            alu_OF.assign(bor(eq2(alu_result_wide[32, 31], 0x2), eq2(alu_result_wide[32, 31], 0x1)))

            begif(alu_unsigned)
            run {
                alu_overflow.assign(alu_CF)
            }; endif()
            begelse()
            run {
                alu_overflow.assign(alu_OF)
            }; endif()

        }; endif()

        // rd wdata processing
        begif(eq2(rd_source, RD_LUI))
        run {
            rd_wdata.assign(immediate)
            rd_rdy.assign(1)
        }; endif()

        begif(eq2(rd_source, RD_ALU))
        run {
            rd_wdata.assign(alu_result)
            rd_rdy.assign(1)
        }; endif()

        begif(eq2(rd_source, RD_CF_COND))
        run {
            rd_wdata.assign(alu_CF)
            rd_rdy.assign(1)
        }; endif()

        begif(eq2(rd_source, RD_OF_COND))
        run {
            rd_wdata.assign(alu_OF)
            rd_rdy.assign(1)
        }; endif()

        begif(eq2(rd_source, RD_PC_INC))
        run {
            rd_wdata.assign(nextinstr_addr)
            rd_rdy.assign(1)
        }; endif()
    }

    fun process_rd_csr_prev() {
        begif(eq2(rd_source, RD_CSR))
        run{
            // TODO: fetching previous CSR data
        }; endif()
    }

    fun process_curinstraddr_imm () {
        curinstraddr_imm.assign(curinstr_addr + immediate)
    }

    fun process_jump () {
        begif(eq2(jump_src, JMP_SRC_OP1))
        run {
            jump_vector.assign(alu_op1)
        }; endif()

        begif(eq2(jump_src, JMP_SRC_ALU))
        run {
            jump_vector.assign(alu_result)
        }; endif()

        begif(jump_req_cond)
        run {

            // BEQ
            begif(eq2(funct3, 0x0))
            run {
                begif(alu_ZF)
                run {
                    jump_req.assign(1)
                    jump_vector.assign(curinstraddr_imm)
                }; endif()
            }; endif()

            // BNE
            begif(eq2(funct3, 0x1))
            run {
                begif(!alu_ZF)
                run {
                    jump_req.assign(1)
                    jump_vector.assign(curinstraddr_imm)
                }; endif()
            }; endif()

            // BLT, BLTU
            begif(bor(eq2(funct3, 0x4), eq2(funct3, 0x6)))
            run {
                begif(alu_CF)
                run {
                    jump_req.assign(1)
                    jump_vector.assign(curinstraddr_imm)
                }; endif()
            }; endif()

            // BGE, BGEU
            begif(bor(eq2(funct3, 0x5), eq2(funct3, 0x7)))
            run {
                begif(!alu_CF)
                run {
                    jump_req.assign(1)
                    jump_vector.assign(curinstraddr_imm)
                }; endif()
            }; endif()

        }; endif()
    }

    // mem addr processing
    fun process_setup_mem_reqdata() {
        mem_addr.assign(alu_result)
        mem_wdata.assign(rs2_rdata)
    }

    // branch control
    fun process_branch() {
        jump_req_cmd.assign_succ(jump_req)
        jump_vector_cmd.assign_succ(jump_vector)
        begif(jump_req)
        run {
            pflush()
        }; endif()
    }

    fun process_req_datamem() {
        // memory access

        begif(mem_req)
        run {
            begif(!data_req_done)
            run {
                data_busreq.assign(hw_fractions("addr"), mem_addr)
                data_busreq.assign(hw_fractions("be"), 0xf)
                data_busreq.assign(hw_fractions("wdata"), mem_wdata)

                data_req_done.assign(data_mem.req(data_handle, mem_cmd, data_busreq))
            }; endif()

            begif(!data_req_done)
            run {
                pstall()
            }; endif()
        }; endif()
    }

    fun process_resp_datamem() {
        begif(mem_req)
        run {
            begif(!mem_cmd)
            run {
                begif(data_handle.resp(mem_rdata))
                run {
                    rd_rdy.assign(1)
                }; endif()
                begelse()
                run {
                    pstall()
                }; endif()
            }; endif()
        }; endif()
    }

    fun process_rd_mem_wdata() {
        begif(eq2(rd_source, RD_MEM))
        run {
            rd_wdata.assign(mem_rdata)
        }; endif()
    }

    fun process_wb() {
        begif(rd_req)
        run {
            regfile.assign_succ(hw_fractions(rd_addr), rd_wdata)
        }; endif()
    }

    init {
        // constructing structures
        busreq_mem_struct.addu("addr",     31, 0, "0")
        busreq_mem_struct.addu("be",       3,  0, "0")
        busreq_mem_struct.addu("wdata",    31, 0, "0")

        rf_dim.add(31, 0)
        rf_dim.add(31, 1)

        if (num_stages == 1) {

            var EXEC = stage_handler("EXEC")
            EXEC.begin()
            run {
                process_pc()
                process_req_instrmem()
                process_resp_instrmem()
                process_decode()
                process_regfetch()
                process_alu()
                process_rd_csr_prev()
                process_curinstraddr_imm()
                process_jump()
                process_setup_mem_reqdata()
                process_branch()

                // memory access
                process_req_datamem()
                process_resp_datamem()

                process_rd_mem_wdata()
                process_wb()

            }; endstage()


        } else if (num_stages == 2) {

            var IFETCH = stage_handler("IFETCH")
            var EXEC = stage_handler("EXEC")

            IFETCH.begin()
            run {
                process_pc()
                process_req_instrmem()

            }; endstage()

            EXEC.begin()
            run {
                process_resp_instrmem()

                process_decode()
                process_regfetch()

                process_alu()
                process_rd_csr_prev()
                process_curinstraddr_imm()
                process_jump()
                process_setup_mem_reqdata()

                process_branch()

                // memory access
                process_req_datamem()
                process_resp_datamem()

                process_rd_mem_wdata()
                process_wb()

            }; endstage()

        } else if (num_stages == 3) {

            var IFETCH = stage_handler("IFETCH")
            var EXEC = stage_handler("EXEC")
            var MEMWB = stage_handler("MEMWB")

            IFETCH.begin()
            run {
                process_pc()
                process_req_instrmem()

            }; endstage()

            EXEC.begin()
            run {
                process_resp_instrmem()

                process_decode()
                process_regfetch()
                forward_blk(MEMWB)

                process_alu()
                process_rd_csr_prev()
                process_curinstraddr_imm()
                process_jump()
                process_setup_mem_reqdata()
            }; endstage()

            MEMWB.begin()
            run {
                process_branch()

                // memory access
                process_req_datamem()
                process_resp_datamem()

                process_rd_mem_wdata()
                process_wb()

            }; endstage()

        } else if (num_stages == 4) {

            var IFETCH = stage_handler("IFETCH")
            var IDECODE = stage_handler("IDECODE")
            var EXEC = stage_handler("EXEC")
            var MEMWB = stage_handler("MEMWB")

            IFETCH.begin()
            run {
                process_pc()
                process_req_instrmem()

            }; endstage()

            IDECODE.begin()
            run {
                process_resp_instrmem()
                process_decode()
                process_regfetch()
                forward_unblk(MEMWB)

            }; endstage()

            EXEC.begin()
            run {
                forward_accum_blk(MEMWB)
                process_alu()
                process_rd_csr_prev()
                process_curinstraddr_imm()

            }; endstage()

            MEMWB.begin()
            run {
                process_jump()
                process_setup_mem_reqdata()
                process_branch()

                // memory access
                process_req_datamem()
                process_resp_datamem()

                process_rd_mem_wdata()
                process_wb()

            }; endstage()

        } else if (num_stages == 5) {

            var IFETCH = stage_handler("IFETCH")
            var IDECODE = stage_handler("IDECODE")
            var EXEC = stage_handler("EXEC")
            var MEM = stage_handler("MEM")
            var WB = stage_handler("WB")

            IFETCH.begin()
            run {
                process_pc()
                process_req_instrmem()

            }; endstage()

            IDECODE.begin()
            run {
                process_resp_instrmem()
                process_decode()
                process_regfetch()

                forward_blk(WB)
                forward_blk(MEM)
                forward_blk(EXEC)

            }; endstage()

            EXEC.begin()
            run {
                process_alu()
                process_rd_csr_prev()
                process_curinstraddr_imm()
                process_jump()
                process_setup_mem_reqdata()

            }; endstage()

            MEM.begin()
            run {
                process_branch()
                process_req_datamem()

            }; endstage()

            WB.begin()
            run {
                process_resp_datamem()
                process_rd_mem_wdata()
                process_wb()

            }; endstage()

        } else if (num_stages == 6) {

            var IADDR = stage_handler("IADDR")
            var IFETCH = stage_handler("IFETCH")
            var IDECODE = stage_handler("IDECODE")
            var EXEC = stage_handler("EXEC")
            var MEM = stage_handler("MEM")
            var WB = stage_handler("WB")

            IADDR.begin()
            run {
                process_pc()
            }; endstage()

            IFETCH.begin()
            run {
                process_req_instrmem()

            }; endstage()

            IDECODE.begin()
            run {
                process_resp_instrmem()
                process_decode()
                process_regfetch()

                forward_blk(WB)
                forward_blk(MEM)
                forward_blk(EXEC)

            }; endstage()

            EXEC.begin()
            run {
                process_alu()
                process_rd_csr_prev()

            }; endstage()

            MEM.begin()
            run {
                process_curinstraddr_imm()
                process_jump()
                process_setup_mem_reqdata()
                process_branch()
                process_req_datamem()

            }; endstage()

            WB.begin()
            run {
                process_resp_datamem()
                process_rd_mem_wdata()
                process_wb()

            }; endstage()

        } else {
            throw Exception("RISCV-PIPEX: num_stages parameter incorrect!")
        }
    }
}