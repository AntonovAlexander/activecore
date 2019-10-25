/*
 * SvWriter.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package rtl

import hwast.*
import java.io.File

class SvWriter(module_in : module) {

    var mod = module_in
    var tab_Counter = 0

    fun getStringWithDim(param : hw_param) : String
    {
        if (param.type == PARAM_TYPE.VAR) return param.token_printable
        else return (param.vartype.dimensions[0].GetWidth().toString() + "'d" + param.token_printable)
    }

    fun getStringAssignDefval(tgt: hw_var, src : String) : String
    {
        if (tgt.vartype.VarType == VAR_TYPE.STRUCTURED) {
            return "'{default:" + src + "}"
        } else {
            return src
        }
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
        for (i in 0 until fractions.size) {
            ret_val += getDimString(fractions[i])
        }
        return ret_val
    }

    fun export_structvar(preambule_uncond : String, preambule_cond : String, trailer : String, structvar : hw_structvar, wrFile : java.io.OutputStreamWriter) {
        var dimstring = ""
        if (structvar.vartype.VarType == VAR_TYPE.STRUCTURED) {
            if (!structvar.vartype.dimensions.isSingle()) {
                for (dim in structvar.vartype.dimensions) {
                    dimstring += ("" + getDimString(dim))
                }
            }
            wrFile.write(preambule_uncond
                    + structvar.vartype.src_struct.name
                    + " "
                    + structvar.name
                    + dimstring
                    + trailer)
        } else {
            if (structvar.vartype.dimensions.size > 0) {
                for (DIM_INDEX in 1 until structvar.vartype.dimensions.size) {
                    dimstring += (" [" + structvar.vartype.dimensions[DIM_INDEX].msb + ":" + structvar.vartype.dimensions[DIM_INDEX].lsb + "]")
                }
                wrFile.write(preambule_uncond
                        + preambule_cond
                        + "["
                        + structvar.vartype.dimensions[0].msb.toString()
                        + ":"
                        + structvar.vartype.dimensions[0].lsb.toString()
                        + "] "
                        + structvar.name
                        + dimstring
                        + trailer)
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
        else if (expr.opcode == OP1_COMPLEMENT) 	    opstring = "-"

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
        else if (expr.opcode == OP1_REDUCT_OR) 	opstring = "|"
        else if (expr.opcode == OP1_REDUCT_NOR) 	opstring = "~|"
        else if (expr.opcode == OP1_REDUCT_XOR) 	opstring = "^"
        else if (expr.opcode == OP1_REDUCT_XNOR) 	opstring = "^~"

        else if (expr.opcode == OP2_INDEXED) 	    opstring = ""
        else if (expr.opcode == OP3_RANGED) 	    opstring = ""
        else if (expr.opcode == OP2_SUBSTRUCT) 	opstring = ""
        else if (expr.opcode == OPS_CNCT) 	        opstring = ""
        else if (expr.opcode == OP1_IF) 	        opstring = ""
        else if (expr.opcode == OP1_CASE) 	        opstring = ""
        else if (expr.opcode == OP1_CASEBRANCH) 	opstring = ""
        else if (expr.opcode == OP1_WHILE) 	    opstring = ""

        else ERROR("operation " + expr.opcode.default_string + " not recognized")

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
            wrFile.write("if (" + expr.params[0].GetString() + ")\n")
            tab_Counter++
            PrintTab(wrFile)
            wrFile.write("begin\n")
            for (child_expr in expr.expressions) {
                export_expr(wrFile, child_expr)
            }
            PrintTab(wrFile)
            wrFile.write("end\n")
            tab_Counter--

        } else if (expr.opcode == OP1_CASE) {

            wrFile.write("case (" + expr.params[0].GetString() + ")\n")
            tab_Counter++

            for (casebranch in expr.expressions) {
                PrintTab(wrFile)
                wrFile.write(casebranch.params[0].GetString() + ":\n")
                tab_Counter++
                PrintTab(wrFile)
                wrFile.write("begin\n")
                for (child_expr in casebranch.expressions) {
                    export_expr(wrFile, child_expr)
                }
                PrintTab(wrFile)
                wrFile.write("end\n")
                tab_Counter--
            }
            tab_Counter--
            PrintTab(wrFile)
            wrFile.write("endcase\n")

        } else if (expr.opcode == OP1_WHILE) {
            wrFile.write("while (" + expr.params[0].GetString() + " == 1'b1)\n")
            tab_Counter++
            PrintTab(wrFile)
            wrFile.write("begin\n")
            for (child_expr in expr.expressions) {
                export_expr(wrFile, child_expr)
            }
            PrintTab(wrFile)
            wrFile.write("end\n")
            tab_Counter--

        } else ERROR("undefined opcode")
    }

    fun write(pathname : String) {

        // writing interface structures
        println("Exporting structs...")

        File(pathname).mkdirs()
        val wrFileInterface = File(pathname + "/" + mod.name + ".svh").writer()
        wrFileInterface.write("`ifndef __" + mod.name +"_h_\n")
        wrFileInterface.write("`define __" + mod.name +"_h_\n")
        wrFileInterface.write("\n")

        for (hw_struct in mod.hw_if_structs) {
            wrFileInterface.write("typedef struct packed {\n")
            for (structvar in hw_struct) {
                export_structvar("\t", "logic ", ";\n", structvar, wrFileInterface)
            }
            wrFileInterface.write("} " + hw_struct.name + ";\n\n")
        }
        wrFileInterface.write("`endif\n")
        wrFileInterface.close()
        println("done")

        // writing module
        val wrFileModule = File(pathname + "/" + mod.name + ".sv").writer()

        // Submodules
        println("Exporting submodules...")
        for (submodule in mod.Submodules) {
            submodule.value.src_module.export_to_sv(pathname + "/" + submodule.value.src_module.name)
        }
        println("done")

        println("Exporting modules and ports...")
        var mods_included = ArrayList<String>()
        for (incl in mod.Include_filenames) {
            if (!mods_included.contains(incl)) {
                mods_included.add(incl)
            }
        }
        mods_included.add(mod.name)
        for (submodule in mod.Submodules) {
            if (!mods_included.contains(submodule.value.src_module.name)) {
                mods_included.add(submodule.value.src_module.name)
            }
        }
        for (incl_file in mods_included) {
            wrFileModule.write("`include \"" + incl_file + ".svh\"\n")
        }

        wrFileModule.write("\n")
        wrFileModule.write("module " + mod.name + " (\n")
        var preambule = "\t"
        for (port in mod.Ports) {
            wrFileModule.write(preambule)
            var preambule_cond = "logic "
            var dir_string = ""
            //var assign_default_string = (" = " + getStringAssignDefval(port, port.defval))
            var assign_default_string = ""
            if (port.port_dir == PORT_DIR.IN) dir_string = "input"
            else if (port.port_dir == PORT_DIR.OUT) dir_string = "output"
            else if (port.port_dir == PORT_DIR.INOUT) dir_string = "inout"
            else ERROR("port type unrecognized!")
            export_structvar((dir_string + " "), preambule_cond, assign_default_string, port, wrFileModule)
            preambule = "\n\t, "
        }

        wrFileModule.write("\n);\n")
        wrFileModule.write("\n")

        println("done")

        tab_Counter++

        println("Exporting combinationals...")
        for (comb in mod.Combs) {
            export_structvar("", "logic ", ";\n", comb, wrFileModule)
        }
        wrFileModule.write("\n")

        println("done")

        // Mems
        println("Exporting mems...")
        for (mem in mod.Mems) {

            export_structvar("","logic ", ";\n", mem, wrFileModule)

            if (mem.mem_srcs.size == 0) throw Exception("Mem signals with no sources is not supported, mem signal: %s!\n")
            else {
                var reset_sensivity = ""
                if (mem.rst_present && (mem.rst_type == RST_TYPE.ASYNC)) reset_sensivity = (", " + mem.rst_signal.name)

                var reset_condition = ""
                if (mem.rst_present)
                {
                    if (mem.rst_lvl == SYNC_LVL.POS) reset_condition = (mem.rst_signal.name + " == 1")
                    else reset_condition = (mem.rst_signal.name + " == 0")
                }

                for (mem_src in mem.mem_srcs)
                {
                    var sync_sensivity = ""
                    if (mem.sync_type == SYNC_TYPE.EDGE)
                    {
                        if (mem_src.sync_lvl == SYNC_LVL.POS) sync_sensivity = "posedge "
                        else sync_sensivity = "negedge ";
                    }

                    if (mem.rst_present) {
                        wrFileModule.write("always @("
                                + sync_sensivity
                                + mem_src.sync_signal.name
                                + reset_sensivity
                                + ")\n")

                        wrFileModule.write("\tif ($reset_condition)\n")
                        wrFileModule.write("\t\tbegin\n")
                        if (mem.vartype.dimensions.size == 1) {
                            wrFileModule.write("\t\t" + mem.name + " <= " + mem.rst_src.GetString() +";\n")
                        } else if (mem.vartype.dimensions.size == 2) {
                            var power = mem.vartype.dimensions[1].GetWidth()
                            for (k in 0 until power) {
                                if (mem.rst_src.type == PARAM_TYPE.VAR)
                                    wrFileModule.write("\t\t"
                                            + mem.name
                                            + "["
                                            + (k + mem.vartype.dimensions[1].lsb)
                                            + "] <= "
                                            + mem.rst_src.GetString()
                                            + "["
                                            + (k + mem.rst_src.GetDimensions()[1].lsb)
                                            + "];\n")
                                else
                                    wrFileModule.write("\t\t"
                                            + mem.name
                                            + "["
                                            + (k + mem.vartype.dimensions[1].lsb)
                                            + "] <= "
                                            + mem.rst_src.GetString()
                                            + ";\n")
                            }
                        } else if (mem.vartype.dimensions.size > 2)
                            ERROR("Large dimensions for mems (reset) are currently not supported!\n")
                        else {
                            if (mem.vartype.VarType == VAR_TYPE.STRUCTURED) {
                                wrFileModule.write("\t\t"
                                        + mem.name
                                        + " <= '{default:"
                                        + mem.rst_src.GetString()
                                        + "};\n")
                            } else
                                ERROR("Undimensioned mems (reset) are currently not supported!\n")
                        }

                        wrFileModule.write("\t\tend\n")
                        wrFileModule.write("\telse\n")
                        wrFileModule.write("\t\tbegin\n")
                        if (mem.vartype.dimensions.size == 1) {
                            wrFileModule.write("\t\t"
                                    + mem.name
                                    + " <= "
                                    + mem_src.sync_src.GetString()
                                    + ";\n")
                        } else if (mem.vartype.dimensions.size == 2) {
                            var power = mem.vartype.dimensions[1].GetWidth()
                            for (k in 0 until power) {
                                if (mem_src.sync_src.type == PARAM_TYPE.VAR)
                                    wrFileModule.write("\t\t"
                                            + mem.name
                                            + "["
                                            + (k + mem.vartype.dimensions[1].lsb)
                                            + "] <= "
                                            + mem_src.sync_src.GetString()
                                            + "["
                                            + (k + mem_src.sync_src.GetDimensions()[1].lsb)
                                            + "];\n")
                                else
                                    wrFileModule.write("\t\t"
                                            + mem.name
                                            + "["
                                            + (k + mem.vartype.dimensions[1].lsb)
                                            + "] <= "
                                            + mem_src.sync_src.GetString()
                                            + ";\n")
                            }
                        } else if (mem.vartype.dimensions.size > 2)
                            ERROR("Large dimensions for mems (data) are currently not supported!\n")
                        else {
                            if (mem.vartype.VarType == VAR_TYPE.STRUCTURED) {
                                wrFileModule.write("\t\t"
                                        + mem.name
                                        + " <= "
                                        + mem_src.sync_src.GetString()
                                        + ";\n")
                            } else
                                ERROR("Undimensioned mems (data) are currently not supported!\n")
                        }
                        wrFileModule.write("\t\tend\n")

                    } else {
                        wrFileModule.write("always @("
                                + sync_sensivity
                                + mem_src.sync_signal.name
                                + reset_sensivity
                                + ") "
                                + mem.name
                                + " <= "
                                + mem_src.sync_src.GetString()
                                + ";\n")
                    }
                }
            }
            wrFileModule.write("\n")
        }
        wrFileModule.write("\n")
        println("done")

        // Submodule instantiations
        for (submodule in mod.Submodules) {
            var subm_preambule = ""
            wrFileModule.write(submodule.value.src_module.name + " " + submodule.value.inst_name + " (\n")
            for (conn in submodule.value.Connections) {
                wrFileModule.write("\t" + subm_preambule + "." + conn.key.name + "(" + conn.value.GetString() + ")\n")
                subm_preambule = ", "
            }
            wrFileModule.write(");\n\n")
        }

        // Cprocs
        println("Exporting cprocs...")
        for (cproc in mod.Cprocs) {
            wrFileModule.write("always @*\n")
            tab_Counter = 1
            PrintTab(wrFileModule)
            wrFileModule.write("begin\n")

            for (expression in cproc.expressions) {
                export_expr(wrFileModule, expression)
            }

            PrintTab(wrFileModule)
            wrFileModule.write("end\n")
            tab_Counter = 0
            wrFileModule.write("\n")
        }
        println("done")

        wrFileModule.write("\n")
        wrFileModule.write("endmodule\n")

        wrFileModule.close()
    }
}