#!/bin/bash

find ../../.. -name "build.xml" | egrep '.*services.*|.*connectors.*' | while read line; do
	key=`echo $line | cut -d '/' -f 6`
	if [[ ! -z $2 && $2 != *$key* ]]
	then
		key=`echo $line | cut -d '/' -f 7`
		if [[ ! -z $2 && $2 != *$key* ]]
		then
			continue;
		fi
	fi
	DIR=`dirname $line`
	cd $DIR
	if [ ! -z $1 ]; then
		if [ -d "./reports" ]; then
			echo "Copying $(ls reports/*.jasper) to $1"
			cp reports/*.jasper $1
		fi
	fi
	cd - &> /dev/null
done
