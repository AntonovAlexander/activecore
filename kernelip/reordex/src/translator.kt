/*
 * translator.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*
import cyclix.*

data class __exu_info(val module : cyclix.Streaming,
                      val req_bus : hw_var,
                      val resp_bus : hw_var)

class __TranslateInfo() {
    var exu_assocs = mutableMapOf<Exu, __exu_info>()
}

data class __RF_rs_req(val rs_rdy : hw_var,
                       val rs_tag : hw_var,
                       val rs_src : hw_var,
                       val rs_rdata : hw_var)

open class uop_buffer(cyclix_gen : cyclix.Generic,
                 name_prefix : String,
                 TRX_BUF_SIZE : Int,
                 trx_struct : hw_struct,
                 MultiExu_CFG : Reordex_CFG) : hw_stage(cyclix_gen, name_prefix, trx_struct, TRX_BUF_SIZE, STAGE_FC_MODE.BUFFERED, false) {

    val enb             = AddLocal("enb")
    val fu_req          = AddLocal("fu_req")
    val fu_pending      = AddLocal("fu_pending")
    val fu_id           = AddLocal("fu_id")
    val imm_rsrv        = ArrayList<hw_var>()
    val rs_rsrv         = ArrayList<__RF_rs_req>()
    val rd_tag          = AddLocal("rd_tag")
    val rd_tag_prev     = AddLocal("rd_tag_prev")
    val rd_tag_prev_clr = AddLocal("rd_tag_prev_clr")
    val wb_ext          = AddLocal("wb_ext")
    val rdy             = AddLocal("rdy")

    init {
        for (imm_idx in 0 until MultiExu_CFG.imms.size) {
            imm_rsrv.add(AddLocal(MultiExu_CFG.imms[imm_idx].name))
        }
        for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
            rs_rsrv.add(__RF_rs_req(
                AddLocal("rs" + RF_rs_idx + "_rdy"),
                AddLocal("rs" + RF_rs_idx + "_tag"),
                AddLocal("rs" + RF_rs_idx + "_src"),
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
                MultiExu_CFG : Reordex_CFG,
                val fu_id_num: hw_imm,
                val iq_exu: Boolean) : uop_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, trx_struct, MultiExu_CFG) {

    val rd = cyclix_gen.ulocal(name_prefix + "_rd", 0, 0, "0")
    val wr = cyclix_gen.ulocal(name_prefix + "_wr", 0, 0, "0")
}

class rob_buffer(cyclix_gen : cyclix.Generic,
                 name_prefix : String,
                 TRX_BUF_SIZE : Int,
                 MultiExu_CFG : Reordex_CFG,
                 val cdb_num : Int) : hw_stage(cyclix_gen, name_prefix, TRX_BUF_SIZE, STAGE_FC_MODE.BUFFERED, false) {

    val rd = cyclix_gen.ulocal(name_prefix + "_rd", 0, 0, "0")
    val wr = cyclix_gen.ulocal(name_prefix + "_wr", 0, 0, "0")

    var enb             = AddStageVar(hw_structvar("enb",               DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    var rd_tag_prev     = AddStageVar(hw_structvar("rd_tag_prev",       DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    var rd_tag_prev_clr = AddStageVar(hw_structvar("rd_tag_prev_clr",   DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    var cdb_id          = AddStageVar(hw_structvar("cdb_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(cdb_num)-1, 0, "0"))
    var trx_id          = AddStageVar(hw_structvar("trx_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(TRX_BUF_SIZE)-1, 0, "0"))
}

class __exu_descr(var var_dict : MutableMap<hw_var, hw_var>, var rs_use_flags : ArrayList<Boolean>, var IQ_insts : ArrayList<iq_buffer>, var base_CDB_index : Int)