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

int expr_assign_cmd_generated(unsigned int * cursor, ac_execode** new_expr, VarSegmentVector VarSegments, ac_var* target, ac_param* param)
{
	(*new_expr) = new ac_execode(OP1_ASSIGN);
	(*new_expr)->AddRdParamWithStack(param);
	(*new_expr)->AddWrVarWithStack(target);
	(*new_expr)->VarSegments = VarSegments;
	ExeStack[ExeStack.size()-1]->AddExpr(cursor, *new_expr);
	return 0;
}

int expr_assign_cmd_generated(ac_execode** new_expr, VarSegmentVector VarSegments, ac_var* target, ac_param* param)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_assign_cmd_generated(&cursor, new_expr, VarSegments, target, param);
}

int expr_assign_cmd_generated(unsigned int * cursor, VarSegmentVector VarSegments, ac_var* target, ac_param* param)
{
	ac_execode* new_expr;
	return expr_assign_cmd_generated(cursor, &new_expr, VarSegments, target, param);
}

int expr_assign_cmd_generated(VarSegmentVector VarSegments, ac_var* target, ac_param* param)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	ac_execode* new_expr;
	return expr_assign_cmd_generated(&cursor, &new_expr, VarSegments, target, param);
}

int expr_assign_cmd_generated(unsigned int * cursor, ac_var* target, ac_param* param)
{
	VarSegmentVector VarSegments;
	return expr_assign_cmd_generated(cursor, VarSegments, target, param);
}

int expr_assign_cmd_generated(ac_var* target, ac_param* param)
{
	VarSegmentVector VarSegments;
	return expr_assign_cmd_generated(VarSegments, target, param);
}

