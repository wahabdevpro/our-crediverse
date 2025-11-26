#!/bin/bash
/sbin/ifconfig igb0:1 unplumb

ANSWER=`/sbin/ifconfig igb0:1`
if [[ $ANSWER == *UP* ]]
then
        echo "Could not Detach the VIP"
        exit 1
fi

mysql -u root -pussdgw -e "CHANGE MASTER TO MASTER_HOST='192.168.0.182', MASTER_USER='replication', MASTER_PASSWORD='ussdgw';"
