/*
 * MultiExu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

data class MultiExu_CFG_RF(val input_RF_width : Int,
                           val input_RF_depth : Int,
                           val rename_RF: Boolean,
                           val rename_RF_depth : Int)

data class Exu_CFG(val ExecUnit : Exu,
                   val exu_num : Int)

open class MultiExu(val name : String, val MultiExu_cfg_rf : MultiExu_CFG_RF, val rob_size : Int) {

    val input_rf_addr_width = GetWidthToContain(MultiExu_cfg_rf.input_RF_depth)
    val rename_rf_addr_width = GetWidthToContain(MultiExu_cfg_rf.rename_RF_depth)

    var ExecUnits  = mutableMapOf<String, Exu_CFG>()

    fun add_exu(exu : Exu, exu_num: Int) {
        if (ExecUnits.put(exu.name, Exu_CFG(exu, exu_num)) != null) {
            ERROR("Stage addition problem!")
        }
    }

    fun translate_to_cyclix(DEBUG_FLAG : Boolean) : cyclix.Generic {

        MSG("Translating to cyclix: beginning")

        var cyclix_gen = cyclix.Generic(name)

        //// Generating interfaces ////
        // cmd (sequential instruction stream) //
        var cmd_req_struct = cyclix_gen.add_struct(name + "_cmd_req_struct")
        cmd_req_struct.addu("exec",     0, 0, "0")
        cmd_req_struct.addu("rf_we",       0,  0, "0")
        cmd_req_struct.addu("rf_addr",    input_rf_addr_width-1, 0, "0")
        cmd_req_struct.addu("rf_wdata",    MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        cmd_req_struct.addu("fu_id",    GetWidthToContain(ExecUnits.size)-1, 0, "0")
        cmd_req_struct.addu("fu_rs0",    input_rf_addr_width-1, 0, "0")
        cmd_req_struct.addu("fu_rs1",    input_rf_addr_width-1, 0, "0")
        cmd_req_struct.addu("fu_rd",    input_rf_addr_width-1, 0, "0")
        var cmd_req = cyclix_gen.fifo_in("cmd_req",  hw_type(cmd_req_struct))
        var cmd_resp = cyclix_gen.fifo_out("cmd_resp",  hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(MultiExu_cfg_rf.input_RF_width-1, 0)))

        // TODO: memory interface?

        var MAX_INSTR_NUM = MultiExu_cfg_rf.input_RF_depth + rob_size
        for (ExecUnit in ExecUnits) {
            MAX_INSTR_NUM += ExecUnit.value.exu_num * ExecUnit.value.ExecUnit.stage_num
        }

        val TAG_WIDTH = GetWidthToContain(MAX_INSTR_NUM)

        var uop_struct = cyclix_gen.add_struct("uop_struct")
        uop_struct.addu("enb",     0, 0, "0")
        uop_struct.addu("opcode",     0, 0, "0")
        uop_struct.addu("rs0_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        uop_struct.addu("rs1_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        uop_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")
        uop_struct.addu("rd_wdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")

        var rob_struct = cyclix_gen.add_struct("rob_struct")
        rob_struct.addu("enb",     0, 0, "0")
        rob_struct.addu("sent",     0, 0, "0")
        rob_struct.addu("rdy",     0, 0, "0")
        rob_struct.addu("fu_id",     GetWidthToContain(ExecUnits.size)-1, 0, "0")
        rob_struct.addu("opcode",     0, 0, "0")
        rob_struct.addu("rs0_rdy",     0, 0, "0")
        rob_struct.addu("rs0_tag",     TAG_WIDTH-1, 0, "0")
        rob_struct.addu("rs0_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        rob_struct.addu("rs1_rdy",     0, 0, "0")
        rob_struct.addu("rs1_tag",     TAG_WIDTH-1, 0, "0")
        rob_struct.addu("rs1_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        rob_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")
        rob_struct.addu("rd_wdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")

        var req_struct = cyclix_gen.add_struct("req_struct")
        req_struct.addu("enb",     0, 0, "0")
        req_struct.addu("opcode",     0, 0, "0")
        req_struct.addu("rdy",     0, 0, "0")
        req_struct.addu("rs0_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        req_struct.addu("rs1_rdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")
        req_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")

        var resp_struct = cyclix_gen.add_struct("resp_struct")
        resp_struct.addu("enb",     0, 0, "0")
        resp_struct.addu("tag",     TAG_WIDTH-1, 0, "0")
        resp_struct.addu("wdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")

        var commit_struct = cyclix_gen.add_struct("commit_struct")
        commit_struct.addu("enb",     0, 0, "0")
        commit_struct.addu("rdy",     0, 0, "0")
        commit_struct.addu("rd_enb",     0, 0, "0")
        commit_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")
        commit_struct.addu("rd_wdata",     MultiExu_cfg_rf.input_RF_width-1, 0, "0")

        var TranslateInfo = __TranslateInfo()

        var rob = cyclix_gen.global("genrob_" + name, rob_struct, rob_size-1, 0)
        for (ExUnit in ExecUnits) {

            var exu_cyclix_gen = cyclix.Streaming("genexu_" + ExUnit.value.ExecUnit.name, req_struct, resp_struct)
            exu_cyclix_gen.add(hw_imm("0"), hw_imm("1"))
            exu_cyclix_gen.end()

            cyclix_gen.subproc(exu_cyclix_gen)

            var exu_info = __exu_info(
                exu_cyclix_gen,
                cyclix_gen.global("genexu_" + ExUnit.value.ExecUnit.name + "_req", req_struct, ExUnit.value.exu_num-1, 0),
                cyclix_gen.global("genexu_" + ExUnit.value.ExecUnit.name + "_resp", resp_struct, ExUnit.value.exu_num-1, 0)
            )

            TranslateInfo.exu_assocs.put(ExUnit.value.ExecUnit, exu_info)
        }
        var commit_bus = cyclix_gen.global("genexu_" + name + "_commit", commit_struct)

        // committing ROB head
        var rob_head = cyclix_gen.indexed(rob, 0)
        cyclix_gen.begif(cyclix_gen.subStruct(cyclix_gen.indexed(rob, 0), "enb"))
        run {
            cyclix_gen.assign(
                commit_bus,
                hw_fracs(hw_frac_SubStruct("enb")),
                1)
            cyclix_gen.assign(
                commit_bus,
                hw_fracs(hw_frac_SubStruct("rd_enb")),
                1)
            cyclix_gen.assign(
                commit_bus,
                hw_fracs(hw_frac_SubStruct("rd_tag")),
                cyclix_gen.subStruct(rob_head, "rd_tag"))
            cyclix_gen.assign(
                commit_bus,
                hw_fracs(hw_frac_SubStruct("rd_wdata")),
                cyclix_gen.subStruct(rob_head, "rd_wdata"))

            cyclix_gen.begif(cyclix_gen.subStruct(commit_bus, "rdy"))
            run {

                // shifting ops
                var rob_shift_iter = cyclix_gen.begforrange(rob, hw_imm(0), hw_imm(rob.vartype.dimensions.last().msb-1))
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
            }; cyclix_gen.endif()
        }; cyclix_gen.endif()

        // issuing operations from ROB to FUs
        MSG("Translating: issuing operations from ROB to FUs")
        var rob_iter = cyclix_gen.begforall(rob)
        run {
            cyclix_gen.begif(cyclix_gen.subStruct(rob_iter.iter_elem, "enb"))
            run {
                cyclix_gen.begif(cyclix_gen.band(cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_rdy"), cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_rdy")))
                run {
                    // asserting op to FU req bus
                    cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "sent"))
                    run {
                        var fu_id = 0
                        for (exu_assoc in TranslateInfo.exu_assocs) {
                            cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "fu_id"), fu_id))
                            run {
                                var req_bus_iter = cyclix_gen.begforall(exu_assoc.value.req_bus)
                                run {
                                    cyclix_gen.assign(
                                        exu_assoc.value.req_bus,
                                        hw_fracs(hw_frac_V(req_bus_iter.iter_num), hw_frac_SubStruct("enb")),
                                        1)
                                    cyclix_gen.assign(
                                        exu_assoc.value.req_bus,
                                        hw_fracs(hw_frac_V(req_bus_iter.iter_num), hw_frac_SubStruct("opcode")),
                                        cyclix_gen.subStruct(rob_iter.iter_elem, "opcode"))
                                    cyclix_gen.assign(
                                        exu_assoc.value.req_bus,
                                        hw_fracs(hw_frac_V(req_bus_iter.iter_num), hw_frac_SubStruct("rs0_rdata")),
                                        cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_rdata"))
                                    cyclix_gen.assign(
                                        exu_assoc.value.req_bus,
                                        hw_fracs(hw_frac_V(req_bus_iter.iter_num), hw_frac_SubStruct("rs1_rdata")),
                                        cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_rdata"))
                                    cyclix_gen.assign(
                                        exu_assoc.value.req_bus,
                                        hw_fracs(hw_frac_V(req_bus_iter.iter_num), hw_frac_SubStruct("rd_tag")),
                                        cyclix_gen.subStruct(rob_iter.iter_elem, "rd_tag"))

                                    cyclix_gen.begif(cyclix_gen.subStruct(req_bus_iter.iter_elem, "rdy"))
                                    run {
                                        cyclix_gen.assign(
                                            rob,
                                            hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("sent")),
                                            1)
                                    }; cyclix_gen.endif()
                                }; cyclix_gen.endloop()
                            }; cyclix_gen.endif()
                            fu_id++
                        }
                    }; cyclix_gen.endif()
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()
        }; cyclix_gen.endloop()

        // broadcasting FU results to ROB
        MSG("Translating: broadcasting FU results to ROB")
        for (exu_assoc in TranslateInfo.exu_assocs) {
            var resp_bus_iter = cyclix_gen.begforall(exu_assoc.value.resp_bus)
            run {
                cyclix_gen.begif(cyclix_gen.subStruct(resp_bus_iter.iter_elem, "enb"))
                run {
                    var rob_iter = cyclix_gen.begforall(rob)
                    run {

                        // reading rs0
                        cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_rdy"))
                        run {
                            cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_tag"), cyclix_gen.subStruct(resp_bus_iter.iter_elem, "tag")))
                            run {
                                // setting rs0 ROB entry ready
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs0_rdata")),
                                    cyclix_gen.subStruct(resp_bus_iter.iter_elem, "wdata"))
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs0_rdy")),
                                    1)
                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()

                        // reading rs1
                        cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_rdy"))
                        run {
                            cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_tag"), cyclix_gen.subStruct(resp_bus_iter.iter_elem, "tag")))
                            run {
                                // setting rs1 ROB entry ready
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs1_rdata")),
                                    cyclix_gen.subStruct(resp_bus_iter.iter_elem, "wdata"))
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rs1_rdy")),
                                    1)
                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()

                        // reading rd
                        cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "rdy"))
                        run {
                            cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rd_tag"), cyclix_gen.subStruct(resp_bus_iter.iter_elem, "tag")))
                            run {
                                // setting ROB entry ready for commit
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rd_wdata")),
                                    cyclix_gen.subStruct(resp_bus_iter.iter_elem, "wdata"))
                                cyclix_gen.assign(
                                    rob,
                                    hw_fracs(hw_frac_V(rob_iter.iter_num), hw_frac_SubStruct("rdy")),
                                    1)
                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()

                    }; cyclix_gen.endloop()
                }; cyclix_gen.endif()
            }; cyclix_gen.endloop()
        }

        cyclix_gen.end()
        MSG(DEBUG_FLAG, "Translating to cyclix: complete")
        return cyclix_gen
    }
}
