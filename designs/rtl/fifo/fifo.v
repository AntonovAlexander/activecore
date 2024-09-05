//////////////////////////////////////////////////////////////////////////////////
// Create Date:    	13:11:50 05/24/2012 
// Module Name:    	fifo 
// Description: 	fifo buffer
// Sourced from: 	Pong P. Chu, "FPGA Prototyping by Verilog Examples" (modified)
//////////////////////////////////////////////////////////////////////////////////

module fifo
	#(
	parameter B=8,
	W=4
	)
(
input wire clk_i, rst_i,
input wire rd_i, wr_i,
input wire [B-1:0] w_data_bi,
output wire empty_o, full_o,
output wire [B-1:0] r_data_bo
);
// signal declaration
reg [B-1:0] array_reg [2**W-1:0]; // register array
reg [W-1:0] w_ptr_reg, w_ptr_next, w_ptr_succ;
reg [W-1:0] r_ptr_reg, r_ptr_next, r_ptr_succ;
reg full_reg, empty_reg, full_next, empty_next;
wire wr_en, rd_en;

// body
// register file write operation
always @(posedge clk_i)
	if (wr_en)
		array_reg [w_ptr_reg] <= w_data_bi;
// register file read operation
assign r_data_bo = array_reg[r_ptr_reg];
// write enabled only when FIFO is not full
assign wr_en = wr_i & ~full_reg;
assign rd_en = rd_i & ~empty_reg;
//
// fifo control logic
// register for read and write pointers
always @(posedge clk_i)
if (rst_i)
	begin
	w_ptr_reg <= 0;
	r_ptr_reg <= 0;
	full_reg  <= 1'b0;
	empty_reg <= 1'b1;
	end
else
	begin
	w_ptr_reg <= w_ptr_next;
	r_ptr_reg <= r_ptr_next;
	full_reg  <= full_next;
	empty_reg <= empty_next;
	end
// next-state logic for read and write pointers
always @*
	begin
	// successive pointer values
	w_ptr_succ = w_ptr_reg + 1;
	r_ptr_succ = r_ptr_reg + 1;
	// default: keep old values
	w_ptr_next = w_ptr_reg;
	r_ptr_next = r_ptr_reg;
	full_next  = full_reg;
	empty_next = empty_reg;
	case ({wr_en, rd_en})
		// 2'b00: no op
		
		2'b01: // read
			begin
			r_ptr_next = r_ptr_succ;
			full_next = 1'b0;
			if (r_ptr_succ==w_ptr_reg)
				empty_next = 1'b1;
			end
		
		2'b10: // write
			begin
			w_ptr_next = w_ptr_succ ;
			empty_next = 1'b0;
			if (w_ptr_succ==r_ptr_reg)
				full_next = 1'b1;
			end
		
		2'b11: // write and read
			begin
			w_ptr_next = w_ptr_succ;
			r_ptr_next = r_ptr_succ;
			end
	endcase
end
// output
assign full_o = full_reg;
assign empty_o = empty_reg;
endmodule
