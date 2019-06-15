onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix hexadecimal /xbar_tb/DUV/clk_i
add wave -noupdate -radix hexadecimal /xbar_tb/DUV/rst_i
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_req_i
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_addr_bi
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_we_i
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_wdata_bi
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_ack_o
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_resp_o
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_rdata_bo
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_req_i
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_addr_bi
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_we_i
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_wdata_bi
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_ack_o
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_resp_o
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_rdata_bo
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_req_i
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_addr_bi
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_we_i
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_wdata_bi
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_ack_o
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_resp_o
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_rdata_bo
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_req_i
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_addr_bi
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_we_i
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_wdata_bi
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_ack_o
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_resp_o
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_rdata_bo
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_req_o
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_ack_i
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_addr_bo
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_we_o
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_wdata_bo
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_resp_i
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_rdata_bi
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_req_o
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_ack_i
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_addr_bo
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_we_o
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_resp_i
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_rdata_bi
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_wdata_bo
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_req_o
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_ack_i
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_addr_bo
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_we_o
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_wdata_bo
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_resp_i
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_rdata_bi
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_req_o
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_ack_i
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_addr_bo
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_we_o
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_wdata_bo
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_resp_i
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_rdata_bi
add wave -noupdate -expand -group m0_s0 -radix hexadecimal /xbar_tb/DUV/m0_s0_req
add wave -noupdate -expand -group m0_s0 -radix hexadecimal /xbar_tb/DUV/m0_s_addr
add wave -noupdate -expand -group m0_s0 -radix hexadecimal /xbar_tb/DUV/m0_s_we
add wave -noupdate -expand -group m0_s0 -radix hexadecimal /xbar_tb/DUV/m0_s_wdata
add wave -noupdate -expand -group m0_s0 -radix hexadecimal /xbar_tb/DUV/m0_s0_ack
add wave -noupdate -expand -group m0_s0 -radix hexadecimal /xbar_tb/DUV/m0_s0_rdata
add wave -noupdate -expand -group m0_s0 -radix hexadecimal /xbar_tb/DUV/m0_s0_resp
add wave -noupdate -expand -group m0_s1 -radix hexadecimal /xbar_tb/DUV/m0_s1_req
add wave -noupdate -expand -group m0_s1 -radix hexadecimal /xbar_tb/DUV/m0_s_addr
add wave -noupdate -expand -group m0_s1 -radix hexadecimal /xbar_tb/DUV/m0_s_we
add wave -noupdate -expand -group m0_s1 -radix hexadecimal /xbar_tb/DUV/m0_s_wdata
add wave -noupdate -expand -group m0_s1 -radix hexadecimal /xbar_tb/DUV/m0_s1_ack
add wave -noupdate -expand -group m0_s1 -radix hexadecimal /xbar_tb/DUV/m0_s1_rdata
add wave -noupdate -expand -group m0_s1 -radix hexadecimal /xbar_tb/DUV/m0_s1_resp
add wave -noupdate -expand -group m0_s2 -radix hexadecimal /xbar_tb/DUV/m0_s2_req
add wave -noupdate -expand -group m0_s2 -radix hexadecimal /xbar_tb/DUV/m0_s_addr
add wave -noupdate -expand -group m0_s2 -radix hexadecimal /xbar_tb/DUV/m0_s_we
add wave -noupdate -expand -group m0_s2 -radix hexadecimal /xbar_tb/DUV/m0_s_wdata
add wave -noupdate -expand -group m0_s2 -radix hexadecimal /xbar_tb/DUV/m0_s2_ack
add wave -noupdate -expand -group m0_s2 -radix hexadecimal /xbar_tb/DUV/m0_s2_rdata
add wave -noupdate -expand -group m0_s2 -radix hexadecimal /xbar_tb/DUV/m0_s2_resp
add wave -noupdate -expand -group m0_s3 -radix hexadecimal /xbar_tb/DUV/m0_s3_req
add wave -noupdate -expand -group m0_s3 -radix hexadecimal /xbar_tb/DUV/m0_s_addr
add wave -noupdate -expand -group m0_s3 -radix hexadecimal /xbar_tb/DUV/m0_s_we
add wave -noupdate -expand -group m0_s3 -radix hexadecimal /xbar_tb/DUV/m0_s_wdata
add wave -noupdate -expand -group m0_s3 -radix hexadecimal /xbar_tb/DUV/m0_s3_ack
add wave -noupdate -expand -group m0_s3 -radix hexadecimal /xbar_tb/DUV/m0_s3_rdata
add wave -noupdate -expand -group m0_s3 -radix hexadecimal /xbar_tb/DUV/m0_s3_resp
add wave -noupdate -expand -group m1_s0 -radix hexadecimal /xbar_tb/DUV/m1_s0_req
add wave -noupdate -expand -group m1_s0 -radix hexadecimal /xbar_tb/DUV/m1_s_addr
add wave -noupdate -expand -group m1_s0 -radix hexadecimal /xbar_tb/DUV/m1_s_we
add wave -noupdate -expand -group m1_s0 -radix hexadecimal /xbar_tb/DUV/m1_s_wdata
add wave -noupdate -expand -group m1_s0 -radix hexadecimal /xbar_tb/DUV/m1_s0_ack
add wave -noupdate -expand -group m1_s0 -radix hexadecimal /xbar_tb/DUV/m1_s0_rdata
add wave -noupdate -expand -group m1_s0 -radix hexadecimal /xbar_tb/DUV/m1_s0_resp
add wave -noupdate -expand -group m1_s1 -radix hexadecimal /xbar_tb/DUV/m1_s1_req
add wave -noupdate -expand -group m1_s1 -radix hexadecimal /xbar_tb/DUV/m1_s_addr
add wave -noupdate -expand -group m1_s1 -radix hexadecimal /xbar_tb/DUV/m1_s_we
add wave -noupdate -expand -group m1_s1 -radix hexadecimal /xbar_tb/DUV/m1_s_wdata
add wave -noupdate -expand -group m1_s1 -radix hexadecimal /xbar_tb/DUV/m1_s1_ack
add wave -noupdate -expand -group m1_s1 -radix hexadecimal /xbar_tb/DUV/m1_s1_rdata
add wave -noupdate -expand -group m1_s1 -radix hexadecimal /xbar_tb/DUV/m1_s1_resp
add wave -noupdate -expand -group m1_s2 -radix hexadecimal /xbar_tb/DUV/m1_s2_req
add wave -noupdate -expand -group m1_s2 -radix hexadecimal /xbar_tb/DUV/m1_s_addr
add wave -noupdate -expand -group m1_s2 -radix hexadecimal /xbar_tb/DUV/m1_s_we
add wave -noupdate -expand -group m1_s2 -radix hexadecimal /xbar_tb/DUV/m1_s_wdata
add wave -noupdate -expand -group m1_s2 -radix hexadecimal /xbar_tb/DUV/m1_s2_ack
add wave -noupdate -expand -group m1_s2 -radix hexadecimal /xbar_tb/DUV/m1_s2_rdata
add wave -noupdate -expand -group m1_s2 -radix hexadecimal /xbar_tb/DUV/m1_s2_resp
add wave -noupdate -expand -group m1_s3 -radix hexadecimal /xbar_tb/DUV/m1_s3_req
add wave -noupdate -expand -group m1_s3 -radix hexadecimal /xbar_tb/DUV/m1_s_addr
add wave -noupdate -expand -group m1_s3 -radix hexadecimal /xbar_tb/DUV/m1_s_we
add wave -noupdate -expand -group m1_s3 -radix hexadecimal /xbar_tb/DUV/m1_s_wdata
add wave -noupdate -expand -group m1_s3 -radix hexadecimal /xbar_tb/DUV/m1_s3_ack
add wave -noupdate -expand -group m1_s3 -radix hexadecimal /xbar_tb/DUV/m1_s3_rdata
add wave -noupdate -expand -group m1_s3 -radix hexadecimal /xbar_tb/DUV/m1_s3_resp
add wave -noupdate -expand -group m2_s0 -radix hexadecimal /xbar_tb/DUV/m2_s0_req
add wave -noupdate -expand -group m2_s0 -radix hexadecimal /xbar_tb/DUV/m2_s_addr
add wave -noupdate -expand -group m2_s0 -radix hexadecimal /xbar_tb/DUV/m2_s_we
add wave -noupdate -expand -group m2_s0 -radix hexadecimal /xbar_tb/DUV/m2_s_wdata
add wave -noupdate -expand -group m2_s0 -radix hexadecimal /xbar_tb/DUV/m2_s0_ack
add wave -noupdate -expand -group m2_s0 -radix hexadecimal /xbar_tb/DUV/m2_s0_rdata
add wave -noupdate -expand -group m2_s0 -radix hexadecimal /xbar_tb/DUV/m2_s0_resp
add wave -noupdate -expand -group m2_s1 -radix hexadecimal /xbar_tb/DUV/m2_s1_req
add wave -noupdate -expand -group m2_s1 -radix hexadecimal /xbar_tb/DUV/m2_s_addr
add wave -noupdate -expand -group m2_s1 -radix hexadecimal /xbar_tb/DUV/m2_s_we
add wave -noupdate -expand -group m2_s1 -radix hexadecimal /xbar_tb/DUV/m2_s_wdata
add wave -noupdate -expand -group m2_s1 -radix hexadecimal /xbar_tb/DUV/m2_s1_ack
add wave -noupdate -expand -group m2_s1 -radix hexadecimal /xbar_tb/DUV/m2_s1_rdata
add wave -noupdate -expand -group m2_s1 -radix hexadecimal /xbar_tb/DUV/m2_s1_resp
add wave -noupdate -expand -group m2_s2 -radix hexadecimal /xbar_tb/DUV/m2_s2_req
add wave -noupdate -expand -group m2_s2 -radix hexadecimal /xbar_tb/DUV/m2_s_addr
add wave -noupdate -expand -group m2_s2 -radix hexadecimal /xbar_tb/DUV/m2_s_we
add wave -noupdate -expand -group m2_s2 -radix hexadecimal /xbar_tb/DUV/m2_s_wdata
add wave -noupdate -expand -group m2_s2 -radix hexadecimal /xbar_tb/DUV/m2_s2_ack
add wave -noupdate -expand -group m2_s2 -radix hexadecimal /xbar_tb/DUV/m2_s2_rdata
add wave -noupdate -expand -group m2_s2 -radix hexadecimal /xbar_tb/DUV/m2_s2_resp
add wave -noupdate -expand -group m2_s3 -radix hexadecimal /xbar_tb/DUV/m2_s3_req
add wave -noupdate -expand -group m2_s3 -radix hexadecimal /xbar_tb/DUV/m2_s_addr
add wave -noupdate -expand -group m2_s3 -radix hexadecimal /xbar_tb/DUV/m2_s_we
add wave -noupdate -expand -group m2_s3 -radix hexadecimal /xbar_tb/DUV/m2_s_wdata
add wave -noupdate -expand -group m2_s3 -radix hexadecimal /xbar_tb/DUV/m2_s3_ack
add wave -noupdate -expand -group m2_s3 -radix hexadecimal /xbar_tb/DUV/m2_s3_rdata
add wave -noupdate -expand -group m2_s3 -radix hexadecimal /xbar_tb/DUV/m2_s3_resp
add wave -noupdate -expand -group m3_s0 -radix hexadecimal /xbar_tb/DUV/m3_s0_req
add wave -noupdate -expand -group m3_s0 -radix hexadecimal /xbar_tb/DUV/m3_s_addr
add wave -noupdate -expand -group m3_s0 -radix hexadecimal /xbar_tb/DUV/m3_s_we
add wave -noupdate -expand -group m3_s0 -radix hexadecimal /xbar_tb/DUV/m3_s_wdata
add wave -noupdate -expand -group m3_s0 -radix hexadecimal /xbar_tb/DUV/m3_s0_ack
add wave -noupdate -expand -group m3_s0 -radix hexadecimal /xbar_tb/DUV/m3_s0_rdata
add wave -noupdate -expand -group m3_s0 -radix hexadecimal /xbar_tb/DUV/m3_s0_resp
add wave -noupdate -expand -group m3_s1 -radix hexadecimal /xbar_tb/DUV/m3_s1_req
add wave -noupdate -expand -group m3_s1 -radix hexadecimal /xbar_tb/DUV/m3_s_addr
add wave -noupdate -expand -group m3_s1 -radix hexadecimal /xbar_tb/DUV/m3_s_we
add wave -noupdate -expand -group m3_s1 -radix hexadecimal /xbar_tb/DUV/m3_s_wdata
add wave -noupdate -expand -group m3_s1 -radix hexadecimal /xbar_tb/DUV/m3_s1_ack
add wave -noupdate -expand -group m3_s1 -radix hexadecimal /xbar_tb/DUV/m3_s1_rdata
add wave -noupdate -expand -group m3_s1 -radix hexadecimal /xbar_tb/DUV/m3_s1_resp
add wave -noupdate -expand -group m3_s2 -radix hexadecimal /xbar_tb/DUV/m3_s2_req
add wave -noupdate -expand -group m3_s2 -radix hexadecimal /xbar_tb/DUV/m3_s_addr
add wave -noupdate -expand -group m3_s2 -radix hexadecimal /xbar_tb/DUV/m3_s_we
add wave -noupdate -expand -group m3_s2 -radix hexadecimal /xbar_tb/DUV/m3_s_wdata
add wave -noupdate -expand -group m3_s2 -radix hexadecimal /xbar_tb/DUV/m3_s2_ack
add wave -noupdate -expand -group m3_s2 -radix hexadecimal /xbar_tb/DUV/m3_s2_rdata
add wave -noupdate -expand -group m3_s2 -radix hexadecimal /xbar_tb/DUV/m3_s2_resp
add wave -noupdate -expand -group m3_s3 -radix hexadecimal /xbar_tb/DUV/m3_s3_req
add wave -noupdate -expand -group m3_s3 -radix hexadecimal /xbar_tb/DUV/m3_s_addr
add wave -noupdate -expand -group m3_s3 -radix hexadecimal /xbar_tb/DUV/m3_s_we
add wave -noupdate -expand -group m3_s3 -radix hexadecimal /xbar_tb/DUV/m3_s_wdata
add wave -noupdate -expand -group m3_s3 -radix hexadecimal /xbar_tb/DUV/m3_s3_ack
add wave -noupdate -expand -group m3_s3 -radix hexadecimal /xbar_tb/DUV/m3_s3_rdata
add wave -noupdate -expand -group m3_s3 -radix hexadecimal /xbar_tb/DUV/m3_s3_resp
add wave -noupdate -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpsticky_glbl_rr_arbiter
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_activereq
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_breakreq
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_repeatreq
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_new
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_working
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_succ
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_initiated
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_finish
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_flushreq
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_rdy
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_stalled
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_broken
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_activereq_next
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_genpctrl_breakreq_next
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_mnum
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_address
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_we
add wave -noupdate -expand -group {S0 ARBITER} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_ARBITER_wdata
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_activereq
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_breakreq
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_repeatreq
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_new
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_working
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_succ
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_initiated
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_finish
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_flushreq
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_rdy
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_stalled
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_broken
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_activereq_next
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_genpctrl_breakreq_next
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_address
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_we
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_wdata
add wave -noupdate -expand -group {S0 SREQ} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SREQ_mnum
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_activereq
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_breakreq
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_repeatreq
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_new
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_working
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_succ
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_initiated
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_finish
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_flushreq
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_rdy
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_stalled
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_genpctrl_broken
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_rdata
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_we
add wave -noupdate -expand -group {S0 SRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_s0_pipe_genpstage_SRESP_mnum
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_activereq
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_breakreq
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_repeatreq
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_new
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_working
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_succ
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_initiated
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_finish
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_flushreq
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_rdy
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_stalled
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_broken
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_activereq_next
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_genpctrl_breakreq_next
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_address
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_we
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_wdata
add wave -noupdate -expand -group {M0 DECODE} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_DECODE_snum
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_activereq
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_breakreq
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_repeatreq
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_new
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_working
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_succ
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_initiated
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_finish
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_flushreq
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_rdy
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_stalled
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_broken
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_activereq_next
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_genpctrl_breakreq_next
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_snum
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_address
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_we
add wave -noupdate -expand -group {M0 SEND} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_SEND_wdata
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_activereq
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_breakreq
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_repeatreq
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_new
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_working
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_succ
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_initiated
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_finish
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_flushreq
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_rdy
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_stalled
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_genpctrl_broken
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_rdata
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_we
add wave -noupdate -expand -group {M0 MRESP} -radix hexadecimal /xbar_tb/DUV/genpproc_m0_pipe_genpstage_MRESP_snum
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {81626880 ps} 0}
quietly wave cursor active 1
configure wave -namecolwidth 643
configure wave -valuecolwidth 100
configure wave -justifyvalue left
configure wave -signalnamewidth 0
configure wave -snapdistance 10
configure wave -datasetprefix 0
configure wave -rowmargin 4
configure wave -childrowmargin 2
configure wave -gridoffset 0
configure wave -gridperiod 1
configure wave -griddelta 40
configure wave -timeline 0
configure wave -timelineunits ps
update
WaveRestoreZoom {81357048 ps} {82382952 ps}
