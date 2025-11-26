#!/bin/bash

cd ../AntFileCreator
ant &> /dev/null
echo "Creating build.xml files..."
java -jar dist/*.jar &> /dev/null
cd - &> /dev/null

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
	echo "Running Ant on $line..." 
	cd $DIR
	ant &> /dev/null
	if [ ! -z $1 ]; then
		if [ -d "./dist" ]; then
			echo "Copying $(ls dist/*.jar) to $1"
			cp dist/*.jar $1
		fi
	fi
	cd - &> /dev/null
done
