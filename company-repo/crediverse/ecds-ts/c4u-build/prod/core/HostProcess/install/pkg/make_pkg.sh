#!/bin/bash 

APPS_DIR=/opt/cs/components/app

APP_NAME=hostprocess
INSTALL_DIR=$APPS_DIR/$APP_NAME
APP_VERSION=1.0.0.1
RUN_SERVICE=1
JAVA_MAINCLASS_JAR="-jar $INSTALL_DIR/HostProcess.jar"
JAVA_CLASSPATH="$INSTALL_DIR"
PREINSTALL_TEMPLATE=preinstall.service.template
POSTINSTALL_TEMPLATE=postinstall.service.template

#use 'source' to export variables
. ../../../../install/pkg/base_pkg.sh 

