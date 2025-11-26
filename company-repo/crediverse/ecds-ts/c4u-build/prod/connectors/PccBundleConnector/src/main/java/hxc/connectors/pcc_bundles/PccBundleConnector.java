package hxc.connectors.pcc_bundles;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Internal SUN API package required to set JAX-WS Timeouts, however using this results in a compiler error:
 * import com.sun.xml.internal.ws.client.BindingProviderProperties;
*/

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.bundles.IBundleInfo;
import hxc.connectors.bundles.IBundleProvider;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import systems.concurrent.ssim_api.CompoundServiceDefinitionId;
import systems.concurrent.ssim_api.CompoundServiceDefinitionIdSequence;
import systems.concurrent.ssim_api.DisallowedReason;
import systems.concurrent.ssim_api.FaultResponseException;
import systems.concurrent.ssim_api.GetServiceInstancesRequest;
import systems.concurrent.ssim_api.GetServiceInstancesResponse;
import systems.concurrent.ssim_api.GetServiceNamesRequest;
import systems.concurrent.ssim_api.GetServiceNamesResponse;
import systems.concurrent.ssim_api.Method;
import systems.concurrent.ssim_api.PartialCompoundServiceInstanceId;
import systems.concurrent.ssim_api.RequestHeader;
import systems.concurrent.ssim_api.ResponseHeader;
import systems.concurrent.ssim_api.ResponseStatus;
import systems.concurrent.ssim_api.ServiceInstance;
import systems.concurrent.ssim_api.SsimPort;
import systems.concurrent.ssim_api.SsimPortCompService;
import systems.concurrent.ssim_api.SubscribeRequest;
import systems.concurrent.ssim_api.Tag;
import systems.concurrent.ssim_api.Tags;




public class PccBundleConnector implements IConnector, IBundleProvider
{
	final static Logger logger = LoggerFactory.getLogger(PccBundleConnector.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private boolean initialized = false;

	private SsimPort ssim = null;

	private long counterTs = 0;
	private int counter = 0;

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
		logger.info("PCC Bundle Connector Starting ...");

		this.bindServiceToPccEndpoint(this.config);

		logger.info("PCC Bundle Connector Started");

		return true;
	}

	@Override
	public void stop()
	{
		// Log Information
		logger.info("PCC Bundle Connector Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.initialized = false;
		
		if ( !this.bindServiceToPccEndpoint((PccBundleConfiguration)config) )
		{
			logger.error("PCC Bundle Connector configuration update error");
			throw new ValidationException("Promotion Configuration failed to be set: failed to bind service endpoint");
		}
	}

	private boolean bindServiceToPccEndpoint(PccBundleConfiguration conf) 
	{
		boolean result = true;
		if ( this.initialized ) 
			return true;

		if ( conf != null )
		{
			if ( conf.getWsdlLocation() == null || conf.getEndpoint() == null )
			{
				this.ssim = null;
				
				this.initialized = true;

				logger.info("PCC Bundle Connector: connector disabled by configuration");

				return true;
			}	
		}
		else
			conf = this.config;

		try
		{
			boolean initializedNow = false;

			synchronized(this)
			{
				if ( !this.initialized )
				{
					logger.info("PCC Bundle Connector: WSDL: " + this.config.getWsdlLocation() + ", ENDPOINT: " + this.config.getEndpoint() + ", Connect Timeout: " + conf.getConnectionTimeout() + ", Read Timeout: " + conf.getReadTimeout());
					//Test that the URL is accessible before trying to create the JAX-WS service on an unavailable URL (to avoid long timeout).			
					if(pingURL(conf.getWsdlLocation(), conf.getConnectionTimeout(), conf.getReadTimeout()))
					{
						logger.info("PCC Bundle Connector: WSDL Accessible. Creating SsimPortCompService using: " + this.config.getWsdlLocation());
						SsimPortCompService newService = new SsimPortCompService(new URL(conf.getWsdlLocation()));			
						SsimPort newSsim = newService.getSsimPortCompPort();
						BindingProvider bindingProvider = (BindingProvider)newSsim;
						// FIXME: JAX-WS seems to require internal Sun API properties to set timeouts. Hard coded property strings are not ideal, however this seems to be the best solution: 
						bindingProvider.getRequestContext().put("com.sun.xml.internal.ws.request.timeout", conf.getReadTimeout()); // Timeout in millis
						bindingProvider.getRequestContext().put("com.sun.xml.internal.ws.connect.timeout", conf.getConnectionTimeout()); // Timeout in millis
						bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, conf.getEndpoint());						
						this.config = conf;
						this.ssim = newSsim;
					
						this.initialized = true;
						initializedNow = true;
					} else {
						logger.error("PCC Bundle Connector: WSDL [" + this.config.getWsdlLocation() + "] is not accessible.");
						result = false;
					}
				}	
			}		
				
			if ( initializedNow )	
				logger.info("PCC Bundle Connector: Endpoint successfully bound");
			else 
				logger.error("PCC Bundle Connector: Endpoint failed to bind");
		}	
		catch( MalformedURLException e )
		{
			logger.error("PCC Bundle Connector: MalformedURLException: Failed to bind endpoint: " + e.getMessage());
			return false;
		}
		catch( IOException e) //from pingURL
		{
			logger.error("PCC Bundle Connector: IOException: Failed to access remote server: " + e.getMessage());
			return false;
		}
		catch( Exception e )
		{
			logger.error("PCC Bundle Connector: Exception: Failed to bind endpoint: " + e.getMessage());
			return false;
		}

		return result;
	}
	
