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
	class mcopipe_if;
	class wrfifo_if;
	class rdfifo_if;

	extern char PVAR_STRING[];
	extern char GPVAR_STRING[];

	int reset_cmd();

	int SetPtrs();
	int pproc_cmd(std::string pproc_name, ac_var * clk_var, ac_var * rst_var);
	int endpproc_cmd();
	int pvar_cmd(std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int gpvar_cmd(std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int rdbuf_cmd(std::string gpvar_name, std::string * respvar_name);
	int assign_unblocking_cmd(ac_dimensions dimensions, std::string target, ac_param param);
	int pstage_cmd(std::string pstage_name);
	int pbreak_cmd();
	int pstall_cmd();
	int prepeat_cmd();
	int pflush_cmd();
	int pre_cmd(ac_var * ext_var, ac_var ** int_var);
	int pwe_cmd(ac_param param, ac_var * ext_var);
	int pwe_unblocking_cmd(ac_param param, ac_var * ext_var);
	int prr_cmd(std::string pstage_name, ac_var * remote_var, ac_var ** int_var);
	int accum_cmd(ac_var * target, ac_param source);

	int isactive_cmd(std::string pstage_name, std::string * int_varname);
	int isworking_cmd(std::string pstage_name, std::string * int_varname);
	int isstalled_cmd(std::string pstage_name, std::string * int_varname);
	int issucc_cmd(std::string pstage_name, std::string * int_varname);
	int isbroken_cmd(std::string pstage_name, std::string * int_varname);
	int isfinished_cmd(std::string pstage_name, std::string * int_varname);

	int mcopipeif_cmd(std::string mcopipeif_name, ac_dimensions_static dimensions, ac_dimensions_static wdata_dimensions, ac_dimensions_static rdata_dimensions);
	int scopipeif_cmd(std::string scopipeif_name, ac_dimensions_static wdata_dimensions, ac_dimensions_static rdata_dimensions);
	int copipeif_cmd(std::string copipeif_name, ac_dimensions_static dimensions, ac_dimensions_static wdata_dimensions, ac_dimensions_static rdata_dimensions);
	int mcopipe_connect_cmd(std::string pproc_name, std::string mcopipeif_name, std::string copipeif_name);
	int mcopipe_export_cmd(std::string mcopipeif_name, unsigned int chnum, ac_var * req_var, ac_var * we_var, ac_var * ack_var, ac_var * wdata_var, ac_var * resp_var, ac_var * rdata_var);
	int mcopipe_req_cmd(std::string mcopipeif_name, ac_dimensions dimensions, ac_var ** rdy_var, ac_param cmd_param, ac_param wdata_param);
	int mcopipe_resp_cmd(std::string mcopipeif_name, ac_var ** rdy_var, ac_var * rdata_var);

	int wrfifoif_cmd(std::string wrfifoif_name, ac_var * req_var, ac_var * ack_var, ac_var * wdata_var);
	int wrfiforeq_cmd(std::string wrfifoif_name, std::vector<ac_param> params);

	int rdfifoif_cmd(std::string rdfifoif_name, ac_var * req_var, ac_var * ack_var, ac_var * rdata_var);
	int rdfiforeq_cmd(std::string rdfifoif_name, std::string * int_varname);

	int export_cmd();

}
#endif /* AC_SYNC_H_ */
