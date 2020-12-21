#include "io.h"

void main (void)
{
  int count = 0;
  while(1)
  {
    IO_LED = ++count;
  }
}