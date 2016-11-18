#!/bin/bash
#
# Copyright 2015 SmartX Collaboration (GIST NetCS). All rights reserved.
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
# Name          : Install_MongoDB.sh
# Description   : Script for Installing MongoDB
#
# Created by    : usman@smartx.kr
# Version       : 0.1
# Last Update   : October, 2016


MGMT_IP=$1

mongoExist=`ls | grep mongo`
if [ "$mongoExist" == "" ]; then
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] MongoDB Installing..."
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv EA312927
echo "deb http://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/3.2 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.2.list
sudo apt-get update
sudo apt-get install -y mongodb-org
sed -i "s/bindIp: 127.0.0.1/bindIp: $MGMT_IP/g" /etc/mongod.conf
service mongod restart
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] MongoDB Already Installed."
fi
