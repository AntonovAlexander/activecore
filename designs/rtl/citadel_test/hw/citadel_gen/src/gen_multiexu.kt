import citadel.*

fun main(args: Array<String>) {
    println("Citadel: generating experimental core")

    var multiexu = citadel.test_multiexu("citadel_gen")

    var multiexu_cyclix = multiexu.translate_to_cyclix(true)
    var multiexu_rtl = multiexu_cyclix.export_to_rtl()

    var dirname = "coregen/"
    multiexu_rtl.export_to_sv(dirname + "sverilog")
    multiexu_cyclix.export_to_vivado_cpp(dirname + "vivado_cpp")
}