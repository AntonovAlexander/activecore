module ram
  #(
    parameter dat_width=32, adr_width=32, mem_size=1024
  )
  (
    input [dat_width-1:0]      dat_i,
    input [adr_width-1:0]      adr_i,
    input          we_i,
    output reg [dat_width-1:0] dat_o,
    input          clk
  ); 

   reg [dat_width-1:0] ram [0:mem_size - 1] /* synthesis ram_style = no_rw_check */;
   
   always @ (posedge clk)
     begin 
	     dat_o <= ram[adr_i];
	     if (we_i)
	       ram[adr_i] <= dat_i;
     end 

endmodule // ram
