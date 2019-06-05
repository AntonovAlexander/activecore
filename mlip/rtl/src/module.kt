package rtl

import hwast.*
import java.io.File
import kotlin.reflect.jvm.internal.impl.types.TypeConstructorSubstitution

open class module(name_in : String) : hw_astc() {

    val name = name_in
    val OP_CPROC    = hw_opcode("cproc")
    var tab_Counter = 0

    var hw_structs      = mutableMapOf<String, hw_struct>()
    var hw_if_structs   = ArrayList<hw_struct>()
    var hw_int_structs  = ArrayList<hw_struct>()

    var wrvars      = mutableMapOf<String, hw_var>()
    var rdvars      = mutableMapOf<String, hw_var>()

    var Combs       = ArrayList<hw_var>()
    var Ports       = ArrayList<hw_port>()
    var Mems        = ArrayList<hw_mem>()
    var SyncBufs    = ArrayList<hw_syncbuf>()
    var Cprocs      = ArrayList<hw_exec>()

    fun add_if_struct(new_struct : hw_struct) {
        hw_if_structs.add(new_struct)
        if (hw_structs.put(new_struct.name, new_struct) != null) {
            ERROR("Struct addition problem!")
        }
    }

    fun add_int_struct(new_struct : hw_struct) {
        hw_int_structs.add(new_struct)
        if (hw_structs.put(new_struct.name, new_struct) != null) {
            ERROR("Struct addition problem!")
        }
    }

    private fun add_comb(new_comb : hw_var) {
        if (wrvars.containsKey(new_comb.name)) ERROR("Naming conflict for comb: " + new_comb.name)
        if (rdvars.containsKey(new_comb.name)) ERROR("Naming conflict for comb: " + new_comb.name)

        wrvars.put(new_comb.name, new_comb)
        rdvars.put(new_comb.name, new_comb)
        Combs.add(new_comb)
        new_comb.default_astc = this
    }

