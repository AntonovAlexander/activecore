/*
 * ac_core.cpp
 *
 *  Created on: 30.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#include "ac_utils.hpp"
#include "ac_core.hpp"
#include "ac_core_cmds.hpp"

template<typename T> std::string NumberToString ( T Number )
{
	std::stringstream ss;
	ss << Number;
	return ss.str();
}

bool DEBUG_FLAG = false;

//// GPLC AST ISA ////
char OP1_ASSIGN[] = "ac=";
char OP1_COMPLEMENT[] = "acc-";

char OP2_ARITH_ADD[] = "ac+";
char OP2_ARITH_SUB[] = "ac-";
char OP2_ARITH_MUL[] = "acx";
char OP2_ARITH_DIV[] = "ac/";
char OP2_ARITH_SHL[] = "ac<<";
char OP2_ARITH_SHR[] = "ac>>";
char OP2_ARITH_SRA[] = "ac>>>";

char OP1_LOGICAL_NOT[] = "ac!";
char OP2_LOGICAL_AND[] = "ac&&";
char OP2_LOGICAL_OR[] = "ac||";
char OP2_LOGICAL_G[] = "ac>";
char OP2_LOGICAL_L[] = "ac<";
char OP2_LOGICAL_GEQ[] = "ac>=";
char OP2_LOGICAL_LEQ[] = "ac<=";
char OP2_LOGICAL_EQ2[] = "ac==";
char OP2_LOGICAL_NEQ2[] = "ac!=";
char OP2_LOGICAL_EQ4[] = "ac===";
char OP2_LOGICAL_NEQ4[] = "ac!===";

char OP1_BITWISE_NOT[] = "ac~";
char OP2_BITWISE_AND[] = "ac&";
char OP2_BITWISE_OR[] = "ac|";
char OP2_BITWISE_XOR[] = "ac^";
char OP2_BITWISE_XNOR[] = "ac^~";

char OP1_REDUCT_AND[] = "acr&";
char OP1_REDUCT_NAND[] = "acr~&";
char OP1_REDUCT_OR[] = "acr|";
char OP1_REDUCT_NOR[] = "acr~|";
char OP1_REDUCT_XOR[] = "acr^";
char OP1_REDUCT_XNOR[] = "acr^~";

char OP2_INDEXED[] = "indexed";
char OP3_RANGED[] = "ranged";
char OPS_CNCT[] = "cnct";
char OP1_IF[] = "if";
char OP1_WHILE[] = "while";


// AST state
int GenCounter = 0;
std::deque<ac_execode*> ExeStack;
std::vector<std::vector<ac_var*>* > SignalsReadable;
std::vector<std::vector<ac_var*>* > SignalsWriteable;
VarSegmentVector VarSegmentAccumulator;
ac_dimensions_static StaticDimAccumulator;
std::vector<ac_param*> ParamAccumulator;
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

ac_dimensions_static ac_param::GetDimensions()
{
	return dimensions;
}

void dimension_range_static::PrintDimension()
{
	printf("Dimension range:\n");
	printf("msb: %d\n", msb);
	printf("lsb: %d\n", lsb);
}

void VarSegment::PrintDimension()
{
	switch (type)
	{
	case SegType::C:
		printf("Var segment type: C\n");
		printf("msb: %d\n", msb_int);
		break;

	case SegType::V:
		printf("Var segment type: V\n");
		printf("msb: %s\n", StringToCharArr(msb_var->name));
		break;

	case SegType::CC:
		printf("Var segment type: CC\n");
		printf("msb: %d\n", msb_int);
		printf("lsb: %d\n", lsb_int);
		break;

	case SegType::CV:
		printf("Var segment type: CV\n");
		printf("msb: %d\n", msb_int);
		printf("lsb: %s\n", StringToCharArr(lsb_var->name));
		break;

	case SegType::VC:
		printf("Var segment type: VC\n");
		printf("msb: %s\n", StringToCharArr(msb_var->name));
		printf("lsb: %d\n", lsb_int);
		break;

	case SegType::VV:
		printf("Var segment type: VV\n");
		printf("msb: %s\n", StringToCharArr(msb_var->name));
		printf("lsb: %s\n", StringToCharArr(lsb_var->name));
		break;

	case SegType::SubStruct:
		printf("Var segment type: SubStruct\n");
		printf("struct name: %s\n", StringToCharArr(src_struct->name));
		printf("struct index: %d\n", structIndex);
		printf("struct var name: %s\n", StringToCharArr(src_struct->structvars[structIndex]->name));
		break;

	default:
		printf("Var segment type unrecognized: %d\n", type);
	}
}

void VarSegmentVector::PrintDimensions()
{
	for (unsigned int i = 0; i < size(); i++)
	{
		at(i).PrintDimension();
	}
}

int VarSegmentVector::GetPower(unsigned int * power)
{
	*power = 0;
	for (unsigned int i = 0; i < size(); i++)
	{
		if ((at(i).type == SegType::C) || (at(i).type == SegType::V) || (at(i).type == SegType::SubStruct)) (*power)++;
		else if ((at(i).type == SegType::CC) || (at(i).type == SegType::CV) || (at(i).type == SegType::VC) || (at(i).type == SegType::VV)) continue;
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

bool ac_dimensions_static::isSingle()
{
	if (size() == 0) return true;
	if (size() == 1) {
		if (at(0).GetWidth() == 1) return true;
	}
	return false;
}

int VarSegmentVector::GetDimensionsString(std::string * string_in)
{
	*string_in = "";
	for (int i = (size()-1); i > (-1); i--)
	{
		std::string range_string;
		if (at(i).GetString(&range_string) != 0) return 1;
		*string_in = ((*string_in) + range_string);
	}
	return 0;
}


int DecodeVarType (std::string vartype_in, VarType * vartype) {
	if (vartype_in == "signed") {
		vartype->type = VAR_TYPE::vt_signed;
		vartype->src_struct = nullptr;
		return 0;

	} else if (vartype_in == "unsigned") {
		vartype->type = VAR_TYPE::vt_unsigned;
		vartype->src_struct = nullptr;
		return 0;

	} else {
		for (unsigned int i = 0; i < defined_structs.size(); i++) {
			if (defined_structs[i]->name == vartype_in) {
				vartype->type = VAR_TYPE::vt_structured;
				vartype->src_struct = defined_structs[i];
				return 0;
			}
		}
		printf("Struct decoding failed!\n");
		return 1;
	}
	return 0;
}

ac_structvar::ac_structvar(std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in)
{
	name = name_in;
	vartype = VarType_in;
	dimensions = dimensions_in;
	defval = defval_in;
}

ac_structvar::ac_structvar(std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval_in)
{
	name = name_in;
	vartype = VarType_in;
	dimension_range_static new_range(msb, lsb);
	dimensions = ac_dimensions_static();
	dimensions.push_back(new_range);
	defval = defval_in;
}

ac_structvar::ac_structvar(std::string name_in, VarType VarType_in, std::string defval_in)
{
	name = name_in;
	vartype = VarType_in;
	defval = defval_in;
}

std::vector<ac_struct*> defined_structs;
ac_struct* current_struct;

ac_struct::ac_struct(std::string name_in)
{
	name = name_in;
}

int ac_struct_cmd(ac_struct ** new_struct, std::string name_in) {
	*new_struct = new ac_struct(name_in);
	defined_structs.push_back(*new_struct);
	return 0;
}

void ac_var::init_internal(char * type_name_in)
{
	type_name = type_name_in;
	read_done = false;
	write_done = false;
	type = PARAM_TYPE::VAR;
	string_printable = name;
}

ac_var::ac_var(char * type_name_in, std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in) : ac_structvar(name_in, VarType_in, dimensions_in, defval_in)
{
	init_internal(type_name_in);
}

ac_var::ac_var(char * type_name_in, std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval_in) : ac_structvar(name_in, VarType_in, msb, lsb, defval_in)
{
	init_internal(type_name_in);
}

ac_var::ac_var(char * type_name_in, std::string name_in, VarType VarType_in, std::string defval_in) : ac_structvar(name_in, VarType_in, defval_in)
{
	init_internal(type_name_in);
}

int ac_var::GetDePowered(ac_dimensions_static * ret_dimensions, VarType * ret_vartype, VarSegmentVector * DePower)
{
	ret_dimensions->clear();

	// copying dimensions of a variable
	for (unsigned int i = 0; i < dimensions.size(); i++)
	{
		ret_dimensions->push_back(dimensions[i]);
	}
	*ret_vartype = vartype;

	// detaching dimensions
	for (unsigned int DEPOW_INDEX = 0; DEPOW_INDEX < DePower->size(); DEPOW_INDEX++)
	{
		if (ret_dimensions->isSingle()) {
			// undimensioned var
			ret_dimensions->clear();
			if (DePower->at(DEPOW_INDEX).type == SegType::SubStruct) {
				// retrieving structure
				*ret_vartype = DePower->at(DEPOW_INDEX).src_struct->structvars[DePower->at(DEPOW_INDEX).structIndex]->vartype;
				for (unsigned int SUBSTR_DIM_INDEX = 0; SUBSTR_DIM_INDEX < DePower->at(DEPOW_INDEX).src_struct->structvars[DePower->at(DEPOW_INDEX).structIndex]->dimensions.size(); SUBSTR_DIM_INDEX++)
				{
					ret_dimensions->push_back(DePower->at(DEPOW_INDEX).src_struct->structvars[DePower->at(DEPOW_INDEX).structIndex]->dimensions[DEPOW_INDEX]);
				}
			} else {
				// indexing 1-bit (dim) var
				ret_dimensions->push_back(dimension_range_static(0, 0));
			}
		} else {
			// dimensioned var
			if (DePower->at(DEPOW_INDEX).type == SegType::SubStruct) {
				printf("Depower index generation incorrect!\n");
				return 1;
			} else {
				if ((DePower->at(DEPOW_INDEX).type == SegType::C)
					|| (DePower->at(DEPOW_INDEX).type == SegType::V)) {
					// taking indexed variable - detachment of last dimensions
					ret_dimensions->pop_back();
				} else if (DePower->at(DEPOW_INDEX).type == SegType::CC) {
					// replacing last dimension with taken
					ret_dimensions->pop_back();
					ret_dimensions->push_back(dimension_range_static(DePower->at(DEPOW_INDEX).msb_int, DePower->at(DEPOW_INDEX).lsb_int));
				} else continue;
			}
		}
	}
	return 0;
}

int ac_var::set_default_cmd_internal(unsigned int * cursor, VarSegmentVector * VarSegmentsDePower) {
	int ret_val;

	VarType vartypeDepowered;
	ac_dimensions_static TargetDepoweredDimensions;
	if (GetDePowered(&TargetDepoweredDimensions, &vartypeDepowered, VarSegmentsDePower) != 0) return 1;

	if (vartypeDepowered.type == VAR_TYPE::vt_structured) {
		if (TargetDepoweredDimensions.isSingle()) {
			for (unsigned int i = 0; i < vartypeDepowered.src_struct->structvars.size(); i++) {
				VarSegmentsDePower->push_front(VarSegment(vartypeDepowered.src_struct, i));
				ret_val = expr_assign_cmd(cursor, *VarSegmentsDePower, this, new ac_imm(32,  vartypeDepowered.src_struct->structvars[i]->defval));
				VarSegmentsDePower->pop_front();
				if (ret_val != 0) return ret_val;
			}
		}
		else {
			for (unsigned int i = 0; i < TargetDepoweredDimensions.back().GetWidth(); i++)
			{
				VarSegmentsDePower->push_front(VarSegment(i + TargetDepoweredDimensions.back().lsb));
				ret_val = expr_assign_cmd(cursor, *VarSegmentsDePower, this, new ac_imm(32, "defval"));
				VarSegmentsDePower->pop_front();
				if (ret_val != 0) return ret_val;
			}
			return ret_val;
		}
	} else {
		return (expr_assign_cmd(cursor, this, new ac_imm(defval)) != 0);
	}
}

int ac_var::set_default_cmd(unsigned int * cursor) {
	VarSegmentVector VarSegmentsDePower;
	return set_default_cmd_internal(cursor, &VarSegmentsDePower);
}

std::string ac_param::GetString()
{
	return string_printable;
}

bool ac_param::isDimSingle()
{
	return dimensions.isSingle();
}

ac_imm::ac_imm(ac_dimensions_static dimensions_in, std::string value_in)
{
	dimensions = dimensions_in;
	value = value_in;
	type = PARAM_TYPE::VAL;
	string_printable = value;
}

ac_imm::ac_imm(std::string value_in)
{
	dimension_range_static new_range(31, 0);
	ac_dimensions_static * new_dimensions = new ac_dimensions_static();
	new_dimensions->push_back(new_range);
	dimensions = *new_dimensions;
	value = value_in;
	type = PARAM_TYPE::VAL;
	string_printable = value;
}

ac_imm::ac_imm(unsigned int msb, unsigned int lsb, std::string value_in)
{
	dimension_range_static new_range(msb, lsb);
	ac_dimensions_static * new_dimensions = new ac_dimensions_static();
	new_dimensions->push_back(new_range);
	dimensions = *new_dimensions;
	value = value_in;
	type = PARAM_TYPE::VAL;
	string_printable = value;
}

ac_imm::ac_imm(unsigned int width, std::string value) : ac_imm(width-1, 0, value)
{
	type = PARAM_TYPE::VAL;
	string_printable = value;
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

void ac_execode::AddRdParam(ac_param* new_param)
{
	params.push_back(new_param);
	if (new_param->type == PARAM_TYPE::VAR) AddRdVar((ac_var*)new_param);
}

void ac_execode::AddRdParams(std::vector<ac_param*> new_params)
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

ac_execode::ac_execode(char * opcode_in)
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

void ac_execode::AddRdParamWithStack(ac_param* new_param)
{
	AddRdParam(new_param);
	if (new_param->type == PARAM_TYPE::VAR)
	{
		for (unsigned int i = 0; i < ExeStack.size(); i++)
		{
			ExeStack[i]->AddRdVar((ac_var*)new_param);
		}
	}
}

void ac_execode::AddRdParamsWithStack(std::vector<ac_param*> new_params)
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
		if (DEBUG_FLAG)
		{
			printf("[[ Var list ]]\n");
			for (unsigned int i = 0; i < varlist->size(); i++)
			{
				printf("var name [%d]: %s;\n", i, StringToCharArr(varlist->at(i)->name));
			}
		}
		throw 0;
		return 1;
	} else {
		(*varlist).push_back(new_var);
		return 0;
	}
}
