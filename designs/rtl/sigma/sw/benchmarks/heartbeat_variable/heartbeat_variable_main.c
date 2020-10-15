// See LICENSE for license details.

//**************************************************************************
// HeartBeat with switches-controllable delay
//--------------------------------------------------------------------------

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
