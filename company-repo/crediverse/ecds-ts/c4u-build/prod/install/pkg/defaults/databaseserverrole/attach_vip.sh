#!/bin/bash
/sbin/ifconfig igb0:1 plumb
/sbin/ifconfig igb0:1 192.168.0.182 netmask 255.255.254.0 up

ANSWER=`/sbin/ifconfig igb0:1`
if [[ ! $ANSWER == *UP* ]]
then
        echo "Could not Create VIP"
        exit 1
fi

mysql -u root -pussdgw -e "STOP SLAVE; RESET MASTER;"
