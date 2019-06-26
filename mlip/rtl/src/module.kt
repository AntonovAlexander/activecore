/*
 * hw_module.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package rtl

import hwast.*
import kotlin.reflect.jvm.internal.impl.types.TypeConstructorSubstitution

open class module(name_in : String) : hw_astc() {

    val name = name_in
    val OP_CPROC    = hw_opcode("cproc")

    override var GenNamePrefix   = "rtl"

    var wrvars      = mutableMapOf<String, hw_var>()
    var rdvars      = mutableMapOf<String, hw_var>()

    var Include_filenames = ArrayList<String>()
    var Combs       = ArrayList<hw_var>()
    var Ports       = ArrayList<hw_port>()
    var Mems        = ArrayList<hw_mem>()
    var SyncBufs    = ArrayList<hw_syncbuf>()
    var Cprocs      = ArrayList<hw_exec>()
    var Submodules  = mutableMapOf<String, hw_submodule>()

    fun getPortByName(name : String) : hw_port {
        for (port in Ports) {
            if (port.name == name) return port
        }
        ERROR("port " + name + " not found!")
        return Ports[0]
    }

    fun submodule(inst_name : String, new_submod : module) : hw_submodule {
        if (FROZEN_FLAG) ERROR("Failed to add submodule " + inst_name + ": ASTC frozen")
        if (Submodules.containsKey(inst_name)) ERROR("Naming conflict for instance: " + inst_name)
        var new_inst = hw_submodule(inst_name, new_submod, this)
        Submodules.put(inst_name, new_inst)
        return new_inst
    }

    private fun add_comb(new_comb : hw_var) {
        if (FROZEN_FLAG) ERROR("Failed to add comb " + new_comb.name + ": ASTC frozen")

        if (wrvars.containsKey(new_comb.name)) ERROR("Naming conflict for comb: " + new_comb.name)
        if (rdvars.containsKey(new_comb.name)) ERROR("Naming conflict for comb: " + new_comb.name)

        wrvars.put(new_comb.name, new_comb)
        rdvars.put(new_comb.name, new_comb)
        Combs.add(new_comb)
        new_comb.default_astc = this
    }

    fun comb(name : String, vartype : hw_type, defval : String) : hw_var {
        var ret_var = hw_var(name, vartype, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun comb(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static) : hw_var {
        var ret_var = hw_var(name, src_struct_in, dimensions)
        add_comb(ret_var)
        return ret_var
    }

    fun comb(name : String, src_struct_in : hw_struct) : hw_var {
        var ret_var = hw_var(name, src_struct_in)
        add_comb(ret_var)
        return ret_var
    }

    fun ucomb(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun ucomb(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun ucomb(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun scomb(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, dimensions, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun scomb(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun scomb(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, defval)
        add_comb(ret_var)
        return ret_var
    }

    private fun add_port(new_port : hw_port) {
        if (FROZEN_FLAG) ERROR("Failed to add port " + new_port.name + ": ASTC frozen")

        if (wrvars.containsKey(new_port.name)) ERROR("Naming conflict for port: " + new_port.name)
        if (rdvars.containsKey(new_port.name)) ERROR("Naming conflict for port: " + new_port.name)

        if (new_port.port_dir != PORT_DIR.IN) {
            wrvars.put(new_port.name, new_port)
            new_port.read_done = true
        }
        if (new_port.port_dir != PORT_DIR.OUT) {
            new_port.write_done = true
        }
        rdvars.put(new_port.name, new_port)
        Ports.add(new_port)
        new_port.default_astc = this
    }

    fun port(name : String, port_dir : PORT_DIR, vartype : hw_type, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, vartype, defval)
        add_port(ret_var)
        return ret_var
    }

    fun port(name : String, port_dir : PORT_DIR, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(src_struct_in, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun port(name : String, port_dir : PORT_DIR, src_struct_in : hw_struct) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(src_struct_in), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, vartype : hw_type, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, vartype, defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(src_struct_in, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, src_struct_in : hw_struct) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(src_struct_in), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, vartype : hw_type, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, vartype, defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(src_struct_in, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, src_struct_in : hw_struct) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(src_struct_in), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, vartype : hw_type, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, vartype, defval)
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(src_struct_in, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, src_struct_in : hw_struct) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(src_struct_in), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinout(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinout(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinout(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    private fun add_mem(new_mem : hw_mem) {
        if (FROZEN_FLAG) ERROR("Failed to add mem " + new_mem.name + ": ASTC frozen")

        if (rdvars.containsKey(new_mem.name)) ERROR("Naming conflict for mem: " + new_mem.name)
        rdvars.put(new_mem.name, new_mem)
        Mems.add(new_mem)
    }

    fun mem(name : String, vartype : hw_type, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, vartype, sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun mem(name : String, src_struct_in : hw_struct, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, hw_type(src_struct_in), sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun umem(name : String, dimensions : hw_dim_static, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun umem(name : String, msb: Int, lsb: Int, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun smem(name : String, dimensions : hw_dim_static, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, hw_type(VAR_TYPE.SIGNED, dimensions), sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun smem(name : String, msb: Int, lsb: Int, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), sync_type)
        add_mem(ret_var)
        return ret_var
    }

    private fun add_syncbuf(new_syncbuf : hw_syncbuf) {
        if (FROZEN_FLAG) ERROR("Failed to add syncbuf " + new_syncbuf.name + ": ASTC frozen")

        if (wrvars.containsKey(new_syncbuf.name)) ERROR("Naming conflict for syncbuf: " + new_syncbuf.name)
        if (rdvars.containsKey(new_syncbuf.name)) ERROR("Naming conflict for syncbuf: " + new_syncbuf.name)

        wrvars.put(new_syncbuf.name, new_syncbuf)
        rdvars.put(new_syncbuf.name, new_syncbuf)
        SyncBufs.add(new_syncbuf)
        Combs.add(new_syncbuf)
        Mems.add(new_syncbuf.buf)
        new_syncbuf.default_astc = this
    }

    fun buffered(name : String, vartype : hw_type, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, vartype, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(src_struct_in, dimensions), "0", clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, src_struct_in : hw_struct, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(src_struct_in), "0", clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, vartype : hw_type, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, vartype, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(src_struct_in, dimensions), "0", clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, src_struct_in : hw_struct, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(src_struct_in), "0", clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    // initiate new cproc
    fun cproc_begin() : hw_exec {
        var new_cproc = hw_exec(OP_CPROC)
        this.add(new_cproc)
        return new_cproc
    }

    // finalize cproc
    fun cproc_end() {
        if (this.size != 1) ERROR("Cproc ASTC inconsistent!")

        var cproc = this[0]
        if (cproc.opcode != OP_CPROC) ERROR("Cproc ASTC inconsistent!")

        cproc.SetCursor(0)

        // asserting defaults to iftargets
        for (iftarget in cproc.iftargets) {
            iftarget.assign(hw_imm(iftarget.defval))
        }

        // restoring stickies
        var wrvars_sticky = ArrayList<hw_sticky>()
        for (wrvar in cproc.wrvars) {
            if (wrvar is hw_sticky) wrvars_sticky.add(wrvar)
        }
        for (wrvar_sticky in wrvars_sticky) {
            wrvar_sticky.assign(wrvar_sticky.buf)
        }

        // adding generated combs
        for (genvar in cproc.genvars) {
            Combs.add(genvar)
        }

        cproc.ResetCursor()
        Cprocs.add(cproc)
        clear()
    }

    fun validate_cproc(cproc : hw_exec) : Boolean {
        return (cproc.opcode == OP_CPROC)
    }

    fun validate() {
        for (cproc in Cprocs) validate_cproc(cproc)
        // TODO: cproc wrvars and submodules intersection
        for (wrvar in wrvars) {
            if (!wrvar.value.write_done) WARNING("signal " + wrvar.value.name + " is not initialized")
        }
        for (rdvar in rdvars) {
            if (!rdvar.value.read_done) WARNING("signal " + rdvar.value.name + " is not used!")
        }
        println("Validation complete!")
    }

    fun export_to_sv(pathname : String) {

        println("############################################")
        println("#### rtl: starting SystemVerilog export ####")
        println("############################################")

        validate()
        freeze()

        var writer = SvWriter(this)
        writer.write(pathname)

        println("#############################################")
        println("#### rtl: SystemVerilog export complete! ####")
        println("#############################################")
    }
}