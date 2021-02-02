#include "fp_exu.h"

t_dataout fp_add_sub (t_datain datain) {
#pragma HLS pipeline II=1
	s_dataout ret_val;
	if (datain.opcode == 0) {
		ret_val.rd_wdata = datain.rs0 + datain.rs1;
	} else {
		ret_val.rd_wdata = datain.rs0 - datain.rs1;
	}
	ret_val.rd_tag = datain.rd_tag;
	return ret_val;
}
