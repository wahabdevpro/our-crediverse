package hxc.connectors.kerberos.client;

import static hxc.connectors.kerberos.Authenticator.Error.JAAS_KRB_ERROR;
import static hxc.connectors.kerberos.Authenticator.Error.UNKNOWN_CODE;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.kerberos.Authenticator;
import hxc.connectors.kerberos.utils.JAASNegotiateScheme;

public class JAASLoginClientImpl extends Authenticator
{
	final static Logger logger = LoggerFactory.getLogger(JAASLoginClientImpl.class);
	
	final int defaultConnectionTimeout = 3000; // msec

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Authenticator overrides
    //
    // /////////////////////////////////
	
    /*  https://technet.microsoft.com/en-us/library/dd560670(v=ws.10).aspx
     *       
     *  Both DES cipher suites (DES-CBC-MD5 & DES-CBC-CRC) are disabled by default in Windows 7.
     *  The following cipher suites are enabled by default in Windows 7 and Windows Server 2008 R2:
     *  AES256-CTS-HMAC-SHA1-96
     *  AES128-CTS-HMAC-SHA1-96
     *  RC4-HMAC
     */
 
	String buildConfigContent()
	{ 	
		/*
		Possible configs to add on demand: 
		* capaths, allow_weak_crypto, clockskew
		* Skip past preformatted text udp_preference_limit = 1
		*/
		String defaultContent = "\n";
		defaultContent += "[libdefaults]\n";
		defaultContent += "\tdefault_tkt_enctypes = aes256-cts-hmac-sha1-96 aes128-cts-hmac-sha1-96 rc4-hmac\n";
		defaultContent += "\tkdc_timeout = #timeout#\n";
		defaultContent += "\tmax_retries = 1\n";
		
		// Apply use UDP/TCP field on JAAS
		if (!config.getUseUDP())
			defaultContent += "\tudp_preference_limit = 1\n";		
		
		// Retrieve a valid timeout value
		String connectionTimeout = Integer.valueOf(defaultConnectionTimeout).toString();
		try
		{
			int configTimeout = ( config.getTimeout() > 0 ? config.getTimeout() : 1 );
			connectionTimeout = Integer.valueOf(configTimeout*1000).toString();
		}
		catch(Exception e)
		{
			logger.error(e.getMessage());
		}
		
		defaultContent = defaultContent.replaceAll("#timeout#",  connectionTimeout);
		return defaultContent;
	}
	
	Path buildConfig()
	{
		Path tmpFile = null;
		String content = buildConfigContent();
		
		if ( content.isEmpty() )
			return null;
    	
    	try 
    	{
			tmpFile = Files.createTempFile(null, null);
			BufferedWriter writer = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8);
			writer.write(content);
			writer.close();
		} catch (IOException e) 
    	{
			logger.error(e.getMessage());
			if(tmpFile != null)
				tmpFile.toFile().delete();
		}
      
    	return tmpFile;
	}

    @Override
    public boolean tryAuthenticate(String username, String password,
            Authenticator.Error err)
    {
        boolean finalResult = false;

        Path tmpFile = null;
    	
        try
        {
        	tmpFile = buildConfig();
        	String absoultePath = (tmpFile != null ? absoultePath = tmpFile.toString() : "");
        	
            logger.info(
                    "An attempt to authenticate started Host [{}] Realm [{}]",
                    config.getHostName(), config.getRealm());

            // Enable if running for custom tests "sun.security.krb5.debug",
            // "true"
            String debugTraces = ( logger.isTraceEnabled() ? "true" : "false" );
            System.setProperty("sun.security.krb5.debug", debugTraces);
            // Use krb5.conf customized in order to provide some configurations.
            System.setProperty("java.security.krb5.conf", absoultePath );
            
            // Set KDC and REALM. 
            // Note 1: Due to a bug in krb5.kdc class it does not accept port number
            // That's why we assume default port 88
            // Note 2: Keep passing KDC hostname via setProperty not via java.security.krb5.conf configuration file.
            // That's made in order to protect from hostname injection attack.
            
            System.setProperty("java.security.krb5.realm", config.getRealm());
            System.setProperty("java.security.krb5.kdc", config.getHostName());
            
            
            System.setProperty("javax.security.auth.useSubjectCredsOnly","false");

            JAASNegotiateScheme nego = new JAASNegotiateScheme();
            finalResult = nego.authenticate(username, password, err);
            
            logger.info(
                    "An attempt to authenticate completed. Host [{}] Realm [{}]",
                    config.getHostName(), config.getRealm());

        } catch (Exception e)
        {
            String message = "Kerberos authenticate procedure error: "
                    + e.getMessage();
            logger.error(message);
             
            err.Set(JAAS_KRB_ERROR, UNKNOWN_CODE, e.getMessage());
        }
        finally
        {
        	if (tmpFile != null)
        		tmpFile.toFile().delete();
        }

        return finalResult;
    }

    @Override
    public boolean tryChangePassword(String username, String oldPassword,
            String newPassword, Authenticator.Error err)
    {
        err.Set(JAAS_KRB_ERROR, UNKNOWN_CODE, "Not implemented");

        return false;
    }

}
