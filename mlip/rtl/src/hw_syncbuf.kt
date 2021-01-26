/*
 * hw_syncbuf.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package rtl

import hwast.*

abstract class hw_syncbuf(name : String, buftype : String, vartype : hw_type, defimm : hw_imm, clk : hw_var, rst : hw_var)
    : hw_var(name, vartype, defimm) {

    constructor(name : String, buftype : String, vartype : hw_type, defval : String, clk : hw_var, rst : hw_var)
        : this(name, buftype, vartype, hw_imm(defval), clk, rst)

    var buf = hw_mem((buftype + "_" + name), vartype, SYNC_TYPE.EDGE)
    init {
        buf.AddSource(SYNC_LVL.POS, clk, this)
        buf.AddReset(RST_TYPE.SYNC, SYNC_LVL.POS, rst, defimm)
    }
}

class hw_buffered(name : String, vartype : hw_type, defimm : hw_imm, clk : hw_var, rst : hw_var)
    : hw_syncbuf(name, "genbuffered", vartype, defimm, clk, rst) {

    constructor(name : String, vartype : hw_type, defval : String, clk : hw_var, rst : hw_var)
        : this(name, vartype, hw_imm(defval), clk, rst)
}

class hw_sticky(name : String, vartype : hw_type, defimm : hw_imm, clk : hw_var, rst : hw_var)
    : hw_syncbuf(name, "gensticky", vartype, defimm, clk, rst) {

    constructor(name : String, vartype : hw_type, defval : String, clk : hw_var, rst : hw_var)
        : this(name, vartype, hw_imm(defval), clk, rst)
}
