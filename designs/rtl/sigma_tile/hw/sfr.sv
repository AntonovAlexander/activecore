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
	, parameter CPU_RESET_DEFAULT=0
	, parameter IRQ_NUM_POW = 4
)
(
	input [0:0] clk_i
	, input [0:0] rst_i

	, MemSplit32.Slave host

	, output logic cpu_reset_o

	, output logic msi_req_o
	, output logic [IRQ_NUM_POW-1:0] msi_code_bo
);

localparam IDCODE_ADDR 	= 8'h0;
localparam CTRL_ADDR 	= 8'h4;
localparam CORENUM_ADDR = 8'h8;
localparam MSI_ADDR 	= 8'hC;

logic cpu_reset;
always @(posedge clk_i) cpu_reset_o <= rst_i | cpu_reset;

always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		host.resp <= 1'b0;
		cpu_reset <= CPU_RESET_DEFAULT;
		msi_req_o <= 0;
		msi_code_bo <= 0;
		end
	else
		begin
		host.resp <= 1'b0;
		msi_req_o <= 0;
		if (host.req)
			begin
			if (host.we)
				begin
				if (host.addr[7:0] == CTRL_ADDR)
					begin
					cpu_reset <= host.wdata[0];
					end
				if (host.addr[7:0] == MSI_ADDR)
					begin
					msi_req_o <= 1;
					msi_code_bo <= host.wdata;
					end
				end
			else
				begin
				host.resp <= 1'b1;
				if (host.addr[7:0] == IDCODE_ADDR)  host.rdata <= 32'hdeadbeef;
				if (host.addr[7:0] == CTRL_ADDR)    host.rdata <= {31'h0, cpu_reset};
				if (host.addr[7:0] == CORENUM_ADDR) host.rdata <= corenum;
				end
			end
		end
	end

assign host.ack = host.req;

endmodule
