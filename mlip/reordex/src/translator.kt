/*
 * translator.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

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