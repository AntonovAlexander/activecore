## See LICENSE file for license details

try {namespace delete dlx} on error {} {}
namespace eval dlx {

	# opcodes
	set ALU_ADD		0
	set ALU_SUB		1
	set ALU_AND		2
	set ALU_OR		3
	set ALU_SRA		4
	set ALU_SLL		5
	set ALU_SRL		6
	set ALU_XOR		7

	# op1 sources
	set OP1_SRC_REG 0
	set OP1_SRC_PC 	1
	# op2 sources
	set OP2_SRC_REG 0
	set OP2_SRC_IMM 1

	# rd sources
	set RD_ALU_RES	0
	set RD_PC_INC	1
	set RD_LHI		2
	set RD_ZF_COND	3
	set RD_nZF_COND	4
	set RD_CF_COND	5
	set RD_MEM		6

	# jmp sources
	set JMP_SRC_OP1 0
	set JMP_SRC_ALU 1

	set opcode_ADD	0x20
	set opcode_ADDI	0x08
	set opcode_AND	0x24
	set opcode_ANDI	0x0C
	set opcode_BEQZ	0x04
	set opcode_BNEZ	0x05
	set opcode_J	0x02
	set opcode_JAL	0x03
	set opcode_JALR	0x13
	set opcode_JR	0x12
	set opcode_LHI	0x0F
	set opcode_LW	0x23
	set opcode_OR	0x25
	set opcode_ORI	0x0D
	set opcode_SEQ	0x28
	set opcode_SEQI	0x18
	set opcode_SLE	0x2C
	set opcode_SLEI	0x1C
	set opcode_SLL	0x04
	set opcode_SLLI	0x14
	set opcode_SLT	0x2A
	set opcode_SLTI	0x1A
	set opcode_SNE	0x29
	set opcode_SNEI	0x19
	set opcode_SRA	0x07
	set opcode_SRAI	0x17
	set opcode_SRL	0x06
	set opcode_SRLI	0x16
	set opcode_SUB	0x22
	set opcode_SUBI	0x0A
	set opcode_SW	0x2B
	set opcode_XOR	0x26
	set opcode_XORI	0x0E
}


