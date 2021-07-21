#include "fp_exu.h"

t_dataout genexu_FP_FMA (t_datain datain) {
#pragma HLS pipeline II=1
	s_dataout ret_val;
	ret_val.trx_id = datain.trx_id;
	ret_val.wdata = (datain.rs0 * datain.rs1) + datain.rs2;
	ret_val.tag = datain.rd_tag;
	return ret_val;
}
