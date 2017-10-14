## See LICENSE file for license details

source ../riscv_pipe.tcl

rtl::module riscv_2stage

	riscv_pipe::declare_wrapper_ports

	pipe::pproc instrpipe clk_i rst_i

		riscv_pipe::declare_pcontext


		pipe::pstage IFETCH
			
			riscv_pipe::process_pc
			pipe::mcopipe_rdreq instr_mem [cnct {curinstr_addr curinstr_addr}]

		pipe::pstage EXEC

			pipe::mcopipe_resp instr_mem instr_code
			riscv_pipe::process_decode
			riscv_pipe::process_regfetch

			riscv_pipe::process_alu
			riscv_pipe::process_rd_csr_prev
			riscv_pipe::process_jump_op
			riscv_pipe::process_setup_mem_reqdata
			
			riscv_pipe::process_branch

			# memory access
			begif mem_req
			
				begif mem_cmd
					pipe::mcopipe_wrreq data_mem [cnct {mem_addr mem_be mem_wdata}]
				endif
				begelse
					pipe::mcopipe_rdreq data_mem [cnct {mem_addr mem_be mem_wdata}]
					begif [pipe::mcopipe_resp data_mem mem_rdata]
						s= rd_rdy	1
					endif
				endif

			endif

			riscv_pipe::process_rd_mem_wdata
			riscv_pipe::process_wb

	pipe::endpproc

	riscv_pipe::connect_copipes

#endmodule
