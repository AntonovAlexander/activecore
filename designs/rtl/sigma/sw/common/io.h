#ifndef SIGMA_IO_H
#define SIGMA_IO_H

#define IO_LED            (*(volatile unsigned int *)(0x80000000))
#define IO_SW             (*(volatile unsigned int *)(0x80000004))
#define IO_BUF_ADDR       0x6000
#define IO_BUF_INT_LENGTH 0x2000
#define io_buf_uint       (*(volatile unsigned int (*)[IO_BUF_INT_LENGTH])(IO_BUF_ADDR))
#define io_buf_int        (*(volatile int (*)[IO_BUF_INT_LENGTH])(IO_BUF_ADDR))
#define io_buf_ushort     (*(volatile unsigned short (*)[IO_BUF_INT_LENGTH*2])(IO_BUF_ADDR))
#define io_buf_short      (*(volatile short (*)[IO_BUF_INT_LENGTH*2])(IO_BUF_ADDR))

#endif // SIGMA_IO_H