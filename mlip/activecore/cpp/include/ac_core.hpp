/*
 * ac_core.hpp
 *
 *  Created on: 31.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#ifndef AC_CORE_H_
#define AC_CORE_H_

#include "ac_utils.hpp"

template<typename T> std::string toString(const T& value);
template<typename T> std::string NumberToString ( T Number );

//// GPLC AST ISA ////
extern char OP1_ASSIGN[];
extern char OP1_COMPLEMENT[];

extern char OP2_ARITH_ADD[];
extern char OP2_ARITH_SUB[];
extern char OP2_ARITH_MUL[];
extern char OP2_ARITH_DIV[];
extern char OP2_ARITH_SHL[];
extern char OP2_ARITH_SHR[];
extern char OP2_ARITH_SRA[];

extern char OP1_LOGICAL_NOT[];
extern char OP2_LOGICAL_AND[];
extern char OP2_LOGICAL_OR[];
extern char OP2_LOGICAL_G[];
extern char OP2_LOGICAL_L[];
extern char OP2_LOGICAL_GEQ[];
extern char OP2_LOGICAL_LEQ[];
extern char OP2_LOGICAL_EQ2[];
extern char OP2_LOGICAL_NEQ2[];
extern char OP2_LOGICAL_EQ4[];
extern char OP2_LOGICAL_NEQ4[];

extern char OP1_BITWISE_NOT[];
extern char OP2_BITWISE_AND[];
extern char OP2_BITWISE_OR[];
extern char OP2_BITWISE_XOR[];
extern char OP2_BITWISE_XNOR[];

extern char OP1_REDUCT_AND[];
extern char OP1_REDUCT_NAND[];
extern char OP1_REDUCT_OR[];
extern char OP1_REDUCT_NOR[];
extern char OP1_REDUCT_XOR[];
extern char OP1_REDUCT_XNOR[];

extern char OP2_INDEXED[];
extern char OP3_RANGED[];
extern char OPS_CNCT[];
extern char OP1_IF[];
extern char OP1_WHILE[];


struct dimension_range_static;

class ac_dimensions_static : public std::deque<dimension_range_static>
{
public:
	void PrintDimensions();
	int GetPower(unsigned int * power);
	bool isSingle();
};

enum class PARAM_TYPE: int
{
	VAR = 0,
	VAL = 1
};

class ac_param
{
public:
	PARAM_TYPE type;
	ac_dimensions_static dimensions;
	std::string string_printable;

	std::string GetString();
	ac_dimensions_static GetDimensions();

	bool isDimSingle();
};

class ac_imm : public ac_param
{
public:
	std::string value;

	ac_imm(ac_dimensions_static dimensions_in, std::string value_in);
	ac_imm(std::string value_in);
	ac_imm(unsigned int msb, unsigned int lsb, std::string value_in);
	ac_imm(unsigned int width, std::string value);
};

extern char DEFAULT_TYPEVAR[];

enum class VAR_TYPE: int
{
	vt_signed = 0,
	vt_unsigned = 1,
	vt_structured = 2,
	vt_undefined = 3
};

class ac_struct;

struct VarType {
	VAR_TYPE type;
	ac_struct * src_struct;

	VarType() {
		type = VAR_TYPE::vt_undefined;
		src_struct = nullptr;
	}

	VarType(VAR_TYPE TYPE_in) {
		type = TYPE_in;
		if ((TYPE_in == VAR_TYPE::vt_signed) || (TYPE_in == VAR_TYPE::vt_unsigned)) src_struct = nullptr;
		else {
			printf("ActiveCore ERROR: var type incorrect!\n");
			return;
		}
	}

	VarType(ac_struct * struct_in) {
		type = VAR_TYPE::vt_structured;
		src_struct = struct_in;
	}
};

int DecodeVarType (std::string vartype_in, VarType * vartype);

class ac_structvar : public ac_param
{
public:
	std::string name;
	std::string defval;

	VarType vartype;

	ac_structvar(std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in);
	ac_structvar(std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval_in);
	ac_structvar(std::string name_in, VarType VarType_in, std::string defval_in);
};

class ac_struct
{
public:
	std::string name;
	std::vector<ac_structvar*> structvars;

	ac_struct(std::string name_in);
};

extern std::vector<ac_struct*> defined_structs;
extern ac_struct* current_struct;

int ac_struct_cmd(ac_struct ** new_struct, std::string name_in);

class VarSegmentVector;

class ac_var : public ac_structvar
{
public:
	char * type_name;
	bool read_done;
	bool write_done;

	int GetDePowered(ac_dimensions_static * ret_dimensions, VarType * ret_vartype, VarSegmentVector * DePower);

	ac_var(char * type_name_in, std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in);
	ac_var(char * type_name_in, std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval_in);
	ac_var(char * type_name_in, std::string name_in, VarType VarType_in, std::string defval_in);

	int set_default_cmd(unsigned int * cursor);

private:
	void init_internal(char * type_name_in);
	int set_default_cmd_internal(unsigned int * cursor, VarSegmentVector * VarSegmentsDePower);
};

enum class SegType: int
{
	C = 0,
	V = 1,
	CC = 2,
	CV = 3,
	VC = 4,
	VV = 5,
	SubStruct = 6
};

struct dimension_range_static {
	unsigned int msb;
	unsigned int lsb;

	dimension_range_static(unsigned int msb_in, unsigned int lsb_in)
	{
		msb = msb_in;
		lsb = lsb_in;
	}

	unsigned int GetWidth()
	{
		if (msb > lsb) return ((msb - lsb) + 1);
		else return ((lsb - msb) + 1);
	}

	void PrintDimension();
};

struct VarSegment {
	SegType type;

	unsigned int msb_int;
	unsigned int lsb_int;

	ac_var* msb_var;
	ac_var* lsb_var;

	ac_struct * src_struct;
	unsigned int structIndex;
	std::string substruct_name;

	VarSegment(unsigned int msb)
	{
		type = SegType::C;
		msb_int = msb;
	}

	VarSegment(ac_var* msb)
	{
		type = SegType::V;
		msb_var = msb;
	}

	VarSegment(unsigned int msb, unsigned int lsb)
	{
		type = SegType::CC;
		msb_int = msb;
		lsb_int = lsb;
	}

	VarSegment(unsigned int msb, ac_var* lsb)
	{
		type = SegType::CV;
		msb_int = msb;
		lsb_var = lsb;
	}

	VarSegment(ac_var* msb, unsigned int lsb)
	{
		type = SegType::VC;
		msb_var = msb;
		lsb_int = lsb;
	}

	VarSegment(ac_var* msb, ac_var* lsb)
	{
		type = SegType::VV;
		msb_var = msb;
		lsb_var = lsb;
	}

	VarSegment(ac_struct * src_struct_in, unsigned int structIndex_in)
	{
		type = SegType::SubStruct;
		src_struct = src_struct_in;
		structIndex = structIndex_in;
	}

	VarSegment(std::string substruct_name_in)
	{
		type = SegType::SubStruct;
		substruct_name = substruct_name_in;
	}

	int GetWidth(unsigned int * width)
	{
		if ((type == SegType::C) || (type == SegType::V)) *width = 1;
		else if (type == SegType::CC)
		{
			if (msb_int > lsb_int) *width = msb_int - lsb_int;
			else *width = lsb_int - msb_int;
		} else {
			return 1;
		}
		return 0;
	}

	int GetString(std::string * string_in)
	{
		switch (type)
		{
		case SegType::C:
			*string_in = "[" + NumberToString(msb_int) + "]";
			break;
		case SegType::V:
			*string_in = "[" + msb_var->name + "]";
			break;
		case SegType::CC:
			*string_in = "[" + NumberToString(msb_int) + ":" +  NumberToString(lsb_int) + "]";
			break;
		case SegType::CV:
			*string_in = "[" + NumberToString(msb_int) + ":" +  lsb_var->name + "]";
			break;
		case SegType::VC:
			*string_in = "[" + msb_var->name + ":" +  NumberToString(lsb_int) + "]";
			break;
		case SegType::VV:
			*string_in = "[" + msb_var->name + ":" +  lsb_var->name + "]";
			break;
		case SegType::SubStruct:
			*string_in = "." + src_struct->structvars[structIndex]->name;
			break;
		default:
			return 1;
		}
		return 0;
	}

	void PrintDimension();
};

class VarSegmentVector : public std::deque<VarSegment>
{
public:
	void PrintDimensions();
	int GetDimensionsString(std::string * string_in);
	int GetPower(unsigned int * power);
};

class ac_execode
{
public:
	char * opcode;
	std::vector<ac_param*> params;
	std::vector<std::string> string_params;
	std::vector<int> int_params;
	std::vector<unsigned int> uint_params;
	std::deque<ac_execode*> expressions;

	std::vector<ac_param*> priority_conditions;

	VarSegmentVector VarSegments;

	std::vector<ac_var*> wrvars;
	std::vector<ac_var*> rdvars;
	std::vector<ac_var*> genvars;
	std::vector<ac_var*> iftargets;

	ac_execode(char * opcode_in);

	void AddExpr(ac_execode* new_expr);
	void AddExpr(unsigned int * cursor, ac_execode* new_expr);
	bool AddWrVar(ac_var* new_wrvar);
	bool AddRdVar(ac_var* new_rdvar);
	void AddGenVar(ac_var* new_genvar);
	void AddRdParam(ac_param* new_param);
	void AddRdParams(std::vector<ac_param*> new_params);
	bool AddIfTargetVar(ac_var* new_iftvar);

	void AddRdVarWithStack(ac_var* new_op);
	void AddWrVarWithStack(ac_var* new_op);
	void AddGenVarWithStack(ac_var* new_op);
	void AddRdParamWithStack(ac_param* new_param);
	void AddRdParamsWithStack(std::vector<ac_param*> new_params);
};

extern bool DEBUG_FLAG;

// AST state
extern std::deque<ac_execode*> ExeStack;
extern std::vector<std::vector<ac_var*>* > SignalsReadable;
extern std::vector<std::vector<ac_var*>* > SignalsWriteable;
extern VarSegmentVector VarSegmentAccumulator;
extern ac_dimensions_static StaticDimAccumulator;
extern std::vector<ac_param*> ParamAccumulator;
extern std::vector<std::string> StringParamAccumulator;
extern std::vector<int> IntParamAccumulator;
extern std::vector<unsigned int> UIntParamAccumulator;

// Service functions
void GenReset();
std::string GetGenName(std::string name_in);
std::string NumToString(int number);
int SetVarReadable(std::string op, ac_var** ret_signal);
int SetVarWriteable(std::string op, ac_var** ret_signal);
int GetVarReadable(std::string op, ac_var** ret_signal);
int GetVarWriteable(std::string op, ac_var** ret_signal);
int VarCheckUnique(ac_var* new_var, std::vector<ac_var*> * varlist);
int AddVarCheckUnique(ac_var* new_var, std::vector<ac_var*> * varlist);
void AddGenVarToStack(ac_var* new_op);

#endif /* AC_CORE_H_ */
