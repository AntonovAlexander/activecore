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
#include <list>
#include <sstream>
#include </usr/include/tcl/tcl.h>

char * StringToCharArr(std::string string_in);
int conv_string_to_int(std::string string_in);
unsigned int conv_string_to_uint(std::string string_in);

template<typename T> std::string toString(const T& value)
{
    std::ostringstream oss;
    oss << value;
    return oss.str();
};

template <typename T>
std::vector<T*> unite_vectors(std::vector<T*> vector0, std::vector<T*> vector1)
{
	std::vector<T*> new_vector;

	for (unsigned int i = 0; i < vector0.size(); i++)
	{
		bool element_is_new = true;
		for (unsigned int j = 0; j < new_vector.size(); j++)
		{
			if (new_vector[j] == vector0[i])
			{
				element_is_new = false;
				break;
			}
		}
		if (element_is_new) new_vector.push_back(vector0[i]);
	}

	for (unsigned int i = 0; i < vector1.size(); i++)
	{
		bool element_is_new = true;
		for (unsigned int j = 0; j < new_vector.size(); j++)
		{
			if (new_vector[j] == vector1[i])
			{
				element_is_new = false;
				break;
			}
		}
		if (element_is_new) new_vector.push_back(vector1[i]);
	}

	return new_vector;
}

template <typename T>
std::vector<T*> cross_vectors(std::vector<T*> vector0, std::vector<T*> vector1)
{
	std::vector<T*> new_vector;

	for (unsigned int i = 0; i < vector0.size(); i++)
	{
		for (unsigned int j = 0; j < vector1.size(); j++)
		{
			if (vector0[i] == vector1[j])
			{
				new_vector.push_back(vector0[i]);
				break;
			}
		}
	}

	return new_vector;
}

#endif /* AC_UTILS_H_ */
