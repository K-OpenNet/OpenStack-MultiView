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
# Name          : multi-view-flowcentric-integration.sh
# Description   : Run spark submit jobs.
#
# Created by    : Muhammad Usman
# Version       : 0.1
# Last Update   : January, 2019

export PATH=$PATH:$SPARK_HOME
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
export SPARK_HOME=/opt/KONE-MultiView/MultiView-Dependencies/spark-2.2.0-bin-hadoop2.7

while : 
do
  minute="$(date +'%-M')"
  remainder=$(( minute % 5 ))

  if [ "$remainder" -eq 0 ]; then
    sleep 60
    $SPARK_HOME/bin/spark-submit --class smartx.multiview.flowcentric.Main --master local[*] --driver-memory 24g multi-view-flowcentric-tag-aggregate_2.11-0.1.jar "vc.manage.overcloud" &&
    $SPARK_HOME/bin/spark-submit --class smartx.multiview.flowcentric.Main --master local[*] --driver-memory 16g multi-view-flowcentric-integration_2.11-0.1.jar "vc.manage.overcloud"
  else
    sleep 20
  fi
done
