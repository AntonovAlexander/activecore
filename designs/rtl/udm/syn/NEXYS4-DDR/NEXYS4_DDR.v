/*
 * NEXYS4_DDR.v
 *
 *  Created on: 01.01.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module NEXYS4_DDR
(
	input 	CLK100MHZ
    , input   CPU_RESETN
    
    , input   [15:0] SW
    , output reg  [15:0] LED

    , input   UART_TXD_IN
    , output  UART_RXD_OUT
);

wire clk_gen;
wire pll_locked;

sys_clk sys_clk
(
    .clk_in1(CLK100MHZ)
    , .reset(!CPU_RESETN)
    , .clk_out1(clk_gen)
    , .locked(pll_locked)
);

wire arst;
assign arst = !(CPU_RESETN & pll_locked);

wire srst;
reset_cntrl reset_cntrl
(
	.clk_i(clk_gen),
	.arst_i(arst),
	.srst_o(srst)
);

wire udm_reset;

wire [0:0] udm_req;
wire [0:0] udm_we;
wire [31:0] udm_addr;
wire [3:0] udm_be;
wire [31:0] udm_wdata;
wire [0:0] udm_ack;
reg [0:0] udm_resp;
reg [31:0] udm_rdata;

udm_memsplit udm_memsplit
(
	.clk_i(clk_gen)
	, .rst_i(srst)

	, .rx_i(UART_TXD_IN)
	, .tx_o(UART_RXD_OUT)

	, .rst_o(udm_reset)
	
	, .bus_req_o(udm_req)
	, .bus_we_o(udm_we)
	, .bus_addr_bo(udm_addr)
	, .bus_be_bo(udm_be)
	, .bus_wdata_bo(udm_wdata)
	, .bus_ack_i(udm_ack)
	, .bus_resp_i(udm_resp)
	, .bus_rdata_bi(udm_rdata)
);

localparam CSR_LED_ADDR	 	= 32'h00000000;
localparam CSR_SW_ADDR  	= 32'h00000004;
localparam TESTMEM_ADDR 	= 32'h80000000;
localparam TESTMEM_WSIZE	= 1024;

wire testmem_udm_enb;
assign testmem_udm_enb = (!(udm_addr < TESTMEM_ADDR) && (udm_addr < (TESTMEM_ADDR + (TESTMEM_WSIZE*4))));

reg testmem_udm_we;
reg [31:0] testmem_udm_addr, testmem_udm_wdata;
wire [31:0] testmem_udm_rdata;

wire testmem_p1_we;
wire [31:0] testmem_p1_addr, testmem_p1_wdata;
wire [31:0] testmem_p1_rdata;

// testmem's port1 is inactive
assign testmem_p1_we = 1'b0;
assign testmem_p1_addr = 0;
assign testmem_p1_wdata = 0;

ram_dual #(
    .mem_init("NO")
    , .mem_data("nodata.hex")
    , .dat_width(32)
    , .adr_width(32)
    , .mem_size(TESTMEM_WSIZE)
) testmem (
    .clk(clk_gen)

    , .dat0_i(testmem_udm_wdata)
    , .adr0_i(testmem_udm_addr)
    , .we0_i(testmem_udm_we)
    , .dat0_o(testmem_udm_rdata)

    , .dat1_i(testmem_p1_wdata)
    , .adr1_i(testmem_p1_addr)
    , .we1_i(testmem_p1_we)
    , .dat1_o(testmem_p1_rdata)
);

assign udm_ack = udm_req;   // bus always ready to accept request
reg csr_resp, testmem_resp, testmem_resp_dly;
reg [31:0] csr_rdata;

// bus request
always @(posedge clk_gen)
    begin
    
    testmem_udm_we <= 1'b0;
    testmem_udm_addr <= 0;
    testmem_udm_wdata <= 0;
    
    csr_resp <= 1'b0;
    testmem_resp_dly <= 1'b0;
    testmem_resp <= testmem_resp_dly;
    
    if (udm_req && udm_ack)
        begin
        
        if (udm_we)     // writing
            begin
            if (udm_addr == CSR_LED_ADDR) LED <= udm_wdata;
            if (testmem_udm_enb)
                begin
                testmem_udm_we <= 1'b1;
                testmem_udm_addr <= udm_addr[31:2];     // 4-byte aligned access only
                testmem_udm_wdata <= udm_wdata;
                end
            end
        
        else            // reading
            begin
            if (udm_addr == CSR_LED_ADDR)
                begin
                csr_resp <= 1'b1;
                csr_rdata <= LED;
                end
            if (udm_addr == CSR_SW_ADDR)
                begin
                csr_resp <= 1'b1;
                csr_rdata <= SW;
                end
            if (testmem_udm_enb)
                begin
                testmem_udm_we <= 1'b0;
                testmem_udm_addr <= udm_addr[31:2];     // 4-byte aligned access only
                testmem_udm_wdata <= udm_wdata;
                testmem_resp_dly <= 1'b1;
                end
            end
        end
    end

// bus response
always @*
    begin
    udm_resp = csr_resp | testmem_resp;
    udm_rdata = 0;
    if (csr_resp) udm_rdata = csr_rdata;
    if (testmem_resp) udm_rdata = testmem_udm_rdata;
    end

endmodule
