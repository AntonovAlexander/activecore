create_pblock pblock_tile0
add_cells_to_pblock [get_pblocks pblock_tile0] [get_cells -quiet [list magma/tile0]]
resize_pblock [get_pblocks pblock_tile0] -add {SLICE_X52Y125:SLICE_X87Y149}
resize_pblock [get_pblocks pblock_tile0] -add {DSP48_X1Y50:DSP48_X2Y59}
resize_pblock [get_pblocks pblock_tile0] -add {RAMB18_X1Y50:RAMB18_X3Y59}
resize_pblock [get_pblocks pblock_tile0] -add {RAMB36_X1Y25:RAMB36_X3Y29}
create_pblock pblock_tile1
add_cells_to_pblock [get_pblocks pblock_tile1] [get_cells -quiet [list magma/tile1]]
resize_pblock [get_pblocks pblock_tile1] -add {SLICE_X52Y101:SLICE_X87Y124}
resize_pblock [get_pblocks pblock_tile1] -add {DSP48_X1Y42:DSP48_X2Y49}
resize_pblock [get_pblocks pblock_tile1] -add {RAMB18_X1Y42:RAMB18_X3Y49}
resize_pblock [get_pblocks pblock_tile1] -add {RAMB36_X1Y21:RAMB36_X3Y24}
create_pblock pblock_tile2
add_cells_to_pblock [get_pblocks pblock_tile2] [get_cells -quiet [list magma/tile2]]
resize_pblock [get_pblocks pblock_tile2] -add {SLICE_X52Y75:SLICE_X87Y99}
resize_pblock [get_pblocks pblock_tile2] -add {DSP48_X1Y30:DSP48_X2Y39}
resize_pblock [get_pblocks pblock_tile2] -add {RAMB18_X1Y30:RAMB18_X3Y39}
resize_pblock [get_pblocks pblock_tile2] -add {RAMB36_X1Y15:RAMB36_X3Y19}
create_pblock pblock_tile3
add_cells_to_pblock [get_pblocks pblock_tile3] [get_cells -quiet [list magma/tile3]]
resize_pblock [get_pblocks pblock_tile3] -add {SLICE_X52Y51:SLICE_X87Y74}
resize_pblock [get_pblocks pblock_tile3] -add {DSP48_X1Y22:DSP48_X2Y29}
resize_pblock [get_pblocks pblock_tile3] -add {RAMB18_X1Y22:RAMB18_X3Y29}
resize_pblock [get_pblocks pblock_tile3] -add {RAMB36_X1Y11:RAMB36_X3Y14}
create_pblock pblock_xbar
add_cells_to_pblock [get_pblocks pblock_xbar] [get_cells -quiet [list magma/xbar]]
resize_pblock [get_pblocks pblock_xbar] -add {SLICE_X2Y51:SLICE_X51Y149}
resize_pblock [get_pblocks pblock_xbar] -add {DSP48_X0Y22:DSP48_X0Y59}
resize_pblock [get_pblocks pblock_xbar] -add {RAMB18_X0Y22:RAMB18_X0Y59}
resize_pblock [get_pblocks pblock_xbar] -add {RAMB36_X0Y11:RAMB36_X0Y29}
