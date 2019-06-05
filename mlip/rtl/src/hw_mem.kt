package rtl

import hwast.*
import java.lang.Exception

enum class SYNC_TYPE {
    LVL,	EDGE
}

enum class SYNC_LVL {
    POS,    NEG
}

enum class RST_TYPE {
    SYNC,    ASYNC
}

class mem_source(sync_lvl_in : SYNC_LVL, sync_signal_in : hw_var, sync_src_in : hw_param) {
    val sync_lvl = sync_lvl_in
    val sync_signal = sync_signal_in
    val sync_src = sync_src_in
}

class hw_mem(name : String, VarType : VAR_TYPE, src_struct_in: hw_struct, dimensions : hw_dim_static, sync_type_in : SYNC_TYPE)
    : hw_var(name, VarType, src_struct_in, dimensions, "0") {

    val sync_type = sync_type_in

    constructor(name: String, VarType: VAR_TYPE, dimensions: hw_dim_static, sync_type: SYNC_TYPE)
            : this(name, VarType, DUMMY_STRUCT, dimensions, sync_type)

    constructor(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, sync_type: SYNC_TYPE)
            : this(name, VarType, DUMMY_STRUCT, hw_dim_static(msb, lsb), sync_type)

    constructor(name: String, src_struct_in: hw_struct, dimensions : hw_dim_static, sync_type: SYNC_TYPE)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, dimensions, sync_type)

    constructor(name: String, src_struct_in: hw_struct, msb: Int, lsb: Int, sync_type: SYNC_TYPE)
            : this(name, VAR_TYPE.STRUCTURED, src_struct_in, hw_dim_static(msb, lsb), sync_type)

    constructor(name: String, src_struct_in: hw_struct, sync_type: SYNC_TYPE)
            : this(name, src_struct_in, 0, 0, sync_type)


    var mem_srcs = ArrayList<mem_source>()
    fun AddSource(clk_lvl : SYNC_LVL, clk_signal : hw_var, clk_src : hw_param) {

        mem_srcs.add(mem_source(clk_lvl, clk_signal, clk_src))
        this.write_done = true
        clk_signal.read_done = true
        if (clk_src.type == PARAM_TYPE.VAR) (clk_src as hw_var).read_done = true
    }

    var rst_present = false
    var rst_type = RST_TYPE.SYNC
    var rst_lvl = SYNC_LVL.POS
    var rst_signal = hw_var("TEMP", VAR_TYPE.UNSIGNED, "0")
    var rst_src = hw_param(PARAM_TYPE.VAL, hw_dim_static(1), "0")

    fun AddReset(rst_type_in : RST_TYPE, rst_lvl_in : SYNC_LVL, rst_signal_in : hw_var, rst_src_in : hw_param) {

        if (rst_present) ERROR("Only one reset in possible!")

        rst_type = rst_type_in
        rst_lvl = rst_lvl_in
        rst_signal = rst_signal_in
        rst_src = rst_src_in

        rst_present = true

        rst_signal.read_done = true
        if (rst_src.type == PARAM_TYPE.VAR) (rst_src as hw_var).read_done = true
    }
}