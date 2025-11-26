## Prerequisites
- Docker must be installed and running correctly.
- The docker-compose utility must be installed
- MariaDB must be installed and running
- The required database schema must already be in MariaDB and the access details must be known.

## Create the Required Directory Structure
Create the required directory structure by running the following commands
```shellscript
mkdir -p ~/Crediverse/srv/etc/ecds-ts
mkdir -p ~/Crediverse/srv/logs/ecds-ts-server
mkdir -p ~/Crediverse/srv/logs/ecds-gui-admin
mkdir -p ~/Crediverse/srv/logs/ecds-gui-portal
mkdir -p ~/Crediverse/srv/logs/ecds-api
mkdir -p ~/Crediverse/srv/edr/ecds-ts-server
mkdir -p ~/Crediverse/srv/var/ecds-ts-server/airsim
mkdir -p ~/Crediverse/srv/var/ecds-ts-server/accdump
mkdir -p ~/Crediverse/srv/var/ecds-ts-server/batch
```
## Create the MySqlConfig.xml File
Copy file [MySqlConfig.xml.j2](https://gitlab.com/csys/ops/standard-platform-deploy/-/blob/master/templates/crediverse/MySqlConfig.xml.j2) to ~/Crediverse/srv/etc/ecds-ts/MySqlConfig.xml (note the missing .j2 at the destination) and change:
DB password, key="password"
The password must be base64 encoded. Let’s assume your password is “mypass”. (I think the initial password is hardcoded so you must use this hardcoded one. I cannot put it here for security reasons, but ask your colleagues or manager which is it.) To encode a string in your command line you can do
base64
mypass
Ctrl+D twice (without pressing Enter before that)
Result is: bXlwYXNz
base64 -d for decode
<entry key="password">bXlwYXNz</entry>


DB IP address, key="server"
From the container point of view the address of your (host) machine is not 127.0.0.1
You can see the IP address of the host machine with ‘ifconfig -a’. Look for docker0. Mine is 172.17.0.1. Probably yours is the same.
<entry key="server">172.17.0.1</entry>


Driver.
It is not necessary to change the driver but on my machine it is not working with “mariadb” so I’ve changed it to “mysql”:
<entry key="driver">com.mysql.jdbc.Driver</entry>

## Create database-settings-ol[at]p.xml
Copy [database-settings-oltp.xml](https://gitlab.com/csys/products/ecds/ecds-ts/-/blob/master/c4u-build/prod/core/EcdsTestHost/database-settings-oltp.xml) to ~/Crediverse/srv/etc/ecds-ts/database-settings-oltp.xml

and [database-settings-olap.xml](https://gitlab.com/csys/products/ecds/ecds-ts/-/blob/master/c4u-build/prod/core/EcdsTestHost/database-settings-olap.xml) to ~/Crediverse/srv/etc/ecds-ts/database-settings-olap.xml

and in them change the IP address and driver (maridb -> mysql if needed) in the connection string:
For OLTP:
<entry key="javax.persistence.jdbc.url">jdbc:mysql://172.17.0.1:3306/hxc...

For OLAP:
<entry key="javax.persistence.jdbc.url">jdbc:mysql://172.17.0.1:3306/ecdsap...

The password in these two files is not only encoded but also encrypted!
The correctly encrypted password should be already in the files. This password must correspond to your DB password. Ask colleagues or your manager if you think you have problems with the password.

## Database Configuration
The database must listen to all IP addresses, not only 127.0.0.1
To do that open your DB configuration file (On my Ubuntu it is here: /etc/mysql/mariadb.conf.d/50-server.cnf) and comment the line
    #bind-address           = 127.0.0.1
or set the address to 0.0.0.0

Also be sure the DB privilege for user root is granted to all databases and all IP addresses. To be sure execute:
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'mypass';
FLUSH PRIVILEGES;

Restart the DB: /etc/init.d/mysql restart

## Run container as a regular user
It is recommended to run containers as a regular user instead of root.
To make regular user to be able to run docker containers do the following (this will make currently logged user to be able to run docker containers):
- sudo groupadd docker
- sudo usermod -aG docker $USER
- newgrp docker 
- docker run hello-world
- sudo chown "$USER":"$USER" /home/"$USER"/.docker -R
- sudo chmod g+rwx "$HOME/.docker" -R
- Log out and log back in so that your group membership is re-evaluated.
If testing on a virtual machine, it may be necessary to restart the virtual machine for changes to take effect.
On a desktop Linux environment such as X Windows, log out of your session completely and then log back in.
- test with: docker run hello-world

## Properties files
Copy the following files into ~/Crediverse/srv/etc/ecds-ts dir:
[application-prod.properties.admin](https://gitlab.com/csys/ops/standard-platform-deploy/-/blob/master/files/crediverse/application-prod.properties.admin)
[application-prod.properties.api](https://gitlab.com/csys/ops/standard-platform-deploy/-/blob/master/files/crediverse/application-prod.properties.api)
[application-prod.properties.portal](https://gitlab.com/csys/ops/standard-platform-deploy/-/blob/master/files/crediverse/application-prod.properties.portal)
[registration.lic](https://gitlab.com/csys/ops/standard-platform-deploy/-/blob/master/files/crediverse/movivy/registration.lic)
[ecds-ts.env](https://gitlab.com/csys/ops/standard-platform-deploy/-/blob/master/files/crediverse/ecds-ts.env)

## docker-compose.yml
The [template](https://gitlab.com/csys/ops/standard-platform-deploy/-/blob/master/templates/crediverse/docker-compose.yml.j2) of docker-compose.yml file contains a lot of placeholders thus I uploaded my current version on Google drive: [docker-compose.yml](https://drive.google.com/file/d/1F3f-HFt3RUfLyE9___HNKKXVmzUkxDDx/view?usp=sharing)
Place it in ~/Crediverse/docker-compose.yml. It will be used later to start the containers.

## **Debug** Crediverse running **in the container**
To be able to debug the process in the container, in docker-compose.yml add the following in the corresponding sections (if you use my version it is already added):
        ports:
            - "5005:5005"
        volumes:
            - <crediverse_project>/ecds-ts/scripts/c4u-hostprocess.sh:/opt/cs/c4u/bin/c4u-hostprocess.sh
Where <crediverse_project> is the directory where your local Crediverse TS project is.

Open <crediverse_project>/ecds-ts/scripts/**c4u-hostprocess.sh** and in the following if block
	if [ "${ARG_SWITCH}" = "**server**" ];
 add a debug option to java arguments:

**DEBUG="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"**

For Java 9 and later the address format is: **address=*:5005**

CMDLINE="${JAVA} -d64 -server ${JVM_MEMORY} ${TIMEZONE} ${SECURITY} ${GCCHOICE} ${LOGGING} ${GCLOGGING} ${GCLOGROTATION} ${DEBUG} -cp ${C4U_HOME}/lib/*:${C4U_HOME}/lib:${C4U_HOME}/hostprocess/lib/*:${C4U_HOME}/hostprocess/plugins/* hxc.test.HostObject"

After that you will be able to run a normal Remote debugging from your IDE on port 5005.

## Start the Containers
First be sure you have access to images repository. To log in use the following command:

```
docker login -u crediverse -p <pass> registry.gitlab.com
```
Where <pass> is the actual password. If you don’t know it, ask colleagues or your manager.

Start the containers (you can add option -d at the end to run in background):

```
docker-compose -f ~/Crediverse/docker-compose.yml up
```

**TS log** can be seen here ~/Crediverse/srv/logs/ecds-ts-server/log.tmp
ADMIN should be accessible at this address: http://localhost:12080/ 
C4U GUI should be accessible at this address: http://localhost:12082/login 
```
User: supplier
Pass: ask your colleagues
```
<span style="color:red">
Note  that Crediverse updates the supplier password every time it is started! This needs additional investigation. (This is probably because of the license. The password is embedded in it.)
</span>

To be able to execute transactions you have to configure some servers in C4U GUI, e.g. AIR and SMPP server. Remember that from the container point of view your host machine is not 127.0.0.1 but probably 172.17.0.1.
