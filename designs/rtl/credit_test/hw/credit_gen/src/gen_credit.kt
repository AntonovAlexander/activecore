import hwast.*
import ariele.*

fun main() {
    println("taylor credit: generating...")

    var taylor_credit = taylor_credit_pipeline()

    var taylor_credit_cyclix = taylor_credit.translate_to_cyclix(true)
    var taylor_credit_rtl = taylor_credit_cyclix.export_to_rtl()

    var dirname = "coregen/"
    taylor_credit_rtl.export_to_sv(dirname + "sverilog")
    taylor_credit_cyclix.export_to_vivado_cpp(dirname + "vivado_cpp")

    println("taylor credit: done")
}