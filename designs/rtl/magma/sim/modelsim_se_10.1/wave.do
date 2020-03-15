onerror {resume}
quietly WaveActivateNextPane {} 0
add wave -noupdate -radix hexadecimal /magma_tb/magma/clk_i
add wave -noupdate -radix hexadecimal /magma_tb/magma/arst_i
add wave -noupdate -radix hexadecimal /magma_tb/magma/rx_i
add wave -noupdate -radix hexadecimal /magma_tb/magma/tx_o
add wave -noupdate -radix hexadecimal /magma_tb/magma/gpio_bi
add wave -noupdate -radix hexadecimal /magma_tb/magma/gpio_bo
add wave -noupdate -radix hexadecimal /magma_tb/magma/gpio/led_register
add wave -noupdate -expand -group {xbar m0 (tile 0 xbus)} -radix hexadecimal /magma_tb/magma/m0/req
add wave -noupdate -expand -group {xbar m0 (tile 0 xbus)} -radix hexadecimal /magma_tb/magma/m0/ack
add wave -noupdate -expand -group {xbar m0 (tile 0 xbus)} -radix hexadecimal /magma_tb/magma/m0/addr
add wave -noupdate -expand -group {xbar m0 (tile 0 xbus)} -radix hexadecimal /magma_tb/magma/m0/we
add wave -noupdate -expand -group {xbar m0 (tile 0 xbus)} -radix hexadecimal /magma_tb/magma/m0/be
add wave -noupdate -expand -group {xbar m0 (tile 0 xbus)} -radix hexadecimal /magma_tb/magma/m0/wdata
add wave -noupdate -expand -group {xbar m0 (tile 0 xbus)} -radix hexadecimal /magma_tb/magma/m0/resp
add wave -noupdate -expand -group {xbar m0 (tile 0 xbus)} -radix hexadecimal /magma_tb/magma/m0/rdata
add wave -noupdate -expand -group {xbar s0 (tile 0 hpi)} -radix hexadecimal /magma_tb/magma/s0/req
add wave -noupdate -expand -group {xbar s0 (tile 0 hpi)} -radix hexadecimal /magma_tb/magma/s0/ack
add wave -noupdate -expand -group {xbar s0 (tile 0 hpi)} -radix hexadecimal /magma_tb/magma/s0/addr
add wave -noupdate -expand -group {xbar s0 (tile 0 hpi)} -radix hexadecimal /magma_tb/magma/s0/we
add wave -noupdate -expand -group {xbar s0 (tile 0 hpi)} -radix hexadecimal /magma_tb/magma/s0/be
add wave -noupdate -expand -group {xbar s0 (tile 0 hpi)} -radix hexadecimal /magma_tb/magma/s0/wdata
add wave -noupdate -expand -group {xbar s0 (tile 0 hpi)} -radix hexadecimal /magma_tb/magma/s0/resp
add wave -noupdate -expand -group {xbar s0 (tile 0 hpi)} -radix hexadecimal /magma_tb/magma/s0/rdata
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/m4/req
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/m4/ack
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/m4/addr
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/m4/we
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/m4/be
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/m4/wdata
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/m4/resp
add wave -noupdate -expand -group {xbar m4 (udm)} -radix hexadecimal /magma_tb/magma/m4/rdata
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/s4/req
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/s4/ack
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/s4/addr
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/s4/we
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/s4/be
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/s4/wdata
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/s4/resp
add wave -noupdate -expand -group {xbar s4 (gpio)} -radix hexadecimal /magma_tb/magma/s4/rdata
add wave -noupdate -expand -group {tile0 (cpu)} -radix hexadecimal /magma_tb/magma/tile0/clk_i
add wave -noupdate -expand -group {tile0 (cpu)} -radix hexadecimal /magma_tb/magma/tile0/rst_i
add wave -noupdate -expand -group {tile0 (cpu)} -radix hexadecimal /magma_tb/magma/tile0/corenum
TreeUpdate [SetDefaultTree]
WaveRestoreCursors {{Cursor 1} {4280405 ps} 0}
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
WaveRestoreZoom {0 ps} {10500 ns}
