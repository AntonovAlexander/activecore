package xbar_pipex

import hwast.*

data class slave_entry(val start_addr : Int,
                       val addr_width : Int)

class addr_map() : ArrayList<slave_entry>()
