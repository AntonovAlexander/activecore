package rtl

fun ERROR(err_string : String) {
    throw Exception("ActiveCore (rtl) ERROR: " + err_string)
}