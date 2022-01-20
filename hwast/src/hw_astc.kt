/*
 * hw_astc.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.*

open class import_expr_context(var var_dict : MutableMap<hw_var, hw_var>)

// AST constructor for behavioral HW specifications
open class hw_astc() : ArrayList<hw_exec>() {

    var GenCounter      = 0
    open var GenNamePrefix   = "hwast"
    fun GetGenName(name_in : String) : String
    {
        val ret_val = ("gen" + GenCounter.toString() + "_" + GenNamePrefix + "_" + name_in)
        GenCounter++
        return ret_val
    }

    protected var FROZEN_FLAG = false

    fun MSG(msg_string : String) {
        ProcessLogFileStream()
        val str = GenNamePrefix + ": " + msg_string + "\n"
        print(str)
        LogFile.write(str)
        LogFile.close()
    }

    fun MSG(debug_lvl : DEBUG_LEVEL, msg_string : String) {
        if (debug_lvl.level > 0) MSG(msg_string)
    }

    fun WARNING(err_string : String) {
        ProcessLogFileStream()
        val str = "WARNING (" + GenNamePrefix + "): " + err_string + "\n"
        print(str)
        LogFile.write(str)
        LogFile.close()
    }

    fun CRITICAL(err_string : String) {
        ProcessLogFileStream()
        val str = "CRITICAL WARNING (" + GenNamePrefix + "): " + err_string + "\n"
        print(str)
        LogFile.write(str)
        LogFile.close()
    }

    fun ERROR(err_string : String) {
        ProcessLogFileStream()
        val str = "ERROR (" + GenNamePrefix + "): " + err_string + "\n"
        print(str)
        LogFile.write(str)
        LogFile.close()
        throw Exception(str)
    }

    fun freeze() {
        FROZEN_FLAG = true
    }

    protected fun AddWrVar(new_wrvar: hw_var) {
        for (cur_exec in this) {
            cur_exec.AddWrVar(new_wrvar)
        }
    }

    protected fun AddRdVar(new_rdvar: hw_var) {
        for (cur_exec in this) {
            cur_exec.AddRdVar(new_rdvar)
        }
    }

    protected fun AddGenVar(new_genvar: hw_var) {
        for (cur_exec in this) {
            cur_exec.AddGenVar(new_genvar)
        }
        new_genvar.default_astc = this
        if (new_genvar is hw_var_frac) new_genvar.src_var.default_astc = this
    }

    protected fun DistributeVars(new_expr: hw_exec) {

        for (new_wrvar in new_expr.wrvars) {
            AddWrVar(new_wrvar)
        }
        for (new_rdvar in new_expr.rdvars) {
            AddRdVar(new_rdvar)
        }
        for (new_genvar in new_expr.genvars) {
            AddGenVar(new_genvar)
        }

        for (cur_exec in this) {
            for (new_ifvar in new_expr.iftargets) {
                cur_exec.AddIfTargetVar(new_ifvar)
            }
        }
    }

    protected fun AddExpr(new_expr: hw_exec) {
        if (FROZEN_FLAG) ERROR("Failed to add operation " + new_expr.opcode.default_string + ": ASTC frozen")
        if (size == 0) {
            //MSG_COMMENT("Erroneous operation: ")
            //for (dst in new_expr.dsts) MSG_COMMENT("dst: " + dst.name)
            //for (param in new_expr.params) MSG_COMMENT("param: " + param.GetString())
            ERROR("Exec stack size error on opcode: " + new_expr.opcode.default_string + ", exec size: " + size)
        }
        DistributeVars(new_expr)
        last().expressions.add(last().cursor, new_expr)
        last().cursor++
    }

    protected fun AddExpr_op_gen(opcode: hw_opcode, dst: hw_var, srcs: ArrayList<hw_param>) {
        var new_expr = hw_exec(opcode)
        new_expr.AddDst(dst)
        for (new_src in srcs) {
            new_expr.AddParam(new_src)
        }
        AddExpr(new_expr)
    }

    protected fun AddExpr_op_gen_withgen(opcode: hw_opcode, target: hw_var, params: ArrayList<hw_param>) {
        var new_expr = hw_exec(opcode)
        new_expr.AddDst(target)
        //MSG("opcode: " + opcode.default_string)
        //MSG("params: " + params.size)
        for (new_param in params) {
            //MSG("param: " + new_param.GetString())
            new_expr.AddParam(new_param)
        }
        new_expr.AddGenVar(target)
        AddExpr(new_expr)
    }

    protected fun AddExpr_op1_gen(opcode: hw_opcode, dst: hw_var, src: hw_param) {
        var srcs = ArrayList<hw_param>()
        srcs.add(src)
        AddExpr_op_gen(opcode, dst, srcs)
    }

    protected fun AddExpr_op2_gen(opcode: hw_opcode, dst: hw_var, src0: hw_param, src1: hw_param) {
        var srcs = ArrayList<hw_param>()
        srcs.add(src0)
        srcs.add(src1)
        AddExpr_op_gen(opcode, dst, srcs)
    }

    protected fun AddExpr_op2_gen(opcode: hw_opcode, dst: hw_var, src0: hw_param, src1: Int) {
        var srcs = ArrayList<hw_param>()
        srcs.add(src0)
        srcs.add(hw_imm(src1))
        AddExpr_op_gen(opcode, dst, srcs)
    }

    protected fun AddExpr_op3_gen(opcode: hw_opcode, dst: hw_var, src0: hw_param, src1: hw_param, src2: hw_param) {
        var srcs = ArrayList<hw_param>()
        srcs.add(src0)
        srcs.add(src1)
        srcs.add(src2)
        AddExpr_op_gen(opcode, dst, srcs)
    }

    protected fun op2_gen_dimensions(opcode: hw_opcode, dim0: hw_dim_static, dim1: hw_dim_static): hw_dim_static {

        var dim0_anlz = hw_dim_static()
        for (dim in dim0) dim0_anlz.add(dim)
        if (dim0_anlz.isEmpty()) dim0_anlz.add(hw_dim_range_static(0, 0))

        var dim1_anlz = hw_dim_static()
        for (dim in dim1) dim1_anlz.add(dim)
        if (dim1_anlz.isEmpty()) dim1_anlz.add(hw_dim_range_static(0, 0))

        val op1_primary_dimension = dim0_anlz[0]
        val op2_primary_dimension = dim1_anlz[0]
        val op1_length = op1_primary_dimension.GetWidth()
        val op2_length = op2_primary_dimension.GetWidth()
        val max_length = kotlin.math.max(op1_length, op2_length)

        var gen_dim = hw_dim_static()

        // ADD, SUB //
        if ((opcode == OP2_ARITH_ADD) || (opcode == OP2_ARITH_SUB)) {
            if (dim0_anlz.size != dim1_anlz.size) ERROR("Dimensions incorrect")
            for (i: Int in 1 until dim0_anlz.size) {
                if (dim0_anlz[i].CheckEqual(dim1_anlz[i])) gen_dim.add(dim0_anlz[i])
                else ERROR("Dimensions incorrect")
            }
            val gen_length = max_length + 1
            val new_range = hw_dim_range_static(gen_length - 1, 0)
            gen_dim.add(0, new_range)
        }

        // MUL //
        else if (opcode == OP2_ARITH_MUL) {
            if (dim0_anlz.size != dim1_anlz.size) {
                ERROR("Dimensions incorrect")
            }
            for (i: Int in 1 until dim0_anlz.size) {
                if (dim0_anlz[i].CheckEqual(dim1_anlz[i])) gen_dim.add(dim0_anlz[i])
                else ERROR("Dimensions incorrect")
            }
            val gen_length = op1_length + op2_length
            val new_range = hw_dim_range_static(gen_length - 1, 0)
            gen_dim.add(0, new_range)
        }

        // DIV, BITWISE //
        else if ((opcode == OP2_ARITH_DIV) || (opcode == OP2_ARITH_MOD) || (opcode == OP2_BITWISE_AND) || (opcode == OP2_BITWISE_OR) || (opcode == OP2_BITWISE_XOR) || (opcode == OP2_BITWISE_XNOR)) {
            if (dim0_anlz.size != dim1_anlz.size) ERROR("Dimensions incorrect")
            for (i: Int in 1 until dim0_anlz.size) {
                if (dim0_anlz[i].CheckEqual(dim1_anlz[i])) gen_dim.add(dim0_anlz[i])
                else ERROR("Dimensions incorrect")
            }
            val gen_length = max_length
            val new_range = hw_dim_range_static(gen_length - 1, 0)
            gen_dim.add(0, new_range)
        }

        // LOGICAL //
        else if ((opcode == OP2_LOGICAL_AND)
                || (opcode == OP2_LOGICAL_OR)
                || (opcode == OP2_LOGICAL_G)
                || (opcode == OP2_LOGICAL_L)
                || (opcode == OP2_LOGICAL_GEQ)
                || (opcode == OP2_LOGICAL_LEQ)
                || (opcode == OP2_LOGICAL_EQ2)
                || (opcode == OP2_LOGICAL_NEQ2)
                || (opcode == OP2_LOGICAL_EQ4)
                || (opcode == OP2_LOGICAL_NEQ4)) {

            if ((dim0_anlz.size != 1) || (dim1_anlz.size != 1)) ERROR("Dimensions incorrect")
            val new_range = hw_dim_range_static(0, 0)
            gen_dim.add(0, new_range)
        }

        // SHIFT //
        else if ((opcode == OP2_ARITH_SLL) || (opcode == OP2_ARITH_SRL) || (opcode == OP2_ARITH_SRA)) {
            if (dim1_anlz.size != 1) ERROR("Dimensions incorrect")
            for (i in 0 until dim0_anlz.size) gen_dim.add(dim0_anlz[i])
        }

        // INDEXED //
        else if (opcode == OP2_INDEXED) {
            if (dim1_anlz.size > 1) ERROR("Dimensions incorrect")
            if (dim0_anlz.size > 1) {
                for (i in 0 until (dim0_anlz.size-1)) gen_dim.add(dim0_anlz[i])
            }
        } else ERROR("Opcode unrecognized")

        return gen_dim
    }

    protected fun op_generate_genvar(opcode: hw_opcode, params: ArrayList<hw_param>): hw_var {
        //MSG("op_generate_genvar, opcode: " + opcode.default_string)
        if ((opcode == OP1_BITWISE_NOT)
                || (opcode == OP1_LOGICAL_NOT)
                || (opcode == OP1_COMPLEMENT)
                || (opcode == OP1_REDUCT_AND)
                || (opcode == OP1_REDUCT_NAND)
                || (opcode == OP1_REDUCT_OR)
                || (opcode == OP1_REDUCT_NOR)
                || (opcode == OP1_REDUCT_XOR)
                || (opcode == OP1_REDUCT_XNOR)) {
            if (params.size != 1) ERROR("params incorrect for operation " + opcode.default_string + " - number of params: " + params.size + ", expected: 1")
            var gen_dim = hw_dim_static()

            if ((opcode == OP1_BITWISE_NOT) || (opcode == OP1_COMPLEMENT)) {
                for (param in params[0].GetDimensions()) {
                    gen_dim.add(param)
                }
            }
            else if (opcode == OP1_LOGICAL_NOT) gen_dim.add(hw_dim_range_static(0, 0))
            else {
                // TODO: vectored operations
                if (params[0].GetDimensions().size > 1) ERROR("Reduct param dimensions error!")
                for (param in params[0].GetDimensions()) {
                    gen_dim.add(param)
                }
                gen_dim.removeAt(0)
                gen_dim.add(0, hw_dim_range_static(0, 0))
            }

            var curDataType = DATA_TYPE.BV_UNSIGNED
            if (params[0] is hw_var) curDataType = params[0].vartype.DataType

            return hw_var(GetGenName("var"), curDataType, gen_dim, "0")
        } else if ((opcode == OP2_ARITH_ADD)
                || (opcode == OP2_ARITH_SUB)
                || (opcode == OP2_ARITH_MUL)
                || (opcode == OP2_ARITH_DIV)
                || (opcode == OP2_ARITH_MOD)
                || (opcode == OP2_ARITH_SLL)
                || (opcode == OP2_ARITH_SRL)
                || (opcode == OP2_ARITH_SRA)

                || (opcode == OP2_LOGICAL_AND)
                || (opcode == OP2_LOGICAL_OR)
                || (opcode == OP2_LOGICAL_G)
                || (opcode == OP2_LOGICAL_L)
                || (opcode == OP2_LOGICAL_GEQ)
                || (opcode == OP2_LOGICAL_LEQ)
                || (opcode == OP2_LOGICAL_EQ2)
                || (opcode == OP2_LOGICAL_NEQ2)
                || (opcode == OP2_LOGICAL_EQ4)
                || (opcode == OP2_LOGICAL_NEQ4)

                || (opcode == OP2_BITWISE_AND)
                || (opcode == OP2_BITWISE_OR)
                || (opcode == OP2_BITWISE_XOR)
                || (opcode == OP2_BITWISE_XNOR)

                || (opcode == OP2_INDEXED)) {
            if (params.size != 2)
                ERROR("params incorrect for operation " + opcode.default_string + " - number of params: " + params.size + ", expected: 2")

            var gendim = op2_gen_dimensions(opcode, params[0].GetDimensions(), params[1].GetDimensions())

            // by default: unsigned
            var curDataType = DATA_TYPE.BV_UNSIGNED
            var curStruct = DUMMY_STRUCT

            // if any operand for arith - signed
            if ((opcode == OP2_ARITH_ADD)
                    || (opcode == OP2_ARITH_SUB)
                    || (opcode == OP2_ARITH_MUL)
                    || (opcode == OP2_ARITH_DIV)
                    || (opcode == OP2_ARITH_MOD)
                    || (opcode == OP2_ARITH_SLL)
                    || (opcode == OP2_ARITH_SRL)
                    || (opcode == OP2_ARITH_SRA)) {
                for (param in params) {
                    if (param is hw_var) {
                        if (param.vartype.DataType == DATA_TYPE.BV_SIGNED) curDataType = DATA_TYPE.BV_SIGNED
                    }
                }
            }

            // if indexed of struct array - assert struct
            if (opcode == OP2_INDEXED) {
                if (params[0] is hw_var) {
                    if (params[0].vartype.DataType == DATA_TYPE.STRUCTURED) {
                        curDataType = DATA_TYPE.STRUCTURED
                        curStruct = params[0].vartype.src_struct
                    }
                }
            }

            return hw_var(GetGenName("var"), hw_type(curDataType, curStruct, gendim), "0")
        } else if (opcode == OP3_RANGED) {
            if (params.size != 3) ERROR("params incorrect for operation " + opcode.default_string + " - number of params: " + params.size + ", expected: 3")
            if ((params[0].GetDimensions().size > 1) || (params[1].GetDimensions().size > 1) || (params[2].GetDimensions().size > 1))
                ERROR("params incorrect for operation")

            var gendim = hw_dim_static()
            if ((params[1] is hw_imm) && (params[2] is hw_imm)) {
                var msb = params[1].token_printable.toInt()
                var lsb = params[2].token_printable.toInt()
                gendim.add(hw_dim_range_static(msb, lsb))
            } else {
                gendim = params[0].GetDimensions()
            }

            return hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, gendim, "0")
        } else if (opcode == OPS_CNCT) {
            if (params.size == 0)
                ERROR("params incorrect for operation " + opcode.default_string + " - number of params: " + params.size)

            var width = 0
            for (i in 0 until params.size) {
                if (params[i].GetDimensions().size > 1) ERROR("params incorrect for operation")
                width += params[i].GetWidth()
            }

            var gendim = hw_dim_static()
            gendim.add(hw_dim_range_static(width - 1, 0))

            return hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, gendim, "0")
        } else ERROR("Opcode unrecognized")
        return hw_var(GetGenName("var"), DUMMY_STRUCT, 0, 0)
    }

    protected fun AddExpr_op(opcode: hw_opcode, srcs: ArrayList<hw_param>): hw_var {
        var genvar = op_generate_genvar(opcode, srcs)
        AddExpr_op_gen_withgen(opcode, genvar, srcs)
        return genvar
    }

    protected fun AddExpr_op1(opcode: hw_opcode, src: hw_param): hw_var {
        var srcs = ArrayList<hw_param>()
        srcs.add(src)
        return AddExpr_op(opcode, srcs)
    }

    protected fun AddExpr_op1(opcode: hw_opcode, src: Int): hw_var {
        return AddExpr_op1(opcode, hw_imm(src))
    }

    protected fun AddExpr_op2(opcode: hw_opcode, src0: hw_param, src1: hw_param): hw_var {
        var srcs = ArrayList<hw_param>()
        srcs.add(src0)
        srcs.add(src1)
        return AddExpr_op(opcode, srcs)
    }

    protected fun AddExpr_op2(opcode: hw_opcode, src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(opcode, src0, hw_imm(src1))
    }

    protected fun AddExpr_op2(opcode: hw_opcode, src0: Int, src1: hw_param): hw_var {
        return AddExpr_op2(opcode, hw_imm(src0), src1)
    }

    protected fun AddExpr_op2(opcode: hw_opcode, src0: Int, src1: Int): hw_var {
        return AddExpr_op2(opcode, hw_imm(src0), hw_imm(src1))
    }

    protected fun AddExpr_op3(opcode: hw_opcode, src0: hw_param, src1: hw_param, src2: hw_param): hw_var {
        var srcs = ArrayList<hw_param>()
        srcs.add(src0)
        srcs.add(src1)
        srcs.add(src2)
        return AddExpr_op(opcode, srcs)
    }

    private fun assign_gen(dst: hw_var, src: hw_param) {
        var new_expr = hw_exec(OP1_ASSIGN)
        new_expr.AddParam(src)
        new_expr.AddDst(dst)
        AddExpr(new_expr)
    }

    fun assign(dst: hw_var, src: hw_param) {
        //if (src is hw_var)
        //    MSG("ASSIGNMENT! dst: " + dst.name + " (struct: " + dst.vartype.src_struct.name + "), src: " + src.GetString() + "(struct: " + src.vartype.src_struct.name + ")")

        var dst_Power = dst.GetDimensions().size
        if (dst_Power < 1) dst_Power = 1                    // for 1-bit signals

        var src_Power = src.GetDimensions().size
        if (src_Power < 1) src_Power = 1                    // for 1-bit signals

        if (src is hw_var) {
            if (dst_Power != src_Power) ERROR("dimensions do not match for target " + dst.name + " (dst power: " + dst_Power + "), src: " + src.GetString() + " (src power: " + src_Power + ")")
            else if (dst_Power == 1) {

                if (((src as hw_var).vartype.DataType == DATA_TYPE.STRUCTURED) && (dst.vartype.DataType != DATA_TYPE.STRUCTURED)) {
                    dst.vartype.Print()
                    src.vartype.Print()
                    ERROR("assignment to non-structured variable (" + dst.name + ") of structured variable (" + src.name + ")")
                } else if ((src.vartype.DataType != DATA_TYPE.STRUCTURED) && (dst.vartype.DataType == DATA_TYPE.STRUCTURED)) {
                    dst.vartype.Print()
                    src.vartype.Print()
                    ERROR("assignment to structured variable (" + dst.name + ") of non-structured variable (" + src.name + ")")
                } else if (src.vartype.DataType == DATA_TYPE.STRUCTURED) {
                    // assignment of 1-bit structs
                    if (src.vartype.src_struct != dst.vartype.src_struct) {
                        // assignment of inequally structured variables
                        MSG("Structure of dst: " + dst.name + ", struct name: " + dst.vartype.src_struct.name)
                        for (structvar in dst.vartype.src_struct) {
                            MSG("-- " + structvar.name)
                        }
                        MSG("Structure of src: " + dst.name + ", struct name: " + src.vartype.src_struct.name)
                        for (structvar in src.vartype.src_struct) {
                            MSG("-- " + structvar.name)
                        }
                        ERROR("assignment of inequally structured variables! dst: " + dst.name + " (struct: " + dst.vartype.src_struct.name + "), src: " + src.GetString() + "(struct: " + src.vartype.src_struct.name + ")")
                    }
                }
                assign_gen(dst, src)

            } else {
                for (i in 0 until dst.vartype.dimensions.last().GetWidth()) {
                    var src_ref = src.GetFracRef(hw_frac_C(i + dst.vartype.dimensions.last().lsb))
                    var dst_ref = dst.GetFracRef(hw_frac_C(i + dst.vartype.dimensions.last().lsb))
                    assign(dst_ref, src_ref)
                }
            }
        } else if (src is hw_imm) {
            if (dst_Power == 1) assign_gen(dst, src)
            else {
                for (i in 0 until dst.vartype.dimensions.get(dst_Power - 1).GetWidth()) {
                    var dst_ref = dst.GetFracRef(hw_frac_C(i + dst.vartype.dimensions.last().lsb))
                    assign(dst_ref, src)
                }
            }
        }
    }

    fun COMMENT(new_comment : String) {
        var new_expr = hw_exec(OP_COMMENT)
        new_expr.comment = new_comment
        AddExpr(new_expr)
    }

    fun MSG_COMMENT(new_comment : String) {
        MSG(new_comment)
        COMMENT(new_comment)
    }

    fun assign(dst: hw_var, src: Int) {
        assign(dst, hw_imm(src))
    }

    private fun CheckIfBothStructured(var1 : hw_var, var2 : hw_var) {
        if ((var1.vartype.DataType != DATA_TYPE.STRUCTURED) || (var2.vartype.DataType != DATA_TYPE.STRUCTURED)) {
            if (var1.vartype.DataType != DATA_TYPE.STRUCTURED) MSG_COMMENT("dst: " + var1.name + ": not structured!")
            if (var2.vartype.DataType != DATA_TYPE.STRUCTURED) MSG_COMMENT("src: " + var2.name + ": not structured!")
            ERROR("Attempting to assign_subStructs non structured variables")
        }
    }

    fun assign_subStructs(dst: hw_var, src: hw_var) {
        CheckIfBothStructured(dst, src)
        for (dst_substruct in dst.vartype.src_struct) {
            for (src_substruct in src.vartype.src_struct) {
                if (dst_substruct.name == src_substruct.name)
                    assign(dst.GetFracRef(dst_substruct.name), src.GetFracRef(src_substruct.name))
            }
        }
    }

    fun assign_subStructsWithDefaultsIfAbsent(dst: hw_var, src: hw_var) {
        CheckIfBothStructured(dst, src)
        for (dst_substruct in dst.vartype.src_struct) {
            var drv_found = false
            for (src_substruct in src.vartype.src_struct) {
                if (dst_substruct.name == src_substruct.name) {
                    assign(dst.GetFracRef(dst_substruct.name), src.GetFracRef(src_substruct.name))
                    drv_found = true
                    break
                }
            }
            if (!drv_found) assign(dst.GetFracRef(dst_substruct.name), dst_substruct.defimm)
        }
    }

    fun complement(src: hw_param): hw_var {
        return AddExpr_op1(OP1_COMPLEMENT, src)
    }

    // implements tree for bitwise logic operations
    fun op_tree(opcode : hw_opcode, srcs : ArrayList<hw_param>) : hw_var {
        if (srcs.size == 0) {
            ERROR("Empty param list for bitwise operation!")
            return DUMMY_VAR
        } else if (srcs.size == 1) {
            var ret_var = hw_var(GetGenName("btree"), srcs[0].vartype, "0")
            assign_gen(ret_var, srcs[0])
            return ret_var
        } else if (srcs.size == 2) {
            return AddExpr_op2(opcode, srcs[0], srcs[1])
        } else {
            var sum_inters = ArrayList<hw_param>()
            for (src in srcs) {
                val CUR_SRC_INDEX = srcs.indexOf(src)
                val NEXT_SRC_INDEX = CUR_SRC_INDEX + 1
                if (CUR_SRC_INDEX % 2 == 0) {
                    if (srcs.lastIndex < NEXT_SRC_INDEX) {
                        sum_inters.add(src)
                    } else {
                        var new_inter = op_tree(opcode, srcs[CUR_SRC_INDEX], srcs[CUR_SRC_INDEX+1])
                        AddGenVar(new_inter)
                        sum_inters.add(new_inter)
                    }
                }
            }
            return op_tree(opcode, sum_inters)
        }
    }

    fun op_tree(opcode : hw_opcode, vararg srcs : hw_param) : hw_var {
        var srcsList = ArrayList<hw_param>()
        for (src in srcs) srcsList.add(src)
        return op_tree(opcode, srcsList)
    }

    fun count_ones(src: hw_var) : hw_var {
        var srcsList = ArrayList<hw_param>()
        for (src_bit in 0 until src.GetWidth()) {
            srcsList.add(src.GetFracRef(src_bit))
        }
        return op_tree(OP2_ARITH_ADD, srcsList)
    }

    fun count_zeroes(src: hw_var) : hw_var {
        return count_ones(bnot(src))
    }

    fun add(srcs : ArrayList<hw_param>): hw_var {
        return op_tree(OP2_ARITH_ADD, srcs)
    }

    fun add(vararg srcs : hw_param): hw_var {
        var srcList = ArrayList<hw_param>()
        for (src in srcs) srcList.add(src)
        return add(srcList)
    }

    fun add(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_ARITH_ADD, src0, src1)
    }

    fun sub(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_ARITH_SUB, src0, src1)
    }

    fun sub(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_ARITH_SUB, src0, src1)
    }

    fun mul(srcs : ArrayList<hw_param>): hw_var {
        return op_tree(OP2_ARITH_MUL, srcs)
    }

    fun mul(vararg srcs : hw_param): hw_var {
        var srcList = ArrayList<hw_param>()
        for (src in srcs) srcList.add(src)
        return mul(srcList)
    }

    fun mul(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_ARITH_MUL, src0, src1)
    }

    fun div(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_ARITH_DIV, src0, src1)
    }

    fun div(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_ARITH_DIV, src0, src1)
    }

    fun mod(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_ARITH_MOD, src0, src1)
    }

    fun mod(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_ARITH_MOD, src0, src1)
    }

    fun sll(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_ARITH_SLL, src0, src1)
    }

    fun sll(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_ARITH_SLL, src0, src1)
    }

    fun srl(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_ARITH_SRL, src0, src1)
    }

    fun srl(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_ARITH_SRL, src0, src1)
    }

    fun sra(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_ARITH_SRA, src0, src1)
    }

    fun sra(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_ARITH_SRA, src0, src1)
    }

    fun lnot(src: hw_param): hw_var {
        return AddExpr_op1(OP1_LOGICAL_NOT, src)
    }

    fun land(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_LOGICAL_AND, src0, src1)
    }

    fun land(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_LOGICAL_AND, src0, src1)
    }

    fun lor(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_LOGICAL_OR, src0, src1)
    }

    fun lor(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_LOGICAL_OR, src0, src1)
    }

    fun gr(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_LOGICAL_G, src0, src1)
    }

    fun gr(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_LOGICAL_G, src0, src1)
    }

    fun less(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_LOGICAL_L, src0, src1)
    }

    fun less(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_LOGICAL_L, src0, src1)
    }

    fun geq(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_LOGICAL_GEQ, src0, src1)
    }

    fun geq(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_LOGICAL_GEQ, src0, src1)
    }

    fun leq(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_LOGICAL_LEQ, src0, src1)
    }

    fun leq(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_LOGICAL_LEQ, src0, src1)
    }

    fun eq2(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_LOGICAL_EQ2, src0, src1)
    }

    fun eq2(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_LOGICAL_EQ2, src0, src1)
    }

    fun neq2(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_LOGICAL_NEQ2, src0, src1)
    }

    fun neq2(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_LOGICAL_NEQ2, src0, src1)
    }

    fun eq4(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_LOGICAL_EQ4, src0, src1)
    }

    fun eq4(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_LOGICAL_EQ4, src0, src1)
    }

    fun neq4(src0: hw_param, src1: hw_param): hw_var {
        return AddExpr_op2(OP2_LOGICAL_NEQ4, src0, src1)
    }

    fun neq4(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_LOGICAL_NEQ4, src0, src1)
    }

    fun bnot(src: hw_param): hw_var {
        return AddExpr_op1(OP1_BITWISE_NOT, src)
    }

    fun band(vararg srcs : hw_param): hw_var {
        var srcsList = ArrayList<hw_param>()
        for (src in srcs) srcsList.add(src)
        return op_tree(OP2_BITWISE_AND, srcsList)
    }

    fun band(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_BITWISE_AND, src0, src1)
    }

    fun bor(vararg srcs : hw_param): hw_var {
        var srcsList = ArrayList<hw_param>()
        for (src in srcs) srcsList.add(src)
        return op_tree(OP2_BITWISE_OR, srcsList)
    }

    fun bor(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_BITWISE_OR, src0, src1)
    }

    fun bxor(vararg srcs : hw_param): hw_var {
        var srcsList = ArrayList<hw_param>()
        for (src in srcs) srcsList.add(src)
        return op_tree(OP2_BITWISE_XOR, srcsList)
    }

    fun bxor(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_BITWISE_XOR, src0, src1)
    }

    fun bxnor(vararg srcs : hw_param): hw_var {
        var srcsList = ArrayList<hw_param>()
        for (src in srcs) srcsList.add(src)
        return op_tree(OP2_BITWISE_XNOR, srcsList)
    }

    fun bxnor(src0: hw_param, src1: Int): hw_var {
        return AddExpr_op2(OP2_BITWISE_XNOR, src0, src1)
    }

    fun rand(src0: hw_param): hw_var {
        return AddExpr_op1(OP1_REDUCT_AND, src0)
    }

    fun rnand(src0: hw_param): hw_var {
        return AddExpr_op1(OP1_REDUCT_NAND, src0)
    }

    fun ror(src0: hw_param): hw_var {
        return AddExpr_op1(OP1_REDUCT_OR, src0)
    }

    fun rnor(src0: hw_param): hw_var {
        return AddExpr_op1(OP1_REDUCT_NOR, src0)
    }

    fun rxor(src0: hw_param): hw_var {
        return AddExpr_op1(OP1_REDUCT_XOR, src0)
    }

    fun rxnor(src0: hw_param): hw_var {
        return AddExpr_op1(OP1_REDUCT_XNOR, src0)
    }

    fun indexed(src: hw_param, index: hw_param): hw_var {
        return AddExpr_op2(OP2_INDEXED, src, index)
    }

    fun indexed(src: hw_param, index: Int): hw_var {
        return AddExpr_op2(OP2_INDEXED, src, index)
    }

    fun ranged(src: hw_param, msb: hw_param, lsb: hw_param): hw_var {
        return AddExpr_op3(OP3_RANGED, src, msb, lsb)
    }

    fun ranged(src: hw_param, msb: Int, lsb: Int): hw_var {
        return AddExpr_op3(OP3_RANGED, src, hw_imm(msb), hw_imm(lsb))
    }

    fun subStruct_withgen(dst: hw_var, src: hw_var, subStruct_name: String) {
        var new_expr = hw_exec(OP2_SUBSTRUCT)
        new_expr.AddDst(dst)
        new_expr.AddParam(src)
        new_expr.AddGenVar(dst)
        new_expr.subStructvar_name = subStruct_name
        AddExpr(new_expr)
    }

    fun get_subStruct_type(src: hw_var, subStruct_name: String): hw_type {

        if (!src.vartype.dimensions.isSingle()) ERROR("Attempting to take substruct of array")
        if (src.vartype.DataType != DATA_TYPE.STRUCTURED) ERROR("Attempting to take substruct non-structured variable " + src.name + " (substruct: " + subStruct_name + ")")

        var structvar_found = false
        for (structvar in src.vartype.src_struct)
        {
            if (structvar.name == subStruct_name) {
                return hw_type(structvar.vartype.DataType, structvar.vartype.src_struct, structvar.vartype.dimensions)
            }
        }
        if (!structvar_found) {
            MSG("Struct elements for variable " + src.name + ":")
            for (structvar_entry in src.vartype.src_struct) {
                MSG("-- " + structvar_entry.name)
            }
            ERROR("Structvar " + subStruct_name + " not found in var " + src.name)
        }
        return hw_type(src.vartype.DataType, src.vartype.src_struct, src.vartype.dimensions)
    }

    fun subStruct_gen(dst: hw_var, src : hw_param, subStruct_name : String) {
        var new_expr = hw_exec(OP2_SUBSTRUCT)
        new_expr.AddDst(dst)
        new_expr.AddParam(src)
        new_expr.subStructvar_name = subStruct_name
        AddExpr(new_expr)
    }

    fun subStruct(src : hw_var, subStruct_name : String) : hw_var {
        var dst_vartype = get_subStruct_type(src, subStruct_name)
        var genvar = hw_var(GetGenName("var"), dst_vartype, "0")
        subStruct_withgen(genvar, src, subStruct_name)
        return genvar
    }

    fun cnct(src : ArrayList<hw_param>): hw_var {
        return AddExpr_op(OPS_CNCT, src)
    }

    fun cnct(vararg srcs : hw_param): hw_var {
        var srcsList = ArrayList<hw_param>()
        for (src in srcs) srcsList.add(src)
        return cnct(srcsList)
    }

    fun mat_mul(src0: hw_var, src1: hw_var) : hw_var {
        if ((src0.vartype.dimensions.size != src1.vartype.dimensions.size) || (src0.vartype.dimensions.size != 3)) {
            ERROR("Dimensions error of matrix multiplication")
        }

        var ret_dim = hw_dim_static()
        ret_dim.add(31, 0)                  // TODO: calculate actual width
        ret_dim.add(src0.vartype.dimensions[2])
        ret_dim.add(src1.vartype.dimensions[1])

        var ret_var = ugenvar(GetGenName("matmul"), ret_dim, "0")

        mat_mul_gen(ret_var, src0, src1)

        return ret_var
    }

    ///////////////////////////////////

    fun complement_gen(dst : hw_var, src : hw_param) {
        AddExpr_op1_gen(OP1_COMPLEMENT, dst, src)
    }

    // implements adder tree
    fun op_tree_gen(opcode : hw_opcode, dst : hw_var, srcs : ArrayList<hw_param>) {
        if (srcs.size == 0) return
        else if (srcs.size == 1) {
            assign_gen(dst, srcs[0])
        } else if (srcs.size == 2) {
            AddExpr_op2_gen(opcode, dst, srcs[0], srcs[1])
        } else {
            var sum_inters = ArrayList<hw_param>()
            for (src in srcs) {
                val CUR_SRC_INDEX = srcs.indexOf(src)
                val NEXT_SRC_INDEX = CUR_SRC_INDEX + 1
                if (CUR_SRC_INDEX % 2 == 0) {
                    if (srcs.lastIndex < NEXT_SRC_INDEX) {
                        sum_inters.add(src)
                    } else {
                        var new_inter = AddExpr_op2(opcode, srcs[CUR_SRC_INDEX], srcs[CUR_SRC_INDEX+1])
                        AddGenVar(new_inter)
                        sum_inters.add(new_inter)
                    }
                }
            }
            op_tree_gen(opcode, dst, sum_inters)
        }
    }

    fun add_gen(dst : hw_var, srcs : ArrayList<hw_param>) {
        op_tree_gen(OP2_ARITH_ADD, dst, srcs)
    }

    fun add_gen(dst : hw_var, vararg srcs : hw_param) {
        var srcsList = ArrayList<hw_param>()
        for (src in srcs) srcsList.add(src)
        add_gen(dst, srcsList)
    }

    fun add_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_ARITH_ADD, dst, src0, src1)
    }

    fun sub_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_ARITH_SUB, dst, src0, src1)
    }

    fun sub_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_ARITH_SUB, dst, src0, src1)
    }

    fun mul_gen(dst : hw_var, srcs : ArrayList<hw_param>) {
        op_tree_gen(OP2_ARITH_MUL, dst, srcs)
    }

    fun mul_gen(dst : hw_var, vararg srcs : hw_param) {
        var srcsList = ArrayList<hw_param>()
        for (src in srcs) srcsList.add(src)
        mul_gen(dst, srcsList)
    }

    fun mul_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_ARITH_MUL, dst, src0, src1)
    }

    fun div_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_ARITH_DIV, dst, src0, src1)
    }

    fun div_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_ARITH_DIV, dst, src0, src1)
    }

    fun mod_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_ARITH_MOD, dst, src0, src1)
    }

    fun mod_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_ARITH_MOD, dst, src0, src1)
    }

    fun sll_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_ARITH_SLL, dst, src0, src1)
    }

    fun sll_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_ARITH_SLL, dst, src0, src1)
    }

    fun srl_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_ARITH_SRL, dst, src0, src1)
    }

    fun srl_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_ARITH_SRL, dst, src0, src1)
    }

    fun sra_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_ARITH_SRA, dst, src0, src1)
    }

    fun sra_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_ARITH_SRA, dst, src0, src1)
    }

    fun lnot_gen(dst : hw_var, src : hw_param) {
        AddExpr_op1_gen(OP1_LOGICAL_NOT, dst, src)
    }

    fun land_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_LOGICAL_AND, dst, src0, src1)
    }

    fun land_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_LOGICAL_AND, dst, src0, src1)
    }

    fun lor_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_LOGICAL_OR, dst, src0, src1)
    }

    fun lor_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_LOGICAL_OR, dst, src0, src1)
    }

    fun grt_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_LOGICAL_G, dst, src0, src1)
    }

    fun grt_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_LOGICAL_G, dst, src0, src1)
    }

    fun less_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_LOGICAL_L, dst, src0, src1)
    }

    fun less_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_LOGICAL_L, dst, src0, src1)
    }

    fun geq_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_LOGICAL_GEQ, dst, src0, src1)
    }

    fun geq_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_LOGICAL_GEQ, dst, src0, src1)
    }

    fun leq_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_LOGICAL_LEQ, dst, src0, src1)
    }

    fun leq_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_LOGICAL_LEQ, dst, src0, src1)
    }

    fun eq2_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_LOGICAL_EQ2, dst, src0, src1)
    }

    fun eq2_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_LOGICAL_EQ2, dst, src0, src1)
    }

    fun neq2_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_LOGICAL_NEQ2, dst, src0, src1)
    }

    fun neq2_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_LOGICAL_NEQ2, dst, src0, src1)
    }

    fun eq4_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_LOGICAL_EQ4, dst, src0, src1)
    }

    fun eq4_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_LOGICAL_EQ4, dst, src0, src1)
    }

    fun neq4_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_LOGICAL_NEQ4, dst, src0, src1)
    }

    fun neq4_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_LOGICAL_NEQ4, dst, src0, src1)
    }

    fun bnot_gen(dst : hw_var, src : hw_param){
        AddExpr_op1_gen(OP1_BITWISE_NOT, dst, src)
    }

    fun band_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_BITWISE_AND, dst, src0, src1)
    }

    fun band_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_BITWISE_AND, dst, src0, src1)
    }

    fun bor_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_BITWISE_OR, dst, src0, src1)
    }

    fun bor_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_BITWISE_OR, dst, src0, src1)
    }

    fun bxor_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_BITWISE_XOR, dst, src0, src1)
    }

    fun bxor_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_BITWISE_XOR, dst, src0, src1)
    }

    fun bxnor_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP2_BITWISE_XNOR, dst, src0, src1)
    }

    fun bxnor_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP2_BITWISE_XNOR, dst, src0, src1)
    }

    fun rand_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP1_REDUCT_AND, dst, src0, src1)
    }

    fun rand_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP1_REDUCT_AND, dst, src0, src1)
    }

    fun rnand_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP1_REDUCT_NAND, dst, src0, src1)
    }

    fun rnand_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP1_REDUCT_NAND, dst, src0, src1)
    }

    fun ror_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP1_REDUCT_OR, dst, src0, src1)
    }

    fun ror_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP1_REDUCT_OR, dst, src0, src1)
    }

    fun rnor_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP1_REDUCT_NOR, dst, src0, src1)
    }

    fun rnor_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP1_REDUCT_NOR, dst, src0, src1)
    }

    fun rxor_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP1_REDUCT_XOR, dst, src0, src1)
    }

    fun rxor_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP1_REDUCT_XOR, dst, src0, src1)
    }

    fun rxnor_gen(dst : hw_var, src0 : hw_param, src1 : hw_param) {
        AddExpr_op2_gen(OP1_REDUCT_XNOR, dst, src0, src1)
    }

    fun rxnor_gen(dst : hw_var, src0 : hw_param, src1 : Int) {
        AddExpr_op2_gen(OP1_REDUCT_XNOR, dst, src0, src1)
    }

    fun indexed_gen(dst : hw_var, src : hw_param, index: hw_param) {
        AddExpr_op2_gen(OP2_INDEXED, dst, src, index)
    }

    fun indexed_gen(dst : hw_var, src : hw_param, index: Int) {
        AddExpr_op2_gen(OP2_INDEXED, dst, src, index)
    }

    fun ranged_gen(dst : hw_var, src : hw_param, msb: hw_param, lsb: hw_param) {
        AddExpr_op3_gen(OP3_RANGED, dst, src, msb, lsb)
    }

    fun ranged_gen(dst : hw_var, src : hw_param, msb: Int, lsb: Int) {
        AddExpr_op3_gen(OP3_RANGED, dst, src, hw_imm(msb), hw_imm(lsb))
    }

    fun cnct_gen(dst : hw_var, src : ArrayList<hw_param>) {
        AddExpr_op_gen(OPS_CNCT, dst, src)
    }

    fun zeroext(src : hw_param, dst_width : Int): hw_var {
        if (src.vartype.dimensions.size > 1) ERROR("zeroext op dimensions error")
        val src_width = src.vartype.dimensions[0].GetWidth()

        var cnct_params = ArrayList<hw_param>()
        if (dst_width > src_width) {
            val ext_imm = hw_imm(dst_width - src_width, "0")
            cnct_params.add(ext_imm)
            cnct_params.add(src)
            return AddExpr_op(OPS_CNCT, cnct_params)
        } else {
            return AddExpr_op3(OP3_RANGED, src, hw_imm(dst_width-1), hw_imm(0))
        }
    }

    fun mat_mul_gen(dst: hw_var, src0: hw_var, src1: hw_var) {
        if ((src0.vartype.dimensions.size != src1.vartype.dimensions.size) || (src0.vartype.dimensions.size != 3)) {
            ERROR("Dimensions error of matrix multiplication")
        }
        for (dst_row_num in 0 until dst.vartype.dimensions[2].GetWidth()) {
            var dst_row = dst.GetFracRef(dst_row_num)

            for (dst_col_num in 0 until dst.vartype.dimensions[1].GetWidth()) {
                var dst_elem = dst_row.GetFracRef(dst_col_num)

                var factors = ArrayList<hw_param>()
                for (factor in 0 until src0.vartype.dimensions[1].GetWidth()) {
                    var scr0_row = src0.GetFracRef(dst_row_num)
                    var src0_elem = scr0_row.GetFracRef(factor)

                    var scr1_row = src1.GetFracRef(factor)
                    var src1_elem = scr1_row.GetFracRef(dst_col_num)

                    factors.add(mul(src0_elem, src1_elem))
                }
                add_gen(dst_elem, factors)
            }
        }
    }

    fun signext(src : hw_param, dst_width : Int): hw_var {
        if (src.vartype.dimensions.size > 1) ERROR("signext op dimensions error")
        val src_width = src.vartype.dimensions[0].GetWidth()

        var cnct_params = ArrayList<hw_param>()
        if (dst_width > src_width) {
            var src_var = src
            if (src is hw_var_frac) {           // hack against ranges sequence restriction in SV
                src_var = genvar(GetGenName("gensignext"), src.vartype, src.defimm)
                src_var.assign(src)
            }
            val sign_imm = indexed(src_var, src_var.vartype.dimensions[0].msb)
            for (i in 0 until (dst_width - src_width)) {
                cnct_params.add(sign_imm)
            }
            cnct_params.add(src)
            return AddExpr_op(OPS_CNCT, cnct_params)
        } else {
            return AddExpr_op3(OP3_RANGED, src, hw_imm(dst_width-1), hw_imm(0))
        }
    }

    fun clrif() {
        last().priority_conditions.clear()
    }

    fun begif(cond : hw_param) {
        last().priority_conditions.clear()

        var new_expr = hw_exec(OP1_IF)

        if (cond is hw_var) {
            val genvar = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
            new_expr.AddGenVar(genvar)
            assign_gen(genvar, cond)
            new_expr.AddParam(genvar)
            last().priority_conditions.add(genvar)
        } else {
            last().priority_conditions.add(cond)
        }

        AddExpr(new_expr)
        add(new_expr)
    }

    fun begelsif(cond : hw_param) {

        val curif_cond = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")

        // summing previous conditions - some of previous occurred
        assign(curif_cond, hw_imm(1, "0"))
        for (pcond in last().priority_conditions) {
            AddExpr_op2_gen(OP2_LOGICAL_OR, curif_cond, curif_cond, pcond)
        }

        // inverting - none of previous occurred
        AddExpr_op1_gen(OP1_LOGICAL_NOT, curif_cond, curif_cond)

        // final condition
        AddExpr_op2_gen(OP2_LOGICAL_AND, curif_cond, curif_cond, cond)

        var new_expr = hw_exec(OP1_IF)
        new_expr.AddParam(curif_cond)
        new_expr.AddGenVar(curif_cond)

        last().priority_conditions.add(curif_cond)

        AddExpr(new_expr)
        add(new_expr)
    }

    fun begelse() {

        val curif_cond = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")

        // summing previous conditions - some of previous occurred
        assign(curif_cond, hw_imm(1, "0"))
        for (pcond in last().priority_conditions) {
            AddExpr_op2_gen(OP2_LOGICAL_OR, curif_cond, curif_cond, pcond)
        }

        // inverting - none of previous occurred
        AddExpr_op1_gen(OP1_LOGICAL_NOT, curif_cond, curif_cond)

        var new_expr = hw_exec(OP1_IF)
        new_expr.AddParam(curif_cond)
        new_expr.AddGenVar(curif_cond)

        last().priority_conditions.add(curif_cond)

        AddExpr(new_expr)
        add(new_expr)
    }

    fun endif() {
        if (last().opcode != OP1_IF) ERROR("endif without begif!")
        for (wrvar in last().wrvars) {
            for (cur_exec in this) {
                cur_exec.AddIfTargetVar(wrvar)
            }
        }
        removeAt(lastIndex)
    }

    fun begcase(cond : hw_param) {
        var new_expr = hw_exec(OP1_CASE)
        new_expr.AddParam(cond)
        AddExpr(new_expr)
        add(new_expr)
    }

    fun endcase() {
        if (last().opcode != OP1_CASE) ERROR("endcase without begcase!")
        for (wrvar in last().wrvars) {
            for (cur_exec in this) {
                cur_exec.AddIfTargetVar(wrvar)
            }
        }
        removeAt(lastIndex)
    }

    fun begbranch(cond : hw_param) {
        if (last().opcode != OP1_CASE) ERROR("begbranch without begcase!")
        var new_expr = hw_exec(OP1_CASEBRANCH)
        new_expr.AddParam(cond)
        AddExpr(new_expr)
        add(new_expr)
    }

    fun begbranch(cond : Int) {
        begbranch(hw_imm(cond))
    }

    fun begbranch(cond : String) {
        begbranch(hw_imm(cond))
    }

    fun endbranch() {
        if (last().opcode != OP1_CASEBRANCH) ERROR("endbranch without begbranch!")
        /*
        for (wrvar in last().wrvars) {
            for (cur_exec in this) {
                cur_exec.AddIfTargetVar(wrvar)
            }
        }
        */
        removeAt(lastIndex)
    }

    fun begwhile(cond : hw_param) {
        var new_expr = hw_exec(OP1_WHILE)
        new_expr.AddParam(cond)

        AddExpr(new_expr)
        add(new_expr)
    }

    data class for_loop_iteration(var iter_num : hw_var, var iter_elem: hw_var, var iter_num_next : hw_var)

    fun begforrange(elements : hw_param, start: hw_param, end: hw_param) : for_loop_iteration {

        var new_expr = hw_exec(OP1_WHILE)

        val iterations = elements.GetWidth()
        val genvar_iter_num = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, GetWidthToContain(iterations)-1, 0, "0")
        assign(genvar_iter_num, start)
        new_expr.AddGenVar(genvar_iter_num)

        val genvar_iter_cont = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        assign(genvar_iter_cont, 1)
        new_expr.AddGenVar(genvar_iter_cont)
        new_expr.AddParam(genvar_iter_cont)
        new_expr.while_trailer = WHILE_TRAILER.INCR_COUNTER

        val genvar_iter_num_next = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, GetWidthToContain(iterations)-1, 0, "0")
        new_expr.AddGenVar(genvar_iter_num_next)

        AddExpr(new_expr)
        add(new_expr)

        val genvar_iter_elem = indexed(elements, genvar_iter_num)

        begif(less(start, end))
        run {
            assign(genvar_iter_cont, less(genvar_iter_num, end))
            add_gen(genvar_iter_num_next, genvar_iter_num, 1)
        }; endif()
        begelse()
        run {
            assign(genvar_iter_cont, gr(genvar_iter_num, end))
            sub_gen(genvar_iter_num_next, genvar_iter_num, 1)
        }; endif()

        return for_loop_iteration(genvar_iter_num, genvar_iter_elem, genvar_iter_num_next)
    }

    fun begforrange_asc(elements : hw_param, start: hw_param, end: hw_param) : for_loop_iteration {

        var new_expr = hw_exec(OP1_WHILE)

        val iterations = elements.GetWidth()
        val genvar_iter_num = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, GetWidthToContain(iterations)-1, 0, "0")
        assign(genvar_iter_num, start)
        new_expr.AddGenVar(genvar_iter_num)

        val genvar_iter_cont = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        assign(genvar_iter_cont, 1)
        new_expr.AddGenVar(genvar_iter_cont)
        new_expr.AddParam(genvar_iter_cont)
        new_expr.while_trailer = WHILE_TRAILER.INCR_COUNTER

        val genvar_iter_num_next = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, GetWidthToContain(iterations)-1, 0, "0")
        new_expr.AddGenVar(genvar_iter_num_next)

        AddExpr(new_expr)
        add(new_expr)

        var genvar_iter_elem = DUMMY_VAR
        if (elements is hw_var) genvar_iter_elem = elements.GetFracRef(genvar_iter_num)
        else genvar_iter_elem = indexed(elements, genvar_iter_num)

        assign(genvar_iter_cont, less(genvar_iter_num, end))
        add_gen(genvar_iter_num_next, genvar_iter_num, 1)

        return for_loop_iteration(genvar_iter_num, genvar_iter_elem, genvar_iter_num_next)
    }

    fun begforrange_desc(elements : hw_param, start: hw_param, end: hw_param) : for_loop_iteration {

        var new_expr = hw_exec(OP1_WHILE)

        val iterations = elements.GetWidth()
        val genvar_iter_num = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, GetWidthToContain(iterations)-1, 0, "0")
        assign(genvar_iter_num, start)
        new_expr.AddGenVar(genvar_iter_num)

        val genvar_iter_cont = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        assign(genvar_iter_cont, 1)
        new_expr.AddGenVar(genvar_iter_cont)
        new_expr.AddParam(genvar_iter_cont)
        new_expr.while_trailer = WHILE_TRAILER.INCR_COUNTER

        val genvar_iter_num_next = hw_var(GetGenName("var"), DATA_TYPE.BV_UNSIGNED, GetWidthToContain(iterations)-1, 0, "0")
        new_expr.AddGenVar(genvar_iter_num_next)

        AddExpr(new_expr)
        add(new_expr)

        val genvar_iter_elem = indexed(elements, genvar_iter_num)

        assign(genvar_iter_cont, gr(genvar_iter_num, end))
        sub_gen(genvar_iter_num_next, genvar_iter_num, 1)

        return for_loop_iteration(genvar_iter_num, genvar_iter_elem, genvar_iter_num_next)
    }

    fun begforall_asc(elements : hw_param) : for_loop_iteration {
        return begforrange_asc(elements, hw_imm(elements.vartype.dimensions.last().lsb), hw_imm(elements.vartype.dimensions.last().msb))
    }

    fun begforall_desc(elements : hw_param) : for_loop_iteration {
        return begforrange_desc(elements, hw_imm(elements.vartype.dimensions.last().msb), hw_imm(elements.vartype.dimensions.last().lsb))
    }

    fun begforall(elements : hw_param, depth : Int) : for_loop_iteration {

        if (depth > 1) {
            // TODO
            var new_depth = depth-1
            ERROR("Deep bigforall not currently supported!")
            throw Exception()
        } else {
            // TODO
            ERROR("Deep bigforall not currently supported!")
            throw Exception()
        }
    }

    fun endloop() {
        if (last().opcode != OP1_WHILE) ERROR("Ending loop without beginning!")
        if (last().while_trailer == WHILE_TRAILER.INCR_COUNTER) {
            assign(last().genvars[0], last().genvars[2])
        }
        removeAt(lastIndex)
    }

    fun genvar(name : String, vartype : hw_type, defimm : hw_imm) : hw_var {
        var ret_var = hw_var(name, vartype, defimm)
        AddGenVar(ret_var)
        return ret_var
    }

    fun genvar(name : String, vartype : hw_type, defval : String) : hw_var {
        var ret_var = hw_var(name, vartype, defval)
        AddGenVar(ret_var)
        return ret_var
    }

    fun genvar(name : String, src_struct : hw_struct ,dimensions : hw_dim_static) : hw_var {
        var ret_var = hw_var(name, src_struct, dimensions)
        AddGenVar(ret_var)
        return ret_var
    }

    fun genvar(name : String, src_struct : hw_struct) : hw_var {
        var ret_var = hw_var(name, src_struct)
        AddGenVar(ret_var)
        return ret_var
    }

    fun ugenvar(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_UNSIGNED, dimensions, defval)
        AddGenVar(ret_var)
        return ret_var
    }

    fun ugenvar(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_UNSIGNED, msb, lsb, defval)
        AddGenVar(ret_var)
        return ret_var
    }

    fun ugenvar(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_UNSIGNED, defval)
        AddGenVar(ret_var)
        return ret_var
    }

    fun sgenvar(name : String, dimensions : hw_dim_static, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_SIGNED, dimensions, defval)
        AddGenVar(ret_var)
        return ret_var
    }

    fun sgenvar(name : String, msb: Int, lsb: Int, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_SIGNED, msb, lsb, defval)
        AddGenVar(ret_var)
        return ret_var
    }

    fun sgenvar(name : String, defval : String) : hw_var {
        var ret_var = hw_var(name, DATA_TYPE.BV_SIGNED, defval)
        AddGenVar(ret_var)
        return ret_var
    }

    data class bit_position(var found : hw_var, var position: hw_var)

    fun max0(datain : hw_var, group_size : Int) : bit_position {

        var datain_buf = genvar(GetGenName("datain_buf"), datain.vartype, datain.defimm)
        var found = ugenvar(GetGenName("flag"), 0, 0, "0")
        var position = ugenvar(GetGenName("position"), GetWidthToContain(datain_buf.vartype.dimensions[0].msb+1)-1, 0, "0")

        assign(datain_buf, datain)

        if (datain_buf.GetWidth() < (group_size + 1)) {
            for (bit_idx in datain_buf.vartype.dimensions[0].lsb .. datain_buf.vartype.dimensions[0].msb) {
                begif(!datain_buf.GetFracRef(bit_idx))
                run {
                    assign(found, 1)
                    assign(position, bit_idx)
                }; endif()
            }

        } else {

            var l_elem_lsb = datain_buf.vartype.dimensions[0].lsb
            var l_elem_width = ceil(datain_buf.GetWidth().toDouble() / 2 ).toInt()
            var l_elem_msb = (l_elem_lsb + l_elem_width - 1)

            var h_elem_lsb = l_elem_msb + 1
            var h_elem_width = datain_buf.GetWidth() - l_elem_width
            var h_elem_msb = datain_buf.vartype.dimensions[0].msb

            var bit_pos_l = max0(datain_buf.GetFracRef(l_elem_msb, l_elem_lsb), group_size)
            var bit_pos_h = max0(datain_buf.GetFracRef(h_elem_msb, h_elem_lsb), group_size)

            begif(bit_pos_l.found)
            run {
                assign(found, 1)
                assign(position, bit_pos_l.position)
            }; endif()

            begif(bit_pos_h.found)
            run {
                assign(found, 1)
                assign(position, bit_pos_h.position)
            }; endif()

        }

        return bit_position(found, position)
    }

    fun min0(datain : hw_var, group_size : Int) : bit_position {

        var datain_buf = genvar(GetGenName("datain_buf"), datain.vartype, datain.defimm)
        var found = ugenvar(GetGenName("flag"), 0, 0, "0")
        var position = ugenvar(GetGenName("position"), GetWidthToContain(datain_buf.vartype.dimensions[0].msb+1)-1, 0, "0")

        assign(datain_buf, datain)

        if (datain_buf.GetWidth() < (group_size + 1)) {
            for (bit_idx in datain_buf.vartype.dimensions[0].msb downTo datain_buf.vartype.dimensions[0].lsb) {
                begif(!datain_buf.GetFracRef(bit_idx))
                run {
                    assign(found, 1)
                    assign(position, bit_idx)
                }; endif()
            }

        } else {

            var l_elem_lsb = datain_buf.vartype.dimensions[0].lsb
            var l_elem_width = ceil(datain_buf.GetWidth().toDouble() / 2 ).toInt()
            var l_elem_msb = (l_elem_lsb + l_elem_width - 1)

            var h_elem_lsb = l_elem_msb + 1
            var h_elem_width = datain_buf.GetWidth() - l_elem_width
            var h_elem_msb = datain_buf.vartype.dimensions[0].msb

            var bit_pos_l = min0(datain_buf.GetFracRef(l_elem_msb, l_elem_lsb), group_size)
            var bit_pos_h = min0(datain_buf.GetFracRef(h_elem_msb, h_elem_lsb), group_size)

            begif(bit_pos_h.found)
            run {
                assign(found, 1)
                assign(position, bit_pos_h.position)
            }; endif()

            begif(bit_pos_l.found)
            run {
                assign(found, 1)
                assign(position, bit_pos_l.position)
            }; endif()

        }

        return bit_position(found, position)
    }

    fun max1(datain : hw_var, group_size : Int) : bit_position {

        var datain_buf = genvar(GetGenName("datain_buf"), datain.vartype, datain.defimm)
        var found = ugenvar(GetGenName("flag"), 0, 0, "0")
        var position = ugenvar(GetGenName("position"), GetWidthToContain(datain_buf.vartype.dimensions[0].msb+1)-1, 0, "0")

        assign(datain_buf, datain)

        if (datain_buf.GetWidth() < (group_size + 1)) {
            for (bit_idx in datain_buf.vartype.dimensions[0].lsb .. datain_buf.vartype.dimensions[0].msb) {
                begif(datain_buf.GetFracRef(bit_idx))
                run {
                    assign(found, 1)
                    assign(position, bit_idx)
                }; endif()
            }

        } else {

            var l_elem_lsb = datain_buf.vartype.dimensions[0].lsb
            var l_elem_width = ceil(datain_buf.GetWidth().toDouble() / 2 ).toInt()
            var l_elem_msb = (l_elem_lsb + l_elem_width - 1)

            var h_elem_lsb = l_elem_msb + 1
            var h_elem_width = datain_buf.GetWidth() - l_elem_width
            var h_elem_msb = datain_buf.vartype.dimensions[0].msb

            var bit_pos_l = max1(datain_buf.GetFracRef(l_elem_msb, l_elem_lsb), group_size)
            var bit_pos_h = max1(datain_buf.GetFracRef(h_elem_msb, h_elem_lsb), group_size)

            begif(bit_pos_l.found)
            run {
                assign(found, 1)
                assign(position, bit_pos_l.position)
            }; endif()

            begif(bit_pos_h.found)
            run {
                assign(found, 1)
                assign(position, bit_pos_h.position)
            }; endif()

        }

        return bit_position(found, position)
    }

    fun min1(datain : hw_var, group_size : Int) : bit_position {

        var datain_buf = genvar(GetGenName("datain_buf"), datain.vartype, datain.defimm)
        var found = ugenvar(GetGenName("flag"), 0, 0, "0")
        var position = ugenvar(GetGenName("position"), GetWidthToContain(datain_buf.vartype.dimensions[0].msb+1)-1, 0, "0")

        assign(datain_buf, datain)

        if (datain_buf.GetWidth() < (group_size + 1)) {
            for (bit_idx in datain_buf.vartype.dimensions[0].msb downTo datain_buf.vartype.dimensions[0].lsb) {
                begif(datain_buf.GetFracRef(bit_idx))
                run {
                    assign(found, 1)
                    assign(position, bit_idx)
                }; endif()
            }

        } else {

            var l_elem_lsb = datain_buf.vartype.dimensions[0].lsb
            var l_elem_width = ceil(datain_buf.GetWidth().toDouble() / 2 ).toInt()
            var l_elem_msb = (l_elem_lsb + l_elem_width - 1)

            var h_elem_lsb = l_elem_msb + 1
            var h_elem_width = datain_buf.GetWidth() - l_elem_width
            var h_elem_msb = datain_buf.vartype.dimensions[0].msb

            var bit_pos_l = min1(datain_buf.GetFracRef(l_elem_msb, l_elem_lsb), group_size)
            var bit_pos_h = min1(datain_buf.GetFracRef(h_elem_msb, h_elem_lsb), group_size)

            begif(bit_pos_h.found)
            run {
                assign(found, 1)
                assign(position, bit_pos_h.position)
            }; endif()

            begif(bit_pos_l.found)
            run {
                assign(found, 1)
                assign(position, bit_pos_l.position)
            }; endif()

        }

        return bit_position(found, position)

    }

    fun import_expr(debug_lvl : DEBUG_LEVEL, expr : hw_exec, context : import_expr_context, process_subexpr : (debug_lvl : DEBUG_LEVEL, astc_gen : hw_astc, expr : hw_exec, context : import_expr_context) -> Unit) {

        if ((expr.opcode == OP_COMMENT)) {
            COMMENT(expr.comment)

        } else if ((expr.opcode == OP1_ASSIGN)) {
            assign(TranslateVar(expr.dsts[0], context.var_dict), TranslateParam(expr.params[0], context.var_dict))

        } else if ((expr.opcode == OP2_ARITH_ADD)
                || (expr.opcode == OP2_ARITH_SUB)
                || (expr.opcode == OP2_ARITH_MUL)
                || (expr.opcode == OP2_ARITH_DIV)
                || (expr.opcode == OP2_ARITH_SLL)
                || (expr.opcode == OP2_ARITH_SRL)
                || (expr.opcode == OP2_ARITH_SRA)

                || (expr.opcode == OP1_LOGICAL_NOT)
                || (expr.opcode == OP2_LOGICAL_AND)
                || (expr.opcode == OP2_LOGICAL_OR)
                || (expr.opcode == OP2_LOGICAL_G)
                || (expr.opcode == OP2_LOGICAL_L)
                || (expr.opcode == OP2_LOGICAL_GEQ)
                || (expr.opcode == OP2_LOGICAL_LEQ)
                || (expr.opcode == OP2_LOGICAL_EQ2)
                || (expr.opcode == OP2_LOGICAL_NEQ2)
                || (expr.opcode == OP2_LOGICAL_EQ4)
                || (expr.opcode == OP2_LOGICAL_NEQ4)

                || (expr.opcode == OP1_COMPLEMENT)
                || (expr.opcode == OP1_BITWISE_NOT)
                || (expr.opcode == OP2_BITWISE_AND)
                || (expr.opcode == OP2_BITWISE_OR)
                || (expr.opcode == OP2_BITWISE_XOR)
                || (expr.opcode == OP2_BITWISE_XNOR)

                || (expr.opcode == OP1_REDUCT_AND)
                || (expr.opcode == OP1_REDUCT_NAND)
                || (expr.opcode == OP1_REDUCT_OR)
                || (expr.opcode == OP1_REDUCT_NOR)
                || (expr.opcode == OP1_REDUCT_XOR)
                || (expr.opcode == OP1_REDUCT_XNOR)

                || (expr.opcode == OP2_INDEXED)
                || (expr.opcode == OP3_RANGED)
                || (expr.opcode == OPS_CNCT)) {

            var params = ArrayList<hw_param>()
            for (param in expr.params) {
                params.add(TranslateParam(param, context.var_dict))
            }
            AddExpr_op_gen(expr.opcode, TranslateVar(expr.dsts[0], context.var_dict), params)

        } else if (expr.opcode == OP2_SUBSTRUCT) {
            subStruct_gen(
                    TranslateVar(expr.dsts[0], context.var_dict),
                    TranslateParam(expr.params[0], context.var_dict),
                    expr.subStructvar_name
            )

        } else if (expr.opcode == OP1_IF) {

            begif(TranslateParam(expr.params[0], context.var_dict))
            run {
                for (child_expr in expr.expressions) {
                    process_subexpr(debug_lvl, this, child_expr, context)
                }
            }; endif()

        } else if (expr.opcode == OP1_CASE) {

            begcase(TranslateParam(expr.params[0], context.var_dict))
            run {
                for (casebranch in expr.expressions) {
                    if (casebranch.opcode != OP1_CASEBRANCH) ERROR("non-branch op in case")
                    begbranch(TranslateParam(casebranch.params[0], context.var_dict))
                    for (subexpr in casebranch.expressions) {
                        process_subexpr(debug_lvl, this, subexpr, context)
                    }
                    endbranch()
                }
            }; endcase()

        } else if (expr.opcode == OP1_WHILE) {

            begwhile(TranslateParam(expr.params[0], context.var_dict))
            run {
                for (child_expr in expr.expressions) {
                    process_subexpr(debug_lvl, this, child_expr, context)
                }
            }; endloop()

        } else ERROR("Reconstruction of expression failed: opcode undefined: " + expr.opcode.default_string)
    }
}

