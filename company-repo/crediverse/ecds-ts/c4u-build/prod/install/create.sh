#!/bin/bash

echo "Starting Installation Creation Process"
##################### REQUIRED PARAMETERS ####################
# These parameters are passed in from Jenkins and are as follows
# SVN_REVISION = <int value representing latest svn revision number>
# ANT_HOME = <path to root of ANT, therefore $ANT_HOME\bin\ant runs ANT>
# JAVA_HOME = <path to root of JAVA installation, therefore $JAVA_HOME\bin\java runs JAVA>
# For Jenkins specific folders: http://testlab-intel-11:8080/jenkins/env-vars.html/
#
# ---------- EXAMPLES ---------------------------------------
# To build on Linux (From install folder), run with:
# ./create.sh prod/connectors/AirConnector
# ANT_HOME=/usr/share/ant
# JAVA_HOME=/usr/lib/jvm/java-7-oracle

##################### GLOBALS #####################

BASH_SCRIPT=$0

# Passed parameters
C4U_PLUGINS=""
CUSTOM_PREFIX=""
CUSTOMER_BRANDING=""
DEBUGGING_SYMBOLS=""
RELEASE_TO_STAGING=""

echo "[loading package specific variables]"

PACKAGE_NAME="c4u-main"
VERSION="1.0"
APPLICATION_NAME="c4u"
APPLICATION_PATH="/opt/cs/c4u/${VERSION}"

WAREHOUSE_USER="c4uteam"
WAREHOUSE_URL="warehouse.concurrent.co.za"

WAREHOUSE_DIR="/var/opt/webdav/warehouse.concurrent.co.za/"
WAREHOUSE_RELEASE_PATH="products/c4u/core/"
WAREHOUSE_VERSION_PATH="${VERSION}/"
WAREHOUSE_PACKAGES_PATH=""

##################### UTILITY METHODS #####################
function printHelpExit
{
  echo "Usage: $BASH_SCRIPT [OPTIONS]"
  echo "The following [OPTIONS] are available:"
  echo "-l --plugins  Plugins list (Required)"
  echo "-p --prefix   Build Prefix (Required)"
  echo "-c --branding Customer Branding details"
  echo "-d --debug    Build with debuggin symbols"
  
  echo "-s --staging  TS Release to warehouse staging area"

  echo "Detailed Usage: $0 -l C4U_PLUGINS -p CUSTOM_PREFIX [CUSTOMER_BRANDING]"
  echo "Example: $0 -l prod/connectors/AirConnector -p example -c ecozim"
  echo "Note:   For branding logos are stored in CUSTOMER_BRANDING/ui/CustomerCare/CrmWebServer/webapp/web/img"
  exit 1
}

function contains
{
    local n=$#
    local value=${!n}
    for ((i=1;i < $#;i++)) {
        if [ "${!i}" == "${value}" ]; then
            echo "y"
            return 0
        fi
    }
    echo "n"
    return 1
}

##################### LOADING PARAMETERS #####################

echo "[loading passed arguments]"

# Command line arguments
# -p Build Prefix
# -l Plugins list
# -d Use debugging symbols
# -c Customer branding
# -h Help

# If there are no parameters then display a usage message
if [[ $# -eq 0 ]] ; then
  printHelpExit
fi

# Pass shell arguments
while [[ $# -gt 0 ]]
do
  keyParam="$1"
  valueParam="$2"
  case $keyParam in
    -h|--help)
    printHelpExit
    ;;
    -p|--prefix)
    CUSTOM_PREFIX=$valueParam
    shift
    ;;
    -l|--plugins)
    C4U_PLUGINS=$valueParam
    shift
    ;;
    -d|--debug)
    DEBUGGING_SYMBOLS="true"
    ;;
    -c|--branding)
    CUSTOMER_BRANDING=$valueParam
    shift
    ;;
    -s|--staging)
    RELEASE_TO_STAGING="true"
    ;;    
    *)
    echo "Unknown argument $keyParam"
    printHelpExit
    ;;
  esac
  shift
done

# Check required parameters
if [[ -z "$C4U_PLUGINS" || -z "$CUSTOM_PREFIX" ]]; then
  echo "Arguments missing"
  printHelpExit
fi

echo "[loading parameters]"

################ EXTRACT PLUGIN NAMES INTO ARRAY ################

IFS=',' read -ra ARR <<< "$C4U_PLUGINS"
for folder in "${ARR[@]}"; do
    PLUGIN_ARR[$i]=` echo $folder | awk -F/ '{print $NF}'`
    ((i++))
done

################### LOADING ENV VARIABLES ###################

echo "[loading variables]"

CUR=`pwd`
PROD=$(cd ../; pwd)
INSTALL=$PROD/install
TMP=$INSTALL/tmp
COMMON=$INSTALL/common

