package ariele

import hwast.*
import cyclix.STAGE_FC_MODE
import pipex.*

class slave_pipe(name        : String,
                 num_masters    : Int,
                 req_vartype    : hw_type,
                 resp_vartype   : hw_type,
                 rob_size : Int) : pipex.Pipeline(name, PIPELINE_FC_MODE.STALLABLE) {

    var master_ifs      = ArrayList<hw_scopipe_if>()
    var master_handle   = scopipe_handle("master", req_vartype, resp_vartype)

    var slave_if        = mcopipe_if("slave", req_vartype, resp_vartype, 4)
    var slave_handle    = mcopipe_handle(slave_if)

    var mreq_we         = ulocal("mreq_we", 0, 0, "0")
    var mreq_wdata      =  local("mreq_wdata", req_vartype, "0")

    var rr_arb          = uglobal("rr_arb", (GetWidthToContain(num_masters)-1), 0, "0")
    var rdata           = local("rdata", resp_vartype, "0")

    var mcmd_accepted   = ulocal("mcmd_accepted", 0, 0, "0")
    var scmd_accepted   = ulocal("scmd_accepted", 0, 0, "0")

    init {

        for (mnum in 0 until num_masters) {
            master_ifs.add(scopipe_if("master" + mnum, req_vartype, resp_vartype))
        }

        var ARB     = stage_handler("ARB", STAGE_FC_MODE.BUFFERED)
        var REQ     = stage_handler("REQ", STAGE_FC_MODE.BUFFERED)
        var RESP    = stage_handler("RESP", STAGE_FC_MODE.BUFFERED, rob_size)

        ARB.begin()
        run {

            clrif()
            for (mnum in 0 until num_masters) {
                begelsif(eq2(rr_arb, mnum))
                run {
                    clrif()
                    for (mnum_internal in mnum until (mnum + num_masters)) {
                        var mnum_rr = mnum_internal
                        if (mnum_rr >= num_masters) mnum_rr -= num_masters
                        var mnum_rr_next = mnum_rr + 1
                        if (mnum_rr_next >= num_masters) mnum_rr_next -= num_masters

                        begif(!mcmd_accepted)
                        run {
                            mcmd_accepted.accum(master_ifs[mnum_rr].req(master_handle, mreq_we, mreq_wdata))
                            mreq_we.accum(mreq_we)
                            mreq_wdata.accum(mreq_wdata)
                            begif(mcmd_accepted)
                            run {
                                rr_arb.assign(mnum_rr_next)
                            }; endif()
                        }; endif()
                    }
                }; endif()
            }

            begif(!mcmd_accepted)
            run {
                pstall()
            }; endif()

        }; endstage()

        REQ.begin()
        run {

            // sending command
            begif(!scmd_accepted)
            run {
                scmd_accepted.accum(slave_if.req(slave_handle, mreq_we, mreq_wdata))
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

        RESP.begin()
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
