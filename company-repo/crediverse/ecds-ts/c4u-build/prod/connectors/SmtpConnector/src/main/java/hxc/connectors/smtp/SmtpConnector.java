package hxc.connectors.smtp;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import jakarta.mail.Message;
import jakarta.mail.SendFailedException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.exec.LogOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import com.sun.mail.util.MailConnectException;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;

public class SmtpConnector implements ISmtpConnector
{
	final static Logger logger = LoggerFactory.getLogger(SmtpConnector.class);
	final static Logger sessionLogger = LoggerFactory.getLogger(Session.class);
	
	private IServiceBus esb;
	Configuration config = new Configuration();
	private BlockingQueue<SmtpConnection> connectionQueue = new LinkedBlockingQueue<SmtpConnection>();
	private List<SmtpConnection> connectionList = new ArrayList<SmtpConnection>();
	private volatile int connectionCount = 0;

	public boolean started = false;
	private final Object startedMonitor = new Object();

	private static final int HISTORY_LIMIT = 100;
	private final Queue<SmtpHistory> historyQueue = Queues.synchronizedQueue(EvictingQueue.create( HISTORY_LIMIT ));

	@Perms(perms = { @Perm(name = "ViewSmtpParameters", description = "View SMTP Parameters", category = "SMTP", supplier = true),
			@Perm(name = "ChangeSmtpParameters", implies = "ViewSmtpParameters", description = "Change SMTP Parameters", category = "SMTP", supplier = true) })
	public class Configuration extends ConfigurationBase
	{
		private ServerConfiguration smtpServers[] = new ServerConfiguration[] { new ServerConfiguration(1), new ServerConfiguration(2), new ServerConfiguration(3), new ServerConfiguration(4) };

		private int retriesPerServer = 1;
		private int retryServers = 1;

		/////////////////////////////////////////////////////////

		public int getRetriesPerServer()
		{
			check(esb, "ViewSmtpParameters");
			return this.retriesPerServer;
		}

		public void setRetriesPerServer( int retriesPerServer ) throws ValidationException
		{
			check(esb, "ChangeSmtpParameters");
			ValidationException.min(0, retriesPerServer, "retriesPerServer");
			this.retriesPerServer = retriesPerServer;
		}

		public int getRetryServers()
		{
			return this.retryServers;
		}

		public void setRetryServers( int retryServers ) throws ValidationException
		{
			ValidationException.min(0, retryServers, "retryServers");
			this.retryServers = retryServers;
		}

		/////////////////////////////////////////////////////////

		@Config(hidden=true, description = "")
        public String describe(String extra)
        {   
            return String.format("%s@%s(smtpServers = %s%s%s)",
                this.getClass().getName(), Integer.toHexString(this.hashCode()),
				smtpServers,
                (extra.isEmpty() ? "" : ", "), extra);
        }

		@Config(hidden=true, description = "")
        public String describe()
        {   
            return this.describe("");
        }

		@Config(hidden=true, description = "")
        public String toString()
        {   
            return this.describe();
        }

		/////////////////////////////////////////////////////////

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return -7944368308790445508L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "SMTP Connector";
		}

		@Override
		public void validate() throws ValidationException
		{
			
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeSmtpNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewSmtpNotifications");
		}

        @Override
        public Collection<IConfiguration> getConfigurations()
        {
            Collection<IConfiguration> configs = new ArrayList<IConfiguration>();
            for (int i = 0; i < smtpServers.length; i++)
            {
                configs.add(smtpServers[i]);
            }
            return configs;
        }

