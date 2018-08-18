/*
 * ac_pipe_ir.hpp
 *
 *  Created on: 26 июл. 2018 г.
 *      Author: alexander
 */

#ifndef INCLUDE_AC_PIPE_IR_HPP_
#define INCLUDE_AC_PIPE_IR_HPP_

#include "ac_utils.hpp"
#include "ac_core.hpp"

namespace pipe
{
	extern char IR_PROC[];
	extern char IR_GLOBAL_STRING[];
	extern char IR_LOCAL_STRING[];
	extern char IR_PORT_IN[];
	extern char IR_PORT_OUT[];
	extern char IR_WRFIFO[];
	extern char IR_RDFIFO[];

	extern char IR_OP_WRFIFOREQ[];
	extern char IR_OP_RDFIFOREQ[];

	int ir_global_cmd(ac_var** new_global, std::string name, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval);
	int ir_global_cmd(ac_var** new_global, std::string name, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval);
	int ir_global_cmd(ac_var** new_global, std::string name, VarType VarType_in, std::string defval);

	int ir_local_cmd(ac_var** new_local, std::string name, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval);
	int ir_local_cmd(ac_var** new_local, std::string name, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval);
	int ir_local_cmd(ac_var** new_local, std::string name, VarType VarType_in, std::string defval);

	class ir_wrfifo_if : public ac_var
	{
	public:
			ir_wrfifo_if(std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in);
			ir_wrfifo_if(std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb);
			ir_wrfifo_if(std::string name_in, VarType VarType_in);
	};

	class ir_rdfifo_if : public ac_var
	{
	public:
			ir_rdfifo_if(std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in);
			ir_rdfifo_if(std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb);
			ir_rdfifo_if(std::string name_in, VarType VarType_in);
	};

	class ir_port : public ac_var
	{
	public:
			ir_port(char * type_name_in, std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in, std::string defval_in);
			ir_port(char * type_name_in, std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb, std::string defval_in);
	};

	int ir_wrfifo_if_cmd(ir_wrfifo_if ** new_wrfifo_if, std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in);
	int ir_wrfifo_if_cmd(ir_wrfifo_if ** new_wrfifo_if, std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb);
	int ir_wrfifo_if_cmd(ir_wrfifo_if ** new_wrfifo_if, std::string name_in, VarType VarType_in);

	int ir_rdfifo_if_cmd(ir_rdfifo_if ** new_rdfifo_if, std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in);
	int ir_rdfifo_if_cmd(ir_rdfifo_if ** new_rdfifo_if, std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb);
	int ir_rdfifo_if_cmd(ir_rdfifo_if ** new_rdfifo_if, std::string name_in, VarType VarType_in);

	int ir_port_in_cmd(ir_port ** new_port, std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in);
	int ir_port_in_cmd(ir_port ** new_port, std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb);

	int ir_port_out_cmd(ir_port ** new_port, std::string name_in, VarType VarType_in, ac_dimensions_static dimensions_in);
	int ir_port_out_cmd(ir_port ** new_port, std::string name_in, VarType VarType_in, unsigned int msb, unsigned int lsb);

	int ir_wrfifo_req(ac_var ** fifo_rdy, ir_wrfifo_if * fifo_if, ac_param* wrdata);
	int ir_rdfifo_req(ac_var ** fifo_rdy, ir_rdfifo_if * fifo_if, ac_var * rddata);

	class ac_pproc_ir : public ac_execode
	{
	public:
		std::string name;

		std::vector<ac_var*> ir_globals;
		std::vector<ac_var*> ir_locals;

		std::vector<ir_port*> ir_ports_in;
		std::vector<ir_port*> ir_ports_out;

		std::vector<ir_wrfifo_if*> ir_wrfifo_ifs;
		std::vector<ir_rdfifo_if*> ir_rdfifo_ifs;

		ac_pproc_ir(std::string name_in) : ac_execode(IR_PROC)
		{
			name = name_in;
		}
	};

	extern ac_pproc_ir* Pproc_ir;
	int set_new_pproc_ir(ac_pproc_ir ** new_proc_ir, std::string name);
}

#endif /* INCLUDE_AC_PIPE_IR_HPP_ */
