/*
 * sigma.sv
 *
 *  Created on: 24.09.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

module sigma
#(
	parameter CPU = "none",
	parameter delay_test_flag = 0,
	parameter mem_init="YES",
	parameter mem_data = "data.hex",
	parameter mem_size = 1024
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
reset_cntrl reset_cntrl
(
	.clk_i(clk_i),
	.arst_i(arst_i),
	.srst_o(srst)
);

wire udm_reset;
wire cpu_reset;
assign cpu_reset = srst | udm_reset;

wire irq_btn_debounced;
debouncer debouncer
(
	.clk_i(clk_i)
	, .rst_i(srst)
	, .in(irq_btn_i)
	, .out(irq_btn_debounced)
);

MemSplit32 hpi();
MemSplit32 xbus();

sigma_tile #(
	.corenum(0)
	, .mem_init(mem_init)
	, .mem_data(mem_data)
	, .mem_size(mem_size)
	, .CPU(CPU)
	, .PATH_THROUGH("YES")
) sigma_tile (
	.clk_i(clk_i)
	, .rst_i(cpu_reset)

	, .irq_debounced_i(irq_btn_debounced)
    , .hpi(hpi)
    , .xbus(xbus)
);
	
udm udm
(
	.clk_i(clk_i)
	, .rst_i(srst)

	, .rx_i(rx_i)
	, .tx_o(tx_o)

	, .rst_o(udm_reset)
	
	, .bus_req_o(hpi.req)
	, .bus_we_o(hpi.we)
	, .bus_addr_bo(hpi.addr)
	, .bus_be_bo(hpi.be)
	, .bus_wdata_bo(hpi.wdata)
	, .bus_ack_i(hpi.ack)
	, .bus_resp_i(hpi.resp)
	, .bus_rdata_bi(hpi.rdata)
);


localparam CSR_LED_ADDR         = 32'h80000000;
localparam CSR_SW_ADDR          = 32'h80000004;

logic [31:0] gpio_bo_reg;
assign gpio_bo = gpio_bo_reg;
logic [31:0] gpio_bi_reg;
always @(posedge clk_i) gpio_bi_reg <= gpio_bi;

assign xbus.ack = xbus.req;   // xbus always ready to accept request
logic csr_resp;
logic [31:0] csr_rdata;

// bus request
always @(posedge clk_i)
    begin
    
    csr_resp <= 1'b0;
    
    if (xbus.req && xbus.ack)
        begin
        
        if (xbus.we)     // writing
            begin
            if (xbus.addr == CSR_LED_ADDR) gpio_bo_reg <= xbus.wdata;
            end
        
        else            // reading
            begin
            if (xbus.addr == CSR_LED_ADDR)
                begin
                csr_resp <= 1'b1;
                csr_rdata <= gpio_bo_reg;
                end
            if (xbus.addr == CSR_SW_ADDR)
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
    xbus.resp = csr_resp;
    xbus.rdata = 0;
    if (csr_resp) xbus.rdata = csr_rdata;
    end

endmodule
