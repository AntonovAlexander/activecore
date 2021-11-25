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

    var __global_buf_assocs = mutableMapOf<hw_var, hw_var>()
    var Subprocs            = mutableMapOf<String, hw_subproc>()

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

    fun local(name : String, vartype : hw_type, defimm : hw_imm) : hw_local {
        var ret_var = hw_local(name, vartype, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, vartype : hw_type, defval : String) : hw_local {
        var ret_var = hw_local(name, vartype, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static) : hw_local {
        var ret_var = hw_local(name, src_struct_in, dimensions)
        add_local(ret_var)
        return ret_var
    }

    fun local(name : String, src_struct_in : hw_struct) : hw_local {
        var ret_var = hw_local(name, src_struct_in)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_UNSIGNED, dimensions, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, dimensions : hw_dim_static, defval : String) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_UNSIGNED, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, msb: Int, lsb: Int, defval : String) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, defimm : hw_imm) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_UNSIGNED, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name : String, defval : String) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_UNSIGNED, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_SIGNED, dimensions, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, dimensions : hw_dim_static, defval : String) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_SIGNED, dimensions, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_SIGNED, msb, lsb, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, msb: Int, lsb: Int, defval : String) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_SIGNED, msb, lsb, defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, defimm : hw_imm) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_SIGNED, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name : String, defval : String) : hw_local {
        var ret_var = hw_local(name, DATA_TYPE.BV_SIGNED, defval)
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

    fun global(name : String, vartype : hw_type, defimm : hw_imm) : hw_global {
        var ret_var = hw_global(name, vartype, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, vartype : hw_type, defval : String) : hw_global {
        var ret_var = hw_global(name, vartype, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, src_struct_in : hw_struct, dimensions : hw_dim_static) : hw_global {
        var ret_var = hw_global(name, src_struct_in, dimensions)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, src_struct_in : hw_struct, msb: Int, lsb: Int) : hw_global {
        var ret_var = hw_global(name, src_struct_in, msb, lsb)
        add_global(ret_var)
        return ret_var
    }

    fun global(name : String, src_struct_in : hw_struct) : hw_global {
        var ret_var = hw_global(name, src_struct_in)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_UNSIGNED, dimensions, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, dimensions : hw_dim_static, defval : String) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_UNSIGNED, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, msb: Int, lsb: Int, defval : String) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, defimm : hw_imm) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_UNSIGNED, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name : String, defval : String) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_UNSIGNED, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_SIGNED, dimensions, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, dimensions : hw_dim_static, defval : String) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_SIGNED, dimensions, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_SIGNED, msb, lsb, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, msb: Int, lsb: Int, defval : String) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_SIGNED, msb, lsb, defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, defimm : hw_imm) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_SIGNED, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name : String, defval : String) : hw_global {
        var ret_var = hw_global(name, DATA_TYPE.BV_SIGNED, defval)
        add_global(ret_var)
        return ret_var
    }

    fun subproc(inst_name : String, subproc_inst : Generic) : hw_subproc {
        var new_inst = hw_subproc(inst_name, subproc_inst, this)
        Subprocs.put(inst_name, new_inst)
        return new_inst
    }

    fun readPrev(rdata : hw_var) : hw_local {
        var ret_var = DUMMY_VAR
        if (__global_buf_assocs.containsKey(rdata)) {
            ret_var = __global_buf_assocs[rdata]!!
        } else {
            ret_var = hw_local(GetGenName("readPrev"), rdata.vartype, rdata.defimm)
            AddGenVar(ret_var)
            __global_buf_assocs.put(rdata, ret_var)
        }
        return (ret_var as hw_local)
    }

    fun fifo_internal_wr_unblk(subproc : hw_subproc, fifo_name : String, wdata : hw_param) : hw_var {
        var new_expr = hw_exec_fifo_internal_wr_unblk(subproc, fifo_name, wdata)
        var genvar = hw_var(GetGenName("fifo_rdy"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddParam(wdata)
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun fifo_internal_rd_unblk(subproc : hw_subproc, fifo_name : String, rdata : hw_var) : hw_var {
        var new_expr = hw_exec_fifo_internal_rd_unblk(subproc, fifo_name, rdata)
        var genvar = hw_var(GetGenName("fifo_rdy"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        new_expr.AddDst(rdata)
        AddExpr(new_expr)
        return genvar
    }
    fun fifo_internal_wr_blk(subproc : hw_subproc, fifo_name : String, wdata : hw_param) {
        var new_expr = hw_exec_fifo_internal_wr_blk(subproc, fifo_name, wdata)
        new_expr.AddParam(wdata)
        AddExpr(new_expr)
    }

    fun fifo_internal_rd_blk(subproc : hw_subproc, fifo_name : String) : hw_var {
        var new_expr = hw_exec_fifo_internal_rd_blk(subproc, fifo_name)
        var genvar = hw_var(GetGenName("fifo_rdata"), subproc.getFifoByName(fifo_name).vartype, subproc.getFifoByName(fifo_name).defval)
        new_expr.AddDst(genvar)
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
            if (port.vartype.DataType == DATA_TYPE.STRUCTURED) {
                port.vartype.src_struct.MarkStructInterface()
            }
        }
        for (fifo in fifo_ins) {
            if (fifo.vartype.DataType == DATA_TYPE.STRUCTURED) {
                fifo.vartype.src_struct.MarkStructInterface()
            }
        }
        for (fifo in fifo_outs) {
            if (fifo.vartype.DataType == DATA_TYPE.STRUCTURED) {
                fifo.vartype.src_struct.MarkStructInterface()
            }
        }
        validate()
        freeze()
    }

    fun export_to_rtl(debug_lvl : DEBUG_LEVEL) : rtl.module {

        NEWLINE()
        MSG("###########################################")
        MSG("#### Starting Cyclix-to-RTL generation ####")
        MSG("#### module: " + name)
        MSG("###########################################")

        validate()

        var rtl_generator = RtlGenerator(this)
        var rtl_gen = rtl_generator.generate(debug_lvl)

        MSG("############################################")
        MSG("#### Cyclix-to-RTL generation complete! ####")
        MSG("#### module: " + name)
        MSG("############################################")

        return rtl_gen
    }

    fun export_to_vivado_cpp(pathname : String, debug_lvl : DEBUG_LEVEL) {

        NEWLINE()
        MSG("############################################")
        MSG("#### Cyclix: starting vivado_cpp export ####")
        MSG("#### module: " + name)
        MSG("############################################")

        validate()
        freeze()

        var writer = VivadoCppWriter(this)
        writer.write(pathname, debug_lvl)

        NEWLINE()
        MSG("#############################################")
        MSG("#### Cyclix: vivado_cpp export complete! ####")
        MSG("#### module: " + name)
        MSG("#############################################")
    }
}


enum class STREAM_PREF_IMPL {
    RTL,
    HLS
}

var STREAM_REQ_BUS_NAME = "stream_req_bus"
var STREAM_RESP_BUS_NAME = "stream_resp_bus"
open class Streaming (name : String, fifo_in_struct: hw_struct, fifo_out_struct: hw_struct, val pref_impl : STREAM_PREF_IMPL) : Generic(name) {

    constructor(name : String, fifo_in_struct: hw_struct, fifo_out_struct: hw_struct) : this(name, fifo_in_struct, fifo_out_struct, STREAM_PREF_IMPL.RTL)

    var stream_req_bus = fifo_in(STREAM_REQ_BUS_NAME, hw_type(fifo_in_struct))
    var stream_req_var = local(GetGenName("stream_req_var"), fifo_in_struct)

    var stream_resp_bus = fifo_out(STREAM_RESP_BUS_NAME, hw_type(fifo_out_struct))
    var stream_resp_var = local(GetGenName("stream_resp_var"), fifo_out_struct)

    init {
        stream_req_var.write_done = true
        stream_resp_var.read_done = true
    }

    fun export_rtl_wrapper(debug_lvl : DEBUG_LEVEL) : rtl.module {

        NEWLINE()
        MSG("#################################################")
        MSG("#### Cyclix: starting RTL wrapper generation ####")
        MSG("#### module: " + name)
        MSG("#################################################")

        var wrapper_rtl_gen = rtl.module(name + "_hls_wrapper")

        MSG("Generating ports...")

        var clk = wrapper_rtl_gen.uinput("clk_i", 0, 0, "0")
        var rst = wrapper_rtl_gen.uinput("rst_i", 0, 0, "1")

        var stream_req_bus_genfifo_req_i        = wrapper_rtl_gen.uinput("stream_req_bus_genfifo_req_i", 0, 0, "0")
        var stream_req_bus_genfifo_ack_o        = wrapper_rtl_gen.uoutput("stream_req_bus_genfifo_ack_o", 0, 0, "0")
        var stream_req_bus_genfifo_rdata_bi     = wrapper_rtl_gen.input("stream_req_bus_genfifo_rdata_bi", stream_req_bus.vartype.src_struct)
        var stream_resp_bus_genfifo_req_o       = wrapper_rtl_gen.uoutput("stream_resp_bus_genfifo_req_o", 0, 0, "0")
        var stream_resp_bus_genfifo_ack_i       = wrapper_rtl_gen.uinput("stream_resp_bus_genfifo_ack_i", 0, 0, "0")
        var stream_resp_bus_genfifo_wdata_bo    = wrapper_rtl_gen.output("stream_resp_bus_genfifo_wdata_bo", stream_resp_bus.vartype.src_struct)

        var datain_drv  = wrapper_rtl_gen.ucomb("datain_drv", stream_req_bus_genfifo_rdata_bi.GetUnpackWidth()-1, 0, "0")
        var dataout_drv = wrapper_rtl_gen.ucomb("dataout_drv", stream_resp_bus_genfifo_wdata_bo.GetUnpackWidth()-1, 0, "0")

        wrapper_rtl_gen.cproc_begin()
        run {
            var datain_drvs = ArrayList<hw_param>()
            for (structvar in stream_req_bus_genfifo_rdata_bi.vartype.src_struct.asReversed()) {
                datain_drvs.add(stream_req_bus_genfifo_rdata_bi.GetFracRef(structvar.name))
            }
            wrapper_rtl_gen.cnct_gen(datain_drv, datain_drvs)
        }; wrapper_rtl_gen.cproc_end()

        wrapper_rtl_gen.cproc_begin()
        run {
            var idx = 0
            for (structvar in stream_resp_bus_genfifo_wdata_bo.vartype.src_struct) {
                wrapper_rtl_gen.assign(stream_resp_bus_genfifo_wdata_bo.GetFracRef(structvar.name), dataout_drv.GetFracRef(idx + structvar.GetWidth() - 1, idx))
                idx += structvar.GetWidth()
            }
        }; wrapper_rtl_gen.cproc_end()

        MSG("Generating ports: done")

        MSG("Generating submodule instance...")

        var wrapped_module = rtl.module(name)
        var ap_clk      = wrapped_module.uinput("ap_clk", 0, 0, "0")
        var ap_rst      = wrapped_module.uinput("ap_rst", 0, 0, "0")
        var ap_start    = wrapped_module.uinput("ap_start", 0, 0, "0")
        var ap_done     = wrapped_module.uoutput("ap_done", 0, 0, "0")
        var ap_idle     = wrapped_module.uoutput("ap_idle", 0, 0, "0")
        var ap_ready    = wrapped_module.uoutput("ap_ready", 0, 0, "0")
        var datain      = wrapped_module.uinput("datain", 0, 0, "0")
        var ap_return   = wrapped_module.uoutput("ap_return", 0, 0, "0")

        var wrapped_module_inst = wrapper_rtl_gen.submodule_bb(name + "_inst", wrapped_module, false)
        wrapped_module_inst.connect(ap_clk, clk)
        wrapped_module_inst.connect(ap_rst, rst)

        wrapped_module_inst.connect(ap_start,   stream_req_bus_genfifo_req_i)
        wrapped_module_inst.connect(ap_done,    stream_resp_bus_genfifo_req_o)
        wrapped_module_inst.connect(ap_ready,   stream_req_bus_genfifo_ack_o)
        wrapped_module_inst.connect(datain,     datain_drv)
        wrapped_module_inst.connect(ap_return,  dataout_drv)

        MSG("Generating submodule instance: done")

        NEWLINE()
        MSG("##################################################")
        MSG("#### Cyclix: RTL wrapper generation complete! ####")
        MSG("#### module: " + name)
        MSG("##################################################")

        return wrapper_rtl_gen
    }
}
