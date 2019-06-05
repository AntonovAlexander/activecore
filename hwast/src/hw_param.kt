package hwast

enum class PARAM_TYPE {
    VAR, VAL
}

open class hw_param (val type : PARAM_TYPE, val dimensions : hw_dim_static, var token_printable : String) {

    fun GetString(): String {
        return token_printable
    }

    fun GetDimensions(): hw_dim_static {
        return dimensions
    }

    fun isDimSingle(): Boolean {
        return dimensions.isSingle()
    }
}