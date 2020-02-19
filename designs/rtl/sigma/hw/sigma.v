/*
 * sigma.v
 *
 *  Created on: 24.09.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


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

wire cpu_irq_req;
wire [7:0] cpu_irq_code;
wire cpu_irq_ack;
irq_adapter irq_adapter
(
	.clk_i(clk_i)
	, .rst_i(srst)
	, .irq_btn_debounced_i(irq_btn_debounced)
	, .irq_req_o(cpu_irq_req)
	, .irq_code_bo(cpu_irq_code)
	, .irq_ack_i(cpu_irq_ack)
);

wire [0:0] cpu_instr_req;
wire [0:0] cpu_instr_we;
wire [31:0] cpu_instr_addr;
wire [3:0] cpu_instr_be;
wire [31:0] cpu_instr_wdata;
wire [0:0] cpu_instr_ack;
wire [0:0] cpu_instr_resp;
wire [31:0] cpu_instr_rdata;

wire [0:0] cpu_data_req;
wire [0:0] cpu_data_we;
wire [31:0] cpu_data_addr;
wire [3:0] cpu_data_be;
wire [31:0] cpu_data_wdata;
reg [0:0] cpu_data_ack;
reg [0:0] cpu_data_resp;
reg [31:0] cpu_data_rdata;

wire [0:0] udm_req;
wire [0:0] udm_we;
wire [31:0] udm_addr;
wire [3:0] udm_be;
wire [31:0] udm_wdata;
reg [0:0] udm_ack;
reg [0:0] udm_resp;
reg [31:0] udm_rdata;

wire [0:0] bu0_req;
wire [0:0] bu0_we;
wire [31:0] bu0_addr;
wire [3:0] bu0_be;
wire [31:0] bu0_wdata;
wire [0:0] bu0_ack;
wire [0:0] bu0_resp;
wire [31:0] bu0_rdata;

reg [0:0] bu1_req;
reg [0:0] bu1_we;
reg [31:0] bu1_addr;
reg [3:0] bu1_be;
reg [31:0] bu1_wdata;
wire [0:0] bu1_ack;
wire [0:0] bu1_resp;
wire [31:0] bu1_rdata;

// cpu instr-bu0 connection
assign bu0_req = cpu_instr_req;
assign bu0_we = cpu_instr_we;
assign bu0_addr = cpu_instr_addr;
assign bu0_be = cpu_instr_be;
assign bu0_wdata = cpu_instr_wdata;
assign cpu_instr_ack = bu0_ack;
assign cpu_instr_resp = bu0_resp;
assign cpu_instr_rdata = bu0_rdata;

// cpu data-udm-bu1 connection
reg cpu_rd_inprogress, udm_rd_inprogress;
always @(posedge clk_i)
	begin
	if (srst)
		begin
		cpu_rd_inprogress <= 1'b0;
		udm_rd_inprogress <= 1'b0;
		end
	else
		begin
		if ((cpu_data_req == 1'b1) && (cpu_data_we == 1'b0) && (cpu_data_ack == 1'b1)) cpu_rd_inprogress <= 1'b1;
		else if ((cpu_rd_inprogress == 1'b1) && (cpu_data_resp == 1'b1)) cpu_rd_inprogress <= 1'b0;
		
		if ((udm_req == 1'b1) && (udm_we == 1'b0) && (udm_ack == 1'b1)) udm_rd_inprogress <= 1'b1;
		else if ((udm_rd_inprogress == 1'b1) && (udm_resp == 1'b1)) udm_rd_inprogress <= 1'b0;
		end
	end
	
always @*
	begin
	bu1_req = 1'b0;
	bu1_we = 1'b0;
	bu1_addr = 32'h0;
	bu1_be = 4'h0;
	bu1_wdata = 32'h0;
	udm_ack = 1'b0;
	cpu_data_ack = 1'b0;
	if (udm_req)
		begin
		bu1_req = udm_req;
		bu1_we = udm_we;
		bu1_addr = udm_addr;
		bu1_be = udm_be;
		bu1_wdata = udm_wdata;
		udm_ack = bu1_ack;
		end
	else if (cpu_data_req)
		begin
		bu1_req = cpu_data_req;
		bu1_we = cpu_data_we;
		bu1_addr = cpu_data_addr;
		bu1_be = cpu_data_be;
		bu1_wdata = cpu_data_wdata;
		cpu_data_ack = bu1_ack;
		end
	end

always @*
	begin
	udm_resp = 1'b0;
	udm_rdata = 32'h0;
	cpu_data_resp = 1'b0;
	cpu_data_rdata = 32'h0;
	if (cpu_rd_inprogress)
		begin
		cpu_data_resp = bu1_resp;
		cpu_data_rdata = bu1_rdata;
		end
	if (udm_rd_inprogress)
		begin
		udm_resp = bu1_resp;
		udm_rdata = bu1_rdata;
		end
	end
	
udm udm
(
	.clk_i(clk_i)
	, .rst_i(srst)

	, .rx_i(rx_i)
	, .tx_o(tx_o)

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

// Processor core
cpu_wrapper
#(
	.CPU(CPU)
	, .delay_test_flag (delay_test_flag)
)  cpu_wrapper (
	.clk_i(clk_i)
	, .rst_i(cpu_reset)

	, .irq_req_i(cpu_irq_req)
	, .irq_code_bi(cpu_irq_code)
	, .irq_ack_o(cpu_irq_ack)
	
	, .instr_mem_ack(cpu_instr_ack)
	, .instr_mem_resp(cpu_instr_resp)
	, .instr_mem_rdata(cpu_instr_rdata)
	, .data_mem_ack(cpu_data_ack)
	, .data_mem_resp(cpu_data_resp)
	, .data_mem_rdata(cpu_data_rdata)
	, .instr_mem_req(cpu_instr_req)
	, .instr_mem_we(cpu_instr_we)
	, .instr_mem_addr(cpu_instr_addr)
	, .instr_mem_wdata(cpu_instr_wdata)
	, .instr_mem_be(cpu_instr_be)
	, .data_mem_req(cpu_data_req)
	, .data_mem_we(cpu_data_we)
	, .data_mem_addr(cpu_data_addr)
	, .data_mem_wdata(cpu_data_wdata)
	, .data_mem_be(cpu_data_be)
);

bus_unit_memsplit
#(
	.mem_init(mem_init),
	.mem_data(mem_data),
	.mem_size(mem_size)
) bus_unit
(
	
	.clk_i(clk_i)
	, .rst_i(srst)
	
	, .bus0_req_i(bu0_req)
	, .bus0_we_i(bu0_we)
	, .bus0_addr_bi(bu0_addr)
	, .bus0_be_bi(bu0_be)
	, .bus0_wdata_bi(bu0_wdata)
	, .bus0_ack_o(bu0_ack)
	, .bus0_resp_o(bu0_resp)
	, .bus0_rdata_bo(bu0_rdata)
	
	, .bus1_req_i(bu1_req)
	, .bus1_we_i(bu1_we)
	, .bus1_addr_bi(bu1_addr)
	, .bus1_be_bi(bu1_be)
	, .bus1_wdata_bi(bu1_wdata)
	, .bus1_ack_o(bu1_ack)
	, .bus1_resp_o(bu1_resp)
	, .bus1_rdata_bo(bu1_rdata)
	
	, .gpio_bi(gpio_bi)
	, .gpio_bo(gpio_bo)
);

endmodule
