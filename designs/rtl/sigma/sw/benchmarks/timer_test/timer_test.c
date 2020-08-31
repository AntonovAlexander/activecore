// See LICENSE for license details.

//**************************************************************************
// Test program for timer
//--------------------------------------------------------------------------

#include "sfr.h"
#include "io.h"
#include "isr.h"


int led_counter;

void SetLED(int value) {
  IO_LED = value;
}

void timer_handler(int mcause) {
  SetLED(++led_counter);
}

//--------------------------------------------------------------------------
// Main

int main( int argc, char* argv[] )
{
  led_counter = 1;
  SetLED(led_counter);

  ConnectISR(TIMER_IRQ_NUM, &timer_handler);
  SFR_IRQ_EN = 0x1 << TIMER_IRQ_NUM;

  SFR_TIMER_PERIOD = IO_SW;
  SFR_TIMER_CTRL = TIMER_START_FLAG | TIMER_RELOAD_FLAG;

  while (1) {}
}
