package rtl

import hwast.*

enum class PORT_DIR {
    IN,	OUT, INOUT
}

class hw_port(name : String, port_dir_in : PORT_DIR, VarType : VAR_TYPE, src_struct: hw_struct, dimensions : hw_dim_static, defval : String)
    : hw_var(name, VarType, src_struct, dimensions, defval) {

    var port_dir = port_dir_in

    constructor(name : String, port_dir : PORT_DIR, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String)
            : this(name, port_dir, VarType, DUMMY_STRUCT, dimensions, defval)

    constructor(name: String, port_dir : PORT_DIR, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String)
            : this(name, port_dir, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb), defval)

    constructor(name: String, port_dir : PORT_DIR, VarType: VAR_TYPE, defval: String)
            : this(name, port_dir, VarType, DUMMY_STRUCT, hw_dim_static(defval), defval)

    constructor(name: String, port_dir : PORT_DIR, src_struct_in: hw_struct, dimensions : hw_dim_static)
            : this(name, port_dir, VAR_TYPE.STRUCTURED, src_struct_in, dimensions, "0")

    constructor(name: String, port_dir : PORT_DIR, src_struct_in: hw_struct, msb: Int, lsb: Int)
            : this(name, port_dir, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb), "0")

    constructor(name: String, port_dir : PORT_DIR, src_struct_in: hw_struct)
            : this(name, port_dir, src_struct_in, 0, 0)
}