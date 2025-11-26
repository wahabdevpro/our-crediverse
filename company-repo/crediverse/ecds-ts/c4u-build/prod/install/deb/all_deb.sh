#!/bin/bash +x

#VERSION=0.0
#CUSTOMER=core

APPS_DIR=opt/cs/c4u/$VERSION/
APP_NAME="c4u"
INSTALL_FILE=$APP_NAME-$VERSION.$SVN_REVISION-all.deb

function create_from_template {
	sed -e "s@\${APP_NAME}@$INSTALL_PKG_NAME@g" \
	    -e "s@\${APP_NAME_INIT}@$APP_NAME@g"\
	    -e "s@\${APP_DIR}@$APPS_DIR@g" \
	    -e "s@\${CLASSPATH}@$CLASSPATH@g" \
	    -e "s@\${MAIN_CLASS}@$MAIN_CLASS@g" \
	    -e "s@\${VERSION}@$VERSION@g" \
	$1 > $2 
}

function clean_up {
	sudo rm -rf $TEMP_FOLDER
}

APP_PATH=debian/$INSTALL_PATH
DOC_PATH=$INSTALL_PATH/doc
CONFIG_PATH=debian/DEBIAN
TEMP_FOLDER=tmp
TEMPLATE_FOLDER=deb/templates

clean_up

mkdir -p $TEMP_FOLDER/$CONFIG_PATH
mkdir -p $TEMP_FOLDER/debian/$DOC_PATH
mkdir -p $TEMP_FOLDER/debian/etc/init.d
mkdir -p $TEMP_FOLDER/debian/etc/rc3.d

# get the structure correct
for index in "${!APP_LOCATIONS[@]}"
do
	APP_NAME=${APP_NAMES[$index]}
	INSTALL_PATH=$APPS_DIR/$APP_NAME
	APP_PATH=debian/$INSTALL_PATH	

	#Directory Creating
	mkdir -p $TEMP_FOLDER/$APP_PATH
	#Copy Files
	cp -r ${APP_LOCATIONS[$index]}/dist/* $TEMP_FOLDER/$APP_PATH
	
	if [ "$APP_NAME" == "supervisor" ]; then
		cp ${APP_LOCATIONS[$index]}/dist/service/$APP_NAME $TEMP_FOLDER/debian/etc/init.d/supervisor
		cp ${APP_LOCATIONS[$index]}/dist/service/boot/* $TEMP_FOLDER/debian/etc/rc3.d/
	fi
	#echo "$index ${APP_LOCATIONS[$index]} ${APP_NAMES[$index]}"
done




# Create Control
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

chmod 0755 -R $TEMP_FOLDER/debian/DEBIAN


#Create Debian Package
sudo rm *.deb
sudo dpkg-deb --build $TEMP_FOLDER/debian
sudo mv $TEMP_FOLDER/debian.deb $COMPILED_INSTALL_FILE_PATH/$INSTALL_FILE
#sudo rm -r $TEMP_FOLDER




