package xbar_pipex

import hwast.*
import pipex.*

class master_pipe(name          : String,
                  addr_width    : Int,
                  be_width      : Int,
                  req_vartype   : hw_type,
                  map           : addr_map,
                  resp_vartype  : hw_type) : pipex.pipeline(name) {

    var busreq_struct = add_if_struct(name + "_busreq_struct")

    var master_if = scopipe_if("master",
        hw_type(busreq_struct),
        resp_vartype)
    var master_handle = scopipe_handle(master_if)

    var slave_ifs = ArrayList<hw_mcopipe_if>()
    var slave_handle = mcopipe_handle("slave",
        hw_type(busreq_struct),
        resp_vartype)


    var mreq_we         = ulocal("mreq_we", 0, 0, "0")
    var mreq_wdata      =  local("mreq_wdata", busreq_struct)

    var addr            = ulocal("addr", (addr_width-1), 0, "0")
    var be              = ulocal("be", (be_width-1), 0, "0")
    var wdata           =  local("wdata", req_vartype,"0")
    var slave_enb       = ArrayList<hw_var>()
    var rdata           = local("rdata", resp_vartype, "0")

    var mcmd_accepted   = ulocal("mcmd_accepted", 0, 0, "0")
    var scmd_accepted   = ulocal("scmd_accepted", 0, 0, "0")

    init {

        busreq_struct.addu("addr",     (addr_width-1), 0, "0")
        busreq_struct.addu("be",       (be_width-1),  0, "0")
        busreq_struct.add("wdata", req_vartype, "0")

        for (slave in map) {
            slave_ifs.add(mcopipe_if("slave" + map.indexOf(slave), hw_type(busreq_struct), resp_vartype))
            slave_enb.add(ulocal("slave_enb" + map.indexOf(slave), 0, 0, "0"))
        }

        var MDEC = add_stage("MDEC")
        MDEC.begin()
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

            // fetching data from command
            addr.assign (subStruct(mreq_wdata, "addr"))
            be.assign   (subStruct(mreq_wdata, "be"))
            wdata.assign(subStruct(mreq_wdata, "wdata"))

            // decoding command
            for (slave in map) {
                begif(land( geq(addr, hw_imm(slave.start_addr)), (less(addr, hw_imm(slave.start_addr + (1.shl(slave.addr_width))))) ) )
                run {
                    slave_enb[map.indexOf(slave)].assign(1)
                }; endif()
            }
        }; endstage()

        var MSEQ = add_stage("MSEQ")
        MSEQ.begin()
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

        var MRESP = add_stage("MRESP")
        MRESP.begin()
        run {
            begif(slave_handle.resp(rdata))
            run {
                master_handle.resp(rdata)
            }; endif()
            begelse()
            run {
                pstall()
            }; endif()
        }; endstage()
    }
}
