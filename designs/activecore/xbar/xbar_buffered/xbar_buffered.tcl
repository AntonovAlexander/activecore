## See LICENSE file for license details

try {namespace delete xbar_buffered} on error {} {}
namespace eval xbar_buffered {

	set mnum 0
	set snum 0

	set addr_map []

	set master_bufsize	0
	set slave_bufsize	0
	set master_seqsize	0

	proc reset {} {
		set xbar_buffered::mnum 0
		set xbar_buffered::snum 0

		set xbar_buffered::addr_map []

		set xbar_buffered::master_bufsize	4
		set xbar_buffered::slave_bufsize	4
		set xbar_buffered::master_seqsize	4
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

		set master_bufsize	$xbar_buffered::master_bufsize
		set slave_bufsize	$xbar_buffered::slave_bufsize
		set master_seqsize	$xbar_buffered::master_seqsize

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

			set master_seqsize_indices [list [expr $master_seqsize - 1] 0]
			set master_seqsize_ptr_width [ActiveCore::getimmlength [expr $master_seqsize - 1]]
			set master_seqsize_ptr_indices [list [expr $master_seqsize_ptr_width - 1] 0]

			for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {

				pipe::pproc m$mnum_idx\_pipe

					pipe::pvar $snum_indices	snum 		0
					pipe::pvar {31 0} 			address		0
					pipe::pvar {0 0} 			we			0
					pipe::pvar {3 0} 			be			0
					pipe::pvar {31 0}			wdata 		0
					pipe::pvar {31 0}			rdata 		0

					# mbuf fifo signals
					_acc_index $master_bufsize_indices
					pipe::psticky_glbl	$snum_indices 	mbuf_fifo_snum		0
					_acc_index $master_bufsize_indices
					pipe::psticky_glbl	{0 0} 	mbuf_fifo_we		0
					_acc_index $master_bufsize_indices
					pipe::psticky_glbl	{31 0} 	mbuf_fifo_addr		0
					_acc_index $master_bufsize_indices
					pipe::psticky_glbl	{31 0} 	mbuf_fifo_wdata		0
					pipe::psticky_glbl	$master_bufsize_ptr_indices 	mbuf_fifo_wptr	0
					pipe::psticky_glbl	$master_bufsize_ptr_indices 	mbuf_fifo_rptr	0
					pipe::psticky_glbl	{0 0} 	mbuf_fifo_empty	1
					pipe::psticky_glbl	{0 0} 	mbuf_fifo_full	0

					# sequencer fifo
					_acc_index $master_seqsize_indices
					pipe::psticky_glbl	$snum_indices 	seq_fifo_data	0
					pipe::psticky_glbl	$master_seqsize_ptr_indices 	seq_fifo_wptr	0
					pipe::psticky_glbl	$master_seqsize_ptr_indices 	seq_fifo_rptr	0
					pipe::psticky_glbl	{0 0} 	seq_fifo_empty	1
					pipe::psticky_glbl	{0 0} 	seq_fifo_full	0

					# sequencer data
					_acc_index $master_seqsize_indices
					pipe::psticky_glbl	{31 0} 	seq_rdata	0
					_acc_index $master_seqsize_indices
					pipe::psticky_glbl	$snum_indices 	seq_snum	0
					pipe::psticky_glbl	$master_seqsize_ptr_indices 	seq_wptr	0

					pipe::pstage DECODE

						acif::begin [ac|| [pipe::rdbuf mbuf_fifo_full] [ac! [pipe::pre m$mnum_idx\_req]]]
							pipe::pstall
						acif::end
						acif::begelse

							# fetching request
							ac= address [pipe::pre m$mnum_idx\_addr]
							ac= we [pipe::pre m$mnum_idx\_we]
							ac= be [pipe::pre m$mnum_idx\_be]
							ac= wdata [pipe::pre m$mnum_idx\_wdata]

							# decoding address
							for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
								acif::begin [ac&& [ac>= address [lindex [lindex $addr_map $snum_idx] 0]] [ac< address [expr [lindex [lindex $addr_map $snum_idx] 0] + [lindex [lindex $addr_map $snum_idx] 1]]]]
									ac= snum $snum_idx
								acif::end
							}

							# put data in fifo
							_acc_index mbuf_fifo_wptr
							ac= mbuf_fifo_snum 	snum
							_acc_index mbuf_fifo_wptr
							ac= mbuf_fifo_we 	we
							_acc_index mbuf_fifo_wptr
							ac= mbuf_fifo_addr 	address
							_acc_index mbuf_fifo_wptr
							ac= mbuf_fifo_wdata 	wdata
							
							ac= mbuf_fifo_wptr [ac+ mbuf_fifo_wptr 1]
							ac= mbuf_fifo_empty 0
							acif::begin [ac== mbuf_fifo_wptr mbuf_fifo_rptr]
								ac= mbuf_fifo_full 1
							acif::end

							pipe::pwe m$mnum_idx\_ack 1
							pipe::pkill

						acif::end
						
						# req fifo -> resp seq fifo processing
						pipe::pactivate
						acif::begin [ac&& [ac! [pipe::rdbuf seq_fifo_full]] [ac! [pipe::rdbuf mbuf_fifo_empty]]]
							# grab data from fifo
							ac= snum 	[indexed [pipe::rdbuf mbuf_fifo_snum] mbuf_fifo_rptr]
							ac= we 		[indexed [pipe::rdbuf mbuf_fifo_we] mbuf_fifo_rptr]
							ac= address [indexed [pipe::rdbuf mbuf_fifo_addr] mbuf_fifo_rptr]
							ac= wdata 	[indexed [pipe::rdbuf mbuf_fifo_wdata] mbuf_fifo_rptr]

							# sending request
							pipe::pwe m$mnum_idx\_s_addr 	address
							pipe::pwe m$mnum_idx\_s_we 		we
							pipe::pwe m$mnum_idx\_s_be 		be
							pipe::pwe m$mnum_idx\_s_wdata 	wdata

							for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
								acif::begin [ac== snum $snum_idx]
									pipe::pwe m$mnum_idx\_s$snum_idx\_req 		1

									acif::begin [pipe::pre m$mnum_idx\_s$snum_idx\_ack]
										ac= mbuf_fifo_rptr [ac+ mbuf_fifo_rptr 1]
										ac= mbuf_fifo_full 0
										acif::begin [ac== mbuf_fifo_wptr mbuf_fifo_rptr]
											ac= mbuf_fifo_empty 1
										acif::end

										acif::begnot we
											# placing request in sequencer fifo
											_acc_index seq_fifo_wptr
											ac= seq_fifo_data snum
											
											ac= seq_fifo_wptr [ac+ seq_fifo_wptr 1]
											ac= seq_fifo_empty 0
											acif::begin [ac== seq_fifo_wptr seq_fifo_rptr]
												ac= seq_fifo_full 1
											acif::end
										acif::end
									acif::end
								acif::end
							}
						acif::end

