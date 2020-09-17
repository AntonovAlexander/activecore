/*
 * multipipeline.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

open class multipipeline(name_in : String) : hw_astc() {

    val name = name_in

    override var GenNamePrefix   = "reordex"

    var wrvars = mutableMapOf<String, hw_var>()
    var rdvars = mutableMapOf<String, hw_var>()

    var Ports           = ArrayList<hw_port>()
    var locals          = ArrayList<hw_var>()
    var globals         = ArrayList<hw_global>()

    var fifo_ifs = mutableMapOf<String, hw_structvar>()
    var fifo_ins = ArrayList<hw_fifo_in>()
    var fifo_outs = ArrayList<hw_fifo_out>()

    var ExecUnits  = mutableMapOf<String, hw_exec_unit>()

    fun exec_unit(name_in : String) : hw_exec_unit {
        if (FROZEN_FLAG) ERROR("Failed to add stage " + name_in + ": ASTC frozen")
        var new_stage = hw_exec_unit(name_in, this)
        if (ExecUnits.put(new_stage.name, new_stage) != null) {
            ERROR("Stage addition problem!")
        }
        return new_stage
    }

    fun begstage(stage : hw_exec_unit) {
        if (FROZEN_FLAG) ERROR("Failed to begin stage " + stage.name + ": ASTC frozen")
        if (this.size != 0) ERROR("reordex ASTC inconsistent!")
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

    fun local(name: String, vartype : hw_type, defval: String): hw_local {
        var ret_var = hw_local(name, vartype, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_local {
        var ret_var = hw_local(name, hw_type(src_struct_in, dimensions), "0")
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct_in: hw_struct): hw_local {
        var ret_var = hw_local(name, hw_type(src_struct_in), "0")
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    private fun add_local_sticky(new_local_sticky: hw_local_sticky) {
        if (FROZEN_FLAG) ERROR("Failed to add local_sticky " + new_local_sticky.name + ": ASTC frozen")

        if (wrvars.containsKey(new_local_sticky.name)) ERROR("Naming conflict for local_sticky: " + new_local_sticky.name)
        if (rdvars.containsKey(new_local_sticky.name)) ERROR("Naming conflict for local_sticky: " + new_local_sticky.name)

        wrvars.put(new_local_sticky.name, new_local_sticky)
        rdvars.put(new_local_sticky.name, new_local_sticky)
        locals.add(new_local_sticky)
        new_local_sticky.default_astc = this
    }

    fun local_sticky(name: String, vartype: hw_type, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, vartype, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(src_struct_in, dimensions), "0")
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, src_struct_in: hw_struct): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(src_struct_in), "0")
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, dimensions: hw_dim_static, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, msb: Int, lsb: Int, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, dimensions: hw_dim_static, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, msb: Int, lsb: Int, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_local_sticky(ret_var)
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

    fun global(name: String, vartype: hw_type, defval: String): hw_global {
        var ret_var = hw_global(name, vartype, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_global {
        var ret_var = hw_global(name, hw_type(src_struct_in, dimensions), "0")
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct_in: hw_struct): hw_global {
        var ret_var = hw_global(name, hw_type(src_struct_in), "0")
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    fun add_port(new_port: hw_port) {
        if (FROZEN_FLAG) ERROR("Failed to add port " + new_port.name + ": ASTC frozen")

        if (new_port.port_dir != PORT_DIR.IN) {
            if (wrvars.put(new_port.name, new_port) != null) {
                ERROR("Port addition problem!")
            }
            new_port.write_done = true
        }
        if (rdvars.put(new_port.name, new_port) != null) {
            ERROR("Port addition problem!")
        }
        Ports.add(new_port)
        new_port.default_astc = this
    }

    fun port(name: String,
             port_dir: PORT_DIR, vartype: hw_type, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, vartype, defval)
        add_port(ret_var)
        return ret_var
    }

    fun port(name: String, port_dir: PORT_DIR, src_struct_in: hw_struct, dimensions: hw_dim_static
    ): hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(src_struct_in, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun port(name: String, port_dir: PORT_DIR, src_struct_in: hw_struct): hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(src_struct_in), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uport(name: String, port_dir: PORT_DIR, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name: String, port_dir: PORT_DIR, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name: String, port_dir: PORT_DIR, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name: String, port_dir: PORT_DIR, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name: String, port_dir: PORT_DIR, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name: String, port_dir: PORT_DIR, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VarType, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VarType, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name: String, VarType: VAR_TYPE, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VarType, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(src_struct_in, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun input(name: String, src_struct_in: hw_struct): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(src_struct_in), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name: String, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name: String, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name: String, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name: String, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name: String, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name: String, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VarType, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VarType, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name: String, VarType: VAR_TYPE, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VarType, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(src_struct_in, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun output(name: String, src_struct_in: hw_struct): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(src_struct_in), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name: String, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name: String, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name: String, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name: String, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name: String, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name: String, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun add_fifo_in(new_fifo_in: hw_fifo_in) {
        if (FROZEN_FLAG) ERROR("Failed to add fifo_in " + new_fifo_in.name + ": ASTC frozen")

        if (fifo_ifs.put(new_fifo_in.name, new_fifo_in) != null) {
            ERROR("FIFO addition problem!")
        }
        fifo_ins.add(new_fifo_in)
    }

    fun fifo_in(name: String, vartype: hw_type): hw_fifo_in {
        var ret_var = hw_fifo_in(name, vartype)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int): hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(VarType, msb, lsb))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(src_struct_in, dimensions))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name: String, src_struct_in: hw_struct): hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(src_struct_in))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun ufifo_in(name: String, dimensions: hw_dim_static): hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(VAR_TYPE.UNSIGNED, dimensions))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun ufifo_in(name: String, msb: Int, lsb: Int): hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun sfifo_in(name: String, dimensions: hw_dim_static): hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(VAR_TYPE.SIGNED, dimensions))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun sfifo_in(name: String, msb: Int, lsb: Int): hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(VAR_TYPE.SIGNED, msb, lsb))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun add_fifo_out(new_fifo_out: hw_fifo_out) {
        if (FROZEN_FLAG) ERROR("Failed to add fifo_out " + new_fifo_out.name + ": ASTC frozen")

        if (fifo_ifs.put(new_fifo_out.name, new_fifo_out) != null) {
            ERROR("FIFO addition problem!")
        }
        fifo_outs.add(new_fifo_out)
    }

    fun fifo_out(name: String, vartype: hw_type): hw_fifo_out {
        var ret_var = hw_fifo_out(name, vartype)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int): hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(VarType, msb, lsb))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(src_struct_in, dimensions))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name: String, src_struct_in: hw_struct): hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(src_struct_in))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun ufifo_out(name: String, dimensions: hw_dim_static): hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(VAR_TYPE.UNSIGNED, dimensions))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun ufifo_out(name: String, msb: Int, lsb: Int): hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun sfifo_out(name: String, dimensions: hw_dim_static): hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(VAR_TYPE.SIGNED, dimensions))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun sfifo_out(name: String, msb: Int, lsb: Int): hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(VAR_TYPE.SIGNED, msb, lsb))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_wr(fifo: hw_fifo_out, wdata: hw_param): hw_var {
        var new_expr = hw_exec_fifo_wr(fifo)
        var genvar = hw_var(GetGenName("fifo_rdy"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddRdParam(wdata)
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun fifo_rd(fifo: hw_fifo_in, rdata: hw_var): hw_var {
        var new_expr = hw_exec_fifo_rd(fifo)
        var genvar = hw_var(GetGenName("fifo_rdy"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        new_expr.AddWrVar(rdata)
        AddExpr(new_expr)
        return genvar
    }
}
