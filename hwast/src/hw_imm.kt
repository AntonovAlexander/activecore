/*
 * hw_imm.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

class hw_imm (val dimensions_in : hw_dim_static, val imm_value : String) : hw_param(PARAM_TYPE.VAL, hw_type(VAR_TYPE.UNSIGNED, dimensions_in), imm_value)
{
    constructor(imm_value : Int) : this(hw_dim_static(imm_value.toString()), imm_value.toString())
    constructor(imm_value : String) : this(hw_dim_static(imm_value), imm_value)
    constructor(msb: Int, lsb: Int, imm_value : String) : this(hw_dim_static(msb, lsb), imm_value)
    constructor(width: Int, imm_value : String) : this(hw_dim_static(width), imm_value)

    fun toInt() : Int {
        return imm_value.toInt()
    }
}