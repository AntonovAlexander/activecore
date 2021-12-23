#include "genexu_FP_ADD_SUB.hpp"

resp_struct genexu_FP_ADD_SUB (req_struct datain) {
#pragma HLS pipeline II=1
	resp_struct ret_val;
	if (datain.opcode == 0) {
		ret_val.rd0_wdata = datain.src0_data + datain.src1_data;
	} else {
		ret_val.rd0_wdata = datain.src0_data - datain.src1_data;
	}
	ret_val.trx_id = datain.trx_id;
	ret_val.rd0_req = datain.rd0_req;
	ret_val.rd0_tag = datain.rd0_tag;
	return ret_val;
}
