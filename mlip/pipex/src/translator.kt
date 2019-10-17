/*
 * translator.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

import hwast.*

val COPIPE_TRX_ID_WIDTH = 4

data class __global_info(val cyclix_global : hw_var,
                         val cyclix_global_buf : hw_var)

data class __mcopipe_if_info(   val wr_done : hw_var,
                                val rd_done : hw_var,
                                val full_flag : hw_var,
                                val empty_flag : hw_var,
                                val wr_ptr : hw_var,
                                val rd_ptr : hw_var,
                                val wr_ptr_next : hw_var,
                                val rd_ptr_next : hw_var,
                                val req_fifo : cyclix.hw_fifo_out,
                                var resp_fifo : cyclix.hw_fifo_in )

data class __mcopipe_handle_info(val if_id : hw_var,
                                 val resp_done : hw_var,
                                 val rdata : hw_var,
                                 val rdreq_pending : hw_var,
                                 val tid : hw_var)

data class __scopipe_if_info(val req_fifo : cyclix.hw_fifo_in,
                             var resp_fifo : cyclix.hw_fifo_out)

data class __scopipe_handle_info(val if_id : hw_var,
                                 val we : hw_var)

data class __assign_succ_buf(val req : hw_var,
                             val buf : hw_var)

data class __pstage_info(val TranslateInfo : __TranslateInfo,
                                   val name_prefix : String,

                                   val pctrl_new : hw_var,
                                   val pctrl_working : hw_var,
                                   val pctrl_succ : hw_var,
                                   val pctrl_occupied : hw_var,
                                   val pctrl_finish : hw_var,
                                   val pctrl_flushreq : hw_var,
                                   val pctrl_nevictable : hw_var,
                                   val pctrl_rdy : hw_var,

                                   val pctrl_active_glbl : hw_var,
                                   val pctrl_stalled_glbl : hw_var,
                                   val pctrl_killed_glbl : hw_var) {

    var pContext_local_dict     = mutableMapOf<hw_var, hw_var>()    // local variables
    var pContext_srcglbl_dict   = mutableMapOf<hw_var, hw_var>()    // src global bufs for not-new local (non-sticky) variables

    var global_tgts         = ArrayList<hw_global>()
    var assign_succ_assocs  = mutableMapOf<hw_pipex_var, __assign_succ_buf>()

    var mcopipe_handle_reqs  = ArrayList<hw_mcopipe_handle>()
    var mcopipe_handle_resps = ArrayList<hw_mcopipe_handle>()
    var mcopipe_handles      = ArrayList<hw_mcopipe_handle>()
    var mcopipe_handles_last = ArrayList<hw_mcopipe_handle>()

    var scopipe_handle_reqs  = ArrayList<hw_scopipe_handle>()
    var scopipe_handle_resps = ArrayList<hw_scopipe_handle>()
    var scopipe_handles      = ArrayList<hw_scopipe_handle>()

    fun TranslateVar(src : hw_var) : hw_var {
        if (pContext_local_dict.contains(src)) return pContext_local_dict[src] as hw_var
        else if (TranslateInfo.__global_assocs.containsKey(src)) return TranslateInfo.__global_assocs[src]!!.cyclix_global
        else {
            ERROR("Translation of variable " + src.name + " failed!")
            return src
        }
    }

    fun TranslateParam(src : hw_param) : hw_param {
        if (src is hw_imm) return src
        else return TranslateVar(src as hw_var)
    }

    fun TranslateParams(srcs : ArrayList<hw_param>) : ArrayList<hw_param> {
        var params = ArrayList<hw_param>()
        for (src in srcs) params.add(TranslateParam(src))
        return params
    }

    fun pkill_cmd_internal(cyclix_gen : cyclix.module) {
        cyclix_gen.begif(pctrl_active_glbl)
        run {
            cyclix_gen.assign(pctrl_active_glbl, 0)
            cyclix_gen.assign(pctrl_killed_glbl, 1)
        }; cyclix_gen.endif()
    }

    fun pstall_ifactive_cmd(cyclix_gen : cyclix.module) {
        cyclix_gen.bor_gen(pctrl_stalled_glbl, pctrl_stalled_glbl, pctrl_active_glbl)
        cyclix_gen.assign(pctrl_active_glbl, 0)
    }

    fun pstall_ifoccupied_cmd(cyclix_gen : cyclix.module) {
        cyclix_gen.bor_gen(pctrl_occupied, pctrl_active_glbl, pctrl_killed_glbl)
        cyclix_gen.begif(pctrl_occupied)
        run {
            cyclix_gen.bor_gen(pctrl_stalled_glbl, pctrl_stalled_glbl, 1)
        }; cyclix_gen.endif()
    }

    fun pflush_cmd_internal(cyclix_gen : cyclix.module) {
        cyclix_gen.bor_gen(pctrl_flushreq, pctrl_flushreq, pctrl_active_glbl)
    }
}

class __TranslateInfo() {
    var __global_assocs = mutableMapOf<hw_var, __global_info>()

    var __fifo_wr_assocs = mutableMapOf<hw_fifo_out, cyclix.hw_fifo_out>()
    var __fifo_rd_assocs = mutableMapOf<hw_fifo_in, cyclix.hw_fifo_in>()

    var __mcopipe_if_assocs = mutableMapOf<hw_mcopipe_if, __mcopipe_if_info>()
    var __mcopipe_handle_assocs = mutableMapOf<hw_mcopipe_handle, __mcopipe_handle_info>()
    var __mcopipe_handle_reqdict = mutableMapOf<hw_mcopipe_handle, ArrayList<hw_mcopipe_if>>()

    var __scopipe_if_assocs = mutableMapOf<hw_scopipe_if, __scopipe_if_info>()
    var __scopipe_handle_assocs = mutableMapOf<hw_scopipe_handle, __scopipe_handle_info>()
    var __scopipe_handle_reqdict = mutableMapOf<hw_scopipe_handle, ArrayList<hw_scopipe_if>>()

    var __stage_assocs = mutableMapOf<hw_stage, __pstage_info>()
}