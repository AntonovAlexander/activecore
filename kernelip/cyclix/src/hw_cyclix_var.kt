/*
 * hw_cyclix_var.kt
 *
 *  Created on: 03.09.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*

open class hw_local(name : String, vartype : hw_type, defimm : hw_imm) : hw_var(name, vartype, defimm) {

    constructor(name : String, vartype : hw_type, defval : String)
            : this(name, vartype, hw_imm(defval))

    constructor(name: String, VarType : DATA_TYPE, dimensions : hw_dim_static, defimm : hw_imm)
            : this(name, hw_type(VarType, dimensions), defimm)

    constructor(name: String, VarType : DATA_TYPE, dimensions : hw_dim_static, defval : String)
            : this(name, hw_type(VarType, dimensions), defval)

    constructor(name: String, VarType: DATA_TYPE, msb: Int, lsb: Int, defimm : hw_imm)
            : this(name, hw_type(VarType, msb, lsb), defimm)

    constructor(name: String, VarType: DATA_TYPE, msb: Int, lsb: Int, defval: String)
            : this(name, hw_type(VarType, msb, lsb), defval)

    constructor(name: String, VarType: DATA_TYPE, defimm : hw_imm)
            : this(name, hw_type(VarType, defimm.imm_value), defimm)

    constructor(name: String, VarType: DATA_TYPE, defval: String)
            : this(name, hw_type(VarType, defval), defval)

    constructor(name: String, src_struct: hw_struct, dimensions : hw_dim_static)
            : this(name, hw_type(src_struct, dimensions), "0")

    constructor(name: String, src_struct: hw_struct, msb: Int, lsb: Int)
            : this(name, hw_type(src_struct, msb, lsb), "0")

    constructor(name: String, src_struct: hw_struct)
            : this(name, hw_type(src_struct), "0")

    constructor(name : String, msb : Int, lsb : Int, defval : String)
            : this(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, hw_imm(defval))

}

open class hw_global(name : String, vartype : hw_type, defimm : hw_imm) : hw_var(name, vartype, defimm) {

    constructor(name : String, vartype : hw_type, defval : String)
            : this(name, vartype, hw_imm(defval))

    constructor(name: String, VarType : DATA_TYPE, dimensions : hw_dim_static, defimm : hw_imm)
            : this(name, hw_type(VarType, dimensions), defimm)

    constructor(name: String, VarType : DATA_TYPE, dimensions : hw_dim_static, defval : String)
            : this(name, hw_type(VarType, dimensions), defval)

    constructor(name: String, VarType: DATA_TYPE, msb: Int, lsb: Int, defimm : hw_imm)
            : this(name, hw_type(VarType, msb, lsb), defimm)

    constructor(name: String, VarType: DATA_TYPE, msb: Int, lsb: Int, defval: String)
            : this(name, hw_type(VarType, msb, lsb), defval)

    constructor(name: String, VarType: DATA_TYPE, defimm : hw_imm)
            : this(name, hw_type(VarType, defimm.imm_value), defimm)

    constructor(name: String, VarType: DATA_TYPE, defval: String)
            : this(name, hw_type(VarType, defval), defval)

    constructor(name: String, src_struct: hw_struct, dimensions : hw_dim_static)
            : this(name, hw_type(src_struct, dimensions), "0")

    constructor(name: String, src_struct: hw_struct, msb: Int, lsb: Int)
            : this(name, hw_type(src_struct, msb, lsb), "0")

    constructor(name: String, src_struct: hw_struct)
            : this(name, hw_type(src_struct), "0")

    constructor(name : String, msb : Int, lsb : Int, defval : String)
            : this(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, hw_imm(defval))

    fun readPrev() : hw_local {
        if (default_astc is Generic) return (default_astc as Generic).readPrev(this)
        else ERROR("readPrev cmd is out of place!")
        return DUMMY_VAR as hw_local
    }

}

