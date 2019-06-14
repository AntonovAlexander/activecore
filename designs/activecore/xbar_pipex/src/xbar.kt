package xbar_pipex

import hwast.*
import rtl.*
import pipex.*

data class slave_entry(val start_addr : Int,
                       val addr_width : Int)

class addr_map() : ArrayList<slave_entry>()

class xbar(name         : String,
           num_masters  : Int,
           addr_width   : Int,
           be_width     : Int,
           req_vartype  : hw_type,
           resp_vartype : hw_type,
           map          : addr_map) : rtl.module(name) {

    var master_pipe_insts   = ArrayList<hw_submodule>()
    var slave_pipe_insts    = ArrayList<hw_submodule>()

    var busreq_struct = add_if_struct(name + "_busreq_struct")

    data class fifo_channel_internal(val req : hw_var, val ack : hw_var, var wdata : hw_var)
    data class copipe_channel_internal(val req : fifo_channel_internal, val resp : fifo_channel_internal)
    var master_inst_signals = ArrayList<copipe_channel_internal>()

    init {
        // TODO: address overlap check

        // generating req structure
        busreq_struct.addu("addr",  (addr_width-1), 0, "0")
        busreq_struct.addu("be",    (be_width-1),  0, "0")
        busreq_struct.add("wdata",  req_vartype, "0")

        // generating clock/reset
        var clk = uinput("clk_i", 0, 0, "0")
        var rst = uinput("rst_i", 0, 0, "0")

        for (num_master in 0 until num_masters) {
            var fifo_mreq_channel = fifo_channel_internal(
                ucomb("m" + num_master + "_" + name + "_req", 0, 0, "0"),
                ucomb("m" + num_master + "_" + name + "_ack", 0, 0, "0"),
                 comb("m" + num_master + "_" + name + "_wdata", hw_type(busreq_struct), "0")
            )
            var fifo_sreq_channel = fifo_channel_internal(
                ucomb("s" + num_master + "_" + name + "_req", 0, 0, "0"),
                ucomb("s" + num_master + "_" + name + "_ack", 0, 0, "0"),
                 comb("s" + num_master + "_" + name + "_wdata", resp_vartype, "0")
            )
            var copipe_channel = copipe_channel_internal(fifo_mreq_channel, fifo_sreq_channel)
            master_inst_signals.add(copipe_channel)
        }

        // generating master pipe instances
        var master_name = "m_" + name
        var pipex_master_pipe = master_pipe(master_name, hw_type(busreq_struct), map, resp_vartype)
        var cyclix_master_pipe = pipex_master_pipe.translate_to_cyclix(true)
        var rtl_master_pipe = cyclix_master_pipe.export_to_rtl()
        for (num_master in 0 until num_masters) {
            var new_inst = submodule("m" + num_master + "_" + name + "_inst", rtl_master_pipe)
            master_pipe_insts.add(new_inst)
            new_inst.connect("clk_i", clk)
            new_inst.connect("rst_i", rst)
        }

        // generating slave pipe instances
        for (num_slave in 0 until map.size) {
            var slave_name = "s" + num_slave + "_" + name
            var pipex_slave_pipe = slave_pipe(slave_name, num_masters, hw_type(busreq_struct), resp_vartype)
            var cyclix_slave_pipe = pipex_slave_pipe.translate_to_cyclix(true)
            var rtl_slave_pipe = cyclix_slave_pipe.export_to_rtl()
            slave_pipe_insts.add(submodule(slave_name + "_inst", rtl_slave_pipe))
        }
        // TODO: connecting submodules
    }
}