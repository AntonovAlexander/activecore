package cyclix

import hwast.*
import java.lang.Exception

val OP_CYCPROC = hw_opcode("cycproc")

open class module(name_in : String) : hw_astc() {

    val name = name_in

    var tab_Counter = 0

    var wrvars = mutableMapOf<String, hw_var>()
    var rdvars = mutableMapOf<String, hw_var>()

    var Ports = ArrayList<hw_port>()
    var globals = ArrayList<hw_var>()
    var locals = ArrayList<hw_var>()

    var fifo_ifs = mutableMapOf<String, hw_structvar>()
    var fifo_ins = ArrayList<hw_fifo_in>()
    var fifo_outs = ArrayList<hw_fifo_out>()

    var proc = hw_exec(OP_CYCPROC)

    init {
        this.add(proc)
    }

    class fifo_out_descr (val ext_req      : rtl.hw_port,
                               val ext_wdata    : rtl.hw_port,
                               val ext_ack      : rtl.hw_port,
                               var reqbuf_req   : rtl.hw_sticky,
                               var reqbuf_wdata : rtl.hw_sticky)

    class fifo_in_descr  (val ext_req      : rtl.hw_port,
                               val ext_rdata    : rtl.hw_port,
                               val ext_ack      : rtl.hw_port,
                               var buf_req      : hw_var,
                               var buf_rdata    : hw_var)

    var var_dict        = mutableMapOf<hw_var, hw_var>()
    var fifo_out_dict   = mutableMapOf<hw_fifo_out, fifo_out_descr>()
    var fifo_in_dict    = mutableMapOf<hw_fifo_in, fifo_in_descr>()

    private fun add_local(new_local : hw_var) {
        if (wrvars.put(new_local.name, new_local) != null) {
            ERROR("local addition problem!")
        }
        if (rdvars.put(new_local.name, new_local) != null) {
            ERROR("local addition problem!")
        }
        locals.add(new_local)
        new_local.default_astc = this
    }

    fun local(name : String, VarType : VAR_TYPE, src_struct_in : hw_struct, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, src_struct_in, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, msb, lsb, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, VarType : VAR_TYPE, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_var {
        var ret_var = hw_var(name, src_struct_in, dimensions)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, src_struct_in : hw_struct) : hw_var {
        var ret_var = hw_var(name, src_struct_in)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, defval)
        add_local(ret_var)
        return ret_var
    }

    private fun add_global(new_global : hw_var) {
        if (wrvars.put(new_global.name, new_global) != null) {
            ERROR("global addition problem!")
        }
        if (rdvars.put(new_global.name, new_global) != null) {
            ERROR("global addition problem!")
        }
        globals.add(new_global)
        new_global.default_astc = this
    }

