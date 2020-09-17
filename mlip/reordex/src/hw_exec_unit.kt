/*
 * hw_exec_unit.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

val OP_STAGE = hwast.hw_opcode("rexec_unit")

class hw_exec_unit(name_in : String, pipeline_in : multipipeline) : hwast.hw_exec(OP_STAGE) {
    val name = name_in
    val pipeline = pipeline_in

    fun begin() {
        pipeline.begstage(this)
    }
}
