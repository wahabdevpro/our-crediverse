package hxc.connectors.kerberos;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.kerberos.client.ApacheClientImpl;
import hxc.connectors.kerberos.client.JAASLoginClientImpl;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notifications;

public class KerberosConnector implements IConnector, IAuthenticator
{
	final static Logger logger = LoggerFactory.getLogger(KerberosConnector.class);
    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Properties
    //
    // /////////////////////////////////
    private IServiceBus esb;

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // IConnector implementation
    //
    // /////////////////////////////////
    @Override
    public void initialise(IServiceBus esb)
    {
        this.esb = esb;
    }

    @Override
    public boolean start(String[] args)
    {
        logger.info("Kerberos Connector Started");

        return true;
    }

    @Override
    public void stop()
    {
        // Log Information
        logger.info("Kerberos Connector Stopped");
    }

    @Override
    public IConfiguration getConfiguration()
    {
        return config;
    }

    @Override
    public void setConfiguration(IConfiguration config)
            throws ValidationException
    {

        this.config = (KerberosConfiguration) config;

    }

    @Override
    public IConnection getConnection(String optionalConnectionString)
            throws IOException
    {
        return null;
    }

    @Override
    public boolean canAssume(String serverRole)
    {
        return false;
    }

    @Override
    public boolean isFit()
    {
        // GG Implement Fitness Test
        return true;
    }

    @Override
    public IMetric[] getMetrics()
    {
        return null;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Configuration
    //
    // /////////////////////////////////
    @Perms(perms = {
            @Perm(name = "ViewKerberosParameters", description = "View Kerberos Connector Parameters", category = "Kerberos", supplier = true),
            @Perm(name = "ChangeKerberosParameters", implies = "ViewKerberosParameters", description = "Change Kerberos Connector Parameters", category = "Kerberos", supplier = true),
            @Perm(name = "ViewKerberosNotifications", description = "View Kerberos Connector Notifications", category = "Kerberos", supplier = true),
            @Perm(name = "ChangeKerberosNotifications", implies = "ViewKerberosNotifications", description = "Change Kerberos Connector Notifications", category = "Kerberos", supplier = true) })
    public class KerberosConfiguration extends ConfigurationBase
    {
        /** Kerberos Realm */
        private String realm = "ECDS.CONCURRENT.SYSTEMS";//"EXAMPLE.COM";

        /** host name of the Kerberos server */
        private String hostName = "172.17.4.26";// ipa.ecds.concurrent.systems
        
        /** host pport of the Kerberos server */
        private int port = 88;
        
        /** transport type for communication to Kerberos server */
        private boolean useUDP = false;
        
        /** Connection establishment timeout (seconds) */
        private  int timeout = 3;
        
        /** Select which 3rd party Kerberos client implementation to use on Authentication only */
        private boolean useApacheDS = false;
        
        @Override
        public String getPath(String languageCode)
        {
            return "Technical Settings";
        }

        @Override
        public INotifications getNotifications()
        {
            return notifications;
        }

        @Override
        public long getSerialVersionUID()
        {
            return 9149607424108891088L;
        }

        @Override
        public String getName(String languageCode)
        {
            return "Kerberos Connector";
        }

        @Override
        public void validate() throws ValidationException
        {

        }

        @Override
        public void performUpdateNotificationSecurityCheck()
        {
        }

        @Override
        public void performGetNotificationSecurityCheck()
        {
        }

        public String getRealm()
        {
            check(esb, "ViewKerberosParameters");
            return realm;
        }

        public void setRealm(String realm)
        {
            check(esb, "ChangeKerberosParameters");
            this.realm = realm;
        }

        public String getHostName()
        {
            check(esb, "ViewKerberosParameters");
            return hostName;
        }

        public void setHostName(String hostName)
        {
            check(esb, "ChangeKerberosParameters");
            this.hostName = hostName;
        }
        
        public boolean getUseUDP()
        {
            check(esb, "ViewKerberosParameters");
            return useUDP;
        }

        public void setUseUDP(boolean enableUDP )
        {
            check(esb, "ChangeKerberosParameters");
            this.useUDP = enableUDP;
        }
        
        public int getTimeout()
        {
            check(esb, "ViewKerberosParameters");
            return timeout;
        }

        public void setTimeout(int timeoutValue)
        {
            check(esb, "ChangeKerberosParameters");
            this.timeout = timeoutValue;
        }

        public int getPort()
        {
            check(esb, "ViewKerberosParameters");
            return port;
        }

        public void setPort(int port)
        {
            check(esb, "ChangeKerberosParameters");
            this.port = port;
        }
        
        public boolean getUseApacheDS()
        {
            check(esb, "ViewKerberosParameters");
            return useApacheDS;
        }

        public void setUseApacheDS(boolean useApacheDS )
        {
            check(esb, "ChangeKerberosParameters");
            this.useApacheDS = useApacheDS;
        }
        

    };

    KerberosConfiguration config = new KerberosConfiguration();

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Notifications
    //
    // /////////////////////////////////
    Notifications notifications = new Notifications(null);

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // IAuthenticator
    //
    // /////////////////////////////////
    @Override
    public Result authenticate(String username, String password)
    {
        Result result = new Result();
      
        if ( !checkConfig(username,password,result) )
            return result;
        
        // Can switch between JAASLoginClientImpl and ApacheClientImpl
        Authenticator authLogin = null;
        
        if( config.getUseApacheDS() )
        	authLogin = new ApacheClientImpl();
        else
        	authLogin = new JAASLoginClientImpl();
        
        authLogin.setConfig(config);

        Authenticator.Error err = new Authenticator.Error();
        if ( !authLogin.tryAuthenticate(username, password, err) )
        {
            result.code = err.getCode();
            result.description = err.getDescription();  
        }
        else
		{
            result.code = Result.SUCCESS;
            result.description = "SUCCESS";
		}
        

        return result;
    }

    @Override
    public int changePassword(String username, String oldPassword,
            String newPassword)
    { 
        // Only ApacheClientImpl is capable of changing password
        Authenticator apacheLogin = new ApacheClientImpl();
        apacheLogin.setConfig(config);

        Authenticator.Error err = new Authenticator.Error();
        boolean result = apacheLogin.tryChangePassword(username, oldPassword, newPassword, err);

        return (result ? IAuthenticator.Result.SUCCESS : IAuthenticator.Result.UNKNOWN_FAILURE);
    }
    
    private boolean checkConfig(String username, String password,Result result )
    {
        if ( config.getHostName().isEmpty() )
        {
            String description = "Empty KDC hostname provided";      
            
            result.code = -1;
            result.description = description;
            
            logger.error(description );
            
            return false;
        }
        
        if ( username == null || password == null )
        {
            String description = "Null username/password provided";      
      
            result.code = -1;
            result.description = description;
            
            logger.error(description );
            
            return false;
        }
        
        return true;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Methods
    //
    // /////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Helper Methods
    //
    // /////////////////////////////////

}
