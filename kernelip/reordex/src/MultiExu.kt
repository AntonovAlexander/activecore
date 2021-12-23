/*
 * MultiExu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*
import cyclix.*

enum class REORDEX_MODE {
    COPROCESSOR,
    RISC
}

abstract class REG_MGMT_MODE()
class REG_MGMT_SCOREBOARD() : REG_MGMT_MODE()
class REG_MGMT_RENAMING(val PRF_depth: Int) : REG_MGMT_MODE()

open class Reordex_CFG(val RF_width : Int,
                       val ARF_depth : Int,
                       val DataPath_width : Int,
                       val REG_MGMT: REG_MGMT_MODE,
                       val ROB_size : Int,
                       val mode : REORDEX_MODE) {

    internal val trx_inflight_num = ROB_size * DataPath_width;

    internal val ARF_addr_width = GetWidthToContain(ARF_depth)
    internal val PRF_addr_width = GetWidthToContain((REG_MGMT as REG_MGMT_RENAMING).PRF_depth)

    internal var req_struct = hw_struct("req_struct")
    internal var resp_struct = hw_struct("resp_struct")

    internal var src_imms = ArrayList<hw_var>()
    fun AddSrcImm(name : String, new_type : hw_type) : hw_var {
        var new_var = hw_var(name, new_type, "0")
        req_struct.add(name, new_type, "0")
        src_imms.add(new_var)
        return new_var
    }
    fun AddSrcUImm(name : String, new_width : Int) : hw_var {
        return AddSrcImm(name, hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(new_width)))
    }
    fun AddSrcSImm(name : String, new_width : Int) : hw_var {
        return AddSrcImm(name, hw_type(DATA_TYPE.BV_SIGNED, hw_dim_static(new_width)))
    }

    internal var dst_imms = ArrayList<hw_var>()
    internal fun AddDstImm(name : String, new_type : hw_type) : hw_var {
        var new_var = hw_var(name, new_type, "0")
        resp_struct.add(name, new_type, "0")
        dst_imms.add(new_var)
        return new_var
    }
    internal fun AddDstUImm(name : String, new_width : Int) : hw_var {
        return AddDstImm(name, hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(new_width)))
    }
    internal fun AddDstSImm(name : String, new_width : Int) : hw_var {
        return AddDstImm(name, hw_type(DATA_TYPE.BV_SIGNED, hw_dim_static(new_width)))
    }

    internal var srcs = ArrayList<hw_var>()
    fun AddSrc() : Src {
        var new_src = Src("src" + srcs.size, RF_width-1, 0, "0")
        req_struct.addu("src" + srcs.size + "_data", RF_width-1, 0, "0")
        srcs.add(new_src)
        return new_src
    }

    internal var rds = ArrayList<hw_var>()
    fun AddRd() : hw_var {
        var new_var = hw_var("rd" + rds.size, RF_width-1, 0, "0")

        req_struct.addu("rd" + rds.size + "_req", 0, 0, "0")
        req_struct.addu("rd" + rds.size + "_tag", RF_width-1, 0, "0")

        resp_struct.addu("rd" + rds.size + "_req",     0, 0, "0")      // TODO: clean up size
        resp_struct.addu("rd" + rds.size + "_tag",     31, 0, "0")      // TODO: clean up size
        resp_struct.addu("rd" + rds.size + "_wdata",   RF_width-1, 0, "0")

        rds.add(new_var)

        return new_var
    }

    internal var alu_CF          = DUMMY_VAR
    internal var alu_SF          = DUMMY_VAR
    internal var alu_ZF          = DUMMY_VAR
    internal var alu_OF          = DUMMY_VAR

    init {
        req_struct.addu("trx_id",   31, 0, "0")      // TODO: clean up size
        resp_struct.addu("trx_id",  31, 0, "0")      // TODO: clean up size

        if (mode == REORDEX_MODE.RISC) {
            alu_CF          = AddDstUImm("alu_CF", 1)
            alu_SF          = AddDstUImm("alu_SF", 1)
            alu_ZF          = AddDstUImm("alu_ZF", 1)
            alu_OF          = AddDstUImm("alu_OF", 1)
        }
    }
}

val OP_SRC_SET_IMM = hw_opcode("src_set_imm")
class hw_exec_src_set_imm(val src : Src, val imm : hw_param) : hw_exec(OP_SRC_SET_IMM)

val OP_SRC_RD_REG = hw_opcode("src_rd_reg")
class hw_exec_src_rd_reg(val src : Src, val raddr : hw_param) : hw_exec(OP_SRC_RD_REG)

open class RISCDecodeContainer (MultiExu_CFG : Reordex_CFG) : hw_astc_stdif() {

    var RootExec = hw_exec(hw_opcode("RISCDecode"))

    init {
        add(RootExec)
    }
}

open class RISCDecoder (MultiExu_CFG : Reordex_CFG) : RISCDecodeContainer(MultiExu_CFG) {

    var instr_code = ugenvar("instr_code", 31, 0, "0")

    // op1 sources
    val OP0_SRC_RS      = 0
    val OP0_SRC_IMM     = 1
    val OP0_SRC_PC 	    = 2
    // op2 sources
    val OP1_SRC_RS      = 0
    val OP1_SRC_IMM     = 1
    val OP1_SRC_CSR     = 2

    // rd sources
    val RD_LUI		    = 0
    val RD_ALU		    = 1
    val RD_CF_COND	    = 2
    val RD_OF_COND	    = 3
    val RD_PC_INC	    = 4
    val RD_MEM		    = 5
    val RD_CSR		    = 6

    // jmp sources
    val JMP_SRC_IMM     = 0
    val JMP_SRC_ALU     = 1

    val curinstr_addr   = ugenvar("curinstr_addr_decoder", 31, 0, "0")

    var branchctrl = Branchctrl(
        ugenvar("genbranch_req", 0, 0, "0"),
        ugenvar("genbranch_req_cond", 0, 0, "0"),
        ugenvar("genbranch_src", 0, 0, JMP_SRC_IMM.toString()),
        ugenvar("genbranch_vector", 31, 0, "0"),
        ugenvar("genbranch_mask", 2, 0, "0")
    )

    // regfile control signals
    var rsctrls = mutableMapOf<hw_var, RISCDecoder_rs>()
    var rdctrls = mutableMapOf<hw_var, RISCDecoder_rd>()

    var csr_rdata       = ugenvar("csr_rdata", 31, 0, "0")
    var immediate       = ugenvar("immediate", 31, 0, "0")
    var curinstraddr_imm    = ugenvar("curinstraddr_imm", 31, 0, "0")

    var fencereq        = ugenvar("fencereq", 0, 0, "0")
    var pred            = ugenvar("pred", 3, 0, "0")
    var succ            = ugenvar("succ", 3, 0, "0")

    var ecallreq        = ugenvar("ecallreq", 0, 0, "0")
    var ebreakreq       = ugenvar("ebreakreq", 0, 0, "0")

    var csrreq          = ugenvar("csrreq", 0, 0, "0")
    var csrnum          = ugenvar("csrnum", 11, 0, "0")

    var exu_req         = ugenvar("exu_req", 0, 0, "0")

    var memctrl         = RISCDecoder_memctrl(
        ugenvar("mem_req", 0, 0, "0"),
        ugenvar("mem_cmd", 0, 0, "0"),
        ugenvar("mem_addr", 31, 0, "0"),
        ugenvar("mem_be", 3, 0, "0"),
        ugenvar("mem_wdata", 31, 0, "0"),
        ugenvar("mem_rdata", 31, 0, "0"),
        ugenvar("mem_rshift", 0, 0, "0"),
        ugenvar("load_signext", 0, 0, "0")
    )

    var mret_req        = ugenvar("mret_req", 0, 0, "0")
    var MRETADDR        = ugenvar("MRETADDR", 31, 0, "0")

    //////////
    internal var rss_ctrl = ArrayList<RISCDecoder_rs_ctrl>()
    internal var rds_ctrl = ArrayList<RISCDecoder_rd_ctrl>()

    var CSR_MCAUSE      = hw_var("CSR_MCAUSE", 7, 0, "0")

    init {
        for (rs_idx in 0 until MultiExu_CFG.srcs.size) {

            rsctrls.put(MultiExu_CFG.srcs[rs_idx], RISCDecoder_rs(
                ugenvar("rs" + rs_idx + "_req", 0, 0, "0"),
                ugenvar("rs" + rs_idx + "_addr",  MultiExu_CFG.ARF_addr_width-1, 0, "0"),
                ugenvar("rs" + rs_idx + "_rdata", MultiExu_CFG.RF_width-1, 0, "0")
            ))

            rss_ctrl.add(
                RISCDecoder_rs_ctrl(
                    ugenvar("rs" + rs_idx + "_rdy", 0, 0, "0"),
                    ugenvar("rs" + rs_idx + "_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")
                )
            )
        }
        for (rd_idx in 0 until MultiExu_CFG.rds.size) {

            rdctrls.put(MultiExu_CFG.rds[rd_idx], RISCDecoder_rd(
                ugenvar("rd" + rd_idx + "_req", 0, 0, "0"),
                ugenvar("rd" + rd_idx + "_source", 2, 0, RD_ALU.toString()),
                ugenvar("rd" + rd_idx + "_addr", 4, 0, "0"),
                ugenvar("rd" + rd_idx + "_wdata", 31, 0, "0"),
                ugenvar("rd" + rd_idx + "_rdy", 0, 0, "0")
            ))

            rds_ctrl.add(
                RISCDecoder_rd_ctrl(
                    ugenvar("rd" + rd_idx + "_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")
                )
            )
        }

        for (src_imm in MultiExu_CFG.src_imms)  src_imm.default_astc = this
        for (src in MultiExu_CFG.srcs)          src.default_astc = this
        for (dst_imm in MultiExu_CFG.dst_imms)  dst_imm.default_astc = this
        for (rd in MultiExu_CFG.rds)            rd.default_astc = this

    }

    fun SrcSetImm(src : Src, imm : hw_param) {
        AddExpr(hw_exec_src_set_imm(src, imm))
    }

    fun SrcReadReg(src : Src, raddr : hw_param) {
        AddExpr(hw_exec_src_rd_reg(src, raddr))
    }

}

data class Exu_CFG(val ExecUnit : Exu,
                   val exu_num : Int,
                   val iq_length : Int,
                   val pref_impl : STREAM_PREF_IMPL)

open class MultiExuCoproc(val name : String, val MultiExu_CFG : Reordex_CFG, val io_iq_size : Int) {

    var ExecUnits  = mutableMapOf<String, Exu_CFG>()

    fun add_exu(exu : Exu, exu_num: Int, iq_length: Int, pref_impl : STREAM_PREF_IMPL) {
        if (ExecUnits.put(exu.name, Exu_CFG(exu, exu_num, iq_length, pref_impl)) != null) {
            ERROR("Exu addition error!")
        }
    }

    fun reconstruct_expression(debug_lvl : DEBUG_LEVEL,
                               cyclix_gen : hw_astc,
                               expr : hw_exec,
                               context : import_expr_context) {

        cyclix_gen as cyclix.Streaming

        cyclix_gen.import_expr(debug_lvl, expr, context, ::reconstruct_expression)
    }

    fun translate_to_cyclix(debug_lvl : DEBUG_LEVEL) : cyclix.Generic {

        NEWLINE()
        MSG("################################################")
        MSG("#### Starting Reordex-to-Cyclix translation ####")
        MSG("#### module: " + name)
        MSG("################################################")

        var cyclix_gen = cyclix.Generic(name)

        MSG("generating control structures...")

        var EXU_NUM = 0
        var RISC_LSU_NUM = 0
        var CDB_NUM = 0

        var CDB_RISC_LSU_POS = 0

        var IREQ_BUF_SIZE = 1
        var FETCH_BUF_SIZE = 4
        var RENAME_BUF_SIZE = 2
        var INSTR_IO_ID_WIDTH = GetWidthToContain(FETCH_BUF_SIZE + IREQ_BUF_SIZE)

        for (ExUnit in ExecUnits) EXU_NUM += ExUnit.value.exu_num
        CDB_NUM = EXU_NUM
        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
            RISC_LSU_NUM = 1
            CDB_RISC_LSU_POS = CDB_NUM
            CDB_NUM += RISC_LSU_NUM
        }

        var exu_descrs = mutableMapOf<String, __exu_descr>()
        var exu_rst = cyclix_gen.ulocal("genexu_rst", 0, 0, "0")
        var control_structures = __control_structures_renaming(cyclix_gen, MultiExu_CFG, CDB_NUM, ExecUnits, exu_descrs, exu_rst)

        MSG("generating control structures: done")

        MSG("generating internal structures...")

        var cdb_struct  = hw_struct("cdb_struct")
        cdb_struct.addu("enb", 0, 0, "0")
        cdb_struct.add("data", MultiExu_CFG.resp_struct)
        var cdb = cyclix_gen.local("gencdb", cdb_struct, hw_dim_static(CDB_NUM-1, 0))       // Common Data Bus
        var io_cdb_buf =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) DUMMY_VAR
            else cyclix_gen.global("io_cdb_buf", cdb_struct)
        var io_cdb_rs1_wdata_buf =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) DUMMY_VAR
            else cyclix_gen.uglobal("io_cdb_rs1_wdata_buf", MultiExu_CFG.RF_width-1, 0, "0")

        var rob =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) rob(cyclix_gen, "genrob", MultiExu_CFG.ROB_size, MultiExu_CFG, CDB_NUM)
            else rob_risc(name, cyclix_gen, "genrob", MultiExu_CFG.ROB_size, MultiExu_CFG, CDB_NUM, control_structures)

        var TranslateInfo = __TranslateInfo()

        var IQ_insts = ArrayList<iq_buffer>()
        var ExUnits_insts = ArrayList<ArrayList<cyclix.hw_subproc>>()

        var exu_req     = cyclix_gen.local(cyclix_gen.GetGenName("exu_req"), MultiExu_CFG.req_struct)
        var exu_resp    = cyclix_gen.local(cyclix_gen.GetGenName("exu_resp"), MultiExu_CFG.resp_struct)

        var MRETADDR =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) DUMMY_VAR
            else cyclix_gen.uglobal("MRETADDR", 31, 0, "0")

        // CSRs
        var cyclix_CSR_MCAUSE =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) DUMMY_VAR
            else cyclix_gen.uglobal("CSR_MCAUSE", 7, 0, "0")

        // busreq
        var busreq_mem_struct = hw_struct(name + "_busreq_mem_struct")
        busreq_mem_struct.addu("addr",     31, 0, "0")
        busreq_mem_struct.addu("be",       3,  0, "0")
        busreq_mem_struct.addu("wdata",    31, 0, "0")

        MSG("generating internal structures: done")

        var instr_fetch = (rob as hw_stage)
        var instr_req = (rob as hw_stage)
        var instr_iaddr = (rob as hw_stage)
        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
            instr_fetch = instr_fetch_buffer(name, cyclix_gen, FETCH_BUF_SIZE, (this as MultiExuRISC), MultiExu_CFG, control_structures, CDB_NUM, INSTR_IO_ID_WIDTH)
            instr_fetch.var_dict.put(this.RISCDecode.CSR_MCAUSE, cyclix_CSR_MCAUSE)
            instr_req = instr_req_stage(name, cyclix_gen, INSTR_IO_ID_WIDTH, MultiExu_CFG, busreq_mem_struct)
            instr_iaddr = instr_iaddr_stage(name, cyclix_gen, MultiExu_CFG)
        }

        var ExUnit_idx = 0
        var fu_num = 0
        for (ExUnit in ExecUnits) {
            MSG("## generating execution unit: " + ExUnit.value.ExecUnit.name + "... ##")

            var new_exu_descr = __exu_descr(mutableMapOf(), ArrayList(), ArrayList())

            for (ExUnit_num in 0 until ExUnit.value.exu_num) {
                var iq_buf = iq_buffer(cyclix_gen, ExUnit.key, ExUnit_num, "geniq_" + ExUnit.key + "_" + ExUnit_num, ExUnit.value.iq_length, MultiExu_CFG, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), true, fu_num, CDB_NUM)
                new_exu_descr.IQ_insts.add(iq_buf)
                IQ_insts.add(iq_buf)
                fu_num++
            }

            MSG("generating submodules...")
            var exu_cyclix_gen = cyclix.Streaming("genexu_" + ExUnit.value.ExecUnit.name, MultiExu_CFG.req_struct, MultiExu_CFG.resp_struct, ExUnit.value.pref_impl)
            MSG("generating submodules: done")

            MSG("generating locals...")
            for (local in ExUnit.value.ExecUnit.locals)
                new_exu_descr.var_dict.put(local, exu_cyclix_gen.local(local.name, local.vartype, local.defimm))
            for (imm_num in 0 until ExUnit.value.ExecUnit.src_imms.size)
                new_exu_descr.var_dict.put(MultiExu_CFG.src_imms[imm_num], new_exu_descr.var_dict[ExUnit.value.ExecUnit.src_imms[imm_num]!!]!!)
            for (src_num in 0 until ExUnit.value.ExecUnit.srcs.size)
                new_exu_descr.var_dict.put(MultiExu_CFG.srcs[src_num], new_exu_descr.var_dict[ExUnit.value.ExecUnit.srcs[src_num]!!]!!)
            for (rd_num in 0 until ExUnit.value.ExecUnit.rds.size)
                new_exu_descr.var_dict.put(MultiExu_CFG.rds[rd_num], new_exu_descr.var_dict[ExUnit.value.ExecUnit.rds[rd_num]!!]!!)
            MSG("generating locals: done")

            MSG("generating globals...")
            for (global in ExUnit.value.ExecUnit.globals)
                new_exu_descr.var_dict.put(global, exu_cyclix_gen.global(global.name, global.vartype, global.defimm))
            MSG("generating globals: done")

            MSG("generating intermediates...")
            for (genvar in ExUnit.value.ExecUnit[0].genvars)
                new_exu_descr.var_dict.put(genvar, exu_cyclix_gen.local(genvar.name, genvar.vartype, genvar.defimm))
            MSG("generating intermediates: done")

            MSG("generating DstIms...")
            for (dst_imm in MultiExu_CFG.dst_imms)
                new_exu_descr.var_dict.put(dst_imm, TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict).GetFracRef(dst_imm.name))
            MSG("generating DstIms: done")

            MSG("generating logic...")

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict), exu_cyclix_gen.stream_req_var)

            for (imm_num in 0 until MultiExu_CFG.src_imms.size) {
                exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.src_imms[imm_num], new_exu_descr.var_dict), exu_cyclix_gen.subStruct((TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict)), MultiExu_CFG.src_imms[imm_num].name))
            }
            for (src_num in 0 until MultiExu_CFG.srcs.size) {
                exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.srcs[src_num], new_exu_descr.var_dict), exu_cyclix_gen.subStruct((TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict)), "src" + src_num + "_data"))
            }

            for (expr in ExUnit.value.ExecUnit[0].expressions) {
                reconstruct_expression(debug_lvl,
                    exu_cyclix_gen,
                    expr,
                    import_expr_context(new_exu_descr.var_dict))
            }

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict).GetFracRef("rd0_wdata"), TranslateVar(ExUnit.value.ExecUnit.rds[0], new_exu_descr.var_dict) )

            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var, TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict))
            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var.GetFracRef("rd0_req"), exu_cyclix_gen.stream_req_var.GetFracRef("rd0_req"))        // TODO: fix
            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var.GetFracRef("rd0_tag"), exu_cyclix_gen.stream_req_var.GetFracRef("rd0_tag"))        // TODO: fix
            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var.GetFracRef("trx_id"), exu_cyclix_gen.stream_req_var.GetFracRef("trx_id"))

            exu_cyclix_gen.end()

            MSG("generating logic: done")

            MSG("generating submodule instances...")
            var ExUnit_insts = ArrayList<cyclix.hw_subproc>()
            for (exu_num in 0 until ExUnit.value.exu_num) {
                var exu_inst = cyclix_gen.subproc(exu_cyclix_gen.name + "_" + exu_num, exu_cyclix_gen)
                exu_inst.AddResetDriver(exu_rst)
                ExUnit_insts.add(exu_inst)
            }
            ExUnits_insts.add(ExUnit_insts)

            var exu_info = __exu_info(
                exu_cyclix_gen,
                exu_req,
                exu_resp
            )

            TranslateInfo.exu_assocs.put(ExUnit.value.ExecUnit, exu_info)
            MSG("generating submodule instances: done")

            for (src_num in 0 until ExUnit.value.ExecUnit.srcs.size)
                if (new_exu_descr.var_dict[ExUnit.value.ExecUnit.srcs[src_num]!!]!!.read_done) {
                    MSG("Exu waits for: " + ExUnit.value.ExecUnit.srcs[src_num]!!.name)
                    new_exu_descr.rs_use_flags.add(true)
                } else {
                    new_exu_descr.rs_use_flags.add(false)
                }
            exu_descrs.put(ExUnit.key, new_exu_descr)

            ExUnit_idx++

            MSG("## generating execution unit " + ExUnit.value.ExecUnit.name + ": done ##")
        }

        MSG("generating I/O IQ...")
        var io_iq =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) io_buffer(cyclix_gen, "genstore", 0, "genstore", io_iq_size, MultiExu_CFG, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), false, fu_num, CDB_NUM, busreq_mem_struct, cdb.GetFracRef(CDB_RISC_LSU_POS))
            else io_buffer(cyclix_gen, "genlsu", 0, "genlsu", io_iq_size, MultiExu_CFG, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), false, CDB_RISC_LSU_POS, CDB_NUM, busreq_mem_struct, cdb.GetFracRef(CDB_RISC_LSU_POS))
        IQ_insts.add(io_iq)
        MSG("generating I/O IQ: done")

        MSG("generating logic...")

        cyclix_gen.MSG_COMMENT("Initializing CDB...")
        var exu_cdb_num = 0
        for (exu_num in 0 until ExUnits_insts.size) {
            for (exu_inst_num in 0 until ExUnits_insts[exu_num].size) {
                var exu_cdb_inst        = cdb.GetFracRef(exu_cdb_num)
                var exu_cdb_inst_enb    = exu_cdb_inst.GetFracRef("enb")
                var exu_cdb_inst_data   = exu_cdb_inst.GetFracRef("data")

                cyclix_gen.assign(exu_cdb_inst_enb, cyclix_gen.fifo_internal_rd_unblk(ExUnits_insts[exu_num][exu_inst_num], cyclix.STREAM_RESP_BUS_NAME, exu_cdb_inst_data))
                exu_cdb_num++
            }
        }
        cyclix_gen.MSG_COMMENT("Initializing CDB: done")

        var dispatch_uop_buf =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) dispatch_buffer(cyclix_gen, "gendispatch", 1, MultiExu_CFG, ExecUnits.size, CDB_NUM, IQ_insts)
            else dispatch_buffer_risc(cyclix_gen, "gendispatch", RENAME_BUF_SIZE, MultiExu_CFG, ExecUnits.size, CDB_NUM, IQ_insts)

        cyclix_gen.MSG_COMMENT("ROB committing...")
        if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) rob.Commit(control_structures)
        else {
            var bufs_to_reset = ArrayList<hw_stage>()
            bufs_to_reset.add(rob)
            for (IQ_inst in IQ_insts) bufs_to_reset.add(IQ_inst)
            bufs_to_reset.add(dispatch_uop_buf)
            bufs_to_reset.add(instr_fetch)
            bufs_to_reset.add(instr_req)
            (rob as rob_risc).Commit(control_structures, (instr_iaddr as instr_iaddr_stage).pc, bufs_to_reset, (IQ_insts as ArrayList<hw_stage>), MRETADDR, cyclix_CSR_MCAUSE)
        }
        cyclix_gen.MSG_COMMENT("ROB committing: done")

        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
            cyclix_gen.MSG_COMMENT("Deactivating CDB in case of backoff...")
            cyclix_gen.begif(control_structures.exu_rst)
            run {
                for (cdb_idx in 0 until cdb.GetWidth()) {
                    cyclix_gen.assign(cdb.GetFracRef(cdb_idx).GetFracRef("enb"), 0)
                }
            }; cyclix_gen.endif()
            cyclix_gen.MSG_COMMENT("Deactivating CDB in case of backoff: done")
        }

        io_iq.ProcessIO(io_cdb_buf, io_cdb_rs1_wdata_buf, rob)

        rob.FillFromCDB(MultiExu_CFG, cdb, io_cdb_rs1_wdata_buf)

        var fu_id = 0
        for (ExUnit in ExecUnits) {
            for (ExUnit_num in 0 until ExUnit.value.exu_num) {

                cyclix_gen.MSG_COMMENT("IQ processing: ExUnit: " + ExUnit.key + ", instance num: " + ExUnit_num)

                var IQ_inst = exu_descrs[ExUnit.key]!!.IQ_insts[ExUnit_num]
                IQ_inst.preinit_ctrls()
                IQ_inst.init_locals()

                IQ_inst.Issue(ExUnit.value, exu_req, ExUnits_insts[fu_id][ExUnit_num], ExUnit_num)

            }
            fu_id++
        }

        cyclix_gen.MSG_COMMENT("broadcasting FU results to IQ and dispatch buffer...")
        for (cdb_idx in 0 until CDB_NUM) {

            var exu_cdb_inst        = cdb.GetFracRef(cdb_idx)
            var exu_cdb_inst_enb    = exu_cdb_inst.GetFracRef("enb")
            var exu_cdb_inst_data   = exu_cdb_inst.GetFracRef("data")

            cyclix_gen.begif(exu_cdb_inst_enb)
            run {

                for (rd_idx in 0 until MultiExu_CFG.rds.size) {

                    var exu_cdb_inst_req    = exu_cdb_inst_data.GetFracRef("rd" + rd_idx + "_req")
                    var exu_cdb_inst_tag    = exu_cdb_inst_data.GetFracRef("rd" + rd_idx + "_tag")
                    var exu_cdb_inst_wdata  = exu_cdb_inst_data.GetFracRef("rd" + rd_idx + "_wdata")

                    cyclix_gen.begif(exu_cdb_inst_req)
                    run {

                        control_structures.WritePRF(exu_cdb_inst_tag, exu_cdb_inst_wdata)

                        // broadcasting FU results to dispatch buffer
                        for (dispatch_uop_buf_idx in 0 until dispatch_uop_buf.TRX_BUF_SIZE) {
                            var dispatch_uop_buf_entries = dispatch_uop_buf.TRX_BUF.GetFracRef(dispatch_uop_buf_idx)

                            for (dispatch_uop_buf_single_entry_idx in 0 until dispatch_uop_buf_entries.GetWidth()) {
                                var dispatch_uop_buf_entry = dispatch_uop_buf_entries.GetFracRef(dispatch_uop_buf_single_entry_idx)

                                for (RF_rs_idx in 0 until MultiExu_CFG.srcs.size) {

                                    var src_rdy     = dispatch_uop_buf_entry.GetFracRef("src" + RF_rs_idx + "_rdy")
                                    var src_tag     = dispatch_uop_buf_entry.GetFracRef("src" + RF_rs_idx + "_tag")
                                    var src_data    = dispatch_uop_buf_entry.GetFracRef("src" + RF_rs_idx + "_data")

                                    cyclix_gen.begif(!src_rdy)
                                    run {
                                        cyclix_gen.begif(cyclix_gen.eq2(src_tag, exu_cdb_inst_tag))
                                        run {
                                            // setting IQ entry ready
                                            cyclix_gen.assign(src_data, exu_cdb_inst_wdata)
                                            cyclix_gen.assign(src_rdy, 1)
                                        }; cyclix_gen.endif()
                                    }; cyclix_gen.endif()
                                }

                                //// setting rdy for io_req if data generated ////
                                cyclix_gen.begif(dispatch_uop_buf_entry.GetFracRef("io_req"))
                                run {
                                    cyclix_gen.assign(dispatch_uop_buf_entry.GetFracRef("rdy"), dispatch_uop_buf_entry.GetFracRef("src0_rdy"))
                                }; cyclix_gen.endif()
                            }
                        }

                    }; cyclix_gen.endif()
                }

            }; cyclix_gen.endif()
        }

        // broadcasting FU results to IQ
        for (IQ_inst in IQ_insts) IQ_inst.FillFromCDB(cdb)

        cyclix_gen.MSG_COMMENT("broadcasting FU results to IQ and dispatch buffer: done")

        dispatch_uop_buf.Process(rob, control_structures.PRF_src, io_iq, ExecUnits, CDB_RISC_LSU_POS)

        if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) {
            var frontend = coproc_frontend(name, cyclix_gen, MultiExu_CFG, control_structures)
            frontend.Send_toRenameBuf(dispatch_uop_buf)

        } else {            // MultiExu_CFG.mode == REORDEX_MODE.RISC
            (instr_fetch as instr_fetch_buffer).Process(dispatch_uop_buf, MRETADDR, (this as MultiExuRISC).RISCDecode.CSR_MCAUSE)
            (instr_req as instr_req_stage).Process(instr_fetch)
            (instr_iaddr as instr_iaddr_stage).Process(instr_req)
            //(instr_iaddr as instr_iaddr_stage).ProcessSingle(instr_req)
        }

        cyclix_gen.end()

        MSG("generating logic: done")

        MSG("#################################################")
        MSG("#### Reordex-to-Cyclix translation complete! ####")
        MSG("#### module: " + name)
        MSG("#################################################")

        return cyclix_gen
    }
}

open class MultiExuRISC(name : String, MultiExu_CFG : Reordex_CFG, io_iq_size : Int, var RISCDecode : RISCDecoder)
    : MultiExuCoproc(name, MultiExu_CFG, io_iq_size) {
}
