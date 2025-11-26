package cs.service;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs.dto.security.LoginSessionData;
import cs.utility.NetUtil;
import za.concurrent.NetworkIdentifier.NetID;
import za.concurrent.NetworkIdentifier.NetIDs;


@Service
public class NetworkIdentifierService
{
	private static Logger logger = LoggerFactory.getLogger(NetworkIdentifierService.class);

	private String defaultIp;

	@Autowired
	private LoginSessionData sessionData;

	private List<NetIDs> uniqueServiceList = new ArrayList<NetIDs>();

	private Map<String, NetIDs> serviceMap = new HashMap<String, NetIDs>();

	private boolean addService(NetworkInterface netIf)
	{
		//NetIDs np = new NetIDs(netIf.getName());
		/*NetIDs np = new NetIDs(netIf.getName(), CONST_ARPCACHE_MAX_ENTRIES, CONST_ARPCACHE_EXPIRE_MINUTES);
		np.init();
		uniqueServiceList.add(np);
		for (InterfaceAddress address : netIf.getInterfaceAddresses())
		{
			String currentAddress = address.getAddress().getHostAddress();
			String[] parts = currentAddress.split( "\\." );
			if (parts.length == 4 && defaultIp == null) {
				defaultIp = currentAddress;
			}
			serviceMap.put(currentAddress, np);
		}*/
		return true;
	}

	@PostConstruct
	private void configure() throws SocketException
	{
		try
		{
			NetUtil.getInterfaces(this::addService);
		}
		catch(Throwable th)
		{
			logger.error("", th);
		}
	}

	private NetID getIpDetails(String ip, String incomingIp) throws ExecutionException, InterruptedException
	{
		NetID result = null;
		NetIDs np = serviceMap.get((incomingIp == null)?defaultIp:incomingIp);
		if (np != null && ip != null)
		{
			result = np.getPrint(ip);
		}
		return result;
	}

	public void lookupAddress(LoginSessionData sessionData)
	{
		try
		{
			NetID result = getIpDetails(sessionData.getIpAddress(), sessionData.getIncomingIp());

			if (result != null)
			{
				this.sessionData.updateDetails(result);
				logger.info("Setting IP to "+result.getIpAdress());
				logger.info("Setting Hostname to "+result.getHostname());
				logger.info("Setting MAC Address to "+result.getMacAddress());
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	@PreDestroy
	private void destroy() throws Exception
	{
		for (NetIDs np : uniqueServiceList)
		{
			np.terminate();
		}
	}
}
