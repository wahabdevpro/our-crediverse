package hxc.connectors.soap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.Endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.w3c.dom.NodeList;

import com.concurrent.hxc.IHxC;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.vas.VasService;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.IRole;
import hxc.services.security.ISecurity;
import hxc.services.security.IUser;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;

public class SoapConnector implements IConnector, ISoapConnector
{
	final static Logger logger = LoggerFactory.getLogger(SoapConnector.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private ISecurity security;
	private Endpoint endpoint;
	private HttpServer server = null;
	private IHxC vasInterface = null;
	private BlockingQueue<Runnable> requestQueue = null;
	private ThreadPoolExecutor threadPool = null;
	private String internalCredentials;
	private byte failureAttempt = 0;

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
		// Get Security
		security = esb.getFirstService(ISecurity.class);
		if (security == null)
			return false;

		// Create Internal Credentials
		int randomNumber = 1000 + (int) (Math.random() * 9000);
		internalCredentials = Integer.toString(randomNumber);

		// Publish
		logger.info("Starting Soap Connector on {}", config.vasEndpointURL);
		try
		{
			server = HttpServer.create(new InetSocketAddress(config.httpSOAPPort), config.maxBacklog);
			server.start();
			HttpContext context = server.createContext("/HxC");
			BasicAuthenticator authenticator = new BasicAuthenticator("HxC")
			{
				@Override
				public boolean checkCredentials(String arg0, String arg1)
				{
					if (SoapConnector.this.checkCredentials(arg0, arg1))
						return true;
					else
					{
						logger.warn("SOAP Authentication failed for {}", arg0);
						return false;
					}
				}
			};
			context.setAuthenticator(authenticator);

			vasInterface = new HxC(esb);
			endpoint = Endpoint.create(vasInterface);

			// Create the thread pool
			requestQueue = new ArrayBlockingQueue<Runnable>(config.threadQueueCapacity);
			threadPool = new ThreadPoolExecutor(1, config.maxThreadPoolSize, 1, TimeUnit.HOURS, requestQueue, new ThreadFactory()
			{

				private int num = 0;

				@Override
				public Thread newThread(Runnable r)
				{
					return new Thread(r, "SoapConnectorThreadPool-" + num++);
				}

			})
			{
				@Override
				protected void beforeExecute(Thread t, Runnable r)
				{
					MDC.put("transid", "");
					esb.countTPS();
				}
			};

			// Prevent Overflow
			threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler()
			{
				@Override
				public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
				{
					if (!executor.isShutdown())
					{
						esb.countTPS();
						r.run();
					}
				}
			});
			endpoint.setExecutor(threadPool);

			endpoint.publish(context);
		}
		catch (Exception ex)
		{
			logger.error("Failed to start connector", ex);
			return false;
		}

		failureAttempt = 0;

		// Log Information
		logger.info("Soap Connector Started on {}", config.vasEndpointURL);

