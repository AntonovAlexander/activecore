/*
 * frontend_risc.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

internal class instr_iaddr_stage(val name : String, cyclix_gen : cyclix.Generic, MultiExu_CFG : Reordex_CFG) : trx_buffer(cyclix_gen, "geninstr_iaddr", 1, MultiExu_CFG) {

    var pc = cyclix_gen.uglobal("pc", 31, 0, hw_imm(32, IMM_BASE_TYPE.HEX, "200"))
    val curinstr_addr  = AdduLocal("curinstr_addr", 31, 0, "0")
    val nextinstr_addr = AdduLocal("nextinstr_addr", 31, 0, "0")

    fun Process(instr_req : instr_req_stage) {

        cyclix_gen.MSG_COMMENT("Generating instruction addresses...")

        var new_req_buf_total = instr_req.GetPushTrx()

        cyclix_gen.begif(instr_req.ctrl_rdy)
        run {
            var inc_pc = 4
            cyclix_gen.assign(nextinstr_addr, pc)
            for (entry_num in 0 until MultiExu_CFG.DataPath_width) {
                cyclix_gen.assign(curinstr_addr, nextinstr_addr)
                cyclix_gen.add_gen(nextinstr_addr, pc , inc_pc)

                var new_req_buf = new_req_buf_total.GetFracRef(entry_num)
                cyclix_gen.assign(new_req_buf.GetFracRef("enb"), 1)
                cyclix_gen.assign(new_req_buf.GetFracRef("curinstr_addr"), curinstr_addr)
                cyclix_gen.assign(new_req_buf.GetFracRef("nextinstr_addr"), nextinstr_addr)
                inc_pc += 4
            }

            cyclix_gen.assign(instr_req.push, 1)
            instr_req.push_trx(new_req_buf_total)
            cyclix_gen.assign(pc, nextinstr_addr)
        }; cyclix_gen.endif()

        cyclix_gen.MSG_COMMENT("Generating instruction addresses: done")
    }

    fun ProcessSingle(instr_req : instr_req_stage) {

        var new_req_buf_total = instr_req.GetPushTrx()
        cyclix_gen.assign(new_req_buf_total.GetFracRef(1).GetFracRef("enb"), 0)
        var new_req_buf = new_req_buf_total.GetFracRef(0)

        cyclix_gen.assign(curinstr_addr, pc)
        cyclix_gen.add_gen(nextinstr_addr, pc, 4)
        cyclix_gen.assign(TRX_LOCAL.GetFracRef("enb"), 1)
        cyclix_gen.assign_subStructs(new_req_buf, TRX_LOCAL)

        cyclix_gen.begif(instr_req.ctrl_rdy)
        run {
            cyclix_gen.assign(pc, nextinstr_addr)
            cyclix_gen.assign(instr_req.push, 1)
            instr_req.push_trx(new_req_buf_total)
        }; cyclix_gen.endif()
    }
}

internal class instr_req_stage(val name : String, cyclix_gen : cyclix.Generic, INSTR_IO_ID_WIDTH : Int, MultiExu_CFG : Reordex_CFG, var busreq_mem_struct : hw_struct) : trx_buffer(cyclix_gen, "geninstr_req", 2, MultiExu_CFG.DataPath_width, MultiExu_CFG) {

    val curinstr_addr  = AdduStageVar("curinstr_addr", 31, 0, "0")
    val nextinstr_addr = AdduStageVar("nextinstr_addr", 31, 0, "0")

    val instr_name_prefix = "genmcopipe_instr_mem_"

    var wr_struct = hw_struct("genpmodule_" + name + "_" + instr_name_prefix + "genstruct_fifo_wdata")

    var instr_req_fifos = ArrayList<hw_fifo_out>()

    var instr_io_wr_ptr_dim = hw_dim_static()
    var instr_io_wr_ptr = cyclix_gen.uglobal("geninstr_io_wr_ptr", instr_io_wr_ptr_dim, "0")

    var ireq_active         = cyclix_gen.ulocal("genireq_active", 0, 0, "1")
    var entry_toproc_mask   = cyclix_gen.uglobal("genireq_toproc_mask", TRX_BUF_MULTIDIM-1, 0, hw_imm_ones(TRX_BUF_MULTIDIM))

    init {
        wr_struct.addu("we", 0, 0, "0")
        wr_struct.add("wdata", hw_type(busreq_mem_struct), "0")

        for (i in 0 until MultiExu_CFG.DataPath_width) {
            instr_req_fifos.add(cyclix_gen.fifo_out((instr_name_prefix + "req_" + i), wr_struct))
        }

        instr_io_wr_ptr_dim.add(INSTR_IO_ID_WIDTH-1, 0)
        instr_io_wr_ptr_dim.add(TRX_BUF_MULTIDIM-1, 0)
    }

    fun Process(instr_fetch : instr_fetch_buffer) {

        cyclix_gen.MSG_COMMENT("Requesting instructions...")

        preinit_ctrls()
        init_locals()

        var new_fetch_buf_total = instr_fetch.GetPushTrx()

        var instr_data_wdata = cyclix_gen.local("instr_data_wdata", instr_req_fifos[0].vartype, "0")

        cyclix_gen.begif(cyclix_gen.band(ctrl_active, instr_fetch.ctrl_rdy))
        run {

            for (entry_num in 0 until MultiExu_CFG.DataPath_width) {
                cyclix_gen.begif(TRX_LOCAL_PARALLEL.GetFracRef(entry_num).GetFracRef("enb"))
                run {

                    switch_to_local(entry_num)
                    var new_fetch_buf = new_fetch_buf_total.GetFracRef(entry_num)
                    var instr_io_wr_ptr_ref = instr_io_wr_ptr.GetFracRef(entry_num)

                    cyclix_gen.begif(cyclix_gen.band(ireq_active, entry_toproc_mask.GetFracRef(entry_num), enb))
                    run {
                        cyclix_gen.assign(instr_data_wdata.GetFracRef("we"), 0)
                        cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("addr"), curinstr_addr)
                        cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("be"), 15)
                        cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("wdata"), 0)

                        cyclix_gen.assign(new_fetch_buf.GetFracRef("geninstr_io_id"), instr_io_wr_ptr_ref)

                        cyclix_gen.assign(ireq_active, cyclix_gen.fifo_wr_unblk(instr_req_fifos[entry_num], instr_data_wdata))
                        cyclix_gen.begif(ireq_active)
                        run {
                            cyclix_gen.assign(entry_toproc_mask.GetFracRef(entry_num), 0)
                            cyclix_gen.assign(instr_fetch.push, 1)
                            cyclix_gen.add_gen(instr_io_wr_ptr_ref, instr_io_wr_ptr_ref, 1)
                        }; cyclix_gen.endif()

                        cyclix_gen.assign_subStructs(new_fetch_buf, TRX_LOCAL)

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()

                cyclix_gen.begif(ireq_active)
                run {
                    cyclix_gen.assign(pop, 1)
                }; cyclix_gen.endif()

            }

            cyclix_gen.begif(instr_fetch.push)
            run {
                instr_fetch.push_trx(new_fetch_buf_total)
            }; cyclix_gen.endif()

            cyclix_gen.begif(pop)
            run{
                pop_trx()
                cyclix_gen.assign(entry_toproc_mask, hw_imm_ones(TRX_BUF_MULTIDIM))
            }; cyclix_gen.endif()

        }; cyclix_gen.endif()

        cyclix_gen.MSG_COMMENT("Requesting instructions: done")

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

internal class instr_fetch_buffer(name: String,
                                  cyclix_gen : cyclix.Generic,
                                  TRX_BUF_SIZE : Int,
                                  val MultiExu_inst : MultiExuRISC,
                                  MultiExu_CFG : Reordex_CFG,
                                  val global_structures: __control_structures,
                                  cdb_num : Int,
                                  INSTR_IO_ID_WIDTH : Int) : uop_buffer(cyclix_gen, "geninstr_fetch", TRX_BUF_SIZE, MultiExu_CFG.DataPath_width, MultiExu_CFG, cdb_num) {

    val curinstr_addr  = AdduStageVar("curinstr_addr", 31, 0, "0")
    val nextinstr_addr = AdduStageVar("nextinstr_addr", 31, 0, "0")

    val instr_name_prefix = "genmcopipe_instr_mem_"

    var instr_resp_fifos = ArrayList<hw_fifo_in>()

    var instr_io_rd_ptr_dim = hw_dim_static()
    var instr_io_rd_ptr = cyclix_gen.uglobal("geninstr_io_rd_ptr", instr_io_rd_ptr_dim, "0")

    val instr_io_id     = AdduStageVar("geninstr_io_id", INSTR_IO_ID_WIDTH-1, 0, "0")

    var instr_recv_code_buf = cyclix_gen.ulocal("instr_recv_code_buf", 31, 0, "0")
    var instr_recv      = AdduStageVar("instr_recv", 0, 0, "0")
    var instr_recv_code = AdduStageVar("instr_recv_code", 31, 0, "0")

    var decode_active       = cyclix_gen.ulocal("geninstr_recv_decode_active", 0, 0, "1")
    var entry_toproc_mask   = cyclix_gen.uglobal("geninstr_fetch_entry_toproc_mask", TRX_BUF_MULTIDIM-1, 0, hw_imm_ones(TRX_BUF_MULTIDIM))

    var var_dict = mutableMapOf<hw_var, hw_var>()
    init {
        for (src_imm in MultiExu_CFG.src_imms) {
            var_dict.put(src_imm, TRX_LOCAL.GetFracRef(src_imm.name))
        }
        for (genvar in MultiExu_inst.RISCDecode[0].genvars) {
            var_dict.put(genvar, AddLocal(genvar.name, genvar.vartype, genvar.defimm))
        }
        for (i in 0 until MultiExu_CFG.DataPath_width) {
            instr_resp_fifos.add(cyclix_gen.ufifo_in((instr_name_prefix + "resp_" + i), 31, 0))
        }

        instr_io_rd_ptr_dim.add(INSTR_IO_ID_WIDTH-1, 0)
        instr_io_rd_ptr_dim.add(TRX_BUF_MULTIDIM-1, 0)
    }
    fun TranslateVar(var_totran : hw_var) : hw_var {
        return TranslateVar(var_totran, var_dict)
    }
    fun TranslateParam(param_totran : hw_param) : hw_param {
        return TranslateParam(param_totran, var_dict)
    }

    fun reconstruct_expression(debug_lvl : DEBUG_LEVEL,
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
            cyclix_gen.import_expr(debug_lvl, expr, context, ::reconstruct_expression)
        }
    }

    fun Process(dispatch_uop_buf : dispatch_buffer, MRETADDR : hw_var, CSR_MCAUSE : hw_var) {

        cyclix_gen.MSG_COMMENT("Decoding operations...")

        preinit_ctrls()
        init_locals()

        var new_renamed_uop_total = dispatch_uop_buf.GetPushTrx()

        if (global_structures is __control_structures_renaming) global_structures.InitFreePRFBuf()
        else if (global_structures is __control_structures_scoreboarding) global_structures.InitFreeARFRdy()

        cyclix_gen.assign(decode_active, cyclix_gen.band(ctrl_active, dispatch_uop_buf.ctrl_rdy))
        cyclix_gen.begif(decode_active)
        run {

            for (entry_num in 0 until MultiExu_CFG.DataPath_width) {
                cyclix_gen.begif(TRX_LOCAL_PARALLEL.GetFracRef(entry_num).GetFracRef("enb"))
                run {

                    switch_to_local(entry_num)
                    var new_renamed_uop = new_renamed_uop_total.GetFracRef(entry_num)

                    cyclix_gen.begif(cyclix_gen.band(decode_active, entry_toproc_mask.GetFracRef(entry_num), enb))
                    run {

                        cyclix_gen.assign(decode_active, instr_recv)
                        cyclix_gen.begif(decode_active)
                        run {

                            cyclix_gen.assign(TranslateVar(MultiExu_inst.RISCDecode.instr_code), instr_recv_code)
                            cyclix_gen.assign(TranslateVar(MultiExu_inst.RISCDecode.curinstr_addr), curinstr_addr)
                            cyclix_gen.assign(TranslateVar(MultiExu_inst.RISCDecode.MRETADDR), MRETADDR)

                            for (src in src_rsrv) {
                                cyclix_gen.assign(src.src_rdy, 1)
                            }

                            cyclix_gen.MSG_COMMENT("Generating payload...")
                            for (expr in MultiExu_inst.RISCDecode[0].expressions) {
                                reconstruct_expression(DEBUG_LEVEL.FULL,
                                    cyclix_gen,
                                    expr,
                                    import_expr_context(var_dict)
                                )
                            }
                            cyclix_gen.MSG_COMMENT("Generating payload: done")

                            cyclix_gen.MSG_COMMENT("Identifying rd resources availability...")
                            if (global_structures is __control_structures_scoreboarding) {
                                for (rd_idx in 0 until MultiExu_CFG.rds.size) {
                                    cyclix_gen.begif(!global_structures.ARF_rdy_prev.GetFracRef(TranslateVar(MultiExu_inst.RISCDecode.rdctrls[MultiExu_CFG.rds[rd_idx]]!!.addr)))
                                    run {
                                        cyclix_gen.assign(decode_active, 0)
                                    }; cyclix_gen.endif()
                                }
                            } else if (global_structures is __control_structures_renaming) {
                                for (rd_idx in 0 until MultiExu_CFG.rds.size) {         // TODO: sum rd reqs
                                    cyclix_gen.begif(cyclix_gen.band(TranslateVar(MultiExu_inst.RISCDecode.rdctrls[MultiExu_CFG.rds[rd_idx]]!!.req), cyclix_gen.rand((global_structures as __control_structures_renaming).PRF_mapped_prev)))
                                    run {
                                        cyclix_gen.assign(decode_active, 0)
                                    }; cyclix_gen.endif()
                                }
                            } else ERROR("Configuration inconsistent!")
                            cyclix_gen.MSG_COMMENT("Identifying rd resources availability: done")

                            cyclix_gen.begif(cyclix_gen.band(dispatch_uop_buf.ctrl_rdy, decode_active))
                            run {

                                cyclix_gen.MSG_COMMENT("Rds reservation...")

                                for (rd_idx in 0 until MultiExu_CFG.rds.size) {

                                    if (global_structures is __control_structures_scoreboarding) {
                                        cyclix_gen.begif(TranslateVar(MultiExu_inst.RISCDecode.rdctrls[MultiExu_CFG.rds[rd_idx]]!!.req))
                                        run {
                                            cyclix_gen.assign(TranslateVar(MultiExu_inst.RISCDecode.rds_ctrl[rd_idx].tag), TranslateVar(MultiExu_inst.RISCDecode.rdctrls[MultiExu_CFG.rds[rd_idx]]!!.addr))
                                            (global_structures as __control_structures_scoreboarding).ReserveRd(TranslateVar(MultiExu_inst.RISCDecode.rdctrls[MultiExu_CFG.rds[rd_idx]]!!.addr))
                                        }; cyclix_gen.endif()

                                    } else if (global_structures is __control_structures_renaming) {
                                        var nru_rd_tag_prev     = new_renamed_uop.GetFracRef("rd" + rd_idx + "_tag_prev")
                                        var nru_rd_tag_prev_clr = new_renamed_uop.GetFracRef("rd" + rd_idx + "_tag_prev_clr")

                                        cyclix_gen.begif(TranslateVar(MultiExu_inst.RISCDecode.rdctrls[MultiExu_CFG.rds[rd_idx]]!!.req))
                                        run {
                                            cyclix_gen.assign(nru_rd_tag_prev, (global_structures as __control_structures_renaming).RenameReg(TranslateVar(MultiExu_inst.RISCDecode.rdctrls[MultiExu_CFG.rds[rd_idx]]!!.addr)))
                                            cyclix_gen.assign(nru_rd_tag_prev_clr, cyclix_gen.indexed(global_structures.PRF_mapped_prev, nru_rd_tag_prev))

                                            var alloc_rd_tag = global_structures.GetFreePRF()
                                            cyclix_gen.assign(TranslateVar(MultiExu_inst.RISCDecode.rds_ctrl[rd_idx].tag), alloc_rd_tag.position)
                                            global_structures.ReserveRd(TranslateVar(MultiExu_inst.RISCDecode.rdctrls[MultiExu_CFG.rds[rd_idx]]!!.addr), TranslateVar(MultiExu_inst.RISCDecode.rds_ctrl[rd_idx].tag))
                                        }; cyclix_gen.endif()

                                    } else ERROR("Configuration inconsistent!")
                                }

                                cyclix_gen.MSG_COMMENT("Rds reservation: done")

                                // forming push trx
                                cyclix_gen.assign_subStructs(new_renamed_uop, TRX_LOCAL)
                                cyclix_gen.assign(new_renamed_uop.GetFracRef("rdy"), !TranslateVar(MultiExu_inst.RISCDecode.exu_req, var_dict))
                                cyclix_gen.assign(new_renamed_uop.GetFracRef("io_req"), TranslateVar(MultiExu_inst.RISCDecode.memctrl.req, var_dict))
                                cyclix_gen.assign(entry_toproc_mask.GetFracRef(entry_num), 0)
                                cyclix_gen.assign(dispatch_uop_buf.push, 1)

                            }; cyclix_gen.endif()

                        }; cyclix_gen.endif()

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()
            }

            cyclix_gen.begif(decode_active)
            run {
                cyclix_gen.assign(pop, 1)
            }; cyclix_gen.endif()

        }; cyclix_gen.endif()

        cyclix_gen.begif(dispatch_uop_buf.push)
        run {
            dispatch_uop_buf.push_trx(new_renamed_uop_total)
        }; cyclix_gen.endif()

        cyclix_gen.begif(pop)
        run{
            pop_trx()
            cyclix_gen.assign(entry_toproc_mask, hw_imm_ones(TRX_BUF_MULTIDIM))
        }; cyclix_gen.endif()

        cyclix_gen.COMMENT("fetching instruction code...")
        for (entry_num in 0 until MultiExu_CFG.DataPath_width) {
            cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(instr_resp_fifos[entry_num], instr_recv_code_buf))
            run {
                for (trx_idx in 0 until TRX_BUF_SIZE) {
                    var fetch_iter      = TRX_BUF.GetFracRef(trx_idx).GetFracRef(entry_num)
                    var instr_io_id_ref = fetch_iter.GetFracRef("geninstr_io_id")
                    var instr_recv      = fetch_iter.GetFracRef("instr_recv")
                    var instr_recv_code = fetch_iter.GetFracRef("instr_recv_code")

                    cyclix_gen.begif(cyclix_gen.eq2(instr_io_id_ref, instr_io_rd_ptr.GetFracRef(entry_num)))
                    run {
                        cyclix_gen.assign(instr_recv, 1)
                        cyclix_gen.assign(instr_recv_code, instr_recv_code_buf)
                    }; cyclix_gen.endif()
                }
                cyclix_gen.add_gen(instr_io_rd_ptr.GetFracRef(entry_num), instr_io_rd_ptr.GetFracRef(entry_num), 1)
            }; cyclix_gen.endif()
            cyclix_gen.COMMENT("fetching instruction code: done")
        }

        cyclix_gen.MSG_COMMENT("Decoding operations: done")
    }
}