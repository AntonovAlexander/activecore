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
    val pctrl_new           = cyclix_gen.ulocal((name_prefix + "genpctrl_new"), 0, 0, "0")
    val pctrl_working       = cyclix_gen.ulocal((name_prefix + "genpctrl_working"), 0, 0, "0")
    val pctrl_succ          = cyclix_gen.ulocal((name_prefix + "genpctrl_succ"), 0, 0, "0")
    val pctrl_occupied      = cyclix_gen.ulocal((name_prefix + "genpctrl_occupied"), 0, 0, "0")
    var pctrl_rdy           = cyclix_gen.ulocal((name_prefix + "genpctrl_rdy"), 0, 0, "0")

    var driven_locals       = mutableMapOf<hw_var, String>()

    var TRX_BUF                 = cyclix_gen.global(name_prefix + "TRX_BUF", hw_struct(name_prefix + "TRX_BUF_STRUCT"), TRX_BUF_SIZE-1, 0)
    var TRX_BUF_COUNTER         = cyclix_gen.uglobal(name_prefix + "TRX_BUF_COUNTER", GetWidthToContain(TRX_BUF_SIZE)-1, 0, "0")
    var TRX_BUF_COUNTER_NEMPTY  = cyclix_gen.uglobal(name_prefix + "TRX_BUF_COUNTER_NEMPTY", 0, 0, "0")
    var TRX_BUF_COUNTER_FULL    = cyclix_gen.uglobal(name_prefix + "TRX_BUF_COUNTER_FULL", 0, 0, "0")

    fun AddBuf(new_structvar : hw_structvar) {
        TRX_BUF.vartype.src_struct.add(new_structvar)
    }

    fun AddStageVar(new_var : hw_var) : hw_var {
        TRX_BUF.vartype.src_struct.add(hw_structvar(new_var.name, new_var.vartype, new_var.defimm))
        driven_locals.put(new_var, new_var.name)
        return new_var
    }

    open fun init_pctrls() {
        // Asserting pctrl defaults (deployed even if signal is not used)
        cyclix_gen.assign(pctrl_succ, 0)
        cyclix_gen.assign(pctrl_working, 0)

        if (AUTO_FIRED) {
            // new transaction
            cyclix_gen.assign(pctrl_active, 1)
        } else {
            cyclix_gen.assign(pctrl_active, TRX_BUF_COUNTER_NEMPTY)
        }
        cyclix_gen.assign(pctrl_new, pctrl_active)

        // Generating "occupied" pctrl
        cyclix_gen.assign(pctrl_occupied, pctrl_active)
    }

    fun init_locals() {
        var TRX_BUF_0 = TRX_BUF.GetFracRef(0)
        for (driven_local in driven_locals) {
            var TRX_BUF_REF = TRX_BUF_0.GetFracRef(hw_frac_SubStruct(driven_local.value))
            cyclix_gen.assign(driven_local.key, TRX_BUF_REF)
        }
    }

    fun set_rdy() {
        cyclix_gen.assign(pctrl_rdy, !TRX_BUF_COUNTER_FULL)
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

    fun push_trx(pushed_var : hw_param) {
        var fracs = hw_fracs(0)
        if (TRX_BUF_SIZE != 1) {
            fracs = hw_fracs(TRX_BUF_COUNTER)
        }
        cyclix_gen.assign(TRX_BUF, fracs, pushed_var)
        inc_trx_counter()
    }

    fun push_trx_frac(tgt_buf_fracs : hw_fracs, pushed_var : hw_param) {
        var fracs = hw_fracs(0)
        if (TRX_BUF_SIZE != 1) {
            fracs = hw_fracs(TRX_BUF_COUNTER)
        }
        for (tgt_buf_frac in tgt_buf_fracs) {
            fracs.add(tgt_buf_frac)
        }
        cyclix_gen.assign(TRX_BUF, fracs, pushed_var)
    }

    open fun pop_trx() {
        cyclix_gen.assign(pctrl_active, 0)
        cyclix_gen.assign(TRX_BUF, hw_fracs(0), 0)
        for (BUF_INDEX in 0 until TRX_BUF_SIZE-1) {
            cyclix_gen.assign(TRX_BUF, hw_fracs(BUF_INDEX), TRX_BUF[BUF_INDEX+1])
        }
        dec_trx_counter()
    }

    fun pkill_cmd_internal() {
        cyclix_gen.assign(pctrl_active, 0)
    }

    fun accum(tgt : hw_var, fracs : hw_fracs, src : hw_param) {
        cyclix_gen.assign(tgt, fracs, src)
        cyclix_gen.begif(pctrl_active)
        run {
            var trx_buf_fracs = hw_fracs(0)
            trx_buf_fracs.add(hw_frac_SubStruct(driven_locals[tgt]!!))
            for (frac in fracs) trx_buf_fracs.add(frac)
            cyclix_gen.assign(TRX_BUF, trx_buf_fracs, src)
        }; cyclix_gen.endif()
    }
}

open class hw_stage_stallable(cyclix_gen : cyclix.Generic,
                              name_prefix : String,
                              TRX_BUF_SIZE : Int,
                              AUTO_FIRED : Boolean) : hw_stage(cyclix_gen, name_prefix, TRX_BUF_SIZE, AUTO_FIRED) {

    val pctrl_stalled_glbl  = cyclix_gen.uglobal((name_prefix + "genpctrl_stalled_glbl"), 0, 0, "0")

    override fun init_pctrls() {
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

    override fun pop_trx() {
        cyclix_gen.assign(pctrl_active, 0)
        cyclix_gen.assign(pctrl_stalled_glbl, 0)
        cyclix_gen.assign(TRX_BUF, hw_fracs(0), 0)
        for (BUF_INDEX in 0 until TRX_BUF_SIZE-1) {
            cyclix_gen.assign(TRX_BUF, hw_fracs(BUF_INDEX), TRX_BUF[BUF_INDEX+1])
        }

        dec_trx_counter()
    }

    fun pstall_cmd_internal() {
        cyclix_gen.begif(pctrl_active)
        run {
            cyclix_gen.assign(pctrl_stalled_glbl, 1)
        }; cyclix_gen.endif()
        cyclix_gen.assign(pctrl_active, 0)
    }
}