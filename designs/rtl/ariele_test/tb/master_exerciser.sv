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
