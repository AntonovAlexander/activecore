import agenda.*
import hwast.DEBUG_LEVEL

fun main(args: Array<String>) {
    println("Agenda: generating CPU")

    var multiexu = agenda.cpu("agenda_cpu")

    var multiexu_cyclix = multiexu.translate_to_cyclix(DEBUG_LEVEL.FULL)
    var multiexu_rtl = multiexu_cyclix.export_to_rtl(DEBUG_LEVEL.FULL)

    var dirname = "coregen/"
    multiexu_rtl.export_to_sv(dirname + "sverilog", DEBUG_LEVEL.FULL)
    multiexu_cyclix.export_to_vivado_cpp(dirname + "vivado_cpp", DEBUG_LEVEL.FULL)
}