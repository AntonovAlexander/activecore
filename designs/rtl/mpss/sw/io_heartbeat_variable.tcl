source ../../../activecore/dlx/sw/dlx_asm.tcl

address: 0x00
	# loading i/o offset (LED address) 0x80000000 to r1 
	lhi r1 r0 0xC000
	# loading initial value to r2
	addi r2 r0 0x00
	# storing initial value from r2
	sw r2 r1 0x0

label: heartbeat_cycle
	# loading busy wait cycles value from SW to r3
	lw r3 r1 0x00

label: busy_wait_cycle
	# decrementing busy wait cycles value
	subi r3 r3 0x01
	# branch if waiting is not finished
	bnez r3 busy_wait_cycle
	# incrementing r2
	addi r2 r2 0x01
	# storing new value in r2
	sw r2 r1 0x80
	# repeating
	j heartbeat_cycle

dlx_asm::image_size: 1024
dlx_asm::assemble bin io_heartbeat_variable.bin
dlx_asm::assemble txt_hex io_heartbeat_variable.hex
