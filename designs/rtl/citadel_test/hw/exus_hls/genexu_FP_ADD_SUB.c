#include "fp_exu.h"

t_dataout genexu_FP_ADD_SUB (t_datain datain) {
#pragma HLS pipeline II=1
	s_dataout ret_val;
	if (datain.opcode == 0) {
		ret_val.wdata = datain.rs0 + datain.rs1;
	} else {
		ret_val.wdata = datain.rs0 - datain.rs1;
	}
	ret_val.trx_id = datain.trx_id;
	ret_val.tag = datain.rd_tag;
	return ret_val;
}
