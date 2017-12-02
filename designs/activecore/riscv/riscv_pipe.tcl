## See LICENSE file for license details

try {namespace delete riscv_pipe} on error {} {}
namespace eval riscv_pipe {

	set START_ADDR			512

	# base opcodes
	set opcode_LOAD			0x03
	set opcode_LOAD_FP		0x07
	set opcode_MISC_MEM		0x0f
	set opcode_OP_IMM		0x13
	set opcode_AUIPC		0x17
	set opcode_OP_IMM_32	0x1b
	set opcode_STORE		0x23
	set opcode_STORE_FP		0x27
	set opcode_AMO			0x2f
	set opcode_OP			0x33
	set opcode_LUI			0x37
	set opcode_OP_32		0x3b
	set opcode_MADD			0x43
	set opcode_MSUB			0x47
	set opcode_NMSUB		0x4b
	set opcode_NMADD		0x4f
	set opcode_OP_FP		0x53
	set opcode_BRANCH		0x63
	set opcode_JALR			0x67
	set opcode_JAL			0x6f
	set opcode_SYSTEM		0x73

	# ALU opcodes
	set aluop_ADD		0
	set aluop_SUB		1
	set aluop_AND		2
	set aluop_OR		3
	set aluop_SRA		4
	set aluop_SLL		5
	set aluop_SRL		6
	set aluop_XOR		7
	set aluop_CLRB		8

	# op1 sources
	set OP1_SRC_RS1 0
	set OP1_SRC_IMM 1
	set OP1_SRC_PC 	2
	# op2 sources
	set OP2_SRC_RS2 0
	set OP2_SRC_IMM 1
	set OP2_SRC_CSR 2

	# rd sources
	set RD_LUI		2
	set RD_ALU		0
	set RD_CF_COND	4
	set RD_OF_COND	3
	set RD_PC_INC	1
	set RD_MEM		5
	set RD_CSR		6

	# jmp sources
	set JMP_SRC_OP1 0
	set JMP_SRC_ALU 1

	## wrapper ports
	proc declare_wrapper_ports {} {
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
		rtl::comb 	{67 0} 	data_mcopipe_wdata	0
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
		s= data_mem_addr [indexed data_mcopipe_wdata {67 36}]
		s= data_mem_be [indexed data_mcopipe_wdata {35 32}]
		s= data_mem_wdata [indexed data_mcopipe_wdata {31 0}]
		s= data_mcopipe_resp data_mem_resp
		s= data_mcopipe_rdata data_mem_rdata
	}

