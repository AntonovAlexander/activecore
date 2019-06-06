/*
 * hw_dim_range_static.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

class hw_dim_range_static (val msb: Int, val lsb: Int) {

    fun GetWidth(): Int {
        if (msb > lsb) return ((msb - lsb) + 1)
        else return ((lsb - msb) + 1)
    }

    fun CheckEqual(dim : hw_dim_range_static): Boolean {
        return ((msb == dim.msb) && (lsb == dim.lsb))
    }

    fun run() {
        println("Hello World!")
    }
}