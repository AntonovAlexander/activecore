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
