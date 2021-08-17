/*
 * rename_buffer.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

open class rename_buffer(cyclix_gen : cyclix.Generic,
                         name_prefix : String,
                         TRX_BUF_SIZE : Int,
                         val MultiExu_CFG : Reordex_CFG,
                         ExecUnits_size : Int,
                         cdb_num : Int) : uop_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, cdb_num) {

    var fu_id           = AddStageVar(hw_structvar("fu_id",             DATA_TYPE.BV_UNSIGNED, GetWidthToContain(ExecUnits_size), 0, "0"))
    val rd_tag          = AddStageVar(hw_structvar("rd0_tag",           DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    val rd_tag_prev     = AddStageVar(hw_structvar("rd_tag_prev",       DATA_TYPE.BV_UNSIGNED, MultiExu_CFG.PRF_addr_width-1, 0, "0"))
    val rd_tag_prev_clr = AddStageVar(hw_structvar("rd_tag_prev_clr",   DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    val wb_ext          = AddStageVar(hw_structvar("wb_ext",            DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    fun Process(rob : rob, PRF_src : hw_var, store_iq : iq_buffer, ExecUnits : MutableMap<String, Exu_CFG>, exu_descrs : MutableMap<String, __exu_descr>) {

        cyclix_gen.MSG_COMMENT("sending new operations to IQs...")

        var rob_push_trx = rob.GetPushTrx()
        cyclix_gen.assign_subStructs(rob_push_trx, TRX_BUF_head_ref)
        cyclix_gen.assign(rob_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)

        cyclix_gen.begif(rob.ctrl_rdy)
        run {

            cyclix_gen.begif(ctrl_active)
            run {

                var num_rs = 0
                for (rs_rsrv in rs_rsrv) {
                    cyclix_gen.assign(TRX_BUF_head_ref.GetFracRef("rs" + num_rs + "_src"), PRF_src.GetFracRef(rs_rsrv.rs_tag))      // TODO: stage context cleanup
                    num_rs++
                }

                cyclix_gen.clrif()

                if (MultiExu_CFG.mode == REORDEX_MODE.COPROCESSOR) {
                    cyclix_gen.begif(wb_ext)
                    run {
                        cyclix_gen.begif(store_iq.ctrl_rdy)
                        run {

                            // signaling iq_wr
                            cyclix_gen.assign(store_iq.push, 1)

                            var store_push_trx = store_iq.GetPushTrx()
                            cyclix_gen.assign_subStructs(store_push_trx, TRX_BUF_head_ref)
                            cyclix_gen.assign(store_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                            store_iq.push_trx(store_push_trx)

                            cyclix_gen.assign(rob_push_trx.GetFracRef("cdb_id"), store_iq.CDB_index)

                            // clearing renamed uop buffer
                            cyclix_gen.assign(pop, 1)

                        }; cyclix_gen.endif()
                    }; cyclix_gen.endif()
                }

                cyclix_gen.begelse()
                run {
                    var ExUnit_num = 0
                    for (ExUnit in ExecUnits) {
                        var IQ_inst = exu_descrs[ExUnit.key]!!.IQ_insts[0]      // TODO: multiple queues

                        cyclix_gen.begif(cyclix_gen.eq2(fu_id, IQ_inst.fu_id_num))
                        run {
                            cyclix_gen.begif(IQ_inst.ctrl_rdy)
                            run {

                                // signaling iq_wr
                                cyclix_gen.assign(IQ_inst.push, 1)

                                var iq_push_trx = IQ_inst.GetPushTrx()
                                cyclix_gen.assign_subStructs(iq_push_trx, TRX_BUF_head_ref)
                                cyclix_gen.assign(iq_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                                IQ_inst.push_trx(iq_push_trx)

                                cyclix_gen.assign(PRF_src.GetFracRef(rd_tag), IQ_inst.CDB_index)

                                cyclix_gen.assign(rob_push_trx.GetFracRef("cdb_id"), IQ_inst.CDB_index)

                                // clearing renamed uop buffer
                                cyclix_gen.assign(pop, 1)

                                cyclix_gen.assign(rob.push, 1)

                            }; cyclix_gen.endif()
                        }; cyclix_gen.endif()

                        ExUnit_num++
                    }
                }; cyclix_gen.endif()

            }; cyclix_gen.endif()

        }; cyclix_gen.endif()

        cyclix_gen.begif(pop)
        run {
            cyclix_gen.begif(rob.push)
            run {
                cyclix_gen.assign(rob_push_trx.GetFracRef("trx_id"), rob.TRX_ID_COUNTER)
                cyclix_gen.assign(rob_push_trx.GetFracRef("rdy"), 0)
                cyclix_gen.assign(rob.TRX_ID_COUNTER, cyclix_gen.add(rob.TRX_ID_COUNTER, 1))
                rob.push_trx(rob_push_trx)
            }; cyclix_gen.endif()
            pop_trx()
        }; cyclix_gen.endif()
        finalize_ctrls()               //  TODO: cleanup

        cyclix_gen.MSG_COMMENT("sending new operation to IQs: done")
    }
}

class rename_buffer_risc(cyclix_gen : cyclix.Generic,
                         name_prefix : String,
                         TRX_BUF_SIZE : Int,
                         MultiExu_CFG : Reordex_CFG,
                         ExecUnits_size : Int,
                         cdb_num : Int) : rename_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG, ExecUnits_size, cdb_num) {

    var curinstr_addr   = AdduStageVar("curinstr_addr", 31, 0, "0")
    var nextinstr_addr  = AdduStageVar("nextinstr_addr", 31, 0, "0")

    // opcode signals
    //var opcode          = AdduStageVar("opcode", 6, 0, "0")

    // control transfer signals
    var jump_req        = AdduStageVar("jump_req", 0, 0, "0")
    var jump_req_cond   = AdduStageVar("jump_req_cond", 0, 0, "0")
    var jump_src        = AdduStageVar("jump_src", 0, 0, "0")
    var jump_vector     = AdduStageVar("jump_vector", 31, 0, "0")

    // regfile control signals
    var rs0_req         = AdduStageVar("rs0_req", 0, 0, "0")
    var rs0_addr        = AdduStageVar("rs0_addr", 4, 0, "0")
    //var rs0_rdata       = AdduStageVar("rs0_rdata", 31, 0, "0")

    var rs1_req         = AdduStageVar("rs1_req", 0, 0, "0")
    var rs1_addr        = AdduStageVar("rs1_addr", 4, 0, "0")
    //var rs1_rdata       = AdduStageVar("rs1_rdata", 31, 0, "0")

    var rs2_req         = AdduStageVar("rs2_req", 0, 0, "0")
    var rs2_addr        = AdduStageVar("rs2_addr", 4, 0, "0")
    //var rs2_rdata       = AdduStageVar("rs2_rdata", 31, 0, "0")

    var csr_rdata       = AdduStageVar("csr_rdata", 31, 0, "0")

    var rd_req          = AdduStageVar("rd_req", 0, 0, "0")
    var rd_source       = AdduStageVar("rd_source", 2, 0, "0")
    var rd_addr         = AdduStageVar("rd_addr", 4, 0, "0")
    var rd_wdata        = AdduStageVar("rd_wdata", 31, 0, "0")
    var rd_rdy          = AdduStageVar("rd_rdy", 0, 0, "0")

    var immediate_I     = AdduStageVar("immediate_I", 31, 0, "0")
    var immediate_S     = AdduStageVar("immediate_S", 31, 0, "0")
    var immediate_B     = AdduStageVar("immediate_B", 31, 0, "0")
    var immediate_U     = AdduStageVar("immediate_U", 31, 0, "0")
    var immediate_J     = AdduStageVar("immediate_J", 31, 0, "0")

    var immediate       = AdduStageVar("immediate", 31, 0, "0")

    var curinstraddr_imm    = AdduStageVar("curinstraddr_imm", 31, 0, "0")

    var funct3          = AdduStageVar("funct3", 2, 0, "0")
    var funct7          = AdduStageVar("funct7", 6, 0, "0")
    var shamt           = AdduStageVar("shamt", 4, 0, "0")

    var fencereq        = AdduStageVar("fencereq", 0, 0, "0")
    var pred            = AdduStageVar("pred", 3, 0, "0")
    var succ            = AdduStageVar("succ", 3, 0, "0")

    var ecallreq        = AdduStageVar("ecallreq", 0, 0, "0")
    var ebreakreq       = AdduStageVar("ebreakreq", 0, 0, "0")

    var csrreq          = AdduStageVar("csrreq", 0, 0, "0")
    var csrnum          = AdduStageVar("csrnum", 11, 0, "0")
    var zimm            = AdduStageVar("zimm", 4, 0, "0")

    var op1_source      = AdduStageVar("op1_source", 1, 0, "0")
    var op2_source      = AdduStageVar("op2_source", 1, 0, "0")

    // ALU control
    var alu_req         = AdduStageVar("alu_req", 0, 0, "0")
    var alu_op1         = AdduStageVar("alu_op1", 31, 0, "0")
    var alu_op2         = AdduStageVar("alu_op2", 31, 0, "0")
    var alu_op1_wide    = AdduStageVar("alu_op1_wide", 32, 0, "0")
    var alu_op2_wide    = AdduStageVar("alu_op2_wide", 32, 0, "0")

    var alu_result_wide = AdduStageVar("alu_result_wide", 32, 0, "0")
    var alu_result      = AdduStageVar("alu_result", 31, 0, "0")
    var alu_CF          = AdduStageVar("alu_CF", 0, 0, "0")
    var alu_SF          = AdduStageVar("alu_SF", 0, 0, "0")
    var alu_ZF          = AdduStageVar("alu_ZF", 0, 0, "0")
    var alu_OF          = AdduStageVar("alu_OF", 0, 0, "0")
    var alu_overflow    = AdduStageVar("alu_overflow", 0, 0, "0")

    // data memory control
    var mem_req         = AdduStageVar("mem_req", 0, 0, "0")
    var mem_cmd         = AdduStageVar("mem_cmd", 0, 0, "0")
    var mem_addr        = AdduStageVar("mem_addr", 31, 0, "0")
    var mem_be          = AdduStageVar("mem_be", 3, 0, "0")
    var mem_wdata       = AdduStageVar("mem_wdata", 31, 0, "0")
    var mem_rdata       = AdduStageVar("mem_rdata", 31, 0, "0")
    var mem_rshift      = AdduStageVar("mem_rshift", 0, 0, "0")
    var load_signext    = AdduStageVar("load_signext", 0, 0, "0")

    var mret_req        = AdduStageVar("mret_req", 0, 0, "0")
}