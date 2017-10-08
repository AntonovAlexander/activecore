module NEXYS4_DDR
(
	input 	CLK100MHZ
    , input   CPU_RESETN
    
    , input   [15:0] SW
    , output  [15:0] LED

    , input   UART_TXD_IN
    , output  UART_RXD_OUT
);

pss_memsplit
#(
	.CPU("riscv"),
	.delay_test_flag(0),
	.mem_data("../../activecore/riscv/sw/benchmarks/heartbeat_variable.riscv.hex"),
	.mem_size(8192)
) dlx_udm
(
	.clk_i(CLK100MHZ)
	, .arst_i(!CPU_RESETN)
	, .rx_i(UART_TXD_IN)
	, .tx_o(UART_RXD_OUT)
	, .gpio_bi({8'h0, SW, 8'h0})
	, .gpio_bo(LED)
);

endmodule