int expr_assign_cmd(unsigned int * cursor, VarSegmentVector VarSegmentsDePower, ac_var* target, ac_param* param)
{
	if (DEBUG_FLAG) printf("expr_assign_cmd: start: target: %s, source: %s\n", StringToCharArr(target->name), StringToCharArr(param->GetString()));

	std::string dimstring = "";
	if (VarSegmentsDePower.GetDimensionsString(&dimstring) != 0) return 1;
	if (DEBUG_FLAG) printf("expr_assign_var_cmd: dimensions obtained: %s\n", StringToCharArr(dimstring));

	int ret_val = 1;

	if (DEBUG_FLAG) {
		if (param->type == PARAM_TYPE::VAR)
		{
			if (((ac_var*)param)->vartype.type == VAR_TYPE::vt_structured)
			{
				printf("param structured, struct name: %s\n", StringToCharArr(((ac_var*)param)->vartype.src_struct->name));
			}
		}
	}

	VarType vartypeDepowered;
	ac_dimensions_static TargetDepoweredDimensions;
	if (target->GetDePowered(&TargetDepoweredDimensions, &vartypeDepowered, &VarSegmentsDePower) != 0) return 1;

	if (DEBUG_FLAG) {
		printf("TargetDimensions size: %d\n", TargetDepoweredDimensions.size());
		TargetDepoweredDimensions.PrintDimensions();
	}

	unsigned int targetDePowered;
	targetDePowered = TargetDepoweredDimensions.size();
	if (targetDePowered < 1) targetDePowered = 1;		// for 1-bit signals

	unsigned int paramPower = param->GetDimensions().size();
	if (paramPower < 1) paramPower = 1;

	if (DEBUG_FLAG) {
		printf("targetDePowered: %d\n", targetDePowered);
		printf("paramPower: %d\n", paramPower);
		if (target->vartype.type == VAR_TYPE::vt_structured) {
			printf("target (raw) structured\n");
		} else {
			printf("target (raw) not structured\n");
		}
		if (vartypeDepowered.type == VAR_TYPE::vt_structured) {
			printf("target (translated) structured\n");
		} else {
			printf("target (translated) not structured\n");
		}
		if (((ac_var*)param)->vartype.type == VAR_TYPE::vt_structured) {
			printf("param structured\n");
		} else {
			printf("param not structured\n");
		}
	}

	if ((targetDePowered == 1) && (paramPower == 1) && (param->type == PARAM_TYPE::VAR))
	{
		if  ( ( (((ac_var*)param)->vartype.type == VAR_TYPE::vt_structured) && (vartypeDepowered.type != VAR_TYPE::vt_structured) )
				|| ( (((ac_var*)param)->vartype.type != VAR_TYPE::vt_structured) && (vartypeDepowered.type == VAR_TYPE::vt_structured) ) ) {
			printf("ERROR: assignment between structured and non-structured variables! target: %s, source: %s\n", StringToCharArr(target->name), StringToCharArr(((ac_var*)param)->name));
			return 1;
		} else if ( (((ac_var*)param)->vartype.type == VAR_TYPE::vt_structured) && (vartypeDepowered.src_struct) ) {
			// assignment of 1-bit structs
			if (((ac_var*)param)->vartype.src_struct != vartypeDepowered.src_struct) {
				// assignment of inequally structured variables
				printf("ERROR: assignment of inequally structured variables! target: %s, source: %s\n", StringToCharArr(target->name), StringToCharArr(((ac_var*)param)->name));
				return 1;
			}
		}
	}

	if ((param->type == PARAM_TYPE::VAR) && (targetDePowered != paramPower)) {
		printf("ERROR: dimensions do not match for target %s, param: %s!\n", StringToCharArr(target->name), StringToCharArr(param->GetString()));
		return 1;
	}
	else if (targetDePowered == 1) ret_val = expr_assign_cmd_generated(cursor, VarSegmentsDePower, target, param);
	else if (param->type == PARAM_TYPE::VAR) {
		for (unsigned int i = 0; i < target->dimensions.back().GetWidth(); i++)
		{
			VarSegmentsDePower.push_front(VarSegment(i + target->dimensions.back().lsb));

			dimension_range_static * new_range_static = new dimension_range_static(i-1, 0);
			ac_dimensions_static gen_dimensions;
			gen_dimensions.push_back(*new_range_static);

			std::vector<ac_param*> ac_params_new;
			ac_params_new.push_back(param);
			ac_imm * new_imm = new ac_imm(NumToString(i + target->dimensions[targetDePowered-1].lsb));
			ac_params_new.push_back(new_imm);

			ac_var * new_source;
			if (expr_op_cmd(cursor, OP2_INDEXED, (&new_source), ac_params_new) != 0) return 1;

			ret_val = expr_assign_cmd(cursor, VarSegmentsDePower, target, new_source);
			VarSegmentsDePower.pop_front();
			if (ret_val != 0) return ret_val;
		}
		return ret_val;
	} else if (param->type == PARAM_TYPE::VAL) {
		if (targetDePowered > 0)
		{
			for (unsigned int i = 0; i < target->dimensions[targetDePowered-1].GetWidth(); i++)
			{
				VarSegmentsDePower.push_front(VarSegment(i + target->dimensions.back().lsb));
				ret_val = expr_assign_cmd(cursor, VarSegmentsDePower, target, param);
				VarSegmentsDePower.pop_front();
			}
		} else {
			printf("ERROR: dimensions are incorrect!\n");
			return 1;
		}
	} else {
		printf("ERROR: dimensions are incorrect!\n");
		return 1;
	}
	return ret_val;
}

int expr_assign_cmd(VarSegmentVector VarSegments, ac_var* target, ac_param* param)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_assign_cmd(&cursor, VarSegments, target, param);
}

int expr_assign_cmd(unsigned int * cursor, ac_var* target, ac_param* param)
{
	VarSegmentVector VarSegments;
	return expr_assign_cmd(cursor, VarSegments, target, param);
}

int expr_assign_cmd(ac_var* target, ac_param* param)
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

bool DimensionsEqual(VarSegment dim0, VarSegment dim1)
{
	if (dim0.type != dim1.type) return false;
	switch (dim0.type)
	{
		case SegType::C:
			if (dim0.msb_int != dim1.msb_int) return false;

		case SegType::V:
			if (dim0.msb_var != dim1.msb_var) return false;

		case SegType::CC:
			if ((dim0.msb_int != dim1.msb_int) || (dim0.lsb_int != dim1.lsb_int)) return false;

		case SegType::CV:
			if ((dim0.msb_int != dim1.msb_int) || (dim0.lsb_var != dim1.lsb_var)) return false;

		case SegType::VC:
			if ((dim0.msb_var != dim1.msb_var) || (dim0.lsb_int != dim1.lsb_int)) return false;

		case SegType::VV:
			if ((dim0.msb_var != dim1.msb_var) || (dim0.lsb_var != dim1.lsb_var)) return false;

		case SegType::SubStruct:
			if ((dim0.src_struct != dim1.src_struct)) return false;

		default:
			return false;
	}
	return true;
}

