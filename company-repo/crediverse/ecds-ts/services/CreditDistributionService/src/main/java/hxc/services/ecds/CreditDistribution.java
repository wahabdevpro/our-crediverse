package hxc.services.ecds;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.DSAPrivateKeySpec;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;
import javax.persistence.metamodel.EntityType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509v3CertificateBuilder;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.DSAKeyPairGenerator;
import org.bouncycastle.crypto.generators.DSAParametersGenerator;
import org.bouncycastle.crypto.params.DSAKeyGenerationParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
//import org.bouncycastle.crypto.params.DSAValidationParameters;
import org.bouncycastle.crypto.util.DigestFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcDSAContentSignerBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IInteraction;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.bundles.IBundleProvider;
import hxc.connectors.cai.ICaiConnector;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.ecdsapi.IEcdsApiRestConnector;
import hxc.connectors.hlr.IHlrConnector;
import hxc.connectors.hlr.IHlrInformation;
import hxc.connectors.kerberos.IAuthenticator;
import hxc.connectors.kerberos.IAuthenticator.Result;
import hxc.connectors.sms.ISmsConnector;
import hxc.connectors.smtp.ISmtpConnector;
import hxc.connectors.snmp.ISnmpConnector;
import hxc.ecds.protocol.rest.RegisterTransactionNotificationRequest;
import hxc.ecds.protocol.rest.WebUser;
import hxc.ecds.utils.encrypt.AesUtils;
import hxc.servicebus.ILocale;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.Trigger;
import hxc.services.IService;
import hxc.services.ecds.grizzly.utils.GrizzlyConnectionSnapshot;
import hxc.services.ecds.grizzly.utils.GrizzlyMonitorLogger;
import hxc.services.ecds.grizzly.utils.GrizzlyThreadPoolSnapshot;
import hxc.services.ecds.grizzly.utils.HuxConnectionProbe;
import hxc.services.ecds.grizzly.utils.HuxThreadPoolProbe;
//import hxc.services.ecds.interfaces.connectors.ecdsapi.IEcdsApiRestConnector;
import hxc.services.ecds.model.Company;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Role;
import hxc.services.ecds.model.State;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.olapmodel.OlapSchemaData;
import hxc.services.ecds.rest.Authentication;
import hxc.services.ecds.rest.IChannelTarget;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.RestExtenders;
import hxc.services.ecds.rest.tdr.TdrWriter;
import hxc.services.ecds.rewards.RewardProcessor;
import hxc.services.ecds.util.DbUtils;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.ILocaleCustomisationConfig;
import hxc.services.ecds.util.LocaleCustomisation;
import hxc.services.ecds.util.LocaleCustomisationMap;
import hxc.services.ecds.util.LocaleKey;
import hxc.services.ecds.util.PooledDataSourceUtils;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RequestNumberFilter;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.logging.LoggingConstants;
import hxc.services.notification.INotificationText;
import hxc.services.notification.INotifications;
import hxc.services.notification.Phrase;
import hxc.services.numberplan.INumberPlan;
import hxc.services.security.ISecurity;
import hxc.services.security.IUser;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.http.HttpClient;
import hxc.utils.http.HttpConnection;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notifications;

