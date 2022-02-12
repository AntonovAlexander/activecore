/*
 * sigrun.sv
 *
 *  Created on: 22.07.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

module sigrun
#(
	parameter UDM_BUS_TIMEOUT = (1024*1024*100)
	, UDM_RTX_EXTERNAL_OVERRIDE = "NO"
	, DEBOUNCER_FACTOR_POW = 20
	, mem_init_type = "elf"
	, mem_init_data = "data.hex"
	, mem_size = 1024
)
(
	input clk_i
	, input arst_i
	, input irq_btn_i
	, input rx_i
	, output tx_o
	, input [31:0] gpio_bi
	, output [31:0] gpio_bo
);

wire srst;
reset_sync reset_sync
(
	.clk_i(clk_i),
	.arst_i(arst_i),
	.srst_o(srst)
);

wire udm_reset;
wire cpu_reset;
assign cpu_reset = srst | udm_reset;

wire irq_btn_debounced;
debouncer
#(
    .FACTOR_POW(DEBOUNCER_FACTOR_POW)
) debouncer (
	.clk_i(clk_i)
	, .rst_i(srst)
	, .in(irq_btn_i)
	, .out(irq_btn_debounced)
);

MemSplit32 hif();
MemSplit32 xif();

sigrun_tile #(
	.corenum(0)
	, .mem_init_type(mem_init_type)
	, .mem_init_data(mem_init_data)
	, .mem_size(mem_size)
	, .PATH_THROUGH("YES")
) sigrun_tile (
	.clk_i(clk_i)
	, .rst_i(cpu_reset)

	, .irq_debounced_bi({0, irq_btn_debounced, 3'h0})
	
	, .hif(hif)
	, .xif(xif)
);
	
udm #(
    .BUS_TIMEOUT(UDM_BUS_TIMEOUT)
    , .RTX_EXTERNAL_OVERRIDE(UDM_RTX_EXTERNAL_OVERRIDE)
) udm (
	.clk_i(clk_i)
	, .rst_i(srst)

	, .rx_i(rx_i)
	, .tx_o(tx_o)

	, .rst_o(udm_reset)
	
	, .bus_req_o(hif.req)
	, .bus_we_o(hif.we)
	, .bus_addr_bo(hif.addr)
	, .bus_be_bo(hif.be)
	, .bus_wdata_bo(hif.wdata)
	, .bus_ack_i(hif.ack)
	, .bus_resp_i(hif.resp)
	, .bus_rdata_bi(hif.rdata)
);


localparam CSR_LED_ADDR         = 32'h80000000;
localparam CSR_SW_ADDR          = 32'h80000004;

logic [31:0] gpio_bo_reg;
assign gpio_bo = gpio_bo_reg;
logic [31:0] gpio_bi_reg;
always @(posedge clk_i) gpio_bi_reg <= gpio_bi;

assign xif.ack = xif.req;   // xif always ready to accept request
logic csr_resp;
logic [31:0] csr_rdata;

// bus request
always @(posedge clk_i)
    begin
    
    csr_resp <= 1'b0;
    
    if (xif.req && xif.ack)
        begin
        
        if (xif.we)     // writing
            begin
            if (xif.addr == CSR_LED_ADDR) gpio_bo_reg <= xif.wdata;
            end
        
        else            // reading
            begin
            if (xif.addr == CSR_LED_ADDR)
                begin
                csr_resp <= 1'b1;
                csr_rdata <= gpio_bo_reg;
                end
            if (xif.addr == CSR_SW_ADDR)
                begin
                csr_resp <= 1'b1;
                csr_rdata <= gpio_bi_reg;
                end
            end
        end
    end

// bus response
always @*
    begin
    xif.resp = csr_resp;
    xif.rdata = 0;
    if (csr_resp) xif.rdata = csr_rdata;
    end

endmodule
