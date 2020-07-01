/*
 * sigma_tile.sv
 *
 *  Created on: 27.12.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`include "sigma_tile.svh"

`include "riscv_1stage.svh"
`include "riscv_2stage.svh"
`include "riscv_3stage.svh"
`include "riscv_4stage.svh"
`include "riscv_5stage.svh"
`include "riscv_6stage.svh"

module sigma_tile
#(
    parameter corenum=0
    , mem_init="YES"
    , mem_data="data.hex"
    , mem_size=1024
    , CPU="none"
    , PATH_THROUGH="YES"
    , CPU_RESET_DEFAULT=0
    , IRQ_NUM_POW=4
)
(
    input clk_i
    , input rst_i

    , input [(2**IRQ_NUM_POW)-1:0] irq_debounced_bi
    
    , MemSplit32.Slave hif     // host interface
    , MemSplit32.Master xif    // expansion interface
);

    localparam XBUS_BITSEL  = 31;
    localparam SFR_BITSEL   = 20;

    logic cpu_reset;
    MemSplit32 cpu_instr();
    MemSplit32 cpu_data();

    logic msi_req;
    logic [IRQ_NUM_POW-1:0] msi_code;
    logic cpu_irq_req;
    logic [IRQ_NUM_POW-1:0] cpu_irq_code;
    logic cpu_irq_ack;
    irq_adapter #(
        .IRQ_NUM_POW(IRQ_NUM_POW)
    ) irq_adapter (
        .clk_i(clk_i)
        , .rst_i(rst_i)
        , .irq_debounced_bi(irq_debounced_bi)
        , .msi_req_i(msi_req)
        , .msi_code_bi(msi_code)
        , .irq_req_o(cpu_irq_req)
        , .irq_code_bo(cpu_irq_code)
        , .irq_ack_i(cpu_irq_ack)
    );
	
    // Processor core
    generate
        if (CPU == "riscv_1stage")
            
            begin
    
            genpmodule_riscv_1stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
            assign cpu_instr.we     = instr_mem_struct_bus.we;
            assign cpu_instr.addr   = instr_mem_struct_bus.wdata.addr;
            assign cpu_instr.be     = instr_mem_struct_bus.wdata.be;
            assign cpu_instr.wdata  = instr_mem_struct_bus.wdata.wdata;
    
            genpmodule_riscv_1stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
            assign cpu_data.we      = data_mem_struct_bus.we;
            assign cpu_data.addr    = data_mem_struct_bus.wdata.addr;
            assign cpu_data.be      = data_mem_struct_bus.wdata.be;
            assign cpu_data.wdata   = data_mem_struct_bus.wdata.wdata;
    
            riscv_1stage riscv (
                .clk_i(clk_i)
                , .rst_i(cpu_reset)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(cpu_irq_req)
                , .irq_fifo_genfifo_rdata_bi({0, cpu_irq_code})
                , .irq_fifo_genfifo_ack_o(cpu_irq_ack)
                
                // instr req bus
                , .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr.req)
                , .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
                , .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr.ack)
    
                // data req bus
                , .genmcopipe_data_mem_req_genfifo_req_o(cpu_data.req)
                , .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
                , .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data.ack)
    
                // instr resp bus
                , .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr.resp)
                , .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr.rdata)
                // , .genmcopipe_instr_mem_resp_genfifo_ack_o
    
                // data resp bus
                , .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data.resp)
                , .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data.rdata)
                //, .genmcopipe_data_mem_resp_genfifo_ack_o
            );
    
            end
    
        else if (CPU == "riscv_2stage")
        
            begin
    
            genpmodule_riscv_2stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
            assign cpu_instr.we     = instr_mem_struct_bus.we;
            assign cpu_instr.addr   = instr_mem_struct_bus.wdata.addr;
            assign cpu_instr.be     = instr_mem_struct_bus.wdata.be;
            assign cpu_instr.wdata  = instr_mem_struct_bus.wdata.wdata;
    
            genpmodule_riscv_2stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
            assign cpu_data.we      = data_mem_struct_bus.we;
            assign cpu_data.addr    = data_mem_struct_bus.wdata.addr;
            assign cpu_data.be      = data_mem_struct_bus.wdata.be;
            assign cpu_data.wdata   = data_mem_struct_bus.wdata.wdata;
    
            riscv_2stage riscv (
                .clk_i(clk_i)
                , .rst_i(cpu_reset)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(cpu_irq_req)
                , .irq_fifo_genfifo_rdata_bi({0, cpu_irq_code})
                , .irq_fifo_genfifo_ack_o(cpu_irq_ack)
                
                // instr req bus
                , .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr.req)
                , .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
                , .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr.ack)
    
                // data req bus
                , .genmcopipe_data_mem_req_genfifo_req_o(cpu_data.req)
                , .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
                , .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data.ack)
    
                // instr resp bus
                , .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr.resp)
                , .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr.rdata)
                // , .genmcopipe_instr_mem_resp_genfifo_ack_o
    
                // data resp bus
                , .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data.resp)
                , .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data.rdata)
                //, .genmcopipe_data_mem_resp_genfifo_ack_o
            );
    
            end
    
        else if (CPU == "riscv_3stage")
        
            begin
    
            genpmodule_riscv_3stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
            assign cpu_instr.we     = instr_mem_struct_bus.we;
            assign cpu_instr.addr   = instr_mem_struct_bus.wdata.addr;
            assign cpu_instr.be     = instr_mem_struct_bus.wdata.be;
            assign cpu_instr.wdata  = instr_mem_struct_bus.wdata.wdata;
    
            genpmodule_riscv_3stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
            assign cpu_data.we      = data_mem_struct_bus.we;
            assign cpu_data.addr    = data_mem_struct_bus.wdata.addr;
            assign cpu_data.be      = data_mem_struct_bus.wdata.be;
            assign cpu_data.wdata   = data_mem_struct_bus.wdata.wdata;
    
            riscv_3stage riscv (
                .clk_i(clk_i)
                , .rst_i(cpu_reset)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(cpu_irq_req)
                , .irq_fifo_genfifo_rdata_bi({0, cpu_irq_code})
                , .irq_fifo_genfifo_ack_o(cpu_irq_ack)
                
                // instr req bus
                , .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr.req)
                , .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
                , .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr.ack)
    
                // data req bus
                , .genmcopipe_data_mem_req_genfifo_req_o(cpu_data.req)
                , .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
                , .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data.ack)
    
                // instr resp bus
                , .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr.resp)
                , .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr.rdata)
                // , .genmcopipe_instr_mem_resp_genfifo_ack_o
    
                // data resp bus
                , .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data.resp)
                , .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data.rdata)
                //, .genmcopipe_data_mem_resp_genfifo_ack_o
            );
    
            end
    
        else if (CPU == "riscv_4stage")
        
            begin
    
            genpmodule_riscv_4stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
            assign cpu_instr.we     = instr_mem_struct_bus.we;
            assign cpu_instr.addr   = instr_mem_struct_bus.wdata.addr;
            assign cpu_instr.be     = instr_mem_struct_bus.wdata.be;
            assign cpu_instr.wdata  = instr_mem_struct_bus.wdata.wdata;
    
            genpmodule_riscv_4stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
            assign cpu_data.we      = data_mem_struct_bus.we;
            assign cpu_data.addr    = data_mem_struct_bus.wdata.addr;
            assign cpu_data.be      = data_mem_struct_bus.wdata.be;
            assign cpu_data.wdata   = data_mem_struct_bus.wdata.wdata;
    
            riscv_4stage riscv (
                .clk_i(clk_i)
                , .rst_i(cpu_reset)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(cpu_irq_req)
                , .irq_fifo_genfifo_rdata_bi({0, cpu_irq_code})
                , .irq_fifo_genfifo_ack_o(cpu_irq_ack)
                
                // instr req bus
                , .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr.req)
                , .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
                , .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr.ack)
    
                // data req bus
                , .genmcopipe_data_mem_req_genfifo_req_o(cpu_data.req)
                , .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
                , .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data.ack)
    
                // instr resp bus
                , .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr.resp)
                , .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr.rdata)
                // , .genmcopipe_instr_mem_resp_genfifo_ack_o
    
                // data resp bus
                , .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data.resp)
                , .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data.rdata)
                //, .genmcopipe_data_mem_resp_genfifo_ack_o
            );
    
            end
    
        else if (CPU == "riscv_5stage")
        
            begin
    
            genpmodule_riscv_5stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
            assign cpu_instr.we     = instr_mem_struct_bus.we;
            assign cpu_instr.addr   = instr_mem_struct_bus.wdata.addr;
            assign cpu_instr.be     = instr_mem_struct_bus.wdata.be;
            assign cpu_instr.wdata  = instr_mem_struct_bus.wdata.wdata;
    
            genpmodule_riscv_5stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
            assign cpu_data.we      = data_mem_struct_bus.we;
            assign cpu_data.addr    = data_mem_struct_bus.wdata.addr;
            assign cpu_data.be      = data_mem_struct_bus.wdata.be;
            assign cpu_data.wdata   = data_mem_struct_bus.wdata.wdata;
    
            riscv_5stage riscv (
                .clk_i(clk_i)
                , .rst_i(cpu_reset)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(cpu_irq_req)
                , .irq_fifo_genfifo_rdata_bi({0, cpu_irq_code})
                , .irq_fifo_genfifo_ack_o(cpu_irq_ack)
                
                // instr req bus
                , .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr.req)
                , .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
                , .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr.ack)
    
                // data req bus
                , .genmcopipe_data_mem_req_genfifo_req_o(cpu_data.req)
                , .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
                , .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data.ack)
    
                // instr resp bus
                , .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr.resp)
                , .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr.rdata)
                // , .genmcopipe_instr_mem_resp_genfifo_ack_o
    
                // data resp bus
                , .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data.resp)
                , .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data.rdata)
                //, .genmcopipe_data_mem_resp_genfifo_ack_o
            );
    
            end
    
        else if (CPU == "riscv_6stage")
        
            begin
    
            genpmodule_riscv_6stage_genmcopipe_instr_mem_genstruct_fifo_wdata instr_mem_struct_bus;
            assign cpu_instr.we     = instr_mem_struct_bus.we;
            assign cpu_instr.addr   = instr_mem_struct_bus.wdata.addr;
            assign cpu_instr.be     = instr_mem_struct_bus.wdata.be;
            assign cpu_instr.wdata  = instr_mem_struct_bus.wdata.wdata;
    
            genpmodule_riscv_6stage_genmcopipe_data_mem_genstruct_fifo_wdata data_mem_struct_bus;
            assign cpu_data.we      = data_mem_struct_bus.we;
            assign cpu_data.addr    = data_mem_struct_bus.wdata.addr;
            assign cpu_data.be      = data_mem_struct_bus.wdata.be;
            assign cpu_data.wdata   = data_mem_struct_bus.wdata.wdata;
    
            riscv_6stage riscv (
                .clk_i(clk_i)
                , .rst_i(cpu_reset)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(cpu_irq_req)
                , .irq_fifo_genfifo_rdata_bi({0, cpu_irq_code})
                , .irq_fifo_genfifo_ack_o(cpu_irq_ack)
                
                // instr req bus
                , .genmcopipe_instr_mem_req_genfifo_req_o(cpu_instr.req)
                , .genmcopipe_instr_mem_req_genfifo_wdata_bo(instr_mem_struct_bus)
                , .genmcopipe_instr_mem_req_genfifo_ack_i(cpu_instr.ack)
    
                // data req bus
                , .genmcopipe_data_mem_req_genfifo_req_o(cpu_data.req)
                , .genmcopipe_data_mem_req_genfifo_wdata_bo(data_mem_struct_bus)
                , .genmcopipe_data_mem_req_genfifo_ack_i(cpu_data.ack)
    
                // instr resp bus
                , .genmcopipe_instr_mem_resp_genfifo_req_i(cpu_instr.resp)
                , .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr.rdata)
                // , .genmcopipe_instr_mem_resp_genfifo_ack_o
    
                // data resp bus
                , .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data.resp)
                , .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data.rdata)
                //, .genmcopipe_data_mem_resp_genfifo_ack_o
            );
    
            end
    
        else
    
            cpu_stub cpu_stub (
                .clk_i(clk_i)
                , .rst_i(cpu_reset)
                
                , .instr_mem(cpu_instr)
                , .data_mem(cpu_data)
            );
        
    endgenerate

    MemSplit32 dmem_if();
    MemSplit32 sfr_if();
    
    generate
        if (PATH_THROUGH == "YES")
            begin

            arb_2m3s
            #(
                .SFR_BITSEL(SFR_BITSEL)
                , .XBUS_BITSEL(XBUS_BITSEL)
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
                .BITSEL(XBUS_BITSEL)
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
    
	
	ram_dual_memsplit #(
		.mem_init(mem_init)
		, .mem_data		(mem_data)
		, .dat_width	(32)
		, .adr_width	(30)
		, .mem_size		(mem_size)
	) ram (
		.clk_i(clk_i)
		, .rst_i(rst_i)

		, .bus0_req_i	(cpu_instr.req)
		, .bus0_we_i	(cpu_instr.we)
		, .bus0_addr_bi	(cpu_instr.addr[31:2])
		, .bus0_be_bi	(cpu_instr.be)
		, .bus0_wdata_bi(cpu_instr.wdata)
		, .bus0_ack_o	(cpu_instr.ack)

		, .bus0_resp_o	(cpu_instr.resp)
		, .bus0_rdata_bo(cpu_instr.rdata)

		, .bus1_req_i	(dmem_if.req)
		, .bus1_we_i	(dmem_if.we)
		, .bus1_addr_bi	(dmem_if.addr[31:2])
		, .bus1_be_bi	(dmem_if.be)
		, .bus1_wdata_bi(dmem_if.wdata)
		, .bus1_ack_o	(dmem_if.ack)

		, .bus1_resp_o	(dmem_if.resp)
		, .bus1_rdata_bo(dmem_if.rdata)
	);
	
    sfr #(
        .corenum(corenum)
        , .CPU_RESET_DEFAULT(CPU_RESET_DEFAULT)
        , .IRQ_NUM_POW(IRQ_NUM_POW)
    ) sfr(
        .clk_i		(clk_i)
        , .rst_i	(rst_i)

        , .host(sfr_if)

        , .cpu_reset_o(cpu_reset)

        , .msi_req_o(msi_req)
        , .msi_code_bo(msi_code)
    );
	

endmodule
