/*
 * Pipeline.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

import hwast.*
import cyclix.*

val OP_RD_REMOTE        = hw_opcode("rd_remote")
val OP_ISACTIVE         = hw_opcode("isactive")
val OP_ISWORKING        = hw_opcode("isworking")
val OP_ISSTALLED        = hw_opcode("isstalled")
val OP_ISSUCC           = hw_opcode("issucc")
val OP_ISFINISHED       = hw_opcode("isfinished")

val OP_PSTALL           = hw_opcode("pstall")
val OP_PKILL            = hw_opcode("pkill")
val OP_PFLUSH           = hw_opcode("pflush")

val OP_ACCUM            = hw_opcode("accum")
val OP_ASSIGN_SUCC      = hw_opcode("assign_succ")
val OP_ASSIGN_ALWAYS    = hw_opcode("assign_always")
val OP_RD_PREV          = hw_opcode("rd_prev")

enum class PIPELINE_FC_MODE {
    STALLABLE, CREDIT_BASED
}


open class hw_exec_stage_stat(var stage : hw_pipex_stage, opcode : hw_opcode) : hw_exec(opcode)

class hw_exec_read_remote(stage : hw_pipex_stage, var remote_var : hw_pipex_var) : hw_exec_stage_stat(stage, OP_RD_REMOTE)

internal open class pipex_import_expr_context(var_dict : MutableMap<hw_var, hw_var>,
                                     var curStage : hw_pipex_stage,
                                     var TranslateInfo: __TranslateInfo,
                                     var curStageInfo : __pstage_info) : import_expr_context(var_dict)

open class Pipeline(val name : String, val pipeline_fc_mode : PIPELINE_FC_MODE, val dpath_reset : Boolean) : hw_astc_stdif() {

    constructor(name : String, pipeline_fc_mode : PIPELINE_FC_MODE)
            : this(name, pipeline_fc_mode, true)

    override var GenNamePrefix   = "pipex"

    var locals          = ArrayList<hw_var>()
    var globals         = ArrayList<hw_global>()

    var copipe_ifs = mutableMapOf<String, hw_copipe>()
    var mcopipe_ifs = ArrayList<hw_mcopipe_if>()
    var scopipe_ifs = ArrayList<hw_scopipe_if>()

    var copipe_handles = mutableMapOf<String, hw_copipe>()
    var mcopipe_handles = ArrayList<hw_mcopipe_handle>()
    var scopipe_handles = ArrayList<hw_scopipe_handle>()

    var Stages  = mutableMapOf<String, hw_pipex_stage>()

    fun stage_handler(name : String, fc_mode : STAGE_FC_MODE, buf_size_cfg : PSTAGE_BUF_SIZE_CFG) : hw_pipex_stage {
        if (FROZEN_FLAG) ERROR("Failed to add stage " + name + ": ASTC frozen")
        var new_stage = hw_pipex_stage(name, fc_mode, buf_size_cfg, this)
        if (Stages.put(new_stage.name, new_stage) != null) {
            ERROR("Stage addition problem!")
        }
        return new_stage
    }

    fun stage_handler(name : String, fc_mode : STAGE_FC_MODE, BUF_SIZE : Int) : hw_pipex_stage {
        return stage_handler(name, fc_mode, PSTAGE_BUF_SIZE_CFG(BUF_SIZE))
    }

    fun stage_handler(name : String, fc_mode : STAGE_FC_MODE) : hw_pipex_stage {
        return stage_handler(name, fc_mode, PSTAGE_BUF_SIZE_CFG())
    }

    fun begstage(stage : hw_pipex_stage) {
        if (FROZEN_FLAG) ERROR("Failed to begin stage " + stage.name + ": ASTC frozen")
        if (this.size != 0) ERROR("Pipex ASTC inconsistent!")
        // TODO: validate stage presence
        add(stage)
    }

    fun endstage() {
        if (FROZEN_FLAG) ERROR("Failed to end stage: ASTC frozen")
        if (this.size != 1) ERROR("Stage ASTC inconsistent!")
        if (this[0].opcode != OP_STAGE) ERROR("Stage ASTC inconsistent!")
        this.clear()
    }

    private fun add_local(new_local: hw_local) {
        if (FROZEN_FLAG) ERROR("Failed to add local " + new_local.name + ": ASTC frozen")

        if (wrvars.containsKey(new_local.name)) ERROR("Naming conflict for local: " + new_local.name)
        if (rdvars.containsKey(new_local.name)) ERROR("Naming conflict for local: " + new_local.name)

        wrvars.put(new_local.name, new_local)
        rdvars.put(new_local.name, new_local)
        locals.add(new_local)
        new_local.default_astc = this
    }

    fun local(name: String, vartype : hw_type, defimm: hw_imm): hw_local {
        var ret_var = hw_local(name, vartype, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, vartype : hw_type, defval: String): hw_local {
        var ret_var = hw_local(name, vartype, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct: hw_struct, dimensions: hw_dim_static): hw_local {
        var ret_var = hw_local(name, hw_type(src_struct, dimensions), "0")
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct: hw_struct): hw_local {
        var ret_var = hw_local(name, hw_type(src_struct), "0")
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, dimensions: hw_dim_static, defimm: hw_imm): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defimm: hw_imm): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_UNSIGNED, defimm.imm_value), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_UNSIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defimm: hw_imm): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, defimm: hw_imm): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_SIGNED, defimm.imm_value), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(DATA_TYPE.BV_SIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    private fun add_global(new_global: hw_global) {
        if (FROZEN_FLAG) ERROR("Failed to add global " + new_global.name + ": ASTC frozen")

        if (wrvars.containsKey(new_global.name)) ERROR("Naming conflict for global: " + new_global.name)
        if (rdvars.containsKey(new_global.name)) ERROR("Naming conflict for global: " + new_global.name)

        wrvars.put(new_global.name, new_global)
        rdvars.put(new_global.name, new_global)
        globals.add(new_global)
        new_global.default_astc = this
    }

    fun global(name: String, vartype: hw_type, defimm: hw_imm): hw_global {
        var ret_var = hw_global(name, vartype, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, vartype: hw_type, defval: String): hw_global {
        var ret_var = hw_global(name, vartype, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct: hw_struct, dimensions: hw_dim_static): hw_global {
        var ret_var = hw_global(name, hw_type(src_struct, dimensions), "0")
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct: hw_struct): hw_global {
        var ret_var = hw_global(name, hw_type(src_struct), "0")
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, dimensions: hw_dim_static, defimm: hw_imm): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defimm: hw_imm): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_UNSIGNED, defimm.imm_value), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_UNSIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defimm: hw_imm): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defimm: hw_imm): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_SIGNED, defimm.imm_value), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(DATA_TYPE.BV_SIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    private fun add_mcopipe_if(new_mcopipe: hw_mcopipe_if) {
        if (FROZEN_FLAG) ERROR("Failed to add mcopipe_if " + new_mcopipe.name + ": ASTC frozen")
        if (copipe_ifs.put(new_mcopipe.name, new_mcopipe) != null) {
            ERROR("Mcopipe addition problem!")
        }
        mcopipe_ifs.add(new_mcopipe)
    }

    fun mcopipe_if(name: String,
                   wdata_vartype_in: hw_type,
                   rdata_vartype_in: hw_type,
                   trx_id_width_in: Int): hw_mcopipe_if {
        var new_mcopipe = hw_mcopipe_if(this,
            name,
            wdata_vartype_in,
            rdata_vartype_in,
            trx_id_width_in)
        add_mcopipe_if(new_mcopipe)
        return new_mcopipe
    }

    private fun add_mcopipe_handle(new_mcopipe: hw_mcopipe_handle) {
        if (FROZEN_FLAG) ERROR("Failed to add mcopipe_handle " + new_mcopipe.name + ": ASTC frozen")
        if (copipe_handles.put(new_mcopipe.name, new_mcopipe) != null) {
            ERROR("Mcopipe addition problem!")
        }
        mcopipe_handles.add(new_mcopipe)
    }

    fun mcopipe_handle(name: String,
                       wdata_vartype_in: hw_type,
                       rdata_vartype_in: hw_type,
                       trx_id_width_in: Int): hw_mcopipe_handle {
        var new_mcopipe = hw_mcopipe_handle(this,
            name,
            wdata_vartype_in,
            rdata_vartype_in,
            trx_id_width_in)
        add_mcopipe_handle(new_mcopipe)
        return new_mcopipe
    }

    fun mcopipe_handle(mcopipe: hw_mcopipe_if): hw_mcopipe_handle {
        var new_mcopipe = hw_mcopipe_handle(mcopipe)
        add_mcopipe_handle(new_mcopipe)
        return new_mcopipe
    }

    private fun add_scopipe_if(new_scopipe: hw_scopipe_if) {
        if (FROZEN_FLAG) ERROR("Failed to add scopipe_if " + new_scopipe.name + ": ASTC frozen")
        if (copipe_ifs.put(new_scopipe.name, new_scopipe) != null) {
            ERROR("Scopipe addition problem!")
        }
        scopipe_ifs.add(new_scopipe)
    }

    fun scopipe_if(name: String,
                   wdata_vartype_in: hw_type,
                   rdata_vartype_in: hw_type): hw_scopipe_if {
        var new_scopipe = hw_scopipe_if(this,
            name,
            wdata_vartype_in,
            rdata_vartype_in)
        add_scopipe_if(new_scopipe)
        return new_scopipe
    }

    private fun add_scopipe_handle(new_scopipe: hw_scopipe_handle) {
        if (FROZEN_FLAG) ERROR("Failed to add scopipe_handle " + new_scopipe.name + ": ASTC frozen")
        if (copipe_handles.put(new_scopipe.name, new_scopipe) != null) {
            ERROR("Scopipe addition problem!")
        }
        scopipe_handles.add(new_scopipe)
    }

    fun scopipe_handle(name: String,
                       wdata_vartype_in: hw_type,
                       rdata_vartype_in: hw_type): hw_scopipe_handle {
        var new_scopipe = hw_scopipe_handle(this,
            name,
            wdata_vartype_in,
            rdata_vartype_in)
        add_scopipe_handle(new_scopipe)
        return new_scopipe
    }

    fun scopipe_handle(scopipe: hw_scopipe_if): hw_scopipe_handle {
        var new_scopipe = hw_scopipe_handle(scopipe)
        add_scopipe_handle(new_scopipe)
        return new_scopipe
    }

    fun readremote(remote_stage : hw_pipex_stage, remote_local: hw_pipex_var) : hw_var {
        var new_expr = hw_exec_read_remote(remote_stage, remote_local)
        var genvar = hw_var(GetGenName("var"), remote_local.vartype, "0")
        remote_stage.AddRdVar(remote_local)
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun isactive(remote_stage : hw_pipex_stage) : hw_var {
        var new_expr = hw_exec_stage_stat(remote_stage, OP_ISACTIVE)
        var genvar = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun isworking(remote_stage : hw_pipex_stage) : hw_var {
        var new_expr = hw_exec_stage_stat(remote_stage, OP_ISWORKING)
        var genvar = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun isstalled(remote_stage : hw_pipex_stage) : hw_var {
        var new_expr = hw_exec_stage_stat(remote_stage, OP_ISSTALLED)
        var genvar = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun issucc(remote_stage : hw_pipex_stage) : hw_var {
        var new_expr = hw_exec_stage_stat(remote_stage, OP_ISSUCC)
        var genvar = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun pstall() {
        AddExpr(hw_exec(OP_PSTALL))
    }

    fun pkill() {
        AddExpr(hw_exec(OP_PKILL))
    }

    fun pflush() {
        AddExpr(hw_exec(OP_PFLUSH))
    }

    fun assign_always(tgt : hw_global, src: hw_param) {
        var new_expr = hw_exec(OP_ASSIGN_ALWAYS)
        new_expr.AddDst(tgt)
        new_expr.AddParam(src)
        AddExpr(new_expr)
    }

    fun assign_always(tgt : hw_global, src: Int) {
        assign_always(tgt, hw_imm(src))
    }

    fun assign_succ(tgt : hw_var, src: hw_param) {
        var new_expr = hw_exec(OP_ASSIGN_SUCC)
        new_expr.AddDst(tgt)
        new_expr.AddParam(src)
        AddExpr(new_expr)
    }

    fun assign_succ(tgt : hw_var, src: Int) {
        assign_succ(tgt, hw_imm(src))
    }

    fun accum(tgt : hw_local, src: hw_param) {
        var new_expr = hw_exec(OP_ACCUM)
        new_expr.AddDst(tgt)
        new_expr.AddParam(src)
        AddExpr(new_expr)
    }

    fun accum(tgt : hw_local, src: Int) {
        accum(tgt, hw_imm(src))
    }

    fun readPrev(src : hw_pipex_var) : hw_pipex_var {
        var new_expr = hw_exec(OP_RD_PREV)
        var genvar = hw_pipex_var(GetGenName("readPrev"), src.vartype, src.defimm)
        genvar.default_astc = this
        new_expr.AddRdVar(src)
        new_expr.AddGenVar(genvar)
        new_expr.AddDst(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun mcopipe_req(mcopipe_if : hw_mcopipe_if, mcopipe_handle : hw_mcopipe_handle, cmd : hw_param, wdata : hw_param) : hw_var {
        var new_expr = hw_exec_mcopipe_req(mcopipe_if, mcopipe_handle)
        var genvar = hw_var(GetGenName("mcopipe_req_rdy"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddParam(cmd)
        new_expr.AddParam(wdata)
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun mcopipe_resp(mcopipe_handle : hw_mcopipe_handle, rdata : hw_var) : hw_var {
        var new_expr = hw_exec_mcopipe_resp(mcopipe_handle)
        var genvar = hw_var(GetGenName("mcopipe_resp_rdy"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        new_expr.AddDst(rdata)
        AddExpr(new_expr)
        return genvar
    }

    fun scopipe_req(scopipe_if : hw_scopipe_if, scopipe_handle : hw_scopipe_handle, cmd : hw_var, rdata : hw_var) : hw_var {
        var new_expr = hw_exec_scopipe_req(scopipe_if, scopipe_handle)
        var genvar = hw_var(GetGenName("scopipe_req_rdy"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddDst(cmd)
        new_expr.AddDst(rdata)
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun scopipe_resp(scopipe_handle : hw_scopipe_handle, wdata : hw_param) : hw_var {
        var new_expr = hw_exec_scopipe_resp(scopipe_handle)
        var genvar = hw_var(GetGenName("scopipe_resp_rdy"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddParam(wdata)
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun validate() {
        if ((pipeline_fc_mode == PIPELINE_FC_MODE.CREDIT_BASED) && (Stages.size < 3)) ERROR("Can't make credit-based mechanism for stage number < 3")
        for (wrvar in wrvars) {
            if (!wrvar.value.write_done) WARNING("signal " + wrvar.value.name + " is not initialized")
        }
        for (rdvar in rdvars) {
            if (!rdvar.value.read_done) WARNING("signal " + rdvar.value.name + " is not used!")
        }
    }

    internal fun ProcessSyncOp(expression : hw_exec, Translate_info : __TranslateInfo, pstage_info : __pstage_info, cyclix_gen : cyclix.Generic) {
        if ((expression.opcode == OP1_IF) || (expression.opcode == OP1_WHILE)) {
            for (subexpression in expression.expressions) ProcessSyncOp(subexpression, Translate_info, pstage_info, cyclix_gen)

        } else if (expression.opcode == OP_ACCUM) {
            if (!pstage_info.accum_tgts.contains(expression.dsts[0])) {
                pstage_info.accum_tgts.add(expression.dsts[0])
            }

        } else if (expression.opcode == OP_ASSIGN_SUCC) {
            if (!pstage_info.assign_succ_assocs.containsKey(expression.dsts[0])) {
                val req = cyclix_gen.ulocal(GetGenName("succreq"), 0, 0, "0")

                var buf = DUMMY_VAR
                if (expression.dsts[0] is hw_pipex_var) buf = cyclix_gen.local(GetGenName("succbuf"), expression.dsts[0].vartype, expression.dsts[0].defimm)
                else if (expression.dsts[0] is hw_var_frac) buf = cyclix_gen.local(GetGenName("succbuf"), ((expression.dsts[0] as hw_var_frac).src_var as hw_pipex_var).vartype, expression.dsts[0].defimm)
                else ERROR("ProcessSyncOp")

                if (expression.dsts[0] is hw_pipex_var) pstage_info.assign_succ_assocs.put((expression.dsts[0] as hw_pipex_var), __assign_buf(req, buf))
                else if (expression.dsts[0] is hw_var_frac) pstage_info.assign_succ_assocs.put(((expression.dsts[0] as hw_var_frac).src_var as hw_pipex_var), __assign_buf(req, buf))
                else ERROR("ProcessSyncOp")
            }

        } else if (expression.opcode == OP_RD_REMOTE) {
            (expression as hw_exec_read_remote).stage.AddRdVar(expression.remote_var)

        } else if (expression.opcode == OP_MCOPIPE_REQ) {
            if (!pstage_info.mcopipe_handle_reqs.contains((expression as hw_exec_mcopipe_req).mcopipe_handle)) {
                pstage_info.mcopipe_handle_reqs.add(expression.mcopipe_handle)
            }

        } else if (expression.opcode == OP_MCOPIPE_RESP) {
            if (!pstage_info.mcopipe_handle_resps.contains((expression as hw_exec_mcopipe_resp).mcopipe_handle))
                pstage_info.mcopipe_handle_resps.add(expression.mcopipe_handle)

        } else if (expression.opcode == OP_SCOPIPE_REQ) {
            if (!pstage_info.scopipe_handle_reqs.contains((expression as hw_exec_scopipe_req).scopipe_handle))
                pstage_info.scopipe_handle_reqs.add(expression.scopipe_handle)

        } else if (expression.opcode == OP_SCOPIPE_RESP) {
            if (!pstage_info.scopipe_handle_resps.contains((expression as hw_exec_scopipe_resp).scopipe_handle))
                pstage_info.scopipe_handle_resps.add(expression.scopipe_handle)

        }
    }

    internal fun FillMcopipeReqDict(expression : hw_exec, TranslateInfo : __TranslateInfo) {
        if ((expression.opcode == OP1_IF) || (expression.opcode == OP1_WHILE)) {
            for (subexpression in expression.expressions) FillMcopipeReqDict(subexpression, TranslateInfo)
        } else if (expression.opcode == OP_MCOPIPE_REQ) {
            var mcopipe_ArrayList = TranslateInfo.__mcopipe_handle_reqdict[(expression as hw_exec_mcopipe_req).mcopipe_handle]!!
            if (!mcopipe_ArrayList.contains(expression.mcopipe_if)) mcopipe_ArrayList.add(expression.mcopipe_if)
        }
    }

    internal fun FillScopipeReqDict(expression : hw_exec, TranslateInfo : __TranslateInfo) {
        if ((expression.opcode == OP1_IF) || (expression.opcode == OP1_WHILE)) {
            for (subexpression in expression.expressions) FillScopipeReqDict(subexpression, TranslateInfo)
        } else if (expression.opcode == OP_SCOPIPE_REQ) {
            var scopipe_ArrayList = TranslateInfo.__scopipe_handle_reqdict[(expression as hw_exec_scopipe_req).scopipe_handle]!!
            if (!scopipe_ArrayList.contains(expression.scopipe_if)) scopipe_ArrayList.add(expression.scopipe_if)
        }
    }

    internal fun reconstruct_expression(debug_lvl : DEBUG_LEVEL,
                               cyclix_gen : hw_astc,
                               expr : hw_exec,
                               context : import_expr_context) {

        cyclix_gen as cyclix.Generic
        context as pipex_import_expr_context

        MSG(debug_lvl, "Reconstructing expression: " + expr.opcode.default_string)
        for (param in expr.params)  MSG(debug_lvl, "\t\tparam: "   + param.GetString())
        for (tgt in expr.dsts)      MSG(debug_lvl, "\t\ttgt: "     + tgt.GetString())
        for (rdvar in expr.rdvars)  MSG(debug_lvl, "\t\trdvar: "   + rdvar.GetString())
        for (wrvar in expr.wrvars)  MSG(debug_lvl, "\t\twrvar: "   + wrvar.GetString())

        if ((expr.opcode == OP1_ASSIGN)) {

            if (expr.dsts[0] is hw_global) {
                cyclix_gen.begif(context.curStageInfo.ctrl_active)
                run {
                    cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), context.curStageInfo.TranslateParam(expr.params[0]))
                }; cyclix_gen.endif()
            } else {
                cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), context.curStageInfo.TranslateParam(expr.params[0]))
            }

        } else if (expr.opcode == OP_PKILL) {
            if (context.TranslateInfo.pipeline.pipeline_fc_mode == PIPELINE_FC_MODE.CREDIT_BASED) {
                var curStageIndex = context.TranslateInfo.StageList.indexOf(context.curStage)
                if ((curStageIndex != 0) && (curStageIndex != context.TranslateInfo.StageList.lastIndex)) {
                    ERROR("Attempting to kill transaction with credit-based mechanism at stage " + context.curStage.name)
                }
            }
            context.curStageInfo.kill_cmd_internal()

        } else if (expr.opcode == OP_PSTALL) {
            if (context.TranslateInfo.pipeline.pipeline_fc_mode == PIPELINE_FC_MODE.CREDIT_BASED) {
                var curStageIndex = context.TranslateInfo.StageList.indexOf(context.curStage)
                if ((curStageIndex != 0) && (curStageIndex != context.TranslateInfo.StageList.lastIndex)) {
                    ERROR("Attempting to stall transaction with credit-based mechanism at stage " + context.curStage.name)
                }
            }
            context.curStageInfo.stall_cmd_internal()

        } else if (expr.opcode == OP_PFLUSH) {
            if (context.TranslateInfo.pipeline.pipeline_fc_mode == PIPELINE_FC_MODE.CREDIT_BASED) {
                ERROR("Attempting to slush pipeline with credit-based mechanism at stage " + context.curStage.name)
            }
            context.curStageInfo.pflush_cmd_internal(cyclix_gen)

        } else if (expr.opcode == OP_RD_PREV) {
            cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), cyclix_gen.readPrev(context.curStageInfo.TranslateVar(expr.rdvars[0])))

        } else if (expr.opcode == OP_ACCUM) {
            context.curStageInfo.accum(context.curStageInfo.TranslateVar(expr.dsts[0]), context.curStageInfo.TranslateParam(expr.params[0]))

        } else if (expr.opcode == OP_ASSIGN_ALWAYS) {
            cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), context.curStageInfo.TranslateParam(expr.params[0]))

        } else if (expr.opcode == OP_ASSIGN_SUCC) {
            if (expr.dsts[0] is hw_pipex_var) {
                cyclix_gen.assign(context.curStageInfo.assign_succ_assocs[expr.dsts[0]]!!.req, 1)
                cyclix_gen.assign(context.curStageInfo.assign_succ_assocs[expr.dsts[0]]!!.buf, context.curStageInfo.TranslateParam(expr.params[0]))
            } else if (expr.dsts[0] is hw_var_frac) {
                cyclix_gen.assign(context.curStageInfo.assign_succ_assocs[(expr.dsts[0] as hw_var_frac).src_var]!!.req, 1)
                cyclix_gen.assign(context.curStageInfo.assign_succ_assocs[(expr.dsts[0] as hw_var_frac).src_var]!!.buf.GetFracRef(TranslateFractions((expr.dsts[0] as hw_var_frac).depow_fractions, context.curStageInfo.var_dict)), context.curStageInfo.TranslateParam(expr.params[0]))
            } else ERROR("ASSIGN_SUCC")

        } else if (expr.opcode == OP_RD_REMOTE) {
            cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), context.TranslateInfo.__stage_assocs[(expr as hw_exec_read_remote).stage]!!.TranslateVar(expr.remote_var))

        } else if (expr.opcode == OP_ISACTIVE) {
            cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), context.TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.ctrl_active)

        } else if (expr.opcode == OP_ISWORKING) {
            cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), context.TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.ctrl_working)

        } else if (expr.opcode == OP_ISSTALLED) {
            cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), context.TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.ctrl_stalled_glbl)

        } else if (expr.opcode == OP_ISSUCC) {
            cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), context.TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.ctrl_succ)

        } else if (expr.opcode == OP_ISFINISHED) {
            cyclix_gen.assign(
                context.curStageInfo.TranslateVar(expr.dsts[0]),
                context.TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.ctrl_finish
            )

        } else if (expr.opcode == OP_FIFO_WR_UNBLK) {
            var wdata_translated = context.curStageInfo.TranslateParam(expr.params[0])
            var ack_translated = context.curStageInfo.TranslateVar(expr.dsts[0])
            var fifo_wr_translated = context.TranslateInfo.__fifo_wr_assocs[(expr as hw_exec_fifo_wr_unblk).fifo]!!
            cyclix_gen.begif(context.curStageInfo.ctrl_active)
            run {
                cyclix_gen.assign(ack_translated, cyclix_gen.fifo_wr_unblk(fifo_wr_translated, wdata_translated))
            }; cyclix_gen.endif()

        } else if (expr.opcode == OP_FIFO_RD_UNBLK) {
            var ack_translated = context.curStageInfo.TranslateVar(expr.dsts[0])
            var rdata_translated = context.curStageInfo.TranslateVar(expr.dsts[1])
            var fifo_rd_translated = context.TranslateInfo.__fifo_rd_assocs[(expr as hw_exec_fifo_rd_unblk).fifo]!!
            cyclix_gen.begif(context.curStageInfo.ctrl_active)
            run {
                cyclix_gen.assign(ack_translated, cyclix_gen.fifo_rd_unblk(fifo_rd_translated, rdata_translated))
            }; cyclix_gen.endif()

        } else if (expr.opcode == OP_MCOPIPE_REQ) {

            var mcopipe_if              = (expr as hw_exec_mcopipe_req).mcopipe_if
            var mcopipe_if_assoc        = context.TranslateInfo.__mcopipe_if_assocs[mcopipe_if]!!
            var mcopipe_handle          = expr.mcopipe_handle
            var mcopipe_handle_assoc    = context.TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!

            var handleref = context.curStageInfo.TRX_BUF.GetFracRef(hw_frac_C(0), hw_frac_SubStruct("genmcopipe_handle_" + mcopipe_handle.name))

            var if_id           = handleref.GetFracRef(hw_frac_SubStruct("if_id"))
            var rdreq_pending   = handleref.GetFracRef(hw_frac_SubStruct("rdreq_pending"))
            var tid             = handleref.GetFracRef(hw_frac_SubStruct("tid"))

            cyclix_gen.begif(context.curStageInfo.ctrl_active)
            run {

                cyclix_gen.begif(cyclix_gen.bnot(mcopipe_if_assoc.full_flag))
                run {

                    // translating params
                    var cmd_translated      = context.curStageInfo.TranslateParam(expr.params[0])
                    var wdata_translated    = context.curStageInfo.TranslateParam(expr.params[1])

                    // finding id
                    var handle_id = context.TranslateInfo.__mcopipe_handle_reqdict[mcopipe_handle]!!.indexOf(expr.mcopipe_if)

                    var req_struct = cyclix_gen.local(GetGenName("req_struct"),
                        mcopipe_if_assoc.req_fifo.vartype,
                        mcopipe_if_assoc.req_fifo.defimm)

                    cyclix_gen.assign(req_struct.GetFracRef("we"), cmd_translated)
                    cyclix_gen.assign(req_struct.GetFracRef("wdata"), wdata_translated)

                    cyclix_gen.begif(cyclix_gen.fifo_wr_unblk(mcopipe_if_assoc.req_fifo, req_struct))
                    run {

                        // req management
                        cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), 1)
                        cyclix_gen.assign(rdreq_pending, cyclix_gen.lnot(cmd_translated))
                        cyclix_gen.assign(tid, mcopipe_if_assoc.wr_ptr)
                        cyclix_gen.assign(if_id, hw_imm(if_id.vartype.dimensions, handle_id.toString()))

                        // mcopipe wr done
                        cyclix_gen.begif(rdreq_pending)
                        run {
                            cyclix_gen.assign(mcopipe_if_assoc.wr_done, 1)
                        }; cyclix_gen.endif()

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()

            }; cyclix_gen.endif()

        } else if (expr.opcode == OP_MCOPIPE_RESP) {

            var mcopipe_handle          = (expr as hw_exec_mcopipe_resp).mcopipe_handle
            var mcopipe_handle_assoc    = context.TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!

            var handleref   = context.curStageInfo.TRX_BUF.GetFracRef(hw_frac_C(0), hw_frac_SubStruct("genmcopipe_handle_" + mcopipe_handle.name))

            var resp_done   = handleref.GetFracRef(hw_frac_SubStruct("resp_done"))
            var rdata       = handleref.GetFracRef(hw_frac_SubStruct("rdata"))

            cyclix_gen.begif(resp_done)
            run {
                cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[1]), rdata)
            }; cyclix_gen.endif()

            cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), resp_done)

        } else if (expr.opcode == OP_SCOPIPE_REQ) {

            var scopipe_if              = (expr as hw_exec_scopipe_req).scopipe_if
            var scopipe_if_assoc        = context.TranslateInfo.__scopipe_if_assocs[scopipe_if]!!
            var scopipe_handle          = expr.scopipe_handle
            var scopipe_handle_assoc    = context.TranslateInfo.__scopipe_handle_assocs[scopipe_handle]!!

            var handleref = context.curStageInfo.TRX_BUF.GetFracRef(hw_frac_C(0), hw_frac_SubStruct("genscopipe_handle_" + scopipe_handle.name))

            var if_id   = handleref.GetFracRef(hw_frac_SubStruct("if_id"))
            var we      = handleref.GetFracRef(hw_frac_SubStruct("we"))

            // finding id
            var handle_id = context.TranslateInfo.__scopipe_handle_reqdict[scopipe_handle]!!.indexOf(expr.scopipe_if)

            cyclix_gen.begif(context.curStageInfo.ctrl_active)
            run {

                var req_struct = cyclix_gen.local(GetGenName("req_struct"),
                    scopipe_if_assoc.req_fifo.vartype,
                    scopipe_if_assoc.req_fifo.defimm)

                cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(scopipe_if_assoc.req_fifo, req_struct))
                run {
                    cyclix_gen.subStruct_gen(context.curStageInfo.TranslateVar(expr.dsts[0]),  req_struct, "we")
                    cyclix_gen.subStruct_gen(context.curStageInfo.TranslateVar(expr.dsts[1]),  req_struct, "wdata")
                    cyclix_gen.assign(if_id, hw_imm(if_id.vartype.dimensions, handle_id.toString()))

                    cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[2]), 1)
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

        } else if (expr.opcode == OP_SCOPIPE_RESP) {

            var scopipe_handle          = (expr as hw_exec_scopipe_resp).scopipe_handle
            var scopipe_handle_assoc    = context.TranslateInfo.__scopipe_handle_assocs[scopipe_handle]!!

            var handleref = context.curStageInfo.TRX_BUF.GetFracRef(hw_frac_C(0), hw_frac_SubStruct("genscopipe_handle_" + scopipe_handle.name))

            var if_id   = handleref.GetFracRef(hw_frac_SubStruct("if_id"))
            var we      = handleref.GetFracRef(hw_frac_SubStruct("we"))

            cyclix_gen.begif(context.curStageInfo.ctrl_active)
            run {
                for (scopipe_if in context.TranslateInfo.__scopipe_handle_reqdict[scopipe_handle]!!) {
                    var scopipe_if_assoc        = context.TranslateInfo.__scopipe_if_assocs[scopipe_if]!!

                    cyclix_gen.begif(cyclix_gen.eq2(if_id, context.TranslateInfo.__scopipe_handle_reqdict[scopipe_handle]!!.indexOf(scopipe_if)))
                    run {
                        cyclix_gen.assign(context.curStageInfo.TranslateVar(expr.dsts[0]), cyclix_gen.fifo_wr_unblk(scopipe_if_assoc.resp_fifo, context.curStageInfo.TranslateVar(expr.rdvars[0])))
                    }; cyclix_gen.endif()
                }
            }; cyclix_gen.endif()

        } else cyclix_gen.import_expr(debug_lvl, expr, context, ::reconstruct_expression)
    }

    fun check_bufsize(stage : hw_pipex_stage, actual_bufsize: Int) {
        if ((stage.BUF_SIZE.cfg_mode == PSTAGE_BUF_SIZE_CFG_MODE.EXACT) && (stage.BUF_SIZE.SIZE != actual_bufsize)) {
            WARNING("Overriding pstage buffer size for stage " + stage.name + ", given: " + stage.BUF_SIZE.SIZE + ", actual: " + actual_bufsize)
            throw Exception()
        }
    }

    fun translate_to_cyclix(debug_lvl : DEBUG_LEVEL) : cyclix.Generic {

        NEWLINE()
        MSG("##############################################")
        MSG("#### Starting Pipex-to-Cyclix translation ####")
        MSG("#### module: " + name)
        MSG("##############################################")

        validate()

        var cyclix_gen = cyclix.Generic(name)
        var TranslateInfo = __TranslateInfo(this)

        MSG("Generating resources...")

        MSG(debug_lvl, "Processing globals")
        for (global in globals) {
            var new_global = cyclix_gen.global(("genpsticky_glbl_" + global.name), global.vartype, global.defimm)
            TranslateInfo.__global_assocs.put(global, new_global)
        }

        // Processing FIFOs
        for (fifo_out in fifo_outs) {
            var new_fifo_out = cyclix_gen.fifo_out(fifo_out.name, fifo_out.vartype)
            TranslateInfo.__fifo_wr_assocs.put(fifo_out, new_fifo_out)
        }
        for (fifo_in in fifo_ins) {
            var new_fifo_in = cyclix_gen.fifo_in(fifo_in.name, fifo_in.vartype)
            TranslateInfo.__fifo_rd_assocs.put(fifo_in, new_fifo_in)
        }

        // Generating mcopipes' resources //
        MSG(debug_lvl, "Generating mcopipes if resources")
        for (mcopipe_if in mcopipe_ifs) {
            val mcopipe_name_prefix = "genmcopipe_" + mcopipe_if.name + "_"

            var wr_done     = cyclix_gen.uglobal((mcopipe_name_prefix + "wr_done"), 0, 0, "0")
            var rd_done     = cyclix_gen.uglobal((mcopipe_name_prefix + "rd_done"), 0, 0, "0")
            var full_flag   = cyclix_gen.uglobal((mcopipe_name_prefix + "full_flag"), 0, 0, "0")
            var empty_flag  = cyclix_gen.uglobal((mcopipe_name_prefix + "empty_flag"), 0, 0, "1")
            var wr_ptr      = cyclix_gen.uglobal((mcopipe_name_prefix + "wr_ptr"), (mcopipe_if.trx_id_width-1), 0, "0")
            var rd_ptr      = cyclix_gen.uglobal((mcopipe_name_prefix + "rd_ptr"), (mcopipe_if.trx_id_width-1), 0, "0")
            var wr_ptr_next = cyclix_gen.ulocal((mcopipe_name_prefix + "wr_ptr_next"), (mcopipe_if.trx_id_width-1), 0, "0")
            var rd_ptr_next = cyclix_gen.ulocal((mcopipe_name_prefix + "rd_ptr_next"), (mcopipe_if.trx_id_width-1), 0, "0")

            var wr_struct = hw_struct("genpmodule_" + name + "_" + mcopipe_name_prefix + "genstruct_fifo_wdata")
            wr_struct.addu("we", 0, 0, "0")
            wr_struct.add("wdata", mcopipe_if.wdata_vartype, "0")

            var req_fifo = cyclix_gen.fifo_out((mcopipe_name_prefix + "req"), wr_struct)
            var resp_fifo = cyclix_gen.fifo_in((mcopipe_name_prefix + "resp"), mcopipe_if.rdata_vartype)

            TranslateInfo.__mcopipe_if_assocs.put(mcopipe_if, __mcopipe_if_info(
                wr_done,
                rd_done,
                full_flag,
                empty_flag,
                wr_ptr,
                rd_ptr,
                wr_ptr_next,
                rd_ptr_next,
                req_fifo,
                resp_fifo))
        }

        // Generating mcopipe handles' resources //
        MSG(debug_lvl, "Generating mcopipe handles' resources")
        for (mcopipe_handle in mcopipe_handles) {
            TranslateInfo.__mcopipe_handle_reqdict.put(mcopipe_handle, ArrayList<hw_mcopipe_if>())
        }
        for (stage in Stages) {
            for (expression in stage.value.expressions) {
                FillMcopipeReqDict(expression, TranslateInfo)
            }
        }
        for (mcopipe_handle in mcopipe_handles) {
            val name_prefix = "genmcopipe_handle_" + mcopipe_handle.name + "_genvar_"

            var mcopipe_handle_struct = hw_struct("genmcopipe_handle_" + mcopipe_handle.name + "_struct")
            mcopipe_handle_struct.addu("if_id", GetWidthToContain(TranslateInfo.__mcopipe_handle_reqdict[mcopipe_handle]!!.size)-1, 0, "0")
            mcopipe_handle_struct.addu("resp_done", 0, 0, "0")
            mcopipe_handle_struct.add ("rdata", mcopipe_handle.rdata_vartype, "0")
            mcopipe_handle_struct.addu("rdreq_pending", 0, 0, "0")
            mcopipe_handle_struct.addu("tid", (mcopipe_handle.trx_id_width-1), 0, "0")

            TranslateInfo.__mcopipe_handle_assocs.put(mcopipe_handle, __mcopipe_handle_info(
                mcopipe_handle_struct))
        }

        // Generating scopipes' resources //
        MSG(debug_lvl, "Generating scopipes' resources")
        for (scopipe_if in scopipe_ifs) {
            val scopipe_name_prefix = "genscopipe_" + scopipe_if.name + "_"

            var rd_struct = hw_struct("genpmodule_" + name + "_" + scopipe_name_prefix + "genstruct_fifo_wdata")
            rd_struct.addu("we", 0, 0, "0")
            rd_struct.add("wdata", scopipe_if.wdata_vartype, "0")

            var req_fifo = cyclix_gen.fifo_in((scopipe_name_prefix + "req"), rd_struct)
            var resp_fifo = cyclix_gen.fifo_out((scopipe_name_prefix + "resp"), scopipe_if.rdata_vartype)

            TranslateInfo.__scopipe_if_assocs.put(scopipe_if, __scopipe_if_info(
                req_fifo,
                resp_fifo))
        }

        // Generating scopipe handles' resources //
        MSG(debug_lvl, "Generating scopipe handles' resources")
        for (scopipe_handle in scopipe_handles) {
            TranslateInfo.__scopipe_handle_reqdict.put(scopipe_handle, ArrayList<hw_scopipe_if>())
        }
        for (stage in Stages) {
            for (expression in stage.value.expressions) {
                FillScopipeReqDict(expression, TranslateInfo)
            }
        }
        for (scopipe_handle in scopipe_handles) {
            val name_prefix = "genscopipe_handle_" + scopipe_handle.name + "_genvar_"

            var scopipe_handle_struct = hw_struct("genscopipe_handle_" + scopipe_handle.name + "_struct")
            scopipe_handle_struct.addu("if_id", GetWidthToContain(TranslateInfo.__scopipe_handle_reqdict[scopipe_handle]!!.size)-1, 0, "0")
            scopipe_handle_struct.addu("we", 0, 0, "0")

            TranslateInfo.__scopipe_handle_assocs.put(scopipe_handle, __scopipe_handle_info(
                scopipe_handle_struct))
        }

        freeze()

        // Put stages in ArrayLists
        for (stage in Stages) TranslateInfo.StageList.add(stage.value)

        // Analyzing sync operations //
        MSG(debug_lvl, "Distributing synchronization primitives by pstages")

        for (CUR_STAGE_INDEX in 0 until TranslateInfo.StageList.size) {
            val stage = TranslateInfo.StageList[CUR_STAGE_INDEX]
            val name_prefix         = "genpstage_" + stage.name
            val name_prefix_delim   = name_prefix + "_"

            var pctrl_flushreq      = cyclix_gen.ulocal((name_prefix_delim + "genpctrl_flushreq"), 0, 0, "0")

            var pstage_buf_size = 1
            if (CUR_STAGE_INDEX == 0) {
                check_bufsize(stage, 1)
                pstage_buf_size = 1
            } else {
                if (pipeline_fc_mode == PIPELINE_FC_MODE.CREDIT_BASED) {
                    if (CUR_STAGE_INDEX == TranslateInfo.StageList.lastIndex) {
                        check_bufsize(stage, TranslateInfo.StageList.size-1)
                        pstage_buf_size = TranslateInfo.StageList.size-1
                    } else {
                        check_bufsize(stage, 1)
                        pstage_buf_size = 1
                    }
                } else {
                    if (stage.BUF_SIZE.cfg_mode == PSTAGE_BUF_SIZE_CFG_MODE.EXACT) {
                        pstage_buf_size = stage.BUF_SIZE.SIZE
                    } else {
                        pstage_buf_size = 1
                    }
                }
            }

            var pstage_info = __pstage_info(cyclix_gen,
                name_prefix,
                pstage_buf_size,
                stage.fc_mode,
                (CUR_STAGE_INDEX == 0),
                TranslateInfo,
                pctrl_flushreq)

            TranslateInfo.__stage_assocs.put(stage, pstage_info)
            for (expression in stage.expressions) {
                ProcessSyncOp(expression, TranslateInfo, pstage_info, cyclix_gen)
            }
        }
        if (pipeline_fc_mode == PIPELINE_FC_MODE.CREDIT_BASED) TranslateInfo.gencredit_counter = cyclix_gen.uglobal("gencredit_counter", GetWidthToContain(Stages.size)-1, 0, "0")

        // Generating resources //
        MSG(debug_lvl, "Generating resources")

        // Put stages info in ArrayLists
        for (stageAssoc in TranslateInfo.__stage_assocs) TranslateInfo.StageInfoList.add(stageAssoc.value)

        MSG(debug_lvl, "Processing genvars")
        for (CUR_STAGE_INDEX in 0 until TranslateInfo.StageList.size) {
            for (genvar in TranslateInfo.StageList[CUR_STAGE_INDEX].genvars) {
                var genvar_local = cyclix_gen.local(GetGenName("var"), genvar.vartype, genvar.defimm)
                TranslateInfo.StageInfoList[CUR_STAGE_INDEX].pContext_local_dict.put(genvar, genvar_local)
            }
        }

        // Generate resources for (m/s)copipes
        for (CUR_STAGE_INDEX in 0 until TranslateInfo.StageList.size) {

            var curStage = TranslateInfo.StageList[CUR_STAGE_INDEX]
            var curStageInfo = TranslateInfo.StageInfoList[CUR_STAGE_INDEX]

            var prev_req_mcopipelist    = ArrayList<hw_mcopipe_handle>()
            var cur_req_mcopipelist     = ArrayList<hw_mcopipe_handle>()
            var cur_resp_mcopipelist    = ArrayList<hw_mcopipe_handle>()
            var next_resp_mcopipelist   = ArrayList<hw_mcopipe_handle>()

            var prev_req_scopipelist    = ArrayList<hw_scopipe_handle>()
            var cur_req_scopipelist     = ArrayList<hw_scopipe_handle>()
            var cur_resp_scopipelist    = ArrayList<hw_scopipe_handle>()
            var next_resp_scopipelist   = ArrayList<hw_scopipe_handle>()

            for (STAGE_INDEX_ANLZ in 0 until TranslateInfo.StageList.size) {

                var curStageAnlz = TranslateInfo.StageList[STAGE_INDEX_ANLZ]
                var curStageAnlz_info = TranslateInfo.StageInfoList[STAGE_INDEX_ANLZ]

                if (STAGE_INDEX_ANLZ < CUR_STAGE_INDEX) {
                    prev_req_mcopipelist =
                        UniteArrayLists(prev_req_mcopipelist, curStageAnlz_info.mcopipe_handle_reqs)
                    prev_req_scopipelist =
                        UniteArrayLists(prev_req_scopipelist, curStageAnlz_info.scopipe_handle_reqs)

                } else if (STAGE_INDEX_ANLZ == CUR_STAGE_INDEX) {
                    cur_req_mcopipelist =
                        UniteArrayLists(cur_req_mcopipelist, curStageAnlz_info.mcopipe_handle_reqs)
                    cur_resp_mcopipelist =
                        UniteArrayLists(cur_resp_mcopipelist, curStageAnlz_info.mcopipe_handle_resps)

                    cur_req_scopipelist =
                        UniteArrayLists(cur_req_scopipelist, curStageAnlz_info.scopipe_handle_reqs)
                    cur_resp_scopipelist =
                        UniteArrayLists(cur_resp_scopipelist, curStageAnlz_info.scopipe_handle_resps)

                } else {
                    next_resp_mcopipelist = UniteArrayLists(
                        next_resp_mcopipelist,
                        curStageAnlz_info.mcopipe_handle_resps
                    )
                    next_resp_scopipelist = UniteArrayLists(
                        next_resp_scopipelist,
                        curStageAnlz_info.scopipe_handle_resps
                    )
                }
            }

            curStageInfo.mcopipe_handles = UniteArrayLists(UniteArrayLists(cur_req_mcopipelist, cur_resp_mcopipelist), CrossArrayLists(prev_req_mcopipelist, next_resp_mcopipelist))
            curStageInfo.scopipe_handles = UniteArrayLists(UniteArrayLists(cur_req_scopipelist, cur_resp_scopipelist), CrossArrayLists(prev_req_scopipelist, next_resp_scopipelist))
        }

        // Generate resources for locals
        for (CUR_STAGE_INDEX in 0 until TranslateInfo.StageList.size) {
            var curStage = TranslateInfo.StageList[CUR_STAGE_INDEX]
            var curStageInfo = TranslateInfo.StageInfoList[CUR_STAGE_INDEX]

            var prev_wr_pvarlist    = ArrayList<hw_var>()
            var cur_wr_pvarlist     = ArrayList<hw_var>()
            var cur_rd_pvarlist     = ArrayList<hw_var>()
            var next_rd_pvarlist    = ArrayList<hw_var>()

            for (STAGE_INDEX_ANLZ in 0 until TranslateInfo.StageList.size) {
                if (STAGE_INDEX_ANLZ < CUR_STAGE_INDEX) {
                    prev_wr_pvarlist = UniteArrayLists(prev_wr_pvarlist, CrossArrayLists(TranslateInfo.StageList[STAGE_INDEX_ANLZ].wrvars, locals))

                } else if (STAGE_INDEX_ANLZ == CUR_STAGE_INDEX) {
                    cur_wr_pvarlist = UniteArrayLists(cur_wr_pvarlist, CrossArrayLists(TranslateInfo.StageList[STAGE_INDEX_ANLZ].wrvars, locals))
                    cur_rd_pvarlist = UniteArrayLists(cur_rd_pvarlist, CrossArrayLists(TranslateInfo.StageList[STAGE_INDEX_ANLZ].rdvars, locals))

                } else {
                    next_rd_pvarlist = UniteArrayLists(next_rd_pvarlist, CrossArrayLists(TranslateInfo.StageList[STAGE_INDEX_ANLZ].rdvars, locals))

                }
            }

            var pContext_locals = UniteArrayLists(UniteArrayLists(cur_wr_pvarlist, cur_rd_pvarlist), CrossArrayLists(prev_wr_pvarlist, next_rd_pvarlist))
            var pContext_notnew = CrossArrayLists(UniteArrayLists(cur_rd_pvarlist, next_rd_pvarlist), prev_wr_pvarlist)

            // processing pContext list
            for (local in pContext_locals) {
                if (local is hw_local) {
                    curStageInfo.pContext_local_dict.put(local, curStageInfo.AddLocal(hw_structvar(local.name, local.vartype, local.defimm)))
                }
            }

            // generating global sources
            for (notnew in pContext_notnew) {
                if (notnew is hw_local) {
                    curStageInfo.pContext_srcglbls.add(notnew)
                }
            }
            for (accum_tgt in curStageInfo.accum_tgts) {
                if (!curStageInfo.pContext_srcglbls.contains(accum_tgt)) {
                    curStageInfo.pContext_srcglbls.add(accum_tgt)
                    curStageInfo.newaccums.add(accum_tgt)
                }
            }

            for (srcglbl in curStageInfo.pContext_srcglbls) {
                curStageInfo.AddBuf(hw_structvar(srcglbl.name, srcglbl.vartype, srcglbl.defimm))
            }
            for (cur_mcopipe_handle in curStageInfo.mcopipe_handles) {
                curStageInfo.AddBuf(hw_structvar("genmcopipe_handle_" + cur_mcopipe_handle.name, TranslateInfo.__mcopipe_handle_assocs[cur_mcopipe_handle]!!.struct_descr))
            }
            for (cur_scopipe_handle in curStageInfo.scopipe_handles) {
                curStageInfo.AddBuf(hw_structvar("genscopipe_handle_" + cur_scopipe_handle.name, TranslateInfo.__scopipe_handle_assocs[cur_scopipe_handle]!!.struct_descr))
            }

            for (pContext_local_dict_entry in curStageInfo.pContext_local_dict) {
                curStageInfo.var_dict.put(pContext_local_dict_entry.key, pContext_local_dict_entry.value)
            }
            for (global_assoc in TranslateInfo.__global_assocs) {
                curStageInfo.var_dict.put(global_assoc.key, global_assoc.value)
            }
        }
        MSG("Generating resources: done")

        MSG("Generating logic...")

        cyclix_gen.MSG_COMMENT("mcopipe processing")
        for (mcopipe_if in TranslateInfo.__mcopipe_if_assocs) {
            cyclix_gen.assign(mcopipe_if.value.wr_done, 0)
            cyclix_gen.assign(mcopipe_if.value.rd_done, 0)

            // forming mcopipe ptr next values
            cyclix_gen.add_gen(mcopipe_if.value.wr_ptr_next, mcopipe_if.value.wr_ptr, 1)
            cyclix_gen.add_gen(mcopipe_if.value.rd_ptr_next, mcopipe_if.value.rd_ptr, 1)
        }

        cyclix_gen.MSG_COMMENT("logic of stages")
        for (CUR_STAGE_INDEX in TranslateInfo.StageList.lastIndex downTo 0) {

            var curStage        = TranslateInfo.StageList[CUR_STAGE_INDEX]
            var curStageInfo    = TranslateInfo.StageInfoList[CUR_STAGE_INDEX]

            cyclix_gen.COMMENT("#### Stage processing: " + curStage.name + " ####")

            curStageInfo.preinit_ctrls()
            cyclix_gen.assign(curStageInfo.pctrl_flushreq, 0)

            // Forming mcopipe_handles_last list
            MSG(debug_lvl ,"Detecting last mcopipe handles")
            for (mcopipe_handle in curStageInfo.mcopipe_handles) {
                if (CUR_STAGE_INDEX == TranslateInfo.StageList.lastIndex) {
                    curStageInfo.mcopipe_handles_last.add(mcopipe_handle)
                } else {
                    if (!TranslateInfo.StageInfoList[CUR_STAGE_INDEX+1].mcopipe_handles_last.contains(mcopipe_handle)) {
                        curStageInfo.mcopipe_handles_last.add(mcopipe_handle)
                    }
                }
            }

            // pipeline flush processing
            if (CUR_STAGE_INDEX < TranslateInfo.StageList.lastIndex) {
                cyclix_gen.bor_gen(curStageInfo.pctrl_flushreq, curStageInfo.pctrl_flushreq, TranslateInfo.StageInfoList[CUR_STAGE_INDEX+1].pctrl_flushreq)
            }

            // not a bubble
            cyclix_gen.begif(curStageInfo.ctrl_occupied)
            run {

                cyclix_gen.COMMENT("Initializing new newaccums descriptors ####")
                if (curStageInfo.newaccums.size != 0) {
                    cyclix_gen.begif(curStageInfo.ctrl_new)
                    run {
                        for (newaccum in curStageInfo.newaccums) {
                            var fracs = hw_fracs(0)
                            fracs.add(hw_frac_SubStruct(newaccum.name))
                            cyclix_gen.assign(curStageInfo.TRX_BUF.GetFracRef(fracs), newaccum.defimm)
                        }
                    }; cyclix_gen.endif()
                }

                cyclix_gen.MSG_COMMENT("Fetching locals from src_glbls")
                curStageInfo.init_locals()

                cyclix_gen.MSG_COMMENT("Acquiring mcopipe rdata")
                for (BUF_INDEX in 0 until curStageInfo.TRX_BUF_SIZE) {
                    for (mcopipe_handle in curStageInfo.mcopipe_handles) {

                        var handleref = curStageInfo.TRX_BUF.GetFracRef(hw_frac_C(BUF_INDEX), hw_frac_SubStruct("genmcopipe_handle_" + mcopipe_handle.name))

                        var if_id = handleref.GetFracRef(hw_frac_SubStruct("if_id"))
                        var rdreq_pending = handleref.GetFracRef(hw_frac_SubStruct("rdreq_pending"))
                        var tid = handleref.GetFracRef(hw_frac_SubStruct("tid"))
                        var resp_done = handleref.GetFracRef(hw_frac_SubStruct("resp_done"))
                        var rdata = handleref.GetFracRef(hw_frac_SubStruct("rdata"))

                        cyclix_gen.begif(rdreq_pending)
                        run {

                            var IF_NUM = 0
                            for (mcopipe_if in TranslateInfo.__mcopipe_handle_reqdict[mcopipe_handle]!!) {
                                var mcopipe_if_assoc = TranslateInfo.__mcopipe_if_assocs[mcopipe_if]!!

                                cyclix_gen.begif(cyclix_gen.eq2(if_id, hw_imm(if_id.vartype.dimensions, IF_NUM.toString())))
                                run {

                                    cyclix_gen.begif(cyclix_gen.eq2(tid, mcopipe_if_assoc.rd_ptr))
                                    run {

                                        var fifo_rdata = cyclix_gen.local(GetGenName("mcopipe_rdata"), mcopipe_if.rdata_vartype, "0")

                                        cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(mcopipe_if_assoc.resp_fifo, fifo_rdata))
                                        run {
                                            // acquiring data
                                            cyclix_gen.assign(rdreq_pending, 0)
                                            cyclix_gen.assign(resp_done, 1)
                                            cyclix_gen.assign(rdata, fifo_rdata)

                                            // mcopipe rd done
                                            cyclix_gen.assign(mcopipe_if_assoc.rd_done, 1)
                                        }; cyclix_gen.endif()

                                    }; cyclix_gen.endif()

                                }; cyclix_gen.endif()
                                IF_NUM++
                            }

                        }; cyclix_gen.endif()
                    }
                }

                // do not start payload if flush requested
                cyclix_gen.MSG_COMMENT("Pipeline flush processing")
                cyclix_gen.begif(curStageInfo.pctrl_flushreq)
                run {
                    if (CUR_STAGE_INDEX != 0) curStageInfo.kill_cmd_internal()
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()

            cyclix_gen.MSG_COMMENT("Saving succ targets")        // for indexed assignments
            for (assign_succ_assoc in curStageInfo.assign_succ_assocs) {
                cyclix_gen.assign(assign_succ_assoc.value.buf, curStageInfo.TranslateVar(assign_succ_assoc.key))
            }

            cyclix_gen.MSG_COMMENT("Generating payload")
            for (expr in curStage.expressions) {
                reconstruct_expression(debug_lvl,
                    cyclix_gen,
                    expr,
                    pipex_import_expr_context(curStageInfo.var_dict, curStage, TranslateInfo, curStageInfo)
                )
            }

            cyclix_gen.MSG_COMMENT("Processing of next pstage busyness")
            if (TranslateInfo.pipeline.pipeline_fc_mode == PIPELINE_FC_MODE.CREDIT_BASED) {
                if (CUR_STAGE_INDEX == 0) {
                    cyclix_gen.begif(cyclix_gen.eq2(TranslateInfo.gencredit_counter, TranslateInfo.StageList.size-1))
                    run {
                        curStageInfo.stall_cmd_internal()
                    }; cyclix_gen.endif()
                }
            } else {
                if (CUR_STAGE_INDEX < TranslateInfo.StageList.lastIndex) {
                    cyclix_gen.begif(!TranslateInfo.StageInfoList[CUR_STAGE_INDEX+1].ctrl_rdy)
                    run {
                        // prepeat from next pstage requested
                        curStageInfo.stall_cmd_internal()
                    }; cyclix_gen.endif()
                }
            }

            cyclix_gen.MSG_COMMENT("pctrl_finish and pctrl_succ formation")
            curStageInfo.postinit_ctrls()

            cyclix_gen.MSG_COMMENT("credit counter processing")
            if (pipeline_fc_mode == PIPELINE_FC_MODE.CREDIT_BASED) {
                if (CUR_STAGE_INDEX == 0) {
                    cyclix_gen.begif(curStageInfo.ctrl_succ)
                    run {
                        cyclix_gen.add_gen(TranslateInfo.gencredit_counter, TranslateInfo.gencredit_counter, 1)
                    }; cyclix_gen.endif()
                } else if (CUR_STAGE_INDEX == TranslateInfo.StageList.lastIndex) {
                    cyclix_gen.begif(curStageInfo.ctrl_succ)
                    run {
                        cyclix_gen.sub_gen(TranslateInfo.gencredit_counter, TranslateInfo.gencredit_counter, 1)
                    }; cyclix_gen.endif()
                }
            }

            cyclix_gen.MSG_COMMENT("asserting succ signals")
            if (curStageInfo.assign_succ_assocs.size > 0) {
                cyclix_gen.begif(curStageInfo.ctrl_succ)
                run {
                    for (assign_succ_assoc in curStageInfo.assign_succ_assocs) {
                        cyclix_gen.begif(assign_succ_assoc.value.req)
                        run {
                            cyclix_gen.assign(curStageInfo.TranslateVar(assign_succ_assoc.key), assign_succ_assoc.value.buf)
                        }; cyclix_gen.endif()
                    }
                }; cyclix_gen.endif()
            }

            cyclix_gen.MSG_COMMENT("processing context in case transaction is ready to propagate")
            cyclix_gen.begif(curStageInfo.ctrl_finish)
            run {
                cyclix_gen.MSG_COMMENT("programming next stage in case transaction is able to propagate")
                cyclix_gen.begif(curStageInfo.ctrl_succ)
                run {
                    if (CUR_STAGE_INDEX < TranslateInfo.StageList.lastIndex) {
                        if (pipeline_fc_mode != PIPELINE_FC_MODE.CREDIT_BASED) cyclix_gen.begif(TranslateInfo.StageInfoList[CUR_STAGE_INDEX+1].ctrl_rdy)
                        run {
                            cyclix_gen.MSG_COMMENT("propagating transaction context")

                            var push_trx = TranslateInfo.StageInfoList[CUR_STAGE_INDEX+1].GetPushTrx(TranslateInfo.StageInfoList[CUR_STAGE_INDEX+1].name_prefix + "_push_trx")

                            // locals
                            cyclix_gen.assign_subStructs(push_trx, curStageInfo.TRX_LOCAL)

                            // mcopipe_handles
                            for (mcopipe_handle in curStageInfo.mcopipe_handles) {
                                if (TranslateInfo.StageInfoList[CUR_STAGE_INDEX+1].mcopipe_handles.contains(mcopipe_handle)) {
                                    cyclix_gen.assign_subStructs(push_trx.GetFracRef("genmcopipe_handle_" + mcopipe_handle.name), curStageInfo.TRX_BUF.GetFracRef(0).GetFracRef("genmcopipe_handle_" + mcopipe_handle.name))
                                }
                            }

                            // scopipe_handles
                            for (scopipe_handle in curStageInfo.scopipe_handles) {
                                if (TranslateInfo.StageInfoList[CUR_STAGE_INDEX+1].scopipe_handles.contains(scopipe_handle)) {
                                    cyclix_gen.assign_subStructs(push_trx.GetFracRef("genscopipe_handle_" + scopipe_handle.name), curStageInfo.TRX_BUF.GetFracRef(0).GetFracRef("genscopipe_handle_" + scopipe_handle.name))
                                }
                            }

                            TranslateInfo.StageInfoList[CUR_STAGE_INDEX+1].push_trx(push_trx)

                        }; if (pipeline_fc_mode != PIPELINE_FC_MODE.CREDIT_BASED) cyclix_gen.endif()
                    }
                }; cyclix_gen.endif()

                curStageInfo.pop_trx()

            }; cyclix_gen.endif()

            curStageInfo.finalize_ctrls()

            // working signal: succ or pstall
            cyclix_gen.bor_gen(curStageInfo.ctrl_working, curStageInfo.ctrl_succ, curStageInfo.ctrl_stalled_glbl)
        }

        for (__mcopipe_if_assoc in TranslateInfo.__mcopipe_if_assocs) {

            var mcopipe_if_assoc = __mcopipe_if_assoc.value

            // mcopipe rd fifo management
            cyclix_gen.begif(mcopipe_if_assoc.rd_done)
            run {
                cyclix_gen.assign(mcopipe_if_assoc.rd_ptr, mcopipe_if_assoc.rd_ptr_next)
                cyclix_gen.assign(mcopipe_if_assoc.full_flag, 0)

                cyclix_gen.begif(cyclix_gen.eq2(mcopipe_if_assoc.rd_ptr, mcopipe_if_assoc.wr_ptr))
                run {
                    cyclix_gen.assign(mcopipe_if_assoc.empty_flag, 1)
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

            // mcopipe wr fifo management
            cyclix_gen.begif(mcopipe_if_assoc.wr_done)
            run {
                cyclix_gen.assign(mcopipe_if_assoc.wr_ptr, mcopipe_if_assoc.wr_ptr_next)
                cyclix_gen.assign(mcopipe_if_assoc.empty_flag, 0)

                cyclix_gen.begif(cyclix_gen.eq2(mcopipe_if_assoc.rd_ptr, mcopipe_if_assoc.wr_ptr))
                run {
                    cyclix_gen.assign(mcopipe_if_assoc.full_flag, 1)
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()
        }

        cyclix_gen.end()

        MSG("Generating logic: done")

        MSG("###############################################")
        MSG("#### Pipex-to-Cyclix translation complete! ####")
        MSG("#### module: " + name)
        MSG("###############################################")

        return cyclix_gen
    }
}
