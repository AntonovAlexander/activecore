/*
 * MultiExu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import cyclix.STREAM_PREF_IMPL
import hwast.*

enum class REORDEX_MODE {
    COPROCESSOR,
    RISC
}

open class Reordex_CFG(val RF_width : Int,
                       val ARF_depth : Int,
                       val rename_RF: Boolean,
                       val PRF_depth : Int,
                       val trx_inflight_num : Int,
                       val mode : REORDEX_MODE) {

    val ARF_addr_width = GetWidthToContain(ARF_depth)
    val PRF_addr_width = GetWidthToContain(PRF_depth)

    var req_struct = hw_struct("req_struct")
    var resp_struct = hw_struct("resp_struct")

    var imms = ArrayList<hw_var>()
    fun AddImm(name : String, new_type : hw_type) : hw_var {
        var new_var = hw_var(name, new_type, "0")
        req_struct.add(name, new_type, "0")
        imms.add(new_var)
        return new_var
    }
    fun AddUImm(name : String, new_width : Int) : hw_var {
        return AddImm(name, hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(new_width)))
    }
    fun AddSImm(name : String, new_width : Int) : hw_var {
        return AddImm(name, hw_type(DATA_TYPE.BV_SIGNED, hw_dim_static(new_width)))
    }

    var rss = ArrayList<hw_var>()
    fun AddRs() : hw_var {
        var new_var = hw_var("rs" + rss.size, RF_width-1, 0, "0")
        req_struct.addu("rs" + rss.size + "_rdata", RF_width-1, 0, "0")
        rss.add(new_var)
        return new_var
    }

    init {
        req_struct.addu("trx_id",     31, 0, "0")       // TODO: clean up
        req_struct.addu("rd_tag",     31, 0, "0")       // TODO: clean up
        resp_struct.addu("trx_id",     31, 0, "0")      // TODO: clean up
        resp_struct.addu("tag",     31, 0, "0")         // TODO: clean up
        resp_struct.addu("wdata",     RF_width-1, 0, "0")
    }
}

data class Exu_CFG(val ExecUnit : Exu,
                   val exu_num : Int,
                   val iq_length : Int,
                   val pref_impl : STREAM_PREF_IMPL)

open class MultiExu(val name : String, val MultiExu_CFG : Reordex_CFG, val out_iq_size : Int) {

    var ExecUnits  = mutableMapOf<String, Exu_CFG>()

    fun add_exu(exu : Exu, exu_num: Int, iq_length: Int, pref_impl : STREAM_PREF_IMPL) {
        if (ExecUnits.put(exu.name, Exu_CFG(exu, exu_num, iq_length, pref_impl)) != null) {
            ERROR("Exu addition error!")
        }
    }

    fun reconstruct_expression(DEBUG_FLAG : Boolean,
                               cyclix_gen : hw_astc,
                               expr : hw_exec,
                               context : import_expr_context) {

        cyclix_gen as cyclix.Streaming

        cyclix_gen.import_expr(DEBUG_FLAG, expr, context, ::reconstruct_expression)
    }

    fun translate_to_cyclix(DEBUG_FLAG : Boolean) : cyclix.Generic {

        NEWLINE()
        MSG("################################################")
        MSG("#### Starting Reordex-to-Cyclix translation ####")
        MSG("#### module: " + name)
        MSG("################################################")

        var cyclix_gen = cyclix.Generic(name)

        MSG("generating control structures...")

        var cdb_num = 0
        for (ExUnit in ExecUnits) cdb_num += ExUnit.value.exu_num

        var prf_dim = hw_dim_static()
        prf_dim.add(MultiExu_CFG.RF_width-1, 0)
        prf_dim.add(MultiExu_CFG.PRF_depth-1, 0)
        var PRF = cyclix_gen.uglobal("genPRF", prf_dim, "0")

        var PRF_mapped = cyclix_gen.uglobal("genPRF_mapped", MultiExu_CFG.PRF_depth-1, 0, hw_imm_ones(MultiExu_CFG.ARF_depth))

        var PRF_rdy = cyclix_gen.uglobal("genPRF_rdy", MultiExu_CFG.PRF_depth-1, 0, hw_imm_ones(MultiExu_CFG.PRF_depth))

        var arf_map_dim = hw_dim_static()
        arf_map_dim.add(MultiExu_CFG.PRF_addr_width-1, 0)
        arf_map_dim.add(MultiExu_CFG.ARF_depth-1, 0)

        var ARF_map_default = hw_imm_arr(arf_map_dim)
        for (RF_idx in 0 until MultiExu_CFG.PRF_depth) {
            if (RF_idx < MultiExu_CFG.ARF_depth) {
                ARF_map_default.AddSubImm(RF_idx.toString())
            } else {
                ARF_map_default.AddSubImm("0")
            }
        }

        var ARF_map = cyclix_gen.uglobal("genARF_map", arf_map_dim, ARF_map_default)        // ARF-to-PRF mappings

        var prf_src_dim = hw_dim_static()
        prf_src_dim.add(GetWidthToContain(cdb_num)-1, 0)
        prf_src_dim.add(MultiExu_CFG.PRF_depth-1, 0)
        var PRF_src = cyclix_gen.uglobal("genPRF_src", prf_src_dim, "0") // uncomputed PRF sources

        MSG("generating control structures: done")

        MSG("generating interface structure...")
        // cmd (sequential instruction stream) //
        var cmd_req_struct = hw_struct(name + "_cmd_req_struct")
        cmd_req_struct.addu("exec",     0, 0, "0")
        cmd_req_struct.addu("rf_we",       0,  0, "0")
        cmd_req_struct.addu("rf_addr",    MultiExu_CFG.ARF_addr_width-1, 0, "0")
        cmd_req_struct.addu("rf_wdata",    MultiExu_CFG.RF_width-1, 0, "0")
        cmd_req_struct.addu("fu_id",    GetWidthToContain(ExecUnits.size)-1, 0, "0")
        for (imm_idx in 0 until MultiExu_CFG.imms.size) {
            cmd_req_struct.add("fu_imm_" + MultiExu_CFG.imms[imm_idx].name, MultiExu_CFG.imms[imm_idx].vartype, MultiExu_CFG.imms[imm_idx].defimm)
        }
        for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
            cmd_req_struct.addu("fu_rs" + RF_rs_idx, MultiExu_CFG.ARF_addr_width-1, 0, "0")
        }
        cmd_req_struct.addu("fu_rd",    MultiExu_CFG.ARF_addr_width-1, 0, "0")

        var cmd_req = DUMMY_FIFO_IN
        var cmd_resp = DUMMY_FIFO_OUT
        var data_req_fifo = DUMMY_FIFO_OUT
        var data_resp_fifo = DUMMY_FIFO_IN

        if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) {
            cmd_req = cyclix_gen.fifo_in("cmd_req",  hw_type(cmd_req_struct))
            cmd_resp = cyclix_gen.fifo_out("cmd_resp",  hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(MultiExu_CFG.RF_width-1, 0)))

        } else {            // MultiExu_CFG.mode == REORDEX_MODE.RISC
            var irq_fifo    = cyclix_gen.ufifo_in("irq_fifo", 7, 0)

            var busreq_mem_struct = hw_struct(name + "_busreq_mem_struct")

            busreq_mem_struct.addu("addr",     31, 0, "0")
            busreq_mem_struct.addu("be",       3,  0, "0")
            busreq_mem_struct.addu("wdata",    31, 0, "0")

            val instr_name_prefix = "genmcopipe_instr_mem_"
            val data_name_prefix = "genmcopipe_data_mem_"

            var rd_struct = hw_struct("genpmodule_" + name + "_" + data_name_prefix + "genstruct_fifo_wdata")
            rd_struct.addu("we", 0, 0, "0")
            rd_struct.add("wdata", hw_type(busreq_mem_struct), "0")

            data_req_fifo = cyclix_gen.fifo_out((data_name_prefix + "req"), rd_struct)
            data_resp_fifo = cyclix_gen.ufifo_in((data_name_prefix + "resp"), 31, 0)
        }

        var cmd_req_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_req_data"), cmd_req_struct)
        var cmd_resp_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_resp_data"), hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(MultiExu_CFG.RF_width-1, 0)), "0")

        MSG("generating interface structure: done")

        MSG("generating internal structures...")

        var cdb_struct  = hw_struct("cdb_struct")
        cdb_struct.addu("enb", 0, 0, "0")
        cdb_struct.add("data", MultiExu_CFG.resp_struct)
        var exu_cdb     = cyclix_gen.local("genexu_cdb", cdb_struct, hw_dim_static(cdb_num-1, 0))

        var rob =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) rob_buffer(cyclix_gen, "genrob", 64, MultiExu_CFG, cdb_num)
            else rob_buffer_risc(cyclix_gen, "genrob", 64, MultiExu_CFG, cdb_num)

        var TranslateInfo = __TranslateInfo()

        var IQ_insts = ArrayList<iq_buffer>()
        var ExUnits_insts = ArrayList<ArrayList<cyclix.hw_subproc>>()

        var exu_req     = cyclix_gen.local(cyclix_gen.GetGenName("exu_req"), MultiExu_CFG.req_struct)
        var exu_resp    = cyclix_gen.local(cyclix_gen.GetGenName("exu_resp"), MultiExu_CFG.resp_struct)

        MSG("generating internal structures: done")

        var exu_descrs = mutableMapOf<String, __exu_descr>()
        var ExUnit_idx = 0
        var fu_num = 0
        for (ExUnit in ExecUnits) {
            MSG("generating execution unit: " + ExUnit.value.ExecUnit.name + "...")

            var new_exu_descr = __exu_descr(mutableMapOf(), ArrayList(), ArrayList())

            for (ExUnit_num in 0 until ExUnit.value.exu_num) {
                var iq_buf = iq_buffer(cyclix_gen, "geniq_" + ExUnit.key + "_" + ExUnit_num, ExUnit.value.iq_length, MultiExu_CFG, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), true, fu_num, cdb_num)
                new_exu_descr.IQ_insts.add(iq_buf)
                IQ_insts.add(iq_buf)
            }

            MSG("generating submodules...")
            var exu_cyclix_gen = cyclix.Streaming("genexu_" + ExUnit.value.ExecUnit.name, MultiExu_CFG.req_struct, MultiExu_CFG.resp_struct, ExUnit.value.pref_impl)
            MSG("generating submodules: done")

            MSG("generating locals...")
            for (local in ExUnit.value.ExecUnit.locals)
                new_exu_descr.var_dict.put(local, exu_cyclix_gen.local(local.name, local.vartype, local.defimm))
            for (imm_num in 0 until ExUnit.value.ExecUnit.imms.size)
                new_exu_descr.var_dict.put(MultiExu_CFG.imms[imm_num], new_exu_descr.var_dict[ExUnit.value.ExecUnit.imms[imm_num]!!]!!)
            for (rs_num in 0 until ExUnit.value.ExecUnit.rss.size)
                new_exu_descr.var_dict.put(MultiExu_CFG.rss[rs_num], new_exu_descr.var_dict[ExUnit.value.ExecUnit.rss[rs_num]!!]!!)
            MSG("generating locals: done")

            MSG("generating globals...")
            for (global in ExUnit.value.ExecUnit.globals)
                new_exu_descr.var_dict.put(global, exu_cyclix_gen.global(global.name, global.vartype, global.defimm))
            MSG("generating globals: done")

            MSG("generating intermediates...")
            for (genvar in ExUnit.value.ExecUnit[0].genvars)
                new_exu_descr.var_dict.put(genvar, exu_cyclix_gen.local(genvar.name, genvar.vartype, genvar.defimm))
            MSG("generating intermediates: done")

            MSG("generating logic...")

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict), exu_cyclix_gen.stream_req_var)

            for (imm_num in 0 until MultiExu_CFG.imms.size) {
                exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.imms[imm_num], new_exu_descr.var_dict), exu_cyclix_gen.subStruct((TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict)), MultiExu_CFG.imms[imm_num].name))
            }
            for (rs_num in 0 until MultiExu_CFG.rss.size) {
                exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.rss[rs_num], new_exu_descr.var_dict), exu_cyclix_gen.subStruct((TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict)), "rs" + rs_num + "_rdata"))
            }

            for (expr in ExUnit.value.ExecUnit[0].expressions) {
                reconstruct_expression(false,
                    exu_cyclix_gen,
                    expr,
                    import_expr_context(new_exu_descr.var_dict))
            }

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict).GetFracRef("wdata"), TranslateVar(ExUnit.value.ExecUnit.result, new_exu_descr.var_dict) )

            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var, TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict))
            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var.GetFracRef("tag"), exu_cyclix_gen.stream_req_var.GetFracRef("rd_tag"))
            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var.GetFracRef("trx_id"), exu_cyclix_gen.stream_req_var.GetFracRef("trx_id"))

            exu_cyclix_gen.end()

            MSG("generating logic: done")

            MSG("generating submodule instances...")
            var ExUnit_insts = ArrayList<cyclix.hw_subproc>()
            for (exu_num in 0 until ExUnit.value.exu_num) {
                var exu_inst = cyclix_gen.subproc(exu_cyclix_gen.name + "_" + exu_num, exu_cyclix_gen)
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

            for (rs_num in 0 until ExUnit.value.ExecUnit.rss.size)
                if (new_exu_descr.var_dict[ExUnit.value.ExecUnit.rss[rs_num]!!]!!.read_done) {
                    MSG("Exu waits for: " + ExUnit.value.ExecUnit.rss[rs_num]!!.name)
                    new_exu_descr.rs_use_flags.add(true)
                } else {
                    new_exu_descr.rs_use_flags.add(false)
                }
            exu_descrs.put(ExUnit.key, new_exu_descr)

            ExUnit_idx++
            fu_num += ExUnit.value.exu_num

            MSG("generating execution unit " + ExUnit.value.ExecUnit.name + ": done")
        }

        MSG("generating store IQ...")
        var store_iq = iq_buffer(cyclix_gen, "genwb", out_iq_size, MultiExu_CFG, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), false, fu_num, cdb_num)
        IQ_insts.add(store_iq)
        MSG("generating store IQ: done")

        MSG("generating logic...")

        cyclix_gen.MSG_COMMENT("Acquiring EXU CDB...")
        var exu_cdb_num = 0
        for (exu_num in 0 until ExUnits_insts.size) {
            for (exu_inst_num in 0 until ExUnits_insts[exu_num].size) {
                var exu_cdb_inst        = exu_cdb.GetFracRef(exu_cdb_num)
                var exu_cdb_inst_enb    = exu_cdb_inst.GetFracRef("enb")
                var exu_cdb_inst_data   = exu_cdb_inst.GetFracRef("data")

                cyclix_gen.assign(exu_cdb_inst_enb, cyclix_gen.fifo_internal_rd_unblk(ExUnits_insts[exu_num][exu_inst_num], cyclix.STREAM_RESP_BUS_NAME, exu_cdb_inst_data))

                exu_cdb_num++
            }
        }
        cyclix_gen.MSG_COMMENT("Acquiring EXU CDB: done")

        cyclix_gen.MSG_COMMENT("ROB committing...")
        rob.preinit_ctrls()
        rob.init_locals()

        if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) {
            cyclix_gen.begif(rob.ctrl_active)
            run {
                cyclix_gen.begif(rob.rdy)
                run {
                    cyclix_gen.begif(rob.rd_tag_prev_clr)
                    run {
                        // PRF written, and previous tag can be remapped
                        cyclix_gen.assign(
                            PRF_mapped.GetFracRef(rob.rd_tag_prev),
                            0)
                    }; cyclix_gen.endif()
                    cyclix_gen.assign(rob.pop, 1)
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

        } else {    // MultiExu_CFG.mode == REORDEX_MODE.RISC
            rob as rob_buffer_risc

            var mem_rd_inprogress   = cyclix_gen.uglobal("mem_rd_inprogress", 0, 0, "0")
            var mem_data_wdata        = cyclix_gen.local("mem_data_wdata", data_req_fifo.vartype, "0")
            var mem_data_rdata        = cyclix_gen.local("mem_data_rdata", data_resp_fifo.vartype, "0")

            cyclix_gen.begif(mem_rd_inprogress)
            run {
                cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(data_resp_fifo, mem_data_rdata))
                run {
                    cyclix_gen.assign(mem_rd_inprogress, 0)
                    cyclix_gen.assign(rob.pop, 1)
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()
            cyclix_gen.begelse()
            run {
                cyclix_gen.begif(rob.ctrl_active)
                run {
                    cyclix_gen.assign(rob.pop, 1)
                    cyclix_gen.begif(rob.mem_req)
                    run {
                        cyclix_gen.assign(mem_data_wdata.GetFracRef("we"), rob.mem_cmd)
                        cyclix_gen.assign(mem_data_wdata.GetFracRef("wdata").GetFracRef("addr"), rob.mem_addr)
                        cyclix_gen.assign(mem_data_wdata.GetFracRef("wdata").GetFracRef("be"), rob.mem_be)
                        cyclix_gen.assign(mem_data_wdata.GetFracRef("wdata").GetFracRef("wdata"), rob.rd_data)
                        cyclix_gen.fifo_wr_unblk(data_req_fifo, mem_data_wdata)
                        cyclix_gen.begif(!rob.mem_cmd)
                        run {
                            cyclix_gen.assign(rob.pop, 0)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()
        }

        // popping
        cyclix_gen.begif(rob.pop)
        run {
            rob.pop_trx()
        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("ROB committing: done")

        cyclix_gen.MSG_COMMENT("Filling ROB with data from CDB...")
        var rob_iter = cyclix_gen.begforall_asc(rob.TRX_BUF)
        run {
            var CDB_ref = exu_cdb.GetFracRef(rob_iter.iter_elem.GetFracRef("cdb_id"))
            cyclix_gen.begif(cyclix_gen.eq2(rob_iter.iter_elem.GetFracRef("trx_id"), CDB_ref.GetFracRef("data").GetFracRef("trx_id")))
            run {
                cyclix_gen.assign(rob_iter.iter_elem.GetFracRef("rdy"), 1)
                if (MultiExu_CFG.mode == REORDEX_MODE.RISC) cyclix_gen.assign(rob_iter.iter_elem.GetFracRef("rd_data"), CDB_ref.GetFracRef("data").GetFracRef("wdata"))
            }; cyclix_gen.endif()
        }; cyclix_gen.endloop()
        cyclix_gen.MSG_COMMENT("Filling ROB with data from CDB: done")

        cyclix_gen.MSG_COMMENT("IQ processing: store IQ...")
        store_iq.preinit_ctrls()
        store_iq.init_locals()
        cyclix_gen.begif(store_iq.ctrl_active)
        run {
            cyclix_gen.begif(store_iq.rs_rsrv[0].rs_rdy)
            run {
                if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) cyclix_gen.assign(store_iq.pop, cyclix_gen.fifo_wr_unblk(cmd_resp, store_iq.rs_rsrv[0].rs_rdata))
            }; cyclix_gen.endif()
        }; cyclix_gen.endif()
        // popping
        cyclix_gen.begif(store_iq.pop)
        run {
            store_iq.pop_trx()
        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("IQ processing: store IQ: done")

        var fu_id = 0
        for (ExUnit in ExecUnits) {
            for (ExUnit_num in 0 until ExUnit.value.exu_num) {

                cyclix_gen.MSG_COMMENT("IQ processing: ExUnit: " + ExUnit.key + ", instance num: " + ExUnit_num)

                var IQ_inst = exu_descrs[ExUnit.key]!!.IQ_insts[ExUnit_num]
                IQ_inst.preinit_ctrls()
                IQ_inst.init_locals()

                cyclix_gen.MSG_COMMENT("committing IQ head...")
                cyclix_gen.begif(cyclix_gen.band(IQ_inst.enb, IQ_inst.rdy))
                run {
                    cyclix_gen.assign(IQ_inst.pop, 1)

                    // popping
                    cyclix_gen.begif(IQ_inst.pop)
                    run {
                        IQ_inst.pop_trx()
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
                cyclix_gen.MSG_COMMENT("committing IQ head: done")

                cyclix_gen.MSG_COMMENT("issuing uops...")
                var iq_iter = cyclix_gen.begforall_asc(IQ_inst.TRX_BUF)
                run {

                    var iq_entry            = IQ_inst.TRX_BUF.GetFracRef(iq_iter.iter_num)
                    var iq_entry_enb        = iq_entry.GetFracRef("enb")
                    var iq_entry_fu_pending = iq_entry.GetFracRef("fu_pending")
                    var iq_entry_rd_tag     = iq_entry.GetFracRef("rd_tag")
                    var iq_entry_rdy        = iq_entry.GetFracRef("rdy")

                    cyclix_gen.begif(iq_entry_enb)
                    run {
                        var rss_rdy = cyclix_gen.ulocal(cyclix_gen.GetGenName("rss_rdy"), 0, 0, "0")
                        cyclix_gen.assign(rss_rdy, 1)
                        for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
                            cyclix_gen.band_gen(rss_rdy, rss_rdy, iq_entry.GetFracRef("rs" + RF_rs_idx + "_rdy"))
                        }
                        cyclix_gen.begif(rss_rdy)
                        run {
                            cyclix_gen.begif(cyclix_gen.subStruct(iq_iter.iter_elem, "fu_req"))
                            run {

                                // writing op to FU
                                cyclix_gen.begif(!iq_entry_fu_pending)
                                run {

                                    // filling exu_req with iq data
                                    cyclix_gen.assign_subStructs(exu_req, iq_entry)

                                    cyclix_gen.begif(cyclix_gen.fifo_internal_wr_unblk(ExUnits_insts[fu_id][ExUnit_num], cyclix.STREAM_REQ_BUS_NAME, exu_req))
                                    run {
                                        cyclix_gen.assign(iq_entry_fu_pending, 1)
                                        cyclix_gen.assign(iq_entry_rdy, 1)
                                    }; cyclix_gen.endif()

                                }; cyclix_gen.endif()

                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()
                }; cyclix_gen.endloop()
                cyclix_gen.MSG_COMMENT("issuing uops: done")
            }
            fu_id++
        }

        var renamed_uop_buf =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) rename_buffer(cyclix_gen, "genrenamed_uop_buf", 1, MultiExu_CFG, ExecUnits.size, cdb_num)
            else rename_buffer_risc(cyclix_gen, "genrenamed_uop_buf", 1, MultiExu_CFG, ExecUnits.size, cdb_num)

        renamed_uop_buf.preinit_ctrls()
        renamed_uop_buf.init_locals()

        cyclix_gen.MSG_COMMENT("broadcasting FU results to IQ and renamed buffer...")
        fu_id = 0
        exu_cdb_num = 0
        for (exu_num in 0 until ExUnits_insts.size) {
            for (exu_inst_num in 0 until ExUnits_insts[exu_num].size) {

                var exu_cdb_inst        = exu_cdb.GetFracRef(exu_cdb_num)
                var exu_cdb_inst_enb    = exu_cdb_inst.GetFracRef("enb")
                var exu_cdb_inst_data   = exu_cdb_inst.GetFracRef("data")
                var exu_cdb_inst_tag    = exu_cdb_inst_data.GetFracRef("tag")
                var exu_cdb_inst_wdata  = exu_cdb_inst_data.GetFracRef("wdata")

                cyclix_gen.begif(exu_cdb_inst_enb)
                run {

                    // updating PRF state
                    cyclix_gen.assign(
                        PRF_rdy.GetFracRef(exu_cdb_inst_tag),
                        1)
                    cyclix_gen.assign(
                        PRF.GetFracRef(exu_cdb_inst_tag),
                        exu_cdb_inst_wdata)

                    // broadcasting FU results to renamed buffer
                    // monitoring generated data
                    for (renamed_uop_buf_idx in 0 until renamed_uop_buf.TRX_BUF_SIZE) {
                        var renamed_uop_buf_entry = renamed_uop_buf.TRX_BUF.GetFracRef(renamed_uop_buf_idx)
                        for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {

                            var rs_rdy      = renamed_uop_buf_entry.GetFracRef("rs" + RF_rs_idx + "_rdy")
                            var rs_tag      = renamed_uop_buf_entry.GetFracRef("rs" + RF_rs_idx + "_tag")
                            var rs_rdata    = renamed_uop_buf_entry.GetFracRef("rs" + RF_rs_idx + "_rdata")

                            cyclix_gen.begif(!rs_rdy)
                            run {
                                cyclix_gen.begif(cyclix_gen.eq2(rs_tag, exu_cdb_inst_tag))
                                run {
                                    // setting IQ entry ready
                                    cyclix_gen.assign(rs_rdata, exu_cdb_inst_wdata)
                                    cyclix_gen.assign(rs_rdy, 1)
                                }; cyclix_gen.endif()
                            }; cyclix_gen.endif()
                        }

                        //// setting rdy for wb_ext if data generated ////
                        cyclix_gen.begif(renamed_uop_buf_entry.GetFracRef("wb_ext"))
                        run {
                            cyclix_gen.assign(renamed_uop_buf_entry.GetFracRef("rdy"), renamed_uop_buf_entry.GetFracRef("rs0_rdy"))
                        }; cyclix_gen.endif()
                    }

                }; cyclix_gen.endif()
                exu_cdb_num++
            }
            fu_id++
        }

        // broadcasting FU results to IQ
        for (IQ_inst in IQ_insts) {

            var iq_iter = cyclix_gen.begforall_asc(IQ_inst.TRX_BUF)
            run {

                var iq_entry            = IQ_inst.TRX_BUF.GetFracRef(iq_iter.iter_num)
                var iq_entry_enb        = iq_entry.GetFracRef("enb")
                var iq_entry_rdy        = iq_entry.GetFracRef("rdy")
                var iq_entry_rd_tag     = iq_entry.GetFracRef("rd_tag")
                var iq_entry_wb_ext     = iq_entry.GetFracRef("wb_ext")
                var iq_entry_fu_pending = iq_entry.GetFracRef("fu_pending")

                cyclix_gen.begif(iq_entry_enb)
                run {

                    for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {

                        var iq_entry_rs_tag     = iq_entry.GetFracRef("rs" + RF_rs_idx + "_tag")
                        var iq_entry_rs_src     = iq_entry.GetFracRef("rs" + RF_rs_idx + "_src")
                        var iq_entry_rs_rdy     = iq_entry.GetFracRef("rs" + RF_rs_idx + "_rdy")
                        var iq_entry_rs_rdata   = iq_entry.GetFracRef("rs" + RF_rs_idx + "_rdata")

                        cyclix_gen.begif(!iq_entry_rs_rdy)
                        run {

                            var src_cdb         = exu_cdb.GetFracRef(iq_entry_rs_src)
                            var src_cdb_enb     = src_cdb.GetFracRef("enb")
                            var src_cdb_data    = src_cdb.GetFracRef("data")
                            var src_cdb_tag     = src_cdb_data.GetFracRef("tag")
                            var src_cdb_wdata   = src_cdb_data.GetFracRef("wdata")

                            cyclix_gen.begif(cyclix_gen.eq2(iq_entry_rs_tag, src_cdb_tag))
                            run {
                                // setting IQ entry ready
                                cyclix_gen.assign(iq_entry_rs_rdata, src_cdb_wdata)
                                cyclix_gen.assign(iq_entry_rs_rdy, 1)
                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()
                    }

                    //// setting rdy if data generated ////
                    // wb_ext //
                    cyclix_gen.begif(iq_entry_wb_ext)
                    run {
                        cyclix_gen.assign(iq_entry_rdy, iq_entry.GetFracRef("rs0_rdy"))
                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()

            }; cyclix_gen.endloop()

        }

        cyclix_gen.MSG_COMMENT("broadcasting FU results to IQ and renamed buffer: done")

        cyclix_gen.MSG_COMMENT("sending new operations to IQs...")

        var rob_push_trx = rob.GetPushTrx()
        cyclix_gen.assign(rob_push_trx.GetFracRef("enb"), 1)
        cyclix_gen.assign(rob_push_trx.GetFracRef("rd_tag_prev"), renamed_uop_buf.rd_tag_prev)
        cyclix_gen.assign(rob_push_trx.GetFracRef("rd_tag_prev_clr"), renamed_uop_buf.rd_tag_prev_clr)
        cyclix_gen.assign(rob_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)

        cyclix_gen.begif(renamed_uop_buf.ctrl_active)
        run {

            var num_rs = 0
            for (rs_rsrv in renamed_uop_buf.rs_rsrv) {
                cyclix_gen.assign(renamed_uop_buf.TRX_BUF_head_ref.GetFracRef("rs" + num_rs + "_src"), PRF_src.GetFracRef(rs_rsrv.rs_tag))      // TODO: stage context cleanup
                num_rs++
            }

            cyclix_gen.begif(renamed_uop_buf.wb_ext)
            run {
                cyclix_gen.begif(store_iq.ctrl_rdy)
                run {

                    // signaling iq_wr
                    cyclix_gen.assign(store_iq.push, 1)

                    var store_push_trx = store_iq.GetPushTrx()
                    cyclix_gen.assign_subStructs(store_push_trx, renamed_uop_buf.TRX_BUF_head_ref)
                    cyclix_gen.assign(store_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                    store_iq.push_trx(store_push_trx)

                    cyclix_gen.assign(rob_push_trx.GetFracRef("cdb_id"), store_iq.CDB_index)

                    // clearing renamed uop buffer
                    cyclix_gen.assign(renamed_uop_buf.pop, 1)

                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

            cyclix_gen.begelse()
            run {
                var ExUnit_num = 0
                for (ExUnit in ExecUnits) {
                    var IQ_inst = exu_descrs[ExUnit.key]!!.IQ_insts[0]      // TODO: multiple queues

                    cyclix_gen.begif(cyclix_gen.eq2(renamed_uop_buf.fu_id, IQ_inst.fu_id_num))
                    run {
                        cyclix_gen.begif(IQ_inst.ctrl_rdy)
                        run {

                            // signaling iq_wr
                            cyclix_gen.assign(IQ_inst.push, 1)

                            var iq_push_trx = IQ_inst.GetPushTrx()
                            cyclix_gen.assign_subStructs(iq_push_trx, renamed_uop_buf.TRX_BUF_head_ref)
                            cyclix_gen.assign(iq_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                            IQ_inst.push_trx(iq_push_trx)

                            cyclix_gen.assign(PRF_src.GetFracRef(renamed_uop_buf.rd_tag), IQ_inst.CDB_index)

                            cyclix_gen.assign(rob_push_trx.GetFracRef("cdb_id"), IQ_inst.CDB_index)

                            // clearing renamed uop buffer
                            cyclix_gen.assign(renamed_uop_buf.pop, 1)

                            cyclix_gen.assign(rob.push, 1)

                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()

                    ExUnit_num++
                }
            }; cyclix_gen.endif()

        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("acquiring new operation to IQs: done")

        cyclix_gen.MSG_COMMENT("renaming...")
        cyclix_gen.begif(renamed_uop_buf.pop)
        run {
            cyclix_gen.begif(rob.push)
            run {
                cyclix_gen.assign(rob_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                cyclix_gen.assign(rob.TRX_ID_COUNTER, cyclix_gen.add(rob.TRX_ID_COUNTER, 1))
                rob.push_trx(rob_push_trx)
            }; cyclix_gen.endif()
            renamed_uop_buf.pop_trx()
        }; cyclix_gen.endif()
        renamed_uop_buf.finalize_ctrls()               //  TODO: cleanup

        var new_renamed_uop     = renamed_uop_buf.GetPushTrx()
        var nru_enb             = new_renamed_uop.GetFracRef("enb")
        var nru_rdy             = new_renamed_uop.GetFracRef("rdy")
        var nru_fu_req          = new_renamed_uop.GetFracRef("fu_req")
        var nru_fu_id           = new_renamed_uop.GetFracRef("fu_id")
        var nru_wb_ext          = new_renamed_uop.GetFracRef("wb_ext")
        var nru_rd_tag          = new_renamed_uop.GetFracRef("rd_tag")
        var nru_rd_tag_prev     = new_renamed_uop.GetFracRef("rd_tag_prev")
        var nru_rd_tag_prev_clr = new_renamed_uop.GetFracRef("rd_tag_prev_clr")
        var nru_rs_use_mask     = cyclix_gen.ulocal("genrs_use_mask", MultiExu_CFG.rss.size-1, 0, "0")

        if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) {
            cyclix_gen.begif(renamed_uop_buf.ctrl_rdy)
            run {
                cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(cmd_req, cmd_req_data))
                run {

                    // decoding input
                    cyclix_gen.assign(nru_enb, 1)

                    cyclix_gen.assign(nru_fu_id,      cmd_req_data.GetFracRef("fu_id"))

                    for (imm_idx in 0 until MultiExu_CFG.imms.size) {
                        cyclix_gen.assign(
                            new_renamed_uop.GetFracRef(MultiExu_CFG.imms[imm_idx].name),
                            cmd_req_data.GetFracRef("fu_imm_" + MultiExu_CFG.imms[imm_idx].name))
                    }

                    // LOAD/STORE commutation
                    cyclix_gen.begif(!cmd_req_data.GetFracRef("exec"))
                    run {
                        cyclix_gen.assign(nru_fu_id, ExecUnits.size)

                        // LOAD
                        cyclix_gen.begif(cmd_req_data.GetFracRef("rf_we"))
                        run {
                            cyclix_gen.assign(cmd_req_data.GetFracRef("fu_rd"), cmd_req_data.GetFracRef("rf_addr"))
                        }; cyclix_gen.endif()

                        // STORE
                        cyclix_gen.begelse()
                        run {
                            cyclix_gen.assign(cmd_req_data.GetFracRef("fu_rs0"), cmd_req_data.GetFracRef("rf_addr"))
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()

                    var rss_tags = ArrayList<hw_var>()
                    for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
                        rss_tags.add(cyclix_gen.indexed(ARF_map, cmd_req_data.GetFracRef("fu_rs" + RF_rs_idx)))
                    }
                    var rd_tag = cyclix_gen.indexed(ARF_map, cmd_req_data.GetFracRef("fu_rd"))

                    for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
                        cyclix_gen.assign(new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_tag"), rss_tags[RF_rs_idx])
                        cyclix_gen.assign(
                            new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdata"),
                            cyclix_gen.indexed(PRF, new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_tag")))
                    }

                    var alloc_rd_tag = cyclix_gen.min0(PRF_mapped)

                    cyclix_gen.begif(cmd_req_data.GetFracRef("exec"))
                    run {

                        cyclix_gen.assign(nru_fu_req, 1)

                        for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {

                            var nru_rs_use = nru_rs_use_mask.GetFracRef(RF_rs_idx)
                            var exu_descr_idx = 0
                            for (exu_descr in exu_descrs) {
                                cyclix_gen.begif(cyclix_gen.eq2(nru_fu_id, exu_descr_idx))
                                run {
                                    cyclix_gen.assign(nru_rs_use, hw_imm(exu_descr.value.rs_use_flags[RF_rs_idx]))
                                }; cyclix_gen.endif()
                                exu_descr_idx++
                            }

                            // fetching rdy flags from PRF_rdy and masking with rsX_req
                            cyclix_gen.assign(
                                new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdy"),
                                cyclix_gen.bor(PRF_rdy.GetFracRef(rss_tags[RF_rs_idx]), !nru_rs_use))
                        }

                        cyclix_gen.assign(nru_rd_tag, alloc_rd_tag.position)            // TODO: check for availability flag
                        cyclix_gen.assign(nru_rd_tag_prev, rd_tag)
                        cyclix_gen.assign(nru_rd_tag_prev_clr, cyclix_gen.indexed(PRF_mapped, rd_tag))

                        cyclix_gen.assign(ARF_map.GetFracRef(cmd_req_data.GetFracRef("fu_rd")), alloc_rd_tag.position)
                        cyclix_gen.assign(PRF_mapped.GetFracRef(alloc_rd_tag.position), 1)
                        cyclix_gen.assign(PRF_rdy.GetFracRef(alloc_rd_tag.position), 0)

                        cyclix_gen.assign(nru_rdy, 0)
                        cyclix_gen.assign(nru_wb_ext, 0)

                        cyclix_gen.assign(renamed_uop_buf.push, 1)
                    }; cyclix_gen.endif()

                    cyclix_gen.begelse()
                    run {

                        // LOAD
                        cyclix_gen.begif(cmd_req_data.GetFracRef("rf_we"))
                        run {

                            cyclix_gen.assign(nru_rdy, 1)
                            cyclix_gen.assign(nru_rd_tag, alloc_rd_tag.position)        // TODO: check for availability flag
                            cyclix_gen.assign(nru_rd_tag_prev, rd_tag)
                            cyclix_gen.assign(nru_rd_tag_prev_clr, PRF_mapped.GetFracRef(rd_tag))
                            cyclix_gen.assign(PRF_mapped.GetFracRef(alloc_rd_tag.position), 1)

                            cyclix_gen.assign(ARF_map.GetFracRef(cmd_req_data.GetFracRef("fu_rd")), alloc_rd_tag.position)
                            cyclix_gen.assign(PRF_rdy.GetFracRef(alloc_rd_tag.position), 1)
                            cyclix_gen.assign(PRF.GetFracRef(alloc_rd_tag.position), cmd_req_data.GetFracRef("rf_wdata"))

                        }; cyclix_gen.endif()

                        // STORE
                        cyclix_gen.begelse()
                        run {
                            cyclix_gen.assign(new_renamed_uop.GetFracRef("rs0_rdy"), PRF_rdy.GetFracRef(rss_tags[0]))

                            for (RF_rs_idx in 1 until MultiExu_CFG.rss.size) {
                                cyclix_gen.assign(new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdy"), 1)
                            }

                            cyclix_gen.assign(nru_rdy, new_renamed_uop.GetFracRef("rs0_rdy"))
                            cyclix_gen.assign(nru_wb_ext, 1)

                            cyclix_gen.assign(renamed_uop_buf.push, 1)

                        }; cyclix_gen.endif()

                    }; cyclix_gen.endif()

                    // placing new uop in rename_buf
                    cyclix_gen.begif(renamed_uop_buf.push)
                    run {
                        renamed_uop_buf.push_trx(new_renamed_uop)
                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

        } else {            // MultiExu_CFG.mode == REORDEX_MODE.RISC

            var instr_fetch = instr_fetch_buffer(name, cyclix_gen, "instr_fetch", 1, MultiExu_CFG)
            var instr_req = instr_req_stage(name, cyclix_gen, instr_fetch)

            instr_fetch.Process(renamed_uop_buf)
            instr_req.Process()
        }

        cyclix_gen.MSG_COMMENT("renaming: done")

        cyclix_gen.end()

        MSG("generating logic: done")

        MSG("#################################################")
        MSG("#### Reordex-to-Cyclix translation complete! ####")
        MSG("#### module: " + name)
        MSG("#################################################")

        return cyclix_gen
    }
}
