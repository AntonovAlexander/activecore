#include "io.h"
#include "loadelf.h"

#define ELF_HEADER_SIZE      52
#define ELF_PROG_HEADER_SIZE 32

int loadelf( unsigned int elf_image_addr, unsigned int * entry_point ) {

  if ((*(unsigned int *)(elf_image_addr)) == 0x464c457f) {

    // parsing ELF header
    unsigned short e_entry = (*(unsigned short *)(elf_image_addr + 24));
    //IO_LED = 0x11111111;
    //IO_LED = e_entry;
    *entry_point = e_entry;

    unsigned short e_phnum = (*(unsigned short *)(elf_image_addr + 44));
    //IO_LED = 0x33333333;
    //IO_LED = e_phnum;

    for (unsigned int header_idx = 0; header_idx < e_phnum; header_idx++) {
      unsigned int prog_header_addr = elf_image_addr + ELF_HEADER_SIZE + (ELF_PROG_HEADER_SIZE * header_idx);
      //IO_LED = 0x77777777;
      //IO_LED = prog_header_addr;

      // parsing program header
      unsigned int p_offset = (*(unsigned int *)(prog_header_addr + 8));
      unsigned int p_vaddr  = (*(unsigned int *)(prog_header_addr + 12));
      unsigned int p_filesz = (*(unsigned int *)(prog_header_addr + 20));

      //IO_LED = 0x55aa55aa;
      //IO_LED = p_offset;
      //IO_LED = 0xbadc0ffe;
      //IO_LED = p_vaddr;
      //IO_LED = 0x99999999;
      //IO_LED = p_filesz;

      // loading segment to memory
      for (unsigned int load_byte_counter = 0; load_byte_counter < p_filesz; load_byte_counter = load_byte_counter + 4) {
        (*(unsigned int *)(p_vaddr + load_byte_counter)) = (*(unsigned int *)(elf_image_addr + p_offset + load_byte_counter));
      }    
    }

    return 0;
  
  } else {
    return -1;
  }
}