module arb_l2
(
	input [0:0] clk_i
	, input [0:0] rst_i
	
	, input  [0:0] 	m_req
	, input  [0:0] 	m_we
	, input  [31:0] m_addr
	, input  [3:0] 	m_be
	, input  [31:0] m_wdata
	, output [0:0] 	m_ack
	, output [0:0] 	m_resp
	, output [31:0]	m_rdata
	
	, output [0:0] 	s0_req
	, output [0:0] 	s0_we
	, output [31:0] s0_addr
	, output [3:0] 	s0_be
	, output [31:0] s0_wdata
	, input  [0:0] 	s0_ack
	, input  [0:0] 	s0_resp
	, input  [31:0]	s0_rdata
	
	, output [0:0] 	s1_req
	, output [0:0] 	s1_we
	, output [31:0] s1_addr
	, output [3:0] 	s1_be
	, output [31:0] s1_wdata
	, input  [0:0] 	s1_ack
	, input  [0:0] 	s1_resp
	, input  [31:0]	s1_rdata
);



endmodule
