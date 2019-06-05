package pipex

import hwast.*

val OP_RD_REMOTE    = hw_opcode("rd_remote")
val OP_ISACTIVE     = hw_opcode("isactive")
val OP_ISWORKING    = hw_opcode("isworking")
val OP_ISSTALLED    = hw_opcode("isstalled")
val OP_ISSUCC       = hw_opcode("issucc")
val OP_ISKILLED     = hw_opcode("iskilled")
val OP_ISFINISHED   = hw_opcode("isfinished")

val OP_PSTALL       = hw_opcode("pstall")
val OP_PKILL        = hw_opcode("pkill")
val OP_PFLUSH       = hw_opcode("pflush")

val OP_ASSIGN_SUCC  = hw_opcode("assign_succ")
val OP_ACCUM        = hw_opcode("accum")
val OP_RD_PREV     = hw_opcode("rd_prev")


open class hw_exec_stage_stat(stage_in : hw_stage, opcode : hw_opcode) : hw_exec(opcode) {
    var stage = stage_in
}

class hw_exec_read_remote(stage_in : hw_stage, remote_var_in : hw_pipex_var) : hw_exec_stage_stat(stage_in, OP_RD_REMOTE) {
    var remote_var = remote_var_in
}

open class pipeline(name_in : String) : hw_astc() {

    val name = name_in
    var tab_Counter = 0

    var hw_structs = mutableMapOf<String, hw_struct>()
    var hw_if_structs = ArrayList<hw_struct>()
    var hw_int_structs = ArrayList<hw_struct>()

    var wrvars = mutableMapOf<String, hw_var>()
    var rdvars = mutableMapOf<String, hw_var>()

    var Ports           = ArrayList<hw_port>()
    var locals          = ArrayList<hw_var>()
    var globals         = ArrayList<hw_global>()

    var fifo_ifs = mutableMapOf<String, hw_structvar>()
    var fifo_ins = ArrayList<hw_fifo_in>()
    var fifo_outs = ArrayList<hw_fifo_out>()

    var copipe_ifs = mutableMapOf<String, hw_copipe>()
    var mcopipe_ifs = ArrayList<hw_mcopipe_if>()
    var scopipe_ifs = ArrayList<hw_scopipe_if>()

    var copipe_handles = mutableMapOf<String, hw_copipe>()
    var mcopipe_handles = ArrayList<hw_mcopipe_handle>()
    var scopipe_handles = ArrayList<hw_scopipe_handle>()

    var Stages  = mutableMapOf<String, hw_stage>()

    fun add_stage(name_in : String) : hw_stage {
        var new_stage = hw_stage(name_in, this)
        if (Stages.put(new_stage.name, new_stage) != null) {
            ERROR("Stage addition problem!")
        }
        return new_stage
    }

    fun begstage(stage : hw_stage) {
        if (this.size != 0) ERROR("Pipex ASTC inconsistent!")
        // TODO: validate stage presence
        add(stage)
    }

    fun endstage() {
        if (this.size != 1) ERROR("Stage ASTC inconsistent!")
        if (this[0].opcode != OP_STAGE) ERROR("Stage ASTC inconsistent!")
        this.clear()
    }

    class fifo_out_descr(
        val ext_req: rtl.hw_port,
        val ext_wdata: rtl.hw_port,
        val ext_ack: rtl.hw_port,
        var reqbuf_req: rtl.hw_sticky,
        var reqbuf_wdata: rtl.hw_sticky
    )

    class fifo_in_descr(
        val ext_req: rtl.hw_port,
        val ext_rdata: rtl.hw_port,
        val ext_ack: rtl.hw_port,
        var buf_req: hw_var,
        var buf_rdata: hw_var
    )

    var var_dict = mutableMapOf<hw_var, hw_var>()
    var fifo_out_dict = mutableMapOf<hw_fifo_out, fifo_out_descr>()
    var fifo_in_dict = mutableMapOf<hw_fifo_in, fifo_in_descr>()

    fun add_if_struct(new_struct: hw_struct) {
        hw_if_structs.add(new_struct)
        if (hw_structs.put(new_struct.name, new_struct) != null) {
            ERROR("Struct addition problem!")
        }
    }

    fun add_if_struct(new_struct_name : String) : hw_struct {
        var new_struct = hw_struct(new_struct_name)
        add_if_struct(new_struct)
        return new_struct
    }

    fun add_int_struct(new_struct: hw_struct) {
        hw_int_structs.add(new_struct)
        if (hw_structs.put(new_struct.name, new_struct) != null) {
            ERROR("Struct addition problem!")
        }
    }

    fun add_int_struct(new_struct_name : String) : hw_struct {
        var new_struct = hw_struct(new_struct_name)
        add_int_struct(new_struct)
        return new_struct
    }

    private fun add_local(new_local: hw_local) {
        if (wrvars.put(new_local.name, new_local) != null) {
            ERROR("local addition problem!")
        }
        if (rdvars.put(new_local.name, new_local) != null) {
            ERROR("local addition problem!")
        }
        locals.add(new_local)
        new_local.default_astc = this
    }