						# resp seq processing
						pipe::pactivate
						# filling sequencer with data
						for {set snum_idx 0} {$snum_idx < $snum} {incr snum_idx} {
							acif::begin [pipe::pre m$mnum_idx\_s$snum_idx\_resp]
								_acc_index seq_wptr
								ac= seq_snum $snum_idx
								_acc_index seq_wptr
								ac= seq_rdata [pipe::pre m$mnum_idx\_s$snum_idx\_rdata]
								ac= seq_wptr [ac+ seq_wptr 1]
							acif::end
						}
						# checking data from sequencer
						acif::begnot [pipe::rdbuf seq_fifo_empty]
							ac= snum [indexed [pipe::rdbuf seq_fifo_data] seq_fifo_rptr]
							acif::clr
							for {set seq_idx 0} {$seq_idx < $master_seqsize} {incr seq_idx} {
								acif::begelsif [ac&& [ac== snum [indexed [pipe::rdbuf seq_snum] $seq_idx]] [ac< $seq_idx [pipe::rdbuf seq_wptr]]]
									## data present in sequencer

									# reading seq fifo
									ac= seq_fifo_rptr [ac+ seq_fifo_rptr 1]
									ac= seq_fifo_full 0
									acif::begin [ac== seq_fifo_wptr seq_fifo_rptr]
										ac= seq_fifo_empty 1
									acif::end

									# reading seq rdata
									ac= rdata [indexed [pipe::rdbuf seq_rdata] $seq_idx]

									# freeing sequencer from data
									for {set squeeze_seq_idx $seq_idx} {$squeeze_seq_idx < [expr $master_seqsize - 1]} {incr squeeze_seq_idx} {
										aci= seq_snum $squeeze_seq_idx [indexed seq_snum [expr $squeeze_seq_idx + 1]]
										aci= seq_rdata $squeeze_seq_idx [indexed seq_rdata [expr $squeeze_seq_idx + 1]]
									}
									ac= seq_wptr [ac- seq_wptr 1]
								acif::end
							}
							acif::begelse
								pipe::pkill
							acif::end
						acif::end
						acif::begelse
							pipe::pkill
						acif::end

