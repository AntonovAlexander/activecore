package rtl

import hwast.*

class hw_submodule(inst_name_in : String, src_module_in: module, parent_module_in: module) {

    var inst_name = inst_name_in
    var src_module = src_module_in
    var parent_module = parent_module_in
    var Ports = mutableMapOf<String, hw_port>()
    var Connections = mutableMapOf<hw_port, hw_var>()

    init {
        for (port in src_module_in.Ports) {
            Ports.put(port.name, port)
        }
    }

    fun getPortByName(name : String) : hw_port {
        if (!Ports.containsKey(name)) ERROR("Port " + name + " is unknown for instance " + inst_name)
        return Ports[name]!!
    }

    fun connect(port : hw_port, signal : hw_var) {
        if (!Ports.containsValue(port)) ERROR("Port " + port.name + " is unknown!")
        if (Connections.containsKey(port)) ERROR("port " + port.name + " for instance " + inst_name
                + "cannot be connected to signal " + signal.name +
                " - it is already connected to signal " + Connections[port]!!.name)
        Connections.put(port, signal)
    }

    fun connect(port_name : String, signal : hw_var) {
        var port = getPortByName(port_name)
        if (Connections.containsKey(port)) ERROR("port " + port.name + " for instance " + inst_name
                + "cannot be connected to signal " + signal.name +
                " - it is already connected to signal " + Connections[port]!!.name)
        Connections.put(port, signal)
    }
}
