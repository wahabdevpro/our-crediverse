## Configuring JLI
To enable JLI so that Java can use `libjli.so`, edit `/etc/ld.so.conf.d/ecdsui.conf` and a single line containing the path to ${JAVA_HOME}/jre/lib/amd64/jli,
e.g.
``` 
	/opt/ecds/3rd/java/jre/lib/amd64/jli
```
Then run:
```
	sudo ldconfig -v
```

For more information, please check:
 - https://github.com/kaitoy/pcap4j/blob/pcap4j-1.6.4/README.md#others
 - http://bugs.java.com/view_bug.do?bug_id=7157699
 - https://bugs.openjdk.java.net/browse/JDK-7076745
 - https://github.com/kaitoy/pcap4j/issues/63

## Configuring capabilities for libpcap

Run the following from terminal as root:

```
	setcap cap_net_raw,cap_net_admin=eip ${JAVA_HOME}/bin/java
```
where ${JAVA_HOME} is the root of the Java installation, example 

```
	setcap cap_net_raw,cap_net_admin=eip /opt/ecds/3rd/java/bin/java
```

For more information please check:
 - http://man7.org/linux/man-pages/man8/ld.so.8.html#ENVIRONMENT
 
## Starting ecdsui as Portal
The SystemD service instance name is used to indicate how the Web Application will start. 
If it contains the work "portal" in the instance, the ecdsui was start using the Portal profile, else it will start using prod (AdminUI) profile