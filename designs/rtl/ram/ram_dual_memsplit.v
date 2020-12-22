module ram_dual_memsplit
  #(
    parameter mem_init="YES", mem_type="hex", mem_data="data.hex", dat_width=32, adr_width=32, mem_size=1024, P0_RD_ALIGHED="YES", P1_RD_ALIGHED="YES"
  )
  (
	input clk_i,
	input rst_i,
	
	input [0:0] bus0_req_i,
	input [0:0] bus0_we_i,
	input [31:0] bus0_addr_bi,
	input [3:0] bus0_be_bi,
	input [31:0] bus0_wdata_bi,
	output [0:0] bus0_ack_o,
	
	output reg [0:0] bus0_resp_o,
	output reg [31:0] bus0_rdata_bo,
	
	input [0:0] bus1_req_i,
	input [0:0] bus1_we_i,
	input [31:0] bus1_addr_bi,
	input [3:0] bus1_be_bi,
	input [31:0] bus1_wdata_bi,
	output [0:0] bus1_ack_o,
	
	output reg [0:0] bus1_resp_o,
	output reg [31:0] bus1_rdata_bo
  ); 

  
  assign bus0_ack_o = bus0_req_i;
  assign bus1_ack_o = bus1_req_i;
  
  reg [1:0] bus0_addr_lsbs;
  reg [3:0] bus0_be;
  wire [31:0] bus0_rdata;
  
  reg [1:0] bus1_addr_lsbs;
  reg [3:0] bus1_be;
  wire [31:0] bus1_rdata;
  
  always @*
    begin
    
    bus0_rdata_bo = bus0_rdata;
    if (P0_RD_ALIGHED == "NO")
        begin
        if (bus0_addr_lsbs == 2'd1) bus0_rdata_bo = bus0_rdata >> 8;
        if (bus0_addr_lsbs == 2'd2) bus0_rdata_bo = bus0_rdata >> 16;
        if (bus0_addr_lsbs == 2'd3) bus0_rdata_bo = bus0_rdata >> 24;
        end
    
    bus1_rdata_bo = bus1_rdata;
    if (P1_RD_ALIGHED == "NO")
        begin
        if (bus1_addr_lsbs == 2'd1) bus1_rdata_bo = bus1_rdata >> 8;
        if (bus1_addr_lsbs == 2'd2) bus1_rdata_bo = bus1_rdata >> 16;
        if (bus1_addr_lsbs == 2'd3) bus1_rdata_bo = bus1_rdata >> 24;
        end
    
    end
  
  always @(posedge clk_i)
	begin
	if (rst_i)
		begin
		bus0_resp_o <= 1'b0;
		bus1_resp_o <= 1'b0;
		end
	else
		begin
		bus0_resp_o <= 1'b0;
		bus1_resp_o <= 1'b0;
		if ((bus0_req_i == 1'b1) && (bus0_we_i == 1'b0))
		  begin
		  bus0_resp_o <= 1'b1;
		  bus0_addr_lsbs <= bus0_addr_bi[1:0];
		  bus0_be <= bus0_be_bi;
		  end
		if ((bus1_req_i == 1'b1) && (bus1_we_i == 1'b0))
		  begin
		  bus1_resp_o <= 1'b1;
		  bus1_addr_lsbs <= bus1_addr_bi[1:0];
		  bus1_be <= bus1_be_bi;
		  end
		end
	end
  
  ram_dual
  #(
	.mem_init(mem_init),
	.mem_type(mem_type),
	.mem_data(mem_data),
	.dat_width(dat_width),
	.adr_width(adr_width),
	.mem_size(mem_size)
  ) ram_dual (
	
	.clk(clk_i)
	
	, .dat0_i(bus0_wdata_bi)
    , .adr0_i(bus0_addr_bi[31:2])
    , .we0_i(bus0_req_i & bus0_we_i)
    , .dat0_o(bus0_rdata)

    , .dat1_i(bus1_wdata_bi)
    , .adr1_i(bus1_addr_bi[31:2])
    , .we1_i(bus1_req_i & bus1_we_i)
    , .dat1_o(bus1_rdata)
  );


endmodule
