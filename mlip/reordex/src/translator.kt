/*
 * translator.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*
import cyclix.hw_stage

data class __exu_info(val module : cyclix.Streaming,
                      val req_bus : hw_var,
                      val resp_bus : hw_var)

data class __iq_info(val iq: hw_var,
                     val iq_wr_ptr: hw_var,
                     val iq_wr_ptr_prev: hw_var,
                     val iq_wr_ptr_inc: hw_var,
                     val iq_wr_ptr_dec: hw_var,
                     val iq_wr: hw_var,
                     val iq_rd: hw_var,
                     val iq_full: hw_var,
                     val iq_head: hw_var,
                     val iq_num: hw_imm,
                     val iq_exu: Boolean)

class __TranslateInfo() {
    var exu_assocs = mutableMapOf<Exu, __exu_info>()
}

data class __RF_rs_req(val rs_rdy : hw_var,
                       val rs_tag : hw_var,
                       val rs_rdata : hw_var)

open class uop_buffer(cyclix_gen : cyclix.Generic,
                 name_prefix : String,
                 TRX_BUF_SIZE : Int,
                 ExecUnits_size : Int,
                 trx_struct : hw_struct,
                 MultiExu_cfg_rf : MultiExu_CFG_RF,
                 Exu_cfg_rf : Exu_CFG_RF) : hw_stage(cyclix_gen, name_prefix, trx_struct, TRX_BUF_SIZE, false) {

    val enb             = AddLocal("enb")
    val fu_req          = AddLocal("fu_req")
    val fu_pending      = AddLocal("fu_pending")
    val fu_id           = AddLocal("fu_id")
    val fu_opcode       = AddLocal("fu_opcode")
    val rs_rsrv         = ArrayList<__RF_rs_req>()
    val rd_tag          = AddLocal("rd_tag")
    val rd_tag_prev     = AddLocal("rd_tag_prev")
    val rd_tag_prev_clr = AddLocal("rd_tag_prev_clr")
    val wb_ext          = AddLocal("wb_ext")
    val rdy             = AddLocal("rdy")

    init {
        for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
            rs_rsrv.add(__RF_rs_req(
                AddLocal("rs" + RF_rs_idx + "_rdy"),
                AddLocal("rs" + RF_rs_idx + "_tag"),
                AddLocal("rs" + RF_rs_idx + "_rdata")
            ))
        }
    }
}

class iq_buffer(cyclix_gen : cyclix.Generic,
                name_prefix : String,
                TRX_BUF_SIZE : Int,
                ExecUnits_size : Int,
                trx_struct : hw_struct,
                MultiExu_cfg_rf : MultiExu_CFG_RF,
                Exu_cfg_rf : Exu_CFG_RF,
                val iq_num: hw_imm,
                val iq_exu: Boolean) : uop_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, ExecUnits_size, trx_struct, MultiExu_cfg_rf, Exu_cfg_rf) {

    val rd = cyclix_gen.ulocal(name_prefix + "_rd", 0, 0, "0")
    val wr = cyclix_gen.ulocal(name_prefix + "_wr", 0, 0, "0")
}