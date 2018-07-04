#!/usr/bin/python
#
# Name          : control_plane_tacing.py
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
import time

from kafka  import KafkaProducer
from kafka.errors import KafkaError
import json

# import iovisor_pb2
# from google.protobuf.internal.encoder import _VarintBytes
# from google.protobuf.internal.decoder import _DecodeVarint32

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

# This function fills in a NetworkPacket message.
# def AddPacket(netpacket, collectionTime, mpName, mpIP, srcIP, destIP, srcPort, destPort, protocol, dataBytes):
#    netpacket.collectionTime = collectionTime
#    netpacket.mpName = mpName
#    netpacket.mpIP = mpIP
    # netpacket.ipVersion = ipVersion
#    netpacket.srcIP = srcIP
#    netpacket.destIP = destIP
#    netpacket.srcPort = srcPort
#    netpacket.destPort = destPort
#    netpacket.protocol = protocol
#    netpacket.dataBytes = dataBytes
    # netpacket.message = ""
    
     
# initialize BPF - load source code from http-parse-simple.c
bpf = BPF(src_file="mcd_planes_tracing.c", debug=0)

# load eBPF program http_filter of type SOCKET_FILTER into the kernel eBPF vm
# more info about eBPF program types http://man7.org/linux/man-pages/man2/bpf.2.html
function_ip_filter = bpf.load_func("ip_filter", BPF.SOCKET_FILTER)

# create raw socket, bind it to eth0
# attach bpf program to socket created
BPF.attach_raw_socket(function_ip_filter, "eno3")

ni.ifaddresses('eno2')
ip = ni.ifaddresses('eno2')[ni.AF_INET][0]['addr']

# get file descriptor of the socket previously created inside BPF.attach_raw_socket
socket_fd = function_ip_filter.sock

# create python socket object, from the file descriptor
sock = socket.fromfd(socket_fd, socket.PF_PACKET, socket.SOCK_RAW, socket.IPPROTO_IP)
# set it as blocking socket
sock.setblocking(True)
print("MachineIP Hostname   ipver     Src IP Addr          Dst IP Addr    src Port    Dst Port      protocol  TCP_Window_Size Packet_Length")

count_c1 = 0

while 1:
    # retrieve raw packet from socket
    packet_str = os.read(socket_fd, 2048)

    # DEBUG - print raw packet in hex format
    # packet_hex = toHex(packet_str)
    # print ("%s" % packet_hex)

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
    srcAddr = str(packet_bytearray[26]) + "." + str(packet_bytearray[27]) + "." + str(packet_bytearray[28]) + "." + str(packet_bytearray[29])
    dstAddr = str(packet_bytearray[30]) + "." + str(packet_bytearray[31]) + "." + str(packet_bytearray[32]) + "." + str(packet_bytearray[33])

    # parsing source port and destination port
    if (packet_bytearray[23] == 6):
        protocol = 6
        srcPort = packet_bytearray[34] << 8 | packet_bytearray[35]
        dstPort = packet_bytearray[36] << 8 | packet_bytearray[37]
        TCP_Window_Size = packet_bytearray[48] << 8 | packet_bytearray[49]
    elif (packet_bytearray[23] == 1):
        protocol = 1
        srcPort = -1
        dstPort = -1
        TCP_Window_Size = 0
    elif (packet_bytearray[23] == 17):
        protocol = 17
        srcPort = packet_bytearray[34] << 8 | packet_bytearray[35]
        dstPort = packet_bytearray[36] << 8 | packet_bytearray[37]
        TCP_Window_Size = 0
    else:
        protocol = -1
        srcPort = packet_bytearray[34] << 8 | packet_bytearray[35]
        dstPort = packet_bytearray[36] << 8 | packet_bytearray[37]
        TCP_Window_Size = 0

    # Define Protocol Buffer
    # network_packet = iovisor_pb2.NetworkPackets()
    
    # Add a packet
    # AddPacket(network_packet.packet.add(), int(round(time.time() * 1000000)), socket.gethostname(), ip, srcAddr, dstAddr, srcPort, dstPort, protocol, total_length)
    
    MESSAGE = str(int(round(time.time() * 1000000))) + ",1," + socket.gethostname() + "," + ip + "," + str(int(ipversion, 2)) + "," + srcAddr + "," + dstAddr + "," + str(srcPort) + "," + str(dstPort) + "," + str(protocol) + "," + str(TCP_Window_Size) + "," + str(total_length)
    print (MESSAGE)
    
    CurrentMin = int(time.strftime("%M"))
    BoxName=socket.gethostname()
    
    if (CurrentMin < 30):
        if (CurrentMin < 10):
            if (CurrentMin < 5):
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-00"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-05"
        elif (CurrentMin < 20):
            if (CurrentMin < 15):
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-10"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-15"
        else:
            if (CurrentMin < 25):
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-20"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-25"
    else:
        if (CurrentMin < 40):
            if (CurrentMin < 35):
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-30"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-35"
        elif (CurrentMin < 50):
            if (CurrentMin < 45):
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-40"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-45"
        else:
            if (CurrentMin < 55):
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-50"
            else:
                filename = "/opt/IOVisor-Data/"+BoxName+"-mc-" + time.strftime("%Y-%m-%d-%H") + "-55"
    
    # f = open(filename, "wb")
    f = open(filename, "a")
    # size = network_packet.ByteSize()
    # f.write(_VarintBytes(size))
    # f.writeDelimitedTo(network_packet.SerializeToString())
    f.write("%s\n" % MESSAGE)
    f.close
