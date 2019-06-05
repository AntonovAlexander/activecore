package hwast

class hw_imm (val dimensions_in : hw_dim_static, val imm_value : String) : hw_param(PARAM_TYPE.VAL, dimensions_in, imm_value)
{
    constructor(imm_value : Int) : this(hw_dim_static(imm_value.toString()), imm_value.toString())
    constructor(imm_value : String) : this(hw_dim_static(imm_value), imm_value)
    constructor(msb: Int, lsb: Int, imm_value : String) : this(hw_dim_static(msb, lsb), imm_value)
    constructor(width: Int, imm_value : String) : this(hw_dim_static(width), imm_value)

    fun toInt() : Int {
        return imm_value.toInt()
    }
}