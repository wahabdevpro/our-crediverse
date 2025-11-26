#!/bin/bash
echo "s$LOG_FILE_SIZE_BYTES" > /var/log/credistats-server/config
echo "n$LOG_FILE_COUNT_LIMIT" >> /var/log/credistats-server/config
echo "N$LOG_FILE_COUNT_MIN" >> /var/log/credistats-server/config
exec node ./bin/www.mjs 2>&1 | tee >(svlogd "/var/log/credistats-server")

