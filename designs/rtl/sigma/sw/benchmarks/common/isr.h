#ifndef SIGMA_ISR_H
#define SIGMA_ISR_H

int ConnectISR(int mcause, void (*new_isr)(int));

#endif // SIGMA_ISR_H