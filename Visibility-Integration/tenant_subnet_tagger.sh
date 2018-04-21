#!/bin/bash
#
# Copyright 2017 SmartX Collaboration (GIST NetCS). All rights reserved.
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
# Name			: Get OpenStack Subnet List (Multi-region Deployment)
# Description	: Script for Integating/Mapping neutron network, subnet and tenant for operator+tenant tagging to assist flowcentric visibility. It must be executed in ID center.
#
# Created by    : Muhammad Usman
# Version       : 0.1
# Last Update	: April, 2018
#

OS_REGIONS="GIST1 KR-GIST2 GIST3 TW-NCKU MYREN MY-UM TH-CHULA VN-HUST"

. /home/netcs/openstack/admin-openrc
rm -rf tenant_subnet.temp1 tenant.tags.temp tenant.tags

for region in $OS_REGIONS
do
        openstack --os-region-name $region subnet list --no-dhcp -c Network -c Subnet -f csv --quote none > tenant_subnet.temp1

        while read -r subnetline
        do
                if [[ $subnetline == *"Network"* ]]; then
                        echo "Skip..."
                else
                        network_id=`echo $subnetline | cut -d ',' -f1`
                        subnet_address=`echo $subnetline | cut -d ',' -f2`

                        subnetdetails=`openstack --os-region-name $region network show $network_id -c project_id -c provider:segmentation_id -f value`
                        project_id=`echo $subnetdetails | awk '{print $1}'`
                        vlan_id=`echo $subnetdetails | awk '{print $2}'`

                        project_name=`openstack project list | grep  $project_id | cut -d'|' -f3 | awk '{$1=$1;print}'`

                        echo "$vlan_id,$project_id,$project_name,$subnet_address" >> tenant.tags.temp
                fi
        done < "tenant_subnet.temp1"
done
rm -rf tenant.tags
cat tenant.tags.temp | sort -u > tenant.tags
