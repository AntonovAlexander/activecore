module ram_dual_memsplit_2banks
  #(
    parameter init_type="hex", init_data="data.hex", dat_width=32, adr_width=32, mem_size=1024, P0_FRAC="NO", P1_FRAC="NO"
  )
  (
	input clk_i,
	input rst_i,
	
	input [0:0] bus0_bank0_req_i,
	input [0:0] bus0_bank0_we_i,
	input [31:0] bus0_bank0_addr_bi,
	input [3:0] bus0_bank0_be_bi,
	input [31:0] bus0_bank0_wdata_bi,
	output reg [0:0] bus0_bank0_ack_o,
	output reg [0:0] bus0_bank0_resp_o,
	output reg [31:0] bus0_bank0_rdata_bo,

	input [0:0] bus0_bank1_req_i,
	input [0:0] bus0_bank1_we_i,
	input [31:0] bus0_bank1_addr_bi,
	input [3:0] bus0_bank1_be_bi,
	input [31:0] bus0_bank1_wdata_bi,
	output reg [0:0] bus0_bank1_ack_o,
	output reg [0:0] bus0_bank1_resp_o,
	output reg [31:0] bus0_bank1_rdata_bo,
	
	input [0:0] bus1_bank0_req_i,
	input [0:0] bus1_bank0_we_i,
	input [31:0] bus1_bank0_addr_bi,
	input [3:0] bus1_bank0_be_bi,
	input [31:0] bus1_bank0_wdata_bi,
	output reg [0:0] bus1_bank0_ack_o,
	output reg [0:0] bus1_bank0_resp_o,
	output reg [31:0] bus1_bank0_rdata_bo,

	input [0:0] bus1_bank1_req_i,
	input [0:0] bus1_bank1_we_i,
	input [31:0] bus1_bank1_addr_bi,
	input [3:0] bus1_bank1_be_bi,
	input [31:0] bus1_bank1_wdata_bi,
	output reg [0:0] bus1_bank1_ack_o,
	output reg [0:0] bus1_bank1_resp_o,
	output reg [31:0] bus1_bank1_rdata_bo
  ); 

  
  reg bus0_bank0_wb, bus0_bank0_wb_next;
  reg bus0_bank1_wb, bus0_bank1_wb_next;
  reg bus1_bank0_wb, bus1_bank0_wb_next;
  reg bus1_bank1_wb, bus1_bank1_wb_next;
  
  always @(posedge clk_i) bus0_bank0_wb <= rst_i ? 1'b0 : bus0_bank0_wb_next;
  always @(posedge clk_i) bus0_bank1_wb <= rst_i ? 1'b0 : bus0_bank1_wb_next;
  always @(posedge clk_i) bus1_bank0_wb <= rst_i ? 1'b0 : bus1_bank0_wb_next;
  always @(posedge clk_i) bus1_bank1_wb <= rst_i ? 1'b0 : bus1_bank1_wb_next;

  
  reg [31:0] bus0_bank0_addr, bus0_bank0_addr_buf;
  reg [3:0]  bus0_bank0_be, bus0_bank0_be_buf;
  reg bus0_bank0_we;
  reg [31:0] bus0_bank0_wdata, bus0_bank0_wdata_buf;
  reg bus0_bank0_resp, bus0_bank0_resp_next;
  wire [31:0] bus0_bank0_rdata;

  reg [31:0] bus0_bank1_addr, bus0_bank1_addr_buf;
  reg [3:0]  bus0_bank1_be, bus0_bank1_be_buf;
  reg bus0_bank1_we;
  reg [31:0] bus0_bank1_wdata, bus0_bank1_wdata_buf;
  reg bus0_bank1_resp, bus0_bank1_resp_next;
  wire [31:0] bus0_bank1_rdata;
  
  reg [31:0] bus1_bank0_addr, bus1_bank0_addr_buf;
  reg [3:0]  bus1_bank0_be, bus1_bank0_be_buf;
  reg bus1_bank0_we;
  reg [31:0] bus1_bank0_wdata, bus1_bank0_wdata_buf;
  reg bus1_bank0_resp, bus1_bank0_resp_next;
  wire [31:0] bus1_bank0_rdata;

  reg [31:0] bus1_bank1_addr, bus1_bank1_addr_buf;
  reg [3:0]  bus1_bank1_be, bus1_bank1_be_buf;
  reg bus1_bank1_we;
  reg [31:0] bus1_bank1_wdata, bus1_bank1_wdata_buf;
  reg bus1_bank1_resp, bus1_bank1_resp_next;
  wire [31:0] bus1_bank1_rdata;

  reg bus0_rdata_xchg, bus0_rdata_xchg_next;
  reg bus1_rdata_xchg, bus1_rdata_xchg_next;

  always @(posedge clk_i) bus0_bank0_resp <= rst_i ? 0 : bus0_bank0_resp_next;
  always @(posedge clk_i) bus0_bank1_resp <= rst_i ? 0 : bus0_bank1_resp_next;
  always @(posedge clk_i) bus1_bank0_resp <= rst_i ? 0 : bus1_bank0_resp_next;
  always @(posedge clk_i) bus1_bank1_resp <= rst_i ? 0 : bus1_bank1_resp_next;

  always @(posedge clk_i) bus0_rdata_xchg <= rst_i ? 0 : bus0_rdata_xchg_next;
  always @(posedge clk_i) bus1_rdata_xchg <= rst_i ? 0 : bus1_rdata_xchg_next;

  wire bus0_bank0_we_mask;
  wire bus0_bank1_we_mask;
  wire bus1_bank0_we_mask;
  wire bus1_bank1_we_mask;

  assign bus0_bank0_we_mask = bus0_bank0_req_i & bus0_bank0_we_i;
  assign bus0_bank1_we_mask = bus0_bank1_req_i & bus0_bank1_we_i;
  assign bus1_bank0_we_mask = bus1_bank0_req_i & bus1_bank0_we_i;
  assign bus1_bank1_we_mask = bus1_bank1_req_i & bus1_bank1_we_i;

  wire bus0_bank0_we_fullword;
  wire bus0_bank1_we_fullword;
  wire bus1_bank0_we_fullword;
  wire bus1_bank1_we_fullword;

  assign bus0_bank0_we_fullword = bus0_bank0_we_mask & (bus0_bank0_be_bi == 4'hf);
  assign bus0_bank1_we_fullword = bus0_bank1_we_mask & (bus0_bank1_be_bi == 4'hf);
  assign bus1_bank0_we_fullword = bus1_bank0_we_mask & (bus1_bank0_be_bi == 4'hf);
  assign bus1_bank1_we_fullword = bus1_bank1_we_mask & (bus1_bank1_be_bi == 4'hf);

  wire bus0_bank0_we_nfullword;
  wire bus0_bank1_we_nfullword;
  wire bus1_bank0_we_nfullword;
  wire bus1_bank1_we_nfullword;

  assign bus0_bank0_we_nfullword = bus0_bank0_we_mask & (bus0_bank0_be_bi != 4'hf);
  assign bus0_bank1_we_nfullword = bus0_bank1_we_mask & (bus0_bank1_be_bi != 4'hf);
  assign bus1_bank0_we_nfullword = bus1_bank0_we_mask & (bus1_bank0_be_bi != 4'hf);
  assign bus1_bank1_we_nfullword = bus1_bank1_we_mask & (bus1_bank1_be_bi != 4'hf);

  always @*
    begin

    bus0_bank0_wb_next = 1'b0;
    bus0_bank1_wb_next = 1'b0;

    bus0_bank0_addr  = bus0_bank0_addr_bi;
    bus0_bank0_we    = bus0_bank0_we_fullword;
    bus0_bank0_be    = bus0_bank0_be_bi;
    bus0_bank0_wdata = bus0_bank0_wdata_bi;
    bus0_bank0_ack_o = bus0_bank0_req_i & !bus0_bank0_wb;
    if (P0_FRAC=="YES") bus0_bank0_wb_next = bus0_bank0_we_nfullword & !bus0_bank0_wb;
    bus0_bank0_resp_next = bus0_bank0_req_i & !bus0_bank0_we;

    bus0_bank1_addr  = bus0_bank1_addr_bi;
    bus0_bank1_we    = bus0_bank1_we_fullword;
    bus0_bank1_be    = bus0_bank1_be_bi;
    bus0_bank1_wdata = bus0_bank1_wdata_bi;
    bus0_bank1_ack_o = bus0_bank1_req_i & !bus0_bank1_wb;
    if (P0_FRAC=="YES") bus0_bank1_wb_next = bus0_bank1_we_nfullword & !bus0_bank1_wb;
    bus0_bank1_resp_next = bus0_bank1_req_i & !bus0_bank1_we;

    bus0_rdata_xchg_next = 1'b0;

    if (bus0_bank0_req_i)
      begin
      if (bus0_bank0_addr_bi[2])
        begin
        bus0_bank1_addr  = bus0_bank0_addr_bi;
        bus0_bank1_we    = bus0_bank0_we_fullword;
        bus0_bank1_be    = bus0_bank0_be_bi;
        bus0_bank1_wdata = bus0_bank0_wdata_bi;
        bus0_bank0_we = 1'b0;
        bus0_bank0_ack_o = bus0_bank0_req_i & !bus0_bank1_wb;
        if (P0_FRAC=="YES") bus0_bank0_wb_next = 1'b0;
        if (P0_FRAC=="YES") bus0_bank1_wb_next = bus0_bank0_we_nfullword & !bus0_bank1_wb;
        bus0_bank0_resp_next = 1'b0;
        bus0_bank1_resp_next = bus0_bank0_req_i & !bus0_bank0_we;
        bus0_rdata_xchg_next = 1'b1;
        end

      end

    if (bus0_bank1_req_i)
      begin
      if (bus0_bank0_req_i && (bus0_bank0_addr_bi[2] == bus0_bank1_addr_bi[2]))
        begin
        bus0_bank1_ack_o = 1'b0;
        if (!bus0_bank1_addr_bi[2]) bus0_bank1_resp_next = 1'b0;
        end
      else if (!bus0_bank1_addr_bi[2])
        begin
        bus0_bank0_addr  = bus0_bank1_addr_bi;
        bus0_bank0_we    = bus0_bank1_we_fullword;
        bus0_bank0_be    = bus0_bank1_be_bi;
        bus0_bank0_wdata = bus0_bank1_wdata_bi;
        bus0_bank1_ack_o = bus0_bank1_req_i & !bus0_bank0_wb;
        if (P0_FRAC=="YES") bus0_bank0_wb_next = bus0_bank1_we_nfullword & !bus0_bank0_wb;
        bus0_bank0_resp_next = bus0_bank1_req_i & !bus0_bank1_we;
        if (!bus0_bank0_req_i) bus0_bank1_resp_next = 1'b0;
        bus0_rdata_xchg_next = 1'b1;
        end
      end

    if (P0_FRAC=="YES")
        begin
        
        if (bus0_bank0_wb)
	        begin
	        bus0_bank0_addr = bus0_bank0_addr_buf;
	        bus0_bank0_we = 1'b1;
	        bus0_bank0_wdata = bus0_bank0_rdata;

	        case (bus0_bank0_addr_buf[1:0])

            	2'd0 :
            		begin
	                if (bus0_bank0_be_buf[0]) bus0_bank0_wdata[7:0]   = bus0_bank0_wdata_buf[7:0];
	                if (bus0_bank0_be_buf[1]) bus0_bank0_wdata[15:8]  = bus0_bank0_wdata_buf[15:8];
	                if (bus0_bank0_be_buf[2]) bus0_bank0_wdata[23:16] = bus0_bank0_wdata_buf[23:16];
	                if (bus0_bank0_be_buf[3]) bus0_bank0_wdata[31:24] = bus0_bank0_wdata_buf[31:24];
	                end

	            2'd1:
	            	begin
	            	if (bus0_bank0_be_buf[0]) bus0_bank0_wdata[15:8]  = bus0_bank0_wdata_buf[7:0];
	                if (bus0_bank0_be_buf[1]) bus0_bank0_wdata[23:16] = bus0_bank0_wdata_buf[15:8];
	                if (bus0_bank0_be_buf[2]) bus0_bank0_wdata[31:24] = bus0_bank0_wdata_buf[23:16];
	            	end

	            2'd2:
	            	begin
	                if (bus0_bank0_be_buf[0]) bus0_bank0_wdata[23:16] = bus0_bank0_wdata_buf[7:0];
	                if (bus0_bank0_be_buf[1]) bus0_bank0_wdata[31:24] = bus0_bank0_wdata_buf[15:8];
	                end

	            2'd3:
	            	begin
	                if (bus0_bank0_be_buf[0]) bus0_bank0_wdata[31:24] = bus0_bank0_wdata_buf[7:0];
	                end

            endcase

	        end

	    if (bus0_bank1_wb)
	        begin
	        bus0_bank1_addr = bus0_bank1_addr_buf;
	        bus0_bank1_we = 1'b1;
	        bus0_bank1_wdata = bus0_bank1_rdata;

	        case (bus0_bank1_addr_buf[1:0])

            	2'd0 :
            		begin
	                if (bus0_bank1_be_buf[0]) bus0_bank1_wdata[7:0]   = bus0_bank1_wdata_buf[7:0];
	                if (bus0_bank1_be_buf[1]) bus0_bank1_wdata[15:8]  = bus0_bank1_wdata_buf[15:8];
	                if (bus0_bank1_be_buf[2]) bus0_bank1_wdata[23:16] = bus0_bank1_wdata_buf[23:16];
	                if (bus0_bank1_be_buf[3]) bus0_bank1_wdata[31:24] = bus0_bank1_wdata_buf[31:24];
	                end

	            2'd1:
	            	begin
	            	if (bus0_bank1_be_buf[0]) bus0_bank1_wdata[15:8]  = bus0_bank1_wdata_buf[7:0];
	                if (bus0_bank1_be_buf[1]) bus0_bank1_wdata[23:16] = bus0_bank1_wdata_buf[15:8];
	                if (bus0_bank1_be_buf[2]) bus0_bank1_wdata[31:24] = bus0_bank1_wdata_buf[23:16];
	            	end

	            2'd2:
	            	begin
	                if (bus0_bank1_be_buf[0]) bus0_bank1_wdata[23:16] = bus0_bank1_wdata_buf[7:0];
	                if (bus0_bank1_be_buf[1]) bus0_bank1_wdata[31:24] = bus0_bank1_wdata_buf[15:8];
	                end

	            2'd3:
	            	begin
	                if (bus0_bank1_be_buf[0]) bus0_bank1_wdata[31:24] = bus0_bank1_wdata_buf[7:0];
	                end

            endcase
            
	        end

        end
    end

  always @*
    begin

    bus1_bank0_wb_next = 1'b0;
    bus1_bank1_wb_next = 1'b0;

    bus1_bank0_addr  = bus1_bank0_addr_bi;
    bus1_bank0_we    = bus1_bank0_we_fullword;
    bus1_bank0_be    = bus1_bank0_be_bi;
    bus1_bank0_wdata = bus1_bank0_wdata_bi;
    bus1_bank0_ack_o = bus1_bank0_req_i & !bus1_bank0_wb;
    if (P1_FRAC=="YES") bus1_bank0_wb_next = bus1_bank0_we_nfullword & !bus1_bank0_wb;
    bus1_bank0_resp_next = bus1_bank0_req_i & !bus1_bank0_we;

    bus1_bank1_addr  = bus1_bank1_addr_bi;
    bus1_bank1_we    = bus1_bank1_we_fullword;
    bus1_bank1_be    = bus1_bank1_be_bi;
    bus1_bank1_wdata = bus1_bank1_wdata_bi;
    bus1_bank1_ack_o = bus1_bank1_req_i & !bus1_bank1_wb;
    if (P1_FRAC=="YES") bus1_bank1_wb_next = bus1_bank1_we_nfullword & !bus1_bank1_wb;
    bus1_bank1_resp_next = bus1_bank1_req_i & !bus1_bank1_we;

    bus1_rdata_xchg_next = 1'b0;

    if (bus1_bank0_req_i)
      begin
      if (bus1_bank0_addr_bi[2])
        begin
        bus1_bank1_addr  = bus1_bank0_addr_bi;
        bus1_bank1_we    = bus1_bank0_we_fullword;
        bus1_bank1_be    = bus1_bank0_be_bi;
        bus1_bank1_wdata = bus1_bank0_wdata_bi;
        bus1_bank0_we = 1'b0;
        bus1_bank0_ack_o = bus1_bank0_req_i & !bus1_bank1_wb;
        if (P1_FRAC=="YES") bus1_bank0_wb_next = 1'b0;
        if (P1_FRAC=="YES") bus1_bank1_wb_next = bus1_bank0_we_nfullword & !bus1_bank1_wb;
        bus1_bank0_resp_next = 1'b0;
        bus1_bank1_resp_next = bus1_bank0_req_i & !bus1_bank0_we;
        bus1_rdata_xchg_next = 1'b1;
        end

      end

    if (bus1_bank1_req_i)
      begin
      if (bus1_bank0_req_i && (bus1_bank0_addr_bi[2] == bus1_bank1_addr_bi[2]))
        begin
        bus1_bank1_ack_o = 1'b0;
        if (!bus1_bank1_addr_bi[2]) bus1_bank1_resp_next = 1'b0;
        end
      else if (!bus1_bank1_addr_bi[2])
        begin
        bus1_bank0_addr  = bus1_bank1_addr_bi;
        bus1_bank0_we    = bus1_bank1_we_fullword;
        bus1_bank0_be    = bus1_bank1_be_bi;
        bus1_bank0_wdata = bus1_bank1_wdata_bi;
        bus1_bank1_ack_o = bus1_bank1_req_i & !bus1_bank0_wb;
        if (P1_FRAC=="YES") bus1_bank0_wb_next = bus1_bank1_we_nfullword & !bus1_bank0_wb;
        bus1_bank0_resp_next = bus1_bank1_req_i & !bus1_bank1_we;
        if (!bus1_bank0_req_i) bus1_bank1_resp_next = 1'b0;
        bus1_rdata_xchg_next = 1'b1;
        end
      end

    if (P1_FRAC=="YES")
        begin
        
        if (bus1_bank0_wb)
	        begin
	        bus1_bank0_addr = bus1_bank0_addr_buf;
	        bus1_bank0_we = 1'b1;
	        bus1_bank0_wdata = bus1_bank0_rdata;

	        case (bus1_bank0_addr_buf[1:0])

            	2'd0 :
            		begin
	                if (bus1_bank0_be_buf[0]) bus1_bank0_wdata[7:0]   = bus1_bank0_wdata_buf[7:0];
	                if (bus1_bank0_be_buf[1]) bus1_bank0_wdata[15:8]  = bus1_bank0_wdata_buf[15:8];
	                if (bus1_bank0_be_buf[2]) bus1_bank0_wdata[23:16] = bus1_bank0_wdata_buf[23:16];
	                if (bus1_bank0_be_buf[3]) bus1_bank0_wdata[31:24] = bus1_bank0_wdata_buf[31:24];
	                end

	            2'd1:
	            	begin
	            	if (bus1_bank0_be_buf[0]) bus1_bank0_wdata[15:8]  = bus1_bank0_wdata_buf[7:0];
	                if (bus1_bank0_be_buf[1]) bus1_bank0_wdata[23:16] = bus1_bank0_wdata_buf[15:8];
	                if (bus1_bank0_be_buf[2]) bus1_bank0_wdata[31:24] = bus1_bank0_wdata_buf[23:16];
	            	end

	            2'd2:
	            	begin
	                if (bus1_bank0_be_buf[0]) bus1_bank0_wdata[23:16] = bus1_bank0_wdata_buf[7:0];
	                if (bus1_bank0_be_buf[1]) bus1_bank0_wdata[31:24] = bus1_bank0_wdata_buf[15:8];
	                end

	            2'd3:
	            	begin
	                if (bus1_bank0_be_buf[0]) bus1_bank0_wdata[31:24] = bus1_bank0_wdata_buf[7:0];
	                end

            endcase

	        end

	    if (bus1_bank1_wb)
	        begin
	        bus1_bank1_addr = bus1_bank1_addr_buf;
	        bus1_bank1_we = 1'b1;
	        bus1_bank1_wdata = bus1_bank1_rdata;

	        case (bus1_bank1_addr_buf[1:0])

            	2'd0 :
            		begin
	                if (bus1_bank1_be_buf[0]) bus1_bank1_wdata[7:0]   = bus1_bank1_wdata_buf[7:0];
	                if (bus1_bank1_be_buf[1]) bus1_bank1_wdata[15:8]  = bus1_bank1_wdata_buf[15:8];
	                if (bus1_bank1_be_buf[2]) bus1_bank1_wdata[23:16] = bus1_bank1_wdata_buf[23:16];
	                if (bus1_bank1_be_buf[3]) bus1_bank1_wdata[31:24] = bus1_bank1_wdata_buf[31:24];
	                end

	            2'd1:
	            	begin
	            	if (bus1_bank1_be_buf[0]) bus1_bank1_wdata[15:8]  = bus1_bank1_wdata_buf[7:0];
	                if (bus1_bank1_be_buf[1]) bus1_bank1_wdata[23:16] = bus1_bank1_wdata_buf[15:8];
	                if (bus1_bank1_be_buf[2]) bus1_bank1_wdata[31:24] = bus1_bank1_wdata_buf[23:16];
	            	end

	            2'd2:
	            	begin
	                if (bus1_bank1_be_buf[0]) bus1_bank1_wdata[23:16] = bus1_bank1_wdata_buf[7:0];
	                if (bus1_bank1_be_buf[1]) bus1_bank1_wdata[31:24] = bus1_bank1_wdata_buf[15:8];
	                end

	            2'd3:
	            	begin
	                if (bus1_bank1_be_buf[0]) bus1_bank1_wdata[31:24] = bus1_bank1_wdata_buf[7:0];
	                end

            endcase
            
	        end

        end
    end
  
  
  always @*
    begin
    bus0_bank0_resp_o = bus0_bank0_resp;
    bus0_bank1_resp_o = bus0_bank1_resp;
    bus0_bank0_rdata_bo = bus0_bank0_rdata;
    bus0_bank1_rdata_bo = bus0_bank1_rdata;
    if (P0_FRAC == "YES")
        begin
        if (bus0_bank0_addr_buf[1:0] == 2'd1) bus0_bank0_rdata_bo = bus0_bank0_rdata >> 8;
        if (bus0_bank0_addr_buf[1:0] == 2'd2) bus0_bank0_rdata_bo = bus0_bank0_rdata >> 16;
        if (bus0_bank0_addr_buf[1:0] == 2'd3) bus0_bank0_rdata_bo = bus0_bank0_rdata >> 24;

        if (bus0_bank1_addr_buf[1:0] == 2'd1) bus0_bank1_rdata_bo = bus0_bank1_rdata >> 8;
        if (bus0_bank1_addr_buf[1:0] == 2'd2) bus0_bank1_rdata_bo = bus0_bank1_rdata >> 16;
        if (bus0_bank1_addr_buf[1:0] == 2'd3) bus0_bank1_rdata_bo = bus0_bank1_rdata >> 24;
        end
    if (bus0_rdata_xchg)
    	begin
    	{bus0_bank0_resp_o, bus0_bank1_resp_o} = {bus0_bank1_resp_o, bus0_bank0_resp_o};
    	{bus0_bank0_rdata_bo, bus0_bank1_rdata_bo} = {bus0_bank1_rdata_bo, bus0_bank0_rdata_bo};
    	end
    end

  always @*
    begin
    bus1_bank0_resp_o = bus1_bank0_resp;
    bus1_bank1_resp_o = bus1_bank1_resp;
    bus1_bank0_rdata_bo = bus1_bank0_rdata;
    bus1_bank1_rdata_bo = bus1_bank1_rdata;
    if (P1_FRAC == "YES")
        begin
        if (bus1_bank0_addr_buf[1:0] == 2'd1) bus1_bank0_rdata_bo = bus1_bank0_rdata >> 8;
        if (bus1_bank0_addr_buf[1:0] == 2'd2) bus1_bank0_rdata_bo = bus1_bank0_rdata >> 16;
        if (bus1_bank0_addr_buf[1:0] == 2'd3) bus1_bank0_rdata_bo = bus1_bank0_rdata >> 24;

        if (bus1_bank1_addr_buf[1:0] == 2'd1) bus1_bank1_rdata_bo = bus1_bank1_rdata >> 8;
        if (bus1_bank1_addr_buf[1:0] == 2'd2) bus1_bank1_rdata_bo = bus1_bank1_rdata >> 16;
        if (bus1_bank1_addr_buf[1:0] == 2'd3) bus1_bank1_rdata_bo = bus1_bank1_rdata >> 24;
        end
    if (bus1_rdata_xchg)
    	begin
    	{bus1_bank0_resp_o, bus1_bank1_resp_o} = {bus1_bank1_resp_o, bus1_bank0_resp_o};
    	{bus1_bank0_rdata_bo, bus1_bank1_rdata_bo} = {bus1_bank1_rdata_bo, bus1_bank0_rdata_bo};
    	end
    end

  always @(posedge clk_i)
	begin
	bus0_bank0_addr_buf 	<= bus0_bank0_addr;
	bus0_bank0_be_buf 		<= bus0_bank0_be;
	bus0_bank0_wdata_buf 	<= bus0_bank0_wdata;

	bus0_bank1_addr_buf 	<= bus0_bank1_addr;
	bus0_bank1_be_buf 		<= bus0_bank1_be;
	bus0_bank1_wdata_buf 	<= bus0_bank1_wdata;
	end

  always @(posedge clk_i)
	begin
	bus1_bank0_addr_buf 	<= bus1_bank0_addr;
	bus1_bank0_be_buf 		<= bus1_bank0_be;
	bus1_bank0_wdata_buf 	<= bus1_bank0_wdata;

	bus1_bank1_addr_buf 	<= bus1_bank1_addr;
	bus1_bank1_be_buf 		<= bus1_bank1_be;
	bus1_bank1_wdata_buf 	<= bus1_bank1_wdata;
	end
  
  ram_dual
  #(
	.init_type("none")
	, .init_data("nodata.hex")
	, .dat_width(dat_width)
	, .adr_width(adr_width-1)
	, .mem_size(mem_size >> 1)
  ) ram_dual_bank0 (
	
	.clk(clk_i)
	
	, .dat0_i(bus0_bank0_wdata)
    , .adr0_i(bus0_bank0_addr[31:3])
    , .we0_i(bus0_bank0_we)
    , .dat0_o(bus0_bank0_rdata)

    , .dat1_i(bus1_bank0_wdata)
    , .adr1_i(bus1_bank0_addr[31:3])
    , .we1_i(bus1_bank0_we)
    , .dat1_o(bus1_bank0_rdata)
  );

  ram_dual
  #(
	.init_type("none")
	, .init_data("nodata.hex")
	, .dat_width(dat_width)
	, .adr_width(adr_width-1)
	, .mem_size(mem_size >> 1)
  ) ram_dual_bank1 (
	
	.clk(clk_i)
	
	, .dat0_i(bus0_bank1_wdata)
    , .adr0_i(bus0_bank1_addr[31:3])
    , .we0_i(bus0_bank1_we)
    , .dat0_o(bus0_bank1_rdata)

    , .dat1_i(bus1_bank1_wdata)
    , .adr1_i(bus1_bank1_addr[31:3])
    , .we1_i(bus1_bank1_we)
    , .dat1_o(bus1_bank1_rdata)
  );

// elf processing
integer File_ID, Rd_Status;
reg [7:0] File_Rdata [0 : (mem_size * (dat_width / 8)) - 1] ;
integer File_ptr, header_idx;
integer e_machine, e_phnum, p_offset, p_vaddr, p_filesz, elf_param;
integer bytes_in_word, load_byte_counter;
integer ram_ptr, wrword_byte_counter;
reg [dat_width-1:0] wrword;
reg [8*8:0] e_machine_str;

initial
begin
  if (init_type != "none")
    begin
    if (init_type == "elf")
        begin
        
        File_ID = $fopen(init_data, "rb");
        Rd_Status = $fread(File_Rdata, File_ID);
        if (Rd_Status == 0) $fatal("File %s not found!", init_data);
        
        $display("\n##################################");
        $display("#### Loading elf file: %s", init_data);
        
        // parsing ELF header
        if ((File_Rdata[0] != 8'h7f) || (File_Rdata[1] != 8'h45) || (File_Rdata[2] != 8'h4c) || (File_Rdata[3] != 8'h46)) $fatal("%s: elf format incorrect!", init_data);
        e_machine = File_Rdata[18] + (File_Rdata[19] << 8);
        e_machine_str = "UNKNOWN";
        if (e_machine == 32'hF3) e_machine_str = "RISC-V";
        $display("e_machine: 0x%x (%s)", e_machine, e_machine_str);
        e_phnum = File_Rdata[44] + (File_Rdata[45] << 8);
        $display("e_phnum: 0x%x", e_phnum);
        
        File_ptr = 52;
        for (header_idx = 0; header_idx < e_phnum; header_idx = header_idx + 1)
            begin
            
            // parsing program header
            $display("---- HEADER: %0d ----", header_idx);
            
            elf_param = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_type: 0x%x", elf_param);
            File_ptr = File_ptr + 4;
            
            p_offset = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_offset: 0x%x", p_offset);
            File_ptr = File_ptr + 4;
            
            p_vaddr = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_vaddr: 0x%x", p_vaddr);
            File_ptr = File_ptr + 4;
            
            elf_param = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_paddr: 0x%x", elf_param);
            File_ptr = File_ptr + 4;
            
            p_filesz = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_filesz: 0x%x", p_filesz);
            File_ptr = File_ptr + 4;
            
            elf_param = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_memsz: 0x%x", elf_param);
            File_ptr = File_ptr + 4;
            
            elf_param = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_flags: 0x%x", elf_param);
            File_ptr = File_ptr + 4;
            
            elf_param = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_align: 0x%x", elf_param);
            File_ptr = File_ptr + 4;
            
            // loading segment to memory
            bytes_in_word = dat_width / 8;
            for (load_byte_counter = 0; load_byte_counter < p_filesz; load_byte_counter = load_byte_counter + bytes_in_word)
                begin
                wrword = 0;
                for (wrword_byte_counter = 0; wrword_byte_counter < bytes_in_word; wrword_byte_counter = wrword_byte_counter + 1)
                    begin
                    wrword = {File_Rdata[p_offset + load_byte_counter + wrword_byte_counter], wrword[dat_width-1:8]};
                    end
                ram_ptr = (p_vaddr + load_byte_counter) / bytes_in_word;
                if (ram_ptr[0] == 1'b0) ram_dual_bank0.ram[ram_ptr >> 1] = wrword;
                else ram_dual_bank1.ram[ram_ptr >> 1] = wrword;
                end
            end
        $display("##################################\n");
        $fclose(File_ID);
        end
    else $fatal("init_type parameter incorrect!");
    end
end

endmodule
