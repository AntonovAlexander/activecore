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

internal class __exu_descr(var var_dict : MutableMap<hw_var, hw_var>, var rs_use_flags : ArrayList<Boolean>, var IQ_insts : ArrayList<iq_buffer>)