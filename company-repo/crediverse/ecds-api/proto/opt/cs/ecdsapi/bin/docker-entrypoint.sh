#!/bin/sh
ARG_SWITCH=${1}
export BASEDIR=/etc/opt/cs/ecdsapi/default
export LOG_DIR=/var/opt/cs/ecdsapi/log
export HOST_HOSTNAME=`cat /etc/hostname`
JAVA=java
LOGLEVEL=info
CMDLINE="${JAVA} -server -Decdsapi.instance=default -Djava.security.egd=file:/dev/./urandom -Dlogging.config=/etc/opt/cs/ecdsapi/default/logback.xml -jar /opt/cs/ecdsapi/libexec/ecds-api.jar cs.Application"
echo ${CMDLINE}
${CMDLINE}