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


`timescale 1ns / 1ps

`define HALF_PERIOD			10						// external 50 MHZ
`define PERIOD				(2*`HALF_PERIOD)

`define M0_RAND_SEED 	1
`define M1_RAND_SEED 	2
`define M2_RAND_SEED 	3
`define M3_RAND_SEED 	4

module xbar_tb ();

localparam SLAVE_MEMSIZE32 = 1024;
localparam TRANS_BUFSISE = 4;

reg clk, rst;

wire master_0_req;
wire [31:0] master_0_addr;
wire master_0_cmd;
wire [31:0] master_0_wdata;
wire master_0_ack;
wire [31:0] master_0_rdata;
wire master_0_resp;

wire master_1_req;
wire [31:0] master_1_addr;
wire master_1_cmd;
wire [31:0] master_1_wdata;
wire master_1_ack;
wire [31:0] master_1_rdata;
wire master_1_resp;

wire master_2_req;
wire [31:0] master_2_addr;
wire master_2_cmd;
wire [31:0] master_2_wdata;
wire master_2_ack;
wire [31:0] master_2_rdata;
wire master_2_resp;

wire master_3_req;
wire [31:0] master_3_addr;
wire master_3_cmd;
wire [31:0] master_3_wdata;
wire master_3_ack;
wire [31:0] master_3_rdata;
wire master_3_resp;

wire slave_0_req;
wire [31:0] slave_0_addr;
wire slave_0_cmd;
wire [1:0] slave_0_reqtid;
wire [31:0] slave_0_wdata;
wire slave_0_ack;
wire [1:0] slave_0_resptid;
wire [31:0] slave_0_rdata;
wire slave_0_resp;

wire slave_1_req;
wire [31:0] slave_1_addr;
wire slave_1_cmd;
wire [1:0] slave_1_reqtid;
wire [31:0] slave_1_wdata;
wire slave_1_ack;
wire [1:0] slave_1_resptid;
wire [31:0] slave_1_rdata;
wire slave_1_resp;

wire slave_2_req;
wire [31:0] slave_2_addr;
wire slave_2_cmd;
wire [1:0] slave_2_reqtid;
wire [31:0] slave_2_wdata;
wire slave_2_ack;
wire [1:0] slave_2_resptid;
wire [31:0] slave_2_rdata;
wire slave_2_resp;

wire slave_3_req;
wire [31:0] slave_3_addr;
wire slave_3_cmd;
wire [1:0] slave_3_reqtid;
wire [31:0] slave_3_wdata;
wire slave_3_ack;
wire [1:0] slave_3_resptid;
wire [31:0] slave_3_rdata;
wire slave_3_resp;

wire rdreq0_fifo_full, rdreq1_fifo_full, rdreq2_fifo_full, rdreq3_fifo_full;

master_rd_monitor
#(
	.MNUM(0)
) master_rd_monitor_0 (
	.clk_i(clk),
	.rst_i(rst),

	.master_req(master_0_req),
	.master_addr(master_0_addr),
	.master_cmd(master_0_cmd),
	.master_wdata(master_0_wdata),
	.master_ack(master_0_ack),
	.master_rdata(master_0_rdata),
	.master_resp(master_0_resp),

	.rdreq_fifo_full(rdreq0_fifo_full)
);

master_rd_monitor
#(
	.MNUM(1)
) master_rd_monitor_1 (
	.clk_i(clk),
	.rst_i(rst),

	.master_req(master_1_req),
	.master_addr(master_1_addr),
	.master_cmd(master_1_cmd),
	.master_wdata(master_1_wdata),
	.master_ack(master_1_ack),
	.master_rdata(master_1_rdata),
	.master_resp(master_1_resp),

	.rdreq_fifo_full(rdreq1_fifo_full)
);

master_rd_monitor
#(
	.MNUM(2)
) master_rd_monitor_2 (
	.clk_i(clk),
	.rst_i(rst),

	.master_req(master_2_req),
	.master_addr(master_2_addr),
	.master_cmd(master_2_cmd),
	.master_wdata(master_2_wdata),
	.master_ack(master_2_ack),
	.master_rdata(master_2_rdata),
	.master_resp(master_2_resp),

	.rdreq_fifo_full(rdreq2_fifo_full)
);

master_rd_monitor
#(
	.MNUM(3)
) master_rd_monitor_3 (
	.clk_i(clk),
	.rst_i(rst),

	.master_req(master_3_req),
	.master_addr(master_3_addr),
	.master_cmd(master_3_cmd),
	.master_wdata(master_3_wdata),
	.master_ack(master_3_ack),
	.master_rdata(master_3_rdata),
	.master_resp(master_3_resp),

	.rdreq_fifo_full(rdreq3_fifo_full)
);

