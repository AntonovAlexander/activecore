## See LICENSE file for license details

## rtl ##
try {namespace delete rtl} on error {} {}
namespace eval rtl {

	proc reset {} {
		__ac_rtl_reset
		set rtl::interfaces		[list]
		set rtl::interface_instances [list]
	}

	proc input {dimension name} {
		_port in $dimension $name
	}

	proc output {dimension name} {
		_port out $dimension $name
	}

	proc inout {dimension name} {
		_port inout $dimension $name
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

	proc _port {type dimensions name} {
		_acc_index $dimensions
		__ac_rtl_port $type $name
	}

	proc comb {dimensions name defval} {
		if {[ActiveCore::isnumeric $defval] == 0} {
			ActiveCore::ERROR default\ value\ of\ comb\ $name\ is\ not\ a\ number!
		}
		_acc_index $dimensions
		__ac_rtl_comb $name $defval
	}

	set SYNC_LEVEL 	false
	set SYNC_EDGE 	true
	set SYNC_POS	false
	set	SYNC_NEG	true

	proc mem {dimensions name sync_levedge} {
		_acc_index $dimensions
		if { $sync_levedge != true && $sync_levedge != false } {
			ActiveCore::ERROR sync\ parameter\ of\ mem\ $name\ is\ not\ a\ number!
		}
		_acc_index $dimension
		__ac_rtl_mem $name $sync_levedge
	}

	proc latch {dimension name} {
		mem $dimension $name $rtl::SYNC_LEVEL
	}

	proc ff {dimension name} {
		mem $dimension $name $rtl::SYNC_EDGE
	}

	proc _mem_addsource {mem_name signal posneg source} {
		__ac_core_acc_param_clr
		ActiveCore::_accum_param $source
		__ac_rtl_mem_addsource $mem_name $signal $posneg
	}

	proc _mem_addreset {mem_name syncasync signal posneg source} {
		__ac_core_acc_param_clr
		ActiveCore::_accum_param $source
		__ac_rtl_mem_addreset $mem_name $syncasync $signal $posneg
	}

	proc _mem_addreset_sync {mem_name signal posneg source} {
		_mem_addreset $mem_name sync $signal $posneg $source
	}

	proc _mem_addreset_async {mem_name signal posneg source} {
		_mem_addreset $mem_name async $signal $posneg $source
	}

	proc ffvar {dimensions name defval clk rst} {
		_acc_index $dimensions
		__ac_rtl_ffvar $name $defval $clk $rst
	}

	proc rdprev {name} {
		__ac_rtl_rdprev $name
	}

	proc cproc {} {
		__ac_rtl_cproc
	}

	proc endcproc {} {
		__ac_rtl_endcproc
	}

	proc module {name} {
		__ac_rtl_module_name $name
	}

	proc export {language filename} {
		__ac_rtl_export $language $filename
	}

	proc monitor {filename} {
		__ac_rtl_monitor $filename
	}
}