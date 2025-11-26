package hxc.userinterfaces.gui.jetty;

import java.io.File;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class CmdArgs
{

	public static String baseDir = null;
	public static int port = 8082; // Default PORT
//	private static StringBuilder classpath;
	
	public static String varDir;
	public static String logDir;
	public static int sessionTimeout = 180000;
	public static String serverHost = "localhost";
	public static int serverPort = 10101;
//	public static int debugLevel = 0;
//	public static String workdir;
//	public static String csRoot;

//	public static String getClassPath()
//	{
//		return classpath.toString();
//	}

//	public static String getOptionByName(String name)
//	{
//		return other.get(name);
//	}

//	public static void addLibDir(String dir)
//	{
//		Boolean present = false;
//		for (String segment : classpath.toString().split(":"))
//		{
//			if (segment.equals(dir))
//			{
//				present = true;
//				break;
//			}
//		}
//
//		if (!present)
//		{
//			if (classpath.length() > 0)
//			{
//				classpath.append(":");
//			}
//			classpath.append(dir);
//		}
//	}

	/**
	 * All arguments are passed as name1=value1 name2=value2
	 */
	private static Map<String, String> extractArgs(String [] args) throws Exception
	{
		Map<String, String> argsMap = new HashMap<>();
		for (String arg : args)
		{
			if (arg.contains("="))
			{
				String[] parts = arg.split("=");
				if (parts.length > 2)
				{
					// Invalid arg exception
					throw (new Exception("Invalid Option : " + arg));
				}
				else
				{
					String key = parts[0].replaceAll("-", "");
					argsMap.put(key, parts[1]);
				}
			}
		}
		return argsMap;
	}
	
	public static void parseArgs(String[] args) throws Exception
	{
		// Pass Arguments
		Map<String, String> argsMap = extractArgs(args);

		// Port Server Runs on Default 8082 
		if (argsMap.containsKey("port") || argsMap.containsKey("httpPort")) 
			port = argsMap.containsKey("port")? Integer.valueOf(argsMap.get("port")) : Integer.valueOf(argsMap.get("httpPort"));
		
		// Discover Base folder (used for logging)
		if (argsMap.containsKey("baseDir"))
		{
			baseDir = argsMap.get("baseDir");
		}
		else
		{
			String path = CmdArgs.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			baseDir = URLDecoder.decode(path, "UTF-8");
			if (baseDir.indexOf("classes") > 0)
			{
				baseDir = baseDir.substring(0, baseDir.indexOf("class") - 1);
			} 
			else if (baseDir.lastIndexOf("lib") >= (baseDir.length() -4)) 
			{
				baseDir = baseDir.substring(0, baseDir.lastIndexOf(File.separator, baseDir.length()-2)) + File.separator;
			} 
		}
		
		if (argsMap.containsKey("session-timeout"))
			sessionTimeout = Integer.valueOf(argsMap.get("session-timeout"));
		
		if (argsMap.containsKey("serverHost"))
			serverHost = argsMap.get("serverHost");
		
		if (argsMap.containsKey("serverPort"))
			serverPort = Integer.valueOf(argsMap.get("serverPort"));
				varDir = baseDir + "/var/";
		logDir = varDir + "log/";
		
	}

}
