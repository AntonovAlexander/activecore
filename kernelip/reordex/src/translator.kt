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
        for (imm_idx in 0 until MultiExu_CFG.src_imms.size) {
            imm_rsrv.add(AddStageVar(hw_structvar(MultiExu_CFG.src_imms[imm_idx].name, MultiExu_CFG.src_imms[imm_idx].vartype, MultiExu_CFG.src_imms[imm_idx].defimm)))
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

class __exu_descr(var var_dict : MutableMap<hw_var, hw_var>, var rs_use_flags : ArrayList<Boolean>, var IQ_insts : ArrayList<iq_buffer>)