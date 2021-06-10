import hwast.*

fun main() {
    println("taylor credit: generating...")

    var taylor_credit = taylor_credit_pipeline()

    var taylor_credit_cyclix = taylor_credit.translate_to_cyclix(true)
    var taylor_credit_rtl = taylor_credit_cyclix.export_to_rtl(true)

    var dirname = "coregen/"
    taylor_credit_rtl.export_to_sv(dirname + "sverilog", true)
    taylor_credit_cyclix.export_to_vivado_cpp(dirname + "vivado_cpp", true)

    println("taylor credit: done")
}