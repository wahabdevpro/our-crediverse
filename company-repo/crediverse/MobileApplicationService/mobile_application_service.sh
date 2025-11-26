#!/bin/bash
echo "s$LOG_FILE_SIZE_BYTES" >/var/log/mas/config
echo "n$LOG_FILE_COUNT_LIMIT" >>/var/log/mas/config
echo "N$LOG_FILE_COUNT_MIN" >>/var/log/mas/config
exec /usr/local/bin/mas-service 2>&1 | tee >(svlogd "/var/log/mas")
