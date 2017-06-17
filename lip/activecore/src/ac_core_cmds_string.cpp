/*
 * ac_core_cmds_ext.cpp
 *
 *  Created on: 30.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#include <stdio.h>
#include <string>
#include <vector>
#include <sstream>

#include "ac_core_cmds_string.hpp"
#include "ac_core_cmds.hpp"
#include "ac_rtl.hpp"

int expr_assign_cmd_string(std::string target, ac_param param)
{
	bool cproc_gen = false;
	if (ExeStack.size() == 0) cproc_gen = true;
	if (cproc_gen == true) rtl::cproc_cmd();

	ac_var* target_var;
	if (SetVarReadable(target, &target_var) != 0) return 1;
	int ret_val = expr_assign_cmd(DimensionsAccumulator, target_var, param);

	if (cproc_gen == true) rtl::endcproc_cmd();
	return ret_val;
}

int expr_op_cmd_string(std::string opcode, std::vector<ac_param> params, std::string * respvarname)
{
	bool cproc_gen = false;
	if (ExeStack.size() == 0) cproc_gen = true;
	if (cproc_gen == true) rtl::cproc_cmd();

	ac_var* resp_var;
	if (expr_op_cmd(opcode, params, &resp_var) != 0)
	{
		printf("expr_op_cmd_string: ERROR\n");
		return 1;
	}

	(*respvarname) = resp_var->name;
	if (cproc_gen == true) rtl::endcproc_cmd();
	return 0;
}

int expr_zeroext_cmd_string(std::string size, std::vector<ac_param> params, std::string * respvarname)
{
	if (params.size() != 1)
	{
		printf("ActiveCore ERROR: zeroext operand error\n");
		return 1;
	}

	unsigned int target_width = conv_string_to_int(size);

	bool cproc_gen = false;
	if (ExeStack.size() == 0) cproc_gen = true;
	if (cproc_gen == true) rtl::cproc_cmd();

	ac_var* resp_var;
	if (expr_zeroext_cmd(target_width, params[0], &resp_var) != 0)
	{
		printf("expr_op_cmd_string: ERROR\n");
		return 1;
	}

	(*respvarname) = resp_var->name;
	if (cproc_gen == true) rtl::endcproc_cmd();
	return 0;
}

int expr_signext_cmd_string(std::string size, std::vector<ac_param> params, std::string * respvarname)
{
	if (params.size() != 1)
	{
		printf("ActiveCore ERROR: signext operand error\n");
	}

	unsigned int target_width = conv_string_to_int(size);

	bool cproc_gen = false;
	if (ExeStack.size() == 0) cproc_gen = true;
	if (cproc_gen == true) rtl::cproc_cmd();

	ac_var* resp_var;
	if (expr_signext_cmd(target_width, params[0], &resp_var) != 0)
	{
		printf("expr_op_cmd_string: ERROR\n");
		return 1;
	}

	(*respvarname) = resp_var->name;
	if (cproc_gen == true) rtl::endcproc_cmd();
	return 0;
}

int expr_initval_cmd_string(std::string width, std::string value, std::string * respvarname)
{
	bool cproc_gen = false;
	if (ExeStack.size() == 0) cproc_gen = true;
	if (cproc_gen == true) rtl::cproc_cmd();

	int conv_val = conv_string_to_int(width);

	ac_var * resp_var;
	int ret_stat = expr_initval_cmd(conv_val, value, &resp_var);

	if (cproc_gen == true) rtl::endcproc_cmd();

	*respvarname = resp_var->name;
	return ret_stat;
}

int expr_begif_cmd_string(std::string cond_op)
{
	if (ExeStack.size() == 0) return 1;

	ac_var* cond_op_var;
	if (SetVarReadable(cond_op, &cond_op_var) != 0) return 1;
	int ret_val = expr_begif_cmd(cond_op_var);

	return ret_val;
}

int expr_elsif_cmd_string(std::string cond_op)
{
	if (ExeStack.size() == 0) return 1;

	ac_var* cond_op_var;
	if (SetVarReadable(cond_op, &cond_op_var) != 0) return 1;
	int ret_val = expr_elsif_cmd(cond_op_var);

	return ret_val;
}

int expr_begelse_cmd_string()
{
	if (ExeStack.size() == 0) return 1;
	return expr_begelse_cmd();
}

int expr_endif_cmd_string()
{
	if (ExeStack.size() == 0) return 1;

	int ret_val = expr_endif_cmd();

	return ret_val;
}


