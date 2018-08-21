/*
 * ac_pipe_rtl_export.hpp
 *
 *  Created on: 26 июл. 2018 г.
 *      Author: alexander
 */

#include "ac_utils.hpp"
#include "ac_core.hpp"
#include "ac_pipe.hpp"
#include "ac_pipe_ir.hpp"

#ifndef INCLUDE_AC_PIPE_EXPORT_RTL_HPP_
#define INCLUDE_AC_PIPE_EXPORT_RTL_HPP_

namespace pipe
{

	int export_rtl(ac_cycled_ir* cycled_ir);

}


#endif /* INCLUDE_AC_PIPE_EXPORT_RTL_HPP_ */
