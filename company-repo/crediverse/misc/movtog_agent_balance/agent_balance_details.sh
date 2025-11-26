#!/bin/bash

# get my own directory
AGENT_SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]:-$0}"; )" &> /dev/null && pwd 2> /dev/null; )";
CREDIVERSE_AGENT_CONFIG_DIR=${CREDIVERSE_AGENT_CONFIG_DIR:=${AGENT_SCRIPT_DIR}}
if [ -f ${CREDIVERSE_AGENT_CONFIG_DIR}/.env ]
then
  # Use sed to ignore any line content after a #
  export $(cat ${CREDIVERSE_AGENT_CONFIG_DIR}/.env | sed 's/#.*$//' |  xargs)j
fi

# Database Configuration
CREDIVERSE_DB_USER="${CREDIVERSE_DB_USER:=root}"
CREDIVERSE_DB_PASSWORD="${CREDIVERSE_DB_PASSWORD:=password}"
CREDIVERSE_DB_PORT="${CREDIVERSE_DB_PORT:=3306}"
CREDIVERSE_DB_HOST="${CREDIVERSE_DB_HOST:=127.0.0.1}"
CREDIVERSE_DB_NAME="${CREDIVERSE_DB_NAME:=ecdsap}"
CREDIVERSE_COMPANY_ID="${CREDIVERSE_COMPANY_ID:=2}"
CREDIVERSE_DB_CONNECT_TIMEOUT="${CREDIVERSE_DB_CONNECT_TIMEOUT:=30}"

# Remote FTP Configuration
AGENT_FTP_HOST="${AGENT_FTP_HOST:=localhost}"
AGENT_FTP_USER="${AGENT_FTP_USER:=cs}"
AGENT_FTP_PASSWORD="${AGENT_FTP_PASSWORD:=cs}"
AGENT_FTP_PORT="${AGENT_FTP_PORT:=21}"
AGENT_FTP_USE_PASSIVE="${AGENT_FTP_USE_PASSIVE:=true}"
CREDIVERSE_REMOTEFILE_BASE="${CREDIVERSE_REMOTEFILE_BASE:=account_balance_data}"
CREDIVERSE_REMOTEFILE_SUFFIX="${CREDIVERSE_REMOTEFILE_SUFFIX:=.csv}"
CREDIVERSE_TEMP_DIR="${CREDIVERSE_TEMP_DIR:=/tmp}"


ftp_agent_file() {
  default_options=-inv
  remotefiletmp=${CREDIVERSE_REMOTEFILE_BASE}.`date +%Y%m%d-%H%M%S`.tmp
  remotefile=${CREDIVERSE_REMOTEFILE_BASE}.`date +%Y%m%d-%H%M%S`${CREDIVERSE_REMOTEFILE_SUFFIX}
  echo "remotefiletmp=${remotefiletmp}"
  echo "remotefile=${remotefile}"
  if [[ ${AGENT_FTP_USE_PASSIVE} == true ]]; then
    default_options=${default_options}p
  fi
  ftp ${default_options} ${AGENT_FTP_HOST} <<EOF
user ${AGENT_FTP_USER} ${AGENT_FTP_PASSWORD}
pwd
put ${SQLOUTFILE} ${remotefiletmp}
rename ${remotefiletmp} ${remotefile}
bye
EOF
}

run_agent_sql() {
  SQLOUTFILE="$(mktemp cs_agent_report.XXXXXXXX -p /tmp --suffix=.csv)"
  #SQLOUTFILE=test.csv
  echo "file = ${SQLOUTFILE}"
  echo "id,msisdn,balance,bonus_balance,group_name,tier_name,agent_name"> ${SQLOUTFILE}
  mysql --abort-source-on-error --connect_timeout=${CREDIVERSE_DB_CONNECT_TIMEOUT} -N -u$CREDIVERSE_DB_USER -p$CREDIVERSE_DB_PASSWORD -P$CREDIVERSE_DB_PORT -h$CREDIVERSE_DB_HOST $CREDIVERSE_DB_NAME >> ${SQLOUTFILE} <<EOF

SELECT CONCAT('"', id, '","',
              IFNULL(msisdn, ""),'","',
              IFNULL(balance, ""),'","',
              IFNULL(bonus_balance, ""),'","',
              IFNULL(group_name, ""),'","',
              IFNULL(tier_name, ""),'","',
              IFNULL(agent_name, ""),'"'
            )
FROM ap_agent_account
WHERE comp_id=$CREDIVERSE_COMPANY_ID
EOF
if [[ ${?} -ne 0 ]]; then
  echo "Unable to connect to DB host ${CREDIVERSE_DB_HOST}"
  status=5
else
  status=0
fi
}

run_agent_sql
if [[ ${status} -eq 0 ]]; then
  ftp_agent_file
fi
if [[ -f ${SQLOUTFILE} ]]; then
  #echo "Deleting tmp file ${SQLOUTFILE}"
  #cp -f ${SQLOUTFILE} test.csv
  rm -f ${SQLOUTFILE}
fi