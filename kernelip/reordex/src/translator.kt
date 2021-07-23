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

open class trx_buffer(cyclix_gen : cyclix.Generic,
                      name_prefix : String,
                      TRX_BUF_SIZE : Int,
                      MultiExu_CFG : Reordex_CFG) : hw_stage(cyclix_gen, name_prefix, TRX_BUF_SIZE, STAGE_FC_MODE.BUFFERED, false) {

    var enb     = AddStageVar(hw_structvar("enb", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    var push    = cyclix_gen.ulocal(name_prefix + "_push", 0, 0, "0")
    var pop     = cyclix_gen.ulocal(name_prefix + "_pop", 0, 0, "0")

}

data class __RF_rs_req(val rs_rdy : hw_var,
                       val rs_tag : hw_var,
                       val rs_src : hw_var,
                       val rs_rdata : hw_var)

open class uop_buffer(cyclix_gen : cyclix.Generic,
                      name_prefix : String,
                      TRX_BUF_SIZE : Int,
                      MultiExu_CFG : Reordex_CFG,
                      val cdb_num : Int) : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG) {

    var fu_req      = AddStageVar(hw_structvar("fu_req", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    val imm_rsrv    = ArrayList<hw_var>()
    val rs_rsrv     = ArrayList<__RF_rs_req>()
    val rdy         = AddStageVar(hw_structvar("rdy", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    init {
        for (imm_idx in 0 until MultiExu_CFG.imms.size) {
            imm_rsrv.add(AddStageVar(hw_structvar(MultiExu_CFG.imms[imm_idx].name, MultiExu_CFG.imms[imm_idx].vartype, MultiExu_CFG.imms[imm_idx].defimm)))
        }
        for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
            rs_rsrv.add(__RF_rs_req(
                AddStageVar(hw_structvar("rs" + RF_rs_idx + "_rdy",     DATA_TYPE.BV_UNSIGNED,     0, 0, "0")),
                AddStageVar(hw_structvar("rs" + RF_rs_idx + "_tag",     DATA_TYPE.BV_UNSIGNED,     MultiExu_CFG.PRF_addr_width-1, 0, "0")),
                AddStageVar(hw_structvar("rs" + RF_rs_idx + "_src",     DATA_TYPE.BV_UNSIGNED,     GetWidthToContain(cdb_num)-1, 0, "0")),
                AddStageVar(hw_structvar("rs" + RF_rs_idx + "_rdata",   DATA_TYPE.BV_UNSIGNED,     MultiExu_CFG.RF_width-1, 0, "0"))
            ))
        }
    }
}

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

    var curinstr_addr   = DUMMY_VAR
    var nextinstr_addr  = DUMMY_VAR

    var mem_req         = DUMMY_VAR
    var mem_cmd         = DUMMY_VAR
    var mem_addr        = DUMMY_VAR
    var mem_be          = DUMMY_VAR

    init {
        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
            curinstr_addr   = AdduStageVar("curinstr_addr", 31, 0, "0")
            nextinstr_addr  = AdduStageVar("nextinstr_addr", 31, 0, "0")

            mem_req         = AdduStageVar("mem_req", 0, 0, "0")
            mem_cmd         = AdduStageVar("mem_cmd", 0, 0, "0")
            mem_addr        = AdduStageVar("mem_addr", 31, 0, "0")
            mem_be          = AdduStageVar("mem_be", 3, 0, "0")
        }
    }
}

class iq_buffer(cyclix_gen : cyclix.Generic,
                name_prefix : String,
                TRX_BUF_SIZE : Int,
                MultiExu_CFG : Reordex_CFG,
                val fu_id_num: hw_imm,
                val iq_exu: Boolean,
                var CDB_index : Int,
                cdb_num : Int) : uop_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, cdb_num) {

    var trx_id      = AddStageVar(hw_structvar("trx_id",     DATA_TYPE.BV_UNSIGNED, GetWidthToContain(MultiExu_CFG.trx_inflight_num)-1, 0, "0"))
    var fu_pending  = AddStageVar(hw_structvar("fu_pending", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    val rs_srcs     = ArrayList<hw_var>()
    val rd_tag      = AddStageVar(hw_structvar("rd_tag",    DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    val wb_ext      = AddStageVar(hw_structvar("wb_ext",    DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
}

class rob_buffer(cyclix_gen : cyclix.Generic,
                 name_prefix : String,
                 TRX_BUF_SIZE : Int,
                 MultiExu_CFG : Reordex_CFG,
                 val cdb_num : Int) : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG) {

    var trx_id          = AddStageVar(hw_structvar("trx_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(MultiExu_CFG.trx_inflight_num)-1, 0, "0"))
    var rd_tag_prev     = AddStageVar(hw_structvar("rd_tag_prev",       DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    var rd_tag_prev_clr = AddStageVar(hw_structvar("rd_tag_prev_clr",   DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    var cdb_id          = AddStageVar(hw_structvar("cdb_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(cdb_num)-1, 0, "0"))
    val rdy             = AddStageVar(hw_structvar("rdy",               DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    var TRX_ID_COUNTER  = cyclix_gen.uglobal(name_prefix + "_TRX_ID_COUNTER", GetWidthToContain(TRX_BUF_SIZE)-1, 0, "0")

    var curinstr_addr   = DUMMY_VAR
    var nextinstr_addr  = DUMMY_VAR

    var mem_req         = DUMMY_VAR
    var mem_cmd         = DUMMY_VAR
    var mem_addr        = DUMMY_VAR
    var mem_be          = DUMMY_VAR

    init {
        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
            curinstr_addr   = AdduStageVar("curinstr_addr", 31, 0, "0")
            nextinstr_addr  = AdduStageVar("nextinstr_addr", 31, 0, "0")

            mem_req         = AdduStageVar("mem_req", 0, 0, "0")
            mem_cmd         = AdduStageVar("mem_cmd", 0, 0, "0")
            mem_addr        = AdduStageVar("mem_addr", 31, 0, "0")
            mem_be          = AdduStageVar("mem_be", 3, 0, "0")
        }
    }
}

class __exu_descr(var var_dict : MutableMap<hw_var, hw_var>, var rs_use_flags : ArrayList<Boolean>, var IQ_insts : ArrayList<iq_buffer>)