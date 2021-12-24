/*
 * Reordex_CFG.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*
import cyclix.*

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

    internal val trx_inflight_num = ROB_size * DataPath_width

    internal val ARF_addr_width = GetWidthToContain(ARF_depth)
    internal val PRF_addr_width =
        if (REG_MGMT is REG_MGMT_RENAMING) GetWidthToContain(REG_MGMT.PRF_depth)
        else GetWidthToContain(ARF_depth)

    internal var req_struct = hw_struct("req_struct")
    internal var resp_struct = hw_struct("resp_struct")

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

    internal var alu_CF          = DUMMY_VAR
    internal var alu_SF          = DUMMY_VAR
    internal var alu_ZF          = DUMMY_VAR
    internal var alu_OF          = DUMMY_VAR

    init {
        req_struct.addu("trx_id",   31, 0, "0")      // TODO: clean up size
        resp_struct.addu("trx_id",  31, 0, "0")      // TODO: clean up size

        if (mode == REORDEX_MODE.RISC) {
            alu_CF          = AddDstUImm("alu_CF", 1)
            alu_SF          = AddDstUImm("alu_SF", 1)
            alu_ZF          = AddDstUImm("alu_ZF", 1)
            alu_OF          = AddDstUImm("alu_OF", 1)
        }
    }
}