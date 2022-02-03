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

internal data class Exu_CFG(val ExecUnit : Exu,
                   val exu_num : Int,
                   val iq_length : Int,
                   val pref_impl : STREAM_PREF_IMPL)

internal open class reordex_import_expr_context(var_dict : MutableMap<hw_var, hw_var>,
                                                var ExUnit : Exu_CFG) : import_expr_context(var_dict)

open class MultiExuCoproc(val name : String, val MultiExu_CFG : Reordex_CFG, val io_iq_size : Int) {

    internal var ExecUnits  = mutableMapOf<String, Exu_CFG>()

    fun add_exu(exu : Exu, exu_num: Int, iq_length: Int, pref_impl : STREAM_PREF_IMPL) {
        if (ExecUnits.put(exu.name, Exu_CFG(exu, exu_num, iq_length, pref_impl)) != null) {
            ERROR("Exu addition error!")
        }
    }

    internal fun reconstruct_expression(debug_lvl : DEBUG_LEVEL,
                                        cyclix_gen : hw_astc,
                                        expr : hw_exec,
                                        context : import_expr_context) {

        cyclix_gen as cyclix.Streaming
        context as reordex_import_expr_context

        if (expr.opcode == OP1_ASSIGN) {
            if (expr.dsts[0] == context.ExUnit.ExecUnit.curinstr_addr) {
                cyclix_gen.ERROR("curinstr_addr cannot be written!")
            }
            if (expr.dsts[0] == context.ExUnit.ExecUnit.nextinstr_addr) {
                cyclix_gen.assign(TranslateVar(context.ExUnit.ExecUnit.resp_data, context.var_dict).GetFracRef("branch_req"), 1)
                cyclix_gen.assign(TranslateVar(context.ExUnit.ExecUnit.resp_data, context.var_dict).GetFracRef("branch_vec"), TranslateParam(expr.params[0], context.var_dict))
            }
        }

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

        var INSTR_IO_ID_WIDTH =
            if (this is MultiExuRISC) GetWidthToContain(FETCH_BUF_SIZE + IREQ_BUF_SIZE)
            else 0

        for (ExUnit in ExecUnits) EXU_NUM += ExUnit.value.exu_num
        CDB_NUM = EXU_NUM
        if (MultiExu_CFG.mode == REORDEX_MODE.RISC) {
            RISC_LSU_NUM = 1
            CDB_RISC_LSU_POS = CDB_NUM
            CDB_NUM += RISC_LSU_NUM
        }

        var exu_descrs = mutableMapOf<String, __exu_descr>()
        var exu_rst = cyclix_gen.ulocal("genexu_rst", 0, 0, "0")
        var control_structures =
            if (MultiExu_CFG.REG_MGMT is REG_MGMT_SCOREBOARDING) __control_structures_scoreboarding(cyclix_gen, MultiExu_CFG, CDB_NUM, ExecUnits, exu_descrs, exu_rst)
            else __control_structures_renaming(cyclix_gen, MultiExu_CFG, CDB_NUM, ExecUnits, exu_descrs, exu_rst)

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
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) rob(cyclix_gen, "genrob", MultiExu_CFG.ROB_size, MultiExu_CFG, CDB_NUM, control_structures)
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
            instr_fetch = instr_fetch_buffer(name, cyclix_gen, (this as MultiExuRISC).FETCH_BUF_SIZE, (this as MultiExuRISC), MultiExu_CFG, ExecUnits, control_structures, CDB_NUM, INSTR_IO_ID_WIDTH)
            instr_fetch.var_dict.put(this.RISCDecode.CSR_MCAUSE, cyclix_CSR_MCAUSE)
            instr_req = instr_req_stage(name, cyclix_gen, (this as MultiExuRISC).IREQ_BUF_SIZE, INSTR_IO_ID_WIDTH, MultiExu_CFG, busreq_mem_struct, control_structures)
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

            // initializing instr addrs
            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.curinstr_addr, new_exu_descr.var_dict), TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict).GetFracRef("curinstr_addr"))
            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.nextinstr_addr, new_exu_descr.var_dict), TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict).GetFracRef("nextinstr_addr"))

            // default branching
            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict).GetFracRef("branch_req"), 0)
            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict).GetFracRef("branch_vec"), 0)

            for (expr in ExUnit.value.ExecUnit[0].expressions) {
                reconstruct_expression(debug_lvl,
                    exu_cyclix_gen,
                    expr,
                    reordex_import_expr_context(new_exu_descr.var_dict, ExUnit.value))
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
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) io_buffer_coproc(cyclix_gen, "genstore", 0, "genstore", io_iq_size, MultiExu_CFG, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), false, fu_num, CDB_NUM, busreq_mem_struct, cdb.GetFracRef(CDB_RISC_LSU_POS))
            else io_buffer_risc(cyclix_gen, "geniq_LSU", 0, "geniq_LSU", io_iq_size, MultiExu_CFG, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), false, CDB_RISC_LSU_POS, CDB_NUM, busreq_mem_struct, cdb.GetFracRef(CDB_RISC_LSU_POS))
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
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) dispatch_buffer(cyclix_gen, "gendispatch", 1, MultiExu_CFG, ExecUnits.size, CDB_NUM, IQ_insts, control_structures)
            else dispatch_buffer_risc(cyclix_gen, "gendispatch", (this as MultiExuRISC).RENAME_BUF_SIZE, MultiExu_CFG, ExecUnits.size, CDB_NUM, IQ_insts, control_structures)

        cyclix_gen.MSG_COMMENT("ROB committing...")
        if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) rob.Commit(control_structures)
        else {
            var bufs_to_reset = ArrayList<hw_stage>()
            bufs_to_reset.add(rob)
            for (IQ_inst in IQ_insts) {
                bufs_to_reset.add(IQ_inst)
                if (IQ_inst is io_buffer_risc) bufs_to_reset.add(IQ_inst.resp_buf)
            }
            bufs_to_reset.add(dispatch_uop_buf)
            bufs_to_reset.add(instr_fetch)
            bufs_to_reset.add(instr_req)
            (rob as rob_risc).Commit(instr_iaddr as instr_iaddr_stage, bufs_to_reset, (IQ_insts as ArrayList<hw_stage>), MRETADDR, cyclix_CSR_MCAUSE)
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

                        if (control_structures is __control_structures_scoreboarding) control_structures.WriteARF(exu_cdb_inst_tag, exu_cdb_inst_wdata)
                        else if (control_structures is __control_structures_renaming) control_structures.WritePRF(exu_cdb_inst_tag, exu_cdb_inst_wdata)
                        else ERROR("Configuration inconsistent!")

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

open class MultiExuRISC(name : String, MultiExu_CFG : Reordex_CFG, var RISCDecode : RISCDecoder, val IREQ_BUF_SIZE : Int, val FETCH_BUF_SIZE : Int, val RENAME_BUF_SIZE : Int, io_iq_size : Int)
    : MultiExuCoproc(name, MultiExu_CFG, io_iq_size) {
}