	## transaction context
	proc declare_pcontext {} {
		pipe::pvar {0 0} 	reset_active	0
		pipe::pvar {31 0} 	curinstr_addr	0
		pipe::pvar {31 0} 	nextinstr_addr	0
		pipe::pvar {31 0} 	instr_code		0

		# opcode signals
		pipe::pvar {6 0}	opcode 			$riscv_pipe::aluop_ADD

		# control transfer signlas
		pipe::pvar {0 0} 	jump_req		0
		pipe::pvar {0 0} 	jump_req_cond	0
		pipe::pvar {0 0}	jump_src		$riscv_pipe::JMP_SRC_OP1
		pipe::pvar {31 0} 	jump_vector		0

		# regfile control signals
		pipe::pvar {0 0}	rs1_req 		0
		pipe::pvar {4 0} 	rs1_addr		0
		pipe::pvar {31 0}	rs1_rdata 		0

		pipe::pvar {0 0}	rs2_req 		0
		pipe::pvar {4 0} 	rs2_addr		0
		pipe::pvar {31 0}	rs2_rdata 		0

		pipe::pvar {0 0}	rd_req 			0
		pipe::pvar {2 0} 	rd_source		$riscv_pipe::RD_ALU
		pipe::pvar {4 0} 	rd_addr			0
		pipe::pvar {31 0}	rd_wdata 		0
		pipe::pvar {0 0}	rd_rdy 			0

		pipe::pvar {31 0}	immediate_I		0
		pipe::pvar {31 0}	immediate_S		0
		pipe::pvar {31 0}	immediate_B		0
		pipe::pvar {31 0}	immediate_U		0
		pipe::pvar {31 0}	immediate_J		0

		pipe::pvar {31 0}	immediate		0

		pipe::pvar {31 0}	curinstraddr_imm	0

		pipe::pvar {2 0}	funct3			0
		pipe::pvar {6 0}	funct7			0
		pipe::pvar {4 0}	shamt			0
		
		pipe::pvar {0 0}	fencereq		0
		pipe::pvar {3 0}	pred			0
		pipe::pvar {3 0}	succ			0

		pipe::pvar {0 0}	ecallreq		0
		pipe::pvar {0 0}	ebreakreq		0

		pipe::pvar {0 0}	csrreq			0
		pipe::pvar {11 0} 	csrnum 			0
		pipe::pvar {4 0}	zimm			0

		pipe::pvar {1 0}	op1_source		$riscv_pipe::OP1_SRC_RS1
		pipe::pvar {1 0}	op2_source		$riscv_pipe::OP2_SRC_RS2

		# ALU control
		pipe::pvar {0 0} 	alu_req			0
		pipe::pvar {31 0}	alu_op1			0
		pipe::pvar {31 0}	alu_op2			0
		pipe::pvar {3 0}	alu_opcode		0
		pipe::pvar {0 0} 	alu_unsigned	0

		pipe::pvar {32 0}	alu_op1_wide	0
		pipe::pvar {32 0}	alu_op2_wide	0
		pipe::pvar {32 0}	alu_result_wide 0
		pipe::pvar {31 0}	alu_result 		0
		pipe::pvar {0 0} 	alu_CF			0
		pipe::pvar {0 0} 	alu_SF			0
		pipe::pvar {0 0} 	alu_ZF			0
		pipe::pvar {0 0} 	alu_OF			0
		pipe::pvar {0 0} 	alu_overflow	0

		# data memory control
		pipe::pvar {0 0} 	mem_req 		0
		pipe::pvar {0 0} 	mem_cmd			0
		pipe::pvar {31 0} 	mem_addr		0
		pipe::pvar {3 0}	mem_be 			0
		pipe::pvar {31 0}	mem_wdata		0
		pipe::pvar {31 0} 	mem_rdata 		0
		pipe::pvar {0 0} 	mem_rshift		0
		
		pipe::gpvar	{31 0} 	pc				$riscv_pipe::START_ADDR
		_acc_index 	{31 1}	
		pipe::gpvar {31 0} 	regfile			0
		pipe::gpvar {0 0}	jump_req_cmd	0
		pipe::gpvar {31 0} 	jump_vector_cmd	0

		# TODO: CSRs

		pipe::_acc_index_wdata {63 0}
		pipe::_acc_index_rdata {31 0}
		pipe::mcopipe::declare {0 0} instr_mem

		pipe::_acc_index_wdata {67 0}
		pipe::_acc_index_rdata {31 0}
		pipe::mcopipe::declare {0 0} data_mem
	}

	# RISC-V pipeline macro-operations
	proc process_pc {} {
		s= curinstr_addr pc

		begif jump_req_cmd
			s= curinstr_addr jump_vector_cmd
		endif
		pipe::s<= jump_req_cmd 0

		s= nextinstr_addr [s+ curinstr_addr 4]

		pipe::s<= pc nextinstr_addr
	}

