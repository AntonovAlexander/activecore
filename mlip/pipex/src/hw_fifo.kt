/*
 * hw_fifo.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

val OP_FIFO_WR = hwast.hw_opcode("fifo_wr")
val OP_FIFO_RD = hwast.hw_opcode("fifo_rd")

class hw_exec_fifo_wr(fifo_in : hw_fifo_out) : hwast.hw_exec(OP_FIFO_WR) {
    var fifo = fifo_in
}

class hw_exec_fifo_rd(fifo_in : hw_fifo_in) : hwast.hw_exec(OP_FIFO_RD) {
    var fifo = fifo_in
}

class hw_fifo_out(name_in : String, VarType : hwast.VAR_TYPE, src_struct_in: hwast.hw_struct, dimensions : hwast.hw_dim_static)
    : hwast.hw_structvar(name_in, VarType, src_struct_in, dimensions, "0") {

    constructor(name : String, VarType : hwast.VAR_TYPE, dimensions : hwast.hw_dim_static)
            : this(name, VarType, hwast.DUMMY_STRUCT, dimensions)

    constructor(name: String, VarType: hwast.VAR_TYPE, msb: Int, lsb: Int)
            : this(name, VarType, hwast.DUMMY_STRUCT, hwast.hw_dim_static(msb, lsb))

    constructor(name: String, src_struct_in: hwast.hw_struct, dimensions : hwast.hw_dim_static)
            : this(name, hwast.VAR_TYPE.STRUCTURED, src_struct_in, dimensions)

    constructor(name: String, src_struct_in: hwast.hw_struct, msb: Int, lsb: Int)
            : this(name, hwast.VAR_TYPE.STRUCTURED, src_struct_in, hwast.hw_dim_static(msb, lsb))

    constructor(name: String, src_struct_in: hwast.hw_struct)
            : this(name, src_struct_in, 0, 0)

}

class hw_fifo_in(name_in : String, VarType : hwast.VAR_TYPE, src_struct_in: hwast.hw_struct, dimensions : hwast.hw_dim_static)
    : hwast.hw_structvar(name_in, VarType, src_struct_in, dimensions, "0") {

    constructor(name : String, VarType : hwast.VAR_TYPE, dimensions : hwast.hw_dim_static)
            : this(name, VarType, hwast.DUMMY_STRUCT, dimensions)

    constructor(name: String, VarType: hwast.VAR_TYPE, msb: Int, lsb: Int)
            : this(name, VarType, hwast.DUMMY_STRUCT, hwast.hw_dim_static(msb, lsb))

    constructor(name: String, src_struct_in: hwast.hw_struct, dimensions : hwast.hw_dim_static)
            : this(name, hwast.VAR_TYPE.STRUCTURED, src_struct_in, dimensions)

    constructor(name: String, src_struct_in: hwast.hw_struct, msb: Int, lsb: Int)
            : this(name, hwast.VAR_TYPE.STRUCTURED, src_struct_in, hwast.hw_dim_static(msb, lsb))

    constructor(name: String, src_struct_in: hwast.hw_struct)
            : this(name, src_struct_in, 0, 0)

}
