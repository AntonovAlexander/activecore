/*
 * udm_controller.v
 *
 *  Created on: 17.04.2016
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module udm_controller
(
	input clk_i, reset_i,

	// uart rx
	input rx_done_tick_i,
    input [7:0] rx_din_bi,

	// uart tx
	output reg [7:0] tx_dout_bo,
	output reg tx_start_o,
    input tx_done_tick_i,
	
	// bus
	output reg rst_o,
	output reg bus_enb_o,
	output reg bus_we_o,
	output reg [31:0] bus_addr_bo,
    output reg [31:0] bus_wdata_bo,

    input bus_ack_i,
    input [31:0] bus_rdata_bi
);

// control bytes
localparam SYNC_BYTE = 8'h55;
localparam ESCAPE_BYTE = 8'h5a;

// commands
localparam IDCODE_CMD = 8'h00;		// check udm accessibility
localparam RST_CMD = 8'h80;			// Reset slave
localparam nRST_CMD = 8'hC0;		// nReset slave
localparam WR_INC_CMD = 8'h81;		// Write slave with autoincrement
localparam RD_INC_CMD = 8'h82;		// Read slave with autoincrement
localparam WR_NOINC_CMD = 8'h83;	// Write slave without autoincrement
localparam RD_NOINC_CMD = 8'h84;	// Read slave without autoincrement


reg rx_req, rx_sync;
reg [7:0] r_data;

// rx sync logic
reg escape_received;
always @(posedge clk_i)
	begin
	if (reset_i)
		begin
		rx_req <= 1'b0;
		rx_sync <= 1'b0;
		r_data <= 8'h0;
		escape_received <= 1'b0;
		end
	else 
		begin
		rx_req <= 1'b0;
		rx_sync <= 1'b0;
		r_data <= 8'h0;

		if (escape_received == 1'b0)
			begin
			if (rx_done_tick_i == 1'b1)
				begin
				if (rx_din_bi == SYNC_BYTE)
					rx_sync <= 1'b1;
				else if (rx_din_bi == ESCAPE_BYTE)
					escape_received <= 1'b1;
				else 
					begin
					rx_req <= 1'b1;
					r_data <= rx_din_bi;
					end
				end
			end
		else 
			begin
			if (rx_done_tick_i == 1'b1)
				begin
				rx_req <= 1'b1;
				r_data <= rx_din_bi;
				escape_received <= 1'b0;
				end
			end
		end
	end

// states
localparam IDLE = 8'h00;
localparam FETCH_ADDR = 8'h01;
localparam FETCH_LENGTH = 8'h02;
localparam FETCH_DATA = 8'h03;
localparam WAIT_ACCEPT = 8'h04;
localparam RD_DATA = 8'h05;
localparam TX_DATA = 8'h06;
localparam WAIT_TX = 8'h07;

reg [7:0] state;
reg [1:0] counter;
reg cmd_ff, autoinc_ff;
reg [31:0] RD_DATA_reg;
reg [31:0] tr_length;

always @(posedge clk_i, posedge reset_i)
	begin
	if (reset_i == 1'b1)
		begin
		rst_o <= 1'b0;

		state <= IDLE;
		tx_start_o <= 1'b0;
		
		bus_enb_o <= 1'b0;
		bus_we_o <= 1'bx;
		bus_addr_bo <= 32'h0;
		bus_wdata_bo <= 32'h0;
		end
	else
		begin
		
		if (rx_sync == 1'b1)
			begin
			state <= IDLE;
			tx_start_o <= 1'b0;
		
			bus_enb_o <= 1'b0;
			bus_we_o <= 1'bx;
			bus_addr_bo <= 32'h0;
			bus_wdata_bo <= 32'h0;
			tr_length <= 32'h0;
			end

		else 
			begin

			tx_start_o <= 1'b0;
			
			case (state)
			
				IDLE:
					begin
					if (rx_req)
						begin
						case (r_data)
						
							IDCODE_CMD:
								begin
								tx_start_o <= 1'b1;
								tx_dout_bo <= SYNC_BYTE;
								end
								
							RST_CMD:
								begin
								rst_o <= 1'b1;
								end
							
							nRST_CMD:
								begin
								rst_o <= 1'b0;
								end
							
							WR_INC_CMD:
								begin
								cmd_ff <= 1'b1;
								autoinc_ff <= 1'b1;
								state <= FETCH_ADDR;
								counter <= 2'b00;
								end
								
							RD_INC_CMD:
								begin
								cmd_ff <= 1'b0;
								autoinc_ff <= 1'b1;
								state <= FETCH_ADDR;
								counter <= 2'b00;
								end
							
							WR_NOINC_CMD:
								begin
								cmd_ff <= 1'b1;
								autoinc_ff <= 1'b0;
								state <= FETCH_ADDR;
								counter <= 2'b00;	
								end

							RD_NOINC_CMD:
								begin
								cmd_ff <= 1'b0;
								autoinc_ff <= 1'b0;
								state <= FETCH_ADDR;
								counter <= 2'b00;
								end

							default:
								state <= IDLE;
						
						endcase
						end
					end
			
				FETCH_ADDR:
					begin
					if (rx_req)
						begin
						bus_addr_bo <= {r_data, bus_addr_bo[31:8]};
						if (counter == 2'b11)
							begin
							state <= FETCH_LENGTH;
							counter <= 2'b00;
							end
						else
							begin
							counter <= counter + 2'b01;
							end
						end
					end
			
				FETCH_LENGTH:
					begin
					if (rx_req)
						begin
						tr_length <= {r_data, tr_length[31:8]};
						if (counter == 2'b11)
							begin
							if (cmd_ff == 1'b1)
								begin
								state <= FETCH_DATA;
								counter <= 2'b00;
								end							
							else
								begin
								bus_enb_o <= 1'b1;
								bus_we_o <= 1'b0;
								bus_wdata_bo <= 32'h0;
								state <= WAIT_ACCEPT;
								counter <= 2'b00;
								end
							end
						else
							begin
							counter <= counter + 2'b01;
							end
						end
					end

				FETCH_DATA:
					begin
					if (rx_req)
						begin
						bus_wdata_bo <= {r_data, bus_wdata_bo[31:8]};
						if (counter == 2'b11)
							begin
							bus_enb_o <= 1'b1;
							bus_we_o <= 1'b1;
							state <= WAIT_ACCEPT;
							end
						else
							begin
							counter <= counter + 2'b01;
							end
						end
					end
				
				WAIT_ACCEPT:
					begin
					if (bus_ack_i == 1'b1)
						begin
						bus_enb_o <= 1'b0;
						bus_we_o <= 1'bx;
						bus_wdata_bo <= 32'h0;
						RD_DATA_reg <= bus_rdata_bi;
						if (cmd_ff == 1'b0)
							begin
							state <= TX_DATA;
							end
						else
							begin
							if (tr_length == 32'h4) state <= IDLE;
							else
								begin
								if (autoinc_ff == 1'b1) bus_addr_bo <= bus_addr_bo + 32'h4;
								state <= FETCH_DATA;
								counter <= 2'b00;
								end
							tr_length <= tr_length - 32'h4;
							end
						end
					end
				
				TX_DATA:
					begin
					tx_start_o <= 1'b1;
					tx_dout_bo <= RD_DATA_reg[7:0];
					RD_DATA_reg <= {8'h0, RD_DATA_reg[31:8]};
					counter <= 2'b00;
					state <= WAIT_TX;
					end

				WAIT_TX:
					begin
					if (tx_done_tick_i == 1'b1)
						begin
						if (counter == 2'b11)
							begin
							if (tr_length == 32'h4) state <= IDLE;
							else 
								begin
								if (autoinc_ff == 1'b1) bus_addr_bo <= bus_addr_bo + 32'h4;
								bus_enb_o <= 1'b1;
								bus_we_o <= 1'b0;
								bus_wdata_bo <= 32'h0;
								state <= WAIT_ACCEPT;
								counter <= 2'b00;
								end
							tr_length <= tr_length - 32'h4;
							end
						else 
							begin
							tx_start_o <= 1'b1;
							tx_dout_bo <= RD_DATA_reg[7:0];
							RD_DATA_reg <= {8'h0, RD_DATA_reg[31:8]};
							end
						counter <= counter + 2'b01;
						end
					end

				default:
					begin
					state <= IDLE;
					end
				
			endcase
			end
		end
	end

endmodule
