/*
 PSS

 Copyright (c) 2016 Alexander Antonov <153287@niuitmo.ru>
 All rights reserved.

 Version 0.99

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
 PSS PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


`timescale 1ns / 1ps

`define HALF_PERIOD			10						//external 50 MHZ
`define DIVIDER_115200		32'd8680
`define DIVIDER_19200		32'd52083
`define DIVIDER_9600		32'd104166
`define DIVIDER_4800		32'd208333
`define DIVIDER_2400		32'd416666

// udm interface
`define SYNC_BYTE			8'h55
`define ESCAPE_BYTE			8'h5a
`define IDCODE_CMD			8'h00	// check udm accessibility
`define RST_CMD				8'h80	// Reset slave	
`define nRST_CMD			8'hC0	// nReset slave	
`define WR_CMD 				8'h81	// Write slave with autoincrement
`define RD_CMD 				8'h82	// Read slave with autoincrement
`define WR_CMD_NOINC 		8'h83	// Write slave without autoincrement
`define RD_CMD_NOINC 		8'h84	// Read slave without autoincrement

module dlx_tb ();
//
reg CLK_50MHZ, RST, rx;
reg [31:0] SW;
wire [31:0] LED;
	
dlx_udm_memsplit
#(
	//.mem_data("../../../../activecore/dlx/sw/io_heartbeat/io_heartbeat.hex"),
	.mem_data("../../../../activecore/dlx/sw/io_heartbeat_variable/io_heartbeat_variable.hex"),
	.mem_size(1024)
) dlx_udm
(
	.clk_i(CLK_50MHZ)
	, .rst_i(RST)
	, .rx_i(rx)
	//, .tx_o()
	, .gpio_bi(SW)
	, .gpio_bo(LED)
);

//////////////////////////
/////////tasks////////////
//////////////////////////

reg parity;
integer i, j, k;

reg [32:0] rate;
reg [1:0] configuration;


////wait////
task WAIT
	(
	 input reg [15:0] periods
	 );
begin
for (i=0; i<periods; i=i+1)
	begin
	#(`HALF_PERIOD*2);
	end
end
endtask


////reset all////
task RESET_ALL ();
begin
	CLK_50MHZ = 1'b0;
	RST = 1'b1;
	rx = 1'b1;
	#(`HALF_PERIOD/2);
	RST = 1;
	#(`HALF_PERIOD*6);
	RST = 0;
end
endtask


task UART_CFG
	(
		input reg [32:0] rate_i,
		input reg [1:0] configuration_i
	);
	begin
	rate = rate_i;
	configuration = configuration_i;
	end
endtask

////Send byte to UART////

task UART_SEND 
	(
	 input reg [7:0] send_byte
	 );
	begin
	parity = 0;
	//start
	rx = 1'b0;
	#rate;
	//sending data
	for (i=0; i<8; i=i+1)
		begin
		rx = send_byte[0];
		#rate;
		parity = parity ^ send_byte[0];
		send_byte = send_byte >> 1;
		end
	//parity
	if (configuration != 2'b00)
		begin
		if (configuration == 2'b10)
			begin
			rx = parity;
			#rate;
			end
		else if (configuration == 2'b01)
			begin
			rx = ~parity;
			#rate;
			end
		end
	//stop;
		rx = 1'b1;
		#rate;
	end
endtask


task udm_rst ();
	begin
	UART_SEND(`SYNC_BYTE);
	UART_SEND(`RST_CMD);
	end
endtask

task udm_nrst ();
	begin
	UART_SEND(`SYNC_BYTE);
	UART_SEND(`nRST_CMD);
	end
endtask

task udm_hreset ();
	begin
	udm_rst();
	udm_nrst();
	end
endtask

task udm_sendbyte
	(
		input reg [7:0] databyte
	);
	begin
	if ((databyte == `SYNC_BYTE) || (databyte == `ESCAPE_BYTE))
		UART_SEND(`ESCAPE_BYTE);
	UART_SEND(databyte);
	end
endtask


task udm_sendword_le
	(
		input reg [31:0] dataword
	);
	begin
	udm_sendbyte(dataword[7:0]);
	udm_sendbyte(dataword[15:8]);
	udm_sendbyte(dataword[23:16]);
	udm_sendbyte(dataword[31:24]);
	end
endtask


task udm_sendword_be
	(
		input reg [31:0] dataword
	);
	begin
	udm_sendbyte(dataword[31:24]);
	udm_sendbyte(dataword[23:16]);
	udm_sendbyte(dataword[15:8]);
	udm_sendbyte(dataword[7:0]);
	end
endtask


task udm_wr_single
	(
		input reg [31:0] wr_addr,
		input reg [31:0] wr_data
	);
	begin

	// header
	UART_SEND(`SYNC_BYTE);
	UART_SEND(`WR_CMD);
	
	// address
	udm_sendword_le(wr_addr);

	// length
	udm_sendword_le(32'h4);
	
	// data
	udm_sendword_le(wr_data);

	end
endtask


task udm_rd_single
	(
		input reg [31:0] wr_addr
	);
	begin
	
	// header
	UART_SEND(`SYNC_BYTE);
	UART_SEND(`RD_CMD);
	
	// address
	udm_sendword_le(wr_addr);

	// length
	udm_sendword_le(32'h4);

	end
endtask


//////////////////////////
//initial block
initial
begin
	$display ("### SIMULATION STARTED ###");

	SW = 8'h30;
	RESET_ALL();
	UART_CFG(`DIVIDER_115200, 2'b00);
	WAIT(50000);
	UART_SEND(`SYNC_BYTE);
	UART_SEND(`IDCODE_CMD);
	udm_hreset();
	
	udm_rd_single(32'h00000000);
	udm_wr_single(32'h00000000, 32'h123455aa);
	udm_rd_single(32'h00000000);
	udm_wr_single(32'h80000000, 32'h5aaa5aaa);
	udm_rd_single(32'h80000004);
	/*
	WAIT(50000);
	$display ("### READING MEMORY ###");
	udm_rd_single(32'h00000000);
	udm_rd_single(32'h00000004);
	udm_rd_single(32'h00000008);
	udm_rd_single(32'h0000000C);
	WAIT(777);
	udm_rd_single(32'h00000000);
	udm_rd_single(32'h00000004);
	udm_rd_single(32'h00000008);
	udm_rd_single(32'h0000000C);
	*/
	
	
	WAIT(50000);

	$display ("### TEST PROCEDURE FINISHED ###");
	$stop;
end
//
always #`HALF_PERIOD CLK_50MHZ = ~CLK_50MHZ;

always #1000 SW = SW + 8'h1;
//
endmodule
