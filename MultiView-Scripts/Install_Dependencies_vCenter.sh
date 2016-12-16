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
# Name          : Install_Dependencies_vCenter.sh
# Description   : Script for Installing Dependencies on Visibility Center
#
# Created by    : usman@smartx.kr
# Version       : 0.2
# Last Update   : December, 2016

MGMT_IP=$1

wget_check ()
{
  if command -v wget > /dev/null; then
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] wget Already Installed.\n"
  else
    echo -n "[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] wget Installing .................... "
    apt-get -y install wget nmap &> /dev/null
	echo -e "Done.\n"
  fi
}

wget_check
MultiView-Dependencies/Install_Oracle_Java.sh
MultiView-Dependencies/Install_InfluxDB.sh "$MGMT_IP"
MultiView-Dependencies/Install_Elasticsearch.sh "$MGMT_IP"
MultiView-Dependencies/Install_MongoDB.sh "$MGMT_IP"
MultiView-Dependencies/Install_NodeJS.sh
MultiView-Dependencies/Install_Kafka.sh
MultiView-Dependencies/Install_NodeJS_Libraries.sh
