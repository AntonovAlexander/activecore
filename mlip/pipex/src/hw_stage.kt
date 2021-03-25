/*
 * hw_stage.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

val OP_STAGE = hwast.hw_opcode("pstage")

class hw_stage(val name : String, val mode : PSTAGE_BUSY_MODE, val BUF_SIZE : Int, val pipeline : Pipeline) : hwast.hw_exec(OP_STAGE) {

    fun begin() {
        pipeline.begstage(this)
    }

    fun readremote(remote_local: hw_pipex_var) : hwast.hw_var {
        return pipeline.readremote(this, remote_local)
    }

    fun isactive() : hwast.hw_var {
        return pipeline.isactive(this)
    }

    fun isworking() : hwast.hw_var {
        return pipeline.isworking(this)
    }

    fun isstalled() : hwast.hw_var {
        return pipeline.isstalled(this)
    }

    fun issucc() : hwast.hw_var {
        return pipeline.issucc(this)
    }
}
