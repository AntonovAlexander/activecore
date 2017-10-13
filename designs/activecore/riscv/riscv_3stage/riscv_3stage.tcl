## See LICENSE file for license details

source ../riscv_pipe.tcl

rtl::module riscv_3stage

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

			# pipeline EXECMEMWB forwarding
			begif [s&& [pipe::isworking EXECMEMWB] [pipe::prr EXECMEMWB rd_req]]
				begif [s== [pipe::prr EXECMEMWB rd_addr] rs1_addr]
					begif [pipe::prr EXECMEMWB rd_rdy]
						s= rs1_rdata [pipe::prr EXECMEMWB rd_wdata]
					endif
				endif
				begif [s== [pipe::prr EXECMEMWB rd_addr] rs2_addr]
					begif [pipe::prr EXECMEMWB rd_rdy]
						s= rs2_rdata [pipe::prr EXECMEMWB rd_wdata]
					endif
				endif
			endif

		pipe::pstage EXECMEMWB

			set ST_EXEC		0
			set ST_MEM		1
			pipe::pvar {0 0} exestate		$ST_EXEC

			begif [s== exestate $ST_EXEC]
				riscv_pipe::process_alu
				riscv_pipe::process_rd_csr_prev
				riscv_pipe::process_jump_op
				riscv_pipe::process_mem_reqdata
				riscv_pipe::process_branch

				begif mem_req
					pipe::accum mem_addr mem_addr
					pipe::accum mem_be mem_be
					pipe::accum mem_wdata mem_wdata
					pipe::accum exestate $ST_MEM
					pipe::pstall
				endif
				begelse
					riscv_pipe::process_wb
				endif
			endif

			begelse
				begif mem_cmd
					pipe::mcopipe_wrreq data_mem [cnct {mem_addr mem_be mem_wdata}]
				endif
				begelse
					pipe::mcopipe_rdreq data_mem [cnct {mem_addr mem_be mem_wdata}]
					begif [pipe::mcopipe_resp data_mem mem_rdata]
						s= rd_rdy	1
					endif
				endif
				riscv_pipe::process_rd_mem_wdata
				riscv_pipe::process_wb
			endif		

	pipe::endpproc

	riscv_pipe::connect_copipes

#endmodule
