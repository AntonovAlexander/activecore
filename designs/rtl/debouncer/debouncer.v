/*
 * debouncer.v
 *
 *  Created on: 16.10.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module debouncer 
#(
	parameter FACTOR_POW = 20
)
(
	input [0:0] clk_i,
	input [0:0] rst_i,
	input [0:0] in,
	output reg [0:0] out
);

reg in_buf0, in_buf1;
reg [FACTOR_POW-1:0] counter;

always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		in_buf0 <= 1'b0;
		in_buf1 <= 1'b0;
		counter <= 0;
		out <= 1'b0;
		end
	else
		begin
		in_buf0 <= in;
		in_buf1 <= in_buf0;
		if (in_buf1)
			begin
			if (counter != {FACTOR_POW{1'b1}}) counter <= counter + 1;
			else out <= 1'b1;
			end
		else
			begin
			if (counter != 0) counter <= counter - 1;
			else out <= 1'b0;
			end
		end
	end

endmodule
