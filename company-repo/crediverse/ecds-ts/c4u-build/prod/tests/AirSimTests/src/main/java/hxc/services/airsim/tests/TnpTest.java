package hxc.services.airsim.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.air.AirConnector;
import hxc.connectors.air.AirConnector.AirConnectionConfig;
import hxc.connectors.air.AirConnector.AirConnectorConfig;
import hxc.connectors.air.AirException;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.file.ConfigRecord;
import hxc.connectors.file.FileConnector;
import hxc.connectors.file.FileConnector.FileConnectorConfiguration;
import hxc.connectors.file.FileProcessorType;
import hxc.connectors.file.FileType;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.servicebus.Trigger;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.Subscriber;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.airsim.protocol.TnpThreshold;
import hxc.services.airsim.protocol.TnpThreshold.TnpTriggerTypes;
import hxc.services.logging.LoggerService;
import hxc.services.numberplan.NumberPlanService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV3;

public class TnpTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(TnpTest.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;
	private static IAirSim airSimulator = null;
	private static AirConnector air;

	private ThresholdNotificationFileV3 message = null;
	
	private static String INPUT_FOLDER = "/tmp/c4u";
	private static String OUTPUT_FOLDER = "/tmp/c4u/done";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup
	//
	// /////////////////////////////////
	@BeforeClass
	public static void setup() throws ValidationException
	{
		deleteFiles(INPUT_FOLDER);
		deleteFiles(OUTPUT_FOLDER);
		
		// Create Transaction Service
		esb = ServiceBus.getInstance();
		esb.stop();
		esb.registerService(new LoggerService());
		esb.registerConnector(new CtrlConnector());

		air = new AirConnector();
		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");

		NumberPlanService numberPlan = new NumberPlanService();
		esb.registerService(numberPlan);

		esb.registerConnector(air);
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "CFR");

		FileConnector fc = new FileConnector();
		FileConnectorConfiguration fcc = (FileConnectorConfiguration) fc.getConfiguration();
		ConfigRecord cr = new ConfigRecord();
		cr.setSequence(1);
		cr.setFilenameFilter("*v3.0.TNP");
		cr.setInputDirectory(INPUT_FOLDER);
		cr.setOutputDirectory(OUTPUT_FOLDER);
		cr.setFileProcessorType(FileProcessorType.CSV);
		cr.setFileType(FileType.ThresholdNotificationFileV3);
		cr.setServerRole(HostInfo.getNameOrElseHxC());
		cr.setStrictlySequential(false);
		fcc.setFileConfigs(new ConfigRecord[] { cr });
		esb.registerConnector(fc);

		boolean started = esb.start(null);
		assert (started);
		airSimulator.start();

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tear Down
	//
	// /////////////////////////////////
	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		deleteFiles(OUTPUT_FOLDER);
		airSimulator.stop();
		esb.stop();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	@Test
	public void testTNP() throws AirException, InterruptedException
	{
		final int DA_ID = 1;

		// Create
		airSimulator.reset();
		Subscriber subscriber = airSimulator.addSubscriber("07245566612", 1, 12, 5000, SubscriberState.active);
		airSimulator.createDedicatedAccount(subscriber.getInternationalNumber(), DA_ID, 1, 3001L, null, new Date());

		Trigger<ThresholdNotificationFileV3> trigger = new Trigger<ThresholdNotificationFileV3>(ThresholdNotificationFileV3.class)
		{
			@Override
			public boolean testCondition(ThresholdNotificationFileV3 message)
			{
				return message.thresholdID == 2;
			}

			@Override
			public void action(ThresholdNotificationFileV3 message, IConnection connection)
			{
				TnpTest.this.message = message;
			}

		};
		esb.addTrigger(trigger);
		Thread.sleep(1000);
		message = null;

		TnpThreshold threshold = new TnpThreshold();
		threshold.setServiceClass(subscriber.getServiceClassCurrent());
		threshold.setThresholdID(2);
		threshold.setDirectory(INPUT_FOLDER);
		threshold.setVersion("3.0");
		threshold.setSenderID("sdp1");
		threshold.setReceiverID("hxc5");
		threshold.setAccountID(DA_ID);
		threshold.setLevel(3000);
		threshold.setUpwards(false);
		threshold.setTriggerType(TnpTriggerTypes.TRAFFIC);
		threshold.setAccountGroupID(12);

		boolean ok = airSimulator.addTnpThreshold(threshold);
		assertTrue(ok);

		DedicatedAccount dedicatedAccount = airSimulator.getDedicatedAccount(subscriber.getInternationalNumber(), DA_ID);
		dedicatedAccount.setDedicatedAccountValue1(2999L);
		airSimulator.updateDedicatedAccount(subscriber.getInternationalNumber(), dedicatedAccount);
		Thread.sleep(10000);
		assertNotNull(message);
		assertEquals(2, message.thresholdID);

	}
	
	private static void deleteFiles(String folderName)
	{
		File folder = new File(folderName);
		if (folder.exists())
		{
			File[] files = folder.listFiles(new FilenameFilter()
			{

				@Override
				public boolean accept(File dir, String name)
				{
					int length = name.length();
					if (length <= 4)
						return false;
					String extension = name.substring(length - 4);
					return extension.equalsIgnoreCase(".tnp");
				}

			});

			for (File file : files)
			{
				if (file.exists())
					file.delete();
			}
		}
	}
}