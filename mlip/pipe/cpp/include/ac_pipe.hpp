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
#include "ac_pipe_ir.hpp"
#include "ac_pipe_export_rtl.hpp"
#include "ac_pipe_export_hls.hpp"

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
	extern char PIPE_OP_ISKILLED[];
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
	int pmodule_begin_cmd(std::string pproc_name);
	int pmodule_end_cmd();
	int pvar_cmd(std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int psticky_cmd(std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int psticky_glbl_cmd(std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in);
	int rdbuf_cmd(std::string gpvar_name, std::string * respvar_name);
	int assign_always_cmd(VarSegmentVector VarSegments, ac_var * target, ac_param* param);
	int assign_succ_cmd(VarSegmentVector VarSegments, ac_var * target, ac_param* param);
	int pstage_cmd(std::string pstage_name);
	int pkill_cmd();
	int pactivate_cmd(ac_var ** int_var);
	int pstall_cmd();
	int prepeat_cmd();
	int pflush_cmd();
	int pre_cmd(ac_var * ext_var, ac_var ** int_var);
	int pwe_cmd(ac_param* param, ac_var * ext_var);
	int pwe_active_cmd(ac_param* param, ac_var * ext_var);
	int pwe_succ_cmd(ac_param* param, ac_var * ext_var);
	int prr_cmd(std::string pstage_name, ac_var * remote_var, ac_var ** int_var);
	int accum_cmd(ac_var * target, ac_param* source);

	int isactive_cmd(std::string pstage_name, std::string * int_varname);
	int isworking_cmd(std::string pstage_name, std::string * int_varname);
	int isstalled_cmd(std::string pstage_name, std::string * int_varname);
	int issucc_cmd(std::string pstage_name, std::string * int_varname);
	int iskilled_cmd(std::string pstage_name, std::string * int_varname);
	int isfinished_cmd(std::string pstage_name, std::string * int_varname);

	int copipeif_cmd(std::string copipeif_name, ac_dimensions_static wdata_dimensions, ac_dimensions_static rdata_dimensions);

	int mcopipeif_cmd(std::string mcopipeif_name, VarType wdata_vartype_in, ac_dimensions_static wdata_dimensions_in, VarType rdata_vartype_in, ac_dimensions_static rdata_dimensions_in);
	int mcopipe_req_cmd(std::string mcopipeif_name, ac_var ** rdy_var, ac_param* cmd_param, ac_param* wdata_param);
	int mcopipe_resp_cmd(std::string mcopipeif_name, ac_var ** rdy_var, ac_var * rdata_var);
	int mcopipe_connect_cmd(std::string pproc_name, std::string mcopipeif_name, std::string copipeif_name);
	int mcopipe_export_cmd(std::string mcopipeif_name, ac_var * req_var, ac_var * we_var, ac_var * ack_var, ac_var * wdata_var, ac_var * resp_var, ac_var * rdata_var);

	int scopipeif_cmd(std::string scopipeif_name, VarType wdata_vartype_in, ac_dimensions_static wdata_dimensions_in, VarType rdata_vartype_in, ac_dimensions_static rdata_dimensions_in);
	int scopipe_req_cmd(std::string scopipeif_name, ac_var ** rdy_var, ac_var * cmd_var, ac_var * rdata_var);
	int scopipe_resp_cmd(std::string scopipeif_name, ac_var ** rdy_var, ac_param* wdata_param);
	int scopipe_connect_cmd(std::string copipeif_name, unsigned int chnum, std::string pproc_name, std::string scopipeif_name);

	int translate_cmd();
	int export_rtl_cmd(std::string language, std::string pathname);
	int export_hls_cmd(std::string system, std::string pathname);

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

		// external interfaces
		ir_wrfifo_if * req_fifo;
		ir_rdfifo_if * resp_fifo;

		// fifo management
		ac_var * full_flag;
		ac_var * empty_flag;
		ac_var * wr_ptr;
		ac_var * rd_ptr;
		ac_var * wr_ptr_next;
		ac_var * rd_ptr_next;

		VarType wdata_vartype;
		ac_dimensions_static wdata_dimensions;
		VarType rdata_vartype;
		ac_dimensions_static rdata_dimensions;

		// used for identification
		ac_var * psticky_req_done;
		ac_var * psticky_resp_done;
		ac_var * psticky_rdata;
		ac_var * psticky_tid;
		ac_var * psticky_rdreq_pending;

		mcopipe_if(std::string name_in, VarType wdata_vartype_in, ac_dimensions_static wdata_dimensions_in, VarType rdata_vartype_in, ac_dimensions_static rdata_dimensions_in)
		{
			name = name_in;

			wdata_vartype = wdata_vartype_in;
			wdata_dimensions = wdata_dimensions_in;
			rdata_vartype = rdata_vartype_in;
			rdata_dimensions = rdata_dimensions_in;

			psticky_req_done = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_req_done"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");
			psticky_resp_done = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_resp_done"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");
			psticky_rdata = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_rdata"), rdata_vartype_in, rdata_dimensions, "0");
			psticky_tid = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_tid"), VarType(VAR_TYPE::vt_unsigned), 3, 0, "0");
			psticky_rdreq_pending = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_rdreq_pending"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");
		}
	};

	class scopipe_if
	{
	public:
		std::string name;

		// external signals (request also used by pipeline)
		ac_var * req_var;
		ac_var * ack_var;
		ac_var * we_var;
		ac_var * wdata_var;
		ac_var * resp_var;
		ac_var * rdata_var;

		VarType wdata_vartype;
		ac_dimensions_static wdata_dimensions;
		VarType rdata_vartype;
		ac_dimensions_static rdata_dimensions;

		// used for identification
		ac_var * psticky_req_done;
		ac_var * psticky_resp_done;
		ac_var * psticky_wdata;
		ac_var * psticky_rdreq_pending;

		scopipe_if(std::string name_in, VarType wdata_vartype_in, ac_dimensions_static wdata_dimensions_in, VarType rdata_vartype_in, ac_dimensions_static rdata_dimensions_in)
		{
			name = name_in;

			wdata_vartype = wdata_vartype_in;
			wdata_dimensions = wdata_dimensions_in;
			rdata_vartype = rdata_vartype_in;
			rdata_dimensions = rdata_dimensions_in;

			ir_local_cmd(&req_var, GetGenName("mcopipe_var"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");
			ir_local_cmd(&ack_var, GetGenName("mcopipe_var"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");
			ir_local_cmd(&we_var, GetGenName("mcopipe_var"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");
			ir_local_cmd(&wdata_var, GetGenName("mcopipe_var"), wdata_vartype, wdata_dimensions_in, "0");
			ir_local_cmd(&resp_var, GetGenName("mcopipe_var"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");
			ir_local_cmd(&rdata_var, GetGenName("mcopipe_var"), rdata_vartype_in, rdata_dimensions_in, "0");

			psticky_req_done = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_req_done"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");
			psticky_resp_done = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_resp_done"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");
			psticky_wdata = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_wdata"), wdata_vartype, wdata_dimensions, "0");
			psticky_rdreq_pending = new ac_var(PSTICKY_STRING, ("genmcopipe_" + name + "_genvar_rdreq_pending"), VarType(VAR_TYPE::vt_unsigned), 0, 0, "0");
		}
	};

	class copipe_if
	{
	public:
		std::string name;

		// external signals
		ac_var * we_var;
		ac_var * wdata_var;

		ac_var * req_var;
		ac_var * ack_var;
		ac_var * resp_var;
		ac_var * rdata_var;

		bool mcopipe_connected;
		bool scopipe_connected;

		copipe_if(std::string name_in, ac_dimensions_static wdata_dimensions, ac_dimensions_static rdata_dimensions)
		{
			name = name_in;

			/*
			ir_local_cmd(&we_var, GetGenName("copipe_var"), 0, 0, "0");
			ir_local_cmd(&wdata_var, GetGenName("copipe_var"), wdata_dimensions, "0");

			ir_local_cmd(&req_var, GetGenName("copipe_var"), 0, 0, "0");
			ir_local_cmd(&ack_var, GetGenName("copipe_var"), 0, 0, "0");
			ir_local_cmd(&resp_var, GetGenName("copipe_var"), 0, 0, "0");
			ir_local_cmd(&rdata_var, GetGenName("copipe_var"), rdata_dimensions, "0");
			*/

			mcopipe_connected = false;
			scopipe_connected = false;
		}
	};

	class ac_pproc : public ac_execode
	{
	public:
		std::string name;

		std::vector<ac_var*> pvars;
		std::vector<ac_var*> pstickys;
		std::vector<ac_var*> psticky_glbls;

		std::vector<mcopipe_if*> mcopipe_ifs;
		std::vector<scopipe_if*> scopipe_ifs;

		std::vector<prr_vector> prr_vectors;

		ac_pproc(std::string pproc_name);

		bool GetMcopipeIf(std::string mcopipeif_name, mcopipe_if ** mcopipe_if_fetched);
		bool GetScopipeIf(std::string scopipeif_name, scopipe_if ** scopipe_if_fetched);
	};

	extern std::deque<ac_pproc*> Pprocs;

}
#endif /* AC_PIPE_H_ */