int expr_2op_gen_dimensions(char * opcode, ac_dimensions_static op1_dimensions, ac_dimensions_static op2_dimensions, ac_dimensions_static ** gen_dimensions)
{
	dimension_range_static op1_primary_dimension = op1_dimensions[0];
	dimension_range_static op2_primary_dimension = op2_dimensions[0];

	unsigned int op1_length, op2_length, max_length;

	op1_length = op1_primary_dimension.GetWidth();
	op2_length = op2_primary_dimension.GetWidth();

	if (op1_length > op2_length) max_length = op1_length;
	else max_length = op2_length;

	(*gen_dimensions) = new ac_dimensions_static();
	if ((opcode == OP2_ARITH_ADD) || (opcode == OP2_ARITH_SUB)) {
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

	} else if (opcode == OP2_ARITH_MUL) {
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

	} else if ((opcode == OP2_ARITH_DIV) || (opcode == OP2_BITWISE_AND) || (opcode == OP2_BITWISE_OR) || (opcode == OP2_BITWISE_XOR) || (opcode == OP2_BITWISE_XNOR)) {
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

	} else if ((opcode == OP2_LOGICAL_AND)
			|| (opcode == OP2_LOGICAL_OR)
			|| (opcode == OP2_LOGICAL_G)
			|| (opcode == OP2_LOGICAL_L)
			|| (opcode == OP2_LOGICAL_GEQ)
			|| (opcode == OP2_LOGICAL_LEQ)
			|| (opcode == OP2_LOGICAL_EQ2)
			|| (opcode == OP2_LOGICAL_NEQ2)
			|| (opcode == OP2_LOGICAL_EQ4)
			|| (opcode == OP2_LOGICAL_NEQ4)) {
		if ((op1_dimensions.size() != 1) || (op2_dimensions.size() != 1)) return 1;
		dimension_range_static new_range((unsigned int)0, (unsigned int)0);
		(*gen_dimensions)->push_front(new_range);

	} else if ((opcode == OP2_ARITH_SHL) || (opcode == OP2_ARITH_SHR) || (opcode == OP2_ARITH_SRA)) {
		if (op2_dimensions.size() != 1) return 1;
		for (int i = 0; i < op1_dimensions.size(); i++)
		{
			(*gen_dimensions)->push_back(op1_dimensions[i]);
		}

	} else if (opcode == OP2_INDEXED) {
		if (op2_dimensions.size() > 1) return 1;
		if (op1_dimensions.size() > 1) {
			for (int i = 0; i < (op1_dimensions.size() - 1); i++)
			{
				(*gen_dimensions)->push_back(op1_dimensions[i]);
			}
		} else {
			dimension_range_static new_range((unsigned int)0, (unsigned int)0);
			(*gen_dimensions)->push_front(new_range);
		}

	} else {
		printf("expr_2op_gen_dimensions: opcode unrecognized: %s\n", opcode);
		return 1;
	}
	//printf("dimensions generated: %d\n", (*gen_dimensions)->size());
	return 0;
}

int expr_op_cmd_generated(unsigned int * cursor, ac_execode** new_expr, char * opcode, ac_var * target, std::vector<ac_param*> params)
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

int expr_op_cmd_generated(ac_execode** new_expr, char * opcode, ac_var * target, std::vector<ac_param*> params)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_op_cmd_generated(&cursor, new_expr, opcode, target, params);
}

