package pipex

import hwast.*

val OP_MCOPIPE_REQ  = hw_opcode("mcopipe_req")
val OP_MCOPIPE_RESP = hw_opcode("mcopipe_resp")
val OP_SCOPIPE_REQ  = hw_opcode("scopipe_req")
val OP_SCOPIPE_RESP = hw_opcode("scopipe_resp")

class hw_exec_mcopipe_req(mcopipe_if_in : hw_mcopipe_if, mcopipe_handle_in : hw_mcopipe_handle) : hw_exec(OP_MCOPIPE_REQ) {
    var mcopipe_if = mcopipe_if_in
    var mcopipe_handle = mcopipe_handle_in
}

class hw_exec_mcopipe_resp(mcopipe_handle_in : hw_mcopipe_handle) : hw_exec(OP_MCOPIPE_RESP) {
    var mcopipe_handle = mcopipe_handle_in
}

class hw_exec_scopipe_req(scopipe_if_in : hw_scopipe_if, scopipe_handle_in : hw_scopipe_handle) : hw_exec(OP_SCOPIPE_REQ) {
    var scopipe_if = scopipe_if_in
    var scopipe_handle = scopipe_handle_in
}

class hw_exec_scopipe_resp(scopipe_handle_in : hw_scopipe_handle) : hw_exec(OP_SCOPIPE_RESP) {
    var scopipe_handle = scopipe_handle_in
}

open class hw_copipe(name_in: String,
                    wdata_VarType_in: VAR_TYPE,
                    wdata_src_struct_in: hw_struct,
                    wdata_dim_in: hw_dim_static,
                    rdata_VarType_in: VAR_TYPE,
                    rdata_src_struct_in: hw_struct,
                    rdata_dim_in: hw_dim_static) {

    var name = name_in
    var wdata_VarType = wdata_VarType_in
    var wdata_src_struct = wdata_src_struct_in
    var wdata_dim = wdata_dim_in
    var rdata_VarType = rdata_VarType_in
    var rdata_src_struct = rdata_src_struct_in
    var rdata_dim = rdata_dim_in
}

class hw_mcopipe_if(pipeline_in: pipeline,
                name_in: String,
                wdata_VarType_in: VAR_TYPE,
                wdata_src_struct_in: hw_struct,
                wdata_dim_in: hw_dim_static,
                rdata_VarType_in: VAR_TYPE,
                rdata_src_struct_in: hw_struct,
                rdata_dim_in: hw_dim_static)
    : hw_copipe(name_in,
        wdata_VarType_in,
        wdata_src_struct_in,
        wdata_dim_in,
        rdata_VarType_in,
        rdata_src_struct_in,
        rdata_dim_in) {

    val pipeline = pipeline_in

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

class hw_mcopipe_handle(pipeline_in: pipeline,
                        name_in: String,
                        wdata_VarType_in: VAR_TYPE,
                        wdata_src_struct_in: hw_struct,
                        wdata_dim_in: hw_dim_static,
                        rdata_VarType_in: VAR_TYPE,
                        rdata_src_struct_in: hw_struct,
                        rdata_dim_in: hw_dim_static)
            : hw_copipe(name_in,
            wdata_VarType_in,
            wdata_src_struct_in,
            wdata_dim_in,
            rdata_VarType_in,
            rdata_src_struct_in,
            rdata_dim_in) {

    val pipeline = pipeline_in

    constructor(mcopipe_if : hw_mcopipe_if)
            : this(mcopipe_if.pipeline,
                mcopipe_if.name,
                mcopipe_if.wdata_VarType,
                mcopipe_if.wdata_src_struct,
                mcopipe_if.wdata_dim,
                mcopipe_if.rdata_VarType,
                mcopipe_if.rdata_src_struct,
                mcopipe_if.rdata_dim)

    fun resp(rdata : hw_var) : hw_var {
        return pipeline.mcopipe_resp(this, rdata)
    }
}

class hw_scopipe_if(pipeline_in: pipeline,
                    name_in: String,
                    wdata_VarType_in: VAR_TYPE,
                    wdata_src_struct_in: hw_struct,
                    wdata_dim_in: hw_dim_static,
                    rdata_VarType_in: VAR_TYPE,
                    rdata_src_struct_in: hw_struct,
                    rdata_dim_in: hw_dim_static)
    : hw_copipe(name_in,
    wdata_VarType_in,
    wdata_src_struct_in,
    wdata_dim_in,
    rdata_VarType_in,
    rdata_src_struct_in,
    rdata_dim_in) {

    val pipeline = pipeline_in

    fun req(scopipe_handle : hw_scopipe_handle, cmd : hw_var, wdata : hw_var) : hw_var {
        return pipeline.scopipe_req(this, scopipe_handle, cmd, wdata)
    }
}

class hw_scopipe_handle(pipeline_in: pipeline,
                        name_in: String,
                        wdata_VarType_in: VAR_TYPE,
                        wdata_src_struct_in: hw_struct,
                        wdata_dim_in: hw_dim_static,
                        rdata_VarType_in: VAR_TYPE,
                        rdata_src_struct_in: hw_struct,
                        rdata_dim_in: hw_dim_static)
                : hw_copipe(name_in,
                wdata_VarType_in,
                wdata_src_struct_in,
                wdata_dim_in,
                rdata_VarType_in,
                rdata_src_struct_in,
                rdata_dim_in) {

    val pipeline = pipeline_in

    constructor(scopipe_if : hw_scopipe_if)
            : this(scopipe_if.pipeline,
                scopipe_if.name,
                scopipe_if.wdata_VarType,
                scopipe_if.wdata_src_struct,
                scopipe_if.wdata_dim,
                scopipe_if.rdata_VarType,
                scopipe_if.rdata_src_struct,
                scopipe_if.rdata_dim)

    fun resp(rdata : hw_param) : hw_var {
        return pipeline.scopipe_resp(this, rdata)
    }
}