	private SsimPort getService() 
	{
		this.bindServiceToPccEndpoint(null);
		
		return this.ssim;
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
		// FIXME - Perform PCC Interface Fitness Check
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
	@Perms(perms = { @Perm(name = "ChangePccBundleParameters", implies = "ViewPccBundleParameters", description = "Change PCC Bundle Parameters", category = "PCC Bundle"),
			@Perm(name = "ViewPccBundleParameters", description = "View PCC Bundle Parameters", category = "PCC Bundle"), })
	public class PccBundleConfiguration extends ConfigurationBase
	{
		private boolean testMode = false;

		private String endpoint = null;//"http://localhost:8181/cxf/HelloWorld";
		private String wsdlLocation = null;//"http://localhost:8181/cxf/HelloWorld?wsdl";
		private String userID = "unknown";
		private String hostName = "ecds-host";
		private String systemType = "ECDS";
		private String channel = "API";
		private String agentName = "ECDS";
		private String chargingType = "deferred";
		private String provisionChargingType = "none";
		private Integer connectionTimeout = 3000;
		private Integer readTimeout = 15000;

		@SupplierOnly
		public boolean isTestMode()
		{
			check(esb, "ViewPccBundleParameters");
			return testMode;
		}

		@SupplierOnly
		public void setTestMode(boolean testMode)
		{
			check(esb, "ChangePccBundleParameters");
			this.testMode = testMode;
		}

		public String getEndpoint()
		{
			return endpoint;
		}

		public void setEndpoint(String endpoint)
		{
			if (endpoint != null && endpoint.length() == 0)
			{
				endpoint = null;
			}
			else
			{
				this.endpoint = endpoint;	
			}
		}

		public String getWsdlLocation()
		{
			return this.wsdlLocation;
		}

		public void setWsdlLocation(String wsdlLocation)
		{
			if (wsdlLocation != null && wsdlLocation.length() == 0)
			{
				this.wsdlLocation = null;
			}
			else
			{
				this.wsdlLocation = wsdlLocation;	
			}			
		}

		public String getUserID()
		{
			return this.userID;
		}

		public void setUserID(String userID)
		{
			this.userID = userID;
		}

		public String getHostName()
		{
			return this.hostName;
		}

		public void setHostName(String hostName)
		{
			this.hostName = hostName;
		}

		public String getSystemType()
		{
			return this.systemType;
		}

		public void setSystemType(String systemType)
		{
			this.systemType = systemType;
		}

		public String getChannel()
		{
			return this.channel;
		}

		public void setChannel(String channel)
		{
			this.channel = channel;
		}

		public String getAgentName()
		{
			return this.agentName;
		}

		public void setAgentName(String agentName)
		{
			this.agentName = agentName;
		}

		public String getChargingType()
		{
			return this.chargingType;
		}

		public void setChargingType(String chargingType)
		{
			this.chargingType = chargingType;
		}

		public String getProvisionChargingType()
		{
			return this.provisionChargingType;
		}

		public void setProvisionChargingType(String chargingType)
		{
			this.provisionChargingType = chargingType;
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
			return 4165795720478587280L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "PCC Bundle Connector";
		}

		@Override
		public void validate() throws ValidationException
		{
			if (endpoint != null) 
			{
				try
				{
					new URL(endpoint);
				} 
				catch (MalformedURLException e)
				{
					throw new ValidationException("'End Point' is not a valid URL. Leave field empty to ignore setting.");
				}
			}
			
			if (wsdlLocation != null) 
			{
				try
				{
					new URL(wsdlLocation);
				} 
				catch (MalformedURLException e)
				{
					throw new ValidationException("'Wsdl Location' is not a valid URL. Leave field empty to ignore setting.");
				}
			}
			

		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{

		}
		
		@Config(description = "Connect Timeout", comment = "milliseconds")
		public Integer getConnectionTimeout()
		{
			return connectionTimeout;
		}
		
		public void setConnectionTimeout(Integer connectionTimeout) throws ValidationException
		{
			check(esb, "ViewPccBundleParameters");
			ValidationException.min(0, connectionTimeout, "connectTimeout");
			this.connectionTimeout = connectionTimeout;
		}
		
		@Config(description = "Read Timeout", comment = "milliseconds")
		public Integer getReadTimeout()
		{
			return readTimeout;
		}
		
		public void setReadTimeout(Integer readTimeout) throws ValidationException
		{
			check(esb, "ViewPccBundleParameters");
			ValidationException.min(0, readTimeout, "readTimeout");
			this.readTimeout = readTimeout;
		}
	};

	PccBundleConfiguration config = new PccBundleConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Worker Threads
	//
	// /////////////////////////////////
			
	private static final Pattern pairExtractPattern = Pattern.compile("^(\\d+)\\s*[-:/]\\s*(.*)$");

	private RequestHeader buildRequestHeader( String extTransactionId )
	{
		Date now = new Date();
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(now);
		XMLGregorianCalendar xmlDate = null;
		try
		{
			xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
		}
		catch( DatatypeConfigurationException e )
		{
			logger.warn("PCC Bundle Connector: date conversion exception: " + e.getMessage());
			return null;
		}

		String transactionId = extTransactionId;
		if ( transactionId == null )
		{
			long ts = now.getTime() / 1000;
	
			long useCounterTs = 0;
			int useCounter = 0;
	
			synchronized(this)
			{
				if ( ts != this.counterTs )
				{
					this.counterTs = ts;
					this.counter = 0;
				}
	
				useCounterTs = this.counterTs;
				useCounter = this.counter;
				
				this.counter++;
			}
		
			transactionId = String.format( "%010d%04d", useCounterTs, useCounter );
		}

		RequestHeader rh = new RequestHeader();
		rh.setTransactionGroupId( transactionId );
		rh.setTransactionId( transactionId );
		rh.setDateTime( xmlDate );
		rh.setContextId( "" );
		rh.setUserId( this.config.getUserID() );
		rh.setSystemType( this.config.getSystemType() );
		rh.setHostName( this.config.getHostName() );
		rh.setChannel( this.config.getChannel() );

		return rh;
	}

	@Override
	public IBundleInfo[] getBundleInfo(int companyID)
	{
		// Testing purposes only
		if (config.testMode)
		{
			return new IBundleInfo[] { //
					new Bundle("PCC01_01", "Internet", "All Day", "Few Benefits"), //
					new Bundle("PCC01_02", "Internet", "Midnight", "Little Benefits"), //
					new Bundle("PCC01_03", "Internet", "Weekend", "No Benefits"), //
			};
		}

		SsimPort ssim = this.getService();
		if ( ssim == null )
		{
			logger.warn("PCC Bundle Connector: service endpoint not configured or not bound");
			return null;
		}

		try
		{
			GetServiceNamesRequest request = new GetServiceNamesRequest();
			Holder<ResponseHeader> responseHeader = new Holder<ResponseHeader>();
			GetServiceNamesResponse response = ssim.getServiceNames( request, buildRequestHeader( null ), responseHeader );
			
			if ( responseHeader.value.getStatus() != ResponseStatus.SUCCESS )
			{
				logger.warn("getBundleInfo FAILED: status: " + responseHeader.value.getStatus() + ", code: " + responseHeader.value.getCode() + ", msg: " + responseHeader.value.getMessage() );
				return null;
			}	
	
			CompoundServiceDefinitionIdSequence defIds = response.getDefinitionIds();
			List<CompoundServiceDefinitionId> ids = defIds.getDefinitionId();
			
			ArrayList<IBundleInfo> bundles = new ArrayList<IBundleInfo>();

			for (CompoundServiceDefinitionId item : ids)
			{
				int nsId = 0;
				int defId = 0;
				String tag = null;
				String type = null;
				String name = null;
				String description = null;

				Matcher ns = pairExtractPattern.matcher(item.getNamespace());
				if ( ns.find() )
				{	
					nsId = Integer.parseInt(ns.group(1)); 
					type = ns.group(2);
				}
				else
				{
					logger.warn("PCC namespace ID & Name format invalid [" + item.getNamespace() + "], must be in [ID - Name] format, skipping bundle.");
					continue;
				}
				Matcher def = pairExtractPattern.matcher(item.getDefinitionId());
				if ( def.find() )
				{	
					defId = Integer.parseInt(def.group(1)); 
					name = def.group(2);
					description = name;
				}
				else
				{
					logger.warn("PCC definition ID & Name format invalid [" + item.getDefinitionId() + "], must be in [ID - Name] format, skipping bundle.");
					continue;
				}

				tag = String.format( "PCC:%02d:%02d", nsId, defId );

				logger.info("PCC Bundle [" + tag + "|" + type + "|" + name  + "|" + description + "].");

				bundles.add( new Bundle( tag, type, name, description ) );
			}

			return bundles.toArray(new IBundleInfo[0]);
		} 
		catch( FaultResponseException e )
		{
			logger.info("getBundleInfo FAILED: " + e.getMessage());
		}

		return null;
	}

	@Override
	public StatusCode isEligible(String msisdn, String tag, String subscriberIMSI)
	{
		// Testing purposes only
		if (config.testMode)
		{
			return StatusCode.Success;
		}

		logger.info("isEligible request: MSISDN [" + msisdn + "] tag [" + tag + "]");

		SsimPort ssim = this.getService();
		if ( ssim == null )
		{
			logger.warn("PCC Bundle Connector: service endpoint not configured or not bound");
			return StatusCode.Failed;
		}

		try
		{
			String[] tagParts = tag.split(":");
			if ( tagParts.length != 3 )
			{
				logger.warn("isEligible FAILED: invalid tag [" + tag + "]");
				return StatusCode.Failed;
			}

			GetServiceInstancesRequest request = new GetServiceInstancesRequest();
			Tag chargingType = new Tag();
			chargingType.setNamespace("agentOptions");
			chargingType.setKey("chargingType");
			chargingType.setValue(config.getChargingType());
			Tags options = new Tags();
			options.getTag().add( chargingType );
			request.setOptions( options );

			request.setSubscriberId( msisdn );
			PartialCompoundServiceInstanceId bundleId = new PartialCompoundServiceInstanceId();
			bundleId.setNamespace( tagParts[1].replaceAll( "^0*", "" ) );
			bundleId.setDefinitionId( tagParts[2].replaceAll( "^0*", "" ) );
			request.setPartialInstanceId( bundleId );
			Holder<ResponseHeader> responseHeader = new Holder<ResponseHeader>();
			GetServiceInstancesResponse response = ssim.getServiceInstances( request, buildRequestHeader( null ), responseHeader );
			
			if ( responseHeader.value.getStatus() != ResponseStatus.SUCCESS )
			{
				logger.warn("isEligible FAILED: status: " + responseHeader.value.getStatus() + ", code: " + responseHeader.value.getCode() + ", msg: " + responseHeader.value.getMessage() );
				return StatusCode.Failed;
			}	
			
			List<ServiceInstance> instances = response.getServiceInstance();
			
			logger.trace("isEligible: SERVICE INSTANCES RETRIEVED FROM PCC MSISDN [" + msisdn + "] tag [" + tag + "]: " + instances.size() );
			for ( ServiceInstance instance : instances )
			{
				logger.trace("isEligible: service instance namespace [" + instance.getId().getNamespace() + "] definition [" + instance.getId().getDefinitionId() + "] instance [" + instance.getId().getInstanceId() + "]" );
				for ( Method method : instance.getMethods().getMethod() )
				{
					logger.trace("isEligible: method [" + method.getId().value() + "] is [" + ( method.getDisallowed() != null ? "disallowed" : "ALLOWED"  ) + "]" );
					if ( method.getDisallowed() != null )
					{
						for ( DisallowedReason reason : method.getDisallowed().getReasons().getReason() )
						{
							logger.trace("isEligible:     method [" + method.getId().value() + "] disallowed reason [" + reason.getCode() + ":" + reason.getMessage() + "]");
						}
					}
				}
			}
			
			if ( instances.size() < 1 )
			{
				logger.warn("isEligible: service instance not found for MSISDN [" + msisdn + "] tag [" + tag + "]");
				return StatusCode.Failed;
			}
			if ( instances.size() > 1 )
			{
				logger.warn("isEligible: multiple service instance found for MSISDN [" + msisdn + "] tag [" + tag + "]");
				return StatusCode.Failed;
			}
				
			ServiceInstance instance = instances.get(0);
			for ( Method method : instance.getMethods().getMethod() )
			{
				if ( !method.getId().value().equals("subscribe") ) continue;

				if ( method.getDisallowed() != null )
				{
					logger.warn("isEligible: MSISDN [" + msisdn + "] tag [" + tag + "] provision disallowed for [" + method.getDisallowed().getReasons().getReason().size() + "] reason(s).");
					for ( DisallowedReason reason : method.getDisallowed().getReasons().getReason() )
					{
						logger.warn("isEligible: MSISDN [" + msisdn + "] tag [" + tag + "] provision disallowed reason [" + reason.getCode() + ":" + reason.getMessage() + "]");
					}
					return StatusCode.NotEligible;
				}
					
				logger.info("isEligible: MSISDN [" + msisdn + "] is elegible for service [" + tag + "]");

				return StatusCode.Success;
			}
		} 
		catch( FaultResponseException e )
		{
			logger.warn("isEligible FAILED: " + e.getMessage());
		}

		return StatusCode.Failed;
	}

	@Override
	public StatusCode provision(String msisdn, String tag, String agentMSISDN, String transactionNo, String subscriberIMSI, BigDecimal price, 
		Integer agentCellId, String agentCellGroupCode, Integer subscriberCellId, String subscriberCellGroupCode)
	{
		logger.info("PCC Bundle Connector | provision ENTER");

		logger.info(String.format("PCC Bundle Connector | provision location info: %s / %s / %s / %s", agentCellId, agentCellGroupCode, subscriberCellId, subscriberCellGroupCode));

		// Testing purposes only
		if (config.testMode)
		{
			return mockTest(tag);
		}

		SsimPort ssim = this.getService();
		if ( ssim == null )
		{
			logger.warn("PCC Bundle Connector: service endpoint not configured or not bound");
			return StatusCode.Failed;
		}

		try
		{
			String[] tagParts = tag.split(":");
			if ( tagParts.length != 3 )
			{
				logger.warn("provision FAILED: invalid tag [" + tag + "]");
				return StatusCode.Failed;
			}
	
			SubscribeRequest request = new SubscribeRequest();
			Tag agentNameTag = new Tag();
			agentNameTag.setNamespace("agentOptions");
			agentNameTag.setKey("agentName");
			agentNameTag.setValue(config.getAgentName());
			Tag agentIdTag = new Tag();
			agentIdTag.setNamespace("agentOptions");
			agentIdTag.setKey("agentId");
			agentIdTag.setValue(agentMSISDN);
			Tag chargingType = new Tag();
			chargingType.setNamespace("agentOptions");
			chargingType.setKey("chargingType");
			chargingType.setValue(config.getProvisionChargingType());
			Tag beneficiaryImsi = new Tag();
			beneficiaryImsi.setNamespace("beneficiaryInfo");
			beneficiaryImsi.setKey("imsi");
			beneficiaryImsi.setValue(subscriberIMSI);
			Tags options = new Tags();
			options.getTag().add( agentNameTag );
			options.getTag().add( agentIdTag );
			options.getTag().add( chargingType );
			options.getTag().add( beneficiaryImsi );

			Tag subscriberCellIdTag = new Tag();
			subscriberCellIdTag.setNamespace("ecds");
			subscriberCellIdTag.setKey("subscriberCellID");
			subscriberCellIdTag.setValue(subscriberCellId != null ? subscriberCellId.toString() : "");
			options.getTag().add(subscriberCellIdTag);

			Tag subscriberCellGroupCodeTag = new Tag();
			subscriberCellGroupCodeTag.setNamespace("ecds");
			subscriberCellGroupCodeTag.setKey("subscriberCellGroupCode");
			subscriberCellGroupCodeTag.setValue(subscriberCellGroupCode != null ? subscriberCellGroupCode : "");
			options.getTag().add(subscriberCellGroupCodeTag);
			
			Tag agentCellIdTag = new Tag();
			agentCellIdTag.setNamespace("ecds");
			agentCellIdTag.setKey("agentCellID");
			agentCellIdTag.setValue(agentCellId != null ? agentCellId.toString() : "");
			options.getTag().add(agentCellIdTag);

			Tag agentCellGroupCodeTag = new Tag();
			agentCellGroupCodeTag.setNamespace("ecds");
			agentCellGroupCodeTag.setKey("agentCellGroupCode");
			agentCellGroupCodeTag.setValue(agentCellGroupCode != null ? agentCellGroupCode : "");
			options.getTag().add(agentCellGroupCodeTag);
			
			request.setOptions( options );

			request.setBenefitingSubscriberId( msisdn );
			request.setChargedSubscriberId( msisdn );
			CompoundServiceDefinitionId bundleId = new CompoundServiceDefinitionId();
			bundleId.setNamespace( tagParts[1].replaceAll( "^0*", "" ) );
			bundleId.setDefinitionId( tagParts[2].replaceAll( "^0*", "" ) );
			request.setServiceDefinitionId( bundleId );
			Holder<ResponseHeader> responseHeader = new Holder<ResponseHeader>();

			try
			{
				ssim.subscribe( request, buildRequestHeader( transactionNo ), responseHeader );

				switch( responseHeader.value.getStatus() )
				{
				case SUCCESS:
				case WARNING:
					break;

				case REJECTION:
				case UNCHANGED_STATE_FAILURE:
					logger.warn("provision FAILED (unchanged state): status: " + responseHeader.value.getStatus() + ", code: " + responseHeader.value.getCode() + ", msg: " + responseHeader.value.getMessage() );
					return StatusCode.Failed;

				case UNKNOWN_STATE_FAILURE:
				default:
					logger.warn("provision FAILED (UNKNOWN state): status: " + responseHeader.value.getStatus() + ", code: " + responseHeader.value.getCode() + ", msg: " + responseHeader.value.getMessage() );
					return StatusCode.Unknown;
				}
			}
			catch( Exception e )
			{
				// FIXME this may need some refinement to report things like connection or pre-connection errors toward PCC as Failed instead of Unknown
				logger.warn("provision FAILED (UNKNOWN state): exception: " + e.getMessage());
				return StatusCode.Unknown;
			}

			return StatusCode.Success;
		} 
		catch( Exception e )
		{
			logger.warn("provision FAILED (before API call): exception: " + e.getMessage());
		}

		return StatusCode.Failed;
	}

	@Override
	public StatusCode reverse(String msisdn, String tag, BigDecimal amount, String agentMSISDN, String transactionNo)
	{
		// Testing purposes only
		if (config.testMode)
		{
			return mockTest(tag);
		}

		return StatusCode.Failed;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	public StatusCode mockTest(String tag)
	{
		if (tag == null || tag.isEmpty())
			return StatusCode.Failed;
		else if (tag.endsWith("1"))
			return StatusCode.Success;
		else if (tag.endsWith("2"))
			return StatusCode.Unknown;
		else
			return StatusCode.Failed;
	}

	private class Bundle implements IBundleInfo
	{
		private String tag;
		private String name;
		private String type;
		private String description;

		@Override
		public String getTag()
		{
			return tag;
		}

        @SuppressWarnings("unused") // public, can be used externally
		public Bundle setTag(String tag)
		{
			this.tag = tag;
			return this;
		}

		@Override
		public String getName()
		{
			return name;
		}

        @SuppressWarnings("unused") // public, can be used externally
		public Bundle setName(String name)
		{
			this.name = name;
			return this;
		}

		@Override
		public String getType()
		{
			return type;
		}

        @SuppressWarnings("unused") // public, can be used externally
		public Bundle setType(String type)
		{
			this.type = type;
			return this;
		}

		public String getDescription()
		{
			return description;
		}

        @SuppressWarnings("unused") // public, can be used externally
		public Bundle setDescription(String description)
		{
			this.description = description;
			return this;
		}

		private Bundle()
		{

		}

		private Bundle(String tag, String type, String name, String description)
		{
			this.tag = tag;
			this.name = name;
			this.type = type;
			this.description = description;
		}

	}
	
	/**
	 * Pings a HTTP URL. This effectively sends a HEAD request and returns <code>true</code> if the response code is in 
	 * the 200-399 range.
	 * @param url The HTTP URL to be pinged.
	 * @param connectTimeout The connect timeout in millis.
	 * @param readTimeout The read timeout in millis.
	 * @return <code>true</code> if the given HTTP URL has returned response code 200-399 on a GET request within the
	 * given timeout, otherwise <code>false</code>.
	 * @throws IOException 
	 * @throws MalformedURLException
	 * Ref: https://stackoverflow.com/questions/3584210/preferred-java-way-to-ping-an-http-url-for-availability 
	 */
	public static boolean pingURL(String url, int connectTimeout, int readTimeout) throws MalformedURLException, IOException 
	{
	    url = url.replaceFirst("^https", "http"); // Otherwise an exception may be thrown on invalid SSL certificates.
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        return (200 <= responseCode && responseCode <= 399);
	}

}
