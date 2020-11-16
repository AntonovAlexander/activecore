/*
 * riscv_tb.v
 *
 *  Created on: 24.09.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`timescale 1ns / 1ps

`define HALF_PERIOD			5						//external 100 MHZ
`define DIVIDER_115200		32'd8680
`define DIVIDER_19200		32'd52083
`define DIVIDER_9600		32'd104166
`define DIVIDER_4800		32'd208333
`define DIVIDER_2400		32'd416666


module riscv_tb ();
//
reg CLK_100MHZ, RST, rx;
reg [31:0] SW;
wire [31:0] LED;
reg irq_btn;
	
sigma
#(
	.CPU("riscv_1stage")
	//.CPU("riscv_2stage")
	//.CPU("riscv_3stage")
	//.CPU("riscv_4stage")
	//.CPU("riscv_5stage")
	//.CPU("riscv_6stage")

	, .UDM_RTX_EXTERNAL_OVERRIDE("YES")
	, .delay_test_flag(0)
	
	, .mem_init("YES")
	, .mem_type("elf")
	, .mem_data("../../sw/benchmarks/heartbeat_variable.riscv")
	, .mem_size(8192)
) sigma
(
	.clk_i(CLK_100MHZ)
	, .arst_i(RST)
	, .irq_btn_i(irq_btn)
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
	CLK_100MHZ = 1'b0;
	RST = 1'b1;
	irq_btn = 1'b0;
	rx = 1'b1;
	#(`HALF_PERIOD/2);
	RST = 1;
	#(`HALF_PERIOD*6);
	RST = 0;
end
endtask

`define UDM_RX_SIGNAL rx
`define UDM_BLOCK sigma.udm
`include "udm.svh"
udm_driver udm;

///////////////////
// initial block //
localparam CPU_RAM_ADDR         = 32'h00000000;
localparam CSR_LED_ADDR         = 32'h80000000;
localparam CSR_SW_ADDR          = 32'h80000004;

initial
begin
	$display ("### SIMULATION STARTED ###");

	SW = 8'h30;
	RESET_ALL();
	WAIT(1000);
	irq_btn = 1'b0;
	WAIT(100);
	irq_btn = 1'b0;
	WAIT(50);
	udm.check();
	udm.hreset();
	
	udm.wr32(CSR_LED_ADDR, 32'hdeadbeef);
	udm.rd32(CSR_SW_ADDR);
	
	WAIT(50000);

	$display ("### TEST PROCEDURE FINISHED ###");
	$stop;
end
//
always #`HALF_PERIOD CLK_100MHZ = ~CLK_100MHZ;

always #1000 SW = SW + 8'h1;
//
endmodule
