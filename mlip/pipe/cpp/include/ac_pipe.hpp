/*
 * ac_pipe.hpp
 *
 *  Created on: 31.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#ifndef AC_PIPE_H_
#define AC_PIPE_H_

#include "ac_utils.hpp"
#include "ac_core.hpp"
#include "ac_rtl.hpp"

namespace pipe
{
	extern char PIPE_OP_PPROC[];
	extern char PIPE_OP_PSTAGE[];

	extern char PIPE_OP_ASSIGN_ALWAYS[];
	extern char PIPE_OP_ASSIGN_SUCC[];

	extern char PIPE_OP_RDBUF[];

	extern char PIPE_OP_PKILL[];
	extern char PIPE_OP_PACTIVATE[];
	extern char PIPE_OP_PSTALL[];
	extern char PIPE_OP_PREPEAT[];
	extern char PIPE_OP_PFLUSH[];

	extern char PIPE_OP_PWE[];
	extern char PIPE_OP_PWE_ACTIVE[];
	extern char PIPE_OP_PWE_SUCC[];
	extern char PIPE_OP_PRE[];
	extern char PIPE_OP_PRR[];
	extern char PIPE_OP_ACCUM[];

	extern char PIPE_OP_ISACTIVE[];
	extern char PIPE_OP_ISWORKING[];
	extern char PIPE_OP_ISSTALLED[];
	extern char PIPE_OP_ISSUCC[];
	extern char PIPE_OP_ISBROKEN[];
	extern char PIPE_OP_ISFINISHED[];

	extern char PIPE_OP_MCOPIPE_REQ[];
	extern char PIPE_OP_MCOPIPE_RESP[];
	extern char PIPE_OP_SCOPIPE_REQ[];
	extern char PIPE_OP_SCOPIPE_RESP[];

	extern char PVAR_STRING[];
	extern char PSTICKY_STRING[];
	extern char PSTICKY_GLBL_STRING[];

	int reset_cmd();

	int SetPtrs();
	int pproc_cmd(std::string pproc_name, ac_var * clk_var, ac_var * rst_var);
	int endpproc_cmd();
	int pvar_cmd(std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int psticky_cmd(std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int psticky_glbl_cmd(std::string name_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int rdbuf_cmd(std::string gpvar_name, std::string * respvar_name);
	int assign_always_cmd(ac_dimensions dimensions, ac_var * target, ac_param param);
	int assign_succ_cmd(ac_dimensions dimensions, ac_var * target, ac_param param);
	int pstage_cmd(std::string pstage_name);
	int pkill_cmd();
	int pactivate_cmd(ac_var ** int_var);
	int pstall_cmd();
	int prepeat_cmd();
	int pflush_cmd();
	int pre_cmd(ac_var * ext_var, ac_var ** int_var);
	int pwe_cmd(ac_param param, ac_var * ext_var);
	int pwe_active_cmd(ac_param param, ac_var * ext_var);
	int pwe_succ_cmd(ac_param param, ac_var * ext_var);
	int prr_cmd(std::string pstage_name, ac_var * remote_var, ac_var ** int_var);
	int accum_cmd(ac_var * target, ac_param source);

	int isactive_cmd(std::string pstage_name, std::string * int_varname);
	int isworking_cmd(std::string pstage_name, std::string * int_varname);
	int isstalled_cmd(std::string pstage_name, std::string * int_varname);
	int issucc_cmd(std::string pstage_name, std::string * int_varname);
	int isbroken_cmd(std::string pstage_name, std::string * int_varname);
	int isfinished_cmd(std::string pstage_name, std::string * int_varname);

	int copipeif_cmd(std::string copipeif_name, ac_dimensions_static dimensions, ac_dimensions_static wdata_dimensions, ac_dimensions_static rdata_dimensions);

	int mcopipeif_cmd(std::string mcopipeif_name, ac_dimensions_static dimensions, ac_dimensions_static wdata_dimensions, ac_dimensions_static rdata_dimensions);
	int mcopipe_req_cmd(std::string mcopipeif_name, ac_dimensions dimensions, ac_var ** rdy_var, ac_param cmd_param, ac_param wdata_param);
	int mcopipe_resp_cmd(std::string mcopipeif_name, ac_var ** rdy_var, ac_var * rdata_var);
	int mcopipe_connect_cmd(std::string pproc_name, std::string mcopipeif_name, std::string copipeif_name);
	int mcopipe_export_cmd(std::string mcopipeif_name, unsigned int chnum, ac_var * req_var, ac_var * we_var, ac_var * ack_var, ac_var * wdata_var, ac_var * resp_var, ac_var * rdata_var);

	int scopipeif_cmd(std::string scopipeif_name, ac_dimensions_static wdata_dimensions, ac_dimensions_static rdata_dimensions);
	int scopipe_req_cmd(std::string scopipeif_name, ac_dimensions dimensions, ac_var ** rdy_var, ac_var * cmd_var, ac_var * rdata_var);
	int scopipe_resp_cmd(std::string scopipeif_name, ac_var ** rdy_var, ac_param wdata_param);
	int scopipe_connect_cmd(std::string copipeif_name, unsigned int chnum, std::string pproc_name, std::string scopipeif_name);

	int export_cmd();
	//int translate_cmd();
	//int export_rtl_cmd();
	//int export_hls_cmd();

	class mcopipe_if;

	struct prr_vector {
	public:
		std::string pipe_stage;
		ac_var * rdvar;

		prr_vector(std::string pipe_stage_in, ac_var * rdvar_in)
		{
			pipe_stage = pipe_stage_in;
			rdvar = rdvar_in;
		}
	};

	class mcopipe_if
	{
	public:
		std::string name;

		// external signals (request also used by pipeline)
		rtl::ac_comb * we_var;
		rtl::ac_comb * wdata_var;

		rtl::ac_comb * init_we;
		rtl::ac_comb * init_wdata;

		rtl::ac_syncbuf * reqbuf_we;
		rtl::ac_syncbuf * reqbuf_wdata;
		rtl::ac_syncbuf * reqbuf_rdy;

		rtl::ac_syncbuf * full_flag;
		rtl::ac_syncbuf * empty_flag;
		rtl::ac_syncbuf * wr_ptr;
		rtl::ac_syncbuf * rd_ptr;
		rtl::ac_comb * wr_ptr_next;
		rtl::ac_comb * rd_ptr_next;

		ac_dimensions_static dimensions;
		ac_dimensions_static rdata_dimensions;
		ac_dimensions_static rdata_ext_dimensions;

		rtl::ac_comb * req_var;
		rtl::ac_comb * ack_var;
		rtl::ac_comb * resp_var;
		rtl::ac_comb * resp_var_buf;
		rtl::ac_comb * rdata_var;
		rtl::ac_comb * init_req;
		rtl::ac_syncbuf * reqbuf_req;

		// used for identification
		ac_var * psticky_req_done;
		ac_var * psticky_resp_done;
		ac_var * psticky_rdata;
		ac_var * psticky_tid;
		ac_var * psticky_rdreq_pending;

		mcopipe_if(std::string name_in, ac_dimensions_static dimensions_in, ac_dimensions_static wdata_dimensions_in, ac_dimensions_static rdata_dimensions_in)
		{
			name = name_in;
			dimensions = dimensions_in;
			rdata_dimensions = rdata_dimensions_in;
			rdata_ext_dimensions = rdata_dimensions_in;

			rtl::comb_cmd(&we_var, GetGenName("mcopipe_var"), 0, 0, "0");
			rtl::comb_cmd(&wdata_var, GetGenName("mcopipe_var"), wdata_dimensions_in, "0");

			for (unsigned int dim_num = 0; dim_num < dimensions.size(); dim_num++)
			{
				rdata_ext_dimensions.push_back(dimensions[dim_num]);
			}
			rtl::comb_cmd(&req_var, GetGenName("mcopipe_var"), dimensions, "0");
			rtl::comb_cmd(&ack_var, GetGenName("mcopipe_var"), dimensions, "0");
			rtl::comb_cmd(&resp_var, GetGenName("mcopipe_var"), dimensions, "0");
			rtl::comb_cmd(&resp_var_buf, GetGenName("mcopipe_var"), dimensions, "0");
			rtl::comb_cmd(&rdata_var, GetGenName("mcopipe_var"), rdata_ext_dimensions, "0");

			psticky_req_done = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_req_done"), 0, 0, "0");
			psticky_resp_done = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_resp_done"), 0, 0, "0");
			psticky_rdata = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_rdata"), rdata_dimensions, "0");
			psticky_tid = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_tid"), 3, 0, "0");
			psticky_rdreq_pending = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_rdreq_pending"), dimensions, "0");
		}
	};

	class scopipe_if
	{
	public:
		std::string name;

		// external signals (request also used by pipeline)
		rtl::ac_comb * req_var;
		rtl::ac_comb * ack_var;
		rtl::ac_comb * we_var;
		rtl::ac_comb * wdata_var;
		rtl::ac_comb * resp_var;
		rtl::ac_comb * rdata_var;

		ac_dimensions_static wdata_dimensions;
		ac_dimensions_static rdata_dimensions;
		ac_dimensions_static rdata_ext_dimensions;

		// used for identification
		ac_var * psticky_req_done;
		ac_var * psticky_resp_done;
		ac_var * psticky_wdata;
		ac_var * psticky_rdreq_pending;

		scopipe_if(std::string name_in, ac_dimensions_static wdata_dimensions_in, ac_dimensions_static rdata_dimensions_in)
		{
			name = name_in;
			wdata_dimensions = wdata_dimensions_in;
			rdata_dimensions = rdata_dimensions_in;
			rdata_ext_dimensions = rdata_dimensions_in;

			rtl::comb_cmd(&req_var, GetGenName("mcopipe_var"), 0, 0, "0");
			rtl::comb_cmd(&ack_var, GetGenName("mcopipe_var"), 0, 0, "0");
			rtl::comb_cmd(&we_var, GetGenName("mcopipe_var"), 0, 0, "0");
			rtl::comb_cmd(&wdata_var, GetGenName("mcopipe_var"), wdata_dimensions_in, "0");
			rtl::comb_cmd(&resp_var, GetGenName("mcopipe_var"), 0, 0, "0");
			rtl::comb_cmd(&rdata_var, GetGenName("mcopipe_var"), rdata_dimensions_in, "0");

			psticky_req_done = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_req_done"), 0, 0, "0");
			psticky_resp_done = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_resp_done"), 0, 0, "0");
			psticky_wdata = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_wdata"), wdata_dimensions, "0");
			psticky_rdreq_pending = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_rdreq_pending"), 0, 0, "0");
		}
	};

	class copipe_if
	{
	public:
		std::string name;

		// external signals
		rtl::ac_comb * we_var;
		rtl::ac_comb * wdata_var;

		rtl::ac_comb * req_var;
		rtl::ac_comb * ack_var;
		rtl::ac_comb * resp_var;
		rtl::ac_comb * rdata_var;

		std::vector<bool> scopipe_connected;
		ac_dimensions_static dimensions;

		bool mcopipe_connected;

		copipe_if(std::string name_in, ac_dimensions_static dimensions_in, ac_dimensions_static wdata_dimensions, ac_dimensions_static rdata_dimensions)
		{
			name = name_in;
			dimensions = dimensions_in;

			rtl::comb_cmd(&we_var, GetGenName("copipe_var"), 0, 0, "0");
			rtl::comb_cmd(&wdata_var, GetGenName("copipe_var"), wdata_dimensions, "0");

			for (unsigned int dim_num = 0; dim_num < dimensions.size(); dim_num++)
			{
				rdata_dimensions.push_back(dimensions[dim_num]);
			}
			rtl::comb_cmd(&req_var, GetGenName("copipe_var"), dimensions, "0");
			rtl::comb_cmd(&ack_var, GetGenName("copipe_var"), dimensions, "0");
			rtl::comb_cmd(&resp_var, GetGenName("copipe_var"), dimensions, "0");
			rtl::comb_cmd(&rdata_var, GetGenName("copipe_var"), rdata_dimensions, "0");

			// TODO: large dimensions
			for (unsigned int dim_num = 0; dim_num < dimensions[0].GetWidth(); dim_num++)
			{
				scopipe_connected.push_back(false);
			}

			mcopipe_connected = false;
		}
	};

	class ac_pproc : public ac_execode
	{
	public:
		std::string name;

		ac_var * clk;
		ac_var * rst;

		std::vector<ac_var*> pvars;
		std::vector<ac_var*> pstickys;
		std::vector<ac_var*> psticky_glbls;

		std::vector<mcopipe_if*> mcopipe_ifs;
		std::vector<scopipe_if*> scopipe_ifs;

		std::vector<prr_vector> prr_vectors;

		ac_pproc(std::string pproc_name, ac_var * clk_in, ac_var * rst_in);

		bool GetMcopipeIf(std::string mcopipeif_name, mcopipe_if ** mcopipe_if_fetched);
		bool GetScopipeIf(std::string scopipeif_name, scopipe_if ** scopipe_if_fetched);
	};

	extern std::deque<ac_pproc*> Pprocs;

}
#endif /* AC_PIPE_H_ */
