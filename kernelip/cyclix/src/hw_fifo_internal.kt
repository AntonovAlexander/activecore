/*
 * hw_fifo_internal.kt
 *
 *  Created on: 15.01.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*

val OP_TRY_FIFO_INTERNAL_WR = hw_opcode("try_fifo_internal_wr")
val OP_TRY_FIFO_INTERNAL_RD = hw_opcode("try_fifo_internal_rd")

val OP_FIFO_INTERNAL_WR = hw_opcode("fifo_internal_wr")
val OP_FIFO_INTERNAL_RD = hw_opcode("fifo_internal_rd")

class hw_exec_fifo_internal_wr_unblk(var subproc : hw_subproc, var fifo_name : String, var wdata : hw_param) : hw_exec(OP_TRY_FIFO_INTERNAL_WR)
class hw_exec_fifo_internal_rd_unblk(var subproc : hw_subproc, var fifo_name : String, var rdata : hw_var) : hw_exec(OP_TRY_FIFO_INTERNAL_RD)

class hw_exec_fifo_internal_wr_blk(var subproc : hw_subproc, var fifo_name : String, var wdata : hw_param) : hw_exec(OP_FIFO_INTERNAL_WR)
class hw_exec_fifo_internal_rd_blk(var subproc : hw_subproc, var fifo_name : String) : hw_exec(OP_FIFO_INTERNAL_RD)