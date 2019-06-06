/*
 * hw_struct.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

enum class VAR_TYPE {
    SIGNED, UNSIGNED, STRUCTURED
}

open class hw_structvar(name_in: String, VarType_in : VAR_TYPE, src_struct_in : hw_struct, dimensions : hw_dim_static, defval_in : String) : hw_param(PARAM_TYPE.VAR, dimensions, defval_in) {

    var defval = defval_in
    var name = name_in
    var VarType = VarType_in
    var src_struct = src_struct_in

    constructor(name_in: String, VarType_in : VAR_TYPE, msb: Int, lsb: Int, defval : String) : this(name_in, VarType_in, DUMMY_STRUCT, hw_dim_static(msb, lsb), defval)
    constructor(name_in: String, VarType_in : VAR_TYPE, defval : String) : this(name_in, VarType_in, DUMMY_STRUCT, hw_dim_static(defval), defval)
    constructor(name_in: String, src_struct_in : hw_struct) : this(name_in, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(0, 0), "0")
}

class hw_struct(name_in: String) : ArrayList<hw_structvar>() {
    var name = name_in

    fun add(name_in: String, VarType_in : VAR_TYPE, src_struct_in : hw_struct, dimensions : hw_dim_static, defval : String) {
        add(hw_structvar(name_in, VarType_in, src_struct_in, dimensions, defval))
    }

    fun add(name_in: String, VarType_in : VAR_TYPE, dimensions : hw_dim_static, defval : String) {
        add(hw_structvar(name_in, VarType_in, DUMMY_STRUCT, dimensions, defval))
    }

    fun add(name_in: String, VarType_in : VAR_TYPE, msb: Int, lsb: Int, defval : String) {
        add(hw_structvar(name_in, VarType_in, msb, lsb, defval))
    }

    fun add(name_in: String, VarType_in : VAR_TYPE, defval : String) {
        add(hw_structvar(name_in, VarType_in, defval))
    }

    fun add(name_in: String, src_struct_in : hw_struct) {
        add(hw_structvar(name_in, src_struct_in))
    }

    fun addu(name_in: String, dimensions : hw_dim_static, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.UNSIGNED, DUMMY_STRUCT, dimensions, defval))
    }

    fun addu(name_in: String, msb: Int, lsb: Int, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.UNSIGNED, msb, lsb, defval))
    }

    fun addu(name_in: String, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.UNSIGNED, defval))
    }

    fun adds(name_in: String, dimensions : hw_dim_static, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.SIGNED, DUMMY_STRUCT, dimensions, defval))
    }

    fun adds(name_in: String, msb: Int, lsb: Int, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.SIGNED, msb, lsb, defval))
    }

    fun adds(name_in: String, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.SIGNED, defval))
    }
}
