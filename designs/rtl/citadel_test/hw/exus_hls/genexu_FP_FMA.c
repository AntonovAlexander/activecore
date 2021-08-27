#include "fp_exu.h"

t_dataout genexu_FP_FMA (t_datain datain) {
#pragma HLS pipeline II=1
	t_dataout ret_val;
	ret_val.trx_id = datain.trx_id;
	ret_val.wdata = (datain.rs0_rdata * datain.rs1_rdata) + datain.rs2_rdata;
	ret_val.tag = datain.rd0_tag;
	return ret_val;
}
