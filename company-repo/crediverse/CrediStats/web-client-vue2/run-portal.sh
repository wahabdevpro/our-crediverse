#!/bin/bash
echo "s$LOG_FILE_SIZE_BYTES" > /var/log/credistats-portal/config
echo "n$LOG_FILE_COUNT_LIMIT" >> /var/log/credistats-portal/config
echo "N$LOG_FILE_COUNT_MIN" >> /var/log/credistats-portal/config
exec serve -p $WEB_PORTAL_PORT -s . 2>&1 | tee >(svlogd "/var/log/credistats-portal")

