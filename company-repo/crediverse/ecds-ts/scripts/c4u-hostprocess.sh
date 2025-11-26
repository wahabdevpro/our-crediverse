#!/bin/sh
ARG_SWITCH="${1}"
shift
ARG_PARAMS="${@}"
BASEDIR=/opt/cs/c4u/
C4U_LOG_DIR=/var/opt/cs/c4u/log
export HOST_HOSTNAME=`cat /etc/hostname`
C4U_HOME=/opt/cs/c4u/1.0
export BASEDIR C4U_LOG_DIR

JAVA=java
CLASSPATH=${BASEDIR}/classes/:${BASEDIR}/classes/*:${BASEDIR}/conf/:${BASEDIR}/lib/*
LOGLEVEL=INFO
TIMEZONE="-Duser.timezone=${TZ:-'Africa/Johannesburg'}"
if [ "${ARG_SWITCH}" = "server" ];
then
	export APP_NAME=crediverse-ts
	#Default to 3000M if JVM_MEMORY is not set.
	JVM_MEMORY="${JVM_MEMORY:--Xmx3g -Xms3g}"
	GCLOGGING="-Xlog:gc:/var/opt/cs/c4u/log/java-gc -verbose:gc"
	GCCHOICE="-XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=1 -XX:G1MaxNewSizePercent=7"
	SECURITY="-Djava.security.egd=file:/dev/./urandom"
	CMDLINE="${JAVA} -server ${JVM_MEMORY} ${TIMEZONE} ${SECURITY} ${GCCHOICE} ${GCLOGGING} -cp ${C4U_HOME}/lib/*:${C4U_HOME}/lib:${C4U_HOME}/hostprocess/lib/*:${C4U_HOME}/hostprocess/plugins/* hxc.test.HostObject"
	echo ${CMDLINE}
	${CMDLINE}
elif [ "${ARG_SWITCH}" = "gui" ];
then
	export APP_NAME=crediverse-c4u-gui
	CLASSPATH="${C4U_HOME}/hostprocess/lib/*:${C4U_HOME}/guiserver/lib:${C4U_HOME}/guiserver/lib/*:${C4U_HOME}/guiserver/lib/*.jar:${C4U_HOME}/lib/*"
	CMDLINE="${JAVA} -server ${TIMEZONE} -cp ${CLASSPATH} hxc.userinterfaces.gui.jetty.JettyMain ${ARG_PARAMS}"
	echo ${CMDLINE}
	${CMDLINE}
elif [ "${ARG_SWITCH}" = "config" ];
then
	export APP_NAME=crediverse-config
	JVM_MEMORY="${JVM_MEMORY:--Xmx1g -Xms1g}"
	SECURITY="-Djava.security.egd=file:/dev/./urandom"
	CMDLINE="${JAVA} -server ${JVM_MEMORY} ${TIMEZONE} ${SECURITY} -cp ${C4U_HOME}/lib/*:${C4U_HOME}/lib:${C4U_HOME}/hostprocess/lib/*:${C4U_HOME}/hostprocess/plugins/* hxc.ecds.configure.EcdsConfigure ${ARG_PARAMS}"
	echo ${CMDLINE}
	${CMDLINE}
elif [ "${ARG_SWITCH}" = "airsim" ];
then
	export APP_NAME=crediverse-airsim
	#Default to 512M if JVM_MEMORY is not set.
	echo "Removing CreditDistributionService*.jar"
	rm /opt/cs/c4u/1.0/hostprocess/plugins/CreditDistributionService.jar
	rm /opt/cs/c4u/1.0/lib/CreditDistributionService-1.7.5-beta-10.jar
	echo "Removing TamperCheckConnector.jar"
	rm /opt/cs/c4u/1.0/hostprocess/plugins/TamperCheckConnector.jar
	JVM_MEMORY="${JVM_MEMORY:--Xmx512m -Xms512m}"
	GCLOGGING="-Xlog:gc:/var/opt/cs/c4u/log/java-gc -verbose:gc"
	GCCHOICE="-XX:+UseG1GC"
	SECURITY="-Djava.security.egd=file:/dev/./urandom"
	CMDLINE="${JAVA} -server ${JVM_MEMORY} ${TIMEZONE} ${SECURITY} ${GCCHOICE} ${GCLOGGING} -cp ${C4U_HOME}/lib/*:${C4U_HOME}/lib:${C4U_HOME}/hostprocess/lib/*:${C4U_HOME}/hostprocess/plugins/* hxc.test.HostObject"
	echo ${CMDLINE}
	${CMDLINE}
else
	echo "Accepted arguments are 'server', 'gui', 'airsim' and 'config'."
fi

