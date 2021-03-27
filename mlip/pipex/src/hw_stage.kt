/*
 * hw_stage.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

val OP_STAGE = hwast.hw_opcode("pstage")

enum class PSTAGE_BUF_SIZE_CFG_MODE {
    AUTO, EXACT
}

data class PSTAGE_BUF_SIZE_CFG(val cfg_mode : PSTAGE_BUF_SIZE_CFG_MODE, val SIZE : Int)

class hw_stage(val name : String, val busy_mode : PSTAGE_BUSY_MODE, val BUF_SIZE : PSTAGE_BUF_SIZE_CFG, val pipeline : Pipeline) : hwast.hw_exec(OP_STAGE) {

    constructor(name : String, busy_mode : PSTAGE_BUSY_MODE, pipeline : Pipeline) : this(name, busy_mode, PSTAGE_BUF_SIZE_CFG(PSTAGE_BUF_SIZE_CFG_MODE.AUTO, 0), pipeline)
    constructor(name : String, busy_mode : PSTAGE_BUSY_MODE, buf_size : Int, pipeline : Pipeline) : this(name, busy_mode, PSTAGE_BUF_SIZE_CFG(PSTAGE_BUF_SIZE_CFG_MODE.EXACT, buf_size), pipeline)

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
