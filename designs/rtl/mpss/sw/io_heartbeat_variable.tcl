source ../../../activecore/dlx/sw/dlx_asm.tcl

address: 0x00
	# loading SFR offset (corenum address) 0x00010000 to r1 
	lhi r1 r0 0x0001
	# loading corenum value to r2
	lw r2 r1 0x00
	# storing LED offset in r2
	addi r2 r2 0x80
	# loading i/o offset (SW address) 0xС0000000 to r1 
	lhi r1 r0 0xC000
	# storing total LED offset in r2
	add r2 r2 r1
	# loading initial value to r3
	addi r3 r0 0x00
	# storing initial value from r3
	sw r3 r1 0x0

label: heartbeat_cycle
	# loading busy wait cycles value from SW to r4
	lw r4 r1 0x00

label: busy_wait_cycle
	# decrementing busy wait cycles value
	subi r4 r4 0x01
	# branch if waiting is not finished
	bnez r4 busy_wait_cycle
	# incrementing r3
	addi r3 r3 0x01
	# storing new value in r3 in LED register
	sw r3 r2 0x00
	# repeating
	j heartbeat_cycle

dlx_asm::image_size: 1024
dlx_asm::assemble bin io_heartbeat_variable.bin
dlx_asm::assemble txt_hex io_heartbeat_variable.hex
