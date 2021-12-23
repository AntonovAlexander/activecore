import hwast.*

fun main() {
    println("Taylor pipeline: generating...")

    var taylor_credit = taylor_pipeline()

    var taylor_credit_cyclix = taylor_credit.translate_to_cyclix(DEBUG_LEVEL.FULL)
    var taylor_credit_rtl = taylor_credit_cyclix.export_to_rtl(DEBUG_LEVEL.FULL)

    var dirname = "coregen/"
    taylor_credit_rtl.export_to_sv(dirname + "sverilog", DEBUG_LEVEL.FULL)
    taylor_credit_cyclix.export_to_vivado_cpp(dirname + "vivado_cpp", DEBUG_LEVEL.FULL)

    println("Taylor pipeline: done")
}