        public ServerConfiguration[] gConfigurationsTypedArray()
        {
			return smtpServers;
        }
	}

	@Perms(perms = { @Perm(name = "ViewSmtpServerParameters", description = "View SMTP Server Parameters", category = "SMTP", supplier = true),
			@Perm(name = "ChangeSmtpServerParameters", implies = "ViewSmtpServerParameters", description = "Change SMTP Parameters", category = "SMTP", supplier = true) })
	public class ServerConfiguration extends ConfigurationBase
	{
		private int index = 0;
		private boolean debug = false;
		private boolean debugAuth = false;
		private boolean enabled = false;
		private String protocol = "smtps";
		private String username = "smtpuser";
		private String password = "password";
		private String host = "127.0.0.1";
		private int port = 25;
		private int connectionTimeout = 10000;
		private int timeout = 120000;
		private int writeTimeout = 120000;
		private String fromAddress = "c4u@concurrent.systems";
		private boolean authEnabled = false;
		private String authMechanisms = "";
		private String ntlmDomain = "workgroup";
		private long ntlmFlags = -1;
		private boolean sendPartial = true;
		private boolean saslEnabled = false;
		private String saslMechanisms = "";
		private String saslAuthorizationId = "";
		private String saslRealm = "";
		private boolean saslUseCanonicalHostname = false;
		private boolean sslEnabled = false;
		private boolean sslCheckServerIdentity = true;
		private String sslTrust = "";
		private String sslProtocols = "";
		private String sslCiphersuites = "";
		private boolean startTlsRequired = false;
		private boolean startTlsEnabled = false;
		private String socksHost = "";
		private int socksPort = -1;
		private String baseProperties = "";
		private String overrideProperties = "";
		private String maskProperties = "";

		//////////////////////////////////////////////////////////

		public boolean getDebug()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.debug;
		}
		public void setDebug( boolean debug )
		{
			check(esb, "ChangeSmtpServerParameters");
			this.debug = debug;
		}

		public boolean getDebugAuth()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.debugAuth;
		}
		public void setDebugAuth( boolean debugAuth )
		{
			check(esb, "ChangeSmtpServerParameters");
			this.debugAuth = debugAuth;
		}


		public boolean getEnabled()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.enabled;
		}

		public void setEnabled( boolean enabled ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.enabled = enabled;
		}

		@Config(description = "Protocol", comment = "smtp OR smtps")
		public String getProtocol()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.protocol;
		}

		public void setProtocol( String protocol ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			ValidationException.isOneOff(protocol, "protocol", "protocol must be smtp OR smtps", "smtp", "smtps");
			this.protocol = protocol;
		}

		public String getUsername()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.username;
		}

		public void setUsername( String username ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.username = username;
		}

		public String getPassword()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.password;
		}

		public void setPassword( String password ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.password = password;
		}

		public String getHost()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.host;
		}

		public void setHost( String host ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.host = host;
		}

		public int getPort()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.port;
		}

		public void setPort( int port ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			ValidationException.port(port, "port");
			this.port = port;
		}

		@Config(description = "Connection Timeout", comment = "milliseconds - 0 for infinite timeout")
		public int getConnectionTimeout()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.connectionTimeout;
		}
		public void setConnectionTimeout( int connectionTimeout ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			ValidationException.min(0, connectionTimeout, "connectionTimeout");
			this.connectionTimeout = connectionTimeout;
		}

		@Config(description = "Timeout", comment = "milliseconds - 0 for infinite timeout")
		public int getTimeout()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.timeout;
		}
		public void setTimeout( int timeout ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			ValidationException.min(0, timeout, "timeout");
			this.timeout = timeout;
		}

		@Config(description = "Write Timeout", comment = "milliseconds - 0 for infinite timeout")
		public int getWriteTimeout()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.writeTimeout;
		}
		public void setWriteTimeout( int writeTimeout ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			ValidationException.min(0, writeTimeout, "writeTimeout");
			this.writeTimeout = writeTimeout;
		}

		@Config(description = "From Address", comment = "leave blank for default")
		public String getFromAddress()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.fromAddress;
		}
		public void setFromAddress( String fromAddress ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.fromAddress = fromAddress;
		}

		public boolean getAuthEnabled()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.authEnabled;
		}
		public void setAuthEnabled( boolean authEnabled ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.authEnabled = authEnabled;
		}

		@Config(description = "Auth Mechanism", comment = "leave blank for default, space seperated list of LOGIN PLAIN DIGEST-MD5 NTLM XOAUTH2")
		public String getAuthMechanisms()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.authMechanisms;
		}
		public void setAuthMechanisms( String authMechanisms ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.authMechanisms = authMechanisms;
		}

		@Config(description = "NTLM Domain", comment = "leave blank for default")
		public String getNtlmDomain()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.ntlmDomain;
		}
		public void setNtlmDomain( String ntlmDomain ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.ntlmDomain = ntlmDomain;
		}

		@Config(description = "NTLM Flags", comment = "use -1 for default")
		public long getNtlmFlags()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.ntlmFlags;
		}
		public void setNtlmFlags( long ntlmFlags ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			ValidationException.inRange(-1, ntlmFlags, (long)Integer.MAX_VALUE, "ntlmFlags");
			this.ntlmFlags = ntlmFlags;
		}

		public boolean getSendPartial()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.sendPartial;
		}
		public void setSendPartial( boolean sendPartial ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.sendPartial = sendPartial;
		}

		@Config(description = "SASL Enabled")
		public boolean getSaslEnabled()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.saslEnabled;
		}
		public void setSaslEnabled( boolean saslEnabled ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.saslEnabled = saslEnabled;
		}

		@Config(description = "SASL Mechanisms", comment = "leave blank for default, space seperated list ...")
		public String getSaslMechanisms()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.saslMechanisms;
		}
		public void setSaslMechanisms( String saslMechanisms ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.saslMechanisms = saslMechanisms;
		}

		@Config(description = "SASL Authorization ID", comment = "leave blank for default")
		public String getSaslAuthorizationId()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.saslAuthorizationId;
		}
		public void setSaslAuthorizationId( String saslAuthorizationId ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.saslAuthorizationId = saslAuthorizationId;
		}

		@Config(description = "SASL Realm", comment = "leave blank for default")
		public String getSaslRealm()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.saslRealm;
		}
		public void setSaslRealm( String saslRealm ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.saslRealm = saslRealm;
		}

		@Config(description = "SASL Use Canonical Hostname", comment = "leave blank for default")
		public boolean getSaslUseCanonicalHostname()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.saslUseCanonicalHostname;
		}
		public void setSaslUseCanonicalHostname( boolean saslUseCanonicalHostname ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.saslUseCanonicalHostname = saslUseCanonicalHostname;
		}

		@Config(description = "SSL Enabled")
		public boolean getSslEnabled()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.sslEnabled;
		}
		public void setSslEnabled( boolean sslEnabled ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.sslEnabled = sslEnabled;
		}

		@Config(description = "SSL Protocols", comment = "leave blank for default, space seperated list")
		public String getSslProtocols()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.sslProtocols;
		}
		public void setSslProtocols( String sslProtocols ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.sslProtocols = sslProtocols;
		}

		@Config(description = "SSL Check Server Identity")
		public boolean getSslCheckServerIdentity()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.sslCheckServerIdentity;
		}
		public void setSslCheckServerIdentity( boolean sslCheckServerIdentity )
		{
			check(esb, "ChangeSmtpServerParameters");
			this.sslCheckServerIdentity = sslCheckServerIdentity;
		}

		@Config(description = "SSL Trust", comment = "leave blank for default, space seperated list, supports wildcards")
		public String getSslTrust()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.sslTrust;
		}
		public void setSslTrust( String sslTrust )
		{
			check(esb, "ChangeSmtpServerParameters");
			this.sslTrust = sslTrust;
		}


		@Config(description = "SSL Cipher Suites", comment = "leave blank for default, space seperated list")
		public String getSslCiphersuites()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.sslCiphersuites;
		}
		public void setSslCiphersuites( String sslCiphersuites ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.sslCiphersuites = sslCiphersuites;
		}

		@Config(description = "STARTTLS Enabled")
		public boolean getStartTlsEnabled()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.startTlsEnabled;
		}
		public void setStartTlsEnabled( boolean startTlsEnabled ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.startTlsEnabled = startTlsEnabled;
		}

		@Config(description = "STARTTLS Required")
		public boolean getStartTlsRequired()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.startTlsRequired;
		}
		public void setStartTlsRequired( boolean startTlsRequired ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.startTlsRequired = startTlsRequired;
		}

		@Config(description = "SOCSK Host", comment = "leave blank to disable")
		public String getSocksHost()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.socksHost;
		}
		public void setSocksHost( String socksHost ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.socksHost = socksHost;
		}

		@Config(description = "SOCKS Port", comment = "-1 for disabled")
		public int getSocksPort()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.socksPort;
		}
		public void setSocksPort( int socksPort ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.socksPort = socksPort;
		}

		@Config(description = "Base Properties", comment = "property=value;property=value;...")
		public String getBaseProperties()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.baseProperties;
		}
		public void setBaseProperties( String baseProperties ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.baseProperties = baseProperties;
		}

		@Config(description = "Override Properties", comment = "property=value;property=value;...")
		public String getOverrideProperties()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.overrideProperties;
		}
		public void setOverrideProperties( String overrideProperties ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.overrideProperties = overrideProperties;
		}

		@Config(description = "Mask Properties", comment = "property,property,property,")
		public String getMaskProperties()
		{
			check(esb, "ViewSmtpServerParameters");
			return this.maskProperties;
		}
		public void setMaskProperties( String maskProperties ) throws ValidationException
		{
			check(esb, "ChangeSmtpServerParameters");
			this.maskProperties = maskProperties;
		}


		//////////////////////////////////////////////////////////

		public ServerConfiguration(int index)
		{
			this.index = index;
		}

		public ServerConfiguration()
		{
		}
		
		//////////////////////////////////////////////////////////

        public String describe(String extra)
        {   
            return String.format("%s@%s(" +
				"index = '%s', enabled = '%s', protocol = '%s', username = '%s', password = '%s', host = '%s', port = '%s', " +
				"connectionTimeout = '%s', timeout = '%s', writeTimeout = '%s', fromAddress = '%s', " +
				"authEnabled = '%s', authMechanisms = '%s', ntlmDomain = '%s', ntlmFlags = '%s', " +
				"sendPartial = '%s', " +
				"saslEnabled = '%s', saslMechanisms = '%s', saslAuthorizationId = '%s', saslRealm = '%s', saslUseCanonicalHostname = '%s', " +
				"sslEnabled = '%s', sslProtocols = '%s', sslCheckServerIdentity = '%s', sslTrust = '%s', sslCiphersuites = '%s', " +
				"startTlsRequired = '%s', startTlsEnabled = '%s', " +
				"socksHost = '%s', socksPort = '%s'," +
				"baseProperties = '%s', overrideProperties = '%s', maskProperties = '%s'," +
				"%s%s)",
                this.getClass().getName(), Integer.toHexString(this.hashCode()),
				index, enabled, protocol, username, password, host, port,
				connectionTimeout, timeout, writeTimeout, fromAddress,
				authEnabled, authMechanisms, ntlmDomain, ntlmFlags,
				sendPartial,
				saslEnabled, saslMechanisms, saslAuthorizationId, saslRealm, saslUseCanonicalHostname,
				sslEnabled, sslProtocols, sslCheckServerIdentity, sslTrust, sslCiphersuites,
			
				startTlsRequired, startTlsEnabled,
				socksHost, socksPort,
				baseProperties, overrideProperties, maskProperties,
                (extra.isEmpty() ? "" : ", "), extra);
        }

        public String describe()
        {   
            return this.describe("");
        }

        public String toString()
        {   
            return this.describe();
        }

		public Properties asProperties()
		{
			Properties properties = new Properties();

			properties.put(String.format("mail.debug"), String.format("%s", debug));
			properties.put(String.format("mail.debug.auth"), String.format("%s", debugAuth));

			String prefix = String.format("mail.%s.",protocol);

			properties.put(String.format("%suser", prefix), String.format("%s", username));
			properties.put(String.format("%shost", prefix), String.format("%s", host));
			properties.put(String.format("%sport", prefix), String.format("%s", port));
			
			properties.put(String.format("%sconnectiontimeout", prefix), String.format("%s", connectionTimeout));
			properties.put(String.format("%stimeout", prefix), String.format("%s", timeout));
			properties.put(String.format("%swritetimeout", prefix), String.format("%s", writeTimeout));
			if (fromAddress.isEmpty() == false) properties.put(String.format("%sfrom", prefix), String.format("%s", fromAddress));

			properties.put(String.format("%sauth", prefix), String.format("%s", authEnabled));
			if (authMechanisms.isEmpty() == false) properties.put(String.format("%sauth.mechanisms", prefix), String.format("%s", authMechanisms));
			if (ntlmDomain.isEmpty() == false) properties.put(String.format("%sauth.ntlm.domain", prefix), String.format("%s", ntlmDomain));
			if (ntlmFlags != -1) properties.put(String.format("%sauth.ntlm.flags", prefix), String.format("%s", ntlmFlags));

			properties.put(String.format("%ssendpartial", prefix), String.format("%s", sendPartial));
			
			properties.put(String.format("%ssasl.enable", prefix), String.format("%s", saslEnabled));
			if (saslMechanisms.isEmpty() == false) properties.put(String.format("%ssasl.mechanisms", prefix), String.format("%s", saslMechanisms));
			if (saslAuthorizationId.isEmpty() == false) properties.put(String.format("%ssasl.authorizationid", prefix), String.format("%s", saslAuthorizationId));
			if (saslRealm.isEmpty() == false) properties.put(String.format("%ssasl.realm", prefix), String.format("%s", saslRealm));
			properties.put(String.format("%ssasl.usecanonicalhostname", prefix), String.format("%s", saslUseCanonicalHostname));

			properties.put(String.format("%sssl.enable", prefix), String.format("%s", sslEnabled));
			if (sslProtocols.isEmpty() == false) properties.put(String.format("%sssl.protocols", prefix), String.format("%s", sslProtocols));
			properties.put(String.format("%sssl.checkserveridentity", prefix), String.format("%s", sslCheckServerIdentity));
			if (sslTrust.isEmpty() == false) properties.put(String.format("%sssl.trust", prefix), String.format("%s", sslTrust));
			if (sslCiphersuites.isEmpty() == false) properties.put(String.format("%sssl.ciphersuites", prefix), String.format("%s", sslCiphersuites));

			properties.put(String.format("%sstarttls.required", prefix), String.format("%s", startTlsRequired));
			properties.put(String.format("%sstarttls.enable", prefix), String.format("%s", startTlsEnabled));

			if (socksHost.isEmpty() == false) properties.put(String.format("%ssocks.host", prefix), String.format("%s", socksHost));
			if (socksPort != -1) properties.put(String.format("%ssocks.port", prefix), String.format("%s", socksPort));

			return properties;
		}

		public Session asSession()
		{
			Properties properties = this.asProperties();
			Session session = Session.getInstance(properties, new DirectAuthenticator(this.getUsername(), this.getPassword()));
			LogOutputStream logOutputStream = new LogOutputStream() {
				@Override
				protected void processLine(String line, int level) {
					sessionLogger.debug(line);
				}
			};
			session.setDebugOut(new PrintStream(logOutputStream));
			return session;
		}

		//////////////////////////////////////////////////////////

		@Override
		public String getPath(String languageCode)
		{
			return "";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return 2075888220398034032L + this.index;
		}

		@Override
		public String getName(String languageCode)
		{
			return "SMTP Server";
		}

		@Override
		public void validate() throws ValidationException
		{
			
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeSmtpNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewSmtpNotifications");
		}
	}

	@Override
	public void initialise(IServiceBus esb)
	{
		this.esb = esb;
	}

	public void createConnections() throws Exception
	{
		logger.info("SmtpConnector.createConnections: Initializing connections ...");
		for ( ServerConfiguration serverConfiguration : this.config.gConfigurationsTypedArray() )
		{
			if ( serverConfiguration.getEnabled() )
			{
				SmtpConnection smtpConnection = new SmtpConnection( this.esb, this, this.config, serverConfiguration, this.historyQueue );
				logger.info("SmtpConnector.createConnections: Created connection {} with {}", this.connectionCount, serverConfiguration);
				this.connectionQueue.put(smtpConnection);
				this.connectionList.add(smtpConnection);
				this.connectionCount++;
			}
		}
		logger.info("SmtpConnector.createConnections: connectionQueue = {}, connectionList = {}", this.connectionQueue, this.connectionList);
	}

	public void destroyConnections() throws Exception
	{
		logger.info("SmtpConnector.destroyConnections: Destroying connections ...");
		while( this.connectionCount > 0 )
		{
			try( SmtpConnection smtpConnection = this.connectionQueue.take(); )
			{
				this.connectionList.remove(smtpConnection);
				this.connectionCount--;
			}
			logger.info("SmtpConnector.destroyConnections: Destroyed connection {}", this.connectionCount);
		}
		logger.info("SmtpConnector.destroyConnections: connectionQueue = {}, connectionList = {}", this.connectionQueue, this.connectionList);
	}

	@Override
	public boolean start(String[] args)
	{
		synchronized(startedMonitor)
		{
			try
			{
				if (started)
				{
					logger.info("SMTP Connector already started ...");
				}
				else
				{
					logger.info("SMTP Connector starting ...");
					this.createConnections();
				}
			}
			catch(Throwable throwable)
			{
				logger.error("SmtpConnector failed to start", throwable);
			}
			logger.info("SMTP Connector started succesfully ...");
			this.started = true;
			return true;
		}
	}

	@Override
	public void stop()
	{
		synchronized(startedMonitor)
		{
			try
			{
				if (started)
				{
					logger.info("SMTP Connector stopping ...");
					this.destroyConnections();
				}
				else
				{
					logger.info("SMTP Connector already started ...");
				}
			}
			catch(Throwable e)
			{
				logger.error(e.getMessage(), e);

			}
			logger.info("SMTP Connector stopped succesfully ...");
			this.started = false;
		}
	}

	@Override
	public Configuration getConfiguration()
	{
		return this.config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		logger.info("SmtpConnector.setConfiguration( {} )", config);
		synchronized(startedMonitor)
		{
			this.config = (Configuration) config;
			try
			{
				if (started)
				{
					logger.info("SmtpConnector.setConfiguration(): started == true -> recreating connections", config);
					this.destroyConnections();
					this.createConnections();
				}
				else
				{
					logger.info("SmtpConnector.setConfiguration(): started != true -> not recreating connections", config);
				}
			}
			catch( Throwable e )
			{
				logger.error(e.getMessage(), e);
			}
			logger.info("Updated configuration to {}", config);
		}
	}

	@Override
	public SmtpConnectionReference getConnection(String optionalConnectionString) throws IOException
	{
		try
		{
			return new SmtpConnectionReference(this, this.getConnectionActual(optionalConnectionString, null, null));
		}
		catch(InterruptedException interruptedException)
		{
			throw new IOException(String.format("Wrapped InterruptedException: %s", interruptedException.getMessage()), interruptedException);
		}
	}

	public SmtpConnectionReference getConnection(String optionalConnectionString, long timeout, TimeUnit timeUnit) throws IOException
	{
		try
		{
			return new SmtpConnectionReference(this, this.getConnectionActual(optionalConnectionString, timeout, timeUnit));
		}
		catch(InterruptedException interruptedException)
		{
			throw new IOException(String.format("Wrapped InterruptedException: %s", interruptedException.getMessage()), interruptedException);
		}
	}

	public SmtpConnectionReference getConnection(String optionalConnectionString, Long timeout, TimeUnit timeUnit) throws IOException
	{
		try
		{
			return new SmtpConnectionReference(this, this.getConnectionActual(optionalConnectionString, timeout, timeUnit));
		}
		catch(InterruptedException interruptedException)
		{
			throw new IOException(String.format("Wrapped InterruptedException: %s", interruptedException.getMessage()), interruptedException);
		}
	}

	private SmtpConnection getConnectionActual(String optionalConnectionString, long timeout, TimeUnit timeUnit) throws InterruptedException
	{
		return this.getConnectionActual(optionalConnectionString, timeout, timeUnit);
	}

	private SmtpConnection getConnectionActual(String optionalConnectionString, Long timeout, TimeUnit timeUnit) throws InterruptedException
	{
		logger.debug("SmtpConnector.getConnectionActual: timeout = {}, timeUnit = {}", timeout, timeUnit);
		if (timeout != null) return this.connectionQueue.poll(timeout, timeUnit);
		else return this.connectionQueue.take();
	}

	public void returnConnection(SmtpConnection connection)
	{
		for(int retry = 0; retry < 10; ++retry)
		{
			try
			{
				this.connectionQueue.put(connection);
				return;
			}	
			catch(Throwable e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public MimeMessage createMimeMessage()
	{
		MimeMessage msg = null;
		if (connectionList != null && connectionList.size() > 0)
		{
			msg = connectionList.get(0).createMimeMessage();
		}
		else
		{
			logger.warn("createMimeMessage:: connectionList is empty, cannot create message.");
		}
		return msg;
	}

	@Override
	public void send(Message message) throws Exception
	{
		this.send(message, null, null);
	}

	@Override
	public void send(Message message, long timeout, TimeUnit timeUnit) throws Exception
	{
		this.send(message, (Long) timeout, timeUnit);
	}

	public void send(Message message, Long timeout, TimeUnit timeUnit) throws Exception
	{
		int retries = config.getRetryServers();
		for ( int retry = 0; retry <= retries; ++retry )
		{
			logger.info("SmtpConnector.send:(retry = {}) sending message {}", retry, message);
			try(SmtpConnectionReference connectionReference = this.getConnection(null, timeout, timeUnit))
			{
				connectionReference.send(message);
				return;
			}
			catch(SendFailedException e)
			{
				logger.error(e.getMessage(), e);
				if (retry >= retries) throw e;
			}
			catch(MailConnectException e)
			{
				logger.error(e.getMessage(), e);
				if (retry >= retries) throw e;
			}
			catch(Throwable e)
			{
				logger.error(e.getMessage(), e);
				throw e;
			}
		}
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{	   
		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	@Override
	public ISmtpHistory[] getHistory()
	{
		ISmtpHistory[] result = this.historyQueue.toArray(new ISmtpHistory[0]);
		if ( result.length == 0 ) return result;
		logger.debug("SmtpConnector.getHistory: result[0] = {}", result[0]);
		return result;
	}

	@Override
	public void clearHistory()
	{
		logger.debug("SmtpConnector.clearHistory: clearning history ...");
		this.historyQueue.clear();
		logger.debug("SmtpConnector.clearHistory: cleared history ... size = {}", this.historyQueue.size());
	}
}
