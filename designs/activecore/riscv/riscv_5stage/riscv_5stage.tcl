## See LICENSE file for license details

source ../riscv_pipe.tcl

rtl::module riscv_5stage

	riscv_pipe::declare_wrapper_ports

	pipe::pproc instrpipe clk_i rst_i

		riscv_pipe::declare_pcontext


		pipe::pstage IFETCH

			riscv_pipe::process_pc
			pipe::mcopipe::rdreq instr_mem 0 [cnct {curinstr_addr curinstr_addr}]

		pipe::pstage IDECODE

			pipe::mcopipe::resp instr_mem instr_code
			riscv_pipe::process_decode
			riscv_pipe::process_regfetch
			
			riscv_pipe::forward_blocking WB
			riscv_pipe::forward_blocking MEM
			riscv_pipe::forward_blocking EXEC

		pipe::pstage EXEC

			riscv_pipe::process_alu
			riscv_pipe::process_rd_csr_prev
			riscv_pipe::process_curinstraddr_imm
			riscv_pipe::process_jump
			riscv_pipe::process_setup_mem_reqdata

		pipe::pstage MEM
			
			riscv_pipe::process_branch

			# memory access
			begif mem_req
				begif mem_cmd
					pipe::mcopipe::wrreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
				endif
				begelse
					pipe::mcopipe::rdreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
				endif
			endif

		pipe::pstage WB
			
			begif mem_req
				begif [s! mem_cmd]
					begif [pipe::mcopipe::resp data_mem mem_rdata]
						s= rd_rdy	1
					endif
				endif
			endif

			riscv_pipe::process_rd_mem_wdata
			riscv_pipe::process_wb

	pipe::endpproc

	riscv_pipe::connect_copipes

rtl::endmodule
