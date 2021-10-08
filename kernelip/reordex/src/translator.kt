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

open class trx_buffer(cyclix_gen : cyclix.Generic,
                      name_prefix : String,
                      TRX_BUF_SIZE : Int,
                      MultiExu_CFG : Reordex_CFG) : hw_stage(cyclix_gen, name_prefix, TRX_BUF_SIZE, STAGE_FC_MODE.BUFFERED, false) {

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
                      MultiExu_CFG : Reordex_CFG,
                      val cdb_num : Int) : trx_buffer(cyclix_gen, name_prefix, TRX_BUF_SIZE, MultiExu_CFG) {

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

class __exu_descr(var var_dict : MutableMap<hw_var, hw_var>, var rs_use_flags : ArrayList<Boolean>, var IQ_insts : ArrayList<iq_buffer>)

class __control_structures(val cyclix_gen : cyclix.Generic,
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
        cyclix_gen.MSG_COMMENT("Fetching data from physical registers")
        fetch_tag.assign(RenameReg(raddr))
        fetch_rdy.assign(FetchRsRdy(fetch_tag))
        fetch_data.assign(FetchRs(fetch_tag))
    }

    fun GetFreePRF() : hwast.hw_astc.bit_position {
        return cyclix_gen.min0(PRF_mapped)
    }

    fun ReserveRd(rd_addr : hw_param, rd_tag : hw_param) {
        cyclix_gen.assign(ARF_map.GetFracRef(rd_addr), rd_tag)
        cyclix_gen.assign(PRF_mapped.GetFracRef(rd_tag), 1)
        cyclix_gen.assign(PRF_rdy.GetFracRef(rd_tag), 0)
    }

    fun ReserveWriteRd(src_rd : hw_param, src_tag : hw_param, src_wdata : hw_param) {
        cyclix_gen.assign(ARF_map.GetFracRef(src_rd), src_tag)
        cyclix_gen.assign(PRF_mapped.GetFracRef(src_tag), 1)
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