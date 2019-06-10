package xbar_pipex

import hwast.*
import pipex.*

class master_pipe(name: String,
                  req_vartype: hw_type,
                  map: addr_map,
                  resp_vartype: hw_type) : pipex.pipeline(name) {

    var master_busreq_struct = add_if_struct(name + "_master_busreq_struct")

    var master_if = scopipe_if("master",
        hw_type(master_busreq_struct),
        resp_vartype)
    var master_handle = scopipe_handle(master_if)

    var slave_ifs = ArrayList<hw_mcopipe_if>()
    var slave_handle = mcopipe_handle("slave", req_vartype, resp_vartype)

    var cmd     = ulocal("cmd", 0, 0, "0")
    var wdata   =  local("wdata", req_vartype,"0")

    init {

        master_busreq_struct.addu("addr",     31, 0, "0")
        master_busreq_struct.addu("be",       3,  0, "0")
        master_busreq_struct.add("wdata", req_vartype, "0")

        for (slave in map) {
            slave_ifs.add(mcopipe_if("slave" + map.indexOf(slave), req_vartype, resp_vartype))
        }

        var slave_rdata = ulocal("rs1_rdata", 31, 0, "0")

        var MDEC = add_stage("MDEC")
        MDEC.begin()
        run {
            begif(!master_if.req(master_handle, cmd, wdata))
            run {
                pstall()
            }; endif()
        }; endstage()

        var MSEQ = add_stage("MSEQ")
        MSEQ.begin()
        run {
            begif(!data_handle.resp(mem_rdata))
            run {
                pstall()
            }; endif()
        }; endstage()

        var MRESP = add_stage("MRESP")
        MRESP.begin()
        run {
            master_data_handle.resp(mem_rdata)
        }; endstage()
    }
}

class slave_pipe(name_in : String) : pipex.pipeline(name_in) {

    init {

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
    }
}

class xbar(name_in : String,
           num_masters_in : Int,
           req_vartype_in: hw_type,
           map_in : addr_map,
           resp_vartype_in: hw_type) : rtl.module(name_in) {

    var num_masters = num_masters_in
    var map         = map_in

    var master_pipes    = ArrayList<master_pipe>()
    var slave_pipes     = ArrayList<slave_pipe>()

    init {
        for (num_master in 0 until num_masters_in) {
            master_pipes.add(master_pipe(name_in, map_in))
        }
        for (num_slave in 0 until map.size) {
            slave_pipes.add(slave_pipe(name_in))
        }
        // TODO: connecting submodules
    }
}