package hxc.userinterfaces.gui.jetty;

import java.io.File;
import java.net.URLDecoder;

public class CmdArgs
{
	public static String pluginPaths;

	private static StringBuilder classpath;
	public static int sessionTimeout = 1200; // Time in seconds
	public static int debugLevel;
	public static String baseDir;
	public static int port = 8083;

	// public static String workdir;
	// public static String csRoot;

	public static String varDir;
	public static String tmpDir;
	public static String logDir;

	public static String getClassPath()
	{
		return classpath.toString();
	}

	public static void addLibDir(String dir)
	{
		Boolean present = false;
		for (String segment : classpath.toString().split(":"))
		{
			if (segment.equals(dir))
			{
				present = true;
				break;
			}
		}

		if (!present)
		{
			if (classpath.length() > 0)
			{
				classpath.append(":");
			}
			classpath.append(dir);
		}
	}

	// --commonLibFolder=/Projects/guicoredev/guicore/lib --webappsDir=/Projects/guicoredev/guicore/base
	// --httpPort=8081 --debug=9 --useJasper --toolsJar=/Projects/guicoredev/guicore/lib/tools.jar --workdir

	public static void parseArgs(String[] args) throws Exception
	{

		classpath = new StringBuilder();
		debugLevel = 0;
		File pwd = new File(".");
		baseDir = pwd.getCanonicalPath();

		String path = CmdArgs.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, "UTF-8");
		if (decodedPath.indexOf("WEB-INF") > 0)
		{
			// decodedPath = decodedPath.substring(0, decodedPath.indexOf("classes") - 1);
			decodedPath = decodedPath.substring(0, decodedPath.indexOf("web") - 1);
		}
		// System.out.println("FOUND PATH: " + baseDir);
		// System.out.println("FOUND PATH (2): " + decodedPath); //Using this one for now previous one caused problems

		baseDir = decodedPath;

		for (String arg : args)
		{
			String name;
			String value;
			if (arg.contains("="))
			{
				String[] parts = arg.split("=");
				if (parts.length > 2)
				{
					// Invalid arg exception
					Exception ex = new Exception("Invalid Option : " + arg);
					throw ex;
				}
				name = parts[0];
				value = parts[1];
				if (name.equals("debug"))
				{
					debugLevel = Integer.parseInt(value);
				}
				else if (name.equals("--httpPort") || name.equals("--port"))
				{
					port = Integer.parseInt(value);
				}
				else if (name.equalsIgnoreCase("--plugins"))
				{
					pluginPaths = value;
				}

			}
			else if (arg.equals("--debug"))
			{
				debugLevel = 9;
			}
			else
			{
				// Invalid arg exception
				Exception ex = new Exception("Invalid Option : " + arg);
				throw ex;
			}
		}
		varDir = baseDir + "/var/";
		logDir = varDir + "/log/";
		tmpDir = varDir + "/tmp/";
	}
}
