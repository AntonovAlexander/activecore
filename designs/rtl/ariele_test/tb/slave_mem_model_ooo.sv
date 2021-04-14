/*
 * slave_mem_model.sv
 *
 *  Created on: 20.11.2016
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module slave_mem_model_ooo
#(
	parameter MEMSIZE32 = 1024
)
(
	input clk_i, input rst_i,

	input slave_req,
	input [31:0] slave_addr,
	input slave_cmd,
	output reg [1:0] slave_reqtid,
	input [31:0] slave_wdata,
	output slave_ack,
	output reg [1:0] slave_resptid,
	output reg [31:0] slave_rdata,
	output reg slave_resp
);

localparam BUFSIZE_REORDER = 4;

wire rdreq, wrreq;
assign rdreq = slave_req && slave_ack && !slave_cmd;
assign wrreq = slave_req && slave_ack && slave_cmd;

// tid generation logic //
reg [3:0] tid_use_mask, tid_use_mask_next;

always @(posedge clk_i)
	begin
	if (rst_i) tid_use_mask <= 0;
	else tid_use_mask <= tid_use_mask_next;
	end

always @*
	begin
	tid_use_mask_next = tid_use_mask;
	if (slave_resp) tid_use_mask_next[slave_resptid] = 1'b0;
	if (slave_req && slave_ack && !slave_cmd)
		begin
		if (tid_use_mask_next[0] == 1'b0) 		begin slave_reqtid = 2'h0; tid_use_mask_next[0] = 1'b1; end
		else if (tid_use_mask_next[1] == 1'b0) 	begin slave_reqtid = 2'h1; tid_use_mask_next[1] = 1'b1; end
		else if (tid_use_mask_next[2] == 1'b0) 	begin slave_reqtid = 2'h2; tid_use_mask_next[2] = 1'b1; end
		else if (tid_use_mask_next[3] == 1'b0) 	begin slave_reqtid = 2'h3; tid_use_mask_next[3] = 1'b1; end
		else $error ("Not enough tids!");
		end
	end
////

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

// simulation of reodered responses
reg [0:0] resp_busy [BUFSIZE_REORDER-1:0];
reg [31:0] resp_rdata [BUFSIZE_REORDER-1:0];

reg [0:0] resp_busy_next [BUFSIZE_REORDER-1:0];
reg [31:0] resp_rdata_next [BUFSIZE_REORDER-1:0];

reg [1:0] tid_return;
integer return_active;

integer i;
always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		for (i = 0; i < BUFSIZE_REORDER; i = i + 1)
			begin
			resp_busy[i] <= 1'b0;
			resp_rdata[i] <= 0;
			end
		end
	else 
		begin
		return_active <= $urandom_range(0,1);
		tid_return = $urandom_range(0,3);

		for (i = 0; i < BUFSIZE_REORDER; i = i + 1)
			begin
			resp_busy[i] <= resp_busy_next[i];
			resp_rdata[i] <= resp_rdata_next[i];
			end
		end
	end


assign slave_ack = slave_req & !(resp_busy[0] & resp_busy[1] & resp_busy[2] & resp_busy[3]);		// no ack if buffer is full

integer j;
always @*
	begin

	slave_resp = 0;
	slave_rdata = 0;
	slave_resptid = 0;
	for (j = 0; j < BUFSIZE_REORDER; j = j + 1)
		begin
		resp_busy_next[j] = resp_busy[j];
		resp_rdata_next[j] = resp_rdata[j];
		end
	
	if (return_active > 0)
		begin
		slave_resp = resp_busy_next[tid_return];
		slave_rdata = resp_rdata_next[tid_return];
		slave_resptid = tid_return;
		resp_busy_next[tid_return] = 1'b0;
		resp_rdata_next[tid_return] = 0;
		end

		if (rdreq)
			begin
			resp_busy_next[slave_reqtid] = 1'b1;
			resp_rdata_next[slave_reqtid] = rdata;
			end

	end

////

endmodule
