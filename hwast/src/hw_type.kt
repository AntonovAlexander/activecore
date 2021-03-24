/*
 * hw_type.kt
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
