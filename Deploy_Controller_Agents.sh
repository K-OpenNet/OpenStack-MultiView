#!/bin/bash

if [ $# -lt 6 ]
then
    echo "Usage: `basename $0` <Box Account> <IP Address> <Box_name> <Directory> <ElasticSearch IP> <OpenStack Env>" 1>&2
    exit
fi


#echo "Acccount is $1"
Account=$1
IP=$2
Box_name=$3
Dir=$4
ELASTIC=$5
OPENSTACK=$6

#echo "OpenStack is $6"

cp src/deploy_template/Template_Controller.sh jade/SmartX_Agent_Controller.sh

sed -i "/#Env=/a\Env=\"$6\"" jade/SmartX_Agent_Controller.sh
sed -i "s/LOCALIP/$2/g" jade/SmartX_Agent_Controller.sh


cp src/jade_template/JADE_Controller.sh jade
sed -i "s/ELASTICIP/$5/g" jade/JADE_Controller.sh
sed -i "s/LOCALIP/$2/g" jade/JADE_Controller.sh
sed -i "s/NAME/$3/g" jade/JADE_Controller.sh




scp -r jade $1@$IP:$Dir
rm jade/JADE_Controller.sh
rm jade/SmartX_Agent_Controller.sh

