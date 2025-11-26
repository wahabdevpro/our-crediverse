#!/bin/bash

DIST_FOLDER=dist

#Clean Up
echo "cleaning up..."
rm -rf $DIST_FOLDER
mkdir $DIST_FOLDER

#Build Class files
ant clean
ant

#Create Runnable structure
echo "creating dist structure..."
cp -R classes/hxc $DIST_FOLDER
cp -R lib $DIST_FOLDER
cp -R etc $DIST_FOLDER
cp -R share $DIST_FOLDER
mkdir -p app/var/log
