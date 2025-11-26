#!/bin/sh
ARG_SWITCH=${1}
JAVA=java
CLASSPATH=${BASEDIR}/classes/:${BASEDIR}/classes/*:${BASEDIR}/conf/:${BASEDIR}/lib/*
LOGLEVEL=info
export LOG_DIR=/var/opt/cs/ecdsui/log
export HOST_HOSTNAME=`cat /etc/hostname`

CMDLINE="" 
if [ "${ARG_SWITCH}" = "admin" ];
then
	export BASEDIR=/etc/opt/cs/ecdsui/default
	export ECDSUI_SYSCONFDIR=/etc/opt/cs/ecdsui/default
	export ECDSUI_PROFILES=
	export ECDSUI_INSTANCE=default
	export ECDSUI_PREFIX=/opt/cs/ecdsui
	export ECDSUI_SHAREDSTATEDIR=/var/opt/cs/ecdsui
	export APP_NAME=crediverse-admin
	cd ${BASEDIR}
	CMDLINE="${JAVA} -server -Decdsui.instance=default -Djava.security.egd=file:/dev/./urandom -Dlogging.config=/etc/opt/cs/ecdsui/default/logback.xml -jar /opt/cs/ecdsui/libexec/ecds-gui.jar cs.Application"
	echo ${CMDLINE}
	${CMDLINE}
elif [ "${ARG_SWITCH}" = "portal" ];
then
	export BASEDIR=/etc/opt/cs/ecdsui/portal
	export ECDSUI_SYSCONFDIR=/etc/opt/cs/ecdsui/portal
	export ECDSUI_PROFILES=portal
	export ECDSUI_INSTANCE=portal
	export ECDSUI_PREFIX=/opt/cs/ecdsui
	export ECDSUI_SHAREDSTATEDIR=/var/opt/cs/ecdsui
	export APP_NAME=crediverse-agent-portal
	cd ${BASEDIR}
	CMDLINE="${JAVA} -server -Decdsui.instance=portal -Djava.security.egd=file:/dev/./urandom -Dlogging.config=/etc/opt/cs/ecdsui/default/logback.xml -Dspring.profiles.active=portal -jar /opt/cs/ecdsui/libexec/ecds-gui.jar cs.Application"
	echo ${CMDLINE}
	${CMDLINE}
elif [ "${ARG_SWITCH}" = "mobile" ];
then
	export BASEDIR=/etc/opt/cs/ecdsui/mobile
	export ECDSUI_SYSCONFDIR=/etc/opt/cs/ecdsui/mobile
	export ECDSUI_PROFILES=mobile
	export ECDSUI_INSTANCE=mobile
	export ECDSUI_PREFIX=/opt/cs/ecdsui
	export ECDSUI_SHAREDSTATEDIR=/var/opt/cs/ecdsui
	export APP_NAME=crediverse-mobile
	cd ${BASEDIR}
	CMDLINE="${JAVA} -server -Decdsui.instance=mobile -Djava.security.egd=file:/dev/./urandom -Dlogging.config=/etc/opt/cs/ecdsui/default/logback.xml -Dspring.profiles.active=mobile -jar /opt/cs/ecdsui/libexec/ecds-gui.jar cs.Application"
	echo ${CMDLINE}
	${CMDLINE}
else
	echo "Accepted arguments are 'admin', 'portal' and 'mobile'."
fi
