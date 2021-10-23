// See LICENSE for license details.

//**************************************************************************
// Median filter benchmark
//--------------------------------------------------------------------------
//
// This benchmark performs a 1D three element median filter. The
// input data (and reference data) should be generated using the
// median_gendata.pl perl script and dumped to a file named
// dataset1.h.

//#include "util.h"

#include "median.h"

//--------------------------------------------------------------------------
// Input/Reference Data

#include "io.h"
#include "dataset1.h"

//--------------------------------------------------------------------------
// Main

int main( int argc, char* argv[] )
{
  IO_LED = 0x0;

  // Do the filter
  median( DATA_SIZE, input_data, io_buf_int );
  
  // Display status
  IO_LED = 0x55aa55aa;

  // hang
  while (1) {}
}
