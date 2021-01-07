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

    fun begin() {
        multiexu.begexu(this)
    }
}
