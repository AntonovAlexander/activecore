/*
 * hw_fractured.kt
 *
 *  Created on: 08.11.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

// Type container for depowered variables
class hw_fractured (src_in: hw_var, depow_fractions_in: hw_fractions) {

    val src = src_in
    val depow_fractions = depow_fractions_in
    val type_depowered = src_in.GetDepowered(depow_fractions)
}