	proc process_decode {} {
		s= opcode [indexed instr_code {6 0}]
		s= alu_unsigned 0

		s= rs1_addr [indexed instr_code {19 15}]
		s= rs2_addr [indexed instr_code {24 20}]
		s= rd_addr  [indexed instr_code {11 7}]

		s= funct3	[indexed instr_code {14 12}]
		s= funct7	[indexed instr_code {31 25}]
		s= shamt 	[indexed instr_code {24 20}]
		s= pred 	[indexed instr_code {27 24}]
		s= succ		[indexed instr_code {23 20}]
		s= csrnum 	[indexed instr_code {31 20}]
		s= zimm		[indexed instr_code {19 15}]

		s= immediate_I	[signext [indexed instr_code {31 20}] 32]
		s= immediate_S	[signext [cnct [list	[indexed instr_code {31 25}] \
												[indexed instr_code {11 7}] \
							]] 32]
		s= immediate_B	[signext [cnct [list	[indexed instr_code 31] \
												[indexed instr_code 7] \
												[indexed instr_code {30 25}] \
												[indexed instr_code {11 8}] \
												[initval {0 0} 0] \
							]] 32]
		s= immediate_U	[cnct [list				[indexed instr_code {31 12}] \
												[initval {11 0} 0] \
							]]
		s= immediate_J	[signext [cnct [list	[indexed instr_code 31] \
												[indexed instr_code {19 12}] \
												[indexed instr_code 20] \
												[indexed instr_code {30 21}] \
												[initval {0 0} 0] \
							]] 32]

		begif [s== opcode $riscv_pipe::opcode_LUI]
			s= op1_source $riscv_pipe::OP1_SRC_IMM
			s= rd_req 		1
			s= rd_source	$riscv_pipe::RD_LUI
			s= immediate immediate_U
		endif

		begif [s== opcode $riscv_pipe::opcode_AUIPC]
			s= op1_source	$riscv_pipe::OP1_SRC_PC
			s= op2_source	$riscv_pipe::OP2_SRC_IMM
			s= alu_req		1
			s= alu_opcode 	$riscv_pipe::aluop_ADD
			s= rd_req		1
			s= rd_source	$riscv_pipe::RD_ALU

			s= immediate immediate_U
		endif

		begif [s== opcode $riscv_pipe::opcode_JAL]
			s= op1_source 	$riscv_pipe::OP1_SRC_PC
			s= op2_source 	$riscv_pipe::OP2_SRC_IMM
			s= alu_req		1
			s= alu_opcode 	$riscv_pipe::aluop_ADD
			s= rd_req		1
			s= rd_source	$riscv_pipe::RD_PC_INC
			s= jump_req 	1
			s= jump_src		$riscv_pipe::JMP_SRC_ALU
			s= immediate immediate_J
		endif

		begif [s== opcode $riscv_pipe::opcode_JALR]
			s= rs1_req		1
			s= op1_source 	$riscv_pipe::OP1_SRC_RS1
			s= op2_source 	$riscv_pipe::OP2_SRC_IMM
			s= alu_req		1
			s= alu_opcode 	$riscv_pipe::aluop_ADD
			s= rd_req		1
			s= rd_source	$riscv_pipe::RD_PC_INC
			s= jump_req 	1
			s= jump_src		$riscv_pipe::JMP_SRC_ALU
			s= immediate immediate_I
		endif

		begif [s== opcode $riscv_pipe::opcode_BRANCH]
			s= rs1_req		1
			s= rs2_req		1
			s= alu_req		1
			s= alu_opcode 	$riscv_pipe::aluop_SUB
			s= jump_req_cond 1
			s= jump_src		$riscv_pipe::JMP_SRC_ALU
			s= immediate immediate_B

			begif [s|| [s== funct3 0x6] [s== funct3 0x7] ]
				s= alu_unsigned 1
			endif

		endif

		begif [s== opcode $riscv_pipe::opcode_LOAD]
			s= rs1_req 		1
			s= op1_source 	$riscv_pipe::OP1_SRC_RS1
			s= op2_source 	$riscv_pipe::OP2_SRC_IMM
			s= rd_req		1
			s= rd_source	$riscv_pipe::RD_MEM

			s= alu_req		1

			s= mem_req 		1
			s= mem_cmd		0

			begif [s|| [s== funct3 0x0] [s== funct3 0x4] ]
				s= mem_be 0x1
			endif
			begif [s|| [s== funct3 0x1] [s== funct3 0x5] ]
				s= mem_be 0x3
			endif
			begif [s== funct3 0x2]
				s= mem_be 0xf
			endif

			s= immediate immediate_I
		endif

		begif [s== opcode $riscv_pipe::opcode_STORE]
			s= rs1_req 		1
			s= rs2_req 		1
			s= op1_source 	$riscv_pipe::OP1_SRC_RS1
			s= op2_source 	$riscv_pipe::OP2_SRC_IMM

			s= alu_req		1
			
			s= mem_req 		1
			s= mem_cmd		1

			begif [s== funct3 0x0]
				s= mem_be 0x1
			endif

			begif [s== funct3 0x1]
				s= mem_be 0x3
			endif

			begif [s== funct3 0x2]
				s= mem_be 0xf
			endif

			s= immediate immediate_S
		endif

		begif [s== opcode $riscv_pipe::opcode_OP_IMM]
			s= rs1_req 		1
			s= op1_source 	$riscv_pipe::OP1_SRC_RS1
			s= op2_source 	$riscv_pipe::OP2_SRC_IMM
			s= rd_req 		1

			s= immediate 	immediate_I

			s= alu_req		1

			# ADDI
			begif [s== funct3 0x0]
				s= alu_opcode $riscv_pipe::aluop_ADD
				s= rd_source 	$riscv_pipe::RD_ALU
			endif

			# SLTI
			begif [s== funct3 0x2]
				s= alu_opcode $riscv_pipe::aluop_SUB
				s= rd_source $riscv_pipe::RD_OF_COND
			endif

			# SLTIU
			begif [s== funct3 0x3]
				s= alu_opcode $riscv_pipe::aluop_SUB
				s= alu_unsigned 1
				s= rd_source $riscv_pipe::RD_CF_COND
			endif

			# XORI
			begif [s== funct3 0x4]
				s= alu_opcode $riscv_pipe::aluop_XOR
				s= rd_source $riscv_pipe::RD_ALU
			endif

			# ORI
			begif [s== funct3 0x6]
				s= alu_opcode $riscv_pipe::aluop_OR
				s= rd_source $riscv_pipe::RD_ALU
			endif

			# ANDI
			begif [s== funct3 0x7]
				s= alu_opcode $riscv_pipe::aluop_AND
				s= rd_source $riscv_pipe::RD_ALU
			endif

			# SLLI
			begif [s== funct3 0x1]
				s= alu_opcode $riscv_pipe::aluop_SLL
				s= rd_source $riscv_pipe::RD_ALU
				s= immediate [zeroext [indexed instr_code {24 20}] 32]
			endif

			# SRLI, SRAI
			begif [s== funct3 0x5]
				# SRAI
				begif [indexed instr_code 30]
					s= alu_opcode $riscv_pipe::aluop_SRA
				endif
				# SRLI
				begelse
					s= alu_opcode $riscv_pipe::aluop_SRL
				endif
				s= rd_source $riscv_pipe::RD_ALU
				s= immediate [zeroext [indexed instr_code {24 20}] 32]
			endif

		endif

		begif [s== opcode $riscv_pipe::opcode_OP]
			s= rs1_req 		1
			s= op1_source 	$riscv_pipe::OP1_SRC_RS1
			s= op2_source 	$riscv_pipe::OP2_SRC_RS2
			s= rd_req 		1
			s= rd_source 	$riscv_pipe::RD_ALU

			s= alu_req		1

			# ADD
			begif [s== funct3 0x0]
				# SUB
				begif [indexed instr_code 30]
					s= alu_opcode $riscv_pipe::aluop_SUB
				endif
				# ADD
				begelse
					s= alu_opcode $riscv_pipe::aluop_ADD
				endif
				s= rd_source 	$riscv_pipe::RD_ALU
			endif

			# SLL
			begif [s== funct3 0x1]
				s= alu_opcode $riscv_pipe::aluop_SLL
				s= rd_source $riscv_pipe::RD_OF_COND
			endif

			# SLT
			begif [s== funct3 0x2]
				s= alu_opcode $riscv_pipe::aluop_SUB
				s= rd_source $riscv_pipe::RD_OF_COND
			endif

			# SLTU
			begif [s== funct3 0x3]
				s= alu_opcode $riscv_pipe::aluop_SUB
				s= alu_unsigned 1
				s= rd_source $riscv_pipe::RD_CF_COND
			endif

			# XORI
			begif [s== funct3 0x4]
				s= alu_opcode $riscv_pipe::aluop_XOR
				s= rd_source $riscv_pipe::RD_ALU
			endif

			# SRL, SRA
			begif [s== funct3 0x5]
				# SRA
				begif [indexed instr_code 30]
					s= alu_opcode $riscv_pipe::aluop_SRA
				endif
				# SRL
				begelse
					s= alu_opcode $riscv_pipe::aluop_SRL
				endif
				s= rd_source $riscv_pipe::RD_ALU
			endif

			# OR
			begif [s== funct3 0x6]
				s= alu_opcode $riscv_pipe::aluop_OR
				s= rd_source $riscv_pipe::RD_ALU
			endif

			# AND
			begif [s== funct3 0x7]
				s= alu_opcode $riscv_pipe::aluop_AND
				s= rd_source $riscv_pipe::RD_ALU
			endif

		endif

		begif [s== opcode $riscv_pipe::opcode_MISC_MEM]
			s= fencereq 	1
		endif

		begif [s== opcode $riscv_pipe::opcode_SYSTEM]
			begif [s== funct3 0]
				begif [indexed instr_code 20]
					# EBREAK
					s= ebreakreq 1
				endif
				begelse
					# ECALL
					s= ecallreq 1
				endif
			endif
			
			# CSRRW
			begif [s== funct3 0x1]
				begif [s!= rs1_addr 0x0]
					s= csrreq		1
					s= rs1_req		1
					s= rd_req		1
					s= rd_source 	$riscv_pipe::RD_CSR
					s= op1_source	$riscv_pipe::OP1_SRC_RS1
					s= op2_source 	$riscv_pipe::OP2_SRC_CSR
				endif
			endif

			# CSRRS
			begif [s== funct3 0x2]
				begif [s!= rs1_addr 0x0]
					s= csrreq		1
					s= rs1_req		1
					s= rd_req		1
					s= rd_source 	$riscv_pipe::RD_CSR
					s= alu_req		1
					s= alu_opcode	$riscv_pipe::aluop_OR
					s= op1_source	$riscv_pipe::OP1_SRC_RS1
					s= op2_source 	$riscv_pipe::OP2_SRC_CSR
				endif
			endif

			# CSRRC
			begif [s== funct3 0x3]
				begif [s!= rs1_addr 0x0]
					s= csrreq		1
					s= rs1_req		1
					s= rd_req		1
					s= rd_source 	$riscv_pipe::RD_CSR
					s= alu_req		1
					s= alu_opcode	$riscv_pipe::aluop_CLRB
					s= op1_source	$riscv_pipe::OP1_SRC_RS1
					s= op2_source 	$riscv_pipe::OP2_SRC_CSR
				endif
			endif

			# CSRRWI
			begif [s== funct3 0x5]
				s= csrreq		1
				s= rd_req		1
				s= op1_source	$riscv_pipe::OP1_SRC_IMM
				s= op2_source 	$riscv_pipe::OP2_SRC_CSR
				s= immediate [zeroext zimm 32]
			endif

			# CSRRSI
			begif [s== funct3 0x6]
				s= csrreq		1
				s= rd_req		1
				s= rd_source 	$riscv_pipe::RD_CSR
				s= alu_req		1
				s= alu_opcode	$riscv_pipe::aluop_CLRB
				s= op1_source	$riscv_pipe::OP1_SRC_IMM
				s= op2_source 	$riscv_pipe::OP2_SRC_CSR
				s= immediate [zeroext zimm 32]
			endif

			# CSRRCI
			begif [s== funct3 0x7]
				s= csrreq		1
				s= rd_req		1
				s= rd_source 	$riscv_pipe::RD_CSR
				s= alu_req		1
				s= alu_opcode	$riscv_pipe::aluop_CLRB
				s= op1_source	$riscv_pipe::OP1_SRC_IMM
				s= op2_source 	$riscv_pipe::OP2_SRC_CSR
				s= immediate [zeroext zimm 32]
			endif

		endif

		begif [s== rd_addr 0]
			s= rd_req 0
		endif
	}

