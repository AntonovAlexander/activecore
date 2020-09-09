#include "io.h"
#include "isr.h"

#define DUMMY_ISR_PTR 0xffffffff
#define ISR_SIZE 10
void (*isr[ISR_SIZE])(int) = {[0 ... 9] = (void*)DUMMY_ISR_PTR};

void __int_handler(int mcause)
{
  if ((isr[mcause] != (void*)DUMMY_ISR_PTR) && (mcause < ISR_SIZE)) {
    isr[mcause](mcause);
  }
}

int ConnectISR(int mcause, void (*new_isr)(int)) {
  if (mcause < ISR_SIZE) {
    isr[mcause] = new_isr;
    return 0;
  } else {
    return -1;
  }
}