package cs.utility;

import java.io.File;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.AuthenticationResponse;

public class Common
{
	private static final Logger logger = LoggerFactory.getLogger(Common.class);
	
	public static final String CONST_FATAL_ERROR = "{\"state\":\"" + AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID + "\"}";
	
	// Configuration Profiles
	public static final String CONST_DEVELOPMENT_PROFILE = "dev";
	public static final String CONST_TEST_PROFILE = "test";
	public static final String CONST_PROD_PROFILE = "prod";
	
	// Operational Profiles
	public static final String CONST_EXTAPI_PROFILE = "extapi";
	
	// Developer specific configs
	public static final String CONST_DEVELOPER_PROPERTIES = "/developer.properties";
	public static final String CONST_DEV_DEFAULT_PROPERTIES = "/application-dev-default.properties";
	public static final String CONST_SPRING_CONFIG_LOCATION = "spring.config.location";

    public static void checkAgentIdForNull(Integer agentID) {
        if (agentID == null) {
            throw new IllegalArgumentException("Cannot get agent ID from the session. " +
                    "Do you use service user for ordinary API call? Service users are intended to non-airtime calls.");
        }
    }

    private enum ApplicationState
	{
		PROD, DEV, TEST,
		ADMIN, PORTAL, MOBILE, EXTAPI
	};

	public static Set<ApplicationState> applicationStates = new HashSet<ApplicationState>() ;

	private static List<String> profiles = new ArrayList<String>();
	
	private static String[] testServerAddress = new String[]{
			"172.17.5.58",
			"172.17.4.126"
	};
	
	private static boolean checkForTestIp(NetworkInterface netIf)
	{
		for (InterfaceAddress address : netIf.getInterfaceAddresses())
		{
			String ipAddress = address.getAddress().getHostAddress();
			for (String testAdddress : testServerAddress)
			{
				if (testAdddress.equals(ipAddress))
				{
					//applicationState = ApplicationState.TEST;
					//logger.info("Identified test server, enabling test profile.");
				}
			}
		}
		//NetIDs np = new NetIDs(netIf.getName());
		/*NetIDs np = new NetIDs(netIf.getName(), CONST_ARPCACHE_MAX_ENTRIES, CONST_ARPCACHE_EXPIRE_MINUTES);
		np.init();
		uniqueServiceList.add(np);
		for (InterfaceAddress address : netIf.getInterfaceAddresses())
    	{
			serviceMap.put(address.getAddress().getHostAddress(), np);
    	}*/
		return true;
	}

	public static void printCurrentLoadedStates()
	{
		for(ApplicationState state : applicationStates)
		{
			logger.info( String.format("Enabling Profile: %s", state) );
		}
	}
	
	private static void configurePropertyLocations(String profile)
	{
		try
		{
			if (profile != null && profile.equals(CONST_DEVELOPMENT_PROFILE))
			{
				StringBuilder developmentProfile = new StringBuilder("Loading development profile from ");
				URL profileProperties = Common.class.getResource(CONST_DEVELOPER_PROPERTIES);
				if (profileProperties != null)
				{
					System.setProperty(CONST_SPRING_CONFIG_LOCATION, profileProperties.toString());
					developmentProfile.append(profileProperties.toString());
				}
				else
				{
					profileProperties = Common.class.getResource(CONST_DEV_DEFAULT_PROPERTIES);
					if (profileProperties != null)
					{
						System.setProperty(CONST_SPRING_CONFIG_LOCATION, profileProperties.toString());
						developmentProfile.append(profileProperties.toString());
					}
				}
				
				if (profileProperties != null)
				{
					logger.info(developmentProfile.toString());
				}
			}
		}
		catch(Exception ex)
		{
			logger.error("Exception locating properties file ", ex);
		}
	}
	
	/**
	 * Please note that you need to pass in VM argument for other profiles, e.g.
	 * java -jar -Dspring.profiles.active="mobile,portal"
	 * 
	 * Development profile is picked up automatically
	 */
	public static void configure()
	{
		if (System.getProperty( "spring.profiles.active" ) != null) 
		{
			String [] springProfiles = System.getProperty( "spring.profiles.active" ).split(",");
			for(String profile: springProfiles)
			{
				switch(profile.toLowerCase()) 
				{
					case CONST_EXTAPI_PROFILE:
						applicationStates.add( ApplicationState.EXTAPI );
						profiles.add(CONST_EXTAPI_PROFILE);
						break;

					case CONST_PROD_PROFILE:
						applicationStates.add(ApplicationState.PROD);
						break;
				}
			}
		} else {
			applicationStates.add( ApplicationState.EXTAPI );
			profiles.add(CONST_EXTAPI_PROFILE);
		}
		
		/*
		 * Best way I can think of to detect running in an IDE. That is if the source code exists, then assume we are in development mode.
		 */
		if ((!applicationStates.contains(ApplicationState.PROD)) && (new File(System.getProperty("user.dir") + "/src/main/java").exists()))
		{
			applicationStates.add( ApplicationState.DEV );
		}
		
		// Add Default(CAN ONLY HAVE DEV || PROD || TEST)
		if (!applicationStates.contains(ApplicationState.DEV))
		{
			applicationStates.add( ApplicationState.PROD );
		}
		
		// Possibly add Test profile (and iterate through network interfaces for MAC lookup stuff)
		try 
		{
			NetUtil.getInterfaces(Common::checkForTestIp);
		} 
		catch (SocketException e) 
		{
			logger.error("Iterate through network interfaces for MAC lookup stuff", e);
		}
		
		printCurrentLoadedStates();

		// Create list of profiles to set as active
		{
			// CAN ONLY HAVE (DEV || PROD || TEST)
			if (isProduction())
				profiles.add(CONST_PROD_PROFILE);
			else if (isDevelopment())
			{
				configurePropertyLocations(CONST_DEVELOPMENT_PROFILE);
				profiles.add(CONST_DEVELOPMENT_PROFILE);
			}
			else if (isTest())
			{
				configurePropertyLocations(CONST_DEVELOPMENT_PROFILE);
				profiles.add(CONST_TEST_PROFILE);
			}
		}
	}

	public static String[] getProfiles()
	{
		return profiles.toArray(new String[profiles.size()]);
	}

	// Configuration Profiles
	public static boolean isDevelopment()
	{
		return applicationStates.contains(ApplicationState.DEV);
	}

	public static boolean isTest()
	{
		return applicationStates.contains(ApplicationState.TEST);
	}

	public static boolean isProduction()
	{
		return applicationStates.contains(ApplicationState.PROD);
	}
}
