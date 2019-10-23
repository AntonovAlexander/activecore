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
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {IFETCH ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {IFETCH ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {IFETCH ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_active_ff
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {IFETCH ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_broken_ff
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {IFETCH ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_nextinstr_addr_ff
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {EXEC ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {EXEC ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {EXEC ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {EXEC ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {EXEC ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe ffs} -expand -group {EXEC ffs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_pc
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_req_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_vector_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_initiated
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_activereq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_killreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_broken
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_repeatreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_stalled
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_rdy
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_activereq_next
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genpctrl_killreq_next
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_curinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_nextinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genmcopipe_instr_mem_genvar_req_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genmcopipe_instr_mem_genvar_rdreq_pending
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genmcopipe_instr_mem_genvar_resp_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_IFETCH_genmcopipe_instr_mem_genvar_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mem_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mem_ack
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mem_we
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mem_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mem_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mem_be
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mcopipe_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mcopipe_we
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mcopipe_ack
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mcopipe_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_init_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_init_we
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_init_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_reqbuf_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_reqbuf_we
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_reqbuf_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {IFETCH combs} -expand -group {riscv instr reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_instr_mem_reqbuf_rdy
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal -childformat {{{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[31]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[30]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[29]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[28]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[27]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[26]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[25]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[24]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[23]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[22]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[21]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[20]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[19]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[18]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[17]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[16]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[15]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[14]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[13]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[12]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[11]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[10]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[9]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[8]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[7]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[6]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[5]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[4]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[3]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[2]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[1]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[0]} -radix hexadecimal}} -expand -subitemconfig {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[31]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[30]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[29]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[28]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[27]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[26]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[25]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[24]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[23]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[22]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[21]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[20]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[19]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[18]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[17]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[16]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[15]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[14]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[13]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[12]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[11]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[10]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[9]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[8]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[7]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[6]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[5]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[4]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[3]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[2]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[1]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile[0]} {-height 21 -radix hexadecimal}} /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_regfile
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_req_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_vector_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_initiated
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_activereq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_killreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_broken
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_repeatreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_stalled
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_rdy
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_curinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_nextinstr_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_instr_code
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_opcode
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rs1_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rs1_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rs1_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rs2_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rs2_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rs2_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_immediate_I
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_immediate_S
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_immediate_B
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_immediate_U
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_immediate_J
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_immediate
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_opcode
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_alu_unsigned
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_op1_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_op2_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_funct3
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_funct7
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_shamt
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_pred
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_csrnum
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_zimm
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_jump_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_jump_req_cond
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_jump_src
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_instr_mem_genvar_req_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_instr_mem_genvar_rdreq_pending
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_instr_mem_genvar_resp_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_instr_mem_genvar_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv instr resp bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mem_resp
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv instr resp bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mem_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv instr resp bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mcopipe_resp
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv instr resp bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/instr_mcopipe_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_req_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpsticky_glbl_jump_vector_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_initiated
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_activereq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_killreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_broken
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_repeatreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_stalled
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_rdy
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
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_jump_vector
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_jump_src
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_funct3
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_initiated
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_activereq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_killreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_broken
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_repeatreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_stalled
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_rdy
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_data_mem_genvar_req_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_data_mem_genvar_rdreq_pending
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_data_mem_genvar_resp_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_data_mem_genvar_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_ack
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_we
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_be
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mcopipe_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mcopipe_we
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mcopipe_ack
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data req bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mcopipe_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_init_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_init_we
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_init_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_reqbuf_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_reqbuf_we
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_reqbuf_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data reqbuf} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genmcopipe_data_mem_reqbuf_rdy
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/clk_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/rst_i
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_initiated
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_activereq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_killreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_broken
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_repeatreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_new
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_working
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_succ
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_finish
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_stalled
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_flushreq
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genpctrl_rdy
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_addr
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_wdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_rd_source
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_req
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_cmd
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_mem_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_data_mem_genvar_req_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_data_mem_genvar_rdreq_pending
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_data_mem_genvar_resp_done
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/genpstage_EXEC_genmcopipe_data_mem_genvar_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data resp bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_resp
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data resp bus} -radix hexadecimal -childformat {{{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[31]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[30]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[29]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[28]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[27]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[26]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[25]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[24]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[23]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[22]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[21]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[20]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[19]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[18]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[17]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[16]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[15]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[14]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[13]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[12]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[11]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[10]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[9]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[8]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[7]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[6]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[5]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[4]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[3]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[2]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[1]} -radix hexadecimal} {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[0]} -radix hexadecimal}} -subitemconfig {{/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[31]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[30]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[29]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[28]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[27]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[26]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[25]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[24]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[23]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[22]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[21]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[20]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[19]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[18]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[17]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[16]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[15]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[14]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[13]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[12]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[11]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[10]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[9]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[8]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[7]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[6]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[5]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[4]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[3]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[2]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[1]} {-height 21 -radix hexadecimal} {/riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata[0]} {-height 21 -radix hexadecimal}} /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mem_rdata
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data resp bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mcopipe_resp
add wave -noupdate -expand -group {riscv instrpipe combs} -expand -group {EXEC combs} -expand -group {riscv data resp bus} -radix hexadecimal /riscv_tb/sigma/cpu_wrapper/genblk1/riscv/data_mcopipe_rdata
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
WaveRestoreCursors {{Cursor 1} {315421687 ps} 0}
quietly wave cursor active 1
configure wave -namecolwidth 527
configure wave -valuecolwidth 104
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
WaveRestoreZoom {0 ps} {1050 us}
