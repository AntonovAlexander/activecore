/*
 PAVANA_XBAR

 Copyright (c) 2016 Alexander Antonov <153287@niuitmo.ru>
 All rights reserved.

 Version 1.0

 The FreeBSD license
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above
    copyright notice, this list of conditions and the following
    disclaimer in the documentation and/or other materials
    provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 PAVANA_XBAR PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


module slave_mem_model
#(
	parameter MEMSIZE32 = 1024
	, parameter REQ_RAND = 4
	, parameter RESP_RAND = 4
)
(
	input clk_i, input rst_i,

	input slave_req,
	input [31:0] slave_addr,
	input slave_cmd,
	input [31:0] slave_wdata,
	output slave_ack,
	output [31:0] slave_rdata,
	output slave_resp
);


	// memory model //
	reg [31:0] mem [MEMSIZE32-1:0];
	reg [31:0] rdata;

	integer r;
	always @(posedge clk_i)
		begin
		if (rst_i)
			begin
			for (r = 0; r < MEMSIZE32; r = r + 1)
				begin
				mem[r] <= 32'hdeadbeef;
				end
			end
		else if (slave_req && slave_ack && slave_cmd) mem[slave_addr[29:2]] <= slave_wdata;
		end
	assign rdata = mem[slave_addr[29:2]];

	reg req_enb, resp_enb;

	wire fifo_req_empty, fifo_req_full;
	wire fifo_req_wr, fifo_req_rd;

	assign slave_ack = slave_req & req_enb & (!fifo_req_full);
	assign slave_resp = resp_enb & (!fifo_req_empty);

	assign fifo_req_wr = slave_req & slave_ack & (!slave_cmd);
	assign fifo_req_rd = slave_resp;

	always @(posedge clk_i)
		begin
		if (rst_i)
			begin
			req_enb <= 1'b0;
			resp_enb <= 1'b0;
			end
		else 
			begin
			req_enb <= ($urandom_range(0,(REQ_RAND-1)) == (REQ_RAND-1));
			resp_enb <= ($urandom_range(0,(RESP_RAND-1)) == (RESP_RAND-1));
			end
		end

	fifo #(
		.B(32)
		, .W(4)
	) fifo (
		.clk(clk_i)
		, .reset(rst_i)

		, .full(fifo_req_full)
		, .wr(fifo_req_wr)
		, .w_data(rdata)

		, .empty(fifo_req_empty)
		, .rd(fifo_req_rd)
		, .r_data(slave_rdata)
	);

endmodule
