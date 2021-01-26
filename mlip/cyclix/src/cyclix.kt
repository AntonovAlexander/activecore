/*
 * cyclix.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*
import java.lang.Exception
import java.io.File

val OP_CYCPROC = hw_opcode("cycproc")

open class Generic(name_in : String) : hw_astc_stdif() {

    val name = name_in

    override var GenNamePrefix   = "cyclix"

    var globals = ArrayList<hw_var>()
    var locals = ArrayList<hw_var>()

    var Subprocs  = mutableMapOf<String, hw_subproc>()

    var proc = hw_exec(OP_CYCPROC)

    init {
        this.add(proc)
    }

    private fun add_local(new_local : hw_var) {
        if (FROZEN_FLAG) ERROR("Failed to add local " + new_local.name + ": ASTC frozen")

        if (wrvars.containsKey(new_local.name)) ERROR("Naming conflict for local: " + new_local.name)
        if (rdvars.containsKey(new_local.name)) ERROR("Naming conflict for local: " + new_local.name)

        wrvars.put(new_local.name, new_local)
        rdvars.put(new_local.name, new_local)
        locals.add(new_local)
        new_local.default_astc = this
    }

    fun local(name : String, vartype : hw_type, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, vartype, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, vartype : hw_type, defval : String) : hw_var {
        var ret_var = hw_var(name, vartype, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static) : hw_var {
        var ret_var = hw_var(name, src_struct_in, dimensions)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, src_struct_in : hw_struct) : hw_var {
        var ret_var = hw_var(name, src_struct_in)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, dimensions, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, msb, lsb, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, dimensions, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, msb, lsb, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, defval)
        add_local(ret_var)
        return ret_var
    }

    private fun add_global(new_global : hw_var) {
        if (FROZEN_FLAG) ERROR("Failed to add comb " + new_global.name + ": ASTC frozen")

        if (wrvars.containsKey(new_global.name)) ERROR("Naming conflict for global: " + new_global.name)
        if (rdvars.containsKey(new_global.name)) ERROR("Naming conflict for global: " + new_global.name)

        wrvars.put(new_global.name, new_global)
        rdvars.put(new_global.name, new_global)
        globals.add(new_global)
        new_global.default_astc = this
    }

    fun global(name : String, vartype : hw_type, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, vartype, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, vartype : hw_type, defval : String) : hw_var {
        var ret_var = hw_var(name, vartype, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static) : hw_var {
        var ret_var = hw_var(name, src_struct_in, dimensions)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, src_struct_in : hw_struct, msb: Int, lsb: Int) : hw_var {
        var ret_var = hw_var(name, src_struct_in, msb, lsb)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, src_struct_in : hw_struct) : hw_var {
        var ret_var = hw_var(name, src_struct_in)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, dimensions, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, msb, lsb, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, dimensions, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, msb, lsb, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, defval)
        add_global(ret_var)
        return ret_var
    }

    fun subproc(inst_name : String, subproc_inst : Generic) : hw_subproc {
        var new_inst = hw_subproc(inst_name, subproc_inst, this)
        Subprocs.put(inst_name, new_inst)
        return new_inst
    }

    fun fifo_internal_wr_unblk(subproc : hw_subproc, fifo_name : String, wdata : hw_param) : hw_var {
        var new_expr = hw_exec_fifo_internal_wr_unblk(subproc, fifo_name, wdata)
        var genvar = hw_var(GetGenName("fifo_rdy"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddRdParam(wdata)
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun fifo_internal_rd_unblk(subproc : hw_subproc, fifo_name : String, rdata : hw_var) : hw_var {
        var new_expr = hw_exec_fifo_internal_rd_unblk(subproc, fifo_name, rdata)
        var genvar = hw_var(GetGenName("fifo_rdy"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        new_expr.AddWrVar(rdata)
        AddExpr(new_expr)
        return genvar
    }
    fun fifo_internal_wr_blk(subproc : hw_subproc, fifo_name : String, wdata : hw_param) {
        var new_expr = hw_exec_fifo_internal_wr_blk(subproc, fifo_name, wdata)
        new_expr.AddRdParam(wdata)
        AddExpr(new_expr)
    }

    fun fifo_internal_rd_blk(subproc : hw_subproc, fifo_name : String) : hw_var {
        var new_expr = hw_exec_fifo_internal_rd_blk(subproc, fifo_name)
        var genvar = hw_var(GetGenName("fifo_rdata"), subproc.getFifoByName(fifo_name).vartype, subproc.getFifoByName(fifo_name).defval)
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun validate() {
        for (wrvar in wrvars) {
            if (!wrvar.value.write_done) WARNING("signal " + wrvar.value.name + " is not initialized")
        }
        for (rdvar in rdvars) {
            if (!rdvar.value.read_done) WARNING("signal " + rdvar.value.name + " is not used!")
        }
    }

    fun end() {
        for (genvar in proc.genvars) {
            genvar.name = GetGenName("var")
            add_local(genvar)
        }
        for (port in Ports) {
            if (port.vartype.VarType == VAR_TYPE.STRUCTURED) {
                port.vartype.src_struct.MarkStructInterface()
            }
        }
        for (fifo in fifo_ins) {
            if (fifo.vartype.VarType == VAR_TYPE.STRUCTURED) {
                fifo.vartype.src_struct.MarkStructInterface()
            }
        }
        for (fifo in fifo_outs) {
            if (fifo.vartype.VarType == VAR_TYPE.STRUCTURED) {
                fifo.vartype.src_struct.MarkStructInterface()
            }
        }
        validate()
        freeze()
    }

    fun export_to_rtl() : rtl.module {

        println("###########################################")
        println("#### Starting Cyclix-to-RTL generation ####")
        println("#### module: " + name)
        println("###########################################")

        validate()

        var rtl_generator = RtlGenerator(this)
        var rtl_gen = rtl_generator.generate()

        println("############################################")
        println("#### Cyclix-to-RTL generation complete! ####")
        println("#### module: " + name)
        println("############################################")

        return rtl_gen
    }

    fun export_to_vivado_cpp(pathname : String) {

        println("############################################")
        println("#### Cyclix: starting vivado_cpp export ####")
        println("#### module: " + name)
        println("############################################")

        validate()
        freeze()

        var writer = VivadoCppWriter(this)
        writer.write(pathname)

        println("#############################################")
        println("#### Cyclix: vivado_cpp export complete! ####")
        println("#### module: " + name)
        println("#############################################")
    }
}


var STREAM_REQ_BUS_NAME = "stream_req_bus"
var STREAM_RESP_BUS_NAME = "stream_resp_bus"
open class Streaming (name : String, fifo_in_struct: hw_struct, fifo_out_struct: hw_struct) : Generic(name) {

    var stream_req_bus = fifo_in(STREAM_REQ_BUS_NAME, hw_type(fifo_in_struct))
    var stream_req_var = local(GetGenName("stream_req_var"), fifo_in_struct)

    var stream_resp_bus = fifo_out(STREAM_RESP_BUS_NAME, hw_type(fifo_out_struct))
    var stream_resp_var = local(GetGenName("stream_resp_var"), fifo_out_struct)
}
