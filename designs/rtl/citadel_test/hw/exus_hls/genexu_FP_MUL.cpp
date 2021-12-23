#include "genexu_FP_MUL.hpp"

resp_struct genexu_FP_MUL (req_struct datain) {
#pragma HLS pipeline II=1
	resp_struct ret_val;
	ret_val.trx_id = datain.trx_id;
	ret_val.rd0_wdata = datain.src0_data * datain.src1_data;
	ret_val.rd0_req = datain.rd0_req;
	ret_val.rd0_tag = datain.rd0_tag;
	return ret_val;
}
