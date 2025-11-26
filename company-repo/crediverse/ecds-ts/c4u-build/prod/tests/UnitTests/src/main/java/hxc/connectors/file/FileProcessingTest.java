package hxc.connectors.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;

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
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.file.FileConnector.FileConnectorConfiguration;
import hxc.servicebus.HostInfo;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.servicebus.Trigger;
import hxc.services.IService;
import hxc.services.airsim.AirSim;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.airsim.protocol.TnpThreshold;
import hxc.services.airsim.protocol.TnpThreshold.TnpTriggerTypes;
import hxc.services.numberplan.INumberPlan;
import hxc.services.numberplan.NumberPlanService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV2;
import hxc.utils.protocol.sdp.ThresholdNotificationFileV3;

public class FileProcessingTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(FileProcessingTest.class);

	private static IServiceBus esb;
	private static INumberPlan numberPlan;
	private static AirSim airSimulator;
	private static SubscriberEx subscriber;
	private static int processCount = 0;
	private static final String MSISDN = "0848654805";
	private static final int SERVICE_CLASS = 76;

	@BeforeClass
	public static void setup() throws ValidationException
	{
		deleteFiles("/tmp/c4u");
		deleteFiles("/tmp/c4u/done");

		esb = ServiceBus.getInstance();
		
		configureLogging(esb);

		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new CtrlConnector());

		FileConnector file = new FileConnector();
		FileConnectorConfiguration config = (FileConnectorConfiguration) file.getConfiguration();
		ConfigRecord fx = new ConfigRecord();
		fx.setSequence(1);
		fx.setFilenameFilter("*v3.0.TNP");
		fx.setInputDirectory("/tmp/c4u");
		fx.setOutputDirectory("/tmp/c4u/done");
		fx.setFileProcessorType(FileProcessorType.CSV);
		fx.setFileType(FileType.ThresholdNotificationFileV3);
		fx.setServerRole(HostInfo.getName());
		fx.setStrictlySequential(false);
		esb.registerConnector(file);

		AirConnector air = new AirConnector();
		AirConnectorConfig airConfig = (AirConnectorConfig) air.getConfiguration();
		AirConnectionConfig conConfig = (AirConnectionConfig) airConfig.getConfigurations().iterator().next();
		conConfig.setUri("http://127.0.0.1:10011/Air");
		esb.registerConnector(air);

		esb.registerService((IService) (numberPlan = new NumberPlanService()));

		Trigger<ThresholdNotificationFileV2> thresholdTrigger2 = new Trigger<ThresholdNotificationFileV2>(ThresholdNotificationFileV2.class)
		{

			@Override
			public boolean testCondition(ThresholdNotificationFileV2 message)
			{
				return message.serviceClassID == SERVICE_CLASS;
			}

			@Override
			public void action(ThresholdNotificationFileV2 message, IConnection connection)
			{
				assertEquals(SERVICE_CLASS, message.serviceClassID);
				processCount--;
			}
		};
		esb.addTrigger(thresholdTrigger2);

		Trigger<ThresholdNotificationFileV3> thresholdTrigger3 = new Trigger<ThresholdNotificationFileV3>(ThresholdNotificationFileV3.class)
		{

			@Override
			public boolean testCondition(ThresholdNotificationFileV3 message)
			{
				return message.serviceClassID == SERVICE_CLASS;
			}

			@Override
			public void action(ThresholdNotificationFileV3 message, IConnection connection)
			{
				assertEquals(SERVICE_CLASS, message.serviceClassID);
				processCount++;
			}
		};
		esb.addTrigger(thresholdTrigger3);

		assertTrue(esb.start(null));
		config.setFileConfigs(new ConfigRecord[] { fx });
		file.setConfiguration(config);
	}

	@AfterClass
	public static void tearDown()
	{
		if (airSimulator != null)
			airSimulator.stop();
		deleteFiles("/tmp/c4u/done");
		esb.stop();
	}

	@Test
	public void testProcessingOnce() throws Exception
	{
		processCount = 0;

		airSimulator = new AirSim(esb, 10011, "/Air", numberPlan, "USD");

		// TODO
		final int MAX = 1; // 5;

		for (int i = 0; i < MAX; i++)
		{
			subscriber = (SubscriberEx) airSimulator.addSubscriber(MSISDN, 1, SERVICE_CLASS, 10000, SubscriberState.active);
			TnpThreshold threshold = new TnpThreshold();
			threshold.setServiceClass(subscriber.getServiceClassCurrent());
			threshold.setThresholdID(5);
			threshold.setDirectory("/tmp/c4u");
			threshold.setVersion("3.0");
			threshold.setSenderID(String.format("sdp%s", i));
			threshold.setReceiverID("hxc5");
			threshold.setAccountID(0);
			threshold.setLevel(10);
			threshold.setUpwards(false);
			threshold.setTriggerType(TnpTriggerTypes.TRAFFIC);
			threshold.setAccountGroupID(12);
			boolean ok = airSimulator.addTnpThreshold(threshold);
			assertTrue(ok);
			airSimulator.setBalance(subscriber.getInternationalNumber(), 5);

			Thread.sleep(1500);
		}

		Thread.sleep(10000);

		assertEquals(MAX, processCount);
		assertEquals(MAX, new File("/tmp/c4u/done").listFiles().length);

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
