## See LICENSE file for license details

## rtl ##
try {namespace delete rtl} on error {} {}
namespace eval rtl {

	proc reset {} {
		__mlip_rtl_reset
		set rtl::interfaces		[list]
		set rtl::interface_instances [list]
	}

	set clk_domain undef
	set rst_domain undef

	proc setclk {signame} {
		set rtl::clk_domain $signame
	}

	proc setrst {signame} {
		set rtl::rst_domain $signame
	}

	proc input {vartype dimension name} {
		_port in $vartype $dimension $name
	}

	proc output {vartype dimension name} {
		_port out $vartype $dimension $name
	}

	proc inout {vartype dimension name} {
		_port inout $vartype $dimension $name
	}

	set interfaces		[list]
	set interface_instances [list]

	proc interface {name ports} {
		# TODO: validation
		set new_interface [list $name $ports]
		lappend rtl::interfaces $new_interface
		return new_interface
	}

	proc interface_inst {inst_name interface_name mode} {
		lappend rtl::interface_instances [list $inst_name $interface_name $mode]

		foreach interface_inst $rtl::interfaces {
			if {[lindex $interface_inst 0] == $interface_name} {
				foreach signal [lindex $interface_inst 1] {
					if {$mode == "m"} {
						if {[lindex $signal 0] == "mosi"} {
							output [lindex $signal 1] $inst_name\_$interface_name\_[lindex $signal 2]\_o
						} elseif {[lindex $signal 0] == "miso"} {
							input [lindex $signal 1] $inst_name\_$interface_name\_[lindex $signal 2]\_i
						} elseif {[lindex $signal 0] == "io"} {
							inout [lindex $signal 1] $inst_name\_$interface_name\_[lindex $signal 2]\_io
						}
					} elseif {$mode == "s"} {
						if {[lindex $signal 0] == "mosi"} {
							input [lindex $signal 1] $inst_name\_$interface_name\_[lindex $signal 2]\_i
						} elseif {[lindex $signal 0] == "miso"} {
							output [lindex $signal 1] $inst_name\_$interface_name\_[lindex $signal 2]\_o
						} elseif {[lindex $signal 0] == "io"} {
							inout [lindex $signal 1] $inst_name\_$interface_name\_[lindex $signal 2]\_io
						}
					}
				}
			}
		}
	}

	proc interface_getports {interface_name mode} {
		set retval ""
		foreach interface_inst $rtl::interfaces {
			if {[lindex $interface_inst 0] == $interface_name} {
				foreach signal [lindex $interface_inst 1] {
					if {$mode == "m"} {
						if {[lindex $signal 0] == "mosi"} {
							lappend retval [list output $interface_name\_[lindex $signal 2]\_o [lindex $signal 1]]
						} elseif {[lindex $signal 0] == "miso"} {
							lappend retval [list input $interface_name\_[lindex $signal 2]\_i [lindex $signal 1]]
						} elseif {[lindex $signal 0] == "io"} {
							lappend retval [list inout $interface_name\_[lindex $signal 2]\_io [lindex $signal 1]]
						}
					} elseif {$mode == "s"} {
						if {[lindex $signal 0] == "mosi"} {
							lappend retval [list input $interface_name\_[lindex $signal 2]\_i [lindex $signal 1]]
						} elseif {[lindex $signal 0] == "miso"} {
							lappend retval [list output $interface_name\_[lindex $signal 2]\_o [lindex $signal 1]]
						} elseif {[lindex $signal 0] == "io"} {
							lappend retval [list inout $interface_name\_[lindex $signal 2]\_io [lindex $signal 1]]
						}
					}
				}
			}
		}
		return $retval
	}

	proc interface_getport_this {interface_inst_name port_name} {
		set retval ""
		set interface_inst_found false
		set interface_found false
		set port_found false

		foreach interface_instance $rtl::interface_instances {
			if {[lindex $interface_instance 0] == $interface_inst_name} {
				set interface_inst_found true
				foreach interface $rtl::interfaces {
					if {[lindex $interface 0] == [lindex $interface_instance 1]} {
						set interface_found true
						foreach interface_port [lindex $interface 1] {
							if {[lindex $interface_port 2] == $port_name} {
								set port_found true
								append retval [lindex $interface_instance 0]
								append retval _
								append retval [lindex $interface 0]
								append retval _
								append retval $port_name
								if {[lindex $interface_port 0] == "mosi"} {
									if {[lindex $interface_instance 2] == "m"} {
										append retval _o
									} elseif {[lindex $interface_instance 2] == "s"} {
										append retval _i
									}
								} elseif {[lindex $interface_port 0] == "miso"} {
									if {[lindex $interface_instance 2] == "m"} {
										append retval _i
									} elseif {[lindex $interface_instance 2] == "s"} {
										append retval _o
									}
								}
							}
						}
					}
				}
			}
		}

		if {$interface_inst_found == false} {
			ActiveCore::ERROR "No interface instance found!"
		} elseif {$interface_found == false} {
			ActiveCore::ERROR "No interface found!"
		} elseif {$port_found == false} {
			ActiveCore::ERROR "No port named $port_name of interface instance $interface_inst_name found!"
		} else {
			return $retval
		}

	}

	proc interface_getwires {interface_name mode} {

	}

	proc interface_getwire {interface_name mode port_name} {

	}