public class CreditDistribution implements IService, ICreditDistribution
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final int TS_NUMBER_LENGTH = 18;
	private static final String oltpConfigFileName = "database-settings-oltp.xml";
	private static final String olapConfigFileName = "database-settings-olap.xml";
	private static final String oltpDatasourceName = "ecdsOltp";
	private static final String olapDatasourceName = "ecdsOlap";
	private static final String oltpPersistenceUnit = "ecds";
	private static final String olapPersistenceUnit = "ecdsap";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	protected IServiceBus esb;
	protected IService service = this;
	protected ISnmpConnector snmpConnector;
	
	protected ILocale locale;
	protected IAirConnector air;
	protected IAuthenticator authenticator;
	protected IBundleProvider bundleProvider;
	protected IHlrConnector hlr;
	protected IEcdsApiRestConnector ecdsApiConnector;
	protected ISecurity security = null;
	protected ICtrlConnector control = null;
	protected HttpServer restServer = null;
	protected Object sessionLock;
	protected Sessions sessions;
	protected TdrWriter tdrWriter;
	protected OlapLoader olapLoader;
	protected INumberPlan numberPlan;
	protected ICaiConnector cai;
	protected LocaleCustomisationMap localeCustomisationMap;

	// @PersistenceUnit(unitName = "ecds")
	private EntityManagerFactory oltpEntityManagerFactory;
	private PooledDataSource oltpPooledDataSource;
	// @PersistenceUnit(unitName = "ecdsap")
	private EntityManagerFactory olapEntityManagerFactory;
	private PooledDataSource olapPooledDataSource;
	
	private static Map<String, String> forceDatabase; // used for unit tests

	protected ScheduledFuture<?> sessionCleanupTask = null;
	protected ScheduledFuture<?> tdrRotatorTask = null;
	protected ScheduledFuture<?> olapLoaderTask = null;
	protected List<ScheduledAccountDumpProcessor> scheduledAccountDumpProcessors;
	protected List<RewardProcessor> rewardProcessors;
	//protected List<DataCleanoutProcessor> dataCleanoutProcessors;
	protected List<OlapAnalyticsHistoryProcessor> olapAnalyticsHistoryProcessor;
	protected List<OlapCleaner> olapCleaners;
	protected List<OltpCleaner> oltpCleaners;
	protected List<OlapSync> olapSyncs;
	protected List<ReportScheduleExecutor> reportScheduleExecutors;
	protected ConcurrentHashMap<Integer, HashSet<ICallbackItem>> agentNotification = new ConcurrentHashMap<Integer, HashSet<ICallbackItem>>();
	protected Object agentNotificationLock = new Object();
	protected ISmsConnector smsConnector;
	protected ISmtpConnector smtpConnector;
	protected ChannelManager channelManager = new ChannelManager();
	private ConcurrentMap<Integer, CompanyInfo> companyMap = new ConcurrentHashMap<Integer, CompanyInfo>();
	private Set<Class<?>> restClasses;
	private final GrizzlyThreadPoolSnapshot threadPoolSnapshot = new GrizzlyThreadPoolSnapshot();
	private final GrizzlyConnectionSnapshot connectionSnapshot = new GrizzlyConnectionSnapshot();
	//Hack: Ideally this should be a JMX Managed Bean rather than a timed thread logging statistical data...
	private final GrizzlyMonitorLogger serverMonitor = new GrizzlyMonitorLogger(this.threadPoolSnapshot, this.connectionSnapshot);
	
	final static Logger logger = LoggerFactory.getLogger(CreditDistribution.class);
	final static java.util.logging.Logger localLogger = java.util.logging.Logger.getLogger(CreditDistribution.class.getName());

	private Semaphore querySemaphores;

	
	
	public static void overrideDb(Map<String, String> config)
	{
		forceDatabase = config;
	}
	
	

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	{
		if ( Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null )
		{
			Security.addProvider(new BouncyCastleProvider());
		}
	}

	 public CreditDistribution()
	 {
		 querySemaphores = new Semaphore(config.maxSimultaneousQueries, false);
	 }
		
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IService Implementation
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
		// Must have SMS Connector
		smsConnector = esb.getFirstConnector(ISmsConnector.class);
		if (smsConnector == null)
		{
			logger.error("Failed to get ISmsConnector.class");
			return false;
		}

		smtpConnector = esb.getFirstConnector(ISmtpConnector.class);
		if (smtpConnector == null)
		{
			logger.error("Failed to get ISmtpConnector.class");
			return false;
		}

		snmpConnector = esb.getFirstConnector(ISnmpConnector.class);
		if (snmpConnector == null)
		{
			logger.error("Failed to get ISnmpConnector.class");
			return false;
		}

		// Must have Air
		air = esb.getFirstConnector(IAirConnector.class);
		if (air == null)
		{
			logger.error("Failed to get IAirConnector.class");
			return false;
		}

		// Must have an Authenticator Connector
		authenticator = esb.getFirstConnector(IAuthenticator.class);
		if (authenticator == null)
		{
			logger.error("Failed to get IAuthenticator.class");
			return false;
		}

		// Must have a Bundle Provider
		bundleProvider = esb.getFirstConnector(IBundleProvider.class);
		if (bundleProvider == null)
		{
			logger.error("Failed to get IBundleProvider.class");
			return false;
		}

		// Must have HLR
		hlr = esb.getFirstConnector(IHlrConnector.class);
		if (hlr == null)
		{
			logger.error("Failed to get IHlrConnector.class");
			return false;
		}

		// Must have Cai
		cai = esb.getFirstConnector(ICaiConnector.class);
		if (cai == null)
		{
			logger.error("Failed to get ICaiConnector.class");
			return false;
		}

		ecdsApiConnector = esb.getFirstConnector(IEcdsApiRestConnector.class);
		if(ecdsApiConnector == null)
		{
			logger.error("Failed to get IEcdsApiRestConnector.class");
			return false;
		}

		// Must have Number Plan
		numberPlan = esb.getFirstService(INumberPlan.class);
		if (numberPlan == null)
		{
			logger.error("Failed to get INumberPlan.class");
			return false;
		}

		security = esb.getFirstService(ISecurity.class);
		if (security == null)
		{
			logger.error("Failed to get ISecurity.class");
			return false;
		}

		control = esb.getFirstConnector(ICtrlConnector.class);
		if (security == null)
		{
			logger.info("ICtrlConnector not yet available");
			return false;
		}

		logger.info("Starting Credit Distribution service...");

		// Get Locale
		this.locale = esb.getLocale();

		this.localeCustomisationMap = (this.config.localeCustomisationArray == null ? new LocaleCustomisationMap() : LocaleCustomisationMap.valueOf(this.config.localeCustomisationArray));

		// Verify the database Version
		if (!State.verifyDatabaseVersion(this))
		{
			logger.error("Failed to verify OLTP database version ...");
			return false;
		}

		if (!OlapSchemaData.verifyDatabaseVersion(this))
		{
			logger.error("Failed to verify OLAP database version ...");
			return false;
		}

		// Create the Session Cache
		sessions = new Sessions();
		sessionCleanupTask = esb.getScheduledThreadPool().scheduleAtFixedRate(sessions, 1L, 60L, TimeUnit.SECONDS);

		// Create the TDR writer
		tdrWriter = new TdrWriter(this);
		tdrRotatorTask = esb.getScheduledThreadPool().scheduleAtFixedRate(tdrWriter, 10L, 60L, TimeUnit.SECONDS);

		// Create JPA Entity Manager Factory
		try
		{
			logger.info("Starting JPA/Hibernate...");

			logger.trace("Creating Entity Manager Factory for OLTP");
			this.oltpEntityManagerFactory = createOltpEntityManagerFactory(oltpPersistenceUnit, oltpDatasourceName);
			this.oltpPooledDataSource = C3P0Registry.pooledDataSourceByName(oltpDatasourceName);
			
			logger.trace("Creating Entity Manager Factory for OLAP");
			this.olapEntityManagerFactory = createOlapEntityManagerFactory(olapPersistenceUnit, olapDatasourceName);
			this.olapPooledDataSource = C3P0Registry.pooledDataSourceByName(olapDatasourceName);
			

			logger.info("oltpPooledDataSource = {}", oltpPooledDataSource);
			logger.info("olapPooledDataSource = {}", olapPooledDataSource);
	
			Objects.requireNonNull(oltpPooledDataSource, "oltpPooledDataSource may not be null");
			Objects.requireNonNull(olapPooledDataSource, "olapPooledDataSource may not be null");

			// Load MRD
			try (EntityManagerEx em = getEntityManager(); EntityManagerEx emAp = getApEntityManager())
			{
				logger.debug("Doing LoadMRD for OLTP && OLAP DB");
				if (!loadMRD(em, emAp))
					logger.error("LoadMRD failed");
			}
		}
		catch (Throwable tr)
		{
			logger.error("Unable to load MRD data", tr);
			return false;
		}

		Set<PooledDataSource> pooledDataSources = C3P0Registry.getPooledDataSources();
		logger.info("pooledDataSources = {}", pooledDataSources);

		// NOTE: THE CODE BELOW ASSUMES THAT companyMap is populated ... this does not happen explicitly but happens as a consequence of MRD loading... so don't remove MRD loading without adding
		// something else ...

		if (args == null || args.length <= 0 || !Arrays.asList(args).contains("ecds.mode.unitTest.olap"))
		{
			logger.info("Starting OLAP worker ...");
			try
			{
				olapLoader = new OlapLoader(esb, this, this);
				olapLoaderTask = esb.getScheduledThreadPool().scheduleAtFixedRate(olapLoader, 1L, (long) config.getApLoaderInterval(), TimeUnit.SECONDS);
				this.olapCleaners = new ArrayList<OlapCleaner>();
				for (CompanyInfo company : companyMap.values())
				{
					OlapCleaner olapCleaner = new OlapCleaner(esb, this, this, company);
					olapCleaner.start();
					this.olapCleaners.add(olapCleaner);
				}
				this.olapSyncs = new ArrayList<OlapSync>();
				for (CompanyInfo company : companyMap.values())
				{
					OlapSync olapSync = new OlapSync(esb, this, this, company);
					olapSync.start();
					this.olapSyncs.add(olapSync);
				}
			}
			catch (Exception exception)
			{
				logger.error("Failed to start OLAP worker", exception);
				return false;
			}
		}
		else
		{
			String msg = "Got ecds.mode.unitTest.olap ... not starting OLAP jobs ...";
			logger.error(msg);
		}

		if (args == null || args.length <= 0 || !Arrays.asList(args).contains("ecds.mode.unitTest.oltp"))
		{
			logger.info("Starting OLTP workers ...");
			try
			{
				this.oltpCleaners = new ArrayList<OltpCleaner>();
				for (CompanyInfo company : companyMap.values())
				{
					OltpCleaner oltpCleaner = new OltpCleaner(esb, this, this, company);
					oltpCleaner.start();
					this.oltpCleaners.add(oltpCleaner);
				}
			}
			catch (Exception exception)
			{
				logger.error("Failed to start OLTP worker", exception);
				return false;
			}
		}
		else
		{
			String msg = "Got ecds.mode.unitTest.oltp ... not starting OLTP jobs ...";
			logger.error(msg);
		}

		if (args == null || args.length <= 0 || !Arrays.asList(args).contains("ecds.mode.unitTest.reportScheduleExecutor"))
		{
			logger.info("Starting report schedule executor ...");
			try
			{
				this.reportScheduleExecutors = new ArrayList<ReportScheduleExecutor>();
				for (CompanyInfo company : companyMap.values())
				{
					ReportScheduleExecutor reportScheduleExecutor = new ReportScheduleExecutor(this, company);
					reportScheduleExecutor.start();
					this.reportScheduleExecutors.add(reportScheduleExecutor);
				}

			}
			catch (Exception exception)
			{
				logger.error("Unable to start report schedule executor", exception);
				return false;
			}
		}
		else
		{
			String msg = "Got ecds.mode.unitTest.olap ... not starting OLAP jobs ...";
			logger.error(msg);
		}

		// Start RESTful Server
		boolean secure = false;
		try
		{
			logger.info("Starting REST server ...");
			secure = startRestfulServer();

		}
		catch (Throwable tr)
		{
			logger.error("Starting RESTful Failed on {} with error: {}", config.getRestURL(secure), tr.getMessage());
			logger.error("Starting RESTful Failed", tr);
			return false;
		}

		// Initialise channelFilters
		logger.info("Initializing channel filters ...");
		initialiseChannelFilters();

		// Create an SMS/USSD Trigger
		Trigger<IInteraction> smsTrigger = new Trigger<IInteraction>(IInteraction.class)
		{
			@Override
			public boolean testCondition(IInteraction interaction)
			{
				return channelManager.testCondition(interaction);
			}

			@Override
			public void action(IInteraction interaction, IConnection connection)
			{
				assignTsNumber(true);
				channelManager.execute(interaction, locale);
			}
		};
		esb.addTrigger(smsTrigger);

		/*
		logger.info("Creating Data Cleanout Processors ...");
		// Create Data Cleanout Processors
		dataCleanoutProcessors = new ArrayList<DataCleanoutProcessor>();
		for (CompanyInfo company : companyMap.values())
		{
			DataCleanoutProcessor processor = new DataCleanoutProcessor(this, company);
			dataCleanoutProcessors.add(processor);
			processor.start(esb.getScheduledThreadPool());

		}
		*/
		

		logger.info("Creating OlapAnalyticsHistory Processor ...");
		// Create OlapAnalyticsHistory Processor
		olapAnalyticsHistoryProcessor = new ArrayList<OlapAnalyticsHistoryProcessor>();
		for (CompanyInfo company : companyMap.values())
		{
			if ( company.getCompany().getId() == this.config.analyticsConfigurationCompanyID )
			{
				OlapAnalyticsHistoryProcessor processor = new OlapAnalyticsHistoryProcessor(esb, this, this, company);
				olapAnalyticsHistoryProcessor.add(processor);
				processor.start();
			}
			else
			{
				logger.info("OlapAnalyticsHistory Processor: Not creating scheduler for company {}", company.getCompany().getId());
			}
		}

		// Create Scheduled Account Dump Processors
		logger.info("Creating Scheduled Account Dump Processors ...");
		scheduledAccountDumpProcessors = new ArrayList<ScheduledAccountDumpProcessor>();
		for (CompanyInfo company : companyMap.values())
		{
			ScheduledAccountDumpProcessor processor = new ScheduledAccountDumpProcessor(this, company);
			scheduledAccountDumpProcessors.add(processor);
			processor.start(esb.getScheduledThreadPool());
		}

		// Create Reward Processors
		logger.info("Creating Reward Processors ...");
		rewardProcessors = new ArrayList<RewardProcessor>();
		for (CompanyInfo company : companyMap.values())
		{
			RewardProcessor processor = new RewardProcessor(this, company);
			rewardProcessors.add(processor);
			processor.start(esb.getScheduledThreadPool());
		}
		this.serverMonitor.start(esb.getScheduledThreadPool());
		// Log Information
		logger.info("Credit Distribution Service Started");

		return true;
	}

	@Override
	public void stop()
	{
		serverMonitor.stop();
		
		// Stop the REST server
		HttpServer rest = restServer;
		if (rest != null)
		{
			rest.shutdown();
			restServer = null;
		}

		// Stop the Scheduled Account Dump Processors
		if (scheduledAccountDumpProcessors != null)
		{
			for (ScheduledAccountDumpProcessor scheduledAccountDumpProcessor : scheduledAccountDumpProcessors)
			{
				scheduledAccountDumpProcessor.stop();
			}
		}

		// Stop the Reward Processors
		if (rewardProcessors != null)
		{
			for (RewardProcessor rewardProcessor : rewardProcessors)
			{
				rewardProcessor.stop();
			}
		}

		/*
		// Stop the Data Cleanout Processors
		if (dataCleanoutProcessors != null)
		{
			for (DataCleanoutProcessor dataCleanoutProcessor : dataCleanoutProcessors)
			{
				dataCleanoutProcessor.stop();
			}
		}
		*/
		
		// Stop the OlapAnalyticsHistory Processors
/*		if (olapAnalyticsHistoryProcessor != null)
		{
			for (OlapAnalyticsHistoryProcessor oaHistoryProcessor : olapAnalyticsHistoryProcessor)
			{
				oaHistoryProcessor.stop();
			}
		}*/

		// Stop the Session Cleanup Scheduled task
		if (sessionCleanupTask != null)
			sessionCleanupTask.cancel(true);

		// Stop the TDR rotator Task
		if (tdrRotatorTask != null)
		{
			tdrWriter.close();
			tdrRotatorTask.cancel(true);
		}

		// Stop the OLAP loader Task
		if (olapLoaderTask != null)
		{
			olapLoader.close();
			olapLoaderTask.cancel(true);
		}

		if (this.olapCleaners != null)
		{
			for (OlapCleaner olapCleaner : this.olapCleaners)
			{
				olapCleaner.stop();
			}
		}

		if (this.olapSyncs != null)
		{
			for (OlapSync olapSync : this.olapSyncs)
			{
				olapSync.stop();
			}
		}

		if (this.reportScheduleExecutors != null)
		{
			for (ReportScheduleExecutor reportScheduleExecutor : this.reportScheduleExecutors)
			{
				reportScheduleExecutor.stop();
			}
		}

		// Stop the JPA Entity Manager factory
		if (oltpEntityManagerFactory != null)
			oltpEntityManagerFactory.close();

		// Stop the OLAP JPA Entity Manager factory
		if (olapEntityManagerFactory != null)
			olapEntityManagerFactory.close();

		sessions = null;
		// Log Information
		logger.info("Credit Distribution Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		CreditDistributionConfig typedConfig = (CreditDistributionConfig) config;
		LocaleCustomisationMap localeCustomisationMap = (typedConfig.localeCustomisationArray == null ? new LocaleCustomisationMap()
				: LocaleCustomisationMap.valueOf(typedConfig.localeCustomisationArray));
		this.config = typedConfig; 
		this.querySemaphores = new Semaphore(this.config.maxSimultaneousQueries, false);
		this.localeCustomisationMap = localeCustomisationMap;
		//Re-init Grizzly...
		{
			HttpServer rest = restServer;
			int newMaxPoolSize = typedConfig.getGrizzlyMaxPoolSize();
			int newCorePoolSize = typedConfig.getGrizzlyCorePoolSize();
			int newSelectorRunners = typedConfig.getGrizzlySelectorRunners();
			int newQueueLimit = typedConfig.getGrizzlyQueueLimit();
			int newBackLogSize = typedConfig.getGrizzlyBacklogSize();
			long newKeepAliveSeconds = typedConfig.getGrizzlyKeepAliveSeconds();
			TCPNIOTransport transport = restServer.getListener("grizzly").getTransport();
			int currentCorePoolSize = transport.getWorkerThreadPoolConfig().getCorePoolSize();
			int currentMaxPoolSize = transport.getWorkerThreadPoolConfig().getMaxPoolSize();
			int currentQueueLimit = transport.getWorkerThreadPoolConfig().getQueueLimit();
			int currentBacklogSize = transport.getServerConnectionBackLog();
			int currentSelectorRunners = transport.getSelectorRunnersCount();
			long currentKeepAliveSeconds = transport.getWorkerThreadPoolConfig().getKeepAliveTime(TimeUnit.SECONDS);
			boolean grizzlyConfigChanged = newMaxPoolSize != currentMaxPoolSize
					|| newCorePoolSize != currentCorePoolSize
					|| newSelectorRunners != currentSelectorRunners
					|| newQueueLimit != currentQueueLimit
					|| newKeepAliveSeconds != currentKeepAliveSeconds
					|| newBackLogSize != currentBacklogSize;
			//Avoid doing this unnecessarily:
			if(grizzlyConfigChanged)
			{
				serverMonitor.stop();
				logger.info("Grizzly HTTPS server configuration updated. Restarting HTTP server. maxPoolSize {} {}; newCorePoolSize {} {}; " +
				 "selectorRunners {} {}; queueLimit {} {}; backlogSize {} {}; keepAliveSeconds {} {}",
						newMaxPoolSize, currentMaxPoolSize, newCorePoolSize, currentCorePoolSize, newSelectorRunners, currentSelectorRunners, 
						newQueueLimit, currentQueueLimit, newBackLogSize, currentBacklogSize, newKeepAliveSeconds, currentKeepAliveSeconds);
				if (rest != null)
				{
					rest.shutdown();
					restServer = null;
				}
				logger.info("Grizzly HTTPS server has shutdown, restarting it now...");
				try {
					startRestfulServer();
					logger.info("Grizzly HTTPS server has restarted.");
					serverMonitor.start(this.esb.getScheduledThreadPool());
				} catch (IOException e) {
					logger.error("Exception occurred while restarting Grizzly HTTP server!", e);
				}
			} else {
				logger.info("Grizzly HTTPS server configuration was not changed by configuration update, therefore leaving it as is.");
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
		try
		{
			logger.info("isFit: oltpPooledDataSource = {} oltpPooledDataSource.connectionSummary = ({})", oltpPooledDataSource, PooledDataSourceUtils.connectionSummariser( oltpPooledDataSource ));
			logger.info("isFit: olapPooledDataSource = {} olapPooledDataSource.connectionSummary = ({})", olapPooledDataSource, PooledDataSourceUtils.connectionSummariser( olapPooledDataSource ));
			String url = String.format("http://%s:%d%s/management/ping", "localhost", config.restPort, config.restPath);
			HttpClient client = new HttpClient(url);
			HttpConnection connection = client.getConnection();
			connection.setRequestMethod("GET");
			InputStream response = connection.getInputStream();
			InputStreamReader reader = new InputStreamReader(response);
			BufferedReader br = new BufferedReader(reader);
			String result = br.readLine();
			br.close();
			reader.close();
			response.close();
			if (!"pong".equals(result))
				return false;
		}
		catch (IOException e)
		{
			logger.error("Failed to ping management port", e);
			return false;
		}

		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Parameters
	//
	// /////////////////////////////////

	@Perms(perms = { @Perm(name = "ViewCreditDistributionParameters", description = "View Electronic Credit Distribution", category = "Financial Services", supplier = true),
			@Perm(name = "ChangeCreditDistributionParameters", implies = "ViewCreditDistributionParameters", description = "Change Electronic Credit Distribution Parameters", category = "Financial Services", supplier = true) })
	public class LocaleCustomisationConfig extends ConfigurationBase implements ILocaleCustomisationConfig
	{
		private String localeString;

		private String currencySymbol;
		private String currencyFormatPattern;
		private String currencyGroupingSeparator;
		private String currencyDecimalSeparator;

		public LocaleCustomisationConfig()
		{
			this.localeString = "NONE";
		}

		public LocaleCustomisationConfig(String localeString)
		{
			this.localeString = localeString;
			LocaleKey localeKey = LocaleKey.valueOf(localeString);
			Currency currency = Currency.getInstance(localeKey.getLocale());
			this.currencySymbol = currency.getSymbol(localeKey.getLocale());
			DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(localeKey.getLocale());
			String pattern = decimalFormat.toPattern();
			this.currencyFormatPattern = pattern;
			DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(localeKey.getLocale());
			this.currencyGroupingSeparator = new String(new char[] { decimalFormatSymbols.getGroupingSeparator() });
			this.currencyDecimalSeparator = new String(new char[] { decimalFormatSymbols.getDecimalSeparator() });
		}

		public LocaleCustomisationConfig(LocaleCustomisation localeCustomisation)
		{
			this(localeCustomisation.getLocaleKey().toString());
			this.currencySymbol = localeCustomisation.getCurrencyFormat().getDecimalFormatSymbols().getCurrencySymbol();
			String pattern = localeCustomisation.getCurrencyFormat().toPattern();
			this.currencyFormatPattern = pattern;
			this.currencyGroupingSeparator = new String(new char[] { localeCustomisation.getCurrencyFormat().getDecimalFormatSymbols().getGroupingSeparator() });
			this.currencyDecimalSeparator = new String(new char[] { localeCustomisation.getCurrencyFormat().getDecimalFormatSymbols().getDecimalSeparator() });
		}

		@Override
		public String getLocaleString()
		{
			return this.localeString;
		}

		public String getCurrencySymbol()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.currencySymbol;
		}

		public void setCurrencySymbol(String currencySymbol)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.currencySymbol = currencySymbol;
		}

		public String getCurrencyFormatPattern()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.currencyFormatPattern;
		}

		public void setCurrencyFormatPattern(String currencyFormatPattern)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.currencyFormatPattern = currencyFormatPattern;
		}

		public String getCurrencyGroupingSeparator()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.currencyGroupingSeparator;
		}

		public void setCurrencyGroupingSeparator(String currencyGroupingSeparator) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.lengthInRange(currencyGroupingSeparator, 1, 1, "Currency Grouping Separator");
			this.currencyGroupingSeparator = currencyGroupingSeparator;
		}

		public String getCurrencyDecimalSeparator()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.currencyDecimalSeparator;
		}

		public void setCurrencyDecimalSeparator(String currencyDecimalSeparator) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.lengthInRange(currencyDecimalSeparator, 1, 1, "Currency Decimal Separator");
			this.currencyDecimalSeparator = currencyDecimalSeparator;
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return 4192444470577726570L + localeString.hashCode();
		}

		@Override
		public String getPath(String languageCode)
		{
			return "";
		}

		@Override
		public String getName(String languageCode)
		{
			return "Locale " + localeString;
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public LocaleCustomisation toCustomisation()
		{
			LocaleKey localeKey = LocaleKey.valueOf(this.getLocaleString());
			DecimalFormat currencyFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(localeKey.getLocale());

			currencyFormat.applyPattern(this.getCurrencyFormatPattern());

			DecimalFormatSymbols currencyFormatSymbols = currencyFormat.getDecimalFormatSymbols();
			currencyFormatSymbols.setCurrencySymbol(this.getCurrencySymbol());
			currencyFormatSymbols.setGroupingSeparator(this.getCurrencyGroupingSeparator().charAt(0));
			currencyFormatSymbols.setDecimalSeparator(this.getCurrencyDecimalSeparator().charAt(0));
			currencyFormatSymbols.setMonetaryDecimalSeparator(this.getCurrencyDecimalSeparator().charAt(0));
			currencyFormat.setDecimalFormatSymbols(currencyFormatSymbols);

			return new LocaleCustomisation(localeKey, currencyFormat);
		}
	}

	@Perms(perms = { @Perm(name = "ViewCreditDistributionParameters", description = "View Electronic Credit Distribution", category = "Financial Services", supplier = true),
			@Perm(name = "ChangeCreditDistributionParameters", implies = "ViewCreditDistributionParameters", description = "Change Electronic Credit Distribution Parameters", category = "Financial Services", supplier = true) })
	public class CreditDistributionConfig extends ConfigurationBase
	{
		protected Phrase serviceName = Phrase.en("Electronic Credit Distribution");

		private int restPort = 14400;

		private String restPath = "/ecds";

		private String smsSourceAddress = "110";

		private boolean enableTLS = false;
		private String serverKeyStore = "/var/opt/cs/c4u/certificates/keystore_server";
		private String serverKeyStorePasswd = "123456";
		private boolean requireClientAuth = false;
		private String serverTrustStore = "/var/opt/cs/c4u/certificates/truststore_server";
		private String serverTrustStorePasswd = "123456";

		private String databaseConnectionString = configureDatabaseConnectionString();
		private int oltpDatabaseConnectionTimeout = 500;
		private int oltpDatabaseMaxStatements = 500;
		private int oltpDatabaseMinPoolSize = 20;
		private int oltpDatabaseMaxPoolSize = 500;
		private int oltpIdleTestPeriod = 300; //less than databaseConnectionTimeout
		private boolean testOltpConnectionOnCheckout = false;
		private boolean testOltpConnectionOnCheckin = true;
		
		private int olapDatabaseConnectionTimeout = 500;
		private int olapDatabaseMaxStatements = 100;
		private int olapDatabaseMinPoolSize = 5;
		private int olapDatabaseMaxPoolSize = 100;
		private int olapIdleTestPeriod = 300; //less than databaseConnectionTimeout
		private boolean testOlapConnectionOnCheckout = false;
		private boolean testOlapConnectionOnCheckin = true;

		private String apDatabaseConnectionString = configureApDatabaseConnectionString();
		private int apLoaderInterval = 300;
		private int analyticsConfigurationCompanyID = 2;
		
		private boolean useInternationalNumbers = false;

		protected LocaleCustomisationConfig[] localeCustomisationArray = null;
		private String localeCustomisations = "";

		private String invalidUssdCommandNotification = "Commande USSD invalide/Invalid USSD Command";
		
		private int maxSimultaneousQueries = 4;
		
		private String oltpUsername = "root";
		private String olapUsername = "root";
		
		private String oltpPassword = "ussdgw";
		private String olapPassword = "ussdgw";

		private String oltpDatabaseDriver = "com.mysql.jdbc.Driver"; //other option is org.mariadb.jdbc.Driver
		private String olapDatabaseDriver = "com.mysql.jdbc.Driver"; //other option is org.mariadb.jdbc.Driver
		private String oltpDatabaseDialect = "org.hibernate.dialect.MySQL5InnoDBDialect"; //org.hibernate.dialect.MariaDBDialect
		private String olapDatabaseDialect = "org.hibernate.dialect.MySQL5InnoDBDialect"; //org.hibernate.dialect.MariaDBDialect
		
		
		//GrizzlyConfigs
		private int grizzlyQueueLimit = Runtime.getRuntime().availableProcessors() * 4;
		private int grizzlyCorePoolSize = Runtime.getRuntime().availableProcessors() * 4;
		private int grizzlyMaxPoolSize = Runtime.getRuntime().availableProcessors() * 4;
		private long grizzlyKeepAliveSeconds = 30;
		private int grizzlySelectorRunners = Runtime.getRuntime().availableProcessors() / 2;
		private int grizzlyBacklogSize = 32;

		@Config(description = "OLTP Database Connection String")
		public String getDatabaseConnectionString()
		{
			check(esb, "ViewCreditDistributionParameters");
			return databaseConnectionString;
		}

		public void setDatabaseConnectionString(String connectionString)
		{
			check(esb, "ChangeCreditDistributionParameters");
			databaseConnectionString = connectionString;
		}

		@Config(description = "OLTP Database Username")
		public String getOltpUsername()
		{
			check(esb, "ViewCreditDistributionParameters");
			return oltpUsername;
		}

		public void setOltpUsername(String oltpUsername)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.oltpUsername = oltpUsername;
		}

		@Config(description = "OLTP Database Password")
		public String getOltpPassword()
		{
			check(esb, "ViewCreditDistributionParameters");
			return oltpPassword;
		}

		public void setOltpPassword(String oltpPassword)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.oltpPassword = oltpPassword;
		}

		@Config(description = "OLTP JDBC Database Driver", comment = "Either com.mysql.jdbc.Driver or org.mariadb.jdbc.Driver")
		public String getOltpDatabaseDriver()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.oltpDatabaseDriver;
		}

		public void setOltpDatabaseDriver(String oltpDatabaseDriver)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.oltpDatabaseDriver = oltpDatabaseDriver;
		}

		@Config(description = "OLTP JDBC Database Dialect", comment = "Either org.hibernate.dialect.MySQL5InnoDBDialect or org.hibernate.dialect.MariaDBDialect")
		public String getOltpDatabaseDialect()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.oltpDatabaseDialect;
		}

		public void setOltpDatabaseDialect(String oltpDatabaseDialect)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.oltpDatabaseDialect = oltpDatabaseDialect;
		}

		@Config(description = "OLTP Database Connection Timeout", comment = "Must be greater than the Idle Test Period Value.")
		public int getOltpDatabaseConnectionTimeout()
		{
			check(esb, "ViewCreditDistributionParameters");
			return oltpDatabaseConnectionTimeout;
		}

		public void setOltpDatabaseConnectionTimeout(int oltpDatabaseConnectionTimeout) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(0, oltpDatabaseConnectionTimeout);
			ValidationException.min(this.oltpIdleTestPeriod, this.oltpDatabaseConnectionTimeout);
			this.oltpDatabaseConnectionTimeout = oltpDatabaseConnectionTimeout;
		}

		@Config(description = "OLTP Max Statements", comment = "Number of prepared statements will be cached. Increase performance. Hibernate default: 0, caching is disable.")
		public int getOltpDatabaseMaxStatements()
		{
			check(esb, "ViewCreditDistributionParameters");
			return oltpDatabaseMaxStatements;
		}

		public void setOltpDatabaseMaxStatements(int maxStatements) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(0, maxStatements);
			this.oltpDatabaseMaxStatements = maxStatements;
		}

		@Config(description = "OLTP Connection Pool Minimum Size")
		public int getOltpDatabaseMinPoolSize()
		{
			check(esb, "ViewCreditDistributionParameters");
			return oltpDatabaseMinPoolSize;
		}

		public void setOltpDatabaseMinPoolSize(int oltpDatabaseMinPoolSize) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(1, oltpDatabaseMinPoolSize);
			this.oltpDatabaseMinPoolSize = oltpDatabaseMinPoolSize;
		}

		@Config(description = "OLTP Connection Pool Maximum Size")
		public int getOltpDatabaseMaxPoolSize()
		{
			check(esb, "ViewCreditDistributionParameters");
			return oltpDatabaseMaxPoolSize;
		}

		public void setOltpDatabaseMaxPoolSize(int oltpDatabaseMaxPoolSize) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(oltpDatabaseMinPoolSize, oltpDatabaseMaxPoolSize);
			this.oltpDatabaseMaxPoolSize = oltpDatabaseMaxPoolSize;
		}

		@Config(description = "OLTP Connection Idle Test Period", comment = "Seconds")
		public int getOltpIdleTestPeriod()
		{
			check(esb, "ViewCreditDistributionParameters");
			return oltpIdleTestPeriod;
		}

		public void setOltpIdleTestPeriod(int oltpIdleTestPeriod) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(0, oltpIdleTestPeriod);
			this.oltpIdleTestPeriod = oltpIdleTestPeriod;
		}

		@Config(description = "OLAP Database Connection String")
		public String getApDatabaseConnectionString()
		{
			check(esb, "ViewCreditDistributionParameters");
			return apDatabaseConnectionString;
		}

		public void setApDatabaseConnectionString(String connectionString)
		{
			check(esb, "ChangeCreditDistributionParameters");
			apDatabaseConnectionString = connectionString;
		}

		@Config(description = "OLAP Database Username")
		public String getOlapUsername()
		{
			check(esb, "ViewCreditDistributionParameters");
			return olapUsername;
		}

		public void setOlapUsername(String olapUsername)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.olapUsername = olapUsername;
		}

		@Config(description = "OLAP Database Password")
		public String getOlapPassword()
		{
			check(esb, "ViewCreditDistributionParameters");
			return olapPassword;
		}

		public void setOlapPassword(String olapPassword)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.olapPassword = olapPassword;
		}

		@SupplierOnly
		@Config(description = "OLAP Loader Interval")
		public int getApLoaderInterval()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.apLoaderInterval;
		}

		public void setApLoaderInterval(int apLoaderInterval) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(5, apLoaderInterval);
			this.apLoaderInterval = apLoaderInterval;
		}

		@Config(description = "OLAP JDBC Database Driver", comment = "Either com.mysql.jdbc.Driver or org.mariadb.jdbc.Driver")
		public String getOlapDatabaseDriver()
		{
			check(esb, "ViewCreditDistributionParameters");
			return olapDatabaseDriver;
		}

		public void setOlapDatabaseDriver(String olapDatabaseDriver) 
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.olapDatabaseDriver = olapDatabaseDriver;
		}

		@Config(description = "OLAP JDBC Database Dialect", comment = "Either org.hibernate.dialect.MySQL5InnoDBDialect or org.hibernate.dialect.MariaDBDialect")
		public String getOlapDatabaseDialect()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.olapDatabaseDialect;
		}

		public void setOlapDatabaseDialect(String olapDatabaseDialect)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.olapDatabaseDialect = olapDatabaseDialect;
		}

		@Config(description = "OLAP Database Connection Timeout", comment = "Must be greater than the Idle Test Period Value.")
		public int getOlapDatabaseConnectionTimeout()
		{
			check(esb, "ViewCreditDistributionParameters");
			return olapDatabaseConnectionTimeout;
		}

		public void setOlapDatabaseConnectionTimeout(int olapDatabaseConnectionTimeout) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(0, olapDatabaseConnectionTimeout);
			ValidationException.min(this.olapIdleTestPeriod, this.oltpDatabaseConnectionTimeout);
			this.olapDatabaseConnectionTimeout = olapDatabaseConnectionTimeout;
		}

		@Config(description = "OLAP Max Statements", comment = "Number of prepared statements will be cached. Increase performance. Hibernate default: 0, caching is disable.")
		public int getOlapDatabaseMaxStatements()
		{
			check(esb, "ViewCreditDistributionParameters");
			return olapDatabaseMaxStatements;
		}

		public void setOlapDatabaseMaxStatements(int maxStatements) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(0, maxStatements);
			this.olapDatabaseMaxStatements = maxStatements;
		}

		@Config(description = "OLAP Connection Pool Minimum Size", comment = "Minimum number of JDBC connections in the pool.")
		public int getOlapDatabaseMinPoolSize()
		{
			check(esb, "ViewCreditDistributionParameters");
			return olapDatabaseMinPoolSize;
		}

		public void setOlapDatabaseMinPoolSize(int olapDatabaseMinPoolSize) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(1, olapDatabaseMinPoolSize);
			this.olapDatabaseMinPoolSize = olapDatabaseMinPoolSize;
		}

		@Config(description = "OLAP Connection Pool Maximum Size", comment = "Maximum number of JDBC connections in the pool.")
		public int getOlapDatabaseMaxPoolSize()
		{
			check(esb, "ViewCreditDistributionParameters");
			return olapDatabaseMaxPoolSize;
		}

		public void setOlapDatabaseMaxPoolSize(int olapDatabaseMaxPoolSize) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(olapDatabaseMinPoolSize, olapDatabaseMaxPoolSize);
			this.olapDatabaseMaxPoolSize = olapDatabaseMaxPoolSize;
		}

		@Config(description = "OLAP Connection Idle Test Period", comment = "Seconds")
		public int getOlapIdleTestPeriod()
		{
			check(esb, "ViewCreditDistributionParameters");
			return olapIdleTestPeriod;
		}

		public void setOlapIdleTestPeriod(int olapIdleTestPeriod) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			ValidationException.min(0, olapIdleTestPeriod);
			this.olapIdleTestPeriod = olapIdleTestPeriod;
		}

		@Config(description = "OLTP Test Connection On Checkout")
		public boolean getTestOltpConnectionOnCheckout()
		{
			check(esb, "ViewCreditDistributionParameters");
			return testOltpConnectionOnCheckout;
		}

		public void setTestOltpConnectionOnCheckout(boolean testOltpConnectionOnCheckout)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.testOltpConnectionOnCheckout = testOltpConnectionOnCheckout;
		}

		@Config(description = "OLAP Test Connection On Checkout")
		public boolean getTestOlapConnectionOnCheckout()
		{
			check(esb, "ViewCreditDistributionParameters");
			return testOlapConnectionOnCheckout;
		}

		public void setTestOlapConnectionOnCheckout(boolean testOlapConnectionOnCheckout)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.testOlapConnectionOnCheckout = testOlapConnectionOnCheckout;
		}

		@Config(description = "OLTP Test Connection On Checkin")
		public boolean getTestOltpConnectionOnCheckin()
		{
			check(esb, "ViewCreditDistributionParameters");
			return testOltpConnectionOnCheckin;
		}

		public void setTestOltpConnectionOnCheckin(boolean testOltpConnectionOnCheckin)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.testOltpConnectionOnCheckin = testOltpConnectionOnCheckin;
		}

		@Config(description = "OLAP Test Connection On Checkin")
		public boolean getTestOlapConnectionOnCheckin()
		{
			check(esb, "ViewCreditDistributionParameters");
			return testOlapConnectionOnCheckin;
		}

		public void setTestOlapConnectionOnCheckin(boolean testOlapConnectionOnCheckin)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.testOlapConnectionOnCheckin = testOlapConnectionOnCheckin;
		}

		public String getInvalidUssdCommandNotification()
		{
			check(esb, "ViewCreditDistributionParameters");
			return invalidUssdCommandNotification;
		}

		public void setInvalidUssdCommandNotification(String invalidUssdCommandNotification)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.invalidUssdCommandNotification = invalidUssdCommandNotification;
		}

		public int getMaxSimultaneousQueries()
		{
			check(esb, "ViewCreditDistributionParameters");
			return maxSimultaneousQueries;
		}

		public void setMaxSimultaneousQueries(int maxSimultaneousQueries)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.maxSimultaneousQueries = maxSimultaneousQueries;
		}

		public Phrase getServiceName()
		{
			check(esb, "ChangeCreditDistributionParameters");
			return serviceName;
		}

		@Config(description = "TLS Enabled", comment = "Restart Required")
		public boolean getTLSEnabled()
		{
			check(esb, "ViewCreditDistributionParameters");
			return enableTLS;
		}

		public void setTLSEnabled(boolean enableTLS)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.enableTLS = enableTLS;
		}

		@Config(description = "REST Port")
		public int getRestPort()
		{
			check(esb, "ViewCreditDistributionParameters");
			return restPort;
		}

		public void setRestPort(int restPort)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.restPort = restPort;
		}

		@Config(description = "REST Path")
		public String getRestPath()
		{
			check(esb, "ViewCreditDistributionParameters");
			return restPath;
		}

		public void setRestPath(String restPath)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.restPath = restPath;
		}

		@Config(description = "REST URL")
		public String getRestURL(boolean secure)
		{
			String http = (secure ? "https:" : "http:");

			check(esb, "ViewCreditDistributionParameters");
			return String.format("%s//%s:%d%s", http, "0.0.0.0", restPort, restPath);
		}

		public URI getRestURI(boolean secure)
		{
			return UriBuilder.fromUri(config.getRestURL(secure)).build();
		}

		public String getSmsSourceAddress()
		{
			check(esb, "ViewCreditDistributionParameters");
			return smsSourceAddress;
		}

		public void setSmsSourceAddress(String smsSourceAddress)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.smsSourceAddress = smsSourceAddress;
		}

		public boolean isUseInternationalNumbers()
		{
			check(esb, "ViewCreditDistributionParameters");
			return useInternationalNumbers;
		}

		public void setUseInternationalNumbers(boolean useInternationalNumbers)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.useInternationalNumbers = useInternationalNumbers;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "Financial Services";
		}

		@Override
		public INotifications getNotifications()
		{
			return notifications;
		}

		@Override
		public long getSerialVersionUID()
		{
			return -3443696493344986702L;
		}

		@Override
		public String getName(String languageCode)
		{
			return serviceName.getSafe(languageCode, "Electronic Credit Distribution");
		}

		@Override
		public void validate() throws ValidationException
		{
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeCreditDistributionNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewCreditDistributionNotifications");
		}

		@Config(description = "Key Store Path", comment = "Restart Required")
		public String getKeyStorePath()
		{
			check(esb, "ViewCreditDistributionParameters");
			return serverKeyStore;
		}

		public void setKeyStorePath(String pathStore)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.serverKeyStore = pathStore;
		}

		@Config(description = "Require Client Authentication", comment = "Restart Required")
		public boolean getRequireClientAuth()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.requireClientAuth;
		}

		public void setRequireClientAuth( boolean requireClientAuth )
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.requireClientAuth = requireClientAuth;
		}

		@Config(description = "Trust Store Path", comment = "Restart Required")
		public String getTrustStorePath()
		{
			check(esb, "ViewCreditDistributionParameters");
			return serverTrustStore;
		}

		public void setTrustStorePath(String pathStore)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.serverTrustStore = pathStore;
		}

		@Config(description = "Key Store Password", comment = "Restart Required")
		public String getKeyStorePassword()
		{
			check(esb, "ViewCreditDistributionParameters");
			return serverKeyStorePasswd;
		}

		public void setKeyStorePassword(String storePasswd)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.serverKeyStorePasswd = storePasswd;
		}
		@Config(description = "Trust Store Password", comment = "Restart Required")
		public String getTrustStorePassword()
		{
			check(esb, "ViewCreditDistributionParameters");
			return serverTrustStorePasswd;
		}

		public void setTrustStorePassword(String storePasswd)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.serverTrustStorePasswd = storePasswd;
		}

		@SupplierOnly
		@Config(description = "Analytics Configuration Company ID", comment = "This is the only company for which the Analytics Scheduler Configuration will be effective")
		public int getAnalyticsConfigurationCompanyID()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.analyticsConfigurationCompanyID;
		}

		public void setAnalyticsConfigurationCompanyID(int analyticsConfigurationCompanyID) throws ValidationException
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.analyticsConfigurationCompanyID = analyticsConfigurationCompanyID;
		}
		
		public LocaleCustomisationConfig gtLocaleCustomisation(String localeString)
		{
			for (LocaleCustomisationConfig item : this.localeCustomisationArray)
			{
				if (localeString.equals(item.localeString))
					return item;
			}
			return null;
		}

		@Config(description = "Locale Customisations", comment = "Comma seperated list of locales in format <ISO 639-1 language code>_<ISO 3166-1 alpha-2>. Example: en_GB,de_CH,fr_CH - this will add locales for English + Great Brittain, German + Switzerland, French + Switzerland.")
		public String getLocaleCustomisation()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.localeCustomisations;
		}

		public void setLocaleCustomisation(String localeCustomisations)
		{
			check(esb, "ChangeCreditDistributionParameters");
			Set<String> localeStrings = new TreeSet<String>();
			if (!localeCustomisations.isEmpty())
			{
				String[] items = localeCustomisations.split(",");
				for (String item : items)
				{
					localeStrings.add(item.trim());
				}
			}
			LocaleCustomisationMap localeCustomisationMap = (this.localeCustomisationArray == null ? new LocaleCustomisationMap() : LocaleCustomisationMap.valueOf(this.localeCustomisationArray));
			// retain entries
			for (String localeString : localeStrings)
			{
				LocaleKey localeKey = LocaleKey.valueOf(localeString);
				if (localeCustomisationMap.get(localeKey) == null)
				{
					localeCustomisationMap.put(localeKey);
				}
			}
			// remove entries
			Set<Map.Entry<LocaleKey, LocaleCustomisation>> entrySet = localeCustomisationMap.entrySet();
			for (Iterator<Map.Entry<LocaleKey, LocaleCustomisation>> iterator = entrySet.iterator(); iterator.hasNext();)
			{
				Map.Entry<LocaleKey, LocaleCustomisation> entry = iterator.next();
				LocaleKey locale = entry.getKey();
				if (!localeStrings.contains(locale.toString()))
					iterator.remove();
			}
			Collection<LocaleCustomisation> values = localeCustomisationMap.values();
			LocaleCustomisationConfig[] newArray = new LocaleCustomisationConfig[values.size()];
			int i = 0;
			for (LocaleCustomisation item : values)
			{
				newArray[i++] = new LocaleCustomisationConfig(item);
			}
			this.localeCustomisationArray = newArray;
			this.localeCustomisations = localeCustomisations;
		}

		@Override
		@SuppressWarnings({ "unchecked" })
		public Collection<IConfiguration> getConfigurations()
		{
			if (this.localeCustomisationArray == null)
			{
				this.setLocaleCustomisation(this.localeCustomisations);
			}
			return (Collection<IConfiguration>) (Collection<?>) Arrays.asList(this.localeCustomisationArray);
		}
		
		public String configureDatabaseConnectionString()
		{
			StringBuilder dbConnectionString = new StringBuilder("jdbc:mysql://localhost/");
			if (CreditDistribution.forceDatabase != null && forceDatabase.containsKey("hxc"))
				dbConnectionString.append(CreditDistribution.forceDatabase.get("hxc"));
			else
				dbConnectionString.append("hxc");
			
			dbConnectionString.append("?useSSL=false&requireSSL=false&verifyServerCertificate=false&sendFractionalSeconds=false&");
			
			logger.error("COOPMYDB:: Using connect String "+dbConnectionString.toString());
			return dbConnectionString.toString();
		}
		
		public String configureApDatabaseConnectionString()
		{
			StringBuilder dbConnectionString = new StringBuilder("jdbc:mysql://localhost/");
			if (CreditDistribution.forceDatabase != null && forceDatabase.containsKey("ecdsap"))
				dbConnectionString.append(CreditDistribution.forceDatabase.get("ecdsap"));
			else
				dbConnectionString.append("ecdsap");
			
			dbConnectionString.append("?createDatabaseIfNotExist=true&useSSL=false&requireSSL=false&verifyServerCertificate=false&sendFractionalSeconds=false&");
			dbConnectionString.append("sessionVariables=sql_mode='STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'&");
			
			return dbConnectionString.toString();
		}

		@SupplierOnly
		@Config(description = "Grizzly Queue Limit", comment = "The maximum number of pending tasks that may be queued. Updating this value will cause the HTTP HuX Server to be restarted.")
		public int getGrizzlyQueueLimit()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.grizzlyQueueLimit;
		}

		@SupplierOnly
		public void setGrizzlyQueueLimit(int grizzlyQueueLimit)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.grizzlyQueueLimit = grizzlyQueueLimit;
		}

		@SupplierOnly
		@Config(description = "Grizzly Core Pool Size", comment = "The initial number of threads that will be present with the thread pool is created. Updating this value will cause the HTTP HuX Server to be restarted.")
		public int getGrizzlyCorePoolSize()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.grizzlyCorePoolSize;
		}

		@SupplierOnly
		public void setGrizzlyCorePoolSize(int corePoolSize)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.grizzlyCorePoolSize = corePoolSize;
		}

		@SupplierOnly
		@Config(description = "Grizzly Backlog Size", comment = "Specifies the maximum pending connection queue size.")
		public int getGrizzlyBacklogSize() {
			check(esb, "ViewCreditDistributionParameters");
			return this.grizzlyBacklogSize;
		}

		@SupplierOnly
		public void setGrizzlyBacklogSize(int backlogSize) {
			check(esb, "ChangeCreditDistributionParameters");
			this.grizzlyBacklogSize = backlogSize;
		}

		@SupplierOnly
		@Config(description = "Grizzly Max Pool Size", comment = "The maximum number threads that may be maintained by this thread pool. Updating this value will cause the HTTP HuX Server to be restarted.")
		public int getGrizzlyMaxPoolSize()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.grizzlyMaxPoolSize;
		}

		@SupplierOnly
		public void setGrizzlyMaxPoolSize(int grizzlyMaxPoolSize)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.grizzlyMaxPoolSize = grizzlyMaxPoolSize;
		}

		@SupplierOnly
		@Config(description = "Grizzly Keep Alive Time", comment = "The maximum time in seconds that a thread may stay idle and wait for a new task to execute before it will be released. Updating this value will cause the HTTP HuX Server to be restarted.")
		public long getGrizzlyKeepAliveSeconds()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.grizzlyKeepAliveSeconds;
		}

		@SupplierOnly
		public void setGrizzlyKeepAliveSeconds(long grizzlyKeepAliveSeconds)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.grizzlyKeepAliveSeconds = grizzlyKeepAliveSeconds;
		}
		
		@SupplierOnly
		@Config(description = "Grizzly Selector Runners", comment = "Default number of available processors.  Recommended range: one selector runner for every 1-4 cores. Updating this value will cause the HTTP HuX Server to be restarted.")
		public int getGrizzlySelectorRunners()
		{
			check(esb, "ViewCreditDistributionParameters");
			return this.grizzlySelectorRunners;
		}

		@SupplierOnly
		public void setGrizzlySelectorRunners(int grizzlySelectorRunners)
		{
			check(esb, "ChangeCreditDistributionParameters");
			this.grizzlySelectorRunners = grizzlySelectorRunners;
		}

		private void putIfMissing(Map<String, String> props, String key, String value)
		{
			if(!props.containsKey(key))
			{
				props.put(key, value);
			}
		}

		@Override
		public boolean save(IDatabaseConnection database, ICtrlConnector control)
		{
			logger.trace("save: {}", getSerialVersionUID());
			{
				Map<String, String> oltpProps = new HashMap<String, String>();
				File oltpConfigFile = new File(oltpConfigFileName);
				if (oltpConfigFile.exists())
				{
					oltpProps = readPropertiesFile(oltpConfigFileName);
					String connectionString = getDatabaseConnectionString();
					if(connectionString.indexOf("logger=com.mysql.jdbc.log.Slf4JLogger") < 0)
					{
						oltpProps.put("javax.persistence.jdbc.url", getDatabaseConnectionString() + "&logger=com.mysql.jdbc.log.Slf4JLogger");
					} else {
						oltpProps.put("javax.persistence.jdbc.url", getDatabaseConnectionString());
					}
					oltpProps.put("javax.persistence.jdbc.user", getOltpUsername());
					oltpProps.put("javax.persistence.jdbc.password", getOltpPassword());
					oltpProps.put("javax.persistence.jdbc.driver", getOltpDatabaseDriver());
					oltpProps.put("hibernate.dialect", getOltpDatabaseDialect());
					oltpProps.put("hibernate.c3p0.timeout", Integer.toString(getOltpDatabaseConnectionTimeout()));
					oltpProps.put("hibernate.c3p0.min_size", Integer.toString(getOltpDatabaseMinPoolSize()));
					oltpProps.put("hibernate.c3p0.max_size", Integer.toString(getOltpDatabaseMaxPoolSize()));
					oltpProps.put("hibernate.c3p0.max_statements", Integer.toString(getOltpDatabaseMaxStatements()));
					oltpProps.put("hibernate.c3p0.idle_test_period", Integer.toString(getOltpIdleTestPeriod())); //test idle connections every 300 seconds
					oltpProps.put("hibernate.c3p0.testConnectionOnCheckin", Boolean.toString(getTestOltpConnectionOnCheckin()));
					oltpProps.put("hibernate.c3p0.testConnectionOnCheckout", Boolean.toString(getTestOltpConnectionOnCheckout()));
					putIfMissing(oltpProps, "hibernate.connection.isolation", "TRANSACTION_REPEATABLE_READ");
					putIfMissing(oltpProps, "hibernate.show_sql", "false");
					putIfMissing(oltpProps, "hibernate.format_sql", "false");
					putIfMissing(oltpProps, "hibernate.hbm2ddl.auto", "validate");
					putIfMissing(oltpProps, "hibernate.temp.use_jdbc_metadata_defaults", "false");
					createPropertiesFile(oltpConfigFileName, oltpProps);
				}
			}
			{
				Map<String, String> olapProps = new HashMap<String, String>();
				File olapConfigFile = new File(olapConfigFileName);
				if (olapConfigFile.exists())
				{
					olapProps = readPropertiesFile(olapConfigFileName);
					String apConnectionString = getApDatabaseConnectionString();
					if(apConnectionString.indexOf("logger=com.mysql.jdbc.log.Slf4JLogger") < 0)
					{
						olapProps.put("javax.persistence.jdbc.url", getApDatabaseConnectionString() + "&logger=com.mysql.jdbc.log.Slf4JLogger");
					} else {
						olapProps.put("javax.persistence.jdbc.url", getApDatabaseConnectionString());
					}
					olapProps.put("javax.persistence.jdbc.user", getOlapUsername());
					olapProps.put("javax.persistence.jdbc.password", getOlapPassword());
					olapProps.put("javax.persistence.jdbc.driver", getOlapDatabaseDriver());
					olapProps.put("hibernate.dialect", getOlapDatabaseDialect());
					olapProps.put("hibernate.c3p0.timeout", Integer.toString(getOlapDatabaseConnectionTimeout()));
					olapProps.put("hibernate.c3p0.min_size", Integer.toString(getOlapDatabaseMinPoolSize()));
					olapProps.put("hibernate.c3p0.max_size", Integer.toString(getOlapDatabaseMaxPoolSize()));
					olapProps.put("hibernate.c3p0.max_statements", Integer.toString(getOlapDatabaseMaxStatements()));
					olapProps.put("hibernate.c3p0.idle_test_period", Integer.toString(getOlapIdleTestPeriod())); //test idle connections every 300 seconds
					olapProps.put("hibernate.c3p0.testConnectionOnCheckin", Boolean.toString(getTestOlapConnectionOnCheckin()));
					olapProps.put("hibernate.c3p0.testConnectionOnCheckout", Boolean.toString(getTestOlapConnectionOnCheckout()));
					putIfMissing(olapProps, "hibernate.connection.isolation", "TRANSACTION_REPEATABLE_READ");
					putIfMissing(olapProps, "hibernate.show_sql", "false");
					putIfMissing(olapProps, "hibernate.format_sql", "false");
					putIfMissing(olapProps, "hibernate.hbm2ddl.auto", "validate");
					putIfMissing(olapProps, "hibernate.temp.use_jdbc_metadata_defaults", "false");
					createPropertiesFile(olapConfigFileName, olapProps);
				}
			}
			//At end because save triggers a notification reload config introducing a race condition.
			super.save(database, control);
			return true;
		}

		@Override
		public boolean load(IDatabaseConnection databaseConnection)
		{
			//Call the superclass to ensure that the nested configuration
			//pages, such as the locale settings get loaded...
			super.load(databaseConnection);
			logger.trace("load: {}", getSerialVersionUID());
			{
				Map<String, String> oltpProps = new HashMap<String, String>();
				File oltpConfigFile = new File(oltpConfigFileName);
				if (oltpConfigFile.exists())
				{
					oltpProps = readPropertiesFile(oltpConfigFileName);

					if(oltpProps.containsKey("javax.persistence.jdbc.url"))
						config.setDatabaseConnectionString(oltpProps.get("javax.persistence.jdbc.url"));

					if(oltpProps.containsKey("javax.persistence.jdbc.user"))
						config.setOltpUsername(oltpProps.get("javax.persistence.jdbc.user"));
					else
						config.setOltpUsername("root");

					if(oltpProps.containsKey("javax.persistence.jdbc.password"))
						config.setOltpPassword(oltpProps.get("javax.persistence.jdbc.password"));
					else
						config.setOltpPassword("ussdgw");

					if(oltpProps.containsKey("javax.persistence.jdbc.driver"))
						config.setOltpDatabaseDriver(oltpProps.get("javax.persistence.jdbc.driver"));
					else
						config.setOltpDatabaseDriver("com.mysql.jdbc.Driver");

					if(oltpProps.containsKey("hibernate.dialect"))
						config.setOltpDatabaseDialect(oltpProps.get("hibernate.dialect"));
					else
						config.setOltpDatabaseDialect("org.hibernate.dialect.MySQL5InnoDBDialect");

					if(oltpProps.containsKey("hibernate.c3p0.timeout"))
					{
						try {
							config.setOltpDatabaseConnectionTimeout(Integer.parseInt(oltpProps.get("hibernate.c3p0.timeout")));
						} catch (NumberFormatException e) {
							logger.error("Could not parse OLTP configuration value hibernate.c3p0.timeout", e);
						} catch (ValidationException e) {
							logger.error("OLTP configuration validation exception hibernate.c3p0.timeout", e);
						}
					} else {
						try {
							config.setOltpDatabaseConnectionTimeout(500);
						} catch (ValidationException e) {
							logger.error("OLTP configuration validation exception hibernate.c3p0.timeout", e);
						}
					}
					if(oltpProps.containsKey("hibernate.c3p0.min_size"))
					{
						try {
							config.setOltpDatabaseMinPoolSize(Integer.parseInt(oltpProps.get("hibernate.c3p0.min_size")));
						} catch (NumberFormatException e) {
							logger.error("Could not parse OLTP configuration value hibernate.c3p0.min_size", e);
						} catch (ValidationException e) {
							logger.error("OLTP configuration validation exception hibernate.c3p0.min_size", e);
						}
					} else {
						try {
							config.setOltpDatabaseMinPoolSize(120);
						} catch (ValidationException e) {
							logger.error("OLTP configuration validation exception hibernate.c3p0.min_size", e);
						}
					}
					if(oltpProps.containsKey("hibernate.c3p0.max_size"))
					{
						try {
							config.setOltpDatabaseMaxPoolSize(Integer.parseInt(oltpProps.get("hibernate.c3p0.max_size")));
						} catch (NumberFormatException e) {
							logger.error("Could not parse OLTP configuration value hibernate.c3p0.max_size", e);
						} catch (ValidationException e) {
							logger.error("OLTP configuration validation exception hibernate.c3p0.max_size", e);
						}
					} else {
						try {
							config.setOltpDatabaseMaxPoolSize(500);
						} catch (ValidationException e) {
							logger.error("OLTP configuration validation exception hibernate.c3p0.max_size", e);
						}
					}
					if(oltpProps.containsKey("hibernate.c3p0.max_statements"))
					{
						try {
							config.setOltpDatabaseMaxStatements(Integer.parseInt(oltpProps.get("hibernate.c3p0.max_statements")));
						} catch (NumberFormatException e) {
							logger.error("Could not parse OLTP configuration value hibernate.c3p0.max_statements", e);
						} catch (ValidationException e) {
							logger.error("OLTP configuration validation exception hibernate.c3p0.max_statements", e);
						}
					} else {
						try {
							config.setOltpDatabaseMaxStatements(500);
						} catch (ValidationException e) {
							logger.error("OLTP configuration validation exception hibernate.c3p0.max_statements", e);
						}
					}
					if(oltpProps.containsKey("hibernate.c3p0.idle_test_period"))
					{
						try {
							config.setOltpIdleTestPeriod(Integer.parseInt(oltpProps.get("hibernate.c3p0.idle_test_period")));
						} catch (NumberFormatException e) {
							logger.error("Could not parse OLTP configuration value hibernate.c3p0.idle_test_period", e);
						} catch (ValidationException e) {
							logger.error("OLTP configuration validation exception hibernate.c3p0.idle_test_period", e);
						}
					} else {
						try {
							config.setOltpIdleTestPeriod(300);
						} catch (ValidationException e) {
							logger.error("OLTP configuration validation exception hibernate.c3p0.idle_test_period", e);
						}
					}
					if(oltpProps.containsKey("hibernate.c3p0.testConnectionOnCheckin"))
					{
						config.setTestOltpConnectionOnCheckin(Boolean.parseBoolean(oltpProps.get("hibernate.c3p0.testConnectionOnCheckin")));
					} else {
						config.setTestOltpConnectionOnCheckin(true);
					}
					if(oltpProps.containsKey("hibernate.c3p0.testConnectionOnCheckout"))
					{
						config.setTestOltpConnectionOnCheckout(Boolean.parseBoolean(oltpProps.get("hibernate.c3p0.testConnectionOnCheckout")));
					} else {
						config.setTestOltpConnectionOnCheckout(false);
					}
				}
			}
			{
				Map<String, String> olapProps = new HashMap<String, String>();
				File olapConfigFile = new File(olapConfigFileName);
				if (olapConfigFile.exists())
				{
					olapProps = readPropertiesFile(olapConfigFileName);

					if(olapProps.containsKey("javax.persistence.jdbc.url"))
						config.setApDatabaseConnectionString(olapProps.get("javax.persistence.jdbc.url"));

					if(olapProps.containsKey("javax.persistence.jdbc.user"))
						config.setOlapUsername(olapProps.get("javax.persistence.jdbc.user"));
					else
						config.setOlapUsername("root");

					if(olapProps.containsKey("javax.persistence.jdbc.password"))
						config.setOlapPassword(olapProps.get("javax.persistence.jdbc.password"));
					else
						config.setOlapPassword("ussdgw");

					if(olapProps.containsKey("javax.persistence.jdbc.driver"))
						config.setOlapDatabaseDriver(olapProps.get("javax.persistence.jdbc.driver"));
					else
						config.setOlapDatabaseDriver("com.mysql.jdbc.Driver");

					if(olapProps.containsKey("hibernate.dialect"))
						config.setOlapDatabaseDialect(olapProps.get("hibernate.dialect"));
					else
						config.setOlapDatabaseDialect("org.hibernate.dialect.MySQL5InnoDBDialect");

					if(olapProps.containsKey("hibernate.c3p0.timeout"))
					{
						try {
							config.setOlapDatabaseConnectionTimeout(Integer.parseInt(olapProps.get("hibernate.c3p0.timeout")));
						} catch (NumberFormatException e) {
							logger.error("Could not parse OLAP configuration value hibernate.c3p0.timeout", e);
						} catch (ValidationException e) {
							logger.error("OLAP configuration validation exception hibernate.c3p0.timeout", e);
						}
					} else {
						try {
							config.setOlapDatabaseConnectionTimeout(500);
						} catch (ValidationException e) {
							logger.error("OLAP configuration validation exception hibernate.c3p0.timeout", e);
						}
					}
					if(olapProps.containsKey("hibernate.c3p0.min_size"))
					{
						try {
							config.setOlapDatabaseMinPoolSize(Integer.parseInt(olapProps.get("hibernate.c3p0.min_size")));
						} catch (NumberFormatException e) {
							logger.error("Could not parse OLAP configuration value hibernate.c3p0.min_size", e);
						} catch (ValidationException e) {
							logger.error("OLAP configuration validation exception hibernate.c3p0.min_size", e);
						}
					} else {
						try {
							config.setOlapDatabaseMinPoolSize(5);
						} catch (ValidationException e) {
							logger.error("OLAP configuration validation exception hibernate.c3p0.min_size", e);
						}
					}
					if(olapProps.containsKey("hibernate.c3p0.max_size"))
					{
						try {
							config.setOlapDatabaseMaxPoolSize(Integer.parseInt(olapProps.get("hibernate.c3p0.max_size")));
						} catch (NumberFormatException e) {
							logger.error("Could not parse OLAP configuration value hibernate.c3p0.max_size", e);
						} catch (ValidationException e) {
							logger.error("OLAP configuration validation exception hibernate.c3p0.max_size", e);
						}
					} else {
						try {
							config.setOlapDatabaseMaxPoolSize(100);
						} catch (ValidationException e) {
							logger.error("OLAP configuration validation exception hibernate.c3p0.max_size", e);
						}
					}
					if(olapProps.containsKey("hibernate.c3p0.max_statements"))
					{
						try {
							config.setOlapDatabaseMaxStatements(Integer.parseInt(olapProps.get("hibernate.c3p0.max_statements")));
						} catch (NumberFormatException e) {
							logger.error("Could not parse OLAP configuration value hibernate.c3p0.max_statements", e);
						} catch (ValidationException e) {
							logger.error("OLAP configuration validation exception hibernate.c3p0.max_statements", e);
						}
					} else {
						try {
							config.setOlapDatabaseMaxStatements(100);
						} catch (ValidationException e) {
							logger.error("OLAP configuration validation exception hibernate.c3p0.max_statements", e);
						}
					}
					if(olapProps.containsKey("hibernate.c3p0.idle_test_period"))
					{
						try {
							config.setOlapIdleTestPeriod(Integer.parseInt(olapProps.get("hibernate.c3p0.idle_test_period")));
						} catch (NumberFormatException e) {
							logger.error("Could not parse OLAP configuration value hibernate.c3p0.idle_test_period", e);
						} catch (ValidationException e) {
							logger.error("OLAP configuration validation exception hibernate.c3p0.idle_test_period", e);
						}
					} else {
						try {
							config.setOlapIdleTestPeriod(300);
						} catch (ValidationException e) {
							logger.error("OLAP configuration validation exception hibernate.c3p0.idle_test_period", e);
						}
					}
					if(olapProps.containsKey("hibernate.c3p0.testConnectionOnCheckin"))
					{
						config.setTestOlapConnectionOnCheckin(Boolean.parseBoolean(olapProps.get("hibernate.c3p0.testConnectionOnCheckin")));
					} else {
						config.setTestOlapConnectionOnCheckin(false);
					}
					if(olapProps.containsKey("hibernate.c3p0.testConnectionOnCheckout"))
					{
						config.setTestOlapConnectionOnCheckout(Boolean.parseBoolean(olapProps.get("hibernate.c3p0.testConnectionOnCheckout")));
					} else {
						config.setTestOlapConnectionOnCheckout(true);
					}
				}
			}
			return true;
		}
	} // CreditDistributionConfig

	CreditDistributionConfig config = new CreditDistributionConfig();
	

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Notifications
	//
	// /////////////////////////////////
	public Notifications notifications = new Notifications(Properties.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	class Properties
	{

	}
	
	private boolean callLoadMRD(Session session, EntityManager em, EntityManager emAp)
	{
		boolean status = true;
		for (EntityType<?> entity : em.getEntityManagerFactory().getMetamodel().getEntities())
		{
			Class<?> cls = null;
			try
			{
				cls = entity.getBindableJavaType();
//				String className = entity.getName();
				try
				{
					Method loadMRD = cls.getDeclaredMethod("loadMRD", EntityManager.class, EntityManager.class, Session.class);
					logger.trace("Doing LoadMRD for {}", cls);
					loadMRD.invoke(null, em, emAp, session);
					continue;
				}
				catch(NoSuchMethodException ex)
				{
					logger.debug("NOT Doing LoadMRD for {} as loadMRD method does not exist on class ({})", cls, ex);
				}
				try
				{
					Method loadMRD = cls.getDeclaredMethod("loadMRD", EntityManager.class, Session.class);
					logger.trace("Doing LoadMRD for {}", cls);
					loadMRD.invoke(null, em, session);
				}
				catch(NoSuchMethodException ex)
				{
					logger.debug("NOT Doing LoadMRD for {} as loadMRD method does not exist on class ({})", cls, ex);
				}
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				logger.error("Failure in callLoadMRD()", e);
				status = false;
			}
		}
		return status;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Minimum Required Data
	//
	// /////////////////////////////////
	private synchronized boolean loadMRD(EntityManager em, EntityManager emAp)
	{
		logger.info("Loading MRD");
		// Create a super session
		Session session = sessions.getSuperSession();

		boolean status = callLoadMRD(session, em, emAp);
		if (status)
		{
			status = callLoadMRD(session, emAp, em);
		}

		// Obtain a list of companies
		if (status)
		{
			try ( EntityManagerEx cem = this.getEntityManager() )
			{
				List<Company> companies = Company.findAll(cem);
				for (Company company : companies)
				{
					if (!loadCompanyMRD(em, company.getId(), session))
						return false;
					findCompanyInfoByID(cem, company.getId());
				}
			}
		}

		return status;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ICreditDistribution
	//
	// /////////////////////////////////

	private synchronized boolean loadCompanyMRD(EntityManager em, int companyID, Session session)
	{
		logger.info("Loading Company MRD");
		try (RequiresTransaction transaction = new RequiresTransaction(em))
		{
			boolean hasRole = false;
			for (EntityType<?> entity : em.getEntityManagerFactory().getMetamodel().getEntities())
			{
				Class<?> cls = null;
				try
				{
					cls = entity.getBindableJavaType();
					if (cls.equals(Role.class))
						hasRole = true;
					Method loadMRD = cls.getDeclaredMethod("loadMRD", EntityManager.class, int.class, Session.class);
					logger.debug("Doing LoadMRD (companyID={}) for {}", companyID, cls.getName());
					loadMRD.invoke(null, em, companyID, session);
				}
				catch (NoSuchMethodException ex)
				{
					logger.debug("NOT Doing LoadMRD (companyID={}) for {} as loadMRD method does not exist on class ({})", companyID, cls.getName());
				}
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
				{
					String message = String.format("LoadMRD for Company %d entity: %s failed", companyID, entity.getName());
					logger.error(message, e);
					return false;
				}
			}

			// Ensure all Permissions Loaded
			if (hasRole)
			{
				logger.info("Loading MRD for role ...");
				Role.loadMRD(em, companyID, session);
			}
			else
			{
				logger.info("Not doing loadMRD for role (again?) ...");
			}

			transaction.commit();
		}
		catch (RuleCheckException e)
		{
			logger.info("Rulecheck Failure", e);
			return false;
		}

		return true;
	}

	@Override
	public Sessions getSessions()
	{
		return sessions;
	}

	@Override
	public Session getSession(String sessionID) throws RuleCheckException
	{
		return sessions.get(sessionID);
	}

	@Override
	public IService getService()
	{
		return service;
	}

	@Override
	public IServiceBus getServiceBus()
	{
		return esb;
	}

	@Override
	public ISnmpConnector getSnmpConnector()
	{
		return snmpConnector;
	}

	@Override
	public void writeTDR(Transaction transaction) throws IOException
	{
		tdrWriter.write(transaction);
	}

	@Override
	public IAirConnector getAirConnector()
	{
		return air;
	}

	@Override
	public int getMoneyScale()
	{
		return locale.getCurrencyDecimalDigits();
	}

	@Override
	public CompanyInfo findCompanyInfoByID(int companyID)
	{
		// Attempt to read it from Cache
		CompanyInfo companyInfo = companyMap.get(companyID);
		if (companyInfo != null)
			return companyInfo;

		// Read from Database
		try (EntityManagerEx em = getEntityManager())
		{
			return findCompanyInfoByID(em, companyID);
		}
	}

	@Override
	public CompanyInfo findCompanyInfoByID(EntityManager em, int companyID)
	{
		// Attempt to read it from Cache
		CompanyInfo companyInfo = companyMap.get(companyID);
		if (companyInfo != null)
			return companyInfo;

		// Load from Database
		Company company = Company.findByID(em, companyID);
		if (company == null)
			return null;

		// Cache it
		companyInfo = new CompanyInfo(company, this);
		companyMap.put(companyID, companyInfo);

		return companyInfo;
	}

	/*
	@Override
	public EntityManagerFactory getEntityManagerFactory()
	{
		return this.entityManagerFactory;
	}

	@Override
	public EntityManagerFactory getApEntityManagerFactory()
	{
		return this.entityManagerOlapFactory;
	}
	*/

	@Override
	public EntityManagerEx getEntityManager()
	{
		EntityManagerEx em = null;
		try {
			em = EntityManagerEx.create(this.oltpEntityManagerFactory, this.oltpPooledDataSource);
			em.setFlushMode(FlushModeType.COMMIT);
			DbUtils.makeRepeatableRead(em);
			return em;
		}
		catch( Throwable throwable ) {
			if (em != null) em.close();
			throw throwable;
		}
	}

	@Override
	public EntityManagerEx getApEntityManager()
	{
		EntityManagerEx em = null;
		try {
			em = EntityManagerEx.create(this.olapEntityManagerFactory, this.olapPooledDataSource);
			em.setFlushMode(FlushModeType.COMMIT);
			DbUtils.makeRepeatableRead(em);
			return em;
		}
		catch( Throwable throwable ) {
			if (em != null) em.close();
			throw throwable;
		}
	}

	@Override
	public NumberFormat getCurrencyFormat(Locale locale)
	{
		Objects.requireNonNull(localeCustomisationMap, "localeCustomisationMap may not be null");
		LocaleKey localeKey = new LocaleKey(locale);
		//logger.trace("Checking for locale customisation for locale {} in {}", localeKey, localeCustomisationMap);
		if (localeCustomisationMap.containsKey(localeKey))
		{
			//logger.trace("Found locale customisation for locale {}", localeKey);
			LocaleCustomisation localeCustomisation = this.localeCustomisationMap.get(localeKey);
			Objects.requireNonNull(localeCustomisation, "localeCustomisation may not be null");
			return localeCustomisation.getCurrencyFormat();
		}
		else
		{
			logger.trace("No locale customisation for locale {}", localeKey);
			return NumberFormat.getCurrencyInstance(locale);
		}
	}

	@Override
	public ISmtpConnector getSmtpConnector()
	{
		return this.smtpConnector;
	}

	@Override
	public void sendSMS(String msisdn, final String languageCode, final String text)
	{
		sendSMS(config.smsSourceAddress, msisdn, languageCode, text);
	}

	@Override
	public void sendSMS(String source, String msisdn, final String languageCode, final String text)
	{
		if (msisdn == null)
		{
			logger.error("Attempted to send SMS with null msisdn");
			return;
		}
		if (msisdn.isEmpty())
		{
			logger.error("Attempted to send SMS with empty msisdn");
			return;
		}
		if (text == null)
		{
			logger.error("Attempted to send SMS with null text");
			return;
		}
		if (text.isEmpty())
		{
			logger.error("Attempted to send SMS with empty text");
			return;
		}

		INotificationText nt = new INotificationText()
		{
			@Override
			public String getText()
			{
				return text;
			}

			@Override
			public String getLanguageCode()
			{
				return languageCode;
			}
		};

		smsConnector.send(source, msisdn, nt);

	}

	@Override
	public IAuthenticator getAuthenticator()
	{
		return authenticator;
	}

	@Override
	public void defineChannelFilter(IChannelTarget target, int companyID, hxc.ecds.protocol.rest.config.Phrase command, hxc.ecds.protocol.rest.config.Phrase[] fields, int tag)
	{
		channelManager.defineChannelFilter(target, companyID, command, fields, tag);
	}

	@Override
	public IHlrInformation getHlrInformation(String msisdn, boolean needLocation, boolean needMnp, boolean needImsi)
	{
		try
		{
			return hlr.getInformation(msisdn, needLocation, needMnp, needImsi);
		}
		catch (Throwable e)
		{
			logger.error("Unable to obtain location information for msisdn {}, {}", msisdn, e.getMessage());
		}
		return null;
	}

    public static String fetchImsi(String msisdn, ICreditDistribution context) {
        IHlrInformation info = context.getHlrInformation(msisdn, false, false, true);
        if (info != null) {
            return info.getIMSI();
        }
        return null;
    }

	@Override
	public String getImei(String msisdn)
	{
		return cai.getImei(msisdn);
	}

	@Override
	public boolean processUssd(IInteraction interaction)
	{
		boolean result = channelManager.execute(interaction, null);
		logger.trace("interaction = {} -> result = {}", interaction, result);
		logger.info("interaction = {} -> result = {}", interaction, result);
		if (!result)
		{
			INotificationText responce = new INotificationText()
			{
				@Override
				public String getText()
				{
					return config.invalidUssdCommandNotification;
				}

				@Override
				public String getLanguageCode()
				{
					return Phrase.ENG;
				}

			};
			interaction.reply(responce);
		}
		return result;
	}

	@Override
	public void throttleTps()
	{
		esb.countTPS();
	}

	@Override
	public String toMSISDN(String number)
	{
		return config.useInternationalNumbers ? numberPlan.getInternationalFormat(number) : numberPlan.getNationalFormat(number);
	}

	@Override
	public IBundleProvider getBundleProvider()
	{
		return bundleProvider;
	}

	@Override
	public boolean isMasterServer()
	{
		return control.isIncumbent(ICtrlConnector.DATABASE_ROLE);
	}

	@Override
	public void assignTsNumber(boolean assign)
	{
		MDC.put(LoggingConstants.CONST_LOG_TRANSID, assign ? esb.getNextTransactionNumber(TS_NUMBER_LENGTH) : "");
	}

	@Override
	public String getInvalidUssdCommandNotification()
	{
		return config.getInvalidUssdCommandNotification();
	}
		
	@Override
	public QueryToken getQueryToken()
	{		
		return QueryToken.aquire(this, querySemaphores);
	}


	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Restful Server
	//
	// /////////////////////////////////
	private boolean startRestfulServer() throws IOException
	{
		boolean secure;
		String namespace = Authentication.class.getPackage().getName();
		final ResourceConfig resourceConfig = new ResourceConfig().packages(namespace);
		resourceConfig.register(new RestExtenders.RestExceptionMapper());
		RequestNumberFilter.setContext(this);
		resourceConfig.register(RequestNumberFilter.class);
		resourceConfig.register(new RestExtenders.DependencyBinder((ICreditDistribution) this));
		resourceConfig.register(new JacksonFeature());
		{
			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
			mapper.registerModule(new JSR310Module());

			JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
			provider.setMapper(mapper);
			resourceConfig.register(provider);
		}

		// This mess is to make Jersey log through SLF4J.
		java.util.logging.Logger jerseyLogger = java.util.logging.Logger.getLogger(CreditDistribution.class.getName());
		for (java.util.logging.Handler handler : jerseyLogger.getHandlers() )
		{
			jerseyLogger.removeHandler(handler);
		}
		jerseyLogger.setUseParentHandlers(false);
		jerseyLogger.setLevel(Level.ALL);
		SLF4JBridgeHandler slf4jBridgeHandler = new SLF4JBridgeHandler();
		logger.trace("slf4jBridgeHandler.getLevel() = {}", slf4jBridgeHandler.getLevel());
		jerseyLogger.addHandler(slf4jBridgeHandler);
		logger.trace("jerseyLogger.getHandlers() = {}", Arrays.asList(jerseyLogger.getHandlers()));
		logger.trace("jerseyLogger.isLoggable(Level.FINEST) = {}, ", jerseyLogger.isLoggable(Level.FINEST));
		logger.trace("jerseyLogger.getLevel() = {}", jerseyLogger.getLevel());
		resourceConfig.register(new LoggingFeature(jerseyLogger, Level.FINEST, LoggingFeature.Verbosity.PAYLOAD_ANY, null));
		logger.trace("resourceConfig.getResources() = {}", resourceConfig.getResources());


		/*
		// https://jersey.java.net/documentation/latest/logging_chapter.html
		if (logger.isTraceEnabled())
		{
			logger.warn("RESTFul request/response traces are visible in logs. Disable them on production!");
			// FIXME Why have dependency on another logging implementation?  Replace this shit with SLF4J
			resourceConfig.register(new LoggingFeature(localLogger, Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, null));
			
		}
		*/
		

		// Enable TLS support
		// On deployment, a connection with activated SSL is mandatory
		SSLEngineConfigurator engineConf = createSslConfiguration();
		secure = (engineConf != null);

		URI uriRoot = config.getRestURI(secure);

		restServer = GrizzlyHttpServerFactory.createHttpServer(uriRoot, resourceConfig, secure, engineConf, false);
		logger.trace("Grizzly resource config settings: {}", resourceConfig.toString());
		tweakGrizzlyServerConfig();

		restServer.start();

		this.restClasses = resourceConfig.getClasses();
		return secure;
	}

	public class DependencyBinder extends AbstractBinder
	{

		private ICreditDistribution context;

		DependencyBinder(ICreditDistribution c)
		{
			context = c;
		}

		@Override
		protected void configure()
		{
			bindFactory(new ICreditDistributionFactory(context)).to(ICreditDistribution.class).in(Singleton.class);
		}

	}

	public class ICreditDistributionFactory implements Factory<ICreditDistribution>
	{
		@Context
		private ICreditDistribution context;

		public ICreditDistributionFactory(ICreditDistribution c)
		{
			context = c;
		}

		@Override
		public ICreditDistribution provide()
		{
			return context;
		}

		@Override
		public void dispose(ICreditDistribution arg0)
		{

		}
	}

	private SSLEngineConfigurator createSslConfiguration()
	{
		if (!config.getTLSEnabled())
		{
			logger.info("TLS support disabled. No security connection for this instance.");
			return null;
		}

		try
		{
			logger.trace("Creating SSLEngineConfigurator ... ( keyStorePassword = {}, trustStorePassword = {}, keyStorePath = {}, trustStorePath = {}, requireClientAuth = {} )",
				config.getKeyStorePassword().hashCode(), config.getTrustStorePassword().hashCode(),
				config.getKeyStorePath(), config.getTrustStorePath(), config.getRequireClientAuth() );
			final String KEYSTORE_SERVER_PWD = config.getKeyStorePassword();
			final String TRUSTORE_SERVER_PWD = config.getTrustStorePassword();
			final String keyStorePath = config.getKeyStorePath();
			final String trustStorePath = config.getTrustStorePath();
			boolean requireClientAuth = config.getRequireClientAuth();

			// Set key store
			// Initialize SSLContext configuration
			SSLContextConfigurator sslContextConfig = new SSLContextConfigurator();

			File keyStorePathFile = new File(keyStorePath);
			try
			{
				if ( !keyStorePathFile.exists() )
				{
					logger.trace("keyStorePath {} does not exist ... creating", keyStorePath);
					KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
					keyStore.load(null, null);
					File keyStorePathParentFile = keyStorePathFile.getParentFile();
					if ( !keyStorePathParentFile.exists() )
					{
						logger.trace("keyStorePath {} does not exist ... creating", keyStorePath);
						keyStorePathParentFile.mkdirs();
					}
						SecureRandom random = new SecureRandom();
						DSAParametersGenerator parametersGenerator = new DSAParametersGenerator(DigestFactory.createSHA1());
						parametersGenerator.init(1024, 80, random);
						DSAParameters parameters = parametersGenerator.generateParameters();
//						DSAValidationParameters validationParameters = parameters.getValidationParameters();
						DSAKeyPairGenerator keyPairGenerator = new DSAKeyPairGenerator();
						DSAKeyGenerationParameters keyGenerationParameters = new DSAKeyGenerationParameters(random, parameters);
						keyPairGenerator.init(keyGenerationParameters);
						AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();

						DefaultSignatureAlgorithmIdentifierFinder signatureAlgorithmFinder = new DefaultSignatureAlgorithmIdentifierFinder();
						DefaultDigestAlgorithmIdentifierFinder digestAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();

						AlgorithmIdentifier signatureAlgorithm = signatureAlgorithmFinder.find("SHA1withDSA");
						AlgorithmIdentifier digestAlgorithm = digestAlgorithmFinder.find(signatureAlgorithm);

						ContentSigner contentSigner = new BcDSAContentSignerBuilder(signatureAlgorithm, digestAlgorithm).build(keyPair.getPrivate());
						/*
						KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
						keyPairGenerator.initialize(1024);
						KeyPair keyPair = keyPairGenerator.generateKeyPair();
						*/
						Calendar now = Calendar.getInstance();
						Calendar validityStart = (Calendar)now.clone();
						validityStart.add(Calendar.YEAR, -25);
						Calendar validityEnd = (Calendar)now.clone();
						validityEnd.add(Calendar.YEAR, 25);

						X500Name subject = new X500Name("CN=localhost");
						X509v3CertificateBuilder certificateBuilder = new BcX509v3CertificateBuilder(
							subject, BigInteger.valueOf(System.currentTimeMillis()),
							validityStart.getTime(), validityEnd.getTime(),
							subject, keyPair.getPublic()
						);
						X509CertificateHolder certificateHolder = certificateBuilder.build(contentSigner);
						/*
						V3TBSCertificateGenerator certificateGenerator = new V3TBSCertificateGenerator();
						certificateGenerator.setSerialNumber(new DERInteger(BigInteger.valueOf(System.currentTimeMillis())));
						certificateGenerator.setIssuer(PrincipalUtil.getSubjectX509Principal(caCert));
						CertificateValidity certificateValidity = new CertificateValidity(validityStart.getTime(), validityStart.getTime());
						x509CertInfo.set(X509CertInfo.VALIDITY, certificateValidity);
						*/
						DSAPrivateKeyParameters privateKeyParameters = (DSAPrivateKeyParameters) keyPair.getPrivate();
						DSAParameters privateParameters = privateKeyParameters.getParameters();
						DSAPrivateKeySpec privateKeySpec = new DSAPrivateKeySpec(
							privateKeyParameters.getX(),
							privateParameters.getP(),
							privateParameters.getQ(),
							privateParameters.getG()
						);
						PrivateKey privateKey = KeyFactory.getInstance("DSA").generatePrivate(privateKeySpec);
						InputStream inputStream = new ByteArrayInputStream(certificateHolder.getEncoded());
						Certificate certificate = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME).generateCertificate(inputStream);
						keyStore.setKeyEntry("server", privateKey, KEYSTORE_SERVER_PWD.toCharArray(), new Certificate[]{certificate});

					if ( keyStorePathFile.createNewFile() )
					{
						keyStore.store(new FileOutputStream(keyStorePathFile, false), KEYSTORE_SERVER_PWD.toCharArray());
					}
				}
			}
			catch(Throwable throwable)
			{
				logger.error("Exception while trying to create certificate", throwable);
			}
			sslContextConfig.setKeyStoreFile(keyStorePath); // contains server key pair
			sslContextConfig.setKeyStorePass(KEYSTORE_SERVER_PWD);

			// Set trustStore
			if ( requireClientAuth )
			{
				sslContextConfig.setTrustStoreFile(trustStorePath); // contains client certificate
				sslContextConfig.setTrustStorePass(TRUSTORE_SERVER_PWD);
			}

			// Explicitly set but also by default TLS.
			sslContextConfig.setSecurityProtocol("TLS");

			// Require client authentication
			// setNeedClientAuth is used to request and require client certificate authentication:
			// the connection will terminate if no suitable client certificate is presented.
			//final boolean needClientAuthentication = true;
			final boolean validSSLConfig = sslContextConfig.validateConfiguration(true);

			if (!validSSLConfig)
			{
				logger.error("TLS configuration failure. Check trust and key stores paths and passwords");
				return null;
			}

			// Create SSLEngine instance
			return new SSLEngineConfigurator(sslContextConfig.createSSLContext(), false, requireClientAuth, false);

		}
		catch (Exception ex)
		{
			logger.error("Error creating SSLEngineConfigurator", ex);
		}

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// JPA Entity Manager Factory
	//
	// /////////////////////////////////

	public EntityManagerFactory createOltpEntityManagerFactory(String persistenceUnit, String datasourceName) throws Exception
	{
		EntityManagerFactory factory = null;
		Map<String, String> props = new HashMap<String, String>();
		File configFile = new File(oltpConfigFileName);
		if (configFile.exists())
		{
			props = readPropertiesFile(oltpConfigFileName);
		} else {
			props.put("javax.persistence.jdbc.url", config.getDatabaseConnectionString() + "&logger=com.mysql.jdbc.log.Slf4JLogger");
			props.put("javax.persistence.jdbc.user", config.getOltpUsername());
			props.put("javax.persistence.jdbc.password", config.getOltpPassword());
			props.put("javax.persistence.jdbc.driver", config.getOltpDatabaseDriver());
			props.put("hibernate.connection.isolation", "TRANSACTION_REPEATABLE_READ");
			props.put("hibernate.dialect", config.getOltpDatabaseDialect());
			props.put("hibernate.c3p0.timeout", Integer.toString(config.getOltpDatabaseConnectionTimeout()));
			props.put("hibernate.c3p0.min_size", Integer.toString(config.getOltpDatabaseMinPoolSize()));
			props.put("hibernate.c3p0.max_size", Integer.toString(config.getOltpDatabaseMaxPoolSize()));
			props.put("hibernate.c3p0.max_statements", Integer.toString(config.getOltpDatabaseMaxStatements()));
			props.put("hibernate.c3p0.idle_test_period", Integer.toString(config.getOltpIdleTestPeriod()));		
			props.put("hibernate.c3p0.testConnectionOnCheckin", Boolean.toString(config.getTestOltpConnectionOnCheckin()));
			props.put("hibernate.c3p0.testConnectionOnCheckout", Boolean.toString(config.getTestOltpConnectionOnCheckout()));
			props.put("hibernate.show_sql", "false");
			props.put("hibernate.format_sql", "false");
			props.put("hibernate.hbm2ddl.auto", "validate");
			props.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
			// Removed interceptor due to performance issues
			//props.put("hibernate.ejb.interceptor", "hxc.services.ecds.util.MySqlInterceptor");
			createPropertiesFile(oltpConfigFileName, props);
		}
		if(datasourceName != null && !datasourceName.isEmpty())
			props.put("hibernate.c3p0.dataSourceName", datasourceName);
		// Main Performance impact
		logger.info("OLTP hibernate.c3p0.min_size: {}", props.get("hibernate.c3p0.min_size"));
		logger.info("OLTP hibernate.c3p0.max_size: {}", props.get("hibernate.c3p0.max_size"));
		logger.info("OLTP hibernate.c3p0.max_statements: {}", props.get("hibernate.c3p0.max_statements"));

		logger.trace("createOltpEntityManagerFactory:Persistence.createEntityManagerFactory:before: props = {}", props);
		factory = Persistence.createEntityManagerFactory("ecds", props);
		logger.trace("createOltpEntityManagerFactory:Persistence.createEntityManagerFactory:after...");
		return factory;
	}

	public EntityManagerFactory createOlapEntityManagerFactory(String persistenceUnit, String datasourceName)
	{
		// Redirect subsystem Loggers
		// logger.redirectSubsystemLogging("org.hibernate");
		// logger.redirectSubsystemLogging("org.glassfish.grizzly");
		EntityManagerFactory factory = null;
		logger.info("Creating AP Entity Manager Factory ...");
		Map<String, String> props = new HashMap<String, String>();
		File configFile = new File(olapConfigFileName);
		if (configFile.exists())
		{
			props = readPropertiesFile(olapConfigFileName);
		} else {
			props.put("javax.persistence.jdbc.url", config.getApDatabaseConnectionString() + "&logger=com.mysql.jdbc.log.Slf4JLogger");
			props.put("javax.persistence.jdbc.user", config.getOlapUsername());
			props.put("javax.persistence.jdbc.password", config.getOlapPassword());
			props.put("javax.persistence.jdbc.driver", config.getOlapDatabaseDriver());
			props.put("hibernate.dialect", config.getOlapDatabaseDialect());
			props.put("hibernate.c3p0.min_size", Integer.toString(config.getOlapDatabaseMinPoolSize()));
			props.put("hibernate.c3p0.max_size", Integer.toString(config.getOlapDatabaseMaxPoolSize()));
			props.put("hibernate.c3p0.timeout", Integer.toString(config.getOlapDatabaseConnectionTimeout()));
			props.put("hibernate.c3p0.max_statements", Integer.toString(config.getOlapDatabaseMaxStatements()));
			props.put("hibernate.c3p0.idle_test_period", Integer.toString(config.getOlapIdleTestPeriod()));
			props.put("hibernate.c3p0.testConnectionOnCheckin", Boolean.toString(config.getTestOlapConnectionOnCheckin()));
			props.put("hibernate.c3p0.testConnectionOnCheckout", Boolean.toString(config.getTestOlapConnectionOnCheckout()));
			props.put("hibernate.hbm2ddl.auto", "validate");
			props.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
			props.put("hibernate.connection.isolation", "TRANSACTION_REPEATABLE_READ");
			props.put("hibernate.show_sql", "false");
			props.put("hibernate.format_sql", "false");
			
			// Removed interceptor due to performance issues
			props.put("hibernate.ejb.interceptor", "hxc.services.ecds.util.OlapInterceptorV3");
			createPropertiesFile(olapConfigFileName, props);
		}
		if(datasourceName != null && !datasourceName.isEmpty())
			props.put("hibernate.c3p0.dataSourceName", datasourceName);
		logger.trace("createOlapEntityManagerFactory:Persistence.createEntityManagerFactory:before: props = {}", props);
		factory = Persistence.createEntityManagerFactory(persistenceUnit, props);
		logger.trace("createOlapEntityManagerFactory:Persistence.createEntityManagerFactory:after...");
		return factory;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// JPA Entity Manager Factory Helper Functions
	//
	// /////////////////////////////////
	
	private static void createPropertiesFile(String configFilename, Map<String, String> props)
	{
		java.util.Properties properties = new java.util.Properties();
		try (OutputStream os = new FileOutputStream(configFilename))
		{
			for(String key: props.keySet())
			{
				String value = props.get(key);
				if("javax.persistence.jdbc.password".equals(key))
				{
					value = AesUtils.encrypt(value);
				}
				properties.setProperty(key, value);
			}
			properties.storeToXML(os, "MySQL Connection Parameters");
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	private static Map<String, String> readPropertiesFile(String configFilename)
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
				{
					value = AesUtils.decrypt(value);
				}
				props.put(key, value);
			}
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return props;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Channel Filters
	//
	// /////////////////////////////////

	// Initialise channelFilters
	private void initialiseChannelFilters()
	{
		try (EntityManagerEx em = getEntityManager())
		{
			for (Class<?> restClass : restClasses)
			{
				if (!IChannelTarget.class.isAssignableFrom(restClass))
					continue;

				IChannelTarget target;
				try
				{
					target = (IChannelTarget) restClass.newInstance();
				}
				catch (InstantiationException | IllegalAccessException e)
				{
					logger.warn("Cannot instantiate "+restClass.getName(), e);
					continue;
				}

				for (CompanyInfo company : companyMap.values())
				{
					target.defineChannelFilters(em, this, company);
				}
			}
		}
	}

	@Override
	public IAuthenticator.Result tryAuthenticate(WebUser user, String password)
	{
		try
		{
			if (user != null && password != null)
			{
				if (user.getDomainAccountName().equals(hxc.services.ecds.model.WebUser.NAME_SUPPLIER))
				{
					IAuthenticator.Result result = new IAuthenticator.Result();
					result.code = Result.UNKNOWN_FAILURE;

					if (authenticateSpecialUser(user.getDomainAccountName(), password))
						result.code = Result.SUCCESS;

					return result;
				}

				if (user.getDomainAccountName() != null && authenticator != null)
				{
					IAuthenticator.Result result = authenticator.authenticate(user.getDomainAccountName(), password);
					logger.trace("authenticator.authenticate -> ({}, {})", String.valueOf(result.code), result.description);
					return result;
				}
				else
				{
					logger.error("tryAuthenticate: user.domainAccountName or authenticator {}", authenticator.toString());
				}
			}
			else
			{
				logger.error("tryAuthenticate: user {} or password is null", user.toString());
			}

		}
		catch (Exception e)
		{
			logger.error("Authentication error", e);
		}

		return new IAuthenticator.Result();
	}

	@Override
	public IAuthenticator.Result tryAuthenticate(IAgentUser abstractAgentUser, String password)
	{
		try
		{
			if (abstractAgentUser != null && password != null)
			{
				if (abstractAgentUser.getDomainAccountName() != null)
				{
					if (authenticator != null)
					{
						IAuthenticator.Result result = authenticator.authenticate(abstractAgentUser.getDomainAccountName(), password);
						logger.trace("authenticator.authenticate -> ({}, {})", result.code, result.description);
						return result;
					}
					else
					{
						logger.info("tryAuthenticate: authenticator {}", authenticator);
					}
				}
				else if (abstractAgentUser.getMobileNumber() != null)
				{
					IAuthenticator.Result result = new IAuthenticator.Result();
					if (abstractAgentUser.testIfSamePin(password))
					{
						result.code = Result.SUCCESS;
					}
					else
					{
						result.code = Result.KRB_AP_ERR_BAD_INTEGRITY;
					}
					return result;
				}
				else
				{
					logger.error("tryAuthenticate: abstractAgentUser does not have domainAccountName or MSISDN ... ID was {}", abstractAgentUser.getId());
				}
			}
			else
			{
				logger.error("tryAuthenticate: abstractAgentUser {} or password is null", abstractAgentUser);
			}
		}
		catch (Exception e)
		{
			logger.error("Authentication failure", e);
		}

		return new IAuthenticator.Result();
	}

	@Override
	public int tryChangePassword(String user, String oldPassword, String newPassword)
	{
		try
		{
			if (user != null && oldPassword != null && newPassword != null)
			{
				if (authenticator != null)
					return authenticator.changePassword(user, oldPassword, newPassword);
			}
		}
		catch (Exception e)
		{
			logger.info("tryChangePassword error", e);
		}

		return IAuthenticator.Result.UNKNOWN_FAILURE;

	}

	private byte[] encrypt(String password, byte[] publicKey)
	{
		try
		{
			byte[] passwordBytes = password.getBytes("utf-8");
			byte[] credentials = new byte[passwordBytes.length + publicKey.length];
			System.arraycopy(passwordBytes, 0, credentials, 0, passwordBytes.length);
			System.arraycopy(publicKey, 0, credentials, passwordBytes.length, publicKey.length);
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			return md.digest(credentials);
		}
		catch (UnsupportedEncodingException | NoSuchAlgorithmException e)
		{
			logger.warn("encryption error", e);
		}

		return null;
	}

	public boolean authenticateSpecialUser(String userName, String password)
	{
		// User Authentication
		try
		{
			if (security != null)
			{
				IUser user = null;
				byte[] publicKeyBytes = security.getPublicKey(userName);
				user = security.authenticate(userName, encrypt(password, publicKeyBytes));

				if (user != null)
					return (user.getName().equals(userName));
			}
			else
			{
				logger.info("Failed to authenticate special user {}", userName);
			}
		}
		catch (SecurityException ex)
		{
			logger.warn("authenticateSpecialUser security error", ex);
		}
		catch (Exception ex)
		{
			logger.error("authenticateSpecialUser error", ex);
		}

		return false;

	}

	void tweakGrizzlyServerConfig()
	{
		// https://grizzly.java.net/bestpractices.html
		TCPNIOTransport transport = restServer.getListener("grizzly").getTransport();
		{
			int defaultSelectorRunnersCount = transport.getSelectorRunnersCount();
			int defaultCorePoolSize = transport.getWorkerThreadPoolConfig().getCorePoolSize();
			int defaultMaxPoolSize = transport.getWorkerThreadPoolConfig().getMaxPoolSize();
			int defaultQueueLimit = transport.getWorkerThreadPoolConfig().getQueueLimit();
			logger.trace("Grizzly Worker Thread Pool Defaults: selectorRunnersCount = {}; corePoolSize = {}; maxPoolSize = {}; queueLimit = {};", defaultSelectorRunnersCount, defaultCorePoolSize, defaultMaxPoolSize, defaultQueueLimit);
		}

		/*
		 * corePoolSize The initial number of threads that will be present with the thread pool is created. maxPoolSize The maximum number threads that may be maintained by this thread pool.
		 * keepAliveTime The maximum time a thread may stay idle and wait for a new task to execute before it will be released. Custom time units can be used. transactionTimeout The maximum time a
		 * thread may be allowed to run a single task before interrupt signal will be sent. Custom time units can be used.
		 */

		ThreadPoolConfig threadPoolConfig = ThreadPoolConfig.defaultConfig()
				.setPoolName("grizzly-worker-thread-")
				.setCorePoolSize(config.getGrizzlyCorePoolSize())
				.setMaxPoolSize(config.getGrizzlyMaxPoolSize())
				.setQueueLimit(config.getGrizzlyQueueLimit())
				.setKeepAliveTime(config.getGrizzlyKeepAliveSeconds(), TimeUnit.SECONDS);

		transport.configureBlocking(false);
		transport.setSelectorRunnersCount(config.getGrizzlySelectorRunners());
		transport.setWorkerThreadPoolConfig(threadPoolConfig);
		transport.setIOStrategy(WorkerThreadIOStrategy.getInstance());	
		transport.setServerConnectionBackLog(config.getGrizzlyBacklogSize());
		transport.setTcpNoDelay(true);

		transport.getThreadPoolMonitoringConfig().addProbes(new HuxThreadPoolProbe(threadPoolSnapshot));
		transport.getConnectionMonitoringConfig().addProbes(new HuxConnectionProbe(connectionSnapshot));
		
		logRestServerSettings(transport);
		
		//DEBUG
		String threadPoolConfigString =  threadPoolConfig.toString().replace("\n", " ").replace("\r", "");
		logger.info("Grizzly thread pool config settings: {}", threadPoolConfigString);
		logger.info("Grizzly Server Optimization enabled");
	}

	void logRestServerSettings(TCPNIOTransport transport) {
		int selectorRunnersCount = transport.getSelectorRunnersCount();
		int corePoolSize = transport.getWorkerThreadPoolConfig().getCorePoolSize();
		int maxPoolSize = transport.getWorkerThreadPoolConfig().getMaxPoolSize();
		int queueLimit = transport.getWorkerThreadPoolConfig().getQueueLimit();
		int backLog = transport.getServerConnectionBackLog();
		logger.info("Current Grizzly Configuration: " +
							"selectorRunnersCount = {}; corePoolSize = {}; maxPoolSize = {}; queueLimit = {}; backLog: {}",
					selectorRunnersCount, corePoolSize, maxPoolSize, queueLimit, backLog);
	}
	
	public boolean isAgentTaggedForCallback(int agentID)
	{
		return agentNotification.containsKey(agentID);
	}
	
	public HashSet<ICallbackItem> getCallbackItems(int agentID)
	{
		if(agentNotification.containsKey(agentID))
		{
			HashSet<ICallbackItem> callbackItems = agentNotification.get(agentID);
			return callbackItems;
		}
		return null;
	}
	
	public void setAgentTaggedForCallback(RegisterTransactionNotificationRequest request)
	{
		CallbackItem callbackItem = new CallbackItem();		
		int agentID = request.getAgentID();
		String sessionID = request.getSessionID();
		callbackItem.setAgentID(agentID);
		callbackItem.setSessionID(sessionID);
		callbackItem.setBaseUri(request.getBaseUri());
		callbackItem.setCallbackUriPath(request.getCallbackUriPath());
		callbackItem.setTokenUriPath(request.getTokenUriPath());
		callbackItem.setTransactionNo(request.getTransactionNo());
		callbackItem.setOffset(request.getOffset());
		callbackItem.setLimit(request.getLimit());
		if(isAgentTaggedForCallback(agentID))
		{
			HashSet<ICallbackItem> sessions = agentNotification.get(agentID);
			for(ICallbackItem item : sessions)
			{
				if(item.getSessionID().equals(sessionID))
				{
					sessions.remove(item);
				}
			}
			sessions.add(callbackItem);
		} else {
			HashSet<ICallbackItem> sessions = new HashSet<ICallbackItem>();
			sessions.add(callbackItem);
			agentNotification.put(agentID, sessions);
		}
	}
	
	public void deregisterSessionFromCallback(String sessionID, int agentID)
	{
		if(isAgentTaggedForCallback(agentID))
		{
			HashSet<ICallbackItem> sessions = agentNotification.get(agentID);
			CallbackItem remove = new CallbackItem(sessionID);
			if(sessions.contains(remove))
			{
				sessions.remove(remove);
			}
			if(sessions.isEmpty())
			{
				agentNotification.remove(agentID);//no sessions registered with agent, therefore remove agent from callback registry.
			}
		} else {
			logger.error("Agent is not registered for call back.");
		}
	}
	
	@Override
	public void pushTransactionNotification(String sessionID, int agentID, String baseUri, String tokenUriPath, String callbackUriPath, List<? extends hxc.ecds.protocol.rest.Transaction> transactions, boolean isAAgent) throws Exception
	{
		ecdsApiConnector.notifyTransactions(sessionID, baseUri, tokenUriPath, callbackUriPath, transactions);
	}
	
	@Override
	public Object getCallbackItemsLock()
	{
		return agentNotificationLock;
	}
}
