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

		rtl::setclk clk_i
		rtl::setrst rst_i
		
		
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

		ac= instr_mem_req instr_mcopipe_req
		ac= instr_mem_we instr_mcopipe_we
		ac= instr_mcopipe_ack instr_mem_ack
		ac= instr_mem_addr [indexed instr_mcopipe_wdata {63 32}]
		ac= instr_mem_wdata [indexed instr_mcopipe_wdata {31 0}]
		ac= instr_mem_be 0xf
		ac= instr_mcopipe_resp instr_mem_resp
		ac= instr_mcopipe_rdata instr_mem_rdata

		ac= data_mem_req data_mcopipe_req
		ac= data_mem_we data_mcopipe_we
		ac= data_mcopipe_ack data_mem_ack
		ac= data_mem_addr [indexed data_mcopipe_wdata {67 36}]
		ac= data_mem_be [indexed data_mcopipe_wdata {35 32}]
		ac= data_mem_wdata [indexed data_mcopipe_wdata {31 0}]
		ac= data_mcopipe_resp data_mem_resp
		ac= data_mcopipe_rdata data_mem_rdata
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
		
		pipe::psticky_glbl	{31 0} 	pc				$riscv_pipe::START_ADDR
		_acc_index 	{31 1}	
		pipe::psticky_glbl {31 0} 	regfile			0
		pipe::psticky_glbl {0 0}	jump_req_cmd	0
		pipe::psticky_glbl {31 0} 	jump_vector_cmd	0

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
		ac= curinstr_addr pc

		acif::begin jump_req_cmd
			ac= curinstr_addr jump_vector_cmd
		acif::end
		pipe::p<= jump_req_cmd 0

		ac= nextinstr_addr [ac+ curinstr_addr 4]

		pipe::p<= pc nextinstr_addr
	}

	proc process_decode {} {
		ac= opcode [indexed instr_code {6 0}]
		ac= alu_unsigned 0

		ac= rs1_addr [indexed instr_code {19 15}]
		ac= rs2_addr [indexed instr_code {24 20}]
		ac= rd_addr  [indexed instr_code {11 7}]

		ac= funct3	[indexed instr_code {14 12}]
		ac= funct7	[indexed instr_code {31 25}]
		ac= shamt 	[indexed instr_code {24 20}]
		ac= pred 	[indexed instr_code {27 24}]
		ac= succ	[indexed instr_code {23 20}]
		ac= csrnum 	[indexed instr_code {31 20}]
		ac= zimm	[indexed instr_code {19 15}]

		ac= immediate_I	[signext [indexed instr_code {31 20}] 32]
		ac= immediate_S	[signext [cnct [list	[indexed instr_code {31 25}] \
												[indexed instr_code {11 7}] \
							]] 32]
		ac= immediate_B	[signext [cnct [list	[indexed instr_code 31] \
												[indexed instr_code 7] \
												[indexed instr_code {30 25}] \
												[indexed instr_code {11 8}] \
												[initval {0 0} 0] \
							]] 32]
		ac= immediate_U	[cnct [list				[indexed instr_code {31 12}] \
												[initval {11 0} 0] \
							]]
		ac= immediate_J	[signext [cnct [list	[indexed instr_code 31] \
												[indexed instr_code {19 12}] \
												[indexed instr_code 20] \
												[indexed instr_code {30 21}] \
												[initval {0 0} 0] \
							]] 32]

		acif::begin [ac== opcode $riscv_pipe::opcode_LUI]
			ac= op1_source $riscv_pipe::OP1_SRC_IMM
			ac= rd_req 		1
			ac= rd_source	$riscv_pipe::RD_LUI
			ac= immediate immediate_U
		acif::end

		acif::begin [ac== opcode $riscv_pipe::opcode_AUIPC]
			ac= op1_source	$riscv_pipe::OP1_SRC_PC
			ac= op2_source	$riscv_pipe::OP2_SRC_IMM
			ac= alu_req		1
			ac= alu_opcode 	$riscv_pipe::aluop_ADD
			ac= rd_req		1
			ac= rd_source	$riscv_pipe::RD_ALU

			ac= immediate immediate_U
		acif::end

		acif::begin [ac== opcode $riscv_pipe::opcode_JAL]
			ac= op1_source 	$riscv_pipe::OP1_SRC_PC
			ac= op2_source 	$riscv_pipe::OP2_SRC_IMM
			ac= alu_req		1
			ac= alu_opcode 	$riscv_pipe::aluop_ADD
			ac= rd_req		1
			ac= rd_source	$riscv_pipe::RD_PC_INC
			ac= jump_req 	1
			ac= jump_src	$riscv_pipe::JMP_SRC_ALU
			ac= immediate immediate_J
		acif::end

		acif::begin [ac== opcode $riscv_pipe::opcode_JALR]
			ac= rs1_req		1
			ac= op1_source 	$riscv_pipe::OP1_SRC_RS1
			ac= op2_source 	$riscv_pipe::OP2_SRC_IMM
			ac= alu_req		1
			ac= alu_opcode 	$riscv_pipe::aluop_ADD
			ac= rd_req		1
			ac= rd_source	$riscv_pipe::RD_PC_INC
			ac= jump_req 	1
			ac= jump_src	$riscv_pipe::JMP_SRC_ALU
			ac= immediate immediate_I
		acif::end

		acif::begin [ac== opcode $riscv_pipe::opcode_BRANCH]
			ac= rs1_req		1
			ac= rs2_req		1
			ac= alu_req		1
			ac= alu_opcode 	$riscv_pipe::aluop_SUB
			ac= jump_req_cond 	1
			ac= jump_src		$riscv_pipe::JMP_SRC_ALU
			ac= immediate immediate_B

			acif::begin [ac|| [ac== funct3 0x6] [ac== funct3 0x7] ]
				ac= alu_unsigned 1
			acif::end

		acif::end

		acif::begin [ac== opcode $riscv_pipe::opcode_LOAD]
			ac= rs1_req 	1
			ac= op1_source 	$riscv_pipe::OP1_SRC_RS1
			ac= op2_source 	$riscv_pipe::OP2_SRC_IMM
			ac= rd_req		1
			ac= rd_source	$riscv_pipe::RD_MEM

			ac= alu_req		1

			ac= mem_req 	1
			ac= mem_cmd		0

			acif::begin [ac|| [ac== funct3 0x0] [ac== funct3 0x4] ]
				ac= mem_be 0x1
			acif::end
			acif::begin [ac|| [ac== funct3 0x1] [ac== funct3 0x5] ]
				ac= mem_be 0x3
			acif::end
			acif::begin [ac== funct3 0x2]
				ac= mem_be 0xf
			acif::end

			ac= immediate immediate_I
		acif::end

		acif::begin [ac== opcode $riscv_pipe::opcode_STORE]
			ac= rs1_req 	1
			ac= rs2_req 	1
			ac= op1_source 	$riscv_pipe::OP1_SRC_RS1
			ac= op2_source 	$riscv_pipe::OP2_SRC_IMM

			ac= alu_req		1
			
			ac= mem_req 	1
			ac= mem_cmd		1

			acif::begin [ac== funct3 0x0]
				ac= mem_be 0x1
			acif::end

			acif::begin [ac== funct3 0x1]
				ac= mem_be 0x3
			acif::end

			acif::begin [ac== funct3 0x2]
				ac= mem_be 0xf
			acif::end

			ac= immediate immediate_S
		acif::end

		acif::begin [ac== opcode $riscv_pipe::opcode_OP_IMM]
			ac= rs1_req 	1
			ac= op1_source 	$riscv_pipe::OP1_SRC_RS1
			ac= op2_source 	$riscv_pipe::OP2_SRC_IMM
			ac= rd_req 		1

			ac= immediate 	immediate_I

			ac= alu_req		1

			# ADDI
			acif::begin [ac== funct3 0x0]
				ac= alu_opcode $riscv_pipe::aluop_ADD
				ac= rd_source 	$riscv_pipe::RD_ALU
			acif::end

			# SLTI
			acif::begin [ac== funct3 0x2]
				ac= alu_opcode $riscv_pipe::aluop_SUB
				ac= rd_source $riscv_pipe::RD_OF_COND
			acif::end

			# SLTIU
			acif::begin [ac== funct3 0x3]
				ac= alu_opcode $riscv_pipe::aluop_SUB
				ac= alu_unsigned 1
				ac= rd_source $riscv_pipe::RD_CF_COND
			acif::end

			# XORI
			acif::begin [ac== funct3 0x4]
				ac= alu_opcode $riscv_pipe::aluop_XOR
				ac= rd_source $riscv_pipe::RD_ALU
			acif::end

			# ORI
			acif::begin [ac== funct3 0x6]
				ac= alu_opcode $riscv_pipe::aluop_OR
				ac= rd_source $riscv_pipe::RD_ALU
			acif::end

			# ANDI
			acif::begin [ac== funct3 0x7]
				ac= alu_opcode $riscv_pipe::aluop_AND
				ac= rd_source $riscv_pipe::RD_ALU
			acif::end

			# SLLI
			acif::begin [ac== funct3 0x1]
				ac= alu_opcode $riscv_pipe::aluop_SLL
				ac= rd_source $riscv_pipe::RD_ALU
				ac= immediate [zeroext [indexed instr_code {24 20}] 32]
			acif::end

			# SRLI, SRAI
			acif::begin [ac== funct3 0x5]
				# SRAI
				acif::begin [indexed instr_code 30]
					ac= alu_opcode $riscv_pipe::aluop_SRA
				acif::end
				# SRLI
				acif::begelse
					ac= alu_opcode $riscv_pipe::aluop_SRL
				acif::end
				ac= rd_source $riscv_pipe::RD_ALU
				ac= immediate [zeroext [indexed instr_code {24 20}] 32]
			acif::end

		acif::end

		acif::begin [ac== opcode $riscv_pipe::opcode_OP]
			ac= rs1_req 	1
			ac= op1_source 	$riscv_pipe::OP1_SRC_RS1
			ac= op2_source 	$riscv_pipe::OP2_SRC_RS2
			ac= rd_req 		1
			ac= rd_source 	$riscv_pipe::RD_ALU

			ac= alu_req		1

			# ADD
			acif::begin [ac== funct3 0x0]
				# SUB
				acif::begin [indexed instr_code 30]
					ac= alu_opcode $riscv_pipe::aluop_SUB
				acif::end
				# ADD
				acif::begelse
					ac= alu_opcode $riscv_pipe::aluop_ADD
				acif::end
				ac= rd_source 	$riscv_pipe::RD_ALU
			acif::end

			# SLL
			acif::begin [ac== funct3 0x1]
				ac= alu_opcode $riscv_pipe::aluop_SLL
				ac= rd_source $riscv_pipe::RD_OF_COND
			acif::end

			# SLT
			acif::begin [ac== funct3 0x2]
				ac= alu_opcode $riscv_pipe::aluop_SUB
				ac= rd_source $riscv_pipe::RD_OF_COND
			acif::end

			# SLTU
			acif::begin [ac== funct3 0x3]
				ac= alu_opcode $riscv_pipe::aluop_SUB
				ac= alu_unsigned 1
				ac= rd_source $riscv_pipe::RD_CF_COND
			acif::end

			# XORI
			acif::begin [ac== funct3 0x4]
				ac= alu_opcode $riscv_pipe::aluop_XOR
				ac= rd_source $riscv_pipe::RD_ALU
			acif::end

			# SRL, SRA
			acif::begin [ac== funct3 0x5]
				# SRA
				acif::begin [indexed instr_code 30]
					ac= alu_opcode $riscv_pipe::aluop_SRA
				acif::end
				# SRL
				acif::begelse
					ac= alu_opcode $riscv_pipe::aluop_SRL
				acif::end
				ac= rd_source $riscv_pipe::RD_ALU
			acif::end

			# OR
			acif::begin [ac== funct3 0x6]
				ac= alu_opcode $riscv_pipe::aluop_OR
				ac= rd_source $riscv_pipe::RD_ALU
			acif::end

			# AND
			acif::begin [ac== funct3 0x7]
				ac= alu_opcode $riscv_pipe::aluop_AND
				ac= rd_source $riscv_pipe::RD_ALU
			acif::end

		acif::end

		acif::begin [ac== opcode $riscv_pipe::opcode_MISC_MEM]
			ac= fencereq 	1
		acif::end

		acif::begin [ac== opcode $riscv_pipe::opcode_SYSTEM]
			acif::begin [ac== funct3 0]
				acif::begin [indexed instr_code 20]
					# EBREAK
					ac= ebreakreq 1
				acif::end
				acif::begelse
					# ECALL
					ac= ecallreq 1
				acif::end
			acif::end
			
			# CSRRW
			acif::begin [ac== funct3 0x1]
				acif::begin [ac!= rs1_addr 0x0]
					ac= csrreq		1
					ac= rs1_req		1
					ac= rd_req		1
					ac= rd_source 	$riscv_pipe::RD_CSR
					ac= op1_source	$riscv_pipe::OP1_SRC_RS1
					ac= op2_source 	$riscv_pipe::OP2_SRC_CSR
				acif::end
			acif::end

			# CSRRS
			acif::begin [ac== funct3 0x2]
				acif::begin [ac!= rs1_addr 0x0]
					ac= csrreq		1
					ac= rs1_req		1
					ac= rd_req		1
					ac= rd_source 	$riscv_pipe::RD_CSR
					ac= alu_req		1
					ac= alu_opcode	$riscv_pipe::aluop_OR
					ac= op1_source	$riscv_pipe::OP1_SRC_RS1
					ac= op2_source 	$riscv_pipe::OP2_SRC_CSR
				acif::end
			acif::end

			# CSRRC
			acif::begin [ac== funct3 0x3]
				acif::begin [ac!= rs1_addr 0x0]
					ac= csrreq		1
					ac= rs1_req		1
					ac= rd_req		1
					ac= rd_source 	$riscv_pipe::RD_CSR
					ac= alu_req		1
					ac= alu_opcode	$riscv_pipe::aluop_CLRB
					ac= op1_source	$riscv_pipe::OP1_SRC_RS1
					ac= op2_source 	$riscv_pipe::OP2_SRC_CSR
				acif::end
			acif::end

			# CSRRWI
			acif::begin [ac== funct3 0x5]
				ac= csrreq		1
				ac= rd_req		1
				ac= op1_source	$riscv_pipe::OP1_SRC_IMM
				ac= op2_source 	$riscv_pipe::OP2_SRC_CSR
				ac= immediate [zeroext zimm 32]
			acif::end

			# CSRRSI
			acif::begin [ac== funct3 0x6]
				ac= csrreq		1
				ac= rd_req		1
				ac= rd_source 	$riscv_pipe::RD_CSR
				ac= alu_req		1
				ac= alu_opcode	$riscv_pipe::aluop_CLRB
				ac= op1_source	$riscv_pipe::OP1_SRC_IMM
				ac= op2_source 	$riscv_pipe::OP2_SRC_CSR
				ac= immediate [zeroext zimm 32]
			acif::end

			# CSRRCI
			acif::begin [ac== funct3 0x7]
				ac= csrreq		1
				ac= rd_req		1
				ac= rd_source 	$riscv_pipe::RD_CSR
				ac= alu_req		1
				ac= alu_opcode	$riscv_pipe::aluop_CLRB
				ac= op1_source	$riscv_pipe::OP1_SRC_IMM
				ac= op2_source 	$riscv_pipe::OP2_SRC_CSR
				ac= immediate [zeroext zimm 32]
			acif::end

		acif::end

		acif::begin [ac== rd_addr 0]
			ac= rd_req 0
		acif::end
	}

	#### data fetching - reading regfile ##
	proc process_regfetch {} {
		
		## unoptimized
		# ac= rs1_rdata [indexed regfile rs1_addr]
		# ac= rs2_rdata [indexed regfile rs2_addr]
		##
		
		## optimized for synthesis
		ac= rs1_rdata [indexed [pipe::rdbuf regfile] rs1_addr]
		ac= rs2_rdata [indexed [pipe::rdbuf regfile] rs2_addr]

		acif::begin [ac== rs1_addr 0]
			ac= rs1_rdata 0
		acif::end

		acif::begin [ac== rs2_addr 0]
			ac= rs2_rdata 0
		acif::end
	}

	## unblocking forwarding
	proc forward_unblocking {pstage} {
		acif::begin [ac&& [pipe::isworking $pstage] [pipe::prr $pstage rd_req]]
			acif::begin [ac== [pipe::prr $pstage rd_addr] rs1_addr]
				acif::begin [pipe::prr $pstage rd_rdy]
					ac= rs1_rdata [pipe::prr $pstage rd_wdata]
				acif::end
			acif::end
			acif::begin [ac== [pipe::prr $pstage rd_addr] rs2_addr]
				acif::begin [pipe::prr $pstage rd_rdy]
					ac= rs2_rdata [pipe::prr $pstage rd_wdata]
				acif::end
			acif::end
		acif::end
	}

	## unblocking forwarding from stages with guaranteed data availability (for succ transfers only, no rdy checked)
	proc forward_unblocking_succ {pstage} {
		acif::begin [ac&& [pipe::issucc $pstage] [pipe::prr $pstage rd_req]]
			acif::begin [ac== [pipe::prr $pstage rd_addr] rs1_addr]
				ac= rs1_rdata [pipe::prr $pstage rd_wdata]
			acif::end
			acif::begin [ac== [pipe::prr $pstage rd_addr] rs2_addr]
				ac= rs2_rdata [pipe::prr $pstage rd_wdata]
			acif::end
		acif::end
	}

	## blocking forwarding with accumulation
	proc forward_accum_blocking {pstage} {
		acif::begin [ac&& [pipe::isworking $pstage] [pipe::prr $pstage rd_req]]
			acif::begin [ac== [pipe::prr $pstage rd_addr] rs1_addr]
				acif::begin [pipe::prr $pstage rd_rdy]
					pipe::accum rs1_rdata [pipe::prr $pstage rd_wdata]
				acif::end
				acif::begelse
					pipe::pstall
				acif::end
			acif::end
			acif::begin [ac== [pipe::prr $pstage rd_addr] rs2_addr]
				acif::begin [pipe::prr $pstage rd_rdy]
					pipe::accum rs2_rdata [pipe::prr $pstage rd_wdata]
				acif::end
				acif::begelse
					pipe::pstall
				acif::end
			acif::end
		acif::end
	}

	proc forward_blocking {pstage} {
		acif::begin [ac&& [pipe::isworking $pstage] [pipe::prr $pstage rd_req]]
			acif::begin [ac== [pipe::prr $pstage rd_addr] rs1_addr]
				acif::begin [pipe::prr $pstage rd_rdy]
					ac= rs1_rdata [pipe::prr $pstage rd_wdata]
				acif::end
				acif::begelse
					pipe::pstall
				acif::end
			acif::end
			acif::begin [ac== [pipe::prr $pstage rd_addr] rs2_addr]
				acif::begin [pipe::prr $pstage rd_rdy]
					ac= rs2_rdata [pipe::prr $pstage rd_wdata]
				acif::end
				acif::begelse
					pipe::pstall
				acif::end
			acif::end
		acif::end
	}

	## ALU processing ##
	proc process_alu {} {
		
		# acquiring data
		acif::begin [ac== op1_source $riscv_pipe::OP1_SRC_RS1]
			ac= alu_op1 rs1_rdata
		acif::end

		acif::begin [ac== op1_source $riscv_pipe::OP1_SRC_IMM]
			ac= alu_op1 immediate
		acif::end

		acif::begin [ac== op1_source $riscv_pipe::OP1_SRC_PC]
			ac= alu_op1 curinstr_addr
		acif::end

		acif::begin [ac== op2_source $riscv_pipe::OP2_SRC_RS2]
			ac= alu_op2 rs2_rdata
		acif::end

		acif::begin [ac== op2_source $riscv_pipe::OP2_SRC_IMM]
			ac= alu_op2 immediate
		acif::end

		# acif::begin [ac== op2_source $riscv_pipe::OP2_SRC_CSR]
			# TODO: reading CSRs
		# acif::end

		# acquiring wide operandes
		acif::begin alu_unsigned
			ac= alu_op1_wide [zeroext alu_op1 33]
			ac= alu_op2_wide [zeroext alu_op2 33]
		acif::end
		acif::begelse
			ac= alu_op1_wide [signext alu_op1 33]
			ac= alu_op2_wide [signext alu_op2 33]
		acif::end

		ac= alu_result_wide alu_op1_wide

		acif::begin alu_req

			# computing result
			acif::begin [ac== alu_opcode $riscv_pipe::aluop_ADD]
				ac= alu_result_wide [ac+ alu_op1_wide alu_op2_wide]
			acif::end

			acif::begin [ac== alu_opcode $riscv_pipe::aluop_SUB]
				ac= alu_result_wide [ac- alu_op1_wide alu_op2_wide]
			acif::end

			acif::begin [ac== alu_opcode $riscv_pipe::aluop_AND]
				ac= alu_result_wide [ac& alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $riscv_pipe::aluop_OR]
				ac= alu_result_wide [ac| alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $riscv_pipe::aluop_XOR]
				ac= alu_result_wide [ac^ alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $riscv_pipe::aluop_SRL]
				ac= alu_result_wide [ac>> alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $riscv_pipe::aluop_SRA]
				ac= alu_result_wide [ac>>> alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $riscv_pipe::aluop_SLL]
				ac= alu_result_wide [ac<< alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $riscv_pipe::aluop_CLRB]
				ac= alu_result_wide [ac& alu_op1 [ac~ alu_op2]]
			acif::end

			# formation of result and flags

			ac= alu_result [indexed alu_result_wide {31 0}]
			
			ac= alu_CF [indexed alu_result_wide 32]
			
			ac= alu_SF [indexed alu_result_wide 31]
			acif::begin [ac== alu_result_wide 0]
				ac= alu_ZF 1
			acif::end
			
			acif::begin [ac|| [ac== [indexed alu_result_wide {32 31}] 0x2] [ac== [indexed alu_result_wide {32 31}] 0x1]]
				ac= alu_OF 1
			acif::end

			acif::begin alu_unsigned
				ac= alu_overflow alu_CF
			acif::end
			acif::begelse
				ac= alu_overflow alu_OF
			acif::end

		acif::end

		# rd wdata processing
		acif::begin [ac== rd_source $riscv_pipe::RD_LUI]
			ac= rd_wdata immediate
			ac= rd_rdy	1
		acif::end

		acif::begin [ac== rd_source $riscv_pipe::RD_ALU]
			ac= rd_wdata alu_result
			ac= rd_rdy	1
		acif::end

		acif::begin [ac== rd_source $riscv_pipe::RD_CF_COND]
			ac= rd_wdata alu_CF
			ac= rd_rdy	1
		acif::end

		acif::begin [ac== rd_source $riscv_pipe::RD_OF_COND]
			ac= rd_wdata alu_OF
			ac= rd_rdy	1
		acif::end

		acif::begin [ac== rd_source $riscv_pipe::RD_PC_INC]
			ac= rd_wdata nextinstr_addr
			ac= rd_rdy	1
		acif::end
	}

	proc process_rd_csr_prev {} {
		#acif::begin [ac== rd_source $riscv_pipe::RD_CSR]
			# TODO: fetching previous CSR data
		#acif::end
	}

	proc process_curinstraddr_imm {} {
		ac= curinstraddr_imm [ac+ curinstr_addr immediate]
	}

	proc process_jump {} {
		acif::begin [ac== jump_src $riscv_pipe::JMP_SRC_OP1]
			ac= jump_vector alu_op1
		acif::end

		acif::begin [ac== jump_src $riscv_pipe::JMP_SRC_ALU]
			ac= jump_vector alu_result
		acif::end

		acif::begin jump_req_cond

			# BEQ
			acif::begin [ac== funct3 0x0]
				acif::begin alu_ZF
					ac= jump_req 1
					ac= jump_vector curinstraddr_imm
				acif::end
			acif::end

			# BNE
			acif::begin [ac== funct3 0x1]
				acif::begin [ac! alu_ZF]
					ac= jump_req 1
					ac= jump_vector curinstraddr_imm
				acif::end
			acif::end

			# BLT, BLTU
			acif::begin [ac|| [ac== funct3 0x4] [ac== funct3 0x6]]
				acif::begin alu_CF
					ac= jump_req 1
					ac= jump_vector curinstraddr_imm
				acif::end
			acif::end

			# BGE, BGEU
			acif::begin [ac|| [ac== funct3 0x5] [ac== funct3 0x7]]
				acif::begin [ac! alu_CF]
					ac= jump_req 1
					ac= jump_vector curinstraddr_imm
				acif::end
			acif::end

		acif::end
	}

	# mem addr processing
	proc process_setup_mem_reqdata {} {
		ac= mem_addr alu_result
		ac= mem_wdata rs2_rdata
	}

	# branch control
	proc process_branch {} {
		pipe::p<= jump_req_cmd jump_req
		pipe::p<= jump_vector_cmd jump_vector
		acif::begin jump_req
			pipe::pflush
		acif::end
	}

	proc process_rd_mem_wdata {} {
		acif::begin [ac== rd_source $riscv_pipe::RD_MEM]
			ac= rd_wdata mem_rdata
		acif::end
	}

	proc process_wb {} {
		acif::begin rd_req
			_acc_index rd_addr
			pipe::p<= regfile rd_wdata
		acif::end
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

	proc generate {num_stages} {

		set module_name riscv_
		append module_name $num_stages
		append module_name stage

		rtl::module $module_name

			riscv_pipe::declare_wrapper_ports

			pipe::pproc instrpipe

				riscv_pipe::declare_pcontext

				# 1-stage
				if {$num_stages == 1} {

					pipe::pstage EXEC
						
						riscv_pipe::process_pc
						pipe::mcopipe::rdreq instr_mem 0 [cnct {curinstr_addr curinstr_addr}]
						pipe::mcopipe::resp instr_mem instr_code
						riscv_pipe::process_decode
						riscv_pipe::process_regfetch
						riscv_pipe::process_alu
						riscv_pipe::process_rd_csr_prev
						riscv_pipe::process_curinstraddr_imm
						riscv_pipe::process_jump
						riscv_pipe::process_setup_mem_reqdata
						riscv_pipe::process_branch

						# memory access
						acif::begin mem_req
						
							acif::begin mem_cmd
								pipe::mcopipe::wrreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
							acif::end
							acif::begelse
								pipe::mcopipe::rdreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
								acif::begin [pipe::mcopipe::resp data_mem mem_rdata]
									ac= rd_rdy	1
								acif::end
							acif::end

						acif::end

						riscv_pipe::process_rd_mem_wdata
						riscv_pipe::process_wb

				# 2-stage
				} elseif {$num_stages == 2} {

					pipe::pstage IFETCH
						
						riscv_pipe::process_pc
						pipe::mcopipe::rdreq instr_mem 0 [cnct {curinstr_addr curinstr_addr}]

					pipe::pstage EXEC

						pipe::mcopipe::resp instr_mem instr_code
						riscv_pipe::process_decode
						riscv_pipe::process_regfetch

						riscv_pipe::process_alu
						riscv_pipe::process_rd_csr_prev
						riscv_pipe::process_curinstraddr_imm
						riscv_pipe::process_jump
						riscv_pipe::process_setup_mem_reqdata
						
						riscv_pipe::process_branch

						# memory access
						acif::begin mem_req
						
							acif::begin mem_cmd
								pipe::mcopipe::wrreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
							acif::end
							acif::begelse
								pipe::mcopipe::rdreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
								acif::begin [pipe::mcopipe::resp data_mem mem_rdata]
									ac= rd_rdy	1
								acif::end
							acif::end

						acif::end

						riscv_pipe::process_rd_mem_wdata
						riscv_pipe::process_wb

				} elseif {$num_stages == 3} {

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

				} elseif {$num_stages == 4} {

					pipe::pstage IFETCH

						riscv_pipe::process_pc
						pipe::mcopipe::rdreq instr_mem 0 [cnct {curinstr_addr curinstr_addr}]

					pipe::pstage IDECODE

						pipe::mcopipe::resp instr_mem instr_code
						riscv_pipe::process_decode
						riscv_pipe::process_regfetch

						# pipeline MEMWB forwarding
						riscv_pipe::forward_unblocking MEMWB

					pipe::pstage EXEC

						riscv_pipe::forward_accum_blocking MEMWB	

						riscv_pipe::process_alu
						riscv_pipe::process_rd_csr_prev
						riscv_pipe::process_curinstraddr_imm

					pipe::pstage MEMWB
						
						riscv_pipe::process_jump
						riscv_pipe::process_setup_mem_reqdata
						riscv_pipe::process_branch

						# memory access
						acif::begin mem_req
							acif::begin mem_cmd
								pipe::mcopipe::wrreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
							acif::end
							acif::begelse
								pipe::mcopipe::rdreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
								acif::begin [pipe::mcopipe::resp data_mem mem_rdata]
									ac= rd_rdy	1
								acif::end
							acif::end
						acif::end

						riscv_pipe::process_rd_mem_wdata
						riscv_pipe::process_wb

				} elseif {$num_stages == 5} {

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
						acif::begin mem_req
							acif::begin mem_cmd
								pipe::mcopipe::wrreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
							acif::end
							acif::begelse
								pipe::mcopipe::rdreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
							acif::end
						acif::end

					pipe::pstage WB
						
						acif::begin mem_req
							acif::begin [ac! mem_cmd]
								acif::begin [pipe::mcopipe::resp data_mem mem_rdata]
									ac= rd_rdy	1
								acif::end
							acif::end
						acif::end

						riscv_pipe::process_rd_mem_wdata
						riscv_pipe::process_wb

				} elseif {$num_stages == 6} {

					pipe::pstage IADDR

						riscv_pipe::process_pc

					pipe::pstage IFETCH

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

					pipe::pstage MEM

						riscv_pipe::process_setup_mem_reqdata
						riscv_pipe::process_curinstraddr_imm
						riscv_pipe::process_jump
						riscv_pipe::process_branch

						# memory access
						acif::begin mem_req
							acif::begin mem_cmd
								pipe::mcopipe::wrreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
							acif::end
							acif::begelse
								pipe::mcopipe::rdreq data_mem 0 [cnct {mem_addr mem_be mem_wdata}]
							acif::end
						acif::end

					pipe::pstage WB
						
						acif::begin mem_req
							acif::begin [ac! mem_cmd]
								acif::begin [pipe::mcopipe::resp data_mem mem_rdata]
									ac= rd_rdy	1
								acif::end
							acif::end
						acif::end

						riscv_pipe::process_rd_mem_wdata
						riscv_pipe::process_wb

				} else {
					error Generate\ parameter\ incorrect!
				}

			pipe::endpproc

			riscv_pipe::connect_copipes

		rtl::endmodule

	}
}
