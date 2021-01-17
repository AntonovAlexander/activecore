/*
 * tb.v
 *
 *  Created on: 17.10.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`timescale 1ns / 1ps

`include "citadel_gen.svh"

`define HALF_PERIOD			5						//external 100 MHZ

module tb_direct ();
//
logic CLK_100MHZ, RST;
logic [15:0] SW;
logic [15:0] LED;

always #`HALF_PERIOD CLK_100MHZ = ~CLK_100MHZ;

logic cmd_req_genfifo_req;
citadel_gen_cmd_req_struct cmd_req_genfifo_data;
logic cmd_req_genfifo_ack;

logic cmd_resp_genfifo_req;
logic [31:0] cmd_resp_genfifo_data;
logic cmd_resp_genfifo_ack;

////reset all////
task RESET_ALL ();
    begin
    CLK_100MHZ = 1'b0;
    cmd_req_genfifo_req <= 1'b0;
	cmd_req_genfifo_data <= '{default:32'd0};
    RST = 1'b1;
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

task CMD
	(
		input citadel_gen_cmd_req_struct data
	);
	begin
	cmd_req_genfifo_req <= 1'b1;
	cmd_req_genfifo_data <= data;
	do begin
        @(posedge CLK_100MHZ);
    end while (!cmd_req_genfifo_ack);
    cmd_req_genfifo_req <= 1'b0;
	cmd_req_genfifo_data <= '{default:32'd0};
	end
endtask

citadel_gen citadel_inst
(
	.clk_i(CLK_100MHZ)
	, .rst_i(RST)

	, .cmd_req_genfifo_req_i(cmd_req_genfifo_req)
	, .cmd_req_genfifo_rdata_bi(cmd_req_genfifo_data)
	, .cmd_req_genfifo_ack_o(cmd_req_genfifo_ack)

	, .cmd_resp_genfifo_req_o(cmd_resp_genfifo_req)
	, .cmd_resp_genfifo_wdata_bo(cmd_resp_genfifo_data)
	, .cmd_resp_genfifo_ack_i(cmd_resp_genfifo_ack)
);

/////////////////////////
// main test procesure //

initial
    begin

	$display ("### SIMULATION STARTED ###");

	RESET_ALL();
	WAIT(100);
	
	WAIT(100);
	
	// fetching results
	CMD('{default:32'd0});
	CMD('{default:32'd0});
	
	WAIT(1000);

	$display ("### TEST PROCEDURE FINISHED ###");
	$stop;
    end


endmodule
