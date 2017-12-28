module gpio
(
	input [0:0] clk_i
	, input [0:0] rst_i
	
	, input  [0:0] 	bus_req
	, input  [0:0] 	bus_we
	, input  [31:0] bus_addr
	, input  [3:0] 	bus_be
	, input  [31:0] bus_wdata
	, output [0:0] 	bus_ack
	, output reg [0:0] 	bus_resp
	, output reg [31:0]	bus_rdata

	, input  [31:0]	gpio_bi
	, output reg [31:0]	gpio_bo
);

reg [31:0] regfile [31:0];

always @(posedge clk_i)
	begin
	if (rst_i) bus_resp <= 1'b0;
	else bus_resp <= bus_req & !bus_we;
	end

always @(posedge clk_i)
	begin
	if (rst_i) bus_rdata <= 32'h0;
	else bus_rdata <= gpio_bi;
	end

always @(posedge clk_i)
	begin
	if (rst_i) gpio_bo <= 32'h0;
	if (bus_req && bus_we && (bus_addr[7:0] >= 8'h80)) regfile[bus_addr[6:2]] <= bus_wdata;
	end

assign bus_ack = bus_req;

endmodule
