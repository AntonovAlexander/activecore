// See LICENSE for license details.

//**************************************************************************
// Test program for interrupt controller
//--------------------------------------------------------------------------

#include "io.h"
#include "isr.h"


//--------------------------------------------------------------------------
// Main

int led_counter;

void SetLED(int value) {
  IO_LED = value;
}

void int_handler(int mcause) {
  SetLED(++led_counter);
}

int main( int argc, char* argv[] )
{
  led_counter = 1;
  SetLED(led_counter);
  ConnectISR(0x3, &int_handler);
  while (1) {}
}
