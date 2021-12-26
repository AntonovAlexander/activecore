module NEXYS4_DDR
(
      input   CLK100MHZ
    , input   CPU_RESETN
    
    , input   BTNC
    , input   [15:0] SW
    , output  [15:0] LED

    , input   UART_TXD_IN
    , output  UART_RXD_OUT
);

wire CLK_80MHZ;
wire pll_locked;

sys_clk sys_clk
(
    .clk_in1(CLK100MHZ)
    , .reset(!CPU_RESETN)
    , .clk_out1(CLK_80MHZ)
    , .locked(pll_locked)
);

wire arst;
assign arst = !(CPU_RESETN & pll_locked);

sigma
#(
	.CPU("riscv_6stage")
	, .delay_test_flag(0)
	, .mem_init_type("hex")
	, .mem_init_data("../sigma/sw/apps/heartbeat_variable.riscv.hex")
	, .mem_size(8192)
) sigma
(
	.clk_i(CLK_80MHZ)
	, .arst_i(arst)
	, .irq_btn_i(BTNC)
	, .rx_i(UART_TXD_IN)
	, .tx_o(UART_RXD_OUT)
	, .gpio_bi({8'h0, SW, 8'h0})
	, .gpio_bo(LED)
);

endmodule
