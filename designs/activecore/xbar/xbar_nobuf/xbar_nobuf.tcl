## See LICENSE file for license details

try {namespace delete xbar_nobuf} on error {} {}
namespace eval xbar_nobuf {

	set mnum 0
	set snum 0

	set addr_map []

	proc reset {} {
		set xbar_nobuf::mnum 0
		set xbar_nobuf::snum 0

		set xbar_nobuf::addr_map []
	}

	proc set_mnum {mnum_i} {
		set xbar_nobuf::mnum $mnum_i
	}

	proc set_snum {snum_i} {
		set xbar_nobuf::snum $snum_i
	}

	proc add_slave {addr size} {
		lappend xbar_nobuf::addr_map [list $addr $size]
	}

	proc generate {} {

		if {$xbar_nobuf::mnum < 1} {
			error mnum\ param\ incorrect!
		}

		if {$xbar_nobuf::snum < 1} {
			error snum\ param\ incorrect!
		}

		if {[llength $xbar_nobuf::addr_map] != $xbar_nobuf::snum} {
			error addr_map\ param\ incorrect!
		}

		set mnum $xbar_nobuf::mnum
		set snum $xbar_nobuf::snum
		set addr_map $xbar_nobuf::addr_map

		rtl::module xbar_nobuf

			rtl::input 	{0 0} 	clk_i
			rtl::input 	{0 0} 	rst_i

			for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {
				rtl::input 	{0 0} 	m$mnum_idx\_req
				rtl::input 	{31 0} 	m$mnum_idx\_addr
				rtl::input 	{0 0} 	m$mnum_idx\_we
				rtl::input 	{31 0} 	m$mnum_idx\_wdata
				rtl::output {0 0} 	m$mnum_idx\_ack
				rtl::output {31 0} 	m$mnum_idx\_rdata
				rtl::output {0 0} 	m$mnum_idx\_resp
			}

			for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
				rtl::output {0 0} 	s$snum_idx\_req
				rtl::output {31 0} 	s$snum_idx\_addr
				rtl::output {0 0} 	s$snum_idx\_we
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
				rtl::comb {31 0} 	m$mnum_idx\_s_wdata		0
			}

			rtl::setclk clk_i
			rtl::setrst rst_i

			set mnum_width [ActiveCore::getimmlength [expr $mnum - 1]]
			set mnum_indices [list [expr $mnum_width - 1] 0]

			set snum_width [ActiveCore::getimmlength [expr $snum - 1]]
			set snum_indices [list [expr $snum_width - 1] 0]

			for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {

				pipe::pproc m$mnum_idx\_pipe

					pipe::pvar $snum_indices	snum 		0
					pipe::pvar {31 0} 			address		0
					pipe::pvar {0 0} 			we			0
					pipe::pvar {31 0}			wdata 		0
					pipe::pvar {31 0}			rdata 		0

					pipe::pstage DECODE

						begnif [pipe::pre m$mnum_idx\_req]
							pipe::pstall
						endif

						s= address [pipe::pre m$mnum_idx\_addr]
						s= we [pipe::pre m$mnum_idx\_we]
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
					pipe::pvar {31 0} 			wdata		0
					pipe::pvar $mnum_indices	mnum 		0
					pipe::pvar {31 0}			rdata 		0

					pipe::psticky_glbl	{1 0} 	rr_arbiter	0

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

						begnif [pipe::isstalled SRESP]
							pipe::pwe s$snum_idx\_req 		1
							pipe::pwe s$snum_idx\_addr 		address
							pipe::pwe s$snum_idx\_we 		we
							pipe::pwe s$snum_idx\_wdata 	wdata
							
							begnif [pipe::pre s$snum_idx\_ack]
								pipe::pstall
							endif
						endif

						begelse
							pipe::pstall
						endif

					pipe::pstage SRESP

						begif we
							pipe::pbreak
						endif

						s= rdata [pipe::pre s$snum_idx\_rdata]
						begnif [pipe::pre s$snum_idx\_resp]
							pipe::pstall
						endif

						for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {
							begif [s== mnum $mnum_idx]
								pipe::pwe<= m$mnum_idx\_s$snum_idx\_resp 	1
								pipe::pwe<= m$mnum_idx\_s$snum_idx\_rdata 	rdata
							endif
						}

				pipe::endpproc

			}

		rtl::endmodule

	}
}




