/*
 * hw_var.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

open class hw_var(name_in : String, vartype_in : hw_type, defval_in : String) : hw_structvar(name_in, vartype_in, defval_in) {

    var read_done: Boolean
    var write_done: Boolean

    var default_astc = hw_astc()

    init {
        name = name_in
        read_done = false
        write_done = false
        token_printable = name
    }

    constructor(name: String, VarType : VAR_TYPE, dimensions : hw_dim_static, defval : String)
            : this(name, hw_type(VarType, dimensions), defval)

    constructor(name: String, VarType: VAR_TYPE, msb: Int, lsb: Int, defval: String)
            : this(name, hw_type(VarType, msb, lsb), defval)

    constructor(name: String, VarType: VAR_TYPE, defval: String)
            : this(name, hw_type(VarType, defval), defval)

    constructor(name: String, src_struct_in: hw_struct, dimensions : hw_dim_static)
            : this(name, hw_type(src_struct_in, dimensions), "0")

    constructor(name: String, src_struct_in: hw_struct, msb: Int, lsb: Int)
            : this(name, hw_type(src_struct_in, msb, lsb), "0")

    constructor(name: String, src_struct_in: hw_struct)
            : this(name, src_struct_in, 0, 0)

    fun assign(depow_fractions: hw_fractions, src: hw_param) {
        default_astc.assign(depow_fractions, this, src)
    }

    fun assign(depow_fractions: hw_fractions, src: Int) {
        default_astc.assign(depow_fractions, this, src)
    }

    fun assign(src: hw_param) {
        default_astc.assign(this, src)
    }

    fun assign(src: Int) {
        default_astc.assign(this, src)
    }

    operator fun not(): hw_var {
        return default_astc.AddExpr_op1(OP1_BITWISE_NOT, this)
    }

    operator fun plus(src: hw_param): hw_var {
        return default_astc.AddExpr_op2(OP2_ARITH_ADD, this, src)
    }

    operator fun plus(src: Int): hw_var {
        return default_astc.AddExpr_op2(OP2_ARITH_ADD, this, src)
    }

    operator fun minus(src: hw_param): hw_var {
        return default_astc.AddExpr_op2(OP2_ARITH_SUB, this, src)
    }

    operator fun minus(src: Int): hw_var {
        return default_astc.AddExpr_op2(OP2_ARITH_SUB, this, src)
    }

    operator fun times(src: hw_param): hw_var {
        return default_astc.AddExpr_op2(OP2_ARITH_MUL, this, src)
    }

    operator fun times(src: Int): hw_var {
        return default_astc.AddExpr_op2(OP2_ARITH_MUL, this, src)
    }

    operator fun div(src: hw_param): hw_var {
        return default_astc.AddExpr_op2(OP2_ARITH_DIV, this, src)
    }

    operator fun div(src: Int): hw_var {
        return default_astc.AddExpr_op2(OP2_ARITH_DIV, this, src)
    }

    operator fun rem(src: hw_param): hw_var {
        return default_astc.AddExpr_op2(OP2_ARITH_MOD, this, src)
    }

    operator fun rem(src: Int): hw_var {
        return default_astc.AddExpr_op2(OP2_ARITH_MOD, this, src)
    }

    operator fun get(index: hw_param): hw_var {
        return default_astc.AddExpr_op2(OP2_INDEXED, this, index)
    }

    operator fun get(index: Int): hw_var {
        return default_astc.AddExpr_op2(OP2_INDEXED, this, index)
    }

    operator fun get(msb: hw_param, lsb: hw_param): hw_var {
        return default_astc.AddExpr_op3(OP3_RANGED, this, msb, lsb)
    }

    operator fun get(msb: Int, lsb: Int): hw_var {
        return default_astc.AddExpr_op3(OP3_RANGED, this, hw_imm(msb), hw_imm(lsb))
    }

    operator fun set(index: hw_param, src: hw_param) {
        var depow_fractions = hw_fractions()
        if (index.type == PARAM_TYPE.VAL) depow_fractions.add(hw_fraction_C(index as hw_imm))
        else depow_fractions.add(hw_fraction_V(index as hw_var))
        default_astc.assign(depow_fractions, this, src)
    }

    operator fun set(index: Int, src: hw_param) {
        var depow_fractions = hw_fractions()
        depow_fractions.add(hw_fraction_C(index))
        default_astc.assign(depow_fractions, this, src)
    }

    operator fun set(msb: hw_param, lsb: hw_param, src: hw_param) {
        var depow_fractions = hw_fractions()

        if (msb.type == PARAM_TYPE.VAL) {
            if (lsb.type == PARAM_TYPE.VAL) {
                depow_fractions.add(hw_fraction_CC(msb as hw_imm, lsb as hw_imm))
            } else {
                depow_fractions.add(hw_fraction_CV(msb as hw_imm, lsb as hw_var))
            }
        } else {
            if (lsb.type == PARAM_TYPE.VAL) {
                depow_fractions.add(hw_fraction_VC(msb as hw_var, lsb as hw_imm))
            } else {
                depow_fractions.add(hw_fraction_VV(msb as hw_var, lsb as hw_var))
            }
        }
        default_astc.assign(depow_fractions, this, src)
    }

    operator fun set(msb: Int, lsb: Int, src: hw_param) {
        var depow_fractions = hw_fractions()
        depow_fractions.add(hw_fraction_CC(msb, lsb))
        default_astc.assign(depow_fractions, this, src)
    }

    fun GetDepowered(depow_fractions: hw_fractions): hw_type {
        var ret_dim = hw_dim_static()
        ret_dim.clear()
        var ret_vartype : VAR_TYPE
        var ret_struct : hw_struct

        // copying dimensions of a variable
        for (i in 0 until vartype.dimensions.size) ret_dim.add(vartype.dimensions[i])
        ret_vartype = vartype.VarType
        ret_struct = vartype.src_struct

        // detaching dimensions
        for (depow_fraction in depow_fractions) {

            if (ret_dim.isSingle()) {
                // undimensioned var
                ret_dim.clear()
                if (depow_fraction.type == FRAC_TYPE.SubStruct) {
                    // retrieving structure
                    ret_vartype = (depow_fraction as hw_fraction_SubStruct).src_struct[depow_fraction.subStructIndex].vartype.VarType
                    ret_struct = depow_fraction.src_struct[depow_fraction.subStructIndex].vartype.src_struct
                    for (dimension in depow_fraction.src_struct[depow_fraction.subStructIndex].vartype.dimensions) {
                        ret_dim.add(dimension)
                    }
                } else {
                    // indexing 1-bit (dim) var
                    ret_dim.add(hw_dim_range_static(0, 0))
                }

            } else {
                // dimensioned var
                if (depow_fraction.type == FRAC_TYPE.SubStruct) ERROR("Depower index generation incorrect, accessing substruct in multidimensional variable")
                else {
                    if ((depow_fraction.type == FRAC_TYPE.C) || (depow_fraction.type == FRAC_TYPE.V)) {
                        // taking indexed variable - detachment of last dimensions
                        ret_dim.removeAt(ret_dim.lastIndex)
                    } else if (depow_fraction.type == FRAC_TYPE.CC) {
                        // replacing last dimension with taken
                        ret_dim.removeAt(ret_dim.lastIndex)
                        ret_dim.add(hw_dim_range_static((depow_fraction as hw_fraction_CC).msb.toInt(), depow_fraction.lsb.toInt()))
                    } else continue
                }
            }
        }

        return hw_type(ret_vartype, ret_struct, ret_dim)
    }
}
