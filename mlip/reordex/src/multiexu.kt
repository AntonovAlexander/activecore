/*
 * multiexu.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

open class MultiExu(name_in : String, mem_size_in : Int, mem_data_width_in: Int, rob_size_in : Int) : hw_astc_stdif() {

    val name = name_in
    val mem_size = mem_size_in
    val mem_addr_width = GetWidthToContain(mem_size_in)
    val mem_data_width = mem_data_width_in
    val rob_size = rob_size_in

    override var GenNamePrefix   = "reordex"

    var locals          = ArrayList<hw_var>()
    var globals         = ArrayList<hw_global>()

    var ExecUnits  = mutableMapOf<String, hw_exec_unit>()

    fun add_exu(name_in : String, exu_num_in: Int, stage_num_in: Int) : hw_exec_unit {
        if (FROZEN_FLAG) ERROR("Failed to add stage " + name_in + ": ASTC frozen")
        var new_exec_unit = hw_exec_unit(name_in, exu_num_in, stage_num_in, this)
        if (ExecUnits.put(new_exec_unit.name, new_exec_unit) != null) {
            ERROR("Stage addition problem!")
        }
        return new_exec_unit
    }

    fun begexu(eu : hw_exec_unit) {
        if (FROZEN_FLAG) ERROR("Failed to begin stage " + eu.name + ": ASTC frozen")
        if (this.size != 0) ERROR("reordex ASTC inconsistent!")
        // TODO: validate stage presence
        add(eu)
    }

    fun endexu() {
        if (FROZEN_FLAG) ERROR("Failed to end stage: ASTC frozen")
        if (this.size != 1) ERROR("Stage ASTC inconsistent!")
        if (this[0].opcode != OP_STAGE) ERROR("Stage ASTC inconsistent!")
        this.clear()
    }

    private fun add_local(new_local: hw_local) {
        if (FROZEN_FLAG) ERROR("Failed to add local " + new_local.name + ": ASTC frozen")

        if (wrvars.containsKey(new_local.name)) ERROR("Naming conflict for local: " + new_local.name)
        if (rdvars.containsKey(new_local.name)) ERROR("Naming conflict for local: " + new_local.name)

        wrvars.put(new_local.name, new_local)
        rdvars.put(new_local.name, new_local)
        locals.add(new_local)
        new_local.default_astc = this
    }

    fun local(name: String, vartype : hw_type, defval: String): hw_local {
        var ret_var = hw_local(name, vartype, defval)
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_local {
        var ret_var = hw_local(name, hw_type(src_struct_in, dimensions), "0")
        add_local(ret_var)
        return ret_var
    }

    fun local(name: String, src_struct_in: hw_struct): hw_local {
        var ret_var = hw_local(name, hw_type(src_struct_in), "0")
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun ulocal(name: String, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, dimensions: hw_dim_static, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, msb: Int, lsb: Int, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_local(ret_var)
        return ret_var
    }

    fun slocal(name: String, defval: String): hw_local {
        var ret_var = hw_local(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_local(ret_var)
        return ret_var
    }

    private fun add_local_sticky(new_local_sticky: hw_local_sticky) {
        if (FROZEN_FLAG) ERROR("Failed to add local_sticky " + new_local_sticky.name + ": ASTC frozen")

        if (wrvars.containsKey(new_local_sticky.name)) ERROR("Naming conflict for local_sticky: " + new_local_sticky.name)
        if (rdvars.containsKey(new_local_sticky.name)) ERROR("Naming conflict for local_sticky: " + new_local_sticky.name)

        wrvars.put(new_local_sticky.name, new_local_sticky)
        rdvars.put(new_local_sticky.name, new_local_sticky)
        locals.add(new_local_sticky)
        new_local_sticky.default_astc = this
    }

    fun local_sticky(name: String, vartype: hw_type, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, vartype, defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(src_struct_in, dimensions), "0")
        add_local_sticky(ret_var)
        return ret_var
    }

    fun local_sticky(name: String, src_struct_in: hw_struct): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(src_struct_in), "0")
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, dimensions: hw_dim_static, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, msb: Int, lsb: Int, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun ulocal_sticky(name: String, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, dimensions: hw_dim_static, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, msb: Int, lsb: Int, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    fun slocal_sticky(name: String, defval: String): hw_local_sticky {
        var ret_var = hw_local_sticky(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_local_sticky(ret_var)
        return ret_var
    }

    private fun add_global(new_global: hw_global) {
        if (FROZEN_FLAG) ERROR("Failed to add global " + new_global.name + ": ASTC frozen")

        if (wrvars.containsKey(new_global.name)) ERROR("Naming conflict for global: " + new_global.name)
        if (rdvars.containsKey(new_global.name)) ERROR("Naming conflict for global: " + new_global.name)

        wrvars.put(new_global.name, new_global)
        rdvars.put(new_global.name, new_global)
        globals.add(new_global)
        new_global.default_astc = this
    }

    fun global(name: String, vartype: hw_type, defval: String): hw_global {
        var ret_var = hw_global(name, vartype, defval)
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static): hw_global {
        var ret_var = hw_global(name, hw_type(src_struct_in, dimensions), "0")
        add_global(ret_var)
        return ret_var
    }

    fun global(name: String, src_struct_in: hw_struct): hw_global {
        var ret_var = hw_global(name, hw_type(src_struct_in), "0")
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.UNSIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun uglobal(name: String, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.UNSIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, dimensions: hw_dim_static, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.SIGNED, dimensions), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, msb: Int, lsb: Int, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.SIGNED, msb, lsb), defval)
        add_global(ret_var)
        return ret_var
    }

    fun sglobal(name: String, defval: String): hw_global {
        var ret_var = hw_global(name, hw_type(VAR_TYPE.SIGNED, defval), defval)
        add_global(ret_var)
        return ret_var
    }

    fun translate_to_cyclix(DEBUG_FLAG : Boolean) : cyclix.module {

        MSG("Translating to cyclix: beginning")

        var cyclix_gen = cyclix.module(name)

        //// Generating interfaces ////
        // cmd (sequential instruction stream) //
        var cmd_req_struct = cyclix_gen.add_struct(name + "_cmd_req_struct")
        cmd_req_struct.addu("exec",     0, 0, "0")
        cmd_req_struct.addu("rf_we",       0,  0, "0")
        cmd_req_struct.addu("rf_addr",    mem_addr_width-1, 0, "0")
        cmd_req_struct.addu("rf_wdata",    mem_data_width-1, 0, "0")
        cmd_req_struct.addu("fu_id",    GetWidthToContain(ExecUnits.size)-1, 0, "0")
        cmd_req_struct.addu("fu_rs0",    mem_addr_width-1, 0, "0")
        cmd_req_struct.addu("fu_rs1",    mem_addr_width-1, 0, "0")
        cmd_req_struct.addu("fu_rd",    mem_addr_width-1, 0, "0")
        var cmd_req = cyclix_gen.fifo_in("cmd_req",  hw_type(cmd_req_struct))
        var cmd_resp = cyclix_gen.fifo_out("cmd_resp",  hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(mem_data_width-1, 0)))

        // TODO: memory interface?

        var MAX_INSTR_NUM = mem_size + rob_size
        for (ExecUnit in ExecUnits) {
            MAX_INSTR_NUM += ExecUnit.value.exu_num * ExecUnit.value.stage_num
        }

        val TAG_WIDTH = GetWidthToContain(MAX_INSTR_NUM)

        var uop_struct = cyclix_gen.add_struct("uop_struct")
        uop_struct.addu("enb",     0, 0, "0")
        uop_struct.addu("opcode",     0, 0, "0")
        uop_struct.addu("rs0_rdata",     mem_data_width-1, 0, "0")
        uop_struct.addu("rs1_rdata",     mem_data_width-1, 0, "0")
        uop_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")
        uop_struct.addu("rd_wdata",     mem_data_width-1, 0, "0")

        var rob_struct = cyclix_gen.add_struct("rob_struct")
        rob_struct.addu("enb",     0, 0, "0")
        rob_struct.addu("fu_id",     GetWidthToContain(ExecUnits.size)-1, 0, "0")
        rob_struct.addu("opcode",     0, 0, "0")
        rob_struct.addu("rs0_ready",     0, 0, "0")
        rob_struct.addu("rs0_tag",     TAG_WIDTH-1, 0, "0")
        rob_struct.addu("rs0_rdata",     mem_data_width-1, 0, "0")
        rob_struct.addu("rs1_ready",     0, 0, "0")
        rob_struct.addu("rs1_tag",     TAG_WIDTH-1, 0, "0")
        rob_struct.addu("rs1_rdata",     mem_data_width-1, 0, "0")
        rob_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")

        var req_struct = cyclix_gen.add_struct("req_struct")
        req_struct.addu("enb",     0, 0, "0")
        req_struct.addu("opcode",     0, 0, "0")
        req_struct.addu("rdy",     0, 0, "0")
        req_struct.addu("rs0_rdata",     mem_data_width-1, 0, "0")
        req_struct.addu("rs1_rdata",     mem_data_width-1, 0, "0")
        req_struct.addu("rd_tag",     TAG_WIDTH-1, 0, "0")

        var resp_struct = cyclix_gen.add_struct("resp_struct")
        resp_struct.addu("enb",     0, 0, "0")
        resp_struct.addu("tag",     TAG_WIDTH-1, 0, "0")
        resp_struct.addu("wdata",     mem_data_width-1, 0, "0")

        var TranslateInfo = __TranslateInfo()

        var rob = cyclix_gen.global("genexu_" + name + "_rob", rob_struct, rob_size-1, 0)
        for (ExUnit in ExecUnits) {
            var exu_info = __exu_info(
                cyclix_gen.global("genexu_" + ExUnit.value.name + "_req", req_struct, ExUnit.value.exu_num-1, 0),
                cyclix_gen.global("genexu_" + ExUnit.value.name + "_resp", resp_struct, ExUnit.value.exu_num-1, 0)
            )
            TranslateInfo.exu_assocs.put(ExUnit.value, exu_info)
        }

        // issuing operations from ROB to FUs
        MSG("Translating: issuing operations from ROB to FUs")
        var rob_iter = cyclix_gen.begforall(rob)
        run {
            cyclix_gen.begif(cyclix_gen.subStruct(rob_iter.iter_elem, "enb"))
            run {
                cyclix_gen.begif(cyclix_gen.band(cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_ready"), cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_ready")))
                run {
                    // asserting op to FU req bus
                    var req_ndone = cyclix_gen.ulocal(GetGenName("req_ndone"), 0, 0, "0")
                    cyclix_gen.assign(req_ndone, 1)
                    var fu_id = 0
                    for (exu_assoc in TranslateInfo.exu_assocs) {
                        cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "fu_id"), fu_id))
                        run {
                            var req_bus_iter = cyclix_gen.begforall(exu_assoc.value.req_bus)
                            run {
                                cyclix_gen.assign(
                                    exu_assoc.value.req_bus,
                                    hw_fractions(hw_fraction_V(req_bus_iter.iter_num), hw_fraction_SubStruct("enb")),
                                    req_ndone)
                                cyclix_gen.assign(
                                    exu_assoc.value.req_bus,
                                    hw_fractions(hw_fraction_V(req_bus_iter.iter_num), hw_fraction_SubStruct("opcode")),
                                    cyclix_gen.subStruct(rob_iter.iter_elem, "opcode"))
                                cyclix_gen.assign(
                                    exu_assoc.value.req_bus,
                                    hw_fractions(hw_fraction_V(req_bus_iter.iter_num), hw_fraction_SubStruct("rs0_rdata")),
                                    cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_rdata"))
                                cyclix_gen.assign(
                                    exu_assoc.value.req_bus,
                                    hw_fractions(hw_fraction_V(req_bus_iter.iter_num), hw_fraction_SubStruct("rs1_rdata")),
                                    cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_rdata"))
                                cyclix_gen.assign(
                                    exu_assoc.value.req_bus,
                                    hw_fractions(hw_fraction_V(req_bus_iter.iter_num), hw_fraction_SubStruct("rd_tag")),
                                    cyclix_gen.subStruct(rob_iter.iter_elem, "rd_tag"))

                                cyclix_gen.begif(cyclix_gen.band(req_ndone, cyclix_gen.subStruct(req_bus_iter.iter_elem, "rdy")))
                                run {
                                    cyclix_gen.assign(req_ndone, 0)

                                    // shifting ops
                                    var rob_shift_index_next = cyclix_gen.ulocal(GetGenName("rob_shift_index"), GetWidthToContain(rob.vartype.dimensions.last().GetWidth())-1, 0, "0")
                                    var rob_shift_iter = cyclix_gen.begforrange(rob, hw_imm(rob.vartype.dimensions.last().lsb), hw_imm(rob.vartype.dimensions.last().msb))
                                    run {
                                        cyclix_gen.add_gen(rob_shift_index_next, rob_shift_iter.iter_num, 1)

                                        // writing op
                                        cyclix_gen.assign(rob, hw_fractions(hw_fraction_V(rob_shift_iter.iter_num)), cyclix_gen.indexed(rob, rob_shift_index_next))

                                        cyclix_gen.assign(rob_shift_iter.iter_num, rob_shift_index_next)
                                    }; cyclix_gen.endwhile()

                                }; cyclix_gen.endif()
                            }; cyclix_gen.endwhile()
                        }; cyclix_gen.endif()
                        fu_id++
                    }
                }; cyclix_gen.endif()
            }; cyclix_gen.endif()
        }; cyclix_gen.endwhile()

        // broadcasting FU results to ROB
        MSG("Translating: broadcasting FU results to ROB")
        for (exu_assoc in TranslateInfo.exu_assocs) {
            var resp_bus_iter = cyclix_gen.begforall(exu_assoc.value.resp_bus)
            run {
                cyclix_gen.begif(cyclix_gen.subStruct(resp_bus_iter.iter_elem, "enb"))
                run {
                    var rob_iter = cyclix_gen.begforall(rob)
                    run {
                        cyclix_gen.begif(!cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_ready"))
                        run {
                            cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rs0_tag"), cyclix_gen.subStruct(resp_bus_iter.iter_elem, "tag")))
                            run {
                                // setting rs0 ROB entry ready
                                cyclix_gen.assign(
                                    rob,
                                    hw_fractions(hw_fraction_V(rob_iter.iter_num), hw_fraction_SubStruct("rs0_rdata")),
                                    cyclix_gen.subStruct(resp_bus_iter.iter_elem, "wdata"))
                                cyclix_gen.assign(
                                    rob,
                                    hw_fractions(hw_fraction_V(rob_iter.iter_num), hw_fraction_SubStruct("rs0_ready")),
                                    1)
                            }; cyclix_gen.endif()

                            cyclix_gen.begif(cyclix_gen.eq2(cyclix_gen.subStruct(rob_iter.iter_elem, "rs1_tag"), cyclix_gen.subStruct(resp_bus_iter.iter_elem, "tag")))
                            run {
                                // setting rs1 ROB entry ready
                                cyclix_gen.assign(
                                    rob,
                                    hw_fractions(hw_fraction_V(rob_iter.iter_num), hw_fraction_SubStruct("rs1_rdata")),
                                    cyclix_gen.subStruct(resp_bus_iter.iter_elem, "wdata"))
                                cyclix_gen.assign(
                                    rob,
                                    hw_fractions(hw_fraction_V(rob_iter.iter_num), hw_fraction_SubStruct("rs1_ready")),
                                    1)
                            }; cyclix_gen.endif()

                        }; cyclix_gen.endif()
                    }; cyclix_gen.endwhile()
                }; cyclix_gen.endif()
            }; cyclix_gen.endwhile()
        }

        cyclix_gen.end()
        MSG(DEBUG_FLAG, "Translating to cyclix: complete")
        return cyclix_gen
    }
}