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

open class module(val name : String) : hw_astc_stdif() {

    val OP_CPROC    = hw_opcode("cproc")

    override var GenNamePrefix   = "rtl"

    var Include_filenames = ArrayList<String>()
    var Combs       = ArrayList<hw_var>()
    var Mems        = ArrayList<hw_mem>()
    var SyncBufs    = ArrayList<hw_syncbuf>()
    var Cprocs      = ArrayList<hw_exec>()
    var Submodules  = mutableMapOf<String, hw_submodule>()

    fun submodule(inst_name : String, new_submod : module) : hw_submodule {
        if (FROZEN_FLAG) ERROR("Failed to add submodule " + inst_name + ": ASTC frozen")
        if (Submodules.containsKey(inst_name)) ERROR("Naming conflict for instance: " + inst_name)
        var new_inst = hw_submodule(inst_name, new_submod, this, false, true)
        Submodules.put(inst_name, new_inst)
        return new_inst
    }

    fun submodule_bb(inst_name : String, new_submod : module, include_needed : Boolean) : hw_submodule {
        if (FROZEN_FLAG) ERROR("Failed to add submodule " + inst_name + ": ASTC frozen")
        if (Submodules.containsKey(inst_name)) ERROR("Naming conflict for instance: " + inst_name)
        var new_inst = hw_submodule(inst_name, new_submod, this, true, include_needed)
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

    fun comb(name : String, vartype : hw_type, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, vartype, defimm)
        add_comb(ret_var)
        return ret_var
    }

    fun comb(name : String, vartype : hw_type, defval : String) : hw_var {
        var ret_var = hw_var(name, vartype, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun comb(name : String, src_struct : hw_struct, dimensions : hw_dim_static) : hw_var {
        var ret_var = hw_var(name, src_struct, dimensions)
        add_comb(ret_var)
        return ret_var
    }

    fun comb(name : String, src_struct : hw_struct) : hw_var {
        var ret_var = hw_var(name, src_struct)
        add_comb(ret_var)
        return ret_var
    }

    fun ucomb(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_UNSIGNED, dimensions, defimm)
        add_comb(ret_var)
        return ret_var
    }

    fun ucomb(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_UNSIGNED, dimensions, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun ucomb(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defimm)
        add_comb(ret_var)
        return ret_var
    }

    fun ucomb(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun ucomb(name : String, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_UNSIGNED, defimm)
        add_comb(ret_var)
        return ret_var
    }

    fun ucomb(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_UNSIGNED, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun scomb(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_SIGNED, dimensions, defimm)
        add_comb(ret_var)
        return ret_var
    }

    fun scomb(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_SIGNED, dimensions, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun scomb(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_SIGNED, msb, lsb, defimm)
        add_comb(ret_var)
        return ret_var
    }

    fun scomb(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_SIGNED, msb, lsb, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun scomb(name : String, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_SIGNED, defimm)
        add_comb(ret_var)
        return ret_var
    }

    fun scomb(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_SIGNED, defval)
        add_comb(ret_var)
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

    fun mem(name : String, src_struct : hw_struct, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, hw_type(src_struct), sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun umem(name : String, dimensions : hw_dim_static, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun umem(name : String, msb: Int, lsb: Int, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun smem(name : String, dimensions : hw_dim_static, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun smem(name : String, msb: Int, lsb: Int, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), sync_type)
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

    fun buffered(name : String, vartype : hw_type, defimm: hw_imm, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, vartype, defimm, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, vartype : hw_type, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, vartype, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, src_struct : hw_struct, dimensions : hw_dim_static, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(src_struct, dimensions), "0", clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, src_struct : hw_struct, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(src_struct), "0", clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, vartype : hw_type, defimm: hw_imm, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, vartype, defimm, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, vartype : hw_type, defval : String, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, vartype, defval, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, src_struct : hw_struct, dimensions : hw_dim_static, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(src_struct, dimensions), "0", clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, src_struct : hw_struct, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(src_struct), "0", clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, dimensions : hw_dim_static, defimm: hw_imm, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, msb: Int, lsb: Int, defimm: hw_imm, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, dimensions : hw_dim_static, defimm: hw_imm, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, msb: Int, lsb: Int, defimm: hw_imm, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, dimensions : hw_dim_static, defimm: hw_imm, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, msb: Int, lsb: Int, defimm: hw_imm, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, dimensions : hw_dim_static, defimm: hw_imm, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, msb: Int, lsb: Int, defimm: hw_imm, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, vartype : hw_type, defimm: hw_imm, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, vartype, defimm, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, vartype : hw_type, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, vartype, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, src_struct : hw_struct, dimensions : hw_dim_static, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(src_struct, dimensions), "0", clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, src_struct : hw_struct, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(src_struct), "0", clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, vartype : hw_type, defimm: hw_imm, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, vartype, defimm, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, vartype : hw_type, defval : String, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, vartype, defval, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, src_struct : hw_struct, dimensions : hw_dim_static, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(src_struct, dimensions), "0", clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, src_struct : hw_struct, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(src_struct), "0", clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, dimensions : hw_dim_static, defimm: hw_imm, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, msb: Int, lsb: Int, defimm: hw_imm, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, dimensions : hw_dim_static, defimm: hw_imm, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, msb: Int, lsb: Int, defimm: hw_imm, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, dimensions : hw_dim_static, defimm: hw_imm, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, msb: Int, lsb: Int, defimm: hw_imm, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, dimensions : hw_dim_static, defimm: hw_imm, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, msb: Int, lsb: Int, defimm: hw_imm, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm, clk)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval, clk)
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
            assign(iftarget, iftarget.defimm)
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

    fun end() {
        for (port in Ports) {
            if (port.vartype.DataType == DATA_TYPE.STRUCTURED) {
                port.vartype.src_struct.MarkStructInterface()
            }
        }
        validate()
        freeze()
    }

    fun export_to_sv(pathname : String, debug_lvl : DEBUG_LEVEL) {

        NEWLINE()
        MSG("#################################")
        MSG("#### Starting RTL generation ####")
        MSG("#### module: " + name)
        MSG("#################################")

        validate()

        var writer = SvWriter(this)
        writer.write(pathname, debug_lvl)

        MSG("##################################")
        MSG("#### RTL generation complete! ####")
        MSG("#### module: " + name)
        MSG("##################################")
    }
}

val DUMMY_MODULE = module("DUMMY")