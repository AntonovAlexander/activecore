module reset_cntrl
(
	input clk_i, arst_i,
	output reg srst_o
);

reg [15:0] reset_counter;

always @(posedge clk_i, posedge arst_i)
	begin
	if (arst_i)
		begin
		reset_counter <= 16'h0;
		srst_o <= 1'b1;
		end
	else
		begin
		srst_o <= 1'b1;
		reset_counter <= {1'b1, reset_counter[15:1]};
		if (reset_counter == 16'hFFFF) srst_o <= 1'b0;
		end
	end


endmodule
