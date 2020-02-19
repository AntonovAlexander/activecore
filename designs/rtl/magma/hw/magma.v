/*
 * magma.v
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module magma
#(
	parameter CPU = "none",
	parameter mem_init="YES",
	parameter mem_data = "data.hex",
	parameter mem_size = 1024
)
(
	input clk_i
	, input arst_i
	, input rx_i
	, output tx_o
	, input [31:0] gpio_bi
	, output [31:0] gpio_bo
);

	wire srst;
	reset_cntrl reset_cntrl
	(
		.clk_i(clk_i),
		.arst_i(arst_i),
		.srst_o(srst)
	);

	wire udm_reset;
	wire cpu_reset;
	assign cpu_reset = srst | udm_reset;
	
	wire [0:0] 	m0_req;
	wire [0:0] 	m0_we;
	wire [31:0] m0_addr;
	wire [3:0] 	m0_be;
	wire [31:0] m0_wdata;
	wire [0:0] 	m0_ack;
	wire [0:0] 	m0_resp;
	wire [31:0] m0_rdata;
	
	wire [0:0] 	m1_req;
	wire [0:0] 	m1_we;
	wire [31:0] m1_addr;
	wire [3:0] 	m1_be;
	wire [31:0] m1_wdata;
	wire [0:0] 	m1_ack;
	wire [0:0] 	m1_resp;
	wire [31:0] m1_rdata;
	
	wire [0:0] 	m2_req;
	wire [0:0] 	m2_we;
	wire [31:0] m2_addr;
	wire [3:0] 	m2_be;
	wire [31:0] m2_wdata;
	wire [0:0] 	m2_ack;
	wire [0:0] 	m2_resp;
	wire [31:0] m2_rdata;
	
	wire [0:0] 	m3_req;
	wire [0:0] 	m3_we;
	wire [31:0] m3_addr;
	wire [3:0] 	m3_be;
	wire [31:0] m3_wdata;
	wire [0:0] 	m3_ack;
	wire [0:0] 	m3_resp;
	wire [31:0] m3_rdata;
	
	wire [0:0] 	m4_req;
	wire [0:0] 	m4_we;
	wire [31:0] m4_addr;
	wire [3:0] 	m4_be;
	wire [31:0] m4_wdata;
	wire [0:0] 	m4_ack;
	wire [0:0] 	m4_resp;
	wire [31:0] m4_rdata;
	
	wire [0:0] 	s0_req;
	wire [0:0] 	s0_we;
	wire [31:0] s0_addr;
	wire [3:0] 	s0_be;
	wire [31:0] s0_wdata;
	wire [0:0] 	s0_ack;
	wire [0:0] 	s0_resp;
	wire [31:0] s0_rdata;
	
	wire [0:0] 	s1_req;
	wire [0:0] 	s1_we;
	wire [31:0] s1_addr;
	wire [3:0] 	s1_be;
	wire [31:0] s1_wdata;
	wire [0:0] 	s1_ack;
	wire [0:0] 	s1_resp;
	wire [31:0] s1_rdata;
	
	wire [0:0] 	s2_req;
	wire [0:0] 	s2_we;
	wire [31:0] s2_addr;
	wire [3:0] 	s2_be;
	wire [31:0] s2_wdata;
	wire [0:0] 	s2_ack;
	wire [0:0] 	s2_resp;
	wire [31:0] s2_rdata;
	
	wire [0:0] 	s3_req;
	wire [0:0] 	s3_we;
	wire [31:0] s3_addr;
	wire [3:0] 	s3_be;
	wire [31:0] s3_wdata;
	wire [0:0] 	s3_ack;
	wire [0:0] 	s3_resp;
	wire [31:0] s3_rdata;
	
	wire [0:0] 	s4_req;
	wire [0:0] 	s4_we;
	wire [31:0] s4_addr;
	wire [3:0] 	s4_be;
	wire [31:0] s4_wdata;
	wire [0:0] 	s4_ack;
	wire [0:0] 	s4_resp;
	wire [31:0] s4_rdata;
	
	ariele_xbar xbar
	(
		.clk_i(clk_i)
		, .rst_i(srst)
		
		, .m0_req_i	   (m0_req)
		, .m0_we_i	   (m0_we)
		, .m0_addr_bi  (m0_addr)
		, .m0_be_i	   (m0_be)
		, .m0_wdata_bi (m0_wdata)
		, .m0_ack_o	   (m0_ack)
		, .m0_resp_o   (m0_resp)
		, .m0_rdata_bo (m0_rdata)
		
		, .m1_req_i	(m1_req)
		, .m1_we_i	(m1_we)
		, .m1_addr_bi	(m1_addr)
		, .m1_be_i	(m1_be)
		, .m1_wdata_bi	(m1_wdata)
		, .m1_ack_o	(m1_ack)
		, .m1_resp_o	(m1_resp)
		, .m1_rdata_bo	(m1_rdata)
		
		, .m2_req_i	(m2_req)
		, .m2_we_i	(m2_we)
		, .m2_addr_bi	(m2_addr)
		, .m2_be_i	(m2_be)
		, .m2_wdata_bi	(m2_wdata)
		, .m2_ack_o	(m2_ack)
		, .m2_resp_o	(m2_resp)
		, .m2_rdata_bo	(m2_rdata)
		
		, .m3_req_i	(m3_req)
		, .m3_we_i	(m3_we)
		, .m3_addr_bi	(m3_addr)
		, .m3_be_i	(m3_be)
		, .m3_wdata_bi	(m3_wdata)
		, .m3_ack_o	(m3_ack)
		, .m3_resp_o	(m3_resp)
		, .m3_rdata_bo	(m3_rdata)
		
		, .m4_req_i	(m4_req)
		, .m4_we_i	(m4_we)
		, .m4_addr_bi	(m4_addr)
		, .m4_be_i	(m4_be)
		, .m4_wdata_bi	(m4_wdata)
		, .m4_ack_o	(m4_ack)
		, .m4_resp_o	(m4_resp)
		, .m4_rdata_bo	(m4_rdata)
		
		, .s0_req_o	(s0_req)
		, .s0_we_o	(s0_we)
		, .s0_addr_bo	(s0_addr)
		, .s0_be_o	(s0_be)
		, .s0_wdata_bo	(s0_wdata)
		, .s0_ack_i	(s0_ack)
		, .s0_resp_i	(s0_resp)
		, .s0_rdata_bi	(s0_rdata)
		
		, .s1_req_o	(s1_req)
		, .s1_we_o	(s1_we)
		, .s1_addr_bo	(s1_addr)
		, .s1_be_o	(s1_be)
		, .s1_wdata_bo	(s1_wdata)
		, .s1_ack_i	(s1_ack)
		, .s1_resp_i	(s1_resp)
		, .s1_rdata_bi	(s1_rdata)
		
		, .s2_req_o	(s2_req)
		, .s2_we_o	(s2_we)
		, .s2_addr_bo	(s2_addr)
		, .s2_be_o	(s2_be)
		, .s2_wdata_bo	(s2_wdata)
		, .s2_ack_i	(s2_ack)
		, .s2_resp_i	(s2_resp)
		, .s2_rdata_bi	(s2_rdata)
		
		, .s3_req_o	(s3_req)
		, .s3_we_o	(s3_we)
		, .s3_addr_bo	(s3_addr)
		, .s3_be_o	(s3_be)
		, .s3_wdata_bo	(s3_wdata)
		, .s3_ack_i	(s3_ack)
		, .s3_resp_i	(s3_resp)
		, .s3_rdata_bi	(s3_rdata)
		
		, .s4_req_o	(s4_req)
		, .s4_we_o	(s4_we)
		, .s4_addr_bo	(s4_addr)
		, .s4_be_o	(s4_be)
		, .s4_wdata_bo	(s4_wdata)
		, .s4_ack_i	(s4_ack)
		, .s4_resp_i	(s4_resp)
		, .s4_rdata_bi	(s4_rdata)
	);
	
	magma_tile #(
		.corenum(0)
		, .mem_init(mem_init)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
		, .CPU(CPU)
	) tile0 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .hpi_mem_req		(s0_req)
		, .hpi_mem_we		(s0_we)
		, .hpi_mem_addr		(s0_addr)
		, .hpi_mem_be		(s0_be)
		, .hpi_mem_wdata	(s0_wdata)
		, .hpi_mem_ack		(s0_ack)
		, .hpi_mem_resp		(s0_resp)
		, .hpi_mem_rdata	(s0_rdata)
		
		, .xbus_mem_req		(m0_req)
		, .xbus_mem_we		(m0_we)
		, .xbus_mem_addr	(m0_addr)
		, .xbus_mem_be		(m0_be)
		, .xbus_mem_wdata	(m0_wdata)
		, .xbus_mem_ack		(m0_ack)
		, .xbus_mem_resp	(m0_resp)
		, .xbus_mem_rdata	(m0_rdata)
	);
	
	magma_tile #(
		.corenum(1)
		, .mem_init(mem_init)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
		, .CPU(CPU)
	) tile1 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .hpi_mem_req		(s1_req)
		, .hpi_mem_we		(s1_we)
		, .hpi_mem_addr		(s1_addr)
		, .hpi_mem_be		(s1_be)
		, .hpi_mem_wdata	(s1_wdata)
		, .hpi_mem_ack		(s1_ack)
		, .hpi_mem_resp		(s1_resp)
		, .hpi_mem_rdata	(s1_rdata)
		
		, .xbus_mem_req		(m1_req)
		, .xbus_mem_we		(m1_we)
		, .xbus_mem_addr	(m1_addr)
		, .xbus_mem_be		(m1_be)
		, .xbus_mem_wdata	(m1_wdata)
		, .xbus_mem_ack		(m1_ack)
		, .xbus_mem_resp	(m1_resp)
		, .xbus_mem_rdata	(m1_rdata)
	);
	
	magma_tile #(
		.corenum(2)
		, .mem_init(mem_init)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
		, .CPU(CPU)
	) tile2 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .hpi_mem_req		(s2_req)
		, .hpi_mem_we		(s2_we)
		, .hpi_mem_addr		(s2_addr)
		, .hpi_mem_be		(s2_be)
		, .hpi_mem_wdata	(s2_wdata)
		, .hpi_mem_ack		(s2_ack)
		, .hpi_mem_resp		(s2_resp)
		, .hpi_mem_rdata	(s2_rdata)
		
		, .xbus_mem_req		(m2_req)
		, .xbus_mem_we		(m2_we)
		, .xbus_mem_addr	(m2_addr)
		, .xbus_mem_be		(m2_be)
		, .xbus_mem_wdata	(m2_wdata)
		, .xbus_mem_ack		(m2_ack)
		, .xbus_mem_resp	(m2_resp)
		, .xbus_mem_rdata	(m2_rdata)
	);
	
	magma_tile #(
		.corenum(3)
		, .mem_init(mem_init)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
		, .CPU(CPU)
	) tile3 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .hpi_mem_req		(s3_req)
		, .hpi_mem_we		(s3_we)
		, .hpi_mem_addr		(s3_addr)
		, .hpi_mem_be		(s3_be)
		, .hpi_mem_wdata	(s3_wdata)
		, .hpi_mem_ack		(s3_ack)
		, .hpi_mem_resp		(s3_resp)
		, .hpi_mem_rdata	(s3_rdata)
		
		, .xbus_mem_req		(m3_req)
		, .xbus_mem_we		(m3_we)
		, .xbus_mem_addr	(m3_addr)
		, .xbus_mem_be		(m3_be)
		, .xbus_mem_wdata	(m3_wdata)
		, .xbus_mem_ack		(m3_ack)
		, .xbus_mem_resp	(m3_resp)
		, .xbus_mem_rdata	(m3_rdata)
	);
	
	udm udm
	(
		.clk_i(clk_i)
		, .rst_i(srst)

		, .rx_i(rx_i)
		, .tx_o(tx_o)

		, .rst_o(udm_reset)
		
		, .bus_req_o	(m4_req)
		, .bus_we_o		(m4_we)
		, .bus_addr_bo	(m4_addr)
		, .bus_be_bo	(m4_be)
		, .bus_wdata_bo	(m4_wdata)
		, .bus_ack_i	(m4_ack)
		, .bus_resp_i	(m4_resp)
		, .bus_rdata_bi	(m4_rdata)
	);
	
	gpio gpio
	(
		.clk_i(clk_i)
		, .rst_i(srst)
		
		,. bus_req	(s4_req)
		, .bus_we	(s4_we)
		, .bus_addr	(s4_addr)
		, .bus_be	(s4_be)
		, .bus_wdata(s4_wdata)
		, .bus_ack	(s4_ack)
		, .bus_resp	(s4_resp)
		, .bus_rdata(s4_rdata)

		, .gpio_bi(gpio_bi)
		, .gpio_bo(gpio_bo)
	);



endmodule
