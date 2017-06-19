/*
 * activecore.cpp
 *
 *  Created on: 31.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#include "ac_utils.hpp"
#include "ac_core.hpp"
#include "ac_core_cmds_tcl.hpp"
#include "ac_pipe_cmd_tcl.hpp"
#include "ac_rtl_cmd_tcl.hpp"

static int counter;

extern "C" int testCmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
    counter++;
    printf("called with %d arguments, counter: %d\n", objc, counter);
    std::cout << (GetGenName("qq"));
    std::cout << "\n";
    return TCL_OK;
}

extern "C" int Activecore_Init(Tcl_Interp *interp) {
    if (Tcl_InitStubs(interp, "8.1", 0) == NULL) {
	return TCL_ERROR;
    }
    printf("ActiveCore Design Library loaded successfully!\n");
    Tcl_CreateObjCommand(interp, "test", testCmd, NULL, NULL);

    TCL_core_InitCmds(interp);
    TCL_rtl_InitCmds(interp);
    TCL_pipe_InitCmds(interp);

    counter = 1;

    return TCL_OK;
}
