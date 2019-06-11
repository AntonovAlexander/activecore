package xbar_pipex

import hwast.*
import rtl.*
import pipex.*

class xbar(name_in          : String,
           num_masters_in   : Int,
           req_vartype_in   : hw_type,
           map_in           : addr_map,
           resp_vartype_in  : hw_type) : rtl.module(name_in) {

    var num_masters = num_masters_in
    var map         = map_in

    var master_pipes    = ArrayList<master_pipe>()
    var slave_pipes     = ArrayList<slave_pipe>()

    init {
        for (num_master in 0 until num_masters_in) {
            var master_name = "m" + num_master + name_in
            var pipex_master_pipe = master_pipe(master_name, 4, req_vartype_in, map_in, resp_vartype_in)
            var cyclix_master_pipe = pipex_master_pipe.translate_to_cyclix(true)
            var rtl_master_pipe = cyclix_master_pipe.export_to_rtl()
            Submodules.add(hw_submodule(master_name, rtl_master_pipe))
        }
        for (num_slave in 0 until map.size) {
            var slave_name = "s" + num_slave + name_in
            var pipex_slave_pipe = slave_pipe(slave_name, req_vartype_in, resp_vartype_in, map[num_slave].start_addr, map[num_slave].addr_width)
            var cyclix_slave_pipe = pipex_slave_pipe.translate_to_cyclix(true)
            var rtl_slave_pipe = cyclix_slave_pipe.export_to_rtl()
            Submodules.add(hw_submodule(slave_name, rtl_slave_pipe))
        }
        // TODO: connecting submodules
    }
}