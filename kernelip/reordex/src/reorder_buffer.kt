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
               rrb_num : Int) : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG.FrontEnd_width, MultiExu_CFG) {

    var trx_id          = AddStageVar(hw_structvar("trx_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(MultiExu_CFG.trx_inflight_num)-1, 0, "0"))
    var rd_tag_prev     = AddStageVar(hw_structvar("rd_tag_prev",       DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    var rd_tag_prev_clr = AddStageVar(hw_structvar("rd_tag_prev_clr",   DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    var cdb_id          = AddStageVar(hw_structvar("cdb_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(rrb_num) -1, 0, "0"))
    val rdy             = AddStageVar(hw_structvar("rdy",               DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    var TRX_ID_COUNTER  = cyclix_gen.uglobal(name_prefix + "_TRX_ID_COUNTER", MultiExu_CFG.trx_inflight_num-1, 0, "0")

    fun FillFromCDB(MultiExu_CFG : Reordex_CFG, cdb : hw_var, io_cdb_rs1_wdata_buf : hw_var) {
        cyclix_gen.MSG_COMMENT("Filling ROB with data from CDB...")

        for (rob_entry_idx in 0 until MultiExu_CFG.ROB_size) {
            var rob_iter = TRX_BUF.GetFracRef(rob_entry_idx)

            for (rob_entry_single_idx in 0 until TRX_BUF_MULTIDIM) {
                var rob_entry_single = rob_iter.GetFracRef(rob_entry_single_idx)

                var CDB_ref         = cdb.GetFracRef(rob_entry_single.GetFracRef("cdb_id"))
                var CDB_ref_enb     = CDB_ref.GetFracRef("enb")
                var CDB_ref_data    = CDB_ref.GetFracRef("data")
                cyclix_gen.begif(CDB_ref_enb)
                run {
                    cyclix_gen.begif(cyclix_gen.eq2(rob_entry_single.GetFracRef("trx_id"), CDB_ref_data.GetFracRef("trx_id")))
                    run {
                        cyclix_gen.assign(rob_entry_single.GetFracRef("rdy"), 1)
                        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
                            cyclix_gen.assign(rob_entry_single.GetFracRef("alu_result"), CDB_ref_data.GetFracRef("wdata"))
                            for (dst_imm in MultiExu_CFG.dst_imms) {
                                cyclix_gen.assign(rob_entry_single.GetFracRef(dst_imm.name), CDB_ref_data.GetFracRef(dst_imm.name))
                            }
                        }
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
            }
        }
        cyclix_gen.MSG_COMMENT("Filling ROB with data from CDB: done")
    }

    open fun Commit(global_structures: __control_structures) {
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
               rrb_num : Int) : rob(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, rrb_num) {

    var curinstr_addr   = AdduStageVar("curinstr_addr", 31, 0, "0")
    var nextinstr_addr  = AdduStageVar("nextinstr_addr", 31, 0, "0")

    var rd_req          = AdduStageVar("rd_req", 0, 0, "0")
    var rd_addr         = AdduStageVar("rd_addr", 4, 0, "0")
    var rd_wdata        = AdduStageVar("rd_wdata", MultiExu_CFG.RF_width-1, 0, "0")

    var csr_rdata       = AdduStageVar("csr_rdata", 31, 0, "0")

    var immediate           = AdduStageVar("immediate", 31, 0, "0")
    var curinstraddr_imm    = AdduStageVar("curinstraddr_imm", 31, 0, "0")

    var funct3          = AdduStageVar("funct3", 2, 0, "0")
    var funct7          = AdduStageVar("funct7", 6, 0, "0")

    var alu_result      = AdduStageVar("alu_result", 31, 0, "0")
    var alu_CF          = AdduStageVar("alu_CF", 0, 0, "0")
    var alu_SF          = AdduStageVar("alu_SF", 0, 0, "0")
    var alu_ZF          = AdduStageVar("alu_ZF", 0, 0, "0")
    var alu_OF          = AdduStageVar("alu_OF", 0, 0, "0")

    var mret_req        = AdduStageVar("mret_req", 0, 0, "0")

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

    var rf_dim = hw_dim_static()
    var Backoff_ARF = cyclix_gen.uglobal("Backoff_ARF", rf_dim, "0")

    var expected_instraddr = cyclix_gen.uglobal("expected_instraddr", 31, 0, hw_imm(32, IMM_BASE_TYPE.HEX, "200"))

    val irq_width   = 8
    val IRQ_ADDR    = 0x80
    var irq_fifo    = cyclix_gen.ufifo_in("irq_fifo", irq_width-1, 0)
    var irq_mcause  = cyclix_gen.ulocal("irq_mcause", irq_width-1, 0, "0")
    var irq_recv    = cyclix_gen.ulocal("irq_recv", 0, 0, "0")
    var MIRQEN      = cyclix_gen.uglobal("MIRQEN", 0, 0, "1")

    var backoff_cmd     = cyclix_gen.ulocal("backoff_cmd", 0, 0, "0")

    var commit_active       = cyclix_gen.ulocal("genrob_commit_active", 0, 0, "1")
    var entry_mask          = cyclix_gen.uglobal("genrob_entry_mask", TRX_BUF_MULTIDIM-1, 0, hw_imm_ones(TRX_BUF_MULTIDIM))
    var genrob_instr_ptr    = cyclix_gen.uglobal("genrob_instr_ptr", GetWidthToContain(TRX_BUF_MULTIDIM)-1, 0, "0")

    init {
        rf_dim.add(31, 0)
        rf_dim.add(31, 0)
    }

    fun Commit(global_structures: __control_structures, pc : hw_var, bufs_to_rollback : ArrayList<hw_stage>, bufs_to_clr : ArrayList<hw_stage>, MRETADDR : hw_var, CSR_MCAUSE : hw_var) {

        preinit_ctrls()
        init_locals()

        cyclix_gen.MSG_COMMENT("Interrupt receive...")
        cyclix_gen.begif(MIRQEN)
        run {
            cyclix_gen.assign(irq_recv, cyclix_gen.fifo_rd_unblk(irq_fifo, irq_mcause))
            cyclix_gen.begif(irq_recv)
            run {
                MIRQEN.assign(0)
                CSR_MCAUSE.assign(irq_mcause)
                MRETADDR.assign(expected_instraddr)
                cyclix_gen.assign(backoff_cmd, 1)
                cyclix_gen.assign(expected_instraddr, hw_imm(IRQ_ADDR))
                cyclix_gen.assign(commit_active, 0)
                cyclix_gen.assign(entry_mask, hw_imm_ones(TRX_BUF_MULTIDIM))
            }; cyclix_gen.endif()
        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("Interrupt receive: done")

        cyclix_gen.MSG_COMMENT("Regular processing of entries...")
        var single_entry = cyclix_gen.begforall_asc(TRX_LOCAL_PARALLEL)
        run {
            cyclix_gen.MSG_COMMENT("Processing single entry...")
            cyclix_gen.begif(cyclix_gen.band(commit_active, entry_mask.GetFracRef(single_entry.iter_num)))
            run {
                cyclix_gen.begif(TRX_LOCAL_PARALLEL.GetFracRef(single_entry.iter_num).GetFracRef("enb"))
                run {

                    switch_to_local(single_entry.iter_num)

                    cyclix_gen.begif(cyclix_gen.band(ctrl_active, commit_active))
                    run {
                        cyclix_gen.begif(cyclix_gen.neq2(expected_instraddr, curinstr_addr))
                        run {
                            cyclix_gen.assign(backoff_cmd, 1)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()

                    cyclix_gen.assign(commit_active, cyclix_gen.band(ctrl_active, rdy))
                    cyclix_gen.begif(commit_active)
                    run {

                        cyclix_gen.assign(commit_active, cyclix_gen.eq2(expected_instraddr, curinstr_addr))
                        cyclix_gen.begif(commit_active)      // instruction flow fine
                        run {

                            cyclix_gen.begif(mret_req)
                            run {
                                MIRQEN.assign(1)
                            }; cyclix_gen.endif()

                        }; cyclix_gen.endif()

                        cyclix_gen.begelse()                          // instruction flow broken
                        run {
                            cyclix_gen.assign(backoff_cmd, 1)
                        }; cyclix_gen.endif()

                    }; cyclix_gen.endif()

                    cyclix_gen.begif(commit_active)
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

                            cyclix_gen.begbranch(RD_MEM)
                            run {
                                rd_wdata.assign(alu_result)
                                rd_rdy.assign(1)
                            }; cyclix_gen.endbranch()

                            cyclix_gen.begbranch(RD_CSR)
                            run {
                                rd_wdata.assign(csr_rdata)
                                rd_rdy.assign(1)
                            }; cyclix_gen.endbranch()

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

                    }; cyclix_gen.endif()       // commit_active

                }; cyclix_gen.endif()           // enb
            }; cyclix_gen.endif()           // commit_active

            cyclix_gen.begif(commit_active)     // instruction finished
            run {
                cyclix_gen.assign(entry_mask.GetFracRef(single_entry.iter_num), 0)
                cyclix_gen.assign(genrob_instr_ptr, single_entry.iter_num_next)
            }; cyclix_gen.endif()

            cyclix_gen.MSG_COMMENT("Processing single entry: done")
        }; cyclix_gen.endloop()
        cyclix_gen.MSG_COMMENT("Regular processing of entries: done")

        cyclix_gen.begif(backoff_cmd)
        run {
            cyclix_gen.assign(pc, expected_instraddr)
            for (buf_to_rollback in bufs_to_rollback) {
                buf_to_rollback.Reset()
            }
            for (buf_to_clr in bufs_to_clr) {
                for (elem_index in 0 until buf_to_clr.TRX_BUF_SIZE) {
                    cyclix_gen.assign(buf_to_clr.TRX_BUF[elem_index].GetFracRef("enb"), 0)
                }
            }
            global_structures.RollBack(Backoff_ARF)
            cyclix_gen.assign(entry_mask, hw_imm_ones(TRX_BUF_MULTIDIM))
            cyclix_gen.assign(genrob_instr_ptr, 0)
        }; cyclix_gen.endif()

        cyclix_gen.COMMENT("Vectored ROB entry completion...")
        cyclix_gen.begif(commit_active)
        run {
            cyclix_gen.assign(pop, 1)
            pop_trx()
            cyclix_gen.assign(entry_mask, hw_imm_ones(TRX_BUF_MULTIDIM))
            cyclix_gen.assign(genrob_instr_ptr, 0)
        }; cyclix_gen.endif()
        cyclix_gen.COMMENT("Vectored ROB entry completion: done")

        finalize_ctrls()
    }
}