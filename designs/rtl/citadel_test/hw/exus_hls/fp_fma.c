#include "fp_exu.h"

t_dataout fp_fma (t_datain datain) {
#pragma HLS pipeline II=1
	s_dataout ret_val;
	ret_val.rd_wdata = (datain.rs0 * datain.rs1) + datain.rs2;
	ret_val.rd_tag = datain.rd_tag;
	return ret_val;
}