int expr_op_cmd(unsigned int * cursor, char * opcode, ac_var** respvar, std::vector<ac_param*> params)
{
	if ((opcode == OP1_BITWISE_NOT)
			|| (opcode == OP1_LOGICAL_NOT)
			|| (opcode == OP1_COMPLEMENT)
			|| (opcode == OP1_REDUCT_AND)
			|| (opcode == OP1_REDUCT_NAND)
			|| (opcode == OP1_REDUCT_OR)
			|| (opcode == OP1_REDUCT_NOR)
			|| (opcode == OP1_REDUCT_XOR)
			|| (opcode == OP1_REDUCT_XNOR)) {
		if (params.size() != 1)
		{
			printf("ActiveCore ERROR: params incorrect for operation %s - number of params: %d!\n", opcode, params.size());
			return 1;
		}
		ac_dimensions_static genvar_dimensions;
		if ((opcode == OP1_BITWISE_NOT) || (opcode == OP1_COMPLEMENT)) genvar_dimensions = params[0]->GetDimensions();
		else if (opcode == OP1_LOGICAL_NOT) genvar_dimensions.push_back(dimension_range_static(0, 0));
		else {
			// TODO: vectored operations
			if (params[0]->GetDimensions().size() != 1)
			{
				printf("ERROR: Reduct param dimensions error!\n");
				return 1;
			}
			genvar_dimensions = params[0]->GetDimensions();
			genvar_dimensions.pop_front();
			genvar_dimensions.push_front(dimension_range_static(0, 0));
		}

		VarType curVarType;
		if (params[0]->type == PARAM_TYPE::VAL) curVarType = ((ac_var*)params[0])->vartype;
		else curVarType = VarType(VAR_TYPE::vt_unsigned);

		ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), curVarType, genvar_dimensions, "0");

		ac_execode* new_expr;
		if (expr_op_cmd_generated(cursor, &new_expr, opcode, genvar, params) != 0) return 1;
		new_expr->AddGenVarWithStack(genvar);

		*respvar = genvar;
		return 0;

	} else if ((opcode == OP2_ARITH_ADD)
			|| (opcode == OP2_ARITH_SUB)
			|| (opcode == OP2_ARITH_MUL)
			|| (opcode == OP2_ARITH_DIV)
			|| (opcode == OP2_ARITH_SHL)
			|| (opcode == OP2_ARITH_SHR)
			|| (opcode == OP2_ARITH_SRA)

			|| (opcode == OP2_LOGICAL_AND)
			|| (opcode == OP2_LOGICAL_OR)
			|| (opcode == OP2_LOGICAL_G)
			|| (opcode == OP2_LOGICAL_L)
			|| (opcode == OP2_LOGICAL_GEQ)
			|| (opcode == OP2_LOGICAL_LEQ)
			|| (opcode == OP2_LOGICAL_EQ2)
			|| (opcode == OP2_LOGICAL_NEQ2)
			|| (opcode == OP2_LOGICAL_EQ4)
			|| (opcode == OP2_LOGICAL_NEQ4)

			|| (opcode == OP2_BITWISE_AND)
			|| (opcode == OP2_BITWISE_OR)
			|| (opcode == OP2_BITWISE_XOR)
			|| (opcode == OP2_BITWISE_XNOR)

			|| (opcode == OP2_INDEXED)) {
		if (params.size() != 2)
		{
			printf("ActiveCore ERROR: params incorrect for operation %s - number of params: %d!\n", opcode, params.size());
			return 1;
		}
		ac_dimensions_static * genvar_dimensions;
		if (expr_2op_gen_dimensions(opcode, params[0]->GetDimensions(), params[1]->GetDimensions(), &genvar_dimensions) != 0)
		{
			printf ("Operand range error!\n");
			return 1;
		}

		VarType curVarType = VarType(VAR_TYPE::vt_unsigned);
		if ((params[0]->type == PARAM_TYPE::VAL) && (params[1]->type == PARAM_TYPE::VAL))
		{
			if ( (((ac_var*)(params[0]))->vartype.type == VAR_TYPE::vt_signed) && (((ac_var*)(params[1]))->vartype.type == VAR_TYPE::vt_signed) ) {
				curVarType = VarType(VAR_TYPE::vt_signed);
			}
		}

		if (params[0]->type == PARAM_TYPE::VAL) curVarType = ((ac_var*)params[0])->vartype;
		else curVarType = VarType(VAR_TYPE::vt_unsigned);

		ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), curVarType, *genvar_dimensions, "0");

		ac_execode* new_expr;
		if (expr_op_cmd_generated(cursor, &new_expr, opcode, genvar, params) != 0) return 1;
		new_expr->AddGenVarWithStack(genvar);

		*respvar = genvar;
		return 0;

	} else if (opcode == OP3_RANGED) {
		if (params.size() != 3)
		{
			printf("ActiveCore ERROR: params incorrect for operation %s - number of params: %d!\n", StringToCharArr(opcode), params.size());
			return 1;
		}
		if ((params[0]->GetDimensions().size() != 1) || (params[1]->GetDimensions().size() != 1) || (params[2]->GetDimensions().size() != 1))
		{
			printf("ActiveCore ERROR: params incorrect for operation %s!\n", StringToCharArr(opcode));
			return 1;
		}

		ac_dimensions_static genvar_dimensions;
		if ((params[1]->type == PARAM_TYPE::VAL) && (params[2]->type == PARAM_TYPE::VAL))
		{
			unsigned int msb = conv_string_to_int(((ac_imm*)(params[1]))->value);
			unsigned int lsb = conv_string_to_int(((ac_imm*)(params[2]))->value);
			genvar_dimensions.push_back(dimension_range_static(msb, lsb));
		} else {
			genvar_dimensions = params[0]->GetDimensions();
		}

		ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), VarType(VAR_TYPE::vt_unsigned), genvar_dimensions, "0");

		ac_execode* new_expr;
		if (expr_op_cmd_generated(cursor, &new_expr, opcode, genvar, params) != 0) return 1;
		new_expr->AddGenVarWithStack(genvar);

		*respvar = genvar;
		return 0;

	} else if (opcode == OPS_CNCT) {
		if (params.size() == 0)
		{
			printf("ActiveCore ERROR: params incorrect for operation %s - number of params: %d!\n", StringToCharArr(opcode), params.size());
			return 1;
		}

		unsigned int width = 0;
		for (unsigned int i = 0; i < params.size(); i++)
		{
			if (params[i]->GetDimensions().size() != 1)
			{
				printf("ActiveCore ERROR: params incorrect for operation %s\n", StringToCharArr(opcode));
				return 1;
			}
			width += params[i]->GetDimensions().at(0).GetWidth();
		}

		ac_dimensions_static genvar_dimensions;
		genvar_dimensions.push_back(dimension_range_static(width-1, 0));
		ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), VarType(VAR_TYPE::vt_unsigned), genvar_dimensions, "0");

		ac_execode* new_expr;
		if (expr_op_cmd_generated(cursor, &new_expr, opcode, genvar, params) != 0) return 1;
		new_expr->AddGenVarWithStack(genvar);

		*respvar = genvar;
		return 0;

	} else {
		printf("ActiveCore ERROR: incorrect operation: %s\n", StringToCharArr(opcode));
		return 1;
	}
	return 0;
}

