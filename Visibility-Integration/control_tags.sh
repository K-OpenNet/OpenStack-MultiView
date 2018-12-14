#!/bin/bash
#
# Copyright 2018 SmartX Collaboration (GIST NetCS). All rights reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#
# Name		    : control_tags.sh
# Description	: Script for setting up SmartX Tags for SmartX Boxes and Tenants. This script should be executed at ID Center via crontab. e.g. */10 * * * * /bin/bash /home/netcs/control_tags.sh. sshpass should be installed in ID Center.
#
# Created by    : Muhammad Usman
# Version       : 0.1
# Last Update	: December, 2018
#

OS_REGIONS="GIST1 KR-GIST2 GIST3 TW-NCKU MYREN MY-UM TH-CHULA VN-HUST"
. /home/netcs/openstack/admin-openrc

for region in $OS_REGIONS
do
	for server in $(openstack --os-region-name $region server list -c ID -f value --all-projects)
	do
		# the -f shell option sets environment variables
		eval $(openstack --os-region-name $region server show $server -f shell -c id -c name -c OS-EXT-SRV-ATTR:host -c OS-EXT-STS:power_state -c addresses -c project_id)
    		project_name=$(openstack project show -f value -c name $project_id)
#    		echo "$project_id,$project_name,$os_ext_srv_attr_host,$id,$name,$addresses" >> ControlPlane.tags.temp
		address1=$(echo $addresses | cut -d';' -f 1)
		address2=$(echo $addresses | cut -d';' -f 2)
		#echo "$addresses"
		#echo $address1
		#echo $address2
		if [[ $address1 =~ .*control.* ]]
		then
			ctrladdress=$(echo $address1 | cut -d',' -f 2) 
			ctrladdress=$(echo $ctrladdress | xargs)
		elif [[ $address1 =~ .*datapath.* ]]
                then
                        dataaddress=$(echo $address1 | cut -d'=' -f 2)
			dataaddress=$(echo $dataaddress | xargs)
		fi
		if [[ $address2 =~ .*control.* ]]
                then
			ctrladdress=$(echo $address2 | cut -d',' -f 2)
                        ctrladdress=$(echo $ctrladdress | xargs)
                elif [[ $address2 =~ .*datapath.* ]]
                then
                        dataaddress=$(echo $address2 | cut -d'=' -f 2)
			dataaddress=$(echo $dataaddress | xargs)
                fi
		echo "$ctrladdress $dataaddress"
		echo "$project_id,$project_name,$os_ext_srv_attr_host,$id,$name,$ctrladdress,$dataaddress" >> ControlPlane.tags.temp
	done
done

mv ControlPlane.tags.temp ControlPlane.tags