xbar_pipex DUV
(
	.clk_i(clk),
	.rst_i(rst),

	.m0_req(master_0_req),
	.m0_addr(master_0_addr),
	.m0_we(master_0_cmd),
	.m0_wdata(master_0_wdata),
	.m0_ack(master_0_ack),
	.m0_rdata(master_0_rdata),
	.m0_resp(master_0_resp),

	.m1_req(master_1_req),
	.m1_addr(master_1_addr),
	.m1_we(master_1_cmd),
	.m1_wdata(master_1_wdata),
	.m1_ack(master_1_ack),
	.m1_rdata(master_1_rdata),
	.m1_resp(master_1_resp),

	.m2_req(master_2_req),
	.m2_addr(master_2_addr),
	.m2_we(master_2_cmd),
	.m2_wdata(master_2_wdata),
	.m2_ack(master_2_ack),
	.m2_rdata(master_2_rdata),
	.m2_resp(master_2_resp),

	.m3_req(master_3_req),
	.m3_addr(master_3_addr),
	.m3_we(master_3_cmd),
	.m3_wdata(master_3_wdata),
	.m3_ack(master_3_ack),
	.m3_rdata(master_3_rdata),
	.m3_resp(master_3_resp),

	.s0_req(slave_0_req),
	.s0_addr(slave_0_addr),
	.s0_we(slave_0_cmd),
	.s0_wdata(slave_0_wdata),
	.s0_ack(slave_0_ack),
	.s0_rdata(slave_0_rdata),
	.s0_resp(slave_0_resp),

	.s1_req(slave_1_req),
	.s1_addr(slave_1_addr),
	.s1_we(slave_1_cmd),
	.s1_wdata(slave_1_wdata),
	.s1_ack(slave_1_ack),
	.s1_rdata(slave_1_rdata),
	.s1_resp(slave_1_resp),

	.s2_req(slave_2_req),
	.s2_addr(slave_2_addr),
	.s2_we(slave_2_cmd),
	.s2_wdata(slave_2_wdata),
	.s2_ack(slave_2_ack),
	.s2_rdata(slave_2_rdata),
	.s2_resp(slave_2_resp),

	.s3_req(slave_3_req),
	.s3_addr(slave_3_addr),
	.s3_we(slave_3_cmd),
	.s3_wdata(slave_3_wdata),
	.s3_ack(slave_3_ack),
	.s3_rdata(slave_3_rdata),
	.s3_resp(slave_3_resp)
);

slave_mem_model
#(
	.MEMSIZE32(SLAVE_MEMSIZE32)
) slave_0_mem_model (
	.clk_i(clk),
	.rst_i(rst),

	.slave_req(slave_0_req),
	.slave_addr(slave_0_addr),
	.slave_cmd(slave_0_cmd),
	.slave_reqtid(slave_0_reqtid),
	.slave_wdata(slave_0_wdata),
	.slave_ack(slave_0_ack),
	.slave_resptid(slave_0_resptid),
	.slave_rdata(slave_0_rdata),
	.slave_resp(slave_0_resp)
);

slave_mem_model
#(
	.MEMSIZE32(SLAVE_MEMSIZE32)
) slave_1_mem_model (
	.clk_i(clk),
	.rst_i(rst),

	.slave_req(slave_1_req),
	.slave_addr(slave_1_addr),
	.slave_cmd(slave_1_cmd),
	.slave_reqtid(slave_1_reqtid),
	.slave_wdata(slave_1_wdata),
	.slave_ack(slave_1_ack),
	.slave_resptid(slave_1_resptid),
	.slave_rdata(slave_1_rdata),
	.slave_resp(slave_1_resp)
);

slave_mem_model
#(
	.MEMSIZE32(SLAVE_MEMSIZE32)
) slave_2_mem_model (
	.clk_i(clk),
	.rst_i(rst),

	.slave_req(slave_2_req),
	.slave_addr(slave_2_addr),
	.slave_cmd(slave_2_cmd),
	.slave_reqtid(slave_2_reqtid),
	.slave_wdata(slave_2_wdata),
	.slave_ack(slave_2_ack),
	.slave_resptid(slave_2_resptid),
	.slave_rdata(slave_2_rdata),
	.slave_resp(slave_2_resp)
);

slave_mem_model
#(
	.MEMSIZE32(SLAVE_MEMSIZE32)
) slave_3_mem_model (
	.clk_i(clk),
	.rst_i(rst),

	.slave_req(slave_3_req),
	.slave_addr(slave_3_addr),
	.slave_cmd(slave_3_cmd),
	.slave_reqtid(slave_3_reqtid),
	.slave_wdata(slave_3_wdata),
	.slave_ack(slave_3_ack),
	.slave_resptid(slave_3_resptid),
	.slave_rdata(slave_3_rdata),
	.slave_resp(slave_3_resp)
);

// memory initialization/read synchronization //
wire master_0_meminit_done, master_1_meminit_done, master_2_meminit_done, master_3_meminit_done;
wire memread_grant;
assign memread_grant = master_0_meminit_done & master_1_meminit_done & master_2_meminit_done & master_3_meminit_done;