	#### data fetching - reading regfile ##
	proc process_regfetch {} {
		
		## unoptimized
		# s= rs1_rdata [indexed regfile rs1_addr]
		# s= rs2_rdata [indexed regfile rs2_addr]
		##
		
		## optimized for synthesis
		s= rs1_rdata [indexed [pipe::rdprev regfile] rs1_addr]
		s= rs2_rdata [indexed [pipe::rdprev regfile] rs2_addr]

		begif [s== rs1_addr 0]
			s= rs1_rdata 0
		endif

		begif [s== rs2_addr 0]
			s= rs2_rdata 0
		endif
	}

	## unblocking forwarding
	proc forward_unblocking {pstage} {
		begif [s&& [pipe::isworking $pstage] [pipe::prr $pstage rd_req]]
			begif [s== [pipe::prr $pstage rd_addr] rs1_addr]
				begif [pipe::prr $pstage rd_rdy]
					s= rs1_rdata [pipe::prr $pstage rd_wdata]
				endif
			endif
			begif [s== [pipe::prr $pstage rd_addr] rs2_addr]
				begif [pipe::prr $pstage rd_rdy]
					s= rs2_rdata [pipe::prr $pstage rd_wdata]
				endif
			endif
		endif
	}

	## unblocking forwarding from stages with guaranteed data availability (for succ transfers only, no rdy checked)
	proc forward_unblocking_succ {pstage} {
		begif [s&& [pipe::issucc $pstage] [pipe::prr $pstage rd_req]]
			begif [s== [pipe::prr $pstage rd_addr] rs1_addr]
				s= rs1_rdata [pipe::prr $pstage rd_wdata]
			endif
			begif [s== [pipe::prr $pstage rd_addr] rs2_addr]
				s= rs2_rdata [pipe::prr $pstage rd_wdata]
			endif
		endif
	}

