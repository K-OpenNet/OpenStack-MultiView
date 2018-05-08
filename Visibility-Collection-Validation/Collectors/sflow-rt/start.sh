#!/bin/sh

HOME=`dirname $0`
cd $HOME

RTMEM="${RTMEM:-200M}"
JAR="./lib/sflowrt.jar"
JVM_OPTS="-Xms$RTMEM -Xmx$RTMEM -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
RT_OPTS="-Dsflow.port=6343 -Dhttp.port=8008"

exec java ${JVM_OPTS} ${RT_OPTS} ${RTPROP} -jar ${JAR}

