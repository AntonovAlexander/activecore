/*
 * reorder_buffer.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*
import cyclix.*

open class rob(cyclix_gen : cyclix.Generic,
               name_prefix : String,
               TRX_BUF_SIZE : Int,
               MultiExu_CFG : Reordex_CFG,
               val cdb_num : Int) : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG) {

    var trx_id          = AddStageVar(hw_structvar("trx_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(MultiExu_CFG.trx_inflight_num) -1, 0, "0"))
    var rd_tag_prev     = AddStageVar(hw_structvar("rd_tag_prev",       DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    var rd_tag_prev_clr = AddStageVar(hw_structvar("rd_tag_prev_clr",   DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    var cdb_id          = AddStageVar(hw_structvar("cdb_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(cdb_num) -1, 0, "0"))
    val rdy             = AddStageVar(hw_structvar("rdy",               DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    var TRX_ID_COUNTER  = cyclix_gen.uglobal(name_prefix + "_TRX_ID_COUNTER", GetWidthToContain(TRX_BUF_SIZE) -1, 0, "0")

    open fun Commit(global_structures: __global_structures) {
        preinit_ctrls()
        init_locals()

        cyclix_gen.begif(ctrl_active)
        run {
            cyclix_gen.begif(rdy)
            run {
                cyclix_gen.begif(rd_tag_prev_clr)
                run {
                    global_structures.FreePRF(rd_tag_prev)
                }; cyclix_gen.endif()
                cyclix_gen.assign(pop, 1)
            }; cyclix_gen.endif()
        }; cyclix_gen.endif()

        // popping
        cyclix_gen.begif(pop)
        run {
            pop_trx()
        }; cyclix_gen.endif()
    }
}

class rob_risc(name: String,
               cyclix_gen : cyclix.Generic,
               name_prefix : String,
               TRX_BUF_SIZE : Int,
               MultiExu_CFG : Reordex_CFG,
               cdb_num : Int) : rob(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, cdb_num) {

    var curinstr_addr   = AdduStageVar("curinstr_addr", 31, 0, "0")
    var nextinstr_addr  = AdduStageVar("nextinstr_addr", 31, 0, "0")

    var rd_req          = AdduStageVar("rd_req", 0, 0, "0")
    var rd_addr         = AdduStageVar("rd_addr", 4, 0, "0")
    var rd_wdata        = AdduStageVar("rd_wdata", MultiExu_CFG.RF_width-1, 0, "0")

    var immediate           = AdduStageVar("immediate", 31, 0, "0")
    var curinstraddr_imm    = AdduStageVar("curinstraddr_imm", 31, 0, "0")

    var funct3          = AdduStageVar("funct3", 2, 0, "0")
    var funct7          = AdduStageVar("funct7", 6, 0, "0")

    var alu_result      = AdduStageVar("alu_result", 31, 0, "0")
    var alu_CF          = AdduStageVar("alu_CF", 0, 0, "0")
    var alu_SF          = AdduStageVar("alu_SF", 0, 0, "0")
    var alu_ZF          = AdduStageVar("alu_ZF", 0, 0, "0")
    var alu_OF          = AdduStageVar("alu_OF", 0, 0, "0")

    var mem_req         = AdduStageVar("mem_req", 0, 0, "0")
    var mem_cmd         = AdduStageVar("mem_cmd", 0, 0, "0")
    var mem_addr        = AdduStageVar("mem_addr", 31, 0, "0")
    var mem_wdata       = AdduStageVar("mem_wdata", 31, 0, "0")
    var mem_be          = AdduStageVar("mem_be", 3, 0, "0")

    //// committing RF signals
    // rd sources
    val RD_LUI		    = 0
    val RD_ALU		    = 1
    val RD_CF_COND	    = 2
    val RD_OF_COND	    = 3
    val RD_PC_INC	    = 4
    val RD_MEM		    = 5
    val RD_CSR		    = 6

    var rd_source       = AdduStageVar("rd_source", 2, 0, RD_ALU.toString())
    var rd_rdy          = AdduStageVar("rd_rdy", 0, 0, "0")
    val rd_tag          = AdduStageVar("rd0_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")

    //// control transfer signals
    // jmp sources
    val JMP_SRC_IMM     = 0
    val JMP_SRC_ALU     = 1

    var jump_req        = AdduStageVar("jump_req", 0, 0, "0")
    var jump_req_cond   = AdduStageVar("jump_req_cond", 0, 0, "0")
    var jump_src        = AdduStageVar("jump_src", 0, 0, "0")
    var jump_vector     = AdduStageVar("jump_vector", 31, 0, "0")

    var busreq_mem_struct = hw_struct(name + "_busreq_mem_struct")
    val data_name_prefix = "genmcopipe_data_mem_"
    var rd_struct = hw_struct("genpmodule_" + name + "_" + data_name_prefix + "genstruct_fifo_wdata")

    var data_req_fifo   = cyclix_gen.fifo_out((data_name_prefix + "req"), rd_struct)
    var data_resp_fifo  = cyclix_gen.ufifo_in((data_name_prefix + "resp"), 31, 0)

    var irq_fifo        = cyclix_gen.ufifo_in("irq_fifo", 7, 0)

    var rf_dim = hw_dim_static()
    var Backoff_ARF = cyclix_gen.uglobal("Backoff_ARF", rf_dim, "0")

    var expected_instraddr = cyclix_gen.uglobal("expected_instraddr", 31, 0, hw_imm(32, IMM_BASE_TYPE.HEX, "200"))

    init {
        busreq_mem_struct.addu("addr",     31, 0, "0")
        busreq_mem_struct.addu("be",       3,  0, "0")
        busreq_mem_struct.addu("wdata",    31, 0, "0")

        rd_struct.addu("we", 0, 0, "0")
        rd_struct.add("wdata", hw_type(busreq_mem_struct), "0")

        rf_dim.add(31, 0)
        rf_dim.add(31, 0)
    }

    fun Commit(global_structures: __global_structures, pc : hw_var, bufs_to_rollback : ArrayList<hw_stage>, commit_cdb : hw_var) {

        var mem_rd_inprogress   = cyclix_gen.uglobal("mem_rd_inprogress", 0, 0, "0")
        var mem_data_wdata      = cyclix_gen.local("mem_data_wdata", data_req_fifo.vartype, "0")
        var mem_data_rdata      = cyclix_gen.local("mem_data_rdata", data_resp_fifo.vartype, "0")

        var backoff_cmd         = cyclix_gen.ulocal("backoff_cmd", 0, 0, "0")

        preinit_ctrls()
        init_locals()

        cyclix_gen.begif(mem_rd_inprogress)
        run {
            cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(data_resp_fifo, mem_data_rdata))
            run {

                var exu_cdb_inst_enb    = commit_cdb.GetFracRef("enb")
                var exu_cdb_inst_data   = commit_cdb.GetFracRef("data")
                var exu_cdb_inst_tag    = exu_cdb_inst_data.GetFracRef("tag")
                var exu_cdb_inst_wdata  = exu_cdb_inst_data.GetFracRef("wdata")
                cyclix_gen.assign(exu_cdb_inst_enb, 1)
                cyclix_gen.assign(exu_cdb_inst_tag, rd_tag)
                cyclix_gen.assign(exu_cdb_inst_wdata, mem_data_rdata)

                cyclix_gen.assign(rd_wdata, mem_data_rdata)
                cyclix_gen.assign(mem_rd_inprogress, 0)
                cyclix_gen.assign(pop, 1)

            }; cyclix_gen.endif()
        }; cyclix_gen.endif()

        cyclix_gen.begelsif(ctrl_active)
        run {
            cyclix_gen.begif(rdy)
            run {

                cyclix_gen.begif(cyclix_gen.eq2(expected_instraddr, curinstr_addr))     // branch prediction fine
                run {
                    cyclix_gen.assign(pop, 1)

                    cyclix_gen.begif(mem_req)
                    run {

                        cyclix_gen.assign(mem_addr, alu_result)

                        cyclix_gen.assign(mem_data_wdata.GetFracRef("we"), mem_cmd)
                        cyclix_gen.assign(mem_data_wdata.GetFracRef("wdata").GetFracRef("addr"), mem_addr)
                        cyclix_gen.assign(mem_data_wdata.GetFracRef("wdata").GetFracRef("be"), mem_be)
                        cyclix_gen.assign(mem_data_wdata.GetFracRef("wdata").GetFracRef("wdata"), mem_wdata)
                        cyclix_gen.fifo_wr_unblk(data_req_fifo, mem_data_wdata)

                        cyclix_gen.begif(!mem_cmd)
                        run {
                            cyclix_gen.assign(mem_rd_inprogress, 1)
                            cyclix_gen.assign(pop, 0)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()

                cyclix_gen.begelse()
                run {
                    cyclix_gen.assign(backoff_cmd, 1)
                    cyclix_gen.assign(pc, expected_instraddr)
                    for (buf_to_rollback in bufs_to_rollback) {
                        buf_to_rollback.Reset()
                    }
                    global_structures.RollBack(Backoff_ARF)
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()
        }; cyclix_gen.endif()

        // uop completed successfully, popping
        cyclix_gen.COMMENT("uop completion...")
        cyclix_gen.begif(pop)
        run {

            cyclix_gen.COMMENT("committing RF...")

            // rd wdata processing
            cyclix_gen.begcase(rd_source)
            run {
                cyclix_gen.begbranch(RD_LUI)
                run {
                    rd_wdata.assign(immediate)
                    rd_rdy.assign(1)
                }; cyclix_gen.endbranch()

                cyclix_gen.begbranch(RD_ALU)
                run {
                    rd_wdata.assign(alu_result)
                    rd_rdy.assign(1)
                }; cyclix_gen.endbranch()

                cyclix_gen.begbranch(RD_CF_COND)
                run {
                    rd_wdata.assign(alu_CF)
                    rd_rdy.assign(1)
                }; cyclix_gen.endbranch()

                cyclix_gen.begbranch(RD_OF_COND)
                run {
                    rd_wdata.assign(alu_OF)
                    rd_rdy.assign(1)
                }; cyclix_gen.endbranch()

                cyclix_gen.begbranch(RD_PC_INC)
                run {
                    rd_wdata.assign(nextinstr_addr)
                    rd_rdy.assign(1)
                }; cyclix_gen.endbranch()

                /*
                cyclix_gen.begbranch(RD_CSR)
                run {
                    rd_wdata.assign(csr_rdata)
                    rd_rdy.assign(1)
                }; cyclix_gen.endbranch()
                */

            }; cyclix_gen.endcase()

            cyclix_gen.begif(rd_tag_prev_clr)
            run {
                global_structures.FreePRF(rd_tag_prev)
                cyclix_gen.begif(rd_req)
                run {
                    cyclix_gen.assign(Backoff_ARF.GetFracRef(rd_addr), rd_wdata)
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()
            cyclix_gen.COMMENT("committing RF: done")

            cyclix_gen.COMMENT("control transfer...")

            cyclix_gen.assign(expected_instraddr, nextinstr_addr)

            cyclix_gen.begcase(jump_src)
            run {
                cyclix_gen.begbranch(JMP_SRC_IMM)
                run {
                    jump_vector.assign(immediate)
                }; cyclix_gen.endbranch()

                cyclix_gen.begbranch(JMP_SRC_ALU)
                run {
                    jump_vector.assign(alu_result)
                }; cyclix_gen.endbranch()

            }; cyclix_gen.endcase()

            cyclix_gen.begif(jump_req_cond)
            run {

                cyclix_gen.begcase(funct3)
                run {
                    // BEQ
                    cyclix_gen.begbranch(0x0)
                    run {
                        cyclix_gen.begif(alu_ZF)
                        run {
                            jump_req.assign(1)
                            jump_vector.assign(curinstraddr_imm)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endbranch()

                    // BNE
                    cyclix_gen.begbranch(0x1)
                    run {
                        cyclix_gen.begif(!alu_ZF)
                        run {
                            jump_req.assign(1)
                            jump_vector.assign(curinstraddr_imm)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endbranch()

                    // BLT
                    cyclix_gen.begbranch(0x4)
                    run {
                        cyclix_gen.begif(alu_CF)
                        run {
                            jump_req.assign(1)
                            jump_vector.assign(curinstraddr_imm)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endbranch()

                    // BGE
                    cyclix_gen.begbranch(0x5)
                    run {
                        cyclix_gen.begif(!alu_CF)
                        run {
                            jump_req.assign(1)
                            jump_vector.assign(curinstraddr_imm)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endbranch()

                    // BLTU
                    cyclix_gen.begbranch(0x6)
                    run {
                        cyclix_gen.begif(alu_CF)
                        run {
                            jump_req.assign(1)
                            jump_vector.assign(curinstraddr_imm)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endbranch()

                    // BGEU
                    cyclix_gen.begbranch(0x7)
                    run {
                        cyclix_gen.begif(!alu_CF)
                        run {
                            jump_req.assign(1)
                            jump_vector.assign(curinstraddr_imm)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endbranch()

                }; cyclix_gen.endcase()

            }; cyclix_gen.endif()

            cyclix_gen.begif(jump_req)
            run {
                cyclix_gen.assign(expected_instraddr, jump_vector)
            }; cyclix_gen.endif()

            cyclix_gen.COMMENT("control transfer: done")

            pop_trx()

        }; cyclix_gen.endif()
        cyclix_gen.COMMENT("uop completion: done")

        finalize_ctrls()
    }
}