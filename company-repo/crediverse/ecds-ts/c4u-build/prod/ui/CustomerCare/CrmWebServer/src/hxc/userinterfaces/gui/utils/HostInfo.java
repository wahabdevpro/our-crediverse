package hxc.userinterfaces.gui.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostInfo
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Host Name
	//
	// /////////////////////////////////
	private static String hostName = null;

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