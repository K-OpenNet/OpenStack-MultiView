#!/bin/bash

if [ $# -lt 5 ]
then
    echo "Usage: `basename $0` <IP Address> <Box_name> <Directory> <VM_Network_Interface> <External_Network Interface>" 1>&2
    exit
fi

IP=$1
Box_name=$2
Dir=$3
VM_Net=$4
Ext_Net=$5

