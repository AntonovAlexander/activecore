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

	class ac_mem_source
	{
	public:
		bool sync_posneg;
		ac_var* sync_signal;
		ac_param sync_source;
		ac_mem_source(bool new_sync_posneg, ac_var* new_sync_signal, ac_param new_sync_source);
	};

	class ac_mem : public ac_var
	{
	public:
		bool reset_present;
		bool reset_syncasync;
		bool reset_posneg;
		ac_var* reset_signal;
		ac_param reset_source;

		bool sync_levedge;		// false - latch; true - FF
		std::vector<ac_mem_source*> mem_sources;

		ac_mem(std::string name_in, ac_dimensions_static dimensions_in, bool sync_levedge_in) : ac_var(MEM_STRING, name_in, dimensions_in, "0") {
			sync_levedge = sync_levedge_in;
			reset_present = false;
		}

		ac_mem(std::string name_in, unsigned int msb, unsigned int lsb, bool sync_levedge_in) : ac_var(MEM_STRING, name_in, msb, lsb, "0") {
			sync_levedge = sync_levedge_in;
			reset_present = false;
		}

		int AddSource(bool sync_posneg, ac_var* sync_signal, ac_param sync_source);
		int AddReset(bool reset_syncasync_in, bool reset_posneg_in, ac_var* reset_signal_in, ac_param reset_source_in);
	};

	class ac_port : public ac_var
	{
	public:
		ac_port(std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in) : ac_var(PORT_STRING, name_in, dimensions_in, defval_in) {};
	};

	class ac_comb : public ac_var
	{
	public:
		ac_comb(std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in) : ac_var(COMB_STRING, name_in, dimensions_in, defval_in) {};
		ac_comb(std::string name_in, unsigned int msb, unsigned int lsb, std::string defval_in) : ac_var(COMB_STRING, name_in, msb, lsb, defval_in) {};
	};

	extern std::string ModuleName;
	extern std::vector<ac_comb*> Combs;
	extern std::vector<ac_mem*> Mems;
	extern std::vector<ac_port*> Ports_in;
	extern std::vector<ac_port*> Ports_out;
	extern std::vector<ac_port*> Ports_inout;
	extern std::vector<ac_execode*> Cprocs;

	int SetPtrs();
	int comb_cmd(ac_comb** new_comb, std::string name_in, unsigned int msb, unsigned int lsb, std::string defval_in);
	int comb_cmd(ac_comb** new_comb, std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int port_cmd(ac_port** new_port, std::string name_in, std::string port_type_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int mem_cmd(ac_mem** new_mem, std::string name_in, unsigned int msb, unsigned int lsb, bool sync_levedge_in);
	int mem_cmd(ac_mem** new_mem, std::string name_in, ac_dimensions_static dimensions_in, bool sync_levedge_in);
	int mem_addsource_cmd(std::string mem_name, std::string sync_posneg_in, std::string sync_signal_in, ac_param sync_source_in);
	int mem_addreset_cmd(std::string mem_name, std::string sync_levedge_in, std::string sync_posneg_in, std::string sync_signal_in, ac_param reset_source_in);
	int cproc_cmd();
	int endcproc_cmd();
	int export_cmd(std::string language, char* filename);
}

#endif /* AC_RTL_H_ */
