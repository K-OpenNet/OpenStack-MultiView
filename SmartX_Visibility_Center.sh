#!/bin/bash


#Setting your IP Address
IP="localhost"




# This is for the SmartX Visibility Center


# Setup Elasticsearch & Kibana


# Need to run as ROOT
if [ "$(id -u)" != "0" ]; then
	echo "This script must be run as root" 1>&2
	exit 1
fi



Elasticsearch=`dpkg -l | grep elasticsearch`

if [ "$Elasticsearch" == "" ]; then
        echo "You don't install Elasticsearch"

# Elasitcsearch Installation
sudo apt-get update
sudo apt-get install -y python-software-properties debconf-utils
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update
echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
sudo apt-get install -y oracle-java8-installer

#Add Elasticsearch in Repository
wget -qO - https://packages.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -

echo "deb http://packages.elastic.co/elasticsearch/2.x/debian stable main" | sudo tee -a /etc/apt/sources.list.d/elasticsearch-2.x.list


sudo apt-get update

sudo apt-get install -y elasticsearch

sed -i "s/# network.host: 192.168.0.1/network.host: $IP/g" /etc/elasticsearch/elasticsearch.yml

sudo service elasticsearch restart

sudo update-rc.d elasticsearch defaults 95 10

fi



#Kibana Installation
Kibana=`dpkg -l | grep kibana` 

if [ "$Kibana" == "" ]; then
	echo "You don't install Kibana"

echo "deb http://packages.elastic.co/kibana/4.4/debian stable main" | sudo tee -a /etc/apt/sources.list.d/kibana-4.4.x.list

sudo apt-get update

sudo apt-get install -y kibana

sed -i "s/# elasticsearch.url: \"http:\/\/localhost:9200\"/elasticsearch.url: \"http:\/\/$IP:9200\"/g" /opt/kibana/config/kibana.yml

sudo update-rc.d kibana defualts 96 9
sudo service kibana start

fi


# now, we deploy SmartX Agent files to targeted boxes..






