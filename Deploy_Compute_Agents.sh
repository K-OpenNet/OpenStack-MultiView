#!/bin/bash

if [ $# -lt 6 ]
then
    echo "Usage: `basename $0` <Box Account> <IP Address> <Box_name> <Directory> <VM_Network_Interface> <External_Network Interface> <ElasticSearch IP>" 1>&2
    exit
fi


#echo "Acccount is $1"
Acount=$1
IP=$2
Box_name=$3
Dir=$4
VM_Net=$5
Ext_Net=$6
ELASTIC=$7



cp src/deploy_template/Template_Compute.sh jade/SmartX_Agent_Compute.sh

sed -i "s/BOXNAME/$3/g" jade/SmartX_Agent_Compute.sh
sed -i "s/EXTNET/$6/g" jade/SmartX_Agent_Compute.sh
sed -i "s/VMNET/$5/g" jade/SmartX_Agent_Compute.sh
sed -i "s/ELASTIC/$7/g" jade/SmartX_Agent_Compute.sh


cp src/jade_template/JADE_Compute.sh jade
sed -i "s/ELASTICIP/$7/g" jade/JADE_Compute.sh
sed -i "s/LOCALIP/$2/g" jade/JADE_Compute.sh
sed -i "s/NAME/$3/g" jade/JADE_Compute.sh




scp -r jade $1@$IP:$Dir
rm jade/JADE_Compute.sh
rm jade/SmartX_Agent_Compute.sh

