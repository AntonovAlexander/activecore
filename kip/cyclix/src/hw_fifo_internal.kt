/*
 * hw_fifo_internal.kt
 *
 *  Created on: 15.01.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*

val OP_FIFO_INTERNAL_WR_UNBLK = hw_opcode("fifo_internal_wr_unblk")
val OP_FIFO_INTERNAL_RD_UNBLK = hw_opcode("fifo_internal_rd_unblk")

val OP_FIFO_INTERNAL_WR_BLK = hw_opcode("fifo_internal_wr_blk")
val OP_FIFO_INTERNAL_RD_BLK = hw_opcode("fifo_internal_rd_blk")

class hw_exec_fifo_internal_wr_unblk(var subproc : hw_subproc, var fifo_name : String, var wdata : hw_param) : hw_exec(OP_FIFO_INTERNAL_WR_UNBLK)
class hw_exec_fifo_internal_rd_unblk(var subproc : hw_subproc, var fifo_name : String, var rdata : hw_var) : hw_exec(OP_FIFO_INTERNAL_RD_UNBLK)

class hw_exec_fifo_internal_wr_blk(var subproc : hw_subproc, var fifo_name : String, var wdata : hw_param) : hw_exec(OP_FIFO_INTERNAL_WR_BLK)
class hw_exec_fifo_internal_rd_blk(var subproc : hw_subproc, var fifo_name : String) : hw_exec(OP_FIFO_INTERNAL_RD_BLK)