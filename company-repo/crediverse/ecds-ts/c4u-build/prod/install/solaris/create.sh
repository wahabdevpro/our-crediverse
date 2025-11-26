#!/bin/bash

# Create the service script and boot scripts

mkdir $TMP/$CORE_MANAGER/service
create_from_template solaris/templates/init.template $TMP/$CORE_MANAGER/service/$CORE_MANAGER

mkdir $TMP/$CORE_MANAGER/service/boot
create_from_template solaris/templates/bootup.template $TMP/$CORE_MANAGER/service/boot/S01$CORE_MANAGER
create_from_template solaris/templates/shutdown.template $TMP/$CORE_MANAGER/service/boot/K01$CORE_MANAGER

# Create prototype file 

cd $TMP

echo "i pkginfo=./pkginfo
i preinstall=./preinstall
i postinstall=./postinstall
i preremove=./preremove" > prototype
find . -print | grep -v '\.svn' | grep -v '\prototype' | pkgproto >> prototype

echo "d none /etc 0755 root root
d none /etc/init.d 0755 root root
d none /etc/rc3.d 0755 root root
f none /etc/init.d/$CORE_MANAGER=$CORE_MANAGER/service/$CORE_MANAGER 0755 root bin
f none /etc/rc3.d/S01$CORE_MANAGER=$CORE_MANAGER/service/boot/S01$CORE_MANAGER 0755 root bin
f none /etc/rc3.d/K01$CORE_MANAGER=$CORE_MANAGER/service/boot/K01$CORE_MANAGER 0755 root bin" >> prototype

# Create the pkginfo, preinstall, preremove, etc scripts

create_from_template ../solaris/templates/pkginfo.template pkginfo
create_from_template ../solaris/templates/preinstall.template preinstall
create_from_template ../solaris/templates/postinstall.template postinstall
create_from_template ../solaris/templates/preremove.template preremove

mkdir release
RELEASE_FILE=$PACKAGE_NAME-$VERSION.$REVISION-all.pkg

pkgmk -r . -o -d release -a all &> $INSTALL/pkgmk.log

touch $RELEASE_FILE
pkgtrans -s `pwd`/release $RELEASE_FILE $APPLICATION_NAME &> $INSTALL/pkgtrans.log

mkdir ../release
mv *.pkg ../release/

cd - &> /dev/null
rm -rf $TMP
