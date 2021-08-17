/*
 * hw_subproc.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

import hwast.*

class hw_subproc(var inst_name : String, var src_module: Generic, var parent_module: Generic) {

    var RootResetDriver = parent_module.ulocal("gensubmod_" + inst_name + "_genrst", 0, 0, "0")
    var AppResetDrivers = ArrayList<hw_var>()

    var Ports = mutableMapOf<String, hw_port>()
    var PortConnections = mutableMapOf<hw_port, hw_param>()

    var fifo_ifs = mutableMapOf<String, hw_structvar>()
    var FifoConnections = mutableMapOf<hw_structvar, hw_param>()

    init {
        for (port in src_module.Ports) {
            Ports.put(port.name, port)
        }
        for (fifo in src_module.fifo_ifs) {
            fifo_ifs.put(fifo.value.name, fifo.value)
        }
    }

    fun AddResetDriver() : hw_var {
        var rst_var = parent_module.ulocal(parent_module.GetGenName("genrst"), 0, 0, "0")
        AppResetDrivers.add(rst_var)
        return rst_var
    }

    fun AddResetDriver(rst_var : hw_var) : hw_var {
        AppResetDrivers.add(rst_var)
        return rst_var
    }

    fun getPortByName(name : String) : hw_port {
        if (!Ports.containsKey(name)) ERROR("Port " + name + " is unknown for instance " + inst_name)
        return Ports[name]!!
    }

    fun getFifoByName(name : String) : hw_structvar {
        return fifo_ifs[name]!!
    }

    fun connectPort(port : hw_port, src : hw_param) {
        if (!Ports.containsValue(port)) ERROR("Port " + port.name + " is unknown!")
        if (PortConnections.containsKey(port)) ERROR("port " + port.name + " for instance " + inst_name
                + "cannot be connected to src " + src.GetString() +
                " - it is already connected to src " + PortConnections[port]!!.GetString())
        PortConnections.put(port, src)
        if (src is hw_var) {
            if (port.port_dir == PORT_DIR.IN) src.read_done = true
            else if (port.port_dir == PORT_DIR.OUT) src.write_done = true
            else {
                src.read_done = true
                src.write_done = true
            }
        }
    }

    fun connectPort(port : hw_port, value : Int) {
        connectPort(port, hw_imm(value))
    }

    fun connectPort(port_name : String, src : hw_param) {
        var port = getPortByName(port_name)
        connectPort(port, src)
    }

    fun connectPort(port_name : String, value : Int) {
        connectPort(port_name, hw_imm(value))
    }

    fun connectPortGen(port_name : String) : hw_var {
        var part_var = parent_module.local(parent_module.GetGenName(port_name), getPortByName(port_name).vartype, getPortByName(port_name).defval)
        connectPort(port_name, part_var)
        return part_var
    }

    /*
    fun connectFifo(fifo : hw_structvar, src : hw_param) {
        if (!fifo_ifs.containsValue(fifo)) ERROR("Port " + fifo.name + " is unknown!")
        if (FifoConnections.containsKey(fifo)) ERROR("port " + fifo.name + " for instance " + inst_name
                + "cannot be connected to src " + src.GetString() +
                " - it is already connected to src " + FifoConnections[fifo]!!.GetString())
        FifoConnections.put(fifo, src)
        if (src is hw_var) {
            if (fifo is hw_fifo_in) src.read_done = true
            else if (fifo is hw_fifo_out) src.write_done = true
            else {
                src.read_done = true
                src.write_done = true
            }
        }
    }

    fun connectFifo(fifo_name : String, src : hw_param) {
        var fifo = getFifoByName(fifo_name)
        connectFifo(fifo, src)
    }

    fun connectFifoGen(fifo_name : String) : hw_var {
        var part_var = parent_module.local(parent_module.GetGenName(fifo_name), getFifoByName(fifo_name).vartype, getFifoByName(fifo_name).defval)
        connectFifo(fifo_name, part_var)
        return part_var
    }
    */

    fun fifo_internal_wr_unblk(fifo_name : String, wdata : hw_param) : hw_var {
        return parent_module.fifo_internal_wr_unblk(this, fifo_name, wdata)
    }

    fun fifo_internal_rd_unblk(fifo_name : String, rdata : hw_var) : hw_var {
        return parent_module.fifo_internal_rd_unblk(this, fifo_name, rdata)
    }

    fun fifo_internal_wr_blk(fifo_name : String, wdata : hw_param) {
        parent_module.fifo_internal_wr_blk(this, fifo_name, wdata)
    }

    fun fifo_internal_rd_blk(fifo_name : String) : hw_var {
        return parent_module.fifo_internal_rd_blk(this, fifo_name)
    }
}