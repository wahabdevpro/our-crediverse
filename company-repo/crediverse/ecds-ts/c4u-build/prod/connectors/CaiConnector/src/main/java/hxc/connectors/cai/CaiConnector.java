package hxc.connectors.cai;

import java.io.IOException;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.Rendering;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnector;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.numberplan.INumberPlan;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;

public class CaiConnector implements IConnector, ICaiConnector
{
	final static Logger logger = LoggerFactory.getLogger(CaiConnector.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private INumberPlan numberPlan;

	@Perms(perms = { @Perm(name = "ViewCaiParameters", description = "View Cai Parameters", category = "Cai", supplier = true),
			@Perm(name = "ChangeCaiParameters", implies = "ViewCaiParameters", description = "Change Cai Parameters", category = "Cai", supplier = true) })
	public class CaiConfiguration extends ConfigurationBase
	{
		private boolean enabled = false;
		private String server = "localhost";
		private int port = 3300;
		private int connectTimeout = 3000;
		private int readTimeout = 3000;
		private String user = "sogadm";
		private String password = "sogadm";
		private int connectionPoolSize = 20;
		private boolean expireConnections = true;
		private long connectionTTL = 270;

		@SupplierOnly
		public boolean isEnabled()
		{
			check(esb, "ChangeCaiParameters");
			return enabled;
		}

		@SupplierOnly
		public void setEnabled(boolean enabled)
		{
			check(esb, "ChangeCaiParameters");
			this.enabled = enabled;
		}
		
		@Config(description = "CAI Server Address")
		public String getServer()
		{
			check(esb, "ViewCaiParameters");
			return server;
		}

		public void setServer(String server)
		{
			check(esb, "ChangeCaiParameters");
			this.server = server;
		}
		
		@Config(description = "CAI Server Port")
		public int getPort()
		{
			check(esb, "ViewCaiParameters");
			return port;
		}

		public void setPort(int port)
		{
			check(esb, "ChangeCaiParameters");
			this.port = port;
		}

		@Config(description = "Connect Timeout", comment = "milliseconds")
		public int getConnectTimeout()
		{
			check(esb, "ViewCaiParameters");
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout)
		{
			check(esb, "ChangeCaiParameters");
			this.connectTimeout = connectTimeout;
		}

		@Config(description = "Read Timeout", comment = "milliseconds")
		public int getReadTimeout()
		{
			check(esb, "ViewCaiParameters");
			return readTimeout;
		}

		public void setReadTimeout(int readTimeout)
		{
			check(esb, "ChangeCaiParameters");
			this.readTimeout = readTimeout;
		}
				
		@SupplierOnly
		@Config(description = "Cai Authentication User Name")
		public String getUser()
		{
			check(esb, "ViewCaiParameters");
			return user;
		}

		@SupplierOnly
		public void setUser(String user)
		{
			check(esb, "ChangeCaiParameters");
			this.user = user;
		}

		@SupplierOnly
		@Config(description = "Cai Authentication Password", renderAs = Rendering.PASSWORD)
		public String getPassword()
		{
			check(esb, "ViewCaiParameters");
			return password;
		}

		@SupplierOnly
		public void setPassword(String password)
		{
			check(esb, "ChangeCaiParameters");
			this.password = password;
		}

		@SupplierOnly
		public int getConnectionPoolSize()
		{
			check(esb, "ViewCaiParameters");
			return connectionPoolSize;
		}

		@SupplierOnly
		public void setConnectionPoolSize(int connectionPoolSize)
		{
			check(esb, "ChangeCaiParameters");
			this.connectionPoolSize = connectionPoolSize;
		}
	
		@SupplierOnly
		@Config(description = "Enable Connection Lifespan")
		public boolean getExpireConnections()
		{
			check(esb, "ViewCaiParameters");
			return expireConnections;
		}

		@SupplierOnly
		public void setExpireConnections(boolean expireConnections)
		{
			check(esb, "ChangeCaiParameters");
			this.expireConnections = expireConnections;
		}
		
		@SupplierOnly
		@Config(description = "Connection Lifespan (Time to live)", comment = "seconds")
		public long getConnectionTTL()
		{
			check(esb, "ViewCaiParameters");
			return connectionTTL;
		}

		@SupplierOnly
		public void setConnectionTTL(long connectionTTL)
		{
			check(esb, "ChangeCaiParameters");
			this.connectionTTL = connectionTTL;
		}
		
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
			return -1424193388883590183L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Cai Connector";
		}

		@Override
		public void validate() throws ValidationException
		{
		}

	};
	