    fun global(name : String, VarType : VAR_TYPE, src_struct_in : hw_struct, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, src_struct_in, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, msb, lsb, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, VarType : VAR_TYPE, defval : String) : hw_var {
        var ret_var = hw_var(name, VarType, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_var {
        var ret_var = hw_var(name, src_struct_in, dimensions)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, src_struct_in : hw_struct) : hw_var {
        var ret_var = hw_var(name, src_struct_in)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, msb, lsb, defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.UNSIGNED, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, msb, lsb, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, VAR_TYPE.SIGNED, defval)
        add_global(ret_var)
        return ret_var
    }

    private fun add_port(new_port : hw_port) {
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

    fun port(name : String, port_dir : PORT_DIR, src_struct_in : hw_struct, dimensions : hw_dim_static) : hw_port {
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

    private fun add_fifo_in(new_fifo_in: hw_fifo_in) {
        if (fifo_ifs.put(new_fifo_in.name, new_fifo_in) != null) {
            ERROR("FIFO addition problem!")
        }
        fifo_ins.add(new_fifo_in)
    }

    fun fifo_in(name : String, VarType : VAR_TYPE, src_struct_in : hw_struct, dimensions : hw_dim_static) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, VarType, src_struct_in, dimensions)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, VarType, dimensions)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, VarType, msb, lsb)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, src_struct_in, dimensions)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name : String, src_struct_in : hw_struct) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, src_struct_in)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun ufifo_in(name : String, dimensions : hw_dim_static) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, VAR_TYPE.UNSIGNED, dimensions)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun ufifo_in(name : String, msb: Int, lsb: Int) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, VAR_TYPE.UNSIGNED, msb, lsb)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun sfifo_in(name : String, dimensions : hw_dim_static) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, VAR_TYPE.SIGNED, dimensions)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun sfifo_in(name : String, msb: Int, lsb: Int) : hw_fifo_in {
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

    fun fifo_out(name : String, VarType : VAR_TYPE, src_struct_in : hw_struct, dimensions : hw_dim_static) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, VarType, src_struct_in, dimensions)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, VarType, dimensions)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name : String, VarType : VAR_TYPE, msb: Int, lsb: Int) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, VarType, msb, lsb)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name : String, src_struct_in : hw_struct ,dimensions : hw_dim_static) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, src_struct_in, dimensions)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name : String, src_struct_in : hw_struct) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, src_struct_in)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun ufifo_out(name : String, dimensions : hw_dim_static) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, VAR_TYPE.UNSIGNED, dimensions)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun ufifo_out(name : String, msb: Int, lsb: Int) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, VAR_TYPE.UNSIGNED, msb, lsb)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun sfifo_out(name : String, dimensions : hw_dim_static) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, VAR_TYPE.SIGNED, dimensions)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun sfifo_out(name : String, msb: Int, lsb: Int) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, VAR_TYPE.SIGNED, msb, lsb)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_wr(fifo : hw_fifo_out, wdata : hw_param) : hw_var {
        var new_expr = hw_exec_fifo_wr(fifo)
        var genvar = hw_var(GetGenName("fifo_rdy"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddRdParam(wdata)
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun fifo_rd(fifo : hw_fifo_in, rdata : hw_var) : hw_var {
        var new_expr = hw_exec_fifo_rd(fifo)
        var genvar = hw_var(GetGenName("fifo_rdy"), VAR_TYPE.UNSIGNED, 0, 0, "0")
        new_expr.AddWrVar(genvar)
        new_expr.AddGenVar(genvar)
        new_expr.AddWrVar(rdata)
        AddExpr(new_expr)
        return genvar
    }

    fun TranslateVar(var_in : hw_var) : hw_var {
        var ret_var = var_dict[var_in]
        if (ret_var != null) return ret_var
        else ERROR("Var translation error")
        return hw_var("UGLY HACK", VAR_TYPE.UNSIGNED, 0, 0, "0")
    }

    fun TranslateParam(param : hw_param) : hw_param {
        if (param is hw_imm) return param
        else if (param is hw_var) return TranslateVar(param)
        else ERROR("Type unrecognized!")
        return param
    }

    fun TranslateFifoOut(fifo : hw_fifo_out) : fifo_out_descr {
        var ret_var = fifo_out_dict[fifo]
        if (ret_var == null) ERROR("FIFO translation error")
        return ret_var!!
    }

    fun TranslateFifoIn(fifo : hw_fifo_in) : fifo_in_descr {
        var ret_var = fifo_in_dict[fifo]
        if (ret_var == null) ERROR("FIFO translation error")
        return ret_var!!
    }

    fun rtl_export_expr(rtl_gen : hw_astc,
                        expr : hw_exec,
                        rst : rtl.hw_port) {

        // println("#### Cyclix: exporting expression: " + expr.opcode.default_string)
        // for (param in expr.params) println("param: " + param.GetString())
        // for (wrvar in expr.wrvars) println("wrvar: " + wrvar.name)

        var fractions = hw_fractions()
        for (src_fraction in expr.fractions) {
            if (src_fraction is hw_fraction_C) fractions.add(src_fraction)
            else if (src_fraction is hw_fraction_V) fractions.add(hw_fraction_V(TranslateVar(src_fraction.index)))
            else if (src_fraction is hw_fraction_CC) fractions.add(src_fraction)
            else if (src_fraction is hw_fraction_CV) fractions.add(hw_fraction_CV(src_fraction.msb, TranslateVar(src_fraction.lsb)))
            else if (src_fraction is hw_fraction_VC) fractions.add(hw_fraction_VC(TranslateVar(src_fraction.msb), src_fraction.lsb))
            else if (src_fraction is hw_fraction_VV) fractions.add(hw_fraction_VV(TranslateVar(src_fraction.msb), TranslateVar(src_fraction.lsb)))
            else if (src_fraction is hw_fraction_SubStruct) fractions.add(src_fraction)
            else ERROR("dimensions error")
        }

        if ((expr.opcode == OP1_ASSIGN)) {
            rtl_gen.assign(fractions, TranslateVar(expr.wrvars[0]), TranslateParam(expr.params[0]))

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

            var params = ArrayList<hw_param>()
            for (param in expr.params) {
                params.add(TranslateParam(param))
            }
            rtl_gen.AddExpr_op_gen(expr.opcode, TranslateVar(expr.wrvars[0]), params)

        } else if (expr.opcode == OP1_IF) {

            rtl_gen.begif(TranslateParam(expr.params[0]))
            run {
                for (child_expr in expr.expressions) {
                    rtl_export_expr(rtl_gen, child_expr, rst)
                }
            }; rtl_gen.endif()

        } else if (expr.opcode == OP1_WHILE) {

            rtl_gen.begwhile(TranslateParam(expr.params[0]))
            run {
                for (child_expr in expr.expressions) {
                    rtl_export_expr(rtl_gen, child_expr, rst)
                }
            }; rtl_gen.endwhile()

        } else if (expr.opcode == OP_FIFO_WR) {

            var fifo = TranslateFifoOut((expr as hw_exec_fifo_wr).fifo)
            var wdata_translated = TranslateParam(expr.params[0])
            var fifo_rdy = TranslateVar(expr.wrvars[0])

            rtl_gen.begif(rtl_gen.lnot(rst))
            run {
                rtl_gen.begif(fifo.reqbuf_req)
                run {
                    // fifo/reqbuf busy
                    rtl_gen.assign(fifo_rdy, 0)
                }; rtl_gen.endif()
                rtl_gen.begelse()
                run {
                    // fifo/reqbuf ready to consume request
                    rtl_gen.assign(fifo_rdy, 1)
                    rtl_gen.begif(fifo.ext_req)
                    run {
                        // bus busy - putting request in reqbuf
                        rtl_gen.assign(fifo.reqbuf_req, 1)
                        rtl_gen.assign(fifo.reqbuf_wdata, wdata_translated)
                    }; rtl_gen.endif()
                    rtl_gen.begelse()
                    run {
                        // bus free
                        rtl_gen.assign(fifo.ext_req, 1)
                        rtl_gen.assign(fifo.ext_wdata, wdata_translated)

                        rtl_gen.begif(rtl_gen.lnot(fifo.ext_ack))
                        run {
                            // target busy - putting request in reqbuf
                            rtl_gen.assign(fifo.reqbuf_req, 1)
                            rtl_gen.assign(fifo.reqbuf_wdata, wdata_translated)
                        }; rtl_gen.endif()
                    }; rtl_gen.endif()
                }; rtl_gen.endif()
            }; rtl_gen.endif()

        } else if (expr.opcode == OP_FIFO_RD) {

            var fifo = TranslateFifoIn((expr as hw_exec_fifo_rd).fifo)
            var fifo_rdy = TranslateVar(expr.wrvars[0])
            var rdata_translated = TranslateVar(expr.wrvars[1])

            rtl_gen.begif(rtl_gen.lnot(rst))
            run {
                // default: inactive
                rtl_gen.assign(fifo_rdy, 0)

                rtl_gen.begif(fifo.buf_req)
                run {
                    //// request pending
                    // reading data
                    rtl_gen.assign(fifo_rdy, 1)
                    rtl_gen.assign(rdata_translated, fifo.buf_rdata)

                    // clearing buffer
                    rtl_gen.assign(fifo.buf_req, 0)

                    // asserting ack
                    rtl_gen.assign(fifo.ext_ack, 1)
                }; rtl_gen.endif()
            }; rtl_gen.endif()

        } else ERROR("Reconstruction of expression failed: opcode undefined: " + expr.opcode.default_string)

        // println("#### Cyclix: exporting expression complete!")
    }

    fun export_rtl() : rtl.module {

        println("#######################################")
        println("#### Starting Cyclix-to-RTL export ####")
        println("#######################################")

        // TODO: pre-validation

        var_dict.clear()
        fifo_out_dict.clear()
        fifo_in_dict.clear()

        var rtl_gen = rtl.module(name)

        // Adding structs
        for (if_struct in hw_if_structs) {
            rtl_gen.add_if_struct(if_struct)
        }

        for (private_struct in hw_private_structs) {
            rtl_gen.add_private_struct(private_struct)
        }

        // Generating ports
        var clk = rtl_gen.uinput("clk_i", 0, 0, "0")
        var rst = rtl_gen.uinput("rst_i", 0, 0, "0")

        // Generating combinationals
        for (local in locals)
            var_dict.put(local, rtl_gen.comb(local.name, local.VarType, local.src_struct, local.dimensions, local.defval))

        // Generating genvars
        for (genvar in proc.genvars)
            var_dict.put(genvar, rtl_gen.comb(GetGenName("comb"), genvar.VarType, genvar.src_struct, genvar.dimensions, genvar.defval))

        // Generating globals
        for (global in globals)
            var_dict.put(global, rtl_gen.sticky(global.name, global.VarType, global.src_struct, global.dimensions, global.defval, clk, rst))

        // Generating fifo_outs
        for (fifo_out in fifo_outs) {
            fifo_out_dict.put(fifo_out, fifo_out_descr(
                rtl_gen.uoutput((fifo_out.name + "_genfifo_req_o"), 0, 0, "0"),
                rtl_gen.port((fifo_out.name + "_genfifo_wdata_bo"), rtl.PORT_DIR.OUT, fifo_out.VarType, fifo_out.src_struct, fifo_out.dimensions, fifo_out.defval),
                rtl_gen.uinput((fifo_out.name + "_genfifo_ack_i"), 0, 0, "0"),
                rtl_gen.usticky((fifo_out.name + "_genfifo_reqbuf_req"), 0, 0, "0", clk, rst),
                rtl_gen.sticky((fifo_out.name + "_genfifo_reqbuf_wdata"), fifo_out.VarType, fifo_out.src_struct, fifo_out.dimensions, fifo_out.defval, clk, rst)
            ))
        }

        // Generating fifo_ins
        for (fifo_in in fifo_ins) {
            fifo_in_dict.put(fifo_in, fifo_in_descr(
                rtl_gen.uinput((fifo_in.name + "_genfifo_req_i"), 0, 0, "0"),
                rtl_gen.port((fifo_in.name + "_genfifo_rdata_bi"), rtl.PORT_DIR.IN, fifo_in.VarType, fifo_in.src_struct, fifo_in.dimensions, fifo_in.defval),
                rtl_gen.uoutput((fifo_in.name + "_genfifo_ack_o"), 0, 0, "0"),
                rtl_gen.ucomb((fifo_in.name + "_genfifo_buf_req"), 0, 0, "0"),
                rtl_gen.comb((fifo_in.name + "_genfifo_buf_rdata"), fifo_in.VarType, fifo_in.src_struct, fifo_in.dimensions, fifo_in.defval)
            ))
        }

        rtl_gen.cproc_begin()
        run {

            // fifo_out reqbuf buffering
            for (fifo_out in fifo_out_dict) {
                rtl_gen.assign(fifo_out.value.ext_req, fifo_out.value.reqbuf_req)
                rtl_gen.assign(fifo_out.value.ext_wdata, fifo_out.value.reqbuf_wdata)

                rtl_gen.begif(fifo_out.value.reqbuf_req)
                run {
                    rtl_gen.begif(fifo_out.value.ext_ack)
                    run {
                        // clearing reqbuf
                        rtl_gen.assign(fifo_out.value.reqbuf_req, 0)
                        rtl_gen.assign(fifo_out.value.reqbuf_wdata, 0)
                    }; rtl_gen.endif()
                }; rtl_gen.endif()
            }

            // fifo_in buffering
            for (fifo_in in fifo_in_dict) {
                rtl_gen.assign(fifo_in.value.buf_req, fifo_in.value.ext_req)
                rtl_gen.assign(fifo_in.value.buf_rdata, fifo_in.value.ext_rdata)
            }

            // Generating payload
            for (expr in proc.expressions) {
                rtl_export_expr(rtl_gen, expr, rst)
            }

        }; rtl_gen.cproc_end()

        // TODO: post-validation

        println("########################################")
        println("#### Cyclix-to-RTL export complete! ####")
        println("########################################")

        return rtl_gen
    }
}