	proc get_if {prefix interface mode} {
		set retval ""
		foreach signal $interface {
			if {$mode == "m"} {
				if {[lindex $signal 0] == "mosi"} {
					lappend retval [list output $prefix\_[lindex $signal 2]\_o [lindex $signal 1]]
				} elseif {[lindex $signal 0] == "miso"} {
					lappend retval [list input $prefix\_[lindex $signal 2]\_i [lindex $signal 1]]
				} elseif {[lindex $signal 0] == "io"} {
					lappend retval [list inout $prefix\_[lindex $signal 2]\_io [lindex $signal 1]]
				}
			} elseif {$mode == "s"} {
				if {[lindex $signal 0] == "mosi"} {
					lappend retval [list input $prefix\_[lindex $signal 2]\_i [lindex $signal 1]]
				} elseif {[lindex $signal 0] == "miso"} {
					lappend retval [list output $prefix\_[lindex $signal 2]\_o [lindex $signal 1]]
				} elseif {[lindex $signal 0] == "io"} {
					lappend retval [list inout $prefix\_[lindex $signal 2]\_io [lindex $signal 1]]
				}
			}
		}
		return $retval
	}

	proc _port {type vartype dimensions name} {
		__gplc_acc_param_clr
		_acc_index $dimensions
		__gplc_acc_param_string $type
		__gplc_acc_param_string $vartype
		__gplc_acc_param_string $name
		__mlip_rtl_call port
	}

	proc comb {vartype dimensions name defval} {
		__gplc_acc_param_clr
		if {[ActiveCore::isnumeric $defval] == 0} {
			ActiveCore::ERROR default\ value\ of\ comb\ $name\ is\ not\ a\ number!
		}
		_acc_index $dimensions
		__gplc_acc_param_string $name
		__gplc_acc_param_string $vartype
		__gplc_acc_param_string $defval
		__mlip_rtl_call comb
	}

	set SYNC_LEVEL 	false
	set SYNC_EDGE 	true
	set SYNC_POS	false
	set	SYNC_NEG	true

	proc mem {vartype dimensions name sync_levedge} {
		__gplc_acc_param_clr
		_acc_index $dimensions
		if { $sync_levedge != true && $sync_levedge != false } {
			ActiveCore::ERROR sync\ parameter\ of\ mem\ $name\ is\ not\ a\ number!
		}
		_acc_index $dimension
		__gplc_acc_param_string $name
		__gplc_acc_param_string $vartype
		__gplc_acc_param_string $sync_levedge
		__mlip_rtl_call mem
	}

	proc latch {dimension name} {
		mem $dimension $name $rtl::SYNC_LEVEL
	}

	proc ff {dimension name} {
		mem $dimension $name $rtl::SYNC_EDGE
	}

	proc _mem_addsource {mem_name signal posneg source} {
		__gplc_acc_param_clr
		__mlip_rtl_accum_mem $mem_name
		__gplc_acc_param_v_rd $signal
		ActiveCore::_accum_param $source
		__gplc_acc_param_string $posneg
		__mlip_rtl_call addsource
	}

	proc _mem_addreset {mem_name syncasync signal posneg source} {
		__gplc_acc_param_clr
		__mlip_rtl_accum_mem $mem_name
		__gplc_acc_param_v_rd $signal
		ActiveCore::_accum_param $source
		__gplc_acc_param_string $syncasync
		__gplc_acc_param_string $posneg
		__mlip_rtl_call addreset
	}

	proc _mem_addreset_sync {mem_name signal posneg source} {
		_mem_addreset $mem_name sync $signal $posneg $source
	}

	proc _mem_addreset_async {mem_name signal posneg source} {
		_mem_addreset $mem_name async $signal $posneg $source
	}

	proc buffered {vartype dimensions name defval} {
		__gplc_acc_param_clr
		_acc_index $dimensions
		__gplc_acc_param_string $name
		__gplc_acc_param_string $vartype
		__gplc_acc_param_string $defval
		__gplc_acc_param_v_rd $rtl::clk_domain
		__gplc_acc_param_v_rd $rtl::rst_domain
		__mlip_rtl_call buffered
	}

	proc sticky {vartype dimensions name defval} {
		__gplc_acc_param_clr
		_acc_index $dimensions
		__gplc_acc_param_string $name
		__gplc_acc_param_string $vartype
		__gplc_acc_param_string $defval
		__gplc_acc_param_v_rd $rtl::clk_domain
		__gplc_acc_param_v_rd $rtl::rst_domain
		__mlip_rtl_call sticky
	}

	proc rdbuf {name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $name
		__mlip_rtl_call rdbuf
	}

	try {namespace delete module} on error {} {}
	namespace eval module {

		proc begin {name} {
			__gplc_acc_param_clr
			__gplc_acc_param_string $name
			__mlip_rtl_call module_begin
		}

		proc end {} {
			puts endmodule\ is\ currently\ a\ stub!
		}
		
	}

	try {namespace delete cproc} on error {} {}
	namespace eval cproc {

		proc begin {} {
			__mlip_rtl_call cproc
		}

		proc end {} {
			__mlip_rtl_call endcproc
		}
		
	}

	proc export {language filename} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $language
		__gplc_acc_param_string $filename
		__mlip_rtl_call export
	}

	proc monitor {file_name} {
		__gplc_acc_param_clr
		__gplc_acc_param_string $file_name
		__mlip_rtl_call monitor
	}
}