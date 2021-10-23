package ariele

import hwast.*
import rtl.*
import pipex.*

data class slave_entry(val start_addr : String,
                       val addr_width : Int)

class addr_map() : ArrayList<slave_entry>()

class xbar(name         : String,
           num_masters  : Int,
           addr_width   : Int,
           be_width     : Int,
           req_vartype  : hw_type,
           resp_vartype : hw_type,
           map          : addr_map,
           master_rob_size : Int,
           slave_rob_size : Int,
           debug_lvl : DEBUG_LEVEL) : rtl.module(name) {

    var master_pipe_insts   = ArrayList<hw_submodule>()
    var slave_pipe_insts    = ArrayList<hw_submodule>()

    var busreq_struct = hw_struct(name + "_busreq_struct")

    data class reqfifo_channel_internal(val req : hw_var, val ack : hw_var, var wdata : hw_var)
    data class respfifo_channel_internal(val resp : hw_var, var rdata : hw_var)
    data class copipe_channel_internal(val req : reqfifo_channel_internal, val resp : respfifo_channel_internal)
    class m_channels_internal : ArrayList<copipe_channel_internal>()
    var internal_channels = ArrayList<m_channels_internal>()

    data class reqfifo_ports(val req : hw_var, val ack : hw_var, val we : hw_var, val addr : hw_var, val be : hw_var, var wdata : hw_var, var wdata_struct : hw_var)
    data class copipe_ports(val req : reqfifo_ports, val resp : respfifo_channel_internal)
    var master_ifs  = ArrayList<copipe_ports>()
    var slave_ifs   = ArrayList<copipe_ports>()

    init {
        // TODO: address overlap check

        // generating req structure
        busreq_struct.addu("addr",  (addr_width-1), 0, "0")
        busreq_struct.addu("be",    (be_width-1),  0, "0")
        busreq_struct.add("wdata",  req_vartype, "0")

        // generating clock/reset
        var clk = uinput("clk_i", 0, 0, "0")
        var rst = uinput("rst_i", 0, 0, "0")

        // generating master pipe module
        var master_name = "m_" + name
        var pipex_master_pipe = master_pipe(master_name, hw_type(busreq_struct), map, resp_vartype, master_rob_size)
        var cyclix_master_pipe = pipex_master_pipe.translate_to_cyclix(debug_lvl)
        var rtl_master_pipe = cyclix_master_pipe.export_to_rtl(debug_lvl)
        rtl_master_pipe.Include_filenames.add(name)

        // generating slave pipe module
        var slave_name = "s_" + name
        var pipex_slave_pipe = slave_pipe(slave_name, num_masters, hw_type(busreq_struct), resp_vartype, slave_rob_size)
        var cyclix_slave_pipe = pipex_slave_pipe.translate_to_cyclix(debug_lvl)
        var rtl_slave_pipe = cyclix_slave_pipe.export_to_rtl(debug_lvl)
        rtl_slave_pipe.Include_filenames.add(name)

        // generating master ports
        for (num_master in 0 until num_masters) {

            var req_ch = reqfifo_ports(
                uinput("m" + num_master + "_req_i", 0, 0, "0"),
                uoutput("m" + num_master + "_ack_o", 0, 0, "0"),
                uinput("m" + num_master + "_we_i", 0, 0, "0"),
                uinput("m" + num_master + "_addr_bi", (addr_width-1), 0, "0"),
                uinput("m" + num_master + "_be_i", (be_width-1), 0, "0"),
                input("m" + num_master + "_wdata_bi", req_vartype, "0"),
                comb("m" + num_master + "_wdata", rtl_master_pipe.getPortByName("genscopipe_master_req_genfifo_rdata_bi").vartype, "0")
            )

            var resp_ch = respfifo_channel_internal(
                uoutput("m" + num_master + "_resp_o", 0, 0, "0"),
                output("m" + num_master + "_rdata_bo", resp_vartype, "0")
            )

            master_ifs.add(copipe_ports(req_ch, resp_ch))
        }

        // generating slave ports
        for (num_slave in 0 until map.size) {

            var req_ch = reqfifo_ports(
                uoutput("s" + num_slave + "_req_o", 0, 0, "0"),
                uinput("s" + num_slave + "_ack_i", 0, 0, "1"),
                uoutput("s" + num_slave + "_we_o", 0, 0, "0"),
                uoutput("s" + num_slave + "_addr_bo", (addr_width-1), 0, "0"),
                uoutput("s" + num_slave + "_be_o", (be_width-1), 0, "0"),
                output("s" + num_slave + "_wdata_bo", req_vartype, "0"),
                comb("s" + num_slave + "_wdata", rtl_slave_pipe.getPortByName("genmcopipe_slave_req_genfifo_wdata_bo").vartype, "0")
            )

            var resp_ch = respfifo_channel_internal(
                uinput("s" + num_slave + "_resp_i", 0, 0, "1"),
                input("s" + num_slave + "_rdata_bi", resp_vartype, "0")
            )

            slave_ifs.add(copipe_ports(req_ch, resp_ch))
        }

        // generating internal signals
        for (num_master in 0 until num_masters) {

            var m_channels = m_channels_internal()
            for (num_slave in 0 until map.size) {
                var fifo_mreq_channel = reqfifo_channel_internal(
                    ucomb("m" + num_master + "s" + num_slave + "_" + name + "_req", 0, 0, "0"),
                    ucomb("m" + num_master + "s" + num_slave + "_" + name + "_ack", 0, 0, "0"),
                    comb("m" + num_master + "s" + num_slave + "_" + name + "_wdata", rtl_master_pipe.getPortByName("genmcopipe_slave" + num_slave + "_req_genfifo_wdata_bo").vartype, "0")
                )
                var fifo_sreq_channel = respfifo_channel_internal(
                    ucomb("s" + num_slave + "m" + num_master + "_" + name + "_req", 0, 0, "0"),
                    comb("s" + num_slave + "m" + num_master + "_" + name + "_rdata", resp_vartype, "0")
                )
                var s_channel = copipe_channel_internal(fifo_mreq_channel, fifo_sreq_channel)
                m_channels.add(s_channel)
            }
            internal_channels.add(m_channels)
        }

        /*
        for (if_struct in rtl_master_pipe.hw_if_structs) {
            add_if_struct(if_struct)
        }
        rtl_master_pipe.add_if_struct(busreq_struct)
        */

        // instantiating and connecting masters
        for (num_master in 0 until num_masters) {
            var new_inst = submodule("m" + num_master + "_" + name + "_inst", rtl_master_pipe)
            master_pipe_insts.add(new_inst)
            new_inst.connect("clk_i", clk)
            new_inst.connect("rst_i", rst)

            new_inst.connect("genscopipe_master_req_genfifo_req_i", master_ifs[num_master].req.req)
            new_inst.connect("genscopipe_master_req_genfifo_rdata_bi", master_ifs[num_master].req.wdata_struct)
            new_inst.connect("genscopipe_master_req_genfifo_ack_o", master_ifs[num_master].req.ack)

            new_inst.connect("genscopipe_master_resp_genfifo_req_o", master_ifs[num_master].resp.resp)
            new_inst.connect("genscopipe_master_resp_genfifo_ack_i", 1)
            new_inst.connect("genscopipe_master_resp_genfifo_wdata_bo", master_ifs[num_master].resp.rdata)

            for (num_slave in 0 until map.size) {
                new_inst.connect("genmcopipe_slave" + num_slave + "_req_genfifo_req_o", internal_channels[num_master][num_slave].req.req)
                new_inst.connect("genmcopipe_slave" + num_slave + "_req_genfifo_ack_i", internal_channels[num_master][num_slave].req.ack)
                new_inst.connect("genmcopipe_slave" + num_slave + "_req_genfifo_wdata_bo", internal_channels[num_master][num_slave].req.wdata)

                new_inst.connect("genmcopipe_slave" + num_slave + "_resp_genfifo_req_i", internal_channels[num_master][num_slave].resp.resp)
                new_inst.connect("genmcopipe_slave" + num_slave + "_resp_genfifo_rdata_bi", internal_channels[num_master][num_slave].resp.rdata)
            }

            cproc_begin()
            run {
                var we_fraction = hw_fracs("we")

                var addr_fraction = hw_fracs("wdata")
                addr_fraction.add(hw_frac_SubStruct("addr"))

                var be_fraction = hw_fracs("wdata")
                be_fraction.add(hw_frac_SubStruct("be"))

                var wdata_fraction = hw_fracs("wdata")
                wdata_fraction.add(hw_frac_SubStruct("wdata"))

                assign(master_ifs[num_master].req.wdata_struct.GetFracRef(we_fraction),     master_ifs[num_master].req.we)
                assign(master_ifs[num_master].req.wdata_struct.GetFracRef(addr_fraction),   master_ifs[num_master].req.addr)
                assign(master_ifs[num_master].req.wdata_struct.GetFracRef(be_fraction),     master_ifs[num_master].req.be)
                assign(master_ifs[num_master].req.wdata_struct.GetFracRef(wdata_fraction),  master_ifs[num_master].req.wdata)
            }; cproc_end()
        }

        /*
        for (if_struct in rtl_slave_pipe.hw_if_structs) {
            add_if_struct(if_struct)
        }
        rtl_slave_pipe.add_if_struct(busreq_struct)
        */

        for (num_slave in 0 until map.size) {
            var new_inst = submodule("s" + num_slave + "_" + name + "_inst", rtl_slave_pipe)
            slave_pipe_insts.add(new_inst)
            new_inst.connect("clk_i", clk)
            new_inst.connect("rst_i", rst)

            new_inst.connect("genmcopipe_slave_req_genfifo_req_o", slave_ifs[num_slave].req.req)
            new_inst.connect("genmcopipe_slave_req_genfifo_wdata_bo", slave_ifs[num_slave].req.wdata_struct)
            new_inst.connect("genmcopipe_slave_req_genfifo_ack_i", slave_ifs[num_slave].req.ack)

            new_inst.connect("genmcopipe_slave_resp_genfifo_req_i", slave_ifs[num_slave].resp.resp)
            new_inst.connect("genmcopipe_slave_resp_genfifo_rdata_bi", slave_ifs[num_slave].resp.rdata)

            for (num_master in 0 until num_masters) {
                new_inst.connect("genscopipe_master" + num_master + "_req_genfifo_req_i", internal_channels[num_master][num_slave].req.req)
                new_inst.connect("genscopipe_master" + num_master + "_req_genfifo_ack_o", internal_channels[num_master][num_slave].req.ack)
                new_inst.connect("genscopipe_master" + num_master + "_req_genfifo_rdata_bi", internal_channels[num_master][num_slave].req.wdata)

                new_inst.connect("genscopipe_master" + num_master + "_resp_genfifo_req_o", internal_channels[num_master][num_slave].resp.resp)
                new_inst.connect("genscopipe_master" + num_master + "_resp_genfifo_ack_i", 1)
                new_inst.connect("genscopipe_master" + num_master + "_resp_genfifo_wdata_bo", internal_channels[num_master][num_slave].resp.rdata)
            }

            cproc_begin()
            run {
                subStruct_gen(slave_ifs[num_slave].req.we, slave_ifs[num_slave].req.wdata_struct, "we")

                var wdata_substr = subStruct(slave_ifs[num_slave].req.wdata_struct, "wdata")
                subStruct_gen(slave_ifs[num_slave].req.addr,    wdata_substr, "addr")
                subStruct_gen(slave_ifs[num_slave].req.be,      wdata_substr, "be")
                subStruct_gen(slave_ifs[num_slave].req.wdata,   wdata_substr, "wdata")
            }; cproc_end()
        }
    }
}