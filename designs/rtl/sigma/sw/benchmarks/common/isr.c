#include "io.h"
#include "isr.h"

#define DUMMY_ISR_PTR 0xffffffff
#define ISR_SIZE 10
void (*isr[ISR_SIZE])() = {[0 ... 9] = (void*)DUMMY_ISR_PTR};

void __int_handler (int mcause)
{
  if ((isr[mcause] != (void*)DUMMY_ISR_PTR) && (mcause < ISR_SIZE)) {
    isr[mcause]();
  }
}

void ConnectISR( int cause_num, void (*new_isr)()) {
  isr[cause_num] = new_isr;
}