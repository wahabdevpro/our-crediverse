package hxc.servicebus.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.servicebus.ServiceBus.LocaleConfig;
import hxc.services.IService;
import hxc.services.advancedtransfer.TransferMode;
import hxc.services.logging.LoggerService;
import hxc.services.notification.INotification;
import hxc.services.notification.INotificationText;
import hxc.services.notification.INotifications;
import hxc.services.notification.IPhrase;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notifications;
import hxc.utils.string.StringUtils;

public class NotificationsTest extends RunAllTestsBase
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private static IServiceBus esb;

	private static final int ENGLISH = 1;
	private static final int AFRIKAANS = 2;
	private static final int DEUTCH = 3;
	private static final int NEDERLANDS = 4;

	private static final long serialVersionUID = -595853242961436089L;

	private INotifications notifications;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Setup and tear down
	//
	// /////////////////////////////////

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		esb = ServiceBus.getInstance();
		esb.stop();
		esb.registerService(new LoggerService());
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());

		LocaleConfig locale = (ServiceBus.LocaleConfig) esb.getLocale();
		locale.setLanguage2("afr");
		locale.setLanguage2Name("Afrikaans");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Test
	//
	// /////////////////////////////////

	// Message IDs
    @SuppressWarnings("unused") // this seems quite helpful, though not used YET
	private enum MyNotifications
	{
		Subscribed, Balance, Cancelled, Other
	};

	// @Test
	public void testScalingUtility() throws Exception
	{
		int MAX_COUNT = 100;

		System.out.println("Basic Test (1:1 scalling ...");
		for (int i = 0; i < MAX_COUNT; i++)
		{
			long value = StringUtils.parseScaled(String.valueOf(i), 1L, TransferMode.SCALE_DENOMINATOR);
			String s = StringUtils.formatScaled(value, 1L, TransferMode.SCALE_DENOMINATOR);
			assert (s.equals(String.valueOf(i)));
		}

		System.out.println("Advanced scaling Test ...");
		BigDecimal CF_START = new BigDecimal("0.1");
		BigDecimal CF_STEP = new BigDecimal("0.01");
		BigDecimal CF_END = new BigDecimal("2.0");

		for (BigDecimal bd = CF_START; bd.compareTo(CF_END) <= 0; bd = bd.add(CF_STEP))
		{
			long conversionFactor = StringUtils.parseScaled(bd.toString(), 1L, TransferMode.SCALE_DENOMINATOR);
			for (int i = 0; i < MAX_COUNT; i++)
			{
				long scaled = 0;
				try
				{
					scaled = StringUtils.parseScaled(String.valueOf(i), TransferMode.SCALE_DENOMINATOR, conversionFactor);
				}
				catch (Exception e)
				{
					System.err.printf(">> Conversion Factor: %d Value: %d parseScaled Failed: %s%n", conversionFactor, i, e.getMessage());
					continue;
				}

				// Save scaled
				try
				{
					String converedBack = StringUtils.formatScaled(scaled, TransferMode.SCALE_DENOMINATOR, conversionFactor);
					BigDecimal bdConvertedBack = new BigDecimal(converedBack);
					if (bdConvertedBack.compareTo(new BigDecimal(i)) != 0)
					{
						System.err.printf("scaled: %d converted back to %s%n", i, converedBack);
					}
				}
				catch (Exception e)
				{
					System.err.printf(">> Conversion Factor: %d Scaling: %d formatScaled Failed: %s%n", conversionFactor, scaled, e.getMessage());
				}
			}
		}
	}

	@Test
	public void testScaling2() throws Exception
	{
		String displayValue = "50.0";
		long numerator = 64000; // Operation = "/10";
		long denominator = TransferMode.SCALE_DENOMINATOR;
		long storedValue = StringUtils.parseScaled(displayValue, numerator, denominator);
		String displayValue2 = StringUtils.formatScaled(storedValue, numerator, denominator);
		assertEquals(displayValue, displayValue2);
	}

	@Test
	public void testNotifications() throws Exception
	{
		// Properties Class
		final Object properties = new Object()
		{
            @SuppressWarnings("unused") // this (and parent) is public, it can be used externally
			public String getBalance()
			{
				return "$12.34";
			}

            @SuppressWarnings("unused") // this (and parent) is public, it can be used externally
			public String getServiceName(String languageCode)
			{
				switch (languageCode)
				{
					case "eng":
						return "Midnight Service";

					case "afr":
						return "Middernag Diens";

					default:
						return "Midnight Service";
				}
			}
		};

		final Notifications myNotifications = new Notifications(properties.getClass());

		// Notification Codes
		final int Subscribed = myNotifications.add("You have been Subscribed to the {ServiceName}");
		final int Balance = myNotifications.add("Your {ServiceName} balance is {Balance}");
		final int Cancelled = myNotifications.add("You have cancelled");

		// Create a test Service
		IService myService = new IService()
		{

			@Override
			public void initialise(IServiceBus esb)
			{
				notifications = myNotifications;
			}

			@Override
			public boolean start(String[] args)
			{
				return true;
			}

			@Override
			public void stop()
			{
			}

			@Override
			public IConfiguration getConfiguration()
			{
				return null;
			}

			@Override
			public void setConfiguration(IConfiguration config) throws ValidationException
			{
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

		};

		// Register the test service with the ESB
		esb.registerService(myService);

		// Start the ESB
		esb.start(null);

		// Obtain the notifications
		assertNotNull(notifications);

		// Obtain notification IDs
		int[] notificationIds = notifications.getNotificationIds();
		assertEquals(3, notificationIds.length);
		assertEquals(Cancelled, notificationIds[2]);
		assertEquals(Balance, notificationIds[1]);
		assertEquals(Subscribed, notificationIds[0]);

		// Get Subscribed Notification
		INotification msg = notifications.getNotification(Subscribed);
		assertEquals("You have been Subscribed to the {ServiceName}", msg.getDescription());

		// Get Balance Notification
		msg = notifications.getNotification(Balance);
		assertEquals("Your {ServiceName} balance is {Balance}", msg.getDescription());

		// Get Cancelled Notification
		msg = notifications.getNotification(Cancelled);
		assertEquals("You have cancelled", msg.getDescription());

		// Define Language Specific Subscription notifications
		msg = notifications.getNotification(Subscribed);
		msg.setText(ENGLISH, "You have been Subscribed to the {ServiceName}.");
		msg.setText(AFRIKAANS, "U is ingeskryf vir die {ServiceName}");
		msg.setText(DEUTCH, "German {ServiceName}");
		msg.setText(NEDERLANDS, "Dutch {ServiceName}");
		assertEquals("You have been Subscribed to the {ServiceName}.", msg.getText(ENGLISH));
		assertEquals("U is ingeskryf vir die {ServiceName}", msg.getText(AFRIKAANS));
		assertEquals("German {ServiceName}", msg.getText(DEUTCH));
		assertEquals("Dutch {ServiceName}", msg.getText(NEDERLANDS));

		// Define Language Specific Subscription notifications
		msg = notifications.getNotification(Balance);
		msg.setText(ENGLISH, "Your {ServiceName} balance is {Balance}.");
		msg.setText(AFRIKAANS, "U balaans is {Balance} vir die {ServiceName}.");
		msg.setText(DEUTCH, "German {Balance}");
		msg.setText(NEDERLANDS, "Dutch {Balance}");
		assertEquals("Your {ServiceName} balance is {Balance}.", msg.getText(ENGLISH));
		assertEquals("U balaans is {Balance} vir die {ServiceName}.", msg.getText(AFRIKAANS));
		assertEquals("German {Balance}", msg.getText(DEUTCH));
		assertEquals("Dutch {Balance}", msg.getText(NEDERLANDS));

		// Define Language Specific Subscription notifications
		msg = notifications.getNotification(Cancelled);
		msg.setText(ENGLISH, "You have unscubribed from the {ServiceName}.");
		msg.setText(AFRIKAANS, "U is nie meer ingeskryf vir die {servicename} nie");
		msg.setText(DEUTCH, "German Cancelled");
		msg.setText(NEDERLANDS, "Dutch Cancelled");
		assertEquals("You have unscubribed from the {ServiceName}.", msg.getText(ENGLISH));
		assertEquals("U is nie meer ingeskryf vir die {ServiceName} nie", msg.getText(AFRIKAANS));
		assertEquals("German Cancelled", msg.getText(DEUTCH));
		assertEquals("Dutch Cancelled", msg.getText(NEDERLANDS));

		// Test Variables (Updated)
		Map<String, String> varDetails = notifications.getVariableDetails();
		assertNotNull(varDetails);
		assertEquals(3, varDetails.size());
		assert (varDetails.containsKey("Balance"));
		assert (varDetails.containsKey("ServiceName"));
		assert (varDetails.containsKey("Currency"));

		// Test substituted Text
		INotificationText text = notifications.get(Balance, IPhrase.ENG, esb.getLocale(), properties);
		assertEquals("Your Midnight Service balance is $12.34.", text.getText());
		text = notifications.get(Balance, IPhrase.AFR, esb.getLocale(), properties);
		assertEquals("U balaans is $12.34 vir die Middernag Diens.", text.getText());

		// Test Bad Text Update
		msg = notifications.getNotification(Cancelled);
		boolean caught = false;
		try
		{
			msg.setText(AFRIKAANS, "U is nie meer ingeskryf vir die {servicenaam} nie");
		}
		catch (IllegalArgumentException ex)
		{
			caught = true;
		}
		assertTrue(caught);
		assertEquals("U is nie meer ingeskryf vir die {ServiceName} nie", msg.getText(AFRIKAANS));

		// Test Saving
		IDatabase database = esb.getFirstConnector(IDatabase.class);
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			notifications.save(connection, serialVersionUID);

			msg = notifications.getNotification(Cancelled);
			msg.setText(DEUTCH, "German Kanselliert");

			notifications.load(connection, serialVersionUID);
			assertEquals(3, notificationIds.length);

			msg = notifications.getNotification(Cancelled);
			assertEquals("German Cancelled", msg.getText(DEUTCH));

		}
		catch (Exception ex)
		{
			throw ex;
		}

	}
}
