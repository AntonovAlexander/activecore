module arb_l1
(
	input [0:0] clk_i
	, input [0:0] rst_i
	
	, input  [0:0] 	m0_req
	, input  [0:0] 	m0_we
	, input  [31:0] m0_addr
	, input  [3:0] 	m0_be
	, input  [31:0] m0_wdata
	, output [0:0] 	m0_ack
	, output [0:0] 	m0_resp
	, output [31:0]	m0_rdata
	
	, input  [0:0] 	m1_req
	, input  [0:0] 	m1_we
	, input  [31:0] m1_addr
	, input  [3:0] 	m1_be
	, input  [31:0] m1_wdata
	, output [0:0] 	m1_ack
	, output [0:0] 	m1_resp
	, output [31:0]	m1_rdata
	
	, output [0:0] 	s_req
	, output [0:0] 	s_we
	, output [31:0] s_addr
	, output [3:0] 	s_be
	, output [31:0] s_wdata
	, input  [0:0] 	s_ack
	, input  [0:0] 	s_resp
	, input  [31:0]	s_rdata
);



endmodule
