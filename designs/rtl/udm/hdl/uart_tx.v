/*
 * uart_tx.v
 *
 *  Created on: 17.04.2016
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module uart_tx
#(
    parameter RTX_EXTERNAL_OVERRIDE = "NO"
)
(
	input clk_i, rst_i,

	input tx_start_i,
	input [7:0] din_bi,

	input locked_i,
	input [28:0] bitperiod_i,

	output reg tx_done_tick_o,
	output reg tx_o
);

reg [7:0] 	databuf;
reg [3:0] 	state;
reg [31:0] 	clk_counter;
reg [2:0] 	bit_counter;

localparam ST_IDLE 		= 8'h0;
localparam ST_START 	= 8'h1;
localparam ST_TX_DATA 	= 8'h2;
localparam ST_STOP 		= 8'h3;

always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		state <= ST_IDLE;
		databuf <= 8'h0;
		clk_counter <= 32'h0;
		tx_o <= 1'b1;
		tx_done_tick_o <= 1'b0;
		end
	else 
		begin
		
		if (RTX_EXTERNAL_OVERRIDE == "NO") tx_done_tick_o <= 1'b0;
		else tx_done_tick_o <= 1'b1;

		case (state)

			ST_IDLE:
				begin
				tx_o <= 1'b1;
				if ((tx_start_i == 1'b1) && (locked_i == 1'b1))
					begin
					tx_o <= 1'b0;
					state <= ST_START;
					databuf <= din_bi;
					clk_counter <= 32'h0;
					end
				end

			ST_START:
				begin
				clk_counter <= clk_counter + 32'h1;
				if (clk_counter == {3'h0, bitperiod_i})
					begin
					state <= ST_TX_DATA;
					clk_counter <= 32'h0;
					bit_counter <= 3'h0;
					tx_o <= databuf[0];
					databuf <= {1'b0, databuf[7:1]};
					end
				end

			ST_TX_DATA:
				begin
				clk_counter <= clk_counter + 32'h1;
				if (clk_counter == {3'h0, bitperiod_i})
					begin
					clk_counter <= 32'h0;
					bit_counter <= bit_counter + 3'h1;
					if (bit_counter == 3'h7)
						begin
						tx_o <= 1'b1;
						state <= ST_STOP;
						end
					else 
						begin
						tx_o <= databuf[0];
						databuf <= {1'b0, databuf[7:1]};
						end
					end
				end

			ST_STOP:
				begin
				clk_counter <= clk_counter + 32'h1;
				if (clk_counter == {2'h0, bitperiod_i, 1'b0})		// 2 * bit
					begin
					tx_o <= 1'b1;
					tx_done_tick_o <= 1'b1;
					state <= ST_IDLE;
					end
				end

		endcase

		end
	end

endmodule
