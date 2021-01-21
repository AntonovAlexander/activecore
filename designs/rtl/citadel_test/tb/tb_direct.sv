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

localparam exec_EXEC = 1'b1;
localparam exec_RF   = 1'b0;
localparam RF_RD = 1'b0;
localparam RF_WR = 1'b1;


task CMD_EXEC
	(
		input logic unsigned [1:0] fu_id
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rs1
		, input logic unsigned [4:0] fu_rd
	);
	begin
	CMD(exec_EXEC, 0, 0, 0, fu_id, fu_rs0, fu_rs1, fu_rd);
	end
endtask

task CMD_RF_LOAD
	(
		input logic unsigned [4:0] rf_addr
		, input logic unsigned [31:0] rf_wdata
	);
	begin
	CMD(exec_RF, RF_WR, rf_addr, rf_wdata, 0, 0, 0, 0);
	end
endtask

task CMD_RF_STORE
	(
		input logic unsigned [4:0] rf_addr
	);
	begin
	CMD(exec_RF, RF_RD, rf_addr, 32'hdeadbeef, 0, 0, 0, 0);
	end
endtask

logic [31:0] cycle_counter = 0;
always @(posedge CLK_100MHZ) cycle_counter <= cycle_counter + 1;
assign cmd_resp_genfifo_ack = cycle_counter[2] & cycle_counter[1] & cycle_counter[0];

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

always @(posedge CLK_100MHZ)
	begin
	if (cmd_resp_genfifo_req && cmd_resp_genfifo_ack)
		begin
		$display("DATA OUTPUT: 0x%x", cmd_resp_genfifo_data);
		end
	end

/////////////////////////
// main test procesure //

initial
    begin

	$display ("### SIMULATION STARTED ###");

	RESET_ALL();
	WAIT(10);
	
	// fetching results
	CMD_RF_LOAD(0, 32'hbadc0ffe);
	CMD_RF_LOAD(1, 32'h1);
	CMD_RF_LOAD(2, 32'h2);
	CMD_RF_LOAD(3, 32'h3);
	CMD_RF_LOAD(7, 32'h7);
	CMD_RF_LOAD(9, 32'h9);
	CMD_RF_LOAD(31, 32'hfafae00f);
	
	CMD_EXEC(0, 0, 1, 5);

	CMD_RF_STORE(0);
	CMD_RF_STORE(1);
	CMD_RF_STORE(2);
	CMD_RF_STORE(3);
	CMD_RF_STORE(5);
	CMD_RF_STORE(7);
	CMD_RF_STORE(9);
	CMD_RF_STORE(31);
	
	WAIT(100);

	$display ("### TEST PROCEDURE FINISHED ###");
	$stop;
    end


endmodule
