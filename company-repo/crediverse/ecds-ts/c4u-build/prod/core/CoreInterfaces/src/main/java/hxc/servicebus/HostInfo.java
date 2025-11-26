package hxc.servicebus;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostInfo
{
	final static Logger logger = LoggerFactory.getLogger(HostInfo.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Host Name
	//
	// /////////////////////////////////
	private static String hostName = null;

	// Overloaded to support logging
	public static String getName(IServiceBus esb)
	{
		if (hostName == null)
		{
			try
			{
				hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
				int firstDotPosition = hostName.indexOf('.');
				if (firstDotPosition > 0)
				{
					hostName = hostName.substring(0, firstDotPosition);
				}
			}
			catch (UnknownHostException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
		return hostName;
	}

	public static String getName()
	{
		if (hostName == null)
		{
			try
			{
				hostName = InetAddress.getLocalHost().getHostName().toLowerCase();
				int firstDotPosition = hostName.indexOf('.');
				if (firstDotPosition > 0)
				{
					hostName = hostName.substring(0, firstDotPosition);
				}
			}
			catch (UnknownHostException e)
			{

			}
		}
		return hostName;
	}

	public static String getNameOrElseHxC()
	{
		String result = getName();
		return result == null ? "HxC" : result;
	}
}
