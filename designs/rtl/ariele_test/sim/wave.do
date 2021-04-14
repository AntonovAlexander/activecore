onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix hexadecimal /xbar_tb/DUV/clk_i
add wave -noupdate -radix hexadecimal /xbar_tb/DUV/rst_i
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_req_i
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_ack_o
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_we_i
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_addr_bi
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_wdata_bi
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_resp_o
add wave -noupdate -expand -group master_0 -radix hexadecimal /xbar_tb/DUV/m0_rdata_bo
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_req_i
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_ack_o
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_we_i
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_addr_bi
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_wdata_bi
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_resp_o
add wave -noupdate -expand -group master_1 -radix hexadecimal /xbar_tb/DUV/m1_rdata_bo
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_req_i
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_ack_o
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_we_i
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_addr_bi
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_wdata_bi
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_resp_o
add wave -noupdate -expand -group master_2 -radix hexadecimal /xbar_tb/DUV/m2_rdata_bo
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_req_i
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_ack_o
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_we_i
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_addr_bi
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_wdata_bi
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_resp_o
add wave -noupdate -expand -group master_3 -radix hexadecimal /xbar_tb/DUV/m3_rdata_bo
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_req_o
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_ack_i
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_we_o
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_addr_bo
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_wdata_bo
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_resp_i
add wave -noupdate -expand -group slave_0 -radix hexadecimal /xbar_tb/DUV/s0_rdata_bi
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_req_o
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_ack_i
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_we_o
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_addr_bo
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_wdata_bo
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_resp_i
add wave -noupdate -expand -group slave_1 -radix hexadecimal /xbar_tb/DUV/s1_rdata_bi
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_req_o
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_ack_i
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_we_o
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_addr_bo
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_wdata_bo
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_resp_i
add wave -noupdate -expand -group slave_2 -radix hexadecimal /xbar_tb/DUV/s2_rdata_bi
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_req_o
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_ack_i
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_we_o
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_addr_bo
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_wdata_bo
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_resp_i
add wave -noupdate -expand -group slave_3 -radix hexadecimal /xbar_tb/DUV/s3_rdata_bi
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group DEC /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_DEC_TRX_BUF_COUNTER
add wave -noupdate -expand -group xbar_m0 -expand -group DEC /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_DEC_TRX_BUF
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_new
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_working
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_succ
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_occupied
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_finish
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_flushreq
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_nevictable
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_genpctrl_rdy
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -expand -group {master 0} -radix hexadecimal /xbar_tb/DUV/m0_req_i
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -expand -group {master 0} -radix hexadecimal /xbar_tb/DUV/m0_ack_o
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -expand -group {master 0} -radix hexadecimal /xbar_tb/DUV/m0_addr_bi
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -expand -group {master 0} -radix hexadecimal /xbar_tb/DUV/m0_we_i
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -expand -group {master 0} -radix hexadecimal /xbar_tb/DUV/m0_wdata_bi
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -expand -group {master 0} -radix hexadecimal /xbar_tb/DUV/m0_resp_o
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -expand -group {master 0} -radix hexadecimal /xbar_tb/DUV/m0_rdata_bo
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_mreq_we
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal -childformat {{/xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_mreq_wdata.addr -radix hexadecimal} {/xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_mreq_wdata.be -radix hexadecimal} {/xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_mreq_wdata.wdata -radix hexadecimal}} -expand -subitemconfig {/xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_mreq_wdata.addr {-height 21 -radix hexadecimal} /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_mreq_wdata.be {-height 21 -radix hexadecimal} /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_mreq_wdata.wdata {-height 21 -radix hexadecimal}} /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_mreq_wdata
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_mcmd_accepted
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_slave_enb0
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_slave_enb1
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_slave_enb2
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_DEC_slave_enb3
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_DEC_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_DEC_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group DEC -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_DEC_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group REQ /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_REQ_TRX_BUF_COUNTER
add wave -noupdate -expand -group xbar_m0 -expand -group REQ /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_REQ_TRX_BUF
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_new
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_working
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_succ
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_occupied
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_finish
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_flushreq
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_nevictable
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_genpctrl_rdy
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_scmd_accepted
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_mreq_we
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal -childformat {{/xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.addr -radix hexadecimal} {/xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.be -radix hexadecimal} {/xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.wdata -radix hexadecimal}} -expand -subitemconfig {/xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.addr {-height 21 -radix hexadecimal} /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.be {-height 21 -radix hexadecimal} /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.wdata {-height 21 -radix hexadecimal}} /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_mreq_wdata
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_reqbuf_req
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_req_o
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_ack_i
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal -childformat {{/xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_wdata_bo.we -radix hexadecimal} {/xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_wdata_bo.wdata -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}}}} -expand -subitemconfig {/xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_wdata_bo.we {-height 21 -radix hexadecimal} /xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_wdata_bo.wdata {-height 21 -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}} -expand} /xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_wdata_bo.wdata.addr {-radix hexadecimal} /xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_wdata_bo.wdata.be {-radix hexadecimal} /xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_wdata_bo.wdata.wdata {-radix hexadecimal}} /xbar_tb/DUV/m0_ariele_xbar_inst/genmcopipe_slave0_req_genfifo_wdata_bo
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -expand -group m0s0 -radix hexadecimal /xbar_tb/DUV/m0s0_ariele_xbar_req
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -expand -group m0s0 -radix hexadecimal /xbar_tb/DUV/m0s0_ariele_xbar_ack
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -expand -group m0s0 -radix hexadecimal -childformat {{/xbar_tb/DUV/m0s0_ariele_xbar_wdata.we -radix hexadecimal} {/xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}}}} -expand -subitemconfig {/xbar_tb/DUV/m0s0_ariele_xbar_wdata.we {-height 21 -radix hexadecimal} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata {-height 21 -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}} -expand} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata.addr {-radix hexadecimal} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata.be {-radix hexadecimal} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata.wdata {-radix hexadecimal}} /xbar_tb/DUV/m0s0_ariele_xbar_wdata
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_slave_enb0
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_slave_enb1
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_slave_enb2
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_REQ_slave_enb3
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_REQ_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_REQ_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_REQ_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group REQ /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_genpctrl_new
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_SEQ_TRX_BUF_COUNTER
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_SEQ_TRX_BUF
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_new
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_working
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_succ
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_occupied
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_finish
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_flushreq
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_nevictable
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_genpctrl_rdy
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_SEQ_rdata
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_SEQ_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_SEQ_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group SEQ -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_SEQ_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_RESP_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_RESP_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_RESP_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group RESP /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_RESP_TRX_BUF_COUNTER
add wave -noupdate -expand -group xbar_m0 -expand -group RESP /xbar_tb/DUV/m0_ariele_xbar_inst/gensticky_genpstage_RESP_TRX_BUF
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_new
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_working
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_succ
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_occupied
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_finish
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_flushreq
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_nevictable
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_rdy
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_rdata
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group RESP -radix hexadecimal /xbar_tb/DUV/m0_ariele_xbar_inst/genpstage_RESP_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_m0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_genpctrl_new
add wave -noupdate -expand -group {xbar internals} -expand -group m0s0 -radix hexadecimal /xbar_tb/DUV/m0s0_ariele_xbar_req
add wave -noupdate -expand -group {xbar internals} -expand -group m0s0 -radix hexadecimal /xbar_tb/DUV/m0s0_ariele_xbar_ack
add wave -noupdate -expand -group {xbar internals} -expand -group m0s0 -radix hexadecimal -childformat {{/xbar_tb/DUV/m0s0_ariele_xbar_wdata.we -radix hexadecimal} {/xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}}}} -expand -subitemconfig {/xbar_tb/DUV/m0s0_ariele_xbar_wdata.we {-height 21 -radix hexadecimal} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata {-height 21 -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}} -expand} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata.addr {-radix hexadecimal} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata.be {-radix hexadecimal} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata.wdata {-radix hexadecimal}} /xbar_tb/DUV/m0s0_ariele_xbar_wdata
add wave -noupdate -expand -group {xbar internals} -expand -group m0s0 -radix hexadecimal /xbar_tb/DUV/s0m0_ariele_xbar_req
add wave -noupdate -expand -group {xbar internals} -expand -group m0s0 -radix hexadecimal /xbar_tb/DUV/s0m0_ariele_xbar_rdata
add wave -noupdate -expand -group {xbar internals} -expand -group m0s1 -radix hexadecimal /xbar_tb/DUV/m0s1_ariele_xbar_req
add wave -noupdate -expand -group {xbar internals} -expand -group m0s1 -radix hexadecimal /xbar_tb/DUV/m0s1_ariele_xbar_ack
add wave -noupdate -expand -group {xbar internals} -expand -group m0s1 -radix hexadecimal /xbar_tb/DUV/m0s1_ariele_xbar_wdata
add wave -noupdate -expand -group {xbar internals} -expand -group m0s1 -radix hexadecimal /xbar_tb/DUV/s1m0_ariele_xbar_req
add wave -noupdate -expand -group {xbar internals} -expand -group m0s1 -radix hexadecimal /xbar_tb/DUV/s1m0_ariele_xbar_rdata
add wave -noupdate -expand -group {xbar internals} -expand -group m0s2 -radix hexadecimal /xbar_tb/DUV/m0s2_ariele_xbar_req
add wave -noupdate -expand -group {xbar internals} -expand -group m0s2 -radix hexadecimal /xbar_tb/DUV/m0s2_ariele_xbar_ack
add wave -noupdate -expand -group {xbar internals} -expand -group m0s2 -radix hexadecimal /xbar_tb/DUV/m0s2_ariele_xbar_wdata
add wave -noupdate -expand -group {xbar internals} -expand -group m0s2 -radix hexadecimal /xbar_tb/DUV/s2m0_ariele_xbar_req
add wave -noupdate -expand -group {xbar internals} -expand -group m0s2 -radix hexadecimal /xbar_tb/DUV/s2m0_ariele_xbar_rdata
add wave -noupdate -expand -group {xbar internals} -expand -group m0s3 -radix hexadecimal /xbar_tb/DUV/m0s3_ariele_xbar_req
add wave -noupdate -expand -group {xbar internals} -expand -group m0s3 -radix hexadecimal /xbar_tb/DUV/m0s3_ariele_xbar_ack
add wave -noupdate -expand -group {xbar internals} -expand -group m0s3 -radix hexadecimal /xbar_tb/DUV/m0s3_ariele_xbar_wdata
add wave -noupdate -expand -group {xbar internals} -expand -group m0s3 -radix hexadecimal /xbar_tb/DUV/s3m0_ariele_xbar_req
add wave -noupdate -expand -group {xbar internals} -expand -group m0s3 -radix hexadecimal /xbar_tb/DUV/s3m0_ariele_xbar_rdata
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m0s0_ariele_xbar_req
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m0s0_ariele_xbar_ack
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal -childformat {{/xbar_tb/DUV/m0s0_ariele_xbar_wdata.we -radix hexadecimal} {/xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}}}} -expand -subitemconfig {/xbar_tb/DUV/m0s0_ariele_xbar_wdata.we {-height 21 -radix hexadecimal} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata {-height 21 -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}} -expand} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata.addr {-radix hexadecimal} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata.be {-radix hexadecimal} /xbar_tb/DUV/m0s0_ariele_xbar_wdata.wdata.wdata {-radix hexadecimal}} /xbar_tb/DUV/m0s0_ariele_xbar_wdata
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/s0m0_ariele_xbar_req
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/s0m0_ariele_xbar_rdata
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m1s0_ariele_xbar_req
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m1s0_ariele_xbar_ack
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m1s0_ariele_xbar_wdata
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/s0m1_ariele_xbar_req
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/s0m1_ariele_xbar_rdata
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m2s0_ariele_xbar_req
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m2s0_ariele_xbar_ack
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m2s0_ariele_xbar_wdata
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/s0m2_ariele_xbar_req
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/s0m2_ariele_xbar_rdata
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m3s0_ariele_xbar_req
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m3s0_ariele_xbar_ack
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/m3s0_ariele_xbar_wdata
add wave -noupdate -expand -group xbar_s0 -radix hexadecimal /xbar_tb/DUV/s0m3_ariele_xbar_req
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group ARB /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_ARB_TRX_BUF_COUNTER
add wave -noupdate -expand -group xbar_s0 -expand -group ARB /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_ARB_TRX_BUF
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_genpctrl_new
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_genpctrl_working
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_genpctrl_succ
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_genpctrl_occupied
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_genpctrl_finish
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_genpctrl_flushreq
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_genpctrl_nevictable
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpsticky_glbl_rr_arb
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_mreq_we
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal -childformat {{/xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_mreq_wdata.addr -radix hexadecimal} {/xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_mreq_wdata.be -radix hexadecimal} {/xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_mreq_wdata.wdata -radix hexadecimal}} -expand -subitemconfig {/xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_mreq_wdata.addr {-height 21 -radix hexadecimal} /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_mreq_wdata.be {-height 21 -radix hexadecimal} /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_mreq_wdata.wdata {-height 21 -radix hexadecimal}} /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_mreq_wdata
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_ARB_mcmd_accepted
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_req_genfifo_req_i
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal -childformat {{/xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_req_genfifo_rdata_bi.we -radix hexadecimal} {/xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_req_genfifo_rdata_bi.wdata -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}}}} -subitemconfig {/xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_req_genfifo_rdata_bi.we {-height 18 -radix hexadecimal} /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_req_genfifo_rdata_bi.wdata {-height 18 -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}}} /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_req_genfifo_rdata_bi.wdata.addr {-radix hexadecimal} /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_req_genfifo_rdata_bi.wdata.be {-radix hexadecimal} /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_req_genfifo_rdata_bi.wdata.wdata {-radix hexadecimal}} /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_req_genfifo_rdata_bi
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_req_genfifo_ack_o
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master1_req_genfifo_req_i
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master1_req_genfifo_rdata_bi
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master1_req_genfifo_ack_o
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master2_req_genfifo_req_i
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master2_req_genfifo_rdata_bi
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master2_req_genfifo_ack_o
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master3_req_genfifo_req_i
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master3_req_genfifo_rdata_bi
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master3_req_genfifo_ack_o
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_resp_genfifo_req_o
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_resp_genfifo_wdata_bo
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master0_resp_genfifo_ack_i
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master1_resp_genfifo_req_o
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master1_resp_genfifo_wdata_bo
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master1_resp_genfifo_ack_i
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master2_resp_genfifo_req_o
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master2_resp_genfifo_wdata_bo
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master2_resp_genfifo_ack_i
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master3_resp_genfifo_req_o
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master3_resp_genfifo_wdata_bo
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genscopipe_master3_resp_genfifo_ack_i
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_ARB_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_ARB_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group ARB -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_ARB_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group REQ /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_REQ_TRX_BUF_COUNTER
add wave -noupdate -expand -group xbar_s0 -expand -group REQ /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_REQ_TRX_BUF
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_genpctrl_working
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_genpctrl_succ
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_genpctrl_occupied
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_genpctrl_finish
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_genpctrl_flushreq
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_genpctrl_nevictable
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_scmd_accepted
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_mreq_we
add wave -noupdate -expand -group xbar_s0 -expand -group REQ -radix hexadecimal -childformat {{/xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.addr -radix hexadecimal} {/xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.be -radix hexadecimal} {/xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.wdata -radix hexadecimal}} -expand -subitemconfig {/xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.addr {-height 18 -radix hexadecimal} /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.be {-height 18 -radix hexadecimal} /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_mreq_wdata.wdata {-height 18 -radix hexadecimal}} /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_REQ_mreq_wdata
add wave -noupdate -expand -group xbar_s0 -expand -group REQ /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_REQ_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group REQ /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_REQ_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group REQ /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_REQ_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_genpctrl_killed_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_RESP_TRX_BUF_COUNTER
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_RESP_TRX_BUF
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_genpctrl_working
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_genpctrl_succ
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_genpctrl_occupied
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_genpctrl_finish
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_genpctrl_flushreq
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_genpctrl_nevictable
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/genpstage_RESP_rdata
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_RESP_genpctrl_active_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_RESP_genpctrl_stalled_glbl
add wave -noupdate -expand -group xbar_s0 -expand -group RESP /xbar_tb/DUV/s0_ariele_xbar_inst/gensticky_genpstage_RESP_genpctrl_killed_glbl
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {998559868 ps} 0}
quietly wave cursor active 1
configure wave -namecolwidth 565
configure wave -valuecolwidth 130
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
WaveRestoreZoom {998130924 ps} {999830868 ps}
