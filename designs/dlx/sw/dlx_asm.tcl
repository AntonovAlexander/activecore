## See LICENSE file for license details

try {namespace delete dlx_asm} on error {} {}
namespace eval dlx_asm {

	set cmds []
	set labels []
	set cmd_pointer 0
	set imgsize auto

	proc decode_reg {reg} {
		if {$reg == "r0"} {
			return 0
		} elseif {$reg == "r1"} {
			return 1
		} elseif {$reg == "r2"} {
			return 2
		} elseif {$reg == "r3"} {
			return 3
		} elseif {$reg == "r4"} {
			return 4
		} elseif {$reg == "r5"} {
			return 5
		} elseif {$reg == "r6"} {
			return 6
		} elseif {$reg == "r7"} {
			return 7
		} elseif {$reg == "r8"} {
			return 8
		} elseif {$reg == "r9"} {
			return 9
		} elseif {$reg == "r10"} {
			return 10
		} elseif {$reg == "r11"} {
			return 11
		} elseif {$reg == "r12"} {
			return 12
		} elseif {$reg == "r13"} {
			return 13
		} elseif {$reg == "r14"} {
			return 14
		} elseif {$reg == "r15"} {
			return 15
		} elseif {$reg == "r16"} {
			return 16
		} elseif {$reg == "r17"} {
			return 17
		} elseif {$reg == "r18"} {
			return 18
		} elseif {$reg == "r19"} {
			return 19
		} elseif {$reg == "r20"} {
			return 20
		} elseif {$reg == "r21"} {
			return 21
		} elseif {$reg == "r22"} {
			return 22
		} elseif {$reg == "r23"} {
			return 23
		} elseif {$reg == "r24"} {
			return 24
		} elseif {$reg == "r25"} {
			return 25
		} elseif {$reg == "r26"} {
			return 26
		} elseif {$reg == "r27"} {
			return 27
		} elseif {$reg == "r28"} {
			return 28
		} elseif {$reg == "r29"} {
			return 29
		} elseif {$reg == "r30"} {
			return 30
		} elseif {$reg == "r31"} {
			return 31
		} else {
			error Register\ definition\ $reg\ is\ incorrect!
		}
	}

	proc isnumeric {value} {
	    if {![catch {expr {abs($value)}}]} {
	        return 1
	    }
	    set value [string trimleft $value 0]
	    if {![catch {expr {abs($value)}}]} {
	        return 1
	    }
	    return 0
	}

	proc R-type {opcode rd rs1 rs2} {
		set rd_decoded [decode_reg $rd]
		set rs1_decoded [decode_reg $rs1]
		set rs2_decoded [decode_reg $rs2]
		lappend dlx_asm::cmds [list $dlx_asm::cmd_pointer R-type $opcode $rd_decoded $rs1_decoded $rs2_decoded]
		set dlx_asm::cmd_pointer [expr $dlx_asm::cmd_pointer + 4]
	}

	proc I-type {opcode rd rs1 imm} {
		set rd_decoded [decode_reg $rd]
		set rs1_decoded [decode_reg $rs1]
		if {[isnumeric $imm] == 0} {
			error Immediate\ definition\ $imm\ is\ incorrect!
		}
		lappend dlx_asm::cmds [list $dlx_asm::cmd_pointer I-type $opcode $rd_decoded $rs1_decoded $imm]
		set dlx_asm::cmd_pointer [expr $dlx_asm::cmd_pointer + 4]
	}

	proc J-type {opcode label} {
		lappend dlx_asm::cmds [list $dlx_asm::cmd_pointer J-type $opcode $label]
		set dlx_asm::cmd_pointer [expr $dlx_asm::cmd_pointer + 4]
	}

	proc get_label {name} {
		set label_found 0
		foreach label $dlx_asm::labels {
			if {$name == [lindex $label 0]} {
				set ret_val [lindex $label 1]
			}
			set label_found 1
		}
		if {$label_found == 0} {
			error Label\ $name\ is\ not\ found!
		}
		return $ret_val
	}

