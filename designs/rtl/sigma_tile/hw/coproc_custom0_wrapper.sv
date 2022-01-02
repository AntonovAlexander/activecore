`include "coproc_if.svh"

module coproc_custom0_wrapper (
	input logic unsigned [0:0] clk_i
	, input logic unsigned [0:0] rst_i
	, output logic unsigned [0:0] stream_resp_bus_genfifo_req_o
	, output resp_struct stream_resp_bus_genfifo_wdata_bo
	, input logic unsigned [0:0] stream_resp_bus_genfifo_ack_i
	, input logic unsigned [0:0] stream_req_bus_genfifo_req_i
	, input req_struct stream_req_bus_genfifo_rdata_bi
	, output logic unsigned [0:0] stream_req_bus_genfifo_ack_o
);

assign stream_req_bus_genfifo_ack_o = stream_req_bus_genfifo_req_i;
//assign stream_resp_bus_genfifo_req_o = 1;
//assign stream_resp_bus_genfifo_wdata_bo = 0;

logic unsigned [31:0] cur_index, max_index, max_val;
assign stream_resp_bus_genfifo_wdata_bo = max_index;

always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		stream_resp_bus_genfifo_req_o <= 1'b0;
		cur_index <= 0;
		max_index <= 0;
		max_val <= 0;
		end
	else
		begin
		stream_resp_bus_genfifo_req_o <= 1'b0;
		if (stream_req_bus_genfifo_req_i)
			begin
			if (stream_req_bus_genfifo_rdata_bi.src0_data > max_val)
				begin
				max_index <= cur_index;
				max_val <= stream_req_bus_genfifo_rdata_bi.src0_data;
				end
			if ((stream_req_bus_genfifo_rdata_bi.src1_data > max_val) && (stream_req_bus_genfifo_rdata_bi.src1_data > stream_req_bus_genfifo_rdata_bi.src0_data))
				begin
				max_index <= cur_index + 1;
				max_val <= stream_req_bus_genfifo_rdata_bi.src1_data;
				end
			stream_resp_bus_genfifo_req_o <= 1'b1;
			cur_index <= cur_index + 2;
			end
		end
	end

endmodule
