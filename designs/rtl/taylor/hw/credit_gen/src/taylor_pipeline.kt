import hwast.*
import cyclix.*
import pipex.*

class taylor_pipeline() : pipex.Pipeline("taylor_pipeline", PIPELINE_FC_MODE.CREDIT_BASED) {

    val x               = ulocal("x", 15, 0, "0")
    val div6            = hw_imm(16, IMM_BASE_TYPE.HEX, "2a")
    val div120          = hw_imm(16, IMM_BASE_TYPE.HEX, "2")
    val term0           = ulocal("term0", 15, 0, "0")
    val term1           = ulocal("term1", 15, 0, "0")
    val term2           = ulocal("term2", 15, 0, "0")
    val pow2            = ulocal("pow2", 15, 0, "0")
    val pow3            = ulocal("pow3", 15, 0, "0")
    val pow5            = ulocal("pow5", 15, 0, "0")
    val term0_min_term1 = ulocal("term0_min_term1", 15, 0, "0")
    val y               = ulocal("y", 15, 0, "0")

    var datain_done     = ulocal("datain_done", 0, 0, "0")

    //// interfaces ////
    var ext_datain    = ufifo_in("ext_datain", 15, 0)
    var ext_dataout   = ufifo_out("ext_dataout", 15, 0)

    var ST_ACQ_POW2         = stage_handler("ST_ACQ_POW2", STAGE_FC_MODE.FALL_THROUGH)
    var ST_POW3             = stage_handler("ST_POW3", STAGE_FC_MODE.FALL_THROUGH)
    var ST_TERM1_POW5       = stage_handler("ST_TERM1_POW5", STAGE_FC_MODE.FALL_THROUGH)
    var ST_TERM2            = stage_handler("ST_TERM2", STAGE_FC_MODE.FALL_THROUGH)
    var ST_GENRESULT        = stage_handler("ST_GENRESULT", STAGE_FC_MODE.FALL_THROUGH)
    var ST_SENDRESULT       = stage_handler("ST_SENDRESULT", STAGE_FC_MODE.FALL_THROUGH)

    init {

        ST_ACQ_POW2.begin()
        run {
            begif(!datain_done)
            run {
                datain_done.accum(fifo_rd_unblk(ext_datain, x))
                x.accum(x)
            }; endif()
            begif(!datain_done)
            run {
                pstall()
            }; endif()
            term0.assign(x)
            pow2.assign(srl(mul(x, x), 8))
        }; endstage()

        ST_POW3.begin()
        run {
            pow3.assign(srl(mul(pow2, x), 8))
        }; endstage()

        ST_TERM1_POW5.begin()
        run {
            term1.assign(srl(mul(pow3, div6), 8))
            pow5.assign(srl(mul(pow2, pow3), 8))
        }; endstage()

        ST_TERM2.begin()
        run {
            term2.assign(srl(mul(pow5, div120), 8))
            term0_min_term1.assign(sub(term0, term1))
        }; endstage()

        ST_GENRESULT.begin()
        run {
            y.assign(add(term0_min_term1, term2))
        }; endstage()

        ST_SENDRESULT.begin()
        run {
            begif(!fifo_wr_unblk(ext_dataout, y))
            run {
                pstall()
            }; endif()
        }; endstage()
    }
}
