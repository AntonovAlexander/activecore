module DL_NEXYS2_1200E
(
	input 	clk, clk1,

	input 	[7:0] sw,
	input	[3:0] btn,
	output 	[7:0] Led,

	input 	RsRx,
	output	RsTx
);

wire clk_generated;

sys_clk sys_clk(
	.CLKIN_IN(clk), 
	.RST_IN(btn[0]), 
	.CLKDV_OUT(clk_generated),
	.CLKIN_IBUFG_OUT(), 	
	.CLK0_OUT()
);

dlx_udm_memsplit
#(
	//.mem_data("../../../../activecore/dlx/sw/io_heartbeat/io_heartbeat.hex"),
	.mem_data("../../../../activecore/dlx/sw/io_heartbeat_variable/io_heartbeat_variable.hex"),
	.mem_size(1024)
) dlx_udm
(
	.clk_i(clk_generated),
	.rst_i(btn[0]),
	.rx_i(RsRx),
	.tx_o(RsTx),
	.gpio_bi({8'h0, sw, 16'h0}),
	.gpio_bo(Led)
);

endmodule
