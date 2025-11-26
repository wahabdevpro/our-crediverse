#!/bin/bash

CUR=`pwd`
APP_NAME="cs-app-c4u"
CUSTOMER="core"
VERSION="0.2"
#SVN_REV=`svn info https://svn.concurrent.co.za/c4u/prod/ |grep '^Revision:' | sed -e 's/^Revision: //'`
INSTALL_PKG_NAME="cs-app-c4u"

WAREHOUSE_HOST=warehouse.concurrent.co.za
WAREHOUSE_BASE_PATH=/var/opt/webdav/warehouse.concurrent.co.za/products/c4u
WAREHOUSE_PORT=22
WAREHOUSE_USER="johne"

#### Utility functions
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

##### First check Java version
if [ -z $JAVA_HOME ]
then
	JAVA_VER=$(java -version 2>&1 | sed 's/java version "\(.*\)\.\(.*\)\..*"/\1\2/; 1q')
else
	JAVA_VER=$($JAVA_HOME/bin/java -version 2>&1 | sed 's/java version "\(.*\)\.\(.*\)\..*"/\1\2/; 1q')
fi

if [ "$JAVA_VER" -lt 17 ]
then
        echo "Java version needs to be at least 1.7"
	exit 0	
fi

# if argument is passed, this will be where the install file will be copied to
if [ ! -z $1 ]
then
	COMPILED_INSTALL_FILE_PATHR=$1
else
	COMPILED_INSTALL_FILE_PATH=`pwd`
fi

#Configure applications to install
APP_LOCATIONS=( ../ui/CustomerCare/plugins/CrmPlugin ../ui/CustomerCare/CrmWebServer )
APP_NAMES=( custcare custcare )

#Compile section
for index in "${!APP_LOCATIONS[@]}"
do
	cd ${APP_LOCATIONS[$index]}/install
	./make_struct.sh
	cd $CUR
done


#Create default
OS_INFO=`uname -a`

INSTALL_SCRIPT="deb/base_deb.sh"

if [[ "$OS_INFO" == *Ubuntu* ]]
then
	# UBUNTU INSTALL
	INSTALL_SCRIPT="deb/all_deb.sh"
else
	# SOLARIS INSTALL
	INSTALL_SCRIPT="pkg/all_pkg.sh"
fi

source $INSTALL_SCRIPT



################# ETC... #################
### WAREHOUSE ############################

#Create folder for warehouse
#read -p "Warehouse sftp user [$USER]: " WAREHOUSE_USER
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

echo "trying to save: $INSTALL_FILE at $LAST_PATH"

warehouse_file $LAST_PATH $INSTALL_FILE

