## See LICENSE file for license details

rtl::module pavana_xbar

	rtl::input 	{0 0} 	clk_i
	rtl::input 	{0 0} 	rst_i

	rtl::setclk clk_i
	rtl::setrst rst_i

	set mnum 4
	set snum 4
	set mc_copipe_if_indices [list [expr $snum - 1] 0]

	for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {

		pipe::_acc_index_wdata {67 0}
		pipe::_acc_index_rdata {31 0}
		pipe::copipe::declare $mc_copipe_if_indices ms$mnum_idx\_copipe

		pipe::pproc m$mnum_idx\_pipe

			pipe::pvar {67 0} 	transaction	0
			pipe::pvar {31 0} 	address		0
			pipe::pvar {0 0} 	we			0
			pipe::pvar {1 0}	snum 		0
			pipe::pvar {31 0}	rdata 		0

			pipe::_acc_index_wdata {67 0}
			pipe::_acc_index_rdata {31 0}
			pipe::scopipe::declare master

			pipe::_acc_index_wdata {67 0}
			pipe::_acc_index_rdata {31 0}
			pipe::mcopipe::declare $mc_copipe_if_indices slave

			pipe::pstage FETCH

				pipe::scopipe::req master we transaction
				s= address [indexed transaction {67 36}]
				s= snum [indexed address {31 30}]
				s= address [s& address 0x3FFFFFFF]

				begif we
					pipe::mcopipe::wrreq slave snum transaction
				endif
				begelse
					pipe::mcopipe::rdreq slave snum transaction
				endif

			pipe::pstage PROPAGATE

				begnif we
					pipe::pbreak
				endif

				begif [pipe::mcopipe::resp slave rdata]
					pipe::scopipe::resp master rdata
				endif

		pipe::endpproc

		pipe::mcopipe::connect m$mnum_idx\_pipe slave ms$mnum_idx\_copipe
	}

	for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {

		pipe::pproc s$snum_idx\_pipe

			pipe::pvar {67 0} 	transaction	0
			pipe::pvar {31 0} 	address		0
			pipe::pvar {0 0} 	we			0
			pipe::pvar {1 0}	mnum 		0
			pipe::pvar {31 0}	rdata 		0

			for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {
				pipe::_acc_index_wdata {67 0}
				pipe::_acc_index_rdata {31 0}
				pipe::scopipe::declare master$mnum_idx
			}

			pipe::_acc_index_wdata {67 0}
			pipe::_acc_index_rdata {31 0}
			pipe::mcopipe::declare $mc_copipe_if_indices slave

			pipe::pstage FETCH

				#TODO: rr arbiter
				begif [pipe::scopipe::req master0 we transaction]
					s= address [indexed transaction {67 36}]
					s= mnum 0
				endif

				begelsif [pipe::scopipe::req master1 we transaction]
					s= address [indexed transaction {67 36}]
					s= mnum 1
				endif

				begelsif [pipe::scopipe::req master2 we transaction]
					s= address [indexed transaction {67 36}]
					s= mnum 2
				endif

				begelsif [pipe::scopipe::req master3 we transaction]
					s= address [indexed transaction {67 36}]
					s= mnum 3
				endif


				begif we
					pipe::mcopipe::wrreq slave 0 transaction
				endif
				begelse
					pipe::mcopipe::rdreq slave 0 transaction
				endif

			pipe::pstage PROPAGATE

				begnif we
					pipe::pbreak
				endif

				begif [pipe::mcopipe::resp slave rdata]

					for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {
						begif [s== mnum $mnum_idx]
							pipe::scopipe::resp master$mnum_idx rdata
						endif
					}
					
				endif

		pipe::endpproc

		for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {
			pipe::scopipe::connect ms$mnum_idx\_copipe $snum_idx s$snum_idx\_pipe master$mnum_idx
		}
	}

rtl::endmodule
