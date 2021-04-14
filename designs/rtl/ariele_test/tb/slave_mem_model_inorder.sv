/*
 * slave_mem_model.sv
 *
 *  Created on: 20.11.2016
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module slave_mem_model_inorder
#(
	parameter MEMSIZE32 = 1024
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

wire fifo_empty, fifo_full;

// readiness randomization
reg slave_req_rdy, slave_ack_rdy;
always @(posedge clk_i)
	begin
	randcase
        1 : slave_req_rdy <= 1'b1;
        3 : slave_req_rdy <= 1'b0;
    endcase
    randcase
        1 : slave_ack_rdy <= 1'b1;
        3 : slave_ack_rdy <= 1'b0;
    endcase
	end
assign slave_ack  = !fifo_full & slave_req & slave_req_rdy;
assign slave_resp = !fifo_empty & slave_ack_rdy;

wire rdreq, wrreq;
assign rdreq = slave_req && slave_ack && !slave_cmd;
assign wrreq = slave_req && slave_ack && slave_cmd;

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
	else if (wrreq) mem[slave_addr[29:2]] <= slave_wdata;
	end

assign rdata = mem[slave_addr[29:2]];
////

// delay modeling
fifo
#(
	.B(32)
	, .W(4)
) respfifo (
.clk(clk_i)
, .reset(rst_i)
, .rd(slave_resp)
, .wr(rdreq)
, .w_data(rdata)
, .empty(fifo_empty)
, .full(fifo_full)
, .r_data(slave_rdata)
);

endmodule
