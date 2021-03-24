import hwast.*
import pipex.*

class taylor_credit_pipeline() : pipex.Pipeline("test_credit", PIPELINE_CF_MODE.CREDIT_BASED) {

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
    var ext_data_if = scopipe_if("instr_mem",
        hw_type(VAR_TYPE.UNSIGNED, 31, 0),
        hw_type(VAR_TYPE.UNSIGNED, 31, 0))
    var ext_data_handle = scopipe_handle(ext_data_if)
    var ext_data_busreq = ulocal("ext_data_busreq", 31, 0, "0")
    var ext_data_req_done = ulocal_sticky("instr_req_done", 0, 0, "0")

    var ST_TERM1 = stage_handler("ST_TERM1", PSTAGE_MODE.FALL_THROUGH)
    var ST_TERM2 = stage_handler("ST_TERM2", PSTAGE_MODE.FALL_THROUGH)
    var ST_RESULT = stage_handler("ST_RESULT", PSTAGE_MODE.FALL_THROUGH)

    init {

        ST_TERM1.begin()
        run {
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

        ST_RESULT.begin()
        run {
            y.assign(add(sub(term0, term1), term2))
        }; endstage()
    }
}
