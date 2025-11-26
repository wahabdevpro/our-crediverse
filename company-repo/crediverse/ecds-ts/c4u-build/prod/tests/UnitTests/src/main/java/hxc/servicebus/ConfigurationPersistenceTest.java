package hxc.servicebus;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.testsuite.RunAllTestsBase;

public class ConfigurationPersistenceTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(ConfigurationPersistenceTest.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	@Test
	public void testConfigurationPersistence() throws Exception
	{
		// Create esb
		IServiceBus esb = ServiceBus.getInstance();
		esb.stop();
		configureLogging(esb);
		
		MySqlConnector.overrideDb(getDatabaseConfigurationMap());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new CtrlConnector());
		esb.start(null);

		// Get MySQL's config
		IConfiguration config = ((IPlugin) esb.getFirstConnector(IDatabase.class)).getConfiguration();
		config.save(null, null);
		config.load(null);

		// Save the esb's config
		IDatabase database = esb.getFirstConnector(IDatabase.class);
		ICtrlConnector control = esb.getFirstConnector(ICtrlConnector.class);
		config = ((IPlugin) esb).getConfiguration();
		try (IDatabaseConnection connection = database.getConnection(""))
		{
			assertTrue(config.save(connection, control));
		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

		// Change config
		// ILocale locale = esb.getLocale();
		// String currency = locale.getCurrencyCode();
		// config = (IConfiguration) locale;
		// Field field = config.getClass().getDeclaredField("currencyCode");
		// field.setAccessible(true);
		// field.set(config, "???");
		// assertEquals(locale.getCurrencyCode(), "???");

		// Re-Load Config from Database
		// config = ((IPlugin) esb).getConfiguration();
		// try (IDatabaseConnection connection = esb.getFirstConnector(IDatabase.class).getConnection(null))
		// {
		// config.load(connection, logger);
		// }
		// assertEquals(locale.getCurrencyCode(), currency);

	}
}
