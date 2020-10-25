/*
 * magma.sv
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

module magma
#(
	parameter CPU = "none",
	parameter mem_init="YES",
	parameter mem_type="elf",
	parameter mem_data = "data.hex",
	parameter mem_size = 1024
)
(
	input clk_i
	, input arst_i

	, input rx_i
	, output tx_o

	, input irq0_btn_i
	, input irq1_btn_i
	, input irq2_btn_i
	, input irq3_btn_i

	, input [15:0] gpio_bi
	, output [15:0] gpio_bo
);

	logic srst;
	reset_sync reset_sync
	(
		.clk_i(clk_i),
		.arst_i(arst_i),
		.srst_o(srst)
	);

	logic udm_reset;
	logic cpu_reset;
	assign cpu_reset = srst | udm_reset;
	
	MemSplit32 m0();
	MemSplit32 m1();
	MemSplit32 m2();
	MemSplit32 m3();
	MemSplit32 m4();

	MemSplit32 s0();
	MemSplit32 s1();
	MemSplit32 s2();
	MemSplit32 s3();
	MemSplit32 s4();
	
	ariele_xbar xbar
	(
		.clk_i(clk_i)
		, .rst_i(srst)
		
		, .m0_req_i	   (m0.req)
		, .m0_we_i	   (m0.we)
		, .m0_addr_bi  (m0.addr)
		, .m0_be_i	   (m0.be)
		, .m0_wdata_bi (m0.wdata)
		, .m0_ack_o	   (m0.ack)
		, .m0_resp_o   (m0.resp)
		, .m0_rdata_bo (m0.rdata)
		
		, .m1_req_i		(m1.req)
		, .m1_we_i		(m1.we)
		, .m1_addr_bi	(m1.addr)
		, .m1_be_i		(m1.be)
		, .m1_wdata_bi	(m1.wdata)
		, .m1_ack_o		(m1.ack)
		, .m1_resp_o	(m1.resp)
		, .m1_rdata_bo	(m1.rdata)
		
		, .m2_req_i		(m2.req)
		, .m2_we_i		(m2.we)
		, .m2_addr_bi	(m2.addr)
		, .m2_be_i		(m2.be)
		, .m2_wdata_bi	(m2.wdata)
		, .m2_ack_o		(m2.ack)
		, .m2_resp_o	(m2.resp)
		, .m2_rdata_bo	(m2.rdata)
		
		, .m3_req_i		(m3.req)
		, .m3_we_i		(m3.we)
		, .m3_addr_bi	(m3.addr)
		, .m3_be_i		(m3.be)
		, .m3_wdata_bi	(m3.wdata)
		, .m3_ack_o		(m3.ack)
		, .m3_resp_o	(m3.resp)
		, .m3_rdata_bo	(m3.rdata)
		
		, .m4_req_i		(m4.req)
		, .m4_we_i		(m4.we)
		, .m4_addr_bi	(m4.addr)
		, .m4_be_i		(m4.be)
		, .m4_wdata_bi	(m4.wdata)
		, .m4_ack_o		(m4.ack)
		, .m4_resp_o	(m4.resp)
		, .m4_rdata_bo	(m4.rdata)
		
		, .s0_req_o		(s0.req)
		, .s0_we_o		(s0.we)
		, .s0_addr_bo	(s0.addr)
		, .s0_be_o		(s0.be)
		, .s0_wdata_bo	(s0.wdata)
		, .s0_ack_i		(s0.ack)
		, .s0_resp_i	(s0.resp)
		, .s0_rdata_bi	(s0.rdata)
		
		, .s1_req_o		(s1.req)
		, .s1_we_o		(s1.we)
		, .s1_addr_bo	(s1.addr)
		, .s1_be_o		(s1.be)
		, .s1_wdata_bo	(s1.wdata)
		, .s1_ack_i		(s1.ack)
		, .s1_resp_i	(s1.resp)
		, .s1_rdata_bi	(s1.rdata)
		
		, .s2_req_o		(s2.req)
		, .s2_we_o		(s2.we)
		, .s2_addr_bo	(s2.addr)
		, .s2_be_o		(s2.be)
		, .s2_wdata_bo	(s2.wdata)
		, .s2_ack_i		(s2.ack)
		, .s2_resp_i	(s2.resp)
		, .s2_rdata_bi	(s2.rdata)
		
		, .s3_req_o		(s3.req)
		, .s3_we_o		(s3.we)
		, .s3_addr_bo	(s3.addr)
		, .s3_be_o		(s3.be)
		, .s3_wdata_bo	(s3.wdata)
		, .s3_ack_i		(s3.ack)
		, .s3_resp_i	(s3.resp)
		, .s3_rdata_bi	(s3.rdata)
		
		, .s4_req_o		(s4.req)
		, .s4_we_o		(s4.we)
		, .s4_addr_bo	(s4.addr)
		, .s4_be_o		(s4.be)
		, .s4_wdata_bo	(s4.wdata)
		, .s4_ack_i		(s4.ack)
		, .s4_resp_i	(s4.resp)
		, .s4_rdata_bi	(s4.rdata)
	);
	
	logic irq0_debounced;
	debouncer debouncer0
	(
		.clk_i(clk_i)
		, .rst_i(srst)
		, .in(irq0_btn_i)
		, .out(irq0_debounced)
	);

	logic irq1_debounced;
	debouncer debouncer1
	(
		.clk_i(clk_i)
		, .rst_i(srst)
		, .in(irq1_btn_i)
		, .out(irq1_debounced)
	);

	logic irq2_debounced;
	debouncer debouncer2
	(
		.clk_i(clk_i)
		, .rst_i(srst)
		, .in(irq2_btn_i)
		, .out(irq2_debounced)
	);

	logic irq3_debounced;
	debouncer debouncer3
	(
		.clk_i(clk_i)
		, .rst_i(srst)
		, .in(irq3_btn_i)
		, .out(irq3_debounced)
	);

	sigma_tile #(
		.corenum(0)
		, .mem_init(mem_init)
		, .mem_type(mem_type)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
		, .CPU(CPU)
		, .PATH_THROUGH("NO")
		, .SW_RESET_DEFAULT(0)
	) tile0 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .irq_debounced_bi(irq0_debounced)

		, .hif(s0)
		, .xif(m0)
	);
	
	sigma_tile #(
		.corenum(1)
		, .mem_init(mem_init)
		, .mem_type(mem_type)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
		, .CPU(CPU)
		, .PATH_THROUGH("NO")
		, .SW_RESET_DEFAULT(1)
	) tile1 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .irq_debounced_bi(irq1_debounced)

		, .hif(s1)
		, .xif(m1)
	);
	
	sigma_tile #(
		.corenum(2)
		, .mem_init(mem_init)
		, .mem_type(mem_type)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
		, .CPU(CPU)
		, .PATH_THROUGH("NO")
		, .SW_RESET_DEFAULT(1)
	) tile2 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .irq_debounced_bi(irq2_debounced)

		, .hif(s2)
		, .xif(m2)
	);
	
	sigma_tile #(
		.corenum(3)
		, .mem_init(mem_init)
		, .mem_type(mem_type)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
		, .CPU(CPU)
		, .PATH_THROUGH("NO")
		, .SW_RESET_DEFAULT(1)
	) tile3 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .irq_debounced_bi(irq3_debounced)
		
		, .hif(s3)
		, .xif(m3)
	);
	
	udm udm
	(
		.clk_i(clk_i)
		, .rst_i(srst)

		, .rx_i(rx_i)
		, .tx_o(tx_o)

		, .rst_o(udm_reset)
		
		, .bus_req_o	(m4.req)
		, .bus_we_o		(m4.we)
		, .bus_addr_bo	(m4.addr)
		, .bus_be_bo	(m4.be)
		, .bus_wdata_bo	(m4.wdata)
		, .bus_ack_i	(m4.ack)
		, .bus_resp_i	(m4.resp)
		, .bus_rdata_bi	(m4.rdata)
	);
	
	gpio gpio
	(
		.clk_i(clk_i)
		, .rst_i(srst)
		
		, .bus_req	(s4.req)
		, .bus_we	(s4.we)
		, .bus_addr	(s4.addr)
		, .bus_be	(s4.be)
		, .bus_wdata(s4.wdata)
		, .bus_ack	(s4.ack)
		, .bus_resp	(s4.resp)
		, .bus_rdata(s4.rdata)

		, .gpio_bi(gpio_bi)
		, .gpio_bo(gpio_bo)
	);



endmodule
