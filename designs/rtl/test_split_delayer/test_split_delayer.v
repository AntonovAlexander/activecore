/*
 * test_split_delayer.v
 *
 *  Created on: 17.09.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module test_split_delayer
	#(
	parameter 
	REQ_RANDOM_RANGE=8,
	RESP_RANDOM_RANGE=8,
	RESP_FIFO_POW=4
	)
(
	input [0:0] clk_i,
	input [0:0] rst_i,
	
	input 		[0:0] 	host_req,
	output reg 	[0:0] 	host_ack,
	input 		[0:0] 	host_we,
	input 		[31:0] 	host_addr,
	input 		[31:0] 	host_wdata,
	input 		[3:0] 	host_be,
	output reg 	[0:0] 	host_resp,
	output reg 	[31:0] 	host_rdata,
	
	output reg 	[0:0] 	target_req,
	input 		[0:0] 	target_ack,
	output reg 	[0:0] 	target_we,
	output reg 	[31:0] 	target_addr,
	output reg 	[31:0] 	target_wdata,
	output reg 	[3:0] 	target_be,
	input 		[0:0] 	target_resp,
	input 		[31:0] 	target_rdata
);

// req delaying //
integer req_random_var;
always @(posedge clk_i)
	begin
	req_random_var <= $urandom_range(0,REQ_RANDOM_RANGE-1);
	end

always @*
	begin
	if (req_random_var == 0)
		begin
		target_req 		= host_req;
		target_we 		= host_we;
		target_addr 	= host_addr;
		target_wdata 	= host_wdata;
		target_be 		= host_be;
		host_ack 		= target_ack;
		end
	else 
		begin
		target_req 		= 1'b0;
		target_we 		= 1'b0;
		target_addr 	= 32'h0;
		target_wdata 	= 32'h0;
		target_be 		= 4'h0;
		host_ack 		= 1'b0;
		end
	end

// resp delaying //
integer resp_random_var;
always @(posedge clk_i)
	begin
	resp_random_var <= $urandom_range(0,RESP_RANDOM_RANGE-1);
	end

reg resp_fifo_rd;
wire resp_fifo_empty, resp_fifo_full;
wire [31:0] resp_fifo_rdata;

always @*
	begin
	resp_fifo_rd = 1'b0;
	host_resp = 1'b0;
	host_rdata = 32'h0;
	if ((resp_fifo_full) || ( (!resp_fifo_empty) && (resp_random_var == 0) ))
		begin
		resp_fifo_rd = 1'b1;
		host_resp = 1'b1;
		host_rdata = resp_fifo_rdata;
		end
	end
	
fifo
#(
	.B(32),
	.W(RESP_FIFO_POW)
) resp_fifo (
	.clk(clk_i),
	.reset(rst_i),
	.rd(resp_fifo_rd),
	.wr(target_resp),
	.w_data(target_rdata),
	.empty(resp_fifo_empty),
	.full(resp_fifo_full),
	.r_data(resp_fifo_rdata)
);

endmodule
