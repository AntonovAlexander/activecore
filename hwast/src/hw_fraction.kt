/*
 * hw_fraction.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

enum class FRAC_TYPE {
    C, V, CC, CV, VC, VV, SubStruct
}

abstract class hw_fraction (val type : FRAC_TYPE) {
    abstract fun GetWidth() : Int
}

class hw_fraction_C (val index_in : hw_imm) : hw_fraction(FRAC_TYPE.C) {

    val index = index_in

    constructor(index : Int) : this(hw_imm(index.toString()))

    override fun GetWidth() : Int {
        return 1
    }
}

class hw_fraction_V (val index_in : hw_var) : hw_fraction(FRAC_TYPE.V) {

    val index = index_in

    override fun GetWidth() : Int {
        return 1
    }
}

class hw_fraction_CC (val msb_in : hw_imm, val lsb_in : hw_imm) : hw_fraction(FRAC_TYPE.CC) {

    val msb = msb_in
    val lsb = lsb_in

    constructor(msb : Int, lsb : Int) : this(hw_imm(msb.toString()), hw_imm(lsb.toString()))

    override fun GetWidth() : Int {
        if (msb.toInt() > lsb.toInt()) return (msb.toInt() - lsb.toInt())
        else return (lsb.toInt() - msb.toInt())
    }
}

class hw_fraction_CV (val msb_in : hw_imm, val lsb_in : hw_var) : hw_fraction(FRAC_TYPE.CV) {

    val msb = msb_in
    val lsb = lsb_in

    constructor(msb : Int, lsb : hw_var) : this(hw_imm(msb.toString()), lsb)

    override fun GetWidth() : Int {
        return 0
    }
}

class hw_fraction_VC (val msb_in : hw_var, val lsb_in : hw_imm) : hw_fraction(FRAC_TYPE.VC) {

    val msb = msb_in
    val lsb = lsb_in

    constructor(msb : hw_var, lsb : Int) : this(msb, hw_imm(lsb.toString()))

    override fun GetWidth() : Int {
        return 0
    }
}

class hw_fraction_VV (val msb_in : hw_var, val lsb_in : hw_var) : hw_fraction(FRAC_TYPE.VV) {

    val msb = msb_in
    val lsb = lsb_in

    override fun GetWidth() : Int {
        return 0
    }
}

class hw_fraction_SubStruct (val substruct_name_in : String) : hw_fraction(FRAC_TYPE.SubStruct) {

    val substruct_name = substruct_name_in
    var subStructIndex : Int = 0
    var src_struct = DUMMY_STRUCT

    override fun GetWidth() : Int {
        return 0
    }
}

class hw_fractions() : ArrayList<hw_fraction>() {
    constructor(index : Int) : this() {
        this.add(hw_fraction_C(index))
    }

    constructor(index : hw_var) : this() {
        this.add(hw_fraction_V(index))
    }

    constructor(msb : Int, lsb : Int) : this() {
        this.add(hw_fraction_CC(msb, lsb))
    }

    constructor(msb : Int, lsb : hw_var) : this() {
        this.add(hw_fraction_CV(msb, lsb))
    }

    constructor(msb : hw_var, lsb : Int) : this() {
        this.add(hw_fraction_VC(msb, lsb))
    }

    constructor(msb : hw_var, lsb : hw_var) : this() {
        this.add(hw_fraction_VV(msb, lsb))
    }

    constructor(substruct_name_in : String) : this() {
        this.add(hw_fraction_SubStruct(substruct_name_in))
    }
}
