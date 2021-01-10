/*
 * hw_port.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package rtl

import hwast.*

enum class PORT_DIR {
    IN,	OUT, INOUT
}

class hw_port(name : String, var port_dir : PORT_DIR, vartype : hw_type, defval : String)
    : hw_var(name, vartype, defval)