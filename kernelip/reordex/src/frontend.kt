/*
 * frontend.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

class instr_fetch_buffer(name: String,
                         cyclix_gen : cyclix.Generic,
                         name_prefix : String,
                         TRX_BUF_SIZE : Int,
                         MultiExu_CFG : Reordex_CFG) : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG) {

    val curinstr_addr  = AdduStageVar("curinstr_addr", 31, 0, "0")
    val nextinstr_addr = AdduStageVar("nextinstr_addr", 31, 0, "0")

    val instr_name_prefix = "genmcopipe_instr_mem_"

    var instr_resp_fifo = cyclix_gen.ufifo_in((instr_name_prefix + "resp"), 31, 0)

    fun Process(renamed_uop_buf : rename_buffer) {

        var instr_data_rdata = cyclix_gen.local("instr_data_rdata", instr_resp_fifo.vartype, "0")
        var new_renamed_uop     = renamed_uop_buf.GetPushTrx()

        // instruction fetch/decode

        preinit_ctrls()
        init_locals()

        cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(instr_resp_fifo, instr_data_rdata))
        run {
            cyclix_gen.assign_subStructs(new_renamed_uop, TRX_LOCAL)

            cyclix_gen.assign(renamed_uop_buf.push, 1)
            renamed_uop_buf.push_trx(new_renamed_uop)

            cyclix_gen.assign(pop, 1)
            pop_trx()
        }; cyclix_gen.endif()
    }
}

class instr_req_stage(val name : String, val cyclix_gen : cyclix.Generic, val instr_fetch : instr_fetch_buffer) {

    var pc = cyclix_gen.uglobal("pc", 31, 0, hw_imm(32, IMM_BASE_TYPE.HEX, "200"))

    var busreq_mem_struct = hw_struct(name + "_busreq_mem_struct")
    val instr_name_prefix = "genmcopipe_instr_mem_"

    var wr_struct = hw_struct("genpmodule_" + name + "_" + instr_name_prefix + "genstruct_fifo_wdata")
    var instr_req_fifo = cyclix_gen.fifo_out((instr_name_prefix + "req"), wr_struct)

    fun Process() {

        var new_fetch_buf = instr_fetch.GetPushTrx()
        var instr_data_wdata = cyclix_gen.local("instr_data_wdata", instr_req_fifo.vartype, "0")

        busreq_mem_struct.addu("addr",     31, 0, "0")
        busreq_mem_struct.addu("be",       3,  0, "0")
        busreq_mem_struct.addu("wdata",    31, 0, "0")

        wr_struct.addu("we", 0, 0, "0")
        wr_struct.add("wdata", hw_type(busreq_mem_struct), "0")


        cyclix_gen.assign(new_fetch_buf.GetFracRef("curinstr_addr"), pc)

        cyclix_gen.assign(instr_data_wdata.GetFracRef("we"), 0)
        cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("addr"), pc)
        cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("be"), 15)
        cyclix_gen.assign(instr_data_wdata.GetFracRef("wdata").GetFracRef("wdata"), 0)
        cyclix_gen.fifo_wr_unblk(instr_req_fifo, instr_data_wdata)

        cyclix_gen.add_gen(pc, pc, 4)

        cyclix_gen.assign(new_fetch_buf.GetFracRef("enb"), 1)
        cyclix_gen.assign(new_fetch_buf.GetFracRef("nextinstr_addr"), pc)

        cyclix_gen.assign(instr_fetch.push, 1)
        instr_fetch.push_trx(new_fetch_buf)
    }
}

class coproc_frontend(val name : String, val cyclix_gen : cyclix.Generic, val MultiExu_CFG : Reordex_CFG, val global_structures : __global_structures) {

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
        for (imm_idx in 0 until MultiExu_CFG.imms.size) {
            cmd_req_struct.add("fu_imm_" + MultiExu_CFG.imms[imm_idx].name, MultiExu_CFG.imms[imm_idx].vartype, MultiExu_CFG.imms[imm_idx].defimm)
        }
        for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
            cmd_req_struct.addu("fu_rs" + RF_rs_idx, MultiExu_CFG.ARF_addr_width-1, 0, "0")
        }
        cmd_req_struct.addu("fu_rd",    MultiExu_CFG.ARF_addr_width-1, 0, "0")

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

                var rss_tags = ArrayList<hw_var>()
                for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
                    rss_tags.add(cyclix_gen.indexed(global_structures.ARF_map, cmd_req_data.GetFracRef("fu_rs" + RF_rs_idx)))
                }
                var rd_tag = cyclix_gen.indexed(global_structures.ARF_map, cmd_req_data.GetFracRef("fu_rd"))

                for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {
                    cyclix_gen.assign(new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_tag"), rss_tags[RF_rs_idx])
                    cyclix_gen.assign(
                        new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdata"),
                        cyclix_gen.indexed(global_structures.PRF, new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_tag")))
                }

                var alloc_rd_tag = cyclix_gen.min0(global_structures.PRF_mapped)

                cyclix_gen.begif(cmd_req_data.GetFracRef("exec"))
                run {

                    cyclix_gen.assign(nru_fu_req, 1)

                    for (RF_rs_idx in 0 until MultiExu_CFG.rss.size) {

                        var nru_rs_use = nru_rs_use_mask.GetFracRef(RF_rs_idx)
                        var exu_descr_idx = 0
                        for (exu_descr in global_structures.exu_descrs) {
                            cyclix_gen.begif(cyclix_gen.eq2(nru_fu_id, exu_descr_idx))
                            run {
                                cyclix_gen.assign(nru_rs_use, hw_imm(exu_descr.value.rs_use_flags[RF_rs_idx]))
                            }; cyclix_gen.endif()
                            exu_descr_idx++
                        }

                        // fetching rdy flags from PRF_rdy and masking with rsX_req
                        cyclix_gen.assign(
                            new_renamed_uop.GetFracRef("rs" + RF_rs_idx + "_rdy"),
                            cyclix_gen.bor(global_structures.PRF_rdy.GetFracRef(rss_tags[RF_rs_idx]), !nru_rs_use))
                    }

                    cyclix_gen.assign(nru_rd_tag, alloc_rd_tag.position)            // TODO: check for availability flag
                    cyclix_gen.assign(nru_rd_tag_prev, rd_tag)
                    cyclix_gen.assign(nru_rd_tag_prev_clr, cyclix_gen.indexed(global_structures.PRF_mapped, rd_tag))

                    cyclix_gen.assign(global_structures.ARF_map.GetFracRef(cmd_req_data.GetFracRef("fu_rd")), alloc_rd_tag.position)
                    cyclix_gen.assign(global_structures.PRF_mapped.GetFracRef(alloc_rd_tag.position), 1)
                    cyclix_gen.assign(global_structures.PRF_rdy.GetFracRef(alloc_rd_tag.position), 0)

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
                        cyclix_gen.assign(nru_rd_tag_prev_clr, global_structures.PRF_mapped.GetFracRef(rd_tag))
                        cyclix_gen.assign(global_structures.PRF_mapped.GetFracRef(alloc_rd_tag.position), 1)

                        cyclix_gen.assign(global_structures.ARF_map.GetFracRef(cmd_req_data.GetFracRef("fu_rd")), alloc_rd_tag.position)
                        cyclix_gen.assign(global_structures.PRF_rdy.GetFracRef(alloc_rd_tag.position), 1)
                        cyclix_gen.assign(global_structures.PRF.GetFracRef(alloc_rd_tag.position), cmd_req_data.GetFracRef("rf_wdata"))

                    }; cyclix_gen.endif()

                    // STORE
                    cyclix_gen.begelse()
                    run {
                        cyclix_gen.assign(new_renamed_uop.GetFracRef("rs0_rdy"), global_structures.PRF_rdy.GetFracRef(rss_tags[0]))

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
    }
}

class __global_structures(val PRF : hw_var, val PRF_mapped : hw_var, val PRF_rdy : hw_var, val ARF_map : hw_var, val PRF_src : hw_var, val ExecUnits : MutableMap<String, Exu_CFG>, val exu_descrs : MutableMap<String, __exu_descr>)