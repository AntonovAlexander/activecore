/*
 * trx_buffer.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*
import cyclix.*

open class trx_buffer(cyclix_gen : cyclix.Generic,
                      name_prefix : String,
                      TRX_BUF_SIZE : Int,
                      TRX_DIM_SIZE : Int,
                      val MultiExu_CFG : Reordex_CFG
) : hw_stage(cyclix_gen, name_prefix, TRX_BUF_SIZE, TRX_DIM_SIZE, STAGE_FC_MODE.BUFFERED, false) {

    constructor(cyclix_gen : cyclix.Generic,
                name_prefix : String,
                TRX_BUF_SIZE : Int,
                MultiExu_CFG : Reordex_CFG
    ) : this(cyclix_gen, name_prefix, TRX_BUF_SIZE, 0, MultiExu_CFG)

    var enb     = AddStageVar(hw_structvar("enb", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    var push    = cyclix_gen.ulocal(name_prefix + "_push", 0, 0, "0")
    var pop     = cyclix_gen.ulocal(name_prefix + "_pop", 0, 0, "0")

}