	## blocking forwarding with accumulation
	proc forward_accum_blocking {pstage} {
		begif [s&& [pipe::isworking $pstage] [pipe::prr $pstage rd_req]]
			begif [s== [pipe::prr $pstage rd_addr] rs1_addr]
				begif [pipe::prr $pstage rd_rdy]
					pipe::accum rs1_rdata [pipe::prr $pstage rd_wdata]
				endif
				begelse
					pipe::pstall
				endif
			endif
			begif [s== [pipe::prr $pstage rd_addr] rs2_addr]
				begif [pipe::prr $pstage rd_rdy]
					pipe::accum rs2_rdata [pipe::prr $pstage rd_wdata]
				endif
				begelse
					pipe::pstall
				endif
			endif
		endif
	}

	proc forward_blocking {pstage} {
		begif [s&& [pipe::isworking $pstage] [pipe::prr $pstage rd_req]]
			begif [s== [pipe::prr $pstage rd_addr] rs1_addr]
				begif [pipe::prr $pstage rd_rdy]
					s= rs1_rdata [pipe::prr $pstage rd_wdata]
				endif
				begelse
					pipe::pstall
				endif
			endif
			begif [s== [pipe::prr $pstage rd_addr] rs2_addr]
				begif [pipe::prr $pstage rd_rdy]
					s= rs2_rdata [pipe::prr $pstage rd_wdata]
				endif
				begelse
					pipe::pstall
				endif
			endif
		endif
	}

