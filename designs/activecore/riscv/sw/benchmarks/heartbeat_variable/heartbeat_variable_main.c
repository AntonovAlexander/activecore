// See LICENSE for license details.

//**************************************************************************
// Median filter bencmark
//--------------------------------------------------------------------------
//
// This benchmark performs a 1D three element median filter. The
// input data (and reference data) should be generated using the
// median_gendata.pl perl script and dumped to a file named
// dataset1.h.

//--------------------------------------------------------------------------
// Input/Reference Data

#define IO_LED          (*(volatile unsigned int *)(0x80000000))
#define IO_SW            (*(volatile unsigned int *)(0x80000004))


//--------------------------------------------------------------------------
// Main

int main( int argc, char* argv[] )
{
  int led_counter;
  int i;

  led_counter = 0;

  while (1)
  {

    for (i = 0; i < IO_SW; i++) {}
    led_counter++;
    IO_LED = led_counter;
  }
}
