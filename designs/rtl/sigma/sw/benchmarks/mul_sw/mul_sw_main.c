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
  // Initial status
  io_buf_int[0] = 5;
  io_buf_int[1] = 7;
  IO_LED = 0x55aa55aa;

  while (1) {
    IO_LED = mul_sw(io_buf_int[0], io_buf_int[1]);
  }
}
