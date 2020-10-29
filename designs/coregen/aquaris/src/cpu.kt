/*
 * cpu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package aquaris

import hwast.*
import pipex.PSTAGE_MODE

class cpu(name_in : String, num_stages_in : Int, START_ADDR_in : Int, IRQ_ADDR_in : Int, irq_width_in : Int) : pipex.pipeline(name_in) {

    val num_stages  = num_stages_in
    val START_ADDR  = START_ADDR_in
    val IRQ_ADDR    = IRQ_ADDR_in
    val irq_width   = irq_width_in

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
    val OP1_SRC_RS1     = 0
    val OP1_SRC_IMM     = 1
    val OP1_SRC_PC 	    = 2
    // op2 sources
    val OP2_SRC_RS2     = 0
    val OP2_SRC_IMM     = 1
    val OP2_SRC_CSR     = 2

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
    var jump_src        = ulocal("jump_src", 0, 0, JMP_SRC_IMM.toString())
    var jump_vector     = ulocal("jump_vector", 31, 0, "0")

    // regfile control signals
    var rs1_req         = ulocal("rs1_req", 0, 0, "0")
    var rs1_addr        = ulocal("rs1_addr", 4, 0, "0")
    var rs1_rdata       = ulocal_sticky("rs1_rdata", 31, 0, "0")

    var rs2_req         = ulocal("rs2_req", 0, 0, "0")
    var rs2_addr        = ulocal("rs2_addr", 4, 0, "0")
    var rs2_rdata       = ulocal_sticky("rs2_rdata", 31, 0, "0")

    var csr_rdata       = ulocal("csr_rdata", 31, 0, "0")

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

    // data memory control
    var mem_req         = ulocal("mem_req", 0, 0, "0")
    var mem_cmd         = ulocal("mem_cmd", 0, 0, "0")
    var mem_addr        = ulocal("mem_addr", 31, 0, "0")
    var mem_be          = ulocal("mem_be", 3, 0, "0")
    var mem_wdata       = ulocal("mem_wdata", 31, 0, "0")
    var mem_rdata       = ulocal("mem_rdata", 31, 0, "0")
    var mem_rshift      = ulocal("mem_rshift", 0, 0, "0")

    var mret_req         = ulocal("mret_req", 0, 0, "0")

    var pc              = uglobal("pc", 31, 0, START_ADDR.toString())
    var rf_dim = hw_dim_static()
    var regfile         = uglobal("regfile", rf_dim, "0")
    var jump_req_cmd    = uglobal("jump_req_cmd", 0, 0, "0")
    var jump_vector_cmd = uglobal("jump_vector_cmd", 31, 0, "0")

    // CSRs
    var CSR_MCAUSE      = uglobal("CSR_MCAUSE", 7, 0, "0")

    //// interfaces ////
    var instr_mem = mcopipe_if("instr_mem",
        hw_type(busreq_mem_struct),
        hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(31, 0)),
        1)
    var instr_handle = mcopipe_handle(instr_mem)
    var instr_busreq = local("instr_busreq", busreq_mem_struct)
    var instr_req_done = ulocal_sticky("instr_req_done", 0, 0, "0")

    var data_mem = mcopipe_if("data_mem",
        hw_type(busreq_mem_struct),
        hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(31, 0)),
        1)
    var data_handle = mcopipe_handle(data_mem)
    var data_busreq = local("data_busreq", busreq_mem_struct)
    var data_req_done = ulocal_sticky("data_req_done", 0, 0, "0")

    var irq_fifo    = ufifo_in("irq_fifo", irq_width-1, 0)
    var irq_mcause  = ulocal_sticky("irq_mcause", irq_width-1, 0, "0")
    var irq_recv    = ulocal_sticky("irq_recv", 0, 0, "0")
    var MIRQEN      = uglobal("MIRQEN", 0, 0, "1")
    var MRETADDR    = uglobal("MRETADDR", 31, 0, "0")

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

        begcase(opcode)
        run {
            begbranch(opcode_LUI)
            run {
                op1_source.assign(OP1_SRC_IMM)
                rd_req.assign(1)
                rd_source.assign(RD_LUI)
                immediate.assign(immediate_U)
            }
            endbranch()

            begbranch(opcode_AUIPC)
            run {
                op1_source.assign(OP1_SRC_PC)
                op2_source.assign(OP2_SRC_IMM)
                alu_req.assign(1)
                alu_opcode.assign(aluop_ADD)
                rd_req.assign(1)
                rd_source.assign(RD_ALU)
                immediate.assign(immediate_U)
            }; endbranch()

            begbranch(opcode_JAL)
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
            }; endbranch()

            begbranch(opcode_JALR)
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
            }; endbranch()

            begbranch(opcode_BRANCH)
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
            }; endbranch()

            begbranch(opcode_LOAD)
            run {
                rs1_req.assign(1)
                op1_source.assign(OP1_SRC_RS1)
                op2_source.assign(OP2_SRC_IMM)
                rd_req.assign(1)
                rd_source.assign(RD_MEM)
                alu_req.assign(1)
                mem_req.assign(1)
                mem_cmd.assign(0)
                immediate.assign(immediate_I)
            }; endbranch()

            begbranch(opcode_STORE)
            run {
                rs1_req.assign(1)
                rs2_req.assign(1)
                op1_source.assign(OP1_SRC_RS1)
                op2_source.assign(OP2_SRC_IMM)
                alu_req.assign(1)
                mem_req.assign(1)
                mem_cmd.assign(1)
                immediate.assign(immediate_S)
            }; endbranch()

            begbranch(opcode_OP_IMM)
            run {
                rs1_req.assign(1)
                op1_source.assign(OP1_SRC_RS1)
                op2_source.assign(OP2_SRC_IMM)
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
                        alu_opcode.assign(aluop_SUB)
                        rd_source.assign(RD_CF_COND)
                    }; endbranch()

                    // SLTIU
                    begbranch(0x3)
                    run {
                        alu_opcode.assign(aluop_SUB)
                        alu_unsigned.assign(1)
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
                rs1_req.assign(1)
                rs2_req.assign(1)
                op1_source.assign(OP1_SRC_RS1)
                op2_source.assign(OP2_SRC_RS2)
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
                        alu_opcode.assign(aluop_SUB)
                        rd_source.assign(RD_CF_COND)
                    }; endbranch()

                    // SLTU
                    begbranch(0x3)
                    run {
                        alu_opcode.assign(aluop_SUB)
                        alu_unsigned.assign(1)
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
                        rs1_req.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        op1_source.assign(OP1_SRC_RS1)
                        op2_source.assign(OP2_SRC_CSR)
                    }; endbranch()

                    // CSRRS
                    begbranch(0x2)
                    run {
                        csrreq.assign(1)
                        rs1_req.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_OR)
                        op1_source.assign(OP1_SRC_RS1)
                        op2_source.assign(OP2_SRC_CSR)
                    }; endbranch()

                    // CSRRC
                    begbranch(0x3)
                    run {
                        csrreq.assign(1)
                        rs1_req.assign(1)
                        rd_req.assign(1)
                        rd_source.assign(RD_CSR)
                        alu_req.assign(1)
                        alu_opcode.assign(aluop_CLRB)
                        op1_source.assign(OP1_SRC_RS1)
                        op2_source.assign(OP2_SRC_CSR)
                    }; endbranch()

                    // CSRRWI
                    begbranch(0x5)
                    run {
                        csrreq.assign(1)
                        rd_req.assign(1)
                        op1_source.assign(OP1_SRC_IMM)
                        op2_source.assign(OP2_SRC_CSR)
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
                        op1_source.assign(OP1_SRC_IMM)
                        op2_source.assign(OP2_SRC_CSR)
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
                        op1_source.assign(OP1_SRC_IMM)
                        op2_source.assign(OP2_SRC_CSR)
                        immediate.assign(zeroext(zimm, 32))
                    }; endbranch()
                }; endcase()
            }; endbranch()

        }; endcase()

        begif(mem_req)
        run {
            begcase(funct3)
            run {
                begbranch(0x0)
                run {
                    mem_be.assign(0x1)
                }; endbranch()

                begbranch(0x1)
                run {
                    mem_be.assign(0x3)
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

        begif(csrreq)
        run {
            csr_rdata.assign(CSR_MCAUSE)
        }; endif()
    }

    // unblocking forwarding
    fun forward_unblk (fw_stage : pipex.hw_stage) {

        begif(band(fw_stage.isworking(), fw_stage.readremote(rd_req)))
        run {

            begif(rs1_req)
            run {
                begif(eq2(fw_stage.readremote(rd_addr), rs1_addr))
                run {
                    begif(fw_stage.readremote(rd_rdy))
                    run {
                        rs1_rdata.assign(fw_stage.readremote(rd_wdata))
                    }; endif()
                }; endif()
            }; endif()

            begif(rs2_req)
            run {
                begif(eq2(fw_stage.readremote(rd_addr), rs2_addr))
                run {
                    begif(fw_stage.readremote(rd_rdy))
                    run {
                        rs2_rdata.assign(fw_stage.readremote(rd_wdata))
                    }; endif()
                }; endif()
            }; endif()

        }; endif()
    }

    // blocking forwarding
    fun forward_blk (fw_stage : pipex.hw_stage) {

        begif(band(fw_stage.isworking(), fw_stage.readremote(rd_req)))
        run {

            begif(rs1_req)
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
            }; endif()

            begif(rs2_req)
            run {
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

        }; endif()
    }

    // interlocking
    fun interlock (fw_stage : pipex.hw_stage) {

        begif(band(fw_stage.isworking(), fw_stage.readremote(rd_req)))
        run {

            begif(rs1_req)
            run {
                begif(eq2(fw_stage.readremote(rd_addr), rs1_addr))
                run {
                    pstall()
                }; endif()
            }; endif()

            begif(rs2_req)
            run {
                begif(eq2(fw_stage.readremote(rd_addr), rs2_addr))
                run {
                    pstall()
                }; endif()
            }; endif()

        }; endif()
    }

    // ALU multiplexing
    fun process_alu_mux() {
        // multiplexing alu ops
        alu_op1.assign(rs1_rdata)
        begcase(op1_source)
        run {
            begbranch(OP1_SRC_IMM)
            run {
                alu_op1.assign(immediate)
            }; endbranch()

            begbranch(OP1_SRC_PC)
            run {
                alu_op1.assign(curinstr_addr)
            }; endbranch()
        }; endcase()

        alu_op2.assign(rs2_rdata)
        begcase(op2_source)
        run {
            begbranch(OP2_SRC_IMM)
            run {
                alu_op2.assign(immediate)
            }; endbranch()

            begbranch(OP2_SRC_CSR)
            run {
                alu_op2.assign(csr_rdata)
            }; endbranch()
        }; endcase()

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
    }

    fun process_irq() {

        begif(MIRQEN)
        run {
            begif(!irq_recv)
            run {
                irq_recv.assign(fifo_rd(irq_fifo, irq_mcause))
            }; endif()
            begif(irq_recv)
            run {
                // control transfer signals
                jump_req.assign(1)
                jump_req_cond.assign(0)
                jump_src.assign(JMP_SRC_IMM)

                // regfile control signals
                rs1_req.assign(0)
                rs2_req.assign(0)
                rd_req.assign(0)

                immediate.assign(IRQ_ADDR)

                fencereq.assign(0)
                ecallreq.assign(0)
                ebreakreq.assign(0)
                csrreq.assign(0)

                // ALU control
                alu_req.assign(0)

                // data memory control
                mem_req.assign(0)

                MIRQEN.assign(0)
                CSR_MCAUSE.assign(irq_mcause)
                MRETADDR.assign(curinstr_addr)
            }; endif()
        }; endif()

        begif(mret_req)
        run {
            MIRQEN.assign(1)
        }; endif()
    }

    // ALU processing ##
    fun process_alu () {

        alu_result_wide.assign(alu_op1_wide)
        begif(alu_req)
        run {

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

        }; endif()

        // rd wdata processing
        begcase(rd_source)
        run {
            begbranch(RD_LUI)
            run {
                rd_wdata.assign(immediate)
                rd_rdy.assign(1)
            }; endbranch()

            begbranch(RD_ALU)
            run {
                rd_wdata.assign(alu_result)
                rd_rdy.assign(1)
            }; endbranch()

            begbranch(RD_CF_COND)
            run {
                rd_wdata.assign(alu_CF)
                rd_rdy.assign(1)
            }; endbranch()

            begbranch(RD_OF_COND)
            run {
                rd_wdata.assign(alu_OF)
                rd_rdy.assign(1)
            }; endbranch()

            begbranch(RD_PC_INC)
            run {
                rd_wdata.assign(nextinstr_addr)
                rd_rdy.assign(1)
            }; endbranch()

            begbranch(RD_CSR)
            run {
                rd_wdata.assign(csr_rdata)
                rd_rdy.assign(1)
            }; endbranch()

        }; endcase()
    }

    fun process_curinstraddr_imm () {
        curinstraddr_imm.assign(curinstr_addr + immediate)
    }

    fun process_jump () {
        begcase(jump_src)
        run {
            begbranch(JMP_SRC_IMM)
            run {
                jump_vector.assign(immediate)
            }; endbranch()

            begbranch(JMP_SRC_ALU)
            run {
                jump_vector.assign(alu_result)
            }; endbranch()

        }; endcase()

        begif(jump_req_cond)
        run {

            begcase(funct3)
            run {
                // BEQ
                begbranch(0x0)
                run {
                    begif(alu_ZF)
                    run {
                        jump_req.assign(1)
                        jump_vector.assign(curinstraddr_imm)
                    }; endif()
                }; endbranch()

                // BNE
                begbranch(0x1)
                run {
                    begif(!alu_ZF)
                    run {
                        jump_req.assign(1)
                        jump_vector.assign(curinstraddr_imm)
                    }; endif()
                }; endbranch()

                // BLT
                begbranch(0x4)
                run {
                    begif(alu_CF)
                    run {
                        jump_req.assign(1)
                        jump_vector.assign(curinstraddr_imm)
                    }; endif()
                }; endbranch()

                // BGE
                begbranch(0x5)
                run {
                    begif(!alu_CF)
                    run {
                        jump_req.assign(1)
                        jump_vector.assign(curinstraddr_imm)
                    }; endif()
                }; endbranch()

                // BLTU
                begbranch(0x6)
                run {
                    begif(alu_CF)
                    run {
                        jump_req.assign(1)
                        jump_vector.assign(curinstraddr_imm)
                    }; endif()
                }; endbranch()

                // BGEU
                begbranch(0x7)
                run {
                    begif(!alu_CF)
                    run {
                        jump_req.assign(1)
                        jump_vector.assign(curinstraddr_imm)
                    }; endif()
                }; endbranch()

            }; endcase()

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

        // execution schedules
        if (num_stages == 1) {

            var EXEC = stage_handler("EXEC", PSTAGE_MODE.FALL_THROUGH)
            EXEC.begin()
            run {
                process_pc()
                process_req_instrmem()
                process_resp_instrmem()
                process_decode()
                process_regfetch()
                process_alu_mux()
                process_irq()
                process_alu()
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

            var IFETCH = stage_handler("IFETCH", PSTAGE_MODE.FALL_THROUGH)
            var EXEC = stage_handler("EXEC", PSTAGE_MODE.FALL_THROUGH)

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
                process_alu_mux()

                process_irq()
                process_alu()
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

            var IFETCH = stage_handler("IFETCH", PSTAGE_MODE.FALL_THROUGH)
            var EXEC = stage_handler("EXEC", PSTAGE_MODE.FALL_THROUGH)
            var MEMWB = stage_handler("MEMWB", PSTAGE_MODE.FALL_THROUGH)

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
                interlock(MEMWB)
                process_alu_mux()
                process_irq()

                process_alu()
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

            var IFETCH = stage_handler("IFETCH", PSTAGE_MODE.FALL_THROUGH)
            var IDECODE = stage_handler("IDECODE", PSTAGE_MODE.FALL_THROUGH)
            var EXEC = stage_handler("EXEC", PSTAGE_MODE.FALL_THROUGH)
            var MEMWB = stage_handler("MEMWB", PSTAGE_MODE.FALL_THROUGH)

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
                forward_blk(MEMWB)
                forward_blk(EXEC)
                process_alu_mux()

            }; endstage()

            EXEC.begin()
            run {
                process_irq()
                process_alu()
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

            var IFETCH = stage_handler("IFETCH", PSTAGE_MODE.FALL_THROUGH)
            var IDECODE = stage_handler("IDECODE", PSTAGE_MODE.FALL_THROUGH)
            var EXEC = stage_handler("EXEC", PSTAGE_MODE.FALL_THROUGH)
            var MEM = stage_handler("MEM", PSTAGE_MODE.FALL_THROUGH)
            var WB = stage_handler("WB", PSTAGE_MODE.FALL_THROUGH)

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

                process_alu_mux()

            }; endstage()

            EXEC.begin()
            run {
                process_irq()
                process_alu()
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

            var IADDR = stage_handler("IADDR", PSTAGE_MODE.FALL_THROUGH)
            var IFETCH = stage_handler("IFETCH", PSTAGE_MODE.FALL_THROUGH)
            var IDECODE = stage_handler("IDECODE", PSTAGE_MODE.FALL_THROUGH)
            var EXEC = stage_handler("EXEC", PSTAGE_MODE.FALL_THROUGH)
            var MEM = stage_handler("MEM", PSTAGE_MODE.FALL_THROUGH)
            var WB = stage_handler("WB", PSTAGE_MODE.FALL_THROUGH)

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

                process_alu_mux()

            }; endstage()

            EXEC.begin()
            run {
                process_irq()
                process_alu()

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
            throw Exception("aquaris: num_stages parameter incorrect!")
        }
    }
}