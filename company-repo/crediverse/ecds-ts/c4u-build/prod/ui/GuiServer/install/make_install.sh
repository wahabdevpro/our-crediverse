#!/bin/bash

APP_NAME=guiserver
VERSION=0.0.1
CUSTOMER=core
MAIN_CLASS="hxc.userinterfaces.gui.jetty.JettyMain"
NO_BUILD="yes"

###### BUILD AND CREAT STRUCTURE
DIST_FOLDER=dist

#Clean Up
cd ..
echo "cleaning up..."
rm -rf $DIST_FOLDER
mkdir $DIST_FOLDERS

#Build Class files
echo "building..."
ant clean
ant

#Create Runnable structure
echo "creating dist structure..."
mkdir -p $DIST_FOLDER/lib
cp -R classes/hxc $DIST_FOLDER/lib
cp -R lib $DIST_FOLDER
cp -R etc $DIST_FOLDER
cp -R share $DIST_FOLDER
mkdir -p $DIST_FOLDER/var/log
mkdir -p $DIST_FOLDER/bin
cd -

. ../../../install/common/common_install.sh
