/*
 * uop_buffer.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

open class uop_buffer(cyclix_gen : cyclix.Generic,
                      name_prefix : String,
                      TRX_BUF_SIZE : Int,
                      TRX_DIM_SIZE : Int,
                      MultiExu_CFG : Reordex_CFG,
                      val cdb_num : Int) : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, TRX_DIM_SIZE, MultiExu_CFG) {

    constructor(cyclix_gen : cyclix.Generic,
                name_prefix : String,
                TRX_BUF_SIZE : Int,
                MultiExu_CFG : Reordex_CFG,
                cdb_num : Int) : this(cyclix_gen, name_prefix, TRX_BUF_SIZE, 0, MultiExu_CFG, cdb_num)

    var fu_req      = AddStageVar(hw_structvar("fu_req", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    val imm_rsrv    = ArrayList<hw_var>()
    val src_rsrv    = ArrayList<__src_handle>()
    val rdy         = AddStageVar(hw_structvar("rdy", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    init {
        for (imm_idx in 0 until MultiExu_CFG.src_imms.size) {
            imm_rsrv.add(AddStageVar(hw_structvar(MultiExu_CFG.src_imms[imm_idx].name, MultiExu_CFG.src_imms[imm_idx].vartype, MultiExu_CFG.src_imms[imm_idx].defimm)))
        }
        for (RF_rs_idx in 0 until MultiExu_CFG.srcs.size) {
            src_rsrv.add(__src_handle(
                AdduStageVar("src" + RF_rs_idx + "_rdy", 0, 0, "1"),
                AdduStageVar("src" + RF_rs_idx + "_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0"),
                AdduStageVar("src" + RF_rs_idx + "_src", GetWidthToContain(cdb_num) -1, 0, "0"),
                AdduStageVar("src" + RF_rs_idx + "_data",MultiExu_CFG.RF_width-1, 0, "0")
            ))
        }
    }
}