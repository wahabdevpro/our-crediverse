#!/bin/bash

function create_from_template {
	eval "echo \"$(cat $1)\""
}

#Variable Declaration

#######CHANGE#########

if [ ${#@} = 3 ]; then
	app=$1
	main_class=$2
	make_service=$3
else
	echo "Require 3 arguments, [ appname main-class make-service ]"
	exit 0
fi

# app=hostprocess
# main_class="hxc.test.TestHost"
# make_service=true

#######DON'T CHANGE########

jarfile=`find ../../dist -maxdepth 1 -name *.jar`
jarfile=${jarfile:11}
app_path=opt/cs/components/app/$app
debian_app_path=debian/$app_path
debian_usr_path=debian/usr/bin
debian_init_path=$debian_app_path/libexec
debian_run_path=$debian_app_path/run
debian_copyright_path=debian/usr/share/doc/$app
debian_config=debian/DEBIAN
folder=tmp
if [ "$app" = "cli" ]; then
	script=hxc
else
	script=$app
fi
installfiles=../../../../install/deb
if [ -z "$jarfile" ]; then
	classpath="/$app_path:/$app_path/lib/*"
else
	classpath="/$app_path/$jarfile"
fi

#Directory Creating

mkdir -p $folder/$debian_usr_path
mkdir -p $folder/$debian_app_path
mkdir -p $folder/$debian_init_path
mkdir -p $folder/$debian_run_path
mkdir -p $folder/$debian_copyright_path
mkdir -p $folder/$debian_config

#Check for service
if $make_service ; then
	mkdir -p $folder/debian/etc/init.d
fi

find $folder/debian -type d | xargs chmod 755

#Copy Files

cp -r ../../dist/* $folder/$debian_app_path

#Create Script

if [ -f $script ]; then
	cp $script $folder/$debian_usr_path
elif [ -f ../$script ]; then
	cp ../$script $folder/$debian_usr_path
else
	$installfiles/make_script.sh $classpath $main_class
	mv script $folder/$debian_usr_path/$script
fi

chmod +x $folder/$debian_usr_path/$script

#Create Service

if $make_service ; then
	if [ -f service ]; then
		cp service $folder/$debian_init_path/init.sh
	elif [ -f ../service ]; then
		cp ../service $folder/$debian_init_path/init.sh
	else
		$installfiles/make_service.sh $app $classpath $main_class
		mv init.sh $folder/$debian_init_path
	fi
	chmod +x $folder/$debian_init_path/init.sh
fi

#Create Control

if [ -f control ]; then
	cp control $folder/$debian_config
elif [ -f ../control ]; then
	cp ../control $folder/$debian_config
else
	create_from_template $installfiles/control_template > $folder/$debian_config/control
fi

#Create Copyright

if [ -f copyright ]; then
	cp copyright $folder/$debian_copyright_path
elif [ -f ../copyright ]; then
	cp ../copyright $folder/$debian_copyright_path
else
	create_from_template $installfiles/copyright_template > $folder/$debian_copyright_path/copyright
fi

#Create Postinit Script

if [ -f postinst ]; then
	cp postinst $folder/$debian_config
elif [ -f ../postinst ]; then
	cp postinst $folder/$debian_config
else
	create_from_template $installfiles/postinst_template > $folder/$debian_config/postinst
fi

#Create Prerm Script

if [ -f prerm ]; then
	cp prerm $folder/$debian_config
elif [ -f ../prerm ]; then
	cp ../prerm $folder/$debian_config
else
	create_from_template $installfiles/prerm_template > $folder/$debian_config/prerm
fi

#Configure Everything

chmod 0755 -R $folder

#Create Debian Package

dpkg-deb --build $folder/debian
mv $folder/debian.deb ./$app.deb
rm -r $folder

echo "Finished :)"
