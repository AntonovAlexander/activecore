/*
 * hw_mem.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package rtl

import hwast.*
import java.lang.Exception

enum class SYNC_TYPE {
    LVL,	EDGE
}

enum class SYNC_LVL {
    POS,    NEG
}

enum class RST_TYPE {
    SYNC,    ASYNC
}

class mem_source(val sync_lvl : SYNC_LVL, val sync_signal : hw_var, val sync_src : hw_param)

class hw_mem(name : String, vartype : hw_type, val sync_type : SYNC_TYPE)
    : hw_var(name, vartype, "0") {

    var mem_srcs = ArrayList<mem_source>()
    fun AddSource(clk_lvl : SYNC_LVL, clk_signal : hw_var, clk_src : hw_param) {

        mem_srcs.add(mem_source(clk_lvl, clk_signal, clk_src))
        this.write_done = true
        clk_signal.read_done = true
        if (clk_src is hw_var) clk_src.read_done = true
    }

    var rst_present = false
    var rst_type = RST_TYPE.SYNC
    var rst_lvl = SYNC_LVL.POS
    var rst_signal = hw_var("TEMP", DATA_TYPE.BV_UNSIGNED, "0")
    var rst_src = hw_param(hw_type(DATA_TYPE.BV_UNSIGNED, hw_dim_static(1)), "0")

    fun AddReset(rst_type_in : RST_TYPE, rst_lvl_in : SYNC_LVL, rst_signal_in : hw_var, rst_src_in : hw_param) {

        if (rst_present) ERROR("Only one reset in possible!")

        rst_type = rst_type_in
        rst_lvl = rst_lvl_in
        rst_signal = rst_signal_in
        rst_src = rst_src_in

        rst_present = true

        rst_signal.read_done = true
        if (rst_src is hw_var) (rst_src as hw_var).read_done = true
    }
}