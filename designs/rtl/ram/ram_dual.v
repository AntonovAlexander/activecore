module ram_dual
  #(
    parameter init_type="hex", init_data="data.hex", dat_width=32, adr_width=32, mem_size=1024
  )
  (
    input [dat_width-1:0]      dat0_i,
    input [adr_width-1:0]      adr0_i,
    input          we0_i,
    output reg [dat_width-1:0] dat0_o,

    input [dat_width-1:0]      dat1_i,
    input [adr_width-1:0]      adr1_i,
    input          we1_i,
    output reg [dat_width-1:0] dat1_o,

    input          clk
  ); 

//(* ram_style="block" *)
reg [dat_width-1:0] ram [0:mem_size - 1] ;
   
always @ (posedge clk)
    begin
    dat0_o <= ram[adr0_i];
    if (we0_i)
        ram[adr0_i] <= dat0_i;
    end

always @ (posedge clk)
    begin
    dat1_o <= ram[adr1_i];
    if (we1_i)
        ram[adr1_i] <= dat1_i;
    end

// elf processing
integer File_ID, Rd_Status;
reg [7:0] File_Rdata [0 : (mem_size * (dat_width / 8)) - 1] ;
integer File_ptr, header_idx;
integer e_machine, e_phnum, p_offset, p_vaddr, p_filesz, elf_param;
integer bytes_in_word, load_byte_counter;
integer ram_ptr, wrword_byte_counter;
reg [dat_width-1:0] wrword;
reg [8*8:0] e_machine_str;

initial
begin
  if (init_type != "none")
    begin
    if (init_type == "hex") $readmemh(init_data, ram, 0);
    else if (init_type == "elf")
        begin
        
        File_ID = $fopen(init_data, "rb");
        Rd_Status = $fread(File_Rdata, File_ID);
        if (Rd_Status == 0) $fatal("File %s not found!", init_data);
        
        $display("\n##################################");
        $display("#### Loading elf file: %s", init_data);
        
        // parsing ELF header
        if ((File_Rdata[0] != 8'h7f) || (File_Rdata[1] != 8'h45) || (File_Rdata[2] != 8'h4c) || (File_Rdata[3] != 8'h46)) $fatal("%s: elf format incorrect!", init_data);
        e_machine = File_Rdata[18] + (File_Rdata[19] << 8);
        e_machine_str = "UNKNOWN";
        if (e_machine == 32'hF3) e_machine_str = "RISC-V";
        $display("e_machine: 0x%x (%s)", e_machine, e_machine_str);
        e_phnum = File_Rdata[44] + (File_Rdata[45] << 8);
        $display("e_phnum: 0x%x", e_phnum);
        
        File_ptr = 52;
        for (header_idx = 0; header_idx < e_phnum; header_idx = header_idx + 1)
            begin
            
            // parsing program header
            $display("---- HEADER: %0d ----", header_idx);
            
            elf_param = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_type: 0x%x", elf_param);
            File_ptr = File_ptr + 4;
            
            p_offset = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_offset: 0x%x", p_offset);
            File_ptr = File_ptr + 4;
            
            p_vaddr = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_vaddr: 0x%x", p_vaddr);
            File_ptr = File_ptr + 4;
            
            elf_param = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_paddr: 0x%x", elf_param);
            File_ptr = File_ptr + 4;
            
            p_filesz = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_filesz: 0x%x", p_filesz);
            File_ptr = File_ptr + 4;
            
            elf_param = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_memsz: 0x%x", elf_param);
            File_ptr = File_ptr + 4;
            
            elf_param = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_flags: 0x%x", elf_param);
            File_ptr = File_ptr + 4;
            
            elf_param = File_Rdata[File_ptr] + (File_Rdata[File_ptr+1] << 8) + (File_Rdata[File_ptr+2] << 16) + (File_Rdata[File_ptr+3] << 24);
            $display("p_align: 0x%x", elf_param);
            File_ptr = File_ptr + 4;
            
            // loading segment to memory
            bytes_in_word = dat_width / 8;
            for (load_byte_counter = 0; load_byte_counter < p_filesz; load_byte_counter = load_byte_counter + bytes_in_word)
                begin
                wrword = 0;
                for (wrword_byte_counter = 0; wrword_byte_counter < bytes_in_word; wrword_byte_counter = wrword_byte_counter + 1)
                    begin
                    wrword = {File_Rdata[p_offset + load_byte_counter + wrword_byte_counter], wrword[dat_width-1:8]};
                    end
                ram_ptr = (p_vaddr + load_byte_counter) / bytes_in_word;
                ram[ram_ptr] = wrword;
                end
            end
        $display("##################################\n");
        $fclose(File_ID);
        end
    else $fatal("init_type parameter incorrect!");
    end
end


endmodule // ram
