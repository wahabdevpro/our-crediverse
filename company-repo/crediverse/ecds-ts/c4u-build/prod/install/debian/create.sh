#!/bin/bash -x

echo "Starting creating Debian installation"

# Create folder structure
INSTALL_DIR=$INSTALL/$APPLICATION_NAME-$VERSION

# 
mkdir -p $INSTALL_DIR/$APPLICATION_PATH
mv $TMP/* $INSTALL_DIR/$APPLICATION_PATH/

# Startup scripts
mkdir -p $INSTALL_DIR/DEBIAN
mkdir -p $INSTALL_DIR/etc/init.d/rc3.d
mkdir -p $INSTALL_DIR/usr/share/doc/$APPLICATION_NAME/copyright

create_from_template debian/templates/init.template $INSTALL_DIR/etc/init.d/$CORE_MANAGER
create_from_template debian/templates/bootup.template $INSTALL_DIR/etc/init.d/rc3.d/S01$CORE_MANAGER
create_from_template debian/templates/shutdown.template $INSTALL_DIR/etc/init.d/rc3.d/K01$CORE_MANAGER

# Debian installation scripts
create_from_template debian/templates/control.template $INSTALL_DIR/DEBIAN/control
create_from_template debian/templates/copyright.template $INSTALL_DIR/usr/share/doc/$APPLICATION_NAME/copyright
create_from_template debian/templates/preinst.template $INSTALL_DIR/DEBIAN/preinst
create_from_template debian/template/postinst.template $INSTALL_DIR/DEBIAN/postinst
create_from_template debian/template/prerm.template $INSTALL_DIR/DEBIAN/prerm
create_from_template debian/template/postrm.template $INSTALL_DIR/DEBIAN/postrm

chmod 0755 -R $INSTALL_DIR

mkdir $INSTALL/$APPLICATION_NAME-$VERSION

RELEASE_FILE=$PACKAGE_NAME-$VERSION.$REVISION-all.deb

dpkg-deb --build  $INSTALL_DIR
mkdir -p $INSTALL/release
mv $INSTALL/*.deb $INSTALL/release/$RELEASE_FILE

# Cleanup
rm -rf $TMP
rm -rf $INSTALL_DIR