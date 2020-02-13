module ram_dual
  #(
    parameter mem_init="YES", mem_data="data.hex", dat_width=32, adr_width=32, mem_size=1024
  )
  (
    input [dat_width-1:0]      dat0_i,
    input [adr_width-1:0]      adr0_i,
    input          we0_i,
    output reg [dat_width-1:0] dat0_o,

    input [dat_width-1:0]      dat1_i,
    input [adr_width-1:0]      adr1_i,
    input          we1_i,
    output reg [dat_width-1:0] dat1_o,

    input          clk
  ); 

//(* ram_style="block" *)
reg [dat_width-1:0] ram [0:mem_size - 1] ;
   
always @ (posedge clk)
    begin
    dat0_o <= ram[adr0_i];
    if (we0_i)
        ram[adr0_i] <= dat0_i;
    end

always @ (posedge clk)
    begin
    dat1_o <= ram[adr1_i];
    if (we1_i)
        ram[adr1_i] <= dat1_i;
    end

initial
begin
  if (mem_init == "YES") $readmemh(mem_data, ram, 0) ;
end


endmodule // ram
