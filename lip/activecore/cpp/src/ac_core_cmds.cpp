/*
 * ac_core_cmds.cpp
 *
 *  Created on: 30.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#include "ac_core_cmds.hpp"
#include "ac_core.hpp"
#include <math.h>

void AddIfTargetVarToStack(ac_var* new_op)
{
	for (unsigned int i = 0; i < ExeStack.size(); i++)
	{
		ExeStack[i]->AddIfTargetVar(new_op);
	}
}

int expr_assign_cmd_generated(unsigned int * cursor, ac_execode** new_expr, ac_dimensions dimensions, ac_var* target, ac_param param)
{
	(*new_expr) = new ac_execode("=");
	(*new_expr)->AddRdParamWithStack(param);
	(*new_expr)->AddWrVarWithStack(target);
	(*new_expr)->dimensions = dimensions;
	ExeStack[ExeStack.size()-1]->AddExpr(cursor, *new_expr);
	return 0;
}

int expr_assign_cmd_generated(ac_execode** new_expr, ac_dimensions dimensions, ac_var* target, ac_param param)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_assign_cmd_generated(&cursor, new_expr, dimensions, target, param);
}

int expr_assign_cmd_generated(unsigned int * cursor, ac_dimensions dimensions, ac_var* target, ac_param param)
{
	ac_execode* new_expr;
	return expr_assign_cmd_generated(cursor, &new_expr, dimensions, target, param);
}

int expr_assign_cmd_generated(ac_dimensions dimensions, ac_var* target, ac_param param)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	ac_execode* new_expr;
	return expr_assign_cmd_generated(&cursor, &new_expr, dimensions, target, param);
}

int expr_assign_cmd_generated(unsigned int * cursor, ac_var* target, ac_param param)
{
	ac_dimensions * dimensions = new ac_dimensions();
	return expr_assign_cmd_generated(cursor, *dimensions, target, param);
}

int expr_assign_cmd_generated(ac_var* target, ac_param param)
{
	ac_dimensions * dimensions = new ac_dimensions();
	return expr_assign_cmd_generated(*dimensions, target, param);
}

int expr_assign_cmd(unsigned int * cursor, ac_dimensions dimensions, ac_var* target, ac_param param)
{
	if (DEBUG_FLAG == true) printf("expr_assign_cmd: start: target: %s, source: %s\n", StringToCharArr(target->name), StringToCharArr(param.GetString()));

	std::string dimstring = "";
	if (dimensions.GetDimensionsString(&dimstring) != 0) return 1;
	if (DEBUG_FLAG == true) printf("expr_assign_var_cmd: dimensions obtained: %s\n", StringToCharArr(dimstring));

	int ret_val;

	unsigned int targetPower;
	unsigned int targetDePower;
	unsigned int targetDePowered;

	targetPower = target->dimensions.size();
	if (dimensions.GetPower(&targetDePower) != 0) return 1;
	targetDePowered = targetPower - targetDePower;

	//printf("source Power: %d\n", param.GetDimensions().size());
	//printf("targetPower: %d\n", targetPower);
	//printf("targetDePower: %d\n", targetDePower);
	//printf("targetDePowered: %d\n", targetDePowered);
	//printf("repeating: %d\n", target->dimensions[targetDePowered-1].GetWidth());

	if ((param.type == PARAM_TYPE_VAR) && (targetDePowered != param.GetDimensions().size()))
	{
		printf("ERROR: dimensions do not match!\n");
		return 1;
	}
	else if (targetDePowered == 1) ret_val = expr_assign_cmd_generated(cursor, dimensions, target, param);
	else if (param.type == PARAM_TYPE_VAR)
	{
		for (unsigned int i = 0; i < target->dimensions[targetDePowered-1].GetWidth(); i++)
		{
			dimension_range * new_range = new dimension_range(i);
			dimensions.push_front(*new_range);
			dimension_range_static * new_range_static = new dimension_range_static(i-1, 0);
			ac_dimensions_static gen_dimensions;
			gen_dimensions.push_back(*new_range_static);
			ac_var * new_source = new ac_var(DEFAULT_TYPEVAR, GetGenName("gen"), gen_dimensions, std::string("0"));

			std::vector<ac_param> ac_params_new;
			ac_params_new.push_back(param);
			ac_imm * new_imm = new ac_imm(NumToString(i));
			ac_param * new_param = new ac_param(new_imm);
			ac_params_new.push_back(*new_param);

			if (expr_op_cmd(cursor, "indexed", ac_params_new, (&new_source)) != 0) return 1;
			ret_val = expr_assign_cmd(cursor, dimensions, target, new_source);
			dimensions.pop_front();
			if (ret_val != 0) return ret_val;
		}
		return ret_val;
	}
	else if (param.type == PARAM_TYPE_VAL)
	{
		if (targetDePowered > 0)
		{
			for (unsigned int i = 0; i < target->dimensions[targetDePowered-1].GetWidth(); i++)
			{
				dimensions.push_front(*(new dimension_range(i)));
				ret_val = expr_assign_cmd(cursor, dimensions, target, param);
				dimensions.pop_front();
			}
		} else {
			printf("ERROR: dimensions are incorrect!\n");
			return 1;
		}
	}
	return ret_val;
}

int expr_assign_cmd(ac_dimensions dimensions, ac_var* target, ac_param param)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_assign_cmd(&cursor, dimensions, target, param);
}

int expr_assign_cmd(unsigned int * cursor, ac_var* target, ac_param param)
{
	ac_dimensions * dimensions = new ac_dimensions();
	return expr_assign_cmd(cursor, *dimensions, target, param);
}

int expr_assign_cmd(ac_var* target, ac_param param)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_assign_cmd(&cursor, target, param);
}

int GetImmWidth(std::string imm, ac_dimensions_static ** genvar_dimensions)
{
	unsigned int length =  pow (imm.length(), 4);
	dimension_range_static * new_range = new dimension_range_static(length-1, 0);
	*genvar_dimensions = new ac_dimensions_static();
	(*genvar_dimensions)->push_back(*new_range);
	return 0;
}

bool DimensionsEqual(dimension_range_static dim0, dimension_range_static dim1)
{
	if ((dim0.msb == dim1.msb) & (dim0.lsb == dim1.lsb)) return true;
	else return false;
}

bool DimensionsEqual(dimension_range dim0, dimension_range dim1)
{
	if (dim0.type != dim1.type) return false;
	switch (dim0.type)
	{
		case DimType_C:
			if (dim0.msb_int != dim1.msb_int) return false;

		case DimType_V:
			if (dim0.msb_var != dim1.msb_var) return false;

		case DimType_CC:
			if ((dim0.msb_int != dim1.msb_int) || (dim0.lsb_int != dim1.lsb_int)) return false;

		case DimType_CV:
			if ((dim0.msb_int != dim1.msb_int) || (dim0.lsb_var != dim1.lsb_var)) return false;

		case DimType_VC:
			if ((dim0.msb_var != dim1.msb_var) || (dim0.lsb_int != dim1.lsb_int)) return false;

		case DimType_VV:
			if ((dim0.msb_var != dim1.msb_var) || (dim0.lsb_var != dim1.lsb_var)) return false;

		default:
			return false;
	}
	return true;
}

int expr_2op_gen_dimensions(std::string opcode, ac_dimensions_static op1_dimensions, ac_dimensions_static op2_dimensions, ac_dimensions_static ** gen_dimensions)
{
	//printf("expr_2op_gen_dimensions: opcode: %s\n", StringToCharArr(opcode));
	//printf("expr_2op_gen_dimensions: op1_dimensions size: %d\n", op1_dimensions.size());
	//printf("expr_2op_gen_dimensions: op2_dimensions size: %d\n", op2_dimensions.size());

	dimension_range_static op1_primary_dimension = op1_dimensions[0];
	dimension_range_static op2_primary_dimension = op2_dimensions[0];

	unsigned int op1_length, op2_length, max_length;

	op1_length = op1_primary_dimension.GetWidth();
	op2_length = op2_primary_dimension.GetWidth();

	if (op1_length > op2_length) max_length = op1_length;
	else max_length = op2_length;

	(*gen_dimensions) = new ac_dimensions_static();
	if ((opcode == "+") || (opcode == "-")) {
		if (op1_dimensions.size() != op2_dimensions.size()) return 1;
		for (int i = 1; i < op1_dimensions.size(); i++)
		{
			if (DimensionsEqual(op1_dimensions[i], op2_dimensions[i]) == false) return 1;
			else (*gen_dimensions)->push_back(op1_dimensions[i]);
		}
		int gen_length;
		gen_length = max_length + 1;
		dimension_range_static new_range((gen_length - 1), (unsigned int)0);
		(*gen_dimensions)->push_front(new_range);

	} else if (opcode == "*") {
		if (op1_dimensions.size() != op2_dimensions.size()) return 1;
		for (int i = 1; i < op1_dimensions.size(); i++)
		{
			if (DimensionsEqual(op1_dimensions[i], op2_dimensions[i]) == false) return 1;
			else (*gen_dimensions)->push_back(op1_dimensions[i]);
		}
		int gen_length;
		gen_length = op1_length + op2_length;
		dimension_range_static new_range((gen_length - 1), (unsigned int)0);
		(*gen_dimensions)->push_front(new_range);

	} else if ((opcode == "/") || (opcode == "&") || (opcode == "|") || (opcode == "^")) {
		if (op1_dimensions.size() != op2_dimensions.size()) return 1;
		for (int i = 1; i < op1_dimensions.size(); i++)
		{
			if (DimensionsEqual(op1_dimensions[i], op2_dimensions[i]) == false) return 1;
			else (*gen_dimensions)->push_back(op1_dimensions[i]);
		}
		int gen_length;
		gen_length = max_length;
		dimension_range_static new_range((gen_length - 1), (unsigned int)0);
		(*gen_dimensions)->push_front(new_range);

	} else if ((opcode == "&&") || (opcode == "||") || (opcode == "==") || (opcode == "!=")) {
		if ((op1_dimensions.size() != 1) || (op2_dimensions.size() != 1)) return 1;
		dimension_range_static new_range((unsigned int)0, (unsigned int)0);
		(*gen_dimensions)->push_front(new_range);

	} else if ((opcode == "<<") || (opcode == ">>") || (opcode == ">>>")) {
		if (op2_dimensions.size() != 1) return 1;
		for (int i = 0; i < op1_dimensions.size(); i++)
		{
			(*gen_dimensions)->push_back(op1_dimensions[i]);
		}

	} else if (opcode == "indexed") {
		if (op2_dimensions.size() > 1) return 1;
		if (op1_dimensions.size() > 1) {
			for (int i = 1; i < op1_dimensions.size(); i++)
			{
				(*gen_dimensions)->push_back(op1_dimensions[i]);
			}
		} else {
			dimension_range_static new_range((unsigned int)0, (unsigned int)0);
			(*gen_dimensions)->push_front(new_range);
		}

	} else {
		printf("expr_2op_gen_dimensions: opcode unrecognized: %s\n", StringToCharArr(opcode));
		return 1;
	}
	return 0;
}

int expr_op_cmd_generated(unsigned int * cursor, ac_execode** new_expr, std::string opcode, std::vector<ac_param> params, ac_var * target)
{
	(*new_expr) = new ac_execode(opcode);
	for (unsigned int i = 0; i < params.size(); i++)
	{
		(*new_expr)->AddRdParamWithStack(params[i]);
	}
	(*new_expr)->AddWrVarWithStack(target);
	ExeStack[ExeStack.size()-1]->AddExpr(cursor, *new_expr);
	return 0;
}

int expr_op_cmd_generated(ac_execode** new_expr, std::string opcode, std::vector<ac_param> params, ac_var * target)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_op_cmd_generated(&cursor, new_expr, opcode, params, target);
}

int expr_op_cmd(unsigned int * cursor, std::string opcode, std::vector<ac_param> params, ac_var** respvar)
{
	if ((opcode == "~") || (opcode == "!")) {
		if (params.size() != 1)
		{
			printf("ActiveCore ERROR: params incorrect for operation %s - number of params: %d!\n", StringToCharArr(opcode), params.size());
			return 1;
		}
		ac_dimensions_static genvar_dimensions;
		if (opcode == "~") genvar_dimensions = params[0].GetDimensions();
		else genvar_dimensions.push_back(dimension_range_static(0, 0));

		ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), genvar_dimensions, "0");

		ac_execode* new_expr;
		if (expr_op_cmd_generated(cursor, &new_expr, opcode, params, genvar) != 0) return 1;
		new_expr->AddGenVarWithStack(genvar);

		*respvar = genvar;
		return 0;

	} else if ((opcode == "+")
			|| (opcode == "-")
			|| (opcode == "*")
			|| (opcode == "/")
			|| (opcode == "&")
			|| (opcode == "|")
			|| (opcode == "^")
			|| (opcode == "==")
			|| (opcode == "!=")
			|| (opcode == "&&")
			|| (opcode == "||")
			|| (opcode == "<<")
			|| (opcode == ">>")
			|| (opcode == ">>>")
			|| (opcode == "indexed")) {
		if (params.size() != 2)
		{
			printf("ActiveCore ERROR: params incorrect for operation %s - number of params: %d!\n", StringToCharArr(opcode), params.size());
			return 1;
		}
		ac_dimensions_static * genvar_dimensions;
		if (expr_2op_gen_dimensions(opcode, params[0].GetDimensions(), params[1].GetDimensions(), &genvar_dimensions) != 0)
		{
			printf ("Operand range error!\n");
			return 1;
		}
		ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), *genvar_dimensions, "0");

		ac_execode* new_expr;
		if (expr_op_cmd_generated(cursor, &new_expr, opcode, params, genvar) != 0) return 1;
		new_expr->AddGenVarWithStack(genvar);

		*respvar = genvar;
		return 0;

	} else if (opcode == "ranged") {
		if (params.size() != 3)
		{
			printf("ActiveCore ERROR: params incorrect for operation %s - number of params: %d!\n", StringToCharArr(opcode), params.size());
			return 1;
		}
		if ((params[0].GetDimensions().size() != 1) || (params[1].GetDimensions().size() != 1) || (params[2].GetDimensions().size() != 1))
		{
			printf("ActiveCore ERROR: params incorrect for operation %s!\n", StringToCharArr(opcode));
			return 1;
		}

		ac_dimensions_static genvar_dimensions;
		if ((params[1].type == PARAM_TYPE_VAL) && (params[2].type == PARAM_TYPE_VAL))
		{
			unsigned int msb = conv_string_to_int(params[1].imm->value);
			unsigned int lsb = conv_string_to_int(params[2].imm->value);
			genvar_dimensions.push_back(dimension_range_static(msb, lsb));
		} else {
			genvar_dimensions = params[0].GetDimensions();
		}
		ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), genvar_dimensions, "0");

		ac_execode* new_expr;
		if (expr_op_cmd_generated(cursor, &new_expr, opcode, params, genvar) != 0) return 1;
		new_expr->AddGenVarWithStack(genvar);

		*respvar = genvar;
		return 0;

	} else if (opcode == "cnct") {
		if (params.size() == 0)
		{
			printf("ActiveCore ERROR: params incorrect for operation %s - number of params: %d!\n", StringToCharArr(opcode), params.size());
			return 1;
		}

		unsigned int width = 0;
		for (unsigned int i = 0; i < params.size(); i++)
		{
			if (params[i].GetDimensions().size() != 1)
			{
				printf("ActiveCore ERROR: params incorrect for operation %s\n", StringToCharArr(opcode));
				return 1;
			}
			width += params[i].GetDimensions().at(0).GetWidth();
		}

		ac_dimensions_static genvar_dimensions;
		genvar_dimensions.push_back(dimension_range_static(width-1, 0));
		ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), genvar_dimensions, "0");

		ac_execode* new_expr;
		if (expr_op_cmd_generated(cursor, &new_expr, opcode, params, genvar) != 0) return 1;
		new_expr->AddGenVarWithStack(genvar);

		*respvar = genvar;
		return 0;

	} else {
		printf("ActiveCore ERROR: incorrect operation: %s\n", StringToCharArr(opcode));
		return 1;
	}
	return 0;
}

int expr_op_cmd(std::string opcode, std::vector<ac_param> params, ac_var** respvar)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_op_cmd(&cursor, opcode, params, respvar);
}

int expr_1op_cmd(std::string opcode, ac_param param, ac_var** respvar)
{
	std::vector<ac_param> params;
	params.push_back(param);
	return expr_op_cmd(opcode, params, respvar);
}

int expr_2op_cmd(std::string opcode, ac_param param0, ac_param param1, ac_var** respvar)
{
	std::vector<ac_param> params;
	params.push_back(param0);
	params.push_back(param1);
	return expr_op_cmd(opcode, params, respvar);
}

int expr_zeroext_cmd(unsigned int * cursor, unsigned int target_width, ac_param param, ac_var** respvar)
{
	ac_dimensions_static source_dimensions = param.GetDimensions();
	unsigned int source_width;
	if (source_dimensions.size() > 1) {
		printf("ActiveCore ERROR: zeroext operand dimensions error\n");
		return 1;
	} else {
		source_width = source_dimensions[0].GetWidth();
	}

	ac_dimensions_static genvar_dimensions;
	genvar_dimensions.push_back(dimension_range_static(target_width-1, 0));
	ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), genvar_dimensions, "0");

	ac_execode* new_expr;
	std::vector<ac_param> new_params;
	if (target_width > source_width) {
		ac_imm * zeroconst = new ac_imm((target_width - source_width - 1), 0, NumberToString(0));
		ac_param zeroconst_param(zeroconst);
		new_params.push_back(zeroconst_param);
		new_params.push_back(param);
		if (expr_op_cmd_generated(cursor, &new_expr, "cnct", new_params, genvar) != 0) return 1;
	} else if (target_width < source_width) {
		new_params.push_back(param);
		ac_imm * msb_imm = new ac_imm(NumberToString(target_width-1));
		ac_param msb(msb_imm);
		new_params.push_back(msb);
		ac_imm * lsb_imm = new ac_imm(NumberToString(0));
		ac_param lsb(lsb_imm);
		new_params.push_back(lsb);
		if (expr_op_cmd_generated(cursor, &new_expr, "ranged", new_params, genvar) != 0) return 1;
	} else {
		if (expr_assign_cmd_generated(cursor, &new_expr, ac_dimensions(), genvar, param) != 0) return 1;
	}

	new_expr->AddGenVarWithStack(genvar);
	*respvar = genvar;
	return 0;
}

int expr_zeroext_cmd(unsigned int target_width, ac_param param, ac_var** respvar)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_zeroext_cmd(&cursor, target_width, param, respvar);
}

int expr_signext_cmd(unsigned int * cursor, unsigned int target_width, ac_param param, ac_var** respvar)
{
	ac_dimensions_static source_dimensions = param.GetDimensions();
	unsigned int source_width;
	if (source_dimensions.size() > 1) {
		printf("ActiveCore ERROR: zeroext operand dimensions error\n");
		return 1;
	} else {
		source_width = source_dimensions[0].GetWidth();
	}

	ac_dimensions_static genvar_dimensions;
	genvar_dimensions.push_back(dimension_range_static(target_width-1, 0));
	ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), genvar_dimensions, "0");

	ac_execode* new_expr;
	std::vector<ac_param> new_params;
	if (target_width > source_width) {
		ac_var* signvar;

		std::vector<ac_param> inter_params;
		inter_params.push_back(param);
		ac_imm * sign_imm = new ac_imm(NumberToString(source_width-1));
		inter_params.push_back(ac_param(sign_imm));
		if (expr_op_cmd(cursor, "indexed", inter_params, &signvar) != 0) return 1;

		for (unsigned int i = 0; i < (target_width - source_width); i++) new_params.push_back(ac_param(signvar));

		new_params.push_back(param);
		if (expr_op_cmd_generated(cursor, &new_expr, "cnct", new_params, genvar) != 0) return 1;
	} else if (target_width < source_width) {
		new_params.push_back(param);
		ac_imm * msb_imm = new ac_imm(NumberToString(target_width-1));
		ac_param msb(msb_imm);
		new_params.push_back(msb);
		ac_imm * lsb_imm = new ac_imm(NumberToString(0));
		ac_param lsb(lsb_imm);
		new_params.push_back(lsb);
		if (expr_op_cmd_generated(cursor, &new_expr, "ranged", new_params, genvar) != 0) return 1;
	} else {
		if (expr_assign_cmd_generated(cursor, &new_expr, ac_dimensions(), genvar, param) != 0) return 1;
	}

	new_expr->AddGenVarWithStack(genvar);
	*respvar = genvar;
	return 0;
}

int expr_signext_cmd(unsigned int target_width, ac_param param, ac_var** respvar)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_signext_cmd(&cursor, target_width, param, respvar);
}

int expr_initval_cmd(unsigned int width, std::string value, ac_var** respvar)
{
	ac_dimensions_static new_dimension;
	dimension_range_static new_range(width-1, 0);
	new_dimension.push_back(new_range);

	ac_var* gen_var = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), new_dimension, "0");

	ac_execode* new_expr = new ac_execode("initval");
	// TODO: initval command
	*respvar = gen_var;
	return 0;
}

int expr_begif_cmd(ac_var* cond_op)
{
	ac_execode* new_expr = new ac_execode("if");
	new_expr->AddRdVar(cond_op);

	ExeStack[ExeStack.size()-1]->AddExpr(new_expr);
	ExeStack.push_back(new_expr);

	return 0;
}

int expr_begelsif_cmd(ac_var* cond_op)
{
	bool preif_found = false;
	ac_var* preif_cond;
	for (int i = (ExeStack[ExeStack.size()-1]->expressions.size() - 1); i > (-1); i--)
	{
		if (ExeStack[ExeStack.size()-1]->expressions[i]->opcode == "if")
		{
			preif_found = true;
			preif_cond = ExeStack[ExeStack.size()-1]->expressions[i]->rdvars[0];
			break;
		}
	}

	if (preif_found == false)
	{
		printf("ActiveCore ERROR: begelsif not preceded by begif!");
		return 1;
	}

	ac_var* preif_ncond;
	std::vector<ac_param> preif_cond_params;
	preif_cond_params.push_back(ac_param(preif_cond));
	if (expr_op_cmd("!", preif_cond_params, &preif_ncond) != 0) return 1;

	ac_var* curif_cond;
	std::vector<ac_param> curif_cond_params;
	curif_cond_params.push_back(ac_param(preif_ncond));
	curif_cond_params.push_back(ac_param(cond_op));
	if (expr_op_cmd("&&", curif_cond_params, &curif_cond) != 0) return 1;

	ac_execode* new_expr = new ac_execode("if");
	new_expr->AddRdVar(curif_cond);

	for (unsigned int i = 0; i < ExeStack[ExeStack.size()-1]->wrvars.size(); i++)
	{
		AddIfTargetVarToStack(ExeStack[ExeStack.size()-1]->wrvars[i]);
	}

	ExeStack[ExeStack.size()-1]->AddExpr(new_expr);
	ExeStack.push_back(new_expr);

	return 0;
}

int expr_begelse_cmd()
{
	bool preif_found = false;
	ac_var* preif_cond;
	for (int i = (ExeStack[ExeStack.size()-1]->expressions.size() - 1); i > (-1); i--)
	{
		if (ExeStack[ExeStack.size()-1]->expressions[i]->opcode == "if")
		{
			preif_found = true;
			preif_cond = ExeStack[ExeStack.size()-1]->expressions[i]->rdvars[0];
			break;
		}
	}

	if (preif_found == false)
	{
		printf("ActiveCore ERROR: begelse not preceded by begif!\n");
		return 1;
	}

	ac_var* preif_ncond;
	std::vector<ac_param> preif_ncond_params;
	preif_ncond_params.push_back(ac_param(preif_cond));
	if (expr_op_cmd("!", preif_ncond_params, &preif_ncond) != 0) return 1;

	ac_execode* new_expr = new ac_execode("if");
	new_expr->AddRdVar(preif_ncond);

	for (unsigned int i = 0; i < ExeStack[ExeStack.size()-1]->wrvars.size(); i++)
	{
		AddIfTargetVarToStack(ExeStack[ExeStack.size()-1]->wrvars[i]);
	}

	ExeStack[ExeStack.size()-1]->AddExpr(new_expr);
	ExeStack.push_back(new_expr);

	return 0;
}

int expr_endif_cmd()
{
	for (unsigned int i = 0; i < ExeStack[ExeStack.size()-1]->wrvars.size(); i++)
	{
		AddIfTargetVarToStack(ExeStack[ExeStack.size()-1]->wrvars[i]);
	}
	ExeStack.pop_back();
	return 0;
}

int expr_begwhile_cmd(ac_var* cond_op)
{
	ac_execode* new_expr = new ac_execode("while");
	new_expr->AddRdVar(cond_op);

	ExeStack[ExeStack.size()-1]->AddExpr(new_expr);
	ExeStack.push_back(new_expr);

	return 0;
}

int expr_endwhile_cmd()
{
	ExeStack.pop_back();
	return 0;
}

