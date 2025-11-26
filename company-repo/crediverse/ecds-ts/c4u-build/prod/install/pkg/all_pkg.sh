#!/bin/bash

#VERSION=0.0.1
CUSTOMER=core

APPS_DIR=opt/cs/c4u/$VERSION


#Utility functions
function create_from_template {
	sed -e "s@\${APP_NAME}@$APP_NAME@g" \
	    -e "s@\${APPS_DIR}@$APPS_DIR@g" \
	    -e "s@\${CLASSPATH}@$CLASSPATH@g" \
	    -e "s@\${MAIN_CLASS}@$MAIN_CLASS@g" \
	    -e "s@\${VERSION}@$VERSION.$SVN_REVISION@g" \
	    -e "s@\${BASEDIR}@$BASEDIR@g" \
	$1 > $2 
}

function clean_up {
	sudo rm -rf $TEMP_FOLDER
}

#Variable Declaration
APP_PATH=$INSTALL_PATH
DOC_PATH=$INSTALL_PATH/doc
TEMP_FOLDER=tmp
TEMPLATE_FOLDER=pkg/templates
DEFAULTS_FOLDER=pkg/defaults

PKGINFO_TEMPLATE=pkginfo.template
MANIFEST_TEMPLATE=manifest.template
INIT_TEMPLATE=init.template
RUN_TEMPLATE=run.templates
PREINSTALL_TEMPLATE=preinstall.template
PKGINFO_TEMPLATE=pkginfo.template
POSTINSTALL_TEMPLATE=postinstall.template
PREREMOVE_TEMPLATE=preremove.template
PKG_ARCH=all

MANIFEST_FILE=$APP_NAME.xml

clean_up
mkdir $TEMP_FOLDER

# get the structure correct
for index in "${!APP_LOCATIONS[@]}"
do
	APP_NAME=${APP_NAMES[$index]}
	INSTALL_PATH=$APPS_DIR/$APP_NAME
	APP_PATH=$INSTALL_PATH	

	#Directory Creating
	mkdir -p $TEMP_FOLDER/$APP_NAME
	#Copy Files
	cp -r ${APP_LOCATIONS[$index]}/dist/* $TEMP_FOLDER/$APP_NAME

	if [ "$APP_NAME" == "hostprocess" ]; then
		if [ -d "./plugins" ]; then
			cp -r ./plugins $TEMP_FOLDER/$APP_NAME/	
		fi

		if [ -d "./reports" ]; then
			cp -r ./reports $TEMP_FOLDER/$APP_NAME/
		fi
	fi
done

# Defaults
#mkdir -p $TEMP_FOLDER/defaults
#cp -r $DEFAULTS_FOLDER/* $TEMP_FOLDER/defaults/

cd $TEMP_FOLDER
echo "i pkginfo=./pkginfo
i preinstall=./preinstall
i postinstall=./postinstall
i preremove=./preremove" > prototype
find . -print | grep -v '\.svn' | grep -v '\prototype' | pkgproto >> prototype

if [ "$APP_NAME" != "custcare" ]; then
	echo "d none /etc 0755 root root
	d none /etc/init.d 0755 root root
	d none /etc/rc3.d 0755 root root
	f none /etc/init.d/supervisor=supervisor/service/supervisor 0755 root bin" >> prototype
	echo "f none /etc/rc3.d/S01$APP_NAME=supervisor/service/boot/S01$APP_NAME 0755 root bin
	f none /etc/rc3.d/K01$APP_NAME=supervisor/service/boot/K01$APP_NAME 0755 root bin" >> prototype
else
	echo "d none /etc 0755 root root
        d none /etc/init.d 0755 root root
        f none /etc/init.d/custcare=custcare/service/custcare 0755 root bin" >> prototype
fi

cd -

#Create preinstall file
if [ -f $TEMPLATE_FOLDER/$PREINSTALL_TEMPLATE ]; 
then
	create_from_template $TEMPLATE_FOLDER/$PREINSTALL_TEMPLATE ./$TEMP_FOLDER/preinstall
else
	echo "missing $TEMPLATE_FOLDER/$PREINSTALL_TEMPLATE"
	exit 1
fi

#Create pkginfo
if [ "$APP_NAME" != "custcare" ]; then
	APP_NAME="c4u"
fi


if [ -f $TEMPLATE_FOLDER/$PKGINFO_TEMPLATE ]; 
then
	BASEDIR=/$APPS_DIR
	create_from_template $TEMPLATE_FOLDER/$PKGINFO_TEMPLATE ./$TEMP_FOLDER/pkginfo
else
	echo "missing $PKGINFO_FILE"
	exit 1
fi

INSTALL_FILE=$APP_NAME-$VERSION.$SVN_REVISION-all.pkg	

if [ "$APP_NAME" != "custcare" ]; then

        if [ ! -z $1 ]; then
		INSTALL_FILE=$1-$VERSION.$SVN_REVISION-all.pkg
        fi
fi


#create postinstall
if [ -f $TEMPLATE_FOLDER/$POSTINSTALL_TEMPLATE ]; 
then
	create_from_template $TEMPLATE_FOLDER/$POSTINSTALL_TEMPLATE ./$TEMP_FOLDER/postinstall
else
	echo "missing $TEMPLATE_FOLDER/$POSTINSTALL_TEMPLATE"
	exit 1
fi

#create preremove
if [ -f $TEMPLATE_FOLDER/$PREREMOVE_TEMPLATE ];
then
	create_from_template $TEMPLATE_FOLDER/$PREREMOVE_TEMPLATE ./$TEMP_FOLDER/preremove
else
	echo "missing $TEMPLATE_FOLDER/$PREREMOVE_TEMPLATE"
	exit 1
fi

#Clean up ?

#now build package
cd $TEMP_FOLDER
mkdir tmp
pkgmk -r . -o -d tmp -a all

touch  $INSTALL_FILE
pkgtrans -s `pwd`/tmp $INSTALL_FILE $APP_NAME
sleep 3

mv *.pkg $COMPILED_INSTALL_FILE_PATH/$INSTALL_FILE
cd -
#rm -rf tmp
