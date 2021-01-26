/*
 * hw_port.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

enum class PORT_DIR {
    IN,	OUT, INOUT
}

class hw_port(name : String, var port_dir : PORT_DIR, vartype : hw_type, defimm : hw_imm)
    : hw_var(name, vartype, defimm) {

    constructor(name : String, port_dir : PORT_DIR, vartype : hw_type, defval : String)
        : this(name, port_dir, vartype, hw_imm(defval))
}