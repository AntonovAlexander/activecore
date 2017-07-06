/*
 * ac_pipe.hpp
 *
 *  Created on: 31.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#ifndef AC_SYNC_H_
#define AC_SYNC_H_

#include "ac_utils.hpp"
#include "ac_core.hpp"
#include "ac_rtl.hpp"

namespace pipe
{

	struct prr_vector {
	public:
		std::string pipe_step;
		ac_var * rdvar;

		prr_vector(std::string pipe_step_in, ac_var * rdvar_in)
		{
			pipe_step = pipe_step_in;
			rdvar = rdvar_in;
		}
	};

	class copipe_if
	{
	public:
		std::string name;

		ac_var * req_var;
		ac_var * we_var;
		ac_var * ack_var;
		ac_var * wdata_var;
		ac_var * resp_var;
		ac_var * rdata_var;

		rtl::ac_comb * req_done_next;
		rtl::ac_mem * req_done_ff;

		copipe_if(std::string name_in, ac_var * req_var_in, ac_var * we_var_in, ac_var * ack_var_in, ac_var * wdata_var_in, ac_var * resp_var_in, ac_var * rdata_var_in)
		{
			name = name_in;
			req_var = req_var_in;
			we_var = we_var_in;
			ack_var = ack_var_in;
			wdata_var = wdata_var_in;
			resp_var = resp_var_in;
			rdata_var = rdata_var_in;
		}
	};

	class wrfifo_if
	{
	public:
		std::string name;

		ac_var * req_var;
		ac_var * ack_var;
		ac_var * wdata_var;

		rtl::ac_comb * req_done_next;
		rtl::ac_mem * req_done_ff;

		wrfifo_if(std::string name_in, ac_var * req_var_in, ac_var * ack_var_in, ac_var * wdata_var_in)
		{
			name = name_in;
			req_var = req_var_in;
			ack_var = ack_var_in;
			wdata_var = wdata_var_in;
		}
	};

	class rdfifo_if
	{
	public:
		std::string name;

		ac_var * req_var;
		ac_var * ack_var;
		ac_var * rdata_var;

		rtl::ac_comb * req_done_next;
		rtl::ac_mem * req_done_ff;

		rdfifo_if(std::string name_in, ac_var * req_var_in, ac_var * ack_var_in, ac_var * rdata_var_in)
		{
			name = name_in;
			req_var = req_var_in;
			ack_var = ack_var_in;
			rdata_var = rdata_var_in;
		}
	};

	class ac_pproc : public ac_execode
	{
	public:
		ac_var * clk;
		ac_var * rst;

		ac_var * active_req;
		ac_var * prepeat_req;

		std::vector<ac_var*> pvars;
		std::vector<ac_var*> gpvars;

		std::vector<copipe_if*> copipe_ifs;
		std::vector<wrfifo_if*> wrfifo_ifs;
		std::vector<rdfifo_if*> rdfifo_ifs;

		std::vector<prr_vector> prr_vectors;

		ac_pproc(ac_var * clk_in, ac_var * rst_in);

		bool GetCopipeIf(std::string copipeif_name, copipe_if ** copipe_if_fetched);
		bool GetWrfifoIf(std::string wrfifoif_name, wrfifo_if ** wrfifo_if_fetched);
		bool GetRdfifoIf(std::string rdfifoif_name, rdfifo_if ** rdfifo_if_fetched);
	};

	extern char PVAR_STRING[];
	extern std::deque<ac_pproc*> Pprocs;

	int pproc_cmd(std::string clk_varname, std::string rst_varname);
	int endpproc_cmd();
	int pvar_cmd(std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int gpvar_cmd(std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int pstage_cmd(std::string pstage_name);
	int pbreak_cmd();
	int pstall_cmd();
	int prepeat_cmd();
	int pre_cmd(std::string ext_varname, std::string * int_varname);
	int pwe_cmd(std::vector<ac_param> params, std::string ext_varname);
	int prr_cmd(std::string pstage_name, std::string ext_varname, std::string * int_varname);
	int isactive_cmd(std::string pstage_name, std::string * int_varname);

	int copipeif_cmd(std::string copipeif_name, std::string req_varname, std::string we_varname, std::string ack_varname, std::string wdata_varname, std::string resp_varname, std::string rdata_varname);
	int copipereq_cmd(std::string copipeif_name, std::vector<ac_param> params);
	int copiperesp_cmd(std::string copipeif_name, std::string * resp_varname);

	int wrfifoif_cmd(std::string wrfifoif_name, std::string req_signame, std::string ack_signame, std::string wdata_signame);
	int wrfiforeq_cmd(std::string wrfifoif_name, std::vector<ac_param> params);

	int rdfifoif_cmd(std::string rdfifoif_name, std::string req_signame, std::string ack_signame, std::string rdata_signame);
	int rdfiforeq_cmd(std::string rdfifoif_name, std::string * int_varname);

	int export_cmd();

}
#endif /* AC_SYNC_H_ */
