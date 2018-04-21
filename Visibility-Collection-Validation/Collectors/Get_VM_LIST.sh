#!/bin/bash
#
# Copyright 2016 SmartX Collaboration (GIST NetCS). All rights reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#
# Name          : Get_VM_List.sh
# Description   : Script for Getting VM's/Box
#
# Created by    : usman@smartx.kr
# Version       : 0.1
# Last Update   : April, 2018

#Source the Admin File
. /home/netcs/openstack/admin-openrc

#openstack  region list
OS_REGIONS="GIST1 KR-GIST2 GIST3 TW-NCKU MYREN MY-UM TH-CHULA VN-HUST"

. /home/netcs/openstack/admin-openrc
rm -rf vm_list.temp InstanceList

for region in $OS_REGIONS
do
	openstack --os-region-name $region server list --all-projects -f csv --quote none --long > vm_list.temp
	while read -r vmline
	do
		if [[ $vmline == *"ID"* ]]; then
			echo "Skip..."
		else 
			instance_id=`echo $vmline | cut -d ',' -f1`
			instance_name=`echo $vmline | cut -d ',' -f2`
			instance_status=`echo $vmline | cut -d ',' -f3`
			instance_power_state=`echo $vmline | cut -d ',' -f5`
			instance_networks=`echo $vmline | cut -d ',' -f6`
			instance_image=`echo $vmline | cut -d ',' -f7`
			instance_host_box=`echo $vmline | cut -d ',' -f11`
			echo "$instance_id,$instance_name,$instance_status,$instance_power_state,$instance_networks,$instance_image,$instance_host_box" >> InstanceList
		fi
	done < "vm_list.temp"
done

