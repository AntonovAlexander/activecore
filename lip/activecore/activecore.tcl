## See LICENSE file for license details

## utils ##
try {namespace delete ActiveCore} on error {} {}
namespace eval ActiveCore {

	proc reset {} {
		__ac_core_reset
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

	proc _accum_param {param} {
		if {[isnumeric $param] == 1} {
			if {$param == 0} {
				set width 1
			} else {
				set width [getimmlength $param]
			}
			__ac_core_acc_param_c [expr $width - 1] 0 [expr $param + 0]
		} else {
			__ac_core_acc_param_v_rd $param
		}
	}

	proc WARNING {code} {
		puts ActiveCore\ Warning!\ Message:\ $code
	}

	proc CRITICAL_WARNING {code} {
		puts ActiveCore\ Critical\ warning!\ Message:\ $code
	}

	proc ERROR {code} {
		puts ActiveCore\ Error!\ Error\ message:\ $code
		error ActiveCore\ interrupted\ due\ to\ error!
	}

	proc debug_set {} {
		__ac_core_debug_set
	}

	proc debug_clr {} {
		__ac_core_debug_clr
	}

	proc getimmlength {imm} {
    	set imm [expr $imm + 0]
    	if {$imm == 0} {
    		return 1
    	} else {
    		expr [expr {int([expr {floor([expr {log($imm)/log(2)}])}])} + 1]
    	}
	}

	proc expr_1op {opcode op} {
		__ac_core_acc_param_clr
		_accum_param $op
		__ac_core_op $opcode
	}

	proc expr_2op {opcode op1 op2} {
		__ac_core_acc_param_clr
		_accum_param $op1
		_accum_param $op2
		__ac_core_op $opcode
	}

	proc expr_3op {opcode op1 op2 op3} {
		__ac_core_acc_param_clr
		_accum_param $op1
		_accum_param $op2
		_accum_param $op3
		__ac_core_op $opcode
	}

	proc export {language filename} {
		pipe::export
		rtl::export $language $filename
	}
}

proc _acc_index {index} {
	if {[llength $index] > 2} {
		ActiveCore::ERROR Range\ is\ incorrect!
	}
	if {[llength $index] == 1} {
		if {[ActiveCore::isnumeric [lindex $index 0]] == 1} {
			__ac_core_acc_index_c [lindex $index 0]
		} else {
			__ac_core_acc_index_v [lindex $index 0]
		}
	} else {
		if {[ActiveCore::isnumeric [lindex $index 0]] == 1} {
			if {[ActiveCore::isnumeric [lindex $index 1]] == 1} {
				__ac_core_acc_range_cc [lindex $index 0] [lindex $index 1]
			} else {
				__ac_core_acc_range_cv [lindex $index 0] [lindex $index 1]
			}
		} else {
			if {[ActiveCore::isnumeric [lindex $index 1]] == 1} {
				__ac_core_acc_range_vc [lindex $index 0] [lindex $index 1]
			} else {
				__ac_core_acc_range_vv [lindex $index 0] [lindex $index 1]
			}
		}
	}
}

proc s= {target source} {
	if {[ActiveCore::isnumeric $target] == 1} {
		ActiveCore::ERROR Target\ $target\ is\ numeric!
	}
	ActiveCore::_accum_param $source
	__ac_core_assign $target
}

proc s~ {op} {
	ActiveCore::expr_1op "~" $op
}

proc s+ {op1 op2} {
	ActiveCore::expr_2op "+" $op1 $op2
}

proc s- {op1 op2} {
	ActiveCore::expr_2op "-" $op1 $op2
}

proc sx {op1 op2} {
	ActiveCore::expr_2op "x" $op1 $op2
}

proc s/ {op1 op2} {
	ActiveCore::expr_2op "/" $op1 $op2
}

proc s& {op1 op2} {
	ActiveCore::expr_2op "&" $op1 $op2
}

proc s| {op1 op2} {
	ActiveCore::expr_2op "|" $op1 $op2
}

proc s^ {op1 op2} {
	ActiveCore::expr_2op "^" $op1 $op2
}

proc s>> {op1 op2} {
	ActiveCore::expr_2op ">>" $op1 $op2
}

proc s>>> {op1 op2} {
	ActiveCore::expr_2op ">>>" $op1 $op2
}

proc s<< {op1 op2} {
	ActiveCore::expr_2op "<<" $op1 $op2
}

proc s! {op} {
	ActiveCore::expr_1op "!" $op
}

proc s&& {op1 op2} {
	ActiveCore::expr_2op "&&" $op1 $op2
}

proc s|| {op1 op2} {
	ActiveCore::expr_2op "||" $op1 $op2
}

proc s== {op1 op2} {
	ActiveCore::expr_2op "==" $op1 $op2
}

proc s!= {op1 op2} {
	ActiveCore::expr_2op "!=" $op1 $op2
}

proc indexed {op index} {
	if {[llength $index] > 2} {
		ActiveCore::ERROR Index\ $index\ of\ operand\ $op\ is\ incorrect!
	} else {
		if {[llength $index] == 1} {
			ActiveCore::expr_2op "indexed" $op $index
		} else {
			ActiveCore::expr_3op "ranged" $op [lindex $index 0] [lindex $index 1]
		}
	}	
}

proc begif {condition} {
	__ac_core_begif $condition
}

proc begelsif {condition} {
	__ac_core_begelsif $condition
}

proc begelse {} {
	__ac_core_begelse
}

proc endif {} {
	__ac_core_endif
}

proc cnct {ops} {
	__ac_core_acc_param_clr
	foreach op $ops {
		ActiveCore::_accum_param $op
	}
	__ac_core_op "cnct"
}

proc zeroext {op size} {
	if {[ActiveCore::isnumeric $size] == 0} {
		ActiveCore::ERROR size\ cannot\ be\ variable!
	}
	__ac_core_acc_param_clr
	ActiveCore::_accum_param $op
	__ac_core_zeroext $size
}

proc signext {op size} {
	if {[ActiveCore::isnumeric $size] == 0} {
		ActiveCore::ERROR size\ cannot\ be\ variable!
	}
	__ac_core_acc_param_clr
	ActiveCore::_accum_param $op
	__ac_core_signext $size
}

proc ActiveCore_Reset {} {
	rtl::reset
	ActiveCore::reset
}

source [file join $LIP_PATH activecore config.tcl]

#ActiveCore::debug_set
