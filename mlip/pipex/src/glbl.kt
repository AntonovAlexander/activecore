/*
 * glbl.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package pipex

fun ERROR(err_string : String) {
    throw Exception("ActiveCore (pipex) ERROR: " + err_string)
}

fun MSG(msg_string : String) {
    println("pipex: " + msg_string)
}

fun MSG(DEBUG_FLAG : Boolean, msg_string : String) {
    if (DEBUG_FLAG) MSG(msg_string)
}
