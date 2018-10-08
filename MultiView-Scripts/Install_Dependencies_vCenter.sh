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
# Version       : 0.3
# Create Data   : October, 2016
# Last Update   : May, 2018

MGMT_IP=$1
VC_NAME="vc.manage.overcloud"

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

java_check ()
{
  if command -v java > /dev/null; then
    echo -e "[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] JAVA Already Installed.\n"
	echo -e `java -version`
  else
	echo -n "[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] JAVA Installing .................... "
	sudo add-apt-repository -y ppa:webupd8team/java &> /dev/null
	sudo apt-get -y update &> /dev/null
	echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
	echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
	sudo apt-get -y install oracle-java8-installer &> /dev/null
	sudo apt-get -y install oracle-java8-set-default &> /dev/null
	echo -e "Done.\n"
	java -version
  fi
}

influxDB_check()
{
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
}

elasticsearch_check()
{
Elasticsearch=`dpkg -l | grep elasticsearch`

if [ "$Elasticsearch" == "" ]; then
echo -e "[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Elasticsearch Installing .................... "
CurrentDir=`pwd`
cd /tmp/
wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
sudo apt-get install apt-transport-https &> /dev/null
echo "deb https://artifacts.elastic.co/packages/6.x/apt stable main" | sudo tee -a /etc/apt/sources.list.d/elastic-6.x.list
sudo apt-get update && sudo apt-get install elasticsearch &> /dev/null
sudo /bin/systemctl daemon-reload
sudo /bin/systemctl enable elasticsearch.service

#wget https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-5.0.0.deb &> /dev/null
#sudo dpkg -i elasticsearch-5.0.0.deb &> /dev/null
#sudo update-rc.d elasticsearch defaults 95 10 &> /dev/null

# Configure Elasticsearch
sed -i "s/#cluster.name: elasticsearch/cluster.name: elasticsearch/g" /etc/elasticsearch/elasticsearch.yml
sed -i "s/#network.host: 192.168.0.1/network.host: $MGMT_IP/g" /etc/elasticsearch/elasticsearch.yml

sudo systemctl restart elasticsearch.service &> /dev/null
echo -e "Done.\n"

cd $CurrentDir
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Elasticsearch Already Installed."
#echo `curl -XGET '$MGMT_IP:9200'`
fi
}

mongoDB_check()
{
mongoExist=`ls | grep mongo`
if [ "$mongoExist" == "" ]; then
echo -e "[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] MongoDB Installing .................... "
sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv EA312927 &> /dev/null
echo "deb http://repo.mongodb.org/apt/ubuntu trusty/mongodb-org/3.2 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-3.2.list
sudo apt-get update &> /dev/null
sudo apt-get install -y mongodb-org &> /dev/null
sed -i "s/bindIp: 127.0.0.1/bindIp: 0.0.0.0/g" /etc/mongod.conf
service mongod restart &> /dev/null
echo -e "Done. \n"
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] MongoDB Already Installed."
fi
}

nodeJS_check()
{
NodeJSExist=`dpkg -l | grep  nodejs`
if [ "$NodeJSExist" == "" ]; then
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] NodeJS Installing .................... "
apt-get install -y nodejs npm
ln -s /usr/bin/nodejs /usr/bin/node
echo -e "Done.\n"
echo `node -v`
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] NodeJS Already Installed."
echo `node -v`
fi
}

zookeeper_check()
{
zookeeperExist=`dpkg -l | grep  zookeeperd`
if [ "$zookeeperExist" == "" ]; then
echo -e ""
echo -n "[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Get Zookeeper .................... "
sudo apt install -y zookeeperd &> /dev/null
echo -e "Done.\n"
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Zookeeper Already Installed."
fi
}

