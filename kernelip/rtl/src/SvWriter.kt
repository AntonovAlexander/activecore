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

class SvWriter(var mod : module) {

    var tab_Counter = 0

    fun getStringAssignDefval(tgt: hw_var, src : String) : String
    {
        if (tgt.vartype.DataType == DATA_TYPE.STRUCTURED) {
            return "'{default:" + src + "}"
        } else {
            return src
        }
    }

    fun getDimString(in_range : hw_dim_range_static) : String
    {
        return ("[" + in_range.msb.toString() + ":" + in_range.lsb.toString() + "]")
    }

    fun getDimString(fraction : hw_frac) : String
    {
        if (fraction is hw_frac_C)
            return ("[" + fraction.index.token_printable + "]")

        else if (fraction is hw_frac_V)
            return ("[" + GetParamString(fraction.index) + "]")

        else if (fraction is hw_frac_CC)
            return ("[" + fraction.msb.token_printable + ":"
                    + fraction.lsb.token_printable + "]")

        else if (fraction is hw_frac_CV)
            return ("[" + fraction.msb.token_printable + ":"
                    + GetParamString(fraction.lsb) + "]")

        else if (fraction is hw_frac_VC)
            return ("[" + GetParamString(fraction.msb) + ":"
                    + fraction.lsb.token_printable + "]")

        else if (fraction is hw_frac_VV)
            return ("[" + GetParamString(fraction.msb) + ":"
                    + GetParamString(fraction.lsb) + "]")

        else if (fraction is hw_frac_SubStruct)
            return ("." + fraction.substruct_name)

        else {
            ERROR("Dimensions unrecognized!")
            return ""
        }
    }

    fun getDimString(fractions : hw_fracs) : String
    {
        var ret_val = ""
        for (i in 0 until fractions.size) {
            ret_val += getDimString(fractions[i])
        }
        return ret_val
    }

    fun export_structvar(preambule_uncond : String, preambule_cond : String, trailer : String, structvar : hw_structvar, wrFile : java.io.OutputStreamWriter) {
        var dimstring = ""
        if (structvar.vartype.DataType == DATA_TYPE.STRUCTURED) {
            if (!structvar.vartype.dimensions.isEmpty()) {
                for (DIM_INDEX in structvar.vartype.dimensions.size-1 downTo 0) {
                    dimstring += (" " + getDimString(structvar.vartype.dimensions[DIM_INDEX]))
                }
            }
            wrFile.write(preambule_uncond
                    + structvar.vartype.src_struct.name
                    + " "
                    + structvar.name
                    + dimstring
                    + trailer)
        } else {
            var msb = 0
            var lsb = 0
            if (!structvar.vartype.dimensions.isEmpty()) {
                msb = structvar.vartype.dimensions[0].msb
                lsb = structvar.vartype.dimensions[0].lsb
            }
            for (DIM_INDEX in structvar.vartype.dimensions.size-1 downTo 1) {
                dimstring += (" [" + structvar.vartype.dimensions[DIM_INDEX].msb + ":" + structvar.vartype.dimensions[DIM_INDEX].lsb + "]")
            }
            var sign_string = ""
            if (structvar.vartype.DataType == DATA_TYPE.BV_UNSIGNED) sign_string = "unsigned "
            else sign_string = "signed "
            wrFile.write(preambule_uncond
                    + preambule_cond
                    + sign_string
                    + "["
                    + msb
                    + ":"
                    + lsb
                    + "] "
                    + structvar.name
                    + dimstring
                    + trailer)
        }
    }

    fun PrintTab(wrFile : java.io.OutputStreamWriter){
        for (i in 0 until tab_Counter) wrFile.write("\t")
    }

