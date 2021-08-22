/*
 * iq_buffer.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*
import cyclix.*

class iq_buffer(cyclix_gen : cyclix.Generic,
                ExUnit_name : String,
                ExUnit_num : Int,
                name_prefix : String,
                TRX_BUF_SIZE : Int,
                val MultiExu_CFG : Reordex_CFG,
                val fu_id_num: hw_imm,
                val iq_exu: Boolean,
                var CDB_index : Int,
                cdb_num : Int) : uop_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, cdb_num) {

    var trx_id      = AddStageVar(hw_structvar("trx_id",     DATA_TYPE.BV_UNSIGNED, GetWidthToContain(MultiExu_CFG.trx_inflight_num)-1, 0, "0"))
    var fu_pending  = AddStageVar(hw_structvar("fu_pending", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    val rs_srcs     = ArrayList<hw_var>()
    val rd_tag      = AddStageVar(hw_structvar("rd0_tag",   DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    val io_req      = AddStageVar(hw_structvar("io_req",    DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    var rss_rdy         = cyclix_gen.ulocal((ExUnit_name + ExUnit_num + "_rss_rdy"), TRX_BUF_SIZE-1, 0, "0")
    var op_issue        = cyclix_gen.ulocal((ExUnit_name + ExUnit_num + "_op_issue"), 0, 0, "0")
    var op_issued_num   = cyclix_gen.ulocal((ExUnit_name + ExUnit_num + "_op_issued_num"), GetWidthToContain(TRX_BUF.GetWidth())-1, 0, "0")

    val curinstr_addr  = AdduStageVar("curinstr_addr", 31, 0, "0")          // for debug purposes
    var immediate      = AdduStageVar("immediate", 31, 0, "0")

    fun Issue(ExUnit : Exu_CFG, exu_req : hw_var, subproc : hw_subproc, ExUnit_num : Int) {
        cyclix_gen.MSG_COMMENT("selecting uop to issue...")

        cyclix_gen.assign(op_issue, 0)

        var iq_iter = cyclix_gen.begforall_asc(TRX_BUF)
        run {

            var cur_rss_rdy         = rss_rdy.GetFracRef(iq_iter.iter_num)
            var iq_entry            = TRX_BUF.GetFracRef(iq_iter.iter_num)
            var iq_entry_enb        = iq_entry.GetFracRef("enb")
            var iq_entry_fu_pending = iq_entry.GetFracRef("fu_pending")
            var iq_entry_rd0_tag    = iq_entry.GetFracRef("rd0_tag")
            var iq_entry_rdy        = iq_entry.GetFracRef("rdy")

            cyclix_gen.begif(!op_issue)
            run {

                cyclix_gen.begif(iq_entry_enb)
                run {
                    cyclix_gen.assign(cur_rss_rdy, 1)
                    for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
                        cyclix_gen.band_gen(cur_rss_rdy, cur_rss_rdy, iq_entry.GetFracRef("rs" + RF_rs_idx + "_rdy"))
                    }
                    cyclix_gen.begif(cur_rss_rdy)
                    run {
                        cyclix_gen.assign(op_issue, 1)
                        cyclix_gen.assign(op_issued_num, iq_iter.iter_num)
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()

        }; cyclix_gen.endloop()

        cyclix_gen.MSG_COMMENT("selecting uop to issue: done")

        cyclix_gen.MSG_COMMENT("issuing IQ...")
        cyclix_gen.begif(op_issue)
        run {

            // filling exu_req with iq data
            cyclix_gen.assign_subStructs(exu_req, TRX_BUF.GetFracRef(op_issued_num))

            // writing op to FU
            cyclix_gen.begif(cyclix_gen.fifo_internal_wr_unblk(subproc, cyclix.STREAM_REQ_BUS_NAME, exu_req))
            run {
                remove_and_squash_trx(op_issued_num)
                pop.assign(1)
            }; cyclix_gen.endif()

        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("issuing IQ: done")
    }
}