	CaiConfiguration config = new CaiConfiguration();
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConnector Implementation
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
		numberPlan = esb.getFirstService(INumberPlan.class);
		if (numberPlan == null)
			return false;
		
		logger.info("Cai Connector Started");

		return true;
	}

	@Override
	public void stop()
	{
		closePooledConnections();
		logger.info("Cai Connector Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (CaiConfiguration) config;
	}

	@Override
	public CaiConnection getConnection(String optionalConnectionString) throws IOException
	{
		CaiConnection result = null;
		try
		{
			result = getPooledConnection();
			if (result == null || result.getConnection().isClosed())
			{				
				result = new CaiConnection(this, config.getServer(), config.getPort(), config.getConnectTimeout(), config.getReadTimeout(), config.getExpireConnections(), config.getConnectionTTL());
			} else {
				logger.debug("Obtained an existing and already connected CAI connection from pool.");
			}
		}
		catch (IOException e)
		{
			String message = e.getMessage();
			if (e.getCause() != null)
			{
				message = message.replaceAll("\n", "\t");
				message += String.format("Cai connection failure cause: %s", e.getCause().getMessage());
			}			
			throw new IOException(message, e);
		}
		return result;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return true;
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Connection Pooling
	//
	// /////////////////////////////////
	private Stack<CaiConnection> connectionPool = new Stack<CaiConnection>();
	private static Object poolSync = new Object();

	void returnConnection(CaiConnection connection) throws IOException
	{
		if(connection.isConnectionStale())
		{
			connection.getConnection().close();			
			return;
		}
		synchronized (poolSync)
		{				
			if (connectionPool.size() < config.connectionPoolSize)
			{
				connectionPool.push(connection);
				return;
			}
		}		
	}

	private CaiConnection getPooledConnection() throws IOException
	{
		long connectionTTL = config.getConnectionTTL();
		synchronized (poolSync)
		{
			if (connectionPool.size() == 0)
				return null;
			CaiConnection connection = null;
			do
			{
				connection = connectionPool.pop();
				if(connection.isConnectionStale())
				{					
					connection.getConnection().close();
				}
			} while(connectionPool.size() > 0 && connection.isConnectionStale());
			if(connection.isConnectionStale())
			{
				logger.trace("All CaiConnections in the pool are older than {} seconds therefore are deemed to be stale.", connectionTTL);
				return null;
			}
			return connection;
		}
	}

	private void closePooledConnections()
	{
		while (true)
		{
			try
			{
				CaiConnection connection = getPooledConnection();
				if (connection == null)
					return;
				connection.getConnection().close();
			}
			catch (IOException e) 
			{
				logger.error("Failed to close pooled connections", e);
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICaiConnector
	//
	// /////////////////////////////////
	@Override
	public String getImei(String msisdn)  
	{
		if(!config.isEnabled())
		{
			logger.trace("CAI Interface is not enabled. Skipping IMEI request.");
			return null;
		}
		String imei = new String();
		 
		try
		{			
			CaiConnection connection = getConnection("");
			if(!connection.isLoggedIn())
			{
				connection.login(config.getUser(), config.getPassword());
			}
			if(connection.isLoggedIn())
			{
				String intMsisdn = numberPlan.getInternationalFormat(msisdn);
				imei = connection.getImei(intMsisdn);
			}
			returnConnection(connection);
		} catch(IOException e) {
			logger.info("IOException occurred while trying to connect to CAI: [{}]", e.toString());
		}
		return imei;
	}
}
