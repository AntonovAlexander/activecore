/*
 * hw_fractured.kt
 *
 *  Created on: 08.11.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

// Type container for depowered variables
class hw_fractured (val src: hw_var, depow_fractions_in: hw_fracs) {

    var depow_fractions = hw_fracs()
    var depowered_fractions = hw_type(DUMMY_STRUCT)

    init {
        for (frac in depow_fractions_in) depow_fractions.add(frac)
        depowered_fractions = src.GetDepowered(depow_fractions)
    }
}
