/*
 * frontend.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

class instr_req_stage(val name : String, val cyclix_gen : cyclix.Generic, val instr_fetch : instr_fetch_buffer, INSTR_IO_ID_WIDTH : Int) : hw_imm(0) {

    var pc = cyclix_gen.uglobal("pc", 31, 0, hw_imm(32, IMM_BASE_TYPE.HEX, "200"))

    var busreq_mem_struct = hw_struct(name + "_busreq_mem_struct")
    val instr_name_prefix = "genmcopipe_instr_mem_"

    var wr_struct = hw_struct("genpmodule_" + name + "_" + instr_name_prefix + "genstruct_fifo_wdata")
    var instr_req_fifo = cyclix_gen.fifo_out((instr_name_prefix + "req"), wr_struct)

    var instr_io_wr_ptr = cyclix_gen.uglobal("geninstr_io_wr_ptr", INSTR_IO_ID_WIDTH-1, 0, "0")

    fun Process(instr_fetch : instr_fetch_buffer) {

        var new_fetch_buf = instr_fetch.GetPushTrx()
        var instr_data_wdata = cyclix_gen.local("instr_data_wdata", instr_req_fifo.vartype, "0")

        busreq_mem_struct.addu("addr",     31, 0, "0")
        busreq_mem_struct.addu("be",       3,  0, "0")
        busreq_mem_struct.addu("wdata",    31, 0, "0")

        wr_struct.addu("we", 0, 0, "0")
        wr_struct.add("wdata", hw_type(busreq_mem_struct), "0")

        cyclix_gen.assign(new_fetch_buf.GetFracRef("curinstr_addr"), pc)

        cyclix_gen.begif(instr_fetch.ctrl_rdy)
        run {
            cyclix_gen.assign(instr_data_wdata.GetFracRef("we"), 0)
            cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("addr"), pc)
            cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("be"), 15)
            cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("wdata"), 0)

            cyclix_gen.assign(new_fetch_buf.GetFracRef("geninstr_io_id"), instr_io_wr_ptr)
            cyclix_gen.begif(cyclix_gen.fifo_wr_unblk(instr_req_fifo, instr_data_wdata))
            run {
                cyclix_gen.assign(instr_fetch.push, 1)
                cyclix_gen.add_gen(instr_io_wr_ptr, instr_io_wr_ptr, 1)
                cyclix_gen.add_gen(pc, pc, 4)
            }; cyclix_gen.endif()

            cyclix_gen.assign(new_fetch_buf.GetFracRef("enb"), 1)
            cyclix_gen.assign(new_fetch_buf.GetFracRef("nextinstr_addr"), pc)

            cyclix_gen.begif(instr_fetch.push)
            run {
                instr_fetch.push_trx(new_fetch_buf)
            }; cyclix_gen.endif()

        }; cyclix_gen.endif()
    }
}

class instr_fetch_buffer(name: String,
                         cyclix_gen : cyclix.Generic,
                         name_prefix : String,
                         TRX_BUF_SIZE : Int,
                         val MultiExu_inst : MultiExuRISC,
                         val MultiExu_CFG : Reordex_CFG,
                         val global_structures: __control_structures,
                         cdb_num : Int,
                         INSTR_IO_ID_WIDTH : Int) : uop_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, cdb_num) {

    val curinstr_addr  = AdduStageVar("curinstr_addr", 31, 0, "0")
    val nextinstr_addr = AdduStageVar("nextinstr_addr", 31, 0, "0")

    val instr_name_prefix = "genmcopipe_instr_mem_"
    var instr_resp_fifo = cyclix_gen.ufifo_in((instr_name_prefix + "resp"), 31, 0)

    var instr_io_rd_ptr = cyclix_gen.uglobal("geninstr_io_rd_ptr", INSTR_IO_ID_WIDTH-1, 0, "0")

    val instr_io_id     = AdduStageVar("geninstr_io_id", INSTR_IO_ID_WIDTH-1, 0, "0")

    var instr_recv_code_buf = cyclix_gen.local("instr_recv_code_buf", instr_resp_fifo.vartype, "0")
    var instr_recv      = AdduStageVar("instr_recv", 0, 0, "0")
    var instr_recv_code = AddStageVar("instr_recv_code", instr_resp_fifo.vartype, "0")

    var var_dict = mutableMapOf<hw_var, hw_var>()
    init {
        for (src_imm in MultiExu_CFG.src_imms) {
            var_dict.put(src_imm, TRX_LOCAL.GetFracRef(src_imm.name))
        }
        for (genvar in MultiExu_inst.RISCDecode[0].genvars) {
            var_dict.put(genvar, AddLocal(genvar.name, genvar.vartype, genvar.defimm))
        }
    }
    fun TranslateVar(var_totran : hw_var) : hw_var {
        return TranslateVar(var_totran, var_dict)
    }
    fun TranslateParam(param_totran : hw_param) : hw_param {
        return TranslateParam(param_totran, var_dict)
    }

    fun reconstruct_expression(DEBUG_FLAG : Boolean,
                               cyclix_gen : hw_astc,
                               expr : hw_exec,
                               context : import_expr_context) {

        if (expr is hw_exec_src_set_imm) {
            var num = MultiExu_CFG.srcs.indexOf(expr.src)
            src_rsrv[num].src_rdy.assign(1)
            src_rsrv[num].src_data.assign(TranslateParam(expr.imm))

        } else if (expr is hw_exec_src_rd_reg) {
            var num = MultiExu_CFG.srcs.indexOf(expr.src)
            global_structures.FillReadRs(src_rsrv[num].src_tag, src_rsrv[num].src_rdy, src_rsrv[num].src_data, TranslateParam(expr.raddr))

        } else {
            cyclix_gen.import_expr(DEBUG_FLAG, expr, context, ::reconstruct_expression)
        }
    }

    fun Process(renamed_uop_buf : rename_buffer, MRETADDR : hw_var, CSR_MCAUSE : hw_var) {

        var new_renamed_uop = renamed_uop_buf.GetPushTrx()

        cyclix_gen.COMMENT("fetching instruction code...")
        cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(instr_resp_fifo, instr_recv_code_buf))
        run {
            var fetch_iter = cyclix_gen.begforall_asc(TRX_BUF)
            run {
                var instr_io_id_ref = fetch_iter.iter_elem.GetFracRef("geninstr_io_id")
                var instr_recv      = fetch_iter.iter_elem.GetFracRef("instr_recv")
                var instr_recv_code = fetch_iter.iter_elem.GetFracRef("instr_recv_code")

                cyclix_gen.begif(cyclix_gen.eq2(instr_io_id_ref, instr_io_rd_ptr))
                run {
                    cyclix_gen.assign(instr_recv, 1)
                    cyclix_gen.assign(instr_recv_code, instr_recv_code_buf)
                }; cyclix_gen.endif()
            }; cyclix_gen.endloop()
            cyclix_gen.add_gen(instr_io_rd_ptr, instr_io_rd_ptr, 1)
        }; cyclix_gen.endif()
        cyclix_gen.COMMENT("fetching instruction code: done")

        preinit_ctrls()
        init_locals()

        cyclix_gen.begif(ctrl_active)
        run {

            cyclix_gen.begif(instr_recv)
            run {

                cyclix_gen.assign(TranslateVar(MultiExu_inst.RISCDecode.instr_code), instr_recv_code)
                cyclix_gen.assign(TranslateVar(MultiExu_inst.RISCDecode.curinstr_addr), curinstr_addr)
                cyclix_gen.assign(TranslateVar(MultiExu_inst.RISCDecode.MRETADDR), MRETADDR)

                for (src in src_rsrv) {
                    cyclix_gen.assign(src.src_rdy, 1)
                }

                cyclix_gen.MSG_COMMENT("Generating payload")
                for (expr in MultiExu_inst.RISCDecode[0].expressions) {
                    reconstruct_expression(true,
                        cyclix_gen,
                        expr,
                        import_expr_context(var_dict)
                    )
                }

                TranslateVar(MultiExu_inst.RISCDecode.opcode).assign(TranslateVar(MultiExu_inst.RISCDecode.alu_opcode))

                cyclix_gen.begif(renamed_uop_buf.ctrl_rdy)
                run {

                    var nru_rd_tag_prev     = new_renamed_uop.GetFracRef("rd_tag_prev")
                    var nru_rd_tag_prev_clr = new_renamed_uop.GetFracRef("rd_tag_prev_clr")

                    cyclix_gen.begif(TranslateVar(MultiExu_inst.RISCDecode.rd_req))
                    run {
                        cyclix_gen.assign(nru_rd_tag_prev, global_structures.RenameReg(TranslateVar(MultiExu_inst.RISCDecode.rd_addr)))
                        cyclix_gen.assign(nru_rd_tag_prev_clr, cyclix_gen.indexed(global_structures.PRF_mapped, nru_rd_tag_prev))

                        var alloc_rd_tag = global_structures.GetFreePRF()
                        cyclix_gen.assign(TranslateVar(MultiExu_inst.RISCDecode.rd_tag), alloc_rd_tag.position)
                        global_structures.ReserveRd(TranslateVar(MultiExu_inst.RISCDecode.rd_addr), TranslateVar(MultiExu_inst.RISCDecode.rd_tag))      // TODO: "free not found" processing
                    }; cyclix_gen.endif()

                    cyclix_gen.assign_subStructs(new_renamed_uop, TRX_LOCAL)
                    cyclix_gen.assign(new_renamed_uop.GetFracRef("exu_opcode"), TranslateVar(MultiExu_inst.RISCDecode.alu_opcode, var_dict))
                    cyclix_gen.assign(new_renamed_uop.GetFracRef("rdy"), !TranslateVar(MultiExu_inst.RISCDecode.alu_req, var_dict))
                    cyclix_gen.assign(new_renamed_uop.GetFracRef("io_req"), TranslateVar(MultiExu_inst.RISCDecode.mem_req, var_dict))

                    cyclix_gen.assign(renamed_uop_buf.push, 1)
                    renamed_uop_buf.push_trx(new_renamed_uop)

                    cyclix_gen.assign(pop, 1)
                    pop_trx()

                }; cyclix_gen.endif()

            }; cyclix_gen.endif()

        }; cyclix_gen.endif()
    }
}

class coproc_frontend(val name : String, val cyclix_gen : cyclix.Generic, val MultiExu_CFG : Reordex_CFG, val global_structures : __control_structures) {

    var cmd_req_struct = hw_struct(name + "_cmd_req_struct")

    var cmd_req = cyclix_gen.fifo_in("cmd_req",  hw_type(cmd_req_struct))

    var cmd_req_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_req_data"), cmd_req_struct)
    var cmd_resp_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_resp_data"), hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(MultiExu_CFG.RF_width-1, 0)), "0")

    fun Send_toRenameBuf(renamed_uop_buf : rename_buffer) {

        cmd_req_struct.addu("exec",     0, 0, "0")
        cmd_req_struct.addu("rf_we",       0,  0, "0")
        cmd_req_struct.addu("rf_addr",    MultiExu_CFG.ARF_addr_width-1, 0, "0")
        cmd_req_struct.addu("rf_wdata",    MultiExu_CFG.RF_width-1, 0, "0")
        cmd_req_struct.addu("fu_id",    GetWidthToContain(global_structures.ExecUnits.size)-1, 0, "0")
        for (imm_idx in 0 until MultiExu_CFG.src_imms.size) {
            cmd_req_struct.add("fu_imm_" + MultiExu_CFG.src_imms[imm_idx].name, MultiExu_CFG.src_imms[imm_idx].vartype, MultiExu_CFG.src_imms[imm_idx].defimm)
        }
        for (RF_rs_idx in 0 until MultiExu_CFG.srcs.size) {
            cmd_req_struct.addu("fu_rs" + RF_rs_idx, MultiExu_CFG.ARF_addr_width-1, 0, "0")
        }
        cmd_req_struct.addu("fu_rd",    MultiExu_CFG.ARF_addr_width-1, 0, "0")

        var new_renamed_uop     = renamed_uop_buf.GetPushTrx()
        var nru_enb             = new_renamed_uop.GetFracRef("enb")
        var nru_rdy             = new_renamed_uop.GetFracRef("rdy")
        var nru_fu_req          = new_renamed_uop.GetFracRef("fu_req")
        var nru_fu_id           = new_renamed_uop.GetFracRef("fu_id")
        var nru_io_req          = new_renamed_uop.GetFracRef("io_req")
        var nru_rd_tag          = new_renamed_uop.GetFracRef("rd0_tag")
        var nru_rd_tag_prev     = new_renamed_uop.GetFracRef("rd_tag_prev")
        var nru_rd_tag_prev_clr = new_renamed_uop.GetFracRef("rd_tag_prev_clr")
        var nru_rs_use_mask     = cyclix_gen.ulocal("genrs_use_mask", MultiExu_CFG.srcs.size-1, 0, "0")

        cyclix_gen.begif(renamed_uop_buf.ctrl_rdy)
        run {
            cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(cmd_req, cmd_req_data))
            run {

                // decoding input
                cyclix_gen.assign(nru_enb, 1)

                cyclix_gen.assign(nru_fu_id,      cmd_req_data.GetFracRef("fu_id"))

                // getting imms from req
                for (imm_idx in 0 until MultiExu_CFG.src_imms.size) {
                    cyclix_gen.assign(
                        new_renamed_uop.GetFracRef(MultiExu_CFG.src_imms[imm_idx].name),
                        cmd_req_data.GetFracRef("fu_imm_" + MultiExu_CFG.src_imms[imm_idx].name))
                }

                // LOAD/STORE commutation
                cyclix_gen.begif(!cmd_req_data.GetFracRef("exec"))
                run {
                    cyclix_gen.assign(nru_fu_id, global_structures.ExecUnits.size)

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

                for (RF_rs_idx in 0 until MultiExu_CFG.srcs.size) {
                    global_structures.FillReadRs(
                        new_renamed_uop.GetFracRef("src" + RF_rs_idx + "_tag"),
                        new_renamed_uop.GetFracRef("src" + RF_rs_idx + "_rdy"),
                        new_renamed_uop.GetFracRef("src" + RF_rs_idx + "_data"),
                        cmd_req_data.GetFracRef("fu_rs" + RF_rs_idx)
                    )
                }

                var rd_tag = global_structures.RenameReg(cmd_req_data.GetFracRef("fu_rd"))

                var alloc_rd_tag = global_structures.GetFreePRF()

                cyclix_gen.begif(cmd_req_data.GetFracRef("exec"))
                run {

                    cyclix_gen.assign(nru_fu_req, 1)

                    for (RF_rs_idx in 0 until MultiExu_CFG.srcs.size) {

                        // processing RS use mask
                        var nru_rs_use = nru_rs_use_mask.GetFracRef(RF_rs_idx)
                        var exu_descr_idx = 0
                        for (exu_descr in global_structures.exu_descrs) {
                            cyclix_gen.begif(cyclix_gen.eq2(nru_fu_id, exu_descr_idx))
                            run {
                                cyclix_gen.assign(nru_rs_use, hw_imm(exu_descr.value.rs_use_flags[RF_rs_idx]))
                            }; cyclix_gen.endif()
                            exu_descr_idx++
                        }

                        // masking rdys for unused rss
                        cyclix_gen.assign(
                            new_renamed_uop.GetFracRef("src" + RF_rs_idx + "_rdy"),
                            cyclix_gen.bor(new_renamed_uop.GetFracRef("src" + RF_rs_idx + "_rdy"), !nru_rs_use))
                    }

                    cyclix_gen.assign(nru_rd_tag, alloc_rd_tag.position)            // TODO: check for availability flag
                    cyclix_gen.assign(nru_rd_tag_prev, rd_tag)
                    cyclix_gen.assign(nru_rd_tag_prev_clr, cyclix_gen.indexed(global_structures.PRF_mapped, rd_tag))

                    global_structures.ReserveRd(cmd_req_data.GetFracRef("fu_rd"), alloc_rd_tag.position)

                    cyclix_gen.assign(nru_rdy, 0)
                    cyclix_gen.assign(nru_io_req, 0)

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
                        cyclix_gen.assign(nru_rd_tag_prev_clr, global_structures.PRF_mapped.GetFracRef(rd_tag))

                        global_structures.ReserveWriteRd(cmd_req_data.GetFracRef("fu_rd"), alloc_rd_tag.position, cmd_req_data.GetFracRef("rf_wdata"))

                    }; cyclix_gen.endif()

                    // STORE
                    cyclix_gen.begelse()
                    run {

                        // nulling unused rdys
                        for (RF_rs_idx in 1 until MultiExu_CFG.srcs.size) {
                            cyclix_gen.assign(new_renamed_uop.GetFracRef("src" + RF_rs_idx + "_rdy"), 1)
                        }

                        cyclix_gen.assign(nru_rdy, new_renamed_uop.GetFracRef("src0_rdy"))
                        cyclix_gen.assign(nru_io_req, 1)

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
    }
}