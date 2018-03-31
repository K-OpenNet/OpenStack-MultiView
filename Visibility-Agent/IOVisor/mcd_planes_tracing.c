/* 
 Name          : mcd_planes_tacing.c
 Description   : A script for tracing network packets at kernel-level

 Created by    : Muhammad Usman
 Version       : 0.1
 Last Update   : March, 2018
*/

#include <uapi/linux/ptrace.h>
#include <net/sock.h>
#include <bcc/proto.h>

#define IP_TCP 	6
#define IP_UDP 17
#define IP_ICMP 1
/* 
  In 802.3, both the source and destination addresses are 48 bits (4 bytes) MAC address.
  6 bytes (src) + 6 bytes (dst) + 2 bytes (type) = 14 bytes 
*/
#define ETH_HLEN 14

/*eBPF program.
  Filter TCP/UDP/ICMP packets, having payload not empty
  if the program is loaded as PROG_TYPE_SOCKET_FILTER
  and attached to a socket
  return  0 -> DROP the packet
  return -1 -> KEEP the packet and return it to user space (userspace can read it from the socket_fd )
*/
int ip_filter(struct __sk_buff *skb) { 

	u8 *cursor = 0;	// unsigned 8 bits = unsigned 1 byte

	struct ethernet_t *ethernet = cursor_advance(cursor, sizeof(*ethernet));  // ethernet header (frame)
	//filter IP packets (ethernet type = 0x0800) 0x0800 is IPv4 packet
	if (!(ethernet->type == 0x0800)) {
		goto DROP;	
	}

	struct ip_t *ip = cursor_advance(cursor, sizeof(*ip));	// IP header (datagram)
	//filter TCP packets (ip next protocol = 0x06)
	if (ip->nextp != IP_TCP) {
		if (ip->nextp != IP_UDP){
			if (ip->nextp != IP_ICMP){
			goto DROP;}}
	}

	u32  tcp_header_length = 0;
	u32  ip_header_length = 0;
	u32  payload_offset = 0;
	u32  payload_length = 0;

	struct tcp_t *tcp = cursor_advance(cursor, sizeof(*tcp));
	/*
	  calculate ip header length
	  value to multiply * 4
	  e.g. ip->hlen = 5 ; IP Header Length = 5 x 4 byte = 20 byte

	  The minimum value for this field is 5, which indicates a length of 5 x 32 bits(4 bytes) = 20 bytes
	*/
	ip_header_length = ip->hlen << 2;    //SHL 2 -> *4 multiply

	/*
	  calculate tcp header length
	  value to multiply *4
	  e.g. tcp->offset = 5 ; TCP Header Length = 5 x 4 byte = 20 byte

	  The minimum value for this field is 5, which indicates a length of 5 x 32 bits(4 bytes) = 20 bytes
	*/
	tcp_header_length = tcp->offset << 2; //SHL 2 -> *4 multiply

	//calculate patload offset and length
	payload_offset = ETH_HLEN + ip_header_length + tcp_header_length;
	payload_length = ip->tlen - ip_header_length - tcp_header_length;

	/*
	  http://stackoverflow.com/questions/25047905/http-request-minimum-size-in-bytes
	  minimum length of http request is always geater than 7 bytes
	  avoid invalid access memory
	  include empty payload
	*/
	if(payload_length < 7) {
		goto DROP;
	}
	
	/*
	  load firt 7 byte of payload into p (payload_array)
	  direct access to skb not allowed
	  load_byte(): read binary data from socket buffer(skb)
	*/
	unsigned long p[7];
	int i = 0;
	int j = 0;
	for (i = payload_offset ; i < (payload_offset + 7) ; i++) {
		p[j] = load_byte(skb , i);
		j++;
	}
		
	goto KEEP;

	//keep the packet and send it to userspace retruning -1
	KEEP:
	return -1;

	//drop the packet returning 0
	DROP:
	return 0;

}

int vxlan_filter(struct __sk_buff *skb) { 

	u8 *cursor = 0;	// unsigned 8 bits = unsigned 1 byte

	struct ethernet_t *ethernet = cursor_advance(cursor, sizeof(*ethernet));  // ethernet header (frame)
	//filter IP packets (ethernet type = 0x0800) 0x0800 is IPv4 packet
	
	switch(ethernet->type){
		case 0x0800: goto IP;
	    	default: goto DROP;
	}

	IP: ;
		struct ip_t *ip = cursor_advance(cursor, sizeof(*ip));  // IP header (datagram)
	        switch (ip->nextp){
			case 17: goto UDP;
			default: goto DROP;
		}

	UDP: ;
		struct udp_t *udp = cursor_advance(cursor, sizeof(*udp));
		switch (udp->dport) {
    			case 4789: goto ISDATA;
    			default: goto DROP;
  		}

	ISDATA: ;
		u32  tcp_header_length = 0;
		u32  ip_header_length = 0;
		u32  payload_offset = 0;
		u32  payload_length = 0;

		struct tcp_t *tcp = cursor_advance(cursor, sizeof(*tcp));

		/*
	  	calculate ip header length
	  	value to multiply * 4
	  	e.g. ip->hlen = 5 ; IP Header Length = 5 x 4 byte = 20 byte
	  
	  	The minimum value for this field is 5, which indicates a length of 5 x 32 bits(4 bytes) = 20 bytes
		*/
		ip_header_length = ip->hlen << 2;    //SHL 2 -> *4 multiply
		
		/*
	  	calculate tcp header length
	  	value to multiply *4
	  	e.g. tcp->offset = 5 ; TCP Header Length = 5 x 4 byte = 20 byte
	
	  	The minimum value for this field is 5, which indicates a length of 5 x 32 bits(4 bytes) = 20 bytes
		*/
		tcp_header_length = tcp->offset << 2; //SHL 2 -> *4 multiply

		//calculate patload offset and length
		payload_offset = ETH_HLEN + ip_header_length + tcp_header_length; 
		payload_length = ip->tlen - ip_header_length - tcp_header_length;
		  
		if(payload_length < 7) {
			goto DROP;
		}

		/*
		load firt 7 byte of payload into p (payload_array)
		direct access to skb not allowed
		load_byte(): read binary data from socket buffer(skb)
		*/
		unsigned long p[7];
		int i = 0;
		int j = 0;
		for (i = payload_offset ; i < (payload_offset + 7) ; i++) {
			p[j] = load_byte(skb , i);
			j++;
		}

		goto KEEP;

	//keep the packet and send it to userspace retruning -1
	KEEP:
		return -1;

	//drop the packet returning 0
	DROP:
		return 0;
}