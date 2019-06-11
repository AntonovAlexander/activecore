package xbar_pipex

import hwast.*
import pipex.*

class slave_pipe(name_in        : String,
                 req_vartype    : hw_type,
                 resp_vartype   : hw_type,
                 start_addr     : Int,
                 addr_width     : Int) : pipex.pipeline(name_in) {

    var master_busreq_struct = add_if_struct(name + "_master_busreq_struct")

    var master_if = ArrayList<hw_scopipe_if>()
    var master_handle = scopipe_handle("master", req_vartype, resp_vartype)

    var slave_if = mcopipe_if("slave",
        hw_type(master_busreq_struct),
        resp_vartype)
    var slave_handle = mcopipe_handle("slave", req_vartype, resp_vartype)

    init {

        /*
        var rr_arbiter = ulocal("rs1_rdata", 31, 0, "0")

        var SARB = add_stage("SARB")
        SARB.begin()
        run {
            master_data_handle.req(mem_rdata)
        }; endstage()

        var SREQ = add_stage("SREQ")
        SREQ.begin()
        run {
            begif(!data_handle.resp(mem_rdata))
            run {
                pstall()
            }; endif()
        }; endstage()

        var SRESP = add_stage("SRESP")
        SRESP.begin()
        run {
            master_data_handle.resp(mem_rdata)
        }; endstage()
        */
    }
}
