/*
 * Exu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

val OP_EXU = hwast.hw_opcode("exec_unit")

class Exu_CFG_RF(val RF_width : Int,
                 val RF_rs_num : Int) {

    var req_struct = hw_struct("req_struct")
    var resp_struct = hw_struct("resp_struct")

    init {
        req_struct.addu("opcode",     31, 0, "0")
        for (RF_rs_idx in 0 until RF_rs_num) {
            req_struct.addu("rs" + RF_rs_idx + "_rdata", RF_width-1, 0, "0")
        }
        req_struct.addu("rd_tag",     31, 0, "0")       // TODO: clean up

        resp_struct.addu("tag",     31, 0, "0")         // TODO: clean up
        resp_struct.addu("wdata",     RF_width-1, 0, "0")
    }
}

open class Exu(val name : String, val Exu_cfg_rf : Exu_CFG_RF) : hw_astc_stdif() {

    override var GenNamePrefix   = "reordex"

    var locals          = ArrayList<hw_var>()
    var globals         = ArrayList<hw_var>()

    var req_data = local(GetGenName("req_data"), Exu_cfg_rf.req_struct)
    var resp_data = local(GetGenName("resp_data"), Exu_cfg_rf.resp_struct)

    var opcode      = ulocal("opcode", 31, 0, "0")
    var rs          = ArrayList<hw_var>()
    var result      = ulocal("result", Exu_cfg_rf.RF_width-1, 0, "0")

    init {
        if (FROZEN_FLAG) ERROR("Failed to begin stage " + name + ": ASTC frozen")
        if (this.size != 0) ERROR("reordex ASTC inconsistent!")
        add(hw_exec(OP_EXU))

        for (rs_num in 0 until Exu_cfg_rf.RF_rs_num) {
            rs.add(ulocal("rs" + rs_num, Exu_cfg_rf.RF_width-1, 0, "0"))
        }

    }

    fun endexu() {
        if (FROZEN_FLAG) ERROR("Failed to end stage: ASTC frozen")
        if (this.size != 1) ERROR("Exu ASTC inconsistent!")
        if (this[0].opcode != OP_EXU) ERROR("Exu ASTC inconsistent!")
        this.clear()
    }

    private fun add_local(new_local: hw_var) {
        if (FROZEN_FLAG) ERROR("Failed to add local " + new_local.name + ": ASTC frozen")

        if (wrvars.containsKey(new_local.name)) ERROR("Naming conflict for local: " + new_local.name)
        if (rdvars.containsKey(new_local.name)) ERROR("Naming conflict for local: " + new_local.name)

        wrvars.put(new_local.name, new_local)
        rdvars.put(new_local.name, new_local)
        locals.add(new_local)
        new_local.default_astc = this
    }

    fun local(name: String, vartype : hw_type, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, vartype, defimm)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, vartype : hw_type, defval: String): hw_var {
        var ret_var = hw_var(name, vartype, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_var {
        var ret_var = hw_var(name, hw_type(src_struct_in, dimensions), "0")
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct_in: hw_struct): hw_var {
        var ret_var = hw_var(name, hw_type(src_struct_in), "0")
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, dimensions: hw_dim_static, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, defimm.imm_value), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, dimensions), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, defimm.imm_value), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    private fun add_global(new_global: hw_var) {
        if (FROZEN_FLAG) ERROR("Failed to add global " + new_global.name + ": ASTC frozen")

        if (wrvars.containsKey(new_global.name)) ERROR("Naming conflict for global: " + new_global.name)
        if (rdvars.containsKey(new_global.name)) ERROR("Naming conflict for global: " + new_global.name)

        wrvars.put(new_global.name, new_global)
        rdvars.put(new_global.name, new_global)
        globals.add(new_global)
        new_global.default_astc = this
    }

    fun global(name: String, vartype: hw_type, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, vartype, defimm)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, vartype: hw_type, defval: String): hw_var {
        var ret_var = hw_var(name, vartype, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_var {
        var ret_var = hw_var(name, hw_type(src_struct_in, dimensions), "0")
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct_in: hw_struct): hw_var {
        var ret_var = hw_var(name, hw_type(src_struct_in), "0")
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, dimensions: hw_dim_static, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, defimm.imm_value), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, dimensions), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, defimm.imm_value), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }
}
