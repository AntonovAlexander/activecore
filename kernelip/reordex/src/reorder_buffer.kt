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
import kotlin.math.log2

internal open class rob(cyclix_gen : cyclix.Generic,
                        name_prefix : String,
                        TRX_BUF_SIZE : Int,
                        MultiExu_CFG : Reordex_CFG,
                        rrb_num : Int,
                        var control_structures: __control_structures)
    : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG.DataPath_width, MultiExu_CFG) {

    var trx_id          = AddStageVar(hw_structvar("trx_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(MultiExu_CFG.trx_inflight_num)-1, 0, "0"))
    var cdb_id          = AddStageVar(hw_structvar("cdb_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(rrb_num) -1, 0, "0"))
    val rdy             = AddStageVar(hw_structvar("rdy",               DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    var rds             = ArrayList<RISCDecoder_rd>()
    var rds_ctrl        = ArrayList<ROB_rd_ctrl>()

    var TRX_ID_COUNTER  = cyclix_gen.uglobal(name_prefix + "_TRX_ID_COUNTER", GetWidthToContain(MultiExu_CFG.trx_inflight_num)-1, 0, "0")

    init {
        Fill_RISCDecoder_rds_StageVars(this, MultiExu_CFG.rds.size, rds, MultiExu_CFG.ARF_addr_width)
        Fill_ROB_rds_ctrl_StageVars(this, MultiExu_CFG.rds.size, rds_ctrl, MultiExu_CFG.PRF_addr_width)
    }

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
                            cyclix_gen.assign(rob_entry_single.GetFracRef("rd0_wdata"), CDB_ref_data.GetFracRef("rd0_wdata"))
                            for (dst_imm in MultiExu_CFG.dst_imms) {
                                cyclix_gen.assign(rob_entry_single.GetFracRef(dst_imm.name), CDB_ref_data.GetFracRef(dst_imm.name))
                            }
                            cyclix_gen.begif(CDB_ref_data.GetFracRef("branch_req"))
                            run {
                                cyclix_gen.assign(rob_entry_single.GetFracRef("nextinstr_addr"), CDB_ref_data.GetFracRef("branch_vec"))
                            }; cyclix_gen.endif()
                        }
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
            }
        }
        cyclix_gen.MSG_COMMENT("Filling ROB with data from CDB: done")
    }

    open fun Commit(control_structures: __control_structures) {
        preinit_ctrls()
        init_locals()

        cyclix_gen.begif(ctrl_active)
        run {
            cyclix_gen.begif(rdy)
            run {
                if (control_structures is __control_structures_renaming) {
                    for (rd_idx in 0 until MultiExu_CFG.rds.size) {
                        cyclix_gen.begif(rds_ctrl[rd_idx].tag_prev_clr)
                        run {
                            control_structures.FreePRF(rds_ctrl[rd_idx].tag_prev)
                        }; cyclix_gen.endif()
                    }
                }
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

internal class rob_risc(name: String,
               cyclix_gen : cyclix.Generic,
               name_prefix : String,
               TRX_BUF_SIZE : Int,
               MultiExu_CFG : Reordex_CFG,
               rrb_num : Int,
               control_structures : __control_structures) : rob(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, rrb_num, control_structures) {

    var curinstr_addr   = AdduStageVar("curinstr_addr", 31, 0, "0")
    var nextinstr_addr  = AdduStageVar("nextinstr_addr", 31, 0, "0")
    var cf_can_alter    = AdduStageVar("cf_can_alter", 0, 0, "0")

    var csr_rdata       = AdduStageVar("csr_rdata", 31, 0, "0")

    var immediate           = AdduStageVar("immediate", 31, 0, "0")
    var curinstraddr_imm    = AdduStageVar("curinstraddr_imm", 31, 0, "0")

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

    var expected_instraddr  = cyclix_gen.uglobal("expected_instraddr", 31, 0, hw_imm(32, IMM_BASE_TYPE.HEX, "200"))
    var prev_instraddr      = cyclix_gen.uglobal("prev_instraddr", 31, 0, hw_imm(32, IMM_BASE_TYPE.HEX, "200"))

    val irq_width   = 8
    val IRQ_ADDR    = 0x80
    var irq_fifo    = cyclix_gen.ufifo_in("irq_fifo", irq_width-1, 0)
    var irq_mcause  = cyclix_gen.ulocal("irq_mcause", irq_width-1, 0, "0")
    var irq_recv    = cyclix_gen.ulocal("irq_recv", 0, 0, "0")
    var MIRQEN      = cyclix_gen.uglobal("MIRQEN", 0, 0, "1")

    var backoff_cmd     = cyclix_gen.ulocal("genbackoff_cmd", 0, 0, "0")

    var commit_active       = cyclix_gen.ulocal("genrob_commit_active", 0, 0, "1")
    var entry_mask          = cyclix_gen.uglobal("genrob_entry_mask", TRX_BUF_MULTIDIM-1, 0, hw_imm_ones(TRX_BUF_MULTIDIM))
    var genrob_instr_ptr    = cyclix_gen.uglobal("genrob_instr_ptr", GetWidthToContain(TRX_BUF_MULTIDIM)-1, 0, "0")

    var btac_upd_found = cyclix_gen.ulocal("genbtac_upd_found", 0, 0, "0")
    var btac_upd_ptr = cyclix_gen.ulocal("genbtac_upd_ptr", GetWidthToContain(MultiExu_CFG.BTAC_SIZE)-1, 0, "0")
    var btac_upd_fetch = cyclix_gen.ulocal("genbtac_upd_fetch", 31, 0, "0")
    var btac_upd_lru_ptr = ArrayList<hw_var>()

    init {
        control_structures.states_toRollBack.add(entry_mask)
        control_structures.states_toRollBack.add(genrob_instr_ptr)
        for (i in 0 until log2(MultiExu_CFG.BTAC_SIZE.toDouble()).toInt()) {
            btac_upd_lru_ptr.add(cyclix_gen.ulocal("genbtac_upd_ptr_lvl" + i, i, 0, "0"))
        }
    }

    fun Commit(instr_iaddr : instr_iaddr_stage, bufs_to_rollback : ArrayList<hw_stage>, bufs_to_clr : ArrayList<hw_stage>, MRETADDR : hw_var, CSR_MCAUSE : hw_var) {

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

        cyclix_gen.assign(commit_active, ctrl_active)
        cyclix_gen.begif(commit_active)
        run {

            cyclix_gen.MSG_COMMENT("Regular processing of entries...")
            for (single_entry_idx in 0 until TRX_BUF_MULTIDIM)
            {
                switch_to_local(single_entry_idx)

                cyclix_gen.MSG_COMMENT("Processing single entry...")

                cyclix_gen.begif(cyclix_gen.band(commit_active, TRX_LOCAL_PARALLEL.GetFracRef(single_entry_idx).GetFracRef("enb"), entry_mask.GetFracRef(single_entry_idx)))
                run {

                    cyclix_gen.begif(cyclix_gen.neq2(expected_instraddr, curinstr_addr))    // instruction flow broken
                    run {
                        cyclix_gen.assign(backoff_cmd, 1)
                        cyclix_gen.assign(commit_active, 0)
                    }; cyclix_gen.endif()

                    cyclix_gen.assign(genrob_instr_ptr, single_entry_idx)
                    cyclix_gen.band_gen(commit_active, commit_active, rdy)

                    cyclix_gen.begif(commit_active)         // instruction ready to retire
                    run {

                        cyclix_gen.COMMENT("committing RF...")

                        cyclix_gen.begif(mret_req)
                        run {
                            MIRQEN.assign(1)
                        }; cyclix_gen.endif()

                        // rd wdata processing
                        cyclix_gen.begcase(rds[0].source)
                        run {

                            cyclix_gen.begbranch(RD_CSR)
                            run {
                                rds[0].wdata.assign(csr_rdata)
                                rds[0].rdy.assign(1)
                            }; cyclix_gen.endbranch()

                        }; cyclix_gen.endcase()

                        if (control_structures is __control_structures_renaming) {
                            cyclix_gen.begif(rds_ctrl[0].tag_prev_clr)
                            run {
                                (control_structures as __control_structures_renaming).FreePRF(rds_ctrl[0].tag_prev)
                            }; cyclix_gen.endif()
                        }

                        cyclix_gen.begif(rds[0].req)
                        run {
                            cyclix_gen.assign(control_structures.Backoff_ARF.GetFracRef(rds[0].addr), rds[0].wdata)
                        }; cyclix_gen.endif()

                        cyclix_gen.COMMENT("committing RF: done")

                        cyclix_gen.assign(prev_instraddr, curinstr_addr)
                        cyclix_gen.assign(expected_instraddr, nextinstr_addr)

                        cyclix_gen.assign(entry_mask.GetFracRef(single_entry_idx), 0)

                    }; cyclix_gen.endif()       // instruction ready to retire

                }; cyclix_gen.endif()           // instruction should be processed

                cyclix_gen.MSG_COMMENT("Processing single entry: done")
            }
            cyclix_gen.MSG_COMMENT("Regular processing of entries: done")

        }; cyclix_gen.endif()

        cyclix_gen.MSG_COMMENT("Searching BTAC...")
        for (i in 0 until log2(MultiExu_CFG.BTAC_SIZE.toDouble()).toInt()) {
            if (i == 0) {
                cyclix_gen.assign(btac_upd_lru_ptr[i], cyclix_gen.bnot(instr_iaddr.mru_ptrs[i].readPrev()))
            } else {
                cyclix_gen.assign(btac_upd_lru_ptr[i], cyclix_gen.cnct(btac_upd_lru_ptr[i-1], cyclix_gen.bnot(instr_iaddr.mru_ptrs[i].readPrev()).GetFracRef(btac_upd_lru_ptr[i-1])))
            }
        }
        cyclix_gen.assign(btac_upd_ptr, btac_upd_lru_ptr.last())
        for (btac_idx in 0 until MultiExu_CFG.BTAC_SIZE) {
            var btac_enb = instr_iaddr.BTAC.GetFracRef(btac_idx).GetFracRef("Enb")
            var btac_bpc = instr_iaddr.BTAC.GetFracRef(btac_idx).GetFracRef("Bpc")
            var btac_btgt = instr_iaddr.BTAC.GetFracRef(btac_idx).GetFracRef("Btgt")
            cyclix_gen.begif(cyclix_gen.eq2(prev_instraddr, btac_bpc))
            run {
                cyclix_gen.assign(btac_upd_found, btac_enb)
                cyclix_gen.begif(btac_enb)
                run {
                    cyclix_gen.assign(btac_upd_ptr, btac_idx)
                }; cyclix_gen.endif()
                cyclix_gen.assign(btac_upd_fetch, btac_btgt)
            }; cyclix_gen.endif()
        }
        cyclix_gen.MSG_COMMENT("Searching BTAC: done")

        cyclix_gen.MSG_COMMENT("Backing off...")
        cyclix_gen.begif(backoff_cmd)
        run {
            cyclix_gen.assign(instr_iaddr.iaddr_enb, 0)
            cyclix_gen.assign(instr_iaddr.pc, expected_instraddr)
            for (buf_to_rollback in bufs_to_rollback) {
                buf_to_rollback.Reset()
            }
            for (buf_to_clr in bufs_to_clr) {
                for (elem_index in 0 until buf_to_clr.TRX_BUF_SIZE) {
                    cyclix_gen.assign(buf_to_clr.TRX_BUF[elem_index].GetFracRef("enb"), 0)
                }
            }

            cyclix_gen.MSG_COMMENT("Rolling back...")
            control_structures.RollBack()
            cyclix_gen.MSG_COMMENT("Rolling back: done")

            cyclix_gen.MSG_COMMENT("BTAC processing...")
            cyclix_gen.assign(instr_iaddr.BTAC.GetFracRef(btac_upd_ptr).GetFracRef("Enb"), 1)
            cyclix_gen.assign(instr_iaddr.BTAC.GetFracRef(btac_upd_ptr).GetFracRef("Bpc"), prev_instraddr)
            cyclix_gen.assign(instr_iaddr.BTAC.GetFracRef(btac_upd_ptr).GetFracRef("Btgt"), expected_instraddr)
            cyclix_gen.MSG_COMMENT("BTAC processing: done")

        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("Backing off: done")

        cyclix_gen.COMMENT("Vectored ROB entry completion...")
        cyclix_gen.begelsif(commit_active)
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