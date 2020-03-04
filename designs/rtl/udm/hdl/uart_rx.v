/*
 * uart_rx.v
 *
 *  Created on: 17.04.2016
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module uart_rx
#(
    parameter RTX_EXTERNAL_OVERRIDE = "NO"
)
(
	input clk_i, rst_i,

	input rx_i,
	output reg rx_done_tick_o,
	output reg [7:0] dout_bo,

	output reg locked_o,
	output reg [28:0] bitperiod_o
);

localparam ST_NOSYNC 			= 4'h0;
localparam ST_NOSYNC_WAIT1_1 	= 4'h1;
localparam ST_NOSYNC_WAIT0_2 	= 4'h2;
localparam ST_NOSYNC_WAIT1_3 	= 4'h3;
localparam ST_NOSYNC_WAIT0_4 	= 4'h4;
localparam ST_NOSYNC_WAIT1_5 	= 4'h5;
localparam ST_NOSYNC_WAIT0_6 	= 4'h6;
localparam ST_NOSYNC_WAIT1_7 	= 4'h7;
localparam ST_NOSYNC_WAIT0_8 	= 4'h8;
localparam ST_NOSYNC_WAIT_STOP 	= 4'h9;
localparam ST_SYNC 				= 4'hA;
localparam ST_SYNC_WAIT_START 	= 4'hB;
localparam ST_SYNC_RX_DATA 		= 4'hC;
localparam ST_SYNC_WAIT_STOP 	= 4'hD;

reg [4:0] 	state;
reg [31:0] 	clk_counter;
reg [2:0]	bit_counter;

reg rx_buf;
always @(posedge clk_i)
	begin
	if (rst_i) rx_buf <= 1'b1;
	else rx_buf <= rx_i;
	end

always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		state <= ST_NOSYNC;
		clk_counter <= 32'h0;
		bit_counter <= 3'h0;
		locked_o <= 1'b0;
		bitperiod_o <= 32'h0;
		rx_done_tick_o <= 1'b0;
		dout_bo <= 8'h0;
		end
	else 
		begin

		if (RTX_EXTERNAL_OVERRIDE == "NO") rx_done_tick_o <= 1'b0;

		case (state)

			ST_NOSYNC:
				begin
				if (rx_buf == 1'b0) state <= ST_NOSYNC_WAIT1_1;
				end

			ST_NOSYNC_WAIT1_1:
				begin
				if (rx_buf == 1'b1) state <= ST_NOSYNC_WAIT0_2;
				end

			ST_NOSYNC_WAIT0_2:
				begin
				if (rx_buf == 1'b0) state <= ST_NOSYNC_WAIT1_3;
				clk_counter <= clk_counter + 32'h1;
				end

			ST_NOSYNC_WAIT1_3:
				begin
				if (rx_buf == 1'b1) state <= ST_NOSYNC_WAIT0_4;
				clk_counter <= clk_counter + 32'h1;
				end

			ST_NOSYNC_WAIT0_4:
				begin
				if (rx_buf == 1'b0) state <= ST_NOSYNC_WAIT1_5;
				clk_counter <= clk_counter + 32'h1;
				end

			ST_NOSYNC_WAIT1_5:
				begin
				if (rx_buf == 1'b1) state <= ST_NOSYNC_WAIT0_6;
				clk_counter <= clk_counter + 32'h1;
				end

			ST_NOSYNC_WAIT0_6:
				begin
				if (rx_buf == 1'b0) state <= ST_NOSYNC_WAIT1_7;
				clk_counter <= clk_counter + 32'h1;
				end

			ST_NOSYNC_WAIT1_7:
				begin
				if (rx_buf == 1'b1) state <= ST_NOSYNC_WAIT0_8;
				clk_counter <= clk_counter + 32'h1;
				end

			ST_NOSYNC_WAIT0_8:
				begin
				if (rx_buf == 1'b0) state <= ST_NOSYNC_WAIT_STOP;
				clk_counter <= clk_counter + 32'h1;
				end

			ST_NOSYNC_WAIT_STOP:
				begin
				if (rx_buf == 1'b1)
					begin
					state <= ST_SYNC;
					locked_o <= 1'b1;
					bitperiod_o <= clk_counter[31:3];		// clk_counter / 8
					dout_bo <= 32'h55;
					rx_done_tick_o <= 1'b1;
					end
				clk_counter <= clk_counter + 32'h1;
				end

			ST_SYNC:
				begin
				if (rx_buf == 1'b0)
					begin
					state <= ST_SYNC_WAIT_START;
					clk_counter <= 32'h0;
					end
				end

			ST_SYNC_WAIT_START:
				begin
				clk_counter <= clk_counter + 32'h1;
				if (clk_counter == {4'h0, bitperiod_o[28:1]})
					begin
					state <= ST_SYNC_RX_DATA;
					clk_counter <= 32'h0;
					bit_counter <= 3'h0;
					end
				end

			ST_SYNC_RX_DATA:
				begin
				clk_counter <= clk_counter + 32'h1;
				if (clk_counter == {3'h0, bitperiod_o})
					begin
					dout_bo <= {rx_buf, dout_bo[7:1]};
					clk_counter <= 32'h0;
					bit_counter <= bit_counter + 3'h1;
					if (bit_counter == 3'h7)
						begin
						rx_done_tick_o <= 1'b1;
						state <= ST_SYNC_WAIT_STOP;
						end
					end
				end

			ST_SYNC_WAIT_STOP:
				begin
				if (rx_buf == 1'b1) state <= ST_SYNC;
				end

		endcase

		end
	end

endmodule
