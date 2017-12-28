onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix hexadecimal /dlx_tb/mpss/clk_i
add wave -noupdate -radix hexadecimal /dlx_tb/mpss/arst_i
add wave -noupdate -radix hexadecimal /dlx_tb/mpss/rx_i
add wave -noupdate -radix hexadecimal /dlx_tb/mpss/tx_o
add wave -noupdate -radix hexadecimal /dlx_tb/mpss/gpio_bi
add wave -noupdate -radix hexadecimal /dlx_tb/mpss/gpio_bo
add wave -noupdate -radix hexadecimal /dlx_tb/mpss/gpio/regfile
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /dlx_tb/mpss/xbar/m0_req
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /dlx_tb/mpss/xbar/m0_ack
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /dlx_tb/mpss/xbar/m0_addr
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /dlx_tb/mpss/xbar/m0_we
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /dlx_tb/mpss/xbar/m0_be
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /dlx_tb/mpss/xbar/m0_wdata
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /dlx_tb/mpss/xbar/m0_resp
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /dlx_tb/mpss/xbar/m0_rdata
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /dlx_tb/mpss/xbar/s0_req
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /dlx_tb/mpss/xbar/s0_ack
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /dlx_tb/mpss/xbar/s0_addr
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /dlx_tb/mpss/xbar/s0_we
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /dlx_tb/mpss/xbar/s0_be
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /dlx_tb/mpss/xbar/s0_wdata
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /dlx_tb/mpss/xbar/s0_resp
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /dlx_tb/mpss/xbar/s0_rdata
add wave -noupdate -expand -group {xbar m8 (udm)} -radix hexadecimal /dlx_tb/mpss/xbar/m8_req
add wave -noupdate -expand -group {xbar m8 (udm)} -radix hexadecimal /dlx_tb/mpss/xbar/m8_ack
add wave -noupdate -expand -group {xbar m8 (udm)} -radix hexadecimal /dlx_tb/mpss/xbar/m8_addr
add wave -noupdate -expand -group {xbar m8 (udm)} -radix hexadecimal /dlx_tb/mpss/xbar/m8_we
add wave -noupdate -expand -group {xbar m8 (udm)} -radix hexadecimal /dlx_tb/mpss/xbar/m8_be
add wave -noupdate -expand -group {xbar m8 (udm)} -radix hexadecimal /dlx_tb/mpss/xbar/m8_wdata
add wave -noupdate -expand -group {xbar m8 (udm)} -radix hexadecimal /dlx_tb/mpss/xbar/m8_resp
add wave -noupdate -expand -group {xbar m8 (udm)} -radix hexadecimal /dlx_tb/mpss/xbar/m8_rdata
add wave -noupdate -expand -group {xbar s8 (gpio)} -radix hexadecimal /dlx_tb/mpss/xbar/s8_req
add wave -noupdate -expand -group {xbar s8 (gpio)} -radix hexadecimal /dlx_tb/mpss/xbar/s8_ack
add wave -noupdate -expand -group {xbar s8 (gpio)} -radix hexadecimal /dlx_tb/mpss/xbar/s8_addr
add wave -noupdate -expand -group {xbar s8 (gpio)} -radix hexadecimal /dlx_tb/mpss/xbar/s8_we
add wave -noupdate -expand -group {xbar s8 (gpio)} -radix hexadecimal /dlx_tb/mpss/xbar/s8_be
add wave -noupdate -expand -group {xbar s8 (gpio)} -radix hexadecimal /dlx_tb/mpss/xbar/s8_wdata
add wave -noupdate -expand -group {xbar s8 (gpio)} -radix hexadecimal /dlx_tb/mpss/xbar/s8_resp
add wave -noupdate -expand -group {xbar s8 (gpio)} -radix hexadecimal /dlx_tb/mpss/xbar/s8_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -radix hexadecimal /dlx_tb/mpss/tile0/clk_i
add wave -noupdate -expand -group {tile0 (cpu)} -radix hexadecimal /dlx_tb/mpss/tile0/rst_i
add wave -noupdate -expand -group {tile0 (cpu)} -radix hexadecimal /dlx_tb/mpss/tile0/corenum
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /dlx_tb/mpss/tile0/hpi_mem_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /dlx_tb/mpss/tile0/hpi_mem_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /dlx_tb/mpss/tile0/hpi_mem_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /dlx_tb/mpss/tile0/hpi_mem_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /dlx_tb/mpss/tile0/hpi_mem_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /dlx_tb/mpss/tile0/hpi_mem_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /dlx_tb/mpss/tile0/hpi_mem_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /dlx_tb/mpss/tile0/hpi_mem_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /dlx_tb/mpss/tile0/xbus_mem_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /dlx_tb/mpss/tile0/xbus_mem_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /dlx_tb/mpss/tile0/xbus_mem_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /dlx_tb/mpss/tile0/xbus_mem_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /dlx_tb/mpss/tile0/xbus_mem_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /dlx_tb/mpss/tile0/xbus_mem_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /dlx_tb/mpss/tile0/xbus_mem_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /dlx_tb/mpss/tile0/xbus_mem_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_instr_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_instr_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_instr_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_instr_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_instr_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_instr_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_instr_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_instr_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_data_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_data_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_data_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_data_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_data_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_data_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_data_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_data_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_internal_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_internal_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_internal_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_internal_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_internal_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_internal_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_internal_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /dlx_tb/mpss/tile0/cpu_internal_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /dlx_tb/mpss/tile0/internal_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /dlx_tb/mpss/tile0/internal_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /dlx_tb/mpss/tile0/internal_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /dlx_tb/mpss/tile0/internal_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /dlx_tb/mpss/tile0/internal_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /dlx_tb/mpss/tile0/internal_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /dlx_tb/mpss/tile0/internal_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /dlx_tb/mpss/tile0/internal_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /dlx_tb/mpss/tile0/dmem_data_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /dlx_tb/mpss/tile0/dmem_data_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /dlx_tb/mpss/tile0/dmem_data_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /dlx_tb/mpss/tile0/dmem_data_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /dlx_tb/mpss/tile0/dmem_data_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /dlx_tb/mpss/tile0/dmem_data_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /dlx_tb/mpss/tile0/dmem_data_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /dlx_tb/mpss/tile0/dmem_data_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /dlx_tb/mpss/tile0/sfr_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /dlx_tb/mpss/tile0/sfr_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /dlx_tb/mpss/tile0/sfr_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /dlx_tb/mpss/tile0/sfr_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /dlx_tb/mpss/tile0/sfr_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /dlx_tb/mpss/tile0/sfr_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /dlx_tb/mpss/tile0/sfr_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /dlx_tb/mpss/tile0/sfr_rdata
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {1640640 ps} 0}
quietly wave cursor active 1
configure wave -namecolwidth 444
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
WaveRestoreZoom {8149680 ps} {9790320 ps}
