## See LICENSE file for license details

try {namespace delete xbar_buffered} on error {} {}
namespace eval xbar_buffered {

	set mnum 0
	set snum 0

	set addr_map []

	proc reset {} {
		set xbar_buffered::mnum 0
		set xbar_buffered::snum 0

		set xbar_buffered::addr_map []
	}

	proc set_mnum {mnum_i} {
		set xbar_buffered::mnum $mnum_i
	}

	proc set_snum {snum_i} {
		set xbar_buffered::snum $snum_i
	}

	proc add_slave {addr size} {
		lappend xbar_buffered::addr_map [list $addr $size]
	}

	proc generate {} {

		if {$xbar_buffered::mnum < 1} {
			error mnum\ param\ incorrect!
		}

		if {$xbar_buffered::snum < 1} {
			error snum\ param\ incorrect!
		}

		if {[llength $xbar_buffered::addr_map] != $xbar_buffered::snum} {
			error addr_map\ param\ incorrect!
		}

		set mnum $xbar_buffered::mnum
		set snum $xbar_buffered::snum
		set addr_map $xbar_buffered::addr_map

		set master_bufsize	8
		set slave_bufsize	8

		rtl::module xbar_buffered

			rtl::input 	{0 0} 	clk_i
			rtl::input 	{0 0} 	rst_i

			for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {
				rtl::input 	{0 0} 	m$mnum_idx\_req
				rtl::input 	{31 0} 	m$mnum_idx\_addr
				rtl::input 	{0 0} 	m$mnum_idx\_we
				rtl::input 	{3 0} 	m$mnum_idx\_be
				rtl::input 	{31 0} 	m$mnum_idx\_wdata
				rtl::output {0 0} 	m$mnum_idx\_ack
				rtl::output {31 0} 	m$mnum_idx\_rdata
				rtl::output {0 0} 	m$mnum_idx\_resp
			}

			for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
				rtl::output {0 0} 	s$snum_idx\_req
				rtl::output {31 0} 	s$snum_idx\_addr
				rtl::output {0 0} 	s$snum_idx\_we
				rtl::output {3 0} 	s$snum_idx\_be
				rtl::output {31 0} 	s$snum_idx\_wdata
				rtl::input 	{0 0} 	s$snum_idx\_ack
				rtl::input 	{31 0} 	s$snum_idx\_rdata
				rtl::input 	{0 0} 	s$snum_idx\_resp
			}

			for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {
				for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
					rtl::comb {0 0} 	m$mnum_idx\_s$snum_idx\_req		0
					rtl::comb {0 0} 	m$mnum_idx\_s$snum_idx\_ack		0
					rtl::comb {31 0} 	m$mnum_idx\_s$snum_idx\_rdata	0
					rtl::comb {0 0} 	m$mnum_idx\_s$snum_idx\_resp	0
				}

