/*
 * ac_utils.hpp
 *
 *  Created on: 31.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#ifndef AC_UTILS_H_
#define AC_UTILS_H_

#include <stdio.h>
#include <iostream>
#include <string>
#include <vector>
#include <deque>
#include <sstream>
#include </usr/include/tcl/tcl.h>

char * StringToCharArr(std::string string_in);
int conv_string_to_int(std::string string_in);

template<typename T> std::string toString(const T& value)
{
    std::ostringstream oss;
    oss << value;
    return oss.str();
};

#endif /* AC_UTILS_H_ */
