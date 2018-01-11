## See LICENSE file for license details

source ../riscv_pipe.tcl

rtl::module riscv_3stage

	riscv_pipe::declare_wrapper_ports

	pipe::pproc instrpipe

		riscv_pipe::declare_pcontext


		pipe::pstage IFETCH
			
			riscv_pipe::process_pc
			pipe::mcopipe::rdreq instr_mem 0 [cnct {curinstr_addr curinstr_addr}]

		pipe::pstage EXEC

			pipe::mcopipe::resp instr_mem instr_code
			riscv_pipe::process_decode
			riscv_pipe::process_regfetch
			riscv_pipe::forward_unblocking EXECMEMWB

		pipe::pstage EXECMEMWB

			set ST_EXEC		0
			set ST_MEM		1
			pipe::pvar {0 0} exestate		$ST_EXEC

			acif::begin [ac== exestate $ST_EXEC]
				riscv_pipe::process_alu
				riscv_pipe::process_rd_csr_prev
				riscv_pipe::process_curinstraddr_imm
				riscv_pipe::process_jump
				riscv_pipe::process_setup_mem_reqdata
				riscv_pipe::process_branch

				acif::begin mem_req
					pipe::accum mem_addr mem_addr
					pipe::accum mem_be mem_be
					pipe::accum mem_wdata mem_wdata
					pipe::accum exestate $ST_MEM
					pipe::pstall
				acif::end
				acif::begelse
					riscv_pipe::process_wb
				acif::end
			acif::end

			acif::begelse
				acif::begin mem_cmd
					pipe::mcopipe::wrreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
				acif::end
				acif::begelse
					pipe::mcopipe::rdreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
					acif::begin [pipe::mcopipe::resp data_mem mem_rdata]
						ac= rd_rdy	1
					acif::end
				acif::end
				riscv_pipe::process_rd_mem_wdata
				riscv_pipe::process_wb
			acif::end		

	pipe::endpproc

	riscv_pipe::connect_copipes

rtl::endmodule
