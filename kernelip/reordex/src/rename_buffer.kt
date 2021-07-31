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