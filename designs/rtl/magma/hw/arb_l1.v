/*
 * arb_l1.v
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module arb_l1
(
	input [0:0] clk_i
	, input [0:0] rst_i
	
	, input  [0:0] 	m0_req
	, input  [0:0] 	m0_we
	, input  [31:0] m0_addr
	, input  [3:0] 	m0_be
	, input  [31:0] m0_wdata
	, output reg [0:0] 	m0_ack
	, output reg [0:0] 	m0_resp
	, output reg [31:0]	m0_rdata
	
	, input  [0:0] 	m1_req
	, input  [0:0] 	m1_we
	, input  [31:0] m1_addr
	, input  [3:0] 	m1_be
	, input  [31:0] m1_wdata
	, output reg [0:0] 	m1_ack
	, output reg [0:0] 	m1_resp
	, output reg [31:0]	m1_rdata
	
	, output reg [0:0] 	s_req
	, output reg [0:0] 	s_we
	, output reg [31:0] s_addr
	, output reg [3:0] 	s_be
	, output reg [31:0] s_wdata
	, input  [0:0] 	s_ack
	, input  [0:0] 	s_resp
	, input  [31:0]	s_rdata
);

	reg m0_rd_inprogress, m1_rd_inprogress;
	reg m0_rd_inprogress_next, m1_rd_inprogress_next;
	reg rr_arb_state;

	always @(posedge clk_i)
		begin
		if (rst_i)
			begin
			m0_rd_inprogress <= 1'b0;
			m1_rd_inprogress <= 1'b0;
			end
		else
			begin
			m0_rd_inprogress <= m0_rd_inprogress_next;
			m1_rd_inprogress <= m1_rd_inprogress_next;
			end
		end

	always @*
		begin
		m0_rd_inprogress_next = m0_rd_inprogress;
		m1_rd_inprogress_next = m1_rd_inprogress;

		if (s_resp)
			begin
			if (m0_rd_inprogress_next) m0_rd_inprogress_next = 1'b0;
			if (m1_rd_inprogress_next) m1_rd_inprogress_next = 1'b0;
			end

		if (m0_req && !m0_we && m0_ack) m0_rd_inprogress_next = 1'b1;
		if (m1_req && !m1_we && m1_ack) m1_rd_inprogress_next = 1'b1;
		end

	always @*
		begin
		s_req = 1'b0;
		s_we = 1'b0;
		s_addr = 32'h0;
		s_be = 4'h0;
		s_wdata = 32'h0;

		m0_ack = 1'b0;
		m0_resp = 1'b0;
		m0_rdata = 32'h0;

		m1_ack = 1'b0;
		m1_resp = 1'b0;
		m1_rdata = 32'h0;

		if (m0_rd_inprogress)
			begin
			m0_resp = s_resp;
			m0_rdata = s_rdata;
			end

		if (m1_rd_inprogress)
			begin
			m1_resp = s_resp;
			m1_rdata = s_rdata;
			end

		if (!m0_rd_inprogress && !m1_rd_inprogress)
			begin
			if (!rr_arb_state)
				begin
				if (m0_req)
					begin
					s_req = m0_req;
					s_we = m0_we;
					s_addr = m0_addr;
					s_be = m0_be;
					s_wdata = m0_wdata;
					m0_ack = s_ack;
					end
				else if (m1_req)
					begin
					s_req = m1_req;
					s_we = m1_we;
					s_addr = m1_addr;
					s_be = m1_be;
					s_wdata = m1_wdata;
					m1_ack = s_ack;
					end
				end
			else
				begin
				if (m1_req)
					begin
					s_req = m1_req;
					s_we = m1_we;
					s_addr = m1_addr;
					s_be = m1_be;
					s_wdata = m1_wdata;
					m1_ack = s_ack;
					end
				else if (m0_req)
					begin
					s_req = m0_req;
					s_we = m0_we;
					s_addr = m0_addr;
					s_be = m0_be;
					s_wdata = m0_wdata;
					m0_ack = s_ack;
					end
				end
			end
		end


endmodule
