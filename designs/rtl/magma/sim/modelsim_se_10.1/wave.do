onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix hexadecimal /magma_tb/magma/clk_i
add wave -noupdate -radix hexadecimal /magma_tb/magma/arst_i
add wave -noupdate -radix hexadecimal /magma_tb/magma/rx_i
add wave -noupdate -radix hexadecimal /magma_tb/magma/tx_o
add wave -noupdate -radix hexadecimal /magma_tb/magma/gpio_bi
add wave -noupdate -radix hexadecimal /magma_tb/magma/gpio_bo
add wave -noupdate -radix hexadecimal /magma_tb/magma/gpio/led_register
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /magma_tb/magma/xbar/m0_req
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /magma_tb/magma/xbar/m0_ack
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /magma_tb/magma/xbar/m0_addr
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /magma_tb/magma/xbar/m0_we
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /magma_tb/magma/xbar/m0_be
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /magma_tb/magma/xbar/m0_wdata
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /magma_tb/magma/xbar/m0_resp
add wave -noupdate -expand -group {xbar m0 (cpu xbus)} -radix hexadecimal /magma_tb/magma/xbar/m0_rdata
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /magma_tb/magma/xbar/s0_req
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /magma_tb/magma/xbar/s0_ack
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /magma_tb/magma/xbar/s0_addr
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /magma_tb/magma/xbar/s0_we
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /magma_tb/magma/xbar/s0_be
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /magma_tb/magma/xbar/s0_wdata
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /magma_tb/magma/xbar/s0_resp
add wave -noupdate -expand -group {xbar s0 (cpu hpi)} -radix hexadecimal /magma_tb/magma/xbar/s0_rdata
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/xbar/m4_req
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/xbar/m4_ack
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/xbar/m4_addr
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/xbar/m4_we
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/xbar/m4_be
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/xbar/m4_wdata
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/xbar/m4_resp
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/xbar/m4_rdata
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/xbar/s4_req
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/xbar/s4_ack
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/xbar/s4_addr
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/xbar/s4_we
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/xbar/s4_be
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/xbar/s4_wdata
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/xbar/s4_resp
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/xbar/s4_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -radix hexadecimal /magma_tb/magma/tile0/clk_i
add wave -noupdate -expand -group {tile0 (cpu)} -radix hexadecimal /magma_tb/magma/tile0/rst_i
add wave -noupdate -expand -group {tile0 (cpu)} -radix hexadecimal /magma_tb/magma/tile0/corenum
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /magma_tb/magma/tile0/hpi_mem_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /magma_tb/magma/tile0/hpi_mem_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /magma_tb/magma/tile0/hpi_mem_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /magma_tb/magma/tile0/hpi_mem_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /magma_tb/magma/tile0/hpi_mem_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /magma_tb/magma/tile0/hpi_mem_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /magma_tb/magma/tile0/hpi_mem_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group hpi -radix hexadecimal /magma_tb/magma/tile0/hpi_mem_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /magma_tb/magma/tile0/xbus_mem_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /magma_tb/magma/tile0/xbus_mem_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /magma_tb/magma/tile0/xbus_mem_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /magma_tb/magma/tile0/xbus_mem_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /magma_tb/magma/tile0/xbus_mem_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /magma_tb/magma/tile0/xbus_mem_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /magma_tb/magma/tile0/xbus_mem_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group xbus -radix hexadecimal /magma_tb/magma/tile0/xbus_mem_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /magma_tb/magma/tile0/cpu_instr_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /magma_tb/magma/tile0/cpu_instr_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /magma_tb/magma/tile0/cpu_instr_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /magma_tb/magma/tile0/cpu_instr_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /magma_tb/magma/tile0/cpu_instr_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /magma_tb/magma/tile0/cpu_instr_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /magma_tb/magma/tile0/cpu_instr_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu instr} -radix hexadecimal /magma_tb/magma/tile0/cpu_instr_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /magma_tb/magma/tile0/cpu_data_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /magma_tb/magma/tile0/cpu_data_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /magma_tb/magma/tile0/cpu_data_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /magma_tb/magma/tile0/cpu_data_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /magma_tb/magma/tile0/cpu_data_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /magma_tb/magma/tile0/cpu_data_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /magma_tb/magma/tile0/cpu_data_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu data} -radix hexadecimal /magma_tb/magma/tile0/cpu_data_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /magma_tb/magma/tile0/cpu_internal_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /magma_tb/magma/tile0/cpu_internal_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /magma_tb/magma/tile0/cpu_internal_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /magma_tb/magma/tile0/cpu_internal_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /magma_tb/magma/tile0/cpu_internal_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /magma_tb/magma/tile0/cpu_internal_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /magma_tb/magma/tile0/cpu_internal_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group {cpu internal} -radix hexadecimal /magma_tb/magma/tile0/cpu_internal_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /magma_tb/magma/tile0/internal_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /magma_tb/magma/tile0/internal_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /magma_tb/magma/tile0/internal_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /magma_tb/magma/tile0/internal_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /magma_tb/magma/tile0/internal_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /magma_tb/magma/tile0/internal_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /magma_tb/magma/tile0/internal_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group internal -radix hexadecimal /magma_tb/magma/tile0/internal_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /magma_tb/magma/tile0/dmem_data_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /magma_tb/magma/tile0/dmem_data_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /magma_tb/magma/tile0/dmem_data_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /magma_tb/magma/tile0/dmem_data_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /magma_tb/magma/tile0/dmem_data_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /magma_tb/magma/tile0/dmem_data_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /magma_tb/magma/tile0/dmem_data_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group dmem -radix hexadecimal /magma_tb/magma/tile0/dmem_data_rdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /magma_tb/magma/tile0/sfr_req
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /magma_tb/magma/tile0/sfr_we
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /magma_tb/magma/tile0/sfr_addr
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /magma_tb/magma/tile0/sfr_be
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /magma_tb/magma/tile0/sfr_wdata
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /magma_tb/magma/tile0/sfr_ack
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /magma_tb/magma/tile0/sfr_resp
add wave -noupdate -expand -group {tile0 (cpu)} -expand -group sfr -radix hexadecimal /magma_tb/magma/tile0/sfr_rdata
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