kafka_check()
{
kafkaExist=`ls | grep kafka`
if [ "$kafkaExist" == "" ]; then
echo -e ""
echo -n "[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Get Kafka .................... "
wget http://mirror.apache-kr.org/kafka/1.1.0/kafka_2.11-1.1.0.tgz &> /dev/null

tar -xvzf kafka_2.11-1.1.0.tgz &> /dev/null
mv kafka_2.11-1.1.0 /opt
rm -rf kafka_2.11-1.1.0.tgz

mkdir /var/lib/kafka
mkdir /var/lib/kafka/data

echo "delete.topic.enable = true" >> /opt/kafka_2.11-1.1.0/config/server.properties
echo "advertised.host.name=$VC_NAME" >> /opt/kafka_2.11-1.1.0/config/server.properties
sed -i "s/log.dirs=\/tmp\/kafka-logs/log.dirs=\/var\/lib\/kafka\/data/g" /opt/kafka_2.11-1.1.0/config/server.properties
sed -i "s/num.partitions=1/num.partitions=2/g" /opt/kafka_2.11-1.1.0/config/server.properties
sed -i "s/log.retention.hours=168/log.retention.hours=24/g" /opt/kafka_2.11-1.1.0/config/server.properties
 
touch /etc/systemd/system/kafka.service

echo "[Unit]" >> /etc/systemd/system/kafka.service
echo "Description=High-available, distributed message broker" >> /etc/systemd/system/kafka.service
echo "After=network.target" >> /etc/systemd/system/kafka.service
echo "After=network-online.target" >> /etc/systemd/system/kafka.service
echo "[Service]" >> /etc/systemd/system/kafka.service
echo "User=root" >> /etc/systemd/system/kafka.service
echo "ExecStart=/opt/kafka_2.11-1.1.0/bin/kafka-server-start.sh /opt/kafka_2.11-1.1.0/config/server.properties" >> /etc/systemd/system/kafka.service
echo "Restart=on-failure" >> /etc/systemd/system/kafka.service
echo "RestartSec=30" >> /etc/systemd/system/kafka.service
echo "[Install]" >> /etc/systemd/system/kafka.service
echo "WantedBy=multi-user.target" >> /etc/systemd/system/kafka.service

systemctl daemon-reload
systemctl enable kafka.service
systemctl start kafka.service
systemctl status kafka.service

#kafka/bin/kafka-server-start.sh kafka/config/server.properties
#In case kafka server can't start then add IP to /etc/hosts
echo -e "Done. \n"
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Kafka Already Installed."
fi
}

multiviewJavaCollector_service()
{
touch /etc/systemd/system/multiview-java-collectors.service

echo "[Unit]" >> /etc/systemd/system/multiview-java-collectors.service
echo "Description=Multi-View Java-based Collectors" >> /etc/systemd/system/multiview-java-collectors.service
echo "After=network.target" >> /etc/systemd/system/multiview-java-collectors.service
echo "After=network-online.target" >> /etc/systemd/system/multiview-java-collectors.service
echo "[Service]" >> /etc/systemd/system/multiview-java-collectors.service
echo "User=root" >> /etc/systemd/system/multiview-java-collectors.service
echo "WorkingDirectory=/opt/OpenStack-MultiView/MultiView-RunnableJars" >> /etc/systemd/system/multiview-java-collectors.service
echo "ExecStart=/opt/OpenStack-MultiView/MultiView-RunnableJars/start-multiview-java-collectors" >> /etc/systemd/system/multiview-java-collectors.service
echo "SuccessExitStatus=143" >> /etc/systemd/system/multiview-java-collectors.service
echo "StandardOutput=null" >> /etc/systemd/system/multiview-java-collectors.service
echo "StandardError=null" >> /etc/systemd/system/multiview-java-collectors.service
echo "Restart=on-failure" >> /etc/systemd/system/multiview-java-collectors.service
echo "RestartSec=10" >> /etc/systemd/system/multiview-java-collectors.service
echo "[Install]" >> /etc/systemd/system/multiview-java-collectors.service
echo "WantedBy=multi-user.target" >> /etc/systemd/system/multiview-java-collectors.service

systemctl daemon-reload
systemctl enable multiview-java-collectors.service
systemctl start multiview-java-collectors.service
systemctl status multiview-java-collectors.service
}

