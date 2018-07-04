#!/usr/bin/python
#
# Name          : data_plane_tacing.py
# Description   : A script for processing network packets at user-level
#
# Created by    : Muhammad Usman
# Version       : 0.2
# Last Update   : June, 2018

from __future__ import print_function
from bcc import BPF
from datetime import datetime

import sys
import socket
import os
import argparse
import netifaces as ni
import re
import json
from urllib2 import urlopen
import time

from kafka import KafkaProducer
from kafka.errors import KafkaError
import json

# convert a bin string into a string of hex char
# helper function to print raw packet in hex
def toHex(s):
    lst = []
    for ch in s:
        hv = hex(ord(ch)).replace('0x', '')
        if len(hv) == 1:
            hv = '0' + hv
        lst.append(hv)

    return reduce(lambda x, y:x + y, lst)

 
# initialize BPF - load source code from http-parse-simple.c
bpf = BPF(src_file="mcd_planes_tracing.c", debug=0)

# load eBPF program http_filter of type SOCKET_FILTER into the kernel eBPF vm
# more info about eBPF program types http://man7.org/linux/man-pages/man2/bpf.2.html
function_vxlan_filter = bpf.load_func("vxlan_filter", BPF.SOCKET_FILTER)

# create raw socket, bind it to eth0
# attach bpf program to socket created
BPF.attach_raw_socket(function_vxlan_filter, "eno5")

ni.ifaddresses('eno2')
ip = ni.ifaddresses('eno2')[ni.AF_INET][0]['addr']

# get file descriptor of the socket previously created inside BPF.attach_raw_socket
socket_fd = function_vxlan_filter.sock

# create python socket object, from the file descriptor
sock = socket.fromfd(socket_fd, socket.PF_PACKET, socket.SOCK_RAW, socket.IPPROTO_IP)

# set it as blocking socket
sock.setblocking(True)

print("hosname, MachineIP   ipver     Src IP Addr     Dst IP Addr     Src_Port   Des_Port  Local_Src_Addr   Local_des_Addr     Local_Src_Port   Local_Des_Port VNI  VLANID  protocol  Packet Length ")
count_c1 = 0

