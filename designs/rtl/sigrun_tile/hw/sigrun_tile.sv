/*
 * sigrun_tile.sv
 *
 *  Created on: 22.07.2021
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

`include "agenda_cpu.svh"

module sigrun_tile
#(
    parameter corenum=0
    , mem_init_type="elf"
    , mem_init_data="data.elf"
    , mem_size=1024
    , PATH_THROUGH="YES"
    , SW_RESET_DEFAULT=0
    , IRQ_NUM_POW=4
)
(
    input clk_i
    , input rst_i

    , input sw_reset_enb_i = 1'b0
    , input sw_reset_set_i = 1'b0
    , input sw_reset_autoclr_i = 1'b0
    , output logic core_reset_o

    , input [(2**IRQ_NUM_POW)-1:0] irq_debounced_bi
    
    , MemSplit32.Slave hif     // host interface
    , MemSplit32.Master xif    // expansion interface
);

    localparam XIF_BITSEL  = 31;
    localparam SFR_BITSEL  = 20;
    
    MemSplit32 cpu_instr_0(), cpu_instr_1();
    MemSplit32 cpu_data();

    logic [(2**IRQ_NUM_POW)-1:0] irq_en;
    logic irq_timer;
    logic sgi_req;
    logic [IRQ_NUM_POW-1:0] sgi_code;

    logic cpu_irq_req;
    logic [IRQ_NUM_POW-1:0] cpu_irq_code;
    logic cpu_irq_ack;

    irq_adapter #(
        .IRQ_NUM_POW(IRQ_NUM_POW)
    ) irq_adapter (
        .clk_i(clk_i)
        , .rst_i(core_reset_o)
        , .irq_debounced_bi((irq_debounced_bi | (irq_timer << 1)) & irq_en)
        , .sgi_req_i(sgi_req)
        , .sgi_code_bi(sgi_code)
        , .irq_req_o(cpu_irq_req)
        , .irq_code_bo(cpu_irq_code)
        , .irq_ack_i(cpu_irq_ack)
    );
	
    // Processor core
    genpmodule_agenda_cpu_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus_0;
    assign cpu_instr_0.we     = instr_mem_struct_bus_0.we;
    assign cpu_instr_0.addr   = instr_mem_struct_bus_0.wdata.addr;
    assign cpu_instr_0.be     = instr_mem_struct_bus_0.wdata.be;
    assign cpu_instr_0.wdata  = instr_mem_struct_bus_0.wdata.wdata;
    
    genpmodule_agenda_cpu_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus_1;
    assign cpu_instr_1.we     = instr_mem_struct_bus_1.we;
    assign cpu_instr_1.addr   = instr_mem_struct_bus_1.wdata.addr;
    assign cpu_instr_1.be     = instr_mem_struct_bus_1.wdata.be;
    assign cpu_instr_1.wdata  = instr_mem_struct_bus_1.wdata.wdata;

    genpmodule_agenda_cpu_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
    assign cpu_data.we      = data_mem_struct_bus.we;
    assign cpu_data.addr    = data_mem_struct_bus.wdata.addr;
    assign cpu_data.be      = data_mem_struct_bus.wdata.be;
    assign cpu_data.wdata   = data_mem_struct_bus.wdata.wdata;

    agenda_cpu riscv (
        .clk_i(clk_i)
        , .rst_i(core_reset_o)
        
        // interrupt bus
        , .irq_fifo_genfifo_req_i(cpu_irq_req)
        , .irq_fifo_genfifo_rdata_bi({0, cpu_irq_code})
        , .irq_fifo_genfifo_ack_o(cpu_irq_ack)
        
        // instr req 0 bus
        , .genmcopipe_instr_mem_req_0_genfifo_req_o(cpu_instr_0.req)
        , .genmcopipe_instr_mem_req_0_genfifo_wdata_bo(instr_mem_struct_bus_0)
        , .genmcopipe_instr_mem_req_0_genfifo_ack_i(cpu_instr_0.ack)
        
        // instr req 1 bus
        , .genmcopipe_instr_mem_req_1_genfifo_req_o(cpu_instr_1.req)
        , .genmcopipe_instr_mem_req_1_genfifo_wdata_bo(instr_mem_struct_bus_1)
        , .genmcopipe_instr_mem_req_1_genfifo_ack_i(cpu_instr_1.ack)

        // data req bus
        , .genmcopipe_data_mem_req_genfifo_req_o(cpu_data.req)
        , .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
        , .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data.ack)

        // instr resp 0 bus
        , .genmcopipe_instr_mem_resp_0_genfifo_req_i(cpu_instr_0.resp)
        , .genmcopipe_instr_mem_resp_0_genfifo_rdata_bi(cpu_instr_0.rdata)
        // , .genmcopipe_instr_mem_resp_0_genfifo_ack_o
        
        // instr resp 1 bus
        , .genmcopipe_instr_mem_resp_1_genfifo_req_i(cpu_instr_1.resp)
        , .genmcopipe_instr_mem_resp_1_genfifo_rdata_bi(cpu_instr_1.rdata)
        // , .genmcopipe_instr_mem_resp_1_genfifo_ack_o

        // data resp bus
        , .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data.resp)
        , .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data.rdata)
        //, .genmcopipe_data_mem_resp_genfifo_ack_o
    );

    MemSplit32 dmem_if();
    MemSplit32 sfr_if();
    
    generate
        if (PATH_THROUGH == "YES")
            begin

            arb_2m3s
            #(
                .SFR_BITSEL(SFR_BITSEL)
                , .XIF_BITSEL(XIF_BITSEL)
            ) arb_cpu (
                .clk_i      (clk_i)
                , .rst_i    (rst_i)

                , .m0(hif)
                , .m1(cpu_data)
                , .s0(dmem_if)
                , .s1(sfr_if)
                , .s2(xif)
            );

            end

        else
            begin
    
            MemSplit32 internal_if();
            MemSplit32 cpu_internal();

            arb_1m2s
            #(
                .BITSEL(XIF_BITSEL)
            ) arb_cpu (
                .clk_i      (clk_i)
                , .rst_i    (rst_i)

                , .m(cpu_data)
                , .s0(cpu_internal)
                , .s1(xif)
            );

            arb_2m1s arb_internal
            (
                .clk_i      (clk_i)
                , .rst_i    (rst_i)
                
                , .m0(cpu_internal)
                , .m1(hif)
                , .s(internal_if)
            );

            arb_1m2s
            #(
                .BITSEL(SFR_BITSEL)
            ) arb_l2 (
                .clk_i      (clk_i)
                , .rst_i    (rst_i)
                
                , .m(internal_if)
                , .s0(dmem_if)
                , .s1(sfr_if)
            );
            
            end

    endgenerate
    
	
	ram_dual_memsplit_2banks #(
		.init_type      (mem_init_type)
		, .init_data    (mem_init_data)
		, .dat_width	(32)
		, .adr_width	(30)
		, .mem_size		(mem_size)
		, .P0_FRAC("NO")
		, .P1_FRAC("YES")
	) ram (
		.clk_i(clk_i)
		, .rst_i(rst_i)

		, .bus0_bank0_req_i		(cpu_instr_0.req)
		, .bus0_bank0_we_i		(cpu_instr_0.we)
		, .bus0_bank0_addr_bi	(cpu_instr_0.addr)
		, .bus0_bank0_be_bi		(cpu_instr_0.be)
		, .bus0_bank0_wdata_bi	(cpu_instr_0.wdata)
		, .bus0_bank0_ack_o		(cpu_instr_0.ack)
		, .bus0_bank0_resp_o	(cpu_instr_0.resp)
		, .bus0_bank0_rdata_bo	(cpu_instr_0.rdata)

		, .bus0_bank1_req_i		(cpu_instr_1.req)
		, .bus0_bank1_we_i		(cpu_instr_1.we)
		, .bus0_bank1_addr_bi	(cpu_instr_1.addr)
		, .bus0_bank1_be_bi		(cpu_instr_1.be)
		, .bus0_bank1_wdata_bi	(cpu_instr_1.wdata)
		, .bus0_bank1_ack_o		(cpu_instr_1.ack)
		, .bus0_bank1_resp_o	(cpu_instr_1.resp)
		, .bus0_bank1_rdata_bo	(cpu_instr_1.rdata)

		, .bus1_bank0_req_i		(dmem_if.req)
		, .bus1_bank0_we_i		(dmem_if.we)
		, .bus1_bank0_addr_bi	(dmem_if.addr)
		, .bus1_bank0_be_bi		(dmem_if.be)
		, .bus1_bank0_wdata_bi	(dmem_if.wdata)
		, .bus1_bank0_ack_o		(dmem_if.ack)
		, .bus1_bank0_resp_o	(dmem_if.resp)
		, .bus1_bank0_rdata_bo	(dmem_if.rdata)

		, .bus1_bank1_req_i		(1'b0)
		, .bus1_bank1_we_i		(1'b0)
		, .bus1_bank1_addr_bi	(0)
		, .bus1_bank1_be_bi		(4'h0)
		, .bus1_bank1_wdata_bi	(0)
		//, .bus1_bank1_ack_o	(dmem_if.ack)
		//, .bus1_bank1_resp_o	(dmem_if.resp)
		//, .bus1_bank1_rdata_bo(dmem_if.rdata)
	);
	
    sfr #(
        .corenum(corenum)
        , .SW_RESET_DEFAULT(SW_RESET_DEFAULT)
        , .IRQ_NUM_POW(IRQ_NUM_POW)
    ) sfr (
        .clk_i		(clk_i)
        , .rst_i	(rst_i)

        , .host(sfr_if)

        , .sw_reset_enb_i(sw_reset_enb_i)
        , .sw_reset_set_i(sw_reset_set_i)
        , .sw_reset_autoclr_i(sw_reset_autoclr_i)
        , .core_reset_o(core_reset_o)

        , .irq_en_bo(irq_en)
        , .irq_timer(irq_timer)

        , .sgi_req_o(sgi_req)
        , .sgi_code_bo(sgi_code)
    );
	

endmodule
