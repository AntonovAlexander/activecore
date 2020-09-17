/*
 * hw_reordex_var.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package reordex

import hwast.*

open class hw_reordex_var(name_in : String, vartype : hw_type, defval_in : String) : hw_var(name_in, vartype, defval_in) {

}

class hw_local(name_in : String, vartype : hw_type, defval_in : String)
    : hw_reordex_var(name_in, vartype, defval_in)

class hw_local_sticky(name_in : String, vartype : hw_type, defval_in : String)
    : hw_reordex_var(name_in, vartype, defval_in)

class hw_global(name_in : String, vartype : hw_type, defval_in : String)
    : hw_reordex_var(name_in, vartype, defval_in)
