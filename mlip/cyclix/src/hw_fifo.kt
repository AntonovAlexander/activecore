/*
 * hw_fifo.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*

val OP_FIFO_WR = hw_opcode("fifo_wr")
val OP_FIFO_RD = hw_opcode("fifo_rd")

class hw_exec_fifo_wr(fifo_in : hw_fifo_out) : hw_exec(OP_FIFO_WR) {
    var fifo = fifo_in
}

class hw_exec_fifo_rd(fifo_in : hw_fifo_in) : hw_exec(OP_FIFO_RD) {
    var fifo = fifo_in
}

class hw_fifo_out(name_in : String, vartype : hw_type)
    : hw_structvar(name_in, vartype, "0")

class hw_fifo_in(name_in : String, vartype : hw_type)
    : hw_structvar(name_in, vartype, "0")
