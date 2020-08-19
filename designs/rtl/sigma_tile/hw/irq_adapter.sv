/*
 * irq_adapter.v
 *
 *  Created on: 16.10.2019
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


module irq_adapter
#(
    parameter IRQ_NUM_POW = 4
)
(
    input clk_i
    , input rst_i

    // external interface
    , input [(2**IRQ_NUM_POW)-1:0] irq_debounced_bi

    // sgi interface
    , input sgi_req_i
    , input [IRQ_NUM_POW-1:0] sgi_code_bi

    // cpu interface
    , output logic irq_req_o
    , output logic [IRQ_NUM_POW-1:0] irq_code_bo
    , input irq_ack_i
);

localparam IRQ_NUM = 2**IRQ_NUM_POW;

logic [IRQ_NUM-1:0] irq_buf0, irq_buf1, irq_posedge, irq_flags;
always @*
    begin
    irq_posedge = 0;
    for (integer i=0; i<IRQ_NUM; i++)
        begin
        if (!(irq_buf1[i]) && irq_buf0[i]) irq_posedge[i] = 1'b1;
        end
    end

logic irq_prior_req;
logic [IRQ_NUM_POW-1:0] irq_prior_code;
always @*
    begin
    integer i;
    irq_prior_req = 1'b0;
    irq_prior_code = 0;
    i = IRQ_NUM-1;
    do
        begin
        if (irq_flags[i])
            begin
            irq_prior_req = 1'b1;
            irq_prior_code = i;
            end
        i--;
        end
    while(i != 0);
    end

always @(posedge clk_i)
    begin
    if (rst_i)
        begin
        irq_buf0 <= 0;
        irq_buf1 <= 0;
        irq_flags <= 0;
        irq_req_o <= 1'b0;
        irq_code_bo <= 0;
        end
    else
        begin
        irq_buf0 <= irq_debounced_bi;
        irq_buf1 <= irq_buf0;
        
        irq_flags <= irq_flags | irq_posedge;               // ext irq detection
        if (irq_ack_i) irq_flags[irq_code_bo] <= 1'b0;      // irq clearing
        
        // choosing least significant flag
        irq_req_o <= 1'b0;
        irq_code_bo <= 0;
        if (irq_prior_req)
            begin
            irq_req_o <= 1'b1;
            irq_code_bo <= irq_prior_code;
            end
        
        if (sgi_req_i) irq_flags[sgi_code_bi] <= 1'b1;      // sgi irq detection
        end
    end

endmodule
