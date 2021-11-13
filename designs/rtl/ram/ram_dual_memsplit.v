module ram_dual_memsplit
  #(
    parameter init_type="hex", init_data="data.hex", dat_width=32, adr_width=32, mem_size=1024, P0_FRAC="NO", P1_FRAC="NO"
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

  
  reg p0_wb, p0_wb_next;
  reg p1_wb, p1_wb_next;
  
  always @*
    begin
    p0_wb_next = 1'b0;
    if ((P0_FRAC=="YES") && bus0_ack_o && bus0_we_i && (bus0_be_bi != 4'hf)) p0_wb_next = 1'b1;
    end

  always @*
    begin
    p1_wb_next = 1'b0;
    if ((P1_FRAC=="YES") && bus1_ack_o && bus1_we_i && (bus1_be_bi != 4'hf)) p1_wb_next = 1'b1;
    end
  
  always @(posedge clk_i) p0_wb <= rst_i ? 1'b0 : p0_wb_next;
  always @(posedge clk_i) p1_wb <= rst_i ? 1'b0 : p1_wb_next;
  
  reg [31:0] bus0_addr, bus0_addr_buf;
  reg [3:0]  bus0_be_buf;
  reg bus0_we;
  reg [31:0] bus0_wdata, bus0_wdata_buf;
  wire [31:0] bus0_rdata;
  
  reg [31:0] bus1_addr, bus1_addr_buf;
  reg [3:0]  bus1_be_buf;
  reg [31:0] bus1_wdata, bus1_wdata_buf;
  reg bus1_we;
  wire [31:0] bus1_rdata;
  
  always @*
    begin

    bus0_addr = bus0_addr_bi;
    bus0_we = bus0_req_i & bus0_we_i;
    bus0_wdata = bus0_wdata_bi;
    
    if (P0_FRAC=="YES")
        begin
        if (p0_wb_next) bus0_we = 1'b0;
        else if (p0_wb)
            begin
            bus0_addr = bus0_addr_buf;
            bus0_we = 1'b1;
            bus0_wdata = bus0_rdata;

            case (bus0_addr_buf[1:0])

                2'd0 :
                    begin
                    if (bus0_be_buf[0]) bus0_wdata[7:0]   = bus0_wdata_buf[7:0];
                    if (bus0_be_buf[1]) bus0_wdata[15:8]  = bus0_wdata_buf[15:8];
                    if (bus0_be_buf[2]) bus0_wdata[23:16] = bus0_wdata_buf[23:16];
                    if (bus0_be_buf[3]) bus0_wdata[31:24] = bus0_wdata_buf[31:24];
                    end

                2'd1:
                    begin
                    if (bus0_be_buf[0]) bus0_wdata[15:8]  = bus0_wdata_buf[7:0];
                    if (bus0_be_buf[1]) bus0_wdata[23:16] = bus0_wdata_buf[15:8];
                    if (bus0_be_buf[2]) bus0_wdata[31:24] = bus0_wdata_buf[23:16];
                    end

                2'd2:
                    begin
                    if (bus0_be_buf[0]) bus0_wdata[23:16] = bus0_wdata_buf[7:0];
                    if (bus0_be_buf[1]) bus0_wdata[31:24] = bus0_wdata_buf[15:8];
                    end

                2'd3:
                    begin
                    if (bus0_be_buf[0]) bus0_wdata[31:24] = bus0_wdata_buf[7:0];
                    end

            endcase
                
            end
        end
    end
  
  always @*
    begin

    bus1_addr = bus1_addr_bi;
    bus1_we = bus1_req_i & bus1_we_i;
    bus1_wdata = bus1_wdata_bi;
    
    if (P1_FRAC=="YES")
        begin
        if (p1_wb_next) bus1_we = 1'b0;
        else if (p1_wb)
            begin
            bus1_addr = bus1_addr_buf;
            bus1_we = 1'b1;
            bus1_wdata = bus1_rdata;
            
            case (bus1_addr_buf[1:0])

                2'd0:
                    begin
                    if (bus1_be_buf[0]) bus1_wdata[7:0]   = bus1_wdata_buf[7:0];
                    if (bus1_be_buf[1]) bus1_wdata[15:8]  = bus1_wdata_buf[15:8];
                    if (bus1_be_buf[2]) bus1_wdata[23:16] = bus1_wdata_buf[23:16];
                    if (bus1_be_buf[3]) bus1_wdata[31:24] = bus1_wdata_buf[31:24];
                    end

                2'd1:
                    begin
                    if (bus1_be_buf[0]) bus1_wdata[15:8]  = bus1_wdata_buf[7:0];
                    if (bus1_be_buf[1]) bus1_wdata[23:16] = bus1_wdata_buf[15:8];
                    if (bus1_be_buf[2]) bus1_wdata[31:24] = bus1_wdata_buf[23:16];
                    end

                2'd2:
                    begin
                    if (bus1_be_buf[0]) bus1_wdata[23:16] = bus1_wdata_buf[7:0];
                    if (bus1_be_buf[1]) bus1_wdata[31:24] = bus1_wdata_buf[15:8];
                    end

                2'd3:
                    begin
                    if (bus1_be_buf[0]) bus1_wdata[31:24] = bus1_wdata_buf[7:0];
                    end

            endcase
            
            end
        end
    end
  
  assign bus0_ack_o = bus0_req_i && !p0_wb;
  assign bus1_ack_o = bus1_req_i && !p1_wb;
  
  always @*
    begin
    bus0_rdata_bo = bus0_rdata;
    if (P0_FRAC == "YES")
        begin
        if (bus0_addr_buf[1:0] == 2'd1) bus0_rdata_bo = bus0_rdata >> 8;
        if (bus0_addr_buf[1:0] == 2'd2) bus0_rdata_bo = bus0_rdata >> 16;
        if (bus0_addr_buf[1:0] == 2'd3) bus0_rdata_bo = bus0_rdata >> 24;
        end
    end

  always @*
    begin
    bus1_rdata_bo = bus1_rdata;
    if (P1_FRAC == "YES")
        begin
        if (bus1_addr_buf[1:0] == 2'd1) bus1_rdata_bo = bus1_rdata >> 8;
        if (bus1_addr_buf[1:0] == 2'd2) bus1_rdata_bo = bus1_rdata >> 16;
        if (bus1_addr_buf[1:0] == 2'd3) bus1_rdata_bo = bus1_rdata >> 24;
        end
    end
  
  always @(posedge clk_i)
    begin
    if (rst_i)
        begin
        bus0_resp_o <= 1'b0;
        end
    else
        begin
        bus0_resp_o <= 1'b0;
        if (bus0_ack_o)
          begin
          bus0_addr_buf <= bus0_addr_bi;
          bus0_be_buf <= bus0_be_bi;
          bus0_wdata_buf <= bus0_wdata_bi;
          if (!bus0_we_i) bus0_resp_o <= 1'b1;
          end
        end
    end

  always @(posedge clk_i)
    begin
    if (rst_i)
        begin
        bus1_resp_o <= 1'b0;
        end
    else
        begin
        bus1_resp_o <= 1'b0;
        if (bus1_ack_o)
          begin
          bus1_addr_buf <= bus1_addr_bi;
          bus1_be_buf <= bus1_be_bi;
          bus1_wdata_buf <= bus1_wdata_bi;
          if (!bus1_we_i) bus1_resp_o <= 1'b1;
          end
        end
    end
  
  ram_dual
  #(
	.init_type(init_type),
	.init_data(init_data),
	.dat_width(dat_width),
	.adr_width(adr_width),
	.mem_size(mem_size)
  ) ram_dual (
	
	.clk(clk_i)
	
	, .dat0_i(bus0_wdata)
    , .adr0_i(bus0_addr[31:2])
    , .we0_i(bus0_we)
    , .dat0_o(bus0_rdata)

    , .dat1_i(bus1_wdata)
    , .adr1_i(bus1_addr[31:2])
    , .we1_i(bus1_we)
    , .dat1_o(bus1_rdata)
  );


endmodule
