/*
 * cpu_stub.v
 *
 *  Created on: 24.09.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module cpu_stub (
	input [0:0] clk_i,
	input [0:0] rst_i,
	input [0:0] instr_mem_ack,
	input [0:0] instr_mem_resp,
	input [31:0] instr_mem_rdata,
	input [0:0] data_mem_ack,
	input [0:0] data_mem_resp,
	input [31:0] data_mem_rdata,
	output [0:0] instr_mem_req,
	output [0:0] instr_mem_we,
	output [31:0] instr_mem_addr,
	output [31:0] instr_mem_wdata,
	output [3:0] instr_mem_be,
	output [0:0] data_mem_req,
	output [0:0] data_mem_we,
	output [31:0] data_mem_addr,
	output [31:0] data_mem_wdata,
	output [3:0] data_mem_be
);

assign instr_mem_req = 1'b0;
assign instr_mem_we = 1'b0;
assign instr_mem_addr = 32'h0;
assign instr_mem_wdata = 32'h0;
assign instr_mem_be = 4'h0;
assign data_mem_req = 1'b0;
assign data_mem_we = 1'b0;
assign data_mem_addr = 32'h0;
assign data_mem_wdata = 32'h0;
assign data_mem_be = 4'h0;

endmodule
