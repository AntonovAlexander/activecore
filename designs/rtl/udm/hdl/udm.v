/*
 PSS

 Copyright (c) 2016 Alexander Antonov <153287@niuitmo.ru>
 All rights reserved.

 Version 0.99

 The FreeBSD license
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above
    copyright notice, this list of conditions and the following
    disclaimer in the documentation and/or other materials
    provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 PSS PROJECT OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/


module udm
(
	input clk_i, rst_i,

	input rx_i,
	output tx_o,

	output rst_o,
	output bus_enb_o,
	output bus_we_o,
	output [31:0] bus_addr_bo,
    output [31:0] bus_wdata_bo,

    input bus_ack_i,
    input [31:0] bus_rdata_bi
);

wire rx_done_tick;
wire [7:0] rx_data;
wire [7:0] tx_data;
wire tx_start;
wire tx_done_tick;

wire locked;
wire [28:0] bitperiod;

uart_rx uart_rx
(
    .clk_i(clk_i),
    .rst_i(rst_i),

    .rx_i(rx_i),
    .rx_done_tick_o(rx_done_tick),
    .dout_bo(rx_data),

    .locked_o(locked),
    .bitperiod_o(bitperiod)
);

uart_tx uart_tx
(
    .clk_i(clk_i),
    .rst_i(rst_i),

    .tx_start_i(tx_start),
    .din_bi(tx_data),

    .locked_i(locked),
    .bitperiod_i(bitperiod),

    .tx_done_tick_o(tx_done_tick),
    .tx_o(tx_o)
);


udm_controller udm_controller
(
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
	.bus_enb_o(bus_enb_o),
	.bus_we_o(bus_we_o),
	.bus_addr_bo(bus_addr_bo),
    .bus_wdata_bo(bus_wdata_bo),

    .bus_ack_i(bus_ack_i),
    .bus_rdata_bi(bus_rdata_bi)
);

endmodule
