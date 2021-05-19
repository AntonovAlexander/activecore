/*
 * tb.v
 *
 *  Created on: 17.10.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`timescale 1ns / 1ps

`include "../hw/citadel_gen/coregen/sverilog/citadel_fpu.svh"

`define HALF_PERIOD			5						//external 100 MHZ

module tb_direct ();
//
logic CLK_100MHZ, RST;
logic [15:0] SW;
logic [15:0] LED;

always #`HALF_PERIOD CLK_100MHZ = ~CLK_100MHZ;

logic cmd_req_genfifo_req;
citadel_fpu_cmd_req_struct cmd_req_genfifo_data;
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

typedef enum logic unsigned [0:0] { RF=1'b0, EXEC=1'b1 } CMD;
typedef enum logic unsigned [0:0] { RD=1'b0, WR=1'b1 } RF_CMD;

task INIT_CMD
	(
		input CMD
		, input RF_CMD
		, input logic unsigned [4:0]  rf_addr
		, input logic unsigned [31:0] rf_wdata
		, input logic unsigned [1:0]  fu_id
		, input logic unsigned [31:0] fu_imm_opcode
		, input logic unsigned [4:0]  fu_rs0
		, input logic unsigned [4:0]  fu_rs1
		, input logic unsigned [4:0]  fu_rs2
		, input logic unsigned [4:0]  fu_rd
	);
	begin
	cmd_req_genfifo_req <= 1'b1;
	cmd_req_genfifo_data.exec <= CMD;
	cmd_req_genfifo_data.rf_we <= RF_CMD;
	cmd_req_genfifo_data.rf_addr <= rf_addr;
	cmd_req_genfifo_data.rf_wdata <= rf_wdata;
	cmd_req_genfifo_data.fu_id <= fu_id;
	cmd_req_genfifo_data.fu_imm_opcode <= fu_imm_opcode;
	cmd_req_genfifo_data.fu_rs0 <= fu_rs0;
	cmd_req_genfifo_data.fu_rs1 <= fu_rs1;
	cmd_req_genfifo_data.fu_rs2 <= fu_rs2;
	cmd_req_genfifo_data.fu_rd <= fu_rd;
	do begin
        @(posedge CLK_100MHZ);
    end while (!cmd_req_genfifo_ack);
    cmd_req_genfifo_req <= 1'b0;
	cmd_req_genfifo_data <= '{default:32'd0};
	end
endtask

task CMD_EXEC
	(
		input logic unsigned [1:0] fu_id
		, input logic unsigned [31:0] fu_imm_opcode
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rs1
		, input logic unsigned [4:0] fu_rs2
		, input logic unsigned [4:0] fu_rd
	);
	begin
	INIT_CMD(EXEC, RD, 0, 0, fu_id, fu_imm_opcode, fu_rs0, fu_rs1, fu_rs2, fu_rd);
	end
endtask

task CMD_1RS
	(
		input logic unsigned [1:0] fu_id
		, input logic unsigned [31:0] fu_imm_opcode
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rd
	);
	begin
	CMD_EXEC(fu_id, fu_imm_opcode, fu_rs0, 0, 0, fu_rd);
	end
endtask

task CMD_2RS
	(
		input logic unsigned [1:0] fu_id
		, input logic unsigned [31:0] fu_imm_opcode
		, input logic unsigned [4:0] fu_rd
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rs1
	);
	begin
	CMD_EXEC(fu_id, fu_imm_opcode, fu_rs0, fu_rs1, 0, fu_rd);
	end
endtask

task CMD_3RS
	(
		input logic unsigned [1:0] fu_id
		, input logic unsigned [31:0] fu_imm_opcode
		, input logic unsigned [4:0] fu_rd
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rs1
		, input logic unsigned [4:0] fu_rs2
	);
	begin
	CMD_EXEC(fu_id, fu_imm_opcode, fu_rs0, fu_rs1, fu_rs2, fu_rd);
	end
endtask

task CMD_RF_LOAD
	(
		input logic unsigned [4:0] rf_addr
		, input logic unsigned [31:0] rf_wdata
	);
	begin
	INIT_CMD(RF, WR, rf_addr, rf_wdata, 0, 0, 0, 0, 0, 0);
	end
endtask

task CMD_RF_STORE
	(
		input logic unsigned [4:0] rf_addr
	);
	begin
	INIT_CMD(RF, RD, rf_addr, 32'hdeadbeef, 0, 0, 0, 0, 0, 0);
	end
endtask

logic [31:0] cycle_counter = 0;
always @(posedge CLK_100MHZ) cycle_counter <= cycle_counter + 1;
assign cmd_resp_genfifo_ack = cycle_counter[2] & cycle_counter[1] & cycle_counter[0];

citadel_fpu citadel_inst
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


shortreal log_data [31:0];
shortreal log_data_expected [31:0];
integer log_data_counter = 0;
always @(posedge CLK_100MHZ)
	begin
	if (cmd_resp_genfifo_req && cmd_resp_genfifo_ack)
		begin
		$display("DATA OUTPUT: hex: 0x%x, shortreal: %f", cmd_resp_genfifo_data, $bitstoshortreal(cmd_resp_genfifo_data));
		log_data[log_data_counter] = $bitstoshortreal(cmd_resp_genfifo_data);
		log_data_counter = log_data_counter + 1;
		end
	end

task RF_LOAD
	(
		input logic unsigned [4:0] rf_addr
		, input logic unsigned [31:0] rf_wdata
	);
	begin
	CMD_RF_LOAD(rf_addr, rf_wdata);
	log_data_expected[rf_addr] = $bitstoshortreal(rf_wdata);
	end
endtask

task EXEC_ADD
	(
		input logic unsigned [4:0] fu_rd
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rs1
	);
	begin
	CMD_2RS(0, 0, fu_rd, fu_rs0, fu_rs1);
	log_data_expected[fu_rd] = log_data_expected[fu_rs0] + log_data_expected[fu_rs1];
	end
endtask

task EXEC_SUB
	(
		input logic unsigned [4:0] fu_rd
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rs1
	);
	begin
	CMD_2RS(0, 1, fu_rd, fu_rs0, fu_rs1);
	log_data_expected[fu_rd] = log_data_expected[fu_rs0] - log_data_expected[fu_rs1];
	end
endtask

task EXEC_MUL
	(
		input logic unsigned [4:0] fu_rd
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rs1
	);
	begin
	CMD_2RS(1, 0, fu_rd, fu_rs0, fu_rs1);
	log_data_expected[fu_rd] = log_data_expected[fu_rs0] * log_data_expected[fu_rs1];
	end
endtask

task EXEC_DIV
	(
		input logic unsigned [4:0] fu_rd
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rs1
	);
	begin
	CMD_2RS(2, 0, fu_rd, fu_rs0, fu_rs1);
	log_data_expected[fu_rd] = log_data_expected[fu_rs0] / log_data_expected[fu_rs1];
	end
endtask

task EXEC_FMA
	(
		input logic unsigned [4:0] fu_rd
		, input logic unsigned [4:0] fu_rs0
		, input logic unsigned [4:0] fu_rs1
		, input logic unsigned [4:0] fu_rs2
	);
	begin
	CMD_3RS(3, 0, fu_rd, fu_rs0, fu_rs1, fu_rs2);
	log_data_expected[fu_rd] = (log_data_expected[fu_rs0] * log_data_expected[fu_rs1] + log_data_expected[fu_rs2]);
	end
endtask

function logic eq_shortreals (input shortreal src0, input shortreal src1);
    begin
    if (src0 > src1) return ((src0 - src1) > 0.0001);
    else return ((src1 - src0) > 0.0001);
    end
endfunction 

logic test_passed = 1'b1;
task SCAN_REGS ();
    begin
    // fetching results
    for (int i=0; i<32; i=i+1)
        begin
        CMD_RF_STORE(i);
        end
     
	WAIT(100);
	
	// checks
	test_passed = 1'b1;
	for (int i=0; i<32; i=i+1)
	   begin
	   if (eq_shortreals(log_data[i], log_data_expected[i]))
	       begin
	       test_passed = 1'b0;
	       $display("error at address %2d: expected output: %f, actual outputs: %f", i, log_data_expected[i], log_data[i]);
	       end
	   end
	if (test_passed) $display ("### TEST PASSED ###");
	else $error("### TEST FAILED ###");
	
    end
endtask;

/////////////////////////
// main test procesure //
initial
    begin

	$display ("### SIMULATION STARTED ###");

	RESET_ALL();
	WAIT(10);
	
	// initialization
	RF_LOAD(0, $shortrealtobits(2.4));
	RF_LOAD(1, $shortrealtobits(1.0));
	RF_LOAD(2, $shortrealtobits(2.0));
	RF_LOAD(3, $shortrealtobits(3.0));
	RF_LOAD(7, $shortrealtobits(7.0));
	RF_LOAD(9, $shortrealtobits(9.0));
	RF_LOAD(15, $shortrealtobits(15.0));
	
	EXEC_ADD(5, 0, 1);
	EXEC_ADD(6, 2, 5);
	EXEC_MUL(10, 3, 6);
	EXEC_DIV(11, 7, 5);
	EXEC_SUB(12, 6, 3);
	EXEC_FMA(16, 5, 6, 10);

    SCAN_REGS();

	$display ("### TEST PROCEDURE FINISHED ###");
	$stop;
    end


endmodule
