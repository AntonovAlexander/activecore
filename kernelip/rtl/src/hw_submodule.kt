package rtl

import hwast.*

class hw_submodule(var inst_name : String, var src_module: module, var parent_module: module, val bb : Boolean, val include_needed : Boolean) {

    var Ports = mutableMapOf<String, hw_port>()
    var Connections = mutableMapOf<hw_port, hw_param>()

    init {
        for (port in src_module.Ports) {
            Ports.put(port.name, port)
        }
    }

    fun getPortByName(name : String) : hw_port {
        if (!Ports.containsKey(name)) ERROR("Port " + name + " is unknown for instance " + inst_name)
        return Ports[name]!!
    }

    fun connect(port : hw_port, src : hw_param) {
        if (!Ports.containsValue(port)) ERROR("Port " + port.name + " is unknown!")
        if (Connections.containsKey(port)) ERROR("port " + port.name + " for instance " + inst_name
                + "cannot be connected to src " + src.GetString() +
                " - it is already connected to src " + Connections[port]!!.GetString())
        Connections.put(port, src)
        if (src is hw_var) {
            if (port.port_dir == PORT_DIR.IN) src.read_done = true
            else if (port.port_dir == PORT_DIR.OUT) src.write_done = true
            else {
                src.read_done = true
                src.write_done = true
            }
        }
    }

    fun connect(port : hw_port, value : Int) {
        connect(port, hw_imm(value))
    }

    fun connect(port_name : String, src : hw_param) {
        var port = getPortByName(port_name)
        connect(port, src)
    }

    fun connect(port_name : String, value : Int) {
        connect(port_name, hw_imm(value))
    }
}

val DUMMY_SUBMODULE = hw_submodule("DUMMY", DUMMY_MODULE, DUMMY_MODULE, true, false)