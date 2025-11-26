package za.co.concurrent.NetworkIdentifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.pcap4j.util.MacAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSArpAndDNSResolver {
	private static final Logger logger = LoggerFactory.getLogger(OSArpAndDNSResolver.class);

	/**
	 * Do a reverse DNS lookup to find the host name associated with an IP address. Gets results more often than
	 * {@link java.net.InetAddress#getCanonicalHostName()}, but also tries the Inet implementation if reverse DNS does
	 * not work.
	 * 
	 * Based on code found at http://stackoverflow.com/questions/7097623/need-to-perform-a-reverse-dns-lookup-of-a-particular-ip-address-in-java
	 * 
	 * @param ip The IP address to look up
	 * @return   The host name, if one could be found, or the IP address
	 * 
	 */
	final static Logger log = LoggerFactory.getLogger(OSArpAndDNSResolver.class);
	public static String getHostName(final String ip)
	{
		String retVal = null;
		final String[] bytes = ip.split("\\.");
		if (bytes.length == 4)
		{
			try
			{
				final java.util.Hashtable<String, String> env = new java.util.Hashtable<String, String>();
				env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
				final javax.naming.directory.DirContext ctx = new javax.naming.directory.InitialDirContext(env);
				final String reverseDnsDomain = bytes[3] + "." + bytes[2] + "." + bytes[1] + "." + bytes[0] + ".in-addr.arpa";
				final javax.naming.directory.Attributes attrs = ctx.getAttributes(reverseDnsDomain, new String[]
						{
								"PTR",
						});
				for (final javax.naming.NamingEnumeration<? extends javax.naming.directory.Attribute> ae = attrs.getAll(); ae.hasMoreElements();)
				{
					final javax.naming.directory.Attribute attr = ae.next();
					final String attrId = attr.getID();
					for (final java.util.Enumeration<?> vals = attr.getAll(); vals.hasMoreElements();)
					{
						String value = vals.nextElement().toString();
						// logger.info(attrId + ": " + value);

						if ("PTR".equals(attrId))
						{
							final int len = value.length();
							if (value.charAt(len - 1) == '.')
							{
								// Strip out trailing period
								value = value.substring(0, len - 1);
							}
							retVal = value;
						}
					}
				}
				ctx.close();
			}
			catch (final javax.naming.NamingException e)
			{
				// No reverse DNS that we could find, try with InetAddress
				log.debug(""); // NO-OP
			}
		}

		if (null == retVal)
		{
			try
			{
				retVal = java.net.InetAddress.getByName(ip).getCanonicalHostName();
			}
			catch (final java.net.UnknownHostException e1)
			{
				retVal = ip;
			}
		}
		log.debug(" The resolved name for {} is {}!\n", ip, retVal);
		return retVal;
	}
	public static HashMap<String, NetID> getCache( ){
		return getCache("wlan0");
	}

	public static HashMap<String, NetID> getCache( String ifaceName  ){
		HashMap<String, NetID> cachedIds=new HashMap<String, NetID>();
		Process p;
		String strScript = "#!/usr/bin/env bash\r\n\r\niface=$1\r\n\r\nusage=$0\" <network interface>\"\r\nif ([[ \"$iface\" == \"\" ]])\r\nthen\r\n  echo \"Usage: $usage\"; exit 1\r\nfi\r\n\r\nOS=$(uname -s)\r\n\r\nif ([[ \"$OS\" == \"Linux\" ]])\r\nthen\r\n  : #echo \"OS is Linux\"\r\nelif ([[ \"$OS\" == \"SunOS\" ]])\r\nthen\r\n  : #echo \"OS is Solaris\"\r\nelse\r\n  echo \"This OS is unsupported, only Linux and Solaris are supported. Usage: $usage\";  exit 1\r\nfi\r\n\r\nget_broadcast_Linux()\r\n{\r\n  #Get broadcast address in linux:\r\n  b_cast=$(ifconfig -a $iface | awk -F: '/Bcast/ { print $3}' | awk '{ print $1}')\r\n  echo $b_cast\r\n}\r\n\r\nget_broadcast_SunOS()\r\n{\r\n  #Get broadcast address in solaris:\r\n  b_cast=$(ifconfig $iface | awk '/broadcast/ { print $6}')\r\n  echo $b_cast\r\n}\r\n\r\nwarm_arp_cache_Linux()\r\n{\r\n  bcast_addr=$1\r\n  #send 3 broadcast ICMP packets in linux:\r\n  ping -c3 -b $bcast_addr 2 > /dev/null\r\n}\r\n\r\nwarm_arp_cache_SunOS()\r\n{\r\n  bcast_addr=$1\r\n  #send broadcast ICMP packets in solaris and receive the first 100 responses:\r\n  ping -s $bcast_addr 64 100 2 > /dev/null\r\n}\r\n\r\nget_canonical_host_unix()\r\n{\r\n  ip=$1\r\n  echo $( dig -x $ip | grep ANSWER -A1| awk '/PTR/ {print $5}' )\r\n}\r\n\r\nget_names_Linux()\r\n{\r\n  arp -an |perl -e'\r\n  while (<>)\r\n  {\r\n    if ( $_=~/.+\\s+\\((.+)\\)\\s+at\\s+(.+)\\s+\\[ether\\].+/ ){\r\n      my ($ip,$mac)=($1,$2);\r\n      my $nam=`dig -x $ip | grep ANSWER -A1| tail -1 |awk \"/PTR/ {print $5}\"`;\r\n      my @rec=split(/\\s+/,$nam);\r\n      $nam=$rec[-1];\r\n      chomp($nam);\r\n      print \"$ip $mac $nam\\n\";\r\n    }\r\n  }'\r\n}\r\n\r\nget_names_SunOS()\r\n{\r\n  arp -an |perl -e'\r\n  while (<>)\r\n  {\r\n    if ( $_=~/^\\S+\\s+(\\S+)\\s+.+\\s+((..\\:)+..).*$/m ){\r\n      my ($ip,$mac)=($1,$2);\r\n      my $nam=`dig -x $ip |awk \"/PTR/ {print $5}\"| tail -1`;\r\n      my @rec=split(/\\s+/,$nam);\r\n      $nam=$rec[-1];\r\n      chomp($nam);\r\n      print \"$ip $mac $nam\\n\";\r\n    }\r\n  }'\r\n}\r\n\r\n\r\nbc=$(\"get_broadcast_\"$OS)\r\n\r\n#echo \"broadcast: $bc\"\r\n\r\n#echo \"warming up the arp cache...\"\r\n\r\npings=$(\"warm_arp_cache_\"$OS $bc|tail -20)\r\n\r\n#echo \"cache warmed: $pings\"\r\n\r\nall_names=$(\"get_names_\"$OS)\r\n\r\n#echo \"All Names:\"\r\necho \"$all_names\"\r\n\r\n";
		String scriptFileName = System.getProperty("user.home") + "/who_is_around";
		try {
			p = Runtime.getRuntime().exec( "rm -f " + scriptFileName + " 2>/dev/null");
			p.waitFor();
			
			File file = new File(scriptFileName);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(strScript);
			bw.close();
			
			p = Runtime.getRuntime().exec( "dos2unix " + scriptFileName );
			p.waitFor();
			
			p = Runtime.getRuntime().exec( " chmod +x " + scriptFileName );
			p.waitFor();

			p = Runtime.getRuntime().exec( scriptFileName + " " + ifaceName + " 2>/dev/null");
			p.waitFor();
			BufferedReader reader = 
					new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";			
			while ((line = reader.readLine())!= null) {
				log.info("line: {}",line);
				String parts[]=line.split(" ");
				if( parts.length > 0 )
				{
					NetID anId = new NetID( MacAddress.ETHER_BROADCAST_ADDRESS.toString(), parts[0], "localhost" );
					if( parts.length > 1 )
						anId.setMacAddress(parts[1]);
					if( parts.length > 2 )
						anId.setHostname(parts[2]);
					cachedIds.put(anId.getIpAdress(), anId);
				}
			}

		} catch (Exception e) {
			logger.error("", e);
		}

		return cachedIds;
	}
}
