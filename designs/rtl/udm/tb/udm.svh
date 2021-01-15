class udm_driver;

    // udm interface
    `define SYNC_BYTE			8'h55
    `define ESCAPE_BYTE			8'h5a
    `define IDCODE_CMD			8'h00	// check udm accessibility
    `define RST_CMD				8'h80	// Reset slave	
    `define nRST_CMD			8'hC0	// nReset slave	
    `define WR_CMD 				8'h81	// Write slave with autoincrement
    `define RD_CMD 				8'h82	// Read slave with autoincrement
    `define WR_CMD_NOINC 		8'h83	// Write slave without autoincrement
    `define RD_CMD_NOINC 		8'h84	// Read slave without autoincrement
    
    logic parity;
    integer i, j, k;
    
    logic [31:0] rate;
    logic [1:0] configuration;
    
    task cfg (input logic [31:0] rate_bi, input logic [1:0] configuration_bi);
        begin
        rate = rate_bi;
        configuration = configuration_bi;
        end
    endtask
    
    ////Send byte to UART////
    
    task UART_SEND_SERIALIZE
        (
         input logic [7:0] send_byte
         );
        begin
        parity = 0;
        //start
        `UDM_RX_SIGNAL = 1'b0;
        #rate;
        //sending data
        for (i=0; i<8; i=i+1)
            begin
            `UDM_RX_SIGNAL = send_byte[0];
            #rate;
            parity = parity ^ send_byte[0];
            send_byte = send_byte >> 1;
            end
        //parity
        if (configuration != 2'b00)
            begin
            if (configuration == 2'b10)
                begin
                `UDM_RX_SIGNAL = parity;
                #rate;
                end
            else if (configuration == 2'b01)
                begin
                `UDM_RX_SIGNAL = ~parity;
                #rate;
                end
            end
        //stop;
            `UDM_RX_SIGNAL = 1'b1;
            #rate;
        end
    endtask
    
    task UART_SEND 
        (
         input logic [7:0] send_byte
         );
        begin
        @(posedge `UDM_BLOCK.uart_rx.clk_i)
        `UDM_BLOCK.uart_rx.rx_done_tick_o <= 1'b1;
        `UDM_BLOCK.uart_rx.dout_bo <= send_byte;
        @(posedge `UDM_BLOCK.uart_rx.clk_i)
        `UDM_BLOCK.uart_rx.rx_done_tick_o <= 1'b0;
        `UDM_BLOCK.uart_rx.dout_bo <= 8'h0;
        end
    endtask
    
    task rst ();
        begin
        UART_SEND(`SYNC_BYTE);
        UART_SEND(`RST_CMD);
        end
    endtask
    
    task nrst ();
        begin
        UART_SEND(`SYNC_BYTE);
        UART_SEND(`nRST_CMD);
        end
    endtask
    
    task hreset ();
        begin
        rst();
        nrst();
        end
    endtask
    
    task check ();
        begin
        UART_SEND(`SYNC_BYTE);
        UART_SEND(`IDCODE_CMD);
        end
    endtask
    
    task udm_sendbyte
        (
            input logic [7:0] databyte
        );
        begin
        if ((databyte == `SYNC_BYTE) || (databyte == `ESCAPE_BYTE))
            UART_SEND(`ESCAPE_BYTE);
        UART_SEND(databyte);
        end
    endtask
    
    
    task udm_sendword32
        (
            input logic [31:0] dataword
        );
        begin
        udm_sendbyte(dataword[7:0]);
        udm_sendbyte(dataword[15:8]);
        udm_sendbyte(dataword[23:16]);
        udm_sendbyte(dataword[31:24]);
        end
    endtask
    
    
    task wr32
        (
            input logic [31:0] wr_addr,
            input logic [31:0] wr_data
        );
        begin
    
        // header
        UART_SEND(`SYNC_BYTE);
        UART_SEND(`WR_CMD);
        
        // address
        udm_sendword32(wr_addr);
    
        // length
        udm_sendword32(32'h4);
        
        // data
        udm_sendword32(wr_data);
        
        $display("UDM WR32: addr: 0x%8x, data: 0x%8x", wr_addr, wr_data);
    
        end
    endtask
    
    
    task rd32
        (
            input logic [31:0] wr_addr
        );
        begin
        
        // header
        UART_SEND(`SYNC_BYTE);
        UART_SEND(`RD_CMD);
        
        // address
        udm_sendword32(wr_addr);
    
        // length
        udm_sendword32(32'h4);
        
        do begin
            @(posedge `UDM_BLOCK.clk_i);
        end while (!`UDM_BLOCK.bus_resp_i);
        $display("UDM RD32: addr: 0x%8x, data: 0x%8x", wr_addr, `UDM_BLOCK.bus_rdata_bi);
    
        end
    endtask
    
    task wrarr32
        (
            input logic [31:0] wr_addr,
            input logic [31:0] wr_data []
        );
        begin
        
        integer i;
    
        // header
        UART_SEND(`SYNC_BYTE);
        UART_SEND(`WR_CMD);
        
        // address
        udm_sendword32(wr_addr);
    
        // length
        udm_sendword32(wr_data.size() * 4);
        
        // data
        $display("-- UDM WRARR32: addr: 0x%8x, length: %4d --", wr_addr, wr_data.size());
        for (i=0; i<wr_data.size(); i=i+1)
            begin
            udm_sendword32(wr_data[i]);   
            $display("UDM WR32: addr: 0x%8x, data[%4d]: 0x%8x", (wr_addr + i*4), i, wr_data[i]);
            end
        $display("-- UDM WRARR32 complete --");
    
        end
    endtask
    
    task rdarr32
        (
            input logic [31:0] wr_addr,
            input integer size
        );
        begin
        
        integer i;
    
        // header
        UART_SEND(`SYNC_BYTE);
        UART_SEND(`RD_CMD);
        
        // address
        udm_sendword32(wr_addr);
    
        // length
        udm_sendword32(size * 4);
        
        // data
        $display("-- UDM RDARR32: addr: 0x%8x, length: %4d --", wr_addr, size);
        for (i=0; i<size; i=i+1)
            begin
            @(posedge `UDM_BLOCK.bus_resp_i)
            $display("UDM RD32: addr: 0x%8x, data[%4d]: 0x%8x", (wr_addr + i*4), i, `UDM_BLOCK.bus_rdata_bi);
            end
        $display("-- UDM RDARR32 complete --");
    
        end
    endtask

endclass : udm_driver
