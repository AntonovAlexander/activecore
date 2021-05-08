import agenda.*

fun main(args: Array<String>) {
    println("Agenda: generating CPU")

    var multiexu = agenda.cpu("agenda_cpu")

    var multiexu_cyclix = multiexu.translate_to_cyclix(true)
    var multiexu_rtl = multiexu_cyclix.export_to_rtl(true)

    var dirname = "coregen/"
    multiexu_rtl.export_to_sv(dirname + "sverilog", true)
    multiexu_cyclix.export_to_vivado_cpp(dirname + "vivado_cpp", true)
}