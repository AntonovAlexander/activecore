/*
 * arb_2m1s.sv
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

module arb_2m3s
#(
	parameter SFR_BITSEL = 16
	, parameter XIF_BITSEL = 31
)
(
	input [0:0] clk_i
	, input [0:0] rst_i
	
	, MemSplit32.Slave m0
	, MemSplit32.Slave m1
	, MemSplit32.Master s0
	, MemSplit32.Master s1
	, MemSplit32.Master s2
);

	logic m0_s0_rd_inprogress, m0_s0_rd_inprogress_next;
	logic m0_s1_rd_inprogress, m0_s1_rd_inprogress_next;
	logic m0_s2_rd_inprogress, m0_s2_rd_inprogress_next;

	logic m1_s0_rd_inprogress, m1_s0_rd_inprogress_next;
	logic m1_s1_rd_inprogress, m1_s1_rd_inprogress_next;
	logic m1_s2_rd_inprogress, m1_s2_rd_inprogress_next;

	always @(posedge clk_i)
		begin
		if (rst_i)
			begin
			m0_s0_rd_inprogress <= 1'b0;
			m0_s1_rd_inprogress <= 1'b0;
			m0_s2_rd_inprogress <= 1'b0;
			m1_s0_rd_inprogress <= 1'b0;
			m1_s1_rd_inprogress <= 1'b0;
			m1_s2_rd_inprogress <= 1'b0;
			end
		else
			begin
			m0_s0_rd_inprogress <= m0_s0_rd_inprogress_next;
			m0_s1_rd_inprogress <= m0_s1_rd_inprogress_next;
			m0_s2_rd_inprogress <= m0_s2_rd_inprogress_next;
			m1_s0_rd_inprogress <= m1_s0_rd_inprogress_next;
			m1_s1_rd_inprogress <= m1_s1_rd_inprogress_next;
			m1_s2_rd_inprogress <= m1_s2_rd_inprogress_next;
			end
		end
	
	logic s0_busy, s1_busy, s2_busy;

	always @*
		begin

		m0.ack = 1'b0;
		m0.resp = 1'b0;
		m0.rdata = 0;

		m1.ack = 1'b0;
		m1.resp = 1'b0;
		m1.rdata = 0;

		s0_busy = 1'b0;
		s1_busy = 1'b0;
		s2_busy = 1'b0;

		s0.req 		= 1'b0;
		s0.we 		= 1'b0;
		s0.addr 	= 0;
		s0.be 		= 4'h0;
		s0.wdata 	= 0;

		s1.req 		= 1'b0;
		s1.we 		= 1'b0;
		s1.addr 	= 0;
		s1.be 		= 4'h0;
		s1.wdata 	= 0;

		s2.req 		= 1'b0;
		s2.we 		= 1'b0;
		s2.addr 	= 0;
		s2.be 		= 4'h0;
		s2.wdata 	= 0;
		
		m0_s0_rd_inprogress_next = m0_s0_rd_inprogress;
		m0_s1_rd_inprogress_next = m0_s1_rd_inprogress;
		m0_s2_rd_inprogress_next = m0_s2_rd_inprogress;
		m1_s0_rd_inprogress_next = m1_s0_rd_inprogress;
		m1_s1_rd_inprogress_next = m1_s1_rd_inprogress;
		m1_s2_rd_inprogress_next = m1_s2_rd_inprogress;

		if (s0.resp)
			begin
			if (m0_s0_rd_inprogress_next)
				begin
				m0.resp = s0.resp;
				m0.rdata = s0.rdata;
				end
			if (m1_s0_rd_inprogress_next)
				begin
				m1.resp = s0.resp;
				m1.rdata = s0.rdata;
				end
			m0_s0_rd_inprogress_next = 1'b0;
			m1_s0_rd_inprogress_next = 1'b0;
			end

		if (s1.resp)
			begin
			if (m0_s1_rd_inprogress_next)
				begin
				m0.resp = s1.resp;
				m0.rdata = s1.rdata;
				end
			if (m1_s1_rd_inprogress_next)
				begin
				m1.resp = s1.resp;
				m1.rdata = s1.rdata;
				end
			m0_s1_rd_inprogress_next = 1'b0;
			m1_s1_rd_inprogress_next = 1'b0;
			end

		if (s2.resp)
			begin
			if (m0_s2_rd_inprogress_next)
				begin
				m0.resp = s2.resp;
				m0.rdata = s2.rdata;
				end
			if (m1_s2_rd_inprogress_next)
				begin
				m1.resp = s2.resp;
				m1.rdata = s2.rdata;
				end
			m0_s2_rd_inprogress_next = 1'b0;
			m1_s2_rd_inprogress_next = 1'b0;
			end

		if (m0.req)
			begin
			if (m0.addr[XIF_BITSEL])
				begin
				if (!s2_busy && !m0_s2_rd_inprogress_next)
					begin
					s2.req 		= m0.req;
					s2.we 		= m0.we;
					s2.addr 	= m0.addr;
					s2.be 		= m0.be;
					s2.wdata 	= m0.wdata;
					m0.ack 		= s2.ack;
					m0_s2_rd_inprogress_next = !m0.we;
					s2_busy = 1'b1;
					end
				end
			else if (m0.addr[SFR_BITSEL])
				begin
				if (!s1_busy && !m0_s1_rd_inprogress_next)
					begin
					s1.req 		= m0.req;
					s1.we 		= m0.we;
					s1.addr 	= m0.addr;
					s1.be 		= m0.be;
					s1.wdata 	= m0.wdata;
					m0.ack 		= s1.ack;
					m0_s1_rd_inprogress_next = !m0.we;
					s1_busy = 1'b1;
					end
				end
			else
				begin
				if (!s0_busy && !m0_s0_rd_inprogress_next)
					begin
					s0.req 		= m0.req;
					s0.we 		= m0.we;
					s0.addr 	= m0.addr;
					s0.be 		= m0.be;
					s0.wdata 	= m0.wdata;
					m0.ack 		= s0.ack;
					m0_s0_rd_inprogress_next = !m0.we;
					s0_busy = 1'b1;
					end
				end
			end

		if (m1.req)
			begin
			if (m1.addr[XIF_BITSEL])
				begin
				if (!s2_busy && !m1_s2_rd_inprogress_next)
					begin
					s2.req 		= m1.req;
					s2.we 		= m1.we;
					s2.addr 	= m1.addr;
					s2.be 		= m1.be;
					s2.wdata 	= m1.wdata;
					m1.ack 		= s2.ack;
					m1_s2_rd_inprogress_next = !m1.we;
					s2_busy = 1'b1;
					end
				end
			else if (m1.addr[SFR_BITSEL])
				begin
				if (!s1_busy && !m1_s1_rd_inprogress_next)
					begin
					s1.req 		= m1.req;
					s1.we 		= m1.we;
					s1.addr 	= m1.addr;
					s1.be 		= m1.be;
					s1.wdata 	= m1.wdata;
					m1.ack 		= s1.ack;
					m1_s1_rd_inprogress_next = !m1.we;
					s1_busy = 1'b1;
					end
				end
			else
				begin
				if (!s0_busy && !m1_s0_rd_inprogress_next)
					begin
					s0.req 		= m1.req;
					s0.we 		= m1.we;
					s0.addr 	= m1.addr;
					s0.be 		= m1.be;
					s0.wdata 	= m1.wdata;
					m1.ack 		= s0.ack;
					m1_s0_rd_inprogress_next = !m1.we;
					s0_busy = 1'b1;
					end
				end
			end
		end


endmodule
