/*
 * cpu_wrapper.sv
 *
 *  Created on: 24.09.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "riscv_1stage.svh"
`include "riscv_2stage.svh"
`include "riscv_3stage.svh"
`include "riscv_4stage.svh"
`include "riscv_5stage.svh"
`include "riscv_6stage.svh"

module cpu_wrapper
#(
	parameter CPU = "none"
	, parameter delay_test_flag = 0
)
(
	input [0:0] clk_i,
	input [0:0] rst_i,

	input [0:0] irq_req_i,
	input [7:0] irq_code_bi,
	output [0:0] irq_ack_o,

	input [0:0] instr_mem_ack,
	input [0:0] instr_mem_resp,
	input [31:0] instr_mem_rdata,
	input [0:0] data_mem_ack,
	input [0:0] data_mem_resp,
	input [31:0] data_mem_rdata,
	output [0:0] instr_mem_req,
	output [0:0] instr_mem_we,
	output [31:0] instr_mem_addr,
	output [31:0] instr_mem_wdata,
	output [3:0] instr_mem_be,
	output [0:0] data_mem_req,
	output [0:0] data_mem_we,
	output [31:0] data_mem_addr,
	output [31:0] data_mem_wdata,
	output [3:0] data_mem_be
);

wire [0:0] cpu_instr_ack;
wire [0:0] cpu_instr_resp;
wire [31:0] cpu_instr_rdata;
wire [0:0] cpu_data_ack;
wire [0:0] cpu_data_resp;
wire [31:0] cpu_data_rdata;
wire [0:0] cpu_instr_req;
wire [0:0] cpu_instr_we;
wire [31:0] cpu_instr_addr;
wire [31:0] cpu_instr_wdata;
wire [3:0] cpu_instr_be;
wire [0:0] cpu_data_req;
wire [0:0] cpu_data_we;
wire [31:0] cpu_data_addr;
wire [31:0] cpu_data_wdata;
wire [3:0] cpu_data_be;

// Processor core
generate
	if (CPU == "riscv_1stage")
		
		begin

		genpmodule_riscv_1stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
		assign cpu_instr_we 	= instr_mem_struct_bus.we;
		assign cpu_instr_addr 	= instr_mem_struct_bus.wdata.addr;
		assign cpu_instr_be		= instr_mem_struct_bus.wdata.be;
		assign cpu_instr_wdata 	= instr_mem_struct_bus.wdata.wdata;

		genpmodule_riscv_1stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
		assign cpu_data_we 		= data_mem_struct_bus.we;
		assign cpu_data_addr 	= data_mem_struct_bus.wdata.addr;
		assign cpu_data_be		= data_mem_struct_bus.wdata.be;
		assign cpu_data_wdata 	= data_mem_struct_bus.wdata.wdata;

		riscv_1stage riscv (
			.clk_i(clk_i)
			, .rst_i(rst_i)

			// interrupt bus
			, .irq_fifo_genfifo_req_i(irq_req_i)
			, .irq_fifo_genfifo_rdata_bi(irq_code_bi)
			, .irq_fifo_genfifo_ack_o(irq_ack_o)
			
			// instr req bus
			, .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr_req)
			, .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
			, .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr_ack)

			// data req bus
			, .genmcopipe_data_mem_req_genfifo_req_o(cpu_data_req)
			, .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
			, .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data_ack)

			// instr resp bus
			, .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr_resp)
			, .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr_rdata)
			// , .genmcopipe_instr_mem_resp_genfifo_ack_o

			// data resp bus
			, .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data_resp)
			, .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data_rdata)
			//, .genmcopipe_data_mem_resp_genfifo_ack_o
		);

		end

	else if (CPU == "riscv_2stage")
	
		begin

		genpmodule_riscv_2stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
		assign cpu_instr_we 	= instr_mem_struct_bus.we;
		assign cpu_instr_addr 	= instr_mem_struct_bus.wdata.addr;
		assign cpu_instr_be		= instr_mem_struct_bus.wdata.be;
		assign cpu_instr_wdata 	= instr_mem_struct_bus.wdata.wdata;

		genpmodule_riscv_2stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
		assign cpu_data_we 		= data_mem_struct_bus.we;
		assign cpu_data_addr 	= data_mem_struct_bus.wdata.addr;
		assign cpu_data_be		= data_mem_struct_bus.wdata.be;
		assign cpu_data_wdata 	= data_mem_struct_bus.wdata.wdata;

		riscv_2stage riscv (
			.clk_i(clk_i)
			, .rst_i(rst_i)

			// interrupt bus
			, .irq_fifo_genfifo_req_i(irq_req_i)
			, .irq_fifo_genfifo_rdata_bi(irq_code_bi)
			, .irq_fifo_genfifo_ack_o(irq_ack_o)
			
			// instr req bus
			, .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr_req)
			, .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
			, .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr_ack)

			// data req bus
			, .genmcopipe_data_mem_req_genfifo_req_o(cpu_data_req)
			, .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
			, .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data_ack)

			// instr resp bus
			, .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr_resp)
			, .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr_rdata)
			// , .genmcopipe_instr_mem_resp_genfifo_ack_o

			// data resp bus
			, .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data_resp)
			, .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data_rdata)
			//, .genmcopipe_data_mem_resp_genfifo_ack_o
		);

		end

	else if (CPU == "riscv_3stage")
	
		begin

		genpmodule_riscv_3stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
		assign cpu_instr_we 	= instr_mem_struct_bus.we;
		assign cpu_instr_addr 	= instr_mem_struct_bus.wdata.addr;
		assign cpu_instr_be		= instr_mem_struct_bus.wdata.be;
		assign cpu_instr_wdata 	= instr_mem_struct_bus.wdata.wdata;

		genpmodule_riscv_3stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
		assign cpu_data_we 		= data_mem_struct_bus.we;
		assign cpu_data_addr 	= data_mem_struct_bus.wdata.addr;
		assign cpu_data_be		= data_mem_struct_bus.wdata.be;
		assign cpu_data_wdata 	= data_mem_struct_bus.wdata.wdata;

		riscv_3stage riscv (
			.clk_i(clk_i)
			, .rst_i(rst_i)

			// interrupt bus
			, .irq_fifo_genfifo_req_i(irq_req_i)
			, .irq_fifo_genfifo_rdata_bi(irq_code_bi)
			, .irq_fifo_genfifo_ack_o(irq_ack_o)
			
			// instr req bus
			, .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr_req)
			, .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
			, .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr_ack)

			// data req bus
			, .genmcopipe_data_mem_req_genfifo_req_o(cpu_data_req)
			, .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
			, .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data_ack)

			// instr resp bus
			, .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr_resp)
			, .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr_rdata)
			// , .genmcopipe_instr_mem_resp_genfifo_ack_o

			// data resp bus
			, .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data_resp)
			, .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data_rdata)
			//, .genmcopipe_data_mem_resp_genfifo_ack_o
		);

		end

	else if (CPU == "riscv_4stage")
	
		begin

		genpmodule_riscv_4stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
		assign cpu_instr_we 	= instr_mem_struct_bus.we;
		assign cpu_instr_addr 	= instr_mem_struct_bus.wdata.addr;
		assign cpu_instr_be		= instr_mem_struct_bus.wdata.be;
		assign cpu_instr_wdata 	= instr_mem_struct_bus.wdata.wdata;

		genpmodule_riscv_4stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
		assign cpu_data_we 		= data_mem_struct_bus.we;
		assign cpu_data_addr 	= data_mem_struct_bus.wdata.addr;
		assign cpu_data_be		= data_mem_struct_bus.wdata.be;
		assign cpu_data_wdata 	= data_mem_struct_bus.wdata.wdata;

		riscv_4stage riscv (
			.clk_i(clk_i)
			, .rst_i(rst_i)

			// interrupt bus
			, .irq_fifo_genfifo_req_i(irq_req_i)
			, .irq_fifo_genfifo_rdata_bi(irq_code_bi)
			, .irq_fifo_genfifo_ack_o(irq_ack_o)
			
			// instr req bus
			, .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr_req)
			, .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
			, .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr_ack)

			// data req bus
			, .genmcopipe_data_mem_req_genfifo_req_o(cpu_data_req)
			, .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
			, .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data_ack)

			// instr resp bus
			, .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr_resp)
			, .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr_rdata)
			// , .genmcopipe_instr_mem_resp_genfifo_ack_o

			// data resp bus
			, .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data_resp)
			, .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data_rdata)
			//, .genmcopipe_data_mem_resp_genfifo_ack_o
		);

		end

	else if (CPU == "riscv_5stage")
	
		begin

		genpmodule_riscv_5stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
		assign cpu_instr_we 	= instr_mem_struct_bus.we;
		assign cpu_instr_addr 	= instr_mem_struct_bus.wdata.addr;
		assign cpu_instr_be		= instr_mem_struct_bus.wdata.be;
		assign cpu_instr_wdata 	= instr_mem_struct_bus.wdata.wdata;

		genpmodule_riscv_5stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
		assign cpu_data_we 		= data_mem_struct_bus.we;
		assign cpu_data_addr 	= data_mem_struct_bus.wdata.addr;
		assign cpu_data_be		= data_mem_struct_bus.wdata.be;
		assign cpu_data_wdata 	= data_mem_struct_bus.wdata.wdata;

		riscv_5stage riscv (
			.clk_i(clk_i)
			, .rst_i(rst_i)

			// interrupt bus
			, .irq_fifo_genfifo_req_i(irq_req_i)
			, .irq_fifo_genfifo_rdata_bi(irq_code_bi)
			, .irq_fifo_genfifo_ack_o(irq_ack_o)
			
			// instr req bus
			, .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr_req)
			, .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
			, .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr_ack)

			// data req bus
			, .genmcopipe_data_mem_req_genfifo_req_o(cpu_data_req)
			, .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
			, .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data_ack)

			// instr resp bus
			, .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr_resp)
			, .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr_rdata)
			// , .genmcopipe_instr_mem_resp_genfifo_ack_o

			// data resp bus
			, .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data_resp)
			, .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data_rdata)
			//, .genmcopipe_data_mem_resp_genfifo_ack_o
		);

		end

	else if (CPU == "riscv_6stage")
	
		begin

		genpmodule_riscv_6stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
		assign cpu_instr_we 	= instr_mem_struct_bus.we;
		assign cpu_instr_addr 	= instr_mem_struct_bus.wdata.addr;
		assign cpu_instr_be		= instr_mem_struct_bus.wdata.be;
		assign cpu_instr_wdata 	= instr_mem_struct_bus.wdata.wdata;

		genpmodule_riscv_6stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
		assign cpu_data_we 		= data_mem_struct_bus.we;
		assign cpu_data_addr 	= data_mem_struct_bus.wdata.addr;
		assign cpu_data_be		= data_mem_struct_bus.wdata.be;
		assign cpu_data_wdata 	= data_mem_struct_bus.wdata.wdata;

		riscv_6stage riscv (
			.clk_i(clk_i)
			, .rst_i(rst_i)

			// interrupt bus
			, .irq_fifo_genfifo_req_i(irq_req_i)
			, .irq_fifo_genfifo_rdata_bi(irq_code_bi)
			, .irq_fifo_genfifo_ack_o(irq_ack_o)
			
			// instr req bus
			, .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr_req)
			, .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
			, .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr_ack)

			// data req bus
			, .genmcopipe_data_mem_req_genfifo_req_o(cpu_data_req)
			, .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
			, .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data_ack)

			// instr resp bus
			, .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr_resp)
			, .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr_rdata)
			// , .genmcopipe_instr_mem_resp_genfifo_ack_o

			// data resp bus
			, .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data_resp)
			, .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data_rdata)
			//, .genmcopipe_data_mem_resp_genfifo_ack_o
		);

		end

	else

		cpu_stub cpu_stub (
			.clk_i(clk_i)
			, .rst_i(rst_i)
			
			, .instr_mem_ack(cpu_instr_ack)
			, .instr_mem_resp(cpu_instr_resp)
			, .instr_mem_rdata(cpu_instr_rdata)
			, .data_mem_ack(cpu_data_ack)
			, .data_mem_resp(cpu_data_resp)
			, .data_mem_rdata(cpu_data_rdata)
			, .instr_mem_req(cpu_instr_req)
			, .instr_mem_we(cpu_instr_we)
			, .instr_mem_addr(cpu_instr_addr)
			, .instr_mem_wdata(cpu_instr_wdata)
			, .instr_mem_be(cpu_instr_be)
			, .data_mem_req(cpu_data_req)
			, .data_mem_we(cpu_data_we)
			, .data_mem_addr(cpu_data_addr)
			, .data_mem_wdata(cpu_data_wdata)
			, .data_mem_be(cpu_data_be)
		);
	
endgenerate


generate 

	if (delay_test_flag == 0)

		begin
		assign instr_mem_req = cpu_instr_req;
		assign cpu_instr_ack = instr_mem_ack;
		assign instr_mem_we = cpu_instr_we;
		assign instr_mem_addr = cpu_instr_addr;
		assign instr_mem_wdata = cpu_instr_wdata;
		assign instr_mem_be = cpu_instr_be;
		assign cpu_instr_resp = instr_mem_resp;
		assign cpu_instr_rdata = instr_mem_rdata;

		assign data_mem_req = cpu_data_req;
		assign cpu_data_ack = data_mem_ack;
		assign data_mem_we = cpu_data_we;
		assign data_mem_addr = cpu_data_addr;
		assign data_mem_wdata = cpu_data_wdata;
		assign data_mem_be = cpu_data_be;
		assign cpu_data_resp = data_mem_resp;
		assign cpu_data_rdata = data_mem_rdata;
		end

	else 

		begin
		test_split_delayer
		#(
			.REQ_RANDOM_RANGE(8),
			.RESP_RANDOM_RANGE(6),
			.RESP_FIFO_POW(4)
		) instr_split_delayer (
			.clk_i(clk_i),
			.rst_i(rst_i),
			
			.host_req		(cpu_instr_req),
			.host_ack		(cpu_instr_ack),
			.host_we		(cpu_instr_we),
			.host_addr		(cpu_instr_addr),
			.host_wdata		(cpu_instr_wdata),
			.host_be		(cpu_instr_be),
			.host_resp		(cpu_instr_resp),
			.host_rdata		(cpu_instr_rdata),
			
			.target_req		(instr_mem_req),
			.target_ack		(instr_mem_ack),
			.target_we		(instr_mem_we),
			.target_addr	(instr_mem_addr),
			.target_wdata	(instr_mem_wdata),
			.target_be		(instr_mem_be),
			.target_resp	(instr_mem_resp),
			.target_rdata	(instr_mem_rdata)
		);

		test_split_delayer
		#(
			.REQ_RANDOM_RANGE(8),
			.RESP_RANDOM_RANGE(6),
			.RESP_FIFO_POW(4)
		) data_split_delayer (
			.clk_i(clk_i),
			.rst_i(rst_i),
			
			.host_req		(cpu_data_req),
			.host_ack		(cpu_data_ack),
			.host_we		(cpu_data_we),
			.host_addr		(cpu_data_addr),
			.host_wdata		(cpu_data_wdata),
			.host_be		(cpu_data_be),
			.host_resp		(cpu_data_resp),
			.host_rdata		(cpu_data_rdata),
			
			.target_req		(data_mem_req),
			.target_ack		(data_mem_ack),
			.target_we		(data_mem_we),
			.target_addr	(data_mem_addr),
			.target_wdata	(data_mem_wdata),
			.target_be		(data_mem_be),
			.target_resp	(data_mem_resp),
			.target_rdata	(data_mem_rdata)
		);
		end

endgenerate


endmodule