COREAPPS=(HostProcess GuiServer HxC Supervisor)
CORE_PLUGINS_APP=hostprocess
CORE_MANAGER=supervisor
CORE_DIR=prod
VAR_SHARE='/var/opt/cs/c4u/share'



MAX_LENGTH=100

echo "[loading environment variables]"

ANT=ant
if [ ! -z $ANT_HOME ]; then
	ANT=$ANT_HOME/bin/ant
fi

echo "ANT=${ANT}"
${ANT} -version

JAVA=java
if [ ! -z $JAVA_HOME ]; then
	JAVA=$JAVA_HOME/bin/java
fi

echo "JAVA=${JAVA}"
${JAVA} -version

REVISION="0.0"
if [ ! -z $SVN_REVISION ]; then

	sed "s/\[REVISION\]/$SVN_REVISION/g" \
	    $WORKSPACE/prod/core/ServiceBus/java/hxc/servicebus/Version.java > $WORKSPACE/prod/core/ServiceBus/java/hxc/servicebus/Version.java.tmp \
   	    && mv $WORKSPACE/prod/core/ServiceBus/java/hxc/servicebus/Version.java.tmp $WORKSPACE/prod/core/ServiceBus/java/hxc/servicebus/Version.java

	REVISION=$SVN_REVISION

fi

if [ ! -z $CUSTOM_PREFIX ]; then
	PACKAGE_NAME=$CUSTOM_PREFIX
fi

################### LOAD FUNCTIONS ###################

echo "[loading functions]"

function create_from_template {
        sed -e "s@\${APPLICATION_NAME}@$APPLICATION_NAME@g" \
            -e "s@\${NAME}@$CORE_MANAGER@g" \
            -e "s@\${PATH}@$APPLICATION_PATH@g" \
            -e "s@\${VERSION}@$VERSION.$REVISION@g" \
	    -e "s@\${BARE_VERSION}@$VERSION@g" \
	    -e "s@\${REVISION}@$REVISION@g" \
	    -e "s@\${PACKAGE_NAME}@$PACKAGE_NAME@g" \
	    -e "s@\${BUILD_PATH}@$TMP@g" \
        $1 > $2
}

############################# SETUP ############################

echo "[setting up environment]"

[ -z "${noclean}" ] && {
	if [ -d $TMP ]; then
		echo "(cd \"${PWD}\" && rm -rf $TMP)"
		rm -rf $TMP
	fi
	if [ -d release ]; then
		echo "(cd \"${PWD}\" && rm -rf release)"
		rm -rf release
	fi
}
mkdir $TMP

##################### MOVE DIRECTORY #####################

echo "[moving any non-$CORE_DIR items to $CORE_DIR]"

