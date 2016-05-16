#!/bin/bash

#ElasticSearch IP
IP="LOCALIP"
#OpenStack Env
#Env=
#Collection Period
Period="2"


source $Env/admin-openrc.sh

while [ true ]
do

  Hyp_list=`nova hypervisor-list  | awk '{print $4}' | sed -e "s/Hypervisor//g"`

  name=`date '+%Y.%m.%d'`
  #DATE=`date '+%Y-%m-%dT%H:%M:%S'`
  DATE=`date -d '9 hour ago' '+%Y-%m-%dT%H:%M:%S'`


  for hyp in $Hyp_list
  do
     CPUs=`nova hypervisor-show $hyp | grep vcpus | awk '{print $4}' | sed -n 1p`
     Memory=`nova hypervisor-show $hyp | grep memory | awk '{print $4}' | sed -n 1p`
     temp=100
     Memory=`echo $Memory $temp | awk '{printf "%.2f", $1 / $2}'`
   
     echo "$i CPU: $CPUs"
     echo "$i Memory: $Memory"

     #Each Hypervisor has several VMs
     Array=`nova hypervisor-servers $hyp | awk '{print $2}' | sed -e "s/ID//g"`
     echo '{' > function_visibility

     echo '{' > vbox_visibility

     #Each VM
     for i in $Array
     do

        echo $i
        check=`nova show $i | grep vm_state | awk '{print $4}'`


        # We don't check resources utilization on stooped VM
        if [ "$check" == "stopped" ]
        then
                continue
        fi


        user_id=`nova show $i | grep user_id | awk '{print $4}'`
        user_name=`openstack user list | grep $user_id | awk '{print $4}'`
        instance_name=`nova show $i | grep name | sed -n '4p' | awk '{print $4}'`

        echo $user_name
        echo $instance_name

        resource_id=`ceilometer meter-list | grep $i | grep network.incoming.bytes.rate | awk '{print $8}'`
        vcpu_util=`ceilometer sample-list -m cpu_util -q resource_id=$i -l 1 | sed -n "4p" | awk '{print $8}'`
        vcpus=`ceilometer statistics -m vcpus -q resource_id=$i | sed -n "4p" | awk '{print $12}'`
        echo $vcpus
        echo $vcpu_util


        temp=`echo $vcpus $vcpu_util | awk '{printf "%.5f", $1 * $2}'`
        vCPU=`echo $temp $CPUs | awk '{printf "%.5f", $1 / $2}'`
        echo $vCPU

        memory_usage=`ceilometer sample-list -m memory.usage -q resource_id=$i -l 1 | awk '{print $8}' | sed -n "4p"`
        mem=`ceilometer statistics -m memory -q resource_id=$i | sed -n "4P" | awk '{print $12}'`

        echo "Memory Usage: $memory_usage"
        vMemory=`echo $memory_usage $Memory | awk '{printf "%.5f", $1 / $2}'`
        echo $vMemory
        vMemory2=`echo $memory_usage $mem | awk '{printf "%.5f", $1 / $2 * 100 }'`

        vNetwork_outgoing=`ceilometer sample-list -m network.outgoing.bytes.rate -q resource_id=$resource_id -l 1 | awk '{print $8}' | sed -n "4p"`
        vNetwork_incoming=`ceilometer sample-list -m network.incoming.bytes.rate -q resource_id=$resource_id -l 1 | awk '{print $8}' | sed -n "4p"`

        echo "Vnetwork_outgoing: $vNetwork_outgoing"
        echo "vNetwork_incoming: $vNetwork_incoming"



        # This is for vBox_visibility
        echo '
        "'$hyp'-'$i'":
        {
         "Instance_name": "'$instance_name'",
         "User_name": "'$user_name'",
         "vCPUs": '$vcpus',
         "vCPU_util": '$vcpu_util',
         "vMEM_util": '$vMemory2',
         "vNetwork_outgoing_rate": '$vNetwork_outgoing',
         "vNetwork_incoming_rate": '$vNetwork_incoming'
        },' >> vbox_visibility

        # This is for Function_visibility
        echo '
         "'$hyp'-'$i'":
         {
          "Instance_name": "'$instance_name'",
          "User_name": "'$user_name'",
          "vCPU_util": '$vCPU',
          "vMemory_util": '$vMemory',
          "vNetwork_outgoing_rate": '$vNetwork_outgoing',
          "vNetwork_incoming_rate": '$vNetwork_incoming'
         }, ' >> function_visibility
    #end of for that each VMs 
    done

  echo '
        "@timestamp": "'$DATE'"
}' >> vbox_visibility

  echo '
        "@timestamp": "'$DATE'"
}' >> function_visibility




  curl -XPOST "http://$IP:9200/function_visibility-$name/GJ" -d "`cat function_visibility`"
  curl -XPOST "http://$IP:9200/vbox_visibility-$name/GJ" -d "`cat vbox_visibility`"



   #echo $i

#for end (Hypervisor)
done

sleep $Period
#while end
done
