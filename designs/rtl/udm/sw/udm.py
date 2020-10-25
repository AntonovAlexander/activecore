# -*- coding:utf-8 -*-

#
# udm.py
#
#  Created on: 17.04.2016
#      Author: Alexander Antonov <antonov.alex.alex@gmail.com>
#     License: See LICENSE file for details
#

from __future__ import division

import struct
import serial
import os
import random

class udm:
    
    __sync_byte       = 0x55
    __escape_byte     = 0x5a
    __idcode_cmd      = 0x00
    __rst_cmd         = 0x80
    __nrst_cmd        = 0xc0
    __wr_cmd          = 0x81
    __rd_cmd          = 0x82
    __wr_cmd_noinc    = 0x83
    __rd_cmd_noinc    = 0x84
    
    __TRX_WR_SUCC_BYTE    = 0x00
    __TRX_ERR_ACK_BYTE    = 0x01
    __TRX_ERR_RESP_BYTE   = 0x02
    __TRX_IRQ_BYTE        = 0x80
    
    def connect(self, com_num, baudrate):
        """Description:
            Connect to COM port

        Parameters:
            com_num (str): COM port name
            baudrate (int): baudrate

        """
        self.ser = serial.Serial(com_num, baudrate, 8)   
    
    def con(self, com_num, baudrate):
        """Description:
            Same as connect(self, com_num, baudrate)

        """
        self.connect(com_num, baudrate)
    
    def disconnect(self):
        """Description:
            Disconnect from COM port

        """
        if self.ser.is_open:
            self.ser.close()
            print("Connection dropped")
    
    def discon(self):
        """Description:
            Same as disconnect(self)

        """
        self.disconnect()
    
    def __getbyte(self):
        rdata = self.ser.read(1)
        rdata = struct.unpack("B", rdata)
        return rdata[0]
    
    def __getdatabyte(self):
        rdata = self.__getbyte()
        
        if (rdata == self.__TRX_ERR_ACK_BYTE):
            print("UDM BUS ERROR: <ack> not received!")
            raise Exception()
        
        if (rdata == self.__TRX_ERR_RESP_BYTE):
            print("UDM BUS ERROR: <resp> not received!")
            raise Exception()
        
        if (rdata == self.__escape_byte):
            rdata = self.__getbyte()
        
        return rdata
    
    def __getdataword32(self):
        rdata=[]
        rdata.append(self.__getdatabyte())
        rdata.append(self.__getdatabyte())
        rdata.append(self.__getdatabyte())
        rdata.append(self.__getdatabyte())
        rdataword = rdata[0] + (rdata[1] << 8) + (rdata[2] << 16) + (rdata[3] << 24)
        return rdataword
    
    def check(self):
        """Description:
            Check UDM response

        """
        self.ser.flush()
        wdata = (struct.pack('B', self.__sync_byte))
        wdata = wdata + (struct.pack('B', self.__idcode_cmd))
        self.ser.write(wdata)
        rdata = self.ser.read()
        rdata = struct.unpack('B', rdata)
        
        if (rdata[0] == self.__sync_byte):
            print("Connection established, response: ", hex(rdata[0]))
        else:
            print("Connection failed, response: ", hex(rdata[0]))
            raise Exception()
    
    def cc(self, com_num, baudrate):
        """Description:
            Connect to COM port and check UDM response

        Parameters:
            com_num (str): COM port name
            baudrate (int): baudrate

        """
        print("Connecting COM port...")
        self.connect(com_num, baudrate)
        print("COM port connected")
        self.check()
    
    def __sendbyte(self, databyte):
        if ((databyte == self.__sync_byte) or (databyte == self.__escape_byte)):
            wdata = (struct.pack('B', self.__escape_byte))
            self.ser.write(wdata)
        wdata = (struct.pack('B', databyte))
        self.ser.write(wdata)
    
    def rst(self):
        """Description:
            Assert UDM driven reset

        """
        wdata = (struct.pack('B', self.__sync_byte))
        wdata = wdata + (struct.pack('B', self.__rst_cmd))
        self.ser.write(wdata)
    
    def nrst(self):
        """Description:
            Deassert UDM driven reset

        """
        wdata = (struct.pack('B', self.__sync_byte))
        wdata = wdata + (struct.pack('B', self.__nrst_cmd))
        self.ser.write(wdata)
    
    def hreset(self):
        """Description:
            Assert and deassert UDM driven reset

        """
        self.rst()
        self.nrst()
    
    def __sendword32(self, dataword):
        self.__sendbyte((dataword >> 0) & 0xff)
        self.__sendbyte((dataword >> 8) & 0xff)
        self.__sendbyte((dataword >> 16) & 0xff)
        self.__sendbyte((dataword >> 24) & 0xff)
    
    def __wr_finalize(self):
        rdata = self.__getbyte()
        if (rdata == self.__TRX_WR_SUCC_BYTE):
            pass
        elif (rdata == self.__TRX_ERR_ACK_BYTE):
            print("UDM BUS ERROR: <ack> not received!")
            raise Exception()
        else:
            print("UDM BUS ERROR: response unknown!")
            raise Exception()
    
    def wr32(self, address, dataword):
        """Description:
            Write data word to address

        Parameters:
            address (int): Write address
            dataword (int): Data word

        """
        try:
            self.ser.flush()
            self.__sendbyte(self.__sync_byte)
            self.__sendbyte(self.__wr_cmd)
            self.__sendword32(address)     
            self.__sendword32(4)
            self.__sendword32(dataword)
            self.__wr_finalize()
        except:
            self.discon()
            raise Exception()
    
    def wrarr32(self, address, datawords):
        """Description:
            Burst write of array

        Parameters:
            address (int): Starting address
            datawords (int[]): Data words

        """
        try:
            self.ser.flush()
            self.__sendbyte(self.__sync_byte)
            self.__sendbyte(self.__wr_cmd)
            self.__sendword32(address)     
            count = len(datawords) 
            self.__sendword32(count << 2)
            # write data
            for i in range(count):
                self.__sendword32(datawords[i])
            self.__wr_finalize()
        except:
            self.discon()
            raise Exception()
    
    def clr(self, address, size):
        """Description:
            Pad memory with zeroes

        Parameters:
            address (int): Start address
            size (int): Number of bytes

        """
        padding_arr = []
        for i in range(size >> 2):
            padding_arr.append(0x00)
        self.wrarr32(address, padding_arr)
    
    def rd32(self, address):
        """Description:
            Read data word from address

        Parameters:
            address (int): Read address

        Returns:
            int: Read data

        """
        try:
            self.ser.flush()
            self.__sendbyte(self.__sync_byte)
            self.__sendbyte(self.__rd_cmd)
            self.__sendword32(address)
            self.__sendword32(4)
            return self.__getdataword32()
        except:
            self.discon()
            raise Exception()
    
    def rdarr32(self, address, length):
        """Description:
            Burst read into array

        Parameters:
            address (int): Starting address
            length (int): Number of data words

        Returns:
            int[]: Read data

        """
        try:
            self.ser.flush()
            self.__sendbyte(self.__sync_byte)
            self.__sendbyte(self.__rd_cmd)
            self.__sendword32(address)
            self.__sendword32(length << 2)
            rdatawords = []
            for i in range(length):
                rdatawords.append(self.__getdataword32())
            return rdatawords
        except:
            self.discon()
            raise Exception()
    
    def wrbin32_le(self, address, filename):
        """Description:
            Write data from binary file to memory beginning from address

        Parameters:
            address (int): Start address
            filename (str): Binary file name

        """
        wrdataarr = []
        f = open(filename, "rb")
        try:
            while True:            
                wrdata = 0
                dbuf0 = f.read(1)
                dbuf1 = f.read(1)
                dbuf2 = f.read(1)
                dbuf3 = f.read(1)
                if dbuf0:
                    dbuf0 = struct.unpack("B", dbuf0)
                    wrdata = wrdata | dbuf0[0]
                else:
                    break
                if dbuf1:
                    dbuf1 = struct.unpack("B", dbuf1)
                    wrdata = wrdata | (dbuf1[0] << 8)                  
                if dbuf2:
                    dbuf2 = struct.unpack("B", dbuf2)
                    wrdata = wrdata | (dbuf2[0] << 16)                    
                if dbuf3:
                    dbuf3 = struct.unpack("B", dbuf3)
                    wrdata = wrdata | (dbuf3[0] << 24)
                wrdataarr.append(wrdata)
        finally:
            self.wrarr32(address, wrdataarr)
            f.close()
    
    def wrelf32(self, base_offset, filename):
        """Description:
            Write elf file to memory

        Parameters:
            base_offset (int): Write offset
            filename (str): Elf file name

        """
        print("----------------")
        f = open(filename, "rb")
        try:
            e_ident = f.read(16)
            e_ident = struct.unpack("BBBBBBBBBBBBBBBB", e_ident)
            if ((e_ident[0] != 0x7f) | (e_ident[1] != 0x45) | (e_ident[2] != 0x4c) | (e_ident[3] != 0x46)):
                raise Exception("Error: elf signature incorrect!")
            print("Loading elf file: ", filename)
            
            e_type = f.read(2)
            e_type = struct.unpack("H", e_type)
            if (e_type[0] != 0x02):
                raise Exception("Error: e_type is not executable!")
            print("-- e_type: ET_EXEC")
            
            e_machine = f.read(2)
            e_machine = struct.unpack("H", e_machine)
            if (e_machine[0] == 243):
                print("-- e_machine: RISC-V")
            else:
                print("-- e_machine: ", hex(e_machine[0]))
            
            e_version = f.read(4)
            e_version = struct.unpack("L", e_version)
            
            e_entry = f.read(4)
            e_entry = struct.unpack("L", e_entry)
            #print("-- e_entry: ", hex(e_entry[0]))
    
            e_phoff = f.read(4)
            e_phoff = struct.unpack("L", e_phoff)
            #print("-- e_phoff: ", hex(e_phoff[0]))
    
            e_shoff = f.read(4)
            e_shoff = struct.unpack("L", e_shoff)
            #print("-- e_shoff: ", hex(e_shoff[0]))
    
            e_flags = f.read(4)
            e_flags = struct.unpack("L", e_flags)
            #print("-- e_flags: ", hex(e_flags[0]))
    
            e_ehsize = f.read(2)
            e_ehsize = struct.unpack("H", e_ehsize)
            #print("-- e_ehsize: ", hex(e_ehsize[0]))
    
            e_phentsize = f.read(2)
            e_phentsize = struct.unpack("H", e_phentsize)
            #print("-- e_phentsize: ", hex(e_phentsize[0]))
    
            e_phnum = f.read(2)
            e_phnum = struct.unpack("H", e_phnum)
            #print("-- e_phnum: ", hex(e_phnum[0]))
    
            e_shentsize = f.read(2)
            e_shentsize = struct.unpack("H", e_shentsize)
            #print("-- e_shentsize: ", hex(e_shentsize[0]))
    
            e_shnum = f.read(2)
            e_shnum = struct.unpack("H", e_shnum)
            #print("-- e_shnum: ", hex(e_shnum[0]))
    
            e_shstrndx = f.read(2)
            e_shstrndx = struct.unpack("H", e_shstrndx)
            #print("-- e_shstrndx: ", hex(e_shstrndx[0]))
    
            prog_headers = []
            print("Program Headers:")
            print("-----------------------------------------------------------------------------------------------------------")
            print(" № | p_type     | p_offset   | p_vaddr    | p_paddr    | p_filesz   | p_memsz    | p_flags    | p_align")
            phnum = 0
            for h in range(e_phnum[0]):
                prog_header = f.read(32)
                prog_header = struct.unpack("LLLLLLLL", prog_header)
                PT_LOAD = 1
                if prog_header[0] != PT_LOAD:
                    raise Exception("Error: p_type incorrect: 0x%08x" % prog_header[0])
                print("%2d" % phnum, "| 0x%08x" % prog_header[0], "| 0x%08x" % prog_header[1], "| 0x%08x" % prog_header[2], "| 0x%08x" % prog_header[3], "| 0x%08x" % prog_header[4], "| 0x%08x" % prog_header[5], "| 0x%08x" % prog_header[6], "| 0x%08x" % prog_header[7])
                prog_headers.append((prog_header[1], prog_header[2], prog_header[4]))
                phnum+=1
            print("-----------------------------------------------------------------------------------------------------------")
    
            for prog_header in prog_headers:
                offset = prog_header[0]
                vaddr = prog_header[1]
                size = prog_header[2]
                print("LOADING: file offset: 0x%08x" % offset, ", hw addr: 0x%08x" % vaddr, "size: 0x%08x" % size)
                f.seek(offset)
                dbs = f.read(size)
                dbs = struct.unpack('{}L'.format(len(dbs)>>2), dbs)
                self.wrarr32((base_offset + vaddr), dbs)
    
        finally:
            f.close()
        print("----------------")
    
    def memtest32(self, baseaddr, wsize):
        """Description:
            Read/write RAM test

        Parameters:
            baseaddr (int): Base address of tested memory region
            wsize (int): Number of data words in tested memory region

        """
        print("")
        print("---- memtest32 started, word size:", wsize, " ----");
        
        # generating test data
        wrdata = []
        for i in range(wsize):
            wrdata.append(random.randint(0, ((1024*1024*1024*4)-1)))
        
        # writing test data
        self.wrarr32(baseaddr, wrdata)
            
        #reading test data
        rddata = self.rdarr32(baseaddr, wsize)
        
        # checking test data
        test_succ = True
        for i in range(wsize):
            if (rddata[i] != wrdata[i]):
                print("memtest32 failed on address ", hex(baseaddr + (i << 2)), "expected data: ", hex(wrdata[i]), " data read: ", hex(rddata[i]))
                test_succ = False
        
        if (test_succ):
            print("---- memtest32 PASSED ----")
        else:
            print("---- memtest32 FAILED ----")
        print("")
    
    
    def __init__(self, com_num, baudrate):
        self.cc(com_num, baudrate)
    
    def __del__(self):
        self.discon()
