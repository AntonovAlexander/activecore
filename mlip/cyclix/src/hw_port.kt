/*
 * hw_port.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*

enum class PORT_DIR {
    IN,	OUT
}

class hw_port(name : String, port_dir_in : PORT_DIR, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String)
    : hw_var(name, VarType, dimensions, defval) {

    var port_dir = port_dir_in

    constructor(name: String, port_dir : PORT_DIR, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String)
            : this(name, port_dir, VarType, hw_dim_static(msb, lsb), defval)

    constructor(name: String, port_dir : PORT_DIR, VarType: VAR_TYPE, defval: String)
            : this(name, port_dir, VarType, hw_dim_static(defval), defval)

    constructor(name: String, port_dir : PORT_DIR, src_struct_in : hw_struct, dimensions : hw_dim_static)
            : this(name, port_dir, VAR_TYPE.STRUCTURED, dimensions, "0") {
        src_struct = src_struct_in
    }

    constructor(name: String, port_dir : PORT_DIR, src_struct_in : hw_struct)
            : this(name, port_dir, VAR_TYPE.STRUCTURED, 0, 0, "0") {
        src_struct = src_struct_in
    }
}