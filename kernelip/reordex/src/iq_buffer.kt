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

internal open class iq_buffer(cyclix_gen : cyclix.Generic,
                     ExUnit_name : String,
                     ExUnit_num : Int,
                     name_prefix : String,
                     TRX_BUF_SIZE : Int,
                     MultiExu_CFG : Reordex_CFG,
                     val exu_id_num: hw_imm,
                     val iq_exu: Boolean,
                     var CDB_index : Int,
                     cdb_num : Int) : uop_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, cdb_num) {

    var trx_id      = AddStageVar(hw_structvar("trx_id",     DATA_TYPE.BV_UNSIGNED, GetWidthToContain(MultiExu_CFG.trx_inflight_num)-1, 0, "0"))
    var fu_pending  = AddStageVar(hw_structvar("fu_pending", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    val rs_srcs     = ArrayList<hw_var>()
    var rd_ctrls    = ArrayList<iq_rd_ctrl>()
    val io_req      = AddStageVar(hw_structvar("io_req",    DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    var srcs_rdy        = cyclix_gen.ulocal((ExUnit_name + ExUnit_num + "_srcs_rdy"), TRX_BUF_SIZE-1, 0, "0")
    var op_issue        = cyclix_gen.ulocal((ExUnit_name + ExUnit_num + "_op_issue"), 0, 0, "0")
    var op_issued_num   = cyclix_gen.ulocal((ExUnit_name + ExUnit_num + "_op_issued_num"), GetWidthToContain(TRX_BUF.GetWidth())-1, 0, "0")

    var curinstr_addr  = AdduStageVar("curinstr_addr", 31, 0, "0")
    var nextinstr_addr = AdduStageVar("nextinstr_addr", 31, 0, "0")
    var immediate      = AdduStageVar("immediate", 31, 0, "0")

    init {
        for (rd_idx in 0 until MultiExu_CFG.rds.size) {
            rd_ctrls.add(iq_rd_ctrl(
                AddStageVar(hw_structvar("rd" + rd_idx + "_req",   DATA_TYPE.BV_UNSIGNED, 0, 0, "0")),
                AddStageVar(hw_structvar("rd" + rd_idx + "_tag",   DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
            ))
        }
    }

    fun Issue(ExUnit : Exu_CFG, exu_req : hw_var, subproc : hw_subproc, ExUnit_num : Int) {
        cyclix_gen.MSG_COMMENT("selecting uop to issue...")

        cyclix_gen.assign(op_issue, 0)

        cyclix_gen.begif(ctrl_active)
        run {

            for (trx_idx in 0 until TRX_BUF_SIZE) {

                var cur_srcs_rdy        = srcs_rdy.GetFracRef(trx_idx)
                var iq_entry            = TRX_BUF.GetFracRef(trx_idx)
                var iq_entry_enb        = iq_entry.GetFracRef("enb")
                var iq_entry_fu_pending = iq_entry.GetFracRef("fu_pending")
                var iq_entry_rd0_tag    = iq_entry.GetFracRef("rd0_tag")
                var iq_entry_rdy        = iq_entry.GetFracRef("rdy")

                cyclix_gen.begif(!op_issue)
                run {

                    cyclix_gen.begif(iq_entry_enb)
                    run {
                        cyclix_gen.assign(cur_srcs_rdy, 1)
                        for (RF_rs_idx in 0 until MultiExu_CFG.srcs.size) {
                            cyclix_gen.band_gen(cur_srcs_rdy, cur_srcs_rdy, iq_entry.GetFracRef("src" + RF_rs_idx + "_rdy"))
                        }
                        cyclix_gen.begif(cur_srcs_rdy)
                        run {
                            cyclix_gen.assign(op_issue, 1)
                            cyclix_gen.assign(op_issued_num, trx_idx)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()
            }

            cyclix_gen.MSG_COMMENT("selecting uop to issue: done")

        }; cyclix_gen.endif()

        cyclix_gen.MSG_COMMENT("issuing IQ...")
        cyclix_gen.begif(op_issue)
        run {

            // filling exu_req with iq data
            cyclix_gen.assign_subStructs(exu_req, TRX_BUF.GetFracRef(op_issued_num))

            // writing op to FU
            cyclix_gen.begif(cyclix_gen.fifo_internal_wr_unblk(subproc, STREAM_REQ_BUS_NAME, exu_req))
            run {
                remove_and_squash_trx(op_issued_num)
                pop.assign(1)
            }; cyclix_gen.endif()

        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("issuing IQ: done")
    }

    fun FillFromCDB(cdb : hw_var) {
        for (trx_idx in 0 until TRX_BUF_SIZE) {
            var iq_entry            = TRX_BUF.GetFracRef(trx_idx)
            var iq_entry_enb        = iq_entry.GetFracRef("enb")
            var iq_entry_rdy        = iq_entry.GetFracRef("rdy")
            var iq_entry_io_req     = iq_entry.GetFracRef("io_req")

            cyclix_gen.begif(iq_entry_enb)
            run {

                for (RF_rs_idx in 0 until MultiExu_CFG.srcs.size) {

                    var iq_entry_src_tag     = iq_entry.GetFracRef("src" + RF_rs_idx + "_tag")
                    var iq_entry_src_src     = iq_entry.GetFracRef("src" + RF_rs_idx + "_src")
                    var iq_entry_src_rdy     = iq_entry.GetFracRef("src" + RF_rs_idx + "_rdy")
                    var iq_entry_src_data    = iq_entry.GetFracRef("src" + RF_rs_idx + "_data")

                    cyclix_gen.begif(!iq_entry_src_rdy)
                    run {

                        var src_cdb         = cdb.GetFracRef(iq_entry_src_src)
                        var src_cdb_enb     = src_cdb.GetFracRef("enb")
                        var src_cdb_data    = src_cdb.GetFracRef("data")

                        for (rd_idx in 0 until MultiExu_CFG.rds.size) {

                            var src_cdb_req     = src_cdb_data.GetFracRef("rd" + rd_idx + "_req")
                            var src_cdb_tag     = src_cdb_data.GetFracRef("rd" + rd_idx + "_tag")
                            var src_cdb_wdata   = src_cdb_data.GetFracRef("rd" + rd_idx + "_wdata")

                            cyclix_gen.begif(src_cdb_req)
                            run {
                                cyclix_gen.begif(cyclix_gen.eq2(iq_entry_src_tag, src_cdb_tag))
                                run {
                                    // setting IQ entry ready
                                    cyclix_gen.assign(iq_entry_src_data, src_cdb_wdata)
                                    cyclix_gen.assign(iq_entry_src_rdy, 1)
                                }; cyclix_gen.endif()
                            }; cyclix_gen.endif()

                        }

                    }; cyclix_gen.endif()
                }

                //// setting rdy if data generated ////
                // io_req //
                cyclix_gen.begif(iq_entry_io_req)
                run {
                    cyclix_gen.assign(iq_entry_rdy, iq_entry.GetFracRef("src0_rdy"))
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()
        }
    }
}
