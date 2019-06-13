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

data class hw_type(var VarType : VAR_TYPE, var src_struct: hw_struct, var dimensions : hw_dim_static) {
    constructor(VarType: VAR_TYPE, dimensions_in : hw_dim_static) : this(VarType, DUMMY_STRUCT, dimensions_in) {
        if (VarType == VAR_TYPE.STRUCTURED) ERROR("hw_type incorrect!")
    }
    constructor(VarType: VAR_TYPE, msb: Int, lsb: Int) : this(VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb)) {
        if (VarType == VAR_TYPE.STRUCTURED) ERROR("hw_type incorrect!")
    }
    constructor(VarType: VAR_TYPE, defval: String) : this(VarType, DUMMY_STRUCT, hw_dim_static(defval)) {
        if (VarType == VAR_TYPE.STRUCTURED) ERROR("hw_type incorrect!")
    }
    constructor(src_struct_in : hw_struct, dimensions_in : hw_dim_static) : this(VAR_TYPE.STRUCTURED, src_struct_in, dimensions_in)
    constructor(src_struct_in : hw_struct, msb: Int, lsb: Int) : this(VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb))
    constructor(src_struct_in : hw_struct) : this(VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(0, 0))

    fun Print() {
        if (VarType == VAR_TYPE.STRUCTURED) println("Vartype: structured, src_struct: " + src_struct.name)
        else println("Vartype: unstructured")
    }
}

open class hw_structvar(name_in: String, vartype_in : hw_type, defval_in : String) : hw_param(PARAM_TYPE.VAR, vartype_in, defval_in) {

    var defval = defval_in
    var name = name_in

    override fun GetString(): String {
        return name
    }

    constructor(name_in: String, VarType_in : VAR_TYPE, dimensions_in : hw_dim_static, defval : String) : this(name_in, hw_type(VarType_in, dimensions_in), defval)
    constructor(name_in: String, VarType_in : VAR_TYPE, msb: Int, lsb: Int, defval : String) : this(name_in, hw_type(VarType_in, msb, lsb), defval)
    constructor(name_in: String, VarType_in : VAR_TYPE, defval : String) : this(name_in, hw_type(VarType_in, hw_dim_static(defval)), defval)
    constructor(name_in: String, src_struct_in : hw_struct) : this(name_in, hw_type(src_struct_in), "0")
}

class hw_struct(name_in: String) : ArrayList<hw_structvar>() {
    var name = name_in

    fun add(name_in: String, vartype_in : hw_type, defval : String) {
        add(hw_structvar(name_in, vartype_in, defval))
    }

    fun add(name_in: String, VarType_in : VAR_TYPE, dimensions : hw_dim_static, defval : String) {
        add(hw_structvar(name_in, VarType_in, dimensions, defval))
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
        add(hw_structvar(name_in, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval))
    }

    fun addu(name_in: String, msb: Int, lsb: Int, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.UNSIGNED, msb, lsb, defval))
    }

    fun addu(name_in: String, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.UNSIGNED, defval))
    }

    fun adds(name_in: String, dimensions : hw_dim_static, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.SIGNED, dimensions, defval))
    }

    fun adds(name_in: String, msb: Int, lsb: Int, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.SIGNED, msb, lsb, defval))
    }

    fun adds(name_in: String, defval : String) {
        add(hw_structvar(name_in, VAR_TYPE.SIGNED, defval))
    }
}
