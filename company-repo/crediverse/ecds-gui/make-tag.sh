#!/bin/bash

usage( )
{
	if [ "${2}" ]
	then
		echo "ERROR : ${2}"
	fi
	echo "Usage: ${1} [ -h ] [ -c config_file ] [ -l log_file ] [ -m lock_file ]"
	echo
	echo "  -h                     displays this help message"
	echo "  -v                     verbose"
	echo "  -l log_file            specifies log file"
	echo "  -m lock_file           specifies lock file"
	echo "  -c config_file         specifies the config file"
	echo "  -r sdp_registry_list   specifies the list of registered SDP sources"
	echo
	echo "Makes a tag ."
}

SITE_CONFIG_DIR="../site-config"

while getopts "hvs:" OPTION
do
	case "${OPTION}" in
		v)
			echo "VERBOSE"
            VERBOSE=1
			;;

		h)
			usage "${0}"
			exit 0
			;;
		s)
			SITE_CONFIG_DIR="${OPTARG}"
			;;		
		*)
			usage "${0}" "Unsupported option: ${OPTION} ${OPTARG}" 1>&2
			exit 1
			;;
	esac
done

: ${maven:=mvn}

"${maven}" -U clean install -DskipTests || {
	echo "Failed to mvn clean install ..."
	exit 1
}
ECDS_NEXT_TAG=$(git pull >/dev/null && git tag -l | sort -V | tail -1 | awk -F"-" '{print "1.0.0-beta-"$3+1}')
"${maven}" release:clean || {
	echo "Failed to mvn release:clean ..."
	exit 1
}

#echo "Tagging release ${ECDS_NEXT_TAG}"
#"${maven}" release:site || {
#	#echo "Failed to mvn release:site ..."
#	exit 1
#}

echo "Tagging release ${ECDS_NEXT_TAG}"
"${maven}" release:prepare -DdevelopmentVersion=1.0-SNAPSHOT -DreleaseVersion=${ECDS_NEXT_TAG} -Dtag=${ECDS_NEXT_TAG} -DignoreSnapshots || {
	echo "Failed to mvn release:prepare ..."
	exit 1
}
echo "Tag made, check git pipeline ..."

read -r -d '' wait << EOF
set -x
ECDS_NEXT_TAG=${ECDS_NEXT_TAG};
notExists=0;
locked=1;
while [[ "\$(find /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-gui/staging/ -name \${ECDS_NEXT_TAG} | wc -l)" -eq "0" || ( "\$(find /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-gui/staging/\${ECDS_NEXT_TAG} -name *.rpm | wc -l)" -eq "0" || "\$(find /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-gui/staging/\${ECDS_NEXT_TAG} -name *.rpm | xargs -I{} /usr/sbin/fuser {} 2>&1 | wc -l)" -ne "0"  ) ]];
do
  if [[ "\$(find /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-gui/staging/ -name \${ECDS_NEXT_TAG} | wc -l)" == "0" ]];
  then
    echo "waiting for /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-gui/staging/${ECDS_NEXT_TAG} to be created.";
  else
    if [[ \$(find /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-gui/staging/\${ECDS_NEXT_TAG} -name *.rpm | xargs -I{} /usr/sbin/fuser {} | wc -l) != 0 ]];
    then
      echo "waiting for the rpm to finish transferring."
    else
      echo "rpm transfer complete."
    fi;
  fi;
  sleep 2; 
done;
EOF

echo "${wait}"

ssh warehouse.concurrent.systems -C "${wait}"

echo "Tag built and transfered to warehouse. Transferring to local test machines..."

