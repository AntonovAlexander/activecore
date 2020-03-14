/*
 * sfr.sv
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

module sfr
#(
	parameter corenum=0
)
(
	input [0:0] clk_i
	, input [0:0] rst_i
	
	, MemSplit32.Slave host
);

always @(posedge clk_i)
	begin
	if (rst_i) host.resp <= 1'b0;
	else host.resp <= host.req & !host.we;
	end

assign host.ack = host.req;
assign host.rdata = corenum;

endmodule
