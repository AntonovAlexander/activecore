#ifndef __FP_EXU_H
#define __FP_EXU_H

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>

typedef struct s_datain
{
    unsigned int trx_id;
    unsigned int opcode;
    float src0_rdata;
    float src1_rdata;
    float src2_rdata;
    unsigned int rd0_tag;
} t_datain;

typedef struct s_dataout
{
    unsigned int trx_id;
    unsigned int tag;
    float wdata;
} t_dataout;

#endif // __FP_EXU_H
