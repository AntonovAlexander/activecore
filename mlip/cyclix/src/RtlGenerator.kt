/*
 * RtlGenerator.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*
import java.lang.Exception

class RtlGenerator(var cyclix_module : Generic) {

    class fifo_out_descr (val ext_req      : rtl.hw_port,
                          val ext_wdata    : rtl.hw_port,
                          val ext_ack      : rtl.hw_port,
                          var reqbuf_req   : hw_var)

    class fifo_in_descr  (val ext_req      : rtl.hw_port,
                          val ext_rdata    : rtl.hw_port,
                          val ext_ack      : rtl.hw_port,
                          var buf_req      : hw_var,
                          var buf_rdata    : hw_var)

    var var_dict        = mutableMapOf<hw_var, hw_var>()
    var fifo_out_dict   = mutableMapOf<hw_fifo_out, fifo_out_descr>()
    var fifo_in_dict    = mutableMapOf<hw_fifo_in, fifo_in_descr>()

    fun TranslateFifoOut(fifo : hw_fifo_out) : fifo_out_descr {
        var ret_var = fifo_out_dict[fifo]
        if (ret_var == null) ERROR("FIFO translation error")
        return ret_var!!
    }

    fun TranslateFifoIn(fifo : hw_fifo_in) : fifo_in_descr {
        var ret_var = fifo_in_dict[fifo]
        if (ret_var == null) ERROR("FIFO translation error")
        return ret_var!!
    }

    fun export_expr(rtl_gen : hw_astc,
                    expr : hw_exec,
                    rst : rtl.hw_port) {

        // println("#### Cyclix: exporting expression: " + expr.opcode.default_string)
        // for (param in expr.params) println("param: " + param.GetString())
        // for (wrvar in expr.wrvars) println("wrvar: " + wrvar.name)

        var fractions = hw_fracs()
        for (src_fraction in expr.assign_tgt_fractured.depow_fractions) {
            if (src_fraction is hw_frac_C) fractions.add(src_fraction)
            else if (src_fraction is hw_frac_V) fractions.add(hw_frac_V(TranslateVar(src_fraction.index, var_dict)))
            else if (src_fraction is hw_frac_CC) fractions.add(src_fraction)
            else if (src_fraction is hw_frac_CV) fractions.add(hw_frac_CV(src_fraction.msb, TranslateVar(src_fraction.lsb, var_dict)))
            else if (src_fraction is hw_frac_VC) fractions.add(hw_frac_VC(TranslateVar(src_fraction.msb, var_dict), src_fraction.lsb))
            else if (src_fraction is hw_frac_VV) fractions.add(hw_frac_VV(TranslateVar(src_fraction.msb, var_dict), TranslateVar(src_fraction.lsb, var_dict)))
            else if (src_fraction is hw_frac_SubStruct) fractions.add(src_fraction)
            else ERROR("dimensions error")
        }

        if ((expr.opcode == OP1_ASSIGN)) {
            rtl_gen.assign(TranslateVar(expr.wrvars[0], var_dict), fractions, TranslateParam(expr.params[0], var_dict))

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
                params.add(TranslateParam(param, var_dict))
            }
            rtl_gen.AddExpr_op_gen(expr.opcode, TranslateVar(expr.wrvars[0], var_dict), params)

        } else if (expr.opcode == OP2_SUBSTRUCT) {
            rtl_gen.subStruct_gen(
                TranslateVar(expr.wrvars[0], var_dict),
                TranslateVar(expr.rdvars[0], var_dict),
                expr.subStructvar_name
            )

        } else if (expr.opcode == OP1_IF) {

            rtl_gen.begif(TranslateParam(expr.params[0], var_dict))
            run {
                for (child_expr in expr.expressions) {
                    export_expr(rtl_gen, child_expr, rst)
                }
            }; rtl_gen.endif()

        } else if (expr.opcode == OP1_CASE) {

            rtl_gen.begcase(TranslateParam(expr.params[0], var_dict))
            run {
                for (casebranch in expr.expressions) {
                    if (casebranch.opcode != OP1_CASEBRANCH) ERROR("non-branch op in case")
                    rtl_gen.begbranch(TranslateParam(casebranch.params[0], var_dict))
                    for (subexpr in casebranch.expressions) {
                        export_expr(rtl_gen, subexpr, rst)
                    }
                    rtl_gen.endbranch()
                }
            }; rtl_gen.endcase()

        } else if (expr.opcode == OP1_WHILE) {

            rtl_gen.begwhile(TranslateParam(expr.params[0], var_dict))
            run {
                for (child_expr in expr.expressions) {
                    export_expr(rtl_gen, child_expr, rst)
                }
            }; rtl_gen.endloop()

        } else if (expr.opcode == OP_FIFO_WR_UNBLK) {

            var fifo = TranslateFifoOut((expr as hw_exec_fifo_wr_unblk).fifo)
            var wdata_translated = TranslateParam(expr.params[0], var_dict)
            var fifo_rdy = TranslateVar(expr.wrvars[0], var_dict)

            rtl_gen.begif(rtl_gen.lnot(rst))
            run {
                rtl_gen.assign(fifo.ext_req, 1)
                rtl_gen.begif(fifo.reqbuf_req)
                run {
                    // fifo busy
                    rtl_gen.assign(fifo_rdy, 0)
                }; rtl_gen.endif()
                rtl_gen.begelse()
                run {
                    // fifo ready to consume request
                    rtl_gen.assign(fifo.ext_wdata, wdata_translated)
                    rtl_gen.assign(fifo_rdy, fifo.ext_ack)
                }; rtl_gen.endif()
                rtl_gen.assign(fifo.reqbuf_req, 1)
            }; rtl_gen.endif()

        } else if (expr.opcode == OP_FIFO_RD_UNBLK) {

            var fifo = TranslateFifoIn((expr as hw_exec_fifo_rd_unblk).fifo)
            var fifo_rdy = TranslateVar(expr.wrvars[0], var_dict)
            var rdata_translated = TranslateVar(expr.wrvars[1], var_dict)

            rtl_gen.begif(rtl_gen.lnot(rst))
            run {
                // default: inactive
                rtl_gen.assign(fifo_rdy, 0)

                rtl_gen.begif(fifo.buf_req)
                run {
                    //// request pending
                    // reading data
                    rtl_gen.assign(fifo_rdy, 1)
                    rtl_gen.assign(rdata_translated, fifo.buf_rdata)

                    // clearing buffer
                    rtl_gen.assign(fifo.buf_req, 0)

                    // asserting ack
                    rtl_gen.assign(fifo.ext_ack, 1)
                }; rtl_gen.endif()
            }; rtl_gen.endif()

        } else ERROR("Reconstruction of expression failed: opcode undefined: " + expr.opcode.default_string)

        // println("#### Cyclix: exporting expression complete!")
    }

    fun generate() : rtl.module {

        println("#######################################")
        println("#### Starting Cyclix-to-RTL export ####")
        println("#######################################")

        // TODO: pre-validation

        var_dict.clear()
        fifo_out_dict.clear()
        fifo_in_dict.clear()

        var rtl_gen = rtl.module(cyclix_module.name)

        // Adding structs
        for (struct in cyclix_module.hw_structs) {
            rtl_gen.add_struct(struct.value)
        }

        // Generating ports
        var clk = rtl_gen.uinput("clk_i", 0, 0, "0")
        var rst = rtl_gen.uinput("rst_i", 0, 0, "1")

        // Generating combinationals
        for (local in cyclix_module.locals)
            var_dict.put(local, rtl_gen.comb(local.name, local.vartype, local.defval))

        // Generating globals
        for (global in cyclix_module.globals)
            var_dict.put(global, rtl_gen.sticky(global.name, global.vartype, global.defval, clk, rst))

        // Generating fifo_outs
        for (fifo_out in cyclix_module.fifo_outs) {
            fifo_out_dict.put(fifo_out, fifo_out_descr(
                rtl_gen.uoutput((fifo_out.name + "_genfifo_req_o"), 0, 0, "0"),
                rtl_gen.port((fifo_out.name + "_genfifo_wdata_bo"), rtl.PORT_DIR.OUT, fifo_out.vartype, fifo_out.defval),
                rtl_gen.uinput((fifo_out.name + "_genfifo_ack_i"), 0, 0, "1"),
                rtl_gen.ucomb((fifo_out.name + "_genfifo_reqbuf_req"), 0, 0, "0")
            ))
        }

        // Generating fifo_ins
        for (fifo_in in cyclix_module.fifo_ins) {
            fifo_in_dict.put(fifo_in, fifo_in_descr(
                rtl_gen.uinput((fifo_in.name + "_genfifo_req_i"), 0, 0, "0"),
                rtl_gen.port((fifo_in.name + "_genfifo_rdata_bi"), rtl.PORT_DIR.IN, fifo_in.vartype, fifo_in.defval),
                rtl_gen.uoutput((fifo_in.name + "_genfifo_ack_o"), 0, 0, "0"),
                rtl_gen.ucomb((fifo_in.name + "_genfifo_buf_req"), 0, 0, "0"),
                rtl_gen.comb((fifo_in.name + "_genfifo_buf_rdata"), fifo_in.vartype, fifo_in.defval)
            ))
        }

        rtl_gen.cproc_begin()
        run {

            // fifo_in buffering
            for (fifo_in in fifo_in_dict) {
                rtl_gen.assign(fifo_in.value.buf_req, fifo_in.value.ext_req)
                rtl_gen.assign(fifo_in.value.buf_rdata, fifo_in.value.ext_rdata)
            }

            // TODO: Streaming

            // Generating payload
            for (expr in cyclix_module.proc.expressions) {
                export_expr(rtl_gen, expr, rst)
            }

        }; rtl_gen.cproc_end()

        rtl_gen.end()

        return rtl_gen
    }

}
