module reset_sync
#(
	parameter SYNC_STAGES = 4
)
(
	input clk_i, arst_i,
	output srst_o
);

reg [SYNC_STAGES-1:0] reset_syncbuf;
assign srst_o = reset_syncbuf[0];

always @(posedge clk_i, posedge arst_i)
	begin
	if (arst_i) reset_syncbuf <= {SYNC_STAGES{1'b1}};
	else reset_syncbuf <= {1'b0, reset_syncbuf[SYNC_STAGES-1:1]};
	end

endmodule
