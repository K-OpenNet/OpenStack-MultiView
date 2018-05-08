#!/usr/bin/env python
import requests
import json

TCPFlowDetail = {'keys':'vlan,ipdestination,ipsource,ipprotocol,tcpdestinationport','value':'bytes','log':True}
requests.put('http://103.22.221.56:8008/flow/TCPFlowDetail/json',data=json.dumps(TCPFlowDetail))

UDPFlowDetail = {'keys':'vlan,ipdestination,ipsource,ipprotocol,udpdestinationport','value':'bytes','log':True}
requests.put('http://103.22.221.56:8008/flow/UDPFlowDetail/json',data=json.dumps(UDPFlowDetail))

TCPFlowDetailFrames = {'keys':'vlan,ipdestination,ipsource,ipprotocol,tcpdestinationport','value':'frames','log':True}
requests.put('http://103.22.221.56:8008/flow/TCPFlowDetailFrames/json',data=json.dumps(TCPFlowDetailFrames))

UDPFlowDetailFrames = {'keys':'vlan,ipdestination,ipsource,ipprotocol,tcpdestinationport','value':'frames','log':True}
requests.put('http://103.22.221.56:8008/flow/UDPFlowDetailFrames/json',data=json.dumps(UDPFlowDetailFrames))

ICMPFlowDetail = {'keys':'vlan,ipdestination,ipsource,ipprotocol','value':'bytes','filter':'ipprotocol=1','log':True}
requests.put('http://103.22.221.56:8008/flow/ICMPFlowDetail/json',data=json.dumps(ICMPFlowDetail))

ICMPFlowDetailFrames = {'keys':'vlan,ipdestination,ipsource,ipprotocol','value':'frames','filter':'ipprotocol=1','log':True}
requests.put('http://103.22.221.56:8008/flow/ICMPFlowDetailFrames/json',data=json.dumps(ICMPFlowDetailFrames))
