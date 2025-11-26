#!/bin/bash

if [ -z $1 ]; then
	echo "Error: No peer supplied as parameter"
	exit 1
fi

EXISTS=`ping -c 1 $1`
if [ ! $? -eq 0 ]; then
	echo "Error: $1 is down"
	exit 1
fi

if [ -z $2 ]; then
	echo "Error: No VIP supplied as parameter"
	exit 1
fi
VIP="$2"

EXISTS=`ping -c 1 $VIP`
if [ ! $? -eq 0 ]; then
	echo "Error: VIP is down"
	exit 1
fi

HOSTS=`ifconfig -a`
HASVIP=false
if [[ "$HOSTS" == *$VIP* ]];
then
	HASVIP=true
fi

if [ $HASVIP == true ]; then
	if [ -z $3 ]; then
		echo "Error: VIP is up, but server is not incumbent"
		exit 1
	fi
else
	if [ ! -z $3 ]; then
		echo "Error: VIP is down, but server is incumbent"
		exit 1
	fi
fi

exit 0