	proc compute_relative_offset {instr_addr name} {
		set target_addr [get_label $name]
		set ret_val [format %08x [expr ($target_addr - $instr_addr - 4) & 0x0000000003ffffff]]
		return 0x$ret_val
	}

	proc wrcmdcode {fp fmt cmd_code} {
		if {$fmt == "txt_hex"} {
			puts $fp [format %08x $cmd_code]
		} elseif {$fmt == "txt_bin"} {
			puts $fp [format %032b $cmd_code]
		} elseif {$fmt == "bin"} {
			fconfigure $fp -translation binary -encoding binary
			set byte0 [format %c [expr ($cmd_code & 0x000000ff) >> 0]]
		    set byte1 [format %c [expr ($cmd_code & 0x0000ff00) >> 8]]
		    set byte2 [format %c [expr ($cmd_code & 0x00ff0000) >> 16]]
		    set byte3 [format %c [expr ($cmd_code & 0xff000000) >> 24]]
		    puts -nonewline $fp $byte0
		    puts -nonewline $fp $byte1
		    puts -nonewline $fp $byte2
		    puts -nonewline $fp $byte3
		} else {
			error Format\ $fmt\ is\ incorrect!
		}
	}

	proc assemble {fmt outfile} {

		set systemTime [clock seconds]
		puts "#### DLX assembly started ####"
		puts "Time is: [clock format $systemTime -format %d:%m:%y\ %H:%M:%S]"

		set fp [open $outfile w]

		set cursor 0
		foreach cmd $dlx_asm::cmds {
			
			puts instruction:\ \[[format 0x%08x [lindex $cmd 0]]\]\ [lindex $cmd 1]\ [lindex $cmd 2]\ [lindex $cmd 3]\ [lindex $cmd 4]\ [lindex $cmd 5]

			set address [lindex $cmd 0]
			if {$cursor > $address} {
				error Code\ allocation\ error!
			}
			while {$cursor != $address} {
				wrcmdcode $fp $fmt 0x00
				set cursor [expr $cursor + 4]
			}

			if {[lindex $cmd 1] == "R-type"} {
				set cmd_code [expr ([lindex $cmd 4] << 21) + ([lindex $cmd 5] << 16) + ([lindex $cmd 3] << 11) + [lindex $cmd 2]]
			} elseif {[lindex $cmd 1] == "I-type"} {
				set cmd_code [expr ([lindex $cmd 2] << 26) + ([lindex $cmd 4] << 21) + ([lindex $cmd 3] << 16) + [lindex $cmd 5]]
			} elseif {[lindex $cmd 1] == "J-type"} {
				set label [lindex $cmd 3]
				set value [compute_relative_offset [lindex $cmd 0] $label]
				set cmd_code [expr ([lindex $cmd 2] << 26) + $value]
			} elseif {[lindex $cmd 1] == "data"} {
				set dataword [lindex $cmd 2]
				if {[isnumeric $dataword] == 0} {
					error Data\ definition\ $dataword\ is\ incorrect!
				}
				set cmd_code $dataword
			} else {
				error Command\ type\ unknown!
			}

			puts instruction\ code:\ [format 0x%08x $cmd_code]
			wrcmdcode $fp $fmt $cmd_code
			set cursor [expr $cursor + 4]
		}

		if {[isnumeric $dlx_asm::imgsize] == 1} {
			if {$cursor < $dlx_asm::imgsize} {
				while {$cursor != $dlx_asm::imgsize} {
					wrcmdcode $fp $fmt 0x00
					set cursor [expr $cursor + 4]
				}
			} elseif {$cursor > $dlx_asm::imgsize} {
				error Code\ allocation\ error:\ image\ size\ not\ sufficient:\ $dlx_asm::imgsize\ needed:\ $cursor
			}
		} elseif {$dlx_asm::imgsize != "auto"} {
			error Image\ size\ undefined:\ $dlx_asm::imgsize
		}
		close $fp

		puts "#### DLX assembly completed successfully ####"
	}

	proc image_size: {imgsize} {
		set dlx_asm::imgsize $imgsize
	}
}

proc label: {name} {
	lappend dlx_asm::labels [list $name $dlx_asm::cmd_pointer]
}

