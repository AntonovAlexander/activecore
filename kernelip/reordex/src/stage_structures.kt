/*
 * stage_structures.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*
import cyclix.*

open class RISCDecoder_rs (var req : hw_var, var addr : hw_var, var rdata : hw_var)

open class RISCDecoder_rd (var req : hw_var, var source : hw_var, var addr : hw_var, var wdata : hw_var, var rdy : hw_var)
internal fun Fill_RISCDecoder_rds_StageVars(stage : hw_stage, amount : Int, rds : ArrayList<RISCDecoder_rd>, ARF_addr_width : Int) {
    for (rd_idx in 0 until amount) {
        rds.add(
            RISCDecoder_rd(
                stage.AdduStageVar("rd" + rd_idx + "_req", 0, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_source", 2, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_addr", ARF_addr_width-1, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_wdata", 31, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_rdy", 0, 0, "0")
            )
        )
    }
}

open class RISCDecoder_rs_ctrl (var rdy : hw_var, var tag : hw_var)
internal fun Fill_RISCDecoder_rss_StageVars(stage : hw_stage, amount : Int, rss : ArrayList<RISCDecoder_rs>, ARF_addr_width : Int, RF_width: Int) {
    for (rs_idx in 0 until amount) {
        rss.add(
            RISCDecoder_rs(
                stage.AdduStageVar("rs" + rs_idx + "_req", 0, 0, "0"),
                stage.AdduStageVar("rs" + rs_idx + "_addr", ARF_addr_width - 1, 0, "0"),
                stage.AdduStageVar("rs" + rs_idx + "_rdata", RF_width - 1, 0, "0")
            )
        )
    }
}

open class RISCDecoder_rd_ctrl (var tag : hw_var)
internal fun Fill_RISCDecoder_rds_ctrl_StageVars(stage : hw_stage, amount : Int, rds_ctrl : ArrayList<RISCDecoder_rd_ctrl>, PRF_addr_width : Int) {
    for (rd_idx in 0 until amount) {
        rds_ctrl.add(
            RISCDecoder_rd_ctrl(
                stage.AdduStageVar("rd" + rd_idx + "_tag", PRF_addr_width-1, 0, "0")
            )
        )
    }
}

open class ROB_rd_ctrl(tag : hw_var, var tag_prev_clr : hw_var, var tag_prev : hw_var) : RISCDecoder_rd_ctrl(tag)
internal fun Fill_ROB_rds_ctrl_StageVars(stage : hw_stage, amount : Int, rds_ctrl : ArrayList<ROB_rd_ctrl>, PRF_addr_width : Int) {
    for (rd_idx in 0 until amount) {
        rds_ctrl.add(
            ROB_rd_ctrl(
                stage.AdduStageVar("rd" + rd_idx + "_tag", PRF_addr_width-1, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_tag_prev_clr",   0, 0, "0"),
                stage.AdduStageVar("rd" + rd_idx + "_tag_prev",       PRF_addr_width-1, 0, "0")
            )
        )
    }
}

class RISCDecoder_memctrl   (var req : hw_var,
                             var we : hw_var,
                             var addr : hw_var,
                             var be : hw_var,
                             var wdata : hw_var,
                             var rdata : hw_var,
                             var rshift : hw_var,
                             var load_signext : hw_var
)

class Branchctrl(var req: hw_var,
                 var req_cond: hw_var,
                 var src: hw_var,
                 var vector: hw_var,
                 var mask: hw_var
)

class ALUStatus(var CF: hw_var,
                var SF: hw_var,
                var ZF: hw_var,
                var OF: hw_var
)

internal class iq_rd_ctrl (var req: hw_var, var tag: hw_var)

class Src(name : String, msb : Int, lsb : Int, defval : String) : hw_var(name, msb, lsb, defval)

data class __src_handle(val src_rdy : hw_var,
                        val src_tag : hw_var,
                        val src_src : hw_var,
                        val src_data : hw_var
)