    fun export_expr(wrFile : java.io.OutputStreamWriter, expr : hw_exec)
    {
        PrintTab(wrFile)

        var opstring = ""

        if (expr.opcode == OP_COMMENT) 	            opstring = "// "

        else if (expr.opcode == OP1_ASSIGN) 	        opstring = ""
        else if (expr.opcode == OP1_COMPLEMENT) 	    opstring = "-"

        else if (expr.opcode == OP2_ARITH_ADD) 	    opstring = "+"
        else if (expr.opcode == OP2_ARITH_SUB) 	    opstring = "-"
        else if (expr.opcode == OP2_ARITH_MUL) 	    opstring = "*"
        else if (expr.opcode == OP2_ARITH_DIV) 	    opstring = "/"
        else if (expr.opcode == OP2_ARITH_SLL) 	    opstring = "<<"
        else if (expr.opcode == OP2_ARITH_SRL) 	    opstring = ">>"
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

        if (expr.opcode == OP_COMMENT) {
            wrFile.write(opstring + expr.comment + "\n")

        } else if ((expr.opcode == OP1_ASSIGN)
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
            if ((expr.dsts[0].vartype.DataType == DATA_TYPE.STRUCTURED) && (expr.params[0] is hw_imm)) {
                if (opstring == "") {
                    wrFile.write(GetParamString(expr.dsts[0]) +
                            " = '{default:" +
                            GetParamString(expr.params[0]) +
                            "};\n")
                } else ERROR("assignment error")
            } else {
                wrFile.write(GetParamString(expr.dsts[0]) +
                        " = " +
                        opstring +
                        GetParamString(expr.params[0]) +
                        ";\n")
            }

        } else if ((expr.opcode == OP2_ARITH_ADD)
                || (expr.opcode == OP2_ARITH_SUB)
                || (expr.opcode == OP2_ARITH_MUL)
                || (expr.opcode == OP2_ARITH_DIV)
                || (expr.opcode == OP2_ARITH_SLL)
                || (expr.opcode == OP2_ARITH_SRL)
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
            wrFile.write(GetParamString(expr.dsts[0]) +
                    " = (" +
                    GetParamString(expr.params[0]) +
                    " " +
                    opstring +
                    " " +
                    GetParamString(expr.params[1]) +
                    ");\n")

        } else if (expr.opcode == OP2_INDEXED) {
            wrFile.write(GetParamString(expr.dsts[0]) +
                    " = " +
                    GetParamString(expr.params[0]) +
                    "[" +
                    GetParamString(expr.params[1]) +
                    "];\n")

        } else if (expr.opcode == OP3_RANGED) {
            wrFile.write(GetParamString(expr.dsts[0]) +
                    " = " +
                    GetParamString(expr.params[0]) +
                    "[" +
                    GetParamString(expr.params[1]) +
                    ":" +
                    GetParamString(expr.params[2]) +
                    "];\n")

        } else if (expr.opcode == OP2_SUBSTRUCT) {
            wrFile.write(GetParamString(expr.dsts[0]) +
                    " = " +
                    GetParamString(expr.params[0]) +
                    "." +
                    expr.subStructvar_name +
                    ";\n")

        } else if (expr.opcode == OPS_CNCT) {
            var cnct_string = "{"
            for (i in 0 until expr.params.size) {
                if (i != 0) cnct_string += ", "
                cnct_string += GetParamString(expr.params[i])
            }
            cnct_string += "}"
            wrFile.write(GetParamString(expr.dsts[0]) +
                    " = " +
                    cnct_string +
                    ";\n")

        } else if (expr.opcode == OP1_IF) {
            wrFile.write("if (" + GetParamString(expr.params[0]) + ")\n")
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

            wrFile.write("case (" + GetParamString(expr.params[0]) + ")\n")
            tab_Counter++

            for (casebranch in expr.expressions) {
                PrintTab(wrFile)
                wrFile.write(GetParamString(casebranch.params[0]) + ":\n")
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
            wrFile.write("while (" + GetParamString(expr.params[0]) + " == 1'b1)\n")
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

    var structsIfToPrint = mutableMapOf<String, hw_struct>()
    fun AddStructsIfToPrint(param: hw_param) {
        if (param.vartype.DataType == DATA_TYPE.STRUCTURED) {
            if (!structsIfToPrint.containsKey(param.vartype.src_struct.name)) {
                for (structvar in param.vartype.src_struct) {
                    AddStructsIfToPrint(structvar)
                }
                structsIfToPrint.put(param.vartype.src_struct.name, param.vartype.src_struct)
            }
        }
    }
    var structsInternalToPrint = mutableMapOf<String, hw_struct>()
    fun AddStructsInternalToPrint(param: hw_param) {
        if (param.vartype.DataType == DATA_TYPE.STRUCTURED) {
            if (!structsIfToPrint.containsKey(param.vartype.src_struct.name) && !structsInternalToPrint.containsKey(param.vartype.src_struct.name)) {
                for (structvar in param.vartype.src_struct) {
                    AddStructsInternalToPrint(structvar)
                }
                structsInternalToPrint.put(param.vartype.src_struct.name, param.vartype.src_struct)
            }
        }
    }

    fun GetParamString(param : hw_param) : String {
        if (param is hw_var_frac) {
            var ret_string = param.name
            for (fraction in param.depow_fractions) ret_string += getDimString(fraction)
            return ret_string
        } else if (param is hw_var) {
            return param.name
        } else {
            when ((param as hw_imm).base_type) {
                IMM_BASE_TYPE.DEC -> return(param.GetWidth().toString() + "'d" + param.imm_value)
                IMM_BASE_TYPE.BIN -> return(param.GetWidth().toString() + "'b" + param.imm_value)
                IMM_BASE_TYPE.HEX -> return(param.GetWidth().toString() + "'h" + param.imm_value)
                else -> {
                    ERROR("Imm type undefined!")
                    throw Exception()
                }
            }
        }
    }

    fun write(pathname : String, debug_lvl : DEBUG_LEVEL) {

        // writing interface structures
        MSG("Exporting structs...")

        File(pathname).mkdirs()
        val wrFileInterface = File(pathname + "/" + mod.name + ".svh").writer()

        // writing header
        WriteGenSrcHeader(wrFileInterface, "RTL")

        wrFileInterface.write("`ifndef __" + mod.name +"_h_\n")
        wrFileInterface.write("`define __" + mod.name +"_h_\n")
        wrFileInterface.write("\n")

        structsIfToPrint.clear()
        for (port in mod.Ports) {
            AddStructsIfToPrint(port)
        }
        for (hw_struct in structsIfToPrint) {
            val STRUCT_DECL_STRING = "__genstructdel_" + hw_struct.value.name + "_"
            wrFileInterface.write("`ifndef " + STRUCT_DECL_STRING + "\n")
            wrFileInterface.write("`define " + STRUCT_DECL_STRING + "\n")
            wrFileInterface.write("typedef struct packed {\n")
            if (hw_struct.value.isEmpty()) hw_struct.value.addu("DUMMY", 0, 0, "0")
            for (structvar in hw_struct.value) {
                export_structvar("\t", "logic ", ";\n", structvar, wrFileInterface)
            }
            wrFileInterface.write("} " + hw_struct.value.name + ";\n")
            wrFileInterface.write("`endif // " + STRUCT_DECL_STRING + "\n\n")
        }
        wrFileInterface.write("`endif // __" + mod.name +"_h_\n")
        wrFileInterface.close()
        MSG("Exporting structs: done")

        // writing module
        val wrFileModule = File(pathname + "/" + mod.name + ".sv").writer()

        // writing header
        WriteGenSrcHeader(wrFileModule, "RTL")

        // Submodules
        MSG("Exporting submodules...")
        for (submodule in mod.Submodules) {
            if (!submodule.value.bb)
                submodule.value.src_module.export_to_sv(pathname + "/" + submodule.value.src_module.name, debug_lvl)
        }
        MSG("Exporting submodules: done")

        MSG("Exporting modules and ports...")
        var mods_included = ArrayList<String>()
        for (incl in mod.Include_filenames) {
            if (!mods_included.contains(incl)) {
                mods_included.add(incl)
            }
        }
        for (submodule in mod.Submodules) {
            if (submodule.value.include_needed)            // Workaround for HLS-generated modules, TODO: fix
                if (!mods_included.contains(submodule.value.src_module.name))
                    mods_included.add(submodule.value.src_module.name)
        }
        mods_included.add(mod.name)
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

        MSG("Exporting modules and ports: done")

        structsInternalToPrint.clear()
        for (comb in mod.Combs) {
            AddStructsInternalToPrint(comb)
        }
        for (mem in mod.Mems) {
            AddStructsInternalToPrint(mem)
        }
        for (hw_struct in structsInternalToPrint) {
            wrFileModule.write("typedef struct packed {\n")
            if (hw_struct.value.isEmpty()) hw_struct.value.addu("DUMMY", 0, 0, "0")
            for (structvar in hw_struct.value) {
                export_structvar("\t", "logic ", ";\n", structvar, wrFileModule)
            }
            wrFileModule.write("} " + hw_struct.value.name + ";\n\n")
        }
        wrFileModule.write("\n")

        tab_Counter++

        MSG("Exporting combinationals...")
        for (comb in mod.Combs) {
            export_structvar("", "logic ", ";\n", comb, wrFileModule)
        }
        wrFileModule.write("\n")

        MSG("Exporting combinationals: done")

        // Mems
        MSG("Exporting mems...")
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
                        if (mem.vartype.DataType == DATA_TYPE.STRUCTURED) {
                            wrFileModule.write("\t\t"
                                    + mem.name
                                    + " <= '{default:"
                                    + GetParamString(mem.rst_src)
                                    + "};\n")
                        } else if (mem.vartype.dimensions.size == 1) {
                            wrFileModule.write("\t\t" + mem.name + " <= " + GetParamString(mem.rst_src) +";\n")
                        } else if (mem.vartype.dimensions.size == 2) {
                            var power = mem.vartype.dimensions[1].GetWidth()
                            for (k in 0 until power) {
                                if (mem.rst_src is hw_var)
                                    wrFileModule.write("\t\t"
                                            + mem.name
                                            + "["
                                            + (k + mem.vartype.dimensions[1].lsb)
                                            + "] <= "
                                            + GetParamString(mem.rst_src)
                                            + "["
                                            + (k + mem.rst_src.GetDimensions()[1].lsb)
                                            + "];\n")
                                else if (mem.rst_src is hw_imm)
                                    if (mem.rst_src is hw_imm_arr) {
                                        wrFileModule.write("\t\t"
                                                + mem.name
                                                + "["
                                                + (k + mem.vartype.dimensions[1].lsb)
                                                + "] <= "
                                                + GetParamString((mem.rst_src as hw_imm_arr).subimms[k])
                                                + ";\n")
                                    } else {
                                        wrFileModule.write("\t\t"
                                                + mem.name
                                                + "["
                                                + (k + mem.vartype.dimensions[1].lsb)
                                                + "] <= "
                                                + GetParamString(mem.rst_src as hw_imm)
                                                + ";\n")
                                    }
                                else ERROR("mem.rst_src.type unrecognized!\n")
                            }
                        } else if (mem.vartype.dimensions.size > 2)
                            ERROR("Large dimensions for mems (reset) are currently not supported!\n")
                        else
                            ERROR("Undimensioned mems (reset) are currently not supported!\n")

                        wrFileModule.write("\t\tend\n")
                        wrFileModule.write("\telse\n")
                        wrFileModule.write("\t\tbegin\n")
                        if (mem.vartype.dimensions.size == 1) {
                            wrFileModule.write("\t\t"
                                    + mem.name
                                    + " <= "
                                    + GetParamString(mem_src.sync_src)
                                    + ";\n")
                        } else if (mem.vartype.dimensions.size == 2) {
                            var power = mem.vartype.dimensions[1].GetWidth()
                            for (k in 0 until power) {
                                if (mem_src.sync_src is hw_var)
                                    wrFileModule.write("\t\t"
                                            + mem.name
                                            + "["
                                            + (k + mem.vartype.dimensions[1].lsb)
                                            + "] <= "
                                            + GetParamString(mem_src.sync_src)
                                            + "["
                                            + (k + mem_src.sync_src.GetDimensions()[1].lsb)
                                            + "];\n")
                                else
                                    wrFileModule.write("\t\t"
                                            + mem.name
                                            + "["
                                            + (k + mem.vartype.dimensions[1].lsb)
                                            + "] <= "
                                            + GetParamString(mem_src.sync_src)
                                            + ";\n")
                            }
                        } else if (mem.vartype.dimensions.size > 2)
                            ERROR("Large dimensions for mems (data) are currently not supported!\n")
                        else {
                            if (mem.vartype.DataType == DATA_TYPE.STRUCTURED) {
                                wrFileModule.write("\t\t"
                                        + mem.name
                                        + " <= "
                                        + GetParamString(mem_src.sync_src)
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
                                + GetParamString(mem_src.sync_src)
                                + ";\n")
                    }
                }
            }
            wrFileModule.write("\n")
        }
        wrFileModule.write("\n")
        MSG("Exporting mems: done")

        // Submodule instantiations
        for (submodule in mod.Submodules) {
            var subm_preambule = ""
            wrFileModule.write(submodule.value.src_module.name + " " + submodule.value.inst_name + " (\n")
            for (conn in submodule.value.Connections) {
                wrFileModule.write("\t" + subm_preambule + "." + conn.key.name + "(" + GetParamString(conn.value) + ")\n")
                subm_preambule = ", "
            }
            wrFileModule.write(");\n\n")
        }

        // Cprocs
        MSG("Exporting cprocs...")
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
        MSG("Exporting cprocs: done")

        wrFileModule.write("\n")
        wrFileModule.write("endmodule\n")

        wrFileModule.close()
    }
}