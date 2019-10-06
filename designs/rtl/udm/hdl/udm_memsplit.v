/*
 * udm_memsplit.v
 *
 *  Created on: 17.04.2016
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module udm_memsplit
(
	input clk_i, rst_i,

	input rx_i,
	output tx_o,

	output rst_o,
	
	output reg [0:0] bus_req_o,
	output [0:0] bus_we_o,
	output [31:0] bus_addr_bo,
	output [3:0] bus_be_bo,
	output [31:0] bus_wdata_bo,
	input [0:0] bus_ack_i,
	
	input [0:0] bus_resp_i,
	input [31:0] bus_rdata_bi
);

assign bus_be_bo = 4'hf;
wire bus_req_internal;
reg bus_ack_internal;

reg rd_inprogress_reg;
always @(posedge clk_i)
	begin
	if (rst_i) rd_inprogress_reg <= 1'b0;
	else
		begin
		if ((rd_inprogress_reg == 1'b0) && (bus_req_internal == 1'b1) && (bus_we_o == 1'b0)) rd_inprogress_reg <= 1'b1;
		else if (bus_resp_i == 1'b1) rd_inprogress_reg <= 1'b0;
		end
	end

reg rd_inprogress;
always @*
	begin
	if ((bus_req_internal == 1'b1) && (bus_we_o == 1'b0)) rd_inprogress = 1'b1;
	else rd_inprogress = rd_inprogress_reg;
	end

reg rd_ack_received;
always @(posedge clk_i)
	begin
	if (rst_i) rd_ack_received <= 1'b0;
	else
		begin
		if (rd_inprogress)
			begin
			if (bus_ack_i) rd_ack_received <= 1'b1;
			if (bus_resp_i) rd_ack_received <= 1'b0;
			end
		else rd_ack_received <= 1'b0;
		end
	end
	
always @*
	begin
	bus_req_o = 1'b0;
	bus_ack_internal = 1'b0;
	if (bus_req_internal)
		begin
		if (rd_inprogress)
			begin
			bus_req_o = bus_req_internal & (!rd_ack_received);
			bus_ack_internal = bus_resp_i;
			end
		else
			begin
			bus_req_o = bus_req_internal;
			bus_ack_internal = bus_ack_i;
			end
		end
	end
	
udm udm
(
	.clk_i(clk_i)
	, .rst_i(rst_i)

	, .rx_i(rx_i)
	, .tx_o(tx_o)

	, .rst_o(rst_o)
	
	, .bus_enb_o(bus_req_internal)
	, .bus_we_o(bus_we_o)
	, .bus_addr_bo(bus_addr_bo)
    , .bus_wdata_bo(bus_wdata_bo)

    , .bus_ack_i(bus_ack_internal)
    , .bus_rdata_bi(bus_rdata_bi)
);

endmodule
