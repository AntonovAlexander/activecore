/*
 * hw_param.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

open class hw_param (var vartype : hw_type, var token_printable : String) {

    open fun GetString(): String {
        return token_printable
    }

    fun GetDimensions(): hw_dim_static {
        return vartype.dimensions
    }

    fun isDimSingle(): Boolean {
        return vartype.dimensions.isSingle()
    }

    fun GetWidth() : Int {
        if (vartype.dimensions.size == 0) return 1;
        else return vartype.dimensions.last().GetWidth()
    }

    fun GetUnpackWidth() : Int {
        var ret_width = 0
        if (vartype.DataType == DATA_TYPE.STRUCTURED) {
            for (structvar in vartype.src_struct) {
                ret_width += structvar.GetUnpackWidth()
            }
        }
        if (ret_width == 0) ret_width = 1
        for (dim in vartype.dimensions) {
            ret_width *= dim.GetWidth()
        }
        return ret_width
    }
}