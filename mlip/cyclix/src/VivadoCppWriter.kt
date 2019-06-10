/*
 * VivadoCppWriter.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*
import java.io.File

class VivadoCppWriter(module_in : module) {

    var mod = module_in
    var tab_Counter = 0

    fun getStringWithDim(param : hw_param) : String
    {
        // TODO width of imm values
        return param.token_printable
    }

    fun getDimString(in_range : hw_dim_range_static) : String
    {
        return ("[" + in_range.msb.toString() + ":" + in_range.lsb.toString() + "]")
    }

    fun getDimString(fraction : hw_fraction) : String
    {
        if (fraction.type == FRAC_TYPE.C)
            return ("[" + (fraction as hw_fraction_C).index.token_printable + "]")

        else if (fraction.type == FRAC_TYPE.V)
            return ("[" + (fraction as hw_fraction_V).index.name + "]")

        else if (fraction.type == FRAC_TYPE.CC)
            return ("[" + (fraction as hw_fraction_CC).msb.token_printable + ":"
                    + fraction.lsb.token_printable + "]")

        else if (fraction.type == FRAC_TYPE.CV)
            return ("[" + (fraction as hw_fraction_CV).msb.token_printable + ":"
                    + fraction.lsb.name + "]")

        else if (fraction.type == FRAC_TYPE.VC)
            return ("[" + (fraction as hw_fraction_VC).msb.name + ":"
                    + fraction.lsb.token_printable + "]")

        else if (fraction.type == FRAC_TYPE.VV)
            return ("[" + (fraction as hw_fraction_VV).msb.name + ":"
                    + fraction.lsb.name + "]")

        else if (fraction.type == FRAC_TYPE.SubStruct)
            return ("." + (fraction as hw_fraction_SubStruct).substruct_name)

        else {
            ERROR("Dimensions unrecognized!")
            return ""
        }
    }

    fun getDimString(fractions : hw_fractions) : String
    {
        var ret_val = ""
        for (i in fractions.lastIndex downTo 0) {
            ret_val += getDimString(fractions[i])
        }
        return ret_val
    }

    fun export_structvar(structvar : hw_structvar, wrFile : java.io.OutputStreamWriter) {
        var dimstring = ""
        if (structvar.vartype.VarType == VAR_TYPE.STRUCTURED) {
            if (!structvar.vartype.dimensions.isSingle()) {
                for (dim in structvar.vartype.dimensions) {
                    dimstring += ("" + getDimString(dim))
                }
            }
            wrFile.write("\t"
                    + structvar.vartype.src_struct.name
                    + " "
                    + structvar.name
                    + dimstring
                    + ";\n")
        } else {
            if (structvar.vartype.dimensions.size > 0) {
                if (structvar.vartype.dimensions[0].lsb != 0) CRITICAL("lsb of variable " + structvar.name + " is no 0!")
                for (DIM_INDEX in 1 until structvar.vartype.dimensions.size) {
                    if (structvar.vartype.dimensions[DIM_INDEX].lsb != 0) CRITICAL("lsb of variable " + structvar.name + " is no 0!")
                    dimstring += (" [" + (structvar.vartype.dimensions[DIM_INDEX].msb + 1) + "]")
                }

                var typename = "ap_int"
                if (structvar.vartype.VarType == VAR_TYPE.UNSIGNED) typename = "ap_uint"

                wrFile.write("\t"
                        + typename
                        + "<"
                        + (structvar.vartype.dimensions[0].msb + 1)
                        + "> "
                        + structvar.name
                        + dimstring
                        + ";\n")
            } else ERROR("Dimensions error")
        }
    }

    fun PrintTab(wrFile : java.io.OutputStreamWriter){
        for (i in 0 until tab_Counter) wrFile.write("\t")
    }

    fun export_expr(wrFile : java.io.OutputStreamWriter, expr : hw_exec)
    {
        PrintTab(wrFile)

        var dimstring = getDimString(expr.fractions)

        var opstring = ""
        if (expr.opcode == OP1_ASSIGN) 	            opstring = ""
        else if (expr.opcode == OP1_COMPLEMENT) 	opstring = "-"

        else if (expr.opcode == OP2_ARITH_ADD) 	    opstring = "+"
        else if (expr.opcode == OP2_ARITH_SUB) 	    opstring = "-"
        else if (expr.opcode == OP2_ARITH_MUL) 	    opstring = "*"
        else if (expr.opcode == OP2_ARITH_DIV) 	    opstring = "/"
        else if (expr.opcode == OP2_ARITH_SHL) 	    opstring = "<<"
        else if (expr.opcode == OP2_ARITH_SHR) 	    opstring = ">>"
        else if (expr.opcode == OP2_ARITH_SRA) 	    opstring = ">>>"

        else if (expr.opcode == OP1_LOGICAL_NOT)  opstring = "!"
        else if (expr.opcode == OP2_LOGICAL_AND)  opstring = "&&"
        else if (expr.opcode == OP2_LOGICAL_OR)   opstring = "||"
        else if (expr.opcode == OP2_LOGICAL_G)    opstring = ">"
        else if (expr.opcode == OP2_LOGICAL_L)    opstring = "<"
        else if (expr.opcode == OP2_LOGICAL_GEQ)  opstring = ">="
        else if (expr.opcode == OP2_LOGICAL_LEQ)  opstring = "<="
        else if (expr.opcode == OP2_LOGICAL_EQ2)  opstring = "=="
        else if (expr.opcode == OP2_LOGICAL_NEQ2) opstring = "!="
        else if (expr.opcode == OP2_LOGICAL_EQ4)  opstring = "==="
        else if (expr.opcode == OP2_LOGICAL_NEQ4) opstring = "!=="

        else if (expr.opcode == OP1_BITWISE_NOT) 	opstring = "~"
        else if (expr.opcode == OP2_BITWISE_AND) 	opstring = "&"
        else if (expr.opcode == OP2_BITWISE_OR) 	opstring = "|"
        else if (expr.opcode == OP2_BITWISE_XOR) 	opstring = "^"
        else if (expr.opcode == OP2_BITWISE_XNOR) 	opstring = "^~"

        else if (expr.opcode == OP1_REDUCT_AND) 	opstring = "&"
        else if (expr.opcode == OP1_REDUCT_NAND) 	opstring = "~&"
        else if (expr.opcode == OP1_REDUCT_OR) 	    opstring = "|"
        else if (expr.opcode == OP1_REDUCT_NOR) 	opstring = "~|"
        else if (expr.opcode == OP1_REDUCT_XOR) 	opstring = "^"
        else if (expr.opcode == OP1_REDUCT_XNOR) 	opstring = "^~"

        else if (expr.opcode == OP2_INDEXED) 	    opstring = ""
        else if (expr.opcode == OP3_RANGED) 	    opstring = ""
        else if (expr.opcode == OP2_SUBSTRUCT) 	    opstring = ""
        else if (expr.opcode == OPS_CNCT) 	        opstring = ""
        else if (expr.opcode == OP1_IF) 	        opstring = ""
        else if (expr.opcode == OP1_WHILE) 	        opstring = ""

        else if (expr.opcode == OP_FIFO_WR) 	    opstring = ""
        else if (expr.opcode == OP_FIFO_RD) 	    opstring = ""

        else ERROR("operation" + expr.opcode.default_string + " not recognized")

        if ((expr.opcode == OP1_ASSIGN)
            || (expr.opcode == OP1_COMPLEMENT)
            || (expr.opcode == OP1_LOGICAL_NOT)
            || (expr.opcode == OP1_BITWISE_NOT)
            || (expr.opcode == OP1_REDUCT_AND)
            || (expr.opcode == OP1_REDUCT_NAND)
            || (expr.opcode == OP1_REDUCT_OR)
            || (expr.opcode == OP1_REDUCT_NOR)
            || (expr.opcode == OP1_REDUCT_XOR)
            || (expr.opcode == OP1_REDUCT_XNOR))
        {
            var var_descr = expr.wrvars[0].GetDepowered(expr.fractions)
            if ((var_descr.VarType == VAR_TYPE.STRUCTURED) && (expr.params[0].type == PARAM_TYPE.VAL)) {
                if (opstring == "") {
                    wrFile.write(expr.wrvars[0].name +
                            dimstring +
                            " = '{default:" +
                            getStringWithDim(expr.params[0]) +
                            "};\n")
                } else ERROR("assignment error")
            } else {
                wrFile.write(expr.wrvars[0].name +
                        dimstring +
                        " = " +
                        opstring +
                        getStringWithDim(expr.params[0]) +
                        ";\n")
            }

        } else if ((expr.opcode == OP2_ARITH_ADD)
            || (expr.opcode == OP2_ARITH_SUB)
            || (expr.opcode == OP2_ARITH_MUL)
            || (expr.opcode == OP2_ARITH_DIV)
            || (expr.opcode == OP2_ARITH_SHL)
            || (expr.opcode == OP2_ARITH_SHR)
            || (expr.opcode == OP2_ARITH_SRA)

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

            || (expr.opcode == OP2_BITWISE_AND)
            || (expr.opcode == OP2_BITWISE_OR)
            || (expr.opcode == OP2_BITWISE_XOR)
            || (expr.opcode == OP2_BITWISE_XNOR)) {
            wrFile.write(expr.wrvars[0].name +
                    dimstring +
                    " = (" +
                    getStringWithDim(expr.params[0]) +
                    " " +
                    opstring +
                    " " +
                    getStringWithDim(expr.params[1]) +
                    ");\n")

        } else if (expr.opcode == OP2_INDEXED) {
            wrFile.write(expr.wrvars[0].name +
                    " = " +
                    expr.params[0].GetString() +
                    "[" +
                    expr.params[1].GetString() +
                    "];\n")

        } else if (expr.opcode == OP3_RANGED) {
            wrFile.write(expr.wrvars[0].name +
                    " = " +
                    expr.params[0].GetString() +
                    "[" +
                    expr.params[1].GetString() +
                    ":" +
                    expr.params[2].GetString() +
                    "];\n")

        } else if (expr.opcode == OP2_SUBSTRUCT) {
            wrFile.write(expr.wrvars[0].name +
                    " = " +
                    expr.params[0].GetString() +
                    "." +
                    expr.subStructvar_name +
                    ";\n")

        } else if (expr.opcode == OPS_CNCT) {
            var cnct_string = "{"
            for (i in 0 until expr.params.size) {
                if (i != 0) cnct_string += ", "
                cnct_string += getStringWithDim(expr.params[i])
            }
            cnct_string += "}"
            wrFile.write(expr.wrvars[0].name +
                    " = " +
                    cnct_string +
                    ";\n")

        } else if (expr.opcode == OP1_IF) {
            wrFile.write("if (" + expr.params[0].GetString() + ") {\n")
            tab_Counter++
            for (child_expr in expr.expressions) {
                export_expr(wrFile, child_expr)
            }
            tab_Counter--
            PrintTab(wrFile)
            wrFile.write("}\n")

        } else if (expr.opcode == OP1_WHILE) {
            wrFile.write("while (" + expr.params[0].GetString() + " == 1'b1) {\n")
            tab_Counter++
            for (child_expr in expr.expressions) {
                export_expr(wrFile, child_expr)
            }
            tab_Counter--
            PrintTab(wrFile)
            wrFile.write("}\n")

        } else if (expr.opcode == OP_FIFO_WR) {
            wrFile.write(expr.wrvars[0].name + " = " + (expr as hw_exec_fifo_wr).fifo.name + ".write(" + expr.params[0].GetString() + ")\n")

        } else if (expr.opcode == OP_FIFO_RD) {
            wrFile.write(expr.wrvars[0].name + " = " + (expr as hw_exec_fifo_rd).fifo.name + ".read(" + expr.wrvars[1].name + ")\n")

        } else ERROR("undefined opcode")
    }

    fun write(pathname : String) {

        // writing interface structures
        // TODO: restrict to interfaces
        File(pathname).mkdirs()
        val wrFileInterface = File(pathname + "/" + mod.name + ".hpp").writer()
        wrFileInterface.write("`ifndef __" + mod.name +"_h_\n")
        wrFileInterface.write("`define __" + mod.name +"_h_\n")
        wrFileInterface.write("\n")
        wrFileInterface.write("#include <ap_int.h>")
        wrFileInterface.write("\n")

        println("Exporting structs...")

        for (hw_struct in mod.hw_if_structs) {
            wrFileInterface.write("typedef struct {\n")
            for (structvar in hw_struct) {
                export_structvar(structvar, wrFileInterface)
            }
            wrFileInterface.write("} " + hw_struct.name + ";\n\n")
        }
        wrFileInterface.write("`endif\n")
        wrFileInterface.close()
        println("done")

        // writing module
        val wrFileModule = File(pathname + "/" + mod.name + ".cpp").writer()
        println("Exporting modules and ports...")
        wrFileModule.write("#include \"" + mod.name + ".svh\"\n")
        wrFileModule.write("#include <ap_int.h>\n")
        wrFileModule.write("#include <interfaces.hpp>\n")
        wrFileModule.write("\n")

        println("done")

        tab_Counter++

        // globals
        println("Exporting mems...")
        for (global in mod.globals) {
            export_structvar(global, wrFileModule)
        }
        wrFileModule.write("\n")
        println("done")

        // proc
        println("Exporting cyclix process...")

        var params = "ap_uint<1> geninit"
        var preambule = ", "
        for (port in mod.Ports) {
            params += (preambule + port.name)
        }
        for (fifo_in in mod.fifo_ins) {
            params += (preambule + fifo_in.name)
        }
        for (fifo_out in mod.fifo_outs) {
            params += (preambule + fifo_out.name)
        }

        wrFileModule.write("void " + mod.name + "(" + params + ") {\n")
        wrFileModule.write("\n")

        for (fifo_in in mod.fifo_ins) {
            params += (preambule + fifo_in.name)
            wrFileModule.write("#pragma HLS INTERFACE ap_fifo port=" + fifo_in.name + "\n")
        }
        for (fifo_out in mod.fifo_outs) {
            params += (preambule + fifo_out.name)
            wrFileModule.write("#pragma HLS INTERFACE ap_fifo port=" + fifo_out.name + "\n")
        }
        wrFileModule.write("\n")

        tab_Counter = 1

        for (local in mod.locals) {
            export_structvar(local, wrFileModule)
        }
        wrFileModule.write("\n")

        // generating global defaults
        wrFileModule.write("\tif (geninit) {\n")
        for (global in mod.globals) {
            wrFileModule.write("\t\t" + global.name + " = " + global.defval + ";\n")
        }
        wrFileModule.write("\t}\n\n")

        for (expression in mod.proc.expressions) {
            export_expr(wrFileModule, expression)
        }
        tab_Counter = 0
        wrFileModule.write("}\n")
        println("done")

        wrFileModule.write("\n")
        wrFileModule.write("endmodule\n")

        wrFileModule.close()
    }
}