cd ${SITE_CONFIG_DIR}; 
git pull && git submodule update --init --recursive;
rm -r /var/tmp/4mci-cs/c.s/
mkdir -p /var/tmp/4mci-cs/c.s/ecds-gui/staging/
mkdir -p /var/tmp/4mci-cs/c.s/ecds-ts/staging/
{
rsync --partial --progress -av --checksum $( ssh warehouse.concurrent.systems 'find /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-ts/staging/ -type f -printf "%T@:%p\n" | sort -t: -k1 -n | sed "s/^[^:]\+://g" | tail -2' | sed 's/.*/warehouse.concurrent.systems:&/g' | xargs -n1 dirname | xargs -n1 dirname ) /var/tmp/4mci-cs/c.s/ecds-ts/staging/
rsync --partial --progress -av --checksum $( ssh warehouse.concurrent.systems 'find /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-gui/staging/ -type f -printf "%T@:%p\n" | sort -t: -k1 -n | sed "s/^[^:]\+://g" | tail -2' | sed 's/.*/warehouse.concurrent.systems:&/g' | xargs -n1 dirname | xargs -n1 dirname ) /var/tmp/4mci-cs/c.s/ecds-gui/staging/
./extra/rsync.sh --partial --progress -av --checksum warehouse.concurrent.systems:/var/opt/webdav/warehouse.concurrent.co.za/teams/technical/selinux-policy_module-mysql_instance/mysql_instance.pp /var/tmp/4mci-cs/c.s/
./extra/rsync.sh --checksum --partial --progress -av --omit-dir-times /var/tmp/4mci-cs/c.s/ root@ecds-vm2.concurrent.co.za:/var/opt/csip/sources/concurrent.systems/
./extra/rsync.sh --checksum --partial --progress -av --omit-dir-times /var/tmp/4mci-cs/c.s/ root@ecds-vm3.concurrent.co.za:/var/opt/csip/sources/concurrent.systems/
./extra/rsync.sh --checksum --partial --progress -av --omit-dir-times /var/tmp/4mci-cs/c.s/ root@c4u-ecozim.concurrent.co.za:/var/opt/csip/sources/concurrent.systems/
./extra/rsync.sh --checksum --partial --progress -av --omit-dir-times /var/tmp/4mci-cs/c.s/ root@ecds-pilot.ecds.concurrent.systems:/var/opt/csip/sources/concurrent.systems/
./extra/rsync.sh --checksum --partial --progress -av --omit-dir-times /var/tmp/4mci-cs/c.s/ root@lab14-ecds-vm1.concurrent.co.za:/var/opt/csip/sources/concurrent.systems/
./extra/rsync.sh --checksum --partial --progress -av --omit-dir-times /var/tmp/4mci-cs/c.s/ root@lab14-ecds-vm2.concurrent.co.za:/var/opt/csip/sources/concurrent.systems/
}
cd -

echo "Rsync done ..."

ECDS_BUILD=$(ssh warehouse.concurrent.systems -C  "find /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-gui/staging/${ECDS_NEXT_TAG}/ -name "'"'"ecds-gui-1.0.0-[1-9][0-9]*.noarch.rpm"'"'" | sed 's~/var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-gui/staging/~~'")
TS_BUILD=$(ssh warehouse.concurrent.systems -C  "find /var/opt/webdav/warehouse.concurrent.co.za/releases/ecds-ts/staging/ -type f -printf "'"'"%T@:%p\n"'"'" -name "'"'"ecds-ts-*.rpm"'"'" | sort -t: -k1 -n |  sed "'"'"s/^[^:]\+://g"'"'" | tail -1 | sed -e 's~.*/\(.\+/.\+/.\+\)$~\1~g'")

sed -i "s~c4u_package: "'"'".*$~c4u_package: "'"'"${TS_BUILD}"'"'"~g" ${SITE_CONFIG_DIR}/inventory/group_vars/concurrent.co.za.yml
sed -i "s~ecdsui_package: "'"'".*$~ecdsui_package: "'"'"${ECDS_BUILD}"'"'"~g" ${SITE_CONFIG_DIR}/inventory/group_vars/concurrent.co.za.yml

echo "Artefact Inventory..."
grep "c4u_package" ${SITE_CONFIG_DIR}/inventory/group_vars/concurrent.co.za.yml
grep "ecdsui_package" ${SITE_CONFIG_DIR}/inventory/group_vars/concurrent.co.za.yml

echo "Automatic changes made to inventory/group_vars/concurrent.co.za.yml. Please check, commit and push.  Then run ansible."
echo "ansible-playbook local-transactserver.yml --limit concurrent.co.za-ecds-vmx --tags ecds"
