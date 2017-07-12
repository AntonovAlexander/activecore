## See LICENSE file for license details

proc get_bram_if {addrwidth datawidth rwmode crmode} {
	set ret_if ""

	if {$crmode == "true"} {lappend ret_if {mosi {0 0} clk}}
	if {$crmode == "true"} {lappend ret_if {mosi {0 0} rst}}
	lappend ret_if {mosi {0 0} en}
	if {$rwmode == "rw"} {lappend ret_if {mosi {0 0} wr}}
	lappend ret_if [list mosi [list [expr $addrwidth - 1] 0] addr]
	if {$rwmode != "r"} {lappend ret_if [list mosi [list [expr $datawidth - 1] 0] wdata]}
	if {$rwmode != "w"} {lappend ret_if [list miso [list [expr $datawidth - 1] 0] rdata]}

	return $ret_if
}

proc genmodule_RAM {width depth rwports rports wports clk_separate} {

	set depth_div [expr log($depth)/log(2)]
	set depth_log [expr int($depth_div)]
	set depth_remainder [expr $depth_div - $depth_log]
	if {$depth_remainder > 0} {set depth_log [expr $depth_log + 1]}

	set module_name ram\_w$width\_d$depth\_rw$rwports\_r$rports\_w$wports
	rtl::module $module_name	

	if {$clk_separate == "false"} {rtl::input {0 0} clk_i}
	if {$clk_separate == "false"} {rtl::input {0 0} rst_i}

	rtl::interface bramrw [get_bram_if $depth_log $width rw $clk_separate]
	rtl::interface bramr [get_bram_if $depth_log $width r $clk_separate]
	rtl::interface bramw [get_bram_if $depth_log $width w $clk_separate]

	set portnum 0
	for {set idx 0} {$idx < $rwports} {incr idx} {
		rtl::interface_inst port$portnum bramrw s
		incr portnum
	}
	for {set idx 0} {$idx < $rports} {incr idx} {
		rtl::interface_inst port$portnum bramr s
		incr portnum
	}
	for {set idx 0} {$idx < $wports} {incr idx} {
		rtl::interface_inst port$portnum bramw s
		incr portnum
	}

	puts Ports\ declaration\ done!

	_acc_index [list 31 0]
	rtl::ff [list 31 0] ram
	if {$clk_separate == "true"} {
		for {set idx 0} {$idx < [expr $rwports + $wports]} {incr idx} {
			set idx_corr $idx
			if {$idx_corr >= $rwports} {set idx_corr [expr $idx_corr + $rports]}
			_acc_index [list [expr $depth - 1] 0]
			rtl::comb [list [expr $width - 1] 0] ram_next$idx_corr 0
			rtl::_mem_addsource ram [list [rtl::interface_getport_this port$idx_corr clk] pos ram_next$idx_corr
		}
	} else {
		_acc_index [list [expr $depth - 1] 0]
		rtl::comb [list [expr $width - 1] 0] ram_next 0		
		rtl::_mem_addsource ram clk_i pos ram_next
	}

	puts RAM\ declaration\ done!

	# write logic
	if {$clk_separate == "true"} {
		for {set idx 0} {$idx < [expr $rwports + $wports]} {incr idx} {
			set idx_corr $idx
			if {$idx_corr >= $rwports} {set idx_corr [expr $idx_corr + $rports]}

			rtl::cproc
				s= ram_next$idx_corr ram
				begif [s&& [rtl::interface_getport_this port$idx_corr en] [interface_getport_this port$idx_corr wr]]
					_acc_index [rtl::interface_getport_this port$idx_corr addr]
					s= ram_next$idx_corr [rtl::interface_getport_this port$idx_corr wdata] 
				endif
			rtl::endcproc
		}
	} else {
		rtl::cproc
		s= ram_next ram
		for {set idx 0} {$idx < $rwports} {incr idx} {
			begif [s&& [rtl::interface_getport_this port$idx en] [rtl::interface_getport_this port$idx wr]]
				_acc_index [rtl::interface_getport_this port$idx addr]
				s= ram_next [rtl::interface_getport_this port$idx wdata]
			endif
		}
		for {set idx [expr $rwports + $rports]} {$idx < [expr $rwports + $rports + $wports]} {incr idx} {
			begif [rtl::interface_getport_this port$idx en]
				_acc_index [rtl::interface_getport_this port$idx addr]
				s= ram_next [rtl::interface_getport_this port$idx wdata]
			endif
		}
		rtl::endcproc
	}

	puts Write\ logic\ declaration\ done!

	# read logic
	for {set idx 0} {$idx < [expr $rwports + $rports]} {incr idx} {
		rtl::comb [list [expr $width - 1] 0] rdata$idx\_next 0
		if {$clk_separate == "true"} {
			rtl::ff [list [expr $width - 1] 0] rdata$idx
			rtl::_mem_addsource rdata$idx [rtl::interface_getport_this port$idx clk] pos rdata$idx\_next
			rtl::_mem_addreset_sync rdata$idx [rtl::interface_getport_this port$idx rst] pos 0
		} else {
			rtl::ff [list [expr $width - 1] 0] rdata$idx
			rtl::_mem_addsource rdata$idx clk_i pos rdata$idx\_next
			rtl::_mem_addreset_sync rdata$idx rst_i pos 0
		}
		
		s= [rtl::interface_getport_this port$idx rdata] rdata$idx
		rtl::cproc
			s= rdata$idx\_next rdata$idx
			begif [rtl::interface_getport_this port$idx en]
				s= rdata$idx\_next [indexed ram [rtl::interface_getport_this port$idx addr]]
			endif
		rtl::endcproc
	}

	puts Read\ logic\ declaration\ done!

	#endmodule

	return $module_name
}

