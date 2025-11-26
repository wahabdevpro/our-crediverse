#!/bin/bash

# Create tar file of directory

mkdir $INSTALL/$APPLICATION_NAME-$VERSION
mv $TMP/var $INSTALL/$APPLICATION_NAME-$VERSION
mv $TMP $INSTALL/$APPLICATION_NAME-$VERSION/opt

mkdir -p $INSTALL/$APPLICATION_NAME-$VERSION/etc/sysconfig
mkdir -p $INSTALL/$APPLICATION_NAME-$VERSION/usr/lib/systemd/system
#mkdir -p $INSTALL/$APPLICATION_NAME-$VERSION/var/opt/cs/c4u/share/

create_from_template redhat/templates/init.template $INSTALL/$APPLICATION_NAME-$VERSION/etc/$CORE_MANAGER
create_from_template redhat/templates/c4u-supervisor.config $INSTALL/$APPLICATION_NAME-$VERSION/etc/sysconfig/c4u-supervisor
create_from_template redhat/templates/c4u-hostprocess.service $INSTALL/$APPLICATION_NAME-$VERSION/usr/lib/systemd/system/c4u-hostprocess.service
create_from_template redhat/templates/c4u-guiserver.service $INSTALL/$APPLICATION_NAME-$VERSION/usr/lib/systemd/system/c4u-guiserver.service
create_from_template redhat/templates/c4u-supervisor.service $INSTALL/$APPLICATION_NAME-$VERSION/usr/lib/systemd/system/c4u-supervisor.service

echo "Using the following init file:" 
cat $INSTALL/$APPLICATION_NAME-$VERSION/etc/$CORE_MANAGER
echo "------------------------------"

#create_from_template redhat/templates/bootup.template $INSTALL/$APPLICATION_NAME-$VERSION/etc/S01$CORE_MANAGER
#create_from_template redhat/templates/shutdown.template $INSTALL/$APPLICATION_NAME-$VERSION/etc/K01$CORE_MANAGER
#create_from_template redhat/templates/supervisor.config $INSTALL/$APPLICATION_NAME-$VERSION/etc/sysconfig/$CORE_MANAGER

#cp redhat/templates/custcare.init $INSTALL/$APPLICATION_NAME-$VERSION/etc/custcare
#cp redhat/templates/custcare.bootup $INSTALL/$APPLICATION_NAME-$VERSION/etc/S01custcare
#cp redhat/templates/custcare.shutdown $INSTALL/$APPLICATION_NAME-$VERSION/etc/K01custcare

cd $INSTALL
tar zcvf $PACKAGE_NAME-$VERSION.tar.gz --exclude=".svn" $APPLICATION_NAME-$VERSION &> /dev/null
cd - &> /dev/null

mkdir $TMP
mkdir $TMP/BUILD
mkdir $TMP/RPMS
mkdir $TMP/SOURCES
mkdir $TMP/SPECS
mkdir $TMP/SRPMS

mv $INSTALL/$PACKAGE_NAME-$VERSION.tar.gz $TMP/SOURCES/
create_from_template redhat/templates/spec.template $TMP/SPECS/$PACKAGE_NAME.spec

cd $TMP
rpmbuild -v -bb --clean SPECS/$PACKAGE_NAME.spec

RELEASE_FILE=$PACKAGE_NAME-$VERSION.$REVISION.x86_64.rpm
mkdir $INSTALL/release
cp $TMP/RPMS/x86_64/*.rpm $INSTALL/release/$RELEASE_FILE

cd - &> /dev/null
#rm -rf $TMP

# Sign the package

$INSTALL/redhat/rpm-sign.exp $INSTALL/release/$RELEASE_FILE
