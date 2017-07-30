/*
 * ac_utils.cpp
 *
 *  Created on: 31.01.2017
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */

#include "ac_utils.hpp"

char * StringToCharArr(std::string string_in)
{
	char * resp_data = new char[string_in.size() + 1];
	std::copy(string_in.begin(), string_in.end(), resp_data);
	resp_data[string_in.size()] = '\0';
	return resp_data;
}

int conv_string_to_int(std::string string_in)
{
	int conv_val;
	std::stringstream convert(string_in); // stringstream used for the conversion initialized with the contents of Text
	if ( !(convert >> conv_val) )//give the value to Result using the characters in the string
		conv_val = 0;//if that fails set Result to 0
	return conv_val;
}
