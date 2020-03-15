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

	, output reg msi_bo
	, MemSplit32.Slave host
);

localparam CORENUM_ADDR = 8'h0;
localparam MSI_ADDR 	= 8'h4;

always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		host.resp <= 1'b0;
		msi_bo <= 0;
		end
	else
		begin
		host.resp <= 1'b0;
		msi_bo <= 0;
		if (host.req)
			begin
			if (host.we)
				begin
				if (host.addr[7:0] == MSI_ADDR) msi_bo <= 1;
				end
			else
				begin
				host.resp <= 1'b1;
				if (host.addr[7:0] == CORENUM_ADDR) host.rdata <= corenum;
				end
			end
		end
	end

assign host.ack = host.req;

endmodule
