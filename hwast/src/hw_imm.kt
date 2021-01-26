/*
 * hw_imm.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

enum class IMM_BASE_TYPE {
    DEC, BIN, HEX
}

open class hw_imm (val dimensions : hw_dim_static, var base_type : IMM_BASE_TYPE, val imm_value : String) : hw_param(PARAM_TYPE.VAL, hw_type(VAR_TYPE.UNSIGNED, dimensions), imm_value)
{
    constructor(dimensions : hw_dim_static, imm_value : String) : this(dimensions, IMM_BASE_TYPE.DEC, imm_value)
    constructor(imm_value : Int) : this(hw_dim_static(imm_value.toString()), imm_value.toString())
    constructor(imm_value : String) : this(hw_dim_static(imm_value), imm_value)
    constructor(msb: Int, lsb: Int, imm_value : String) : this(hw_dim_static(msb, lsb), imm_value)
    constructor(width: Int, imm_value : String) : this(hw_dim_static(width), imm_value)

    fun toInt() : Int {
        return imm_value.toInt()
    }
}

class hw_imm_arr (dimensions : hw_dim_static) : hw_imm(dimensions, IMM_BASE_TYPE.DEC, "0") {

    var subimms = ArrayList<hw_imm>()

    fun AddSubImm(new_imm : hw_imm) {
        subimms.add(new_imm)
    }
}
