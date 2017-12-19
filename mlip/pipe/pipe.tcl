## See LICENSE file for license details

#### pipe ####
try {namespace delete pipe} on error {} {}
namespace eval pipe {

	proc pproc {name} {
		__gplc_acc_param_clr
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_rd $rtl::clk_domain
		__gplc_acc_param_v_rd $rtl::rst_domain
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

	proc psticky {dimensions name defval} {
		if {[ActiveCore::isnumeric $defval] == 0} {
			ActiveCore::ERROR default\ value\ of\ psticky\ $name\ is\ not\ a\ number!
		}
		__gplc_acc_param_clr
		_acc_index $dimensions
		__gplc_acc_param_string $name
		__gplc_acc_param_string $defval
		__mlip_pipe_call psticky
	}

	proc psticky_glbl {dimensions name defval} {
		if {[ActiveCore::isnumeric $defval] == 0} {
			ActiveCore::ERROR default\ value\ of\ psticky_glbl\ $name\ is\ not\ a\ number!
		}
		__gplc_acc_param_clr
		_acc_index $dimensions
		__gplc_acc_param_string $name
		__gplc_acc_param_string $defval
		__mlip_pipe_call psticky_glbl
	}

	proc rdbuf {name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $name
		__mlip_pipe_call rdbuf
	}

	proc p!= {target source} {
		if {[ActiveCore::isnumeric $target] == 1} {
			ActiveCore::ERROR Target\ $target\ is\ numeric!
		}
		ActiveCore::_accum_param $source
		__gplc_acc_param_v_wr $target
		__mlip_pipe_call p!=
	}

	proc p<= {target source} {
		if {[ActiveCore::isnumeric $target] == 1} {
			ActiveCore::ERROR Target\ $target\ is\ numeric!
		}
		ActiveCore::_accum_param $source
		__gplc_acc_param_v_wr $target
		__mlip_pipe_call p<=
	}

	proc _acc_index_wdata {index} {
		if {[llength $index] > 2} {
			ActiveCore::ERROR Range\ is\ incorrect!
		}
		if {[llength $index] == 1} {
			if {[ActiveCore::isnumeric [lindex $index 0]] == 1} {
				__mlip_pipe_acc_copipe_wr_index_c [lindex $index 0]
			} else {
				ActiveCore::ERROR index\ $index\ incorrect!
			}
		} else {
			if {[ActiveCore::isnumeric [lindex $index 0]] == 1} {
				if {[ActiveCore::isnumeric [lindex $index 1]] == 1} {
					__mlip_pipe_acc_copipe_wr_range_cc [lindex $index 0] [lindex $index 1]
				} else {
					ActiveCore::ERROR index\ $index\ incorrect!
				}
			} else {
				ActiveCore::ERROR index\ $index\ incorrect!
			}
		}
	}

	proc _acc_index_rdata {index} {
		if {[llength $index] > 2} {
			ActiveCore::ERROR Range\ is\ incorrect!
		}
		if {[llength $index] == 1} {
			if {[ActiveCore::isnumeric [lindex $index 0]] == 1} {
				__mlip_pipe_acc_copipe_rd_index_c [lindex $index 0]
			} else {
				ActiveCore::ERROR index\ $index\ incorrect!
			}
		} else {
			if {[ActiveCore::isnumeric [lindex $index 0]] == 1} {
				if {[ActiveCore::isnumeric [lindex $index 1]] == 1} {
					__mlip_pipe_acc_copipe_rd_range_cc [lindex $index 0] [lindex $index 1]
				} else {
					ActiveCore::ERROR index\ $index\ incorrect!
				}
			} else {
				ActiveCore::ERROR index\ $index\ incorrect!
			}
		}
	}

	try {namespace delete copipe} on error {} {}
	namespace eval copipe {

		proc declare {dimensions name} {
			__gplc_acc_param_clr
			__gplc_acc_param_string $name
			_acc_index $dimensions
			__mlip_pipe_call copipeif
		}

	}

	try {namespace delete mcopipe} on error {} {}
	namespace eval mcopipe {

		proc declare {dimensions name} {
			__gplc_acc_param_clr
			__gplc_acc_param_string $name
			_acc_index $dimensions
			__mlip_pipe_call mcopipeif
		}

		proc req {mcopipeif_name dimension cmd param} {
			__gplc_acc_param_clr
			ActiveCore::_accum_param $cmd
			ActiveCore::_accum_param $param
			_acc_index $dimension
			__gplc_acc_param_string $mcopipeif_name
			__mlip_pipe_call mcopipe_req
		}

		proc wrreq {mcopipeif_name dimension param} {
			req $mcopipeif_name $dimension 1 $param
		}

		proc rdreq {mcopipeif_name dimension param} {
			req $mcopipeif_name $dimension 0 $param
		}

		proc resp {mcopipeif_name target} {
			__gplc_acc_param_clr
			__gplc_acc_param_v_wr $target
			__gplc_acc_param_string $mcopipeif_name
			__mlip_pipe_call mcopipe_resp
		}

		proc connect {pproc_name mcopipe_name copipe_name} {
			__gplc_acc_param_clr
			__gplc_acc_param_string $pproc_name
			__gplc_acc_param_string $mcopipe_name
			__gplc_acc_param_string $copipe_name
			__mlip_pipe_call mcopipe_connect
		}

		proc export {mcopipeif_name chnum signals} {
			if { [llength $signals] != 6 } {
				ActiveCore::ERROR parameters\ incorrect!
			} else {
				__gplc_acc_param_clr
				__gplc_acc_param_string $mcopipeif_name
				__mlip_rtl_SetPtrs
				__gplc_acc_param_v_wr [lindex $signals 0]
				__gplc_acc_param_v_wr [lindex $signals 1]
				__gplc_acc_param_v_rd [lindex $signals 2]
				__gplc_acc_param_v_wr [lindex $signals 3]
				__gplc_acc_param_v_rd [lindex $signals 4]
				__gplc_acc_param_v_rd [lindex $signals 5]
				__gplc_acc_param_uint $chnum
				__mlip_pipe_call mcopipe_export
			}
		}
	}


	try {namespace delete scopipe} on error {} {}
	namespace eval scopipe {

		proc declare {name} {
			__gplc_acc_param_clr
			__gplc_acc_param_string $name
			__mlip_pipe_call scopipeif
		}

		proc req {mcopipeif_name we data} {
			__gplc_acc_param_clr
			__gplc_acc_param_v_wr $we
			__gplc_acc_param_v_wr $data
			__gplc_acc_param_string $mcopipeif_name
			__mlip_pipe_call scopipe_req
		}

		proc resp {mcopipeif_name data} {
			__gplc_acc_param_clr
			__gplc_acc_param_v_rd $data
			__gplc_acc_param_string $mcopipeif_name
			__mlip_pipe_call scopipe_resp
		}

		proc connect {copipe_name chnum pproc_name scopipe_name} {
			__gplc_acc_param_clr
			__gplc_acc_param_int $chnum
			__gplc_acc_param_string $copipe_name
			__gplc_acc_param_string $pproc_name
			__gplc_acc_param_string $scopipe_name
			__mlip_pipe_call scopipe_connect
		}
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

	proc pwe {ext_signal pipe_signal} {
		__gplc_acc_param_clr
		ActiveCore::_accum_param $pipe_signal
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $ext_signal
		__mlip_pipe_SetPtrs
		__mlip_pipe_call pwe
	}

	proc pwe! {ext_signal pipe_signal} {
		__gplc_acc_param_clr
		ActiveCore::_accum_param $pipe_signal
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $ext_signal
		__mlip_pipe_SetPtrs
		__mlip_pipe_call pwe!
	}

	proc pwe<= {ext_signal pipe_signal} {
		__gplc_acc_param_clr
		ActiveCore::_accum_param $pipe_signal
		__mlip_rtl_SetPtrs
		__gplc_acc_param_v_wr $ext_signal
		__mlip_pipe_SetPtrs
		__mlip_pipe_call pwe<=
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

	proc export {} {
		__mlip_pipe_call export
	}

	proc monitor {file_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $file_name
		__mlip_pipe_call monitor
	}
}




