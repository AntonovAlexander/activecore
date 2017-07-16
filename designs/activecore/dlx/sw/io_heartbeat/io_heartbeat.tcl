source ../dlx_asm.tcl

address: 0x00

	# loading i/o offset (LED address) 0x80000000 to r1 
	lhi r1 r0 0x8000
	# loading increment value to r2
	addi r2 r0 0x01
	# loading initial value from SW to r3
	lw r3 r1 0x04

label: inc_cycle
	sw r3 r1 0x0 
	# incrementing
	add r3 r2 r3
	# repeating
	j inc_cycle

dlx_asm::image_size: 1024
dlx_asm::assemble bin io_heartbeat.bin
dlx_asm::assemble txt_hex io_heartbeat.hex
