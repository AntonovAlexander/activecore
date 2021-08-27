#include "fp_exu.h"

t_dataout genexu_FP_ADD_SUB (t_datain datain) {
#pragma HLS pipeline II=1
	t_dataout ret_val;
	if (datain.opcode == 0) {
		ret_val.wdata = datain.rs0_rdata + datain.rs1_rdata;
	} else {
		ret_val.wdata = datain.rs0_rdata - datain.rs1_rdata;
	}
	ret_val.trx_id = datain.trx_id;
	ret_val.tag = datain.rd0_tag;
	return ret_val;
}
