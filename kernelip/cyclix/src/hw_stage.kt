/*
 * hw_stage.kt
 *
 *  Created on: 22.04.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*

open class hw_fifo(val cyclix_gen : cyclix.Generic,
                   val name_prefix : String,
                   val TRX_BUF_SIZE : Int,
                   val TRX_BUF_MULTIDIM : Int) {

    constructor(cyclix_gen : cyclix.Generic,
                name_prefix : String,
                TRX_BUF_SIZE : Int) : this(cyclix_gen, name_prefix, TRX_BUF_SIZE, 0)

    var TRX_BUF_dim             = hw_dim_static()
    var TRX_BUF                 = cyclix_gen.global(name_prefix + "_TRX_BUF", hw_struct(name_prefix + "_TRX_BUF_STRUCT"), TRX_BUF_dim)
    var TRX_BUF_head_ref        = TRX_BUF.GetFracRef(0)
    var TRX_BUF_COUNTER         = cyclix_gen.uglobal(name_prefix + "_TRX_BUF_COUNTER", GetWidthToContain(TRX_BUF_SIZE)-1, 0, "0")
    var TRX_BUF_COUNTER_NEMPTY  = cyclix_gen.uglobal(name_prefix + "_TRX_BUF_COUNTER_NEMPTY", 0, 0, "0")
    var TRX_BUF_COUNTER_FULL    = cyclix_gen.uglobal(name_prefix + "_TRX_BUF_COUNTER_FULL", 0, 0, "0")
    var TRX_LOCAL_PARALLEL      = DUMMY_VAR
    var TRX_LOCAL               = cyclix_gen.local(name_prefix + "_TRX_LOCAL", hw_struct(name_prefix + "_TRX_LOCAL_STRUCT")) as hw_var
    var TRX_PUSHED_dim          = hw_dim_static()
    var TRX_PUSHED              = cyclix_gen.local(name_prefix + "_TRX_PUSHED", TRX_BUF.vartype.src_struct, TRX_PUSHED_dim)

    init {
        if (TRX_BUF_MULTIDIM != 0) TRX_BUF_dim.add(TRX_BUF_MULTIDIM-1, 0)
        TRX_BUF_dim.add(TRX_BUF_SIZE-1, 0)
        if (TRX_BUF_MULTIDIM != 0) {
            var TRX_LOCAL_PARALLEL_dim = hw_dim_static()
            TRX_LOCAL_PARALLEL_dim.add(TRX_BUF_MULTIDIM-1, 0)
            TRX_LOCAL_PARALLEL = cyclix_gen.local(name_prefix + "_TRX_LOCAL_PARALLEL", TRX_LOCAL.vartype.src_struct, TRX_LOCAL_PARALLEL_dim)
        }
        if (TRX_BUF_MULTIDIM != 0) TRX_PUSHED_dim.add(TRX_BUF.vartype.dimensions[0])
    }

    fun AddBuf(new_structvar : hw_structvar) {
        TRX_BUF.vartype.src_struct.add(new_structvar)
    }

    fun AddLocal(new_structvar : hw_structvar) : hw_var {
        TRX_LOCAL.vartype.src_struct.add(new_structvar)
        return TRX_LOCAL.GetFracRef(new_structvar.name)
    }

    //// Local vars ////
    fun AddLocal(name : String, vartype : hw_type, defimm : hw_imm) : hw_var {
        return AddLocal(hw_structvar(name, vartype, defimm))
    }

    fun AddLocal(name : String, vartype : hw_type, defval : String) : hw_var {
        return AddLocal(hw_structvar(name, vartype, defval))
    }

    fun AddLocal(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static) : hw_var {
        return AddLocal(hw_structvar(name, src_struct_in, dimensions))
    }

    fun AddLocal(name : String, src_struct_in : hw_struct) : hw_var {
        return AddLocal(hw_structvar(name, src_struct_in))
    }

    fun AdduLocal(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, dimensions, defimm))
    }

    fun AdduLocal(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, dimensions, defval))
    }

    fun AdduLocal(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defimm))
    }

    fun AdduLocal(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defval))
    }

    fun AdduLocal(name : String, defimm : hw_imm) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, defimm))
    }

    fun AdduLocal(name : String, defval : String) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, defval))
    }

    fun AddsLocal(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_SIGNED, dimensions, defimm))
    }

    fun AddsLocal(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_SIGNED, dimensions, defval))
    }

    fun AddsLocal(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_SIGNED, msb, lsb, defimm))
    }

    fun AddsLocal(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_SIGNED, msb, lsb, defval))
    }

    fun AddsLocal(name : String, defimm : hw_imm) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_SIGNED, defimm))
    }

    fun AddsLocal(name : String, defval : String) : hw_var {
        return AddLocal(hw_structvar(name, DATA_TYPE.BV_SIGNED, defval))
    }
    ////////////////////

    //// Stage vars ////
    fun AddStageVar(new_structvar : hw_structvar) : hw_var {
        AddBuf(new_structvar)
        return AddLocal(new_structvar)
    }

    fun AddStageVar(name : String, vartype : hw_type, defimm : hw_imm) : hw_var {
        return AddStageVar(hw_structvar(name, vartype, defimm))
    }

    fun AddStageVar(name : String, vartype : hw_type, defval : String) : hw_var {
        return AddStageVar(hw_structvar(name, vartype, defval))
    }

    fun AddStageVar(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static) : hw_var {
        return AddStageVar(hw_structvar(name, src_struct_in, dimensions))
    }

    fun AddStageVar(name : String, src_struct_in : hw_struct) : hw_var {
        return AddStageVar(hw_structvar(name, src_struct_in))
    }

    fun AdduStageVar(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, dimensions, defimm))
    }

    fun AdduStageVar(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, dimensions, defval))
    }

    fun AdduStageVar(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defimm))
    }

    fun AdduStageVar(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defval))
    }

    fun AdduStageVar(name : String, defimm : hw_imm) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, defimm))
    }

    fun AdduStageVar(name : String, defval : String) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_UNSIGNED, defval))
    }

    fun AddsStageVar(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_SIGNED, dimensions, defimm))
    }

    fun AddsStageVar(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_SIGNED, dimensions, defval))
    }

    fun AddsStageVar(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_SIGNED, msb, lsb, defimm))
    }

    fun AddsStageVar(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_SIGNED, msb, lsb, defval))
    }

    fun AddsStageVar(name : String, defimm : hw_imm) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_SIGNED, defimm))
    }

    fun AddsStageVar(name : String, defval : String) : hw_var {
        return AddStageVar(hw_structvar(name, DATA_TYPE.BV_SIGNED, defval))
    }
    ////////////////////

    open fun Reset() {
        cyclix_gen.assign(TRX_BUF_COUNTER, 0)
        cyclix_gen.assign(TRX_BUF_COUNTER_NEMPTY, 0)
        cyclix_gen.assign(TRX_BUF_COUNTER_FULL, 0)
    }

    fun GetPushTrx(name: String) : hw_var {
        var push_trx_dimensions = hw_dim_static()
        if (TRX_BUF_MULTIDIM != 0) push_trx_dimensions.add(TRX_BUF.vartype.dimensions[0])
        return cyclix_gen.local(name, TRX_BUF.vartype.src_struct, push_trx_dimensions)
    }

    fun GetPushTrx() : hw_var {
        return GetPushTrx(name_prefix + "_" + cyclix_gen.GetGenName("push_trx"))
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
        cyclix_gen.assign(TRX_PUSHED, pushed_var)
        cyclix_gen.assign(TRX_BUF.GetFracRef(fracs), pushed_var)
        inc_trx_counter()
    }

    open fun pop_trx() {
        for (BUF_INDEX in 0 until TRX_BUF_SIZE-1) {
            cyclix_gen.assign(TRX_BUF.GetFracRef(BUF_INDEX), TRX_BUF[BUF_INDEX+1])
        }
        cyclix_gen.assign(TRX_BUF.GetFracRef(hw_fracs(TRX_BUF_SIZE-1)), 0)
        dec_trx_counter()
    }

    fun remove_and_squash_trx(index_var : hw_param) {
        for (elem_id in 0 until TRX_BUF.GetWidth()) {
            cyclix_gen.begif(cyclix_gen.geq(hw_imm(elem_id), index_var))
            run {

                cyclix_gen.begif(cyclix_gen.eq2(hw_imm(elem_id), TRX_BUF.GetWidth()-1))
                run {
                    cyclix_gen.assign(TRX_BUF.GetFracRef(hw_fracs(TRX_BUF_SIZE-1)), 0)
                }; cyclix_gen.endif()

                cyclix_gen.begelse()
                run {
                    cyclix_gen.assign(TRX_BUF.GetFracRef(elem_id), TRX_BUF.GetFracRef(elem_id + 1))
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()
        }
        dec_trx_counter()
    }
}

enum class STAGE_FC_MODE {
    BUFFERED, FALL_THROUGH
}

open class hw_stage(cyclix_gen : cyclix.Generic,
                    name_prefix : String,
                    TRX_BUF_SIZE : Int,
                    TRX_MULTIDIM_SIZE : Int,
                    val fc_mode : STAGE_FC_MODE,
                    val AUTO_FIRED : Boolean) : hw_fifo(cyclix_gen, name_prefix, TRX_BUF_SIZE, TRX_MULTIDIM_SIZE) {

    constructor(cyclix_gen : cyclix.Generic,
                name_prefix : String,
                TRX_BUF_SIZE : Int,
                fc_mode : STAGE_FC_MODE,
                AUTO_FIRED : Boolean) : this(cyclix_gen, name_prefix, TRX_BUF_SIZE, 0, fc_mode, AUTO_FIRED)

    val ctrl_active        = cyclix_gen.ulocal((name_prefix + "_genctrl_active"), 0, 0, "0")
    val ctrl_working       = cyclix_gen.ulocal((name_prefix + "_genctrl_working"), 0, 0, "0")
    val ctrl_succ          = cyclix_gen.ulocal((name_prefix + "_genctrl_succ"), 0, 0, "0")
    val ctrl_occupied      = cyclix_gen.ulocal((name_prefix + "_genctrl_occupied"), 0, 0, "0")
    var ctrl_rdy           = cyclix_gen.ulocal((name_prefix + "_genctrl_rdy"), 0, 0, "0")

    open fun preinit_ctrls() {
        // Asserting ctrl defaults (deployed even if signal is not used)
        cyclix_gen.assign(ctrl_succ, 0)
        cyclix_gen.assign(ctrl_working, 0)

        if (AUTO_FIRED) {
            // new transaction
            cyclix_gen.assign(ctrl_active, 1)
        } else {
            cyclix_gen.assign(ctrl_active, TRX_BUF_COUNTER_NEMPTY)
        }

        // Generating "occupied" pctrl
        cyclix_gen.assign(ctrl_occupied, ctrl_active)

        if (fc_mode == STAGE_FC_MODE.BUFFERED) cyclix_gen.assign(ctrl_rdy, !TRX_BUF_COUNTER_FULL)
    }

    fun init_locals() {
        if (TRX_BUF_MULTIDIM == 0) {
            cyclix_gen.assign_subStructsWithDefaultsIfAbsent(TRX_LOCAL, TRX_BUF_head_ref)
        } else {
            for (PARALLEL_INDEX in 0 until TRX_BUF_MULTIDIM) {
                cyclix_gen.assign_subStructsWithDefaultsIfAbsent(TRX_LOCAL_PARALLEL.GetFracRef(PARALLEL_INDEX), TRX_BUF_head_ref.GetFracRef(PARALLEL_INDEX))
            }
        }
    }

    fun switch_to_local(INDEX_IN_TRX_PARALLEL : Int) {
        if (TRX_BUF_MULTIDIM != 0) {
            cyclix_gen.assign(TRX_LOCAL, TRX_LOCAL_PARALLEL.GetFracRef(INDEX_IN_TRX_PARALLEL))
            //TRX_LOCAL = TRX_LOCAL_PARALLEL.GetFracRef(INDEX_IN_TRX_PARALLEL)
        }
        else ERROR("Attempting to switch_to_local for scalar hw_stage")
    }

    fun switch_to_local(INDEX_IN_TRX_PARALLEL : hw_param) {
        if (TRX_BUF_MULTIDIM != 0) {
            cyclix_gen.assign(TRX_LOCAL, TRX_LOCAL_PARALLEL.GetFracRef(INDEX_IN_TRX_PARALLEL))
            //TRX_LOCAL = TRX_LOCAL_PARALLEL.GetFracRef(INDEX_IN_TRX_PARALLEL)
        }
        else ERROR("Attempting to switch_to_local for scalar hw_stage")
    }

    override fun pop_trx() {
        super.pop_trx()
    }

    fun finalize_ctrls() {
        if (fc_mode == STAGE_FC_MODE.FALL_THROUGH) cyclix_gen.assign(ctrl_rdy, !TRX_BUF_COUNTER_FULL)
    }

    fun kill_cmd_internal() {
        cyclix_gen.assign(ctrl_active, 0)
    }

    fun accum(tgt : hw_var, src : hw_param) {
        cyclix_gen.assign(tgt, src)
        cyclix_gen.begif(ctrl_active)
        run {
            cyclix_gen.assign(TRX_BUF_head_ref.GetFracRef(((tgt as hw_var_frac).depow_fractions[0] as hw_frac_SubStruct).substruct_name), src)  //TODO: cleanup
        }; cyclix_gen.endif()
    }
}

open class hw_stage_stallable(cyclix_gen    : cyclix.Generic,
                              name_prefix   : String,
                              TRX_BUF_SIZE  : Int,
                              fc_mode       : STAGE_FC_MODE,
                              AUTO_FIRED    : Boolean) : hw_stage(cyclix_gen, name_prefix, TRX_BUF_SIZE, fc_mode, AUTO_FIRED) {

    val ctrl_stalled_glbl  = cyclix_gen.uglobal((name_prefix + "_genctrl_stalled_glbl"), 0, 0, "0")
    val ctrl_new           = cyclix_gen.ulocal((name_prefix + "_genctrl_new"), 0, 0, "0")
    val ctrl_finish        = cyclix_gen.ulocal((name_prefix + "_genctrl_finish"), 0, 0, "0")

    override fun preinit_ctrls() {
        // Asserting pctrl defaults (deployed even if signal is not used)
        cyclix_gen.assign(ctrl_succ, 0)
        cyclix_gen.assign(ctrl_working, 0)

        cyclix_gen.begif(ctrl_stalled_glbl)
        run {
            cyclix_gen.assign(ctrl_new, 0)

            // reactivate stalled transaction
            cyclix_gen.assign(ctrl_stalled_glbl, 0)
            cyclix_gen.assign(ctrl_active, 1)
        }; cyclix_gen.endif()
        cyclix_gen.begelse()
        run {
            if (AUTO_FIRED) {
                // new transaction
                cyclix_gen.assign(ctrl_active, 1)
            } else {
                cyclix_gen.assign(ctrl_active, TRX_BUF_COUNTER_NEMPTY)
            }
            cyclix_gen.assign(ctrl_new, ctrl_active)
        }; cyclix_gen.endif()

        cyclix_gen.assign(ctrl_occupied, ctrl_active)
        cyclix_gen.assign(ctrl_finish, 0)

        if (fc_mode == STAGE_FC_MODE.BUFFERED) cyclix_gen.assign(ctrl_rdy, !TRX_BUF_COUNTER_FULL)
    }

    override fun pop_trx() {
        cyclix_gen.assign(ctrl_stalled_glbl, 0)
        super.pop_trx()
    }

    fun stall_cmd_internal() {
        cyclix_gen.begif(ctrl_active)
        run {
            cyclix_gen.assign(ctrl_stalled_glbl, 1)
        }; cyclix_gen.endif()
        cyclix_gen.assign(ctrl_active, 0)
    }

    fun postinit_ctrls() {
        cyclix_gen.begif(ctrl_stalled_glbl)
        run {
            cyclix_gen.assign(ctrl_finish, 0)
            cyclix_gen.assign(ctrl_succ, 0)
        }; cyclix_gen.endif()
        cyclix_gen.begelse()
        run {
            cyclix_gen.assign(ctrl_finish, ctrl_occupied)
            cyclix_gen.assign(ctrl_succ, ctrl_active)
        }; cyclix_gen.endif()
    }
}