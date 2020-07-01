// See LICENSE for license details.

//**************************************************************************
// Software multiplication
//--------------------------------------------------------------------------

//#include "util.h"

#include "mul_sw.h"

//--------------------------------------------------------------------------
// Input/Reference Data

#include "io.h"

//--------------------------------------------------------------------------
// Main

int main( int argc, char* argv[] )
{
  int * io_buf = (int*)IO_MEM_ADDR;
  
  // Initial status
  io_buf[0] = 5;
  io_buf[1] = 7;
  IO_LED = 0x55aa55aa;

  while (1) {
    IO_LED = mul_sw(io_buf[0], io_buf[1]);
  }
}
