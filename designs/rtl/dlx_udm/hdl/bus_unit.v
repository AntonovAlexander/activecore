module bus_unit
#(
    parameter mem_data="data.hex", mem_size=1024
)
(
	
	input [0:0] clk_i,
	input [0:0] rst_i,
	
	input [0:0] port0_req,
	input [0:0] port0_we,
	input [31:0] port0_addr,
	input [31:0] port0_wdata,
	input [3:0] port0_be,
	output [31:0] port0_rdata,
	
	input [0:0] port1_req,
	input [0:0] port1_we,
	input [31:0] port1_addr,
	input [31:0] port1_wdata,
	input [3:0] port1_be,
	output reg [31:0] port1_rdata,
	
	input [31:0] SW,
	output [31:0] LED
);

reg [31:0] led_reg;
assign LED = led_reg;

reg [31:0] sw_reg;
always @(posedge clk_i) sw_reg <= SW;

// ram/IO commutation
reg ram_enb, io_enb;
always @*
	begin
	ram_enb = 1'b0;
	io_enb = 1'b0;
	if (port1_addr < 32'h80000000) ram_enb = port1_req;
	else io_enb = port1_req;
	end

// write io
always @(posedge clk_i)
	begin
	if (io_enb && port1_we)
		begin
		if (port1_addr[7:0] == 8'h0) led_reg <= port1_wdata;
		end
	end
	
// read io
reg [31:0] io_rdata;
reg io_resp;
always @(posedge clk_i)
	begin
	io_resp <= 1'b0;
	if (io_enb && !port1_we)
		begin
		io_resp <= 1'b1;
		if (port1_addr[7:0] == 8'h0) io_rdata <= led_reg;
		else if (port1_addr[7:0] == 8'h4) io_rdata <= sw_reg;
		end
	end

// read commutation
wire [31:0] ram_port1_rdata;
always @*
	begin
	port1_rdata = ram_port1_rdata;
	if (io_resp) port1_rdata = io_rdata;
	end

ram_dual
#(
	.mem_data(mem_data),
	.dat_width(32),
	.adr_width(30),
	.mem_size(mem_size)
)
ram
(
	.clk(clk_i)

	, .dat0_i(port0_wdata)
   , .adr0_i(port0_addr[31:2])
   , .we0_i(port0_we)
   , .dat0_o(port0_rdata)

   , .dat1_i(port1_wdata)
   , .adr1_i(port1_addr[31:2])
   , .we1_i(ram_enb & port1_we)
   , .dat1_o(ram_port1_rdata)
);

endmodule
