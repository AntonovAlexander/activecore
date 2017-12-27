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
	, output [0:0] 	bus_resp
	, output [31:0]	bus_rdata
);



endmodule
