#!/usr/bin/python
#
# Name          : net_vxlan_point.py
# Description   : A script for processing network packets at user-level
#
# Created by    : Jun-Sik Shin, Muhammad Usman
# Version       : 0.3
# Last Update   : July, 2018

from __future__ import print_function

import os
import socket
import time
import logging
import signal
import sys

import netifaces as ni
from bcc import BPF


class NetworkVxlanPacketPoint:
    def __init__(self, point_config):
        self._logger = logging.getLogger(self.__class__.__name__)

        self._bpf_file = "net_vxlan_point.c"
        self._bpf_func = "vxlan_filter"

        self._bpf_bytecode = None
        self._socket_filter = None
        self._socket = None
        self._socket_fd = None

        self._load_config(point_config)

        signal.signal(signal.SIGINT, self.signal_handler)
        signal.signal(signal.SIGTERM, self.signal_handler)

    def _load_config(self, point_config):
        if point_config:
            # Variables that have to be defined in point_config
            self._target_net_if = point_config.get("target")
            self._log_dir = point_config.get("log_dir")
            self._net_type = point_config.get("network_type")
        else:
            self._target_net_if = "enx20180124c41b"
            self._log_dir = "/opt/IOVisor-Data/"
            self._net_type = "data"

    def signal_handler(self, signal, frame):
        self._logger.info("Visibility Point {} was finished successfully".format(self.__class__.__name__))
        self._bpf_bytecode.cleanup()
        self._socket.close()
        sys.exit(0)

    def collect(self):
        self._init_bpf()
        self._logger.debug("MachineIP Hostname   ipver     Src IP Addr          Dst IP Addr    src Port    Dst Port   "
                           "   protocol  TCP_Window_Size Packet_Length")
        while True:
            # For detailed information, please find "_not_used_func()" method.
            packet_info = dict()

            # retrieve raw packet from socket
            packet_str = os.read(self._socket_fd, 2048)

            # convert packet into bytearray
            packet_bytearray = bytearray(packet_str)

            self._get_packet_overall_info(packet_bytearray, packet_info)
            self._get_packet_ip_info(packet_bytearray, packet_info)
            self._get_packet_tcp_info(packet_bytearray, packet_info)
            self._get_packet_vxlan_info(packet_bytearray, packet_info)
            message = self._generate_message(packet_info)
            self._write_message(message)

    def _init_bpf(self):
        # initialize BPF - load source code from http-parse-simple.c
        self._bpf_bytecode = BPF(src_file=self._bpf_file, debug=0)

        # load eBPF program http_filter of type SOCKET_FILTER into the kernel eBPF vm
        # more info about eBPF program types http://man7.org/linux/man-pages/man2/bpf.2.html
        function_ip_filter = self._bpf_bytecode.load_func(self._bpf_func, BPF.SOCKET_FILTER)

        # create raw socket, bind it to eth0
        # attach bpf program to socket created
        BPF.attach_raw_socket(function_ip_filter, self._target_net_if)

        # get file descriptor of the socket previously created inside BPF.attach_raw_socket
        self._socket_fd = function_ip_filter.sock

        # create python socket object, from the file descriptor
        self._socket = socket.fromfd(self._socket_fd, socket.PF_PACKET, socket.SOCK_RAW, socket.IPPROTO_IP)

        # set it as blocking socket
        self._socket.setblocking(True)

    def _get_packet_overall_info(self, packet_bytearray, packet_info):
        # ethernet header length
        ETH_HLEN = 14

        # calculate packet total length
        total_length = packet_bytearray[ETH_HLEN + 2]  # load MSB
        total_length = total_length << 8  # shift MSB
        total_length = total_length + packet_bytearray[ETH_HLEN + 3]  # add LSB
        packet_info["total_length"] = str(total_length)

    def _get_packet_ip_info(self, packet_bytearray, packet_info):
        # parsing ip version from ip packet header
        ipversion = str(bin(packet_bytearray[14])[2:5])
        packet_info["ip_version"] = str(int(ipversion, 2))

        # parsing source ip address, destination ip address from ip packet header
        src_addr = str(packet_bytearray[26]) + "." + str(packet_bytearray[27]) + "." + \
                   str(packet_bytearray[28]) + "." + str(packet_bytearray[29])
        dst_addr = str(packet_bytearray[30]) + "." + str(packet_bytearray[31]) + "." + \
                   str(packet_bytearray[32]) + "." + str(packet_bytearray[33])
        packet_info["src_ip_addr"] = src_addr
        packet_info["dst_ip_addr"] = dst_addr

    def _get_packet_vxlan_info(self, packet_bytearray, packet_info):
        vni = str((packet_bytearray[46]) + (packet_bytearray[47]) + (packet_bytearray[48]))
        vlan_id = str((packet_bytearray[64]) + (packet_bytearray[65]))

        if packet_bytearray[77] == 6:
            protocoll4 = 6
            src_vm_port = packet_bytearray[88] << 8 | packet_bytearray[88]
            dst_vm_port = packet_bytearray[90] << 8 | packet_bytearray[91]
            tcp_window_size = packet_bytearray[102] << 8 | packet_bytearray[103]
        elif packet_bytearray[77] == 1:
            protocoll4 = 1
            src_vm_port = -1
            dst_vm_port = -1
            tcp_window_size = 0
        elif packet_bytearray[77] == 17:
            protocoll4 = 17
            src_vm_port = packet_bytearray[88] << 8 | packet_bytearray[88]
            dst_vm_port = packet_bytearray[90] << 8 | packet_bytearray[91]
            tcp_window_size = 0
        else:
            protocoll4 = packet_bytearray[77]
            src_vm_port = packet_bytearray[88] << 8 | packet_bytearray[88]
            dst_vm_port = packet_bytearray[90] << 8 | packet_bytearray[91]
            tcp_window_size = 0

        src_vm_ip = str(packet_bytearray[80]) + "." + str(packet_bytearray[81]) + "." + str(
            packet_bytearray[82]) + "." + str(packet_bytearray[83])
        dst_vm_ip = str(packet_bytearray[84]) + "." + str(packet_bytearray[85]) + "." + str(
            packet_bytearray[86]) + "." + str(packet_bytearray[87])

        packet_bytearray["protocoll4"] = str(protocoll4)
        packet_bytearray["src_vm_port"] = str(src_vm_port)
        packet_bytearray["dst_vm_port"] = str(dst_vm_port)
        packet_bytearray["tcp_window_size"] = str(tcp_window_size)
        packet_bytearray["src_vm_ip"] = src_vm_ip
        packet_bytearray["dst_vm_ip"] = dst_vm_ip
        packet_bytearray["vni"] = str(int(vni))
        packet_bytearray["vlan_id"] = str(int(vlan_id))

    def _get_packet_tcp_info(self, packet_bytearray, packet_info):
        # parsing source port and destination port
        src_host_port = packet_bytearray[34] << 8 | packet_bytearray[35]
        dst_host_port = packet_bytearray[36] << 8 | packet_bytearray[37]

        packet_info["src_host_port"] = str(src_host_port)
        packet_info["dst_host_port"] = str(dst_host_port)

    def _generate_message(self, packet_info):
        mgmt_ip = self._get_mgmt_ip_address()
        message = "{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}".format(
            str(int(round(time.time() * 1000000))), socket.gethostname(), mgmt_ip,
            packet_info["ip_version"], packet_info["src_ip_addr"], packet_info["dst_ip_addr"],
            packet_info["src_host_port"], packet_info["dst_host_port"],
            packet_info["src_vm_ip"], packet_info["dst_vm_ip"], packet_info["src_vm_port"], packet_info["dst_vm_port"],
            packet_info["vni"], packet_info["vlan_id"], packet_info["protocoll4"], packet_info["tcp_window_size"],
            packet_info["total_length"]
        )

        self._logger.debug(message)
        return message

    def _get_mgmt_ip_address(self):
        mgmt_nic = ni.gateways().get("default").get(ni.AF_INET)[1]
        ni.ifaddresses(mgmt_nic)
        mgmt_ip = ni.ifaddresses(mgmt_nic)[ni.AF_INET][0]['addr']
        return mgmt_ip

    def _write_message(self, msg):
        filename = self._get_filename()

        if not os.path.exists(self._log_dir):
            self._logger.debug("Hello")
            os.mkdir(self._log_dir)

        f = open(filename, "a")
        f.write("%s\n" % msg)
        f.close()

    def _get_filename(self):
        current_min = int(time.strftime("%M"))
        box_name = socket.gethostname()

        min_mul_five = current_min - current_min % 5
        filename = "{}{}-{}-{}-{:02d}".format(self._log_dir, box_name, self._net_type,
                                              time.strftime("%Y-%m-%d-%H"), min_mul_five)
        self._logger.debug(filename)
        return filename

    def to_hex(self, s):
        # convert a bin string into a string of hex char
        # helper function to print raw packet in hex
        lst = []
        for ch in s:
            hv = hex(ord(ch)).replace('0x', '')
            if len(hv) == 1:
                hv = '0' + hv
            lst.append(hv)

        return reduce(lambda x, y: x + y, lst)

    def _not_used_func(self):
        # DEBUG - print raw packet in hex format
        # packet_hex = toHex(packet_str)
        # print ("%s" % packet_hex)

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

        # calculate ip header length
        # ip_header_length = packet_bytearray[ETH_HLEN]  # load Byte
        # ip_header_length = ip_header_length & 0x0F  # mask bits 0..3
        # ip_header_length = ip_header_length << 2  # shift to obtain length

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
        # tcp_header_length = packet_bytearray[ETH_HLEN + ip_header_length + 12]  # load Byte
        # tcp_header_length = tcp_header_length & 0xF0  # mask bit 4..7
        # tcp_header_length = tcp_header_length >> 2  # SHR 4 ; SHL 2 -> SHR 2

        # calculate payload offset
        # payload_offset = ETH_HLEN + ip_header_length + tcp_header_length
        pass


if __name__ == "__main__":
    logging.basicConfig(format="[%(asctime)s / %(levelname)s] %(filename)s,%(funcName)s(#%(lineno)d): %(message)s",
                        level=logging.DEBUG)
    point = NetworkVxlanPacketPoint(None)
    point.collect()

