#!/bin/bash

APPS_DIR=opt/cs/c4u/$VERSION/
INSTALL_PATH=$APPS_DIR/$APP_NAME
DEB_FILE=$APP_NAME-$VERSION-all.deb

function create_from_template {
	sed -e "s@\${APP_NAME}@$APP_NAME@g" \
	    -e "s@\${CLASSPATH}@$CLASSPATH@g" \
	    -e "s@\${MAIN_CLASS}@$MAIN_CLASS@g" \
	    -e "s@\${VERSION}@$VERSION@g" \
	$1 > $2 
}

function clean_up {
	sudo rm -rf $TEMP_FOLDER
}

#Variable Declaration

#######CHANGE#########

# APP_NAME=hostprocess
# MAIN_CLASS="hxc.test.TestHost"
# SERVICE=true

#######DON'T CHANGE########

#JARFILE=`find ../../dist -maxdepth 1 -name *.jar`
#JARFILE=${JARFILE:11}
APP_PATH=debian/$INSTALL_PATH
#USR_PATH=debian/usr/bin
#INIT_PATH=$APP_PATH/libexec
#RUN_PATH=$APP_PATH/run
#COPYRIGHT_PATH=debian/usr/share/doc/$APP_NAME
DOC_PATH=$INSTALL_PATH/doc
CONFIG_PATH=debian/DEBIAN
TEMP_FOLDER=tmp

clean_up

if [ "$APP_NAME" = "cli" ]; then
	SCRIPT=hxc
else
	SCRIPT=$APP_NAME
fi
INSTALL_FILES=../../../../install/deb
TEMPLATE_FOLDER=$INSTALL_FILES/templates
#if [ -z "$JARFILE" ]; then
#	CLASSPATH="/$INSTALL_PATH:/$INSTALL_PATH/lib/*"
#else
#	CLASSPATH="/$INSTALL_PATH/$JARFILE"
#fi

#Directory Creating
#mkdir -p $TEMP_FOLDER/$USR_PATH
mkdir -p $TEMP_FOLDER/$APP_PATH
mkdir -p $TEMP_FOLDER/$INIT_PATH
#mkdir -p $TEMP_FOLDER/$RUN_PATH
mkdir -p $TEMP_FOLDER/$CONFIG_PATH


#Check for service
if $SERVICE ; then
	mkdir -p $TEMP_FOLDER/debian/etc/init.d
fi

find $TEMP_FOLDER/debian -type d | xargs chmod 755

#Copy Files
cp -r ../../dist/* $TEMP_FOLDER/$APP_PATH

mkdir -p $TEMP_FOLDER/debian/$DOC_PATH

#Create Script

#if [ ! -f $TEMPLATE_FOLDER/script.template ]; then
#	echo "Need $TEMPLATE_FOLDER/script.template to proceed."
#	clean_up
#	exit 0
#else
#	create_from_template $TEMPLATE_FOLDER/script.template $TEMP_FOLDER/$USR_PATH/$SCRIPT
#fi
#chmod +x $TEMP_FOLDER/$USR_PATH/$SCRIPT

#Create Service

#if $SERVICE ; then
#	if [ ! -f $TEMPLATE_FOLDER/init.template ]; then
#		echo "Need $TEMPLATE_FOLDER/init.template to proceed."
#		clean_up
#		exit 0
#	else
#		create_from_template $TEMPLATE_FOLDER/init.template $TEMP_FOLDER/$INIT_PATH/init.sh
#	fi
#	chmod +x $TEMP_FOLDER/$INIT_PATH/init.sh
#fi

#Create Control

if [ ! -f $TEMPLATE_FOLDER/control.template ]; then
	echo "Need $TEMPLATE_FOLDER/control.template to proceed."
	clean_up
	exit 0
else
	create_from_template $TEMPLATE_FOLDER/control.template $TEMP_FOLDER/$CONFIG_PATH/control
fi

#Create Copyright
if [ ! -f $TEMPLATE_FOLDER/copyright.template ]; then
	echo "Need $TEMPLATE_FOLDER/copyright.template to proceed."
	clean_up
	exit 0
else
	create_from_template $TEMPLATE_FOLDER/copyright.template $TEMP_FOLDER/debian/$DOC_PATH/copyright
fi

#Create Postinst Script

if [ ! -f $TEMPLATE_FOLDER/postinst.template ]; then
	echo "Need $TEMPLATE_FOLDER/postinst.template to proceed."
	clean_up
	exit 0
else
	create_from_template $TEMPLATE_FOLDER/postinst.template $TEMP_FOLDER/$CONFIG_PATH/postinst
fi

#Create Postrm Script

if [ ! -f $TEMPLATE_FOLDER/postrm.template ]; then
	echo "Need $TEMPLATE_FOLDER/postrm.template to proceed."
	clean_up
	exit 0
else
	create_from_template $TEMPLATE_FOLDER/postrm.template $TEMP_FOLDER/$CONFIG_PATH/postrm
fi

#Create Preinst Script

if [ ! -f $TEMPLATE_FOLDER/preinst.template ]; then
	echo "Need $TEMPLATE_FOLDER/preinst.template to proceed."
	clean_up
	exit 0
else
	create_from_template $TEMPLATE_FOLDER/preinst.template $TEMP_FOLDER/$CONFIG_PATH/preinst
fi

#Create Prerm Script

if [ ! -f $TEMPLATE_FOLDER/prerm.template ]; then
	echo "Need $TEMPLATE_FOLDER/prerm.template to proceed."
	clean_up
	exit 0
else
	create_from_template $TEMPLATE_FOLDER/prerm.template $TEMP_FOLDER/$CONFIG_PATH/prerm
fi


#Configure Everything

chmod 0755 -R $TEMP_FOLDER/debian/DEBIAN
#chmod 0755 -R $TEMP_FOLDER/$CONFIG_PATH
#chmod 0644 $TEMP_FOLDER/$APP_PATH/$JARFILE
#chmod 0644 -R $TEMP_FOLDER/$DOC_PATH

#Create Debian Package
sudo rm ../*.deb
sudo dpkg-deb --build $TEMP_FOLDER/debian
sudo mv $TEMP_FOLDER/debian.deb ../$DEB_FILE
#sudo rm -r $TEMP_FOLDER

echo "Installation created: " $DEB_FILE
