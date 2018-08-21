/*
 * ac_pipe_export_hls.hpp
 *
 *  Created on: 3 авг. 2018 г.
 *      Author: alexander
 */

#include "ac_utils.hpp"
#include "ac_core.hpp"
#include "ac_pipe.hpp"
#include "ac_pipe_ir.hpp"

#ifndef INCLUDE_AC_PIPE_EXPORT_HLS_HPP_
#define INCLUDE_AC_PIPE_EXPORT_HLS_HPP_


namespace pipe
{

	int export_hls(ac_cycled_ir* cycled_ir, std::string system, std::string pathname);

}


#endif /* INCLUDE_AC_PIPE_EXPORT_HLS_HPP_ */
