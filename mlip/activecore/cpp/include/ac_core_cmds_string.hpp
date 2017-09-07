/*
 * ac_core_cmds_string.hpp
 *
 *  Created on: 30.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#ifndef AC_CORE_CMDS_STRING_H_
#define AC_CORE_CMDS_STRING_H_


#include "ac_core.hpp"

// External cmds //
int expr_assign_cmd_string(std::string target, ac_param param);
int expr_op_cmd_string(std::string opcode, std::string * respvarname, std::vector<ac_param> params);
int expr_zeroext_cmd_string(std::string size, std::string * respvarname, std::vector<ac_param> params);
int expr_signext_cmd_string(std::string size, std::string * respvarname, std::vector<ac_param> params);
int expr_initval_cmd_string(std::string width, std::string * respvarname, std::string value);

int expr_begif_cmd_string(std::string cond_op);
int expr_begelsif_cmd_string(std::string cond_op);
int expr_begelse_cmd_string();
int expr_endif_cmd_string();

int expr_begwhile_cmd_string(std::string cond_op);
int expr_endwhile_cmd_string();

#endif /* AC_CORE_CMDS_STRING_H_ */