	## ALU processing ##
	proc process_alu {} {
		
		# acquiring data
		begif [s== op1_source $riscv_pipe::OP1_SRC_RS1]
			s= alu_op1 rs1_rdata
		endif

		begif [s== op1_source $riscv_pipe::OP1_SRC_IMM]
			s= alu_op1 immediate
		endif

		begif [s== op1_source $riscv_pipe::OP1_SRC_PC]
			s= alu_op1 curinstr_addr
		endif

		begif [s== op2_source $riscv_pipe::OP2_SRC_RS2]
			s= alu_op2 rs2_rdata
		endif

		begif [s== op2_source $riscv_pipe::OP2_SRC_IMM]
			s= alu_op2 immediate
		endif

		# begif [s== op2_source $riscv_pipe::OP2_SRC_CSR]
			# TODO: reading CSRs
		# endif

		# acquiring wide operandes
		begif alu_unsigned
			s= alu_op1_wide [zeroext alu_op1 33]
			s= alu_op2_wide [zeroext alu_op2 33]
		endif
		begelse
			s= alu_op1_wide [signext alu_op1 33]
			s= alu_op2_wide [signext alu_op2 33]
		endif

		s= alu_result_wide alu_op1_wide

		begif alu_req

			# computing result
			begif [s== alu_opcode $riscv_pipe::aluop_ADD]
				s= alu_result_wide [s+ alu_op1_wide alu_op2_wide]
			endif

			begif [s== alu_opcode $riscv_pipe::aluop_SUB]
				s= alu_result_wide [s- alu_op1_wide alu_op2_wide]
			endif

			begif [s== alu_opcode $riscv_pipe::aluop_AND]
				s= alu_result_wide [s& alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $riscv_pipe::aluop_OR]
				s= alu_result_wide [s| alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $riscv_pipe::aluop_XOR]
				s= alu_result_wide [s^ alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $riscv_pipe::aluop_SRL]
				s= alu_result_wide [s>> alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $riscv_pipe::aluop_SRA]
				s= alu_result_wide [s>>> alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $riscv_pipe::aluop_SLL]
				s= alu_result_wide [s<< alu_op1 alu_op2]
			endif

			begif [s== alu_opcode $riscv_pipe::aluop_CLRB]
				s= alu_result_wide [s& alu_op1 [s~ alu_op2]]
			endif

			# formation of result and flags

			s= alu_result [indexed alu_result_wide {31 0}]
			
			s= alu_CF [indexed alu_result_wide 32]
			
			s= alu_SF [indexed alu_result_wide 31]
			begif [s== alu_result_wide 0]
				s= alu_ZF 1
			endif
			
			begif [s|| [s== [indexed alu_result_wide {32 31}] 0x2] [s== [indexed alu_result_wide {32 31}] 0x1]]
				s= alu_OF 1
			endif

			begif alu_unsigned
				s= alu_overflow alu_CF
			endif
			begelse
				s= alu_overflow alu_OF
			endif

		endif

		# rd wdata processing
		begif [s== rd_source $riscv_pipe::RD_LUI]
			s= rd_wdata immediate
			s= rd_rdy	1
		endif

		begif [s== rd_source $riscv_pipe::RD_ALU]
			s= rd_wdata alu_result
			s= rd_rdy	1
		endif

		begif [s== rd_source $riscv_pipe::RD_CF_COND]
			s= rd_wdata alu_CF
			s= rd_rdy	1
		endif

		begif [s== rd_source $riscv_pipe::RD_OF_COND]
			s= rd_wdata alu_OF
			s= rd_rdy	1
		endif

		begif [s== rd_source $riscv_pipe::RD_PC_INC]
			s= rd_wdata nextinstr_addr
			s= rd_rdy	1
		endif
	}

