package xbar_pipex

import hwast.*
import pipex.*

class master_pipe(name          : String,
                  req_vartype   : hw_type,
                  map           : addr_map,
                  resp_vartype  : hw_type) : pipex.pipeline(name) {

    var master_if = scopipe_if("master", req_vartype, resp_vartype)
    var master_handle = scopipe_handle(master_if)

    var slave_ifs = ArrayList<hw_mcopipe_if>()
    var slave_handle = mcopipe_handle("slave", req_vartype, resp_vartype)


    var mreq_we         = ulocal("mreq_we", 0, 0, "0")
    var mreq_wdata      =  local("mreq_wdata", req_vartype, "0")

    var slave_enb       = ArrayList<hw_var>()
    var rdata           = local("rdata", resp_vartype, "0")

    var mcmd_accepted   = ulocal("mcmd_accepted", 0, 0, "0")
    var scmd_accepted   = ulocal("scmd_accepted", 0, 0, "0")


    init {

        for (slave in map) {
            slave_ifs.add(mcopipe_if("slave" + map.indexOf(slave), req_vartype, resp_vartype))
            slave_enb.add(ulocal("slave_enb" + map.indexOf(slave), 0, 0, "0"))
        }

        var DEC    = add_stage("DEC")
        var REQ    = add_stage("REQ")
        var SEQ    = add_stage("SEQ")
        var RESP   = add_stage("RESP")

        DEC.begin()
        run {

            // accepting command
            begif(!mcmd_accepted)
            run {
                begif(master_if.req(master_handle, mreq_we, mreq_wdata))
                run {
                    mcmd_accepted.accum(1)
                    mreq_we.accum(mreq_we)
                    mreq_wdata.accum(mreq_wdata)
                }; endif()
                begelse()
                run {
                    pstall()
                }; endif()
            }; endif()

            // fetching address from command
            var addr = (subStruct(mreq_wdata, "addr"))

            // decoding command
            for (slave in map) {

                begif(land( geq(addr, hw_imm(slave.start_addr)), (leq(addr, (add(hw_imm(slave.start_addr), (1.shl(slave.addr_width))-1) )) ) ))
                run {
                    slave_enb[map.indexOf(slave)].assign(1)
                }; endif()
            }
        }; endstage()

        REQ.begin()
        run {
            // sending command
            begif(!scmd_accepted)
            run {
                for (slave in map) {
                    begif(slave_enb[map.indexOf(slave)])
                    run {
                        begif(slave_ifs[map.indexOf(slave)].req(slave_handle, mreq_we, mreq_wdata))
                        run {
                            scmd_accepted.accum(1)
                        }; endif()
                        begelse()
                        run {
                            pstall()
                        }; endif()
                    }; endif()
                }
            }; endif()
            
            begif(!mreq_we)
            run {
                pkill()
            }; endif()
        }; endstage()

        SEQ.begin()
        run {
            begif(!slave_handle.resp(rdata))
            run {
                pstall()
            }; endif()
        }; endstage()

        RESP.begin()
        run {
            master_handle.resp(rdata)
        }; endstage()
    }
}
