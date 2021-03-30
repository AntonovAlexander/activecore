/*
 * hw_dim_static.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

class hw_dim_static() : ArrayList<hw_dim_range_static>() {

    constructor(imm_value : String) : this() {
        // TODO: compute width according to value
        val new_range = hw_dim_range_static(31, 0)
        add(new_range)
    }

    constructor(msb: Int, lsb: Int) : this() {
        val new_range = hw_dim_range_static(msb, lsb)
        add(new_range)
    }

    constructor(width: Int) : this() {
        val new_range = hw_dim_range_static(width-1, 0)
        add(new_range)
    }

    fun add(msb: Int, lsb: Int) {
        add(hw_dim_range_static(msb, lsb))
    }

    fun GetPower(): Int {
        var ret_val: Int = 0
        for (dimrange in this) {
            if (dimrange.msb != dimrange.lsb) continue
            else ret_val++
        }
        return ret_val;
    }

    fun isSingle(): Boolean {
        if (size == 0) return true
        if (size == 1) {
            if (get(0).GetWidth() == 1) return true
        }
        return false;
    }

    fun Print() {
        print("dimensions:")
        for (dim_range_static in this) {
            print(" [" + dim_range_static.msb + ":" + dim_range_static.lsb + "]")
        }
        print("\n")
    }
}