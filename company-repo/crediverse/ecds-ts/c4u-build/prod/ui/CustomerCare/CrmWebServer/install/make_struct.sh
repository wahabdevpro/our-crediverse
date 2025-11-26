#!/bin/bash

APP_NAME=custcare
VERSION=1.0
CUSTOMER=core
MAIN_CLASS="hxc.userinterfaces.gui.jetty.JettyMain"
NO_BUILD="yes"

###### BUILD AND CREAT STRUCTURE
DIST_FOLDER=dist

#Clean Up
cd ..
mkdir -p $DIST_FOLDER
./build.sh

#Create Runnable structure
echo "creating dist structure..."
mkdir -p $DIST_FOLDER/bin
cp -R lib $DIST_FOLDER/
cp -R webapp $DIST_FOLDER/
#cp -R soap $DIST_FOLDER/
cp -R var $DIST_FOLDER/
cp -R etc $DIST_FOLDER/
cd -

. ../../../../install/common/common_make_struct.sh
