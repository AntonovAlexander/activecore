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

module tb ();
//
reg CLK_100MHZ, RST, rx;
reg [15:0] SW;
wire [15:0] LED;
	
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

task UART_SEND_SERIALIZE
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

task UART_SEND 
	(
	 input reg [7:0] send_byte
	 );
	begin
	@(posedge DUT.udm.uart_rx.clk_i)
	DUT.udm.uart_rx.rx_done_tick_o <= 1'b1;
	DUT.udm.uart_rx.dout_bo <= send_byte;
	@(posedge DUT.udm.uart_rx.clk_i)
	DUT.udm.uart_rx.rx_done_tick_o <= 1'b0;
	DUT.udm.uart_rx.dout_bo <= 8'h0;
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

task udm_check ();
	begin
	UART_SEND(`SYNC_BYTE);
	UART_SEND(`IDCODE_CMD);
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
	WAIT(100);
	udm_check();
	udm_hreset();
	
	udm_wr_single(32'h00000000, 32'h33cc);
	udm_rd_single(32'h00000004);
	
	WAIT(50000);

	$display ("### TEST PROCEDURE FINISHED ###");
	$stop;
end
//
always #`HALF_PERIOD CLK_100MHZ = ~CLK_100MHZ;

always #1000 SW = SW + 8'h1;
//
endmodule
