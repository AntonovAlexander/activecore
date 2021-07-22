/*
 * hw_fifo_if.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

val OP_FIFO_WR_UNBLK = hw_opcode("fifo_wr_unblk")
val OP_FIFO_RD_UNBLK = hw_opcode("fifo_rd_unblk")

val OP_FIFO_WR_BLK = hw_opcode("fifo_wr_blk")
val OP_FIFO_RD_BLK = hw_opcode("fifo_rd_blk")

class hw_exec_fifo_wr_unblk(var fifo : hw_fifo_out) : hw_exec(OP_FIFO_WR_UNBLK)
class hw_exec_fifo_rd_unblk(var fifo : hw_fifo_in) : hw_exec(OP_FIFO_RD_UNBLK)

class hw_exec_fifo_wr_blk(var fifo : hw_fifo_out) : hw_exec(OP_FIFO_WR_BLK)
class hw_exec_fifo_rd_blk(var fifo : hw_fifo_in) : hw_exec(OP_FIFO_RD_BLK)

class hw_fifo_out(name : String, vartype : hw_type)
    : hw_structvar(name, vartype, "0")

class hw_fifo_in(name : String, vartype : hw_type)
    : hw_structvar(name, vartype, "0")

var DUMMY_FIFO_IN  = hw_fifo_in("DUMMY", hw_type(DATA_TYPE.BV_UNSIGNED, 0, 0))
var DUMMY_FIFO_OUT = hw_fifo_out("DUMMY", hw_type(DATA_TYPE.BV_UNSIGNED, 0, 0))