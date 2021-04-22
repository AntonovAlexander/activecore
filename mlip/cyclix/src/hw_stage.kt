/*
 * hw_stage.kt
 *
 *  Created on: 22.04.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*

class hw_stage(val cyclix_gen : cyclix.Generic,
               val name_prefix : String,
               val TRX_BUF_SIZE : Int) {

    val pctrl_active = cyclix_gen.ulocal((name_prefix + "genpctrl_active"), 0, 0, "0")

    var pContext_local_dict     = mutableMapOf<hw_var, hw_var>()    // local variables
    var pContext_srcglbls       = ArrayList<hw_var>()               // locals with required src bufs

    var TRX_BUF                 = DUMMY_VAR
    var TRX_BUF_COUNTER         = DUMMY_VAR
    var TRX_BUF_COUNTER_NEMPTY  = DUMMY_VAR
    var TRX_BUF_COUNTER_FULL    = DUMMY_VAR

    fun fetch_locals() {
        for (src_glbl in pContext_srcglbls) {
            cyclix_gen.assign(pContext_local_dict[src_glbl]!!, cyclix_gen.subStruct(TRX_BUF[0], src_glbl.name))
        }
    }
}