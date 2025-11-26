#!/bin/bash

if [ $# -ne 2 ]; then
	echo "Usage: <package> <interface>"
	exit 1
fi

echo "Setting up mysql for remote logging."
mysql -u root -pussdgw -e "use mysql; grant all PRIVILEGES on *.* to root@'%' identified by 'ussdgw';"

echo "Creating replication user..."
mysql -uroot -pussdgw -e "create user 'repl'@'%' identified by 'ussdgw'; grant replication slave on *.* to 'repl'@'%'; flush privileges;"

#Clear the database
echo "Clearing the database of any hxc database."
mysql -u root -pussdgw -e "drop database hxc;" 2>/dev/null

#Uninstall the package if it was there previously
sudo dpkg --purge cs-app-c4u

rm /var/opt/cs/c4u/installations/*

#Check for attach and detach files
if [ ! -f /var/opt/cs/c4u/installations/attach_vip.sh ]; then
	touch /var/opt/cs/c4u/installations/attach_vip.sh
	echo "#!/bin/bash
/sbin/ifconfig $2:1 192.168.0.182/23
/sbin/ifconfig $2:1 up
mysql -u root -pussdgw -e \"STOP SLAVE; RESET MASTER;\"" > /var/opt/cs/c4u/installations/attach_vip.sh
	chmod +x /var/opt/cs/c4u/installations/attach_vip.sh
fi

if [ ! -f /var/opt/cs/c4u/installations/detach_vip.sh ]; then
	touch /var/opt/cs/c4u/installations/detach_vip.sh
	echo "#!/bin/bash
/sbin/ifconfig $2:1 down
mysql -u root -pussdgw -e \"CHANGE MASTER TO MASTER_HOST='192.168.0.182', MASTER_USER='repl', MASTER_PASSWORD='ussdgw';\"" > /var/opt/cs/c4u/installations/detach_vip.sh
	chmod +x /var/opt/cs/c4u/installations/detach_vip.sh
fi


#Install the package
sudo dpkg -i $1

#Run the hostprocess to create the database

/sbin/ifconfig $2:1 192.168.0.182/23
/sbin/ifconfig $2:1 up

#/opt/cs/c4u/0.0/hostprocess/bin/hostprocess &
java -cp /opt/cs/c4u/0.0/hostprocess/lib:/opt/cs/c4u/0.0/hostprocess/lib/* hxc.test.TestHost &
PID=$!
sleep 6
echo "The PID for hostprocess is $PID"
echo "Waiting for database to be created..."
RESULT=`mysqlshow -uroot -pussdgw hxc | grep -c hxc &>/dev/null`
while [ "$RESULT" == "0" ]; do
	RESULT=`mysqlshow -uroot -pussdgw hxc | grep -c hxc &>/dev/null`
done
kill $PID &>/dev/null

/sbin/ifconfig $2:1 down

echo -n "Please enter in number of hosts: "
read NUM_HOSTS

for (( x = 0; x < $NUM_HOSTS ; x++ )) do
	echo -n "Enter in hosts name: "
	read ZONE_HOST	
	ZONES[$x]=$ZONE_HOST
done

for (( x = 0; x < $NUM_HOSTS ; x++ )) do
	let NEXT=$x+1
	if [ "$NEXT" -eq "$NUM_HOSTS" ]; then
		let NEXT=0
	fi
	echo "Adding record: ${ZONES[$x]} and ${ZONES[$NEXT]}"
	mysql -u root -pussdgw -e "use hxc; insert into ct_server (serverhost, peerhost) values("\"${ZONES[$x]}\"", "\"${ZONES[$NEXT]}\"");"
done

mysql -u root -pussdgw -e "use hxc; update ct_role set attachCommand = 'sh /var/opt/cs/c4u/installations/attach_vip.sh'; update ct_role set detachCommand = 'sh /var/opt/cs/c4u/installations/detach_vip.sh';"

echo "Done."