proc address: {pointer} {
	set dlx_asm::cmd_pointer $pointer
}

proc data: {dataword} {
	lappend dlx_asm::cmds [list $dlx_asm::cmd_pointer data $dataword]
	set dlx_asm::cmd_pointer [expr $dlx_asm::cmd_pointer + 4]
}

proc add {rd rs1 rs2} {
    dlx_asm::R-type 0x20 $rd $rs1 $rs2
}

proc addi {rd rs1 imm} {
    dlx_asm::I-type 0x08 $rd $rs1 $imm
}

proc and {rd rs1 rs2} {
    dlx_asm::R-type 0x24 $rd $rs1 $rs2
}

proc andi {rd rs1 imm} {
    dlx_asm::I-type 0x0C $rd $rs1 $imm
}

proc beqz {rd rs1 imm} {
    dlx_asm::I-type 0x04 $rd $rs1 $imm
}

proc bnez {rd rs1 imm} {
    dlx_asm::I-type 0x05 $rd $rs1 $imm
}

proc j {imm} {
    dlx_asm::J-type 0x02 $imm
}

proc jal {imm} {
    dlx_asm::J-type 0x03 $imm
}

proc jalr {rd rs1 imm} {
    dlx_asm::I-type 0x13 $rd $rs1 $imm
}

proc jr {rd rs1 imm} {
    dlx_asm::I-type 0x12 $rd $rs1 $imm
}

proc lhi {rd rs1 imm} {
    dlx_asm::I-type 0x0f $rd $rs1 $imm
}

proc lw {rd rs1 imm} {
    dlx_asm::I-type 0x23 $rd $rs1 $imm
}

proc or {rd rs1 rs2} {
    dlx_asm::R-type 0x25 $rd $rs1 $rs2
}

proc ori {rd rs1 imm} {
    dlx_asm::I-type 0x0d $rd $rs1 $imm
}

proc seq {rd rs1 rs2} {
    dlx_asm::R-type 0x28 $rd $rs1 $rs2
}

proc seqi {rd rs1 imm} {
    dlx_asm::I-type 0x18 $rd $rs1 $imm
}

proc sle {rd rs1 rs2} {
    dlx_asm::R-type 0x2c $rd $rs1 $rs2
}

proc slei {rd rs1 imm} {
    dlx_asm::I-type 0x1c $rd $rs1 $imm
}

proc sll {rd rs1 rs2} {
    dlx_asm::R-type 0x04 $rd $rs1 $rs2
}

proc slli {rd rs1 imm} {
    dlx_asm::I-type 0x14 $rd $rs1 $imm
}

proc slt {rd rs1 rs2} {
    dlx_asm::R-type 0x2a $rd $rs1 $rs2
}

proc slti {rd rs1 imm} {
    dlx_asm::I-type 0x1a $rd $rs1 $imm
}

proc sne {rd rs1 rs2} {
    dlx_asm::R-type 0x29 $rd $rs1 $rs2
}

proc snei {rd rs1 imm} {
    dlx_asm::I-type 0x19 $rd $rs1 $imm
}

proc sra {rd rs1 rs2} {
    dlx_asm::R-type 0x07 $rd $rs1 $rs2
}

proc srai {rd rs1 imm} {
    dlx_asm::I-type 0x17 $rd $rs1 $imm
}

proc srl {rd rs1 rs2} {
    dlx_asm::R-type 0x06 $rd $rs1 $rs2
}

proc srli {rd rs1 imm} {
    dlx_asm::I-type 0x16 $rd $rs1 $imm
}

proc sub {rd rs1 rs2} {
    dlx_asm::R-type 0x22 $rd $rs1 $rs2
}

proc subi {rd rs1 imm} {
    dlx_asm::I-type 0x0a $rd $rs1 $imm
}

proc sw {rd rs1 imm} {
    dlx_asm::I-type 0x2b $rd $rs1 $imm
}

proc xor {rd rs1 rs2} {
    dlx_asm::R-type 0x26 $rd $rs1 $rs2
}

proc xori {rd rs1 imm} {
    dlx_asm::I-type 0x0e $rd $rs1 $imm
}
