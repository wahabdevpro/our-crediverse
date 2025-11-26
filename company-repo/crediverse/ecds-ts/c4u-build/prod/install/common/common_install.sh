#!/bin/bash

WAREHOUSE_HOST=warehouse.concurrent.co.za
WAREHOUSE_BASE_PATH=/var/opt/webdav/warehouse.concurrent.co.za/products/c4u
WAREHOUSE_PORT=22

########## UTIL FUNCTION FOR WAREHOUSING
function remote_mkdir() {
/usr/bin/sftp -o Port=$WAREHOUSE_PORT $WAREHOUSE_USER@$WAREHOUSE_HOST << EOD
	cd $WAREHOUSE_BASE_PATH
        mkdir $1
EOD
}

function warehouse_file()
{
WAREHOUSE_PATH=$1
FILE=$2

/usr/bin/sftp -o Port=$WAREHOUSE_PORT $WAREHOUSE_USER@$WAREHOUSE_HOST << EOD
	cd $WAREHOUSE_BASE_PATH
	cd $WAREHOUSE_PATH
	put $FILE
	quit
EOD
}


if [ -z "$NO_BUILD" ]; then
	echo "Auto Building..."
	######## CLEAN OUT
	rm -rf ../dist
	mkdir ../dist

	######## CALL BUILD
	cd ..
	ant
#	ant create_run_jar
	cd -

	######## CREATE STRUCTURE 
	cd ../dist
	mkdir bin
	mkdir lib
	mv *.jar lib
	cd -
else
	echo "Not building using existing structure"
fi


######## CREATE RUNNABLE (in bin)
#JARFILE=`find ../dist -maxdepth 1 -name *.jar`
#JARFILE=${JARFILE:11}
INSTALL_DIR=../../../install/common
TEMPLATE_FOLDER=$INSTALL_DIR
INSTALL_PATH=opt/cs/c4u/$VERSION/$APP_NAME
RUN_TEMPLATE=run.template
CLASSPATH="/$INSTALL_PATH/lib:/$INSTALL_PATH/lib/*:/$INSTALL_PATH/lib/*.jar"

#if [ -z "$JARFILE" ]; then
#	CLASSPATH="$INSTALL_PATH/lib:$INSTALL_PATH/lib/*"
#else
#	CLASSPATH="$INSTALL_PATH/$JARFILE"
#fi

echo "TEMPLATE: ${PWD}/$TEMPLATE_FOLDER/$RUN_TEMPLATE -> ${PWD}/../dist/bin/$APP_NAME"
echo "TEMPLATE: $RUN_TEMPLATE: Replacing \${APPS_DIR} with $APPS_DIR"
echo "TEMPLATE: $RUN_TEMPLATE: Replacing \${CLASSPATH} with $CLASSPATH"
echo "TEMPLATE: $RUN_TEMPLATE: Replacing \${MAIN_CLASS} with $MAIN_CLASS"

sed -e "s@\${APPS_DIR}@$APPS_DIR@g" \
	-e "s@\${CLASSPATH}@$CLASSPATH@g" \
	-e "s@\${MAIN_CLASS}@$MAIN_CLASS@g" $TEMPLATE_FOLDER/$RUN_TEMPLATE > ../dist/bin/$APP_NAME
echo "TEMPLATE: $RUN_TEMPLATE: chmod 755"
chmod 755 ../dist/bin/$APP_NAME

echo "Before calling base install script PWD:"
echo `pwd`

######## DEPENDING ON PLATFORM CALL CORECT CREATE_INSTALL SCRIPT
OS_INFO=`uname -a`

INSTALL_EXT="deb"
INSTALL_SCRIPT="base_deb.sh"


if [[ "$OS_INFO" == *Ubuntu* ]]
then
	# UBUNTU INSTALL
	INSTALL_EXT="deb"
	INSTALL_SCRIPT="base_deb.sh"
else
	# SOLARIS INSTALL
	INSTALL_EXT="deb"
	INSTALL_SCRIPT="base_deb.sh"
fi

#### CREATE INSTALL SCRIPT
#rm *.$INSTALL_EXT
rm -rf ./$INSTALL_EXT
mkdir $INSTALL_EXT
cd $INSTALL_EXT
source ../../../../install/$INSTALL_EXT/$INSTALL_SCRIPT
cd ..
#rm -rf $INSTALL_EXT
FILE=`ls *.$INSTALL_EXT`


exit 0



#Create folder for warehouse
read -p "Warehouse sftp user [$USER]: " WAREHOUSE_USER
WAREHOUSE_USER=${WAREHOUSE_USER:-$USER}

WAREHOUSE_PATH="$CUSTOMER|$VERSION"
CURDIR=""
ALLDIR=""
IFS="|"
set $WAREHOUSE_PATH
for item
do
        NEW_DIR=$CURDIR$item
        ALLDIR=$ALLDIR" "$NEW_DIR
        CURDIR=$CURDIR$item/
done

IFS=" "
LAST_PATH=""
for d in $ALLDIR ; do
	LAST_PATH=$d
        echo "making: $d"
        remote_mkdir $d
done

warehouse_file $LAST_PATH $FILE

