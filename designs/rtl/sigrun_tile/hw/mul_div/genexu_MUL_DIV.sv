`include "genexu_MUL_DIV.svh"

module genexu_MUL_DIV (
	input logic unsigned [0:0] clk_i
	, input logic unsigned [0:0] rst_i
	, output logic unsigned [0:0] stream_resp_bus_genfifo_req_o
	, output resp_struct stream_resp_bus_genfifo_wdata_bo
	, input logic unsigned [0:0] stream_resp_bus_genfifo_ack_i
	, input logic unsigned [0:0] stream_req_bus_genfifo_req_i
	, input req_struct stream_req_bus_genfifo_rdata_bi
	, output logic unsigned [0:0] stream_req_bus_genfifo_ack_o
);

logic mult_req, div_req, mult_resp, div_resp;

logic [1:0] mult_req_buf;
always @(posedge clk_i) if (rst_i) mult_req_buf <= 0; else mult_req_buf <= {mult_req, mult_req_buf[1]};
assign mult_resp = mult_req_buf[0];

logic req_acc;
assign req_acc = stream_req_bus_genfifo_req_i && stream_req_bus_genfifo_ack_o;

logic online;
always @(posedge clk_i)
	begin
	if (rst_i) online <= 0;
	else
		begin
		if (stream_resp_bus_genfifo_req_o) online <= 1'b0;
		if (req_acc) online <= 1'b1;
		end 
	end

assign stream_req_bus_genfifo_ack_o = !online || stream_resp_bus_genfifo_req_o;

logic [31:0] mult_dataout, div_dataout;
always @*
	begin
	if (mult_resp) stream_resp_bus_genfifo_wdata_bo.rd0_wdata = mult_dataout;
	else stream_resp_bus_genfifo_wdata_bo.rd0_wdata = div_dataout;
	end
assign stream_resp_bus_genfifo_req_o = mult_resp | div_resp;

always @(posedge clk_i)
	begin
	if (req_acc)
		begin
		stream_resp_bus_genfifo_wdata_bo.trx_id <= stream_req_bus_genfifo_rdata_bi.trx_id;
		stream_resp_bus_genfifo_wdata_bo.rd0_req <= stream_req_bus_genfifo_rdata_bi.rd0_req;
		stream_resp_bus_genfifo_wdata_bo.rd0_tag <= stream_req_bus_genfifo_rdata_bi.rd0_tag;
		end
	end

logic inst_mul, inst_mulh, inst_mulhsu, inst_mulhu;
logic inst_div_w, inst_divu_w, inst_rem_w, inst_remu_w;

always @*
	begin
	mult_req = 1'b0;
	div_req = 1'b0;

	inst_mul = 1'b0;
	inst_mulh = 1'b0;
	inst_mulhsu = 1'b0;
	inst_mulhu = 1'b0;
	inst_div_w = 1'b0;
	inst_divu_w = 1'b0;
	inst_rem_w = 1'b0;
	inst_remu_w = 1'b0;

	if (req_acc)
		begin
		case (stream_req_bus_genfifo_rdata_bi.exu_opcode)
			0: begin mult_req = 1'b1; inst_mul = 1'b1; end
			1: begin mult_req = 1'b1; inst_mulh = 1'b1; end
			2: begin mult_req = 1'b1; inst_mulhsu = 1'b1; end
			3: begin mult_req = 1'b1; inst_mulhu = 1'b1; end
			4: begin div_req = 1'b1; inst_div_w = 1'b1; end
			5: begin div_req = 1'b1; inst_divu_w = 1'b1; end
			6: begin div_req = 1'b1; inst_rem_w = 1'b1; end
			7: begin div_req = 1'b1; inst_remu_w = 1'b1; end
		endcase
		end
	end

riscv_multiplier riscv_multiplier
(
    // Inputs
     .clk_i(clk_i)
    ,.rst_i(rst_i)
    ,.opcode_valid_i(mult_req)
    ,.inst_mul(inst_mul)
    ,.inst_mulh(inst_mulh)
    ,.inst_mulhsu(inst_mulhsu)
    ,.inst_mulhu(inst_mulhu)

    ,.opcode_ra_operand_i(stream_req_bus_genfifo_rdata_bi.src0_data)
    ,.opcode_rb_operand_i(stream_req_bus_genfifo_rdata_bi.src1_data)
    ,.hold_i(1'b0)

    // Outputs
    ,.writeback_value_o(mult_dataout)
);

riscv_divider riscv_divider
(
    // Inputs
     .clk_i(clk_i)
    ,.rst_i(rst_i)
    ,.opcode_valid_i(div_req)
    ,.inst_div_w(inst_div_w)
    ,.inst_divu_w(inst_divu_w)
    ,.inst_rem_w(inst_rem_w)
    ,.inst_remu_w(inst_remu_w)

    ,.opcode_ra_operand_i(stream_req_bus_genfifo_rdata_bi.src0_data)
    ,.opcode_rb_operand_i(stream_req_bus_genfifo_rdata_bi.src1_data)

    // Outputs
    ,.writeback_valid_o(div_resp)
    ,.writeback_value_o(div_dataout)
);


endmodule
