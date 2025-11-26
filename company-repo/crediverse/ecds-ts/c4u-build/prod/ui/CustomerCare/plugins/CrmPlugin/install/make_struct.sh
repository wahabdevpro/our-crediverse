#!/bin/bash

APP_NAME=custcare
VERSION=1.0
CUSTOMER=core
MAIN_CLASS="hxc.userinterfaces.gui.jetty.JettyMain"
NO_BUILD="yes"
IS_PLUGIN="yes"

###### BUILD AND CREAT STRUCTURE
DIST_FOLDER=dist/plugins

#Clean Up
cd ..
echo "cleaning up..."
rm -rf $DIST_FOLDER
mkdir -p $DIST_FOLDER

#Build Class files
echo "building..."
ant clean
ant

#Create Runnable structure
echo "creating dist structure..."
cp -R lib $DIST_FOLDER/
cp -R webapp $DIST_FOLDER/
cd -

#. ../../../../../install/common/common_make_struct.sh
