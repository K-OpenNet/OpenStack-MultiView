#!/bin/bash
#
# Name          : create_systemd_service.sh
# Description   : A script for executing packet tracing scripts as systemd service.
#
# Created by    : Muhammad Usman
# Version       : 0.1
# Last Update   : May, 2018
#

# This script must be executed by root user
if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

mv /opt/FlowAgent/control-plane-tracing.service /etc/systemd/system
mv /opt/FlowAgent/data-plane-tracing.service /etc/systemd/system
mv /opt/FlowAgent/management-plane-tracing.service /etc/systemd/system

chmod 664 /etc/systemd/system/control-plane-tracing.service
chmod 664 /etc/systemd/system/data-plane-tracing.service
chmod 664 /etc/systemd/system/management-plane-tracing.service

systemctl daemon-reload

systemctl enable control-plane-tracing.service
systemctl enable data-plane-tracing.service
systemctl enable management-plane-tracing.service

systemctl start control-plane-tracing.service
systemctl start data-plane-tracing.service
systemctl start management-plane-tracing.service

#sudo  journalctl --follow -u control-plane-tracing
#sudo  journalctl --follow -u data-plane-tracing
#sudo  journalctl --follow -u management-plane-tracing