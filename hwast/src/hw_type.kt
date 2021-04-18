/*
 * hw_type.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

enum class DATA_TYPE {
    BV_SIGNED,          // signed bit vector
    BV_UNSIGNED,        // unsigned bit vector
    FP_SINGLE,          // floating-point (single precision)
    FP_DOUBLE,          // floating-point (double precision)
    STRUCTURED
}

// container for generic hardware types
data class hw_type(var DataType : DATA_TYPE, var src_struct: hw_struct, var dimensions : hw_dim_static) {
    constructor(VarType: DATA_TYPE, dimensions : hw_dim_static) : this(VarType, DUMMY_STRUCT, dimensions) {
        if (VarType == DATA_TYPE.STRUCTURED) ERROR("hw_type incorrect!")
    }
    constructor(VarType: DATA_TYPE, msb: Int, lsb: Int) : this(VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb)) {
        if (VarType == DATA_TYPE.STRUCTURED) ERROR("hw_type incorrect!")
    }
    constructor(VarType: DATA_TYPE, defval: String) : this(VarType, DUMMY_STRUCT, hw_dim_static(defval)) {
        if (VarType == DATA_TYPE.STRUCTURED) ERROR("hw_type incorrect!")
    }
    constructor(src_struct : hw_struct, dimensions : hw_dim_static) : this(DATA_TYPE.STRUCTURED, src_struct, dimensions)
    constructor(src_struct : hw_struct, msb: Int, lsb: Int) : this(DATA_TYPE.STRUCTURED, src_struct, hw_dim_static(msb, lsb))
    constructor(src_struct : hw_struct) : this(DATA_TYPE.STRUCTURED, src_struct, hw_dim_static())

    fun Print() {
        if (DataType == DATA_TYPE.STRUCTURED) println("Vartype: structured, src_struct: " + src_struct.name)
        else println("Vartype: unstructured")
        dimensions.Print()
    }
}
