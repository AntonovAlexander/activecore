package xbar_pipex

import hwast.*
import rtl.*
import pipex.*

data class slave_entry(val start_addr : Int,
                       val addr_width : Int)

class addr_map() : ArrayList<slave_entry>()

class xbar(name_in          : String,
           num_masters_in   : Int,
           req_vartype_in   : hw_type,
           map_in           : addr_map,
           resp_vartype_in  : hw_type) : rtl.module(name_in) {

    var num_masters = num_masters_in
    var map         = map_in

    var master_pipe_insts   = ArrayList<hw_submodule>()
    var slave_pipe_insts    = ArrayList<hw_submodule>()

    init {
        // TODO: address overlap check
        var master_name = "m_" + name_in
        var pipex_master_pipe = master_pipe(master_name, 32, 4, req_vartype_in, map_in, resp_vartype_in)
        var cyclix_master_pipe = pipex_master_pipe.translate_to_cyclix(true)
        var rtl_master_pipe = cyclix_master_pipe.export_to_rtl()
        for (num_master in 0 until num_masters_in) {
            master_pipe_insts.add(submodule("m" + num_master + "_" + name_in + "_inst", rtl_master_pipe))
        }
        for (num_slave in 0 until map.size) {
            var slave_name = "s" + num_slave + "_" + name_in
            var pipex_slave_pipe = slave_pipe(slave_name, num_masters_in, map[num_slave].addr_width, 4, req_vartype_in, resp_vartype_in)
            var cyclix_slave_pipe = pipex_slave_pipe.translate_to_cyclix(true)
            var rtl_slave_pipe = cyclix_slave_pipe.export_to_rtl()
            slave_pipe_insts.add(submodule(slave_name + "_inst", rtl_slave_pipe))
        }
        // TODO: connecting submodules
    }
}