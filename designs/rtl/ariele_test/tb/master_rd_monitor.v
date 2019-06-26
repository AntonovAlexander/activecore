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


module master_rd_monitor
#(
	parameter MNUM = 0
)
(
	input clk_i,
	input rst_i,

	input master_req,
	input [31:0] master_addr,
	input master_cmd,
	input [31:0] master_wdata,
	input master_ack,
	input [31:0] master_rdata,
	input master_resp,

	output rdreq_fifo_full
);


wire rdreq_fifo_empty;
wire [31:0] rdreq_fifo_rdata;

fifo
#(
	.B(32),
	.W(8)
) rdreq_fifo (
	.clk(clk_i),
	.reset(rst_i),
	.rd(master_resp),
	.wr(master_req & master_ack & !master_cmd),
	.w_data(master_addr),
	.empty(rdreq_fifo_empty),
	.full(rdreq_fifo_full),
	.r_data(rdreq_fifo_rdata)
);

integer trans_num;

always @(posedge clk_i)
	begin
	if (master_resp)
		begin
		trans_num <= trans_num + 1;
		if (rdreq_fifo_empty) $fatal("Unexpected response in Master %d: 0x%x", MNUM, master_rdata);
		else
			begin
			if (rdreq_fifo_rdata == master_rdata) $display("Response number %d received in Master %d - correct!  Expected output: 0x%x, real output: 0x%x", trans_num, MNUM, rdreq_fifo_rdata, master_rdata);
			else $fatal("Response received in Master %d - incorrect! Expected output: 0x%x, real output: 0x%x", MNUM, rdreq_fifo_rdata, master_rdata);
			end
		end
	end

initial
	begin
	trans_num = 0;
	end

endmodule
