/*
 * Exu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

val OP_EXU = hw_opcode("exec_unit")
val OP_GET_EXU_ID = hw_opcode("get_exu_id")

open class hw_exec_get_exu_id(val srcExuName : String) : hw_exec(OP_GET_EXU_ID)

open class Exu(val name : String, val Exu_CFG : Reordex_CFG) : hw_astc_stdif() {

    override var GenNamePrefix   = "reordex"

    internal var locals          = ArrayList<hw_var>()
    internal var globals         = ArrayList<hw_var>()

    internal var req_data = local(GetGenName("req_data"), Exu_CFG.req_struct)
    internal var resp_data = local(GetGenName("resp_data"), Exu_CFG.resp_struct)

    internal var src_imms    = ArrayList<hw_var>()
    internal var srcs        = ArrayList<hw_var>()
    internal var rds         = ArrayList<hw_var>()

    var curinstr_addr  = ulocal("curinstr_addr", 31, 0, "0")
    var nextinstr_addr = ulocal("nextinstr_addr", 31, 0, "0")

    init {
        if (FROZEN_FLAG) ERROR("Failed to begin stage " + name + ": ASTC frozen")
        if (this.size != 0) ERROR("reordex ASTC inconsistent!")
        add(hw_exec(OP_EXU))

        for (Exu_imm in Exu_CFG.src_imms) {
            src_imms.add(local(Exu_imm.name, Exu_imm.vartype, Exu_imm.defimm))
        }

        for (Exu_src in Exu_CFG.srcs) {
            srcs.add(local(Exu_src.name, Exu_src.vartype, Exu_src.defimm))
        }

        for (rd_idx in 0 until Exu_CFG.rds.size) {
            rds.add(ulocal("rd" + rd_idx, Exu_CFG.RF_width-1, 0, "0"))
        }

        for (src_imm in Exu_CFG.src_imms)   src_imm.default_astc = this
        for (src in Exu_CFG.srcs)           src.default_astc = this
        for (dst_imm in Exu_CFG.dst_imms)   dst_imm.default_astc = this
        for (rd in Exu_CFG.rds)             rd.default_astc = this

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
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, defimm.imm_value), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, defimm.imm_value), defimm)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, defval), defval)
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
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, defimm.imm_value), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_UNSIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defimm: hw_imm): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, defimm.imm_value), defimm)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defval: String): hw_var {
        var ret_var = hw_var(name, hw_type(DATA_TYPE.BV_SIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }
}
