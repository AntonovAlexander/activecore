fun main(args: Array<String>) {
    println("RISC-V: generating all cores")

    for (i in 1..6) {
        var cpu = riscv_pipex.cpu(("riscv_" + i +"stage"), i, 0x200)
        var cpu_cyclix = cpu.translate_to_cyclix(true)
        var cpu_rtl = cpu_cyclix.export_rtl()
        cpu_rtl.export_sv("coregen/riscv_" + i +"stage" + "/sverilog")
    }
}