master_exerciser
#(
	.MNUM(0),
	.START_ADDR((0) << 2),
	.STOP_ADDR(((SLAVE_MEMSIZE32 >> 2) - 1) << 2),
	.RAND_SEED(`M0_RAND_SEED)
) master_0_exerciser (
	.clk(clk),
	.rst(rst),

	.master_req(master_0_req),
	.master_addr(master_0_addr),
	.master_cmd(master_0_cmd),
	.master_wdata(master_0_wdata),
	.master_ack(master_0_ack),
	.master_rdata(master_0_rdata),
	.master_resp(master_0_resp),

	.meminit_done(master_0_meminit_done),
	.memread_grant(memread_grant)
);

master_exerciser
#(
	.MNUM(1),
	.START_ADDR(((SLAVE_MEMSIZE32 >> 1) - 1) << 2),
	.STOP_ADDR((SLAVE_MEMSIZE32 >> 2) << 2),
	.RAND_SEED(`M1_RAND_SEED)
) master_1_exerciser (
	.clk(clk),
	.rst(rst),

	.master_req(master_1_req),
	.master_addr(master_1_addr),
	.master_cmd(master_1_cmd),
	.master_wdata(master_1_wdata),
	.master_ack(master_1_ack),
	.master_rdata(master_1_rdata),
	.master_resp(master_1_resp),

	.meminit_done(master_1_meminit_done),
	.memread_grant(memread_grant)
);

master_exerciser
#(
	.MNUM(2),
	.START_ADDR((SLAVE_MEMSIZE32 >> 1) << 2),
	.STOP_ADDR(((SLAVE_MEMSIZE32 >> 1) + (SLAVE_MEMSIZE32 >> 2) - 1) << 2),
	.RAND_SEED(`M2_RAND_SEED)
) master_2_exerciser (
	.clk(clk),
	.rst(rst),

	.master_req(master_2_req),
	.master_addr(master_2_addr),
	.master_cmd(master_2_cmd),
	.master_wdata(master_2_wdata),
	.master_ack(master_2_ack),
	.master_rdata(master_2_rdata),
	.master_resp(master_2_resp),

	.meminit_done(master_2_meminit_done),
	.memread_grant(memread_grant)
);

master_exerciser
#(
	.MNUM(3),
	.START_ADDR((SLAVE_MEMSIZE32 - 1) << 2),
	.STOP_ADDR(((SLAVE_MEMSIZE32 >> 1) + (SLAVE_MEMSIZE32 >> 2)) << 2),
	.RAND_SEED(`M3_RAND_SEED)
) master_3_exerciser (
	.clk(clk),
	.rst(rst),

	.master_req(master_3_req),
	.master_addr(master_3_addr),
	.master_cmd(master_3_cmd),
	.master_wdata(master_3_wdata),
	.master_ack(master_3_ack),
	.master_rdata(master_3_rdata),
	.master_resp(master_3_resp),

	.meminit_done(master_3_meminit_done),
	.memread_grant(memread_grant)
);

//////// TASKS ////////

////wait////
task WAIT
	(
	 input reg [15:0] periods
	 );
	integer i;
	begin
	for (i=0; i<periods; i=i+1)
		begin
		#(`PERIOD);
		end
	end
endtask

////reset all////
task RESET_ALL ();
	begin
	clk = 1'b0;

	rst = 1'b1;
	#(`HALF_PERIOD);
	rst = 1;
	#(`HALF_PERIOD*6);
	rst = 0;
	end
endtask

always #`HALF_PERIOD clk = ~clk;

var static tb_slavenum_reqtype_cov;
initial
	begin
	$display ("### PAVANA XBAR SIMULATION STARTED ###");
	RESET_ALL();
	$display ("### RESET COMPLETE! ###");
	end

// slave mem monitoring - every 10 cycles
integer mmon;
initial
	begin
	WAIT(10);
	@(posedge memread_grant)
	forever
		begin
		WAIT(10);
		for (mmon = 0; mmon < SLAVE_MEMSIZE32; mmon++)
			begin
			if (slave_0_mem_model.mem[mmon] != ((0 << 30) + (mmon << 2)) ) $fatal("MEMORY 0 CORRUPTED! word_num: %d, data: 0x%x", mmon, slave_0_mem_model.mem[mmon]);
			if (slave_1_mem_model.mem[mmon] != ((1 << 30) + (mmon << 2)) ) $fatal("MEMORY 1 CORRUPTED! word_num: %d, data: 0x%x", mmon, slave_1_mem_model.mem[mmon]);
			if (slave_2_mem_model.mem[mmon] != ((2 << 30) + (mmon << 2)) ) $fatal("MEMORY 2 CORRUPTED! word_num: %d, data: 0x%x", mmon, slave_2_mem_model.mem[mmon]);
			if (slave_3_mem_model.mem[mmon] != ((3 << 30) + (mmon << 2)) ) $fatal("MEMORY 3 CORRUPTED! word_num: %d, data: 0x%x", mmon, slave_3_mem_model.mem[mmon]);
			end
		end
	end

endmodule