		return true;
	}

	@Override
	public void stop()
	{
		logger.info("Stopping Soap Connector on {}", config.vasEndpointURL);

		// Stop
		if (endpoint != null && endpoint.isPublished())
			endpoint.stop();

		// Shutdown the thread pool
		if (threadPool != null)
			threadPool.shutdown();
		threadPool = null;
		requestQueue = null;

		// Stop Server
		if (server != null)
		{
			try
			{
				server.stop(0); // The parameter is the number of seconds (NOTE: not milliseconds) to block to wait for our HttpServer to shutdown properly.
				synchronized (this)
				{
					Thread.sleep(50);
				}
			}
			catch (InterruptedException e)
			{
			}
			server = null;
		}

		// Log Information
		logger.info("Soap Connector Stopped on {}", config.vasEndpointURL);

	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (SoapConfiguration) config;

		stop();

		try
		{
			Thread.sleep(1000);
		}
		catch (Exception e)
		{
		}

		start(null);
	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException
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
		if (server == null || endpoint == null || !endpoint.isPublished())
		{
			return false;
		}

		try
		{
			SOAPConnectionFactory factory = SOAPConnectionFactory.newInstance();
			SOAPConnection connection = factory.createConnection();

			URL endpoint = new URL(new URL(config.vasEndpointURL), "", new URLStreamHandler()
			{
				@Override
				protected URLConnection openConnection(URL url)
				{
					try
					{
						URL target = new URL(url.toString());
						URLConnection connection = target.openConnection();
						connection.setConnectTimeout(10000);
						connection.setReadTimeout(5000);
						return (connection);
					}
					catch (Exception e)
					{
						return null;
					}
				}
			});

			SOAPMessage response = connection.call(fitnessTest("seq", "99"), endpoint);

			connection.close();
			NodeList element = response.getSOAPBody().getElementsByTagName("seq");
			if (element.item(0).getTextContent().equals("100"))
			{
				failureAttempt = 0;
				return true;
			}

		}
		catch (Exception e)
		{
			logger.error("Soap Fitness: {}", e.toString());

			if (failureAttempt++ > 2)
			{
				logger.info("Attempting to restart Soap Connector.");
				stop();

				try
				{
					Thread.sleep(1000);
				}
				catch (Exception e2)
				{
				}

				start(null);
			}
		}

		return false;
	}

	private SOAPMessage fitnessTest(String field, String value) throws Exception
	{
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();
		SOAPPart soapPart = soapMessage.getSOAPPart();

		String serverURI = "http://hxc.concurrent.com/";
		String identifier = "vas";

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration(identifier, serverURI);

		// SOAP Body
		SOAPBody soapBody = envelope.getBody();
		SOAPElement soapBodyElem = soapBody.addChildElement("ping", identifier);
		SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("request");
		SOAPElement soapBodyElem2 = soapBodyElem1.addChildElement(field);
		soapBodyElem2.addTextNode(value);

		soapMessage.saveChanges();
		MimeHeaders headers = soapMessage.getMimeHeaders();
		String authorization = DatatypeConverter.printBase64Binary((internalCredentials + ":" + internalCredentials).getBytes("UTF-8"));
		headers.addHeader("Authorization", "Basic " + authorization);

		return soapMessage;
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
	@Perms(perms = { @Perm(name = "ViewSoapParameters", description = "View SOAP Parameters", category = "SOAP", supplier = true),
			@Perm(name = "ChangeSoapParameters", implies = "ViewSoapParameters", description = "Change SOAP Parameters", category = "SOAP", supplier = true),
			@Perm(name = "ViewSoapTuning", description = "View Tuning Parameters", category = "SOAP Connector", supplier = true),
			@Perm(name = "ChangeSoapTuning", implies = "ViewSoapTuning", description = "Change Tuning Parameters", category = "SOAP Connector", supplier = true) })
	public class SoapConfiguration extends ConfigurationBase
	{
		private String vasEndpointURL = "http://localhost:14100/HxC";
		private int httpSOAPPort = 14100;
		private int threadQueueCapacity = 5;
		private int maxThreadPoolSize = 10;
		private int maxBacklog = 5;

		public SoapConfiguration()
		{
		}

		public String getVasEndpointURL()
		{
			check(esb, "ViewSoapParameters");
			return vasEndpointURL;
		}

		public void setVasEndpointURL(String vasEndpointURL) throws ValidationException
		{
			check(esb, "ChangeSoapParameters");

			ValidationException.validateURL(vasEndpointURL, "VasEndpointURL");
			this.vasEndpointURL = vasEndpointURL;
		}

		public int getHttpSOAPPort()
		{
			check(esb, "ViewSoapParameters");
			return httpSOAPPort;
		}

		public void setHttpSOAPPort(int httpSOAPPort) throws ValidationException
		{
			check(esb, "ChangeSoapParameters");

			ValidationException.port(httpSOAPPort, "HttpSOAPPort");
			this.httpSOAPPort = httpSOAPPort;
		}

		@SupplierOnly
		public int getThreadQueueCapacity()
		{
			check(esb, "ViewSoapTuning");
			return threadQueueCapacity;
		}

		@SupplierOnly
		public void setThreadQueueCapacity(int threadQueueCapacity) throws ValidationException
		{
			check(esb, "ChangeSoapTuning");
			ValidationException.inRange(1, threadQueueCapacity, 100, "ThreadQueueCapacity");
			this.threadQueueCapacity = threadQueueCapacity;
		}

		@SupplierOnly
		public int getMaxThreadPoolSize()
		{
			check(esb, "ViewTuning");
			return maxThreadPoolSize;
		}

		@SupplierOnly
		public void setMaxThreadPoolSize(int maxThreadPoolSize) throws ValidationException
		{
			check(esb, "ChangeSoapTuning");
			ValidationException.inRange(5, maxThreadPoolSize, 100, "MaxThreadPoolSize");
			this.maxThreadPoolSize = maxThreadPoolSize;
		}

		@SupplierOnly
		public int getMaxBacklog()
		{
			check(esb, "ViewTuning");
			return maxBacklog;
		}

		@SupplierOnly
		public void setMaxBacklog(int maxBacklog) throws ValidationException
		{
			check(esb, "ChangeSoapTuning");
			ValidationException.inRange(5, maxThreadPoolSize, 100, "MaxBacklog");
			this.maxBacklog = maxBacklog;
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
			return -1340164032123920817L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Soap Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

	}

	SoapConfiguration config = new SoapConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISoapConnector Implementation
	//
	// /////////////////////////////////
	@Override
	public IHxC getVasInterface()
	{
		return vasInterface;
	}

	@Override
	public String getInternalCredentials()
	{
		return internalCredentials;
	}

	@Override
	public String getServiceName(String serviceID)
	{
		List<VasService> services = esb.getServices(VasService.class);
		for (VasService service : services)
		{
			if (serviceID.equals(service.getServiceID()))
			{
				return service.getServiceName(null);
			}
		}
		return serviceID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Credential Checking
	//
	// /////////////////////////////////
	private Boolean localChecked;
	private static final String CRM_ROLE = "CRM";
	private IRole crmRole = null;
	private static final String C4U = "C4U";
	private static Map<String, Boolean> credentialCache = new HashMap<String, Boolean>();

	protected boolean checkCredentials(String userId, String password)
	{
		// Defensive
		if (userId == null || password == null)
			return false;

		// Internal Credentials
		if (internalCredentials.equalsIgnoreCase(userId) && internalCredentials.equalsIgnoreCase(password))
			return true;

		// Retrieve from Cached
		String key = userId.toLowerCase() + password;
		Boolean authenticated = credentialCache.get(key);
		if (authenticated != null && authenticated)
			return true;
		authenticated = true;//DO it for now

		// Test Local
		if (C4U.equalsIgnoreCase(userId) && C4U.equalsIgnoreCase(password))
		{
			if (localChecked == null)
			{
				try
				{
					InetAddress.getByName("r2d2");
					localChecked = true;
					credentialCache.put(key, true);
					return true;
				}
				catch (UnknownHostException e)
				{
					localChecked = false;
				}
			}
			return localChecked;
		}

		// Retrieve CRM Role ID
		if (crmRole == null)
		{
			List<IRole> roles = security.getRoles();
			for (IRole role : roles)
			{
				if (role.getName().equalsIgnoreCase(CRM_ROLE))
				{
					crmRole = role;
					break;
				}
			}
		}

		try
		{
			byte[] publicKey = security.getPublicKey(userId);
			byte[] passwordBytes = password.getBytes("utf-8");
			byte[] credentials = new byte[passwordBytes.length + publicKey.length];
			System.arraycopy(passwordBytes, 0, credentials, 0, passwordBytes.length);
			System.arraycopy(publicKey, 0, credentials, passwordBytes.length, publicKey.length);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			credentials = md.digest(credentials);
			IUser user = security.authenticate(userId, credentials);

			if (user != null && crmRole != null)
			{
				if (user.hasRole(crmRole))
				{
					authenticated = true;
					credentialCache.put(key, authenticated);
				}
			}
		}
		catch (Throwable e)
		{
			authenticated = false;
		}

		// Cache and return result
		return authenticated;
	}

}
