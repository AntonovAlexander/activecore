// See LICENSE for license details.

//**************************************************************************
// Test program for interrupt controller
//--------------------------------------------------------------------------

#include "sfr.h"
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
  ConnectISR(BTN_IRQ_NUM, &int_handler);
  SFR_IRQ_EN = 0x1 << BTN_IRQ_NUM;
  while (1) {}
}
