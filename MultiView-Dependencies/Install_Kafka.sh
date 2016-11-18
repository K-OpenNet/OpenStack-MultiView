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
# Name          : Install_Kafka.sh
# Description   : Script for Getting Kafka
#
# Created by    : usman@smartx.kr
# Version       : 0.1
# Last Update   : November, 2016

kafkaExist=`ls | grep kafka`
if [ "$kafkaExist" == "" ]; then
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Get Zookeeper..."
wget http://apache.mirror.cdnetworks.com/zookeeper/zookeeper-3.4.9/zookeeper-3.4.9.tar.gz
tar -xvzf zookeeper-3.4.9.tar.gz
rm -rf zookeeper-3.4.9.tar.gz
mv zookeeper-3.4.9 zookeeper
mv zookeeper/conf/zoo_sample.cfg zookeeper/conf/zoo.cfg
#zookeeper/bin/zkServer.sh start

echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Get Kafka..."
wget http://ftp.jaist.ac.jp/pub/apache/kafka/0.10.0.0/kafka_2.11-0.10.0.0.tgz
tar -xvzf kafka_2.11-0.10.0.0.tgz
rm -rf kafka_2.11-0.10.0.0.tgz
mv kafka_2.11-0.10.0.0 kafka
echo "delete.topic.enable = true" >> kafka/config/server.properties
#kafka/bin/kafka-server-start.sh kafka/config/server.properties
#In case kafka server can't start then add IP to /etc/hosts
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Kafka Already Installed."
fi
