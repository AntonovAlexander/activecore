/*
 * udm.v
 *
 *  Created on: 17.04.2016
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module udm
#(
    parameter BUS_TIMEOUT=(1024*1024*100)
    , RTX_EXTERNAL_OVERRIDE = "NO"
)
(
    input clk_i, rst_i,

    input rx_i,
    output tx_o,

    output rst_o,
    output bus_req_o,
    input bus_ack_i,
    output bus_we_o,
    output [31:0] bus_addr_bo,
    output [3:0] bus_be_bo,
    output [31:0] bus_wdata_bo,

    input bus_resp_i,
    input [31:0] bus_rdata_bi
);

wire rx_done_tick;
wire [7:0] rx_data;
wire [7:0] tx_data;
wire tx_start;
wire tx_done_tick;

wire locked;
wire [28:0] bitperiod;

uart_rx
#(
    .RTX_EXTERNAL_OVERRIDE(RTX_EXTERNAL_OVERRIDE)
) uart_rx (
    .clk_i(clk_i),
    .rst_i(rst_i),

    .rx_i(rx_i),
    .rx_done_tick_o(rx_done_tick),
    .dout_bo(rx_data),

    .locked_o(locked),
    .bitperiod_o(bitperiod)
);

uart_tx
#(
    .RTX_EXTERNAL_OVERRIDE(RTX_EXTERNAL_OVERRIDE)
) uart_tx (
    .clk_i(clk_i),
    .rst_i(rst_i),

    .tx_start_i(tx_start),
    .din_bi(tx_data),

    .locked_i(locked),
    .bitperiod_i(bitperiod),

    .tx_done_tick_o(tx_done_tick),
    .tx_o(tx_o)
);


udm_controller
#(
    .BUS_TIMEOUT(BUS_TIMEOUT)
) udm_controller (
	.clk_i(clk_i),
	.reset_i(rst_i),

	// uart rx
	.rx_done_tick_i(rx_done_tick),
    .rx_din_bi(rx_data),

	// uart tx
	.tx_dout_bo(tx_data),
	.tx_start_o(tx_start),
    .tx_done_tick_i(tx_done_tick),
	
	// bus
	.rst_o(rst_o),
	.bus_req_o(bus_req_o),
	.bus_ack_i(bus_ack_i),
	.bus_we_o(bus_we_o),
	.bus_addr_bo(bus_addr_bo),
    .bus_be_bo(bus_be_bo),
    .bus_wdata_bo(bus_wdata_bo),

    .bus_resp_i(bus_resp_i),
    .bus_rdata_bi(bus_rdata_bi)
);

endmodule
