fun main(args: Array<String>) {
    println("RISC-V: generating all cores")

    for (i in 1..6) {
        var cpu = riscv_pipex.cpu(("riscv_" + i +"stage"), i, 0x200)

        var cpu_cyclix = cpu.translate_to_cyclix(false)
        var cpu_rtl = cpu_cyclix.export_to_rtl()

        var dirname = "coregen/riscv_" + i +"stage" + "/"
        cpu_rtl.export_to_sv(dirname + "sverilog")
        cpu_cyclix.export_to_vivado_cpp(dirname + "vivado_cpp")
    }
}