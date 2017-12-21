## See LICENSE file for license details

set mnum 4
set snum 4

rtl::module pavana_xbar

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
			rtl::comb {31 0} 	m$mnum_idx\_s$snum_idx\_addr	0
			rtl::comb {0 0} 	m$mnum_idx\_s$snum_idx\_we		0
			rtl::comb {31 0} 	m$mnum_idx\_s$snum_idx\_wdata	0
			rtl::comb {0 0} 	m$mnum_idx\_s$snum_idx\_ack		0
			rtl::comb {31 0} 	m$mnum_idx\_s$snum_idx\_rdata	0
			rtl::comb {0 0} 	m$mnum_idx\_s$snum_idx\_resp	0
		}
	}

	rtl::setclk clk_i
	rtl::setrst rst_i

	for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {

		pipe::pproc m$mnum_idx\_pipe
			
			pipe::pvar {1 0}	snum 		0
			pipe::pvar {31 0} 	address		0
			pipe::pvar {0 0} 	we			0
			pipe::pvar {31 0}	wdata 		0
			pipe::pvar {31 0}	rdata 		0

			pipe::pstage DECODE

				begnif [pipe::pre m$mnum_idx\_req]
					pipe::pstall
				endif

				s= address [pipe::pre m$mnum_idx\_req]
				s= snum [indexed address {31 30}]

				pipe::pwe<= m$mnum_idx\_ack 1

			pipe::pstage SEND

				begnif [pipe::isstalled MRESP]
					for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
						begif [s== snum $snum_idx]
							pipe::pwe m$mnum_idx\_s$snum_idx\_req 		1
							pipe::pwe m$mnum_idx\_s$snum_idx\_addr 		address
							pipe::pwe m$mnum_idx\_s$snum_idx\_we 		we
							pipe::pwe m$mnum_idx\_s$snum_idx\_wdata 	wdata

							begnif [pipe::pre m$mnum_idx\_s$snum_idx\_ack]
								pipe::pstall
							endif
						endif
					}
				endif

			pipe::pstage MRESP

				begnif we
					pipe::pbreak
				endif

				for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
					begif [s== snum $snum_idx]
						s= rdata [pipe::pre m$mnum_idx\_s$snum_idx\_rdata]
						begnif [pipe::pre m$mnum_idx\_s$snum_idx\_ack]
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

			pipe::pvar {31 0} 	address		0
			pipe::pvar {0 0} 	we			0
			pipe::pvar {31 0} 	wdata		0
			pipe::pvar {1 0}	mnum 		0
			pipe::pvar {31 0}	rdata 		0

			# update by <= copmmand
			pipe::psticky	{1 0} 	rr_arbiter	0

			pipe::pstage ARBITER

				for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {
					begif [pipe::pre m$mnum_idx\_s$snum_idx\_req]
						s= mnum $mnum_idx
						pipe::pwe<= m$mnum_idx\_s$snum_idx\_ack 	1
						#pipe::pwe<= rr_arbiter 1
					endif
				}

				begelse
					pipe::pstall
				endif

			pipe::pstage SREQ

				pipe::pwe s$snum_idx\_req 		1
				pipe::pwe s$snum_idx\_addr 		address
				pipe::pwe s$snum_idx\_we 		we
				pipe::pwe s$snum_idx\_wdata 	wdata
				
				begnif [s== [pipe::pre s$snum_idx\_ack] 1]
					pipe::pstall
				endif

			pipe::pstage SRESP

				begnif we
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
