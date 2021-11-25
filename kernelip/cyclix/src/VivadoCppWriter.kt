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

class VivadoCppWriter(var cyclix_module : Generic) {

    var tab_Counter = 0

    fun getDimString(in_range : hw_dim_range_static) : String
    {
        return ("[" + in_range.msb.toString() + ":" + in_range.lsb.toString() + "]")
    }

    fun getDimString(fraction : hw_frac) : String
    {
        if (fraction is hw_frac_C)
            return ("[" + fraction.index.GetString() + "]")

        else if (fraction is hw_frac_V)
            return ("[" + GetParamString(fraction.index) + "]")

        else if (fraction is hw_frac_CC)
            return ("[" + fraction.msb.GetString() + ":"
                    + fraction.lsb.GetString() + "]")

        else if (fraction is hw_frac_CV)
            return ("[" + fraction.msb.GetString() + ":"
                    + GetParamString(fraction.lsb) + "]")

        else if (fraction is hw_frac_VC)
            return ("[" + GetParamString(fraction.msb) + ":"
                    + fraction.lsb.GetString() + "]")

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
        for (i in fractions.lastIndex downTo 0) {
            ret_val += getDimString(fractions[i])
        }
        return ret_val
    }

    fun export_structvar(preambule : String, prename : String, structvar : hw_structvar, postString : String, wrFile : java.io.OutputStreamWriter) {
        var dimstring = ""
        if (structvar.vartype.DataType == DATA_TYPE.STRUCTURED) {
            if (!structvar.vartype.dimensions.isEmpty()) {
                for (dim in structvar.vartype.dimensions) {
                    dimstring += (" " + getDimString(dim))
                }
            }
            wrFile.write(preambule
                    + structvar.vartype.src_struct.name
                    + " "
                    + prename
                    + structvar.name
                    + dimstring
                    + postString)
        } else {
            var msb = 0
            var lsb = 0
            if (!structvar.vartype.dimensions.isEmpty()) {
                msb = structvar.vartype.dimensions[0].msb
                lsb = structvar.vartype.dimensions[0].lsb
            }
            if (lsb != 0) CRITICAL("lsb of variable " + structvar.name + " is not 0!")
            for (DIM_INDEX in 1 until structvar.vartype.dimensions.size) {
                if (structvar.vartype.dimensions[DIM_INDEX].lsb != 0) CRITICAL("lsb of variable " + structvar.name + " is not 0!")
                dimstring += (" [" + (structvar.vartype.dimensions[DIM_INDEX].msb + 1) + "]")
            }

            var typename = "ap_int"
            if (structvar.vartype.DataType == DATA_TYPE.BV_UNSIGNED) typename = "ap_uint"

            wrFile.write(preambule
                    + typename
                    + "<"
                    + (msb + 1)
                    + "> "
                    + prename
                    + structvar.name
                    + dimstring
                    + postString)
        }
    }

    fun export_stream(preambule : String, structvar : hw_structvar, postString : String, wrFile : java.io.OutputStreamWriter) {
        // TODO: dimensions cleanup
        if (structvar.vartype.DataType == DATA_TYPE.STRUCTURED) {
            wrFile.write(preambule + "<" + structvar.vartype.src_struct.name + ">& " + structvar.name + postString)
        } else {
            wrFile.write(preambule + "<ap_uint<" + structvar.vartype.dimensions[0].GetWidth() + "> >& " + structvar.name + postString)
        }
    }

    fun PrintTab(wrFile : java.io.OutputStreamWriter){
        for (i in 0 until tab_Counter) wrFile.write("\t")
    }

    fun append_cnct(expr : hw_exec, index : Int, str : String) : String {
        var cnct_string = ""
        if (index == expr.params.lastIndex) cnct_string = str + GetParamString(expr.params[index])
        else {
            // TODO: length cleanup
            var length = 0
            for (i in (index + 1) until expr.params.size) length += expr.params[i].GetWidth()
            cnct_string = str +
                    GetParamString(expr.params[index]) +
                    ".concat((ap_uint<" + length + ">)" + append_cnct(expr, (index + 1), cnct_string) + ")"
        }
        return cnct_string
    }

