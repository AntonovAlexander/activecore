/*
 * tb.v
 *
 *  Created on: 17.10.2019
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


module tb_udm ();
//
logic CLK_100MHZ, RST, rx;
logic [15:0] SW;
logic [15:0] LED;

always #`HALF_PERIOD CLK_100MHZ = ~CLK_100MHZ;

always #1000 SW = SW + 8'h1;
	
NEXYS4_DDR
#(
	.SIM("YES")
) DUT (
	.CLK100MHZ(CLK_100MHZ)
    , .CPU_RESETN(!RST)
    
    , .SW(SW)
    , .LED(LED)

    , .UART_TXD_IN(rx)
    , .UART_RXD_OUT()
);

////reset all////
task RESET_ALL ();
    begin
    CLK_100MHZ = 1'b0;
    RST = 1'b1;
    rx = 1'b1;
    #(`HALF_PERIOD/2);
    RST = 1;
    #(`HALF_PERIOD*6);
    RST = 0;
    end
endtask

////wait////
task WAIT
    (
     input logic [15:0] periods
     );
    begin
    integer i;
    for (i=0; i<periods; i=i+1)
        begin
        #(`HALF_PERIOD*2);
        end
    end
endtask

`define UDM_RX_SIGNAL rx
`define UDM_BLOCK DUT.udm
`include "../../udm/tb/udm.svh"
udm_driver udm;

/////////////////////////
// main test procesure //
localparam CSR_LED_ADDR         = 32'h00000000;
localparam CSR_SW_ADDR          = 32'h00000004;
localparam TESTMEM_ADDR         = 32'h80000000;

initial
    begin
    logic [31:0] wrdata [];
    integer ARRSIZE=10;
    
	$display ("### SIMULATION STARTED ###");
	
	udm.cfg(`DIVIDER_115200, 2'b00);

	SW = 8'h30;
	RESET_ALL();
	WAIT(100);
	udm.check();
	udm.hreset();
	
	// test data initialization
	udm.wr32(32'h10000000, 32'h112233cc);
	udm.wr32(32'h10000004, 32'h55aa55aa);
	udm.wr32(32'h10000008, 32'h01010202);
	udm.wr32(32'h1000000C, 32'h44556677);
	udm.wr32(32'h10000010, 32'h00000003);
	udm.wr32(32'h10000014, 32'h00000004);
	udm.wr32(32'h10000018, 32'h00000005);
	udm.wr32(32'h1000001C, 32'h00000006);
	udm.wr32(32'h10000020, 32'h00000007);
	udm.wr32(32'h10000024, 32'hdeadbeef);
	udm.wr32(32'h10000028, 32'hfefe8800);
	udm.wr32(32'h1000002C, 32'h23344556);
	udm.wr32(32'h10000030, 32'h05050505);
	udm.wr32(32'h10000034, 32'h07070707);
	udm.wr32(32'h10000038, 32'h99999999);
	udm.wr32(32'h1000003C, 32'hbadc0ffe);
	
	WAIT(100);
	
	// fetching results
	udm.rd32(32'h20000000);
	udm.rd32(32'h20000004);
	
	WAIT(1000);

	$display ("### TEST PROCEDURE FINISHED ###");
	$stop;
    end


endmodule
