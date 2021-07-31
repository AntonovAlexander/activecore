/*
 * risc_frontend.kt
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