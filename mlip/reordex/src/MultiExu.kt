/*
 * MultiExu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

data class MultiExu_CFG_RF(val RF_width : Int,
                           val ARF_depth : Int,
                           val rename_RF: Boolean,
                           val PRF_depth : Int)

data class Exu_CFG(val ExecUnit : Exu,
                   val exu_num : Int)

open class MultiExu(val name : String, val Exu_cfg_rf : Exu_CFG_RF, val MultiExu_cfg_rf : MultiExu_CFG_RF, val rob_size : Int) {

    val arf_addr_width = GetWidthToContain(MultiExu_cfg_rf.ARF_depth)
    val prf_addr_width = GetWidthToContain(MultiExu_cfg_rf.PRF_depth)

    var ExecUnits  = mutableMapOf<String, Exu_CFG>()

    fun add_exu(exu : Exu, exu_num: Int) {
        if (ExecUnits.put(exu.name, Exu_CFG(exu, exu_num)) != null) {
            ERROR("Stage addition problem!")
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

    fun reconstruct_expression(cyclix_gen : cyclix.Streaming,
                               expr : hw_exec,
                               var_dict : MutableMap<hw_var, hw_var>) {

        // println("#### Cyclix: exporting expression: " + expr.opcode.default_string)
        // for (param in expr.params) println("param: " + param.GetString())
        // for (wrvar in expr.wrvars) println("wrvar: " + wrvar.name)

        var fractions = hw_fracs()
        for (src_fraction in expr.assign_tgt_fractured.depow_fractions) {
            if (src_fraction is hw_frac_C) fractions.add(src_fraction)
            else if (src_fraction is hw_frac_V) fractions.add(hw_frac_V(TranslateVar(src_fraction.index, var_dict)))
            else if (src_fraction is hw_frac_CC) fractions.add(src_fraction)
            else if (src_fraction is hw_frac_CV) fractions.add(hw_frac_CV(src_fraction.msb, TranslateVar(src_fraction.lsb, var_dict)))
            else if (src_fraction is hw_frac_VC) fractions.add(hw_frac_VC(TranslateVar(src_fraction.msb, var_dict), src_fraction.lsb))
            else if (src_fraction is hw_frac_VV) fractions.add(hw_frac_VV(TranslateVar(src_fraction.msb, var_dict), TranslateVar(src_fraction.lsb, var_dict)))
            else if (src_fraction is hw_frac_SubStruct) fractions.add(src_fraction)
            else ERROR("dimensions error")
        }

        if ((expr.opcode == OP1_ASSIGN)) {
            cyclix_gen.assign(TranslateVar(expr.wrvars[0], var_dict), fractions, TranslateParam(expr.params[0], var_dict))

        } else if ((expr.opcode == OP2_ARITH_ADD)
            || (expr.opcode == OP2_ARITH_SUB)
            || (expr.opcode == OP2_ARITH_MUL)
            || (expr.opcode == OP2_ARITH_DIV)
            || (expr.opcode == OP2_ARITH_SLL)
            || (expr.opcode == OP2_ARITH_SRL)
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
                params.add(TranslateParam(param, var_dict))
            }
            cyclix_gen.AddExpr_op_gen(expr.opcode, TranslateVar(expr.wrvars[0], var_dict), params)

        } else if (expr.opcode == OP2_SUBSTRUCT) {
            cyclix_gen.subStruct_gen(
                TranslateVar(expr.wrvars[0], var_dict),
                TranslateVar(expr.rdvars[0], var_dict),
                expr.subStructvar_name
            )

        } else if (expr.opcode == OP1_IF) {

            cyclix_gen.begif(TranslateParam(expr.params[0], var_dict))
            run {
                for (child_expr in expr.expressions) {
                    reconstruct_expression(cyclix_gen, child_expr, var_dict)
                }
            }; cyclix_gen.endif()

        } else if (expr.opcode == OP1_CASE) {

            cyclix_gen.begcase(TranslateParam(expr.params[0], var_dict))
            run {
                for (casebranch in expr.expressions) {
                    if (casebranch.opcode != OP1_CASEBRANCH) ERROR("non-branch op in case")
                    cyclix_gen.begbranch(TranslateParam(casebranch.params[0], var_dict))
                    for (subexpr in casebranch.expressions) {
                        reconstruct_expression(cyclix_gen, subexpr, var_dict)
                    }
                    cyclix_gen.endbranch()
                }
            }; cyclix_gen.endcase()

        } else if (expr.opcode == OP1_WHILE) {

            cyclix_gen.begwhile(TranslateParam(expr.params[0], var_dict))
            run {
                for (child_expr in expr.expressions) {
                    reconstruct_expression(cyclix_gen, child_expr, var_dict)
                }
            }; cyclix_gen.endloop()

        /*
        } else if (expr.opcode == OP_FIFO_WR_UNBLK) {

            var fifo = TranslateFifoOut((expr as hw_exec_fifo_wr_unblk).fifo)
            var wdata_translated = TranslateParam(expr.params[0])
            var fifo_rdy = TranslateVar(expr.wrvars[0])

            rtl_gen.begif(rtl_gen.lnot(rst))
            run {
                rtl_gen.assign(fifo.ext_req, 1)
                rtl_gen.begif(fifo.reqbuf_req)
                run {
                    // fifo busy
                    rtl_gen.assign(fifo_rdy, 0)
                }; rtl_gen.endif()
                rtl_gen.begelse()
                run {
                    // fifo ready to consume request
                    rtl_gen.assign(fifo.ext_wdata, wdata_translated)
                    rtl_gen.assign(fifo_rdy, fifo.ext_ack)
                }; rtl_gen.endif()
                rtl_gen.assign(fifo.reqbuf_req, 1)
            }; rtl_gen.endif()

        } else if (expr.opcode == OP_FIFO_RD_UNBLK) {

            var fifo = TranslateFifoIn((expr as hw_exec_fifo_rd_unblk).fifo)
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
            */

        } else ERROR("Reconstruction of expression failed: opcode undefined: " + expr.opcode.default_string)

        // println("#### Cyclix: exporting expression complete!")
    }

    data class bit_position(var found : hw_var, var position: hw_var)

    fun max0(cyclix_gen : cyclix.Generic, datain : hw_var) : bit_position {
        var found = cyclix_gen.ulocal(cyclix_gen.GetGenName("flag"), 0, 0, "0")
        var position = cyclix_gen.ulocal(cyclix_gen.GetGenName("position"), GetWidthToContain(datain.GetWidth())-1, 0, "0")
        var iter = cyclix_gen.begforall_asc(datain)
        run {
            cyclix_gen.begif(!iter.iter_elem)
            run {
                cyclix_gen.assign(found, 1)
                cyclix_gen.assign(position, iter.iter_num)
            }; cyclix_gen.end()
        }; cyclix_gen.endif()
        return bit_position(found, position)
    }

    fun min0(cyclix_gen : cyclix.Generic, datain : hw_var) : bit_position {
        var found = cyclix_gen.ulocal(cyclix_gen.GetGenName("flag"), 0, 0, "0")
        var position = cyclix_gen.ulocal(cyclix_gen.GetGenName("position"), GetWidthToContain(datain.GetWidth())-1, 0, "0")
        var iter = cyclix_gen.begforall_desc(datain)
        run {
            cyclix_gen.begif(!iter.iter_elem)
            run {
                cyclix_gen.assign(found, 1)
                cyclix_gen.assign(position, iter.iter_num)
            }; cyclix_gen.end()
        }; cyclix_gen.endif()
        return bit_position(found, position)
    }

    fun max1(cyclix_gen : cyclix.Generic, datain : hw_var) : bit_position {
        var found = cyclix_gen.ulocal(cyclix_gen.GetGenName("flag"), 0, 0, "0")
        var position = cyclix_gen.ulocal(cyclix_gen.GetGenName("position"), GetWidthToContain(datain.GetWidth())-1, 0, "0")
        var iter = cyclix_gen.begforall_asc(datain)
        run {
            cyclix_gen.begif(iter.iter_elem)
            run {
                cyclix_gen.assign(found, 1)
                cyclix_gen.assign(position, iter.iter_num)
            }; cyclix_gen.end()
        }; cyclix_gen.endif()
        return bit_position(found, position)
    }

    fun min1(cyclix_gen : cyclix.Generic, datain : hw_var) : bit_position {
        var found = cyclix_gen.ulocal(cyclix_gen.GetGenName("flag"), 0, 0, "0")
        var position = cyclix_gen.ulocal(cyclix_gen.GetGenName("position"), GetWidthToContain(datain.GetWidth())-1, 0, "0")
        var iter = cyclix_gen.begforall_desc(datain)
        run {
            cyclix_gen.begif(iter.iter_elem)
            run {
                cyclix_gen.assign(found, 1)
                cyclix_gen.assign(position, iter.iter_num)
            }; cyclix_gen.end()
        }; cyclix_gen.endif()
        return bit_position(found, position)
    }

    fun translate_to_cyclix(DEBUG_FLAG : Boolean) : cyclix.Generic {

        MSG("Translating to cyclix: beginning")

        var cyclix_gen = cyclix.Generic(name)

        //// Generating PRF ////
        var prf_dim = hw_dim_static()
        prf_dim.add(MultiExu_cfg_rf.RF_width-1, 0)
        prf_dim.add(MultiExu_cfg_rf.PRF_depth-1, 0)
        var PRF = cyclix_gen.uglobal("genPRF", prf_dim, "0")
        var PRF_busy = cyclix_gen.uglobal("genPRF_busy", MultiExu_cfg_rf.PRF_depth-1, 0, "0")   // used tags flags
        var arf_map_dim = hw_dim_static()
        arf_map_dim.add(GetWidthToContain(MultiExu_cfg_rf.PRF_depth)-1, 0)
        arf_map_dim.add(MultiExu_cfg_rf.ARF_depth-1, 0)
        var ARF_map = cyclix_gen.uglobal("genARF_map", arf_map_dim, "0")        // ARF-to-PRF mappings

        //// Generating interfaces ////
        // cmd (sequential instruction stream) //
        var cmd_req_struct = hw_struct(name + "_cmd_req_struct")
        cmd_req_struct.addu("exec",     0, 0, "0")
        cmd_req_struct.addu("rf_we",       0,  0, "0")
        cmd_req_struct.addu("rf_addr",    arf_addr_width-1, 0, "0")
        cmd_req_struct.addu("rf_wdata",    MultiExu_cfg_rf.RF_width-1, 0, "0")
        cmd_req_struct.addu("fu_id",    GetWidthToContain(ExecUnits.size)-1, 0, "0")
        cmd_req_struct.addu("fu_opcode",     0, 0, "0")
        cmd_req_struct.addu("fu_rs0",    arf_addr_width-1, 0, "0")
        cmd_req_struct.addu("fu_rs1",    arf_addr_width-1, 0, "0")
        cmd_req_struct.addu("fu_rd",    arf_addr_width-1, 0, "0")
        var cmd_req = cyclix_gen.fifo_in("cmd_req",  hw_type(cmd_req_struct))
        var cmd_req_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_req_data"), cmd_req_struct)
        var cmd_resp = cyclix_gen.fifo_out("cmd_resp",  hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(MultiExu_cfg_rf.RF_width-1, 0)))
        var cmd_resp_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_resp_data"), hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(MultiExu_cfg_rf.RF_width-1, 0)), "0")

        // TODO: external memory interface

        var rob_struct = hw_struct("rob_struct")
        rob_struct.addu("enb",     0, 0, "0")
        rob_struct.addu("rdy",     0, 0, "0")
        rob_struct.addu("fu_req",     0, 0, "0")
        rob_struct.addu("fu_pending",     0, 0, "0")
        rob_struct.addu("fu_id",     GetWidthToContain(ExecUnits.size)-1, 0, "0")
        rob_struct.addu("fu_opcode",     0, 0, "0")
        rob_struct.addu("rs0_rdy",     0, 0, "0")
        rob_struct.addu("rs0_tag",     MultiExu_cfg_rf.PRF_depth-1, 0, "0")
        rob_struct.addu("rs0_rdata",     MultiExu_cfg_rf.RF_width-1, 0, "0")
        rob_struct.addu("rs1_rdy",     0, 0, "0")
        rob_struct.addu("rs1_tag",     MultiExu_cfg_rf.PRF_depth-1, 0, "0")
        rob_struct.addu("rs1_rdata",     MultiExu_cfg_rf.RF_width-1, 0, "0")
        rob_struct.addu("rd_tag",     MultiExu_cfg_rf.PRF_depth-1, 0, "0")
        rob_struct.addu("rd_tag_prev",     MultiExu_cfg_rf.PRF_depth-1, 0, "0")                 // freeing
        rob_struct.addu("wb_ext",     0, 0, "0")
        rob_struct.addu("wb_wdata",     MultiExu_cfg_rf.RF_width-1, 0, "0")

        var TranslateInfo = __TranslateInfo()

        var rob = cyclix_gen.global("genrob", rob_struct, rob_size-1, 0)
        var rob_wr_ptr = cyclix_gen.uglobal("genrob_wr_ptr", GetWidthToContain(rob_size)-1, 0, "0")
        var rob_wr_ptr_prev = cyclix_gen.ulocal("genrob_wr_ptr_prev", GetWidthToContain(rob_size)-1, 0, "0")
        var rob_wr_ptr_inc = cyclix_gen.ulocal("genrob_wr_ptr_inc", GetWidthToContain(rob_size)-1, 0, "0")
        var rob_wr_ptr_dec = cyclix_gen.ulocal("genrob_wr_ptr_dec", GetWidthToContain(rob_size)-1, 0, "0")
        var rob_wr = cyclix_gen.ulocal("genrob_wr", 0, 0, "0")      // new entry entered ROB tail
        var rob_rd = cyclix_gen.ulocal("genrob_rd", 0, 0, "0")      // entry removed from ROB head
        var rob_full = cyclix_gen.ulocal("genrob_full", 0, 0, "0")      // entry removed from ROB head

        var ExUnits_insts = ArrayList<ArrayList<cyclix.hw_subproc>>()

        var exu_req = cyclix_gen.local(cyclix_gen.GetGenName("exu_req"), Exu_cfg_rf.req_struct)
        var exu_resp = cyclix_gen.local(cyclix_gen.GetGenName("exu_resp"), Exu_cfg_rf.resp_struct)

        for (ExUnit in ExecUnits) {

            var exu_cyclix_gen = cyclix.Streaming("genexu_" + ExUnit.value.ExecUnit.name, Exu_cfg_rf.req_struct, Exu_cfg_rf.resp_struct)

            var var_dict = mutableMapOf<hw_var, hw_var>()

            // Generating locals
            for (local in ExUnit.value.ExecUnit.locals)
                var_dict.put(local, exu_cyclix_gen.local(local.name, local.vartype, local.defval))

            // Generating globals
            for (global in ExUnit.value.ExecUnit.globals)
                var_dict.put(global, exu_cyclix_gen.global(global.name, global.vartype, global.defval))

            // Generating intermediates
            for (genvar in ExUnit.value.ExecUnit[0].genvars)
                var_dict.put(genvar, exu_cyclix_gen.local(genvar.name, genvar.vartype, genvar.defval))

            exu_cyclix_gen.assign(TranslateVar(ExUnit.value.ExecUnit.req_data, var_dict), exu_cyclix_gen.stream_req_var)

            for (expr in ExUnit.value.ExecUnit[0].expressions) {
                reconstruct_expression(exu_cyclix_gen,
                    expr,
                    var_dict)
            }

            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var, TranslateVar(ExUnit.value.ExecUnit.resp_data, var_dict))
            exu_cyclix_gen.assign(exu_cyclix_gen.stream_resp_var, hw_fracs("tag"), exu_cyclix_gen.subStruct(exu_cyclix_gen.stream_req_var, "rd_tag"))

            exu_cyclix_gen.end()

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

        cyclix_gen.assign(rob_wr_ptr_prev, rob_wr_ptr)
        cyclix_gen.add_gen(rob_wr_ptr_inc, rob_wr_ptr, 1)
        cyclix_gen.sub_gen(rob_wr_ptr_dec, rob_wr_ptr, 1)

        // committing ROB head
        var rob_head = cyclix_gen.indexed(rob, 0)
        cyclix_gen.begif(cyclix_gen.band(cyclix_gen.subStruct(rob_head, "enb"), cyclix_gen.subStruct(rob_head, "rdy")))
        run {

            // wb
            cyclix_gen.begif(cyclix_gen.subStruct(rob_head, "wb_ext"))
            run {
                cyclix_gen.begif(cyclix_gen.fifo_wr_unblk(cmd_resp, cyclix_gen.subStruct(rob_head, "wb_wdata")))
                run {
                    cyclix_gen.assign(rob_rd, 1)
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

            // loads and execs
            cyclix_gen.begelse()
            run {
                // PRF written, and previous tag can be remapped
                cyclix_gen.assign(
                    PRF_busy,
                    hw_fracs(hw_frac_V(cyclix_gen.subStruct(rob_head, "rd_tag_prev"))),
                    0)
                cyclix_gen.assign(rob_rd, 1)
            }; cyclix_gen.endif()

            // ROB processing
            cyclix_gen.begif(rob_rd)
            run {
                cyclix_gen.assign(rob_full, 0)
                // shifting ops
                var rob_shift_iter = cyclix_gen.begforrange_asc(rob, hw_imm(0), hw_imm(rob.vartype.dimensions.last().msb-1))
                run {
                    cyclix_gen.assign(
                        rob,
                        hw_fracs(hw_frac_V(rob_shift_iter.iter_num)),
                        cyclix_gen.indexed(rob, rob_shift_iter.iter_num_next))
                }; cyclix_gen.endloop()
                cyclix_gen.assign(
                    rob,
                    hw_fracs(hw_frac_C(rob.vartype.dimensions.last().msb)),
                    0)
                cyclix_gen.assign(rob_wr_ptr, rob_wr_ptr_dec)
            }; cyclix_gen.endif()
        }; cyclix_gen.endif()

        // issuing operations from ROB to FUs
        MSG("Translating: issuing operations from ROB to FUs")
        var rob_iter = cyclix_gen.begforall_asc(rob)
        run {
            cyclix_gen.begif(cyclix_gen.subStruct(rob_iter.iter_elem, "enb"))
            run {
                cyclix_gen.begif(cyclix_gen.band(cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_rdy"), cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_rdy")))
                run {
                    cyclix_gen.begif(cyclix_gen.subStruct(rob_iter.iter_elem, "fu_req"))
                    run {
                        // writing op to FU
                        var fu_id = 0
                        for (exu_num in 0 until ExUnits_insts.size) {

                            for (exu_inst_num in 0 until ExUnits_insts[exu_num].size) {

                                cyclix_gen.begif(!cyclix_gen.subStruct(cyclix_gen.indexed(rob, rob_iter.iter_num), "fu_pending"))
                                run {

                                    cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "fu_id"), fu_id))
                                    run {

                                        // filling exu_req with rob data
                                        cyclix_gen.assign(
                                            exu_req,
                                            hw_fracs(hw_frac_SubStruct("opcode")),
                                            cyclix_gen.subStruct(rob_iter.iter_elem, "fu_opcode"))
                                        cyclix_gen.assign(
                                            exu_req,
                                            hw_fracs(hw_frac_SubStruct("rs0_rdata")),
                                            cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_rdata"))
                                        cyclix_gen.assign(
                                            exu_req,
                                            hw_fracs(hw_frac_SubStruct("rs1_rdata")),
                                            cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_rdata"))
                                        cyclix_gen.assign(
                                            exu_req,
                                            hw_fracs(hw_frac_SubStruct("rd_tag")),
                                            cyclix_gen.subStruct(rob_iter.iter_elem, "rd_tag"))

                                        cyclix_gen.begif(cyclix_gen.fifo_internal_wr_unblk(ExUnits_insts[exu_num][exu_inst_num], cyclix.STREAM_REQ_BUS_NAME, exu_req))
                                        run {
                                            cyclix_gen.assign(
                                                rob,
                                                hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("fu_pending")),
                                                1)
                                        }; cyclix_gen.endif()

                                    }; cyclix_gen.endif()

                                }; cyclix_gen.endif()

                            }
                            fu_id++
                        }
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()
        }; cyclix_gen.endloop()

        // broadcasting FU results to ROB
        MSG("Translating: broadcasting FU results to ROB")
        var fu_id = 0
        for (exu_num in 0 until ExUnits_insts.size) {
            for (exu_inst_num in 0 until ExUnits_insts[exu_num].size) {
                cyclix_gen.begif(cyclix_gen.fifo_internal_rd_unblk(ExUnits_insts[exu_num][exu_inst_num], cyclix.STREAM_RESP_BUS_NAME, exu_resp))
                run {

                    // updating PRF state
                    cyclix_gen.assign(
                        PRF,
                        hw_fracs(hw_frac_V(cyclix_gen.subStruct(exu_resp, "tag"))),
                        cyclix_gen.subStruct(exu_resp, "wdata"))

                    rob_iter = cyclix_gen.begforall_asc(rob)
                    run {

                        cyclix_gen.begif(cyclix_gen.subStruct(rob_iter.iter_elem, "enb"))
                        run {

                            // reading rs0
                            cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_rdy"))
                            run {
                                cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_tag"), cyclix_gen.subStruct(exu_resp, "tag")))
                                run {
                                    // setting rs0 ROB entry ready
                                    cyclix_gen.assign(
                                        rob,
                                        hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs0_rdata")),
                                        cyclix_gen.subStruct(exu_resp, "wdata"))
                                    cyclix_gen.assign(
                                        rob,
                                        hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs0_rdy")),
                                        1)
                                }; cyclix_gen.endif()
                            }; cyclix_gen.endif()

                            // reading rs1
                            cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_rdy"))
                            run {
                                cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_tag"), cyclix_gen.subStruct(exu_resp, "tag")))
                                run {
                                    // setting rs1 ROB entry ready
                                    cyclix_gen.assign(
                                        rob,
                                        hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs1_rdata")),
                                        cyclix_gen.subStruct(exu_resp, "wdata"))
                                    cyclix_gen.assign(
                                        rob,
                                        hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs1_rdy")),
                                        1)
                                }; cyclix_gen.endif()
                            }; cyclix_gen.endif()

                            // setting rdy if data generated
                            cyclix_gen.begif(cyclix_gen.subStruct(rob_iter.iter_elem, "fu_pending"))
                            run {
                                cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rd_tag"), cyclix_gen.subStruct(exu_resp, "tag")))
                                run {
                                    cyclix_gen.assign(
                                        rob,
                                        hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rdy")),
                                        1)
                                }; cyclix_gen.endif()
                            }; cyclix_gen.endif()

                        }; cyclix_gen.endif()

                    }; cyclix_gen.endloop()

                }; cyclix_gen.endif()
            }
            fu_id++
        }

        // acquiring new operation to rob tail
        cyclix_gen.begif(!rob_full)         // checking if ROB not full
        run {
            cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(cmd_req, cmd_req_data))
            run {

                // decoding input

                var rob_wr_uop = cyclix_gen.local("genrob_wr_uop", rob_struct)

                cyclix_gen.assign(
                    rob_wr_uop,
                    hw_fracs(hw_frac_SubStruct("enb")),
                    1)
                cyclix_gen.assign(
                    rob_wr_uop,
                    hw_fracs(hw_frac_SubStruct("fu_req")),
                    cyclix_gen.subStruct(cmd_req_data, "exec"))
                cyclix_gen.assign(
                    rob_wr_uop,
                    hw_fracs(hw_frac_SubStruct("fu_pending")),
                    0)
                cyclix_gen.assign(
                    rob_wr_uop,
                    hw_fracs(hw_frac_SubStruct("fu_id")),
                    cyclix_gen.subStruct(cmd_req_data, "fu_id"))
                cyclix_gen.assign(
                    rob_wr_uop,
                    hw_fracs(hw_frac_SubStruct("fu_opcode")),
                    cyclix_gen.subStruct(cmd_req_data, "fu_opcode"))

                cyclix_gen.assign(
                    rob_wr_uop,
                    hw_fracs(hw_frac_SubStruct("rs0_tag")),
                    cyclix_gen.subStruct(cmd_req_data, "fu_rs0"))         // TODO: renaming
                cyclix_gen.assign(
                    rob_wr_uop,
                    hw_fracs(hw_frac_SubStruct("rs0_rdata")),
                    cyclix_gen.indexed(PRF, cyclix_gen.subStruct(cmd_req_data, "fu_rs0")))

                cyclix_gen.assign(
                    rob_wr_uop,
                    hw_fracs(hw_frac_SubStruct("rs1_tag")),
                    cyclix_gen.subStruct(cmd_req_data, "fu_rs1"))         // TODO: renaming
                cyclix_gen.assign(
                    rob_wr_uop,
                    hw_fracs(hw_frac_SubStruct("rs1_rdata")),
                    cyclix_gen.indexed(PRF, cyclix_gen.subStruct(cmd_req_data, "fu_rs1")))

                cyclix_gen.begif(cyclix_gen.subStruct(cmd_req_data, "exec"))
                run {

                    cyclix_gen.assign(
                        rob_wr_uop,
                        hw_fracs(hw_frac_SubStruct("rs0_rdy")),
                        !cyclix_gen.indexed(PRF_busy, cyclix_gen.subStruct(cmd_req_data, "fu_rs0")))

                    cyclix_gen.assign(
                        rob_wr_uop,
                        hw_fracs(hw_frac_SubStruct("rs1_rdy")),
                        !cyclix_gen.indexed(PRF_busy, cyclix_gen.subStruct(cmd_req_data, "fu_rs1")))

                    cyclix_gen.assign(
                        rob_wr_uop,
                        hw_fracs(hw_frac_SubStruct("rd_tag")),
                        cyclix_gen.subStruct(cmd_req_data, "fu_rd"))         // TODO: renaming

                    cyclix_gen.assign(
                        rob_wr_uop,
                        hw_fracs(hw_frac_SubStruct("rd_tag_prev")),
                        cyclix_gen.subStruct(cmd_req_data, "fu_rd"))         // TODO: renaming

                    cyclix_gen.assign(
                        rob_wr_uop,
                        hw_fracs(hw_frac_SubStruct("rdy")),
                        0)

                    cyclix_gen.assign(
                        PRF_busy,
                        hw_fracs(hw_frac_V(cyclix_gen.subStruct(cmd_req_data, "fu_rd"))),
                        0
                    )

                    cyclix_gen.assign(
                        rob_wr_uop,
                        hw_fracs(hw_frac_SubStruct("wb_ext")),
                        0)

                    cyclix_gen.assign(
                        rob_wr_uop,
                        hw_fracs(hw_frac_SubStruct("wb_wdata")),
                        0)

                    cyclix_gen.assign(rob_wr, 1)
                }; cyclix_gen.endif()

                cyclix_gen.begelse()
                run {

                    // LOAD
                    cyclix_gen.begif(cyclix_gen.subStruct(cmd_req_data, "rf_we"))
                    run {

                        cyclix_gen.assign(
                            rob_wr_uop,
                            hw_fracs(hw_frac_SubStruct("rdy")),
                            1)

                        cyclix_gen.assign(
                            rob_wr_uop,
                            hw_fracs(hw_frac_SubStruct("rd_tag")),
                            cyclix_gen.subStruct(cmd_req_data, "rf_addr"))         // TODO: renaming

                        cyclix_gen.assign(
                            rob_wr_uop,
                            hw_fracs(hw_frac_SubStruct("rd_tag_prev")),
                            cyclix_gen.subStruct(cmd_req_data, "rf_addr"))         // TODO: renaming

                        // writing to PRF
                        cyclix_gen.assign(
                            PRF,
                            hw_fracs(hw_frac_V(cyclix_gen.subStruct(cmd_req_data, "rf_addr"))),
                            cyclix_gen.subStruct(cmd_req_data, "rf_wdata"))
                        cyclix_gen.assign(
                            PRF_busy,
                            hw_fracs(hw_frac_V(cyclix_gen.subStruct(cmd_req_data, "rf_addr"))),
                            1)

                        cyclix_gen.assign(rob_wr, 1)

                    }; cyclix_gen.endif()

                    // STORE
                    cyclix_gen.begelse()
                    run {
                        cyclix_gen.assign(
                            rob_wr_uop,
                            hw_fracs(hw_frac_SubStruct("rs0_rdy")),
                            !cyclix_gen.indexed(PRF_busy, cyclix_gen.subStruct(cmd_req_data, "fu_rs0")))

                        cyclix_gen.assign(
                            rob_wr_uop,
                            hw_fracs(hw_frac_SubStruct("rs1_rdy")),
                            1)

                        cyclix_gen.assign(
                            rob_wr_uop,
                            hw_fracs(hw_frac_SubStruct("rdy")),
                            cyclix_gen.band(cyclix_gen.subStruct(rob_wr_uop, "rs0_rdy"), cyclix_gen.subStruct(rob_wr_uop, "rs1_rdy")))

                        cyclix_gen.assign(
                            rob_wr_uop,
                            hw_fracs(hw_frac_SubStruct("wb_ext")),
                            1)

                        // TODO: checking PRF readiness
                        cyclix_gen.assign(
                            rob_wr_uop,
                            hw_fracs(hw_frac_SubStruct("wb_wdata")),
                            cyclix_gen.indexed(PRF, cyclix_gen.subStruct(cmd_req_data, "rf_addr")))

                        cyclix_gen.assign(rob_wr, 1)
                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()

                cyclix_gen.begif(rob_wr)
                run {
                    // putting new uop in ROB
                    cyclix_gen.assign(
                        rob,
                        hw_fracs(hw_frac_V(rob_wr_ptr)),
                        rob_wr_uop
                    )

                    // asserting rob_full - checking if data written to last available entry
                    cyclix_gen.begif(cyclix_gen.eq2(rob_wr_ptr, rob.vartype.dimensions.last().msb))
                    run {
                        cyclix_gen.assign(rob_full, 1)
                    }; cyclix_gen.endif()

                    // incrementing rob_wr_ptr
                    cyclix_gen.begif(rob_rd)
                    run {
                        cyclix_gen.assign(rob_wr_ptr, rob_wr_ptr_prev)
                    }; cyclix_gen.endif()
                    cyclix_gen.begelse()
                    run {
                        cyclix_gen.assign(rob_wr_ptr, rob_wr_ptr_inc)
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()
        }; cyclix_gen.endif()

        cyclix_gen.end()
        MSG(DEBUG_FLAG, "Translating to cyclix: complete")
        return cyclix_gen
    }
}
