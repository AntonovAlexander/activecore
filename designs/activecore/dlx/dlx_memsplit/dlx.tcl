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

rtl::module dlx

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

	rtl::comb 	{0 0} 	data_mcopipe_req	0
	rtl::comb 	{0 0} 	data_mcopipe_we		0
	rtl::comb 	{0 0} 	data_mcopipe_ack	0
	rtl::comb 	{63 0} 	data_mcopipe_wdata	0
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
	ac= data_mem_addr [indexed data_mcopipe_wdata {63 32}]
	ac= data_mem_wdata [indexed data_mcopipe_wdata {31 0}]
	ac= data_mem_be 0xf
	ac= data_mcopipe_resp data_mem_resp
	ac= data_mcopipe_rdata data_mem_rdata

	pipe::pproc instrpipe

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
		pipe::pvar {0 0}	rd_rdy 			0

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
		
		pipe::psticky_glbl {31 0} 	pc					0
		_acc_index {31 0}	
		pipe::psticky_glbl {31 0} 	regfile				0
		pipe::psticky_glbl {0 0}	jump_req_cmd		0
		pipe::psticky_glbl {31 0} 	jump_vector_cmd		0

		pipe::_acc_index_wdata {63 0}
		pipe::_acc_index_rdata {31 0}
		pipe::mcopipe::declare {0 0} instr_mem

		pipe::_acc_index_wdata {63 0}
		pipe::_acc_index_rdata {31 0}
		pipe::mcopipe::declare {0 0} data_mem

		pipe::pstage IFETCH
			
			ac= curinstr_addr pc

			acif::begin jump_req_cmd
				ac= curinstr_addr jump_vector_cmd
			acif::end
			pipe::p<= jump_req_cmd 0

			pipe::mcopipe::rdreq instr_mem 0 [cnct {curinstr_addr curinstr_addr}]

			ac= nextinstr_addr [ac+ curinstr_addr 4]

			pipe::p<= pc nextinstr_addr
		
		pipe::pstage IDECODE

			pipe::mcopipe::resp instr_mem instr_code

			ac= opcode [indexed instr_code {31 26}]
			acif::begin [ac== opcode 0]
				ac= opcode [indexed instr_code {5 0}]
			acif::end

			ac= rs1_addr [indexed instr_code {25 21}]
			ac= rs2_addr [indexed instr_code {20 16}]
			ac= rd_addr  [indexed instr_code {15 11}]

			ac= immediate [signext [indexed instr_code {15 0}] 32]
			
			acif::begin [ac== opcode $dlx::opcode_ADD]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_ADD
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_ADDI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_ADD
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_AND]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_AND
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_ANDI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_AND
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_BEQZ]
				ac= jump_req 	1
				ac= jump_cond 	1
				ac= jump_cond_eqz 	1
				ac= jump_src			$dlx::JMP_SRC_ALU
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		0
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_ADD
				ac= op1_source 	$dlx::OP1_SRC_PC
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_BNEZ]
				ac= jump_req 	1
				ac= jump_cond 	1
				ac= jump_cond_eqz 	0
				ac= jump_src			$dlx::JMP_SRC_ALU
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		0
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_ADD
				ac= op1_source 	$dlx::OP1_SRC_PC
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_J]
				ac= jump_req 	1
				ac= jump_src	$dlx::JMP_SRC_ALU
				ac= rs2_req 	0
				ac= rd_req 		0
				ac= rd_source	$dlx::RD_PC_INC
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_ADD
				ac= op1_source 	$dlx::OP1_SRC_PC
				ac= op2_source 	$dlx::OP2_SRC_IMM
				ac= immediate [signext [indexed instr_code {25 0}] 32]
			acif::end

			acif::begin [ac== opcode $dlx::opcode_JAL]
				ac= jump_req 	1
				ac= jump_src	$dlx::JMP_SRC_ALU
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_source 	$dlx::RD_PC_INC
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_ADD
				ac= op1_source 	$dlx::OP1_SRC_PC
				ac= op2_source 	$dlx::OP2_SRC_IMM
				ac= rd_addr 31
				ac= immediate [signext [indexed instr_code {25 0}] 32]
			acif::end

			acif::begin [ac== opcode $dlx::opcode_JALR]
				ac= jump_req 	1
				ac= jump_src	$dlx::JMP_SRC_OP1
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_source 	$dlx::RD_PC_INC
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		0
				ac= rd_addr 31
			acif::end

			acif::begin [ac== opcode $dlx::opcode_JR]
				ac= jump_req 	1
				ac= jump_src			$dlx::JMP_SRC_OP1
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		0
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		0
			acif::end

			acif::begin [ac== opcode $dlx::opcode_LHI]
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_source 	$dlx::RD_LHI
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		0
			acif::end

			acif::begin [ac== opcode $dlx::opcode_LW]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_source 	$dlx::RD_MEM
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_ADD
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
				ac= mem_req 	1
				ac= mem_cmd 	0
			acif::end

			acif::begin [ac== opcode $dlx::opcode_OR]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_OR
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_ORI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_OR
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SEQ]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= rd_source 	$dlx::RD_ZF_COND
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SUB
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SEQI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_source 	$dlx::RD_ZF_COND
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SUB
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SLE]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= rd_source 	$dlx::RD_CF_COND
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SUB
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SLEI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_source 	$dlx::RD_CF_COND
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SUB
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SLL]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SLL
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SLLI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SLL
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SLT]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SUB
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SLTI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SUB
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SNE]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= rd_source 	$dlx::RD_nZF_COND
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SUB
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SNEI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_source 	$dlx::RD_nZF_COND
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SUB
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SRA]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SRA
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SRAI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SRA
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SRL]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SRL
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SRLI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SRL
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SUB]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SUB
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SUBI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_SUB
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			acif::begin [ac== opcode $dlx::opcode_SW]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		0
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_ADD
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
				ac= mem_req 	1
				ac= mem_cmd 	1
			acif::end

			acif::begin [ac== opcode $dlx::opcode_XOR]
				ac= rs1_req 	1
				ac= rs2_req 	1
				ac= rd_req 		1
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_XOR
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_REG
			acif::end

			acif::begin [ac== opcode $dlx::opcode_XORI]
				ac= rs1_req 	1
				ac= rs2_req 	0
				ac= rd_req 		1
				ac= rd_addr  [indexed instr_code {20 16}]
				ac= alu_req		1
				ac= alu_opcode 	$dlx::ALU_XOR
				ac= op1_source 	$dlx::OP1_SRC_REG
				ac= op2_source 	$dlx::OP2_SRC_IMM
			acif::end

			#### data fetching - reading regfile ##
			
			## unoptimized
			# ac= rs1_rdata [indexed regfile rs1_addr]
			# ac= rs2_rdata [indexed regfile rs2_addr]
			##
			
			## optimized for synthesis
			ac= rs1_rdata [indexed [pipe::rdbuf regfile] rs1_addr]
			ac= rs2_rdata [indexed [pipe::rdbuf regfile] rs2_addr]

			forward_blocking WB
			forward_blocking MEM
			forward_blocking EXEC

			acif::begin [ac== rs1_addr 0]
				ac= rs1_rdata 0
			acif::end

			acif::begin [ac== rs2_addr 0]
				ac= rs2_rdata 0
			acif::end

		pipe::pstage EXEC

			# ALU processing
			acif::begin [ac== op1_source $dlx::OP1_SRC_REG]
				ac= alu_op1 rs1_rdata
			acif::end

			acif::begin [ac== op1_source $dlx::OP1_SRC_PC]
				ac= alu_op1 nextinstr_addr
			acif::end

			acif::begin [ac== op2_source $dlx::OP2_SRC_REG]
				ac= alu_op2 rs2_rdata
			acif::end

			acif::begin [ac== op2_source $dlx::OP2_SRC_IMM]
				ac= alu_op2 immediate
			acif::end

			acif::begin [ac== alu_opcode $dlx::ALU_ADD]
				ac= alu_result_wide [ac+ alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $dlx::ALU_SUB]
				ac= alu_result_wide [ac- alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $dlx::ALU_AND]
				ac= alu_result_wide [ac& alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $dlx::ALU_OR]
				ac= alu_result_wide [ac| alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $dlx::ALU_XOR]
				ac= alu_result_wide [ac^ alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $dlx::ALU_SRL]
				ac= alu_result_wide [ac>> alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $dlx::ALU_SRA]
				ac= alu_result_wide [ac>>> alu_op1 alu_op2]
			acif::end

			acif::begin [ac== alu_opcode $dlx::ALU_SLL]
				ac= alu_result_wide [ac<< alu_op1 alu_op2]
			acif::end

			ac= alu_result [indexed alu_result_wide {31 0}]
			ac= alu_CF [indexed alu_result_wide {32}]
			ac= alu_SF [indexed alu_result_wide {31}]
			acif::begin [ac== alu_result 0]
				ac= alu_ZF 1
			acif::end
			
			# rd wdata processing
			acif::begin [ac== rd_source $dlx::RD_ALU_RES]
				ac= rd_wdata alu_result
				ac= rd_rdy 1
			acif::end

			acif::begin [ac== rd_source $dlx::RD_PC_INC]
				ac= rd_wdata nextinstr_addr
				ac= rd_rdy 1
			acif::end

			acif::begin [ac== rd_source $dlx::RD_LHI]
				ac= rd_wdata [ac<< immediate 16]
				ac= rd_rdy 1
			acif::end

			acif::begin [ac== rd_source $dlx::RD_ZF_COND]
				ac= rd_wdata alu_ZF
				ac= rd_rdy 1
			acif::end

			acif::begin [ac== rd_source $dlx::RD_nZF_COND]
				ac= rd_wdata [ac! alu_ZF]
				ac= rd_rdy 1
			acif::end

			acif::begin [ac== rd_source $dlx::RD_CF_COND]
				ac= rd_wdata alu_CF
				ac= rd_rdy 1
			acif::end

			# jump vector processing
			acif::begin jump_cond
				acif::begin [ac&& jump_cond_eqz [ac!= rs1_rdata 0]]
					ac= jump_req 0
				acif::end
				acif::begin [ac&& [ac! jump_cond_eqz] [ac== rs1_rdata 0]]
					ac= jump_req 0
				acif::end
			acif::end

			# mem addr processing
			ac= mem_addr alu_result
			ac= mem_wdata rs2_rdata

			acif::begin [ac== jump_src $dlx::JMP_SRC_OP1]
				ac= jump_vector alu_op1
			acif::end

			acif::begin [ac== jump_src $dlx::JMP_SRC_ALU]
				ac= jump_vector alu_result
			acif::end	

		pipe::pstage MEM

			pipe::p<= jump_req_cmd jump_req
			pipe::p<= jump_vector_cmd jump_vector
			acif::begin jump_req
				pipe::pflush
			acif::end

			acif::begin mem_req
				acif::begin mem_cmd
					pipe::mcopipe::wrreq data_mem 0 [cnct {mem_addr mem_wdata}]
				acif::end
				acif::begelse
					pipe::mcopipe::rdreq data_mem 0 [cnct {mem_addr mem_wdata}]
				acif::end
			acif::end

		pipe::pstage WB

			acif::begin mem_req
				acif::begin [ac! mem_cmd]
					acif::begin [pipe::mcopipe::resp data_mem mem_rdata]
						acif::begin [ac== rd_source $dlx::RD_MEM]
							ac= rd_wdata mem_rdata
							ac= rd_rdy	1
						acif::end
					acif::end
				acif::end
			acif::end

			acif::begin rd_req
				_acc_index rd_addr
				pipe::p<= regfile rd_wdata
			acif::end

	pipe::endpproc

	pipe::_acc_index_wdata {63 0}
	pipe::_acc_index_rdata {31 0}
	pipe::copipe::declare {0 0} instr_mem

	pipe::_acc_index_wdata {63 0}
	pipe::_acc_index_rdata {31 0}
	pipe::copipe::declare {0 0} data_mem

	pipe::mcopipe::connect instrpipe instr_mem instr_mem
	pipe::mcopipe::connect instrpipe data_mem data_mem

	pipe::mcopipe::export instr_mem 0  { \
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

#endmodule
