#!/bin/bash

BOX_name="BOXNAME"
External_Network="EXTNET"
VM_Network="VMNET"
CPU_NUM=`lscpu | grep CPU | sed -n "2P" | awk '{print $2}'`
Center_IP="ELASTIC"
Period="2"




num1=4
sum=$(expr "$CPU_NUM" + "$num1")

cm="4,"
cm+=$sum
cm+="P"

#echo "cm is $cm"



#cm="4,8P"


#install mpstat
#apt-get install -y sysstat


while [ true ] 
do

name=`date '+%Y.%m.%d'`
#DATE=`date '+%Y-%m-%dT%H:%M:%S'`
DATE=`date -d '9 hour ago' '+%Y-%m-%dT%H:%M:%S'`



num=0

echo '{' > json

CORE=`mpstat -P ALL 1 1 | sed -n "$cm" | awk '{print $13}'`

for i in $CORE
do
        if [ "$num" == 0 ]
        then
                temp=100
                cpu_util=`echo $temp $i | awk '{printf "%.2f", $1 - $2}'`
                echo "Core Total: $cpu_util"
                let num=num+1
                echo '  "'$BOX_name'_CPU_util": '$cpu_util', ' >> json
        else
        temp=100
        cpu_util=`echo $temp $i | awk '{printf "%.2f", $1 - $2}'`
        echo "Core $num: $cpu_util"
        echo '  "'$BOX_name'_Core'$num'_util": '$cpu_util', ' >> json
        let num=num+1
        fi
done

memtotal=`cat /proc/meminfo | grep MemTotal | awk '{print $2}'`
memfree=`cat /proc/meminfo | grep MemFree | awk '{print $2}'`

mem=`echo $memtotal $memfree | awk '{printf "%.f", $1 - $2}'`
mem_util=`echo $memtotal $mem | awk '{printf "%.2f", $2 / $1 * 100}'`

echo '  "'$BOX_name'_Mem_util": '$mem_util', ' >> json
#echo $mem_util

ext_incoming_rate=`ifstat -q -i $External_Network -S 1 1 | sed -n "3P" | awk '{print $2}'`
ext_outgoing_rate=`ifstat -q -i $External_Network -S 1 1 | sed -n "3P" | awk '{print $3}'`

vm_incoming_rate=`ifstat -q -i $VM_Network -S 1 1 | sed -n "3P" | awk '{print $2}'`
vm_outgoing_rate=`ifstat -q -i $VM_Network -S 1 1 | sed -n "3P" | awk '{print $3}'`


echo '  "'$BOX_name'_EXT_Network_incoming_rate": '$ext_incoming_rate',
	"'$BOX_name'_EXT_Network_outgoing_rate": '$ext_outgoing_rate',
	"'$BOX_name'_VM_Network_incoming_rate": '$vm_incoming_rate',
        "'$BOX_name'_VM_Network_outgoing_rate": '$vm_outgoing_rate', ' >> json


echo '  "@timestamp": "'$DATE'"
}' >> json

curl -XPOST "$IP:9200/box_visibility-$name/$BOX_name" -d "`cat json`"

sleep $period

done
