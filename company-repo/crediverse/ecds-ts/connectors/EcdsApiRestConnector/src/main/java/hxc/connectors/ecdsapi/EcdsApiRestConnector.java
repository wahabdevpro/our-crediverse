package hxc.connectors.ecdsapi;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.Rendering;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.ecdsapi.model.JsonWebToken;
import hxc.connectors.ecdsapi.model.OAuth2Response;
import hxc.connectors.ecdsapi.utils.RestProxy;
import hxc.ecds.protocol.rest.Transaction;
import hxc.ecds.protocol.rest.TransactionNotificationCallback;
import hxc.servicebus.IServiceBus;
//import hxc.services.ecds.interfaces.connectors.ecdsapi.IEcdsApiRestConnector;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;

public class EcdsApiRestConnector implements IConnector, IEcdsApiRestConnector, Runnable
{
	private final static Logger logger = LoggerFactory.getLogger(EcdsApiRestConnector.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private AccessTokenCache accessTokenCache = new AccessTokenCache();
	private Object cacheLock = new Object();
	private LinkedBlockingQueue<NotificationEvent> eventQueue = new LinkedBlockingQueue<NotificationEvent>();
	
	private Thread eventQueueDeamon;

	@Perms(perms = { @Perm(name = "ViewEcdsApiConnectorParameters", description = "View Cai Parameters", category = "Cai", supplier = true),
			@Perm(name = "ChangeEcdsApiConnectorParameters", implies = "ViewEcdsApiConnectorParameters", description = "Change Cai Parameters", category = "Cai", supplier = true) })
	public class EcdsApiConnectorConfiguration extends ConfigurationBase
	{
		private boolean enabled = false;
		private String clientId = "ecds-ts"; //should be sent by client
		private String clientSecret = "3fc3ad76ba19044c43ba498012a6f5b2"; //should be sent by client
		private int connectTimeout = 3000;
		private int readTimeout = 3000;
		private int connectionPoolSize = 20;
		private boolean expireConnections = true;
		private long connectionTTL = 270;

		@SupplierOnly
		public boolean isEnabled()
		{
			check(esb, "ChangeEcdsApiConnectorParameters");
			return enabled;
		}

		@SupplierOnly
		public void setEnabled(boolean enabled)
		{
			check(esb, "ChangeEcdsApiConnectorParameters");
			this.enabled = enabled;
		}
		
		@SupplierOnly
		@Config(description = "ECDS API Client ID")
		public String getClientId()
		{
			check(esb, "ViewEcdsApiConnectorParameters");
			return clientId;
		}

		@SupplierOnly
		public void setClientId(String clientId)
		{
			check(esb, "ChangeEcdsApiConnectorParameters");
			this.clientId = clientId;
		}
		
		@SupplierOnly
		@Config(description = "ECDS API Client Secret", renderAs = Rendering.PASSWORD)
		public String getClientSecret()
		{
			check(esb, "ViewEcdsApiConnectorParameters");
			return clientSecret;
		}

		@SupplierOnly
		public void setClientSecret(String clientSecret)
		{
			check(esb, "ChangeEcdsApiConnectorParameters");
			this.clientSecret = clientSecret;
		}
		
		@Config(description = "Connect Timeout", comment = "milliseconds")
		public int getConnectTimeout()
		{
			check(esb, "ViewEcdsApiConnectorParameters");
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout)
		{
			check(esb, "ChangeEcdsApiConnectorParameters");
			this.connectTimeout = connectTimeout;
		}

		@Config(description = "Read Timeout", comment = "milliseconds")
		public int getReadTimeout()
		{
			check(esb, "ViewEcdsApiConnectorParameters");
			return readTimeout;
		}

		public void setReadTimeout(int readTimeout)
		{
			check(esb, "ChangeEcdsApiConnectorParameters");
			this.readTimeout = readTimeout;
		}
				
		@SupplierOnly
		public int getConnectionPoolSize()
		{
			check(esb, "ViewEcdsApiConnectorParameters");
			return connectionPoolSize;
		}

		@SupplierOnly
		public void setConnectionPoolSize(int connectionPoolSize)
		{
			check(esb, "ChangeEcdsApiConnectorParameters");
			this.connectionPoolSize = connectionPoolSize;
		}
	
		@SupplierOnly
		@Config(description = "Enable Connection Lifespan")
		public boolean getExpireConnections()
		{
			check(esb, "ViewEcdsApiConnectorParameters");
			return expireConnections;
		}

		@SupplierOnly
		public void setExpireConnections(boolean expireConnections)
		{
			check(esb, "ChangeEcdsApiConnectorParameters");
			this.expireConnections = expireConnections;
		}
		
		@SupplierOnly
		@Config(description = "Connection Lifespan (Time to live)", comment = "seconds")
		public long getConnectionTTL()
		{
			check(esb, "ViewEcdsApiConnectorParameters");
			return connectionTTL;
		}

		@SupplierOnly
		public void setConnectionTTL(long connectionTTL)
		{
			check(esb, "ChangeEcdsApiConnectorParameters");
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
			return -25614386863591286L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "ECDS API Connector";
		}

		@Override
		public void validate() throws ValidationException
		{
		}
	};
	
	EcdsApiConnectorConfiguration config = new EcdsApiConnectorConfiguration();
	
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
		logger.info("Starting ECDS-API Rest Connector.");
		eventQueueDeamon = new Thread(this);  
		eventQueueDeamon.start();  
		return true;
	}

