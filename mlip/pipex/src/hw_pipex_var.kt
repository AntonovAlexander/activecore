/*
 * hw_pipex_var.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

import hwast.*

open class hw_pipex_var(name_in : String, vartype : hw_type, defval_in : String) : hw_var(name_in, vartype, defval_in) {

    fun assign_succ(depow_fractions: hw_fractions, src: hw_param) {
        if (default_astc is pipeline) (default_astc as pipeline).assign_succ(depow_fractions, this, src)
        else ERROR("assign_succ cmd is out of place!")
    }

    fun assign_succ(depow_fractions: hw_fractions, src: Int) {
        if (default_astc is pipeline) (default_astc as pipeline).assign_succ(depow_fractions, this, src)
        else ERROR("assign_succ cmd is out of place!")
    }

    fun assign_succ(src: hw_param) {
        if (default_astc is pipeline) (default_astc as pipeline).assign_succ(this, src)
        else ERROR("assign_succ cmd is out of place!")
    }

    fun assign_succ(src: Int) {
        if (default_astc is pipeline) (default_astc as pipeline).assign_succ(this, src)
        else ERROR("assign_succ cmd is out of place!")
    }

    fun readprev() : hw_pipex_var {
        if (default_astc is pipeline) return (default_astc as pipeline).readprev(this)
        else ERROR("readprev cmd is out of place!")
        return this
    }
}

class hw_local(name_in : String, vartype : hw_type, defval_in : String)
    : hw_pipex_var(name_in, vartype, defval_in)

class hw_local_sticky(name_in : String, vartype : hw_type, defval_in : String)
    : hw_pipex_var(name_in, vartype, defval_in)

class hw_global(name_in : String, vartype : hw_type, defval_in : String)
    : hw_pipex_var(name_in, vartype, defval_in)
