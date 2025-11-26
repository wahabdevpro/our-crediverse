#!/bin/bash 

APPS_DIR=/opt/cs/components/app

APP_NAME=guiserver
INSTALL_DIR=$APPS_DIR/$APP_NAME
APP_VERSION=1.0.0.1
RUN_SERVICE=1
JAVA_MAINCLASS_JAR="hxc.userinterfaces.gui.jetty.JettyMain"
JAVA_CLASSPATH="$INSTALL_DIR:$INSTALL_DIR/lib/*"
PREINSTALL_TEMPLATE=preinstall.service.template
POSTINSTALL_TEMPLATE=postinstall.service.template

#use 'source' to export variables
. ../../../../install/pkg/base_pkg.sh 

