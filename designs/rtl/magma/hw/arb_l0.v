/*
 * arb_l0.v
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module arb_l0
(
	input [0:0] clk_i
	, input [0:0] rst_i
	
	, input  [0:0] 	m_req
	, input  [0:0] 	m_we
	, input  [31:0] m_addr
	, input  [3:0] 	m_be
	, input  [31:0] m_wdata
	, output reg [0:0] 	m_ack
	, output reg [0:0] 	m_resp
	, output reg [31:0]	m_rdata
	
	, output reg [0:0] 	s0_req
	, output reg [0:0] 	s0_we
	, output reg [31:0] s0_addr
	, output reg [3:0] 	s0_be
	, output reg [31:0] s0_wdata
	, input  [0:0] 	s0_ack
	, input  [0:0] 	s0_resp
	, input  [31:0]	s0_rdata
	
	, output reg [0:0] 	s1_req
	, output reg [0:0] 	s1_we
	, output reg [31:0] s1_addr
	, output reg [3:0] 	s1_be
	, output reg [31:0] s1_wdata
	, input  [0:0] 	s1_ack
	, input  [0:0] 	s1_resp
	, input  [31:0]	s1_rdata
);

reg s0_rd_inprogress, s1_rd_inprogress;
reg s0_rd_inprogress_next, s1_rd_inprogress_next;

always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		s0_rd_inprogress <= 1'b0;
		s1_rd_inprogress <= 1'b0;
		end
	else
		begin
		s0_rd_inprogress <= s0_rd_inprogress_next;
		s1_rd_inprogress <= s1_rd_inprogress_next;
		end
	end

	always @*
		begin
		s0_rd_inprogress_next = s0_rd_inprogress;
		s1_rd_inprogress_next = s1_rd_inprogress;

		if (s0_resp) s0_rd_inprogress_next = 1'b0;
		if (s1_resp) s1_rd_inprogress_next = 1'b0;

		if (m_req && (!m_we))
			begin
			if (!m_addr[31] && m_ack)
				begin
				s0_rd_inprogress_next = 1'b1;
				end
			if (m_addr[31] && m_ack)
				begin
				s1_rd_inprogress_next = 1'b1;
				end
			end

		end

	always @*
		begin
		s0_req 		= 1'b0;
		s0_we 		= 1'b0;
		s0_addr 	= 32'h0;
		s0_be 		= 4'h0;
		s0_wdata 	= 32'h0;

		s1_req 		= 1'b0;
		s1_we 		= 1'b0;
		s1_addr 	= 32'h0;
		s1_be 		= 4'h0;
		s1_wdata 	= 32'h0;

		m_ack		= 1'b0;

		if (m_req && !s0_rd_inprogress && !s1_rd_inprogress)
			begin
			if (!m_addr[31])
				begin
				s0_req 		= m_req;
				s0_we 		= m_we;
				s0_addr 	= m_addr;
				s0_be 		= m_be;
				s0_wdata 	= m_wdata;
				m_ack		= s0_ack;
				end
			else
				begin
				s1_req 		= m_req;
				s1_we 		= m_we;
				s1_addr 	= m_addr;
				s1_be 		= m_be;
				s1_wdata 	= m_wdata;
				m_ack		= s1_ack;
				end
			end
		end

	always @*
		begin
		m_resp = 1'b0;
		m_rdata = 32'h0;

		if (s0_rd_inprogress)
			begin
			m_resp = s0_resp;
			m_rdata = s0_rdata;
			end

		if (s1_rd_inprogress)
			begin
			m_resp = s1_resp;
			m_rdata = s1_rdata;
			end

		end

endmodule
