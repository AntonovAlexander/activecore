/*
 * io_buffer_coproc.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

internal open class io_buffer_coproc(cyclix_gen : cyclix.Generic,
                                     ExUnit_name : String,
                                     ExUnit_num : Int,
                                     name_prefix : String,
                                     TRX_BUF_SIZE : Int,
                                     MultiExu_CFG : Reordex_CFG,
                                     exu_id_num: hw_imm,
                                     iq_exu: Boolean,
                                     CDB_index : Int,
                                     cdb_num : Int,
                                     var busreq_mem_struct : hw_struct,
                                     var commit_cdb : hw_var
) : iq_buffer(cyclix_gen, ExUnit_name, ExUnit_num, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, exu_id_num, iq_exu, CDB_index, cdb_num) {

    open fun ProcessIO(io_cdb_buf : hw_var, io_cdb_rs1_wdata_buf : hw_var, rob_buf : rob) {

        var cmd_resp = cyclix_gen.fifo_out("cmd_resp",  hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(MultiExu_CFG.RF_width-1, 0)))

        cyclix_gen.MSG_COMMENT("I/O IQ processing...")

        preinit_ctrls()
        init_locals()

        cyclix_gen.begif(ctrl_active)
        run {
            cyclix_gen.begif(src_rsrv[0].src_rdy)
            run {
                cyclix_gen.assign(pop, cyclix_gen.fifo_wr_unblk(cmd_resp, src_rsrv[0].src_data))
            }; cyclix_gen.endif()
        }; cyclix_gen.endif()
        // popping
        cyclix_gen.begif(pop)
        run {
            pop_trx()
        }; cyclix_gen.endif()

        cyclix_gen.MSG_COMMENT("I/O IQ processing: done")
    }
}