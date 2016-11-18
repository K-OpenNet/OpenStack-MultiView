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
# Name          : Install_NodeJS.sh
# Description   : Script for Installing Java
#
# Created by    : usman@smartx.kr
# Version       : 0.1
# Last Update   : November, 2016

NodeJSExist=`dpkg -l | grep  nodejs`
if [ "$NodeJSExist" == "" ]; then
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] NodeJS Installing..."
apt-get install -y nodejs npm
ln -s /usr/bin/nodejs /usr/bin/node
echo `node -v`
else
echo -e "\n[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] NodeJS Already Installed."
echo `node -v`
fi
