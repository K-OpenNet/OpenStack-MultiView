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
# Name          : Install_Elasticsearch.sh
# Description   : Script for Installing Elasticsearch
#
# Created by    : usman@smartx.kr
# Version       : 0.1
# Last Update   : October, 2016

MGMT_IP=$1

Elasticsearch=`dpkg -l | grep elasticsearch`

if [ "$Elasticsearch" == "" ]; then
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Elasticsearch Installing..."
CurrentDir=`pwd`
cd /tmp/
wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.0.0.deb
sudo dpkg -i elasticsearch-5.0.0.deb
sudo update-rc.d elasticsearch defaults 95 10

# Configure Elasticsearch
sed -i "s/#cluster.name: elasticsearch/cluster.name: elasticsearch/g" /etc/elasticsearch/elasticsearch.yml
sed -i "s/# network.host: 192.168.0.1/network.host: $MGMT_IP/g" /etc/elasticsearch/elasticsearch.yml

sudo service elasticsearch restart
cd /usr/share/elasticsearch
#bin/elasticsearch-plugin install mobz/elasticsearch-head
cd $CurrentDir
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Elasticsearch Already Installed."
#echo `curl -XGET '$MGMT_IP:9200'`
fi
