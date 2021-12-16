/*
 * translator.kt
 *
 *  Created on: 24.12.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*
import cyclix.*

data class __exu_info(val module : cyclix.Streaming,
                      val req_bus : hw_var,
                      val resp_bus : hw_var)

class __TranslateInfo() {
    var exu_assocs = mutableMapOf<Exu, __exu_info>()
}

open class RISCDecoder_rs (var req : hw_var, var addr : hw_var, var rdata : hw_var)

open class RISCDecoder_rd (var req : hw_var, var source : hw_var, var addr : hw_var, var wdata : hw_var, var rdy : hw_var)
internal fun Fill_RISCDecoder_rds_StageVars(stage : hw_stage, amount : Int, rds : ArrayList<RISCDecoder_rd>, ARF_addr_width : Int) {
    for (rd_idx in 0 until amount) {
        rds.add(
            RISCDecoder_rd(
                stage.AdduStageVar("rd" + rd_idx + "_req", 0, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_source", 2, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_addr", ARF_addr_width-1, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_wdata", 31, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_rdy", 0, 0, "0")
            )
        )
    }
}

open class RISCDecoder_rs_ctrl (var rdy : hw_var, var tag : hw_var)
internal fun Fill_RISCDecoder_rss_StageVars(stage : hw_stage, amount : Int, rss : ArrayList<RISCDecoder_rs>, ARF_addr_width : Int, RF_width: Int) {
    for (rs_idx in 0 until amount) {
        rss.add(
            RISCDecoder_rs(
                stage.AdduStageVar("rs" + rs_idx + "_req", 0, 0, "0"),
                stage.AdduStageVar("rs" + rs_idx + "_addr", ARF_addr_width - 1, 0, "0"),
                stage.AdduStageVar("rs" + rs_idx + "_rdata", RF_width - 1, 0, "0")
            )
        )
    }
}

open class RISCDecoder_rd_ctrl (var tag : hw_var)
internal fun Fill_RISCDecoder_rds_ctrl_StageVars(stage : hw_stage, amount : Int, rds_ctrl : ArrayList<RISCDecoder_rd_ctrl>, PRF_addr_width : Int) {
    for (rd_idx in 0 until amount) {
        rds_ctrl.add(
            RISCDecoder_rd_ctrl(
                stage.AdduStageVar("rd" + rd_idx + "_tag", PRF_addr_width-1, 0, "0")
            )
        )
    }
}

open class ROB_rd_ctrl(tag : hw_var, var tag_prev_clr : hw_var, var tag_prev : hw_var) : RISCDecoder_rd_ctrl(tag)
internal fun Fill_ROB_rds_ctrl_StageVars(stage : hw_stage, amount : Int, rds_ctrl : ArrayList<ROB_rd_ctrl>, PRF_addr_width : Int) {
    for (rd_idx in 0 until amount) {
        rds_ctrl.add(
            ROB_rd_ctrl(
                stage.AdduStageVar("rd" + rd_idx + "_tag", PRF_addr_width-1, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_tag_prev_clr",   0, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_tag_prev",       PRF_addr_width-1, 0, "0")
            )
        )
    }
}

class RISCDecoder_memctrl   (var req : hw_var,
                             var cmd : hw_var,
                             var addr : hw_var,
                             var be : hw_var,
                             var wdata : hw_var,
                             var rdata : hw_var,
                             var rshift : hw_var,
                             var load_signext : hw_var)

class Branchctrl(var req: hw_var,
                 var req_cond: hw_var,
                 var src: hw_var,
                 var vector: hw_var,
                 var mask: hw_var)

class ALUStatus(var CF: hw_var,
                var SF: hw_var,
                var ZF: hw_var,
                var OF: hw_var)

open class trx_buffer(cyclix_gen : cyclix.Generic,
                      name_prefix : String,
                      TRX_BUF_SIZE : Int,
                      TRX_DIM_SIZE : Int,
                      val MultiExu_CFG : Reordex_CFG) : hw_stage(cyclix_gen, name_prefix, TRX_BUF_SIZE, TRX_DIM_SIZE, STAGE_FC_MODE.BUFFERED, false) {

    constructor(cyclix_gen : cyclix.Generic,
                name_prefix : String,
                TRX_BUF_SIZE : Int,
                MultiExu_CFG : Reordex_CFG) : this(cyclix_gen, name_prefix, TRX_BUF_SIZE, 0, MultiExu_CFG)

    var enb     = AddStageVar(hw_structvar("enb", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    var push    = cyclix_gen.ulocal(name_prefix + "_push", 0, 0, "0")
    var pop     = cyclix_gen.ulocal(name_prefix + "_pop", 0, 0, "0")

}

class Src(name : String, msb : Int, lsb : Int, defval : String) : hw_var(name, msb, lsb, defval)

data class __src_handle(val src_rdy : hw_var,
                        val src_tag : hw_var,
                        val src_src : hw_var,
                        val src_data : hw_var)

open class uop_buffer(cyclix_gen : cyclix.Generic,
                      name_prefix : String,
                      TRX_BUF_SIZE : Int,
                      TRX_DIM_SIZE : Int,
                      MultiExu_CFG : Reordex_CFG,
                      val cdb_num : Int) : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, TRX_DIM_SIZE, MultiExu_CFG) {

    constructor(cyclix_gen : cyclix.Generic,
                name_prefix : String,
                TRX_BUF_SIZE : Int,
                MultiExu_CFG : Reordex_CFG,
                cdb_num : Int) : this(cyclix_gen, name_prefix, TRX_BUF_SIZE, 0, MultiExu_CFG, cdb_num)

    var fu_req      = AddStageVar(hw_structvar("fu_req", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))
    val imm_rsrv    = ArrayList<hw_var>()
    val src_rsrv    = ArrayList<__src_handle>()
    val rdy         = AddStageVar(hw_structvar("rdy", DATA_TYPE.BV_UNSIGNED, 0, 0, "0"))

    init {
        for (imm_idx in 0 until MultiExu_CFG.src_imms.size) {
            imm_rsrv.add(AddStageVar(hw_structvar(MultiExu_CFG.src_imms[imm_idx].name, MultiExu_CFG.src_imms[imm_idx].vartype, MultiExu_CFG.src_imms[imm_idx].defimm)))
        }
        for (RF_rs_idx in 0 until MultiExu_CFG.srcs.size) {
            src_rsrv.add(__src_handle(
                AdduStageVar("src" + RF_rs_idx + "_rdy", 0, 0, "1"),
                AdduStageVar("src" + RF_rs_idx + "_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0"),
                AdduStageVar("src" + RF_rs_idx + "_src", GetWidthToContain(cdb_num)-1, 0, "0"),
                AdduStageVar("src" + RF_rs_idx + "_data",MultiExu_CFG.RF_width-1, 0, "0")
            ))
        }
    }
}

internal class __exu_descr(var var_dict : MutableMap<hw_var, hw_var>, var rs_use_flags : ArrayList<Boolean>, var IQ_insts : ArrayList<iq_buffer>)

internal class __control_structures(val cyclix_gen : cyclix.Generic,
                                    val MultiExu_CFG : Reordex_CFG,
                                    val PRF : cyclix.hw_global,
                                    val PRF_mapped : cyclix.hw_global,
                                    val PRF_rdy : cyclix.hw_global,
                                    val ARF_map : cyclix.hw_global,
                                    val ARF_map_default : hw_imm_arr,
                                    val PRF_src : cyclix.hw_global,
                                    val ExecUnits : MutableMap<String, Exu_CFG>,
                                    val exu_descrs : MutableMap<String, __exu_descr>,
                                    val exu_rst : hw_var) {

    val PRF_mapped_buf = cyclix_gen.global("genPRF_mapped_buf", PRF_mapped.vartype, PRF_mapped.defimm)

    fun RenameReg(src_addr : hw_param) : hw_var {
        return ARF_map.GetFracRef(src_addr)
    }

    fun FetchRs(src_tag : hw_param) : hw_var {
        return PRF.GetFracRef(src_tag)
    }

    fun FetchRsRdy(src_prf_index : hw_param) : hw_var {
        return PRF_rdy.GetFracRef(src_prf_index)
    }

    fun FillReadRs(fetch_tag : hw_var, fetch_rdy : hw_var, fetch_data : hw_var, raddr : hw_param) {
        cyclix_gen.MSG_COMMENT("Fetching data from physical registers...")
        fetch_tag.assign(RenameReg(raddr))
        fetch_rdy.assign(FetchRsRdy(fetch_tag))
        fetch_data.assign(FetchRs(fetch_tag))
        cyclix_gen.MSG_COMMENT("Fetching data from physical registers: done")
    }

    fun InitFreePRFBuf() {
        cyclix_gen.assign(PRF_mapped_buf, PRF_mapped.readPrev())
    }

    fun GetFreePRF() : hw_astc.bit_position {
        return cyclix_gen.min0(PRF_mapped_buf)
    }

    fun ReserveRd(rd_addr : hw_param, rd_tag : hw_param) {
        cyclix_gen.assign(ARF_map.GetFracRef(rd_addr), rd_tag)
        cyclix_gen.assign(PRF_mapped.GetFracRef(rd_tag), 1)
        cyclix_gen.assign(PRF_mapped_buf.GetFracRef(rd_tag), 1)
        cyclix_gen.assign(PRF_rdy.GetFracRef(rd_tag), 0)
    }

    fun ReserveWriteRd(src_rd : hw_param, src_tag : hw_param, src_wdata : hw_param) {
        cyclix_gen.assign(ARF_map.GetFracRef(src_rd), src_tag)
        cyclix_gen.assign(PRF_mapped.GetFracRef(src_tag), 1)
        cyclix_gen.assign(PRF_mapped_buf.GetFracRef(src_tag), 1)
        cyclix_gen.assign(PRF_rdy.GetFracRef(src_tag), 1)
        cyclix_gen.assign(PRF.GetFracRef(src_tag), src_wdata)
    }

    fun WritePRF(rd_tag : hw_param, src_wdata : hw_param) {
        cyclix_gen.assign(PRF_rdy.GetFracRef(rd_tag), 1)
        cyclix_gen.assign(PRF.GetFracRef(rd_tag), src_wdata)
    }

    fun FreePRF(src_tag : hw_param) {
        cyclix_gen.assign(
            PRF_mapped.GetFracRef(src_tag),
            0)
    }

    fun RollBack(Backoff_ARF : hw_var) {
        cyclix_gen.assign(exu_rst, 1)
        cyclix_gen.assign(PRF_mapped, PRF_mapped.defimm)
        cyclix_gen.assign(PRF_rdy, PRF_rdy.defimm)
        //cyclix_gen.assign(ARF_map, ARF_map_default)               // TODO: fix error
        for (reg_idx in 0 until ARF_map.GetWidth()) {
            cyclix_gen.assign(ARF_map.GetFracRef(reg_idx), hw_imm(reg_idx))
        }
        cyclix_gen.assign(PRF_src, PRF_src.defimm)
        for (reg_idx in 0 until Backoff_ARF.GetWidth()) {
            cyclix_gen.assign(PRF.GetFracRef(reg_idx), Backoff_ARF.GetFracRef(reg_idx))
        }
    }
}