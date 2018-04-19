#!/bin/bash
#
# Name          : transfer_iovisor_data.sh
# Description   : A script for transferring network packets data to the SmartX Visibility Center
#
# Created by    : Muhammad Usman
# Version       : 0.1
# Last Update   : April, 2018

#Modify these parameters before execution on SmartX Boxes
#Also install sshpass and add SmartX Visibility Center IP for automatic logins.

VISIBILITY_CENTER_IP=
VISIBILITY_CENTER_USER=

TARGETFOLDER='/home/netcs/IOVisor-Data/SmartX-Box-UM'
SOURCEFOLDER='/opt/IOVisor-Data'

Minute="$(date +'%M')"
Hour="$(date +'%H')"
cDate="$(date +'%Y-%m-%d')"
if [ "$Minute" -le 30 ]
then
  if [ "$Hour" -eq 0 ]
  then
    Hour=23
  else
    Hour=$(( Hour-1 ))
  fi
  FILE1="/opt/IOVisor-Data/management-$cDate-$Hour-30"
  FILE2="/opt/IOVisor-Data/management-$cDate-$Hour-30"
  FILE3="/opt/IOVisor-Data/management-$cDate-$Hour-30"
else
  FILE1="/opt/IOVisor-Data/management-$cDate-$Hour-00"
  FILE2="/opt/IOVisor-Data/control-$cDate-$Hour-00"
  FILE3="/opt/IOVisor-Data/data-$cDate-$Hour-00"
fi 

echo $FILE1
echo $FILE2
echo $FILE3
sshpass -p $PASS scp {$FILE1,$FILE2,$FILE3} $VISIBILITY_CENTER_USER@$VISIBILITY_CENTER_IP:$TARGETFOLDER

sudo rm -rf $FILE1 $FILE2 $FILE3
