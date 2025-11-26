#!/bin/bash

script_dirname=$(dirname "${0}")
script_basename=$(basename "${0}")

[ -z "${ECDSAPI_PREFIX}" ] && {
	echo >&2 "ECDSAPI_PREFIX not set ... calculating from script location ${script_dirname}"
	if [ -e "${script_dirname}/../libexec/ecds-api.jar" ]
	then
		ECDSAPI_PREFIX="${script_dirname}/../"
	else
		echo >&2 "${script_dirname}/../libexec/ecds-api.jar does not exist ..."
		ECDSAPI_PREFIX="/opt/cs/ecdsapi"
	fi
}
echo >&2 "Using ECDSAPI_PREFIX=${ECDSAPI_PREFIX}"

: ${ECDSAPI_PROFILES:=}
echo >&2 "Using ECDSAPI_PROFILES=${ECDSAPI_PROFILES}"
: ${ECDSAPI_INSTANCE:=default}
echo >&2 "Using ECDSAPI_INSTANCE=${ECDSAPI_INSTANCE}"
: ${ECDSAPI_SYSCONFDIR:=/etc/opt/cs/ecdsapi/${ECDSAPI_INSTANCE}}
echo >&2 "Using ECDSAPI_SYSCONFDIR=${ECDSAPI_SYSCONFDIR}"
: ${ECDSAPI_SHAREDSTATEDIR:=/var/opt/cs/ecdsapi}
echo >&2 "Using ECDSAPI_SHAREDSTATEDIR=${ECDSAPI_SHAREDSTATEDIR}"

[ -d "${ECDSAPI_SYSCONFDIR}" ] || {
	echo >&2 "ERROR: ECDSAPI_SYSCONFDIR=${ECDSAPI_SYSCONFDIR} does not exist ... ending"
	exit 1
}

[ -e "${ECDSAPI_SYSCONFDIR}/application-prod.properties" ] || {
	echo >&2 "WARNING: ${ECDSAPI_SYSCONFDIR}/application-prod.properties does not exist"
}

export ECDSAPI_PROFILES ECDSAPI_INSTANCE ECDSAPI_SYSCONFDIR ECDSAPI_SHAREDSTATEDIR

# Read in logback file location, if it cannot be found set to default location
[ "${#}" -ge 1 ] && {
	[ -e "${1}" ] && {
		LOGBACK_CONFIG_FILE="${1}"
		echo >&2 "Using user supplied LOGBACK_CONFIG_FILE=${LOGBACK_CONFIG_FILE}"
	}
}

[ -z "${LOGBACK_CONFIG_FILE}" ] && {
	if [ -e "${ECDSAPI_SYSCONFDIR}/logback-spring.xml" ]
	then
		LOGBACK_CONFIG_FILE="${ECDSAPI_SYSCONFDIR}/logback-spring.xml"
		echo >&2 "Using sysconfig LOGBACK_CONFIG_FILE=${LOGBACK_CONFIG_FILE}"
	else
		LOGBACK_CONFIG_FILE="${ECDSAPI_PREFIX}/share/logback-spring.xml"
	fi
}

sum_array()
{
	local total=0
	local item
	for item in "${@}"
	do
		let total+="${item}"
	done
	echo "${total}"
	return 0
}

join_array()
{
	local IFS="$1";
	shift;
	echo "$*";
}

jre_prefixes=( /usr/java/jre1.8.0_ /opt/oracle-jre-bin-1.8.0. )
jdk_prefixes=( /usr/java/jdk1.8.0_ /opt/oracle-jdk-bin-1.8.0. )

