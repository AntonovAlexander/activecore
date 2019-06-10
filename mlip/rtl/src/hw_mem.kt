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

class mem_source(sync_lvl_in : SYNC_LVL, sync_signal_in : hw_var, sync_src_in : hw_param) {
    val sync_lvl = sync_lvl_in
    val sync_signal = sync_signal_in
    val sync_src = sync_src_in
}

class hw_mem(name : String, vartype : hw_type, sync_type_in : SYNC_TYPE)
    : hw_var(name, vartype, "0") {

    val sync_type = sync_type_in

    var mem_srcs = ArrayList<mem_source>()
    fun AddSource(clk_lvl : SYNC_LVL, clk_signal : hw_var, clk_src : hw_param) {

        mem_srcs.add(mem_source(clk_lvl, clk_signal, clk_src))
        this.write_done = true
        clk_signal.read_done = true
        if (clk_src.type == PARAM_TYPE.VAR) (clk_src as hw_var).read_done = true
    }

    var rst_present = false
    var rst_type = RST_TYPE.SYNC
    var rst_lvl = SYNC_LVL.POS
    var rst_signal = hw_var("TEMP", VAR_TYPE.UNSIGNED, "0")
    var rst_src = hw_param(PARAM_TYPE.VAL, hw_type(VAR_TYPE.UNSIGNED, hw_dim_static(1)), "0")

    fun AddReset(rst_type_in : RST_TYPE, rst_lvl_in : SYNC_LVL, rst_signal_in : hw_var, rst_src_in : hw_param) {

        if (rst_present) ERROR("Only one reset in possible!")

        rst_type = rst_type_in
        rst_lvl = rst_lvl_in
        rst_signal = rst_signal_in
        rst_src = rst_src_in

        rst_present = true

        rst_signal.read_done = true
        if (rst_src.type == PARAM_TYPE.VAR) (rst_src as hw_var).read_done = true
    }
}