/*
 * frontend_coproc.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

internal class coproc_frontend(val name : String,
                               val cyclix_gen : cyclix.Generic,
                               val MultiExu_CFG : Reordex_CFG,
                               val control_structures : __control_structures) {

    var cmd_req_struct = hw_struct(name + "_cmd_req_struct")

    var cmd_req = cyclix_gen.fifo_in("cmd_req",  hw_type(cmd_req_struct))

    var cmd_req_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_req_data"), cmd_req_struct)
    var cmd_resp_data = cyclix_gen.local(cyclix_gen.GetGenName("cmd_resp_data"), hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(MultiExu_CFG.RF_width-1, 0)), "0")

    fun Send_toRenameBuf(dispatch_uop_buf : dispatch_buffer) {

        cyclix_gen.MSG_COMMENT("Decoding operations...")

        cmd_req_struct.addu("exec",     0, 0, "0")
        cmd_req_struct.addu("rf_we",       0,  0, "0")
        cmd_req_struct.addu("rf_addr",    MultiExu_CFG.ARF_addr_width-1, 0, "0")
        cmd_req_struct.addu("rf_wdata",    MultiExu_CFG.RF_width-1, 0, "0")
        cmd_req_struct.addu("fu_id",    GetWidthToContain(control_structures.ExecUnits.size)-1, 0, "0")
        for (imm_idx in 0 until MultiExu_CFG.src_imms.size) {
            cmd_req_struct.add("fu_imm_" + MultiExu_CFG.src_imms[imm_idx].name, MultiExu_CFG.src_imms[imm_idx].vartype, MultiExu_CFG.src_imms[imm_idx].defimm)
        }
        for (RF_rs_idx in 0 until MultiExu_CFG.srcs.size) {
            cmd_req_struct.addu("fu_rs" + RF_rs_idx, MultiExu_CFG.ARF_addr_width-1, 0, "0")
        }
        cmd_req_struct.addu("fu_rd",    MultiExu_CFG.ARF_addr_width-1, 0, "0")

        var new_renamed_uop_vec = dispatch_uop_buf.GetPushTrx()
        var new_renamed_uop     = new_renamed_uop_vec.GetFracRef(0)
        var nru_enb             = new_renamed_uop.GetFracRef("enb")
        var nru_rdy             = new_renamed_uop.GetFracRef("rdy")
        var nru_fu_req          = new_renamed_uop.GetFracRef("fu_req")
        var nru_fu_id           = new_renamed_uop.GetFracRef("fu_id")
        var nru_io_req          = new_renamed_uop.GetFracRef("io_req")
        var nru_rd_tag          = new_renamed_uop.GetFracRef("rd0_tag")
        var nru_rd_tag_prev     = new_renamed_uop.GetFracRef("rd0_tag_prev")
        var nru_rd_tag_prev_clr = new_renamed_uop.GetFracRef("rd0_tag_prev_clr")
        var nru_rs_use_mask     = cyclix_gen.ulocal("genrs_use_mask", MultiExu_CFG.srcs.size-1, 0, "0")

        cyclix_gen.begif(dispatch_uop_buf.ctrl_rdy)
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
                    cyclix_gen.assign(nru_fu_id, control_structures.ExecUnits.size)

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
                    (control_structures as __control_structures_renaming).FillReadRs(
                        new_renamed_uop.GetFracRef("src" + RF_rs_idx + "_tag"),
                        new_renamed_uop.GetFracRef("src" + RF_rs_idx + "_rdy"),
                        new_renamed_uop.GetFracRef("src" + RF_rs_idx + "_data"),
                        cmd_req_data.GetFracRef("fu_rs" + RF_rs_idx)
                    )
                }

                var rd_tag = (control_structures as __control_structures_renaming).RenameReg(cmd_req_data.GetFracRef("fu_rd"))

                var alloc_rd_tag = (control_structures as __control_structures_renaming).GetFreePRF()

                cyclix_gen.begif(cmd_req_data.GetFracRef("exec"))
                run {

                    cyclix_gen.assign(nru_fu_req, 1)

                    for (RF_rs_idx in 0 until MultiExu_CFG.srcs.size) {

                        // processing RS use mask
                        var nru_rs_use = nru_rs_use_mask.GetFracRef(RF_rs_idx)
                        var exu_descr_idx = 0
                        for (exu_descr in control_structures.exu_descrs) {
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
                    cyclix_gen.assign(nru_rd_tag_prev_clr, cyclix_gen.indexed((control_structures as __control_structures_renaming).PRF_mapped_prev, rd_tag))

                    (control_structures as __control_structures_renaming).ReserveRd(cmd_req_data.GetFracRef("fu_rd"), alloc_rd_tag.position)

                    cyclix_gen.assign(nru_rdy, 0)
                    cyclix_gen.assign(nru_io_req, 0)

                    cyclix_gen.assign(dispatch_uop_buf.push, 1)
                }; cyclix_gen.endif()

                cyclix_gen.begelse()
                run {

                    // LOAD
                    cyclix_gen.begif(cmd_req_data.GetFracRef("rf_we"))
                    run {

                        cyclix_gen.assign(nru_rdy, 1)
                        cyclix_gen.assign(nru_rd_tag, alloc_rd_tag.position)        // TODO: check for availability flag
                        cyclix_gen.assign(nru_rd_tag_prev, rd_tag)
                        cyclix_gen.assign(nru_rd_tag_prev_clr, (control_structures as __control_structures_renaming).PRF_mapped_prev.GetFracRef(rd_tag))

                        (control_structures as __control_structures_renaming).ReserveWriteRd(cmd_req_data.GetFracRef("fu_rd"), alloc_rd_tag.position, cmd_req_data.GetFracRef("rf_wdata"))

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

                        cyclix_gen.assign(dispatch_uop_buf.push, 1)

                    }; cyclix_gen.endif()

                }; cyclix_gen.endif()

                // placing new uop in rename_buf
                cyclix_gen.begif(dispatch_uop_buf.push)
                run {
                    dispatch_uop_buf.push_trx(new_renamed_uop_vec)
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()
        }; cyclix_gen.endif()

        cyclix_gen.MSG_COMMENT("Decoding operations: done")
    }
}