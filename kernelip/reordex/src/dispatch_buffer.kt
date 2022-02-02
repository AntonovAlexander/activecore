/*
 * dispatch_buffer.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

internal open class dispatch_buffer(cyclix_gen : cyclix.Generic,
                                    name_prefix : String,
                                    TRX_BUF_SIZE : Int,
                                    MultiExu_CFG : Reordex_CFG,
                                    ExecUnits_size : Int,
                                    cdb_num : Int,
                                    val IQ_insts : ArrayList<iq_buffer>,
                                    val control_structures: __control_structures) : uop_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG.DataPath_width, MultiExu_CFG, cdb_num) {

    var exu_id           = AdduStageVar("exu_id",             GetWidthToContain(ExecUnits_size), 0, "0")

    var rds_ctrl        = ArrayList<ROB_rd_ctrl>()

    var cf_can_alter    = AdduStageVar("cf_can_alter", 0, 0, "0")
    val io_req          = AdduStageVar("io_req", 0, 0, "0")
    var mem_we         = AdduStageVar("mem_we",0, 0, "1")

    var dispatch_active     = cyclix_gen.ulocal("gendispatch_dispatch_active", 0, 0, "1")
    var entry_toproc_mask   = cyclix_gen.uglobal("gendispatch_entry_toproc_mask", TRX_BUF_MULTIDIM-1, 0, hw_imm_ones(TRX_BUF_MULTIDIM))
    var iq_free_mask        = cyclix_gen.ulocal("gendispatch_iq_free_mask", IQ_insts.size-1, 0, hw_imm_ones(IQ_insts.size))
    var store_iq_free_mask  = cyclix_gen.ulocal("gendispatch_store_iq_free_mask", 0, 0, "1")

    init {
        Fill_ROB_rds_ctrl_StageVars(this, MultiExu_CFG.rds.size, rds_ctrl, MultiExu_CFG.PRF_addr_width)

        control_structures.states_toRollBack.add(entry_toproc_mask)
    }

    fun Process(rob : rob, PRF_src : hw_var, store_iq : iq_buffer, ExecUnits : MutableMap<String, Exu_CFG>, CDB_RISC_LSU_POS : Int) {

        cyclix_gen.MSG_COMMENT("sending new operations to IQs...")

        preinit_ctrls()
        init_locals()

        var rob_push_trx_total = rob.GetPushTrx()
        var store_push_trx = store_iq.GetPushTrx()

        cyclix_gen.begif(cyclix_gen.band(ctrl_active, rob.ctrl_rdy))
        run {

            for (entry_num in 0 until MultiExu_CFG.DataPath_width) {

                cyclix_gen.begif(TRX_LOCAL_PARALLEL.GetFracRef(entry_num).GetFracRef("enb"))
                run {

                    switch_to_local(entry_num)
                    var rob_push_trx = rob_push_trx_total.GetFracRef(entry_num)

                    cyclix_gen.begif(cyclix_gen.band(dispatch_active, entry_toproc_mask.GetFracRef(entry_num), enb))
                    run {

                        for (rsrv in src_rsrv) {
                            cyclix_gen.assign(rsrv.src_src, PRF_src.GetFracRef(rsrv.src_tag))
                        }

                        cyclix_gen.assign(dispatch_active, 0)

                        cyclix_gen.begif(io_req)
                        run {
                            cyclix_gen.begif(cyclix_gen.band(store_iq_free_mask, store_iq.ctrl_rdy))
                            run {

                                // pushing trx to IQ
                                cyclix_gen.assign(store_iq.push, 1)
                                cyclix_gen.assign_subStructs(store_push_trx, TRX_LOCAL)
                                cyclix_gen.assign(store_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                                store_iq.push_trx(store_push_trx)

                                // marking rd src
                                cyclix_gen.begif(!mem_we)
                                run {
                                    cyclix_gen.assign(PRF_src.GetFracRef(rds_ctrl[0].tag), CDB_RISC_LSU_POS)
                                }; cyclix_gen.endif()

                                // marking RRB for ROB
                                cyclix_gen.assign(rob_push_trx.GetFracRef("cdb_id"), store_iq.CDB_index)

                                // marking op as scheduled
                                cyclix_gen.assign(entry_toproc_mask.GetFracRef(entry_num), 0)

                                // marking IQ as busy
                                cyclix_gen.assign(store_iq_free_mask, 0)

                                // marking ready to schedule next trx
                                cyclix_gen.assign(dispatch_active, 1)

                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()

                        cyclix_gen.begelse()
                        run {

                            for (IQ_inst_idx in 0 until IQ_insts.size-1) {
                                var IQ_inst = IQ_insts[IQ_inst_idx]

                                cyclix_gen.begif(cyclix_gen.band(entry_toproc_mask.GetFracRef(entry_num), iq_free_mask.GetFracRef(IQ_inst_idx)))
                                run {
                                    cyclix_gen.begif(cyclix_gen.eq2(exu_id, IQ_inst.exu_id_num))
                                    run {
                                        cyclix_gen.begif(IQ_inst.ctrl_rdy)
                                        run {

                                            // pushing trx to IQ
                                            cyclix_gen.assign(IQ_inst.push, 1)
                                            var iq_push_trx = IQ_inst.GetPushTrx()
                                            cyclix_gen.assign_subStructs(iq_push_trx, TRX_LOCAL)
                                            cyclix_gen.assign(iq_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                                            IQ_inst.push_trx(iq_push_trx)

                                            // marking rd src
                                            cyclix_gen.assign(PRF_src.GetFracRef(rds_ctrl[0].tag), IQ_inst.CDB_index)

                                            // marking RRB for ROB
                                            cyclix_gen.assign(rob_push_trx.GetFracRef("cdb_id"), IQ_inst.CDB_index)

                                            // marking op as scheduled
                                            cyclix_gen.assign(entry_toproc_mask.GetFracRef(entry_num), 0)

                                            // marking IQ as busy
                                            cyclix_gen.assign(iq_free_mask.GetFracRef(IQ_inst_idx), 0)

                                            // marking ready to schedule next trx
                                            cyclix_gen.assign(dispatch_active, 1)

                                        }; cyclix_gen.endif()
                                    }; cyclix_gen.endif()
                                }; cyclix_gen.endif()
                            }

                        }; cyclix_gen.endif()

                        cyclix_gen.begif(dispatch_active)
                        run {
                            // pushing to ROB
                            cyclix_gen.assign_subStructs(rob_push_trx, TRX_LOCAL)
                            cyclix_gen.assign(rob_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                            cyclix_gen.assign(rob_push_trx.GetFracRef("rdy"), 0)
                            cyclix_gen.add_gen(rob.TRX_ID_COUNTER, rob.TRX_ID_COUNTER, 1)
                            cyclix_gen.assign(rob.push, 1)
                        }; cyclix_gen.endif()

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()
            }

            cyclix_gen.begif(dispatch_active)
            run {
                cyclix_gen.assign(pop, 1)
            }; cyclix_gen.endif()

        }; cyclix_gen.endif()

        cyclix_gen.begif(rob.push)
        run {
            rob.push_trx(rob_push_trx_total)
        }; cyclix_gen.endif()

        cyclix_gen.begif(pop)
        run {
            pop_trx()
            cyclix_gen.assign(entry_toproc_mask, hw_imm_ones(TRX_BUF_MULTIDIM))
        }; cyclix_gen.endif()

        finalize_ctrls()               //  TODO: cleanup

        cyclix_gen.MSG_COMMENT("sending new operation to IQs: done")
    }
}

internal class dispatch_buffer_risc(cyclix_gen : cyclix.Generic,
                         name_prefix : String,
                         TRX_BUF_SIZE : Int,
                         MultiExu_CFG : Reordex_CFG,
                         ExecUnits_size : Int,
                         cdb_num : Int,
                         IQ_insts : ArrayList<iq_buffer>,
                         control_structures: __control_structures) : dispatch_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, ExecUnits_size, cdb_num, IQ_insts, control_structures) {

    var curinstr_addr   = AdduStageVar("curinstr_addr", 31, 0, "0")
    var nextinstr_addr  = AdduStageVar("nextinstr_addr", 31, 0, "0")

    var rss = ArrayList<RISCDecoder_rs>()
    var rds = ArrayList<RISCDecoder_rd>()

    var csr_rdata       = AdduStageVar("csr_rdata", 31, 0, "0")

    var immediate       = AdduStageVar("immediate", 31, 0, "0")

    var fencereq        = AdduStageVar("fencereq", 0, 0, "0")
    var pred            = AdduStageVar("pred", 3, 0, "0")
    var succ            = AdduStageVar("succ", 3, 0, "0")

    var ecallreq        = AdduStageVar("ecallreq", 0, 0, "0")
    var ebreakreq       = AdduStageVar("ebreakreq", 0, 0, "0")

    var csrreq          = AdduStageVar("csrreq", 0, 0, "0")
    var csrnum          = AdduStageVar("csrnum", 11, 0, "0")
    var zimm            = AdduStageVar("zimm", 4, 0, "0")

    var op1_source      = AdduStageVar("op1_source", 1, 0, "0")
    var op2_source      = AdduStageVar("op2_source", 1, 0, "0")

    // ALU control
    var alu_req         = AdduStageVar("alu_req", 0, 0, "0")
    var alu_op1         = AdduStageVar("alu_op1", 31, 0, "0")
    var alu_op2         = AdduStageVar("alu_op2", 31, 0, "0")
    var alu_op1_wide    = AdduStageVar("alu_op1_wide", 32, 0, "0")
    var alu_op2_wide    = AdduStageVar("alu_op2_wide", 32, 0, "0")

    var alu_result_wide = AdduStageVar("alu_result_wide", 32, 0, "0")
    var alu_result      = AdduStageVar("alu_result", 31, 0, "0")
    var alu_CF          = AdduStageVar("alu_CF", 0, 0, "0")
    var alu_SF          = AdduStageVar("alu_SF", 0, 0, "0")
    var alu_ZF          = AdduStageVar("alu_ZF", 0, 0, "0")
    var alu_OF          = AdduStageVar("alu_OF", 0, 0, "0")
    var alu_overflow    = AdduStageVar("alu_overflow", 0, 0, "0")

    // data memory control
    var mem_req         = AdduStageVar("mem_req", 0, 0, "0")
    //var mem_cmd         = AdduStageVar("mem_cmd", 0, 0, "0")
    var mem_addr        = AdduStageVar("mem_addr", 31, 0, "0")
    var mem_be          = AdduStageVar("mem_be", 3, 0, "0")
    var mem_wdata       = AdduStageVar("mem_wdata", 31, 0, "0")
    var mem_rdata       = AdduStageVar("mem_rdata", 31, 0, "0")
    var mem_rshift      = AdduStageVar("mem_rshift", 0, 0, "0")
    var mem_load_signext = AdduStageVar("mem_load_signext", 0, 0, "0")

    var mret_req        = AdduStageVar("mret_req", 0, 0, "0")

    init {
        Fill_RISCDecoder_rss_StageVars(this, MultiExu_CFG.srcs.size, rss, MultiExu_CFG.ARF_addr_width, MultiExu_CFG.RF_width)
        Fill_RISCDecoder_rds_StageVars(this, MultiExu_CFG.rds.size, rds, MultiExu_CFG.ARF_addr_width)
    }
}