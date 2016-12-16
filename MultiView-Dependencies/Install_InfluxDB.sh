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
# Name          : Install_InfluxDB.sh
# Description   : Script for Installing InfluxDB
#
# Created by    : usman@smartx.kr
# Version       : 0.1
# Last Update   : November, 2016

MGMT_IP=$1

influxdb=`dpkg -l | grep influx`
if [ "$influxdb" == "" ]; then
echo -n "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] InfluxDB Installing .................... "
wget --secure-protocol=TLSv1 https://dl.influxdata.com/influxdb/releases/influxdb_1.0.2_amd64.deb &> /dev/null
sudo dpkg -i influxdb_1.0.2_amd64.deb &> /dev/null
rm -rf influxdb_1.0.2_amd64.deb
echo -e "Done."
echo `influx -version`
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] InfluxDB Already Installed."
echo `influx -version`
fi
