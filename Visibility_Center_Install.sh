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
# Name          : Visibility_Center_Install.sh
# Description   : Script for Installing Dependencies on SmartX Visibility Center.
#
# Created by    : usman@smartx.kr
# Version       : 0.3
# Last Update   : August, 2018

#Update Management IP according to your requirement
MGMT_IP=""

#Ensure Root User Runs this Script
if [ "$(id -u)" != "0" ]; then
   echo "[$(date '+%Y-%m-%d %H:%M:%S')][ERROR][INSTALL]This script must be run as root" 1>&2
   exit 1
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')][INFO][INSTALL] Multi-View Installation Started..."

MultiView-Scripts/Install_Dependencies_vCenter.sh "$MGMT_IP"
MultiView-Scripts/Create_MultiView_Database.sh "$MGMT_IP"