/*
 * hw_exec.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

class hw_opcode (val default_string : String)

val OP_COMMENT          = hw_opcode("//")

val OP1_ASSIGN          = hw_opcode("=")

val OP2_ARITH_ADD       = hw_opcode("+")
val OP2_ARITH_SUB       = hw_opcode("-")
val OP2_ARITH_MUL       = hw_opcode("*")
val OP2_ARITH_DIV       = hw_opcode("/")
val OP2_ARITH_MOD       = hw_opcode("%")
val OP2_ARITH_SLL       = hw_opcode("<<")
val OP2_ARITH_SRL       = hw_opcode(">>")
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

enum class WHILE_TRAILER {
    EMPTY,	INCR_COUNTER
}


// container for operation
open class hw_exec(val opcode : hw_opcode) {

    var while_trailer = WHILE_TRAILER.EMPTY

    var params      = ArrayList<hw_param>()
    var dsts        = ArrayList<hw_var>()

    var rdvars      = ArrayList<hw_var>()
    var wrvars      = ArrayList<hw_var>()
    var genvars     = ArrayList<hw_var>()

    var expressions     = ArrayList<hw_exec>()

    var subStructvar_name       = "UNDEF"
    var iftargets   = ArrayList<hw_var>()
    var priority_conditions  = ArrayList<hw_param>()

    var comment = ""

    var cursor = 0

    fun ResetCursor() {
        cursor = expressions.size
    }

    fun SetCursor(new_val : Int) {
        cursor = new_val
    }

    fun AddWrVar(new_var : hw_var) {
        var real_var = new_var
        if (new_var is hw_var_frac) real_var = new_var.src_var
        if (!wrvars.contains(real_var)) wrvars.add(real_var)

        if (new_var is hw_var_frac) {
            for (depow_frac in new_var.depow_fractions) {
                if (depow_frac is hw_frac_V) AddRdVar(depow_frac.index)
                if (depow_frac is hw_frac_CV) AddRdVar(depow_frac.lsb)
                if (depow_frac is hw_frac_VC) AddRdVar(depow_frac.msb)
                if (depow_frac is hw_frac_VV) {
                    AddRdVar(depow_frac.lsb)
                    AddRdVar(depow_frac.msb)
                }
            }
        }

        real_var.write_done = true
    }

    fun AddRdVar(new_var : hw_var) {
        var real_var = new_var
        if (new_var is hw_var_frac) real_var = new_var.src_var
        if (!rdvars.contains(real_var)) rdvars.add(real_var)

        if (new_var is hw_var_frac) {
            for (depow_frac in new_var.depow_fractions) {
                if (depow_frac is hw_frac_V) AddRdVar(depow_frac.index)
                if (depow_frac is hw_frac_CV) AddRdVar(depow_frac.lsb)
                if (depow_frac is hw_frac_VC) AddRdVar(depow_frac.msb)
                if (depow_frac is hw_frac_VV) {
                    AddRdVar(depow_frac.lsb)
                    AddRdVar(depow_frac.msb)
                }
            }
        }

        real_var.read_done = true
    }

    fun AddGenVar(new_genvar : hw_var) {
        var added_var = new_genvar
        if (added_var is hw_var_frac) added_var = added_var.src_var
        if (!genvars.contains(added_var)) genvars.add(added_var)
    }

    fun AddParam(new_param : hw_param) {
        params.add(new_param)
        if (new_param is hw_var) AddRdVar(new_param)
    }

    fun AddParams(new_params : ArrayList<hw_param>) {
        for(new_param in new_params) AddParam(new_param)
    }

    fun AddDst(new_dst : hw_var) {
        dsts.add(new_dst)
        AddWrVar(new_dst)
    }

    fun AddDsts(new_dsts : ArrayList<hw_var>) {
        for (new_dst in new_dsts) AddDst(new_dst)
    }

    fun AddIfTargetVar(new_ifvar : hw_var) {
        if (!iftargets.contains(new_ifvar)) iftargets.add(new_ifvar)
    }
}
