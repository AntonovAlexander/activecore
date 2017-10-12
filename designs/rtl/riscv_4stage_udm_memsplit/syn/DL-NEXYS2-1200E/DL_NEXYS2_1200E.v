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

pss_memsplit
#(
	.CPU("riscv_4stage"),
	.delay_test_flag(0),
	.mem_data("../../../../activecore/riscv/sw/benchmarks/heartbeat_variable.riscv.hex"),
	//.mem_data("../../../../activecore/riscv/sw/benchmarks/median.riscv.hex"),
	.mem_size(8192)
) riscv_udm
(
	.clk_i(clk_generated),
	.arst_i(btn[0]),
	.rx_i(RsRx),
	.tx_o(RsTx),
	.gpio_bi({8'h0, sw, 16'h0}),
	.gpio_bo(Led)
);

endmodule
