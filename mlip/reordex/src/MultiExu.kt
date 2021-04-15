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

        MSG("Translating to cyclix: beginning")

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
        var cmd_resp = cyclix_gen.fifo_out("cmd_resp",  hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(Exu_cfg_rf.RF_width-1, 0)))
        var cmd_resp_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_resp_data"), hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(Exu_cfg_rf.RF_width-1, 0)), "0")

        // TODO: external memory interface

        var iq_struct = hw_struct("iq_struct")
        iq_struct.addu("enb",     0, 0, "0")
        iq_struct.addu("rdy",     0, 0, "0")
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

        var TranslateInfo = __TranslateInfo()

        var IQ_insts = ArrayList<__iq_info>()
        var ExUnits_insts = ArrayList<ArrayList<cyclix.hw_subproc>>()

        var exu_req = cyclix_gen.local(cyclix_gen.GetGenName("exu_req"), Exu_cfg_rf.req_struct)
        var exu_resp = cyclix_gen.local(cyclix_gen.GetGenName("exu_resp"), Exu_cfg_rf.resp_struct)

        var ExUnit_idx = 0
        for (ExUnit in ExecUnits) {

            // generating IQ
            var iq_info = __iq_info(
                cyclix_gen.global("geniq" + ExUnit_idx, iq_struct, ExUnit.value.iq_length-1, 0),
                cyclix_gen.uglobal("geniq" + ExUnit_idx + "_wr_ptr", GetWidthToContain(ExUnit.value.iq_length)-1, 0, "0"),
                cyclix_gen.ulocal("geniq" + ExUnit_idx + "_wr_ptr_prev", GetWidthToContain(ExUnit.value.iq_length)-1, 0, "0"),
                cyclix_gen.ulocal("geniq" + ExUnit_idx + "_wr_ptr_inc", GetWidthToContain(ExUnit.value.iq_length)-1, 0, "0"),
                cyclix_gen.ulocal("geniq" + ExUnit_idx + "_wr_ptr_dec", GetWidthToContain(ExUnit.value.iq_length)-1, 0, "0"),
                cyclix_gen.ulocal("geniq" + ExUnit_idx + "_wr", 0, 0, "0"),      // new entry entered IQ tail
                cyclix_gen.ulocal("geniq" + ExUnit_idx + "_rd", 0, 0, "0"),      // entry removed from IQ head
                cyclix_gen.uglobal("geniq" + ExUnit_idx + "_full", 0, 0, "0"),      // entry removed from IQ head
                cyclix_gen.local("geniq" + ExUnit_idx + "_head", iq_struct),
                hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()),
                true
            )
            IQ_insts.add(iq_info)
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
        var iq_info = __iq_info(
            cyclix_gen.global("genwb", iq_struct, out_iq_size-1, 0),
            cyclix_gen.uglobal("genwb_wr_ptr", GetWidthToContain(out_iq_size)-1, 0, "0"),
            cyclix_gen.ulocal("genwb_wr_ptr_prev", GetWidthToContain(out_iq_size)-1, 0, "0"),
            cyclix_gen.ulocal("genwb_wr_ptr_inc", GetWidthToContain(out_iq_size)-1, 0, "0"),
            cyclix_gen.ulocal("genwb_wr_ptr_dec", GetWidthToContain(out_iq_size)-1, 0, "0"),
            cyclix_gen.ulocal("genwb_wr", 0, 0, "0"),      // new entry entered IQ tail
            cyclix_gen.ulocal("genwb_rd", 0, 0, "0"),      // entry removed from IQ head
            cyclix_gen.uglobal("genwb_full", 0, 0, "0"),      // entry removed from IQ head
            cyclix_gen.local("genwb_head", iq_struct),
            hw_imm(GetWidthToContain(ExecUnits.size + 1), ExUnit_idx.toString()),
            false
        )
        IQ_insts.add(iq_info)

        var iq_id = 0
        for (IQ_inst in IQ_insts) {

            // generating iq ptrs
            cyclix_gen.assign(IQ_inst.iq_wr_ptr_prev, IQ_inst.iq_wr_ptr)
            cyclix_gen.add_gen(IQ_inst.iq_wr_ptr_inc, IQ_inst.iq_wr_ptr, 1)
            cyclix_gen.sub_gen(IQ_inst.iq_wr_ptr_dec, IQ_inst.iq_wr_ptr, 1)

            // committing IQ head
            cyclix_gen.assign(IQ_inst.iq_head, cyclix_gen.indexed(IQ_inst.iq, 0))
            cyclix_gen.begif(cyclix_gen.band(cyclix_gen.subStruct(IQ_inst.iq_head, "enb"), cyclix_gen.subStruct(IQ_inst.iq_head, "rdy")))
            run {

                if (IQ_inst.iq_exu) {
                    cyclix_gen.begif(cyclix_gen.subStruct(IQ_inst.iq_head, "rd_tag_prev_clr"))
                    run {
                        // PRF written, and previous tag can be remapped
                        cyclix_gen.assign(
                            PRF_mapped,
                            hw_fracs(hw_frac_V(cyclix_gen.subStruct(IQ_inst.iq_head, "rd_tag_prev"))),
                            0)
                    }; cyclix_gen.endif()
                    cyclix_gen.assign(IQ_inst.iq_rd, 1)
                } else {
                    cyclix_gen.assign(IQ_inst.iq_rd, 1)
                    cyclix_gen.begif(cyclix_gen.subStruct(IQ_inst.iq_head, "wb_ext"))
                    run {
                        cyclix_gen.assign(IQ_inst.iq_rd, 0)
                        cyclix_gen.begif(cyclix_gen.fifo_wr_unblk(cmd_resp, cyclix_gen.subStruct(IQ_inst.iq_head, "rs0_rdata")))
                        run {
                            cyclix_gen.assign(IQ_inst.iq_rd, 1)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()
                }

                // IQ processing
                cyclix_gen.begif(IQ_inst.iq_rd)
                run {
                    cyclix_gen.assign(IQ_inst.iq_full, 0)
                    // shifting ops
                    var iq_shift_iter = cyclix_gen.begforrange_asc(IQ_inst.iq, hw_imm(0), hw_imm(IQ_inst.iq.vartype.dimensions.last().msb-1))
                    run {
                        cyclix_gen.assign(
                            IQ_inst.iq,
                            hw_fracs(hw_frac_V(iq_shift_iter.iter_num)),
                            cyclix_gen.indexed(IQ_inst.iq, iq_shift_iter.iter_num_next))
                    }; cyclix_gen.endloop()
                    cyclix_gen.assign(
                        IQ_inst.iq,
                        hw_fracs(hw_frac_C(IQ_inst.iq.vartype.dimensions.last().msb)),
                        0)
                    cyclix_gen.assign(IQ_inst.iq_wr_ptr, IQ_inst.iq_wr_ptr_dec)
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

            // issuing operations from IQ to FUs
            if (iq_id < ExecUnits.size) {

                MSG("Translating: issuing operations from IQ to FUs")
                var iq_iter = cyclix_gen.begforall_asc(IQ_inst.iq)
                run {
                    cyclix_gen.begif(cyclix_gen.subStruct(iq_iter.iter_elem, "enb"))
                    run {
                        var rss_rdy = cyclix_gen.ulocal(cyclix_gen.GetGenName("rss_rdy"), 0, 0, "0")
                        cyclix_gen.assign(rss_rdy, 1)
                        for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                            cyclix_gen.band_gen(rss_rdy, rss_rdy, cyclix_gen.subStruct(iq_iter.iter_elem, "rs" + RF_rs_idx + "_rdy"))
                        }
                        cyclix_gen.begif(rss_rdy)
                        run {
                            cyclix_gen.begif(cyclix_gen.subStruct(iq_iter.iter_elem, "fu_req"))
                            run {

                                // writing op to FU

                                for (exu_inst_num in 0 until ExUnits_insts[iq_id].size) {

                                    cyclix_gen.begif(!cyclix_gen.subStruct(cyclix_gen.indexed(IQ_inst.iq, iq_iter.iter_num), "fu_pending"))
                                    run {

                                        // filling exu_req with iq data
                                        cyclix_gen.assign(
                                            exu_req,
                                            hw_fracs(hw_frac_SubStruct("opcode")),
                                            cyclix_gen.subStruct(iq_iter.iter_elem, "fu_opcode"))

                                        for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                                            cyclix_gen.assign(
                                                exu_req,
                                                hw_fracs(hw_frac_SubStruct("rs" + RF_rs_idx + "_rdata")),
                                                cyclix_gen.subStruct(iq_iter.iter_elem, "rs" + RF_rs_idx + "_rdata"))
                                        }

                                        cyclix_gen.assign(
                                            exu_req,
                                            hw_fracs(hw_frac_SubStruct("rd_tag")),
                                            cyclix_gen.subStruct(iq_iter.iter_elem, "rd_tag"))

                                        cyclix_gen.begif(cyclix_gen.fifo_internal_wr_unblk(ExUnits_insts[iq_id][exu_inst_num], cyclix.STREAM_REQ_BUS_NAME, exu_req))
                                        run {
                                            cyclix_gen.assign(
                                                IQ_inst.iq,
                                                hw_fracs(hw_frac_V(iq_iter.iter_num), hw_frac_SubStruct("fu_pending")),
                                                1)
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

        var renamed_uop_buf = cyclix_gen.global("genrenamed_uop_buf", iq_struct)

        // broadcasting FU results to IQ and renamed buffer
        MSG("Translating: broadcasting FU results to IQ")
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

                        var iq_iter = cyclix_gen.begforall_asc(IQ_inst.iq)
                        run {

                            cyclix_gen.begif(cyclix_gen.subStruct(iq_iter.iter_elem, "enb"))
                            run {

                                for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                                    cyclix_gen.begif(!cyclix_gen.subStruct(iq_iter.iter_elem, "rs" + RF_rs_idx + "_rdy"))
                                    run {
                                        cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(iq_iter.iter_elem, "rs" + RF_rs_idx + "_tag"), cyclix_gen.subStruct(exu_resp, "tag")))
                                        run {
                                            // setting IQ entry ready
                                            cyclix_gen.assign(
                                                IQ_inst.iq,
                                                hw_fracs(hw_frac_V(iq_iter.iter_num), hw_frac_SubStruct("rs" + RF_rs_idx + "_rdata")),
                                                cyclix_gen.subStruct(exu_resp, "wdata"))
                                            cyclix_gen.assign(
                                                IQ_inst.iq,
                                                hw_fracs(hw_frac_V(iq_iter.iter_num), hw_frac_SubStruct("rs" + RF_rs_idx + "_rdy")),
                                                1)
                                        }; cyclix_gen.endif()
                                    }; cyclix_gen.endif()
                                }

                                //// setting rdy if data generated ////
                                // wb_ext //
                                cyclix_gen.begif(cyclix_gen.subStruct(iq_iter.iter_elem, "wb_ext"))
                                run {
                                    cyclix_gen.assign(
                                        IQ_inst.iq,
                                        hw_fracs(hw_frac_V(iq_iter.iter_num), hw_frac_SubStruct("rdy")),
                                        cyclix_gen.subStruct(cyclix_gen.indexed(IQ_inst.iq, iq_iter.iter_num), "rs0_rdy"))
                                }; cyclix_gen.endif()
                                // fu_req //
                                cyclix_gen.begif(cyclix_gen.subStruct(iq_iter.iter_elem, "fu_pending"))
                                run {
                                    cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(iq_iter.iter_elem, "rd_tag"), cyclix_gen.subStruct(exu_resp, "tag")))
                                    run {
                                        cyclix_gen.assign(
                                            IQ_inst.iq,
                                            hw_fracs(hw_frac_V(iq_iter.iter_num), hw_frac_SubStruct("rdy")),
                                            1)
                                    }; cyclix_gen.endif()
                                }; cyclix_gen.endif()

                            }; cyclix_gen.endif()

                        }; cyclix_gen.endloop()

                    }

                    // broadcasting FU results to renamed buffer
                    cyclix_gen.begif(cyclix_gen.subStruct(renamed_uop_buf, "enb"))
                    run {
                        // monitoring generated data
                        for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                            cyclix_gen.begif(!cyclix_gen.subStruct(renamed_uop_buf, "rs" + RF_rs_idx + "_rdy"))
                            run {
                                cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(renamed_uop_buf, "rs" + RF_rs_idx + "_tag"), cyclix_gen.subStruct(exu_resp, "tag")))
                                run {
                                    // setting IQ entry ready
                                    cyclix_gen.assign(
                                        renamed_uop_buf,
                                        hw_fracs(hw_frac_SubStruct("rs" + RF_rs_idx + "_rdata")),
                                        cyclix_gen.subStruct(exu_resp, "wdata"))
                                    cyclix_gen.assign(
                                        renamed_uop_buf,
                                        hw_fracs(hw_frac_SubStruct("rs" + RF_rs_idx + "_rdy")),
                                        1)
                                }; cyclix_gen.endif()
                            }; cyclix_gen.endif()
                        }
                        //// setting rdy for wb_ext if data generated ////
                        cyclix_gen.begif(cyclix_gen.subStruct(renamed_uop_buf, "wb_ext"))
                        run {
                            cyclix_gen.assign(
                                renamed_uop_buf,
                                hw_fracs(hw_frac_SubStruct("rdy")),
                                cyclix_gen.subStruct(renamed_uop_buf, "rs0_rdy"))
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()
            }
            fu_id++
        }

        // acquiring new operation to iq tail
        cyclix_gen.begif(cyclix_gen.subStruct(renamed_uop_buf, "enb"))
        run {

            for (IQ_inst in IQ_insts) {

                cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(renamed_uop_buf, "fu_id"), IQ_inst.iq_num))
                run {

                    cyclix_gen.begif(!IQ_inst.iq_full)
                    run {
                        // signaling iq_wr
                        cyclix_gen.assign(IQ_inst.iq_wr, 1)

                        // putting new uop in IQ
                        cyclix_gen.assign(
                            IQ_inst.iq,
                            hw_fracs(hw_frac_V(IQ_inst.iq_wr_ptr)),
                            renamed_uop_buf
                        )

                        // iq_wr_ptr update
                        cyclix_gen.begif(IQ_inst.iq_wr)
                        run {
                            // asserting iq_full - checking if data written to last available entry
                            cyclix_gen.begif(cyclix_gen.eq2(IQ_inst.iq_wr_ptr, IQ_inst.iq.vartype.dimensions.last().msb))
                            run {
                                cyclix_gen.assign(IQ_inst.iq_full, 1)
                            }; cyclix_gen.endif()

                            // incrementing iq_wr_ptr
                            cyclix_gen.begif(IQ_inst.iq_rd)
                            run {
                                cyclix_gen.assign(IQ_inst.iq_wr_ptr, IQ_inst.iq_wr_ptr_prev)
                            }; cyclix_gen.endif()
                            cyclix_gen.begelse()
                            run {
                                cyclix_gen.assign(IQ_inst.iq_wr_ptr, IQ_inst.iq_wr_ptr_inc)
                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()

                        // clearing renamed uop buffer
                        cyclix_gen.assign(
                            renamed_uop_buf,
                            hw_fracs(hw_frac_SubStruct("enb")),
                            0
                        )

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()
            }
        }; cyclix_gen.endif()

        // renaming
        var new_renamed_uop = cyclix_gen.local("gennew_renamed_uop", iq_struct)
        cyclix_gen.begif(!cyclix_gen.subStruct(renamed_uop_buf, "enb"))         // checking if renamed uop buffer is empty
        run {
            cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(cmd_req, cmd_req_data))
            run {

                // decoding input
                cyclix_gen.assign(
                    new_renamed_uop,
                    hw_fracs(hw_frac_SubStruct("enb")),
                    1)

                cyclix_gen.assign(
                    new_renamed_uop,
                    hw_fracs(hw_frac_SubStruct("fu_pending")),
                    0)
                cyclix_gen.assign(
                    new_renamed_uop,
                    hw_fracs(hw_frac_SubStruct("fu_id")),
                    cyclix_gen.subStruct(cmd_req_data, "fu_id"))
                cyclix_gen.assign(
                    new_renamed_uop,
                    hw_fracs(hw_frac_SubStruct("fu_opcode")),
                    cyclix_gen.subStruct(cmd_req_data, "fu_opcode"))

                // LOAD/STORE commutation
                cyclix_gen.begif(!cyclix_gen.subStruct(cmd_req_data, "exec"))
                run {
                    cyclix_gen.assign(
                        new_renamed_uop,
                        hw_fracs(hw_frac_SubStruct("fu_id")),
                        ExecUnits.size)

                    // LOAD
                    cyclix_gen.begif(cyclix_gen.subStruct(cmd_req_data, "rf_we"))
                    run {
                        cyclix_gen.assign(
                            cmd_req_data,
                            hw_fracs(hw_frac_SubStruct("fu_rd")),
                            cyclix_gen.subStruct(cmd_req_data, "rf_addr"))
                    }; cyclix_gen.endif()

                    // STORE
                    cyclix_gen.begelse()
                    run {
                        cyclix_gen.assign(
                            cmd_req_data,
                            hw_fracs(hw_frac_SubStruct("fu_rs0")),
                            cyclix_gen.subStruct(cmd_req_data, "rf_addr"))
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()

                var rss_tags = ArrayList<hw_var>()
                for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                    rss_tags.add(cyclix_gen.indexed(ARF_map, cyclix_gen.subStruct(cmd_req_data, "fu_rs" + RF_rs_idx)))
                }
                var rd_tag = cyclix_gen.indexed(ARF_map, cyclix_gen.subStruct(cmd_req_data, "fu_rd"))

                for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                    cyclix_gen.assign(
                        new_renamed_uop,
                        hw_fracs(hw_frac_SubStruct("rs" + RF_rs_idx + "_tag")),
                        rss_tags[RF_rs_idx])
                    cyclix_gen.assign(
                        new_renamed_uop,
                        hw_fracs(hw_frac_SubStruct("rs" + RF_rs_idx + "_rdata")),
                        cyclix_gen.indexed(PRF, cyclix_gen.subStruct(new_renamed_uop, "rs" + RF_rs_idx + "_tag")))
                }

                var alloc_rd_tag = cyclix_gen.min0(PRF_mapped)

                cyclix_gen.begif(cyclix_gen.subStruct(cmd_req_data, "exec"))
                run {

                    cyclix_gen.assign(
                        new_renamed_uop,
                        hw_fracs(hw_frac_SubStruct("fu_req")),
                        1)

                    for (RF_rs_idx in 0 until Exu_cfg_rf.RF_rs_num) {
                        // fetching rdy flags from PRF_rdy and masking with rsX_req
                        cyclix_gen.assign(
                            new_renamed_uop,
                            hw_fracs(hw_frac_SubStruct("rs" + RF_rs_idx + "_rdy")),
                            cyclix_gen.bor(cyclix_gen.indexed(PRF_rdy, rss_tags[RF_rs_idx]), !cyclix_gen.subStruct(cmd_req_data, "fu_rs" + RF_rs_idx + "_req")) )
                    }

                    cyclix_gen.assign(
                        new_renamed_uop,
                        hw_fracs(hw_frac_SubStruct("rd_tag")),
                        alloc_rd_tag.position)                                      // TODO: check for availability flag
                    cyclix_gen.assign(
                        new_renamed_uop,
                        hw_fracs(hw_frac_SubStruct("rd_tag_prev")),
                        rd_tag)
                    cyclix_gen.assign(
                        new_renamed_uop,
                        hw_fracs(hw_frac_SubStruct("rd_tag_prev_clr")),
                        cyclix_gen.indexed(PRF_mapped, rd_tag))
                    cyclix_gen.assign(
                        ARF_map,
                        hw_fracs(hw_frac_V(cyclix_gen.subStruct(cmd_req_data, "fu_rd"))),
                        alloc_rd_tag.position)
                    cyclix_gen.assign(
                        PRF_mapped,
                        hw_fracs(hw_frac_V(alloc_rd_tag.position)),
                        1)
                    cyclix_gen.assign(
                        PRF_rdy,
                        hw_fracs(hw_frac_V(alloc_rd_tag.position)),
                        0)

                    cyclix_gen.assign(
                        new_renamed_uop,
                        hw_fracs(hw_frac_SubStruct("rdy")),
                        0)

                    cyclix_gen.assign(
                        new_renamed_uop,
                        hw_fracs(hw_frac_SubStruct("wb_ext")),
                        0)
                }; cyclix_gen.endif()

                cyclix_gen.begelse()
                run {

                    // LOAD
                    cyclix_gen.begif(cyclix_gen.subStruct(cmd_req_data, "rf_we"))
                    run {

                        cyclix_gen.assign(
                            new_renamed_uop,
                            hw_fracs(hw_frac_SubStruct("rdy")),
                            1)

                        cyclix_gen.assign(
                            new_renamed_uop,
                            hw_fracs(hw_frac_SubStruct("rd_tag")),
                            alloc_rd_tag.position)                                      // TODO: check for availability flag
                        cyclix_gen.assign(
                            new_renamed_uop,
                            hw_fracs(hw_frac_SubStruct("rd_tag_prev")),
                            rd_tag)
                        cyclix_gen.assign(
                            new_renamed_uop,
                            hw_fracs(hw_frac_SubStruct("rd_tag_prev_clr")),
                            cyclix_gen.indexed(PRF_mapped, rd_tag))
                        cyclix_gen.assign(
                            PRF_mapped,
                            hw_fracs(hw_frac_V(alloc_rd_tag.position)),
                            1)
                        cyclix_gen.assign(
                            ARF_map,
                            hw_fracs(hw_frac_V(cyclix_gen.subStruct(cmd_req_data, "fu_rd"))),
                            alloc_rd_tag.position)
                        cyclix_gen.assign(
                            PRF_rdy,
                            hw_fracs(hw_frac_V(alloc_rd_tag.position)),
                            1)
                        cyclix_gen.assign(
                            PRF,
                            hw_fracs(hw_frac_V(alloc_rd_tag.position)),
                            cyclix_gen.subStruct(cmd_req_data, "rf_wdata"))

                    }; cyclix_gen.endif()

                    // STORE
                    cyclix_gen.begelse()
                    run {
                        cyclix_gen.assign(
                            new_renamed_uop,
                            hw_fracs(hw_frac_SubStruct("rs0_rdy")),
                            cyclix_gen.indexed(PRF_rdy, rss_tags[0]))

                        for (RF_rs_idx in 1 until Exu_cfg_rf.RF_rs_num) {
                            cyclix_gen.assign(
                                new_renamed_uop,
                                hw_fracs(hw_frac_SubStruct("rs" + RF_rs_idx + "_rdy")),
                                1)
                        }

                        cyclix_gen.assign(
                            new_renamed_uop,
                            hw_fracs(hw_frac_SubStruct("rdy")),
                            cyclix_gen.subStruct(new_renamed_uop, "rs0_rdy"))

                        cyclix_gen.assign(
                            new_renamed_uop,
                            hw_fracs(hw_frac_SubStruct("wb_ext")),
                            1)

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()

                // placing new uop in rename_buf
                cyclix_gen.assign(renamed_uop_buf, new_renamed_uop)

            }; cyclix_gen.endif()
        }; cyclix_gen.endif()

        cyclix_gen.end()
        MSG(DEBUG_FLAG, "Translating to cyclix: complete")
        return cyclix_gen
    }
}
