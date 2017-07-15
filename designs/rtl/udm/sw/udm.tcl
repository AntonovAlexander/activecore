set sync_byte       \x55
set escape_byte     \x5a
set idcode_cmd      \x00
set rst_cmd         \x80
set nrst_cmd        \xc0
set wr_cmd          \x81
set rd_cmd          \x82
set wr_cmd_noinc    \x83
set rd_cmd_noinc    \x84

proc udm_connect {com_num baudrate} {
    global com
    set parity n
    set databits 8
    set stopbits 1

    set com [open $com_num: r+]
    fconfigure $com -mode $baudrate,$parity,$databits,$stopbits \
        -blocking 1 -translation binary -buffering none -buffersize 16 -timeout 1000
}


proc udm_con {com_num baudrate} {
    udm_connect $com_num $baudrate
}


proc udm_disconnect {} {
    global com

    close $com
    puts "Connection dropped"
}


proc udm_discon {} {
    udm_disconnect
}


proc udm_check {} {
    global com
    global sync_byte
    global idcode_cmd

    puts -nonewline $com $sync_byte
    puts -nonewline $com $idcode_cmd
    after 100
    set resp [read -nonewline $com]

    binary scan $resp H* resp_char

    if {$resp == $sync_byte} {puts "Connection established, response: $resp_char"} \
    else {puts "Conection failed, response: $resp_char"}
}


proc udm_cc {com_num baudrate} {
    udm_connect $com_num $baudrate
    udm_check
}

proc udm_sendbyte {databyte} {
    global com
    global sync_byte
    global escape_byte

    if {$databyte == $sync_byte || $databyte == $escape_byte} {puts -nonewline $com $escape_byte}
    puts -nonewline $com $databyte
}

proc udm_rst {} {
	puts -nonewline $com $sync_byte
	puts -nonewline $com $rst_cmd
}

proc udm_nrst {} {
	puts -nonewline $com $sync_byte
	puts -nonewline $com $nrst_cmd
}

proc udm_hreset {} {
	udm_rst
	udm_nrst
}

proc udm_sendword {dataword} {
    set byte0 [format %c [expr [expr {$dataword & 0x000000ff}] >> 0]]
    set byte1 [format %c [expr [expr {$dataword & 0x0000ff00}] >> 8]]
    set byte2 [format %c [expr [expr {$dataword & 0x00ff0000}] >> 16]]
    set byte3 [format %c [expr [expr {$dataword & 0xff000000}] >> 24]]
    udm_sendbyte $byte0
    udm_sendbyte $byte1
    udm_sendbyte $byte2
    udm_sendbyte $byte3
}


proc udm_wr {address datawords} {
    global com
    global sync_byte
    global wr_cmd

    # header
    puts -nonewline $com $sync_byte
    puts -nonewline $com $wr_cmd

    # address
    udm_sendword $address

    #length
    set trlength [expr [llength $datawords] << 2]
    udm_sendword $trlength

    # data
    foreach dataword $datawords {
        udm_sendword $dataword
    }
}


proc udm_rd {address length} {
    global com
    global sync_byte
    global rd_cmd

    # clean input buffers
    read $com

    # header
    puts -nonewline $com $sync_byte
    puts -nonewline $com $rd_cmd

    # address
    udm_sendword $address

    #length
    udm_sendword $length

    set finaddr [expr $length + $address]

    set resp ""
    # data
    for {set curaddr $address} {$curaddr < $finaddr} {incr curaddr 4} {
        set str ""
        set resp0 [read $com 1]
        set resp1 [read $com 1]
        set resp2 [read $com 1]
        set resp3 [read $com 1]
        binary scan $resp0 H2 resp_show0
        binary scan $resp1 H2 resp_show1
        binary scan $resp2 H2 resp_show2
        binary scan $resp3 H2 resp_show3
        append str "0x"
        append str $resp_show3
        append str $resp_show2
        append str $resp_show1
        append str $resp_show0
        lappend resp $str
    }

    return $resp
}


proc udm_wrfile_le {address filename} {
    global com
    global sync_byte
    global wr_cmd

    set datafile [open $filename]
    fconfigure $datafile -translation binary -encoding binary

    # header
    puts -nonewline $com $sync_byte
    puts -nonewline $com $wr_cmd

    # address
    udm_sendword $address

    #length
    set length [file size $filename]
    udm_sendword $length

    while {true} {

        set dbuf0 [read $datafile 1]
        set dbuf1 [read $datafile 1]
        set dbuf2 [read $datafile 1]
        set dbuf3 [read $datafile 1]
        if {[eof $datafile]} {break}

        udm_sendbyte $dbuf0
        udm_sendbyte $dbuf1
        udm_sendbyte $dbuf2
        udm_sendbyte $dbuf3

        incr address 4
    }
    
    close $datafile
}


proc udm_wrfile_be {address filename} {
    global com
    global sync_byte
    global wr_cmd

    set datafile [open $filename]
    fconfigure $datafile -translation binary -encoding binary

    # header
    puts -nonewline $com $sync_byte
    puts -nonewline $com $wr_cmd

    # address
    udm_sendword $address

    #length
    set length [file size $filename]
    udm_sendword $length

    while {true} {

        set dbuf0 [read $datafile 1]
        set dbuf1 [read $datafile 1]
        set dbuf2 [read $datafile 1]
        set dbuf3 [read $datafile 1]
        if {[eof $datafile]} {break}

        udm_sendbyte $dbuf3
        udm_sendbyte $dbuf2
        udm_sendbyte $dbuf1
        udm_sendbyte $dbuf0

        incr address 4
    }
    
    close $datafile
}

proc udm_loadbin {filename} {
    udm_rst
    udm_wrfile_be 0x00000000 $filename
    udm_nrst
}
