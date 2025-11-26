package hxc.connectors.ui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import hxc.configuration.IConfiguration;
import hxc.connectors.ui.model.BaseTestConfiguration;
import hxc.servicebus.IServiceBus;

public class UiConnectorIsFitTest
{

	private static BaseTestConfiguration baseConfig;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		baseConfig = new BaseTestConfiguration();

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		baseConfig.tearDown();
		baseConfig = null;
	}

	@Test
	public void testIsFit() throws Exception
	{
		IServiceBus esb = baseConfig.getEsb();

		UiConnector uic = (UiConnector) esb.getFirstConnector(UiConnector.class);
		assertNotNull(uic);

		// All should be OK
		IConfiguration config = uic.getConfiguration();
		baseConfig.updatePortProperty(config, "ServerPort", "10101");
		uic.setConfiguration(config);
		assertTrue(uic.isFit());

		// Break it
		config = uic.getConfiguration();
		baseConfig.updatePortProperty(config, "ServerPort", "3306");
		uic.setConfiguration(config);
		assertFalse(uic.isFit());

		// Now fix
		config = uic.getConfiguration();
		baseConfig.updatePortProperty(config, "ServerPort", "10101");
		uic.setConfiguration(config);
		assertTrue(uic.isFit());
	}

}
