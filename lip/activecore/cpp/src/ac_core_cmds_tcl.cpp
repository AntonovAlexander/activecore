/*
 * ac_core_cmds_tcl.cpp
 *
 *  Created on: 04.02.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#include "ac_utils.hpp"
#include "ac_core_cmds_tcl.hpp"
#include "ac_core.hpp"
#include "ac_core_cmds_string.hpp"


int TCL_core_reset_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Core reset command!\n");
	if (objc != 1)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	GenReset();
	ExeStack.clear();
	SignalsReadable.clear();
	SignalsWriteable.clear();
	DimensionsAccumulator.clear();
	ParamAccumulator.clear();
	return TCL_OK;
}

int TCL_core_debug_set_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Core debug set command!\n");
	if (objc != 1)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	DEBUG_FLAG = true;
	return TCL_OK;
}

int TCL_core_debug_clr_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Core debug clr command!\n");
	if (objc != 1)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	DEBUG_FLAG = false;
	return TCL_OK;
}

int TCL_accum_param_clr_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Param clr command!\n");
	if (objc != 1)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	ParamAccumulator.clear();

	return TCL_OK;
}

int TCL_accum_param_c_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Accum param c command!\n");
	if (objc != 4)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	std::string msb = std::string(Tcl_GetString(objv[1]));
	std::string lsb = std::string(Tcl_GetString(objv[2]));
	std::string val = std::string(Tcl_GetString(objv[3]));

	ac_dimensions_static new_dimensions;
	dimension_range_static new_range(conv_string_to_int(msb), conv_string_to_int(lsb));
	new_dimensions.push_back(new_range);

	ac_imm * new_imm = new ac_imm(new_dimensions, val);
	ac_param * new_param = new ac_param(new_imm);
	ParamAccumulator.push_back(*new_param);
	return TCL_OK;
}

int TCL_accum_param_v_rd_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Accum param v rd command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	std::string varname = std::string(Tcl_GetString(objv[1]));
	ac_var * var;
	if (GetVarReadable(varname, &var) != 0) return 1;
	ac_param * new_param = new ac_param(var);
	ParamAccumulator.push_back(*new_param);
	return TCL_OK;
}

int TCL_accum_param_v_wr_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Accum param v rd command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	std::string varname = std::string(Tcl_GetString(objv[1]));
	ac_var * var;
	if (GetVarWriteable(varname, &var) != 0) return 1;
	ac_param * new_param = new ac_param(var);
	ParamAccumulator.push_back(*new_param);
	return TCL_OK;
}

int TCL_accum_dim_clr_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Acc clr command!\n");
	if (objc != 1)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	DimensionsAccumulator.clear();

	return TCL_OK;
}

int TCL_accum_index_c_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Accum index (C) command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	int msb = conv_string_to_int(std::string(Tcl_GetString(objv[1])));

	dimension_range new_range(msb);
	DimensionsAccumulator.push_back(new_range);

	return TCL_OK;
}

int TCL_accum_index_v_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Accum index (V) command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	std::string msb = std::string(Tcl_GetString(objv[1]));
	ac_var* msb_var;
	if (SetVarReadable(msb, &msb_var) != 0) return TCL_ERROR;

	dimension_range new_range(msb_var);
	DimensionsAccumulator.push_back(new_range);

	return TCL_OK;
}

int TCL_accum_range_cc_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Accum range (CC) command!\n");
	if (objc != 3)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	int msb = conv_string_to_int(std::string(Tcl_GetString(objv[1])));
	int lsb = conv_string_to_int(std::string(Tcl_GetString(objv[2])));

	dimension_range new_range(msb, lsb);
	DimensionsAccumulator.push_back(new_range);

	return TCL_OK;
}

int TCL_accum_range_cv_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Accum range (CV) command!\n");
	if (objc != 3)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	int msb = conv_string_to_int(std::string(Tcl_GetString(objv[1])));

	std::string lsb = std::string(Tcl_GetString(objv[2]));
	ac_var* lsb_var;
	if (SetVarReadable(lsb, &lsb_var) != 0) return TCL_ERROR;

	dimension_range new_range(msb, lsb_var);
	DimensionsAccumulator.push_back(new_range);

	return TCL_OK;
}

int TCL_accum_range_vc_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Accum range (VC) command!\n");
	if (objc != 3)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	std::string msb = std::string(Tcl_GetString(objv[1]));
	ac_var* msb_var;
	if (SetVarReadable(msb, &msb_var) != 0) return TCL_ERROR;

	int lsb = conv_string_to_int(std::string(Tcl_GetString(objv[2])));

	dimension_range new_range(msb_var, lsb);
	DimensionsAccumulator.push_back(new_range);

	return TCL_OK;
}

int TCL_accum_range_vv_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Accum range (VV) command!\n");
	if (objc != 3)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	std::string msb = std::string(Tcl_GetString(objv[1]));
	ac_var* msb_var;
	if (SetVarReadable(msb, &msb_var) != 0) return TCL_ERROR;

	std::string lsb = std::string(Tcl_GetString(objv[2]));
	ac_var* lsb_var;
	if (SetVarReadable(lsb, &lsb_var) != 0) return TCL_ERROR;

	dimension_range new_range(msb_var, lsb_var);
	DimensionsAccumulator.push_back(new_range);

	return TCL_OK;
}

int TCL_expr_assign_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Assign command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	std::string target = std::string(Tcl_GetString(objv[1]));
	if (DEBUG_FLAG == true) printf("target: %s\n", StringToCharArr(target));
	if (ParamAccumulator.size() != 1)
	{
		printf("ActiveCore ERROR: incorrect params!\n");
		return TCL_ERROR;
	}
	if (DEBUG_FLAG == true)
	{
		printf("--ParamAccumulator size: %d\n", ParamAccumulator.size());
		for (unsigned int i = 0; i < ParamAccumulator.size(); i++)
		{
			printf("---Param %d:, %s\n", i, StringToCharArr(ParamAccumulator[i].GetString()));
		}
	}
	if (expr_assign_cmd_string(target, ParamAccumulator[0]) != 0) return TCL_ERROR;
	ParamAccumulator.clear();
	DimensionsAccumulator.clear();
	if (DEBUG_FLAG == true) printf("Assign command complete!\n");
	return TCL_OK;
}

int TCL_expr_op_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("op c command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	std::string opcode = std::string(Tcl_GetString(objv[1]));
	std::string genvar_name;

	if (DEBUG_FLAG == true) printf("opcode: %s\n", StringToCharArr(opcode));
	if (DEBUG_FLAG == true) printf("--ParamAccumulator size: %d\n", ParamAccumulator.size());
	for (unsigned int i = 0; i < ParamAccumulator.size(); i++)
	{
		if (DEBUG_FLAG == true) printf("---Param %d:, %s\n", i, StringToCharArr(ParamAccumulator[i].GetString()));
	}

	if (expr_op_cmd_string(opcode, ParamAccumulator, &genvar_name) != 0) return TCL_ERROR;
	Tcl_SetResult(interp, StringToCharArr(genvar_name), TCL_VOLATILE);
	ParamAccumulator.clear();
	return TCL_OK;
}

int TCL_expr_zeroext_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("zeroext command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	std::string size = std::string(Tcl_GetString(objv[1]));
	std::string genvar_name;

	if (expr_zeroext_cmd_string(size, ParamAccumulator, &genvar_name) != 0) return TCL_ERROR;
	Tcl_SetResult(interp, StringToCharArr(genvar_name), TCL_VOLATILE);
	ParamAccumulator.clear();
	return TCL_OK;
}

int TCL_expr_signext_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("signext command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}

	std::string size = std::string(Tcl_GetString(objv[1]));
	std::string genvar_name;

	if (expr_signext_cmd_string(size, ParamAccumulator, &genvar_name) != 0) return TCL_ERROR;
	Tcl_SetResult(interp, StringToCharArr(genvar_name), TCL_VOLATILE);
	ParamAccumulator.clear();
	return TCL_OK;
}

int TCL_expr_begif_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Begif command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	std::string cond_op = std::string(Tcl_GetString(objv[1]));
	if (DEBUG_FLAG == true) printf("TCL_expr_begif_cmd: %s\n", StringToCharArr(cond_op));
	if (expr_begif_cmd_string(cond_op) != 0) return TCL_ERROR;
	return TCL_OK;
}

int TCL_expr_begelsif_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Elsif command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	std::string cond_op = std::string(Tcl_GetString(objv[1]));
	if (expr_begelsif_cmd_string(cond_op) != 0) return TCL_ERROR;
	return TCL_OK;
}

int TCL_expr_begelse_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Begelse command!\n");
	if (objc != 1)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	if (expr_begelse_cmd_string() != 0) return TCL_ERROR;
	return TCL_OK;
}

int TCL_expr_endif_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Endif command!\n");
	if (objc != 1)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	if (expr_endif_cmd_string() != 0) return TCL_ERROR;
	return TCL_OK;
}

int TCL_expr_begwhile_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Begwhile command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	std::string cond_op = std::string(Tcl_GetString(objv[1]));
	if (DEBUG_FLAG == true) printf("TCL_expr_begwhile_cmd: %s\n", StringToCharArr(cond_op));
	if (expr_begwhile_cmd_string(cond_op) != 0) return TCL_ERROR;
	return TCL_OK;
}

int TCL_expr_endwhile_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Endwhile command!\n");
	if (objc != 1)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	if (expr_endwhile_cmd_string() != 0) return TCL_ERROR;
	return TCL_OK;
}

int TCL_core_InitCmds(Tcl_Interp *interp)
{
	Tcl_CreateObjCommand(interp, "__ac_core_reset", TCL_core_reset_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_debug_set", TCL_core_debug_set_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_debug_clr", TCL_core_debug_clr_cmd, NULL, NULL);

	Tcl_CreateObjCommand(interp, "__ac_core_acc_param_clr", TCL_accum_param_clr_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_acc_param_c", TCL_accum_param_c_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_acc_param_v_rd", TCL_accum_param_v_rd_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_acc_param_v_wr", TCL_accum_param_v_wr_cmd, NULL, NULL);

	Tcl_CreateObjCommand(interp, "__ac_core_acc_dim_clr", TCL_accum_dim_clr_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_acc_index_c", TCL_accum_index_c_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_acc_index_v", TCL_accum_index_v_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_acc_range_cc", TCL_accum_range_cc_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_acc_range_cv", TCL_accum_range_cv_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_acc_range_vc", TCL_accum_range_vc_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_acc_range_vv", TCL_accum_range_vv_cmd, NULL, NULL);

	Tcl_CreateObjCommand(interp, "__ac_core_assign", TCL_expr_assign_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_op", TCL_expr_op_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_zeroext", TCL_expr_zeroext_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_signext", TCL_expr_signext_cmd, NULL, NULL);

	Tcl_CreateObjCommand(interp, "__ac_core_begif", TCL_expr_begif_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_begelsif", TCL_expr_begelsif_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_begelse", TCL_expr_begelse_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_endif", TCL_expr_endif_cmd, NULL, NULL);

	Tcl_CreateObjCommand(interp, "__ac_core_begwhile", TCL_expr_begwhile_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__ac_core_endwhile", TCL_expr_endwhile_cmd, NULL, NULL);
}
