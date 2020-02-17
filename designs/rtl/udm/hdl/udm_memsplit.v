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
	
	output [0:0] bus_req_o,
	output [0:0] bus_we_o,
	output [31:0] bus_addr_bo,
	output [3:0] bus_be_bo,
	output [31:0] bus_wdata_bo,
	input [0:0] bus_ack_i,
	
	input [0:0] bus_resp_i,
	input [31:0] bus_rdata_bi
);

assign bus_be_bo = 4'hf;
	
udm udm
(
	.clk_i(clk_i)
	, .rst_i(rst_i)

	, .rx_i(rx_i)
	, .tx_o(tx_o)

	, .rst_o(rst_o)
	
	, .bus_req_o(bus_req_o)
	, .bus_ack_i(bus_ack_i)
	, .bus_we_o(bus_we_o)
	, .bus_addr_bo(bus_addr_bo)
    , .bus_wdata_bo(bus_wdata_bo)

    , .bus_resp_i(bus_resp_i)
    , .bus_rdata_bi(bus_rdata_bi)
);

endmodule
