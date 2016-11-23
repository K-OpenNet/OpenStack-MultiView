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
# Name          : Install_Dependencies.sh
# Description   : Script for Installing Dependencies
#
# Created by    : usman@smartx.kr
# Version       : 0.1
# Last Update   : November, 2016

MGMT_IP=$1

#Ensure Root User Runs this Script
if [ "$(id -u)" != "0" ]; then
   echo "[$(date '+%Y-%m-%d %H:%M:%S')][ERROR][INSTALL]This script must be run as root" 1>&2
   exit 1
fi

wgetExist=`dpkg -l | grep wget`
if [ "$wgetExist" == "" ]; then
echo "[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] wget Installing..."
apt-get -y install wget nmap 
fi

MultiView-Dependencies/Install_Oracle_Java.sh
MultiView-Dependencies/Install_InfluxDB.sh MGMT_IP
MultiView-Dependencies/Install_Elasticsearch.sh MGMT_IP
MultiView-Dependencies/Install_MongoDB.sh MGMT_IP
MultiView-Dependencies/Install_NodeJS.sh
MultiView-Dependencies/Install_Kafka.sh
MultiView-Dependencies/Install_NodeJS_Libraries.sh
