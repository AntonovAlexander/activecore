/*
 * sigma_tile.svh
 *
 *  Created on: 01.01.2020
 *      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
 *     License: See LICENSE file for details
 */


`ifndef __SIGMA_TILE_H
  `define __SIGMA_TILE_H

  interface MemSplit32 ();
    logic req;
    logic ack;
    logic [31:0] addr;
    logic we;
    logic [31:0] wdata;
    logic [3:0] be;
    logic resp;
    logic [31:0] rdata;

    modport Master  (output req, input ack, output addr, output we, output wdata, output be, input resp, input rdata);
    modport Slave   (input req, output ack, input addr, input we, input wdata, input be, output resp, output rdata);
    modport Monitor (input req, input ack, input addr, input we, input wdata, input be, input resp, input rdata);
  endinterface

`endif    // __SIGMA_TILE_H
