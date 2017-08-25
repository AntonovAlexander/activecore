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
		__gplc_acc_param_clr
		__gplc_acc_param_string $copipeif_name
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $req
		__gplc_acc_param_v_wr $we
		__gplc_acc_param_v_rd $ack
		__gplc_acc_param_v_wr $wdata
		__gplc_acc_param_v_rd $resp
		__gplc_acc_param_v_rd $rdata
		__mlip_pipe_SetPtrs
		__mlip_pipe_instop copipeif
	}

	proc wrfifoif {wrfifoif_name req ack wdata} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $wrfifoif_name
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $req
		__gplc_acc_param_v_rd $ack
		__gplc_acc_param_v_wr $wdata
		__mlip_pipe_SetPtrs
		__mlip_pipe_instop wrfifoif
	}

	proc rdfifoif {rdfifoif_name req ack rdata} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $rdfifoif_name
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $req
		__gplc_acc_param_v_rd $ack
		__gplc_acc_param_v_rd $rdata
		__mlip_pipe_SetPtrs
		__mlip_pipe_instop rdfifoif
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
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_rd $ext_signal
		__mlip_pipe_SetPtrs
		__mlip_pipe_instop pre
	}

	proc pwe {pipe_signal ext_signal} {
		__gplc_acc_param_clr
		ActiveCore::_accum_param $pipe_signal
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $ext_signal
		__mlip_pipe_SetPtrs
		__mlip_pipe_instop pwe
	}

	proc prr {pstage_name pipe_signal} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__gplc_acc_param_v_rd $pipe_signal
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
		__gplc_acc_param_clr
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_rd $clk
		__gplc_acc_param_v_rd $rst
		__mlip_pipe_SetPtrs
		__gplc_acc_param_string $name
		__mlip_pipe_instop pproc
	}

	proc endpproc {} {
		__mlip_pipe_instop endpproc
	}

	proc export {} {
		__mlip_pipe_instop export
	}

	proc monitor {file_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $file_name
		__mlip_pipe_instop monitor
	}
}




