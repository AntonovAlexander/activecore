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

struct dimension_range;
struct dimension_range_static;

class ac_dimensions : public std::deque<dimension_range>
{
public:
	void PrintDimensions();
	int GetDimensionsString(std::string * string_in);
	int GetPower(unsigned int * power);
};

class ac_dimensions_static : public std::deque<dimension_range_static>
{
public:
	void PrintDimensions();
};

class ac_imm
{
public:
	ac_dimensions_static dimensions;
	std::string value;

	ac_imm(ac_dimensions_static dimensions_in, std::string value_in);
	ac_imm(std::string value_in);
	ac_imm(unsigned int msb, unsigned int lsb, std::string value_in);
};

extern char DEFAULT_TYPEVAR[];

class ac_var
{
public:
	std::string name;
	ac_dimensions_static dimensions;
	std::string defval;
	char * type_name;
	bool read_done;
	bool write_done;

	ac_var(char * type_name_in, std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in);
	ac_var(char * type_name_in, std::string name_in, unsigned int msb, unsigned int lsb, std::string defval_in);
};

#define PARAM_TYPE_VAR 	true
#define PARAM_TYPE_VAL	false

class ac_param
{
public:
	bool type;
	ac_var * var;
	ac_imm * imm;

	ac_param() {};
	ac_param(ac_var* var_in);
	ac_param(ac_imm* imm_in);
	ac_param(ac_dimensions_static dimensions_in, std::string value);
	ac_param(unsigned int width, std::string value);

	std::string GetString();
	std::string GetStringFull();
	ac_dimensions_static GetDimensions();
};

#define DimType_C	0
#define DimType_V	1
#define DimType_CC	2
#define DimType_CV	3
#define DimType_VC	4
#define DimType_VV	5

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

struct dimension_range {
	unsigned int type;

	unsigned int msb_int;
	unsigned int lsb_int;

	ac_var* msb_var;
	ac_var* lsb_var;

	dimension_range(unsigned int msb)
	{
		type = DimType_C;
		msb_int = msb;
	}

	dimension_range(ac_var* msb)
	{
		type = DimType_V;
		msb_var = msb;
	}

	dimension_range(unsigned int msb, unsigned int lsb)
	{
		type = DimType_CC;
		msb_int = msb;
		lsb_int = lsb;
	}

	dimension_range(unsigned int msb, ac_var* lsb)
	{
		type = DimType_CV;
		msb_int = msb;
		lsb_var = lsb;
	}

	dimension_range(ac_var* msb, unsigned int lsb)
	{
		type = DimType_VC;
		msb_var = msb;
		lsb_int = lsb;
	}

	dimension_range(ac_var* msb, ac_var* lsb)
	{
		type = DimType_VV;
		msb_var = msb;
		lsb_var = lsb;
	}

	int GetWidth(unsigned int * width)
	{
		if ((type == DimType_C) || (type == DimType_V)) *width = 1;
		else if (type == DimType_CC)
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
		//printf("GetString: start\n");
		switch (type)
		{
		case DimType_C:
			*string_in = "[" + NumberToString(msb_int) + "]";
			break;
		case DimType_V:
			*string_in = "[" + msb_var->name + "]";
			break;
		case DimType_CC:
			*string_in = "[" + NumberToString(msb_int) + ":" +  NumberToString(lsb_int) + "]";
			break;
		case DimType_CV:
			*string_in = "[" + NumberToString(msb_int) + ":" +  lsb_var->name + "]";
			break;
		case DimType_VC:
			*string_in = "[" + msb_var->name + ":" +  NumberToString(lsb_int) + "]";
			break;
		case DimType_VV:
			*string_in = "[" + msb_var->name + ":" +  lsb_var->name + "]";
			break;
		default:
			return 1;
		}
		//printf("GetString: string: %s\n", StringToCharArr(*string_in));
		//printf("GetString: finish\n");
		return 0;
	}

	void PrintDimension();
};

int getdimstring(dimension_range * in_range, std::string * ret_val);
int getdimstring(dimension_range_static * in_range, std::string * ret_val);

class ac_execode
{
public:
	std::string opcode;
	std::vector<ac_param> params;
	std::vector<std::string> string_params;
	std::deque<ac_execode*> expressions;

	ac_dimensions dimensions;

	std::vector<ac_var*> wrvars;
	std::vector<ac_var*> rdvars;
	std::vector<ac_var*> genvars;
	std::vector<ac_var*> iftargets;

	ac_execode();
	ac_execode(std::string opcode_in);

	void AddExpr(ac_execode* new_expr);
	void AddExpr(unsigned int * cursor, ac_execode* new_expr);
	bool AddWrVar(ac_var* new_wrvar);
	bool AddRdVar(ac_var* new_rdvar);
	void AddGenVar(ac_var* new_genvar);
	void AddRdParam(ac_param new_param);
	void AddRdParams(std::vector<ac_param> new_params);
	bool AddIfTargetVar(ac_var* new_iftvar);

	void AddRdVarWithStack(ac_var* new_op);
	void AddWrVarWithStack(ac_var* new_op);
	void AddGenVarWithStack(ac_var* new_op);
	void AddRdParamWithStack(ac_param new_param);
	void AddRdParamsWithStack(std::vector<ac_param> new_params);
};

extern bool DEBUG_FLAG;

// AST state
extern std::deque<ac_execode*> ExeStack;
extern std::vector<std::vector<ac_var*>* > SignalsReadable;
extern std::vector<std::vector<ac_var*>* > SignalsWriteable;
extern ac_dimensions DimensionsAccumulator;
extern std::vector<ac_param> ParamAccumulator;
extern std::vector<std::string> StringParamAccumulator;

// Service functions
void GenReset();
std::string GetGenName(std::string name_in);
std::string NumToString(int number);
int SetVarReadable(std::string op, ac_var** ret_signal);
int SetVarWriteable(std::string op, ac_var** ret_signal);
int GetVarReadable(std::string op, ac_var** ret_signal);
int GetVarWriteable(std::string op, ac_var** ret_signal);
int AddVarCheckUnique(ac_var* new_var, std::vector<ac_var*> * varlist);

#endif /* AC_CORE_H_ */
