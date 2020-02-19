/*
 * sfr.v
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module sfr
#(
	parameter corenum=0
)
(
	input [0:0] clk_i
	, input [0:0] rst_i
	
	, input  [0:0] 	bus_req
	, input  [0:0] 	bus_we
	, input  [31:0] bus_addr
	, input  [3:0] 	bus_be
	, input  [31:0] bus_wdata
	, output [0:0] 	bus_ack
	, output reg [0:0] 	bus_resp
	, output [31:0]	bus_rdata
);

always @(posedge clk_i)
	begin
	if (rst_i) bus_resp <= 1'b0;
	else bus_resp <= bus_req & !bus_we;
	end

assign bus_ack = bus_req;
assign bus_rdata = corenum;

endmodule
