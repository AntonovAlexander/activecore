/*
 * io_buffer_risc.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

internal class io_buffer_risc(cyclix_gen : cyclix.Generic,
                              ExUnit_name : String,
                              ExUnit_num : Int,
                              name_prefix : String,
                              TRX_BUF_SIZE : Int,
                              MultiExu_CFG : Reordex_CFG,
                              exu_id_num: hw_imm,
                              iq_exu: Boolean,
                              CDB_index : Int,
                              cdb_num : Int,
                              busreq_mem_struct : hw_struct,
                              commit_cdb : hw_var
) : io_buffer_coproc(cyclix_gen, ExUnit_name, ExUnit_num, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, exu_id_num, iq_exu, CDB_index, cdb_num, busreq_mem_struct, commit_cdb) {

    var mem_req         = AdduStageVar("mem_req", 0, 0, "0")
    var mem_we          = AdduStageVar("mem_we", 0, 0, "0")
    var mem_addr        = AdduStageVar("mem_addr", 31, 0, "0")
    var mem_wdata       = AdduStageVar("mem_wdata", 31, 0, "0")
    var mem_be          = AdduStageVar("mem_be", 3, 0, "0")
    var mem_load_signext = AdduStageVar("mem_load_signext", 0, 0, "0")

    var mem_ctrlflow_enb    = AdduStageVar("mem_ctrlflow_enb", 0, 0, "0")

    var mem_addr_generated      = AdduStageVar("mem_addr_generated", 0, 0, "0")
    var mem_addr_generate       = cyclix_gen.ulocal("mem_addr_generate", 0, 0, "0")
    var mem_addr_generate_trx   = cyclix_gen.ulocal("mem_addr_generate_trx", GetWidthToContain(TRX_BUF_SIZE) -1, 0, "0")

    val data_name_prefix = "genmcopipe_data_mem_"
    var rd_struct = hw_struct("genpmodule_" + cyclix_gen.name + "_" + data_name_prefix + "genstruct_fifo_wdata")

    var data_req_fifo   = cyclix_gen.fifo_out((data_name_prefix + "req"), rd_struct)
    var data_resp_fifo  = cyclix_gen.ufifo_in((data_name_prefix + "resp"), 31, 0)

    var mem_data_wdata      = cyclix_gen.local("mem_data_wdata", data_req_fifo.vartype, "0")
    var mem_data_rdata      = cyclix_gen.local("mem_data_rdata", data_resp_fifo.vartype, "0")

    var lsu_iq_done = cyclix_gen.ulocal("lsu_iq_done", 0, 0, "0")
    var lsu_iq_num  = cyclix_gen.ulocal("lsu_iq_num", GetWidthToContain(TRX_BUF.GetWidth()) -1, 0, "0")
    var io_iq_cmd   = TRX_BUF.GetFracRef(lsu_iq_num)

    var exu_cdb_inst_enb    = commit_cdb.GetFracRef("enb")
    var exu_cdb_inst_data   = commit_cdb.GetFracRef("data")
    var exu_cdb_inst_trx_id = exu_cdb_inst_data.GetFracRef("trx_id")
    var exu_cdb_inst_req    = exu_cdb_inst_data.GetFracRef("rd0_req")       // TODO: fix
    var exu_cdb_inst_tag    = exu_cdb_inst_data.GetFracRef("rd0_tag")       // TODO: fix
    var exu_cdb_inst_wdata  = exu_cdb_inst_data.GetFracRef("rd0_wdata")     // TODO: fix

    var resp_buf = io_buffer_resp_risc(cyclix_gen, "LSU_resp", 4, MultiExu_CFG, data_resp_fifo.vartype.dimensions)

    var search_active = cyclix_gen.ulocal("search_active", 0, 0, "1")

    init {
        rd_struct.addu("we", 0, 0, "0")
        rd_struct.add("wdata", hw_type(busreq_mem_struct), "0")
    }

    override fun ProcessIO(io_cdb_buf : hw_var, io_cdb_rs1_wdata_buf : hw_var, rob_buf : rob) {

        cyclix_gen.MSG_COMMENT("I/O IQ processing...")

        preinit_ctrls()
        init_locals()

        resp_buf.preinit_ctrls()

        var io_cdb_enb      = io_cdb_buf.GetFracRef("enb")
        var io_cdb_data     = io_cdb_buf.GetFracRef("data")
        var io_cdb_trx_id   = io_cdb_data.GetFracRef("trx_id")
        var io_cdb_req      = io_cdb_data.GetFracRef("rd0_req")
        var io_cdb_tag      = io_cdb_data.GetFracRef("rd0_tag")
        var io_cdb_wdata    = io_cdb_data.GetFracRef("rd0_wdata")

        cyclix_gen.assign(io_cdb_enb, 0)
        cyclix_gen.assign(io_cdb_data, 0)

        cyclix_gen.assign(mem_addr, src_rsrv[0].src_data)
        cyclix_gen.assign(mem_wdata, src_rsrv[1].src_data)

        cyclix_gen.MSG_COMMENT("Returning I/O to CDB...")

        var resp_buf_head = resp_buf.TRX_BUF.GetFracRef(0)
        var resp_buf_head_rdy = resp_buf_head.GetFracRef("rdy")
        var resp_buf_head_trx_id = resp_buf_head.GetFracRef("trx_id")
        var resp_buf_head_rd0_req = resp_buf_head.GetFracRef("rd0_req")
        var resp_buf_head_rd0_tag = resp_buf_head.GetFracRef("rd0_tag")
        var resp_buf_head_mem_be = resp_buf_head.GetFracRef("mem_be")
        var resp_buf_head_mem_load_signext = resp_buf_head.GetFracRef("mem_load_signext")
        var resp_buf_head_mem_data_rdata = resp_buf_head.GetFracRef("mem_data_rdata")

        cyclix_gen.begif(resp_buf.TRX_BUF_COUNTER_NEMPTY)
        run {
            cyclix_gen.begif(resp_buf_head_rdy)
            run {
                cyclix_gen.assign(exu_cdb_inst_enb, 1)
                cyclix_gen.assign(exu_cdb_inst_trx_id, resp_buf_head_trx_id)
                cyclix_gen.assign(exu_cdb_inst_req, resp_buf_head_rd0_req)
                cyclix_gen.assign(exu_cdb_inst_tag, resp_buf_head_rd0_tag)
                cyclix_gen.assign(exu_cdb_inst_wdata, resp_buf_head_mem_data_rdata)
                resp_buf.pop_trx()
            }; cyclix_gen.endif()
        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("Returning I/O to CDB: done")

        cyclix_gen.MSG_COMMENT("Fetching I/O response...")
        cyclix_gen.begif(cyclix_gen.fifo_rd_unblk(data_resp_fifo, mem_data_rdata))
        run {

            cyclix_gen.begif(cyclix_gen.eq2(resp_buf_head_mem_be, 0x1))
            run {
                cyclix_gen.begif(resp_buf_head_mem_load_signext)
                run {
                    mem_data_rdata.assign(cyclix_gen.signext(mem_data_rdata[7, 0], 32))
                }; cyclix_gen.endif()
                cyclix_gen.begelse()
                run {
                    mem_data_rdata.assign(cyclix_gen.zeroext(mem_data_rdata[7, 0], 32))
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

            cyclix_gen.begif(cyclix_gen.eq2(resp_buf_head_mem_be, 0x3))
            run {
                cyclix_gen.begif(resp_buf_head_mem_load_signext)
                run {
                    mem_data_rdata.assign(cyclix_gen.signext(mem_data_rdata[15, 0], 32))
                }; cyclix_gen.endif()
                cyclix_gen.begelse()
                run {
                    mem_data_rdata.assign(cyclix_gen.zeroext(mem_data_rdata[15, 0], 32))
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()

            cyclix_gen.assign(resp_buf_head_mem_data_rdata, mem_data_rdata)
            cyclix_gen.assign(resp_buf_head_rdy, 1)

        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("Fetching I/O response: done")

        cyclix_gen.MSG_COMMENT("Initiating I/O request...")
        cyclix_gen.begif(cyclix_gen.band(ctrl_active, resp_buf.ctrl_rdy))
        run {

            cyclix_gen.begif(mem_addr_generated)
            run {

                cyclix_gen.begif(mem_ctrlflow_enb)
                run {

                    // data is ready identification
                    cyclix_gen.assign(rdy, src_rsrv[0].src_rdy)
                    cyclix_gen.begif(mem_we)
                    run {
                        cyclix_gen.band_gen(rdy, rdy, src_rsrv[1].src_rdy)
                    }; cyclix_gen.endif()

                    cyclix_gen.begif(rdy)
                    run {

                        cyclix_gen.assign(mem_data_wdata.GetFracRef("we"), mem_we)
                        cyclix_gen.assign(mem_data_wdata.GetFracRef("wdata").GetFracRef("addr"), mem_addr)
                        cyclix_gen.assign(mem_data_wdata.GetFracRef("wdata").GetFracRef("be"), mem_be)
                        cyclix_gen.assign(mem_data_wdata.GetFracRef("wdata").GetFracRef("wdata"), mem_wdata)

                        cyclix_gen.begif(cyclix_gen.fifo_wr_unblk(data_req_fifo, mem_data_wdata))
                        run {
                            var resp_trx = resp_buf.GetPushTrx()
                            var resp_trx_rdy = resp_trx.GetFracRef("rdy")
                            var resp_trx_trx_id = resp_trx.GetFracRef("trx_id")
                            var resp_trx_rd0_req = resp_trx.GetFracRef("rd0_req")
                            var resp_trx_rd0_tag = resp_trx.GetFracRef("rd0_tag")
                            var resp_trx_mem_be = resp_trx.GetFracRef("mem_be")
                            var resp_trx_mem_load_signext = resp_trx.GetFracRef("mem_load_signext")
                            var resp_trx_mem_data_rdata = resp_trx.GetFracRef("mem_data_rdata")

                            cyclix_gen.assign(resp_trx_rdy, mem_we)
                            cyclix_gen.assign(resp_trx_trx_id, trx_id)
                            cyclix_gen.assign(resp_trx_rd0_req, !mem_we)
                            cyclix_gen.assign(resp_trx_rd0_tag, rd_ctrls[0].tag)
                            cyclix_gen.assign(resp_trx_mem_be, mem_be)
                            cyclix_gen.assign(resp_trx_mem_load_signext, mem_load_signext)
                            cyclix_gen.assign(resp_trx_mem_data_rdata, 0)

                            resp_buf.push_trx(resp_trx)
                            cyclix_gen.assign(pop, 1)
                            pop_trx()

                        }; cyclix_gen.endif()

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()

            }; cyclix_gen.endif()

        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("Initiating I/O request: done")

        cyclix_gen.MSG_COMMENT("Mem addr generating...")
        for (trx_idx in TRX_BUF.vartype.dimensions.last().msb downTo 0) {
            var entry_ptr = TRX_BUF.GetFracRef(trx_idx)
            cyclix_gen.begif(cyclix_gen.band(entry_ptr.GetFracRef("enb"), entry_ptr.GetFracRef("src0_rdy"), !entry_ptr.GetFracRef("mem_addr_generated")))
            run {
                cyclix_gen.assign(mem_addr_generate, 1)
                cyclix_gen.assign(mem_addr_generate_trx, trx_idx)
            }; cyclix_gen.endif()
        }
        cyclix_gen.begif(mem_addr_generate)
        run {
            var entry_ptr = TRX_BUF.GetFracRef(mem_addr_generate_trx)
            cyclix_gen.add_gen(entry_ptr.GetFracRef("src0_data"), entry_ptr.GetFracRef("src0_data"), entry_ptr.GetFracRef("immediate"))
            cyclix_gen.assign(entry_ptr.GetFracRef("mem_addr_generated"), 1)
        }; cyclix_gen.endif()
        cyclix_gen.MSG_COMMENT("Mem addr generating: done")

        cyclix_gen.COMMENT("Memory flow control consistency identification...")
        var ROB_SEARCH_DEPTH = 4
        for (rob_trx_idx in 0 until ROB_SEARCH_DEPTH) {
            for (rob_trx_entry_idx in 0 until rob_buf.TRX_BUF_MULTIDIM) {
                cyclix_gen.begif(search_active)
                run {
                    var lsu_entry = TRX_BUF.GetFracRef(0)
                    var rob_entry = rob_buf.TRX_BUF.GetFracRef(rob_trx_idx).GetFracRef(rob_trx_entry_idx)
                    var break_val = (rob_entry.GetFracRef("cf_can_alter") as hw_param)
                    if (rob_trx_idx == 0) {
                        break_val = cyclix_gen.band(break_val, (rob_buf as rob_risc).entry_mask.GetFracRef(rob_trx_entry_idx))
                    }
                    cyclix_gen.begif(break_val)
                    run {
                        search_active.assign(0)
                    }; cyclix_gen.endif()
                    cyclix_gen.begelse()
                    run {
                        var active_trx_id = rob_entry.GetFracRef("trx_id")
                        cyclix_gen.begif(cyclix_gen.eq2(lsu_entry.GetFracRef("trx_id"), active_trx_id))
                        run {
                            cyclix_gen.assign(lsu_entry.GetFracRef("mem_ctrlflow_enb"), 1)
                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
            }
        }
        cyclix_gen.COMMENT("Memory flow control consistency identification: done")

        cyclix_gen.MSG_COMMENT("I/O IQ processing: done")
    }
}

internal class io_buffer_resp_risc (cyclix_gen : cyclix.Generic,
                                    name_prefix : String,
                                    TRX_BUF_SIZE : Int,
                                    MultiExu_CFG : Reordex_CFG,
                                    data_dim : hw_dim_static
        ): trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, 0, MultiExu_CFG) {

    var rdy                 = AdduStageVar("rdy", 0, 0, "0")
    var trx_id              = AddStageVar(hw_structvar("trx_id",     DATA_TYPE.BV_UNSIGNED, GetWidthToContain(MultiExu_CFG.trx_inflight_num)-1, 0, "0"))
    var rd0_req             = AdduStageVar("rd0_req", 0, 0, "0")
    var rd0_tag             = AdduStageVar("rd0_tag", 31, 0, "0")    // TODO: fix dims
    var mem_be              = AdduStageVar("mem_be", 3, 0, "0")
    var mem_load_signext    = AdduStageVar("mem_load_signext", 0, 0, "0")
    var mem_data_rdata      = AdduStageVar("mem_data_rdata", data_dim, "0")
}