int expr_op_cmd(char * opcode, ac_var** respvar, std::vector<ac_param*> params)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_op_cmd(&cursor, opcode, respvar, params);
}

int expr_1op_cmd(char * opcode, ac_var** respvar, ac_param* param)
{
	std::vector<ac_param*> params;
	params.push_back(param);
	return expr_op_cmd(opcode, respvar, params);
}

int expr_1op_cmd_generated(char * opcode, ac_var* target, ac_param* param)
{
	ac_execode* new_expr;
	std::vector<ac_param*> params;
	params.push_back(param);
	return expr_op_cmd_generated(&new_expr, opcode, target, params);
}

int expr_2op_cmd(char * opcode, ac_var** respvar, ac_param* param0, ac_param* param1)
{
	std::vector<ac_param*> params;
	params.push_back(param0);
	params.push_back(param1);
	return expr_op_cmd(opcode, respvar, params);
}

int expr_2op_cmd_generated(char * opcode, ac_var* target, ac_param* param0, ac_param* param1)
{
	ac_execode* new_expr;
	std::vector<ac_param*> params;
	params.push_back(param0);
	params.push_back(param1);
	return expr_op_cmd_generated(&new_expr, opcode, target, params);
}

int expr_zeroext_cmd(unsigned int * cursor, unsigned int target_width, ac_var** respvar, ac_param* param)
{
	ac_dimensions_static source_dimensions = param->GetDimensions();
	unsigned int source_width;
	if (source_dimensions.size() > 1) {
		printf("ActiveCore ERROR: zeroext operand dimensions error\n");
		return 1;
	} else {
		source_width = source_dimensions[0].GetWidth();
	}

	ac_dimensions_static genvar_dimensions;
	genvar_dimensions.push_back(dimension_range_static(target_width-1, 0));
	ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), VarType(VAR_TYPE::vt_unsigned), genvar_dimensions, "0");

	ac_execode* new_expr;
	std::vector<ac_param*> new_params;
	if (target_width > source_width) {
		ac_imm * zeroconst = new ac_imm((target_width - source_width - 1), 0, NumberToString(0));
		new_params.push_back(zeroconst);
		new_params.push_back(param);
		if (expr_op_cmd_generated(cursor, &new_expr, OPS_CNCT, genvar, new_params) != 0) return 1;
	} else if (target_width < source_width) {
		new_params.push_back(param);
		ac_imm * msb_imm = new ac_imm(NumberToString(target_width-1));
		new_params.push_back(msb_imm);
		ac_imm * lsb_imm = new ac_imm(NumberToString(0));
		new_params.push_back(lsb_imm);
		if (expr_op_cmd_generated(cursor, &new_expr, OP3_RANGED, genvar, new_params) != 0) return 1;
	} else {
		if (expr_assign_cmd_generated(cursor, &new_expr, VarSegmentVector(), genvar, param) != 0) return 1;
	}

	new_expr->AddGenVarWithStack(genvar);
	*respvar = genvar;
	return 0;
}

