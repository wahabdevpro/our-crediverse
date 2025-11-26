package cs.utility;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.function.Function;

public class NetUtil {
	public static void getInterfaces(Function<NetworkInterface,Boolean> processService) throws SocketException
	{
		Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        
        for (NetworkInterface netIf : Collections.list(nets))
        {
        	processService.apply(netIf);
        	Enumeration<NetworkInterface> subIfs = netIf.getSubInterfaces();
    		for (NetworkInterface subIf : Collections.list(subIfs))
    		{
    			processService.apply(subIf);
    		}
        }
	}
}
