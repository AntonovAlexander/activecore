/*
 * hw_exec_unit.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

val OP_STAGE = hwast.hw_opcode("rexec_unit")

class hw_exec_unit(name_in : String, exu_num_in: Int, stage_num_in: Int, rs_num_in: Int, pipeline_in : multiexu) : hwast.hw_exec(OP_STAGE) {
    val name = name_in
    val exu_num = exu_num_in
    val stage_num = stage_num_in
    val rs_num = rs_num_in
    val pipeline = pipeline_in

    fun begin() {
        pipeline.begexu(this)
    }
}