    fun export_expr(wrFile : java.io.OutputStreamWriter, expr : hw_exec)
    {
        PrintTab(wrFile)

        var opstring = ""

        if (expr.opcode == OP_COMMENT) 	        opstring = "// "

        else if (expr.opcode == OP1_ASSIGN) 	    opstring = ""
        else if (expr.opcode == OP1_COMPLEMENT) 	opstring = "-"

        else if (expr.opcode == OP2_ARITH_ADD) 	opstring = "+"
        else if (expr.opcode == OP2_ARITH_SUB) 	opstring = "-"
        else if (expr.opcode == OP2_ARITH_MUL) 	opstring = "*"
        else if (expr.opcode == OP2_ARITH_DIV) 	opstring = "/"
        else if (expr.opcode == OP2_ARITH_SLL) 	opstring = "<<"
        else if (expr.opcode == OP2_ARITH_SRL) 	opstring = ">>"
        else if (expr.opcode == OP2_ARITH_SRA) 	opstring = ">>"     // TODO: arith/logical cleanup

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

        else if (expr.opcode == OP_FIFO_WR_UNBLK) 	    opstring = ""
        else if (expr.opcode == OP_FIFO_RD_UNBLK) 	    opstring = ""

        else if (expr.opcode == OP_FIFO_WR_BLK) 	    opstring = ""
        else if (expr.opcode == OP_FIFO_RD_BLK) 	    opstring = ""

        else if (expr.opcode == OP_FIFO_INTERNAL_WR_UNBLK) 	    opstring = ""
        else if (expr.opcode == OP_FIFO_INTERNAL_RD_UNBLK) 	    opstring = ""

        else if (expr.opcode == OP_FIFO_INTERNAL_WR_BLK) 	    opstring = ""
        else if (expr.opcode == OP_FIFO_INTERNAL_RD_BLK) 	    opstring = ""

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
                    wrFile.write(GetParamString(expr.wrvars[0]) +
                            " = '{default:" +
                            GetParamString(expr.params[0]) +
                            "};\n")
                } else ERROR("assignment error")
            } else {
                wrFile.write(GetParamString(expr.wrvars[0]) +
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
            wrFile.write(GetParamString(expr.wrvars[0]) +
                    " = (" +
                    GetParamString(expr.params[0]) +
                    " " +
                    opstring +
                    " " +
                    GetParamString(expr.params[1]) +
                    ");\n")

        } else if (expr.opcode == OP2_INDEXED) {
            wrFile.write(GetParamString(expr.wrvars[0]) +
                    " = " +
                    expr.params[0].GetString() +
                    "[" +
                    expr.params[1].GetString() +
                    "];\n")

        } else if (expr.opcode == OP3_RANGED) {
            wrFile.write(GetParamString(expr.wrvars[0]) +
                    " = " +
                    expr.params[0].GetString() +
                    ".range(" +
                    expr.params[1].GetString() +
                    ", " +
                    expr.params[2].GetString() +
                    ");\n")

        } else if (expr.opcode == OP2_SUBSTRUCT) {
            wrFile.write(GetParamString(expr.wrvars[0]) +
                    " = " +
                    expr.params[0].GetString() +
                    "." +
                    expr.subStructvar_name +
                    ";\n")

        } else if (expr.opcode == OPS_CNCT) {

            var cnct_string = ""
            cnct_string = append_cnct(expr, 0, cnct_string)
            wrFile.write(GetParamString(expr.wrvars[0]) +
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

        } else if (expr.opcode == OP1_CASE) {

            wrFile.write("switch (" + expr.params[0].GetString() + ") {\n")
            tab_Counter++

            for (casebranch in expr.expressions) {
                PrintTab(wrFile)
                wrFile.write("case " + casebranch.params[0].GetString() + ":\n")
                tab_Counter++
                for (child_expr in casebranch.expressions) {
                    export_expr(wrFile, child_expr)
                }
                PrintTab(wrFile)
                wrFile.write("break;\n")
                tab_Counter--
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

        } else if (expr.opcode == OP_FIFO_WR_UNBLK) {
            wrFile.write(GetParamString(expr.wrvars[0]) + " = " + (expr as hw_exec_fifo_wr_unblk).fifo.name + ".write_nb(" + expr.params[0].GetString() + ");\n")

        } else if (expr.opcode == OP_FIFO_RD_UNBLK) {
            wrFile.write(GetParamString(expr.wrvars[0]) + " = " + (expr as hw_exec_fifo_rd_unblk).fifo.name + ".read_nb(" + expr.wrvars[1].name + ");\n")

        } else if (expr.opcode == OP_FIFO_WR_BLK) {
            wrFile.write((expr as hw_exec_fifo_wr_blk).fifo.name + ".write(" + GetParamString(expr.params[0]) + ");\n")

        } else if (expr.opcode == OP_FIFO_RD_BLK) {
            wrFile.write(GetParamString(expr.wrvars[0]) + " = " + (expr as hw_exec_fifo_rd_blk).fifo.name + ".read();\n")

        } else if (expr.opcode == OP_FIFO_INTERNAL_WR_UNBLK) {
            wrFile.write("OPERATION: OP_FIFO_INTERNAL_WR_UNBLK\n")

        } else if (expr.opcode == OP_FIFO_INTERNAL_RD_UNBLK) {
            wrFile.write("OPERATION: OP_FIFO_INTERNAL_RD_UNBLK\n")

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
            return param.GetString()
        } else {
            if ((param as hw_imm).dimensions.size > 1) throw Exception("cyclix: param print error")
            when ((param as hw_imm).base_type) {
                IMM_BASE_TYPE.DEC -> return("ap_uint<" + param.dimensions[0].GetWidth() + ">(" + param.imm_value + ")")
                IMM_BASE_TYPE.BIN -> return(param.GetWidth().toString() + "'b" + param.imm_value)
                IMM_BASE_TYPE.HEX -> return("0x" + param.imm_value)
                else -> {
                    ERROR("Imm type undefined!")
                    throw Exception()
                }
            }
        }
    }

    fun write_module(pathname : String) {
        val wrFileInterface = File(pathname + "/" + cyclix_module.name + ".hpp").writer()

        // writing header
        WriteGenSrcHeader(wrFileInterface, "HLS sources")

        wrFileInterface.write("#ifndef __" + cyclix_module.name +"_h_\n")
        wrFileInterface.write("#define __" + cyclix_module.name +"_h_\n")
        wrFileInterface.write("\n")
        wrFileInterface.write("#include <ap_int.h>\n\n")

        MSG("Exporting structs...")

        structsIfToPrint.clear()
        for (port in cyclix_module.Ports) {
            AddStructsIfToPrint(port)
        }
        for (fifo_if in cyclix_module.fifo_ifs) {
            AddStructsIfToPrint(fifo_if.value)
        }
        for (hw_struct in structsIfToPrint) {
            val STRUCT_DECL_STRING = "__genstructdel_" + hw_struct.value.name + "_"
            wrFileInterface.write("#ifndef " + STRUCT_DECL_STRING + "\n")
            wrFileInterface.write("#define " + STRUCT_DECL_STRING + "\n")
            wrFileInterface.write("typedef struct {\n")
            for (structvar in hw_struct.value) {
                export_structvar("\t", "", structvar, ";\n", wrFileInterface)
            }
            wrFileInterface.write("} " + hw_struct.value.name + ";\n")
            wrFileInterface.write("#endif // " + STRUCT_DECL_STRING + "\n\n")
        }
        wrFileInterface.write("#endif\n")
        wrFileInterface.close()
        MSG("done")

        // writing module
        val wrFileModule = File(pathname + "/" + cyclix_module.name + ".cpp").writer()

        // writing header
        WriteGenSrcHeader(wrFileModule, "HLS sources")

        MSG("Exporting modules and ports...")
        wrFileModule.write("#include <ap_int.h>\n")
        wrFileModule.write("#include <hls_stream.h>\n")
        wrFileModule.write("#include \"" + cyclix_module.name + ".hpp\"\n")
        wrFileModule.write("\n")

        MSG("done")

        structsInternalToPrint.clear()
        for (global in cyclix_module.globals) {
            AddStructsInternalToPrint(global)
        }
        for (local in cyclix_module.locals) {
            AddStructsInternalToPrint(local)
        }
        for (hw_struct in structsInternalToPrint) {
            wrFileModule.write("typedef struct {\n")
            for (structvar in hw_struct.value) {
                export_structvar("\t", "", structvar, ";\n", wrFileModule)
            }
            wrFileModule.write("} " + hw_struct.value.name + ";\n\n")
        }

        tab_Counter++

        // globals
        MSG("Exporting globals...")
        for (global in cyclix_module.globals) {
            export_structvar("", "", global, ";\n", wrFileModule)
        }
        wrFileModule.write("\n")
        MSG("done")

        // proc
        MSG("Exporting cyclix process...")
        wrFileModule.write("void " + cyclix_module.name + "(ap_uint<1> geninit")

        for (port in cyclix_module.Ports) {
            export_structvar(", volatile ", "*", port, "", wrFileModule)
        }
        for (fifo_in in cyclix_module.fifo_ins) {
            export_stream(", hls::stream", fifo_in, "", wrFileModule)
        }
        for (fifo_out in cyclix_module.fifo_outs) {
            export_stream(", hls::stream", fifo_out, "", wrFileModule)
        }

        wrFileModule.write(") {\n")
        wrFileModule.write("\n")

        /*
        for (fifo_in in cyclix_module.fifo_ins) {
            wrFileModule.write("#pragma HLS INTERFACE ap_fifo port=" + fifo_in.name + "\n")
        }
        for (fifo_out in cyclix_module.fifo_outs) {
            wrFileModule.write("#pragma HLS INTERFACE ap_fifo port=" + fifo_out.name + "\n")
        }
        wrFileModule.write("\n")
        */

        tab_Counter = 1

        for (subproc in cyclix_module.Subprocs) {
            wrFileModule.write("<SUBPROCESS>: " + subproc.value.inst_name + "\n") // TODO: interacting with submodules
        }
        wrFileModule.write("\n")

        for (local in cyclix_module.locals) {
            export_structvar("\t", "", local, ";\n", wrFileModule)
        }
        wrFileModule.write("\n")

        // generating global defaults
        wrFileModule.write("\tif (geninit) {\n")
        for (global in cyclix_module.globals) {
            if (global.vartype.dimensions.size < 2) {
                wrFileModule.write("\t\t" + global.name + " = " + GetParamString(global.defimm) + ";\n")
            } else if (global.vartype.dimensions.size == 2) {
                for (i in global.vartype.dimensions[1].lsb..global.vartype.dimensions[1].msb) {
                    if (global.defimm is hw_imm_arr) {
                        wrFileModule.write("\t\t" + global.name + "[" + i + "] = " + GetParamString((global.defimm as hw_imm_arr).subimms[i]) + ";\n")
                    } else {
                        wrFileModule.write("\t\t" + global.name + "[" + i + "] = " + GetParamString(global.defimm) + ";\n")
                    }
                }
            } else {
                throw Exception("cyclix: default value assignment error to value " + global.name)
            }
        }
        wrFileModule.write("\n\t} else {\n")
        tab_Counter = 2

        // buffering globals
        for (global_assoc in cyclix_module.__global_buf_assocs) {
            wrFileModule.write(
                GetParamString(global_assoc.value) +
                    " = " +
                    GetParamString(global_assoc.key) +
                    ";\n")
        }

        // Reading streaming input data
        if (cyclix_module is Streaming) {
            wrFileModule.write("\t\t" + (cyclix_module as Streaming).stream_req_var.name + " = " + (cyclix_module as Streaming).stream_req_bus.name + ".read();\n")
        }

        for (expression in cyclix_module.proc.expressions) {
            export_expr(wrFileModule, expression)
        }

        // Writing streaming output data
        if (cyclix_module is Streaming) {
            wrFileModule.write("\t\t" + (cyclix_module as Streaming).stream_resp_bus.name + ".write(" + (cyclix_module as Streaming).stream_resp_var.name + ");\n")
        }

        tab_Counter = 0
        wrFileModule.write("\t}\n}\n")
        MSG("done")

        wrFileModule.close()
    }

    fun write(pathname : String, debug_lvl : DEBUG_LEVEL) {

        // writing interface structures
        // TODO: restrict to interfaces
        File(pathname).mkdirs()
        for (subproc in cyclix_module.Subprocs) {
            subproc.value.src_module.export_to_vivado_cpp(pathname + "/" + subproc.value.src_module.name, debug_lvl)
        }
        write_module(pathname)
    }
}