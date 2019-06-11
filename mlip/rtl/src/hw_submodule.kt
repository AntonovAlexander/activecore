package rtl

import hwast.*

class hw_submodule(inst_name_in : String, src_module_in: module) {

    var inst_name = inst_name_in
    var src_module = src_module_in
    var Ports = ArrayList<hw_port>()

    init {
        for (port in src_module_in.Ports) {
            Ports.add(port)
        }
    }
}
