/*
 * ac_core_cmds_tcl.cpp
 *
 *  Created on: 04.02.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#include "ac_utils.hpp"
#include "ac_core_cmds.hpp"
#include "ac_core_cmds_tcl.hpp"
#include "ac_core.hpp"
#include "ac_rtl.hpp"


int TCL_gplc_reset_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
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
	StringParamAccumulator.clear();
	IntParamAccumulator.clear();
	UIntParamAccumulator.clear();
	return TCL_OK;
}

int TCL_debug_set_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
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

int TCL_debug_clr_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
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
	StringParamAccumulator.clear();
	IntParamAccumulator.clear();
	UIntParamAccumulator.clear();
	return TCL_OK;
}

int TCL_accum_param_int_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Param clr command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	std::string param = std::string(Tcl_GetString(objv[1]));
	IntParamAccumulator.push_back(conv_string_to_int(param));
	return TCL_OK;
}

int TCL_accum_param_uint_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Param clr command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	std::string param = std::string(Tcl_GetString(objv[1]));
	UIntParamAccumulator.push_back(conv_string_to_uint(param));
	return TCL_OK;
}

int TCL_accum_param_string_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Param clr command!\n");
	if (objc != 2)
	{
		printf("Incorrect command!\n");
		return TCL_ERROR;
	}
	std::string param = std::string(Tcl_GetString(objv[1]));
	StringParamAccumulator.push_back(param);
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
	DimensionsAccumulator.push_front(new_range);

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
	DimensionsAccumulator.push_front(new_range);

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
	DimensionsAccumulator.push_front(new_range);

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
	DimensionsAccumulator.push_front(new_range);

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
	DimensionsAccumulator.push_front(new_range);

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
	DimensionsAccumulator.push_front(new_range);

	return TCL_OK;
}

int TCL_gplc_call_cmd(ClientData clientData, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
{
	if (DEBUG_FLAG == true) printf("Pipe call command!\n");
	if (objc != 2)
	{
		printf("Incorrect arguments!\n");
		return TCL_ERROR;
	}

	std::string opcode = std::string(Tcl_GetString(objv[1]));
	if (DEBUG_FLAG == true)
	{
		printf("################\n");
		printf("command: %s!\n", StringToCharArr(opcode));
		printf("[[Params]]\n");

		printf("[Int params: %d]\n", IntParamAccumulator.size());
		for (unsigned int i = 0; i < IntParamAccumulator.size(); i++)
		{
			printf("%d\n", IntParamAccumulator[i]);
		}

		printf("[UInt params: %d]\n", UIntParamAccumulator.size());
		for (unsigned int i = 0; i < UIntParamAccumulator.size(); i++)
		{
			printf("%d\n", UIntParamAccumulator[i]);
		}

		printf("[String params: %d]\n", StringParamAccumulator.size());
		for (unsigned int i = 0; i < StringParamAccumulator.size(); i++)
		{
			printf("%s\n", StringToCharArr(StringParamAccumulator[i]));
		}

		printf("[Regular params: %d]\n", ParamAccumulator.size());
		for (unsigned int i = 0; i < ParamAccumulator.size(); i++)
		{
			unsigned int dimsize = 1;
			if (ParamAccumulator[i].type == PARAM_TYPE_VAR) dimsize = ParamAccumulator[i].var->dimensions.size();
			printf("%s, dimsize: %d\n", StringToCharArr(ParamAccumulator[i].GetStringFull()), dimsize);
		}
	}

	if (opcode == "op") {

		if (StringParamAccumulator.size() != 1)
		{
			printf("Incorrect params!\n");
			return TCL_ERROR;
		}

		char * opcode;

		std::string string_opcode = StringParamAccumulator[0];

		if (string_opcode == toString(OP1_ASSIGN)) opcode = OP1_ASSIGN;
		else if (string_opcode == toString(OP1_COMPLEMENT)) opcode = OP1_COMPLEMENT;

		else if (string_opcode == toString(OP2_ARITH_ADD)) 		opcode = OP2_ARITH_ADD;
		else if (string_opcode == toString(OP2_ARITH_SUB)) 		opcode = OP2_ARITH_SUB;
		else if (string_opcode == toString(OP2_ARITH_MUL)) 		opcode = OP2_ARITH_MUL;
		else if (string_opcode == toString(OP2_ARITH_DIV)) 		opcode = OP2_ARITH_DIV;
		else if (string_opcode == toString(OP2_ARITH_SHL)) 		opcode = OP2_ARITH_SHL;
		else if (string_opcode == toString(OP2_ARITH_SHR)) 		opcode = OP2_ARITH_SHR;
		else if (string_opcode == toString(OP2_ARITH_SRA)) 		opcode = OP2_ARITH_SRA;

		else if (string_opcode == toString(OP1_LOGICAL_NOT)) 	opcode = OP1_LOGICAL_NOT;
		else if (string_opcode == toString(OP2_LOGICAL_AND)) 	opcode = OP2_LOGICAL_AND;
		else if (string_opcode == toString(OP2_LOGICAL_OR)) 	opcode = OP2_LOGICAL_OR;
		else if (string_opcode == toString(OP2_LOGICAL_G)) 		opcode = OP2_LOGICAL_G;
		else if (string_opcode == toString(OP2_LOGICAL_L)) 		opcode = OP2_LOGICAL_L;
		else if (string_opcode == toString(OP2_LOGICAL_GEQ)) 	opcode = OP2_LOGICAL_GEQ;
		else if (string_opcode == toString(OP2_LOGICAL_LEQ)) 	opcode = OP2_LOGICAL_LEQ;
		else if (string_opcode == toString(OP2_LOGICAL_EQ2)) 	opcode = OP2_LOGICAL_EQ2;
		else if (string_opcode == toString(OP2_LOGICAL_NEQ2)) 	opcode = OP2_LOGICAL_NEQ2;
		else if (string_opcode == toString(OP2_LOGICAL_EQ4)) 	opcode = OP2_LOGICAL_EQ4;
		else if (string_opcode == toString(OP2_LOGICAL_NEQ4)) 	opcode = OP2_LOGICAL_NEQ4;

		else if (string_opcode == toString(OP1_BITWISE_NOT)) 	opcode = OP1_BITWISE_NOT;
		else if (string_opcode == toString(OP2_BITWISE_AND)) 	opcode = OP2_BITWISE_AND;
		else if (string_opcode == toString(OP2_BITWISE_OR)) 	opcode = OP2_BITWISE_OR;
		else if (string_opcode == toString(OP2_BITWISE_XOR)) 	opcode = OP2_BITWISE_XOR;
		else if (string_opcode == toString(OP2_BITWISE_XNOR)) 	opcode = OP2_BITWISE_XNOR;

		else if (string_opcode == toString(OP1_REDUCT_AND)) 	opcode = OP1_REDUCT_AND;
		else if (string_opcode == toString(OP1_REDUCT_NAND)) 	opcode = OP1_REDUCT_NAND;
		else if (string_opcode == toString(OP1_REDUCT_OR)) 		opcode = OP1_REDUCT_OR;
		else if (string_opcode == toString(OP1_REDUCT_NOR)) 	opcode = OP1_REDUCT_NOR;
		else if (string_opcode == toString(OP1_REDUCT_XOR)) 	opcode = OP1_REDUCT_XOR;
		else if (string_opcode == toString(OP1_REDUCT_XNOR)) 	opcode = OP1_REDUCT_XNOR;

		else if (string_opcode == toString(OP2_INDEXED)) 		opcode = OP2_INDEXED;
		else if (string_opcode == toString(OP3_RANGED)) 		opcode = OP3_RANGED;
		else if (string_opcode == toString(OPS_CNCT)) 			opcode = OPS_CNCT;

		else {
			printf("ERROR: Opcode not recognized: %s\n", StringToCharArr(string_opcode));
		}

		if (DEBUG_FLAG == true) printf("decoded opcode: %s\n", opcode);

		ac_var* genvar;

		bool cproc_gen = false;
		if (ExeStack.size() == 0) cproc_gen = true;
		if (cproc_gen == true) rtl::cproc_cmd();
		if (expr_op_cmd(opcode, &genvar, ParamAccumulator) != 0)
		{
			printf("expr_op_cmd_string: ERROR\n");
			return 1;
		}
		if (cproc_gen == true) rtl::endcproc_cmd();

		if (DEBUG_FLAG == true) printf("generated var name: %s\n", StringToCharArr(genvar->name));
		if (DEBUG_FLAG == true) printf("generated var dimsize: %d\n", genvar->dimensions.size());
		Tcl_SetResult(interp, StringToCharArr(genvar->name), TCL_VOLATILE);

	} else if (opcode == "clrif") {
		if (expr_clrif_cmd() != 0) return TCL_ERROR;

	} else if (opcode == "begif") {
		if (ParamAccumulator.size() != 1)
		{
			printf("Incorrect params!\n");
			return TCL_ERROR;
		}
		if (ExeStack.size() == 0) return 1;
		if (expr_begif_cmd(ParamAccumulator[0]) != 0) return TCL_ERROR;

	} else if (opcode == "begelsif") {
		if (ParamAccumulator.size() != 1)
		{
			printf("Incorrect params!\n");
			return TCL_ERROR;
		}
		if (ExeStack.size() == 0) return 1;
		if (expr_begelsif_cmd(ParamAccumulator[0]) != 0) return TCL_ERROR;

	} else if (opcode == "begelse") {
		if (ExeStack.size() == 0) return 1;
		if (expr_begelse_cmd() != 0) return TCL_ERROR;

	} else if (opcode == "endif") {
		if (ExeStack.size() == 0) return 1;
		if (expr_endif_cmd() != 0) return TCL_ERROR;

	} else if (opcode == "begwhile") {
		if (ParamAccumulator.size() != 1)
		{
			printf("Incorrect params!\n");
			return TCL_ERROR;
		}
		if (ExeStack.size() == 0) return 1;
		if (expr_begwhile_cmd(ParamAccumulator[0]) != 0) return TCL_ERROR;

	} else if (opcode == "endwhile") {
		if (ExeStack.size() == 0) return 1;
		if (expr_endwhile_cmd() != 0) return TCL_ERROR;


	} else if (opcode == "assign") {
		if (ParamAccumulator.size() != 2)
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

		bool cproc_gen = false;
		if (ExeStack.size() == 0) cproc_gen = true;
		if (cproc_gen == true) rtl::cproc_cmd();

		if (expr_assign_cmd(DimensionsAccumulator, ParamAccumulator[1].var, ParamAccumulator[0]) != 0) return TCL_ERROR;

		if (cproc_gen == true) rtl::endcproc_cmd();

	} else if (opcode == "initval") {
		ac_dimensions_static new_dimensions;
		for (unsigned int i = 0; i < DimensionsAccumulator.size(); i++)
		{
			if (DimensionsAccumulator[i].type != DimType_CC)
			{
				printf("Range arguments are incorrect!\n");
				return TCL_ERROR;
			}
			new_dimensions.push_back(*(new dimension_range_static(DimensionsAccumulator[i].msb_int, DimensionsAccumulator[i].lsb_int)));
		}

		if (ParamAccumulator.size() != 1)
		{
			printf("Params are incorrect!\n");
			return TCL_ERROR;
		}

		ac_var * genvar;
		if (expr_initval_cmd(&genvar, new_dimensions, ParamAccumulator[0]) != 0) return TCL_ERROR;
		Tcl_SetResult(interp, StringToCharArr(genvar->name), TCL_VOLATILE);

	} else if (opcode == "zeroext") {
		if ((ParamAccumulator.size() != 1) || (UIntParamAccumulator.size() != 1))
		{
			printf("ActiveCore ERROR: incorrect params!\n");
			return TCL_ERROR;
		}
		ac_var * genvar;

		bool cproc_gen = false;
		if (ExeStack.size() == 0) cproc_gen = true;
		if (cproc_gen == true) rtl::cproc_cmd();

		if (expr_zeroext_cmd(UIntParamAccumulator[0], &genvar, ParamAccumulator[0]) != 0)
		{
			printf("expr_zeroext_cmd: ERROR\n");
			return 1;
		}
		if (cproc_gen == true) rtl::endcproc_cmd();

		Tcl_SetResult(interp, StringToCharArr(genvar->name), TCL_VOLATILE);

	} else if (opcode == "signext") {
		if ((ParamAccumulator.size() != 1) || (UIntParamAccumulator.size() != 1))
		{
			printf("ActiveCore ERROR: incorrect params!\n");
			return TCL_ERROR;
		}
		ac_var * genvar;

		bool cproc_gen = false;
		if (ExeStack.size() == 0) cproc_gen = true;
		if (cproc_gen == true) rtl::cproc_cmd();

		if (expr_signext_cmd(UIntParamAccumulator[0], &genvar, ParamAccumulator[0]) != 0)
		{
			printf("expr_signext_cmd: ERROR\n");
			return 1;
		}
		if (cproc_gen == true) rtl::endcproc_cmd();

		Tcl_SetResult(interp, StringToCharArr(genvar->name), TCL_VOLATILE);

	} else {
		printf("Command %s unknown!\n", StringToCharArr(opcode));
		return TCL_ERROR;
	}

	DimensionsAccumulator.clear();
	StringParamAccumulator.clear();
	ParamAccumulator.clear();
	return TCL_OK;
}

int TCL_core_InitCmds(Tcl_Interp *interp)
{
	Tcl_CreateObjCommand(interp, "__gplc_reset", TCL_gplc_reset_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_debug_set", TCL_debug_set_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_debug_clr", TCL_debug_clr_cmd, NULL, NULL);

	Tcl_CreateObjCommand(interp, "__gplc_call", TCL_gplc_call_cmd, NULL, NULL);

	Tcl_CreateObjCommand(interp, "__gplc_acc_param_clr", TCL_accum_param_clr_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_param_string", TCL_accum_param_string_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_param_int", TCL_accum_param_int_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_param_uint", TCL_accum_param_uint_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_param_c", TCL_accum_param_c_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_param_v_rd", TCL_accum_param_v_rd_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_param_v_wr", TCL_accum_param_v_wr_cmd, NULL, NULL);

	Tcl_CreateObjCommand(interp, "__gplc_acc_dim_clr", TCL_accum_dim_clr_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_index_c", TCL_accum_index_c_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_index_v", TCL_accum_index_v_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_range_cc", TCL_accum_range_cc_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_range_cv", TCL_accum_range_cv_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_range_vc", TCL_accum_range_vc_cmd, NULL, NULL);
	Tcl_CreateObjCommand(interp, "__gplc_acc_range_vv", TCL_accum_range_vv_cmd, NULL, NULL);
}
