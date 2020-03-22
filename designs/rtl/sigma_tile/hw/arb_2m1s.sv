/*
 * arb_2m1s.sv
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

module arb_2m1s
(
	input [0:0] clk_i
	, input [0:0] rst_i
	
	, MemSplit32.Slave m0
	, MemSplit32.Slave m1
	, MemSplit32.Master s
);

	logic m0_rd_inprogress, m1_rd_inprogress;
	logic m0_rd_inprogress_next, m1_rd_inprogress_next;
	logic rr_arb_state;

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

		if (m0.resp) m0_rd_inprogress_next = 1'b0;
		if (m1.resp) m1_rd_inprogress_next = 1'b0;
		
		if (m0.req && !m0.we && m0.ack) m0_rd_inprogress_next = 1'b1;
		if (m1.req && !m1.we && m1.ack) m1_rd_inprogress_next = 1'b1;
		end
	
	always @*
		begin
		m0.resp = 1'b0;
		m0.rdata = 32'h0;
		
		m1.resp = 1'b0;
		m1.rdata = 32'h0;
		
		if (m0_rd_inprogress)
			begin
			m0.resp = s.resp;
			m0.rdata = s.rdata;
			end

		if (m1_rd_inprogress)
			begin
			m1.resp = s.resp;
			m1.rdata = s.rdata;
			end
		end

	always @*
		begin
		s.req = 1'b0;
		s.we = 1'b0;
		s.addr = 32'h0;
		s.be = 4'h0;
		s.wdata = 32'h0;

		m0.ack = 1'b0;
		m1.ack = 1'b0;

		if (!m0_rd_inprogress && !m1_rd_inprogress)
			begin
			if (!rr_arb_state)
				begin
				if (m0.req)
					begin
					s.req = m0.req;
					s.we = m0.we;
					s.addr = m0.addr;
					s.be = m0.be;
					s.wdata = m0.wdata;
					m0.ack = s.ack;
					end
				else if (m1.req)
					begin
					s.req = m1.req;
					s.we = m1.we;
					s.addr = m1.addr;
					s.be = m1.be;
					s.wdata = m1.wdata;
					m1.ack = s.ack;
					end
				end
			else
				begin
				if (m1.req)
					begin
					s.req = m1.req;
					s.we = m1.we;
					s.addr = m1.addr;
					s.be = m1.be;
					s.wdata = m1.wdata;
					m1.ack = s.ack;
					end
				else if (m0.req)
					begin
					s.req = m0.req;
					s.we = m0.we;
					s.addr = m0.addr;
					s.be = m0.be;
					s.wdata = m0.wdata;
					m0.ack = s.ack;
					end
				end
			end
		end


endmodule
