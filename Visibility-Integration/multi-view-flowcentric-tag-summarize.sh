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
# Name          : multi-view-flowcentric-summarize-tag.sh
# Description   : Run spark submit job.
#
# Created by    : Muhammad Usman
# Version       : 0.1
# Last Update   : December, 2018

/opt/KONE-MultiView/MultiView-Dependencies/spark-2.2.0-bin-hadoop2.7/bin/spark-submit --class smartx.multiview.flowcentric.Main --master local[*] --driver-memory 32g multi-view-flowcentric-tag-summarize_2.11-0.1.jar
