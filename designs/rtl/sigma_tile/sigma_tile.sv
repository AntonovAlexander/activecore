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
	parameter corenum=0, mem_init="YES", mem_data="data.hex", mem_size=1024, CPU="none"
)
(
	input [0:0] clk_i
	, input [0:0] rst_i

    , MemSplit32.Slave hpi
    , MemSplit32.Master xbus
);

	MemSplit32 cpu_instr();
    MemSplit32 cpu_data();
	
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
                , .rst_i(rst_i)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(1'b0)
                , .irq_fifo_genfifo_rdata_bi(0)
                //, .irq_fifo_genfifo_ack_o()
                
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
                , .rst_i(rst_i)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(1'b0)
                , .irq_fifo_genfifo_rdata_bi(0)
                //, .irq_fifo_genfifo_ack_o()
                
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
                , .genmcopipe_instr_mem_resp_genfifo_rdata_bi(cpu_instr_rdata)
                // , .genmcopipe_instr_mem_resp_genfifo_ack_o
    
                // data resp bus
                , .genmcopipe_data_mem_resp_genfifo_req_i(cpu_data.resp)
                , .genmcopipe_data_mem_resp_genfifo_rdata_bi(cpu_data_rdata)
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
                , .rst_i(rst_i)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(1'b0)
                , .irq_fifo_genfifo_rdata_bi(0)
                //, .irq_fifo_genfifo_ack_o()
                
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
                , .rst_i(rst_i)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(1'b0)
                , .irq_fifo_genfifo_rdata_bi(0)
                //, .irq_fifo_genfifo_ack_o()
                
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
                , .rst_i(rst_i)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(1'b0)
                , .irq_fifo_genfifo_rdata_bi(0)
                //, .irq_fifo_genfifo_ack_o()
                
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
                , .rst_i(rst_i)
                
                // interrupt bus
                , .irq_fifo_genfifo_req_i(1'b0)
                , .irq_fifo_genfifo_rdata_bi(0)
                //, .irq_fifo_genfifo_ack_o()
                
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
                , .rst_i(rst_i)
                
                , .instr_mem(cpu_instr)
                , .data_mem(cpu_data)
            );
        
    endgenerate
	
	MemSplit32 cpu_internal();
	
	arb_l0 arb_l0
	(
		.clk_i		(clk_i)
		, .rst_i	(rst_i)

        , .m(cpu_data)
        , .s0(cpu_internal)
        , .s1(xbus)
	);
	
	MemSplit32 internal();

	arb_l1 arb_l1
	(
		.clk_i		(clk_i)
		, .rst_i	(rst_i)
		
		, .m0(cpu_internal)
        , .m1(hpi)
        , .s(internal)
	);
	
	MemSplit32 dmem_data();
    MemSplit32 sfr_data();
	
	arb_l2 arb_l2
	(
		.clk_i		(clk_i)
		, .rst_i	(rst_i)
		
		, .m(internal)
        , .s0(dmem_data)
        , .s1(sfr_data)
	);
	
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

		, .bus1_req_i	(dmem_data.req)
		, .bus1_we_i	(dmem_data.we)
		, .bus1_addr_bi	(dmem_data.addr[31:2])
		, .bus1_be_bi	(dmem_data.be)
		, .bus1_wdata_bi(dmem_data.wdata)
		, .bus1_ack_o	(dmem_data.ack)

		, .bus1_resp_o	(dmem_data.resp)
		, .bus1_rdata_bo(dmem_data.rdata)
	);
	
	sfr #(
		.corenum(corenum)
	) sfr(
		.clk_i		(clk_i)
		, .rst_i	(rst_i)
		
		, .host(sfr_data)
	);
	

endmodule
