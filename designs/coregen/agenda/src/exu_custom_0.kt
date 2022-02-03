/*
 * exu_custom_0.kt
 *
 *  Created on: 03.02.2022
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

package agenda

internal class EXU_CUSTOM_0 : reordex.Exu("CUSTOM_0", CFG) {

    init {

        begif(gr(CFG.src0, CFG.src1))
        run {
            CFG.rd.assign(CFG.src0)
        }; endif()
        begelse()
        run {
            CFG.rd.assign(CFG.src1)
        }; endif()
    }
}