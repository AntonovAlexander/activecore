/*
 * NEXYS4_DDR.sv
 *
 *  Created on: 01.01.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

`include "citadel_fpu.svh"

module NEXYS4_DDR
#( parameter SIM = "NO" )
(
	input 	CLK100MHZ
    , input   CPU_RESETN
    
    , input   [15:0] SW
    , output logic  [15:0] LED

    , input   UART_TXD_IN
    , output  UART_RXD_OUT
);

localparam UDM_BUS_TIMEOUT = (SIM == "YES") ? 100 : (1024*1024*100);
localparam UDM_RTX_EXTERNAL_OVERRIDE = (SIM == "YES") ? "YES" : "NO";

logic clk_gen;
logic pll_locked;

sys_clk sys_clk
(
    .clk_in1(CLK100MHZ)
    , .reset(!CPU_RESETN)
    , .clk_out1(clk_gen)
    , .locked(pll_locked)
);

logic arst;
assign arst = !(CPU_RESETN & pll_locked);

logic srst;
reset_sync reset_sync
(
	.clk_i(clk_gen),
	.arst_i(arst),
	.srst_o(srst)
);

logic udm_reset;

logic [0:0] udm_req;
logic [0:0] udm_we;
logic [31:0] udm_addr;
logic [3:0] udm_be;
logic [31:0] udm_wdata;
logic [0:0] udm_ack;
logic [0:0] udm_resp;
logic [31:0] udm_rdata;

udm
#(
    .BUS_TIMEOUT(UDM_BUS_TIMEOUT)
    , .RTX_EXTERNAL_OVERRIDE(UDM_RTX_EXTERNAL_OVERRIDE)
) udm (
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

localparam CSR_LED_ADDR         = 32'h00000000;
localparam CSR_SW_ADDR          = 32'h00000004;
localparam TESTMEM_ADDR         = 32'h80000000;

localparam CSR_CITADEL_CTRL_ADDR        = 32'h10000000;
localparam CSR_CITADEL_EXEC_ADDR        = 32'h10000004;
localparam CSR_CITADEL_RF_WE_ADDR       = 32'h10000008;
localparam CSR_CITADEL_RF_ADDR_ADDR     = 32'h1000000C;
localparam CSR_CITADEL_RF_WDATA_ADDR    = 32'h10000010;
localparam CSR_CITADEL_FU_ID_ADDR       = 32'h10000014;
localparam CSR_CITADEL_FU_OPCODE_ADDR   = 32'h10000018;
localparam CSR_CITADEL_FU_RS0_ADDR      = 32'h1000001C;
localparam CSR_CITADEL_FU_RS1_ADDR      = 32'h10000020;
localparam CSR_CITADEL_FU_RS2_ADDR      = 32'h10000024;
localparam CSR_CITADEL_FU_RD_ADDR       = 32'h10000028;

localparam CSR_CITADEL_RDATA_ADDR       = 32'h10000040;

logic cmd_req_genfifo_req;
citadel_fpu_cmd_req_struct cmd_req_genfifo_data;
logic cmd_req_genfifo_ack;

logic cmd_resp_genfifo_req;
logic [31:0] cmd_resp_genfifo_data;
logic cmd_resp_genfifo_ack;
assign cmd_resp_genfifo_ack = 1'b1;

logic [31:0] citadel_databuf;

citadel_fpu citadel_inst (
	.clk_i(clk_gen)
	, .rst_i(srst)
	
	, .cmd_req_genfifo_req_i(cmd_req_genfifo_req)
	, .cmd_req_genfifo_rdata_bi(cmd_req_genfifo_data)
	, .cmd_req_genfifo_ack_o(cmd_req_genfifo_ack)

	, .cmd_resp_genfifo_req_o(cmd_resp_genfifo_req)
	, .cmd_resp_genfifo_wdata_bo(cmd_resp_genfifo_data)
	, .cmd_resp_genfifo_ack_i(cmd_resp_genfifo_ack)
);

localparam TESTMEM_WSIZE_POW    = 10;
localparam TESTMEM_WSIZE        = 2**TESTMEM_WSIZE_POW;

logic testmem_udm_enb;
assign testmem_udm_enb = (!(udm_addr < TESTMEM_ADDR) && (udm_addr < (TESTMEM_ADDR + (TESTMEM_WSIZE*4))));

logic testmem_udm_we;
logic [TESTMEM_WSIZE_POW-1:0] testmem_udm_addr;
logic [31:0] testmem_udm_wdata;
logic [31:0] testmem_udm_rdata;

logic testmem_p1_we;
logic [TESTMEM_WSIZE_POW-1:0] testmem_p1_addr;
logic [31:0] testmem_p1_wdata;
logic [31:0] testmem_p1_rdata;

// testmem's port1 is inactive
assign testmem_p1_we = 1'b0;
assign testmem_p1_addr = 0;
assign testmem_p1_wdata = 0;

ram_dual #(
    .init_type("none")
    , .init_data("nodata.hex")
    , .dat_width(32)
    , .adr_width(TESTMEM_WSIZE_POW)
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
logic csr_resp, testmem_resp, testmem_resp_dly;
logic [31:0] csr_rdata;


// bus request
always @(posedge clk_gen)
    begin
    
    cmd_req_genfifo_req <= 1'b0;
    if (cmd_resp_genfifo_req) citadel_databuf <= cmd_resp_genfifo_data;
    
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
            if (udm_addr == CSR_CITADEL_CTRL_ADDR)      cmd_req_genfifo_req                 <= udm_wdata[0];
            if (udm_addr == CSR_CITADEL_EXEC_ADDR)      cmd_req_genfifo_data.exec           <= udm_wdata;
            if (udm_addr == CSR_CITADEL_RF_WE_ADDR)     cmd_req_genfifo_data.rf_we          <= udm_wdata;
            if (udm_addr == CSR_CITADEL_RF_ADDR_ADDR)   cmd_req_genfifo_data.rf_addr        <= udm_wdata;
            if (udm_addr == CSR_CITADEL_RF_WDATA_ADDR)  cmd_req_genfifo_data.rf_wdata       <= udm_wdata;
            if (udm_addr == CSR_CITADEL_FU_ID_ADDR)     cmd_req_genfifo_data.fu_id          <= udm_wdata;
            if (udm_addr == CSR_CITADEL_FU_OPCODE_ADDR) cmd_req_genfifo_data.fu_imm_opcode  <= udm_wdata;
            if (udm_addr == CSR_CITADEL_FU_RS0_ADDR)    cmd_req_genfifo_data.fu_rs0         <= udm_wdata;
            if (udm_addr == CSR_CITADEL_FU_RS1_ADDR)    cmd_req_genfifo_data.fu_rs1         <= udm_wdata;
            if (udm_addr == CSR_CITADEL_FU_RS2_ADDR)    cmd_req_genfifo_data.fu_rs2         <= udm_wdata;
            if (udm_addr == CSR_CITADEL_FU_RD_ADDR)     cmd_req_genfifo_data.fu_rd          <= udm_wdata;
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
            if (udm_addr == CSR_CITADEL_RDATA_ADDR)
                begin
                csr_resp <= 1'b1;
                csr_rdata <= citadel_databuf;
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
