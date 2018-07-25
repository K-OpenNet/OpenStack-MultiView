#!/bin/bash
#
# Name          : transfer_iovisor_data.sh
# Description   : A script for transferring network packets data to the SmartX Visibility Center
#
# Created by    : Muhammad Usman
# Version       : 0.4
# Last Update   : July, 2018

#Modify these parameters before execution on SmartX Boxes
#Also install sshpass and add SmartX Visibility Center IP for automatic logins.

VISIBILITY_CENTER_IP=
VISIBILITY_CENTER_USER=

TARGETFOLDERDP='/home/netcs/IOVisor-Data/Data-Plane'
TARGETFOLDERCP='/home/netcs/IOVisor-Data/Control-Plane'

SOURCEFOLDER='/opt/IOVisor-Data'

Minute="$(date +'%M')"
Hour="$(date +'%H')"
cDate="$(date +'%Y-%m-%d')"

host=`hostname`
#if [ "$Hour" -lt 10 ]
#    then 
#        Hour="0$Hour"
#        echo $Hour
#fi

if [ "$Minute" -eq 0 ]
    then
		if [ "$Hour" -eq 0 ]
		then
			PREVIOUS_HOUR=23
			PREVIOUS_MINUTE=55
			PREVIOUS_DAY="$(date +'%d')"
			PREVIOUS_DAY=$((PREVIOUS_DAY - 1))
		else	
			PREVIOUS_HOUR=$(($HOUR - 1))
			PREVIOUS_MINUTE=55
			PREVIOUS_DAY="$(date +'%d')"
		fi
        cDate="$(date +'%Y-%m')"
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$PREVIOUS_DAY-$PREVIOUS_HOUR-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$PREVIOUS_DAY-$PREVIOUS_HOUR-$PREVIOUS_MINUTE"
elif [ "$Minute" -eq 5 ]
    then
        PREVIOUS_MINUTE=00
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE"
elif [ "$Minute" -eq 10 ]
    then
        PREVIOUS_MINUTE=05
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE"
elif [ "$Minute" -eq 15 ]
    then
        PREVIOUS_MINUTE=10
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE"
elif [ "$Minute" -eq 20 ]
    then
        PREVIOUS_MINUTE=15
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE"
elif [ "$Minute" -eq 25 ]
    then 
        PREVIOUS_MINUTE=20
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE" 
elif [ "$Minute" -eq 30 ]
    then
        PREVIOUS_MINUTE=25
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE"
elif [ "$Minute" -eq 35 ]
    then
        PREVIOUS_MINUTE=30
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE"
elif [ "$Minute" -eq 40 ]
    then
        PREVIOUS_MINUTE=35
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE"
elif [ "$Minute" -eq 45 ]
    then 
        PREVIOUS_MINUTE=40
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE"
elif [ "$Minute" -eq 50 ]
    then
        PREVIOUS_MINUTE=45
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE"
elif [ "$Minute" -eq 55 ]
    then
        PREVIOUS_MINUTE=50
        PREVIOUS_FILE1="/opt/IOVisor-Data/$host-mc-$cDate-$Hour-$PREVIOUS_MINUTE"
        PREVIOUS_FILE2="/opt/IOVisor-Data/$host-data-$cDate-$Hour-$PREVIOUS_MINUTE"
fi

echo "$PREVIOUS_FILE1 $PREVIOUS_FILE2"

sshpass -p $PASS scp $PREVIOUS_FILE1 $VISIBILITY_CENTER_USER@$VISIBILITY_CENTER_IP:$TARGETFOLDERCP
sshpass -p $PASS scp $PREVIOUS_FILE2 $VISIBILITY_CENTER_USER@$VISIBILITY_CENTER_IP:$TARGETFOLDERDP
sleep 20

shopt -s extglob; set -H
cd $SOURCEFOLDER
CURRENT_FILES=`ls -t1 | head -n 2 | sed 'N; s/\n/|/'`
sudo rm -v !($CURRENT_FILES)

if [ $? -eq 0 ]; then
    echo OK
else
    echo FAIL
fi
