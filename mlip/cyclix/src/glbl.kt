/*
 * glbl.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package cyclix

fun ERROR(err_string : String) {
    throw Exception("ActiveCore (cyclix) ERROR: " + err_string)
}