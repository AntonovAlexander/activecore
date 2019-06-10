/*
 * hw_port.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

import hwast.*

enum class PORT_DIR {
    IN,	OUT
}

class hw_port(name : String, port_dir_in : PORT_DIR, vartype : hw_type, defval : String)
    : hwast.hw_var(name, vartype, defval) {

    var port_dir = port_dir_in
}