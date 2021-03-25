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

module credit_tb ();

logic clk, rst;

logic datain_req, datain_ack;
logic [15:0] datain = 0;
assign datain_req = 1'b1;

logic dataout_req, dataout_ack;
logic [15:0] dataout;
assign dataout_ack = 1'b1;

always @(posedge clk) datain = (datain == 400) ? 0 : (datain + 1);

taylor_credit_pipeline taylor_credit_pipeline_inst (
	.clk_i(clk)
	, .rst_i(rst)

	, .ext_datain_genfifo_req_i(datain_req)
	, .ext_datain_genfifo_rdata_bi(datain)
	, .ext_datain_genfifo_ack_o(datain_ack)

	, .ext_dataout_genfifo_req_o(dataout_req)
	, .ext_dataout_genfifo_wdata_bo(dataout)
	, .ext_dataout_genfifo_ack_i(dataout_ack)
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
	#1;
	rst = 0;
	end
endtask

always #`HALF_PERIOD clk = ~clk;

initial
	begin
	$display ("### TAYLOR CREDIT SIMULATION STARTED ###");
	RESET_ALL();
	$display ("### RESET COMPLETE! ###");

	WAIT(100);
	$display ("### SIMULATION COMPLETE! ###");
	$stop();
	end

endmodule
