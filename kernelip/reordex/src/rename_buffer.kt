/*
 * rename_buffer.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

open class rename_buffer(cyclix_gen : cyclix.Generic,
                         name_prefix : String,
                         TRX_BUF_SIZE : Int,
                         MultiExu_CFG : Reordex_CFG,
                         ExecUnits_size : Int,
                         cdb_num : Int) : uop_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, cdb_num) {

    var fu_id           = AddStageVar(hw_structvar("fu_id",             DATA_TYPE.BV_UNSIGNED, GetWidthToContain(ExecUnits_size), 0, "0"))
    val rd_tag          = AddStageVar(hw_structvar("rd_tag",            DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    val rd_tag_prev     = AddStageVar(hw_structvar("rd_tag_prev",       DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    val rd_tag_prev_clr = AddStageVar(hw_structvar("rd_tag_prev_clr",   DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    val wb_ext          = AddStageVar(hw_structvar("wb_ext",            DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    fun Process(rob : rob_buffer, PRF_src : hw_var, store_iq : iq_buffer, ExecUnits : MutableMap<String, Exu_CFG>, exu_descrs : MutableMap<String, __exu_descr>) {

        var rob_push_trx = rob.GetPushTrx()
        cyclix_gen.assign(rob_push_trx.GetFracRef("enb"), 1)
        cyclix_gen.assign(rob_push_trx.GetFracRef("rd_tag_prev"), rd_tag_prev)
        cyclix_gen.assign(rob_push_trx.GetFracRef("rd_tag_prev_clr"), rd_tag_prev_clr)
        cyclix_gen.assign(rob_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)

        cyclix_gen.begif(ctrl_active)
        run {

            var num_rs = 0
            for (rs_rsrv in rs_rsrv) {
                cyclix_gen.assign(TRX_BUF_head_ref.GetFracRef("rs" + num_rs + "_src"), PRF_src.GetFracRef(rs_rsrv.rs_tag))      // TODO: stage context cleanup
                num_rs++
            }

            cyclix_gen.begif(wb_ext)
            run {
                cyclix_gen.begif(store_iq.ctrl_rdy)
                run {

                    // signaling iq_wr
                    cyclix_gen.assign(store_iq.push, 1)

                    var store_push_trx = store_iq.GetPushTrx()
                    cyclix_gen.assign_subStructs(store_push_trx, TRX_BUF_head_ref)
                    cyclix_gen.assign(store_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                    store_iq.push_trx(store_push_trx)

                    cyclix_gen.assign(rob_push_trx.GetFracRef("cdb_id"), store_iq.CDB_index)

                    // clearing renamed uop buffer
                    cyclix_gen.assign(pop, 1)

                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

            cyclix_gen.begelse()
            run {
                var ExUnit_num = 0
                for (ExUnit in ExecUnits) {
                    var IQ_inst = exu_descrs[ExUnit.key]!!.IQ_insts[0]      // TODO: multiple queues

                    cyclix_gen.begif(cyclix_gen.eq2(fu_id, IQ_inst.fu_id_num))
                    run {
                        cyclix_gen.begif(IQ_inst.ctrl_rdy)
                        run {

                            // signaling iq_wr
                            cyclix_gen.assign(IQ_inst.push, 1)

                            var iq_push_trx = IQ_inst.GetPushTrx()
                            cyclix_gen.assign_subStructs(iq_push_trx, TRX_BUF_head_ref)
                            cyclix_gen.assign(iq_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                            IQ_inst.push_trx(iq_push_trx)

                            cyclix_gen.assign(PRF_src.GetFracRef(rd_tag), IQ_inst.CDB_index)

                            cyclix_gen.assign(rob_push_trx.GetFracRef("cdb_id"), IQ_inst.CDB_index)

                            // clearing renamed uop buffer
                            cyclix_gen.assign(pop, 1)

                            cyclix_gen.assign(rob.push, 1)

                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()

                    ExUnit_num++
                }
            }; cyclix_gen.endif()

        }; cyclix_gen.endif()

        cyclix_gen.begif(pop)
        run {
            cyclix_gen.begif(rob.push)
            run {
                cyclix_gen.assign(rob_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                cyclix_gen.assign(rob.TRX_ID_COUNTER, cyclix_gen.add(rob.TRX_ID_COUNTER, 1))
                rob.push_trx(rob_push_trx)
            }; cyclix_gen.endif()
            pop_trx()
        }; cyclix_gen.endif()
        finalize_ctrls()               //  TODO: cleanup
    }
}

class rename_buffer_risc(cyclix_gen : cyclix.Generic,
                         name_prefix : String,
                         TRX_BUF_SIZE : Int,
                         MultiExu_CFG : Reordex_CFG,
                         ExecUnits_size : Int,
                         cdb_num : Int) : rename_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, ExecUnits_size, cdb_num) {

    var curinstr_addr   = AdduStageVar("curinstr_addr", 31, 0, "0")
    var nextinstr_addr  = AdduStageVar("nextinstr_addr", 31, 0, "0")

    var mem_req         = AdduStageVar("mem_req", 0, 0, "0")
    var mem_cmd         = AdduStageVar("mem_cmd", 0, 0, "0")
    var mem_addr        = AdduStageVar("mem_addr", 31, 0, "0")
    var mem_be          = AdduStageVar("mem_be", 3, 0, "0")
}