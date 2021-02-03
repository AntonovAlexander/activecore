#ifndef __FP_EXU_H
#define __FP_EXU_H

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>

typedef struct s_datain
{
    unsigned int opcode;
    float rs0;
    float rs1;
    float rs2;
    unsigned int rd_tag;
} t_datain;

typedef struct s_dataout
{
    unsigned int rd_tag;
    float rd_wdata;
} t_dataout;

#endif // __FP_EXU_H
