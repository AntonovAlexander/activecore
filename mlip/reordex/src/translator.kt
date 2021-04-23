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

class renamed_uop_stage(cyclix_gen : cyclix.Generic,
                        TRX_BUF_SIZE : Int,
                        ExecUnits_size : Int,
                        MultiExu_cfg_rf : MultiExu_CFG_RF,
                        Exu_cfg_rf : Exu_CFG_RF) : hw_stage(cyclix_gen, "genrenamed_uop_buf", TRX_BUF_SIZE, false) {

    val fu_req          = AddStageVar(cyclix_gen.ulocal("fu_req",     0, 0, "0"))
    val fu_pending      = AddStageVar(cyclix_gen.ulocal("fu_pending",     0, 0, "0"))
    val fu_id           = AddStageVar(cyclix_gen.ulocal("fu_id",     GetWidthToContain(ExecUnits_size), 0, "0"))
    val fu_opcode       = AddStageVar(cyclix_gen.ulocal("fu_opcode",     0, 0, "0"))
    val rs_rsrv         = ArrayList<__RF_rs_req>()
    val rd_tag          = AddStageVar(cyclix_gen.ulocal("rd_tag", MultiExu_cfg_rf.PRF_addr_width-1, 0, "0"))
    val rd_tag_prev     = AddStageVar(cyclix_gen.ulocal("rd_tag_prev_clr", MultiExu_cfg_rf.PRF_addr_width-1, 0, "0"))
    val rd_tag_prev_clr = AddStageVar(cyclix_gen.ulocal("rd_tag_prev_clr",0, 0, "0"))
    val wb_ext          = AddStageVar(cyclix_gen.ulocal("wb_ext",     0, 0, "0"))

    init {
        for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
            rs_rsrv.add(__RF_rs_req(
                AddStageVar(cyclix_gen.ulocal("rs" + RF_rs_idx + "_rdy",     0, 0, "0")),
                AddStageVar(cyclix_gen.ulocal("rs" + RF_rs_idx + "_tag",     MultiExu_cfg_rf.PRF_addr_width-1, 0, "0")),
                AddStageVar(cyclix_gen.ulocal("rs" + RF_rs_idx + "_rdata",     Exu_cfg_rf.RF_width-1, 0, "0"))
            ))
        }
    }
}