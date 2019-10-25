/*
 * hw_exec.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

class hw_opcode (val default_string : String)

val OP1_ASSIGN          = hw_opcode("=")

val OP2_ARITH_ADD       = hw_opcode("+")
val OP2_ARITH_SUB       = hw_opcode("-")
val OP2_ARITH_MUL       = hw_opcode("*")
val OP2_ARITH_DIV       = hw_opcode("/")
val OP2_ARITH_MOD       = hw_opcode("%")
val OP2_ARITH_SHL       = hw_opcode("<<")
val OP2_ARITH_SHR       = hw_opcode(">>")
val OP2_ARITH_SRA       = hw_opcode(">>>")

val OP1_COMPLEMENT      = hw_opcode("-")
val OP1_LOGICAL_NOT     = hw_opcode("!")
val OP2_LOGICAL_AND     = hw_opcode("&&")
val OP2_LOGICAL_OR      = hw_opcode("||")
val OP2_LOGICAL_G       = hw_opcode(">")
val OP2_LOGICAL_L       = hw_opcode("<")
val OP2_LOGICAL_GEQ     = hw_opcode(">=")
val OP2_LOGICAL_LEQ     = hw_opcode("<=")
val OP2_LOGICAL_EQ2     = hw_opcode("==")
val OP2_LOGICAL_NEQ2    = hw_opcode("!=")
val OP2_LOGICAL_EQ4     = hw_opcode("===")
val OP2_LOGICAL_NEQ4    = hw_opcode("!==")

val OP1_BITWISE_NOT     = hw_opcode("~")
val OP2_BITWISE_AND     = hw_opcode("&")
val OP2_BITWISE_OR      = hw_opcode("|")
val OP2_BITWISE_XOR     = hw_opcode("^")
val OP2_BITWISE_XNOR    = hw_opcode("^~")

val OP1_REDUCT_AND      = hw_opcode("&")
val OP1_REDUCT_NAND     = hw_opcode("~&")
val OP1_REDUCT_OR       = hw_opcode("|")
val OP1_REDUCT_NOR      = hw_opcode("~|")
val OP1_REDUCT_XOR      = hw_opcode("^")
val OP1_REDUCT_XNOR     = hw_opcode("^~")

val OP2_INDEXED         = hw_opcode("indexed")
val OP3_RANGED          = hw_opcode("ranged")
val OP2_SUBSTRUCT       = hw_opcode("subStruct")
val OPS_CNCT            = hw_opcode("cnct")
val OP1_IF              = hw_opcode("if")
val OP1_CASE            = hw_opcode("case")
val OP1_CASEBRANCH      = hw_opcode("casebrach")
val OP1_WHILE           = hw_opcode("while")


open class hw_exec(opcode_in : hw_opcode) {

    val opcode      = opcode_in

    var params      = ArrayList<hw_param>()
    var rdvars      = ArrayList<hw_var>()
    var wrvars      = ArrayList<hw_var>()
    var genvars     = ArrayList<hw_var>()

    var expressions     = ArrayList<hw_exec>()

    var fractions               = hw_fractions()
    var subStructvar_name       = "UNDEF"
    var iftargets   = ArrayList<hw_var>()
    var priority_conditions  = ArrayList<hw_param>()

    var cursor = 0

    fun ResetCursor() {
        cursor = expressions.size
    }

    fun SetCursor(new_val : Int) {
        cursor = new_val
    }

    fun AddWrVar(new_wrvar : hw_var) {
        new_wrvar.write_done = true
        if (!wrvars.contains(new_wrvar)) wrvars.add(new_wrvar)
    }

    fun AddRdVar(new_rdvar : hw_var) {
        new_rdvar.read_done = true
        if (!rdvars.contains(new_rdvar)) rdvars.add(new_rdvar)
    }

    fun AddGenVar(new_genvar : hw_var) {
        if (!genvars.contains(new_genvar)) genvars.add(new_genvar)
    }

    fun AddRdParam(new_param : hw_param) {
        params.add(new_param)
        if (new_param.type == PARAM_TYPE.VAR) AddRdVar(new_param as hw_var)
    }

    fun AddRdParams(new_params : ArrayList<hw_param>) {
        for(new_param in new_params) {
            AddRdParam(new_param)
        }
    }

    fun AddIfTargetVar(new_ifvar : hw_var) {
        if (!iftargets.contains(new_ifvar)) iftargets.add(new_ifvar)
    }
}
