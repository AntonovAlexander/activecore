/*
 * hw_frac.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

abstract class hw_frac () {
    abstract fun GetWidth() : Int
}

class hw_frac_C (val index : hw_imm) : hw_frac() {

    constructor(index : Int) : this(hw_imm(index.toString()))

    override fun GetWidth() : Int {
        return 1
    }
}

class hw_frac_V (val index : hw_var) : hw_frac() {

    override fun GetWidth() : Int {
        return 1
    }
}

class hw_frac_CC (val msb : hw_imm, val lsb : hw_imm) : hw_frac() {

    constructor(msb : Int, lsb : Int) : this(hw_imm(msb.toString()), hw_imm(lsb.toString()))

    override fun GetWidth() : Int {
        if (msb.toInt() > lsb.toInt()) return (msb.toInt() - lsb.toInt())
        else return (lsb.toInt() - msb.toInt())
    }
}

class hw_frac_CV (val msb : hw_imm, val lsb : hw_var) : hw_frac() {

    constructor(msb : Int, lsb : hw_var) : this(hw_imm(msb.toString()), lsb)

    override fun GetWidth() : Int {
        return 0
    }
}

class hw_frac_VC (val msb : hw_var, val lsb : hw_imm) : hw_frac() {

    constructor(msb : hw_var, lsb : Int) : this(msb, hw_imm(lsb.toString()))

    override fun GetWidth() : Int {
        return 0
    }
}

class hw_frac_VV (val msb : hw_var, val lsb : hw_var) : hw_frac() {

    override fun GetWidth() : Int {
        return 0
    }
}

class hw_frac_SubStruct (val substruct_name : String) : hw_frac() {

    var subStructIndex : Int = 0
    var src_struct = DUMMY_STRUCT

    override fun GetWidth() : Int {
        return 0
    }
}

// container for variable fractures to take
class hw_fracs() : ArrayList<hw_frac>() {
    constructor(index : Int) : this() {
        this.add(hw_frac_C(index))
    }

    constructor(index : hw_var) : this() {
        this.add(hw_frac_V(index))
    }

    constructor(msb : Int, lsb : Int) : this() {
        this.add(hw_frac_CC(msb, lsb))
    }

    constructor(msb : Int, lsb : hw_var) : this() {
        this.add(hw_frac_CV(msb, lsb))
    }

    constructor(msb : hw_var, lsb : Int) : this() {
        this.add(hw_frac_VC(msb, lsb))
    }

    constructor(msb : hw_var, lsb : hw_var) : this() {
        this.add(hw_frac_VV(msb, lsb))
    }

    constructor(substruct_name : String) : this() {
        this.add(hw_frac_SubStruct(substruct_name))
    }

    constructor(vararg fractions: hw_frac) : this() {
        for (fraction in fractions) {
            this.add(fraction)
        }
    }

    fun add(new_elem : Int) {
        add(hw_frac_C(new_elem))
    }

    fun add(new_elem : hw_imm) {
        add(hw_frac_C(new_elem))
    }

    fun add(new_elem : hw_var) {
        add(hw_frac_V(new_elem))
    }

    fun add(new_elem0 : hw_imm, new_elem1 : hw_imm) {
        add(hw_frac_CC(new_elem0, new_elem1))
    }

    fun add(new_elem0 : hw_imm, new_elem1 : hw_var) {
        add(hw_frac_CV(new_elem0, new_elem1))
    }

    fun add(new_elem0 : hw_var, new_elem1 : hw_imm) {
        add(hw_frac_VC(new_elem0, new_elem1))
    }

    fun add(new_elem0 : hw_var, new_elem1 : hw_var) {
        add(hw_frac_VV(new_elem0, new_elem1))
    }

    fun add(new_elem : String) {
        add(hw_frac_SubStruct(new_elem))
    }

    fun FillSubStructs(tgt: hw_var) {
        var tgt_struct_ptr = tgt.vartype.src_struct
        for (fraction in this) {
            if (fraction is hw_frac_SubStruct) {
                //println("Substruct found!")
                if (tgt_struct_ptr != DUMMY_STRUCT) {
                    var substr_found = false
                    var SUBSTR_INDEX = 0
                    for (structvar in tgt_struct_ptr) {
                        //println("structvar: " + structvar.name)
                        if (structvar.name == fraction.substruct_name) {

                            //println("src_struct: " + tgt_struct_ptr.name)
                            //println("subStructIndex: " + SUBSTR_INDEX)
                            fraction.src_struct = tgt_struct_ptr
                            fraction.subStructIndex = SUBSTR_INDEX

                            if (structvar.vartype.DataType == DATA_TYPE.STRUCTURED) {
                                tgt_struct_ptr = structvar.vartype.src_struct
                            } else {
                                tgt_struct_ptr = DUMMY_STRUCT
                            }
                            substr_found = true
                            break
                        }
                        SUBSTR_INDEX += 1
                    }
                    if (!substr_found){
                        MSG("Available substucts in variable: " + tgt.name)
                        for (structvar in tgt_struct_ptr) MSG("substruct: " + structvar.name)
                        ERROR("substruct " + (fraction as hw_frac_SubStruct).substruct_name + " not found!")
                    }
                } else ERROR("substruct " + (fraction as hw_frac_SubStruct).substruct_name + " request for tgt " + tgt.name + " is inconsistent!")
            }
        }
    }
}