	proc process_rd_csr_prev {} {
		#begif [s== rd_source $riscv_pipe::RD_CSR]
			# TODO: fetching previous CSR data
		#endif
	}

	proc process_curinstraddr_imm {} {
		s= curinstraddr_imm [s+ curinstr_addr immediate]
	}

	proc process_jump {} {
		begif [s== jump_src $riscv_pipe::JMP_SRC_OP1]
			s= jump_vector alu_op1
		endif

		begif [s== jump_src $riscv_pipe::JMP_SRC_ALU]
			s= jump_vector alu_result
		endif

		begif jump_req_cond

			# BEQ
			begif [s== funct3 0x0]
				begif alu_ZF
					s= jump_req 1
					s= jump_vector curinstraddr_imm
				endif
			endif

			# BNE
			begif [s== funct3 0x1]
				begif [s! alu_ZF]
					s= jump_req 1
					s= jump_vector curinstraddr_imm
				endif
			endif

			# BLT, BLTU
			begif [s|| [s== funct3 0x4] [s== funct3 0x6]]
				begif alu_CF
					s= jump_req 1
					s= jump_vector curinstraddr_imm
				endif
			endif

			# BGE, BGEU
			begif [s|| [s== funct3 0x5] [s== funct3 0x7]]
				begif [s! alu_CF]
					s= jump_req 1
					s= jump_vector curinstraddr_imm
				endif
			endif

		endif
	}

	# mem addr processing
	proc process_setup_mem_reqdata {} {
		s= mem_addr alu_result
		s= mem_wdata rs2_rdata
	}

	# branch control
	proc process_branch {} {
		pipe::s<= jump_req_cmd jump_req
		pipe::s<= jump_vector_cmd jump_vector
		begif jump_req
			pipe::pflush
		endif
	}

	proc process_rd_mem_wdata {} {
		begif [s== rd_source $riscv_pipe::RD_MEM]
			s= rd_wdata mem_rdata
		endif
	}

	proc process_wb {} {
		begif rd_req
			_acc_index rd_addr
			pipe::s<= regfile rd_wdata
		endif
	}

	proc connect_copipes {} {
		pipe::_acc_index_wdata {63 0}
		pipe::_acc_index_rdata {31 0}
		pipe::copipe::declare {0 0} instr_mem

		pipe::_acc_index_wdata {67 0}
		pipe::_acc_index_rdata {31 0}
		pipe::copipe::declare {0 0} data_mem

		pipe::mcopipe::connect instrpipe instr_mem instr_mem
		pipe::mcopipe::connect instrpipe data_mem data_mem

		pipe::mcopipe::export instr_mem 0 { \
					instr_mcopipe_req 	\
					instr_mcopipe_we 	\
					instr_mcopipe_ack 	\
					instr_mcopipe_wdata	\
					instr_mcopipe_resp	\
					instr_mcopipe_rdata	\
				}

		pipe::mcopipe::export data_mem 0 { \
					data_mcopipe_req	\
					data_mcopipe_we		\
					data_mcopipe_ack	\
					data_mcopipe_wdata	\
					data_mcopipe_resp	\
					data_mcopipe_rdata	\
				}
	}
}
