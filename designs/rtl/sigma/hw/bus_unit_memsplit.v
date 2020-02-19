/*
 * bus_unit_memsplit.v
 *
 *  Created on: 24.09.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module bus_unit_memsplit
#(
    parameter mem_init="YES", mem_data="data.hex", mem_size=1024
)
(
	
	input [0:0] clk_i,
	input [0:0] rst_i,
	
	input [0:0] bus0_req_i,
	input [0:0] bus0_we_i,
	input [31:0] bus0_addr_bi,
	input [3:0] bus0_be_bi,
	input [31:0] bus0_wdata_bi,
	output reg [0:0] bus0_ack_o,
	
	output reg [0:0] bus0_resp_o,
	output reg [31:0] bus0_rdata_bo,
	
	input [0:0] bus1_req_i,
	input [0:0] bus1_we_i,
	input [31:0] bus1_addr_bi,
	input [3:0] bus1_be_bi,
	input [31:0] bus1_wdata_bi,
	output reg [0:0] bus1_ack_o,
	
	output reg [0:0] bus1_resp_o,
	output reg [31:0] bus1_rdata_bo,
	
	input [31:0] gpio_bi,
	output [31:0] gpio_bo
);

wire bus0_io_req, bus1_io_req;
assign bus0_io_req = bus0_req_i & bus0_addr_bi[31];
assign bus1_io_req = bus1_req_i & bus1_addr_bi[31];

wire bus0_ram_req, bus1_ram_req;
assign bus0_ram_req = bus0_req_i & !bus0_addr_bi[31];
assign bus1_ram_req = bus1_req_i & !bus1_addr_bi[31];

reg [31:0] gpio_bo_reg;
assign gpio_bo = gpio_bo_reg;

reg [31:0] gpio_bi_reg;
always @(posedge clk_i) gpio_bi_reg <= gpio_bi;

wire [0:0] ram0_ack;
wire [0:0] ram0_resp;
wire [31:0] ram0_rdata;

wire [0:0] ram1_ack;
wire [0:0] ram1_resp;
wire [31:0] ram1_rdata;

always @(posedge clk_i)
	begin
	if (rst_i) gpio_bo_reg <= 32'h0;
	else
		begin
		if ((bus0_io_req == 1'b1) && (bus0_we_i == 1'b1) && (bus0_addr_bi[7:0] == 8'h0)) gpio_bo_reg <= bus0_wdata_bi;
		if ((bus1_io_req == 1'b1) && (bus1_we_i == 1'b1) && (bus1_addr_bi[7:0] == 8'h0)) gpio_bo_reg <= bus1_wdata_bi;
		end
	end

reg bus0_io_rd_inprogress, bus1_io_rd_inprogress;
reg [31:0] bus0_io_rdbuf, bus1_io_rdbuf;
always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		bus0_io_rd_inprogress <= 1'b0;
		bus1_io_rd_inprogress <= 1'b0;
		end
	else
		begin
		if ((bus0_io_req == 1'b1) && (bus0_we_i == 1'b0) && (bus0_ack_o == 1'b1))
			begin
			bus0_io_rd_inprogress <= 1'b1;
			if (bus0_addr_bi[7:0] == 8'h0) bus0_io_rdbuf <= gpio_bo_reg;
			else if (bus0_addr_bi[7:0] == 8'h4) bus0_io_rdbuf <= gpio_bi_reg;
			else bus0_io_rdbuf <= 32'h55aa55aa;
			end
		else if ((bus0_io_rd_inprogress == 1'b1) && (bus0_resp_o == 1'b1)) bus0_io_rd_inprogress <= 1'b0;
		
		if ((bus1_io_req == 1'b1) && (bus1_we_i == 1'b0) && (bus1_ack_o == 1'b1))
			begin
			bus1_io_rd_inprogress <= 1'b1;
			if (bus1_addr_bi[7:0] == 8'h0) bus1_io_rdbuf <= gpio_bo_reg;
			else if (bus1_addr_bi[7:0] == 8'h4) bus1_io_rdbuf <= gpio_bi_reg;
			else bus1_io_rdbuf <= 32'h55aa55aa;
			end
		else if ((bus1_io_rd_inprogress == 1'b1) && (bus1_resp_o == 1'b1)) bus1_io_rd_inprogress <= 1'b0;
		end
	end

always @*
	begin
	if (bus0_io_rd_inprogress)
		begin
		bus0_resp_o = 1'b1;
		bus0_rdata_bo = bus0_io_rdbuf;
		end
	else
		begin
		bus0_resp_o = ram0_resp;
		bus0_rdata_bo = ram0_rdata;
		end
	end

always @*
	begin
	if (bus1_io_rd_inprogress)
		begin
		bus1_resp_o = 1'b1;
		bus1_rdata_bo = bus1_io_rdbuf;
		end
	else
		begin
		bus1_resp_o = ram1_resp;
		bus1_rdata_bo = ram1_rdata;
		end
	end

always @*
	begin
	bus0_ack_o = 1'b0;
	if (bus0_ram_req) bus0_ack_o = ram0_ack;
	if (bus0_io_req) bus0_ack_o = 1'b1;
	end

always @*
	begin
	bus1_ack_o = 1'b0;
	if (bus1_ram_req) bus1_ack_o = ram1_ack;
	if (bus1_io_req) bus1_ack_o = 1'b1;
	end
	
ram_dual_memsplit
#(
	.mem_init(mem_init),
	.mem_data(mem_data),
	.dat_width(32),
	.adr_width(30),
	.mem_size(mem_size)
) ram_dual_memsplit (
	.clk_i(clk_i)
	, .rst_i(rst_i)
	
	, .bus0_req_i(bus0_ram_req)
	, .bus0_we_i(bus0_we_i)
	, .bus0_addr_bi(bus0_addr_bi[31:2])
	, .bus0_be_bi(bus0_be_bi)
	, .bus0_wdata_bi(bus0_wdata_bi)
	, .bus0_ack_o(ram0_ack)
	, .bus0_resp_o(ram0_resp)
	, .bus0_rdata_bo(ram0_rdata)
	
	, .bus1_req_i(bus1_ram_req)
	, .bus1_we_i(bus1_we_i)
	, .bus1_addr_bi(bus1_addr_bi[31:2])
	, .bus1_be_bi(bus1_be_bi)
	, .bus1_wdata_bi(bus1_wdata_bi)
	, .bus1_ack_o(ram1_ack)
	, .bus1_resp_o(ram1_resp)
	, .bus1_rdata_bo(ram1_rdata)
);





endmodule
