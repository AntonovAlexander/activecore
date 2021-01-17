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

data class Exu_CFG_RF(val input_RF_width : Int)

open class Exu(val name : String, val Exu_cfg_rf : Exu_CFG_RF, val stage_num: Int) : hw_astc_stdif() {

    override var GenNamePrefix   = "reordex"

    var locals          = ArrayList<hw_var>()
    var globals         = ArrayList<hw_var>()

    var req_struct = add_struct("req_struct")
    var req_data = local(GetGenName("req_data"), req_struct)

    var resp_struct = add_struct("resp_struct")
    var resp_data = local(GetGenName("resp_data"), resp_struct)

    init {
        if (FROZEN_FLAG) ERROR("Failed to begin stage " + name + ": ASTC frozen")
        if (this.size != 0) ERROR("reordex ASTC inconsistent!")
        add(hw_exec(OP_EXU))

        req_struct.addu("opcode",     0, 0, "0")
        req_struct.addu("rs0_rdata",     Exu_cfg_rf.input_RF_width-1, 0, "0")
        req_struct.addu("rs1_rdata",     Exu_cfg_rf.input_RF_width-1, 0, "0")

        resp_struct.addu("wdata",     Exu_cfg_rf.input_RF_width-1, 0, "0")
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

    fun ulocal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
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

    fun uglobal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }
}
