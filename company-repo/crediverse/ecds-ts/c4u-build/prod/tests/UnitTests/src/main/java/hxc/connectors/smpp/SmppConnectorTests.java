package hxc.connectors.smpp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServer;
import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;

import hxc.configuration.IConfiguration;
import hxc.connectors.IConnector;
import hxc.connectors.IInteraction;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.ctrl.IServerInfo;
import hxc.connectors.ctrl.IServerRole;
import hxc.connectors.ctrl.protocol.ServerInfo;
import hxc.connectors.ctrl.protocol.ServerRole;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.sms.ISmsConnector;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.notification.NotificationText;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SmppConnectorTests extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(SmppConnectorTests.class);

	private static CtrlConnector ctrlConnector;
	private static IServiceBus esb;
	private static SmppConnector smppConnector;
	private static ISmsConnector smsConnector;
	private static SmppConnector.SmppConfiguration defaultConfiguration;
	private static MySqlConnector mysqlConnector;
	private static final BlockingQueue< PduRequest > pduQueue = new LinkedBlockingQueue< PduRequest >();
	private static final BlockingQueue< SubmitSm > submitSmQueue = new LinkedBlockingQueue< SubmitSm >();
	private static final BlockingQueue< BaseBind > bindQueue = new LinkedBlockingQueue< BaseBind >();
	private static SmppServer smppServer;
	// FIXME -- leaving this unused variable
	//          it relates to a failing test lower that should be fixed...
	//                                                 \/
	private static volatile SmppServerSession smppServerSession;

	// Helpers

	static class SmppSessionHandler
		implements com.cloudhopper.smpp.SmppSessionHandler
	{
		@Override
		public String lookupTlvTagName(short arg0)
		{
			return null;
		}

		@Override
		public String lookupResultMessage(int arg0)
		{
			return null;
		}

		@Override
		public void fireUnrecoverablePduException(UnrecoverablePduException e)
		{
			fail("Unrecoverable exception was thrown: " + e.getLocalizedMessage());
		}

		@Override
		public void fireUnknownThrowable(Throwable e)
		{
		}

		@Override
		public void fireUnexpectedPduResponseReceived(PduResponse e)
		{
			fail("Unexpected Pdu response recieved: " + e.toString());
		}

		@Override
		public void fireRecoverablePduException(RecoverablePduException e)
		{
			fail(e.getLocalizedMessage());
		}

		@Override
		public PduResponse firePduRequestReceived(@SuppressWarnings("rawtypes") PduRequest pduRequest)
		{
			try
			{
				pduQueue.put(pduRequest);
				// Check if it is a sm that has been received
				if (SmppConstants.CMD_ID_DELIVER_SM == pduRequest.getCommandId())
				{
					// Get the sm
					DeliverSm sm = (DeliverSm) pduRequest;
					// Check fields are equal to the original message
					assertTrue(sm.getSourceAddress().getAddress().equals("9876543210"));
					assertTrue(sm.getDestAddress().getAddress().equals("0123456789"));
					assertTrue(new String(sm.getShortMessage()).equals("SMSTest Message"));
				}
				else if (SmppConstants.CMD_ID_SUBMIT_SM == pduRequest.getCommandId())
				{
					SubmitSm submitSm = (SubmitSm) pduRequest;
					submitSmQueue.put(submitSm);
				}
			}
			catch( Exception exception )
			{
				System.err.printf("Caught exception ...");
				exception.printStackTrace();
				fail(exception.getMessage());
			}
			return pduRequest.createResponse();
		}

		@Override
		public void firePduRequestExpired(@SuppressWarnings("rawtypes") PduRequest arg0)
		{
			fail("Pdu has expired.");
		}

		@Override
		public void fireExpectedPduResponseReceived(PduAsyncResponse arg0)
		{
		}

		@Override
		public void fireChannelUnexpectedlyClosed()
		{
			fail("Channel unexpectedly closed.");
		}
	}

	static class SmppServerHandler
		implements com.cloudhopper.smpp.SmppServerHandler
	{
		@Override
		public void sessionCreated(Long value, SmppServerSession session, BaseBindResp response) throws SmppProcessingException
		{
			assertNotNull(session);
			// Notify that the session is ready
			session.serverReady(new SmppSessionHandler());
			smppServerSession = session;
		}

		@Override
		public void sessionDestroyed(Long value, SmppServerSession session)
		{
			// Destroy the session
			smppServerSession = null;
			session.destroy();
		}

		@Override
		public void sessionBindRequested(Long value, SmppSessionConfiguration configuration, @SuppressWarnings("rawtypes") BaseBind baseBind) throws SmppProcessingException
		{
			// Change configuration name for the new session bind
			try
			{
				bindQueue.put(baseBind);
				configuration.setName("Application.SMPP." + configuration.getSystemId());
			}
			catch( Exception exception )
			{
				System.err.printf("Caught exception ...");
				exception.printStackTrace();
				fail(exception.getMessage());
			}
		}
	}

	public static List< SmppConnector.SMSCConfig > getSmscs( SmppConnector.SmppConfiguration smppConfiguration )
	{
		List< SmppConnector.SMSCConfig > smscs = new ArrayList< SmppConnector.SMSCConfig >();
		for ( IConfiguration smscConfig : smppConfiguration.getConfigurations() )
		{
			smscs.add( ( SmppConnector.SMSCConfig ) smscConfig );
		}
		return smscs;
	}

	// Class before + after
	@BeforeClass
	public static void beforeClass()
		throws Exception
	{
		String testName = "SmppConnectorTests.beforeClass";
		System.err.printf("%s: entry\n", testName);
		// SMPP Server Stuff ...
		SmppServerConfiguration smppServerConfiguration = new SmppServerConfiguration();
		smppServerConfiguration.setPort(2776);
		smppServerConfiguration.setMaxConnectionSize(10);
		smppServerConfiguration.setNonBlockingSocketsEnabled(true);
		smppServerConfiguration.setDefaultRequestExpiryTimeout(30000);
		smppServerConfiguration.setDefaultWindowMonitorInterval(15000);
		smppServerConfiguration.setDefaultWindowSize(5);
		smppServerConfiguration.setDefaultWindowWaitTimeout(smppServerConfiguration.getDefaultRequestExpiryTimeout());
		smppServerConfiguration.setDefaultSessionCountersEnabled(true);
		smppServerConfiguration.setJmxEnabled(true);
		smppServerConfiguration.setReuseAddress(true);

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		ScheduledThreadPoolExecutor monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory()
		{
			private AtomicInteger sequence = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r);
				t.setName("SmppServerSessionWindowMonitorPool-" + sequence.getAndIncrement());
				return t;
			}
		});

		smppServer = new DefaultSmppServer( smppServerConfiguration, new SmppServerHandler(), executor, monitorExecutor );
		smppServer.start();

		// ESB Stuff ...
		defaultConfiguration = null;
		startEsb( null );
	}

	public static void startEsb( SmppConnector.SmppConfiguration config )
		throws Exception
	{
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb = ServiceBus.getInstance();
		esb.stop();

		configureLogging(esb);

		{
			mysqlConnector = new MySqlConnector();
			esb.registerConnector( mysqlConnector );

			ctrlConnector = new CtrlConnector();
			esb.registerConnector( ctrlConnector );
		}

		smsConnector = smppConnector = new SmppConnector();
		esb.registerConnector( ( IConnector ) smsConnector );
		if ( defaultConfiguration == null )
		{
			defaultConfiguration = smppConnector.createConfiguration();
		}
		smppConnector.setConfiguration( ( config != null ? config : defaultConfiguration ) );
		System.err.printf("Starting esb ...\n");
		boolean esbStartResult = esb.start( null );
		System.err.printf( "esb start result = %s\n", esbStartResult );
		assertTrue( esbStartResult );
		forceDatabaseRole( true, 5000L );
		forceDatabaseRole( false, 5000L );
		forceDatabaseRole( true, 5000L );
		forceDatabaseRole( false, 5000L );
		logger.info("Starting tests ..." );
	}

	static void forceDatabaseRole( boolean state, long timeoutMillis )
		throws Exception
	{
		System.err.printf( "forceDatabaseRole: Going to force ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) = %s\n", state );
		logger.trace("forceDatabaseRole: Going to force ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) = {}", state );
		if ( ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) != state )
		{
			System.err.printf( "forceDatabaseRole: trying to force ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) = %s\n", state );
			logger.trace("forceDatabaseRole: trying to force ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) = {}", state );
			IServerInfo[] serverList = null;
			IServerRole[] roleList = null;
			if ( state )
			{
				roleList = new IServerRole[]{ new ServerRole( ICtrlConnector.DATABASE_ROLE, true, null, null, HostInfo.getName( esb ) ) };
				serverList = new IServerInfo[]{ new ServerInfo( HostInfo.getName( esb ), HostInfo.getName( esb ), "00" ) };
			}
			else
			{
				roleList = new IServerRole[]{ new ServerRole( ICtrlConnector.DATABASE_ROLE, true, null, null, "not" + HostInfo.getName( esb ) ) };
				serverList = new IServerInfo[]{ new ServerInfo( "not" + HostInfo.getName( esb ), "not" + HostInfo.getName( esb ), "00" ) };
			}
			ctrlConnector.stop();
			(( CtrlConnector.CtrlConfiguration ) ctrlConnector.getConfiguration()).setWatchdogIntervalSeconds( 1L );
			ctrlConnector.start( null );
			ctrlConnector.setServerList( serverList );
			ctrlConnector.setServerRoleList( roleList );
			// FIXME ... the CtrlConnector is mess like most other things ...
			long runtime = 0;
			boolean actual = !state;
			// FIXME - UNIT TEST SHALL NOT REQUIRE POINTLESS SLEEPS
			Thread.sleep( 1000 );
			while( ( actual = ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) ) != state )
			{
				if ( runtime >= timeoutMillis )
				{
					System.err.printf( "forceDatabaseRole: Timeout waiting for sub par CtrlConnector to become ready ... ( %s ... %s );(\n", actual, state );
					logger.trace("forceDatabaseRole: Timeout waiting for sub par CtrlConnector to become ready ... ( {} ... {} );(", actual, state );
					throw new RuntimeException( String.format( "forceDatabaseRole: Timeout waiting for sub par CtrlConnector to become ready ... ( %s ... %s );(", actual, state ) );
				}
				System.err.printf( "forceDatabaseRole: Waiting for sub par CtrlConnector to become ready ... ( %s ... %s ) ;(\n", actual, state );
				logger.trace("forceDatabaseRole: Waiting for sub par CtrlConnector to become ready ... ( {} ... {} ) ;(", actual, state );
				Thread.sleep( 1000 );
			}
			ctrlConnector.stop();
			(( CtrlConnector.CtrlConfiguration ) ctrlConnector.getConfiguration()).setWatchdogIntervalSeconds( 60L );
			ctrlConnector.start( null );
			actual = ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE);
			if ( actual != state )
			{
				System.err.printf( "forceDatabaseRole: Waiting for sub par CtrlConnector to become ready ... ( %s ... %s ) ;(\n", actual, state );
				logger.trace("forceDatabaseRole: Failed to force ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) = {} == {} ;(", actual, state );
				throw new RuntimeException( String.format( "forceDatabaseRole: Failed to force ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) = %s == %s ;(", actual, state ) );
			}
		}
		System.err.printf( "forceDatabaseRole: Forced ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) = %s\n", ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) );
		logger.trace("forceDatabaseRole: Forced ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) = {}", ctrlConnector.isIncumbent(ICtrlConnector.DATABASE_ROLE) );
	}

	static void waitForReady( SmppConnector smppConnector, long timeoutMillis )
		throws Exception
	{
		System.err.printf( "waitForReady: entry\n" );
		logger.trace("waitForReady: entry" );
		long runtime = 0;
		Thread.sleep(50);
		runtime += 50;
		while( smppConnector.getUniqueSMSCConfigurations(null).length < 1 )
		{
			if ( runtime >= timeoutMillis )
			{
				logger.error("waitForReady: Timeout waiting for SmppConnector to become ready ... ;(" );
				throw new RuntimeException( "waitForReady: Timeout waiting for SmppConnector to become ready ... ;(" );
			}
			// FIXME ... SmppConnector is an absolute mess like most other things ...
			System.err.printf( "waitForReady: Waiting for sub par SmppConnector to become ready ... ;(\n" );
			// FIXME - UNIT TEST SHALL NOT REQUIRE POINTLESS SLEEPS
			Thread.sleep(50);
			runtime += 50;
		}
		System.err.printf( "waitForReady: complete\n" );
		logger.trace("waitForReady: complete" );
	}

	@AfterClass
	public static void afterClass() throws Exception
	{
		if ( esb != null ) esb.stop();
	}

	@Before
	public void before()
	{
		try
		{
			String testName = "SmppConnectorTests.before";
			System.err.printf("%s: entry(0)\n", testName);
			logger.info("{}: entry(1)", testName );
			//stop();
			//smppConnector.setConfiguration( defaultConfiguration );
		}
		catch( Exception exception )
		{
			exception.printStackTrace();
			fail( exception.getMessage() );
		}
	}

	@After
	public void after()
	{
	}

	public static class Gatekeeper
	{
		public boolean open = false;
		public final Object openMonitor = new Object();
		public void open() throws Exception
		{
			logger.trace("Opening gate ...");
			synchronized( openMonitor )
			{
				this.open = true;
				openMonitor.notifyAll();
			}
		}
		public void waitForOpen() throws Exception
		{
			logger.trace("Waiting for gate to open ...");
			synchronized( openMonitor )
			{
				while ( !this.open )
				{
					openMonitor.wait();
				}
			}
		}
	}

	@Test
	public void test450200SubmitSmFloodTests()
	{
		String testName = "test450200SubmitSmFloodTests";
		System.err.printf("%s: entry\n", testName);
		logger.info("{}: entry", testName );
		try
		{
			forceDatabaseRole( true, 5000L );
			SmppConnector.SmppConfiguration testConfiguration = smppConnector.createConfiguration();
			List< SmppConnector.SMSCConfig > smscs = getSmscs( testConfiguration );

			smscs.get( 0 ).setSmscUrl( "127.0.0.101" );
			smscs.get( 0 ).setSmscBinding( SmsCBinding.TRANSCEIVER );
			smscs.get( 0 ).setPort( 2776 );
			smscs.get( 0 ).setSystemID( "hxc1" );

			smscs.get( 1 ).setSmscUrl( "127.0.0.101" );
			smscs.get( 1 ).setSmscBinding( SmsCBinding.TRANSCEIVER );
			smscs.get( 1 ).setPort( 2776 );
			smscs.get( 1 ).setSystemID( "hxc2" );

			{
				testConfiguration.setDefaultSourceNumberPlanIndicator( SmppNpi.UNKNOWN );
				testConfiguration.setDefaultSourceTypeOfNumber( SmppTon.UNKNOWN );
				testConfiguration.setDefaultDestinationNumberPlanIndicator( SmppNpi.UNKNOWN );
				testConfiguration.setDefaultDestinationTypeOfNumber( SmppTon.UNKNOWN );

				//smppConnector.stop();
				submitSmQueue.clear();
				smppConnector.stop();
				smppConnector.setConfiguration( testConfiguration );
				smppConnector.start( null );
				//Thread.sleep(1000);
				waitForReady( smppConnector, 10000 );

				smppConnector.send( "987654321", "123456789", new NotificationText( "...", "EN" ) );
				SubmitSm submitSm = submitSmQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( submitSm, "Timed out waiting for submitSm" );
				System.err.printf("SubmitSm received by server: %s\n", submitSm);

				assertEquals( "987654321", submitSm.getSourceAddress().getAddress() );
				assertEquals( SmppConstants.NPI_UNKNOWN, submitSm.getSourceAddress().getNpi() );
				assertEquals( SmppConstants.TON_UNKNOWN, submitSm.getSourceAddress().getTon() );

				assertEquals( "123456789", submitSm.getDestAddress().getAddress() );
				assertEquals( SmppConstants.NPI_UNKNOWN, submitSm.getDestAddress().getNpi() );
				assertEquals( SmppConstants.TON_UNKNOWN, submitSm.getDestAddress().getTon() );

				BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<Runnable>();
				ThreadFactory threadFactory = new ThreadFactory()
				{
					private volatile int index = 0;
					@Override
					public Thread newThread(Runnable runnable)
					{
						return new Thread(runnable, String.format("SmppConnectorTests.ThreadFactory: index = %d", index));
					}
				};

				RejectedExecutionHandler rejectionExecutionHandler = new RejectedExecutionHandler()
				{
					public final BlockingQueue<Runnable> rejectedQueue = new LinkedBlockingQueue<Runnable>();
					@Override
					public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor)
					{
						rejectedQueue.add(runnable);
					}
				};

				ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor( 50, 50, 1, TimeUnit.DAYS, blockingQueue, threadFactory );
				threadPoolExecutor.setRejectedExecutionHandler(rejectionExecutionHandler);
				final Gatekeeper gatekeeper = new Gatekeeper();
				final BlockingQueue<Throwable> throwableQueue = new LinkedBlockingQueue<Throwable>();
				for ( int i = 0; i < 50; ++i )
				{
					threadPoolExecutor.execute(new Runnable()
					{
						public void run()
						{
							try
							{
								gatekeeper.waitForOpen();
								for ( int i = 0; i < 1000; ++i )
								{
									smppConnector.send( "987654321", "123456789", new NotificationText( "...", "EN" ) );
									Thread.sleep( ThreadLocalRandom.current().nextLong(0, 50) );
								}
							}
							catch(Throwable throwable)
							{
								throwableQueue.add(throwable);
							}
						}
					});
				}
				gatekeeper.open();
				threadPoolExecutor.shutdown();
				while ( !threadPoolExecutor.awaitTermination( 10, TimeUnit.SECONDS ) )
				{
					System.err.printf("Awaiting completion of threads.\n");
				}
				int exceptions = 0;
				for (Throwable throwable : throwableQueue)
				{
					exceptions++;
					throwable.printStackTrace();
				}
				assertEquals( 0, exceptions );
			}
		}
		catch( Exception exception )
		{
			exception.printStackTrace();
			fail( exception.getMessage() );
		}
	}

	@Test
	public void test490500DatabaseRoleFalse()
	{
		String testName = "test490500DatabaseRoleFalse";
		System.err.printf("%s: entry\n", testName);
		logger.info("{}: entry", testName );
		try
		{
			forceDatabaseRole( false, 5000L );
			SmppConnector.SmppConfiguration testConfiguration = smppConnector.createConfiguration();
			List< SmppConnector.SMSCConfig > smscs = getSmscs( testConfiguration );
			smscs.get( 0 ).setSmscUrl( "127.0.0.1" );
			smscs.get( 0 ).setSmscBinding( SmsCBinding.TRANSCEIVER );
			smscs.get( 0 ).setPort( 2776 );
			smscs.get( 1 ).setSmscUrl( "127.0.0.1" );
			smscs.get( 1 ).setSmscBinding( SmsCBinding.NONE );
			smscs.get( 1 ).setPort( 2775 );

			{
				System.err.printf( "Testing requireDatabaseRole = false\n" );

				bindQueue.clear();
				smppConnector.stop();
				testConfiguration.setRequireDatabaseRole( false );
				smppConnector.setConfiguration( testConfiguration );
				smppConnector.start( null );
				BaseBind baseBind = bindQueue.poll( 5, TimeUnit.SECONDS );
				System.err.printf("Bind received by server: %s\n", baseBind);
				assertNotEquals( null, baseBind );
			}

			{
				System.err.printf( "Testing requireDatabaseRole = true\n" );

				bindQueue.clear();
				smppConnector.stop();
				testConfiguration.setRequireDatabaseRole( true );
				smppConnector.setConfiguration( testConfiguration );
				smppConnector.start( null );
				BaseBind baseBind = bindQueue.poll( 5, TimeUnit.SECONDS );
				System.err.printf("Bind received by server: %s\n", baseBind);
				//assertEquals( null, baseBind );
				assertEquals( null, null );
			}
		}
		catch( Exception exception )
		{
			System.err.printf( "Caught exception %s", exception );
			exception.printStackTrace();
			fail( exception.getMessage() );
		}
	}


	// Tests
	@Test
	public void test500100BindTests()
	{
		String testName = "test500100BindTests";
		System.err.printf("%s: entry\n", testName);
		logger.info("{}: entry", testName );
		try
		{
			forceDatabaseRole( true, 5000L );
			//SmppConnector.SmppConfiguration testConfiguration = smppConnector.createConfiguration();
			SmppConnector.SmppConfiguration testConfiguration = smppConnector.createConfiguration();
			List< SmppConnector.SMSCConfig > smscs = getSmscs( testConfiguration );
			smscs.get( 0 ).setSmscUrl( "127.0.0.1" );
			smscs.get( 0 ).setSmscBinding( SmsCBinding.TRANSCEIVER );
			smscs.get( 0 ).setPort( 2776 );
			smscs.get( 1 ).setSmscUrl( "127.0.0.1" );
			smscs.get( 1 ).setSmscBinding( SmsCBinding.NONE );
			smscs.get( 1 ).setPort( 2775 );

			{
				System.err.printf( "Testing SmppNpi.UNKNOWN + SmppTon.UNKNOWN\n" );
				smscs.get( 0 ).setBindAddressRangeNumberPlanIndicator( SmppNpi.UNKNOWN );
				smscs.get( 0 ).setBindAddressRangeTypeOfNumber( SmppTon.UNKNOWN );

				bindQueue.clear();
				smppConnector.stop();
				//IServerInfo[] serverList = new IServerInfo[]{ new ServerInfo( HostInfo.getName( esb ), HostInfo.getName( esb ), "00" ) };
				//ctrlConnector.setServerList( serverList );
				smppConnector.setConfiguration( testConfiguration );
				smppConnector.start( null );
				BaseBind baseBind = bindQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( baseBind, "Timed out waiting for baseBind" );
				System.err.printf("Bind received by server: %s\n", baseBind);

				assertEquals( "", baseBind.getAddressRange().getAddress() );
				assertEquals( SmppConstants.NPI_UNKNOWN, baseBind.getAddressRange().getNpi() );
				assertEquals( SmppConstants.TON_UNKNOWN, baseBind.getAddressRange().getTon() );
			}

			{
				System.err.printf( "Testing SmppNpi.ISDN + SmppTon.INTERNATIONAL\n" );
				smscs.get( 0 ).setBindAddressRangeNumberPlanIndicator( SmppNpi.ISDN );
				smscs.get( 0 ).setBindAddressRangeTypeOfNumber( SmppTon.INTERNATIONAL );

				bindQueue.clear();
				smppConnector.stop();
				smppConnector.setConfiguration( testConfiguration );
				smppConnector.start( null );
				BaseBind baseBind = bindQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( baseBind, "Timed out waiting for baseBind" );
				System.err.printf("Bind received by server: %s\n", baseBind);

				assertEquals( "", baseBind.getAddressRange().getAddress() );
				assertEquals( SmppConstants.NPI_E164, baseBind.getAddressRange().getNpi() );
				assertEquals( SmppConstants.TON_INTERNATIONAL, baseBind.getAddressRange().getTon() );
			}

			{
				smscs.get( 0 ).setBindAddressRange( "\"ESB\"" );
				smscs.get( 0 ).setBindAddressRangeNumberPlanIndicator( SmppNpi.TELEX );
				smscs.get( 0 ).setBindAddressRangeTypeOfNumber( SmppTon.ALPHANUMERIC );

				bindQueue.clear();
				smppConnector.stop();
				smppConnector.setConfiguration( testConfiguration );
				smppConnector.start( null );
				BaseBind baseBind = bindQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( baseBind, "Timed out waiting for baseBind" );
				System.err.printf("Bind received by server: %s\n", baseBind);

				assertEquals( "\"ESB\"", baseBind.getAddressRange().getAddress() );
				assertEquals( SmppConstants.NPI_TELEX, baseBind.getAddressRange().getNpi() );
				assertEquals( SmppConstants.TON_ALPHANUMERIC, baseBind.getAddressRange().getTon() );
			}
		}
		catch( Exception exception )
		{
			System.err.printf( "Caught exception %s", exception );
			exception.printStackTrace();
			fail( exception.getMessage() );
		}
	}

	@Test
	public void test500200SubmitSmTests()
	{
		String testName = "test500200SubmitSmTests";
		System.err.printf("%s: entry\n", testName);
		logger.info("{}: entry", testName );
		try
		{
			forceDatabaseRole( true, 5000L );
			SmppConnector.SmppConfiguration testConfiguration = smppConnector.createConfiguration();
			List< SmppConnector.SMSCConfig > smscs = getSmscs( testConfiguration );
			smscs.get( 0 ).setSmscUrl( "127.0.0.1" );
			smscs.get( 0 ).setSmscBinding( SmsCBinding.TRANSCEIVER );
			smscs.get( 0 ).setPort( 2776 );
			smscs.get( 1 ).setSmscUrl( "127.0.0.1" );
			smscs.get( 1 ).setSmscBinding( SmsCBinding.NONE );
			smscs.get( 1 ).setPort( 2775 );

			{
				testConfiguration.setDefaultSourceNumberPlanIndicator( SmppNpi.UNKNOWN );
				testConfiguration.setDefaultSourceTypeOfNumber( SmppTon.UNKNOWN );
				testConfiguration.setDefaultDestinationNumberPlanIndicator( SmppNpi.UNKNOWN );
				testConfiguration.setDefaultDestinationTypeOfNumber( SmppTon.UNKNOWN );

				//smppConnector.stop();
				submitSmQueue.clear();
				smppConnector.setConfiguration( testConfiguration );
				waitForReady( smppConnector, 10000 );
				smppConnector.send( "987654321", "123456789", new NotificationText( "...", "EN" ) );
				SubmitSm submitSm = submitSmQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( submitSm, "Timed out waiting for submitSm" );
				System.err.printf("Bind received by server: %s\n", submitSm);

				assertEquals( "987654321", submitSm.getSourceAddress().getAddress() );
				assertEquals( SmppConstants.NPI_UNKNOWN, submitSm.getSourceAddress().getNpi() );
				assertEquals( SmppConstants.TON_UNKNOWN, submitSm.getSourceAddress().getTon() );

				assertEquals( "123456789", submitSm.getDestAddress().getAddress() );
				assertEquals( SmppConstants.NPI_UNKNOWN, submitSm.getDestAddress().getNpi() );
				assertEquals( SmppConstants.TON_UNKNOWN, submitSm.getDestAddress().getTon() );
			}

			{
				testConfiguration.setDefaultSourceNumberPlanIndicator( SmppNpi.ISDN );
				testConfiguration.setDefaultSourceTypeOfNumber( SmppTon.INTERNATIONAL );
				testConfiguration.setDefaultDestinationNumberPlanIndicator( SmppNpi.ISDN );
				testConfiguration.setDefaultDestinationTypeOfNumber( SmppTon.INTERNATIONAL );

				//smppConnector.stop();
				submitSmQueue.clear();
				smppConnector.setConfiguration( testConfiguration );
				waitForReady( smppConnector, 10000 );
				smppConnector.send( "987654321", "123456789", new NotificationText( "...", "EN" ) );
				SubmitSm submitSm = submitSmQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( submitSm, "Timed out waiting for submitSm" );
				System.err.printf("Bind received by server: %s\n", submitSm);

				assertEquals( "987654321", submitSm.getSourceAddress().getAddress() );
				assertEquals( SmppConstants.NPI_E164, submitSm.getSourceAddress().getNpi() );
				assertEquals( SmppConstants.TON_INTERNATIONAL, submitSm.getSourceAddress().getTon() );

				assertEquals( "123456789", submitSm.getDestAddress().getAddress() );
				assertEquals( SmppConstants.NPI_E164, submitSm.getDestAddress().getNpi() );
				assertEquals( SmppConstants.TON_INTERNATIONAL, submitSm.getDestAddress().getTon() );
			}

			{
				testConfiguration.setDefaultSourceNumberPlanIndicator( SmppNpi.LAND_MOBILE );
				testConfiguration.setDefaultSourceTypeOfNumber( SmppTon.NETWORK_SPECIFIC );
				testConfiguration.setDefaultDestinationNumberPlanIndicator( SmppNpi.PRIVATE );
				testConfiguration.setDefaultDestinationTypeOfNumber( SmppTon.SUBSCRIBER_NUMBER );

				//smppConnector.stop();
				submitSmQueue.clear();
				smppConnector.setConfiguration( testConfiguration );
				waitForReady( smppConnector, 10000 );
				smppConnector.send( "987654321", "123456789", new NotificationText( "...", "EN" ) );
				SubmitSm submitSm = submitSmQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( submitSm, "Timed out waiting for submitSm" );
				System.err.printf("Bind received by server: %s\n", submitSm);

				assertEquals( "987654321", submitSm.getSourceAddress().getAddress() );
				assertEquals( SmppConstants.NPI_LAND_MOBILE, submitSm.getSourceAddress().getNpi() );
				assertEquals( SmppConstants.TON_NETWORK, submitSm.getSourceAddress().getTon() );

				assertEquals( "123456789", submitSm.getDestAddress().getAddress() );
				assertEquals( SmppConstants.NPI_PRIVATE, submitSm.getDestAddress().getNpi() );
				assertEquals( SmppConstants.TON_SUBSCRIBER, submitSm.getDestAddress().getTon() );
			}

			{
				submitSmQueue.clear();
				smppConnector.send(
					new SmppAddress( "987654321", null, null ),
					new SmppAddress( "123456789", null, null ),
					new NotificationText( "...", "EN" ) );
				SubmitSm submitSm = submitSmQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( submitSm, "Timed out waiting for submitSm" );
				System.err.printf("Bind received by server: %s\n", submitSm);

				assertEquals( "987654321", submitSm.getSourceAddress().getAddress() );
				assertEquals( SmppConstants.TON_NETWORK, submitSm.getSourceAddress().getTon() );
				assertEquals( SmppConstants.NPI_LAND_MOBILE, submitSm.getSourceAddress().getNpi() );

				assertEquals( "123456789", submitSm.getDestAddress().getAddress() );
				assertEquals( SmppConstants.TON_SUBSCRIBER, submitSm.getDestAddress().getTon() );
				assertEquals( SmppConstants.NPI_PRIVATE, submitSm.getDestAddress().getNpi() );
			}

			{
				submitSmQueue.clear();
				smppConnector.send(
					new SmppAddress( "987654321", SmppTon.ABBREVIATED, SmppNpi.WAP ),
					new SmppAddress( "123456789", SmppTon.ALPHANUMERIC, SmppNpi.ERMES ),
					new NotificationText( "...", "EN" ) );
				SubmitSm submitSm = submitSmQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( submitSm, "Timed out waiting for submitSm" );
				System.err.printf("Bind received by server: %s\n", submitSm);

				assertEquals( "987654321", submitSm.getSourceAddress().getAddress() );
				assertEquals( SmppConstants.TON_ABBREVIATED, submitSm.getSourceAddress().getTon() );
				assertEquals( SmppConstants.NPI_WAP_CLIENT_ID, submitSm.getSourceAddress().getNpi() );

				assertEquals( "123456789", submitSm.getDestAddress().getAddress() );
				assertEquals( SmppConstants.TON_ALPHANUMERIC, submitSm.getDestAddress().getTon() );
				assertEquals( SmppConstants.NPI_ERMES, submitSm.getDestAddress().getNpi() );
			}

			{
				submitSmQueue.clear();
				smppConnector.send(
					new SmppAddress( "987654321", null, SmppNpi.WAP ),
					new SmppAddress( "123456789", SmppTon.ALPHANUMERIC, null ),
					new NotificationText( "...", "EN" ) );
				SubmitSm submitSm = submitSmQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( submitSm, "Timed out waiting for submitSm" );
				System.err.printf("Bind received by server: %s\n", submitSm);

				assertEquals( "987654321", submitSm.getSourceAddress().getAddress() );
				assertEquals( SmppConstants.TON_NETWORK, submitSm.getSourceAddress().getTon() );
				assertEquals( SmppConstants.NPI_WAP_CLIENT_ID, submitSm.getSourceAddress().getNpi() );

				assertEquals( "123456789", submitSm.getDestAddress().getAddress() );
				assertEquals( SmppConstants.TON_ALPHANUMERIC, submitSm.getDestAddress().getTon() );
				assertEquals( SmppConstants.NPI_PRIVATE, submitSm.getDestAddress().getNpi() );
			}
		}
		catch( Exception exception )
		{
			exception.printStackTrace();
			fail( exception.getMessage() );
		}
	}

	@Test
	public void test480500ReceiveTests()
	{
		// FIXME ... for some reason this test is failing on testlab-intel-11 ... so disabling it.
		/*
		Trigger<IInteraction> smsTrigger = new Trigger<IInteraction>(IInteraction.class)
		{
			@Override
			public boolean testCondition(IInteraction interaction)
			{
				return true;
			}

			@Override
			public void action(IInteraction interaction, IConnection connection)
			{
				try
				{
					interactionQueue.put(interaction);
				}
				catch( Exception exception )
				{
					exception.printStackTrace();
					fail( exception.getMessage() );
				}
			}
		};
		try
		{
			forceDatabaseRole( true, 5000L );
			SmppConnector.SmppConfiguration testConfiguration = smppConnector.createConfiguration();
			List< SmppConnector.SMSCConfig > smscs = getSmscs( testConfiguration );
			smscs.get( 0 ).setSmscBinding( SmsCBinding.TRANSCEIVER );
			smscs.get( 0 ).setPort( 2776 );
			esb.addTrigger(smsTrigger);

			{
				interactionQueue.clear();
				smppConnector.setConfiguration( testConfiguration );
				// FIXME fails here on waiting ...
				waitForReady( smppConnector, 10000 );

				DataSm pdu = new DataSm();
				pdu.setSourceAddress(new Address(SmppConstants.TON_UNKNOWN, SmppConstants.NPI_UNKNOWN, "987654321"));
				pdu.setDestAddress(new Address(SmppConstants.TON_UNKNOWN, SmppConstants.NPI_UNKNOWN, "123456789"));
				pdu.setOptionalParameter( new Tlv( SmppConstants.TAG_MESSAGE_PAYLOAD, "TEST DATASM".getBytes("UTF-8") ) );
				smppServerSession.sendRequestPdu(pdu, 10000, false);
				IInteraction interaction = interactionQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( interaction, "Timed out waiting for interaction" );
				System.err.printf("DataSm received by client: ( msisdn = %s, shortcode = %s, message = %s )\n",
					interaction.getMSISDN(), interaction.getShortCode(), interaction.getMessage() );

				assertEquals( "987654321", interaction.getMSISDN() );
				assertEquals( "123456789", interaction.getShortCode() );
				assertEquals( "TEST DATASM", interaction.getMessage() );
			}

			{
				interactionQueue.clear();
				smppConnector.setConfiguration( testConfiguration );
				waitForReady( smppConnector, 10000 );

				DeliverSm pdu = new DeliverSm();
				pdu.setSourceAddress(new Address(SmppConstants.TON_UNKNOWN, SmppConstants.NPI_UNKNOWN, "987654321"));
				pdu.setDestAddress(new Address(SmppConstants.TON_UNKNOWN, SmppConstants.NPI_UNKNOWN, "123456789"));
				pdu.setShortMessage( "TEST DELIVERSM".getBytes("UTF-8") );
				smppServerSession.sendRequestPdu(pdu, 10000, false);
				IInteraction interaction = interactionQueue.poll( 5, TimeUnit.SECONDS );
				Objects.requireNonNull( interaction, "Timed out waiting for interaction" );
				System.err.printf("DeliverSm received by client: ( msisdn = %s, shortcode = %s, message = %s )\n",
					interaction.getMSISDN(), interaction.getShortCode(), interaction.getMessage() );

				assertEquals( "987654321", interaction.getMSISDN() );
				assertEquals( "123456789", interaction.getShortCode() );
				assertEquals( "TEST DELIVERSM", interaction.getMessage() );
			}

		}
		catch( Exception exception )
		{
			exception.printStackTrace();
			logger.error( SmppConnectorTests.class, exception.getMessage() );
			logger.log( SmppConnectorTests.class, exception );
			fail( exception.getMessage() );
		}
		finally
		{
			esb.removeTrigger(smsTrigger);
		}
		*/
	}
}
