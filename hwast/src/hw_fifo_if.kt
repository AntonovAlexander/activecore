/*
 * hw_fifo_if.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

val OP_TRY_FIFO_WR = hw_opcode("try_fifo_wr")
val OP_TRY_FIFO_RD = hw_opcode("try_fifo_rd")

val OP_FIFO_WR = hw_opcode("fifo_wr")
val OP_FIFO_RD = hw_opcode("fifo_rd")

class hw_exec_try_fifo_wr(var fifo : hw_fifo_out) : hw_exec(OP_TRY_FIFO_WR)
class hw_exec_try_fifo_rd(var fifo : hw_fifo_in) : hw_exec(OP_TRY_FIFO_RD)

class hw_exec_fifo_wr(var fifo : hw_fifo_out) : hw_exec(OP_FIFO_WR)
class hw_exec_fifo_rd(var fifo : hw_fifo_in) : hw_exec(OP_FIFO_RD)

class hw_fifo_out(name : String, vartype : hw_type)
    : hw_structvar(name, vartype, "0")

class hw_fifo_in(name : String, vartype : hw_type)
    : hw_structvar(name, vartype, "0")

var DUMMY_FIFO_IN  = hw_fifo_in("DUMMY", hw_type(DATA_TYPE.BV_UNSIGNED, 0, 0))
var DUMMY_FIFO_OUT = hw_fifo_out("DUMMY", hw_type(DATA_TYPE.BV_UNSIGNED, 0, 0))