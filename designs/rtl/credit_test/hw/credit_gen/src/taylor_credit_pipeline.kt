import hwast.*
import pipex.*

class taylor_credit_pipeline() : pipex.Pipeline("taylor_credit_pipeline", PIPELINE_CF_MODE.CREDIT_BASED) {

    val x = ulocal("x", 15, 0, "0")
    val div6 = hw_imm(16, IMM_BASE_TYPE.HEX, "2a")
    val div120 = hw_imm(16, IMM_BASE_TYPE.HEX, "2")
    val term0 = ulocal("term0", 15, 0, "0")
    val term1 = ulocal("term1", 15, 0, "0")
    val term2 = ulocal("term2", 15, 0, "0")
    val pow2 = ulocal("pow2", 15, 0, "0")
    val pow3 = ulocal("pow3", 15, 0, "0")
    val pow5 = ulocal("pow5", 15, 0, "0")
    val y = ulocal("y", 15, 0, "0")

    //// interfaces ////
    var ext_datain    = ufifo_in("ext_datain", 15, 0)
    var ext_dataout   = ufifo_out("ext_dataout", 15, 0)

    var ST_TERM1 = stage_handler("ST_TERM1", PSTAGE_BUSY_MODE.FALL_THROUGH)
    var ST_TERM2 = stage_handler("ST_TERM2", PSTAGE_BUSY_MODE.FALL_THROUGH)
    var ST_GENRESULT = stage_handler("ST_GENRESULT", PSTAGE_BUSY_MODE.FALL_THROUGH)
    var ST_SENDRESULT = stage_handler("ST_SENDRESULT", PSTAGE_BUSY_MODE.FALL_THROUGH, 4)

    init {

        ST_TERM1.begin()
        run {
            begif(!fifo_rd_unblk(ext_datain, x))
            run {
                pstall()
            }; endif()
            term0.assign(x)
            pow2.assign(srl(mul(x, x), 8))
            pow3.assign(srl(mul(pow2, x), 8))
            term1.assign(srl(mul(pow3, div6), 8))
        }; endstage()

        ST_TERM2.begin()
        run {
            pow5.assign(srl(mul(pow2, pow3), 8))
            term2.assign(srl(mul(pow5, div120), 8))
        }; endstage()

        ST_GENRESULT.begin()
        run {
            y.assign(add(sub(term0, term1), term2))
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