    fun local(name: String, VarType: VAR_TYPE, src_struct_in: hw_struct, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, VarType, src_struct_in, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, VarType, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, VarType, msb, lsb, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, VarType: VAR_TYPE, defval: String): hw_local {
        var ret_var = hw_local(name, VarType, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_local {
        var ret_var = hw_local(name, src_struct_in, dimensions)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct_in: hw_struct): hw_local {
        var ret_var = hw_local(name, src_struct_in)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defval: String): hw_local {
        var ret_var = hw_local(name, VAR_TYPE.UNSIGNED, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, VAR_TYPE.SIGNED, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, defval: String): hw_local {
        var ret_var = hw_local(name, VAR_TYPE.SIGNED, defval)
        add_local(ret_var)
        return ret_var
    }

    private fun add_local_sticky(new_local_sticky: hw_local_sticky) {
        if (wrvars.put(new_local_sticky.name, new_local_sticky) != null) {
            ERROR("local sticky addition problem!")
        }
        if (rdvars.put(new_local_sticky.name, new_local_sticky) != null) {
            ERROR("local sticky addition problem!")
        }
        locals.add(new_local_sticky)
        new_local_sticky.default_astc = this
    }

    fun local_sticky(name: String, VarType: VAR_TYPE, src_struct_in: hw_struct, dimensions: hw_dim_static, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, VarType, src_struct_in, dimensions, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, VarType, dimensions, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, VarType, msb, lsb, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, VarType: VAR_TYPE, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, VarType, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_local_sticky {
        var ret_var = hw_local_sticky(name, src_struct_in, dimensions)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, src_struct_in: hw_struct): hw_local_sticky {
        var ret_var = hw_local_sticky(name, src_struct_in)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, dimensions: hw_dim_static, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, msb: Int, lsb: Int, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, VAR_TYPE.UNSIGNED, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, dimensions: hw_dim_static, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, VAR_TYPE.SIGNED, dimensions, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, msb: Int, lsb: Int, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, VAR_TYPE.SIGNED, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun add_global(new_global: hw_global) {
        if (wrvars.put(new_global.name, new_global) != null) {
            ERROR("global addition problem!")
        }
        if (rdvars.put(new_global.name, new_global) != null) {
            ERROR("global addition problem!")
        }
        globals.add(new_global)
        new_global.default_astc = this
    }

    fun global(name: String, VarType: VAR_TYPE, src_struct_in: hw_struct, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, VarType, src_struct_in, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, VarType, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, VarType, msb, lsb, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, VarType: VAR_TYPE, defval: String): hw_global {
        var ret_var = hw_global(name, VarType, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_global {
        var ret_var = hw_global(name, src_struct_in, dimensions)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct_in: hw_struct): hw_global {
        var ret_var = hw_global(name, src_struct_in)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defval: String): hw_global {
        var ret_var = hw_global(name, VAR_TYPE.UNSIGNED, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, VAR_TYPE.SIGNED, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defval: String): hw_global {
        var ret_var = hw_global(name, VAR_TYPE.SIGNED, defval)
        add_global(ret_var)
        return ret_var
    }

    fun add_port(new_port: hw_port) {
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

    fun port(
        name: String,
        port_dir: PORT_DIR,
        VarType: VAR_TYPE,
        dimensions: hw_dim_static,
        defval: String
    ): hw_port {
        var ret_var = hw_port(name, port_dir, VarType, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun port(name: String, port_dir: PORT_DIR, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, VarType, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun port(name: String, port_dir: PORT_DIR, VarType: VAR_TYPE, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, VarType, defval)
        add_port(ret_var)
        return ret_var
    }

    fun port(
        name: String,
        port_dir: PORT_DIR,
        src_struct_in: hw_struct,
        dimensions: hw_dim_static
    ): hw_port {
        var ret_var = hw_port(name, port_dir, src_struct_in, dimensions)
        add_port(ret_var)
        return ret_var
    }

    fun port(name: String, port_dir: PORT_DIR, src_struct_in: hw_struct): hw_port {
        var ret_var = hw_port(name, port_dir, src_struct_in)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name: String, port_dir: PORT_DIR, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name: String, port_dir: PORT_DIR, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name: String, port_dir: PORT_DIR, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.UNSIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name: String, port_dir: PORT_DIR, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.SIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name: String, port_dir: PORT_DIR, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name: String, port_dir: PORT_DIR, defval: String): hw_port {
        var ret_var = hw_port(name, port_dir, VAR_TYPE.SIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VarType, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VarType, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name: String, VarType: VAR_TYPE, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VarType, defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, src_struct_in, dimensions)
        add_port(ret_var)
        return ret_var
    }

    fun input(name: String, src_struct_in: hw_struct): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, src_struct_in)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name: String, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name: String, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name: String, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.UNSIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name: String, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.SIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name: String, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name: String, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, VAR_TYPE.SIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VarType, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VarType, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name: String, VarType: VAR_TYPE, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VarType, defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, src_struct_in, dimensions)
        add_port(ret_var)
        return ret_var
    }

    fun output(name: String, src_struct_in: hw_struct): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, src_struct_in)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name: String, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name: String, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name: String, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.UNSIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name: String, dimensions: hw_dim_static, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.SIGNED, dimensions, defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name: String, msb: Int, lsb: Int, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name: String, defval: String): hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, VAR_TYPE.SIGNED, defval)
        add_port(ret_var)
        return ret_var
    }

    fun add_fifo_in(new_fifo_in: hw_fifo_in) {
        if (fifo_ifs.put(new_fifo_in.name, new_fifo_in) != null) {
            ERROR("FIFO addition problem!")
        }
        fifo_ins.add(new_fifo_in)
    }

    fun fifo_in(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static): hw_fifo_in {
        var ret_var = hw_fifo_in(name, VarType, dimensions)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int): hw_fifo_in {
        var ret_var = hw_fifo_in(name, VarType, msb, lsb)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_fifo_in {
        var ret_var = hw_fifo_in(name, src_struct_in, dimensions)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name: String, src_struct_in: hw_struct): hw_fifo_in {
        var ret_var = hw_fifo_in(name, src_struct_in)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun ufifo_in(name: String, dimensions: hw_dim_static): hw_fifo_in {
        var ret_var = hw_fifo_in(name, VAR_TYPE.UNSIGNED, dimensions)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun ufifo_in(name: String, msb: Int, lsb: Int): hw_fifo_in {
        var ret_var = hw_fifo_in(name, VAR_TYPE.UNSIGNED, msb, lsb)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun sfifo_in(name: String, dimensions: hw_dim_static): hw_fifo_in {
        var ret_var = hw_fifo_in(name, VAR_TYPE.SIGNED, dimensions)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun sfifo_in(name: String, msb: Int, lsb: Int): hw_fifo_in {
        var ret_var = hw_fifo_in(name, VAR_TYPE.SIGNED, msb, lsb)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun add_fifo_out(new_fifo_out: hw_fifo_out) {
        if (fifo_ifs.put(new_fifo_out.name, new_fifo_out) != null) {
            ERROR("FIFO addition problem!")
        }
        fifo_outs.add(new_fifo_out)
    }

    fun fifo_out(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static): hw_fifo_out {
        var ret_var = hw_fifo_out(name, VarType, dimensions)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int): hw_fifo_out {
        var ret_var = hw_fifo_out(name, VarType, msb, lsb)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_fifo_out {
        var ret_var = hw_fifo_out(name, src_struct_in, dimensions)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name: String, src_struct_in: hw_struct): hw_fifo_out {
        var ret_var = hw_fifo_out(name, src_struct_in)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun ufifo_out(name: String, dimensions: hw_dim_static): hw_fifo_out {
        var ret_var = hw_fifo_out(name, VAR_TYPE.UNSIGNED, dimensions)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun ufifo_out(name: String, msb: Int, lsb: Int): hw_fifo_out {
        var ret_var = hw_fifo_out(name, VAR_TYPE.UNSIGNED, msb, lsb)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun sfifo_out(name: String, dimensions: hw_dim_static): hw_fifo_out {
        var ret_var = hw_fifo_out(name, VAR_TYPE.SIGNED, dimensions)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun sfifo_out(name: String, msb: Int, lsb: Int): hw_fifo_out {
        var ret_var = hw_fifo_out(name, VAR_TYPE.SIGNED, msb, lsb)
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

    private fun add_mcopipe_if(new_mcopipe: hw_mcopipe_if) {
        if (copipe_ifs.put(new_mcopipe.name, new_mcopipe) != null) {
            ERROR("Mcopipe addition problem!")
        }
        mcopipe_ifs.add(new_mcopipe)
    }

    fun mcopipe_if(name: String,
                   wdata_VarType: VAR_TYPE,
                   wdata_src_struct: hw_struct,
                   wdata_dim: hw_dim_static,
                   rdata_VarType: VAR_TYPE,
                   rdata_src_struct: hw_struct,
                   rdata_dim: hw_dim_static): hw_mcopipe_if {
        var new_mcopipe = hw_mcopipe_if(this,
            name,
            wdata_VarType,
            wdata_src_struct,
            wdata_dim,
            rdata_VarType,
            rdata_src_struct,
            rdata_dim)
        add_mcopipe_if(new_mcopipe)
        return new_mcopipe
    }

    private fun add_mcopipe_handle(new_mcopipe: hw_mcopipe_handle) {
        if (copipe_handles.put(new_mcopipe.name, new_mcopipe) != null) {
            ERROR("Mcopipe addition problem!")
        }
        mcopipe_handles.add(new_mcopipe)
    }

    fun mcopipe_handle(name: String,
                   wdata_VarType: VAR_TYPE,
                   wdata_src_struct: hw_struct,
                   wdata_dim: hw_dim_static,
                   rdata_VarType: VAR_TYPE,
                   rdata_src_struct: hw_struct,
                   rdata_dim: hw_dim_static): hw_mcopipe_handle {
        var new_mcopipe = hw_mcopipe_handle(this,
            name,
            wdata_VarType,
            wdata_src_struct,
            wdata_dim,
            rdata_VarType,
            rdata_src_struct,
            rdata_dim)
        add_mcopipe_handle(new_mcopipe)
        return new_mcopipe
    }

    fun mcopipe_handle(mcopipe: hw_mcopipe_if): hw_mcopipe_handle {
        var new_mcopipe = hw_mcopipe_handle(mcopipe)
        add_mcopipe_handle(new_mcopipe)
        return new_mcopipe
    }

    private fun add_scopipe_if(new_scopipe: hw_scopipe_if) {
        if (copipe_ifs.put(new_scopipe.name, new_scopipe) != null) {
            ERROR("Scopipe addition problem!")
        }
        scopipe_ifs.add(new_scopipe)
    }

    fun scopipe_if(name: String,
                   wdata_VarType: VAR_TYPE,
                   wdata_src_struct: hw_struct,
                   wdata_dim: hw_dim_static,
                   rdata_VarType: VAR_TYPE,
                   rdata_src_struct: hw_struct,
                   rdata_dim: hw_dim_static): hw_scopipe_if {
        var new_scopipe = hw_scopipe_if(this,
            name,
            wdata_VarType,
            wdata_src_struct,
            wdata_dim,
            rdata_VarType,
            rdata_src_struct,
            rdata_dim
        )
        add_scopipe_if(new_scopipe)
        return new_scopipe
    }

    private fun add_scopipe_handle(new_scopipe: hw_scopipe_handle) {
        if (copipe_handles.put(new_scopipe.name, new_scopipe) != null) {
            ERROR("Scopipe addition problem!")
        }
        scopipe_handles.add(new_scopipe)
    }

    fun scopipe_handle(name: String,
                       wdata_VarType: VAR_TYPE,
                       wdata_src_struct: hw_struct,
                       wdata_dim: hw_dim_static,
                       rdata_VarType: VAR_TYPE,
                       rdata_src_struct: hw_struct,
                       rdata_dim: hw_dim_static): hw_scopipe_handle {
        var new_scopipe = hw_scopipe_handle(this,
            name,
            wdata_VarType,
            wdata_src_struct,
            wdata_dim,
            rdata_VarType,
            rdata_src_struct,
            rdata_dim)
        add_scopipe_handle(new_scopipe)
        return new_scopipe
    }

    fun scopipe_handle(scopipe: hw_scopipe_if): hw_scopipe_handle {
        var new_scopipe = hw_scopipe_handle(scopipe)
        add_scopipe_handle(new_scopipe)
        return new_scopipe
    }

    fun readremote(remote_stage : hw_stage, remote_local: hw_pipex_var) : hw_var {
        var new_expr = hw_exec_read_remote(remote_stage, remote_local)
        var genvar = hw_var(GetGenName("var"), remote_local.VarType, remote_local.src_struct, remote_local.dimensions, "0")
        remote_stage.AddRdVar(remote_local)
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun isactive(remote_stage : hw_stage) : hw_var {
        var new_expr = hw_exec_stage_stat(remote_stage, OP_ISACTIVE)
        var genvar = hw_var(GetGenName("var"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun isworking(remote_stage : hw_stage) : hw_var {
        var new_expr = hw_exec_stage_stat(remote_stage, OP_ISWORKING)
        var genvar = hw_var(GetGenName("var"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun isstalled(remote_stage : hw_stage) : hw_var {
        var new_expr = hw_exec_stage_stat(remote_stage, OP_ISSTALLED)
        var genvar = hw_var(GetGenName("var"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun issucc(remote_stage : hw_stage) : hw_var {
        var new_expr = hw_exec_stage_stat(remote_stage, OP_ISSUCC)
        var genvar = hw_var(GetGenName("var"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddWrVar(genvar)
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

    fun assign_succ(depow_fractions: hw_fractions, tgt : hw_pipex_var, src: hw_param) {
        var new_expr = hw_exec(OP_ASSIGN_SUCC)
        new_expr.AddWrVar(tgt)
        new_expr.AddRdParam(src)
        new_expr.fractions = depow_fractions
        AddExpr(new_expr)
    }

    fun assign_succ(depow_fractions: hw_fractions, tgt : hw_pipex_var, src: Int) {
        assign_succ(depow_fractions, tgt, hw_imm(src))
    }

    fun assign_succ(tgt : hw_pipex_var, src: hw_param) {
        var dummy_depow_fractions = hw_fractions()
        assign_succ(dummy_depow_fractions, tgt, src)
    }

    fun assign_succ(tgt : hw_pipex_var, src: Int) {
        assign_succ(tgt, hw_imm(src))
    }

    fun accum(depow_fractions: hw_fractions, tgt : hw_pipex_var, src: hw_param) {
        var new_expr = hw_exec(OP_ACCUM)
        new_expr.AddWrVar(tgt)
        new_expr.AddRdParam(src)
        new_expr.fractions = depow_fractions
        AddExpr(new_expr)
    }

    fun accum(depow_fractions: hw_fractions, tgt : hw_pipex_var, src: Int) {
        accum(depow_fractions, tgt, hw_imm(src))
    }

    fun accum(tgt : hw_pipex_var, src: hw_param) {
        var dummy_depow_fractions = hw_fractions()
        accum(dummy_depow_fractions, tgt, src)
    }

    fun accum(tgt : hw_pipex_var, src: Int) {
        accum(tgt, hw_imm(src))
    }

    fun readprev(src : hw_pipex_var) : hw_pipex_var {
        var new_expr = hw_exec(OP_RD_PREV)
        var genvar = hw_pipex_var(GetGenName("readprev"), src.VarType, src.dimensions, src.defval)
        genvar.default_astc = this
        new_expr.AddRdVar(src)
        new_expr.AddGenVar(genvar)
        new_expr.AddWrVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun mcopipe_req(mcopipe_if : hw_mcopipe_if, mcopipe_handle : hw_mcopipe_handle, cmd : hw_param, wdata : hw_param) : hw_var {
        var new_expr = hw_exec_mcopipe_req(mcopipe_if, mcopipe_handle)
        var genvar = hw_var(GetGenName("mcopipe_req_rdy"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddRdParam(cmd)
        new_expr.AddRdParam(wdata)
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun mcopipe_resp(mcopipe_handle : hw_mcopipe_handle, rdata : hw_var) : hw_var {
        var new_expr = hw_exec_mcopipe_resp(mcopipe_handle)
        var genvar = hw_var(GetGenName("mcopipe_resp_rdy"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        new_expr.AddWrVar(rdata)
        AddExpr(new_expr)
        return genvar
    }

    fun scopipe_req(scopipe_if : hw_scopipe_if, scopipe_handle : hw_scopipe_handle, cmd : hw_var, rdata : hw_var) : hw_var {
        var new_expr = hw_exec_scopipe_req(scopipe_if, scopipe_handle)
        var genvar = hw_var(GetGenName("scopipe_req_rdy"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddWrVar(cmd)
        new_expr.AddWrVar(rdata)
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun scopipe_resp(scopipe_handle : hw_scopipe_handle, wdata : hw_param) : hw_var {
        var new_expr = hw_exec_scopipe_resp(scopipe_handle)
        var genvar = hw_var(GetGenName("scopipe_resp_rdy"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddRdParam(wdata)
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun ProcessSyncOp(expression : hw_exec, Translate_info : __TranslateInfo, pstage_info : __pstage_info, cyclix_gen : cyclix.module) {
        if ((expression.opcode == OP1_IF) || (expression.opcode == OP1_WHILE)) {
            for (subexpression in expression.expressions) ProcessSyncOp(subexpression, Translate_info, pstage_info, cyclix_gen)

        } else if (expression.opcode == OP1_ASSIGN) {
            if (expression.wrvars[0] is hw_global) {
                if (!pstage_info.global_tgts.contains(expression.wrvars[0] as hw_global))
                    pstage_info.global_tgts.add(expression.wrvars[0] as hw_global)
            }

        } else if (expression.opcode == OP_ASSIGN_SUCC) {
            if (!pstage_info.assign_succ_assocs.containsKey(expression.wrvars[0])) {
                val req = cyclix_gen.ulocal(GetGenName("syncreq"), 0, 0, "0")
                val buf = cyclix_gen.local(GetGenName("syncbuf"), expression.wrvars[0].VarType, expression.wrvars[0].src_struct, expression.wrvars[0].dimensions, expression.wrvars[0].defval)
                pstage_info.assign_succ_assocs.put((expression.wrvars[0] as hw_pipex_var), __assign_succ_buf(req, buf))
            }

        } else if (expression.opcode == OP_ACCUM) {
            if (!pstage_info.accum_dict.contains(expression.wrvars[0] as hw_pipex_var))
                pstage_info.accum_dict.add(expression.wrvars[0] as hw_pipex_var)

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

    fun FillMcopipeReqDict(expression : hw_exec, TranslateInfo : __TranslateInfo) {
        if ((expression.opcode == OP1_IF) || (expression.opcode == OP1_WHILE)) {
            for (subexpression in expression.expressions) FillMcopipeReqDict(subexpression, TranslateInfo)
        } else if (expression.opcode == OP_MCOPIPE_REQ) {
            var mcopipe_ArrayList = TranslateInfo.__mcopipe_handle_reqdict[(expression as hw_exec_mcopipe_req).mcopipe_handle]!!
            if (!mcopipe_ArrayList.contains(expression.mcopipe_if)) mcopipe_ArrayList.add(expression.mcopipe_if)
        }
    }

    fun FillScopipeReqDict(expression : hw_exec, TranslateInfo : __TranslateInfo) {
        if ((expression.opcode == OP1_IF) || (expression.opcode == OP1_WHILE)) {
            for (subexpression in expression.expressions) FillScopipeReqDict(subexpression, TranslateInfo)
        } else if (expression.opcode == OP_SCOPIPE_REQ) {
            var scopipe_ArrayList = TranslateInfo.__scopipe_handle_reqdict[(expression as hw_exec_scopipe_req).scopipe_handle]!!
            if (!scopipe_ArrayList.contains(expression.scopipe_if)) scopipe_ArrayList.add(expression.scopipe_if)
        }
    }

    fun reconstruct_expression(DEBUG_FLAG : Boolean,
                               cyclix_gen : cyclix.module,
                               expr : hw_exec,
                               curStage : hw_stage,
                               TranslateInfo: __TranslateInfo,
                               curStageAssoc : __pstage_info) {

        var fractions = hw_fractions()
        for (src_fraction in expr.fractions) {
            if (src_fraction is hw_fraction_C) fractions.add(src_fraction)
            else if (src_fraction is hw_fraction_V) fractions.add(hw_fraction_V(curStageAssoc.TranslateVar(src_fraction.index)))
            else if (src_fraction is hw_fraction_CC) fractions.add(src_fraction)
            else if (src_fraction is hw_fraction_CV) fractions.add(hw_fraction_CV(src_fraction.msb, curStageAssoc.TranslateVar(src_fraction.lsb)))
            else if (src_fraction is hw_fraction_VC) fractions.add(hw_fraction_VC(curStageAssoc.TranslateVar(src_fraction.msb), src_fraction.lsb))
            else if (src_fraction is hw_fraction_VV) fractions.add(hw_fraction_VV(curStageAssoc.TranslateVar(src_fraction.msb), curStageAssoc.TranslateVar(src_fraction.lsb)))
            else if (src_fraction is hw_fraction_SubStruct) fractions.add(src_fraction)
            else ERROR("dimensions error")
        }

        if (DEBUG_FLAG) {
            MSG(DEBUG_FLAG, "Reconstructing expression: " + expr.opcode.default_string)
            for (param in expr.params) MSG(DEBUG_FLAG, "param: " + param.GetString())
        }

        if ((expr.opcode == OP1_ASSIGN)) {
            
            if (expr.wrvars[0] is hw_global) {
                cyclix_gen.begif(!curStageAssoc.pctrl_stalled_glbl)
                run {
                    cyclix_gen.begif(curStageAssoc.pctrl_active_glbl)
                    run {
                        cyclix_gen.assign(fractions, curStageAssoc.TranslateVar(expr.wrvars[0]), curStageAssoc.TranslateParam(expr.params[0]))
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
            } else {
                cyclix_gen.assign(fractions, curStageAssoc.TranslateVar(expr.wrvars[0]), curStageAssoc.TranslateParam(expr.params[0]))
            }

        } else if ((expr.opcode == OP2_ARITH_ADD)
            || (expr.opcode == OP2_ARITH_SUB)
            || (expr.opcode == OP2_ARITH_MUL)
            || (expr.opcode == OP2_ARITH_DIV)
            || (expr.opcode == OP2_ARITH_SHL)
            || (expr.opcode == OP2_ARITH_SHR)
            || (expr.opcode == OP2_ARITH_SRA)

            || (expr.opcode == OP1_LOGICAL_NOT)
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

            || (expr.opcode == OP1_COMPLEMENT)
            || (expr.opcode == OP1_BITWISE_NOT)
            || (expr.opcode == OP2_BITWISE_AND)
            || (expr.opcode == OP2_BITWISE_OR)
            || (expr.opcode == OP2_BITWISE_XOR)
            || (expr.opcode == OP2_BITWISE_XNOR)

            || (expr.opcode == OP1_REDUCT_AND)
            || (expr.opcode == OP1_REDUCT_NAND)
            || (expr.opcode == OP1_REDUCT_OR)
            || (expr.opcode == OP1_REDUCT_NOR)
            || (expr.opcode == OP1_REDUCT_XOR)
            || (expr.opcode == OP1_REDUCT_XNOR)

            || (expr.opcode == OP2_INDEXED)
            || (expr.opcode == OP3_RANGED)
            || (expr.opcode == OPS_CNCT)) {

            cyclix_gen.AddExpr_op_gen(expr.opcode, curStageAssoc.TranslateVar(expr.wrvars[0]), curStageAssoc.TranslateParams(expr.params))

        } else if (expr.opcode == OP1_IF) {

            cyclix_gen.begif(curStageAssoc.TranslateParam(expr.params[0]))
            run {
                for (subexpr in expr.expressions) {
                    reconstruct_expression(DEBUG_FLAG,
                        cyclix_gen,
                        subexpr,
                        curStage,
                        TranslateInfo,
                        curStageAssoc)
                }
            }; cyclix_gen.endif()

        } else if (expr.opcode == OP1_WHILE) {

            cyclix_gen.begwhile(curStageAssoc.TranslateParam(expr.params[0]))
            run {
                for (subexpr in expr.expressions) {
                    reconstruct_expression(DEBUG_FLAG,
                        cyclix_gen,
                        subexpr,
                        curStage,
                        TranslateInfo,
                        curStageAssoc)
                }
            }; cyclix_gen.endwhile()

        } else if (expr.opcode == OP_ACCUM) {

            cyclix_gen.assign(curStageAssoc.TranslateVar(expr.wrvars[0]), curStageAssoc.TranslateParam(expr.params[0]))
            cyclix_gen.assign(curStageAssoc.pContext_srcglbl_dict[expr.wrvars[0]]!!, curStageAssoc.TranslateParam(expr.params[0]))

        } else if (expr.opcode == OP_PKILL) {
            curStageAssoc.pkill_cmd_internal(cyclix_gen)

        } else if (expr.opcode == OP_PSTALL) {
            curStageAssoc.pstall_ifactive_cmd(cyclix_gen)

        } else if (expr.opcode == OP_PFLUSH) {
            curStageAssoc.pflush_cmd_internal(cyclix_gen)

        } else if (expr.opcode == OP_RD_PREV) {
            cyclix_gen.assign(fractions, curStageAssoc.TranslateVar(expr.wrvars[0]), TranslateInfo.__global_assocs[expr.rdvars[0]]!!.cyclix_global_buf)

        } else if (expr.opcode == OP_ASSIGN_SUCC) {
            cyclix_gen.assign(curStageAssoc.assign_succ_assocs[expr.wrvars[0]]!!.req, 1)
            cyclix_gen.assign(fractions, curStageAssoc.assign_succ_assocs[expr.wrvars[0]]!!.buf, curStageAssoc.TranslateParam(expr.params[0]))

        } else if (expr.opcode == OP_RD_REMOTE) {
            cyclix_gen.assign(fractions, curStageAssoc.TranslateVar(expr.wrvars[0]), TranslateInfo.__stage_assocs[(expr as hw_exec_read_remote).stage]!!.TranslateVar(expr.remote_var))

        } else if (expr.opcode == OP_ISACTIVE) {
            cyclix_gen.assign(curStageAssoc.TranslateVar(expr.wrvars[0]), TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.pctrl_active_glbl)

        } else if (expr.opcode == OP_ISWORKING) {
            cyclix_gen.assign(curStageAssoc.TranslateVar(expr.wrvars[0]), TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.pctrl_working)

        } else if (expr.opcode == OP_ISSTALLED) {
            cyclix_gen.assign(curStageAssoc.TranslateVar(expr.wrvars[0]), TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.pctrl_stalled_glbl)

        } else if (expr.opcode == OP_ISSUCC) {
            cyclix_gen.assign(curStageAssoc.TranslateVar(expr.wrvars[0]), TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.pctrl_succ)

        } else if (expr.opcode == OP_ISKILLED) {
            cyclix_gen.assign(curStageAssoc.TranslateVar(expr.wrvars[0]), TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.pctrl_killed_glbl)

        } else if (expr.opcode == OP_ISFINISHED) {
            cyclix_gen.assign(curStageAssoc.TranslateVar(expr.wrvars[0]), TranslateInfo.__stage_assocs[(expr as hw_exec_stage_stat).stage]!!.pctrl_finish)

        } else if (expr.opcode == OP_MCOPIPE_REQ) {

            var mcopipe_if              = (expr as hw_exec_mcopipe_req).mcopipe_if
            var mcopipe_if_assoc        = TranslateInfo.__mcopipe_if_assocs[mcopipe_if]!!
            var mcopipe_handle          = expr.mcopipe_handle
            var mcopipe_handle_assoc    = TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!

            var rdreq_pending_translated    = curStageAssoc.TranslateVar(mcopipe_handle_assoc.rdreq_pending)
            var tid_translated              = curStageAssoc.TranslateVar(mcopipe_handle_assoc.tid)
            var if_id_translated            = curStageAssoc.TranslateVar(mcopipe_handle_assoc.if_id)

            cyclix_gen.begif(!curStageAssoc.pctrl_stalled_glbl)
            run {

                cyclix_gen.begif(curStageAssoc.pctrl_active_glbl)
                run {

                    cyclix_gen.begif(mcopipe_if_assoc.full_flag)
                    run {
                        cyclix_gen.assign(curStageAssoc.mcopipe_stallreq_unblk, 1)
                    }; cyclix_gen.endif()

                    cyclix_gen.begelse()
                    run {

                        // translating params
                        var cmd_translated      = curStageAssoc.TranslateParam(expr.params[0])
                        var wdata_translated    = curStageAssoc.TranslateParam(expr.params[1])

                        // finding id
                        var handle_id = TranslateInfo.__mcopipe_handle_reqdict[mcopipe_handle]!!.indexOf(expr.mcopipe_if)

                        var req_struct = cyclix_gen.local(GetGenName("req_struct"),
                            mcopipe_if_assoc.req_fifo.VarType,
                            mcopipe_if_assoc.req_fifo.src_struct,
                            mcopipe_if_assoc.req_fifo.dimensions,
                            mcopipe_if_assoc.req_fifo.defval)

                        cyclix_gen.assign(hw_fractions("we"), req_struct, cmd_translated)
                        cyclix_gen.assign(hw_fractions("wdata"), req_struct, wdata_translated)

                        cyclix_gen.begif(cyclix_gen.fifo_wr(mcopipe_if_assoc.req_fifo, req_struct))
                        run {

                            // req management
                            cyclix_gen.assign(curStageAssoc.TranslateVar(expr.wrvars[0]), 1)
                            cyclix_gen.assign(rdreq_pending_translated, cyclix_gen.lnot(cmd_translated))
                            cyclix_gen.assign(tid_translated, mcopipe_if_assoc.wr_ptr)
                            cyclix_gen.assign(if_id_translated, hw_imm(mcopipe_handle_assoc.if_id.dimensions, handle_id.toString()))

                            // mcopipe fifo management
                            cyclix_gen.begif(rdreq_pending_translated)
                            run {
                                cyclix_gen.assign(mcopipe_if_assoc.wr_ptr, mcopipe_if_assoc.wr_ptr_next)
                                cyclix_gen.assign(mcopipe_if_assoc.empty_flag, 0)

                                cyclix_gen.begif(cyclix_gen.eq2(mcopipe_if_assoc.rd_ptr, mcopipe_if_assoc.wr_ptr))
                                run {
                                    cyclix_gen.assign(mcopipe_if_assoc.full_flag, 1)
                                }; cyclix_gen.endif()

                            }; cyclix_gen.endif()

                        }; cyclix_gen.endif()
                        cyclix_gen.begelse()
                        run {
                            cyclix_gen.assign(curStageAssoc.mcopipe_stallreq_unblk, 1)
                        }; cyclix_gen.endif()

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()

            }; cyclix_gen.endif()

        } else if (expr.opcode == OP_MCOPIPE_RESP) {

            var mcopipe_handle          = (expr as hw_exec_mcopipe_resp).mcopipe_handle
            var mcopipe_handle_assoc    = TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!

            var resp_done_translated    = curStageAssoc.TranslateVar(mcopipe_handle_assoc.resp_done)
            var rdata_translated        = curStageAssoc.TranslateVar(mcopipe_handle_assoc.rdata)

            cyclix_gen.begif(resp_done_translated)
            run {
                cyclix_gen.assign(curStageAssoc.TranslateVar(expr.wrvars[1]), rdata_translated)
            }; cyclix_gen.endif()
            cyclix_gen.begelse()
            run {
                curStageAssoc.pstall_ifactive_cmd(cyclix_gen)
            }; cyclix_gen.endif()

            cyclix_gen.assign(curStageAssoc.TranslateVar(expr.wrvars[0]), resp_done_translated)

        } else if (expr.opcode == OP_SCOPIPE_REQ) {
            // TODO
            /*
            scopipe_if * cur_scopipe_if;
            if (cur_pmodule->GetScopipeIf(expression->string_params[0], &cur_scopipe_if) != true) return 1;

            scopipe_handle * cur_scopipe_handle;
            if (cur_pmodule->GetScopipeHandle(expression->string_params[1], &cur_scopipe_handle) != true) return 1;

            if (expr_begif_cmd(cur_pstage->pctrl_active_glbl) != 0) return 1;
            {
                ac_var * cmd_translated;
                if (cur_pstage->FetchVarWr(expression->wrvars[0], &cmd_translated) == false) return 1;
                ac_var * wdata_translated;
                if (cur_pstage->FetchVarWr(expression->wrvars[1], &wdata_translated) == false) return 1;

                if (expr_begif_cmd(cur_scopipe_if->req_var) != 0) return 1;
                {
                    // TODO
                    ac_var* rdy_translated;
                    if (cur_pstage->FetchVarWr(expression->wrvars[2], &rdy_translated) == false) return 1;
                    if (expr_assign_cmd(rdy_translated, new ac_imm(1, "1")) != 0) return 1;

                    ac_var* nwe_var;
                    if (expr_1op_cmd(OP1_LOGICAL_NOT, &nwe_var, cur_scopipe_if->we_var) != 0) return 1;
                    if (expr_assign_cmd(cur_scopipe_handle->rdreq_pending, nwe_var) != 0) return 1;
                    if (expr_assign_cmd(cur_scopipe_handle->wdata, cur_scopipe_if->wdata_var) != 0) return 1;
                    if (expr_assign_cmd(cur_scopipe_if->ack_var, new ac_imm(1, "1")) != 0) return 1;
                }
                if (expr_endif_cmd() != 0) return 1;

                if (expr_assign_cmd(cmd_translated, cur_scopipe_if->we_var) != 0) return 1;
                if (expr_assign_cmd(wdata_translated, cur_scopipe_if->wdata_var) != 0) return 1;
            }
            if (expr_endif_cmd() != 0) return 1;

        } else if (expr.opcode == OP_SCOPIPE_RESP) {
            scopipe_handle * cur_scopipe_handle;
            if (cur_pmodule->GetScopipeHandle(expression->string_params[0], &cur_scopipe_handle) != true) return 1;

            if (expr_begifnot_cmd(cur_pstage->pctrl_stalled_glbl) != 0) return 1;
            {
                if (expr_begif_cmd(cur_pstage->pctrl_active_glbl) != 0) return 1;
                {
                    if (expr_begifnot_cmd(cur_scopipe_handle->resp_done) != 0) return 1;
                    {
                        ac_param* rdata_translated;
                        if (cur_pstage->TranslateParam(expression->params[0], &rdata_translated) == false) return 1;

                        if (expr_assign_cmd(cur_scopipe_handle->scopipe->resp_var, new ac_imm(1, "1")) != 0) return 1;
                        if (expr_assign_cmd(cur_scopipe_handle->scopipe->rdata_var, rdata_translated) != 0) return 1;
                        if (expr_assign_cmd(cur_scopipe_handle->resp_done, new ac_imm(1, "1")) != 0) return 1;
                    }
                    if (expr_endif_cmd() != 0) return 1;
                }
                if (expr_endif_cmd() != 0) return 1;
            }
            if (expr_endif_cmd() != 0) return 1;
        */
        } else {
            ERROR("Reconstruction of expression failed: opcode undefined: " + expr.opcode.default_string)
        }
    }

    fun translate_to_cyclix(DEBUG_FLAG : Boolean) : cyclix.module {

        MSG("Translating to cyclix: beginning")

        var cyclix_gen = cyclix.module(name)
        var TranslateInfo = __TranslateInfo()

        MSG(DEBUG_FLAG, "Processing structs")
        for (if_struct in hw_if_structs) {
            cyclix_gen.add_if_struct(if_struct)
        }
        for (int_struct in hw_int_structs) {
            cyclix_gen.add_int_struct(int_struct)
        }

        MSG(DEBUG_FLAG, "Processing globals")
        for (global in globals) {
            var new_global = cyclix_gen.global(("genpsticky_glbl_" + global.name), global.VarType, global.src_struct, global.dimensions, global.defval)
            var new_global_buf = cyclix_gen.local(("genpsticky_glbl_" + global.name + "_buf"), global.VarType, global.src_struct, global.dimensions, global.defval)
            TranslateInfo.__global_assocs.put(global, __global_info(new_global, new_global_buf))
        }

        // Generating mcopipes' resources //
        MSG(DEBUG_FLAG, "Generating mcopipes if resources")
        for (mcopipe_if in mcopipe_ifs) {
            val mcopipe_name_prefix = "genmcopipe_" + mcopipe_if.name + "_"

            var full_flag   = cyclix_gen.uglobal((mcopipe_name_prefix + "full_flag"), 0, 0, "0")
            var empty_flag  = cyclix_gen.uglobal((mcopipe_name_prefix + "empty_flag"), 0, 0, "1")
            var wr_ptr      = cyclix_gen.uglobal((mcopipe_name_prefix + "wr_ptr"), (COPIPE_TRX_ID_WIDTH-1), 0, "0")
            var rd_ptr      = cyclix_gen.uglobal((mcopipe_name_prefix + "rd_ptr"), (COPIPE_TRX_ID_WIDTH-1), 0, "0")
            var wr_ptr_next = cyclix_gen.ulocal((mcopipe_name_prefix + "wr_ptr_next"), (COPIPE_TRX_ID_WIDTH-1), 0, "0")
            var rd_ptr_next = cyclix_gen.ulocal((mcopipe_name_prefix + "rd_ptr_next"), (COPIPE_TRX_ID_WIDTH-1), 0, "0")

            var wr_struct = cyclix_gen.add_if_struct("genpmodule_" + name + "_" + mcopipe_name_prefix + "genstruct_fifo_wdata")
            wr_struct.addu("we", 0, 0, "0")
            wr_struct.add("wdata", mcopipe_if.wdata_VarType, mcopipe_if.wdata_src_struct, mcopipe_if.wdata_dim, "0")

            var req_fifo = cyclix_gen.fifo_out((mcopipe_name_prefix + "req"), wr_struct)
            var resp_fifo = cyclix_gen.fifo_in((mcopipe_name_prefix + "resp"), mcopipe_if.rdata_VarType, mcopipe_if.rdata_src_struct, mcopipe_if.rdata_dim)

            TranslateInfo.__mcopipe_if_assocs.put(mcopipe_if, __mcopipe_if_info(
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
        MSG(DEBUG_FLAG, "Generating mcopipe handles' resources")
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

            var if_id           = ulocal_sticky((name_prefix + "if_id"), GetWidthToContain(TranslateInfo.__mcopipe_handle_reqdict[mcopipe_handle]!!.size)-1, 0, "0")
            var resp_done       = ulocal_sticky((name_prefix + "resp_done"), 0, 0, "0")
            var rdata           =  local_sticky((name_prefix + "rdata"), mcopipe_handle.rdata_VarType, mcopipe_handle.rdata_src_struct, mcopipe_handle.rdata_dim, "0")
            var rdreq_pending   = ulocal_sticky((name_prefix + "rdreq_pending"), 0, 0, "0")
            var tid             = ulocal_sticky((name_prefix + "tid"), (COPIPE_TRX_ID_WIDTH-1), 0, "0")

            TranslateInfo.__mcopipe_handle_assocs.put(mcopipe_handle, __mcopipe_handle_info(
                if_id,
                resp_done,
                rdata,
                rdreq_pending,
                tid))
        }

        // Generating scopipes' resources //
        MSG(DEBUG_FLAG, "Generating scopipes' resources")
        for (scopipe_if in scopipe_ifs) {
            val scopipe_name_prefix = "genscopipe_" + scopipe_if.name + "_"

            var rd_struct = cyclix_gen.add_if_struct("genpmodule_" + name + "_" + scopipe_name_prefix + "genstruct_fifo_rdata")
            rd_struct.addu("we", 0, 0, "0")
            rd_struct.add("rdata", scopipe_if.rdata_VarType, scopipe_if.rdata_dim, "0")

            var req_fifo = cyclix_gen.fifo_in((scopipe_name_prefix + "req"), rd_struct)
            var resp_fifo = cyclix_gen.fifo_out((scopipe_name_prefix + "resp"), scopipe_if.wdata_VarType, scopipe_if.wdata_dim)

            TranslateInfo.__scopipe_if_assocs.put(scopipe_if, __scopipe_if_info(
                req_fifo,
                resp_fifo))
        }

        // Generating scopipe handles' resources //
        MSG(DEBUG_FLAG, "Generating scopipe handles' resources")
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

            var if_id           = ulocal_sticky((name_prefix + "if_id"), GetWidthToContain(TranslateInfo.__scopipe_handle_reqdict[scopipe_handle]!!.size)-1, 0, "0")
            var wdata           =  local_sticky((name_prefix + "wdata"), scopipe_handle.wdata_VarType, scopipe_handle.wdata_src_struct, scopipe_handle.wdata_dim, "0")
            var we              = ulocal_sticky((name_prefix + "we"), 0, 0, "0")
            var resp_done       = ulocal_sticky((name_prefix + "resp_done"), 0, 0, "0")
            var rdreq_pending   = ulocal_sticky((name_prefix + "rdreq_pending"), 0, 0, "0")

            TranslateInfo.__scopipe_handle_assocs.put(scopipe_handle, __scopipe_handle_info(
                if_id,
                wdata,
                we,
                resp_done,
                rdreq_pending))
        }

        // Analyzing sync operations //
        MSG(DEBUG_FLAG, "Distributing synchronization primitives by pstages")
        for (stage in Stages) {
            val name_prefix = "genpstage_" + stage.value.name + "_"

            var pctrl_new           = cyclix_gen.ulocal((name_prefix + "genpctrl_new"), 0, 0, "0")
            var pctrl_working       = cyclix_gen.ulocal((name_prefix + "genpctrl_working"), 0, 0, "0")
            var pctrl_succ          = cyclix_gen.ulocal((name_prefix + "genpctrl_succ"), 0, 0, "0")
            var pctrl_occupied      = cyclix_gen.ulocal((name_prefix + "genpctrl_occupied"), 0, 0, "0")
            var pctrl_finish        = cyclix_gen.ulocal((name_prefix + "genpctrl_finish"), 0, 0, "0")
            var pctrl_flushreq      = cyclix_gen.ulocal((name_prefix + "genpctrl_flushreq"), 0, 0, "0")
            var pctrl_nevictable    = cyclix_gen.ulocal((name_prefix + "genpctrl_nevictable"), 0, 0, "0")

            var pctrl_active_glbl   = cyclix_gen.uglobal((name_prefix + "genpctrl_active_glbl"), 0, 0, "0")
            var pctrl_stalled_glbl  = cyclix_gen.uglobal((name_prefix + "genpctrl_stalled_glbl"), 0, 0, "0")
            var pctrl_killed_glbl   = cyclix_gen.uglobal((name_prefix + "genpctrl_killed_glbl"), 0, 0, "0")

            var mcopipe_stallreq_unblk = cyclix_gen.ulocal((name_prefix + "genmcopipe_stallreq_unblk"), 0, 0, "0")

            var pstage_info = __pstage_info(TranslateInfo,
                name_prefix,
                pctrl_new,
                pctrl_working,
                pctrl_succ,
                pctrl_occupied,
                pctrl_finish,
                pctrl_flushreq,
                pctrl_nevictable,
                pctrl_active_glbl,
                pctrl_stalled_glbl,
                pctrl_killed_glbl,
                mcopipe_stallreq_unblk)

            TranslateInfo.__stage_assocs.put(stage.value, pstage_info)
            for (expression in stage.value.expressions) {
                ProcessSyncOp(expression, TranslateInfo, pstage_info, cyclix_gen)
            }
        }

        // Generating resources //
        MSG(DEBUG_FLAG, "Generating resources")

        // Put stages info in ArrayLists
        var StageList = ArrayList<hw_stage>()
        for (stage in Stages) StageList.add(stage.value)
        var StageAssocList = ArrayList<__pstage_info>()
        for (stageAssoc in TranslateInfo.__stage_assocs) StageAssocList.add(stageAssoc.value)

        MSG(DEBUG_FLAG, "Processing genvars")
        for (CUR_STAGE_INDEX in 0 until StageList.size) {
            for (genvar in StageList[CUR_STAGE_INDEX].genvars) {
                var genvar_local = cyclix_gen.local(GetGenName("var"), genvar.VarType, genvar.src_struct, genvar.dimensions, genvar.defval)
                StageAssocList[CUR_STAGE_INDEX].pContext_local_dict.put(genvar, genvar_local)
            }
        }

        // Generate resources for (m/s)copipes
        for (CUR_STAGE_INDEX in 0 until StageList.size) {
            var pstage_info = TranslateInfo.__stage_assocs[StageList[CUR_STAGE_INDEX]]

            var prev_req_mcopipelist    = ArrayList<hw_mcopipe_handle>()
            var cur_req_mcopipelist     = ArrayList<hw_mcopipe_handle>()
            var cur_resp_mcopipelist    = ArrayList<hw_mcopipe_handle>()
            var next_resp_mcopipelist   = ArrayList<hw_mcopipe_handle>()

            var prev_req_scopipelist    = ArrayList<hw_scopipe_handle>()
            var cur_req_scopipelist     = ArrayList<hw_scopipe_handle>()
            var cur_resp_scopipelist    = ArrayList<hw_scopipe_handle>()
            var next_resp_scopipelist   = ArrayList<hw_scopipe_handle>()

            for (STAGE_INDEX_ANLZ in 0 until StageList.size) {
                if (STAGE_INDEX_ANLZ < CUR_STAGE_INDEX) {
                    prev_req_mcopipelist =
                            UniteArrayLists(prev_req_mcopipelist, StageAssocList[STAGE_INDEX_ANLZ].mcopipe_handle_reqs)
                    prev_req_scopipelist =
                            UniteArrayLists(prev_req_scopipelist, StageAssocList[STAGE_INDEX_ANLZ].scopipe_handle_reqs)

                } else if (STAGE_INDEX_ANLZ == CUR_STAGE_INDEX) {
                    cur_req_mcopipelist =
                            UniteArrayLists(cur_req_mcopipelist, StageAssocList[STAGE_INDEX_ANLZ].mcopipe_handle_reqs)
                    cur_resp_mcopipelist =
                            UniteArrayLists(cur_resp_mcopipelist, StageAssocList[STAGE_INDEX_ANLZ].mcopipe_handle_resps)

                    cur_req_scopipelist =
                            UniteArrayLists(cur_req_scopipelist, StageAssocList[STAGE_INDEX_ANLZ].scopipe_handle_reqs)
                    cur_resp_scopipelist =
                            UniteArrayLists(cur_resp_scopipelist, StageAssocList[STAGE_INDEX_ANLZ].scopipe_handle_resps)

                } else {
                    next_resp_mcopipelist = UniteArrayLists(
                        next_resp_mcopipelist,
                        StageAssocList[STAGE_INDEX_ANLZ].mcopipe_handle_resps
                    )
                    next_resp_scopipelist = UniteArrayLists(
                        next_resp_scopipelist,
                        StageAssocList[STAGE_INDEX_ANLZ].scopipe_handle_resps
                    )
                }
            }

            StageAssocList[CUR_STAGE_INDEX].mcopipe_handles = UniteArrayLists(UniteArrayLists(cur_req_mcopipelist, cur_resp_mcopipelist), CrossArrayLists(prev_req_mcopipelist, next_resp_mcopipelist))
            StageAssocList[CUR_STAGE_INDEX].scopipe_handles = UniteArrayLists(UniteArrayLists(cur_req_scopipelist, cur_resp_scopipelist), CrossArrayLists(prev_req_scopipelist, next_resp_scopipelist))

            for (mcopipe_handle in StageAssocList[CUR_STAGE_INDEX].mcopipe_handles) {
                var mcopipe_handle_info = TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle] as __mcopipe_handle_info

                StageList[CUR_STAGE_INDEX].AddRdVar(mcopipe_handle_info.if_id)
                StageList[CUR_STAGE_INDEX].AddRdVar(mcopipe_handle_info.rdreq_pending)
                StageList[CUR_STAGE_INDEX].AddRdVar(mcopipe_handle_info.tid)
                StageList[CUR_STAGE_INDEX].AddRdVar(mcopipe_handle_info.resp_done)
                StageList[CUR_STAGE_INDEX].AddRdVar(mcopipe_handle_info.rdata)

                StageList[CUR_STAGE_INDEX].AddWrVar(mcopipe_handle_info.if_id)
                StageList[CUR_STAGE_INDEX].AddWrVar(mcopipe_handle_info.rdreq_pending)
                StageList[CUR_STAGE_INDEX].AddWrVar(mcopipe_handle_info.tid)
                StageList[CUR_STAGE_INDEX].AddWrVar(mcopipe_handle_info.resp_done)
                StageList[CUR_STAGE_INDEX].AddWrVar(mcopipe_handle_info.rdata)
            }

            for (scopipe_handle in StageAssocList[CUR_STAGE_INDEX].scopipe_handles) {
                var scopipe_handle_info = TranslateInfo.__scopipe_handle_assocs[scopipe_handle] as __scopipe_handle_info

                StageList[CUR_STAGE_INDEX].AddRdVar(scopipe_handle_info.if_id)
                StageList[CUR_STAGE_INDEX].AddRdVar(scopipe_handle_info.we)
                StageList[CUR_STAGE_INDEX].AddRdVar(scopipe_handle_info.wdata)
                StageList[CUR_STAGE_INDEX].AddRdVar(scopipe_handle_info.rdreq_pending)
                StageList[CUR_STAGE_INDEX].AddRdVar(scopipe_handle_info.resp_done)

                StageList[CUR_STAGE_INDEX].AddWrVar(scopipe_handle_info.if_id)
                StageList[CUR_STAGE_INDEX].AddWrVar(scopipe_handle_info.we)
                StageList[CUR_STAGE_INDEX].AddWrVar(scopipe_handle_info.wdata)
                StageList[CUR_STAGE_INDEX].AddWrVar(scopipe_handle_info.rdreq_pending)
                StageList[CUR_STAGE_INDEX].AddWrVar(scopipe_handle_info.resp_done)
            }

            // println("mcopipe analysis: " + StageList[CUR_STAGE_INDEX].name)
            // println("mcopipe_handles: " + StageAssocList[CUR_STAGE_INDEX].mcopipe_handles.size)
        }

        // Generate resources for locals
        for (CUR_STAGE_INDEX in 0 until StageList.size) {
            var pstage_info = TranslateInfo.__stage_assocs[StageList[CUR_STAGE_INDEX]]

            var prev_wr_pvarlist    = ArrayList<hw_var>()
            var cur_wr_pvarlist     = ArrayList<hw_var>()
            var cur_rd_pvarlist     = ArrayList<hw_var>()
            var next_rd_pvarlist    = ArrayList<hw_var>()

            for (STAGE_INDEX_ANLZ in 0 until StageList.size) {
                if (STAGE_INDEX_ANLZ < CUR_STAGE_INDEX) {
                    prev_wr_pvarlist = UniteArrayLists(prev_wr_pvarlist, CrossArrayLists(StageList[STAGE_INDEX_ANLZ].wrvars, locals))

                } else if (STAGE_INDEX_ANLZ == CUR_STAGE_INDEX) {
                    cur_wr_pvarlist = UniteArrayLists(cur_wr_pvarlist, CrossArrayLists(StageList[STAGE_INDEX_ANLZ].wrvars, locals))
                    cur_rd_pvarlist = UniteArrayLists(cur_rd_pvarlist, CrossArrayLists(StageList[STAGE_INDEX_ANLZ].rdvars, locals))

                } else {
                    next_rd_pvarlist = UniteArrayLists(next_rd_pvarlist, CrossArrayLists(StageList[STAGE_INDEX_ANLZ].rdvars, locals))

                }
            }

            var pContext_locals = UniteArrayLists(UniteArrayLists(cur_wr_pvarlist, cur_rd_pvarlist), CrossArrayLists(prev_wr_pvarlist, next_rd_pvarlist))
            var pContext_notnew = CrossArrayLists(UniteArrayLists(cur_rd_pvarlist, next_rd_pvarlist), prev_wr_pvarlist)

            // processing pContext list
            for (local in pContext_locals) {
                if (local is hw_local) {
                    var new_local = cyclix_gen.local(
                        (StageAssocList[CUR_STAGE_INDEX].name_prefix + local.name),
                        local.VarType,
                        local.src_struct,
                        local.dimensions,
                        local.defval
                    )
                    StageAssocList[CUR_STAGE_INDEX].pContext_local_dict.put(local, new_local)
                } else if (local is hw_local_sticky) {
                    var new_local = cyclix_gen.global(
                        (StageAssocList[CUR_STAGE_INDEX].name_prefix + local.name),
                        local.VarType,
                        local.src_struct,
                        local.dimensions,
                        local.defval
                    )
                    StageAssocList[CUR_STAGE_INDEX].pContext_local_dict.put(local, new_local)
                }
            }

            // adding accumbufs in case they are new
            MSG(DEBUG_FLAG, "Processing accumlist, accum_dict size: " + StageAssocList[CUR_STAGE_INDEX].accum_dict.size)
            for (accum in StageAssocList[CUR_STAGE_INDEX].accum_dict) {
                MSG(DEBUG_FLAG, "accum: " + accum.name)
                if (CUR_STAGE_INDEX == 0) {
                    StageAssocList[CUR_STAGE_INDEX].accum_dict_new.add(accum)
                    pContext_notnew.add(accum)
                } else {
                    if (!StageAssocList[CUR_STAGE_INDEX-1].pContext_local_dict.containsKey(accum)) {
                        StageAssocList[CUR_STAGE_INDEX].accum_dict_new.add(accum)
                        pContext_notnew.add(accum)
                    }
                }
            }

            for (notnew in pContext_notnew) {
                if (notnew is hw_local) {
                    var new_global = cyclix_gen.global(
                        (StageAssocList[CUR_STAGE_INDEX].name_prefix + notnew.name + "_genglbl"),
                        notnew.VarType,
                        notnew.src_struct,
                        notnew.dimensions,
                        notnew.defval)
                    StageAssocList[CUR_STAGE_INDEX].pContext_srcglbl_dict.put(notnew, new_global)
                }
            }
        }

        // Generating logic //
        MSG(DEBUG_FLAG, "Generating logic")

        // mcopipe processing
        MSG(DEBUG_FLAG, "mcopipe processing")
        for (mcopipe_if in TranslateInfo.__mcopipe_if_assocs) {
            // forming mcopipe ptr next values
            cyclix_gen.add_gen(mcopipe_if.value.wr_ptr_next, mcopipe_if.value.wr_ptr, 1)
            cyclix_gen.add_gen(mcopipe_if.value.rd_ptr_next, mcopipe_if.value.rd_ptr, 1)
        }

        // rdbuf processing
        MSG(DEBUG_FLAG, "rdbuf logic")
        for (global in TranslateInfo.__global_assocs) {
            cyclix_gen.assign(global.value.cyclix_global_buf, global.value.cyclix_global)
        }

        for (CUR_STAGE_INDEX in StageList.lastIndex downTo 0) {

            var curStage        = StageList[CUR_STAGE_INDEX]
            var curStageAssoc   = StageAssocList[CUR_STAGE_INDEX]

            MSG(DEBUG_FLAG, "[[ Stage processing: " + curStage.name + " ]]")

            // Asserting pctrl defaults (deployed even if signal is not used)
            cyclix_gen.assign(curStageAssoc.pctrl_succ, 0)
            cyclix_gen.assign(curStageAssoc.pctrl_working, 0)
            cyclix_gen.assign(curStageAssoc.mcopipe_stallreq_unblk, 0)

            // Generating root pctrls
            MSG(DEBUG_FLAG, "#### Initializing pctrls ####")

            cyclix_gen.begif(curStageAssoc.pctrl_stalled_glbl)
            run {
                cyclix_gen.assign(curStageAssoc.pctrl_new, 0)

                // reattempt execution
                cyclix_gen.assign(curStageAssoc.pctrl_stalled_glbl, 0)
            }; cyclix_gen.endif()
            cyclix_gen.begelse()
            run {
                if (CUR_STAGE_INDEX == 0) {
                    // new transaction
                    cyclix_gen.assign(curStageAssoc.pctrl_active_glbl, 1)
                    cyclix_gen.assign(curStageAssoc.pctrl_killed_glbl, 0)
                }
                cyclix_gen.lor_gen(curStageAssoc.pctrl_new, curStageAssoc.pctrl_active_glbl, curStageAssoc.pctrl_killed_glbl)
            }; cyclix_gen.endif()

            cyclix_gen.assign(curStageAssoc.pctrl_finish, 0)
            cyclix_gen.assign(curStageAssoc.pctrl_flushreq, 0)
            cyclix_gen.assign(curStageAssoc.pctrl_nevictable, 0)

            // Generating "occupied" pctrl
            cyclix_gen.bor_gen(curStageAssoc.pctrl_occupied, curStageAssoc.pctrl_active_glbl, curStageAssoc.pctrl_killed_glbl)

            // initializing new accum globals
            MSG(DEBUG_FLAG, "#### Initializing accumbufs ####")
            if (curStageAssoc.accum_dict_new.size > 0) {
                cyclix_gen.begif(curStageAssoc.pctrl_new)
                run {
                    for (accum in curStageAssoc.accum_dict_new) {
                        if (!curStageAssoc.pContext_srcglbl_dict.containsKey(accum)) ERROR("accum err: " + accum.name)
                        cyclix_gen.assign((curStageAssoc.pContext_srcglbl_dict[accum] as hw_var), hw_imm(accum.defval))
                    }
                }; cyclix_gen.endif()
            }

            // Fetching locals from src_glbls
            MSG(DEBUG_FLAG, "Fetching locals from src_glbls")
            for (src_glbl in curStageAssoc.pContext_srcglbl_dict) {
                cyclix_gen.assign(curStageAssoc.pContext_local_dict[src_glbl.key] as hw_var, src_glbl.value)
            }

            // Forming mcopipe_handles_last list
            MSG(DEBUG_FLAG ,"Detecting last mcopipes")
            for (mcopipe_handle in curStageAssoc.mcopipe_handles) {
                if (CUR_STAGE_INDEX != StageList.lastIndex) {
                    if (!StageAssocList[CUR_STAGE_INDEX].mcopipe_handles_last.contains(mcopipe_handle)) {
                        curStageAssoc.mcopipe_handles_last.add(mcopipe_handle)
                    }
                }
            }

            // pipeline flush processing
            if (CUR_STAGE_INDEX < StageList.lastIndex) {
                cyclix_gen.bor_gen(curStageAssoc.pctrl_flushreq, curStageAssoc.pctrl_flushreq, StageAssocList[CUR_STAGE_INDEX+1].pctrl_flushreq)
            }

            // Analyzing local stickies
            var local_stickies_new      = ArrayList<hw_local_sticky>()
            var local_stickies_notnew   = ArrayList<hw_local_sticky>()
            for (local in curStageAssoc.pContext_local_dict) {
                if (local.key is hw_local_sticky) {
                    if (CUR_STAGE_INDEX == 0) {
                        local_stickies_new.add(local.key as hw_local_sticky)
                    } else if (StageAssocList[CUR_STAGE_INDEX-1].pContext_local_dict.containsKey(local.key)) {
                        local_stickies_notnew.add(local.key as hw_local_sticky)
                    } else {
                        local_stickies_new.add(local.key as hw_local_sticky)
                    }
                }
            }

            // not a bubble
            cyclix_gen.begif(curStageAssoc.pctrl_occupied)
            run {

                MSG(DEBUG_FLAG, "#### Initializing new psticky descriptors ####")
                if (local_stickies_new.size != 0) {
                    cyclix_gen.begif(curStageAssoc.pctrl_new)
                    run {
                        for (sticky_new in local_stickies_new) {
                            cyclix_gen.assign(curStageAssoc.TranslateVar(sticky_new), hw_imm(sticky_new.defval))
                        }
                    }; cyclix_gen.endif()
                }

                MSG(DEBUG_FLAG, "#### Acquiring mcopipe rdata ####")
                for (mcopipe_handle in curStageAssoc.mcopipe_handles) {

                    ///
                    var if_id_translated            = curStageAssoc.TranslateVar(TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!.if_id)
                    var rdreq_pending_translated    = curStageAssoc.TranslateVar(TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!.rdreq_pending)
                    var tid_translated              = curStageAssoc.TranslateVar(TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!.tid)
                    var resp_done_translated        = curStageAssoc.TranslateVar(TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!.resp_done)
                    var rdata_translated            = curStageAssoc.TranslateVar(TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!.rdata)

                    cyclix_gen.begif(rdreq_pending_translated)
                    run {

                        var IF_NUM = 0
                        for (mcopipe_if in TranslateInfo.__mcopipe_handle_reqdict[mcopipe_handle]!!) {
                            var mcopipe_if_assoc = TranslateInfo.__mcopipe_if_assocs[mcopipe_if]!!

                            cyclix_gen.begif(cyclix_gen.eq2(if_id_translated, hw_imm(if_id_translated.dimensions, IF_NUM.toString())))
                            run {

                                cyclix_gen.begif(cyclix_gen.eq2(tid_translated, mcopipe_if_assoc.rd_ptr))
                                run {

                                    var fifo_rdata = cyclix_gen.local(GetGenName("mcopipe_rdata"), mcopipe_if.rdata_VarType, mcopipe_if.rdata_src_struct, mcopipe_if.rdata_dim, "0")

                                    cyclix_gen.begif(cyclix_gen.fifo_rd(mcopipe_if_assoc.resp_fifo, fifo_rdata))
                                    run {
                                        // acquiring data
                                        cyclix_gen.assign(rdreq_pending_translated, 0)
                                        cyclix_gen.assign(resp_done_translated, 1)
                                        cyclix_gen.assign(rdata_translated, fifo_rdata)

                                        // mcopipe fifo management
                                        cyclix_gen.assign(mcopipe_if_assoc.rd_ptr, mcopipe_if_assoc.rd_ptr_next)
                                        cyclix_gen.assign(mcopipe_if_assoc.full_flag, 0)

                                        cyclix_gen.begif(cyclix_gen.eq2(mcopipe_if_assoc.rd_ptr, mcopipe_if_assoc.wr_ptr))
                                        run {
                                            cyclix_gen.assign(mcopipe_if_assoc.empty_flag, 1)
                                        }; cyclix_gen.endif()
                                    }; cyclix_gen.endif()

                                }; cyclix_gen.endif()

                            }; cyclix_gen.endif()
                            IF_NUM++
                        }

                    }; cyclix_gen.endif()
                }

                // forming mcopipe rdreq inprogress attribute
                MSG(DEBUG_FLAG, "#### Forming mcopipe rdreq inprogress attribute ####")
                var mcopipe_rdreq_inprogress = cyclix_gen.ulocal(GetGenName(curStageAssoc.name_prefix + "mcopipe_rdreq_inprogress"), 0, 0, "0")
                cyclix_gen.assign(mcopipe_rdreq_inprogress, 0)
                for (mcopipe_handle in curStageAssoc.mcopipe_handles) {
                    cyclix_gen.lor_gen(mcopipe_rdreq_inprogress, mcopipe_rdreq_inprogress, curStageAssoc.TranslateVar(TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!.rdreq_pending))
                }

                // do not start payload if flush requested
                MSG(DEBUG_FLAG, "#### Pipeline flush processing ####")
                cyclix_gen.begif(curStageAssoc.pctrl_flushreq)
                run {
                    if (CUR_STAGE_INDEX == 0) {
                        cyclix_gen.begif(mcopipe_rdreq_inprogress)
                        run {
                            curStageAssoc.pkill_cmd_internal(cyclix_gen)
                        }; cyclix_gen.endif()
                        cyclix_gen.begelse()
                        run {
                            // Dropping transaction context //
                            for (local in curStageAssoc.pContext_local_dict) {
                                if (local.key is hw_local_sticky) cyclix_gen.assign(local.value, hw_imm(local.value.defval))
                            }
                            for (srcglbl in curStageAssoc.pContext_srcglbl_dict) {
                                cyclix_gen.assign(srcglbl.value, hw_imm(srcglbl.value.defval))
                                cyclix_gen.assign(curStageAssoc.TranslateVar(srcglbl.key), hw_imm(srcglbl.value.defval))
                            }
                        }; cyclix_gen.endif()
                    } else {
                        curStageAssoc.pkill_cmd_internal(cyclix_gen)
                    }
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()
            cyclix_gen.begelse()
            run {
                for (mcopipe_handle in curStageAssoc.mcopipe_handles) {
                    var mcopipe_handle_assoc = TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!
                    cyclix_gen.assign(curStageAssoc.TranslateVar(mcopipe_handle_assoc.resp_done), 0)
                    cyclix_gen.assign(curStageAssoc.TranslateVar(mcopipe_handle_assoc.rdreq_pending), 0)
                }
            }; cyclix_gen.endif()

            // Saving synced defaults (for indexed succ assignments)
            MSG(DEBUG_FLAG, "#### Saving synced targets ####")
            for (assign_succ_assoc in curStageAssoc.assign_succ_assocs) {
                cyclix_gen.assign(assign_succ_assoc.value.buf, curStageAssoc.TranslateVar(assign_succ_assoc.key))
            }

            // Generating payload expressions
            MSG(DEBUG_FLAG, "#### Generating payload expressions ####")
            for (expr in curStage.expressions) {
                reconstruct_expression(DEBUG_FLAG,
                    cyclix_gen,
                    expr,
                    curStage,
                    TranslateInfo,
                    curStageAssoc)
            }

            // forming nevictable pctrl
            for (mcopipe_handle in curStageAssoc.mcopipe_handles) {
                cyclix_gen.lor_gen(curStageAssoc.pctrl_nevictable, curStageAssoc.pctrl_nevictable, curStageAssoc.TranslateVar(TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!.rdreq_pending))
            }

            // Stalling in case mcopipe requests are not satisfied
            MSG(DEBUG_FLAG, "#### Stalling in case mcopipe requests are not satisfied ####")
            cyclix_gen.begif(curStageAssoc.mcopipe_stallreq_unblk)
            run {
                curStageAssoc.pstall_ifactive_cmd(cyclix_gen)
            }; cyclix_gen.endif()

            // Processing of pstall from next pstage
            MSG(DEBUG_FLAG, "#### Processing of pstall from next pstage ####")
            if (CUR_STAGE_INDEX < StageList.lastIndex) {
                cyclix_gen.begif(StageAssocList[CUR_STAGE_INDEX+1].pctrl_stalled_glbl)
                run {
                    // prepeat from next pstage requested
                    curStageAssoc.pstall_ifactive_cmd(cyclix_gen)
                    // protection from evicting broken transaction with unfinished I/O by subsequent transaction
                    cyclix_gen.begif(curStageAssoc.pctrl_nevictable)
                    run {
                        curStageAssoc.pstall_ifoccupied_cmd(cyclix_gen)
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
            }

            // Forced stalling in case any last mcopipe pending reads are not satisfied
            MSG(DEBUG_FLAG, "#### Forced stalling in case any last mcopipe pending reads are not satisfied ####")
            for (mcopipe_handle in curStageAssoc.mcopipe_handles_last) {
                // check if mcopipe rd request is pending
                cyclix_gen.begif(curStageAssoc.TranslateVar(TranslateInfo.__mcopipe_handle_assocs[mcopipe_handle]!!.rdreq_pending))
                run {
                    // forced stalling
                    curStageAssoc.pstall_ifoccupied_cmd(cyclix_gen)
                }; cyclix_gen.endif()
            }

            // pstage_finish and pstage_succ formation
            MSG(DEBUG_FLAG, "#### pctrl_finish and pctrl_succ formation ####")
            cyclix_gen.begif(curStageAssoc.pctrl_stalled_glbl)
            run {
                cyclix_gen.assign(curStageAssoc.pctrl_finish, 0)
                cyclix_gen.assign(curStageAssoc.pctrl_succ, 0)
            }; cyclix_gen.endif()
            cyclix_gen.begelse()
            run {
                cyclix_gen.assign(curStageAssoc.pctrl_finish, curStageAssoc.pctrl_occupied)
                cyclix_gen.assign(curStageAssoc.pctrl_succ, curStageAssoc.pctrl_active_glbl)
            }; cyclix_gen.endif()

            // asserting synced signals
            MSG(DEBUG_FLAG, "#### Asserting synced signals ####")
            if (curStageAssoc.assign_succ_assocs.size > 0) {
                cyclix_gen.begif(curStageAssoc.pctrl_succ)
                run {
                    for (assign_succ_assoc in curStageAssoc.assign_succ_assocs) {
                        cyclix_gen.begif(assign_succ_assoc.value.req)
                        run {
                            cyclix_gen.assign(curStageAssoc.TranslateVar(assign_succ_assoc.key), assign_succ_assoc.value.buf)
                        }; cyclix_gen.endif()
                    }
                }; cyclix_gen.endif()
            }

            // processing context in case transaction is finished
            cyclix_gen.begif(curStageAssoc.pctrl_finish)
            run {
                // programming next stage in case transaction is able to propagate
                if (CUR_STAGE_INDEX < StageList.lastIndex) {
                    cyclix_gen.begif(!StageAssocList[CUR_STAGE_INDEX+1].pctrl_stalled_glbl)
                    run {
                        // propagating transaction context
                        for (local in StageAssocList[CUR_STAGE_INDEX+1].pContext_local_dict) {
                            if (curStageAssoc.pContext_local_dict.containsKey(local.key)) {
                                // having data to propagate
                                if (local.key is hw_local) {
                                    // propagating locals
                                    cyclix_gen.assign(StageAssocList[CUR_STAGE_INDEX+1].pContext_srcglbl_dict[local.key]!!, curStageAssoc.TranslateVar(local.key))
                                } else {
                                    // propagating stickies
                                    cyclix_gen.assign(StageAssocList[CUR_STAGE_INDEX+1].pContext_local_dict[local.key]!!, curStageAssoc.TranslateVar(local.key))
                                }
                            }
                        }

                        // propagating pctrls
                        cyclix_gen.assign(StageAssocList[CUR_STAGE_INDEX+1].pctrl_active_glbl, curStageAssoc.pctrl_active_glbl)
                        cyclix_gen.assign(StageAssocList[CUR_STAGE_INDEX+1].pctrl_killed_glbl, curStageAssoc.pctrl_killed_glbl)
                        cyclix_gen.assign(StageAssocList[CUR_STAGE_INDEX+1].pctrl_stalled_glbl, 0)
                    }; cyclix_gen.endif()
                }

                // clearing itself
                cyclix_gen.assign(curStageAssoc.pctrl_active_glbl, 0)
                cyclix_gen.assign(curStageAssoc.pctrl_killed_glbl, 0)
                cyclix_gen.assign(curStageAssoc.pctrl_stalled_glbl, 0)
            }; cyclix_gen.endif()

            // working signal: succ or pstall
            cyclix_gen.bor_gen(curStageAssoc.pctrl_working, curStageAssoc.pctrl_succ, curStageAssoc.pctrl_stalled_glbl)
        }

        MSG(DEBUG_FLAG, "Translating to cyclix: complete")
        return cyclix_gen
    }
}