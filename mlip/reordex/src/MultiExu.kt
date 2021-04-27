/*
 * MultiExu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

data class MultiExu_CFG_RF(val ARF_depth : Int,
                           val rename_RF: Boolean,
                           val PRF_depth : Int) {

    val ARF_addr_width = GetWidthToContain(ARF_depth)
    val PRF_addr_width = GetWidthToContain(PRF_depth)
}

data class Exu_CFG(val ExecUnit : Exu,
                   val exu_num : Int,
                   val iq_length : Int)

open class MultiExu(val name : String, val Exu_cfg_rf : Exu_CFG_RF, val MultiExu_cfg_rf : MultiExu_CFG_RF, val out_iq_size : Int) {

    var ExecUnits  = mutableMapOf<String, Exu_CFG>()

    fun add_exu(exu : Exu, exu_num: Int, iq_length: Int) {
        if (ExecUnits.put(exu.name, Exu_CFG(exu, exu_num, iq_length)) != null) {
            ERROR("Exu addition error!")
        }
    }

    /*
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
    */

    fun reconstruct_expression(DEBUG_FLAG : Boolean,
                               cyclix_gen : hw_astc,
                               expr : hw_exec,
                               context : import_expr_context) {

        cyclix_gen as cyclix.Streaming

        // MSG("#### Cyclix: exporting expression: " + expr.opcode.default_string)
        // for (param in expr.params) MSG("param: " + param.GetString())
        // for (wrvar in expr.wrvars) MSG("wrvar: " + wrvar.name)

        var fractions = ReconstructFractions(expr.assign_tgt_fractured.depow_fractions, context.var_dict)

        cyclix_gen.import_expr(DEBUG_FLAG, expr, context, ::reconstruct_expression)
    }

    fun translate_to_cyclix(DEBUG_FLAG : Boolean) : cyclix.Generic {

        NEWLINE()
        MSG("################################################")
        MSG("#### Starting Reordex-to-Cyclix translation ####")
        MSG("#### module: " + name)
        MSG("################################################")

        var cyclix_gen = cyclix.Generic(name)

        //// Generating control structures ////
        var prf_dim = hw_dim_static()
        prf_dim.add(Exu_cfg_rf.RF_width-1, 0)
        prf_dim.add(MultiExu_cfg_rf.PRF_depth-1, 0)
        var PRF = cyclix_gen.uglobal("genPRF", prf_dim, "0")

        var PRF_mapped = cyclix_gen.uglobal("genPRF_mapped", MultiExu_cfg_rf.PRF_depth-1, 0, hw_imm_ones(MultiExu_cfg_rf.ARF_depth))

        var PRF_rdy = cyclix_gen.uglobal("genPRF_rdy", MultiExu_cfg_rf.PRF_depth-1, 0, hw_imm_ones(MultiExu_cfg_rf.PRF_depth))

        var arf_map_dim = hw_dim_static()
        arf_map_dim.add(MultiExu_cfg_rf.PRF_addr_width-1, 0)
        arf_map_dim.add(MultiExu_cfg_rf.ARF_depth-1, 0)

        var ARF_map_default = hw_imm_arr(arf_map_dim)
        for (RF_idx in 0 until MultiExu_cfg_rf.PRF_depth) {
            if (RF_idx < MultiExu_cfg_rf.ARF_depth) {
                ARF_map_default.AddSubImm(RF_idx.toString())
            } else {
                ARF_map_default.AddSubImm("0")
            }
        }

        var ARF_map = cyclix_gen.uglobal("genARF_map", arf_map_dim, ARF_map_default)        // ARF-to-PRF mappings
        ////

        //// Generating interfaces ////
        // cmd (sequential instruction stream) //
        var cmd_req_struct = hw_struct(name + "_cmd_req_struct")
        cmd_req_struct.addu("exec",     0, 0, "0")
        cmd_req_struct.addu("rf_we",       0,  0, "0")
        cmd_req_struct.addu("rf_addr",    MultiExu_cfg_rf.ARF_addr_width-1, 0, "0")
        cmd_req_struct.addu("rf_wdata",    Exu_cfg_rf.RF_width-1, 0, "0")
        cmd_req_struct.addu("fu_id",    GetWidthToContain(ExecUnits.size)-1, 0, "0")
        cmd_req_struct.addu("fu_opcode",     0, 0, "0")
        for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
            cmd_req_struct.addu("fu_rs" + RF_rs_idx + "_req", 0, 0, "0")
            cmd_req_struct.addu("fu_rs" + RF_rs_idx, MultiExu_cfg_rf.ARF_addr_width-1, 0, "0")
        }
        cmd_req_struct.addu("fu_rd",    MultiExu_cfg_rf.ARF_addr_width-1, 0, "0")
        var cmd_req = cyclix_gen.fifo_in("cmd_req",  hw_type(cmd_req_struct))
        var cmd_req_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_req_data"), cmd_req_struct)
        var cmd_resp = cyclix_gen.fifo_out("cmd_resp",  hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(Exu_cfg_rf.RF_width-1, 0)))
        var cmd_resp_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_resp_data"), hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(Exu_cfg_rf.RF_width-1, 0)), "0")

        // TODO: external memory interface

        var iq_struct = hw_struct("iq_struct")
        iq_struct.addu("enb",     0, 0, "0")
        iq_struct.addu("fu_req",     0, 0, "0")
        iq_struct.addu("fu_pending",     0, 0, "0")
        iq_struct.addu("fu_id",     GetWidthToContain(ExecUnits.size), 0, "0")              // for ExecUnits and wb_ext
        iq_struct.addu("fu_opcode",     0, 0, "0")
        for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
            iq_struct.addu("rs" + RF_rs_idx + "_rdy",     0, 0, "0")
            iq_struct.addu("rs" + RF_rs_idx + "_tag",     MultiExu_cfg_rf.PRF_addr_width-1, 0, "0")
            iq_struct.addu("rs" + RF_rs_idx + "_rdata",     Exu_cfg_rf.RF_width-1, 0, "0")
        }
        iq_struct.addu("rd_tag",     MultiExu_cfg_rf.PRF_addr_width-1, 0, "0")
        iq_struct.addu("rd_tag_prev",     MultiExu_cfg_rf.PRF_addr_width-1, 0, "0")                 // freeing
        iq_struct.addu("rd_tag_prev_clr",     0, 0, "0")
        iq_struct.addu("wb_ext",     0, 0, "0")
        iq_struct.addu("rdy",     0, 0, "0")

        var TranslateInfo = __TranslateInfo()

        var IQ_insts = ArrayList<iq_buffer>()
        var ExUnits_insts = ArrayList<ArrayList<cyclix.hw_subproc>>()

        var exu_req = cyclix_gen.local(cyclix_gen.GetGenName("exu_req"), Exu_cfg_rf.req_struct)
        var exu_resp = cyclix_gen.local(cyclix_gen.GetGenName("exu_resp"), Exu_cfg_rf.resp_struct)

        var ExUnit_idx = 0
        for (ExUnit in ExecUnits) {
            IQ_insts.add(iq_buffer(cyclix_gen, "geniq_" + ExUnit.key, ExUnit.value.iq_length, ExecUnits.size, iq_struct, MultiExu_cfg_rf, Exu_cfg_rf, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), true))
            ExUnit_idx++

            // generating submodules
            var exu_cyclix_gen = cyclix.Streaming("genexu_" + ExUnit.value.ExecUnit.name, Exu_cfg_rf.req_struct, Exu_cfg_rf.resp_struct)

            var var_dict = mutableMapOf<hw_var, hw_var>()

            // Generating locals
            for (local in ExUnit.value.ExecUnit.locals)
                var_dict.put(local, exu_cyclix_gen.local(local.name, local.vartype, local.defimm))

            // Generating globals
            for (global in ExUnit.value.ExecUnit.globals)
                var_dict.put(global, exu_cyclix_gen.global(global.name, global.vartype, global.defimm))

            // Generating intermediates
            for (genvar in ExUnit.value.ExecUnit[0].genvars)
                var_dict.put(genvar, exu_cyclix_gen.local(genvar.name, genvar.vartype, genvar.defimm))

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.req_data, var_dict), exu_cyclix_gen.stream_req_var)

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.opcode, var_dict), exu_cyclix_gen.subStruct(TranslateVar(ExUnit.value.ExecUnit.req_data, var_dict), "opcode"))
            for (rs_num in 0 until Exu_cfg_rf.RF_rs_num) {
                exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.rs[rs_num], var_dict), exu_cyclix_gen.subStruct((TranslateVar(ExUnit.value.ExecUnit.req_data, var_dict)), "rs" + rs_num + "_rdata"))
            }

            for (expr in ExUnit.value.ExecUnit[0].expressions) {
                reconstruct_expression(false,
                    exu_cyclix_gen,
                    expr,
                    import_expr_context(var_dict))
            }

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.resp_data, var_dict), hw_fracs(hw_frac_SubStruct("wdata")), TranslateVar(ExUnit.value.ExecUnit.result, var_dict) )

            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var, TranslateVar(ExUnit.value.ExecUnit.resp_data, var_dict))
            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var, hw_fracs("tag"), exu_cyclix_gen.subStruct(exu_cyclix_gen.stream_req_var, "rd_tag"))

            exu_cyclix_gen.end()

            // generating submodule instances
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
        }

        // adding IQ for stores
        IQ_insts.add(iq_buffer(cyclix_gen, "genwb", out_iq_size, ExecUnits.size, iq_struct, MultiExu_cfg_rf, Exu_cfg_rf, hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()), false))

        var iq_id = 0
        for (IQ_inst in IQ_insts) {

            IQ_inst.init_pctrls()
            IQ_inst.set_rdy()
            IQ_inst.init_locals()

            // committing IQ head
            cyclix_gen.begif(cyclix_gen.band(IQ_inst.enb, IQ_inst.rdy))
            run {

                if (IQ_inst.iq_exu) {
                    cyclix_gen.begif(IQ_inst.rd_tag_prev_clr)
                    run {
                        // PRF written, and previous tag can be remapped
                        cyclix_gen.assign(
                            PRF_mapped,
                            hw_fracs(hw_frac_V(IQ_inst.rd_tag_prev)),
                            0)
                    }; cyclix_gen.endif()
                    cyclix_gen.assign(IQ_inst.rd, 1)
                } else {
                    cyclix_gen.assign(IQ_inst.rd, 1)
                    cyclix_gen.begif(IQ_inst.wb_ext)
                    run {
                        cyclix_gen.assign(IQ_inst.rd, 0)
                        cyclix_gen.begif(cyclix_gen.fifo_wr_unblk(cmd_resp, IQ_inst.rs_rsrv[0].rs_rdata))
                        run {
                            cyclix_gen.assign(IQ_inst.rd, 1)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()
                }

                // IQ processing
                cyclix_gen.begif(IQ_inst.rd)
                run {
                    IQ_inst.pop_trx()
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

            // issuing operations from IQ to FUs
            if (iq_id < ExecUnits.size) {

                MSG("Translating: issuing operations from IQ to FUs")
                var iq_iter = cyclix_gen.begforall_asc(IQ_inst.TRX_BUF)
                run {

                    var iq_entry            = IQ_inst.TRX_BUF.GetFracRef(iq_iter.iter_num)
                    var iq_entry_enb        = iq_entry.GetFracRef("enb")
                    var iq_entry_fu_pending = iq_entry.GetFracRef("fu_pending")
                    var iq_entry_fu_opcode  = iq_entry.GetFracRef("fu_opcode")
                    var iq_entry_rd_tag     = iq_entry.GetFracRef("rd_tag")

                    cyclix_gen.begif(iq_entry_enb)
                    run {
                        var rss_rdy = cyclix_gen.ulocal(cyclix_gen.GetGenName("rss_rdy"), 0, 0, "0")
                        cyclix_gen.assign(rss_rdy, 1)
                        for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                            cyclix_gen.band_gen(rss_rdy, rss_rdy, iq_entry.GetFracRef("rs" + RF_rs_idx + "_rdy"))
                        }
                        cyclix_gen.begif(rss_rdy)
                        run {
                            cyclix_gen.begif(cyclix_gen.subStruct(iq_iter.iter_elem, "fu_req"))
                            run {

                                // writing op to FU
                                for (exu_inst_num in 0 until ExUnits_insts[iq_id].size) {

                                    cyclix_gen.begif(!iq_entry_fu_pending)
                                    run {

                                        // filling exu_req with iq data
                                        cyclix_gen.assign(exu_req.GetFracRef("opcode"), iq_entry_fu_opcode)

                                        for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                                            cyclix_gen.assign(exu_req.GetFracRef("rs" + RF_rs_idx + "_rdata"), iq_entry.GetFracRef("rs" + RF_rs_idx + "_rdata"))
                                        }

                                        cyclix_gen.assign(exu_req.GetFracRef("rd_tag"), iq_entry_rd_tag)

                                        cyclix_gen.begif(cyclix_gen.fifo_internal_wr_unblk(ExUnits_insts[iq_id][exu_inst_num], cyclix.STREAM_REQ_BUS_NAME, exu_req))
                                        run {
                                            cyclix_gen.assign(iq_entry_fu_pending, 1)
                                        }; cyclix_gen.endif()

                                    }; cyclix_gen.endif()

                                }

                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()
                }; cyclix_gen.endloop()

            }

            iq_id++
        }

        var renamed_uop_buf = uop_buffer(cyclix_gen, "genrenamed_uop_buf", 1, ExecUnits.size, iq_struct, MultiExu_cfg_rf, Exu_cfg_rf)
        renamed_uop_buf.init_pctrls()
        renamed_uop_buf.set_rdy()
        renamed_uop_buf.init_locals()

        MSG("Translating: broadcasting FU results to IQ and renamed buffer")
        var fu_id = 0
        for (exu_num in 0 until ExUnits_insts.size) {
            for (exu_inst_num in 0 until ExUnits_insts[exu_num].size) {
                cyclix_gen.begif(cyclix_gen.fifo_internal_rd_unblk(ExUnits_insts[exu_num][exu_inst_num], cyclix.STREAM_RESP_BUS_NAME, exu_resp))
                run {

                    // updating PRF state
                    cyclix_gen.assign(
                        PRF_rdy,
                        hw_fracs(hw_frac_V(cyclix_gen.subStruct(exu_resp, "tag"))),
                        1)
                    cyclix_gen.assign(
                        PRF,
                        hw_fracs(hw_frac_V(cyclix_gen.subStruct(exu_resp, "tag"))),
                        cyclix_gen.subStruct(exu_resp, "wdata"))

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

                                for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {

                                    var iq_entry_rs_tag     = iq_entry.GetFracRef("rs" + RF_rs_idx + "_tag")
                                    var iq_entry_rs_rdy     = iq_entry.GetFracRef("rs" + RF_rs_idx + "_rdy")
                                    var iq_entry_rs_rdata   = iq_entry.GetFracRef("rs" + RF_rs_idx + "_rdata")

                                    cyclix_gen.begif(!iq_entry_rs_rdy)
                                    run {
                                        cyclix_gen.begif(cyclix_gen.eq2(iq_entry_rs_tag, cyclix_gen.subStruct(exu_resp, "tag")))
                                        run {
                                            // setting IQ entry ready
                                            cyclix_gen.assign(iq_entry_rs_rdata, cyclix_gen.subStruct(exu_resp, "wdata"))
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
                                // fu_req //
                                cyclix_gen.begif(iq_entry_fu_pending)
                                run {
                                    cyclix_gen.begif(cyclix_gen.eq2(iq_entry_rd_tag, cyclix_gen.subStruct(exu_resp, "tag")))
                                    run {
                                        cyclix_gen.assign(iq_entry_rdy, 1)
                                    }; cyclix_gen.endif()
                                }; cyclix_gen.endif()

                            }; cyclix_gen.endif()

                        }; cyclix_gen.endloop()

                    }

                    // broadcasting FU results to renamed buffer
                    // monitoring generated data
                    for (renamed_uop_buf_idx in 0 until renamed_uop_buf.TRX_BUF_SIZE) {
                        var renamed_uop_buf_entry = renamed_uop_buf.TRX_BUF.GetFracRef(renamed_uop_buf_idx)
                        for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {

                            var rs_rdy      = renamed_uop_buf_entry.GetFracRef("rs" + RF_rs_idx + "_rdy")
                            var rs_tag      = renamed_uop_buf_entry.GetFracRef("rs" + RF_rs_idx + "_tag")
                            var rs_rdata    = renamed_uop_buf_entry.GetFracRef("rs" + RF_rs_idx + "_rdata")

                            cyclix_gen.begif(!rs_rdy)
                            run {
                                cyclix_gen.begif(cyclix_gen.eq2(rs_tag, cyclix_gen.subStruct(exu_resp, "tag")))
                                run {
                                    // setting IQ entry ready
                                    cyclix_gen.assign(rs_rdata, cyclix_gen.subStruct(exu_resp, "wdata"))
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
            }
            fu_id++
        }

        // acquiring new operation to iq tail
        cyclix_gen.begif(renamed_uop_buf.pctrl_active)
        run {

            for (IQ_inst in IQ_insts) {

                cyclix_gen.begif(cyclix_gen.eq2(renamed_uop_buf.fu_id, IQ_inst.iq_num))
                run {
                    cyclix_gen.begif(IQ_inst.pctrl_rdy)
                    run {

                        // signaling iq_wr
                        cyclix_gen.assign(IQ_inst.wr, 1)
                        IQ_inst.push_trx(renamed_uop_buf.TRX_BUF_head_ref)

                        // clearing renamed uop buffer
                        renamed_uop_buf.pop_trx()

                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
            }
        }; cyclix_gen.endif()

        // renaming
        var new_renamed_uop = cyclix_gen.local("gennew_renamed_uop", iq_struct)
        var nru_enb             = new_renamed_uop.GetFracRef("enb")
        var nru_rdy             = new_renamed_uop.GetFracRef("rdy")
        var nru_fu_req          = new_renamed_uop.GetFracRef("fu_req")
        var nru_fu_pending      = new_renamed_uop.GetFracRef("fu_pending")
        var nru_fu_id           = new_renamed_uop.GetFracRef("fu_id")
        var nru_fu_opcode       = new_renamed_uop.GetFracRef("fu_opcode")
        var nru_wb_ext          = new_renamed_uop.GetFracRef("wb_ext")
        var nru_rd_tag          = new_renamed_uop.GetFracRef("rd_tag")
        var nru_rd_tag_prev     = new_renamed_uop.GetFracRef("rd_tag_prev")
        var nru_rd_tag_prev_clr = new_renamed_uop.GetFracRef("rd_tag_prev_clr")

        cyclix_gen.begif(renamed_uop_buf.pctrl_rdy)                 // checking if renamed uop buffer is empty
        run {
            cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(cmd_req, cmd_req_data))
            run {

                // decoding input
                cyclix_gen.assign(nru_enb, 1)

                cyclix_gen.assign(nru_fu_pending, 0)
                cyclix_gen.assign(nru_fu_id,      cmd_req_data.GetFracRef("fu_id"))
                cyclix_gen.assign(nru_fu_opcode,  cmd_req_data.GetFracRef("fu_opcode"))

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
                for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                    rss_tags.add(cyclix_gen.indexed(ARF_map, cmd_req_data.GetFracRef("fu_rs" + RF_rs_idx)))
                }
                var rd_tag = cyclix_gen.indexed(ARF_map, cmd_req_data.GetFracRef("fu_rd"))

                for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                    cyclix_gen.assign(new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_tag"), rss_tags[RF_rs_idx])
                    cyclix_gen.assign(
                        new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdata"),
                        cyclix_gen.indexed(PRF, new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_tag")))
                }

                var alloc_rd_tag = cyclix_gen.min0(PRF_mapped)

                cyclix_gen.begif(cmd_req_data.GetFracRef("exec"))
                run {

                    cyclix_gen.assign(nru_fu_req, 1)

                    for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                        // fetching rdy flags from PRF_rdy and masking with rsX_req
                        cyclix_gen.assign(
                            new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdy"),
                            cyclix_gen.bor(cyclix_gen.indexed(PRF_rdy, rss_tags[RF_rs_idx]), !cyclix_gen.subStruct(cmd_req_data, "fu_rs" + RF_rs_idx + "_req")) )
                    }

                    cyclix_gen.assign(nru_rd_tag, alloc_rd_tag.position)            // TODO: check for availability flag
                    cyclix_gen.assign(nru_rd_tag_prev, rd_tag)
                    cyclix_gen.assign(nru_rd_tag_prev_clr, cyclix_gen.indexed(PRF_mapped, rd_tag))

                    cyclix_gen.assign(ARF_map.GetFracRef(cmd_req_data.GetFracRef("fu_rd")), alloc_rd_tag.position)
                    cyclix_gen.assign(PRF_mapped.GetFracRef(alloc_rd_tag.position), 1)
                    cyclix_gen.assign(PRF_rdy.GetFracRef(alloc_rd_tag.position), 0)

                    cyclix_gen.assign(nru_rdy, 0)
                    cyclix_gen.assign(nru_wb_ext, 0)
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

                        for (RF_rs_idx in 1 until Exu_cfg_rf.RF_rs_num) {
                            cyclix_gen.assign(new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdy"), 1)
                        }

                        cyclix_gen.assign(nru_rdy, new_renamed_uop.GetFracRef("rs0_rdy"))
                        cyclix_gen.assign(nru_wb_ext, 1)

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()

                // placing new uop in rename_buf
                renamed_uop_buf.push_trx(new_renamed_uop)

            }; cyclix_gen.endif()
        }; cyclix_gen.endif()

        cyclix_gen.end()

        MSG("#################################################")
        MSG("#### Reordex-to-Cyclix translation complete! ####")
        MSG("#### module: " + name)
        MSG("#################################################")

        return cyclix_gen
    }
}
