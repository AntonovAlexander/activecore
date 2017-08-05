module NEXYS4_DDR
(
	input 	CLK100MHZ
    , input   CPU_RESETN
    
    , input   [15:0] SW
    , output  [15:0] LED

    , input   UART_TXD_IN
    , output  UART_RXD_OUT
);

dlx_udm
#(
	//.mem_data("../../../../activecore/dlx/sw/io_heartbeat/io_heartbeat.hex")
	.mem_data("../../../../activecore/dlx/sw/io_heartbeat_variable/io_heartbeat_variable.hex")
	, .mem_size(1024)
) dlx_udm
(
	.clk_i(CLK100MHZ)
	, .rst_i(!CPU_RESETN)
	, .rx_i(UART_TXD_IN)
	, .tx_o(UART_RXD_OUT)
	, .SW({8'h0, SW, 16'h0})
	, .LED(LED)
);

endmodule
