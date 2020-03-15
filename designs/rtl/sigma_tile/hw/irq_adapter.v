/*
 * int_adapter.v
 *
 *  Created on: 16.10.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module irq_adapter
(
	input clk_i
	, input rst_i

	// external interface
	, input irq_debounced_i

	// msi interface
	, input msi_req_i
	, input [7:0] msi_code_bi

	// cpu interface
	, output reg irq_req_o
	, output reg [7:0] irq_code_bo
	, input irq_ack_i
);

reg irq_buf, irq_posedge;
always @*
	begin
	irq_posedge = 1'b0;
	irq_posedge = msi_req_i;
	if (!irq_buf && irq_debounced_i) irq_posedge = 1'b1;
	end

always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		irq_buf <= 1'b0;
		irq_req_o <= 1'b0;
		irq_code_bo <= 0;
		end
	else
		begin
		irq_buf <= irq_debounced_i;
		if (irq_ack_i)
			begin
			irq_req_o <= 1'b0;
			irq_code_bo <= 0;
			end
		if (irq_posedge)
			begin
			irq_req_o <= 1'b1;
			irq_code_bo <= 8'h03;
			end
		end
	end

endmodule
