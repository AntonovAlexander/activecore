import hwast.DEBUG_LEVEL

fun main(args: Array<String>) {
    println("RISC-V: generating all cores")

    for (i in 1..6) {
        var cpu = aquaris.cpu(("riscv_" + i +"stage"), i, 0x200, 0x80, 8, true, true)

        var cpu_cyclix = cpu.translate_to_cyclix(DEBUG_LEVEL.SILENT)
        var cpu_rtl = cpu_cyclix.export_to_rtl(DEBUG_LEVEL.SILENT)

        var dirname = "coregen/riscv_" + i +"stage" + "/"
        cpu_rtl.export_to_sv(dirname + "sverilog", DEBUG_LEVEL.SILENT)
        cpu_cyclix.export_to_vivado_cpp(dirname + "vivado_cpp", DEBUG_LEVEL.SILENT)
    }
}