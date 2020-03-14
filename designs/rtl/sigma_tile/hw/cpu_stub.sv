/*
 * cpu_stub.sv
 *
 *  Created on: 24.09.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

module cpu_stub (
	input [0:0] clk_i
	, input [0:0] rst_i

	, MemSplit32.Master instr_mem
	, MemSplit32.Master data_mem
);

assign instr_mem.req = 1'b0;
assign instr_mem.we = 1'b0;
assign instr_mem.addr = 32'h0;
assign instr_mem.wdata = 32'h0;
assign instr_mem.be = 4'h0;
assign data_mem.req = 1'b0;
assign data_mem.we = 1'b0;
assign data_mem.addr = 32'h0;
assign data_mem.wdata = 32'h0;
assign data_mem.be = 4'h0;

endmodule
