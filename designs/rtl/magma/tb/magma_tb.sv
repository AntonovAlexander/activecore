/*
 * riscv_tb.v
 *
 *  Created on: 24.09.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
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

module magma_tb ();
//
reg CLK_50MHZ, RST, rx;
reg [31:0] SW;
wire [31:0] LED;
	
magma
#(
	.CPU("riscv_5stage"),
	.mem_init("NO"),
	.mem_data("../../sw/io_heartbeat_variable.hex"),
	.mem_size(1024)
) magma
(
	.clk_i(CLK_50MHZ)
	, .arst_i(RST)
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

`define UDM_RX_SIGNAL rx
`define UDM_BLOCK magma.udm
`include "udm.svh"
udm_driver udm;

//////////////////////////
//initial block
initial
begin
	$display ("### SIMULATION STARTED ###");

	SW = 8'h30;
	RESET_ALL();
	WAIT(50);
	udm.check();
	udm.hreset();
	
	udm.rd32(32'h00000000);
	udm.wr32(32'h00000000, 32'h123455aa);
	udm.rd32(32'h00000000);
	udm.wr32(32'h80000000, 32'h5aaa5aaa);
	udm.rd32(32'h80000004);
	
	
	WAIT(50000);

	$display ("### TEST PROCEDURE FINISHED ###");
	$stop;
end
//
always #`HALF_PERIOD CLK_50MHZ = ~CLK_50MHZ;

always #1000 SW = SW + 8'h1;
//
endmodule
