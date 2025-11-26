#!/bin/bash

if [ -z $NAME ]; then
	NAME=`pwd | awk '{ for(i=length;i!=0;i--)x=x substr($0,i,1);}END{print x}'  | cut -d '/' -f 1 | awk '{ for(i=length;i!=0;i--)x=x substr($0,i,1);}END{print x}'`
fi

if [ -z $VERSION ]; then
	VERSION="0.0"
fi

ANT=ant
if [ ! -z $ANT_HOME ]; then
	ANT=$ANT_HOME/bin/ant	
fi

TEXT="[creating (CUSTOM) $NAME application (version: $VERSION)] "
length=$MAX_LENGTH-${#TEXT}
for (( i=0; i < $length; i++))
do
	TEXT="${TEXT} "
done

# CUSTOM SETUP

if [ -d dist ]; then
	rm -rf dist
fi

$ANT clean &> /dev/null
ERROR=$("$ANT")

mkdir -p dist/lib
cp -R classes/hxc dist/lib
cp -R lib dist/
cp -R etc dist/
cp -R share dist/
mkdir -p dist/var/log

if [ ! -z $APPLICATION_PATH ]; then

	mkdir dist/bin
	echo "TEMPLATE: ${PWD}/install/run.template -> ${PWD}/dist/bin/$NAME"
	echo "TEMPLATE: run.template: Replacing \${PATH} with $APPLICATION_PATH"
	sed -e "s@\${PATH}@$APPLICATION_PATH@g" install/run.template > dist/bin/$NAME
	echo "TEMPLATE: run.template: setting executable bit on ${PWD}/dist/bin/$NAME"
	chmod +x dist/bin/$NAME

fi

if [ ! -d "./dist" ]; then
	
	echo "${TEXT}[FAILED: $ERROR]"

else

	echo "${TEXT}[DONE]"

fi

