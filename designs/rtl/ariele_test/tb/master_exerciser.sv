/*
 * master_exerciser.sv
 *
 *  Created on: 20.11.2016
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`timescale 1ns / 1ps

`define HALF_PERIOD			10						// external 50 MHZ
`define PERIOD				(2*`HALF_PERIOD)

module master_exerciser
#(
	parameter MNUM = 0, START_ADDR = 0, STOP_ADDR = 1000, RAND_SEED = 1
)
(
	input clk,
	input rst,

	output reg master_req,
	output reg [31:0] master_addr,
	output reg master_cmd,
	output reg [31:0] master_wdata,
	input wire master_ack,
	input wire [31:0] master_rdata,
	input wire master_resp,

	output reg meminit_done,
	input memread_grant
);

reg meminit_done_next;

always @(posedge clk)
	begin
	if (rst) meminit_done <= 1'b0;
	else meminit_done <= meminit_done_next;
	end

task RESET ();
	begin
	master_req <= 1'b0;
	master_addr <= 32'h0;
	master_cmd <= 1'b0;
	master_wdata <= 32'h0;

	meminit_done_next <= 1'b0;

	#(`PERIOD*3);
	end
endtask

// write //
task BUS_TRANSACTION
	(
	 input reg [1:0] slave_num,
	 input reg cmd,
	 input reg [31:0] addr,
	 input reg [31:0] wdata
	 );
	begin
	master_req <= 1'b1;
	master_cmd <= cmd;
	master_addr <= addr + (slave_num << 30);
	master_wdata <= wdata;
	#(`PERIOD);
	while (master_ack == 1'b0)
		begin
		#(`PERIOD);
		end
	master_req <= 1'b0;
	master_cmd <= 1'b0;
	end
endtask

class Master_rdreq;
	rand bit [1:0] snum;
	rand bit [29:0] addr;

	constraint addr_constr {
		addr < 1024;
	}
endclass

Master_rdreq rdreq;

// exercise logic //
integer i;
integer snum;
initial
	begin
	RESET();
	rdreq = new();
	rdreq.srandom(RAND_SEED);
	#(`HALF_PERIOD*11);
	$display ("### MASTER %d STARTED ###", MNUM);

	$display ("### MASTER %d MEMORY INITIALIZATION SEQUENCE STARTED ###", MNUM);
	for (snum = 0; snum < 4; snum = snum + 1)
		begin
		if (STOP_ADDR > START_ADDR)
			begin
			for (i = START_ADDR; i < STOP_ADDR + 1; i = i + 4)
				begin
				BUS_TRANSACTION(snum, 1, i, (snum << 30) + i);
				end
			end
		else 
			begin
			for (i = START_ADDR; i > STOP_ADDR - 1; i = i - 4)
				begin
				BUS_TRANSACTION(snum, 1, i, (snum << 30) + i);
				end
			end
		end
	meminit_done_next = 1'b1;
	$display ("### MASTER %d MEMORY INITIALIZATION SEQUENCE COMPLETE ###", MNUM);

	while (memread_grant == 1'b0) #`PERIOD;

	$display ("### MASTER %d MEMORY ANALYSIS STARTED ###", MNUM);
	forever
		begin
		rdreq.randomize();
		BUS_TRANSACTION(rdreq.snum, 0, (rdreq.addr << 2), 32'hdeadbeef);
		end
	$display ("### MASTER %d MEMORY ANALYSIS COMPLETE ###", MNUM);
	end

endmodule
