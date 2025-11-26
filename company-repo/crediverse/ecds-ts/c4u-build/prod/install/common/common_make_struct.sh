#!/bin/bash +x

if [ -z "$NO_BUILD" ]; then
	echo "Auto Building..."
	######## CLEAN OUT
	rm -rf ../dist
	mkdir ../dist

	######## CALL BUILD
	cd ..
	ant
#	ant create_run_jar
	cd -

	######## CREATE STRUCTURE 
	cd ../dist
	mkdir bin
	mkdir lib
	mv *.jar lib
	cd -
else
	echo "Not building using existing structure"
fi


######## CREATE RUNNABLE (in bin)
if [ "$APP_NAME" == "custcare" ]; then
	INSTALL_DIR=../../../../install/common
else
	INSTALL_DIR=../../../install/common
fi
TEMPLATE_FOLDER=$INSTALL_DIR
INSTALL_PATH=opt/cs/c4u/$VERSION/$APP_NAME
RUN_TEMPLATE=run.template
INIT_TEMPLATE=init.template
BOOTUP_TEMPLATE=bootup.template
SHUTDOWN_TEMPLATE=shutdown.template
if [ "$APP_NAME" == "custcare" ]; then
	CLASSPATH="/$INSTALL_PATH/webapp/WEB-INF/classes:/$INSTALL_PATH/lib/thymeleaf-2.0.19.jar:/$INSTALL_PATH/lib/soapservices.jar:/$INSTALL_PATH/lib/slf4j-api-1.6.1.jar:/$INSTALL_PATH/lib/ognl-3.0.6.jar:/$INSTALL_PATH/lib/logback-core-1.0.13.jar:/$INSTALL_PATH/lib/logback-classic-1.0.13.jar:/$INSTALL_PATH/lib/jetty-xml-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/jetty-webapp-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/jetty-util-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/jetty-servlet-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/jetty-server-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/jetty-security-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/jetty-plus-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/jetty-jndi-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/jetty-io-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/jetty-http-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/jetty-annotations-9.1.3.v20140225.jar:/$INSTALL_PATH/lib/javax.servlet-api-3.1.0.jar:/$INSTALL_PATH/lib/javax.annotation-api-1.2.jar:/$INSTALL_PATH/lib/javassist-3.16.1-GA.jar:/$INSTALL_PATH/lib/javaee-api-6.0.jar:/$INSTALL_PATH/lib/gson-2.2.4.jar:/$INSTALL_PATH/lib/asm-tree-4.1.jar:/$INSTALL_PATH/lib/asm-commons-4.1.jar:/$INSTALL_PATH/lib/asm-4.1.jar"
	MAIN_CLASS="$MAIN_CLASS --plugins=/$INSTALL_PATH/plugins"
else
	CLASSPATH="/$INSTALL_PATH/lib:/$INSTALL_PATH/lib/*:/$INSTALL_PATH/lib/*.jar"
fi

if [ "$APP_NAME" == "supervisor" ]; then
	mkdir ../dist/service
	mkdir ../dist/service/boot
	sed -e "s@\${INSTALL_PATH}@$INSTALL_PATH@g" \
		-e "s@\${APP_NAME}@$APP_NAME@g" \
		-e "s@\${CLASSPATH}@$CLASSPATH@g"\
		-e "s@\${MAIN_CLASS}@$MAIN_CLASS@g" $TEMPLATE_FOLDER/$INIT_TEMPLATE > ../dist/service/$APP_NAME
	chmod 755 ../dist/service/$APP_NAME

	sed -e "s@\${APP_NAME}@$APP_NAME@g" $TEMPLATE_FOLDER/$BOOTUP_TEMPLATE > ../dist/service/boot/S01$APP_NAME
	chmod 755 ../dist/service/boot/S01$APP_NAME

	sed -e "s@\${APP_NAME}@$APP_NAME@g" $TEMPLATE_FOLDER/$SHUTDOWN_TEMPLATE > ../dist/service/boot/K01$APP_NAME
	chmod 755 ../dist/service/boot/K01$APP_NAME
fi

if [ "$APP_NAME" == "custcare" ]; then
	mkdir ../dist/service
	sed -e "s@\${INSTALL_PATH}@$INSTALL_PATH@g" \
                -e "s@\${APP_NAME}@$APP_NAME@g" \
                -e "s@\${CLASSPATH}@$CLASSPATH@g"\
                -e "s@\${MAIN_CLASS}@$MAIN_CLASS@g"\
		-e "s@\${CHANGE_DIR}@$INSTALL_PATH@g" $TEMPLATE_FOLDER/$INIT_TEMPLATE > ../dist/service/$APP_NAME
        chmod 755 ../dist/service/$APP_NAME
fi

if [ -z "$IS_PLUGIN" ]; then
	echo "TEMPLATE: ${PWD}/$TEMPLATE_FOLDER/$RUN_TEMPLATE -> ${PWD}/../dist/bin/$APP_NAME"
	echo "TEMPLATE: $RUN_TEMPLATE: Replacing \${APPS_DIR} with $APPS_DIR"
	echo "TEMPLATE: $RUN_TEMPLATE: Replacing \${CLASSPATH} with $CLASSPATH"
	echo "TEMPLATE: $RUN_TEMPLATE: Replacing \${MAIN_CLASS} with $MAIN_CLASS"
	sed -e "s@\${APPS_DIR}@$APPS_DIR@g" \
		-e "s@\${CLASSPATH}@$CLASSPATH@g" \
		-e "s@\${MAIN_CLASS}@$MAIN_CLASS@g" $TEMPLATE_FOLDER/$RUN_TEMPLATE > ../dist/bin/$APP_NAME
	echo "TEMPLATE: $RUN_TEMPLATE: chmod 755"
	chmod 755 ../dist/bin/$APP_NAME
fi

exit 1

echo "Before calling base install script PWD:"
echo `pwd`

######## DEPENDING ON PLATFORM CALL CORECT CREATE_INSTALL SCRIPT
OS_INFO=`uname -a`

INSTALL_EXT="deb"
INSTALL_SCRIPT="base_deb.sh"


if [[ "$OS_INFO" == *Ubuntu* ]]
then
	# UBUNTU INSTALL
	INSTALL_EXT="deb"
	INSTALL_SCRIPT="base_group_deb.sh"
else
	# SOLARIS INSTALL
	INSTALL_EXT="deb"
	INSTALL_SCRIPT="base_group_deb.sh"
fi


exit 0

#### CREATE INSTALL SCRIPT
#rm *.$INSTALL_EXT
rm -rf ./$INSTALL_EXT
mkdir $INSTALL_EXT
cd $INSTALL_EXT
source ../../../../install/$INSTALL_EXT/$INSTALL_SCRIPT
cd ..
rm -rf $INSTALL_EXT
FILE=`ls *.$INSTALL_EXT`

#Create folder for warehouse
read -p "Warehouse sftp user [$USER]: " WAREHOUSE_USER
WAREHOUSE_USER=${WAREHOUSE_USER:-$USER}

WAREHOUSE_PATH="$CUSTOMER|$VERSION"
CURDIR=""
ALLDIR=""
IFS="|"
set $WAREHOUSE_PATH
for item
do
        NEW_DIR=$CURDIR$item
        ALLDIR=$ALLDIR" "$NEW_DIR
        CURDIR=$CURDIR$item/
done

IFS=" "
LAST_PATH=""
for d in $ALLDIR ; do
	LAST_PATH=$d
        echo "making: $d"
        remote_mkdir $d
done

warehouse_file $LAST_PATH $FILE