IFS=',' read -ra DIRS <<< "$C4U_PLUGINS"
for i in "${DIRS[@]}"; do

        ROOT_DIR=`echo $i | cut -d '/' -f 1`

	if [ "$ROOT_DIR" != "$CORE_DIR" ]; then

		echo "[copying $ROOT_DIR over to $CORE_DIR]"
		echo "(cd \"${PWD}\" && cp -R $PROD/../$ROOT_DIR/* $PROD/)"
		cp -R $PROD/../$ROOT_DIR/* $PROD/ &> /dev/null
# Following line would remove ECDS folder in building
#		rm -rf $PROD/../$ROOT_DIR

	fi

done

##################### BUILD FILE CREATIONS #####################

echo "[creating build XML files for all connectors and services]"

cd $PROD/tools/AntFileCreator/; pwd
if ! $ANT ; then
	echo "Build Failure $PROD/tools/AntFileCreator did not build successfully"
	exit 1
fi

# $ANT #&> /dev/null

TEXT="[running ant file creator to create build files]"
printf "$TEXT"
length=$MAX_LENGTH-${#TEXT}

for (( i=0; i < $length; i++))
do
	printf ' '
done

if [[ -n $DEBUGGING_SYMBOLS ]]; then
  # Debugging
  echo "Building ANT files with debug symbols [....]"
  ## XXX TODO FIXME ... yes ... this breaks the pathetic formatting ... who cares ... this is garbage software anyway ...
  echo "(cd \"${PWD}\" && $JAVA -jar dist/*.jar --debug)"
  $JAVA -jar dist/*.jar --debug
else
  # Not Debugging
  echo "Building ANT files (normal) [....]"
  ## XXX TODO FIXME ... yes ... this breaks the pathetic formatting ... who cares ... this is garbage software anyway ...
  echo "(cd \"${PWD}\" && $JAVA -jar dist/*.jar)"
  $JAVA -jar dist/*.jar
fi

echo "[DONE creating build XML files for all connectors and services]"

cd - &> /dev/null

##################### BUILD CORE JARS #####################

echo "===[building core applications]==="
BUILD_ERROR=false

for CORE in ${COREAPPS[@]}; do	
	while read line;
	do
		# Change to directory
		cd $line

		if [ ! -d install ]; then
				echo "Skipping as ${PWD}/install is not a directory ..."
				continue;
		fi

		# Load variables
		NAME=`export "LC_ALL=C"; echo $CORE | tr '[:upper:]' '[:lower:]'`
		echo "==== (COREAPP) name is : $NAME ===="

		if [ -f install/create.sh ]; then
			echo "Using ${PWD}/install/create.sh instead of standard create logic ..."
			. ./install/create.sh
		else
			TEXT="[creating core $NAME application (version: $VERSION)] "
			length=$MAX_LENGTH-${#TEXT}
			for (( i=0; i < $length; i++))
			do
				TEXT="${TEXT} "
			done

			if [ -d dist ]; then
				rm -rf dist
			fi

			echo "(cd \"${PWD}\" && ${ANT})"
			OUTPUT=$( "$ANT" 2>&1 ) || {
				echo "${TEXT}[FAILED to create 'dist' folder] Error:"
				echo "$OUTPUT"
				exit 1
			}

			if [ ! -d "./dist" ]; then
				echo "${TEXT}[FAILED to create 'dist' folder] Error:"
				echo "... aborting build"
				exit 1
			else
				mkdir dist/lib
				mv dist/*.jar dist/lib/
				mkdir dist/bin
				echo "TEMPLATE: ${PWD}/install/run.template -> ${PWD}/dist/bin/$NAME"
				echo "TEMPLATE: run.template: Replacing \${PATH} with $APPLICATION_PATH"
				sed -e "s@\${PATH}@$APPLICATION_PATH@g" install/run.template > dist/bin/$NAME
				echo "TEMPLATE: run.template: setting executable bit in ${PWD}/dist/bin/$NAME"
				chmod +x dist/bin/$NAME

				echo "${TEXT}[DONE]"
			fi

		fi

		# Move product to tmp directory
		echo "(cd \"${PWD}\" && rm -r $TMP/$NAME)"
		rm -r $TMP/$NAME
		echo "(cd \"${PWD}\" && mv ./dist $TMP/$NAME)"
		mv ./dist $TMP/$NAME
	done < <( find $PROD -name "$CORE" | egrep '.*core.*|.*ui.*' )
done

################### CREATE CUSTOMER-CARE #################

# Customer Care GUI
if [[ -d "../ui/CustomerCare/CrmWebServer/install" ]]; then
	cd ../ui/CustomerCare/CrmWebServer/install
	./make_struct.sh
	mv ../dist $TMP/custcare
	cd $CUR
fi

# Customer Care Plugin
if [[ -d "../ui/CustomerCare/plugins/CrmPlugin/install" ]]; then
	cd ../ui/CustomerCare/plugins/CrmPlugin/install
	./make_struct.sh
	mv ../dist/plugins $TMP/custcare
	cd $CUR
fi

cd $CUR

# Custom Care Customer Branding
if [[ -n $CUSTOMER_BRANDING ]]; then
	cp ../../$CUSTOMER_BRANDING/ui/CustomerCare/CrmWebServer/webapp/web/img/* $TMP/custcare/webapp/web/img/
fi

##################### CREATE PLUGINS #####################
if [ -d $TMP/$CORE_PLUGINS_APP ]; then

	echo "[creating \"plugins\" for $CORE_PLUGINS_APP]"

	if [ ! -d "$TMP/$CORE_PLUGINS_APP/plugins" ]; then
		mkdir $TMP/$CORE_PLUGINS_APP/plugins
	fi

	while read line;
	do
		key=`dirname $line | awk -F/ '{print $NF}'`

		if [ $(contains "${PLUGIN_ARR[@]}" "$key") == "n" ]; then
			continue;
		fi

		DIR=`dirname $line`
		TEXT="[creating \"plugin\" $key jar]"
		length=$MAX_LENGTH-${#TEXT}
		for (( i=0; i <= $length - 1; i++))
		do
			TEXT="${TEXT} "
		done

		cd $DIR
		if [ -f "./install/create.sh" ]; then
			echo "(cd \"${PWD}\" && source ./install/create.sh)"
			. ./install/create.sh
		fi

		echo "(cd \"${PWD}\" && ${ANT})"
		OUTPUT=$( "$ANT" 2>&1 ) || {
			echo "${TEXT}[FAILED to create 'dist' folder] Error:"
			echo "$OUTPUT"
			exit 1
		}

		if [ ! -d "./dist" ]; then
			echo "${TEXT}[FAILED to create distribution] Error:"
			echo "... aborting build"
			exit 1
		else
			echo "${TEXT}[DONE]"
			echo "(cd \"${PWD}\" && cp dist/*.jar $TMP/$CORE_PLUGINS_APP/plugins)"
			cp dist/*.jar $TMP/$CORE_PLUGINS_APP/plugins
		fi

		cd - &> /dev/null
	done < <( find $PROD -name "build.xml" | egrep '.*services.*|.*connectors.*' )

fi

##################### COPYING REPORTS #####################

mkdir -p "${TMP}${VAR_SHARE}" || echo "could not make folder ${TMP}${VAR_SHARE}"

if [ -d $TMP/$CORE_PLUGINS_APP ]; then

	echo "[copying reports for $CORE_PLUGINS_APP]"

	if [ ! -d "$TMP/$CORE_PLUGINS_APP/reports" ]; then
		mkdir $TMP/$CORE_PLUGINS_APP/reports
	fi

	find $PROD -name "build.xml" | egrep '.*services.*|.*connectors.*' | while read line; do
		key=`echo $line | awk '{ for(i=length;i!=0;i--)x=x substr($0,i,1);}END{print x}'  | cut -d '/' -f 2 | awk '{ for(i=length;i!=0;i--)x=x substr($0,i,1);}END{print x}'`
		if [[ ! -z $C4U_PLUGINS && $C4U_PLUGINS != *$key* ]]
		then
			continue;
		fi
		DIR=`dirname $line`
		cd $DIR
		if [ -d "./reports" ]; then
			TEXT="[copying report(s) from $key]"
			length=$MAX_LENGTH-${#TEXT}
			for (( i=0; i < $length; i++))
			do
				TEXT="${TEXT} "
			done
			echo "${TEXT}[DONE]"
			cp reports/*.jasper $TMP/$CORE_PLUGINS_APP/reports
		fi
		
		# Copy SQL scripts if available
		if [ -d "./pkgextra/share-config" ]; then
			echo "[copying share-config from ${key}]"
			cp -r pkgextra/share-config/* "${TMP}${VAR_SHARE}" || echo "Copying ${DIR}/pkgextra/share-config/ to ${TMP}${VAR_SHARE} Failed"
		fi
		
		cd - &> /dev/null
	done

fi

################ BUILD WAREHOUSE LOCATION ################

if [ "${RELEASE_TO_STAGING}" = "true" ] ; then
  WAREHOUSE_RELEASE_PATH="releases/${PACKAGE_NAME}/staging/"
  WAREHOUSE_VERSION_PATH="${VERSION}.${REVISION}/"
  WAREHOUSE_PACKAGES_PATH="packages/"

  #Application for released version 
  APPLICATION_NAME="${PACKAGE_NAME}"
fi

################### OS Specific Installer ###################

: ${OS:=$(uname)}

echo "O/S Found ... ${OS}"

case $OS in

	SunOS)
		echo "[building SunOS install]"
		. ./solaris/create.sh
		;;

	Linux)

		: ${OS_TYPE:=$(awk '{print $1}' /etc/*-release | head -n1)}
		#OS_TYPE=`grep "^NAME" /etc/*-release | awk -F'"' '{ print $2 }' | awk '{print $1}'`
		echo "Linux O/S found ... $OS_TYPE"

		case $OS_TYPE in
			Ubuntu)
				echo "[building Ubuntu install]"
				. ./debian/create.sh
			;;
			CentOS)
				echo "[building CentOS install]"
				. ./redhat/create.sh 
			;;
		esac
		;;

esac

: ${DISTRIBUTE:=true}
################### DISTRIBUTE RELEASE ###################

if "${DISTRIBUTE}"
then
	if [ ! -d release ]; then

		echo "[failed to create release package]"
		exit 1

	fi

	echo "[distributing release package to warehouse]"

	cd release

	WAREHOUSE_PATH="${WAREHOUSE_RELEASE_PATH}${WAREHOUSE_VERSION_PATH}${WAREHOUSE_PACKAGES_PATH}"
	echo "[making PATH: ${WAREHOUSE_PATH}]"

	ssh ${WAREHOUSE_USER}@${WAREHOUSE_URL} /bin/bash << EOF
	  cd "${WAREHOUSE_DIR}"
	  mkdir -p "${WAREHOUSE_RELEASE_PATH}${WAREHOUSE_VERSION_PATH}${WAREHOUSE_PACKAGES_PATH}"
EOF


	echo "[uploading ${RELEASE_FILE} to warehouse.concurrent.co.za]"
	sftp ${WAREHOUSE_USER}@${WAREHOUSE_URL} << EOF &> /dev/null
		cd "${WAREHOUSE_DIR}/${WAREHOUSE_PATH}"
		put "${RELEASE_FILE}"
		bye
EOF
else
	echo "Not distributing as DISTRIBUTE=${DISTRIBUTE}"
fi

echo "Completed Installation Creation Process"
