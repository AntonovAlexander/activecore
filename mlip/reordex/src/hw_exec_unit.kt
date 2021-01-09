/*
 * hw_exec_unit.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

val OP_STAGE = hwast.hw_opcode("rexec_unit")

class hw_exec_unit(name_in : String, exu_num_in: Int, stage_num_in: Int, multiexu_in : MultiExu) : hwast.hw_exec(OP_STAGE) {

    val name = name_in
    val exu_num = exu_num_in
    val stage_num = stage_num_in
    val multiexu = multiexu_in

    val exu_opcode = multiexu_in.uglobal(("genexu_" + name + "_exu_opcode"), 3, 0, "0")
    val rs0_rdata = multiexu_in.uglobal(("genexu_" + name + "_rs0_rdata"), 31, 0, "0")
    val rs1_rdata = multiexu_in.uglobal(("genexu_" + name + "_rs1_rdata"), 31, 0, "0")
    val rd_wdata = multiexu_in.uglobal(("genexu_" + name + "_rd_wdata"), 31, 0, "0")

    fun begin() {
        multiexu.begexu(this)
    }
}