rtl::module dlx

	rtl::input 	{0 0} 	clk_i
	rtl::input 	{0 0} 	rst_i
	
	rtl::output {0 0} 	instr_mem_req
	rtl::output {0 0} 	instr_mem_we
	rtl::input 	{0 0} 	instr_mem_ack
	rtl::output {31 0} 	instr_mem_addr
	rtl::output {31 0} 	instr_mem_wdata
	rtl::output	{3 0}	instr_mem_be
	rtl::input 	{0 0}	instr_mem_resp
	rtl::input 	{31 0}	instr_mem_rdata

	rtl::output {0 0} 	data_mem_req
	rtl::output {0 0} 	data_mem_we
	rtl::input 	{0 0} 	data_mem_ack
	rtl::output {31 0} 	data_mem_addr
	rtl::output {31 0} 	data_mem_wdata
	rtl::output	{3 0}	data_mem_be
	rtl::input 	{0 0}	data_mem_resp
	rtl::input 	{31 0}	data_mem_rdata

	rtl::comb 	{0 0} 	instr_mcopipe_req 	0
	rtl::comb 	{0 0} 	instr_mcopipe_we 	0
	rtl::comb 	{0 0} 	instr_mcopipe_ack 	0
	rtl::comb 	{63 0} 	instr_mcopipe_wdata	0
	rtl::comb	{0 0}	instr_mcopipe_resp	0
	rtl::comb 	{31 0}	instr_mcopipe_rdata	0

	rtl::comb 	{0 0} 	data_mcopipe_req		0
	rtl::comb 	{0 0} 	data_mcopipe_we		0
	rtl::comb 	{0 0} 	data_mcopipe_ack		0
	rtl::comb 	{63 0} 	data_mcopipe_wdata	0
	rtl::comb	{0 0}	data_mcopipe_resp	0
	rtl::comb 	{31 0}	data_mcopipe_rdata	0

	s= instr_mem_req instr_mcopipe_req
	s= instr_mem_we instr_mcopipe_we
	s= instr_mcopipe_ack instr_mem_ack
	s= instr_mem_addr [indexed instr_mcopipe_wdata {63 32}]
	s= instr_mem_wdata [indexed instr_mcopipe_wdata {31 0}]
	s= instr_mem_be 0xf
	s= instr_mcopipe_resp instr_mem_resp
	s= instr_mcopipe_rdata instr_mem_rdata

	s= data_mem_req data_mcopipe_req
	s= data_mem_we data_mcopipe_we
	s= data_mcopipe_ack data_mem_ack
	s= data_mem_addr [indexed data_mcopipe_wdata {63 32}]
	s= data_mem_wdata [indexed data_mcopipe_wdata {31 0}]
	s= data_mem_be 0xf
	s= data_mcopipe_resp data_mem_resp
	s= data_mcopipe_rdata data_mem_rdata

	pipe::pproc instrpipe clk_i rst_i

		# transaction context
		pipe::pvar {0 0} 	reset_active	0
		pipe::pvar {31 0} 	curinstr_addr	0
		pipe::pvar {31 0} 	nextinstr_addr	0
		pipe::pvar {31 0} 	instr_code		0

		# opcode signals
		pipe::pvar {5 0}	opcode 			$dlx::ALU_ADD

		# control transfer signlas
		pipe::pvar {0 0} 	jump_req		0
		pipe::pvar {0 0}	jump_src		$dlx::JMP_SRC_OP1
		pipe::pvar {0 0} 	jump_cond 		0
		pipe::pvar {0 0}	jump_cond_eqz 	0
		pipe::pvar {31 0} 	jump_vector		0

		# regfile control signals
		pipe::pvar {0 0}	rs1_req 		0
		pipe::pvar {4 0} 	rs1_addr		0
		pipe::pvar {31 0}	rs1_rdata 		0
		pipe::pvar {0 0}	rs2_req 		0
		pipe::pvar {4 0} 	rs2_addr		0
		pipe::pvar {31 0}	rs2_rdata 		0
		pipe::pvar {0 0}	rd_req 			0
		pipe::pvar {4 0} 	rd_addr			0
		pipe::pvar {31 0}	rd_wdata 		0

		pipe::pvar {31 0}	immediate		0

		pipe::pvar {0 0}	op1_source		$dlx::OP1_SRC_REG
		pipe::pvar {0 0}	op2_source		$dlx::OP2_SRC_REG
		pipe::pvar {2 0} 	rd_source		$dlx::RD_ALU_RES

		# ALU control
		pipe::pvar {0 0} 	alu_req			0
		pipe::pvar {31 0}	alu_op1			0
		pipe::pvar {31 0}	alu_op2			0
		pipe::pvar {2 0}	alu_opcode		0
		pipe::pvar {32 0}	alu_result_wide 0
		pipe::pvar {31 0}	alu_result 		0
		pipe::pvar {0 0} 	alu_CF			0
		pipe::pvar {0 0} 	alu_SF			0
		pipe::pvar {0 0} 	alu_ZF			0

		# data memory control
		pipe::pvar {0 0} 	mem_req 		0
		pipe::pvar {0 0} 	mem_cmd			0
		pipe::pvar {31 0} 	mem_addr		0
		pipe::pvar {31 0}	mem_wdata		0
		pipe::pvar {31 0} 	mem_rdata 		0
		pipe::pvar {0 0} 	mem_rshift		0
		
		pipe::gpvar_sync {31 0} 	pc				0
		_acc_index {31 0}	
		pipe::gpvar_sync {31 0} 	regfile			0
		pipe::gpvar_sync {0 0}		jump_req_cmd	0
		pipe::gpvar_sync {31 0} 	jump_vector_cmd	0

		pipe::mcopipeif instr_mem {63 0} {31 0}
		pipe::mcopipeif data_mem {63 0} {31 0}

		pipe::pstage IFETCH
			
			s= curinstr_addr pc

			begif jump_req_cmd
				s= curinstr_addr jump_vector_cmd
			endif
			s= jump_req_cmd 0

			pipe::mcopipe_rdreq instr_mem [cnct {curinstr_addr curinstr_addr}]

			s= nextinstr_addr [s+ curinstr_addr 4]

			s= pc nextinstr_addr
		
		pipe::pstage IDECODE

			pipe::mcopipe_resp instr_mem instr_code

			s= opcode [indexed instr_code {31 26}]
			begif [s== opcode 0]
				s= opcode [indexed instr_code {5 0}]
			endif

			s= rs1_addr [indexed instr_code {25 21}]
			s= rs2_addr [indexed instr_code {20 16}]
			s= rd_addr  [indexed instr_code {15 11}]

			s= immediate [signext [indexed instr_code {15 0}] 32]
			
			begif [s== opcode $dlx::opcode_ADD]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_ADD
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_ADDI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_ADD
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_AND]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_AND
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_ANDI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_AND
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_BEQZ]
				s= jump_req 		1
				s= jump_cond 		1
				s= jump_cond_eqz 	1
				s= jump_src			$dlx::JMP_SRC_ALU
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		0
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_ADD
				s= op1_source 	$dlx::OP1_SRC_PC
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_BNEZ]
				s= jump_req 		1
				s= jump_cond 		1
				s= jump_cond_eqz 	0
				s= jump_src			$dlx::JMP_SRC_ALU
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		0
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_ADD
				s= op1_source 	$dlx::OP1_SRC_PC
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_J]
				s= jump_req 	1
				s= jump_src		$dlx::JMP_SRC_ALU
				s= rs2_req 		0
				s= rd_req 		0
				s= rd_source	$dlx::RD_PC_INC
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_ADD
				s= op1_source 	$dlx::OP1_SRC_PC
				s= op2_source 	$dlx::OP2_SRC_IMM
				s= immediate [signext [indexed instr_code {25 0}] 32]
			endif

			begif [s== opcode $dlx::opcode_JAL]
				s= jump_req 	1
				s= jump_src		$dlx::JMP_SRC_ALU
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_source 	$dlx::RD_PC_INC
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_ADD
				s= op1_source 	$dlx::OP1_SRC_PC
				s= op2_source 	$dlx::OP2_SRC_IMM
				s= rd_addr 31
				s= immediate [signext [indexed instr_code {25 0}] 32]
			endif

			begif [s== opcode $dlx::opcode_JALR]
				s= jump_req 		1
				s= jump_src			$dlx::JMP_SRC_OP1
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_source 	$dlx::RD_PC_INC
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		0
				s= rd_addr 31
			endif

			begif [s== opcode $dlx::opcode_JR]
				s= jump_req 		1
				s= jump_src			$dlx::JMP_SRC_OP1
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		0
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		0
			endif

			begif [s== opcode $dlx::opcode_LHI]
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_source 	$dlx::RD_LHI
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		0
			endif

			begif [s== opcode $dlx::opcode_LW]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_source 	$dlx::RD_MEM
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_ADD
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
				s= mem_req 		1
				s= mem_cmd 		0
			endif

			begif [s== opcode $dlx::opcode_OR]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_OR
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_ORI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_OR
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_SEQ]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= rd_source 	$dlx::RD_ZF_COND
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SUB
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_SEQI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_source 	$dlx::RD_ZF_COND
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SUB
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_SLE]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= rd_source 	$dlx::RD_CF_COND
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SUB
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_SLEI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_source 	$dlx::RD_CF_COND
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SUB
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_SLL]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SLL
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_SLLI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SLL
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_SLT]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SUB
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_SLTI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SUB
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_SNE]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= rd_source 	$dlx::RD_nZF_COND
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SUB
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_SNEI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_source 	$dlx::RD_nZF_COND
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SUB
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_SRA]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SRA
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_SRAI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SRA
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_SRL]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SRL
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_SRLI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SRL
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_SUB]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SUB
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_SUBI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_SUB
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			begif [s== opcode $dlx::opcode_SW]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		0
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_ADD
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
				s= mem_req 		1
				s= mem_cmd 		1
			endif

			begif [s== opcode $dlx::opcode_XOR]
				s= rs1_req 		1
				s= rs2_req 		1
				s= rd_req 		1
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_XOR
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_REG
			endif

			begif [s== opcode $dlx::opcode_XORI]
				s= rs1_req 		1
				s= rs2_req 		0
				s= rd_req 		1
				s= rd_addr  [indexed instr_code {20 16}]
				s= alu_req		1
				s= alu_opcode 	$dlx::ALU_XOR
				s= op1_source 	$dlx::OP1_SRC_REG
				s= op2_source 	$dlx::OP2_SRC_IMM
			endif

			#### data fetching - reading regfile ##
			
			## unoptimized
			# s= rs1_rdata [indexed regfile rs1_addr]
			# s= rs2_rdata [indexed regfile rs2_addr]
			##
			
			## optimized for synthesis
			s= rs1_rdata [indexed [pipe::rdprev regfile] rs1_addr]
			s= rs2_rdata [indexed [pipe::rdprev regfile] rs2_addr]
			# pipeline WB forwarding
			begif [pipe::issucc WB]
				begif [pipe::prr WB rd_req]
					begif [s== [pipe::prr WB rd_addr] rs1_addr]
						s= rs1_rdata [pipe::prr WB rd_wdata]
					endif
					begif [s== [pipe::prr WB rd_addr] rs2_addr]
						s= rs2_rdata [pipe::prr WB rd_wdata]
					endif
				endif
			endif
			##

			begif [s== rs1_addr 0]
				s= rs1_rdata 0
			endif

			begif [s== rs2_addr 0]
				s= rs2_rdata 0
			endif

		pipe::pstage EXEC

			# pipeline WB forwarding
			begif [pipe::isworking WB]
				begif [pipe::issucc WB]
					begif [s== [pipe::prr WB rd_addr] rs1_addr]
						pipe::accum rs1_rdata [pipe::prr WB rd_wdata]
					endif
					begif [s== [pipe::prr WB rd_addr] rs2_addr]
						pipe::accum rs2_rdata [pipe::prr WB rd_wdata]
					endif
				endif
				begelse
					pipe::pstall
				endif
			endif


			# pipeline MEM forwarding
			begif [pipe::isworking MEM]
				begif [pipe::issucc MEM]
					begif [s== [pipe::prr MEM rd_addr] rs1_addr]
						begif [s&& [pipe::prr MEM mem_req] [s~ [pipe::prr MEM mem_cmd]]]
							pipe::pstall
						endif
						begelse
							pipe::accum rs1_rdata [pipe::prr MEM rd_wdata]
						endif
					endif
					begif [s== [pipe::prr MEM rd_addr] rs2_addr]
						begif [s&& [pipe::prr MEM mem_req] [s~ [pipe::prr MEM mem_cmd]]]
							pipe::pstall
						endif
						begelse
							pipe::accum rs2_rdata [pipe::prr MEM rd_wdata]
						endif
					endif
				endif
				begelse
					pipe::pstall
				endif
			endif

			# ALU processing
			begif [s== op1_source $dlx::OP1_SRC_REG]
				s= alu_op1 rs1_rdata
			endif

			begif [s== op1_source $dlx::OP1_SRC_PC]
				s= alu_op1 nextinstr_addr
			endif

			begif [s== op2_source $dlx::OP2_SRC_REG]
				s= alu_op2 rs2_rdata
			endif

			begif [s== op2_source $dlx::OP2_SRC_IMM]
				s= alu_op2 immediate
			endif

			begif [s== alu_opcode $dlx::ALU_ADD]
				s= alu_result_wide [s+ alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $dlx::ALU_SUB]
				s= alu_result_wide [s- alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $dlx::ALU_AND]
				s= alu_result_wide [s& alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $dlx::ALU_OR]
				s= alu_result_wide [s| alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $dlx::ALU_XOR]
				s= alu_result_wide [s^ alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $dlx::ALU_SRL]
				s= alu_result_wide [s>> alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $dlx::ALU_SRA]
				s= alu_result_wide [s>>> alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $dlx::ALU_SLL]
				s= alu_result_wide [s<< alu_op1 alu_op2]
			endif

			s= alu_result [indexed alu_result_wide {31 0}]
			s= alu_CF [indexed alu_result_wide {32}]
			s= alu_SF [indexed alu_result_wide {31}]
			begif [s== alu_result 0]
				s= alu_ZF 1
			endif
			
			# rd wdata processing
			begif [s== rd_source $dlx::RD_ALU_RES]
				s= rd_wdata alu_result
			endif

			begif [s== rd_source $dlx::RD_PC_INC]
				s= rd_wdata nextinstr_addr
			endif

			begif [s== rd_source $dlx::RD_LHI]
				s= rd_wdata [s<< immediate 16]
			endif

			begif [s== rd_source $dlx::RD_ZF_COND]
				s= rd_wdata alu_ZF
			endif

			begif [s== rd_source $dlx::RD_nZF_COND]
				s= rd_wdata [s! alu_ZF]
			endif

			begif [s== rd_source $dlx::RD_CF_COND]
				s= rd_wdata alu_CF
			endif

			# jump vector processing
			begif jump_cond
				begif [s&& jump_cond_eqz [s!= rs1_rdata 0]]
					s= jump_req 0
				endif
				begif [s&& [s! jump_cond_eqz] [s== rs1_rdata 0]]
					s= jump_req 0
				endif
			endif

			# mem addr processing
			s= mem_addr alu_result
			s= mem_wdata rs2_rdata

			begif [s== jump_src $dlx::JMP_SRC_OP1]
				s= jump_vector alu_op1
			endif

			begif [s== jump_src $dlx::JMP_SRC_ALU]
				s= jump_vector alu_result
			endif

			s= jump_req_cmd jump_req
			s= jump_vector_cmd jump_vector
			begif jump_req
				pipe::pflush
			endif

		pipe::pstage MEM
			
			begif mem_cmd
				pipe::mcopipe_wrreq data_mem [cnct {mem_addr mem_wdata}]
			endif
			begelse
				pipe::mcopipe_rdreq data_mem [cnct {mem_addr mem_wdata}]
			endif

		pipe::pstage WB
			
			begif mem_req
				begif [s! mem_cmd]
					pipe::mcopipe_resp data_mem mem_rdata
				endif
			endif

			begif [s== rd_source $dlx::RD_MEM]
				s= rd_wdata mem_rdata
			endif

			begif rd_req
				_acc_index rd_addr
				s= regfile rd_wdata
			endif

	pipe::endpproc


	pipe::copipeif instr_mem {63 0} {31 0}
	pipe::copipeif data_mem {63 0} {31 0}

	pipe::mcopipe_connect instrpipe instr_mem instr_mem
	pipe::mcopipe_connect instrpipe data_mem data_mem

	pipe::mcopipe_export instr_mem  { \
				instr_mcopipe_req 	\
				instr_mcopipe_we 	\
				instr_mcopipe_ack 	\
				instr_mcopipe_wdata	\
				instr_mcopipe_resp	\
				instr_mcopipe_rdata	\
			}

	pipe::mcopipe_export data_mem { \
				data_mcopipe_req	\
				data_mcopipe_we		\
				data_mcopipe_ack	\
				data_mcopipe_wdata	\
				data_mcopipe_resp	\
				data_mcopipe_rdata	\
			}

#endmodule
