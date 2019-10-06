/*
 * master_rd_monitor.v
 *
 *  Created on: 20.11.2016
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
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