int expr_zeroext_cmd(unsigned int target_width, ac_var** respvar, ac_param* param)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_zeroext_cmd(&cursor, target_width, respvar, param);
}

int expr_signext_cmd(unsigned int * cursor, unsigned int target_width, ac_var** respvar, ac_param* param)
{
	ac_dimensions_static source_dimensions = param->GetDimensions();
	unsigned int source_width;
	if (source_dimensions.size() > 1) {
		printf("ActiveCore ERROR: signext operand dimensions error\n");
		return 1;
	} else {
		source_width = source_dimensions[0].GetWidth();
	}

	ac_dimensions_static genvar_dimensions;
	genvar_dimensions.push_back(dimension_range_static(target_width-1, 0));
	ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), VarType(VAR_TYPE::vt_signed), genvar_dimensions, "0");

	ac_execode* new_expr;
	std::vector<ac_param*> new_params;
	if (target_width > source_width) {
		ac_var* signvar;

		std::vector<ac_param*> inter_params;
		inter_params.push_back(param);
		ac_imm * sign_imm = new ac_imm(NumberToString(source_dimensions[0].msb));
		inter_params.push_back(sign_imm);
		if (expr_op_cmd(cursor, OP2_INDEXED, &signvar, inter_params) != 0) return 1;

		for (unsigned int i = 0; i < (target_width - source_width); i++) new_params.push_back(signvar);

		new_params.push_back(param);
		if (expr_op_cmd_generated(cursor, &new_expr, OPS_CNCT, genvar, new_params) != 0) return 1;
	} else if (target_width < source_width) {
		new_params.push_back(param);
		ac_imm * msb_imm = new ac_imm(NumberToString(target_width-1));
		new_params.push_back(msb_imm);
		ac_imm * lsb_imm = new ac_imm(NumberToString(0));
		new_params.push_back(lsb_imm);
		if (expr_op_cmd_generated(cursor, &new_expr, OP3_RANGED, genvar, new_params) != 0) return 1;
	} else {
		if (expr_assign_cmd_generated(cursor, &new_expr, VarSegmentVector(), genvar, param) != 0) return 1;
	}

	new_expr->AddGenVarWithStack(genvar);
	*respvar = genvar;
	return 0;
}

int expr_signext_cmd(unsigned int target_width, ac_var** respvar, ac_param* param)
{
	unsigned int cursor = ExeStack[ExeStack.size()-1]->expressions.size();
	return expr_signext_cmd(&cursor, target_width, respvar, param);
}

int expr_initval_cmd(ac_var** respvar, ac_dimensions_static dimensions, ac_param* param)
{
	ac_var* gen_var = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), VarType(VAR_TYPE::vt_unsigned), dimensions, "0");
	AddGenVarToStack(gen_var);

	if (expr_assign_cmd(gen_var, param) != 0) return 1;
	*respvar = gen_var;
	return 0;
}

