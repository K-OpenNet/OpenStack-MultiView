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
# Name          : Install_Oracle_Java.sh
# Description   : Script for Installing Java
#
# Created by    : usman@smartx.kr
# Version       : 0.1
# Last Update   : October, 2016

javaExist=`dpkg -l | grep oracle-java`
if [ "$javaExist" == "" ]; then
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] JAVA Installing..."
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get -y update
echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
sudo apt-get -y install oracle-java8-installer
sudo apt-get -y install oracle-java8-set-default
java -version
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] JAVA Already Installed."
echo `java -version`
fi
