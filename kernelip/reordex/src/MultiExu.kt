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

    var src_imms = ArrayList<hw_var>()
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

    var dst_imms = ArrayList<hw_var>()
    fun AddDstImm(name : String, new_type : hw_type) : hw_var {
        var new_var = hw_var(name, new_type, "0")
        resp_struct.add(name, new_type, "0")
        dst_imms.add(new_var)
        return new_var
    }
    fun AddDstUImm(name : String, new_width : Int) : hw_var {
        return AddDstImm(name, hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(new_width)))
    }
    fun AddDstSImm(name : String, new_width : Int) : hw_var {
        return AddDstImm(name, hw_type(DATA_TYPE.BV_SIGNED, hw_dim_static(new_width)))
    }

    var rss = ArrayList<hw_var>()
    fun AddRs() : hw_var {
        var new_var = hw_var("rs" + rss.size, RF_width-1, 0, "0")
        req_struct.addu("rs" + rss.size + "_rdata", RF_width-1, 0, "0")
        rss.add(new_var)
        return new_var
    }

    var rds = ArrayList<hw_var>()
    fun AddRd() : hw_var {
        var new_var = hw_var("rd" + rds.size, RF_width-1, 0, "0")
        req_struct.addu("rd" + rds.size + "_tag", RF_width-1, 0, "0")
        rds.add(new_var)
        return new_var
    }

    init {
        req_struct.addu("trx_id",     31, 0, "0")       // TODO: clean up
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

        var exu_descrs = mutableMapOf<String, __exu_descr>()
        var global_structures = __global_structures(cyclix_gen, MultiExu_CFG, PRF, PRF_mapped, PRF_rdy, ARF_map, PRF_src, ExecUnits, exu_descrs)

        MSG("generating control structures: done")

        var cmd_resp = DUMMY_FIFO_OUT
        if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) cmd_resp = cyclix_gen.fifo_out("cmd_resp",  hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(MultiExu_CFG.RF_width-1, 0)))

        MSG("generating internal structures...")

        var cdb_struct  = hw_struct("cdb_struct")
        cdb_struct.addu("enb", 0, 0, "0")
        cdb_struct.add("data", MultiExu_CFG.resp_struct)
        var exu_cdb     = cyclix_gen.local("genexu_cdb", cdb_struct, hw_dim_static(cdb_num-1, 0))

        var rob =
            if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) rob(cyclix_gen, "genrob", 64, MultiExu_CFG, cdb_num)
            else rob_risc(name, cyclix_gen, "genrob", 64, MultiExu_CFG, cdb_num)

        var TranslateInfo = __TranslateInfo()

        var IQ_insts = ArrayList<iq_buffer>()
        var ExUnits_insts = ArrayList<ArrayList<cyclix.hw_subproc>>()

        var exu_req     = cyclix_gen.local(cyclix_gen.GetGenName("exu_req"), MultiExu_CFG.req_struct)
        var exu_resp    = cyclix_gen.local(cyclix_gen.GetGenName("exu_resp"), MultiExu_CFG.resp_struct)

        MSG("generating internal structures: done")

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
            for (imm_num in 0 until ExUnit.value.ExecUnit.src_imms.size)
                new_exu_descr.var_dict.put(MultiExu_CFG.src_imms[imm_num], new_exu_descr.var_dict[ExUnit.value.ExecUnit.src_imms[imm_num]!!]!!)
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

            for (imm_num in 0 until MultiExu_CFG.src_imms.size) {
                exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.src_imms[imm_num], new_exu_descr.var_dict), exu_cyclix_gen.subStruct((TranslateVar(ExUnit.value.ExecUnit.req_data, new_exu_descr.var_dict)), MultiExu_CFG.src_imms[imm_num].name))
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

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict).GetFracRef("wdata"), TranslateVar(ExUnit.value.ExecUnit.rd0, new_exu_descr.var_dict) )

            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var, TranslateVar(ExUnit.value.ExecUnit.resp_data, new_exu_descr.var_dict))
            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var.GetFracRef("tag"), exu_cyclix_gen.stream_req_var.GetFracRef("rd0_tag"))
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
        rob.Commit(global_structures)
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

                var op_issued = cyclix_gen.ulocal((ExUnit.value.ExecUnit.name + ExUnit_num + "_op_issued"), 0, 0, "0")
                var op_issued_num = cyclix_gen.ulocal((ExUnit.value.ExecUnit.name + ExUnit_num + "_op_issued_num"), GetWidthToContain(IQ_inst.TRX_BUF.GetWidth())-1, 0, "0")
                cyclix_gen.assign(op_issued, 0)

                var iq_iter = cyclix_gen.begforall_asc(IQ_inst.TRX_BUF)
                run {

                    cyclix_gen.begif(!op_issued)
                    run {

                        var iq_entry            = IQ_inst.TRX_BUF.GetFracRef(iq_iter.iter_num)
                        var iq_entry_enb        = iq_entry.GetFracRef("enb")
                        var iq_entry_fu_pending = iq_entry.GetFracRef("fu_pending")
                        var iq_entry_rd0_tag    = iq_entry.GetFracRef("rd0_tag")
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

                                // filling exu_req with iq data
                                cyclix_gen.assign_subStructs(exu_req, iq_entry)

                                // writing op to FU
                                cyclix_gen.begif(cyclix_gen.fifo_internal_wr_unblk(ExUnits_insts[fu_id][ExUnit_num], cyclix.STREAM_REQ_BUS_NAME, exu_req))
                                run {
                                    cyclix_gen.assign(op_issued, 1)
                                    cyclix_gen.assign(op_issued_num, iq_iter.iter_num)
                                }; cyclix_gen.endif()

                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()

                    }; cyclix_gen.endif()

                }; cyclix_gen.endloop()

                cyclix_gen.MSG_COMMENT("issuing uops: done")

                cyclix_gen.MSG_COMMENT("squashing IQ...")
                cyclix_gen.begif(op_issued)
                run {
                    IQ_inst.remove_and_squash_trx(op_issued_num)
                }; cyclix_gen.endif()
                cyclix_gen.MSG_COMMENT("squashing IQ: done")
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
                var iq_entry_rd0_tag     = iq_entry.GetFracRef("rd0_tag")
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

        renamed_uop_buf.Process(rob, PRF_src, store_iq, ExecUnits, exu_descrs)

        cyclix_gen.MSG_COMMENT("renaming...")

        if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) {
            var frontend = coproc_frontend(name, cyclix_gen, MultiExu_CFG, global_structures)
            frontend.Send_toRenameBuf(renamed_uop_buf)

        } else {            // MultiExu_CFG.mode == REORDEX_MODE.RISC
            var instr_fetch = instr_fetch_buffer(name, cyclix_gen, "instr_fetch", 1, MultiExu_CFG, global_structures)
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
