/*
 * tb.v
 *
 *  Created on: 17.10.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`timescale 1ns / 1ps

`include "../hw/citadel_gen/coregen/sverilog/citadel_gen.svh"

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
	cmd_resp_genfifo_ack <= 1'b1;
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
        @(posedge CLK_100MHZ);
        end
    end
endtask

task CMD
	(
		input logic unsigned [0:0] exec
		, input logic unsigned [0:0] rf_we
		, input logic unsigned [4:0] rf_addr
		, input logic unsigned [31:0] rf_wdata
		, input logic unsigned [1:0] fu_id
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rs1
		, input logic unsigned [4:0] fu_rd
	);
	begin
	cmd_req_genfifo_req <= 1'b1;
	cmd_req_genfifo_data.exec <= exec;
	cmd_req_genfifo_data.rf_we <= rf_we;
	cmd_req_genfifo_data.rf_addr <= rf_addr;
	cmd_req_genfifo_data.rf_wdata <= rf_wdata;
	cmd_req_genfifo_data.fu_id <= fu_id;
	cmd_req_genfifo_data.fu_rs0 <= fu_rs0;
	cmd_req_genfifo_data.fu_rs1 <= fu_rs1;
	cmd_req_genfifo_data.fu_rd <= fu_rd;
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
	
	// fetching results
	CMD(0, 0, 0, 0, 0, 0, 0, 0);
	WAIT(10);
	CMD(1, 2, 3, 4, 5, 6, 7, 8);
	
	WAIT(1000);

	$display ("### TEST PROCEDURE FINISHED ###");
	$stop;
    end


endmodule
