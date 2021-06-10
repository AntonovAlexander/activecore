/*
 * hw_copipe.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

import hwast.*

val OP_MCOPIPE_REQ  = hw_opcode("mcopipe_req")
val OP_MCOPIPE_RESP = hw_opcode("mcopipe_resp")
val OP_SCOPIPE_REQ  = hw_opcode("scopipe_req")
val OP_SCOPIPE_RESP = hw_opcode("scopipe_resp")

class hw_exec_mcopipe_req(var mcopipe_if : hw_mcopipe_if, var mcopipe_handle : hw_mcopipe_handle) : hw_exec(OP_MCOPIPE_REQ)

class hw_exec_mcopipe_resp(var mcopipe_handle : hw_mcopipe_handle) : hw_exec(OP_MCOPIPE_RESP)

class hw_exec_scopipe_req(var scopipe_if : hw_scopipe_if, var scopipe_handle : hw_scopipe_handle) : hw_exec(OP_SCOPIPE_REQ)

class hw_exec_scopipe_resp(var scopipe_handle : hw_scopipe_handle) : hw_exec(OP_SCOPIPE_RESP)

open class hw_copipe(var name: String,
                     var wdata_vartype: hw_type,
                     var rdata_vartype: hw_type)

class hw_mcopipe_if(val pipeline: Pipeline,
                    name: String,
                    wdata_vartype: hw_type,
                    rdata_vartype: hw_type,
                    var trx_id_width: Int)
    : hw_copipe(name,
                wdata_vartype,
                rdata_vartype) {

    fun req(mcopipe_handle : hw_mcopipe_handle, cmd : hw_param, wdata : hw_param) : hw_var {
        return pipeline.mcopipe_req(this, mcopipe_handle, cmd, wdata)
    }

    fun rdreq(mcopipe_handle : hw_mcopipe_handle, wdata : hw_param) : hw_var {
        return pipeline.mcopipe_req(this, mcopipe_handle, hw_imm(0, 0, "0"), wdata)
    }

    fun wrreq(mcopipe_handle : hw_mcopipe_handle, wdata : hw_param) : hw_var {
        return pipeline.mcopipe_req(this, mcopipe_handle, hw_imm(0, 0, "1"), wdata)
    }
}

class hw_mcopipe_handle(val pipeline: Pipeline,
                        name: String,
                        wdata_vartype: hw_type,
                        rdata_vartype: hw_type,
                        var trx_id_width: Int)
            : hw_copipe(name,
                        wdata_vartype,
                        rdata_vartype) {

    constructor(mcopipe_if : hw_mcopipe_if)
            : this(mcopipe_if.pipeline,
                mcopipe_if.name,
                mcopipe_if.wdata_vartype,
                mcopipe_if.rdata_vartype,
                mcopipe_if.trx_id_width)

    fun resp(rdata : hw_var) : hw_var {
        return pipeline.mcopipe_resp(this, rdata)
    }
}

class hw_scopipe_if(val pipeline: Pipeline,
                    name: String,
                    wdata_vartype: hw_type,
                    rdata_vartype: hw_type)
    : hw_copipe(name,
                wdata_vartype,
                rdata_vartype) {

    fun req(scopipe_handle : hw_scopipe_handle, cmd : hw_var, wdata : hw_var) : hw_var {
        return pipeline.scopipe_req(this, scopipe_handle, cmd, wdata)
    }
}

class hw_scopipe_handle(val pipeline: Pipeline,
                        name: String,
                        wdata_vartype: hw_type,
                        rdata_vartype: hw_type)
                : hw_copipe(name,
                            wdata_vartype,
                            rdata_vartype) {

    constructor(scopipe_if : hw_scopipe_if)
            : this(scopipe_if.pipeline,
                scopipe_if.name,
                scopipe_if.wdata_vartype,
                scopipe_if.rdata_vartype)

    fun resp(rdata : hw_param) : hw_var {
        return pipeline.scopipe_resp(this, rdata)
    }
}
