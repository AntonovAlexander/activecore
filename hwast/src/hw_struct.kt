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

// container for generic hardware types
data class hw_type(var VarType : VAR_TYPE, var src_struct: hw_struct, var dimensions : hw_dim_static) {
    constructor(VarType: VAR_TYPE, dimensions : hw_dim_static) : this(VarType, DUMMY_STRUCT, dimensions) {
        if (VarType == VAR_TYPE.STRUCTURED) ERROR("hw_type incorrect!")
    }
    constructor(VarType: VAR_TYPE, msb: Int, lsb: Int) : this(VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb)) {
        if (VarType == VAR_TYPE.STRUCTURED) ERROR("hw_type incorrect!")
    }
    constructor(VarType: VAR_TYPE, defval: String) : this(VarType, DUMMY_STRUCT, hw_dim_static(defval)) {
        if (VarType == VAR_TYPE.STRUCTURED) ERROR("hw_type incorrect!")
    }
    constructor(src_struct : hw_struct, dimensions : hw_dim_static) : this(VAR_TYPE.STRUCTURED, src_struct, dimensions)
    constructor(src_struct : hw_struct, msb: Int, lsb: Int) : this(VAR_TYPE.STRUCTURED, src_struct, hw_dim_static(msb, lsb))
    constructor(src_struct : hw_struct) : this(VAR_TYPE.STRUCTURED, src_struct, hw_dim_static(0, 0))

    fun Print() {
        if (VarType == VAR_TYPE.STRUCTURED) println("Vartype: structured, src_struct: " + src_struct.name)
        else println("Vartype: unstructured")
        dimensions.Print()
    }
}

// container for named variables
open class hw_structvar(var name: String, vartype : hw_type, var defimm : hw_imm) : hw_param(vartype, defimm.imm_value) {

    override fun GetString(): String {
        return name
    }

    constructor(name: String, vartype : hw_type, defval : String) : this(name, vartype, hw_imm(defval))
    constructor(name: String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String) : this(name, hw_type(VarType, dimensions), defval)
    constructor(name: String, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String) : this(name, hw_type(VarType, msb, lsb), defval)
    constructor(name: String, VarType : VAR_TYPE, defval : String) : this(name, hw_type(VarType, hw_dim_static(defval)), defval)
    constructor(name: String, src_struct : hw_struct) : this(name, hw_type(src_struct), "0")

    var defval = defimm.imm_value           // TODO: remove

    fun DisplayType() {
        print("#### Variable: " + name + " ####\n")
        vartype.Print()
        print("########################\n")
    }
}

// container for structs
class hw_struct(var name: String) : ArrayList<hw_structvar>() {
    
    var IsInInterface = false

    fun MarkStructInterface() {
        IsInInterface = true
        for (structvar in this) {
            if (structvar.vartype.VarType == VAR_TYPE.STRUCTURED) {
                structvar.vartype.src_struct.MarkStructInterface()
            }
        }
    }

    fun add(name: String, vartype : hw_type, defval : String) {
        add(hw_structvar(name, vartype, defval))
    }

    fun add(name: String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String) {
        add(hw_structvar(name, VarType, dimensions, defval))
    }

    fun add(name: String, VarType : VAR_TYPE, msb: Int, lsb: Int, defval : String) {
        add(hw_structvar(name, VarType, msb, lsb, defval))
    }

    fun add(name: String, VarType : VAR_TYPE, defval : String) {
        add(hw_structvar(name, VarType, defval))
    }

    fun add(name: String, src_struct : hw_struct) {
        add(hw_structvar(name, src_struct))
    }

    fun addu(name: String, dimensions : hw_dim_static, defval : String) {
        add(hw_structvar(name, hw_type(VAR_TYPE.UNSIGNED, dimensions), defval))
    }

    fun addu(name: String, msb: Int, lsb: Int, defval : String) {
        add(hw_structvar(name, VAR_TYPE.UNSIGNED, msb, lsb, defval))
    }

    fun addu(name: String, defval : String) {
        add(hw_structvar(name, VAR_TYPE.UNSIGNED, defval))
    }

    fun adds(name: String, dimensions : hw_dim_static, defval : String) {
        add(hw_structvar(name, VAR_TYPE.SIGNED, dimensions, defval))
    }

    fun adds(name: String, msb: Int, lsb: Int, defval : String) {
        add(hw_structvar(name, VAR_TYPE.SIGNED, msb, lsb, defval))
    }

    fun adds(name: String, defval : String) {
        add(hw_structvar(name, VAR_TYPE.SIGNED, defval))
    }
}
