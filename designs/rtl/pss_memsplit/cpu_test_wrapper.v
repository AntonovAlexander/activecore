module cpu_test_wrapper
#(
	parameter CPU = "none"
)
(

	input [0:0] clk_i,
	input [0:0] rst_i,
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

// Processor core
generate
	if (CPU == "dlx")

		dlx dlx (
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

	else if (CPU == "riscv_1stage")
	
		riscv_1stage riscv (
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

	else if (CPU == "riscv_2stage")
	
		riscv_2stage riscv (
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

	else if (CPU == "riscv_3stage")
	
		riscv_3stage riscv (
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

	else if (CPU == "riscv_4stage")
	
		riscv_4stage riscv (
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

	else if (CPU == "riscv_5stage")
	
		riscv_5stage riscv (
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

	else if (CPU == "riscv_6stage")
	
		riscv_6stage riscv (
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

endmodule
