/*
 * rob_buffer.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

open class rob_buffer(cyclix_gen : cyclix.Generic,
                      name_prefix : String,
                      TRX_BUF_SIZE : Int,
                      MultiExu_CFG : Reordex_CFG,
                      val cdb_num : Int) : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG) {

    var trx_id          = AddStageVar(hw_structvar("trx_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(MultiExu_CFG.trx_inflight_num) -1, 0, "0"))
    var rd_tag_prev     = AddStageVar(hw_structvar("rd_tag_prev",       DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    var rd_tag_prev_clr = AddStageVar(hw_structvar("rd_tag_prev_clr",   DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    var cdb_id          = AddStageVar(hw_structvar("cdb_id",            DATA_TYPE.BV_UNSIGNED, GetWidthToContain(cdb_num) -1, 0, "0"))
    val rdy             = AddStageVar(hw_structvar("rdy",               DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    var TRX_ID_COUNTER  = cyclix_gen.uglobal(name_prefix + "_TRX_ID_COUNTER", GetWidthToContain(TRX_BUF_SIZE) -1, 0, "0")
}

class rob_buffer_risc(cyclix_gen : cyclix.Generic,
                      name_prefix : String,
                      TRX_BUF_SIZE : Int,
                      MultiExu_CFG : Reordex_CFG,
                      cdb_num : Int) : rob_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, cdb_num) {

    var curinstr_addr   = AdduStageVar("curinstr_addr", 31, 0, "0")
    var nextinstr_addr  = AdduStageVar("nextinstr_addr", 31, 0, "0")

    var rd_data         = AdduStageVar("rd_data", MultiExu_CFG.RF_width-1, 0, "0")

    var mem_req         = AdduStageVar("mem_req", 0, 0, "0")
    var mem_cmd         = AdduStageVar("mem_cmd", 0, 0, "0")
    var mem_addr        = AdduStageVar("mem_addr", 31, 0, "0")
    var mem_be          = AdduStageVar("mem_be", 3, 0, "0")
}