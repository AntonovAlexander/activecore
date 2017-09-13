## See LICENSE file for license details

#### pipe ####
try {namespace delete pipe} on error {} {}
namespace eval pipe {

	proc pproc {name clk rst} {
		__gplc_acc_param_clr
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_rd $clk
		__gplc_acc_param_v_rd $rst
		__mlip_pipe_SetPtrs
		__gplc_acc_param_string $name
		__mlip_pipe_call pproc
	}

	proc endpproc {} {
		__mlip_pipe_call endpproc
	}

	proc pvar {dimensions name defval} {
		if {[ActiveCore::isnumeric $defval] == 0} {
			ActiveCore::ERROR default\ value\ of\ pvar\ $name\ is\ not\ a\ number!
		}
		__gplc_acc_param_clr
		_acc_index $dimensions
		__gplc_acc_param_string $name
		__gplc_acc_param_string $defval
		__mlip_pipe_call pvar
	}

	proc gpvar {dimensions name defval} {
		if {[ActiveCore::isnumeric $defval] == 0} {
			ActiveCore::ERROR default\ value\ of\ pvar\ $name\ is\ not\ a\ number!
		}
		__gplc_acc_param_clr
		_acc_index $dimensions
		__gplc_acc_param_string $name
		__gplc_acc_param_string $defval
		__mlip_pipe_call gpvar
	}

	proc rdprev {name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $name
		__mlip_pipe_call rdprev
	}

	proc copipeif {name dim_wdata dim_rdata} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $name
		_acc_index $dim_wdata
		_acc_index $dim_rdata
		__mlip_pipe_call copipeif
	}

	proc mcopipeif {name dim_wdata dim_rdata} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $name
		_acc_index $dim_wdata
		_acc_index $dim_rdata
		__mlip_pipe_call mcopipeif
	}

	proc mcopipe_export {mcopipeif_name req we ack wdata resp rdata} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $mcopipeif_name
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $req
		__gplc_acc_param_v_wr $we
		__gplc_acc_param_v_rd $ack
		__gplc_acc_param_v_wr $wdata
		__gplc_acc_param_v_rd $resp
		__gplc_acc_param_v_rd $rdata
		__mlip_pipe_call mcopipe_export
	}

	proc copipe_connect {pproc_name mcopipe_name copipe_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pproc_name
		__gplc_acc_param_string $mcopipe_name
		__gplc_acc_param_string $copipe_name
		__mlip_pipe_call copipe_connect
	}

	proc wrfifoif {wrfifoif_name req ack wdata} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $wrfifoif_name
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $req
		__gplc_acc_param_v_rd $ack
		__gplc_acc_param_v_wr $wdata
		__mlip_pipe_SetPtrs
		__mlip_pipe_call wrfifoif
	}

	proc rdfifoif {rdfifoif_name req ack rdata} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $rdfifoif_name
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $req
		__gplc_acc_param_v_rd $ack
		__gplc_acc_param_v_rd $rdata
		__mlip_pipe_SetPtrs
		__mlip_pipe_call rdfifoif
	}

	# interface functions
	proc pstage {pstage_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__mlip_pipe_call pstage
	}

	proc pbreak {} {
		__gplc_acc_param_clr
		__mlip_pipe_call pbreak
	}

	proc pstall {} {
		__gplc_acc_param_clr
		__mlip_pipe_call pstall
	}

	proc prepeat {} {
		__gplc_acc_param_clr
		__mlip_pipe_call prepeat
	}

	proc pflush {} {
		__gplc_acc_param_clr
		__mlip_pipe_call pflush
	}

	proc pre {ext_signal} {
		__gplc_acc_param_clr
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_rd $ext_signal
		__mlip_pipe_SetPtrs
		__mlip_pipe_call pre
	}

	proc pwe {pipe_signal ext_signal} {
		__gplc_acc_param_clr
		ActiveCore::_accum_param $pipe_signal
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $ext_signal
		__mlip_pipe_SetPtrs
		__mlip_pipe_call pwe
	}

	proc prr {pstage_name pipe_signal} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__gplc_acc_param_v_rd $pipe_signal
		__mlip_pipe_call prr
	}

	proc accum {target source} {
		__gplc_acc_param_clr
		__gplc_acc_param_v_wr $target
		ActiveCore::_accum_param $source
		__mlip_pipe_call accum
	}

	proc isactive {pstage_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__mlip_pipe_call isactive
	}

	proc isworking {pstage_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__mlip_pipe_call isworking
	}

	proc isstalled {pstage_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__mlip_pipe_call isstalled
	}

	proc issucc {pstage_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $pstage_name
		__mlip_pipe_call issucc
	}

	proc mcopipe_req {mcopipeif_name cmd param} {
		__gplc_acc_param_clr
		ActiveCore::_accum_param $cmd
		ActiveCore::_accum_param $param
		__mlip_pipe_mcopipe_req $mcopipeif_name
	}

	proc mcopipe_wrreq {mcopipeif_name param} {
		mcopipe_req $mcopipeif_name 1 $param
	}

	proc mcopipe_rdreq {mcopipeif_name param} {
		mcopipe_req $mcopipeif_name 0 $param
	}

	proc mcopipe_resp {mcopipeif_name} {
		__mlip_pipe_mcopipe_resp $mcopipeif_name
	}

	proc export {} {
		__mlip_pipe_call export
	}

	proc monitor {file_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $file_name
		__mlip_pipe_call monitor
	}
}




