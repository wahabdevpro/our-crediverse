package hxc.connectors.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.air.AirConnector;
import hxc.connectors.ctrl.CtrlConnector;
import hxc.connectors.database.mysql.MySqlConnector;
import hxc.connectors.hsx.HsxConnector;
import hxc.connectors.hux.HuxConnector;
import hxc.connectors.soap.SoapConnector;
import hxc.connectors.ui.UiConnector.UiConnectorConfig;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ServiceBus;
import hxc.services.security.SecurityService;
import hxc.services.transactions.TransactionService;
import hxc.testsuite.RunAllTestsBase;
import hxc.utils.uiconnector.client.UIClient;

public class StartupTest extends RunAllTestsBase
{
	final static Logger logger = LoggerFactory.getLogger(UiMetricsMonitoring.class);

	private static IServiceBus esb;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{

		esb = ServiceBus.getInstance();
		
		configureLogging(esb);

		esb.registerConnector(new SoapConnector()); //HuxConnector depends on SoapConnector
		esb.registerConnector(new HuxConnector());
		esb.registerConnector(new HsxConnector());
		esb.registerConnector(new AirConnector());
		esb.registerConnector(new MySqlConnector());
		esb.registerConnector(new CtrlConnector());
		esb.registerService(new TransactionService());
		esb.registerService(new SecurityService());
		esb.registerConnector(new UiConnector());
		esb.start(null);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		esb.stop();
		esb = null;
	}

	private UIClient createTestClientConnection(int uiConnectorPort) throws Exception
	{
		UIClient uic = new UIClient();

		try
		{
			uic.connect("localhost", uiConnectorPort);
		}
		catch (IOException e1)
		{
			throw e1;
		}
		return uic;
	}

	// Tests that the ESB and the UIConnector startup properly in that the UIConnector waits for the ESB to finish starting.  
	// If ESB::start returns false, the UIConnector will not startup and will block indefinitely in waitForRunning().  
	// This test ensures that the UIConnector starts-up and listens for UI connections. 
	@Test
	public void testSuccessfulStartup() throws Exception
	{
		long timeout = 300L; //5 minutes;
		UiConnector uiConnector = (UiConnector)esb.getFirstConnector(UiConnector.class);
		UiConnectorConfig config = (UiConnectorConfig) uiConnector.getConfiguration();
		int uiConnectorPort = config.getServerPort();
		logger.info("Waiting for ESB to start...");
		esb.waitForRunning(timeout * 1000L);
		logger.info("ESB has started.");
		assertTrue("Service Bus is not running after waiting " + timeout + " seconds." , esb.isRunning().get());		
		UIClient uic = createTestClientConnection(uiConnectorPort);
		assertNotNull("A connection could not be made to the UIConnector on port " + uiConnectorPort, uic);
	}
}
