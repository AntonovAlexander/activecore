/*
 * hw_stage.kt
 *
 *  Created on: 22.04.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*

open class hw_stage(val cyclix_gen : cyclix.Generic,
                    val name_prefix : String,
                    val TRX_BUF_SIZE : Int,
                    val AUTO_FIRED : Boolean) {

    val pctrl_active        = cyclix_gen.ulocal((name_prefix + "genpctrl_active"), 0, 0, "0")
    val pctrl_stalled_glbl  = cyclix_gen.uglobal((name_prefix + "genpctrl_stalled_glbl"), 0, 0, "0")
    val pctrl_new           = cyclix_gen.ulocal((name_prefix + "genpctrl_new"), 0, 0, "0")
    val pctrl_working       = cyclix_gen.ulocal((name_prefix + "genpctrl_working"), 0, 0, "0")
    val pctrl_succ          = cyclix_gen.ulocal((name_prefix + "genpctrl_succ"), 0, 0, "0")
    val pctrl_occupied      = cyclix_gen.ulocal((name_prefix + "genpctrl_occupied"), 0, 0, "0")
    var pctrl_rdy           = cyclix_gen.ulocal((name_prefix + "genpctrl_rdy"), 0, 0, "0")

    var pContext_local_dict     = mutableMapOf<hw_var, hw_var>()    // local variables
    var pContext_srcglbls       = ArrayList<hw_var>()               // locals with required src bufs

    var TRX_BUF                 = DUMMY_VAR
    var TRX_BUF_COUNTER         = DUMMY_VAR
    var TRX_BUF_COUNTER_NEMPTY  = cyclix_gen.uglobal(name_prefix + "TRX_BUF_COUNTER_NEMPTY", 0, 0, "0")
    var TRX_BUF_COUNTER_FULL    = cyclix_gen.uglobal(name_prefix + "TRX_BUF_COUNTER_FULL", 0, 0, "0")

    fun INIT_TRX_BUF(stage_buf_struct : hw_struct, reset_pref : Boolean) {
        TRX_BUF                 = cyclix_gen.global(name_prefix + "TRX_BUF", stage_buf_struct, TRX_BUF_SIZE-1, 0)
        TRX_BUF.reset_pref      = reset_pref
        TRX_BUF_COUNTER         = cyclix_gen.uglobal(name_prefix + "TRX_BUF_COUNTER", GetWidthToContain(TRX_BUF_SIZE)-1, 0, "0")
    }

    fun init_pctrls() {
        // Asserting pctrl defaults (deployed even if signal is not used)
        cyclix_gen.assign(pctrl_succ, 0)
        cyclix_gen.assign(pctrl_working, 0)

        cyclix_gen.begif(pctrl_stalled_glbl)
        run {
            cyclix_gen.assign(pctrl_new, 0)

            // reactivate stalled transaction
            cyclix_gen.assign(pctrl_stalled_glbl, 0)
            cyclix_gen.assign(pctrl_active, 1)
        }; cyclix_gen.endif()
        cyclix_gen.begelse()
        run {
            if (AUTO_FIRED) {
                // new transaction
                cyclix_gen.assign(pctrl_active, 1)
            } else {
                cyclix_gen.assign(pctrl_active, TRX_BUF_COUNTER_NEMPTY)
            }
            cyclix_gen.assign(pctrl_new, pctrl_active)
        }; cyclix_gen.endif()

        // Generating "occupied" pctrl
        cyclix_gen.assign(pctrl_occupied, pctrl_active)
    }

    fun init_locals() {
        for (src_glbl in pContext_srcglbls) {
            cyclix_gen.assign(pContext_local_dict[src_glbl]!!, cyclix_gen.subStruct(TRX_BUF[0], src_glbl.name))
        }
    }

    fun set_rdy() {
        cyclix_gen.assign(pctrl_rdy, !TRX_BUF_COUNTER_FULL)
    }

    fun push_trx_data(tgt_buf_fracs : hw_fracs, pushed_var : hw_param) {
        var fracs = hw_fracs(0)
        if (TRX_BUF_SIZE != 1) {
            fracs = hw_fracs(TRX_BUF_COUNTER)
        }
        for (tgt_buf_frac in tgt_buf_fracs) {
            fracs.add(tgt_buf_frac)
        }
        cyclix_gen.assign(TRX_BUF, fracs, pushed_var)
    }

    fun pop_trx() {
        cyclix_gen.assign(pctrl_active, 0)
        cyclix_gen.assign(pctrl_stalled_glbl, 0)
        cyclix_gen.assign(TRX_BUF, hw_fracs(0), 0)
        for (BUF_INDEX in 0 until TRX_BUF_SIZE-1) {
            cyclix_gen.assign(TRX_BUF, hw_fracs(BUF_INDEX), TRX_BUF[BUF_INDEX+1])
        }

        dec_trx_counter()
    }

    fun inc_trx_counter() {
        cyclix_gen.assign(TRX_BUF_COUNTER_NEMPTY, 1)
        if (TRX_BUF_SIZE == 1) {
            cyclix_gen.assign(TRX_BUF_COUNTER_FULL, 1)
        } else {
            cyclix_gen.begif(cyclix_gen.eq2(TRX_BUF_COUNTER, TRX_BUF_SIZE-1))
            run {
                cyclix_gen.assign(TRX_BUF_COUNTER_FULL, 1)
            }; cyclix_gen.endif()
        }
        cyclix_gen.add_gen(TRX_BUF_COUNTER, TRX_BUF_COUNTER, 1)
    }

    fun dec_trx_counter() {
        cyclix_gen.assign(TRX_BUF_COUNTER_FULL, 0)
        if (TRX_BUF_SIZE == 1) {
            cyclix_gen.assign(TRX_BUF_COUNTER_NEMPTY, 0)
        } else {
            cyclix_gen.begif(cyclix_gen.eq2(TRX_BUF_COUNTER, 1))
            run {
                cyclix_gen.assign(TRX_BUF_COUNTER_NEMPTY, 0)
            }; cyclix_gen.endif()
        }
        cyclix_gen.sub_gen(TRX_BUF_COUNTER, TRX_BUF_COUNTER, 1)
    }
}