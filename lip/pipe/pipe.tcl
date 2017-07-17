## See LICENSE file for license details

#### pipe ####
try {namespace delete pipe} on error {} {}
namespace eval pipe {

	proc pvar {dimensions name defval} {
		if {[ActiveCore::isnumeric $defval] == 0} {
			ActiveCore::ERROR default\ value\ of\ pvar\ $name\ is\ not\ a\ number!
		}
		if {[llength $dimensions] != 2} {
			ActiveCore::ERROR dimension\ length\ of\ pvar\ $name\ is\ incorrect!
		} else {
			if {[ActiveCore::isnumeric [lindex $dimensions 0]] == 0} {
				ActiveCore::ERROR Pvar\ $name\ has\ variable\ length!
			}
			if {[ActiveCore::isnumeric [lindex $dimensions 1]] == 0} {
				ActiveCore::ERROR Pvar\ $name\ has\ variable\ length!
			}
			__ac_pipe_pvar_ranged [lindex $dimensions 0] [lindex $dimensions 1] $name $defval
		}
	}

	proc gpvar {dimensions name defval} {
		if {[ActiveCore::isnumeric $defval] == 0} {
			ActiveCore::ERROR default\ value\ of\ pvar\ $name\ is\ not\ a\ number!
		}
		if {[llength $dimensions] != 2} {
			ActiveCore::ERROR dimension\ length\ of\ pvar\ $name\ is\ incorrect!
		} else {
			if {[ActiveCore::isnumeric [lindex $dimensions 0]] == 0} {
				ActiveCore::ERROR Pvar\ $name\ has\ variable\ length!
			}
			if {[ActiveCore::isnumeric [lindex $dimensions 1]] == 0} {
				ActiveCore::ERROR Pvar\ $name\ has\ variable\ length!
			}
			__ac_pipe_gpvar_ranged [lindex $dimensions 0] [lindex $dimensions 1] $name $defval
		}
	}

	proc copipeif {copipeif_name req we ack wdata resp rdata} {
		__ac_pipe_copipeif $copipeif_name $req $we $ack $wdata $resp $rdata
	}

	proc copipereq {copipeif_name we wdata} {
		__ac_core_acc_param_clr
		ActiveCore::_accum_param $we
		ActiveCore::_accum_param $wdata
		__ac_pipe_copipereq $copipeif_name
	}

	proc copiperesp {copipeif_name} {
		__ac_pipe_copiperesp $copipeif_name
	}

	# interface functions
	proc pstage {pstage_name} {
		__ac_pipe_pstage $pstage_name
	}

	proc pbreak {} {
		__ac_pipe_pbreak
	}

	proc pstall {} {
		__ac_pipe_pstall
	}

	proc prepeat {} {
		__ac_pipe_prepeat
	}

	proc pre {ext_signal} {
		__ac_pipe_pre $ext_signal
	}

	proc pwe {pipe_signal ext_signal} {
		__ac_core_acc_param_clr
		ActiveCore::_accum_param $pipe_signal
		__ac_pipe_pwe $ext_signal
	}

	proc prr {pipe_step_name pipe_signal} {
		__ac_pipe_prr $pipe_step_name $pipe_signal
	}

	proc isactive {pipe_step_name} {
		__ac_pipe_isactive $pipe_step_name
	}

	proc isworking {pipe_step_name} {
		__ac_pipe_isworking $pipe_step_name
	}

	proc isstalled {pipe_step_name} {
		__ac_pipe_isstalled $pipe_step_name
	}

	proc isdone {pipe_step_name} {
		__ac_pipe_isdone $pipe_step_name
	}

	proc pproc {name clk rst} {
		__ac_pipe_pproc $name $clk $rst
	}

	proc endpproc {} {
		__ac_pipe_endpproc
	}

	proc export {} {
		__ac_pipe_export
	}

	proc monitor {filename} {
		__ac_pipe_monitor $filename
	}
}