    fun comb(name : String, VarType : VAR_TYPE, src_struct_in : hw_struct, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, src_struct_in, dimensions, defval)
        add_comb(ret_var)
        return ret_var
    }
    
    fun comb(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, dimensions, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun comb(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, msb, lsb, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun comb(name : String, VarType : VAR_TYPE, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, defval)
        add_comb(ret_var)
        return ret_var
    }

    fun comb(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_var {
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
        if (wrvars.containsKey(new_port.name)) ERROR("Naming conflict for port: " + new_port.name)
        if (rdvars.containsKey(new_port.name)) ERROR("Naming conflict for port: " + new_port.name)

        if (new_port.port_dir != PORT_DIR.IN) {
            wrvars.put(new_port.name, new_port)
            new_port.write_done = true
        }
        rdvars.put(new_port.name, new_port)
        Ports.add(new_port)
        new_port.default_astc = this
    }

    fun port(name : String, port_dir : PORT_DIR, VarType : VAR_TYPE, src_struct_in : hw_struct, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, VarType, src_struct_in, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun port(name : String, port_dir : PORT_DIR, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, VarType, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun port(name : String, port_dir : PORT_DIR, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, VarType, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun port(name : String, port_dir : PORT_DIR, VarType : VAR_TYPE, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, VarType, defval)
        add_port(ret_var)
        return ret_var
    }

    fun port(name : String, port_dir : PORT_DIR, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, port_dir, src_struct_in, dimensions)
        add_port(ret_var)
        return ret_var
    }

    fun port(name : String, port_dir : PORT_DIR, src_struct_in : hw_struct) : hw_port {
        var ret_var = hw_port(name, port_dir, src_struct_in)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.UNSIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.SIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.SIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VarType, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VarType, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, VarType : VAR_TYPE, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VarType, defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, src_struct_in, dimensions)
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, src_struct_in : hw_struct) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, src_struct_in)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.UNSIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.SIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.SIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VarType, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VarType, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, VarType : VAR_TYPE, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VarType, defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, src_struct_in, dimensions)
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, src_struct_in : hw_struct) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, src_struct_in)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.UNSIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.SIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.SIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, VarType, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, VarType, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, VarType : VAR_TYPE, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, VarType, defval)
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, src_struct_in, dimensions)
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, src_struct_in : hw_struct) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, src_struct_in)
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, VAR_TYPE.UNSIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinout(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, VAR_TYPE.SIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinout(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinout(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, VAR_TYPE.SIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    private fun add_mem(new_mem : hw_mem) {
        if (rdvars.containsKey(new_mem.name)) ERROR("Naming conflict for mem: " + new_mem.name)

        rdvars.put(new_mem.name, new_mem)
        Mems.add(new_mem)
    }

    fun mem(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, VarType, dimensions, sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun mem(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, VarType, msb, lsb, sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun mem(name : String, src_struct_in : hw_struct, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, src_struct_in, sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun umem(name : String, dimensions : hw_dim_static, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, VAR_TYPE.UNSIGNED, dimensions, sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun umem(name : String, msb: Int, lsb: Int, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, VAR_TYPE.UNSIGNED, msb, lsb, sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun smem(name : String, dimensions : hw_dim_static, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, VAR_TYPE.SIGNED, dimensions, sync_type)
        add_mem(ret_var)
        return ret_var
    }

    fun smem(name : String, msb: Int, lsb: Int, sync_type : SYNC_TYPE) : hw_mem {
        var ret_var = hw_mem(name, VAR_TYPE.SIGNED, msb, lsb, sync_type)
        add_mem(ret_var)
        return ret_var
    }

    private fun add_syncbuf(new_syncbuf : hw_syncbuf) {
        if (wrvars.containsKey(new_syncbuf.name)) ERROR("Naming conflict for syncbuf: " + new_syncbuf.name)
        if (rdvars.containsKey(new_syncbuf.name)) ERROR("Naming conflict for syncbuf: " + new_syncbuf.name)

        wrvars.put(new_syncbuf.name, new_syncbuf)
        rdvars.put(new_syncbuf.name, new_syncbuf)
        SyncBufs.add(new_syncbuf)
        Combs.add(new_syncbuf)
        Mems.add(new_syncbuf.buf)
        new_syncbuf.default_astc = this
    }

    fun buffered(name : String, VarType : VAR_TYPE, src_struct_in : hw_struct, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, VarType, src_struct_in, dimensions, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, VarType, dimensions, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, VarType, msb, lsb, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, src_struct_in, dimensions, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun buffered(name : String, src_struct_in : hw_struct, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, src_struct_in, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, VAR_TYPE.UNSIGNED, dimensions, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ubuffered(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, VAR_TYPE.UNSIGNED, msb, lsb, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, VAR_TYPE.SIGNED, dimensions, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sbuffered(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_buffered {
        var ret_var = hw_buffered(name, VAR_TYPE.SIGNED, msb, lsb, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, VarType : VAR_TYPE, src_struct_in : hw_struct, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, VarType, src_struct_in, dimensions, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, VarType, dimensions, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, VarType, msb, lsb, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, src_struct_in, dimensions, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun sticky(name : String, src_struct_in : hw_struct, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, src_struct_in, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, VAR_TYPE.UNSIGNED, dimensions, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun usticky(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, VAR_TYPE.UNSIGNED, msb, lsb, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, VAR_TYPE.SIGNED, dimensions, defval, clk, rst)
        add_syncbuf(ret_var)
        return ret_var
    }

    fun ssticky(name : String, msb: Int, lsb: Int, defval : String, clk : hw_var, rst : hw_var) : hw_sticky {
        var ret_var = hw_sticky(name, VAR_TYPE.SIGNED, msb, lsb, defval, clk, rst)
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
        println("Validation complete!")
    }

    fun getStringWithDim(param : hw_param) : String
    {
        if (param.type == PARAM_TYPE.VAR) return param.token_printable
        else return (param.dimensions[0].GetWidth().toString() + "'d" + param.token_printable)
    }

    fun getDimString(in_range : hw_dim_range_static) : String
    {
        return ("[" + in_range.msb.toString() + ":" + in_range.lsb.toString() + "]")
    }

    fun getDimString(fraction : hw_fraction) : String
    {
        if (fraction.type == FRAC_TYPE.C)
            return ("[" + (fraction as hw_fraction_C).index.token_printable + "]")

        else if (fraction.type == FRAC_TYPE.V)
            return ("[" + (fraction as hw_fraction_V).index.name + "]")

        else if (fraction.type == FRAC_TYPE.CC)
            return ("[" + (fraction as hw_fraction_CC).msb.token_printable + ":"
                    + fraction.lsb.token_printable + "]")

        else if (fraction.type == FRAC_TYPE.CV)
            return ("[" + (fraction as hw_fraction_CV).msb.token_printable + ":"
                    + fraction.lsb.name + "]")

        else if (fraction.type == FRAC_TYPE.VC)
            return ("[" + (fraction as hw_fraction_VC).msb.name + ":"
                    + fraction.lsb.token_printable + "]")

        else if (fraction.type == FRAC_TYPE.VV)
            return ("[" + (fraction as hw_fraction_VV).msb.name + ":"
                    + fraction.lsb.name + "]")

        else if (fraction.type == FRAC_TYPE.SubStruct)
            return ("." + (fraction as hw_fraction_SubStruct).substruct_name)

        else {
            ERROR("Dimensions unrecognized!")
            return ""
        }
    }

    fun getDimString(fractions : hw_fractions) : String
    {
        var ret_val = ""
        for (i in fractions.lastIndex downTo 0) {
            ret_val += getDimString(fractions[i])
        }
        return ret_val
    }

    fun export_sverilog_structvar(preambule_uncond : String, preambule_cond : String, trailer : String, structvar : hw_structvar, wrFile : java.io.OutputStreamWriter) {
        var dimstring = ""
        if (structvar.VarType == VAR_TYPE.STRUCTURED) {
            if (!structvar.dimensions.isSingle()) {
                for (dim in structvar.dimensions) {
                    dimstring += ("" + getDimString(dim))
                }
            }
            wrFile.write(preambule_uncond
                    + structvar.src_struct.name
                    + " "
                    + structvar.name
                    + dimstring
                    + trailer)
        } else {
            if (structvar.dimensions.size > 0) {
                for (DIM_INDEX in 1 until structvar.dimensions.size) {
                    dimstring += (" [" + structvar.dimensions[DIM_INDEX].msb + ":" + structvar.dimensions[DIM_INDEX].lsb + "]")
                }
                wrFile.write(preambule_uncond
                        + preambule_cond
                        + "["
                        + structvar.dimensions[0].msb.toString()
                        + ":"
                        + structvar.dimensions[0].lsb.toString()
                        + "] "
                        + structvar.name
                        + dimstring
                        + trailer)
            } else ERROR("Dimensions error")
        }
    }

    fun PrintTab(wrFile : java.io.OutputStreamWriter){
        for (i in 0 until tab_Counter) wrFile.write("\t")
    }

    fun export_sverilog_expr(wrFile : java.io.OutputStreamWriter, expr : hw_exec)
    {
        PrintTab(wrFile)

        var dimstring = getDimString(expr.fractions)

        var opstring = ""
        if (expr.opcode == OP1_ASSIGN) 	            opstring = ""
        else if (expr.opcode == OP1_COMPLEMENT) 	    opstring = "-"

        else if (expr.opcode == OP2_ARITH_ADD) 	    opstring = "+"
        else if (expr.opcode == OP2_ARITH_SUB) 	    opstring = "-"
        else if (expr.opcode == OP2_ARITH_MUL) 	    opstring = "*"
        else if (expr.opcode == OP2_ARITH_DIV) 	    opstring = "/"
        else if (expr.opcode == OP2_ARITH_SHL) 	    opstring = "<<"
        else if (expr.opcode == OP2_ARITH_SHR) 	    opstring = ">>"
        else if (expr.opcode == OP2_ARITH_SRA) 	    opstring = ">>>"

        else if (expr.opcode == OP1_LOGICAL_NOT)  opstring = "!"
        else if (expr.opcode == OP2_LOGICAL_AND)  opstring = "&&"
        else if (expr.opcode == OP2_LOGICAL_OR)   opstring = "||"
        else if (expr.opcode == OP2_LOGICAL_G)    opstring = ">"
        else if (expr.opcode == OP2_LOGICAL_L)    opstring = "<"
        else if (expr.opcode == OP2_LOGICAL_GEQ)  opstring = ">="
        else if (expr.opcode == OP2_LOGICAL_LEQ)  opstring = "<="
        else if (expr.opcode == OP2_LOGICAL_EQ2)  opstring = "=="
        else if (expr.opcode == OP2_LOGICAL_NEQ2) opstring = "!="
        else if (expr.opcode == OP2_LOGICAL_EQ4)  opstring = "==="
        else if (expr.opcode == OP2_LOGICAL_NEQ4) opstring = "!=="

        else if (expr.opcode == OP1_BITWISE_NOT) 	opstring = "~"
        else if (expr.opcode == OP2_BITWISE_AND) 	opstring = "&"
        else if (expr.opcode == OP2_BITWISE_OR) 	    opstring = "|"
        else if (expr.opcode == OP2_BITWISE_XOR) 	opstring = "^"
        else if (expr.opcode == OP2_BITWISE_XNOR) 	opstring = "^~"

        else if (expr.opcode == OP1_REDUCT_AND) 	    opstring = "&"
        else if (expr.opcode == OP1_REDUCT_NAND) 	opstring = "~&"
        else if (expr.opcode == OP1_REDUCT_OR) 	    opstring = "|"
        else if (expr.opcode == OP1_REDUCT_NOR) 	    opstring = "~|"
        else if (expr.opcode == OP1_REDUCT_XOR) 	    opstring = "^"
        else if (expr.opcode == OP1_REDUCT_XNOR) 	opstring = "^~"

        else if (expr.opcode == OP2_INDEXED) 	    opstring = ""
        else if (expr.opcode == OP3_RANGED) 	    opstring = ""
        else if (expr.opcode == OP2_SUBSTRUCT) 	opstring = ""
        else if (expr.opcode == OPS_CNCT) 	    opstring = ""
        else if (expr.opcode == OP1_IF) 	        opstring = ""
        else if (expr.opcode == OP1_WHILE) 	    opstring = ""

        else ERROR("operation" + expr.opcode.default_string + " not recognized")

        if ((expr.opcode == OP1_ASSIGN)
            || (expr.opcode == OP1_COMPLEMENT)
            || (expr.opcode == OP1_LOGICAL_NOT)
            || (expr.opcode == OP1_BITWISE_NOT)
            || (expr.opcode == OP1_REDUCT_AND)
            || (expr.opcode == OP1_REDUCT_NAND)
            || (expr.opcode == OP1_REDUCT_OR)
            || (expr.opcode == OP1_REDUCT_NOR)
            || (expr.opcode == OP1_REDUCT_XOR)
            || (expr.opcode == OP1_REDUCT_XNOR))
            {
                var var_descr = expr.wrvars[0].GetDepowered(expr.fractions)
                if ((var_descr.VarType == VAR_TYPE.STRUCTURED) && (expr.params[0].type == PARAM_TYPE.VAL)) {
                    if (opstring == "") {
                        wrFile.write(expr.wrvars[0].name +
                                dimstring +
                                " = '{default:" +
                                getStringWithDim(expr.params[0]) +
                                "};\n")
                    } else ERROR("assignment error")
                } else {
                    wrFile.write(expr.wrvars[0].name +
                            dimstring +
                            " = " +
                            opstring +
                            getStringWithDim(expr.params[0]) +
                            ";\n")
                }

        } else if ((expr.opcode == OP2_ARITH_ADD)
            || (expr.opcode == OP2_ARITH_SUB)
            || (expr.opcode == OP2_ARITH_MUL)
            || (expr.opcode == OP2_ARITH_DIV)
            || (expr.opcode == OP2_ARITH_SHL)
            || (expr.opcode == OP2_ARITH_SHR)
            || (expr.opcode == OP2_ARITH_SRA)

            || (expr.opcode == OP2_LOGICAL_AND)
            || (expr.opcode == OP2_LOGICAL_OR)
            || (expr.opcode == OP2_LOGICAL_G)
            || (expr.opcode == OP2_LOGICAL_L)
            || (expr.opcode == OP2_LOGICAL_GEQ)
            || (expr.opcode == OP2_LOGICAL_LEQ)
            || (expr.opcode == OP2_LOGICAL_EQ2)
            || (expr.opcode == OP2_LOGICAL_NEQ2)
            || (expr.opcode == OP2_LOGICAL_EQ4)
            || (expr.opcode == OP2_LOGICAL_NEQ4)

            || (expr.opcode == OP2_BITWISE_AND)
            || (expr.opcode == OP2_BITWISE_OR)
            || (expr.opcode == OP2_BITWISE_XOR)
            || (expr.opcode == OP2_BITWISE_XNOR)) {
            wrFile.write(expr.wrvars[0].name +
                    dimstring +
                    " = (" +
                    getStringWithDim(expr.params[0]) +
                    " " +
                    opstring +
                    " " +
                    getStringWithDim(expr.params[1]) +
                    ");\n")

        } else if (expr.opcode == OP2_INDEXED) {
            wrFile.write(expr.wrvars[0].name +
                    " = " +
                    expr.params[0].GetString() +
                    "[" +
                    expr.params[1].GetString() +
                    "];\n")

        } else if (expr.opcode == OP3_RANGED) {
            wrFile.write(expr.wrvars[0].name +
                    " = " +
                    expr.params[0].GetString() +
                    "[" +
                    expr.params[1].GetString() +
                    ":" +
                    expr.params[2].GetString() +
                    "];\n")

        } else if (expr.opcode == OP2_SUBSTRUCT) {
            wrFile.write(expr.wrvars[0].name +
                    " = " +
                    expr.params[0].GetString() +
                    "." +
                    expr.subStructvar_name +
                    ";\n")

        } else if (expr.opcode == OPS_CNCT) {
            var cnct_string = "{"
            for (i in 0 until expr.params.size) {
                if (i != 0) cnct_string += ", "
                cnct_string += getStringWithDim(expr.params[i])
            }
            cnct_string += "}"
            wrFile.write(expr.wrvars[0].name +
                    " = " +
                    cnct_string +
                    ";\n")

        } else if (expr.opcode == OP1_IF) {
            wrFile.write("if (" + expr.params[0].GetString() + ")\n")
            tab_Counter++
            PrintTab(wrFile)
            wrFile.write("begin\n")
            for (child_expr in expr.expressions) {
                export_sverilog_expr(wrFile, child_expr)
            }
            PrintTab(wrFile)
            wrFile.write("end\n")
            tab_Counter--

        } else if (expr.opcode == OP1_WHILE) {
            wrFile.write("while (" + expr.params[0].GetString() + " == 1'b1)\n")
            wrFile.write("begin\n")
            tab_Counter++
            for (child_expr in expr.expressions) {
                export_sverilog_expr(wrFile, child_expr)
            }
            PrintTab(wrFile)
            wrFile.write("end\n")
            tab_Counter--

        } else ERROR("undefined opcode")
    }

    fun export_sv(pathname : String) {

        println("#######################################")
        println("#### Starting SystemVerilog export ####")
        println("#######################################")

        validate()

        // writing interface structures
        // TODO: restrict to interfaces
        File(pathname).mkdirs()
        val wrFileInterface = File(pathname + "/" + name + ".svh").writer()
        wrFileInterface.write("`ifndef __" + name +"_h_\n")
        wrFileInterface.write("`define __" + name +"_h_\n")
        wrFileInterface.write("\n")

        println("Exporting structs...")

        for (hw_struct in hw_if_structs) {
            wrFileInterface.write("typedef struct packed {\n")
            for (structvar in hw_struct) {
                export_sverilog_structvar("\t", "logic ", ";\n", structvar, wrFileInterface)
            }
            wrFileInterface.write("} " + hw_struct.name + ";\n\n")
        }
        wrFileInterface.write("`endif\n")
        wrFileInterface.close()
        println("done")

        // writing module
        val wrFileModule = File(pathname + "/" + name + ".sv").writer()
        println("Exporting modules and ports...")
        wrFileModule.write("`include \"" + name + ".svh\"\n")
        wrFileModule.write("\n")
        wrFileModule.write("module " + name + " (\n")
        var preambule = "\t"
        for (port in Ports) {
            wrFileModule.write(preambule)
            var preambule_cond = ""
            var dir_string = ""
            if (port.port_dir == PORT_DIR.IN) dir_string = "input"
            else if (port.port_dir == PORT_DIR.OUT) {
                dir_string = "output"
                preambule_cond = "reg "
            } else if (port.port_dir == PORT_DIR.INOUT) dir_string = "inout"
            export_sverilog_structvar((dir_string + " "), preambule_cond, "", port, wrFileModule)
            preambule = "\n\t, "
        }

        wrFileModule.write("\n);\n")
        wrFileModule.write("\n")

        println("done")

        tab_Counter++

        println("Exporting combinationals...")
        for (comb in Combs) {
            export_sverilog_structvar("", "reg ", ";\n", comb, wrFileModule)
        }
        wrFileModule.write("\n")

        println("done")

        // Mems
        println("Exporting mems...")
        for (mem in Mems) {

            export_sverilog_structvar("","reg ", ";\n", mem, wrFileModule)

            if (mem.mem_srcs.size == 0) throw Exception("Mem signals with no sources is not supported, mem signal: %s!\n")
            else {
                var reset_sensivity = ""
                if (mem.rst_present && (mem.rst_type == RST_TYPE.ASYNC)) reset_sensivity = (", " + mem.rst_signal.name);

                var reset_condition = ""
                if (mem.rst_present)
                {
                    if (mem.rst_lvl == SYNC_LVL.POS) reset_condition = (mem.rst_signal.name + " == 1")
                    else reset_condition = (mem.rst_signal.name + " == 0");
                }

                for (mem_src in mem.mem_srcs)
                {
                    var sync_sensivity = ""
                    if (mem.sync_type == SYNC_TYPE.EDGE)
                    {
                        if (mem_src.sync_lvl == SYNC_LVL.POS) sync_sensivity = "posedge "
                        else sync_sensivity = "negedge ";
                    }

                    if (mem.rst_present) {
                        wrFileModule.write("always @("
                                + sync_sensivity
                                + mem_src.sync_signal.name
                                + reset_sensivity
                                + ")\n")

                        wrFileModule.write("\tif ($reset_condition)\n");
                        wrFileModule.write("\t\tbegin\n");
                        if (mem.dimensions.size == 1) {
                            wrFileModule.write("\t\t" + mem.name + " <= " + mem.rst_src.GetString() +";\n")
                        } else if (mem.dimensions.size == 2) {
                            var power = mem.dimensions[1].GetWidth()
                            for (k in 0 until power) {
                                if (mem.rst_src.type == PARAM_TYPE.VAR)
                                    wrFileModule.write("\t\t"
                                            + mem.name
                                            + "["
                                            + (k + mem.dimensions[1].lsb)
                                            + "] <= "
                                            + mem.rst_src.GetString()
                                            + "["
                                            + (k + mem.rst_src.GetDimensions()[1].lsb)
                                            + "];\n")
                                else
                                    wrFileModule.write("\t\t"
                                            + mem.name
                                            + "["
                                            + (k + mem.dimensions[1].lsb)
                                            + "] <= "
                                            + mem.rst_src.GetString()
                                            + ";\n")
                            }
                        } else if (mem.dimensions.size > 2)
                            ERROR("Large dimensions for mems (reset) are currently not supported!\n")
                        else {
                            if (mem.VarType == VAR_TYPE.STRUCTURED) {
                            wrFileModule.write("\t\t"
                                    + mem.name
                                    + " <= '{default:"
                                    + mem.rst_src.GetString()
                                    + "};\n")
                            } else
                                ERROR("Undimensioned mems (reset) are currently not supported!\n")
                        }

                        wrFileModule.write("\t\tend\n")
                        wrFileModule.write("\telse\n")
                        wrFileModule.write("\t\tbegin\n")
                        if (mem.dimensions.size == 1) {
                            wrFileModule.write("\t\t"
                                    + mem.name
                                    + " <= "
                                    + mem_src.sync_src.GetString()
                                    + ";\n")
                        } else if (mem.dimensions.size == 2) {
                            var power = mem.dimensions[1].GetWidth()
                            for (k in 0 until power) {
                                if (mem_src.sync_src.type == PARAM_TYPE.VAR)
                                wrFileModule.write("\t\t"
                                        + mem.name
                                        + "["
                                        + (k + mem.dimensions[1].lsb)
                                        + "] <= "
                                        + mem_src.sync_src.GetString()
                                        + "["
                                        + (k + mem_src.sync_src.GetDimensions()[1].lsb)
                                        + "];\n")
                                else
                                    wrFileModule.write("\t\t"
                                            + mem.name
                                            + "["
                                            + (k + mem.dimensions[1].lsb)
                                            + "] <= "
                                            + mem_src.sync_src.GetString()
                                            + ";\n")
                            }
                        } else if (mem.dimensions.size > 2)
                            ERROR("Large dimensions for mems (data) are currently not supported!\n")
                        else {
                            if (mem.VarType == VAR_TYPE.STRUCTURED) {
                                wrFileModule.write("\t\t"
                                        + mem.name
                                        + " <= "
                                        + mem_src.sync_src.GetString()
                                        + ";\n")
                            } else
                                ERROR("Undimensioned mems (data) are currently not supported!\n")
                        }
                        wrFileModule.write("\t\tend\n");

                    } else {
                        wrFileModule.write("always @("
                                + sync_sensivity
                                + mem_src.sync_signal.name
                                + reset_sensivity
                                + ") "
                                + mem.name
                                + " <= "
                                + mem_src.sync_src.GetString()
                                + ";\n")
                    }
                }
            }
            wrFileModule.write("\n")
        }
        wrFileModule.write("\n")
        println("done")

        // Cprocs
        println("Exporting cprocs...");
        for (cproc in Cprocs) {
            wrFileModule.write("always @*\n")
            tab_Counter = 1
            PrintTab(wrFileModule)
            wrFileModule.write("begin\n")

            for (expression in cproc.expressions) {
                export_sverilog_expr(wrFileModule, expression)
            }

            PrintTab(wrFileModule)
            wrFileModule.write("end\n")
            tab_Counter = 0
            wrFileModule.write("\n")
        }
        println("done")

        wrFileModule.write("\n")
        wrFileModule.write("endmodule\n")

        wrFileModule.close()

        println("########################################")
        println("#### SystemVerilog export complete! ####")
        println("########################################")
    }
}