					pipe::pstage MRESP

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

					pipe::psticky_glbl	$mnum_indices 	rr_arbiter	0

					# router fifo signals
					_acc_index $slave_bufsize_indices
					pipe::psticky_glbl	$mnum_indices 	router_fifo_data	0
					pipe::psticky_glbl	$slave_bufsize_ptr_indices 	router_fifo_wptr	0
					pipe::psticky_glbl	$slave_bufsize_ptr_indices 	router_fifo_rptr	0
					pipe::psticky_glbl	{0 0} 	router_fifo_empty	1
					pipe::psticky_glbl	{0 0} 	router_fifo_full	0

					pipe::pstage ARBITER

						acif::clr
						for {set arb_idx 0} {$arb_idx < $mnum} {incr arb_idx} {
							acif::begelsif [ac== rr_arbiter $arb_idx]

								acif::clr
								set mnum_idx2 $arb_idx
								for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {

									acif::begelsif [pipe::pre m$mnum_idx2\_s$snum_idx\_req]
										ac= mnum $mnum_idx2
										ac= address [pipe::pre m$mnum_idx2\_s_addr]
										ac= we 		[pipe::pre m$mnum_idx2\_s_we]
										ac= be 		[pipe::pre m$mnum_idx2\_s_be]
										ac= wdata 	[pipe::pre m$mnum_idx2\_s_wdata]
										pipe::pwe<= m$mnum_idx2\_s$snum_idx\_ack 1


										incr mnum_idx2
										if {$mnum_idx2 > [expr $mnum - 1]} {
											set mnum_idx2 [expr $mnum_idx2 - $mnum]
										}

										pipe::p<= rr_arbiter $mnum_idx2
									acif::end
								}

								acif::begelse
									pipe::pstall
								acif::end

							acif::end
						}

						acif::begelse
							pipe::pkill
						acif::end

					pipe::pstage SREQ

						acif::begin [ac&& [ac! we] [pipe::rdbuf router_fifo_full]]
							pipe::pstall
						acif::end
						acif::begelse
							pipe::pwe s$snum_idx\_req 		1
							pipe::pwe s$snum_idx\_addr 		address
							pipe::pwe s$snum_idx\_we 		we
							pipe::pwe s$snum_idx\_be 		be
							pipe::pwe s$snum_idx\_wdata 	wdata

							acif::begin [pipe::pre s$snum_idx\_ack]
								acif::begin [ac! we]
									# put data in fifo
									_acc_index router_fifo_wptr
									ac= router_fifo_data mnum
									
									ac= router_fifo_wptr [ac+ router_fifo_wptr 1]
									ac= router_fifo_empty 0
									acif::begin [ac== router_fifo_wptr router_fifo_rptr]
										ac= router_fifo_full 1
									acif::end
									pipe::pkill
								acif::end
							acif::end
							acif::begelse
								pipe::pstall
							acif::end
							
						acif::end

						pipe::pactivate
						acif::begin [pipe::pre s$snum_idx\_resp]
							ac= rdata [pipe::pre s$snum_idx\_rdata]
							acif::begin [ac! [pipe::rdbuf router_fifo_empty]]
								# grab data from fifo
								ac= mnum [indexed [pipe::rdbuf router_fifo_data] router_fifo_rptr]
								ac= router_fifo_rptr [ac+ router_fifo_rptr 1]
								ac= router_fifo_full 0
								acif::begin [ac== router_fifo_wptr router_fifo_rptr]
									ac= router_fifo_empty 1
								acif::end
								
								for {set mnum_idx 0} {$mnum_idx < $mnum} {incr mnum_idx} {
									acif::begin [ac== mnum $mnum_idx]
										pipe::pwe m$mnum_idx\_s$snum_idx\_resp 		1
										pipe::pwe m$mnum_idx\_s$snum_idx\_rdata 	rdata
									acif::end
								}
							acif::end
						acif::end

				pipe::endpproc

			}

		rtl::endmodule

	}
}




