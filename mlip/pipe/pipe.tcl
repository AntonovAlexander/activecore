## See LICENSE file for license details

#### pipe ####
try {namespace delete pipe} on error {} {}
namespace eval pipe {

	proc pvar {dimensions name defval} {
		if {[ActiveCore::isnumeric $defval] == 0} {
			ActiveCore::ERROR default\ value\ of\ pvar\ $name\ is\ not\ a\ number!
		}
		_acc_index $dimensions
		__mlip_pipe_pvar $name $defval
	}

	proc gpvar {dimensions name defval} {
		if {[ActiveCore::isnumeric $defval] == 0} {
			ActiveCore::ERROR default\ value\ of\ pvar\ $name\ is\ not\ a\ number!
		}
		_acc_index $dimensions
		__mlip_pipe_gpvar $name $defval
	}

	proc rdprev {name} {
		__mlip_pipe_rdprev $name
	}

	proc copipeif {copipeif_name req we ack wdata resp rdata} {
		__mlip_pipe_copipeif $copipeif_name $req $we $ack $wdata $resp $rdata
	}

	proc copipe_wrreq {copipeif_name param} {
		__gplc_acc_param_clr
		ActiveCore::_accum_param $param
		__mlip_pipe_copipe_wrreq $copipeif_name
	}

	proc copipe_rdreq {copipeif_name param} {
		__gplc_acc_param_clr
		ActiveCore::_accum_param $param
		__mlip_pipe_copipe_rdreq $copipeif_name
	}

	proc copipe_resp {copipeif_name} {
		__mlip_pipe_copipe_resp $copipeif_name
	}

	# interface functions
	proc pstage {pstage_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__mlip_pipe_instop pstage
	}

	proc pbreak {} {
		__gplc_acc_param_clr
		__mlip_pipe_instop pbreak
	}

	proc pstall {} {
		__gplc_acc_param_clr
		__mlip_pipe_instop pstall
	}

	proc prepeat {} {
		__gplc_acc_param_clr
		__mlip_pipe_instop prepeat
	}

	proc pre {ext_signal} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $ext_signal
		__mlip_pipe_instop pre
	}

	proc pwe {pipe_signal ext_signal} {
		__gplc_acc_param_clr
		ActiveCore::_accum_param $pipe_signal
		__gplc_acc_param_string $ext_signal
		__mlip_pipe_instop pwe
	}

	proc prr {pstage_name pipe_signal} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__gplc_acc_param_string $pipe_signal
		__mlip_pipe_instop prr
	}

	proc isactive {pstage_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__mlip_pipe_instop isactive
	}

	proc isworking {pstage_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__mlip_pipe_instop isworking
	}

	proc isstalled {pstage_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__mlip_pipe_instop isstalled
	}

	proc issucc {pstage_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__mlip_pipe_instop issucc
	}

	proc pproc {name clk rst} {
		__mlip_pipe_pproc $name $clk $rst
	}

	proc endpproc {} {
		__mlip_pipe_endpproc
	}

	proc export {} {
		__mlip_pipe_export
	}

	proc monitor {filename} {
		__mlip_pipe_monitor $filename
	}
}




