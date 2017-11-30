/*
 * ac_core.cpp
 *
 *  Created on: 30.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#include "ac_utils.hpp"
#include "ac_core.hpp"


template<typename T> std::string NumberToString ( T Number )
{
	std::stringstream ss;
	ss << Number;
	return ss.str();
}

bool DEBUG_FLAG = false;

// AST state
int GenCounter = 0;
std::deque<ac_execode*> ExeStack;
std::vector<std::vector<ac_var*>* > SignalsReadable;
std::vector<std::vector<ac_var*>* > SignalsWriteable;
ac_dimensions DimensionsAccumulator;
std::vector<ac_param> ParamAccumulator;
std::vector<std::string> StringParamAccumulator;
std::vector<int> IntParamAccumulator;
std::vector<unsigned int> UIntParamAccumulator;

char DEFAULT_TYPEVAR[] = "GENERATED";

void GenReset()
{
	GenCounter = 0;
}

std::string GetGenName(std::string name_in)
{
	return ("gen" + toString(GenCounter++) + "_" + name_in);
}

std::string NumToString(int number)
{
	return NumberToString(number);
	//return ("gen" + toString(GenCounter++) + "_");
}

ac_imm::ac_imm(ac_dimensions_static dimensions_in, std::string value_in)
{
	dimensions = dimensions_in;
	value = value_in;
}

ac_imm::ac_imm(std::string value_in)
{
	dimension_range_static new_range(31, 0);
	ac_dimensions_static * new_dimensions = new ac_dimensions_static();
	new_dimensions->push_back(new_range);
	dimensions = *new_dimensions;
	value = value_in;
}

ac_imm::ac_imm(unsigned int msb, unsigned int lsb, std::string value_in)
{
	dimension_range_static new_range(msb, lsb);
	ac_dimensions_static * new_dimensions = new ac_dimensions_static();
	new_dimensions->push_back(new_range);
	dimensions = *new_dimensions;
	value = value_in;
}

ac_param::ac_param(ac_var* var_in)
{
	type = PARAM_TYPE_VAR;
	var = var_in;
}

ac_param::ac_param(ac_imm* imm_in)
{
	type = PARAM_TYPE_VAL;
	imm = imm_in;
}

ac_param::ac_param(ac_dimensions_static dimensions_in, std::string value)
{
	ac_imm * new_imm = new ac_imm(dimensions_in, value);
	type = PARAM_TYPE_VAL;
	imm = new_imm;
}

ac_param::ac_param(unsigned int width, std::string value)
{
	ac_imm * new_imm = new ac_imm(width-1, 0, value);
	type = PARAM_TYPE_VAL;
	imm = new_imm;
}

std::string ac_param::GetString()
{
	if (type == PARAM_TYPE_VAR) return var->name;
	else return imm->value;
}

std::string ac_param::GetStringFull()
{
	if (type == PARAM_TYPE_VAR) return var->name;
	else return (NumberToString(imm->dimensions[0].GetWidth()) + "'d" + imm->value);
}

ac_dimensions_static ac_param::GetDimensions()
{
	if (type == PARAM_TYPE_VAR) return var->dimensions;
	else return imm->dimensions;
}

void dimension_range_static::PrintDimension()
{
	printf("Dimension range:\n");
	printf("msb: %d\n", msb);
	printf("lsb: %d\n", lsb);
}

void dimension_range::PrintDimension()
{
	switch (type)
	{
	case DimType_C:
		printf("Dimension range type: C\n");
		printf("msb: %d\n", msb_int);
		break;

	case DimType_V:
		printf("Dimension range type: V\n");
		printf("msb: %s\n", StringToCharArr(msb_var->name));
		break;

	case DimType_CC:
		printf("Dimension range type: CC\n");
		printf("msb: %d\n", msb_int);
		printf("lsb: %d\n", lsb_int);
		break;

	case DimType_CV:
		printf("Dimension range type: CV\n");
		printf("msb: %d\n", msb_int);
		printf("lsb: %s\n", StringToCharArr(lsb_var->name));
		break;

	case DimType_VC:
		printf("Dimension range type: VC\n");
		printf("msb: %s\n", StringToCharArr(msb_var->name));
		printf("lsb: %d\n", lsb_int);
		break;

	case DimType_VV:
		printf("Dimension range type: VV\n");
		printf("msb: %s\n", StringToCharArr(msb_var->name));
		printf("lsb: %s\n", StringToCharArr(lsb_var->name));
		break;

	default:
		printf("Dimension range type unrecognized: %d\n", type);
	}
}

void ac_dimensions::PrintDimensions()
{
	for (unsigned int i = 0; i < size(); i++)
	{
		at(i).PrintDimension();
	}
}

int ac_dimensions::GetPower(unsigned int * power)
{
	*power = 0;
	for (unsigned int i = 0; i < size(); i++)
	{
		if ((at(i).type == DimType_C) || (at(i).type == DimType_V)) (*power)++;
		else if ((at(i).type == DimType_CC) || (at(i).type == DimType_CV) || (at(i).type == DimType_VC) || (at(i).type == DimType_VV)) continue;
		else return 1;
	}
	return 0;
}

void ac_dimensions_static::PrintDimensions()
{
	for (unsigned int i = 0; i < size(); i++)
	{
		at(i).PrintDimension();
	}
}

int ac_dimensions_static::GetPower(unsigned int * power)
{
	*power = 0;
	for (unsigned int i = 0; i < size(); i++)
	{
		if (at(i).msb != at(i).lsb) continue;
		else (*power)++;
	}
	return 0;
}

int getdimstring(dimension_range_static * in_range, std::string * ret_val)
{
	*ret_val = ("[" + toString(in_range->msb) + ":" + toString(in_range->lsb) + "]");
	return 0;
}

int getdimstring(dimension_range * in_range, std::string * ret_val)
{
	switch (in_range->type)
	{
	case DimType_C:
		*ret_val = ("[" + toString(in_range->msb_int) + "]");
		return 0;

	case DimType_V:
		*ret_val = ("[" + in_range->msb_var->name + "]");
		return 0;

	case DimType_CC:
		*ret_val = ("[" + toString(in_range->msb_int) + ":" + toString(in_range->lsb_int) + "]");
		return 0;

	case DimType_CV:
		*ret_val = ("[" + toString(in_range->msb_int) + ":" + in_range->lsb_var->name + "]");
		return 0;

	case DimType_VC:
		*ret_val = ("[" + in_range->msb_var->name + ":" + toString(in_range->lsb_int) + "]");
		return 0;

	case DimType_VV:
		*ret_val = ("[" + in_range->msb_var->name + ":" + in_range->lsb_var->name + "]");
		return 0;

	default:
		printf("Dimension type not resolved!\n");
		return 1;
	}
}

int ac_dimensions::GetDimensionsString(std::string * string_in)
{
	//printf("GetDimensionsString: start, dimsize: %d\n", size());

	*string_in = "";
	for (int i = (size()-1); i > (-1); i--)
	{
		std::string range_string;
		if (at(i).GetString(&range_string) != 0) return 1;
		*string_in = ((*string_in) + range_string);
	}
	//printf("GetDimensionsString: string: \n", StringToCharArr(*string_in));
	//printf("GetDimensionsString: finish\n");
	return 0;
}

ac_var::ac_var(char * type_name_in, std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in)
{
	name = name_in;
	dimensions = dimensions_in;
	defval = defval_in;
	type_name = type_name_in;
	read_done = false;
	write_done = false;
}

ac_var::ac_var(char * type_name_in, std::string name_in, unsigned int msb, unsigned int lsb, std::string defval_in)
{
	name = name_in;
	dimension_range_static new_range(msb, lsb);
	dimensions = ac_dimensions_static();
	dimensions.push_back(new_range);
	defval = defval_in;
	type_name = type_name_in;
	read_done = false;
	write_done = false;
}

bool ac_execode::AddWrVar(ac_var* new_wrvar)
{
	bool signal_is_new = true;
	for (unsigned int i = 0; i < wrvars.size(); i += 1)
	{
		if (new_wrvar == wrvars[i])
		{
			signal_is_new = false;
			break;
		}
	}
	if (signal_is_new)
	{
		wrvars.push_back(new_wrvar);
		new_wrvar->write_done = true;
	}
	return signal_is_new;
}

bool ac_execode::AddRdVar(ac_var* new_rdvar)
{
	bool signal_is_new = true;
	for (unsigned int i = 0; i < rdvars.size(); i++)
	{
		if (new_rdvar == rdvars[i])
		{
			signal_is_new = false;
			break;
		}
	}
	if (signal_is_new)
	{
		rdvars.push_back(new_rdvar);
		new_rdvar->read_done = true;
	}
	return signal_is_new;
}

void ac_execode::AddGenVar(ac_var* new_genvar)
{
	genvars.push_back(new_genvar);
}

void ac_execode::AddRdParam(ac_param new_param)
{
	params.push_back(new_param);
	if (new_param.type == PARAM_TYPE_VAR) AddRdVar(new_param.var);
}

void ac_execode::AddRdParams(std::vector<ac_param> new_params)
{
	for (unsigned int i = 0; i < new_params.size(); i++)
	{
		AddRdParam(new_params[i]);
	}
}

bool ac_execode::AddIfTargetVar(ac_var* new_iftvar)
{
	bool signal_is_new = true;
	for (unsigned int i = 0; i < iftargets.size(); i++)
	{
		if (new_iftvar == iftargets[i])
		{
			signal_is_new = false;
			break;
		}
	}
	if (signal_is_new)
	{
		iftargets.push_back(new_iftvar);
	}
	return signal_is_new;
}

ac_execode::ac_execode(std::string opcode_in)
{
	opcode = opcode_in;
}

void ac_execode::AddExpr(unsigned int * cursor, ac_execode* new_expr)
{
	std::deque<ac_execode*>::iterator it = ExeStack[ExeStack.size()-1]->expressions.begin() + (*cursor);
	for (unsigned int i = 0; i < new_expr->wrvars.size(); i++)
	{
		AddWrVar(new_expr->wrvars[i]);
	}

	for (unsigned int i = 0; i < new_expr->rdvars.size(); i++)
	{
		AddRdVar(new_expr->rdvars[i]);
	}
	expressions.insert(it, new_expr);
	(*cursor)++;
}

void ac_execode::AddExpr(ac_execode* new_expr)
{
	for (unsigned int i = 0; i < new_expr->wrvars.size(); i++)
	{
		AddWrVar(new_expr->wrvars[i]);
	}

	for (unsigned int i = 0; i < new_expr->rdvars.size(); i++)
	{
		AddRdVar(new_expr->rdvars[i]);
	}
	expressions.push_back(new_expr);
}

void ac_execode::AddRdVarWithStack(ac_var* new_op)
{
	AddRdVar(new_op);
	for (unsigned int i = 0; i < ExeStack.size(); i++)
	{
		ExeStack[i]->AddRdVar(new_op);
	}
}

void ac_execode::AddWrVarWithStack(ac_var* new_op)
{
	AddWrVar(new_op);
	for (unsigned int i = 0; i < ExeStack.size(); i++)
	{
		ExeStack[i]->AddWrVar(new_op);
	}
}

void AddGenVarToStack(ac_var* new_op)
{
	for (unsigned int i = 0; i < ExeStack.size(); i++)
	{
		ExeStack[i]->AddGenVar(new_op);
	}
}

void ac_execode::AddGenVarWithStack(ac_var* new_op)
{
	AddGenVar(new_op);
	AddGenVarToStack(new_op);
}

void ac_execode::AddRdParamWithStack(ac_param new_param)
{
	AddRdParam(new_param);
	for (unsigned int i = 0; i < ExeStack.size(); i++)
	{
		ExeStack[i]->AddRdParam(new_param);
	}
}

void ac_execode::AddRdParamsWithStack(std::vector<ac_param> new_params)
{
	AddRdParams(new_params);
	for (unsigned int i = 0; i < ExeStack.size(); i++)
	{
		ExeStack[i]->AddRdParams(new_params);
	}
}

int GetVarFromList(std::string op, ac_var** ret_signal, std::vector<std::vector<ac_var*>* > SignalList)
{
	for (unsigned int i = 0; i < SignalList.size(); i++)
	{
		for (unsigned int j = 0; j < SignalList[i]->size(); j++)
		{
			if (SignalList[i]->at(j)->name == op)
			{
				*ret_signal = SignalList[i]->at(j);
				return 0;
			}
		}
	}
	return 1;
}

int SetVarReadable(std::string op, ac_var** ret_signal)
{
	//printf("SetVarReadable: name: %s\n", StringToCharArr(op));
	int ret_val = GetVarFromList(op, ret_signal, SignalsReadable);
	(*ret_signal)->read_done = true;
	//printf("SetVarReadable: ret_val: %d\n", ret_val);
	if (ret_val != 0) printf("CRITICAL: Variable %s not found!\n", StringToCharArr(op));
	return ret_val;
}

int SetVarWriteable(std::string op, ac_var** ret_signal)
{
	int ret_val = GetVarFromList(op, ret_signal, SignalsWriteable);
	(*ret_signal)->write_done = true;
	if (ret_val != 0) printf("CRITICAL: Variable %s not found!\n", StringToCharArr(op));
	return ret_val;
}

int GetVarReadable(std::string op, ac_var** ret_signal)
{
	int ret_val = GetVarFromList(op, ret_signal, SignalsReadable);
	if (ret_val != 0) printf("CRITICAL: Variable %s not found!\n", StringToCharArr(op));
	return ret_val;
}

int GetVarWriteable(std::string op, ac_var** ret_signal)
{
	int ret_val = GetVarFromList(op, ret_signal, SignalsWriteable);
	if (ret_val != 0) printf("CRITICAL: Variable %s not found!\n", StringToCharArr(op));
	return ret_val;
}

int VarCheckUnique(ac_var* new_var, std::vector<ac_var*> * varlist)
{
	for (unsigned int i = 0; i < varlist->size(); i++)
	{
		if ((*varlist)[i]->name == new_var->name) return 1;
	}
	return 0;
}

int AddVarCheckUnique(ac_var* new_var, std::vector<ac_var*> * varlist)
{
	if (VarCheckUnique(new_var, varlist) != 0) {
		printf("Failed to deploy var %s: name is previously defined!\n", StringToCharArr(new_var->name));
		throw 0;
		return 1;
	} else {
		(*varlist).push_back(new_var);
		return 0;
	}
}
