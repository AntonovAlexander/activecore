/*
 * arb_1m2s.sv
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

module arb_1m2s
#(
	parameter BITSEL = 31
)
(
	input [0:0] clk_i
	, input [0:0] rst_i
	
	, MemSplit32.Slave m
	, MemSplit32.Master s0
	, MemSplit32.Master s1
);

	logic s0_rd_inprogress, s1_rd_inprogress;
	logic s0_rd_inprogress_next, s1_rd_inprogress_next;

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

		if (s0.resp) s0_rd_inprogress_next = 1'b0;
		if (s1.resp) s1_rd_inprogress_next = 1'b0;

		if (m.req && m.ack && (!m.we))
			begin
			if (!m.addr[BITSEL]) s0_rd_inprogress_next = 1'b1;
			else s1_rd_inprogress_next = 1'b1;
			end

		end

	always @*
		begin
		s0.req 		= 1'b0;
		s0.we 		= 1'b0;
		s0.addr 	= 32'h0;
		s0.be 		= 4'h0;
		s0.wdata 	= 32'h0;

		s1.req 		= 1'b0;
		s1.we 		= 1'b0;
		s1.addr 	= 32'h0;
		s1.be 		= 4'h0;
		s1.wdata 	= 32'h0;

		m.ack		= 1'b0;

		if (m.req && !s0_rd_inprogress && !s1_rd_inprogress)
			begin
			if (!m.addr[BITSEL])
				begin
				s0.req 		= m.req;
				s0.we 		= m.we;
				s0.addr 	= m.addr;
				s0.be 		= m.be;
				s0.wdata 	= m.wdata;
				m.ack		= s0.ack;
				end
			else
				begin
				s1.req 		= m.req;
				s1.we 		= m.we;
				s1.addr 	= m.addr;
				s1.be 		= m.be;
				s1.wdata 	= m.wdata;
				m.ack		= s1.ack;
				end
			end
		end

	always @*
		begin
		m.resp = 1'b0;
		m.rdata = 32'h0;

		if (s0_rd_inprogress)
			begin
			m.resp = s0.resp;
			m.rdata = s0.rdata;
			end

		if (s1_rd_inprogress)
			begin
			m.resp = s1.resp;
			m.rdata = s1.rdata;
			end

		end

endmodule
