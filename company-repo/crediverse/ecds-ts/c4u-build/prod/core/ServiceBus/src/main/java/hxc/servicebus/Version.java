package hxc.servicebus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version
{
	public static final String PROJECT_PROPERTIES = "/ecds-ts.properties";
	public static String major = "1.0";
	public static String revision = "[REVISION]";
	
	public static void configure()
	{
		Properties prop = new Properties();
		InputStream in = prop.getClass().getResourceAsStream(PROJECT_PROPERTIES);
		if (in != null)
		{
		    try {
			    prop.load(in);
			    major = prop.getProperty("major.version");
			    revision = prop.getProperty("build.number");
			    in.close();
		    } catch (IOException e) {
			    
		    }
		}
	}
}

