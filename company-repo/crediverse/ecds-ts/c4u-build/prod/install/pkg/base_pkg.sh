#!/bin/bash 

echo "PKG CALLED..."

APPS_DIR=opt/cs/c4u/$VERSION
INSTALL_DIR=$APPS_DIR/$APP_NAME

PKGINFO_TEMPLATE=pkginfo.template
MANIFEST_TEMPLATE=manifest.template
INIT_TEMPLATE=init.template
RUN_TEMPLATE=run.template

#Variable Declaration
TEMP_FOLDER=tmp
TEMPLATE_FOLDER=../../../../install/pkg/templates
DIST_FOLDER=../../dist

PREINSTALL_TEMPLATE=preinstall.template
PKGINFO_TEMPLATE=pkginfo.template
POSTINSTALL_TEMPLATE=postinstall.template

PKG_ARCH=all
PKG_FILE=$APP_NAME-$VERSION-all.pkg
MANIFEST_FILE=$APP_NAME.xml
INIT_FILE=init.sh

#Utility functions
#All parameters in template file have form ${BASH_VARIABLE}
function render_template {
  eval "echo \"$(cat $1)\""
}

function create_from_template {
	sed -e "s@\${APP_NAME}@$APP_NAME@g" \
            -e "s@\${BASEDIR}@$BASEDIR@g" \
            -e "s@\${INSTALL_DIR}@$INSTALL_DIR@g" \
            -e "s@\${VERSION}@$VERSION@g" \
        $1 > $2
}




#Setup folders
rm -rf tmp
mkdir $TEMP_FOLDER
#mkdir -p ./$TEMP_FOLDER/libexec
#mkdir -p ./$TEMP_FOLDER/run

#Copy files to install
cp -R $DIST_FOLDER/* $TEMP_FOLDER

#Create service files
#manifest
#if [ -f $TEMPLATE_FOLDER/$MANIFEST_TEMPLATE ]; 
#then
#	render_template $TEMPLATE_FOLDER/$MANIFEST_TEMPLATE > ./$TEMP_FOLDER/libexec/$MANIFEST_FILE
#	chmod 755 ./$TEMP_FOLDER/libexec/$MANIFEST_FILE
#else
#	echo "missing $MANIFEST_TEMPLATE"
#	exit 1
#fi

#create init file (called when start / stop / restart service)
#if [ "$RUN_SERVICE" -eq "1" ]
#then
#
#	if [ -f $TEMPLATE_FOLDER/$INIT_TEMPLATE ]; 
#	then
#		sed -e "s@\${APPS_DIR}@$APPS_DIR@g" \
#			-e "s@\${APP_NAME}@$APP_NAME@g" \
#			-e "s@\${JAVA_CLASSPATH}@$JAVA_CLASSPATH@g" \
#			-e "s@\${JAVA_MAINCLASS_JAR}@$JAVA_MAINCLASS_JAR@g" $TEMPLATE_FOLDER/$INIT_TEMPLATE > ./$TEMP_FOLDER/libexec/$INIT_FILE
#		chmod 755 ./$TEMP_FOLDER/libexec/$INIT_FILE
#	else
#		echo "missing $TEMPLATE_FOLDER/$INIT_TEMPLATE"
#		exit 1
#	fi
#fi

#create run script
#if [ -f $TEMPLATE_FOLDER/$RUN_TEMPLATE ]; 
#then
#	sed -e "s@\${APP_NAME}@$APP_NAME@g" \
#		-e "s@\${JAVA_CLASSPATH}@$JAVA_CLASSPATH@g" \
#               -e "s@\${JAVA_MAINCLASS_JAR}@$JAVA_MAINCLASS_JAR@g" $TEMPLATE_FOLDER/$RUN_TEMPLATE > ./$TEMP_FOLDER/libexec/$APP_NAME.sh
#	chmod 755 ./$TEMP_FOLDER/libexec/$APP_NAME.sh
#else
#	echo "missing $TEMPLATE_FOLDER/$INIT_TEMPLATE"
#	exit 1
#fi

#Create prototype file
cd $TEMP_FOLDER
echo "i pkginfo=./pkginfo
i preinstall=./preinstall
i postinstall=./postinstall" > prototype
find . -print | grep -v '\.svn' | grep -v '\prototype' | pkgproto >> prototype
cd -


#Create preinstall file
if [ -f $TEMPLATE_FOLDER/$PREINSTALL_TEMPLATE ]; 
then
	create_from_template $TEMPLATE_FOLDER/$PREINSTALL_TEMPLATE ./$TEMP_FOLDER/preinstall
#	sed -e "s@\${APP_NAME}@$APP_NAME@g" $TEMPLATE_FOLDER/$PREINSTALL_TEMPLATE > ./$TEMP_FOLDER/preinstall
else
	echo "missing $TEMPLATE_FOLDER/$PREINSTALL_TEMPLATE"
	exit 1
fi

#Create pkginfo
if [ -f $TEMPLATE_FOLDER/$PKGINFO_TEMPLATE ]; 
then
	BASEDIR=/$INSTALL_DIR
	create_from_template $TEMPLATE_FOLDER/$PKGINFO_TEMPLATE ./$TEMP_FOLDER/pkginfo
#	render_template $TEMPLATE_FOLDER/$PKGINFO_TEMPLATE > ./$TEMP_FOLDER/pkginfo
else
	echo "missing $PKGINFO_FILE"
	exit 1
fi

#create postinstall
if [ -f $TEMPLATE_FOLDER/$POSTINSTALL_TEMPLATE ]; 
then
	create_from_template $TEMPLATE_FOLDER/$POSTINSTALL_TEMPLATE ./$TEMP_FOLDER/postinstall
#	sed -e "s@\${INSTALL_DIR}@$INSTALL_DIR@g" \
#		-e "s@\${APP_NAME}@$APP_NAME@g" $TEMPLATE_FOLDER/$POSTINSTALL_TEMPLATE > ./$TEMP_FOLDER/postinstall
else
	echo "missing $TEMPLATE_FOLDER/$POSTINSTALL_TEMPLATE"
	exit 1
fi

#now build package
cd $TEMP_FOLDER
mkdir tmp
pkgmk -r . -o -d tmp -a all

touch  $PKG_FILE
pkgtrans -s `pwd`/tmp $PKG_FILE $APP_NAME
sleep 3
rm ../../*.pkg
cp *.pkg ../../

#Clean up afterwards
cd -
#rm -rf tmp