int expr_clrif_cmd()
{
	ExeStack[ExeStack.size()-1]->priority_conditions.clear();
	return 0;
}

int expr_begif_cmd(ac_param* cond)
{
	ExeStack[ExeStack.size()-1]->priority_conditions.clear();

	ac_execode* new_expr = new ac_execode(OP1_IF);

	if (cond->type == PARAM_TYPE::VAR) {
		ac_var* genvar = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), VAR_TYPE::vt_unsigned, 0, 0, "0");
		new_expr->AddGenVarWithStack(genvar);
		if (expr_assign_cmd_generated(genvar, cond) != 0) return 1;
		new_expr->AddRdParam(genvar);
		ExeStack[ExeStack.size()-1]->priority_conditions.push_back(genvar);
	} else {
		new_expr->AddRdParam(cond);
		ExeStack[ExeStack.size()-1]->priority_conditions.push_back(cond);
	}

	ExeStack[ExeStack.size()-1]->AddExpr(new_expr);
	ExeStack.push_back(new_expr);

	return 0;
}

int expr_begifnot_cmd(ac_param* cond)
{
	ac_var* genvar;
	if (expr_1op_cmd(OP1_LOGICAL_NOT, &genvar, cond) != 0) return 1;
	return expr_begif_cmd(genvar);
}

int expr_begelsif_cmd(ac_param* cond)
{
	ac_var* curif_cond = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");

	// summing previous conditions - some of previous occurred
	if (expr_assign_cmd(curif_cond, new ac_imm(1, "0")) != 0) return 1;
	for (unsigned int i = 0; i < ExeStack[ExeStack.size()-1]->priority_conditions.size(); i++)
	{
		if (expr_2op_cmd_generated(OP2_LOGICAL_OR, curif_cond, curif_cond, ExeStack[ExeStack.size()-1]->priority_conditions[i]) != 0) return 1;
	}

	// inverting - none of previous occurred
	if (expr_1op_cmd_generated(OP1_LOGICAL_NOT, curif_cond, curif_cond) != 0) return 1;

	// final condition
	if (expr_2op_cmd_generated(OP2_LOGICAL_AND, curif_cond, curif_cond, cond) != 0) return 1;

	ac_execode* new_expr = new ac_execode(OP1_IF);
	new_expr->AddRdParam(curif_cond);
	new_expr->AddGenVarWithStack(curif_cond);

	ExeStack[ExeStack.size()-1]->priority_conditions.push_back(curif_cond);
	ExeStack[ExeStack.size()-1]->AddExpr(new_expr);
	ExeStack.push_back(new_expr);

	return 0;
}

int expr_begelse_cmd()
{
	ac_var* curif_cond = new ac_var(DEFAULT_TYPEVAR, GetGenName("var"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");

	// summing previous conditions - some of previous occurred
	if (expr_assign_cmd(curif_cond, new ac_imm(1, "0")) != 0) return 1;
	for (unsigned int i = 0; i < ExeStack[ExeStack.size()-1]->priority_conditions.size(); i++)
	{
		if (expr_2op_cmd_generated(OP2_LOGICAL_OR, curif_cond, curif_cond, ExeStack[ExeStack.size()-1]->priority_conditions[i]) != 0) return 1;
	}

	// inverting - none of previous occurred
	if (expr_1op_cmd_generated(OP1_LOGICAL_NOT, curif_cond, curif_cond) != 0) return 1;

	ac_execode* new_expr = new ac_execode(OP1_IF);
	new_expr->AddRdParam(curif_cond);
	new_expr->AddGenVarWithStack(curif_cond);

	ExeStack[ExeStack.size()-1]->priority_conditions.clear();
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

int expr_begwhile_cmd(ac_param* cond)
{
	ac_execode* new_expr = new ac_execode(OP1_WHILE);
	new_expr->AddRdParam(cond);

	ExeStack[ExeStack.size()-1]->AddExpr(new_expr);
	ExeStack.push_back(new_expr);

	return 0;
}

int expr_endwhile_cmd()
{
	ExeStack.pop_back();
	return 0;
}

