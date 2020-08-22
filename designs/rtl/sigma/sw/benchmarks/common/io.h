#ifndef SIGMA_IO_H
#define SIGMA_IO_H

#define IO_LED          (*(volatile unsigned int *)(0x80000000))
#define IO_SW           (*(volatile unsigned int *)(0x80000004))
#define IO_MEM_ADDR   0x6000

#endif // SIGMA_IO_H