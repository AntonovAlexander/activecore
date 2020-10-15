#include <stdio.h>
#include "crc32.h" 

#include "io.h"

int main(int argc, char* argv[])
{
   
    IO_LED = crc32(INPUT, LEN);
        
    while (1) {}
}