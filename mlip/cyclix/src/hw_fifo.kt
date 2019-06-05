package cyclix

import hwast.*

val OP_FIFO_WR = hw_opcode("fifo_wr")
val OP_FIFO_RD = hw_opcode("fifo_rd")

class hw_exec_fifo_wr(fifo_in : hw_fifo_out) : hw_exec(OP_FIFO_WR) {
    var fifo = fifo_in
}

class hw_exec_fifo_rd(fifo_in : hw_fifo_in) : hw_exec(OP_FIFO_RD) {
    var fifo = fifo_in
}

class hw_fifo_out(name_in : String, VarType : VAR_TYPE, src_struct_in: hw_struct, dimensions : hw_dim_static)
    : hw_structvar(name_in, VarType, src_struct_in, dimensions, "0") {

    constructor(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static)
            : this(name, VarType, DUMMY_STRUCT, dimensions)

    constructor(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb))

    constructor(name: String, src_struct_in: hw_struct, dimensions : hw_dim_static)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, dimensions)

    constructor(name: String, src_struct_in: hw_struct, msb: Int, lsb: Int)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb))

    constructor(name: String, src_struct_in: hw_struct)
            : this(name, src_struct_in, 0, 0)

}

class hw_fifo_in(name_in : String, VarType : VAR_TYPE, src_struct_in: hw_struct, dimensions : hw_dim_static)
    : hw_structvar(name_in, VarType, src_struct_in, dimensions, "0") {

    constructor(name : String, VarType : VAR_TYPE, dimensions : hw_dim_static)
            : this(name, VarType, DUMMY_STRUCT, dimensions)

    constructor(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb))

    constructor(name: String, src_struct_in: hw_struct, dimensions : hw_dim_static)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, dimensions)

    constructor(name: String, src_struct_in: hw_struct, msb: Int, lsb: Int)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb))

    constructor(name: String, src_struct_in: hw_struct)
            : this(name, src_struct_in, 0, 0)

}