sFlowRTCollector_service()
{
touch /etc/systemd/system/sflow-rt-collector.service

echo "[Unit]" >> /etc/systemd/system/sflow-rt-collector.service
echo "Description=sFlow-RT Collector" >> /etc/systemd/system/sflow-rt-collector.service
echo "After=network.target" >> /etc/systemd/system/sflow-rt-collector.service
echo "After=network-online.target" >> /etc/systemd/system/sflow-rt-collector.service
echo "[Service]" >> /etc/systemd/system/sflow-rt-collector.service
echo "User=root" >> /etc/systemd/system/sflow-rt-collector.service
echo "ExecStart=/opt/OpenStack-MultiView/Visibility-Collection-Validation/Collectors/sflow-rt/start.sh" >> /etc/systemd/system/sflow-rt-collector.service
echo "SuccessExitStatus=143" >> /etc/systemd/system/sflow-rt-collector.service
echo "StandardOutput=null" >> /etc/systemd/system/sflow-rt-collector.service
echo "StandardError=null" >> /etc/systemd/system/sflow-rt-collector.service
echo "Restart=on-failure" >> /etc/systemd/system/sflow-rt-collector.service
echo "RestartSec=10" >> /etc/systemd/system/sflow-rt-collector.service
echo "[Install]" >> /etc/systemd/system/sflow-rt-collector.service
echo "WantedBy=multi-user.target" >> /etc/systemd/system/sflow-rt-collector.service

systemctl daemon-reload
systemctl enable sflow-rt-collector.service
systemctl start sflow-rt-collector.service
systemctl status sflow-rt-collector.service
}

grafana_check ()
{
grafana=`dpkg -l | grep grafana`
if [ "$grafana" == "" ]; then
echo -n "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Grafana Installing .................... "
wget https://s3-us-west-2.amazonaws.com/grafana-releases/release/grafana_5.1.1_amd64.deb
sudo dpkg -i grafana_5.1.1_amd64.deb 

sudo systemctl enable grafana-server.service
sudo systemctl restart grafana-server.service
echo -e "Done."
echo `grafana-server -v`
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Grafana Already Installed."
echo `grafana-server -v`
fi
}

kibana_check ()
{
kibana=`dpkg -l | grep kibana`
if [ "$kibana" == "" ]; then
echo -n "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Kibana Installing .................... "
wget https://artifacts.elastic.co/downloads/kibana/kibana-6.2.4-amd64.deb &> /dev/null
shasum -a 512 kibana-6.2.4-amd64.deb
sudo dpkg -i kibana-6.2.4-amd64.deb

#sed -i "s/#server.port:.*/server.port: 5601/" /etc/kibana/kibana.yml
sed -i "s/#server.host:.*/server.host: $MGMT_IP/" /etc/kibana/kibana.yml
sed -i "s/#elasticsearch.url:.*/elasticsearch.url: '"http://$MGMT_IP:9200"'/" /etc/kibana/kibana.yml
sed -i "/#kibana.index:/c\kibana.index: .kibana/" /etc/kibana/kibana.yml

sudo systemctl daemon-reload
sudo systemctl enable kibana
sudo systemctl start kibana

rm -rf kibana-6.2.4-amd64.deb
echo -e "Done."
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Kibana Already Installed."
fi
}

nodeJSlib_check()
{
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] NodeJS Libraries Installing .................... "
sudo npm install npm -g 
currentDir=`pwd`
cd Visibility-Visualization/pvcT-Visualization
npm config set registry https://registry.npmjs.org/
npm cache clean
npm install
sudo npm install -g nodemon
echo -e "Done.\n"
cd $currentDir

touch /etc/systemd/system/multi-view-web.service

echo "[Unit]" >> /etc/systemd/system/multi-view-web.service
echo "Description=Multi-View, Visualization Application" >> /etc/systemd/system/multi-view-web.service
echo "After=network.target" >> /etc/systemd/system/multi-view-web.service
echo "After=network-online.target" >> /etc/systemd/system/multi-view-web.service
echo "[Service]" >> /etc/systemd/system/multi-view-web.service
echo "User=root" >> /etc/systemd/system/multi-view-web.service
echo "ExecStart=/usr/bin/node /opt/KONE-MultiView/Visibility-Visualization/pvcT-Visualization/server.js" >> /etc/systemd/system/multi-view-web.service
echo "Restart=on-failure" >> /etc/systemd/system/multi-view-web.service
echo "RestartSec=10" >> /etc/systemd/system/multi-view-web.service
echo "[Install]" >> /etc/systemd/system/multi-view-web.service
echo "WantedBy=multi-user.target" >> /etc/systemd/system/multi-view-web.service

systemctl daemon-reload
systemctl enable multi-view-web.service
systemctl start multi-view-web.service
systemctl status multi-view-web.service
}

wget_check
java_check
influxDB_check
elasticsearch_check
mongoDB_check
nodeJS_check
zookeeper_check
kafka_check
multiviewJavaCollector_service
sFlowRTCollector_service
grafana_check
kibana_check
nodeJSlib_check