	@Override
	public void stop()
	{
		NotificationEvent poisonedPill = NotificationEvent.makePoisonPill();
		eventQueue.add(poisonedPill);
		try {
			eventQueueDeamon.join();
		} catch (InterruptedException e) {
			logger.error("Exception while joining Event Processor Deamon thread. {}", e);
		}
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException 
	{
		this.config = (EcdsApiConnectorConfiguration) config;
	}

	@Override
	public boolean canAssume(String serverRole) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFit() 
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public IMetric[] getMetrics() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException 
	{
		// TODO Auto-generated method stub
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IEcdsApiRestConnector
	//
	// /////////////////////////////////

	@Override
	public void notifyTransactions(String sessionID, String baseUri, String tokenUriPath, String callbackUriPath, List<? extends Transaction> transactions) throws Exception 
	{
		NotificationEvent event = new NotificationEvent();
		event.setSessionID(sessionID);
		event.setBaseUri(baseUri);
		event.setTokenUriPath(tokenUriPath);
		event.setCallbackUriPath(callbackUriPath);
		event.setTransactions(transactions);
		eventQueue.add(event);
		logger.debug("Added Transaction Notification Event to Queue. sessionID {}, baseUri {}, tokenUri {}, callbackUri {}, EventQueue size {}", sessionID, baseUri, tokenUriPath, callbackUriPath, eventQueue.size());
	}

	private void processEvent(NotificationEvent event) throws Exception
	{
		logger.info("Processing Transaction Notification Event, sessionID {}, baseUri {}, tokenUri {}, callbackUri {}", event.getSessionID(), event.getBaseUri(), event.getTokenUriPath(), event.getCallbackUriPath());
		String clientId = config.getClientId(); //This should be sent from client as per tokenUri and callbackUri
		String clientSecret = config.getClientSecret();		
		try(RestProxy proxy = new RestProxy(event.getBaseUri(), config.getConnectTimeout(), config.getReadTimeout(), false))
		{
			String accessToken;
			JsonWebToken jwt;
			Date tokenExpiryDate;
			synchronized(cacheLock)
			{
				if(accessTokenCache.isCached(event.getBaseUri() + event.getTokenUriPath()))
				{
					jwt = accessTokenCache.getCachedToken(event.getBaseUri() + event.getTokenUriPath());
					tokenExpiryDate = jwt.getPayload().getExp();
					logger.info("Reusing cached access_token for {}, expiry {}", event.getTokenUriPath(), tokenExpiryDate);
					accessToken = jwt.getAccessToken();
				} else {
					logger.info("Access Token requested for cliendId {}, tokenUriPath {}", clientId, event.getTokenUriPath());
					OAuth2Response authResponse = requestAccessToken(proxy, clientId, clientSecret, event.getTokenUriPath());
					accessToken = authResponse.getAccess_token();
					int expiresIn = authResponse.getExpires_in();
					jwt = new JsonWebToken(accessToken);
					accessTokenCache.putToken(event.getBaseUri() + event.getTokenUriPath(), jwt);
					tokenExpiryDate = jwt.getPayload().getExp();
					logger.info("Access Token received: clientId, tokenUriPath {}, expiry {}, expires in {}", clientId, event.getTokenUriPath(), tokenExpiryDate, expiresIn);
				}
			}
			TransactionNotificationCallback callbackRequest = new TransactionNotificationCallback();
			callbackRequest.setTransactions(event.getTransactions());
			callbackRequest.setSessionID(event.getSessionID());
			proxy.put(event.getCallbackUriPath(), accessToken, callbackRequest);
		}
	}

	static OAuth2Response requestAccessToken(RestProxy proxy, String clientID, String clientSecret, String tokenUriPath) throws IOException 
	{
		OAuth2Response authResponse = proxy.postClientCredentialsAuth(tokenUriPath, clientID, clientSecret, OAuth2Response.class);
		return authResponse;
	}

	@Override
	public void run() 
	{
		try {
			boolean on = true;
			while(on)
			{
				NotificationEvent event = eventQueue.take();
				on = !event.isPoisonedPill();
				if(on)
					processEvent(event);
				else
					logger.info("Transaction Notification Queue Deamon thread exiting.");
			}
		} catch (Exception e) {
			logger.error("Exception in Transaction Queue Deamon thread. {}", e);			
		}
	}
}
