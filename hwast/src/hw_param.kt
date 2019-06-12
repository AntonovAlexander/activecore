/*
 * hw_param.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

enum class PARAM_TYPE {
    VAR, VAL
}

open class hw_param (val type : PARAM_TYPE, vartype_in : hw_type, var token_printable : String) {

    var vartype = vartype_in

    open fun GetString(): String {
        return token_printable
    }

    fun GetDimensions(): hw_dim_static {
        return vartype.dimensions
    }

    fun isDimSingle(): Boolean {
        return vartype.dimensions.isSingle()
    }
}