open class hw_astc_stdif() : hw_astc() {

    var wrvars = mutableMapOf<String, hw_var>()
    var rdvars = mutableMapOf<String, hw_var>()

    var Ports = ArrayList<hw_port>()

    var fifo_ifs = mutableMapOf<String, hw_structvar>()
    var fifo_ins = ArrayList<hw_fifo_in>()
    var fifo_outs = ArrayList<hw_fifo_out>()

    fun getPortByName(name : String) : hw_port {
        for (port in Ports) {
            if (port.name == name) return port
        }
        ERROR("port " + name + " not found!")
        throw Exception()
    }

    fun getFifoByName(name : String) : hw_structvar {
        return fifo_ifs[name]!!
    }

    private fun add_port(new_port : hw_port) {
        if (FROZEN_FLAG) ERROR("Failed to add port " + new_port.name + ": ASTC frozen")

        if (new_port.port_dir != PORT_DIR.IN) {
            if (wrvars.put(new_port.name, new_port) != null) {
                ERROR("Port addition problem!")
            }
            new_port.write_done = true
        }
        if (rdvars.put(new_port.name, new_port) != null) {
            ERROR("Port addition problem!")
        }
        Ports.add(new_port)
        new_port.default_astc = this
    }

    fun port(name : String, port_dir : PORT_DIR, vartype : hw_type, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, port_dir, vartype, defimm)
        add_port(ret_var)
        return ret_var
    }

    fun port(name : String, port_dir : PORT_DIR, vartype : hw_type, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, vartype, defval)
        add_port(ret_var)
        return ret_var
    }

    fun port(name : String, port_dir : PORT_DIR, src_struct : hw_struct, dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(src_struct, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun port(name : String, port_dir : PORT_DIR, src_struct : hw_struct) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(src_struct), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, dimensions : hw_dim_static, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, msb: Int, lsb: Int, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_UNSIGNED, defimm.imm_value), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uport(name : String, port_dir : PORT_DIR, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, dimensions : hw_dim_static, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, msb: Int, lsb: Int, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_SIGNED, defimm.imm_value), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun sport(name : String, port_dir : PORT_DIR, defval : String) : hw_port {
        var ret_var = hw_port(name, port_dir, hw_type(DATA_TYPE.BV_SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, vartype : hw_type, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, vartype, defimm)
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, vartype : hw_type, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, vartype, defval)
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, src_struct : hw_struct, dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(src_struct, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun input(name : String, src_struct : hw_struct) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(src_struct), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_UNSIGNED, defimm.imm_value), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uinput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_SIGNED, defimm.imm_value), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun sinput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.IN, hw_type(DATA_TYPE.BV_SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, vartype : hw_type, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, vartype, defimm)
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, vartype : hw_type, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, vartype, defval)
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, src_struct : hw_struct, dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(src_struct, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun output(name : String, src_struct : hw_struct) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(src_struct), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_UNSIGNED, defimm.imm_value), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uoutput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_SIGNED, defimm.imm_value), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun soutput(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.OUT, hw_type(DATA_TYPE.BV_SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, vartype : hw_type, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, vartype, defimm)
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, vartype : hw_type, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, vartype, defval)
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, src_struct : hw_struct, dimensions : hw_dim_static) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(src_struct, dimensions), "0")
        add_port(ret_var)
        return ret_var
    }

    fun inout(name : String, src_struct : hw_struct) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(src_struct), "0")
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, msb: Int, lsb: Int, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(DATA_TYPE.BV_UNSIGNED, defimm.imm_value), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun uinout(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(DATA_TYPE.BV_UNSIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinout(name : String, dimensions : hw_dim_static, defimm : hw_imm) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defimm)
        add_port(ret_var)
        return ret_var
    }

    fun sinout(name : String, dimensions : hw_dim_static, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(DATA_TYPE.BV_SIGNED, dimensions), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinout(name : String, msb: Int, lsb: Int, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb), defval)
        add_port(ret_var)
        return ret_var
    }

    fun sinout(name : String, defval : String) : hw_port {
        var ret_var = hw_port(name, PORT_DIR.INOUT, hw_type(DATA_TYPE.BV_SIGNED, defval), defval)
        add_port(ret_var)
        return ret_var
    }

    private fun add_fifo_in(new_fifo_in: hw_fifo_in) {
        if (FROZEN_FLAG) ERROR("Failed to add comb " + new_fifo_in.name + ": ASTC frozen")

        if (fifo_ifs.put(new_fifo_in.name, new_fifo_in) != null) {
            ERROR("FIFO addition problem!")
        }
        fifo_ins.add(new_fifo_in)
    }

    fun fifo_in(name : String, vartype : hw_type) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, vartype)
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name : String, src_struct : hw_struct ,dimensions : hw_dim_static) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(src_struct, dimensions))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun fifo_in(name : String, src_struct : hw_struct) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(src_struct))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun ufifo_in(name : String, dimensions : hw_dim_static) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun ufifo_in(name : String, msb: Int, lsb: Int) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun sfifo_in(name : String, dimensions : hw_dim_static) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun sfifo_in(name : String, msb: Int, lsb: Int) : hw_fifo_in {
        var ret_var = hw_fifo_in(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb))
        add_fifo_in(ret_var)
        return ret_var
    }

    fun add_fifo_out(new_fifo_out: hw_fifo_out) {
        if (FROZEN_FLAG) ERROR("Failed to add comb " + new_fifo_out.name + ": ASTC frozen")

        if (fifo_ifs.put(new_fifo_out.name, new_fifo_out) != null) {
            ERROR("FIFO addition problem!")
        }
        fifo_outs.add(new_fifo_out)
    }

    fun fifo_out(name : String, vartype : hw_type) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, vartype)
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name : String, src_struct : hw_struct ,dimensions : hw_dim_static) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(src_struct, dimensions))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_out(name : String, src_struct : hw_struct) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(src_struct))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun ufifo_out(name : String, dimensions : hw_dim_static) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(DATA_TYPE.BV_UNSIGNED, dimensions))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun ufifo_out(name : String, msb: Int, lsb: Int) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(DATA_TYPE.BV_UNSIGNED, msb, lsb))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun sfifo_out(name : String, dimensions : hw_dim_static) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(DATA_TYPE.BV_SIGNED, dimensions))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun sfifo_out(name : String, msb: Int, lsb: Int) : hw_fifo_out {
        var ret_var = hw_fifo_out(name, hw_type(DATA_TYPE.BV_SIGNED, msb, lsb))
        add_fifo_out(ret_var)
        return ret_var
    }

    fun fifo_wr_unblk(fifo : hw_fifo_out, wdata : hw_param) : hw_var {
        var new_expr = hw_exec_fifo_wr_unblk(fifo)
        var genvar = hw_var(GetGenName("fifo_rdy"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddParam(wdata)
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }

    fun fifo_rd_unblk(fifo : hw_fifo_in, rdata : hw_var) : hw_var {
        var new_expr = hw_exec_fifo_rd_unblk(fifo)
        var genvar = hw_var(GetGenName("fifo_rdy"), DATA_TYPE.BV_UNSIGNED, 0, 0, "0")
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        new_expr.AddDst(rdata)
        AddExpr(new_expr)
        return genvar
    }

    fun fifo_wr_blk(fifo : hw_fifo_out, wdata : hw_param) {
        var new_expr = hw_exec_fifo_wr_blk(fifo)
        new_expr.AddParam(wdata)
        AddExpr(new_expr)
    }

    fun fifo_rd_blk(fifo : hw_fifo_in) : hw_var {
        var new_expr = hw_exec_fifo_rd_blk(fifo)
        var genvar = hw_var(GetGenName("fifo_rdata"), fifo.vartype, fifo.defimm)
        new_expr.AddDst(genvar)
        new_expr.AddGenVar(genvar)
        AddExpr(new_expr)
        return genvar
    }
}