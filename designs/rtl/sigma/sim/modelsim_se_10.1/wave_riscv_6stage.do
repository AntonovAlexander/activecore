onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix hexadecimal /riscv_tb/sigma/clk_i
add wave -noupdate -radix hexadecimal /riscv_tb/sigma/arst_i
add wave -noupdate -radix hexadecimal /riscv_tb/sigma/srst
add wave -noupdate -radix hexadecimal /riscv_tb/sigma/rx_i
add wave -noupdate -radix hexadecimal /riscv_tb/sigma/tx_o
add wave -noupdate -radix hexadecimal /riscv_tb/sigma/cpu_reset
add wave -noupdate -radix hexadecimal /riscv_tb/sigma/gpio_bi
add wave -noupdate -radix hexadecimal /riscv_tb/sigma/gpio_bo
add wave -noupdate -expand -group {udm bus} -radix hexadecimal /riscv_tb/sigma/udm_reset
add wave -noupdate -expand -group bus_unit -radix hexadecimal /riscv_tb/sigma/bus_unit/clk_i
add wave -noupdate -expand -group bus_unit -radix hexadecimal /riscv_tb/sigma/bus_unit/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_pc
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_req_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_vector_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IADDR_genpctrl_occupied
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IADDR_genpctrl_active_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IADDR_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IADDR_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IADDR_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IADDR_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IADDR_genpctrl_stalled_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IADDR_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IADDR_curinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IADDR_nextinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_active_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_killed_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_stalled_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_occupied
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_nevictable
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genmcopipe_stallreq_unblk
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_curinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_nextinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal /riscv_tb/sigma/bus_unit/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_instr_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_instr_ack
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_instr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_instr_be
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_instr_we
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_instr_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_req_genfifo_reqbuf_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal -childformat {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_req_genfifo_reqbuf_wdata.we -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_req_genfifo_reqbuf_wdata.wdata -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}}}} -subitemconfig {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_req_genfifo_reqbuf_wdata.we {-height 21 -radix hexadecimal} /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_req_genfifo_reqbuf_wdata.wdata {-height 21 -radix hexadecimal -childformat {{addr -radix hexadecimal} {be -radix hexadecimal} {wdata -radix hexadecimal}} -expand} /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_req_genfifo_reqbuf_wdata.wdata.addr {-radix hexadecimal} /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_req_genfifo_reqbuf_wdata.wdata.be {-radix hexadecimal} /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_req_genfifo_reqbuf_wdata.wdata.wdata {-radix hexadecimal}} /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_req_genfifo_reqbuf_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_instr_resp
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {cpu instr bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_instr_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/gen981_var
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/gen1110_var
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {mcopipe instr state} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_wr_ptr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {mcopipe instr state} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_rd_ptr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {mcopipe instr state} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_full_flag
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {mcopipe instr state} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_empty_flag
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {IFETCH instr handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genmcopipe_handle_instr_handle_genvar_if_id
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {IFETCH instr handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genmcopipe_handle_instr_handle_genvar_rdreq_pending
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {IFETCH instr handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genmcopipe_handle_instr_handle_genvar_tid
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {IFETCH instr handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genmcopipe_handle_instr_handle_genvar_resp_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {IFETCH instr handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genmcopipe_handle_instr_handle_genvar_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {IDECODE instr handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genmcopipe_handle_instr_handle_genvar_if_id
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {IDECODE instr handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genmcopipe_handle_instr_handle_genvar_rdreq_pending
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {IDECODE instr handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genmcopipe_handle_instr_handle_genvar_tid
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {IDECODE instr handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genmcopipe_handle_instr_handle_genvar_resp_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IADDR combs} -expand -group {IFETCH combs} -expand -group {IDECODE instr handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genmcopipe_handle_instr_handle_genvar_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal -childformat {{{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[31]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[30]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[29]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[28]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[27]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[26]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[25]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[24]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[23]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[22]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[21]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[20]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[19]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[18]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[17]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[16]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[15]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[14]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[13]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[12]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[11]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[10]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[9]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[8]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[7]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[6]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[5]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[4]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[3]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[2]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[1]} -radix hexadecimal}} -subitemconfig {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[31]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[30]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[29]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[28]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[27]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[26]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[25]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[24]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[23]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[22]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[21]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[20]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[19]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[18]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[17]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[16]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[15]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[14]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[13]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[12]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[11]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[10]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[9]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[8]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[7]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[6]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[5]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[4]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[3]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[2]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[1]} {-height 21 -radix hexadecimal}} /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_req_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_vector_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_instr_req_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genpctrl_active_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genpctrl_killed_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genpctrl_stalled_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genpctrl_occupied
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genpctrl_nevictable
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_curinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_nextinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_instr_code
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_opcode
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_rs1_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_rs1_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_rs1_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_rs2_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_rs2_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_rs2_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_immediate_I
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_immediate_S
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_immediate_B
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_immediate_U
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_immediate_J
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_immediate
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_rd_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_rd_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_rd_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_alu_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_alu_opcode
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_alu_unsigned
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_op1_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_op2_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_funct3
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_funct7
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_shamt
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_pred
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_csrnum
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_zimm
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_jump_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_jump_req_cond
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_jump_src
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_mem_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IDECODE_mem_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IDECODE combs} -expand -group {riscv instr resp bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_rd_ptr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_req_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_vector_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_occupied
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_active_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_stalled_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_curinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_nextinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rs1_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rs2_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_immediate
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_op1_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_op2_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_opcode
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_unsigned
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_op1
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_op2
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_op1_wide
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_op2_wide
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_result_wide
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_result
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_CF
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_SF
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_ZF
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_OF
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_overflow
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_jump_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_jump_req_cond
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_jump_src
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_funct3
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genpctrl_occupied
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genpctrl_active_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genpctrl_stalled_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_data_req_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_rd_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_rd_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_rd_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_rd_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_mem_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_mem_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_mem_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_mem_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {cpu data bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_data_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {cpu data bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_data_ack
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {cpu data bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_data_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {cpu data bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_data_be
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {cpu data bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_data_we
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {cpu data bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_data_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {cpu data bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_data_resp
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {cpu data bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/cpu_data_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {MEM data handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genmcopipe_handle_data_handle_genvar_if_id
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {MEM data handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genmcopipe_handle_data_handle_genvar_rdreq_pending
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {MEM data handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genmcopipe_handle_data_handle_genvar_tid
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {MEM data handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genmcopipe_handle_data_handle_genvar_resp_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {MEM data handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_MEM_genmcopipe_handle_data_handle_genvar_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {WB data handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genmcopipe_handle_data_handle_genvar_if_id
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {WB data handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genmcopipe_handle_data_handle_genvar_rdreq_pending
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {WB data handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genmcopipe_handle_data_handle_genvar_tid
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {WB data handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genmcopipe_handle_data_handle_genvar_resp_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {WB data handle} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genmcopipe_handle_data_handle_genvar_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_full_flag
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_empty_flag
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_wr_ptr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {MEM combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_rd_ptr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genpctrl_occupied
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genpctrl_active_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genpctrl_stalled_glbl
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_rd_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_rd_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_rd_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_rd_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_mem_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_mem_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_WB_mem_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -expand -group {riscv data resp bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_rd_ptr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {WB combs} -expand -group {riscv data resp bus} -radix hexadecimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[7427]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6144]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6145]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6146]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6147]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6148]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6149]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6150]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6151]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6152]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6153]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6154]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6155]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6156]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6157]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6158]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6159]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6160]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6161]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6162]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6163]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6164]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6165]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6166]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6167]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6168]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6169]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6170]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6171]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6172]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6173]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6174]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6175]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6176]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6177]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6178]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6179]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6180]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6181]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6182]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6183]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6184]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6185]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6186]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6187]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6188]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6189]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6190]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6191]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6192]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6193]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6194]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6195]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6196]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6197]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6198]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6199]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6200]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6201]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6202]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6203]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6204]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6205]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6206]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6207]}
add wave -noupdate -expand -group io_buf -radix decimal {/riscv_tb/sigma/bus_unit/ram_dual_memsplit/ram_dual/ram[6208]}
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {32562858 ps} 0}
quietly wave cursor active 1
configure wave -namecolwidth 1041
configure wave -valuecolwidth 164
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
WaveRestoreZoom {32386033 ps} {32643967 ps}
