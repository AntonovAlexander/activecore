package ariele

import hwast.*
import pipex.*

class master_pipe(name          : String,
                  req_vartype   : hw_type,
                  map           : addr_map,
                  resp_vartype  : hw_type) : pipex.pipeline(name) {

    var master_if = scopipe_if("master", req_vartype, resp_vartype)
    var master_handle = scopipe_handle(master_if)

    var slave_ifs = ArrayList<hw_mcopipe_if>()
    var slave_handle = mcopipe_handle("slave", req_vartype, resp_vartype, 4)


    var mreq_we         = ulocal_sticky("mreq_we", 0, 0, "0")
    var mreq_wdata      =  local_sticky("mreq_wdata", req_vartype, "0")

    var slave_enb       = ArrayList<hw_var>()
    var rdata           = local_sticky("rdata", resp_vartype, "0")

    var mcmd_accepted   = ulocal_sticky("mcmd_accepted", 0, 0, "0")
    var scmd_accepted   = ulocal_sticky("scmd_accepted", 0, 0, "0")


    init {

        for (slave in map) {
            slave_ifs.add(mcopipe_if("slave" + map.indexOf(slave), req_vartype, resp_vartype, 4))
            slave_enb.add(ulocal_sticky("slave_enb" + map.indexOf(slave), 0, 0, "0"))
        }

        var DEC    = stage_handler("DEC", PSTAGE_MODE.BUFFERED)
        var REQ    = stage_handler("REQ", PSTAGE_MODE.BUFFERED)
        var SEQ    = stage_handler("SEQ", PSTAGE_MODE.BUFFERED)
        var RESP   = stage_handler("RESP", PSTAGE_MODE.BUFFERED)

        DEC.begin()
        run {

            // accepting command
            begif(!mcmd_accepted)
            run {
                mcmd_accepted.assign(master_if.req(master_handle, mreq_we, mreq_wdata))
            }; endif()

            begif(!mcmd_accepted)
            run {
                pstall()
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
                        scmd_accepted.assign(slave_ifs[map.indexOf(slave)].req(slave_handle, mreq_we, mreq_wdata))
                    }; endif()
                }
            }; endif()

            begif(!scmd_accepted)
            run {
                pstall()
            }; endif()
            
            begif(mreq_we)
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
