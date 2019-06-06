/*
 * hw_syncbuf.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package rtl

import hwast.*

abstract class hw_syncbuf(name : String, buftype : String, VarType : VAR_TYPE, src_struct: hw_struct, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var)
    : hw_var(name, VarType, src_struct, dimensions, defval) {

    var buf = hw_mem((buftype + "_" + name), VarType, src_struct, dimensions, SYNC_TYPE.EDGE)
    init {
        buf.AddSource(SYNC_LVL.POS, clk, this)
        buf.AddReset(RST_TYPE.SYNC, SYNC_LVL.POS, rst, hw_imm(defval))
    }

    constructor(name: String, buftype : String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String, clk : hw_var, rst : hw_var)
            : this(name, buftype, VarType, DUMMY_STRUCT, dimensions, defval, clk, rst)

    constructor(name: String, buftype : String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String, clk : hw_var, rst : hw_var)
            : this(name, buftype, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb), defval, clk, rst)

    constructor(name: String, buftype : String, src_struct_in: hw_struct, dimensions : hw_dim_static, clk : hw_var, rst : hw_var)
            : this(name, buftype, VAR_TYPE.STRUCTURED, src_struct_in, dimensions, "0", clk, rst)

    constructor(name: String, buftype : String, src_struct_in: hw_struct, msb: Int, lsb: Int, clk : hw_var, rst : hw_var)
            : this(name, buftype, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb), "0", clk, rst)

    constructor(name: String, buftype : String, src_struct_in: hw_struct, clk : hw_var, rst : hw_var)
            : this(name, buftype, src_struct_in, 0, 0, clk, rst)
}

class hw_buffered(name : String, VarType : VAR_TYPE, src_struct: hw_struct, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var)
    : hw_syncbuf(name, "genbuffered", VarType, src_struct, dimensions, defval, clk, rst) {

    constructor(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String, clk : hw_var, rst : hw_var)
            : this(name, VarType, DUMMY_STRUCT, dimensions, defval, clk, rst)

    constructor(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String, clk : hw_var, rst : hw_var)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb), defval, clk, rst)

    constructor(name: String, src_struct_in: hw_struct, dimensions : hw_dim_static, clk : hw_var, rst : hw_var)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, dimensions, "0", clk, rst)

    constructor(name: String, src_struct_in: hw_struct, msb: Int, lsb: Int, clk : hw_var, rst : hw_var)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb), "0", clk, rst)

    constructor(name: String, src_struct_in: hw_struct, clk : hw_var, rst : hw_var)
            : this(name, src_struct_in, 0, 0, clk, rst)
}

class hw_sticky(name : String, VarType : VAR_TYPE, src_struct: hw_struct, dimensions : hw_dim_static, defval : String, clk : hw_var, rst : hw_var)
    : hw_syncbuf(name, "gensticky", VarType, src_struct, dimensions, defval, clk, rst) {

    constructor(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String, clk : hw_var, rst : hw_var)
            : this(name, VarType, DUMMY_STRUCT, dimensions, defval, clk, rst)

    constructor(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String, clk : hw_var, rst : hw_var)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb), defval, clk, rst)

    constructor(name: String, src_struct_in: hw_struct, dimensions : hw_dim_static, clk : hw_var, rst : hw_var)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, dimensions, "0", clk, rst)

    constructor(name: String, src_struct_in: hw_struct, msb: Int, lsb: Int, clk : hw_var, rst : hw_var)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb), "0", clk, rst)

    constructor(name: String, src_struct_in: hw_struct, clk : hw_var, rst : hw_var)
            : this(name, src_struct_in, 0, 0, clk, rst)
}
