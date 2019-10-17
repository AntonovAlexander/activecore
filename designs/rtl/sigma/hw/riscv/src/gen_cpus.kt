fun main(args: Array<String>) {
    println("RISC-V: generating all cores")

    for (i in 1..6) {
        var cpu = aquaris.cpu(("riscv_" + i +"stage"), i, 0x200, 0x80, 8)

        var cpu_cyclix = cpu.translate_to_cyclix(true)
        var cpu_rtl = cpu_cyclix.export_to_rtl()

        var dirname = "coregen/riscv_" + i +"stage" + "/"
        cpu_rtl.export_to_sv(dirname + "sverilog")
        cpu_cyclix.export_to_vivado_cpp(dirname + "vivado_cpp")
    }
}