find_latest_jxx()
{
	local jxx_what="${1}"
	shift
	local -a prefixes=( "${@}" )
	local jxx_prefix
	local jxx_use
	for jxx_prefix in "${prefixes[@]}"
	do
		local -a jxx_candidates=( "${jxx_prefix}"* )
		echo >&2 "${jxx_what} candidates for prefix ${jxx_prefix} are ${jxx_candidates[@]}"
		local jxx_latest_version
		jxx_latest_version=$(join_array $'\n' "${jxx_candidates[@]#${jxx_prefix}}" | sort -nr | head -1; exit $( sum_array "${PIPESTATUS[@]}" )) || {
			echo >&2 "ERROR: Failed to determine latest ${jxx_what} version for prefix ${jxx_prefix} ... trying next ..."
			continue
		}
		local jxx_latest
		jxx_latest="${jxx_prefix}${jxx_latest_version}"
		echo >&2 "Preliminary latest ${jxx_what} version is ${jxx_latest}"
		[ -d "${jxx_latest}/bin" ] || {
			echo >&2 "${jxx_latest}/bin is not a directory ... probbably nothing matched ... trying next"
			continue
		}
		echo "${jxx_latest}"
		return 0
	done
	return 1
}

[ -z "${JAVA_HOME}" ] && {
	echo >&2 "JAVA_HOME is not set ... finding latest java from any of the following locations ( ${jre_prefixes[@]} ${jdk_prefixes[@]} )"
	jre_latest=$( find_latest_jxx JRE "${jre_prefixes[@]}" )
	jdk_latest=$( find_latest_jxx JDK "${jdk_prefixes[@]}" )
	if [ -z "${jre_latest}" ]
	then
		if [ -z "${jdk_latest}" ]
		then
			echo >&2 "Could not find latest JRE or JDK ... "
			exit 1
		else
			echo >&2 "Could not find latest JRE ... using latest JDK instead"
			jre_latest="${jdk_latest}/jre"
		fi
	fi
	echo >&2 "Setting JAVA_HOME=${jre_latest}"
	export JAVA_HOME="${jre_latest}"
}

[ -n "${JAVA_HOME}" ] || {
	echo >&2 "ERROR: JAVA_HOME is not set."
	exit 1
}

[ -e "${JAVA_HOME}/bin/java" ] || {
	echo >&2 "ERROR: ${JAVA_HOME}/bin/java does not exist ... please ensure JAVA_HOME is set correctly."
	exit 1
}

[ -x "${JAVA_HOME}/bin/java" ] || {
	echo >&2 "ERROR: ${JAVA_HOME}/bin/java is not executable ... please ensure JAVA_HOME is set correctly."
	exit 1
}

echo >&2 "Using JAVA_HOME=${JAVA_HOME}"

# Check Capabilities
{ getcap "${JAVA_HOME}/bin/java" | grep "cap_net_admin,cap_net_raw+eip" >/dev/null && echo "jvm has packet capture permissions"; } || {
	echo >&2 "ERROR: Java ( ${JAVA_HOME}/bin/java ) does not has packet capture permissions. Please check ${ECDSAPI_PREFIX}/docs/README.md for information on setting up Java Capabilities. Aborting..."
	echo >&2 "ERROR: quick fix: sudo setcap cap_net_raw,cap_net_admin=eip ${JAVA_HOME}/bin/java"
	exit 1
}

"${JAVA_HOME}/bin/java" -version || {
	echo >&2 "ERROR: Java ( ${JAVA_HOME}/bin/java ) seems to not be runnable. If the message mentions libjli.so see ${ECDSAPI_PREFIX}/docs/README.md for information on setting ld.so.conf. Aborting..."
	exit 1
}

command=( )
command=( "${command[@]}" "${JAVA_HOME}/bin/java" -d64 -server )
command=( "${command[@]}" "-Decdsapi.instance=${ECDSAPI_INSTANCE}" )
command=( "${command[@]}" "-Djava.security.egd=file:/dev/./urandom" )

## http://docs.spring.io/autorepo/docs/spring-boot/current/reference/html/boot-features-profiles.html#boot-features-profiles
[ -n "${ECDSAPI_PROFILES}" ] && command=( "${command[@]}" "-Dspring.profiles.active=${ECDSAPI_PROFILES}" )
command=( "${command[@]}" "-Dlogging.config=file:${LOGBACK_CONFIG_FILE}" )
command=( "${command[@]}" -jar "${ECDSAPI_PREFIX}/libexec/ecds-api.jar" cs.Application )
echo >&2 "Starting ECDSAPI with ${command[@]}"
exec "${command[@]}"

