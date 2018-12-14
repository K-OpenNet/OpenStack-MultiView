#!/bin/bash
#
# Copyright 2018 SmartX Collaboration (GIST NetCS). All rights reserved.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#
# Name		    : transfer_data_from_ID_Center.sh
# Description	: Script for transferring data about SmartX Tags to SmartX Visibility Center. This script should be executed at ID Center via crontab. e.g. */5 * * * * /bin/bash /home/netcs/transfer_data_from_ID_Center.sh. sshpass should be installed in ID Center.
#
# Created by    : Muhammad Usman
# Version       : 0.1
# Last Update	: December, 2018
#

VISIBILITY_CENTER_IP=
VISIBILITY_CENTER_USER=
PASS=

TARGETFOLDER='/home/netcs/SmartX-Tagging'

sleep 1m
sshpass -p $PASS scp /home/netcs/ControlPlane.tags $VISIBILITY_CENTER_USER@$VISIBILITY_CENTER_IP:$TARGETFOLDER
sshpass -p $PASS scp /home/netcs/tenant.tags $VISIBILITY_CENTER_USER@$VISIBILITY_CENTER_IP:$TARGETFOLDER

