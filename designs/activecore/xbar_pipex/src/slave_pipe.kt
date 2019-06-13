package xbar_pipex

import hwast.*
import pipex.*

class slave_pipe(name_in        : String,
                 num_masters    : Int,
                 addr_width     : Int,
                 be_width       : Int,
                 req_vartype    : hw_type,
                 resp_vartype   : hw_type) : pipex.pipeline(name_in) {

    var busreq_struct = add_if_struct(name + "_busreq_struct")

    var master_ifs = ArrayList<hw_scopipe_if>()
    var master_handle = scopipe_handle("master",
        hw_type(busreq_struct),
        resp_vartype)

    var slave_if = mcopipe_if("slave",
        hw_type(busreq_struct),
        resp_vartype)
    var slave_handle = mcopipe_handle(slave_if)

    var mreq_we       = ulocal("mreq_we", 0, 0, "0")
    var mreq_wdata    =  local("mreq_wdata", busreq_struct)

    var rr_arb          = ulocal("addr", (GetWidthToContain(num_masters)-1), 0, "0")
    var rdata           = local("rdata", resp_vartype, "0")

    var mcmd_accepted   = ulocal("mcmd_accepted", 0, 0, "0")
    var scmd_accepted   = ulocal("scmd_accepted", 0, 0, "0")

    init {

        busreq_struct.addu("addr",     (addr_width-1), 0, "0")
        busreq_struct.addu("be",       (be_width-1),  0, "0")
        busreq_struct.add("wdata", req_vartype, "0")

        for (mnum in 0 until num_masters) {
            master_ifs.add(scopipe_if("master" + mnum, hw_type(busreq_struct), resp_vartype))
        }

        var SARB = add_stage("SARB")
        var SREQ = add_stage("SREQ")
        var SRESP = add_stage("SRESP")

        SARB.begin()
        run {
            clrif()
            for (mnum in 0 until num_masters) {
                begelsif(eq2(rr_arb, mnum))
                run {
                    clrif()
                    for (mnum_internal in mnum until (mnum + num_masters)) {
                        var mnum_rr = mnum_internal
                        if (!(mnum_rr < num_masters)) mnum_rr -= num_masters
                        begelsif(eq2(rr_arb, mnum_rr))
                        run {
                            begif(!mcmd_accepted)
                            run {
                                begif(master_ifs[mnum_rr].req(master_handle, mreq_we, mreq_wdata))
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
                        }; endif()
                    }
                }; endif()
            }

        }; endstage()

        SREQ.begin()
        run {

            // sending command
            begif(!scmd_accepted)
            run {
                begif(slave_if.req(slave_handle, mreq_we, mreq_wdata))
                run {
                    scmd_accepted.accum(1)
                }; endif()
            }; endif()

            begif(!mreq_we)
            run {
                pkill()
            }; endif()
        }; endstage()

        SRESP.begin()
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
