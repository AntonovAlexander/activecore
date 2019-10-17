module NEXYS4_DDR
(
	input 	CLK100MHZ
    , input   CPU_RESETN
    
    , input   [15:0] SW
    , output reg  [15:0] LED

    , input   UART_TXD_IN
    , output  UART_RXD_OUT
);

wire clk_gen;
wire pll_locked;

sys_clk sys_clk
(
    .clk_in1(CLK100MHZ)
    , .reset(!CPU_RESETN)
    , .clk_out1(clk_gen)
    , .locked(pll_locked)
);

wire arst;
assign arst = !(CPU_RESETN & pll_locked);

wire srst;
reset_cntrl reset_cntrl
(
	.clk_i(clk_gen),
	.arst_i(arst),
	.srst_o(srst)
);

wire [0:0] udm_req;
wire [0:0] udm_we;
wire [31:0] udm_addr;
wire [3:0] udm_be;
wire [31:0] udm_wdata;
wire [0:0] udm_ack;
reg [0:0] udm_resp;
reg [31:0] udm_rdata;

udm_memsplit udm_memsplit
(
	.clk_i(clk_gen)
	, .rst_i(srst)

	, .rx_i(UART_TXD_IN)
	, .tx_o(UART_RXD_OUT)

	, .rst_o(udm_reset)
	
	, .bus_req_o(udm_req)
	, .bus_we_o(udm_we)
	, .bus_addr_bo(udm_addr)
	, .bus_be_bo(udm_be)
	, .bus_wdata_bo(udm_wdata)
	, .bus_ack_i(udm_ack)
	, .bus_resp_i(udm_resp)
	, .bus_rdata_bi(udm_rdata)
);

assign udm_ack = udm_req;   // bus always ready to accept request
// writing
always @(posedge clk_gen)
    begin
    if (udm_req && udm_we)
        begin
        if (udm_addr == 0) LED <= udm_wdata;
        end
    end
// reading
always @(posedge clk_gen)
    begin
    udm_resp <= 1'b0;
    udm_rdata <= 32'h0;
    if (udm_req && udm_ack && !udm_we)
        begin
        udm_resp <= 1'b1;
        if (udm_addr == 32'h0) udm_rdata <= LED;
        if (udm_addr == 32'h4) udm_rdata <= SW;
        end
    end

endmodule
