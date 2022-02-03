/*
 * Reordex_CFG.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

enum class REORDEX_MODE {
    COPROCESSOR,
    RISC
}

abstract class REG_MGMT_MODE()
class REG_MGMT_SCOREBOARDING() : REG_MGMT_MODE()
class REG_MGMT_RENAMING(val PRF_depth: Int) : REG_MGMT_MODE()

open class Reordex_CFG(val RF_width : Int,
                       val ARF_depth : Int,
                       val DataPath_width : Int,
                       val REG_MGMT: REG_MGMT_MODE,
                       val ROB_size : Int,
                       val mode : REORDEX_MODE) {

    internal var BTAC_SIZE = 16

    internal val trx_inflight_num = ROB_size * DataPath_width

    internal val ARF_addr_width = GetWidthToContain(ARF_depth)
    internal val PRF_addr_width =
        if (REG_MGMT is REG_MGMT_RENAMING) GetWidthToContain(REG_MGMT.PRF_depth)
        else GetWidthToContain(ARF_depth)

    internal var req_struct = hw_struct("req_struct")
    internal var resp_struct = hw_struct("resp_struct")

    init {
        if (mode == REORDEX_MODE.RISC) {
            req_struct.add("curinstr_addr", hw_type(DATA_TYPE.BV_UNSIGNED, 31, 0), "0")
            req_struct.add("nextinstr_addr", hw_type(DATA_TYPE.BV_UNSIGNED, 31, 0), "0")

            resp_struct.add("branch_req", hw_type(DATA_TYPE.BV_UNSIGNED, 0, 0), "0")
            resp_struct.add("branch_vec", hw_type(DATA_TYPE.BV_UNSIGNED, 31, 0), "0")
        }
    }

    internal var src_imms = ArrayList<hw_var>()
    fun AddSrcImm(name : String, new_type : hw_type) : hw_var {
        var new_var = hw_var(name, new_type, "0")
        req_struct.add(name, new_type, "0")
        src_imms.add(new_var)
        return new_var
    }
    fun AddSrcUImm(name : String, new_width : Int) : hw_var {
        return AddSrcImm(name, hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(new_width)))
    }
    fun AddSrcSImm(name : String, new_width : Int) : hw_var {
        return AddSrcImm(name, hw_type(DATA_TYPE.BV_SIGNED, hw_dim_static(new_width)))
    }

    internal var dst_imms = ArrayList<hw_var>()
    internal fun AddDstImm(name : String, new_type : hw_type) : hw_var {
        var new_var = hw_var(name, new_type, "0")
        resp_struct.add(name, new_type, "0")
        dst_imms.add(new_var)
        return new_var
    }
    internal fun AddDstUImm(name : String, new_width : Int) : hw_var {
        return AddDstImm(name, hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(new_width)))
    }
    internal fun AddDstSImm(name : String, new_width : Int) : hw_var {
        return AddDstImm(name, hw_type(DATA_TYPE.BV_SIGNED, hw_dim_static(new_width)))
    }

    internal var srcs = ArrayList<hw_var>()
    fun AddSrc() : Src {
        var new_src = Src("src" + srcs.size, RF_width-1, 0, "0")
        req_struct.addu("src" + srcs.size + "_data", RF_width-1, 0, "0")
        srcs.add(new_src)
        return new_src
    }

    internal var rds = ArrayList<hw_var>()
    fun AddRd() : hw_var {
        var new_var = hw_var("rd" + rds.size, RF_width-1, 0, "0")

        req_struct.addu("rd" + rds.size + "_req", 0, 0, "0")
        req_struct.addu("rd" + rds.size + "_tag", RF_width-1, 0, "0")

        resp_struct.addu("rd" + rds.size + "_req",     0, 0, "0")      // TODO: clean up size
        resp_struct.addu("rd" + rds.size + "_tag",     31, 0, "0")      // TODO: clean up size
        resp_struct.addu("rd" + rds.size + "_wdata",   RF_width-1, 0, "0")

        rds.add(new_var)

        return new_var
    }

    init {
        req_struct.addu("trx_id",   31, 0, "0")      // TODO: clean up size
        resp_struct.addu("trx_id",  31, 0, "0")      // TODO: clean up size
    }
}

open class RISCDecoder (MultiExu_CFG : Reordex_CFG) : RISCDecodeContainer(MultiExu_CFG) {

    var instr_code = ugenvar("instr_code", 31, 0, "0")

    val curinstr_addr   = ugenvar("curinstr_addr_decoder", 31, 0, "0")

    // regfile control signals
    var rsctrls = mutableMapOf<hw_var, RISCDecoder_rs>()
    var rdctrls = mutableMapOf<hw_var, RISCDecoder_rd>()

    var cf_can_alter    = ugenvar("cf_can_alter", 0, 0, "0")

    var csr_rdata       = ugenvar("csr_rdata", 31, 0, "0")
    var immediate       = ugenvar("immediate", 31, 0, "0")

    var fencereq        = ugenvar("fencereq", 0, 0, "0")
    var pred            = ugenvar("pred", 3, 0, "0")
    var succ            = ugenvar("succ", 3, 0, "0")

    var ecallreq        = ugenvar("ecallreq", 0, 0, "0")
    var ebreakreq       = ugenvar("ebreakreq", 0, 0, "0")

    var csrreq          = ugenvar("csrreq", 0, 0, "0")
    var csrnum          = ugenvar("csrnum", 11, 0, "0")

    var exu_req         = ugenvar("exu_req", 0, 0, "0")
    var exu_id          = ugenvar("exu_id", 31, 0, "0")     // TODO: dimensions fix

    var memctrl         = RISCDecoder_memctrl(
        ugenvar("mem_req", 0, 0, "0"),
        ugenvar("mem_we", 0, 0, "0"),
        ugenvar("mem_addr", 31, 0, "0"),
        ugenvar("mem_be", 3, 0, "0"),
        ugenvar("mem_wdata", 31, 0, "0"),
        ugenvar("mem_rdata", 31, 0, "0"),
        ugenvar("mem_rshift", 0, 0, "0"),
        ugenvar("mem_load_signext", 0, 0, "0")
    )

    var mret_req        = ugenvar("mret_req", 0, 0, "0")
    var MRETADDR        = ugenvar("MRETADDR", 31, 0, "0")

    //////////
    internal var rss_ctrl = ArrayList<RISCDecoder_rs_ctrl>()
    internal var rds_ctrl = ArrayList<RISCDecoder_rd_ctrl>()

    var CSR_MCAUSE      = hw_var("CSR_MCAUSE", 7, 0, "0")

    init {
        for (rs_idx in 0 until MultiExu_CFG.srcs.size) {

            rsctrls.put(MultiExu_CFG.srcs[rs_idx], RISCDecoder_rs(
                ugenvar("rs" + rs_idx + "_req", 0, 0, "0"),
                ugenvar("rs" + rs_idx + "_addr",  MultiExu_CFG.ARF_addr_width-1, 0, "0"),
                ugenvar("rs" + rs_idx + "_rdata", MultiExu_CFG.RF_width-1, 0, "0")
            ))

            rss_ctrl.add(
                RISCDecoder_rs_ctrl(
                    ugenvar("rs" + rs_idx + "_rdy", 0, 0, "0"),
                    ugenvar("rs" + rs_idx + "_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")
                )
            )
        }
        for (rd_idx in 0 until MultiExu_CFG.rds.size) {

            rdctrls.put(MultiExu_CFG.rds[rd_idx], RISCDecoder_rd(
                ugenvar("rd" + rd_idx + "_req", 0, 0, "0"),
                ugenvar("rd" + rd_idx + "_source", 2, 0, "0"),
                ugenvar("rd" + rd_idx + "_addr", 4, 0, "0"),
                ugenvar("rd" + rd_idx + "_wdata", 31, 0, "0"),
                ugenvar("rd" + rd_idx + "_rdy", 0, 0, "0")
            ))

            rds_ctrl.add(
                RISCDecoder_rd_ctrl(
                    ugenvar("rd" + rd_idx + "_tag", MultiExu_CFG.PRF_addr_width-1, 0, "0")
                )
            )
        }

        for (src_imm in MultiExu_CFG.src_imms)  src_imm.default_astc = this
        for (src in MultiExu_CFG.srcs)          src.default_astc = this
        for (dst_imm in MultiExu_CFG.dst_imms)  dst_imm.default_astc = this
        for (rd in MultiExu_CFG.rds)            rd.default_astc = this

    }

    fun GetExuID(srcExuName : String) : hw_var {
        var new_expr = hw_exec_get_exu_id(srcExuName)
        var genvar = hw_var(GetGenName("getExuID"), 31, 0, "0")
        genvar.default_astc = this
        new_expr.AddGenVar(genvar)
        new_expr.AddDst(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun SrcSetImm(src : Src, imm : hw_param) {
        AddExpr(hw_exec_src_set_imm(src, imm))
    }

    fun SrcReadReg(src : Src, raddr : hw_param) {
        AddExpr(hw_exec_src_rd_reg(src, raddr))
    }

}