/*
 * xbar_tb.sv
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
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
wire master_0_ack;
wire [31:0] master_0_addr;
wire master_0_we;
wire [31:0] master_0_wdata;
wire [31:0] master_0_rdata;
wire master_0_resp;

wire master_1_req;
wire master_1_ack;
wire [31:0] master_1_addr;
wire master_1_we;
wire [31:0] master_1_wdata;
wire [31:0] master_1_rdata;
wire master_1_resp;

wire master_2_req;
wire master_2_ack;
wire [31:0] master_2_addr;
wire master_2_we;
wire [31:0] master_2_wdata;
wire [31:0] master_2_rdata;
wire master_2_resp;

wire master_3_req;
wire master_3_ack;
wire [31:0] master_3_addr;
wire master_3_we;
wire [31:0] master_3_wdata;
wire [31:0] master_3_rdata;
wire master_3_resp;

wire slave_0_req;
wire slave_0_ack;
wire [31:0] slave_0_addr;
wire slave_0_we;
wire [31:0] slave_0_wdata;
wire [31:0] slave_0_rdata;
wire slave_0_resp;

wire slave_1_req;
wire slave_1_ack;
wire [31:0] slave_1_addr;
wire slave_1_we;
wire [31:0] slave_1_wdata;
wire [31:0] slave_1_rdata;
wire slave_1_resp;

wire slave_2_req;
wire slave_2_ack;
wire [31:0] slave_2_addr;
wire slave_2_we;
wire [31:0] slave_2_wdata;
wire [31:0] slave_2_rdata;
wire slave_2_resp;

wire slave_3_req;
wire slave_3_ack;
wire [31:0] slave_3_addr;
wire slave_3_we;
wire [31:0] slave_3_wdata;
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
	.master_cmd(master_0_we),
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
	.master_cmd(master_1_we),
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
	.master_cmd(master_2_we),
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
	.master_cmd(master_3_we),
	.master_wdata(master_3_wdata),
	.master_ack(master_3_ack),
	.master_rdata(master_3_rdata),
	.master_resp(master_3_resp),

	.rdreq_fifo_full(rdreq3_fifo_full)
);

ariele_xbar DUV
(
	.clk_i(clk),
	.rst_i(rst),

	.m0_req_i(master_0_req),
	.m0_ack_o(master_0_ack),
	.m0_addr_bi(master_0_addr),
	.m0_we_i(master_0_we),
	.m0_wdata_bi(master_0_wdata),
	.m0_resp_o(master_0_resp),
	.m0_rdata_bo(master_0_rdata),

	.m1_req_i(master_1_req),
	.m1_ack_o(master_1_ack),
	.m1_addr_bi(master_1_addr),
	.m1_we_i(master_1_we),
	.m1_wdata_bi(master_1_wdata),
	.m1_resp_o(master_1_resp),
	.m1_rdata_bo(master_1_rdata),

	.m2_req_i(master_2_req),
	.m2_ack_o(master_2_ack),
	.m2_addr_bi(master_2_addr),
	.m2_we_i(master_2_we),
	.m2_wdata_bi(master_2_wdata),
	.m2_resp_o(master_2_resp),
	.m2_rdata_bo(master_2_rdata),

	.m3_req_i(master_3_req),
	.m3_ack_o(master_3_ack),
	.m3_addr_bi(master_3_addr),
	.m3_we_i(master_3_we),
	.m3_wdata_bi(master_3_wdata),
	.m3_resp_o(master_3_resp),
	.m3_rdata_bo(master_3_rdata),

	.s0_req_o(slave_0_req),
	.s0_ack_i(slave_0_ack),
	.s0_addr_bo(slave_0_addr),
	.s0_we_o(slave_0_we),
	.s0_wdata_bo(slave_0_wdata),
	.s0_resp_i(slave_0_resp),
	.s0_rdata_bi(slave_0_rdata),

	.s1_req_o(slave_1_req),
	.s1_ack_i(slave_1_ack),
	.s1_addr_bo(slave_1_addr),
	.s1_we_o(slave_1_we),
	.s1_wdata_bo(slave_1_wdata),
	.s1_resp_i(slave_1_resp),
	.s1_rdata_bi(slave_1_rdata),

	.s2_req_o(slave_2_req),
	.s2_ack_i(slave_2_ack),
	.s2_addr_bo(slave_2_addr),
	.s2_we_o(slave_2_we),
	.s2_wdata_bo(slave_2_wdata),
	.s2_resp_i(slave_2_resp),
	.s2_rdata_bi(slave_2_rdata),

	.s3_req_o(slave_3_req),
	.s3_ack_i(slave_3_ack),
	.s3_addr_bo(slave_3_addr),
	.s3_we_o(slave_3_we),
	.s3_wdata_bo(slave_3_wdata),
	.s3_resp_i(slave_3_resp),
	.s3_rdata_bi(slave_3_rdata)
);

slave_mem_model_inorder
#(
	.MEMSIZE32(SLAVE_MEMSIZE32)
) slave_0_mem_model (
	.clk_i(clk),
	.rst_i(rst),

	.slave_req(slave_0_req),
	.slave_addr(slave_0_addr),
	.slave_cmd(slave_0_we),
	.slave_wdata(slave_0_wdata),
	.slave_ack(slave_0_ack),
	.slave_rdata(slave_0_rdata),
	.slave_resp(slave_0_resp)
);

slave_mem_model_inorder
#(
	.MEMSIZE32(SLAVE_MEMSIZE32)
) slave_1_mem_model (
	.clk_i(clk),
	.rst_i(rst),

	.slave_req(slave_1_req),
	.slave_addr(slave_1_addr),
	.slave_cmd(slave_1_we),
	.slave_wdata(slave_1_wdata),
	.slave_ack(slave_1_ack),
	.slave_rdata(slave_1_rdata),
	.slave_resp(slave_1_resp)
);

slave_mem_model_inorder
#(
	.MEMSIZE32(SLAVE_MEMSIZE32)
) slave_2_mem_model (
	.clk_i(clk),
	.rst_i(rst),

	.slave_req(slave_2_req),
	.slave_addr(slave_2_addr),
	.slave_cmd(slave_2_we),
	.slave_wdata(slave_2_wdata),
	.slave_ack(slave_2_ack),
	.slave_rdata(slave_2_rdata),
	.slave_resp(slave_2_resp)
);

slave_mem_model_inorder
#(
	.MEMSIZE32(SLAVE_MEMSIZE32)
) slave_3_mem_model (
	.clk_i(clk),
	.rst_i(rst),

	.slave_req(slave_3_req),
	.slave_addr(slave_3_addr),
	.slave_cmd(slave_3_we),
	.slave_wdata(slave_3_wdata),
	.slave_ack(slave_3_ack),
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
	.master_cmd(master_0_we),
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
	.master_cmd(master_1_we),
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
	.master_cmd(master_2_we),
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
	.master_cmd(master_3_we),
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
	$display ("### ARIELE XBAR SIMULATION STARTED ###");
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
		WAIT(100);
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
