/*
 * glbl.kt
 *
 *  Created on: 05.06.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package hwast

import kotlin.math.log2

fun ERROR(err_string : String) {
    throw Exception("ActiveCore ERROR: " + err_string)
}

fun WARNING(err_string : String) {
    println("ActiveCore WARNING: " + err_string)
}

fun CRITICAL(err_string : String) {
    println("ActiveCore CRITICAL WARNING: " + err_string)
}

fun MSG(msg_string : String) {
    println("ActiveCore: " + msg_string)
}

fun MSG(DEBUG_FLAG : Boolean, msg_string : String) {
    if (DEBUG_FLAG) MSG(msg_string)
}

var DUMMY_STRUCT = hw_struct("GEN_DUMMY")

fun GetWidthToContain(variations: Int) : Int {
    var ret_width = Math.ceil(log2(variations.toDouble())).toInt()
    if (ret_width < 1) ret_width = 1
    return ret_width
}

fun <T> UniteArrayLists(List0 : ArrayList<T>, List1 : ArrayList<T>) : ArrayList<T> {
    var ret_ArrayList = ArrayList<T>()
    ret_ArrayList.addAll(List0)
    for (elem1 in List1) {
        if (!ret_ArrayList.contains(elem1)) ret_ArrayList.add(elem1)
    }
    return ret_ArrayList
}

fun <T> CrossArrayLists(List0 : ArrayList<T>, List1 : ArrayList<T>) : ArrayList<T> {
    var ret_ArrayList = ArrayList<T>()
    for (elem0 in List0) {
        if (List1.contains(elem0)) ret_ArrayList.add(elem0)
    }
    return ret_ArrayList
}
