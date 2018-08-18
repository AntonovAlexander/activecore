/*
 * ac_rtl.hpp
 *
 *  Created on: 31.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#ifndef AC_RTL_H_
#define AC_RTL_H_

#include "ac_core.hpp"
#include "ac_core_cmds.hpp"
#include "ac_utils.hpp"

namespace rtl
{
	#define SYNC_LEVEL 	false
	#define SYNC_EDGE 	true
	#define SYNC_POS	true
	#define SYNC_NEG	false
	#define RST_SYNC	true
	#define RST_ASYNC	false

	extern char COMB_STRING[];
	extern char MEM_STRING[];
	extern char PORT_STRING[];
	extern char BUFFERED_STRING[];
	extern char STICKY_STRING[];

	class ac_mem_source
	{
	public:
		bool sync_posneg;
		ac_var* sync_signal;
		ac_param* sync_source;
		ac_mem_source(bool new_sync_posneg, ac_var* new_sync_signal, ac_param* new_sync_source);
	};

	class ac_mem : public ac_var
	{
	public:
		bool reset_present;
		bool reset_syncasync;
		bool reset_posneg;
		ac_var* reset_signal;
		ac_param* reset_source;

		bool sync_levedge;		// false - latch; true - FF
		std::vector<ac_mem_source*> mem_sources;

		ac_mem(std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, bool sync_levedge_in) : ac_var(MEM_STRING, name_in, VarType_in, dimensions_in, "0") {
			sync_levedge = sync_levedge_in;
			reset_present = false;
		}

		ac_mem(std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb, bool sync_levedge_in) : ac_var(MEM_STRING, name_in, VarType_in, msb, lsb, "0") {
			sync_levedge = sync_levedge_in;
			reset_present = false;
		}

		ac_mem(std::string name_in, VarType VarType_in, bool sync_levedge_in) : ac_var(MEM_STRING, name_in, VarType_in, "0") {
			sync_levedge = sync_levedge_in;
			reset_present = false;
		}

		int AddSource(bool sync_posneg, ac_var* sync_signal, ac_param* sync_source);
		int AddReset(bool reset_syncasync_in, bool reset_posneg_in, ac_var* reset_signal_in, ac_param* reset_source_in);
	};

	enum class PORT_DIR: int
	{
		in = 0, out = 1, inout = 2
	};

	extern char DIR_STRING_IN[];
	extern char DIR_STRING_OUT[];
	extern char DIR_STRING_INOUT[];

	class ac_port : public ac_var
	{
	public:
		PORT_DIR direction;
		ac_port(std::string name_in, PORT_DIR direction_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in) : ac_var(PORT_STRING, name_in, VarType_in, dimensions_in, defval_in) {direction = direction_in;}
		ac_port(std::string name_in, PORT_DIR direction_in, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval_in) : ac_var(PORT_STRING, name_in, VarType_in, msb, lsb, defval_in) {direction = direction_in;}
		ac_port(std::string name_in, PORT_DIR direction_in, VarType VarType_in, std::string defval_in) : ac_var(PORT_STRING, name_in, VarType_in, defval_in) {direction = direction_in;}
	};

	class ac_comb : public ac_var
	{
	public:
		ac_comb(std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in) : ac_var(COMB_STRING, name_in, VarType_in, dimensions_in, defval_in) {};
		ac_comb(std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval_in) : ac_var(COMB_STRING, name_in, VarType_in, msb, lsb, defval_in) {};
		ac_comb(std::string name_in, VarType VarType_in, std::string defval_in) : ac_var(COMB_STRING, name_in, VarType_in, defval_in) {};
	};

	class ac_syncbuf : public ac_var
	{
	public:
		ac_syncbuf(char* TYPE_STRING, std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in, ac_var * clk, ac_var * rst);
		ac_syncbuf(char * type_name_in, std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval_in, ac_var * clk, ac_var * rst);
		ac_syncbuf(char* TYPE_STRING, std::string name_in, VarType VarType_in, std::string defval_in, ac_var * clk, ac_var * rst);

		ac_mem * mem;
	};

	extern std::string ModuleName;
	extern std::vector<ac_comb*> Combs;
	extern std::vector<ac_mem*> Mems;
	extern std::vector<ac_syncbuf*> SyncBufs;
	extern std::vector<ac_port*> Ports;
	extern std::vector<ac_port*> Ports_in;
	extern std::vector<ac_port*> Ports_out;
	extern std::vector<ac_port*> Ports_inout;
	extern std::vector<ac_execode*> Cprocs;

	int SetPtrs();

	int comb_cmd(ac_comb** new_comb, std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval_in);
	int comb_cmd(ac_comb** new_comb, std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int comb_cmd(ac_comb** new_comb, std::string name_in, VarType VarType_in, std::string defval_in);

	int port_cmd(ac_port** new_port, std::string name_in, PORT_DIR direction_in, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval_in);
	int port_cmd(ac_port** new_port, std::string name_in, PORT_DIR direction_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int port_cmd(ac_port** new_port, std::string name_in, PORT_DIR direction_in, VarType VarType_in, std::string defval_in);

	int mem_cmd(ac_mem** new_mem, std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb, bool sync_levedge_in);
	int mem_cmd(ac_mem** new_mem, std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, bool sync_levedge_in);
	int mem_cmd(ac_mem** new_mem, std::string name_in, VarType VarType_in, bool sync_levedge_in);

	int buffered_cmd(ac_syncbuf** new_syncbuf, std::string name, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval, ac_var* clk, ac_var* rst);
	int buffered_cmd(ac_syncbuf** new_syncbuf, std::string name, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval, ac_var* clk, ac_var* rst);

	int sticky_cmd(ac_syncbuf** new_syncbuf, std::string name, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval, ac_var* clk, ac_var* rst);
	int sticky_cmd(ac_syncbuf** new_syncbuf, std::string name, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval, ac_var* clk, ac_var* rst);
	int sticky_cmd(ac_syncbuf** new_syncbuf, std::string name, VarType VarType_in, std::string defval, ac_var* clk, ac_var* rst);

	int rdbuf_cmd(std::string sticky_name, std::string * respvar_name);

	int cproc_cmd();
	int endcproc_cmd();
	int export_cmd(std::string language, std::string pathname);
}

#endif /* AC_RTL_H_ */
