module KC705
(
      input   CLK200MHZ_P
    , input   CLK200MHZ_N
    , input   CPU_RESET
    
    , input   BTNC
    , input   [3:0] SW
    , output  [7:0] LED

    , input   UART_TXD_IN
    , output  UART_RXD_OUT
);

wire CLK200MHZ;
IBUFDS IBUFDS_inst (
    .O(CLK200MHZ)
    , .I(CLK200MHZ_P)
    , .IB(CLK200MHZ_N)
);

wire clk_gen;
wire pll_locked;

sys_clk sys_clk
(
    .clk_in1(CLK200MHZ)
    , .reset(CPU_RESET)
    , .clk_out1(clk_gen)
    , .locked(pll_locked)
);

wire arst;
assign arst = CPU_RESET | !pll_locked;

sigrun
#(
	.mem_init_type("none")
	, .mem_init_data("../sigma/sw/apps/heartbeat_variable.riscv.hex")
	, .mem_size(8192)
) sigrun (
	.clk_i(clk_gen)
	, .arst_i(arst)
	, .irq_btn_i(BTNC)
	, .rx_i(UART_TXD_IN)
	, .tx_o(UART_RXD_OUT)
	, .gpio_bi({8'h0, SW, 8'h0})
	, .gpio_bo(LED)
);

endmodule