				rtl::comb {31 0} 	m$mnum_idx\_s_addr		0
				rtl::comb {0 0} 	m$mnum_idx\_s_we		0
				rtl::comb {3 0} 	m$mnum_idx\_s_be		0
				rtl::comb {31 0} 	m$mnum_idx\_s_wdata		0
			}

			rtl::setclk clk_i
			rtl::setrst rst_i

			set mnum_width [ActiveCore::getimmlength [expr $mnum - 1]]
			set mnum_indices [list [expr $mnum_width - 1] 0]

			set snum_width [ActiveCore::getimmlength [expr $snum - 1]]
			set snum_indices [list [expr $snum_width - 1] 0]

			set master_bufsize_indices [list [expr $master_bufsize - 1] 0]
			set master_bufsize_ptr_width [ActiveCore::getimmlength [expr $master_bufsize - 1]]
			set master_bufsize_ptr_indices [list [expr $master_bufsize_ptr_width - 1] 0]

			set slave_bufsize_indices [list [expr $slave_bufsize - 1] 0]
			set slave_bufsize_ptr_width [ActiveCore::getimmlength [expr $slave_bufsize - 1]]
			set slave_bufsize_ptr_indices [list [expr $slave_bufsize_ptr_width - 1] 0]

			for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {

				pipe::pproc m$mnum_idx\_pipe

					pipe::pvar $snum_indices	snum 		0
					pipe::pvar {31 0} 			address		0
					pipe::pvar {0 0} 			we			0
					pipe::pvar {3 0} 			be			0
					pipe::pvar {31 0}			wdata 		0
					pipe::pvar {31 0}			rdata 		0

					pipe::pstage DECODE

						begnif [pipe::pre m$mnum_idx\_req]
							pipe::pstall
						endif

						s= address [pipe::pre m$mnum_idx\_addr]
						s= we [pipe::pre m$mnum_idx\_we]
						s= be [pipe::pre m$mnum_idx\_be]
						s= wdata [pipe::pre m$mnum_idx\_wdata]

						for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
							begif [s&& [s>= address [lindex [lindex $addr_map $snum_idx] 0]] [s< address [expr [lindex [lindex $addr_map $snum_idx] 0] + [lindex [lindex $addr_map $snum_idx] 1]]]]
								s= snum $snum_idx
							endif
						}

						pipe::pwe<= m$mnum_idx\_ack 1

					pipe::pstage SEND

						pipe::pwe m$mnum_idx\_s_addr 	address
						pipe::pwe m$mnum_idx\_s_we 		we
						pipe::pwe m$mnum_idx\_s_be 		be
						pipe::pwe m$mnum_idx\_s_wdata 	wdata

						begnif [pipe::isstalled MRESP]
							for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
								begif [s== snum $snum_idx]
									pipe::pwe m$mnum_idx\_s$snum_idx\_req 		1

									begnif [pipe::pre m$mnum_idx\_s$snum_idx\_ack]
										pipe::pstall
									endif
								endif
							}
						endif

						begelse
							pipe::pstall
						endif

					pipe::pstage MRESP

						begif we
							pipe::pbreak
						endif

						for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
							begif [s== snum $snum_idx]
								s= rdata [pipe::pre m$mnum_idx\_s$snum_idx\_rdata]
								begnif [pipe::pre m$mnum_idx\_s$snum_idx\_resp]
									pipe::pstall
								endif
							endif
						}

						pipe::pwe<= m$mnum_idx\_resp 1
						pipe::pwe<= m$mnum_idx\_rdata rdata

				pipe::endpproc
			}

			for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {

				pipe::pproc s$snum_idx\_pipe

					pipe::pvar {31 0} 			address		0
					pipe::pvar {0 0} 			we			0
					pipe::pvar {3 0} 			be			0
					pipe::pvar {31 0} 			wdata		0
					pipe::pvar $mnum_indices	mnum 		0
					pipe::pvar {31 0}			rdata 		0

					pipe::psticky_glbl	{1 0} 	rr_arbiter	0

					# router fifo signals
					_acc_index $slave_bufsize_indices
					pipe::psticky_glbl	$mnum_indices 	router_fifo_data	0
					pipe::psticky_glbl	$slave_bufsize_ptr_indices 	router_fifo_wptr	0
					pipe::psticky_glbl	$slave_bufsize_ptr_indices 	router_fifo_rptr	0
					pipe::psticky_glbl	{0 0} 	router_fifo_empty	1
					pipe::psticky_glbl	{0 0} 	router_fifo_full	0

					pipe::pstage ARBITER

						clrif
						for {set arb_idx 0} {$arb_idx < $mnum} {incr arb_idx} {
							begelsif [s== rr_arbiter $arb_idx]

								clrif
								set mnum_idx2 $arb_idx
								for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {

									begelsif [pipe::pre m$mnum_idx2\_s$snum_idx\_req]
										s= mnum $mnum_idx2
										s= address 	[pipe::pre m$mnum_idx2\_s_addr]
										s= we 		[pipe::pre m$mnum_idx2\_s_we]
										s= be 		[pipe::pre m$mnum_idx2\_s_be]
										s= wdata 	[pipe::pre m$mnum_idx2\_s_wdata]
										pipe::pwe<= m$mnum_idx2\_s$snum_idx\_ack 1


										incr mnum_idx2
										if {$mnum_idx2 > [expr $mnum - 1]} {
											set mnum_idx2 [expr $mnum_idx2 - $mnum]
										}

										pipe::p<= rr_arbiter $mnum_idx2
									endif
								}

								begelse
									pipe::pstall
								endif

							endif
						}

						begelse
							pipe::pbreak
						endif

					pipe::pstage SREQ

						begif [s&& [s! we] [pipe::rdbuf router_fifo_full]]
							pipe::pstall
						endif
						begelse
							pipe::pwe s$snum_idx\_req 		1
							pipe::pwe s$snum_idx\_addr 		address
							pipe::pwe s$snum_idx\_we 		we
							pipe::pwe s$snum_idx\_be 		be
							pipe::pwe s$snum_idx\_wdata 	wdata

							begif [pipe::pre s$snum_idx\_ack]
								begif [s! we]
									# put data in fifo
									_acc_index router_fifo_wptr
									s= router_fifo_data mnum
									
									s= router_fifo_wptr [s+ router_fifo_wptr 1]
									s= router_fifo_empty 0
									begif [s== router_fifo_wptr router_fifo_rptr]
										s= router_fifo_full 1
									endif
									pipe::pbreak
								endif
							endif
							begelse
								pipe::pstall
							endif
							
						endif

						pipe::pactivate
						begif [pipe::pre s$snum_idx\_resp]
							s= rdata [pipe::pre s$snum_idx\_rdata]
							begif [s! [pipe::rdbuf router_fifo_empty]]
								# grab data from fifo
								s= mnum [indexed [pipe::rdbuf router_fifo_data] router_fifo_rptr]
								s= router_fifo_rptr [s+ router_fifo_rptr 1]
								s= router_fifo_full 0
								begif [s== router_fifo_wptr router_fifo_rptr]
									s= router_fifo_empty 1
								endif
								
								for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {
									begif [s== mnum $mnum_idx]
										pipe::pwe m$mnum_idx\_s$snum_idx\_resp 		1
										pipe::pwe m$mnum_idx\_s$snum_idx\_rdata 	rdata
									endif
								}
							endif
						endif

				pipe::endpproc

			}

		rtl::endmodule

	}
}




