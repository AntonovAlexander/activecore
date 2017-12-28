module mpss
#(
	parameter CPU = "none",
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
	
	wire [0:0] 	m5_req;
	wire [0:0] 	m5_we;
	wire [31:0] m5_addr;
	wire [3:0] 	m5_be;
	wire [31:0] m5_wdata;
	wire [0:0] 	m5_ack;
	wire [0:0] 	m5_resp;
	wire [31:0] m5_rdata;
	
	wire [0:0] 	m6_req;
	wire [0:0] 	m6_we;
	wire [31:0] m6_addr;
	wire [3:0] 	m6_be;
	wire [31:0] m6_wdata;
	wire [0:0] 	m6_ack;
	wire [0:0] 	m6_resp;
	wire [31:0] m6_rdata;
	
	wire [0:0] 	m7_req;
	wire [0:0] 	m7_we;
	wire [31:0] m7_addr;
	wire [3:0] 	m7_be;
	wire [31:0] m7_wdata;
	wire [0:0] 	m7_ack;
	wire [0:0] 	m7_resp;
	wire [31:0] m7_rdata;
	
	wire [0:0] 	m8_req;
	wire [0:0] 	m8_we;
	wire [31:0] m8_addr;
	wire [3:0] 	m8_be;
	wire [31:0] m8_wdata;
	wire [0:0] 	m8_ack;
	wire [0:0] 	m8_resp;
	wire [31:0] m8_rdata;
	
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
	
	wire [0:0] 	s5_req;
	wire [0:0] 	s5_we;
	wire [31:0] s5_addr;
	wire [3:0] 	s5_be;
	wire [31:0] s5_wdata;
	wire [0:0] 	s5_ack;
	wire [0:0] 	s5_resp;
	wire [31:0] s5_rdata;
	
	wire [0:0] 	s6_req;
	wire [0:0] 	s6_we;
	wire [31:0] s6_addr;
	wire [3:0] 	s6_be;
	wire [31:0] s6_wdata;
	wire [0:0] 	s6_ack;
	wire [0:0] 	s6_resp;
	wire [31:0] s6_rdata;
	
	wire [0:0] 	s7_req;
	wire [0:0] 	s7_we;
	wire [31:0] s7_addr;
	wire [3:0] 	s7_be;
	wire [31:0] s7_wdata;
	wire [0:0] 	s7_ack;
	wire [0:0] 	s7_resp;
	wire [31:0] s7_rdata;
	
	wire [0:0] 	s8_req;
	wire [0:0] 	s8_we;
	wire [31:0] s8_addr;
	wire [3:0] 	s8_be;
	wire [31:0] s8_wdata;
	wire [0:0] 	s8_ack;
	wire [0:0] 	s8_resp;
	wire [31:0] s8_rdata;
	
	xbar_nobuf xbar
	(
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .m0_req	(m0_req)
		, .m0_we	(m0_we)
		, .m0_addr	(m0_addr)
		, .m0_be	(m0_be)
		, .m0_wdata	(m0_wdata)
		, .m0_ack	(m0_ack)
		, .m0_resp	(m0_resp)
		, .m0_rdata	(m0_rdata)
		
		, .m1_req	(m1_req)
		, .m1_we	(m1_we)
		, .m1_addr	(m1_addr)
		, .m1_be	(m1_be)
		, .m1_wdata	(m1_wdata)
		, .m1_ack	(m1_ack)
		, .m1_resp	(m1_resp)
		, .m1_rdata	(m1_rdata)
		
		, .m2_req	(m2_req)
		, .m2_we	(m2_we)
		, .m2_addr	(m2_addr)
		, .m2_be	(m2_be)
		, .m2_wdata	(m2_wdata)
		, .m2_ack	(m2_ack)
		, .m2_resp	(m2_resp)
		, .m2_rdata	(m2_rdata)
		
		, .m3_req	(m3_req)
		, .m3_we	(m3_we)
		, .m3_addr	(m3_addr)
		, .m3_be	(m3_be)
		, .m3_wdata	(m3_wdata)
		, .m3_ack	(m3_ack)
		, .m3_resp	(m3_resp)
		, .m3_rdata	(m3_rdata)
		
		, .m4_req	(m4_req)
		, .m4_we	(m4_we)
		, .m4_addr	(m4_addr)
		, .m4_be	(m4_be)
		, .m4_wdata	(m4_wdata)
		, .m4_ack	(m4_ack)
		, .m4_resp	(m4_resp)
		, .m4_rdata	(m4_rdata)
		
		, .m5_req	(m5_req)
		, .m5_we	(m5_we)
		, .m5_addr	(m5_addr)
		, .m5_be	(m5_be)
		, .m5_wdata	(m5_wdata)
		, .m5_ack	(m5_ack)
		, .m5_resp	(m5_resp)
		, .m5_rdata	(m5_rdata)
		
		, .m6_req	(m6_req)
		, .m6_we	(m6_we)
		, .m6_addr	(m6_addr)
		, .m6_be	(m6_be)
		, .m6_wdata	(m6_wdata)
		, .m6_ack	(m6_ack)
		, .m6_resp	(m6_resp)
		, .m6_rdata	(m6_rdata)
		
		, .m7_req	(m7_req)
		, .m7_we	(m7_we)
		, .m7_addr	(m7_addr)
		, .m7_be	(m7_be)
		, .m7_wdata	(m7_wdata)
		, .m7_ack	(m7_ack)
		, .m7_resp	(m7_resp)
		, .m7_rdata	(m7_rdata)
		
		, .m8_req	(m8_req)
		, .m8_we	(m8_we)
		, .m8_addr	(m8_addr)
		, .m8_be	(m8_be)
		, .m8_wdata	(m8_wdata)
		, .m8_ack	(m8_ack)
		, .m8_resp	(m8_resp)
		, .m8_rdata	(m8_rdata)
		
		, .s0_req	(s0_req)
		, .s0_we	(s0_we)
		, .s0_addr	(s0_addr)
		, .s0_be	(s0_be)
		, .s0_wdata	(s0_wdata)
		, .s0_ack	(s0_ack)
		, .s0_resp	(s0_resp)
		, .s0_rdata	(s0_rdata)
		
		, .s1_req	(s1_req)
		, .s1_we	(s1_we)
		, .s1_addr	(s1_addr)
		, .s1_be	(s1_be)
		, .s1_wdata	(s1_wdata)
		, .s1_ack	(s1_ack)
		, .s1_resp	(s1_resp)
		, .s1_rdata	(s1_rdata)
		
		, .s2_req	(s2_req)
		, .s2_we	(s2_we)
		, .s2_addr	(s2_addr)
		, .s2_be	(s2_be)
		, .s2_wdata	(s2_wdata)
		, .s2_ack	(s2_ack)
		, .s2_resp	(s2_resp)
		, .s2_rdata	(s2_rdata)
		
		, .s3_req	(s3_req)
		, .s3_we	(s3_we)
		, .s3_addr	(s3_addr)
		, .s3_be	(s3_be)
		, .s3_wdata	(s3_wdata)
		, .s3_ack	(s3_ack)
		, .s3_resp	(s3_resp)
		, .s3_rdata	(s3_rdata)
		
		, .s4_req	(s4_req)
		, .s4_we	(s4_we)
		, .s4_addr	(s4_addr)
		, .s4_be	(s4_be)
		, .s4_wdata	(s4_wdata)
		, .s4_ack	(s4_ack)
		, .s4_resp	(s4_resp)
		, .s4_rdata	(s4_rdata)
		
		, .s5_req	(s5_req)
		, .s5_we	(s5_we)
		, .s5_addr	(s5_addr)
		, .s5_be	(s5_be)
		, .s5_wdata	(s5_wdata)
		, .s5_ack	(s5_ack)
		, .s5_resp	(s5_resp)
		, .s5_rdata	(s5_rdata)
		
		, .s6_req	(s6_req)
		, .s6_we	(s6_we)
		, .s6_addr	(s6_addr)
		, .s6_be	(s6_be)
		, .s6_wdata	(s6_wdata)
		, .s6_ack	(s6_ack)
		, .s6_resp	(s6_resp)
		, .s6_rdata	(s6_rdata)
		
		, .s7_req	(s7_req)
		, .s7_we	(s7_we)
		, .s7_addr	(s7_addr)
		, .s7_be	(s7_be)
		, .s7_wdata	(s7_wdata)
		, .s7_ack	(s7_ack)
		, .s7_resp	(s7_resp)
		, .s7_rdata	(s7_rdata)
		
		, .s8_req	(s8_req)
		, .s8_we	(s8_we)
		, .s8_addr	(s8_addr)
		, .s8_be	(s8_be)
		, .s8_wdata	(s8_wdata)
		, .s8_ack	(s8_ack)
		, .s8_resp	(s8_resp)
		, .s8_rdata	(s8_rdata)
	);
	
	mpss_tile #(
		.corenum(0)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
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
	
	mpss_tile #(
		.corenum(1)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
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
	
	mpss_tile #(
		.corenum(2)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
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
	
	mpss_tile #(
		.corenum(3)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
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
	
	mpss_tile #(
		.corenum(4)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
	) tile4 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .hpi_mem_req		(s4_req)
		, .hpi_mem_we		(s4_we)
		, .hpi_mem_addr		(s4_addr)
		, .hpi_mem_be		(s4_be)
		, .hpi_mem_wdata	(s4_wdata)
		, .hpi_mem_ack		(s4_ack)
		, .hpi_mem_resp		(s4_resp)
		, .hpi_mem_rdata	(s4_rdata)
		
		, .xbus_mem_req		(m4_req)
		, .xbus_mem_we		(m4_we)
		, .xbus_mem_addr	(m4_addr)
		, .xbus_mem_be		(m4_be)
		, .xbus_mem_wdata	(m4_wdata)
		, .xbus_mem_ack		(m4_ack)
		, .xbus_mem_resp	(m4_resp)
		, .xbus_mem_rdata	(m4_rdata)
	);
	
	mpss_tile #(
		.corenum(5)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
	) tile5 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .hpi_mem_req		(s5_req)
		, .hpi_mem_we		(s5_we)
		, .hpi_mem_addr		(s5_addr)
		, .hpi_mem_be		(s5_be)
		, .hpi_mem_wdata	(s5_wdata)
		, .hpi_mem_ack		(s5_ack)
		, .hpi_mem_resp		(s5_resp)
		, .hpi_mem_rdata	(s5_rdata)
		
		, .xbus_mem_req		(m5_req)
		, .xbus_mem_we		(m5_we)
		, .xbus_mem_addr	(m5_addr)
		, .xbus_mem_be		(m5_be)
		, .xbus_mem_wdata	(m5_wdata)
		, .xbus_mem_ack		(m5_ack)
		, .xbus_mem_resp	(m5_resp)
		, .xbus_mem_rdata	(m5_rdata)
	);
	
	mpss_tile #(
		.corenum(6)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
	) tile6 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .hpi_mem_req		(s6_req)
		, .hpi_mem_we		(s6_we)
		, .hpi_mem_addr		(s6_addr)
		, .hpi_mem_be		(s6_be)
		, .hpi_mem_wdata	(s6_wdata)
		, .hpi_mem_ack		(s6_ack)
		, .hpi_mem_resp		(s6_resp)
		, .hpi_mem_rdata	(s6_rdata)
		
		, .xbus_mem_req		(m6_req)
		, .xbus_mem_we		(m6_we)
		, .xbus_mem_addr	(m6_addr)
		, .xbus_mem_be		(m6_be)
		, .xbus_mem_wdata	(m6_wdata)
		, .xbus_mem_ack		(m6_ack)
		, .xbus_mem_resp	(m6_resp)
		, .xbus_mem_rdata	(m6_rdata)
	);
	
	mpss_tile #(
		.corenum(7)
		, .mem_data(mem_data)
		, .mem_size(mem_size)
	) tile7 (
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		, .hpi_mem_req		(s7_req)
		, .hpi_mem_we		(s7_we)
		, .hpi_mem_addr		(s7_addr)
		, .hpi_mem_be		(s7_be)
		, .hpi_mem_wdata	(s7_wdata)
		, .hpi_mem_ack		(s7_ack)
		, .hpi_mem_resp		(s7_resp)
		, .hpi_mem_rdata	(s7_rdata)
		
		, .xbus_mem_req		(m7_req)
		, .xbus_mem_we		(m7_we)
		, .xbus_mem_addr	(m7_addr)
		, .xbus_mem_be		(m7_be)
		, .xbus_mem_wdata	(m7_wdata)
		, .xbus_mem_ack		(m7_ack)
		, .xbus_mem_resp	(m7_resp)
		, .xbus_mem_rdata	(m7_rdata)
	);
	
	udm_memsplit udm_memsplit
	(
		.clk_i(clk_i)
		, .rst_i(srst)

		, .rx_i(rx_i)
		, .tx_o(tx_o)

		, .rst_o(udm_reset)
		
		, .bus_req_o	(m8_req)
		, .bus_we_o		(m8_we)
		, .bus_addr_bo	(m8_addr)
		, .bus_be_bo	(m8_be)
		, .bus_wdata_bo	(m8_wdata)
		, .bus_ack_i	(m8_ack)
		, .bus_resp_i	(m8_resp)
		, .bus_rdata_bi	(m8_rdata)
	);
	
	gpio gpio
	(
		.clk_i(clk_i)
		, .rst_i(cpu_reset)
		
		,. bus_req	(s8_req)
		, .bus_we	(s8_we)
		, .bus_addr	(s8_addr)
		, .bus_be	(s8_be)
		, .bus_wdata(s8_wdata)
		, .bus_ack	(s8_ack)
		, .bus_resp	(s8_resp)
		, .bus_rdata(s8_rdata)

		, .gpio_bi(gpio_bi)
		, .gpio_bo(gpio_bo)
	);



endmodule
