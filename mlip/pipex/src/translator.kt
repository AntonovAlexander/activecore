/*
 * translator.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

import hwast.*
import cyclix.*

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
                                val req_fifo : hw_fifo_out,
                                var resp_fifo : hw_fifo_in )

data class __mcopipe_handle_info(val struct_descr : hw_struct)

data class __scopipe_if_info(val req_fifo : hw_fifo_in,
                             var resp_fifo : hw_fifo_out)

data class __scopipe_handle_info(val struct_descr : hw_struct)

data class __assign_buf(val req : hw_var,
                        val buf : hw_var)

data class __pstage_info(val TranslateInfo : __TranslateInfo,
                         val name_prefix : String,
                         val TRX_BUF_SIZE : Int,

                         val stage_ref : hw_stage,
                         val pctrl_new : hw_var,
                         val pctrl_working : hw_var,
                         val pctrl_succ : hw_var,
                         val pctrl_occupied : hw_var,
                         val pctrl_finish : hw_var,
                         val pctrl_flushreq : hw_var,
                         val pctrl_rdy : hw_var,

                         val pctrl_stalled_glbl : hw_var) {

    var pContext_local_dict     = mutableMapOf<hw_var, hw_var>()    // local variables
    var pContext_srcglbls       = ArrayList<hw_var>()               // locals with required src global bufs
    var accum_tgts              = ArrayList<hw_var>()               // targets for accumulation
    var newaccums               = ArrayList<hw_var>()               // new targets for accumulation (without driver on previous stage)

    var var_dict            = mutableMapOf<hw_var, hw_var>()

    var assign_succ_assocs  = mutableMapOf<hw_pipex_var, __assign_buf>()

    var mcopipe_handle_reqs  = ArrayList<hw_mcopipe_handle>()
    var mcopipe_handle_resps = ArrayList<hw_mcopipe_handle>()
    var mcopipe_handles      = ArrayList<hw_mcopipe_handle>()
    var mcopipe_handles_last = ArrayList<hw_mcopipe_handle>()

    var scopipe_handle_reqs  = ArrayList<hw_scopipe_handle>()
    var scopipe_handle_resps = ArrayList<hw_scopipe_handle>()
    var scopipe_handles      = ArrayList<hw_scopipe_handle>()

    var TRX_BUF                 = DUMMY_VAR
    var TRX_BUF_COUNTER         = DUMMY_VAR
    var TRX_BUF_COUNTER_NEMPTY  = DUMMY_VAR
    var TRX_BUF_COUNTER_FULL    = DUMMY_VAR

    fun TranslateVar(src : hw_var) : hw_var {
        return TranslateVar(src, var_dict)
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

    fun pkill_cmd_internal(cyclix_gen : cyclix.Generic) {
        cyclix_gen.begif(stage_ref.pctrl_active)
        run {
            cyclix_gen.assign(stage_ref.pctrl_active, 0)
        }; cyclix_gen.endif()
    }

    fun pstall_ifactive_cmd(cyclix_gen : cyclix.Generic) {
        cyclix_gen.bor_gen(pctrl_stalled_glbl, pctrl_stalled_glbl, stage_ref.pctrl_active)
        cyclix_gen.assign(stage_ref.pctrl_active, 0)
    }

    fun pflush_cmd_internal(cyclix_gen : cyclix.Generic) {
        cyclix_gen.bor_gen(pctrl_flushreq, pctrl_flushreq, stage_ref.pctrl_active)
    }
}

class __TranslateInfo(var pipeline : Pipeline) {
    var __global_assocs = mutableMapOf<hw_var, __global_info>()

    var __fifo_wr_assocs = mutableMapOf<hw_fifo_out, hw_fifo_out>()
    var __fifo_rd_assocs = mutableMapOf<hw_fifo_in, hw_fifo_in>()

    var __mcopipe_if_assocs = mutableMapOf<hw_mcopipe_if, __mcopipe_if_info>()
    var __mcopipe_handle_assocs = mutableMapOf<hw_mcopipe_handle, __mcopipe_handle_info>()
    var __mcopipe_handle_reqdict = mutableMapOf<hw_mcopipe_handle, ArrayList<hw_mcopipe_if>>()

    var __scopipe_if_assocs = mutableMapOf<hw_scopipe_if, __scopipe_if_info>()
    var __scopipe_handle_assocs = mutableMapOf<hw_scopipe_handle, __scopipe_handle_info>()
    var __scopipe_handle_reqdict = mutableMapOf<hw_scopipe_handle, ArrayList<hw_scopipe_if>>()

    var __stage_assocs = mutableMapOf<hw_pipex_stage, __pstage_info>()

    var StageList = ArrayList<hw_pipex_stage>()
    var StageInfoList = ArrayList<__pstage_info>()

    var gencredit_counter = DUMMY_VAR
}