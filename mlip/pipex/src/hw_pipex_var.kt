/*
 * hw_pipex_var.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

import hwast.*

open class hw_pipex_var(name_in : String, VarType : VAR_TYPE, src_struct: hw_struct, dimensions : hw_dim_static, defval_in : String) : hw_var(name_in, VarType, src_struct, dimensions, defval_in) {

    constructor(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String)
            : this(name, VarType, DUMMY_STRUCT, dimensions, defval)

    constructor(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb), defval)

    constructor(name: String, VarType: VAR_TYPE, defval: String)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(defval), defval)

    constructor(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, dimensions, "0")

    constructor(name: String, src_struct_in: hw_struct, msb: Int, lsb: Int)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb), "0")

    constructor(name: String, src_struct_in: hw_struct)
            : this(name, src_struct_in, 0, 0)


    fun assign_succ(depow_fractions: hw_fractions, src: hw_param) {
        if (default_astc is pipeline) (default_astc as pipeline).assign_succ(depow_fractions, this, src)
        else ERROR("assign_succ cmd is out of place!")
    }

    fun assign_succ(depow_fractions: hw_fractions, src: Int) {
        if (default_astc is pipeline) (default_astc as pipeline).assign_succ(depow_fractions, this, src)
        else ERROR("assign_succ cmd is out of place!")
    }

    fun assign_succ(src: hw_param) {
        if (default_astc is pipeline) (default_astc as pipeline).assign_succ(this, src)
        else ERROR("assign_succ cmd is out of place!")
    }

    fun assign_succ(src: Int) {
        if (default_astc is pipeline) (default_astc as pipeline).assign_succ(this, src)
        else ERROR("assign_succ cmd is out of place!")
    }

    fun accum(depow_fractions: hw_fractions, src: hw_param) {
        if (default_astc is pipeline) (default_astc as pipeline).accum(depow_fractions, this, src)
        else ERROR("accum cmd is out of place!")
    }

    fun accum(depow_fractions: hw_fractions, src: Int) {
        if (default_astc is pipeline) (default_astc as pipeline).accum(depow_fractions, this, src)
        else ERROR("accum cmd is out of place!")
    }

    fun accum(src: hw_param) {
        if (default_astc is pipeline) (default_astc as pipeline).accum(this, src)
        else ERROR("accum cmd is out of place!")
    }

    fun accum(src: Int) {
        if (default_astc is pipeline) (default_astc as pipeline).accum(this, src)
        else ERROR("accum cmd is out of place!")
    }

    fun readprev() : hw_pipex_var {
        if (default_astc is pipeline) return (default_astc as pipeline).readprev(this)
        else ERROR("readprev cmd is out of place!")
        return this
    }
}

class hw_local(name_in : String, VarType : VAR_TYPE, src_struct: hw_struct, dimensions : hw_dim_static, defval_in : String) : hw_pipex_var(name_in, VarType, src_struct, dimensions, defval_in) {

    constructor(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String)
            : this(name, VarType, DUMMY_STRUCT, dimensions, defval)

    constructor(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb), defval)

    constructor(name: String, VarType: VAR_TYPE, defval: String)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(defval), defval)

    constructor(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, dimensions, "0")

    constructor(name: String, src_struct_in: hw_struct, msb: Int, lsb: Int)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb), "0")

    constructor(name: String, src_struct_in: hw_struct)
            : this(name, src_struct_in, 0, 0)
}

class hw_local_sticky(name_in : String, VarType : VAR_TYPE, src_struct: hw_struct, dimensions : hw_dim_static, defval_in : String) : hw_pipex_var(name_in, VarType, src_struct, dimensions, defval_in) {

    constructor(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String)
            : this(name, VarType, DUMMY_STRUCT, dimensions, defval)

    constructor(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb), defval)

    constructor(name: String, VarType: VAR_TYPE, defval: String)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(defval), defval)

    constructor(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, dimensions, "0")

    constructor(name: String, src_struct_in: hw_struct, msb: Int, lsb: Int)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb), "0")

    constructor(name: String, src_struct_in: hw_struct)
            : this(name, src_struct_in, 0, 0)
}

class hw_global(name_in : String, VarType : VAR_TYPE, src_struct: hw_struct, dimensions : hw_dim_static, defval_in : String) : hw_pipex_var(name_in, VarType, src_struct, dimensions, defval_in) {

    constructor(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, defval: String)
            : this(name, VarType, DUMMY_STRUCT, dimensions, defval)

    constructor(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb), defval)

    constructor(name: String, VarType: VAR_TYPE, defval: String)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(defval), defval)

    constructor(name: String, src_struct_in: hw_struct, dimensions: hw_dim_static)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, dimensions, "0")

    constructor(name: String, src_struct_in: hw_struct, msb: Int, lsb: Int)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb), "0")

    constructor(name: String, src_struct_in: hw_struct)
            : this(name, src_struct_in, 0, 0)
}
