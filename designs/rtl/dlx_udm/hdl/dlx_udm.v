module dlx_udm
#(
    parameter mem_data="data.hex", mem_size=1024
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

// udm bus //
wire bus_enb;
wire bus_we;
wire [31:0] bus_addr;
wire [31:0] bus_wdata;
reg bus_ack;
wire [31:0] bus_rdata;

// port0 bus //
wire [0:0] port0_req;
wire [0:0] port0_we;
wire [31:0] port0_addr;
wire [31:0] port0_wdata;
wire [3:0] port0_be;
wire [31:0] port0_rdata;

// dlx data bus //
wire [0:0] dlx_data_req;
wire [0:0] dlx_data_we;
wire [31:0] dlx_data_addr;
wire [31:0] dlx_data_wdata;
wire [3:0] dlx_data_be;
wire [31:0] dlx_data_rdata;

// port1 bus //
reg [0:0] port1_req;
reg [0:0] port1_we;
reg [31:0] port1_addr;
reg [31:0] port1_wdata;
reg [3:0] port1_be;
wire [31:0] port1_rdata;

reg bus_rd_resp;		// 0 - dlx, 1 - udm
always @(posedge clk_i)
	begin
	bus_rd_resp <= 1'b0;
	if (!dlx_data_req && bus_enb && !bus_we && !bus_rd_resp) bus_rd_resp <= 1'b1;
	end
assign bus_rdata = port1_rdata;

always @*
	begin
	bus_ack = 1'b0;
	if (bus_enb)
		begin
		if (bus_we) bus_ack = !dlx_data_req;
		else bus_ack = bus_rd_resp;
		end
	end

always @*
	begin
	if (dlx_data_req)
		begin
		port1_req = dlx_data_req;
		port1_we = dlx_data_we;
		port1_addr = dlx_data_addr;
		port1_wdata = dlx_data_wdata;
		port1_be = dlx_data_be;
		end
	else
		begin
		port1_req = bus_enb;
		port1_we = bus_we;
		port1_addr = bus_addr;
		port1_wdata = bus_wdata;
		port1_be = 4'hf;
		end
	end
assign dlx_data_rdata = port1_rdata;

udm udm
(
	.clk_i(clk_i)
	, .rst_i(srst)
	, .rx_i(rx_i)
	, .tx_o(tx_o)
	, .rst_o(udm_reset)
	 
	, .bus_enb_o(bus_enb)
	, .bus_we_o(bus_we)
	, .bus_addr_bo(bus_addr)
   , .bus_wdata_bo(bus_wdata)

   , .bus_ack_i(bus_ack)
   , .bus_rdata_bi(bus_rdata)
);

dlx dlx (
	.clk_i(clk_i)
	, .rst_i(cpu_reset)
	
	, .instr_mem_req(port0_req)
	, .instr_mem_we(port0_we)
	, .instr_mem_addr(port0_addr)
	, .instr_mem_wdata(port0_wdata)
	, .instr_mem_be(port0_be)
	, .instr_mem_rdata(port0_rdata)
	
	, .data_mem_req(dlx_data_req)
	, .data_mem_we(dlx_data_we)
	, .data_mem_addr(dlx_data_addr)
	, .data_mem_wdata(dlx_data_wdata)
	, .data_mem_be(dlx_data_be)
	, .data_mem_rdata(dlx_data_rdata)
);

bus_unit
#(
	.mem_data(mem_data),
	.mem_size(mem_size)
) bus_unit
(
	
	.clk_i(clk_i)
	, .rst_i(srst)
	
	, .port0_req(port0_req)
	, .port0_we(port0_we)
	, .port0_addr(port0_addr)
	, .port0_wdata(port0_wdata)
	, .port0_be(port0_be)
	, .port0_rdata(port0_rdata)
	
	, .port1_req(port1_req)
	, .port1_we(port1_we)
	, .port1_addr(port1_addr)
	, .port1_wdata(port1_wdata)
	, .port1_be(port1_be)
	, .port1_rdata(port1_rdata)
	
	, .gpio_bi(gpio_bi)
	, .gpio_bo(gpio_bo)
);

endmodule
