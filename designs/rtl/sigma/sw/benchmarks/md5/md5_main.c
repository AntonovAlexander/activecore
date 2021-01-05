#include <string.h>
#include "io.h"
#include "md5.h" 


int main()
{
	char* testStr = "Hello World!";
	int length = strlen(testStr);
	md5s result = md5(testStr, length);
	
	for ( int i = 0; i < 4; ++i ){
		io_buf_uint[i] = result.v[i];
	}
        
    while (1) {}
}