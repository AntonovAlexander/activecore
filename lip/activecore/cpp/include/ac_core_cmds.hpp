/*
 * ac_core_cmds.hpp
 *
 *  Created on: 31.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#ifndef AC_CORE_CMDS_H_
#define AC_CORE_CMDS_H_

#include <stdio.h>
#include <iostream>
#include <vector>
#include <string>

#include "ac_core.hpp"

// External cmds //
int expr_assign_cmd_generated(ac_dimensions dimensions, ac_var* target, ac_param param);
int expr_assign_cmd(ac_dimensions dimensions, ac_var* target, ac_param param);
int expr_assign_cmd(ac_var* target, ac_param param);
int expr_op_cmd_generated(ac_execode** new_expr, std::string opcode, std::vector<ac_param> params, ac_var * target);
int expr_op_cmd(std::string opcode, std::vector<ac_param> params, ac_var** respvar);

int expr_1op_cmd(std::string opcode, ac_param param, ac_var** respvar);
int expr_2op_cmd(std::string opcode, ac_param param0, ac_param param1, ac_var** respvar);

int expr_zeroext_cmd(unsigned int target_width, ac_param param, ac_var** respvar);
int expr_signext_cmd(unsigned int target_width, ac_param param, ac_var** respvar);
int expr_initval_cmd(unsigned int width, std::string value, ac_var** respvar);
int expr_begif_cmd(ac_var* cond_op);
int expr_begelsif_cmd(ac_var* cond_op);
int expr_begelse_cmd();
int expr_endif_cmd();

#endif /* AC_CORE_CMDS_H_ */
