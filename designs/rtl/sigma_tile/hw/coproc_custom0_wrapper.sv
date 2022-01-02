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
assign stream_resp_bus_genfifo_req_o = 1;
assign stream_resp_bus_genfifo_wdata_bo = 0;

endmodule
