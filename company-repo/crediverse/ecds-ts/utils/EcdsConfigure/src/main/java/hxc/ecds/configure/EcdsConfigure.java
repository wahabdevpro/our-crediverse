package hxc.ecds.configure;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import hxc.ecds.utils.encrypt.AesUtils;

public class EcdsConfigure
{
	private static final Logger logger = (Logger) LoggerFactory.getLogger(EcdsConfigure.class);
	
	public static void main(String[] args)
	{
		//int returnCode = 0;
		CommandLine cmd = readInConfiguration(args);
		EcdsConfigure app = new EcdsConfigure();
    	app.performOperations(cmd);

        logger.info("Done");
        //return returnCode;
	}

	private static CommandLine readInConfiguration(String[] args )
	{
        Options options = new Options();

        Option passwordC4U = new Option("p", "c4u-password", true, "set the C4U configuration database password");
        passwordC4U.setRequired(false);
        options.addOption(passwordC4U);

        Option passwordOltp = new Option("t", "oltp-password", true, "set the OLTP database password");
        passwordOltp.setRequired(false);
        options.addOption(passwordOltp);
        
        Option passwordOlap = new Option("a", "olap-password", true, "set the OLAP database password");
        passwordOlap.setRequired(false);
        options.addOption(passwordOlap);
        
        Option location = new Option("l", "location", true, "specify the location of the configuration files that are subject to be changed");
        location.setRequired(false);
        options.addOption(location);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            logger.info(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }
		return cmd;
	}
	
	public void performOperations(CommandLine cmd)
	{
		String passwordC4U;
        String passwordOltp;
        String passwordOlap;
        String location = "./";
        String filename;
        if(cmd.hasOption("location"))
        {
        	location = cmd.getOptionValue("location");
        	logger.info("Setting working directory location to {}", location);
        }
		try
		{
	        if(cmd.hasOption("c4u-password"))
	        {
	        	passwordC4U = cmd.getOptionValue("c4u-password");
	        	String encoded = toBase64(passwordC4U);
	        	logger.info("Setting C4U password to {}, encoded value {}", passwordC4U, encoded);
	        	filename = location + "MySqlConfig.xml";
	        	updatePassword(filename, "password", encoded);
	        }
        } catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
        try
		{
		    if(cmd.hasOption("oltp-password"))
		    {
		    	passwordOltp = cmd.getOptionValue("oltp-password");
		    	String encrypted = AesUtils.encrypt(passwordOltp);
		    	logger.info("Setting OLTP password to {}, encrypted value {}", passwordOltp, encrypted);
		    	filename = location + "database-settings-oltp.xml";
		    	updatePassword(filename, "javax.persistence.jdbc.password", encrypted);
		    }
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
        try
		{
	        if(cmd.hasOption("olap-password"))
	        {
	        	passwordOlap = cmd.getOptionValue("olap-password");
	        	String encrypted = AesUtils.encrypt(passwordOlap);
	        	logger.info("Setting OLAP password to {}, encrypted value {}", passwordOlap, encrypted);
	        	filename = location + "database-settings-olap.xml";
	        	updatePassword(filename, "javax.persistence.jdbc.password", encrypted);
	        }
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	private void updatePassword(String configFilename, String passwordKey, String newPassword)
	{
		Map<String, String> props = readPropertiesFile(configFilename);
		java.util.Properties properties = new java.util.Properties();
		try (OutputStream os = new FileOutputStream(configFilename))
		{
			for(String key: props.keySet())
			{
				String value = props.get(key);
				if(passwordKey.equals(key))
				{
					value = newPassword;
				}
				properties.setProperty(key, value);
			}
			if(!properties.isEmpty())
				properties.storeToXML(os, "MySQL Connection Parameters");
			else 
				logger.error("No properties were read from {}", configFilename);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	private String fromBase64(String text)
	{
		try
		{
			return new String(DatatypeConverter.parseBase64Binary(text), "UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			return "";
		}
	}

	private String toBase64(String text)
	{
		try
		{
			return DatatypeConverter.printBase64Binary(text.getBytes("UTF-8"));
		}
		catch (UnsupportedEncodingException e)
		{
			return "";
		}
	}
	
	private Map<String, String> readPropertiesFile(String configFilename)
	{
		Map<String, String> props = new HashMap<String, String>();
		File configFile = new File(configFilename);
		
		if (!configFile.exists())
			return props;

		try (InputStream is = new FileInputStream(configFilename))
		{
			java.util.Properties properties = new java.util.Properties();
			properties.loadFromXML(is);
			for(Object okey : properties.keySet())
			{
				String key = okey.toString();
				String value = properties.getProperty(key);
				if("javax.persistence.jdbc.password".compareTo(key) == 0)
					value = AesUtils.decrypt(value);
				if("password".compareTo(key) == 0)
					value = fromBase64(value);
				props.put(key, value);
			}
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return props;
	}
}
