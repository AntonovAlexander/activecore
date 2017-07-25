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

	extern char PVAR_STRING[];

	int reset_cmd();

	int pproc_cmd(std::string pproc_name, std::string clk_varname, std::string rst_varname);
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
	int isworking_cmd(std::string pstage_name, std::string * int_varname);
	int isstalled_cmd(std::string pstage_name, std::string * int_varname);
	int issucc_cmd(std::string pstage_name, std::string * int_varname);
	int isbroken_cmd(std::string pstage_name, std::string * int_varname);
	int isfinished_cmd(std::string pstage_name, std::string * int_varname);

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
