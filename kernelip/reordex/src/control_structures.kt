/*
 * control_structures.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

internal abstract class __control_structures(val cyclix_gen : cyclix.Generic,
                                             val MultiExu_CFG : Reordex_CFG,
                                             val CDB_NUM : Int,
                                             val ExecUnits : MutableMap<String, Exu_CFG>,
                                             val exu_descrs : MutableMap<String, __exu_descr>,
                                             val exu_rst : hw_var
) {

    var arf_dim = hw_dim_static()
    var Backoff_ARF = cyclix_gen.uglobal("Backoff_ARF", arf_dim, "0")

    var prf_src_dim = hw_dim_static()
    var PRF_src = cyclix_gen.uglobal("genPRF_src", prf_src_dim, "0") // uncomputed PRF sources

    init {
        arf_dim.add(MultiExu_CFG.RF_width-1, 0)
        arf_dim.add(MultiExu_CFG.ARF_depth-1, 0)
    }

    var states_toRollBack = ArrayList<hw_var>()

    abstract fun RollBack()
    abstract fun FetchRs(src_tag : hw_param) : hw_var
    abstract fun FetchRsRdy(src_prf_index : hw_param) : hw_var
    abstract fun FillReadRs(fetch_tag : hw_var, fetch_rdy : hw_var, fetch_data : hw_var, raddr : hw_param)
}

internal class __control_structures_scoreboarding(cyclix_gen : cyclix.Generic,
                                                  MultiExu_CFG : Reordex_CFG,
                                                  CDB_NUM : Int,
                                                  ExecUnits : MutableMap<String, Exu_CFG>,
                                                  exu_descrs : MutableMap<String, __exu_descr>,
                                                  exu_rst : hw_var
) : __control_structures(cyclix_gen, MultiExu_CFG, CDB_NUM, ExecUnits, exu_descrs, exu_rst) {

    var ARF = cyclix_gen.uglobal("genARF", arf_dim, "0")
    var ARF_rdy = cyclix_gen.uglobal("genARF_rdy", MultiExu_CFG.ARF_depth-1, 0, hw_imm_ones(MultiExu_CFG.ARF_depth))
    val ARF_rdy_prev = cyclix_gen.local("genPRF_mapped_prev", ARF_rdy.vartype, ARF_rdy.defimm)

    init {
        if (!(MultiExu_CFG.REG_MGMT is REG_MGMT_SCOREBOARDING)) ERROR("Configuration inconsistent!")

        prf_src_dim.add(GetWidthToContain(CDB_NUM)-1, 0)
        prf_src_dim.add(MultiExu_CFG.ARF_depth-1, 0)
    }

    fun InitFreeARFRdy() {
        cyclix_gen.assign(ARF_rdy_prev, ARF_rdy.readPrev())
    }

    override fun FetchRs(src_tag : hw_param) : hw_var {
        return ARF.GetFracRef(src_tag)
    }

    override fun FetchRsRdy(src_prf_index : hw_param) : hw_var {
        return ARF_rdy.GetFracRef(src_prf_index)
    }

    override fun FillReadRs(fetch_tag : hw_var, fetch_rdy : hw_var, fetch_data : hw_var, raddr : hw_param) {
        cyclix_gen.MSG_COMMENT("Fetching data from architectural registers...")
        fetch_tag.assign(raddr)
        fetch_rdy.assign(FetchRsRdy(fetch_tag))
        fetch_data.assign(FetchRs(fetch_tag))
        cyclix_gen.MSG_COMMENT("Fetching data from architectural registers: done")
    }

    fun ReserveRd(rd_tag : hw_param) {
        cyclix_gen.assign(ARF_rdy.GetFracRef(rd_tag), 0)
    }

    fun WriteARF(rd_tag : hw_param, src_wdata : hw_param) {
        cyclix_gen.assign(ARF_rdy.GetFracRef(rd_tag), 1)
        cyclix_gen.assign(ARF.GetFracRef(rd_tag), src_wdata)
    }

    override fun RollBack() {
        cyclix_gen.assign(exu_rst, 1)
        cyclix_gen.assign(ARF_rdy, ARF_rdy.defimm)
        //cyclix_gen.assign(ARF_map, ARF_map_default)               // TODO: fix error
        cyclix_gen.assign(PRF_src, PRF_src.defimm)
        for (reg_idx in 0 until Backoff_ARF.GetWidth()) {
            cyclix_gen.assign(ARF.GetFracRef(reg_idx), Backoff_ARF.GetFracRef(reg_idx))
        }
        for (state in states_toRollBack) {
            cyclix_gen.assign(state, state.defimm)
        }
    }
}

internal class __control_structures_renaming(cyclix_gen : cyclix.Generic,
                                             MultiExu_CFG : Reordex_CFG,
                                             CDB_NUM : Int,
                                             ExecUnits : MutableMap<String, Exu_CFG>,
                                             exu_descrs : MutableMap<String, __exu_descr>,
                                             exu_rst : hw_var
) : __control_structures(cyclix_gen, MultiExu_CFG, CDB_NUM, ExecUnits, exu_descrs, exu_rst) {

    var prf_dim = hw_dim_static()
    var PRF = cyclix_gen.uglobal("genPRF", prf_dim, "0")

    var PRF_mapped = cyclix_gen.uglobal("genPRF_mapped", (MultiExu_CFG.REG_MGMT as REG_MGMT_RENAMING).PRF_depth-1, 0, hw_imm_ones(MultiExu_CFG.ARF_depth))

    var PRF_rdy = cyclix_gen.uglobal("genPRF_rdy", (MultiExu_CFG.REG_MGMT as REG_MGMT_RENAMING).PRF_depth-1, 0, hw_imm_ones(MultiExu_CFG.REG_MGMT.PRF_depth))

    var arf_map_dim = hw_dim_static()
    var ARF_map_default = hw_imm_arr(arf_map_dim)
    var ARF_map = cyclix_gen.uglobal("genARF_map", arf_map_dim, ARF_map_default)        // ARF-to-PRF mappings

    val PRF_mapped_prev = cyclix_gen.local("genPRF_mapped_prev", PRF_mapped.vartype, PRF_mapped.defimm)

    init {
        if (!(MultiExu_CFG.REG_MGMT is REG_MGMT_RENAMING)) ERROR("Configuration inconsistent!")

        prf_dim.add(MultiExu_CFG.RF_width-1, 0)
        prf_dim.add((MultiExu_CFG.REG_MGMT as REG_MGMT_RENAMING).PRF_depth-1, 0)

        arf_map_dim.add(MultiExu_CFG.PRF_addr_width-1, 0)
        arf_map_dim.add(MultiExu_CFG.ARF_depth-1, 0)

        for (RF_idx in 0 until (MultiExu_CFG.REG_MGMT as REG_MGMT_RENAMING).PRF_depth) {
            if (RF_idx < MultiExu_CFG.ARF_depth) {
                ARF_map_default.AddSubImm(RF_idx.toString())
            } else {
                ARF_map_default.AddSubImm("0")
            }
        }

        prf_src_dim.add(GetWidthToContain(CDB_NUM)-1, 0)
        prf_src_dim.add(MultiExu_CFG.REG_MGMT.PRF_depth-1, 0)
    }

    fun RenameReg(src_addr : hw_param) : hw_var {
        return ARF_map.GetFracRef(src_addr)
    }

    override fun FetchRs(src_tag : hw_param) : hw_var {
        return PRF.GetFracRef(src_tag)
    }

    override fun FetchRsRdy(src_prf_index : hw_param) : hw_var {
        return PRF_rdy.GetFracRef(src_prf_index)
    }

    override fun FillReadRs(fetch_tag : hw_var, fetch_rdy : hw_var, fetch_data : hw_var, raddr : hw_param) {
        cyclix_gen.MSG_COMMENT("Fetching data from physical registers...")
        fetch_tag.assign(RenameReg(raddr))
        fetch_rdy.assign(FetchRsRdy(fetch_tag))
        fetch_data.assign(FetchRs(fetch_tag))
        cyclix_gen.MSG_COMMENT("Fetching data from physical registers: done")
    }

    fun InitFreePRFBuf() {
        cyclix_gen.assign(PRF_mapped_prev, PRF_mapped.readPrev())
    }

    fun GetFreePRF() : hw_astc.bit_position {
        return cyclix_gen.min0(PRF_mapped_prev, 4)
    }

    fun ReserveRd(rd_addr : hw_param, rd_tag : hw_param) {
        cyclix_gen.assign(ARF_map.GetFracRef(rd_addr), rd_tag)
        cyclix_gen.assign(PRF_mapped.GetFracRef(rd_tag), 1)
        cyclix_gen.assign(PRF_mapped_prev.GetFracRef(rd_tag), 1)
        cyclix_gen.assign(PRF_rdy.GetFracRef(rd_tag), 0)
    }

    fun ReserveWriteRd(src_rd : hw_param, src_tag : hw_param, src_wdata : hw_param) {
        cyclix_gen.assign(ARF_map.GetFracRef(src_rd), src_tag)
        cyclix_gen.assign(PRF_mapped.GetFracRef(src_tag), 1)
        cyclix_gen.assign(PRF_mapped_prev.GetFracRef(src_tag), 1)
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

    override fun RollBack() {
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
        for (state in states_toRollBack) {
            cyclix_gen.assign(state, state.defimm)
        }
    }
}