while 1:
    # retrieve raw packet from socket
    packet_str = os.read(socket_fd, 2048)
    
    # convert packet into bytearray
    packet_bytearray = bytearray(packet_str)
    
    # ethernet header length
    ETH_HLEN = 14 
    
    # IP HEADER
    # https://tools.ietf.org/html/rfc791
    # 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
    # +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    # |Version|  IHL  |Type of Service|          Total Length         |
    # +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    #
    # IHL : Internet Header Length is the length of the internet header 
    # value to multiply * 4 byte
    # e.g. IHL = 5 ; IP Header Length = 5 * 4 byte = 20 byte
    #
    # Total length: This 16-bit field defines the entire packet size, 
    # including header and data, in bytes.
    
    # calculate packet total length
    total_length = packet_bytearray[ETH_HLEN + 2]  # load MSB
    total_length = total_length << 8  # shift MSB
    total_length = total_length + packet_bytearray[ETH_HLEN + 3]  # add LSB
    
    # calculate ip header length
    ip_header_length = packet_bytearray[ETH_HLEN]  # load Byte
    ip_header_length = ip_header_length & 0x0F  # mask bits 0..3
    ip_header_length = ip_header_length << 2  # shift to obtain length
    
    # TCP HEADER 
    # https://www.rfc-editor.org/rfc/rfc793.txt
    #  12              13              14              15  
    #  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 
    # +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    # |  Data |           |U|A|P|R|S|F|                               |
    # | Offset| Reserved  |R|C|S|S|Y|I|            Window             |
    # |       |           |G|K|H|T|N|N|                               |
    # +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    #
    # Data Offset: This indicates where the data begins.  
    # The TCP header is an integral number of 32 bits long.
    # value to multiply * 4 byte
    # e.g. DataOffset = 5 ; TCP Header Length = 5 * 4 byte = 20 byte
    
    # calculate tcp header length
    tcp_header_length = packet_bytearray[ETH_HLEN + ip_header_length + 12]  # load Byte
    tcp_header_length = tcp_header_length & 0xF0  # mask bit 4..7
    tcp_header_length = tcp_header_length >> 2  # SHR 4 ; SHL 2 -> SHR 2
    
    # calculate payload offset
    payload_offset = ETH_HLEN + ip_header_length + tcp_header_length
    
    # parsing ip version from ip packet header
    ipversion = str(bin(packet_bytearray[14])[2:5])
    
    # parsing source ip address, destination ip address from ip packet header
    src_host_ip = str(packet_bytearray[26]) + "." + str(packet_bytearray[27]) + "." + str(packet_bytearray[28]) + "." + str(packet_bytearray[29])
    dest_host_ip = str(packet_bytearray[30]) + "." + str(packet_bytearray[31]) + "." + str(packet_bytearray[32]) + "." + str(packet_bytearray[33])
    
    # parsing source port and destination port
    src_host_port = packet_bytearray[34] << 8 | packet_bytearray[35]
    dest_host_port = packet_bytearray[36] << 8 | packet_bytearray[37]
    
    # parsing VNI and VLANID from VXLAN header
    VLANID = ""
    VNI = str((packet_bytearray[46]) + (packet_bytearray[47]) + (packet_bytearray[48]))
    VLANID = str((packet_bytearray[64]) + (packet_bytearray[65]))

    if (packet_bytearray[77] == 6):
        protocoll4 = 6
        src_vm_port = packet_bytearray[88] << 8 | packet_bytearray[88]
        dest_vm_port = packet_bytearray[90] << 8 | packet_bytearray[91]
        TCP_Window_Size = packet_bytearray[102] << 8 | packet_bytearray[103]
    elif (packet_bytearray[77] == 1):
        protocoll4 = 1
        src_vm_port = -1
        dest_vm_port = -1
        TCP_Window_Size = 0
    elif (packet_bytearray[77] == 17):
        protocoll4 = 17
        src_vm_port = packet_bytearray[88] << 8 | packet_bytearray[88]
        dest_vm_port = packet_bytearray[90] << 8 | packet_bytearray[91]
        TCP_Window_Size = 0
    else:
        protocoll4 = packet_bytearray[77]
        src_vm_port = packet_bytearray[88] << 8 | packet_bytearray[88]
        dest_vm_port = packet_bytearray[90] << 8 | packet_bytearray[91]
        TCP_Window_Size = 0

    src_vm_ip = str(packet_bytearray[80]) + "." + str(packet_bytearray[81]) + "." + str(packet_bytearray[82]) + "." + str(packet_bytearray[83])
    dest_vm_ip = str(packet_bytearray[84]) + "." + str(packet_bytearray[85]) + "." + str(packet_bytearray[86]) + "." + str(packet_bytearray[87]) 

    #  MESSAGE = (socket.gethostname(), ip, str(int(ipversion, 2)), srcAddr, str(src_vm_port), dstAddr, str(dest_vm_port), str(total_length), protocoll4, local_src_addr, local_des_addr, str(int(VNI)), str(int(VLANID)))
    #  print (MESSAGE)
    #  MESSAGE1 = ','.join(MESSAGE)
    #  MESSAGE2 = MESSAGE1.encode() 
    
    #  producer = KafkaProducer(bootstrap_servers=['vc.manage.overcloud:9092'])
    #  producer.send('iovisor-oftein', key=b'iovisor', value=MESSAGE2)
    MESSAGE = str(int(round(time.time() * 1000000))) + "," + socket.gethostname() + "," + ip + "," + str(int(ipversion, 2)) + "," + src_host_ip + "," + dest_host_ip + "," + str(src_host_port) + "," + str(dest_host_port) + "," + src_vm_ip + "," + dest_vm_ip + "," + str(src_vm_port) + "," + str(dest_vm_port) + "," + str(int(VNI)) + "," + str(int(VLANID)) + "," + str(protocoll4) + "," +str(TCP_Window_Size)+","+  str(total_length)
    print (MESSAGE)
    
    CurrentMin = int(time.strftime("%M"))
    BoxName=socket.gethostname()
    
    if (CurrentMin < 30):
        if (CurrentMin < 10):
            if (CurrentMin < 5):
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-00"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-05"
        elif (CurrentMin < 20):
            if (CurrentMin < 15):
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-10"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-" + time.strftime("%Y-%m-%d-%H") + "-15"
        else:
            if (CurrentMin < 25):
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-20"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-25"
    else:
        if (CurrentMin < 40):
            if (CurrentMin < 35):
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-30"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-35"
        elif (CurrentMin < 50):
            if (CurrentMin < 45):
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-40"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-45"
        else:
            if (CurrentMin < 55):
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-50"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-data-" + time.strftime("%Y-%m-%d-%H") + "-55"
    
    f = open(filename, "a")
    f.write("%s\n" % MESSAGE)
    f.close
